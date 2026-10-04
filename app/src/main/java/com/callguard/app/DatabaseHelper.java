package com.callguard.app;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "callguard.db";
    private static final int DB_VERSION = 2;

    public DatabaseHelper(Context c) { super(c, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE rules (id INTEGER PRIMARY KEY AUTOINCREMENT, value TEXT NOT NULL, type TEXT NOT NULL, enabled INTEGER NOT NULL DEFAULT 1, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE blocked_calls (id INTEGER PRIMARY KEY AUTOINCREMENT, number TEXT, reason TEXT, timestamp INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_rules_value ON rules(value)");
        db.execSQL("CREATE INDEX idx_rules_type ON rules(type)");
        db.execSQL("CREATE UNIQUE INDEX idx_rules_value_type ON rules(value,type)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.beginTransaction();
            try {
                db.execSQL("CREATE TABLE rules_new (id INTEGER PRIMARY KEY AUTOINCREMENT, value TEXT NOT NULL, type TEXT NOT NULL, enabled INTEGER NOT NULL DEFAULT 1, created_at INTEGER NOT NULL)");
                db.execSQL("INSERT OR IGNORE INTO rules_new(id,value,type,enabled,created_at) SELECT id,value,type,enabled,created_at FROM rules");
                db.execSQL("DROP TABLE rules");
                db.execSQL("ALTER TABLE rules_new RENAME TO rules");
                db.execSQL("CREATE INDEX idx_rules_value ON rules(value)");
                db.execSQL("CREATE INDEX idx_rules_type ON rules(type)");
                db.execSQL("CREATE UNIQUE INDEX idx_rules_value_type ON rules(value,type)");
                db.setTransactionSuccessful();
            } finally { db.endTransaction(); }
        }
    }

    public boolean addRule(String value, String type) {
        SQLiteDatabase db=getWritableDatabase();
        ContentValues v=new ContentValues();
        v.put("value", value); v.put("type", type); v.put("enabled",1); v.put("created_at",System.currentTimeMillis());
        return db.insertWithOnConflict("rules",null,v,SQLiteDatabase.CONFLICT_IGNORE)!=-1;
    }

    public boolean deleteRule(long id) { return getWritableDatabase().delete("rules","id=?",new String[]{String.valueOf(id)})>0; }

    public boolean setRuleEnabled(long id, boolean enabled) {
        ContentValues v=new ContentValues(); v.put("enabled",enabled?1:0);
        return getWritableDatabase().update("rules",v,"id=?",new String[]{String.valueOf(id)})>0;
    }

    public ArrayList<Rule> getRules(String type, String search) {
        ArrayList<Rule> out=new ArrayList<>();
        String q=search==null?"":search.trim();
        String normalized=NumberUtils.normalize(q);
        String local234=normalized;
        if(local234.startsWith("234") && local234.length()>3) local234="0"+local234.substring(3);
        String where; String[] args;
        if ("BLACKLIST".equals(type)) {
            where="type IN (?,?)";
            if(q.isEmpty()) args=new String[]{"PREFIX","EXACT"};
            else { where += " AND (value LIKE ? OR value LIKE ? OR value LIKE ?)"; args=new String[]{"PREFIX","EXACT","%"+q+"%","%"+normalized+"%","%"+local234+"%"}; }
        } else if ("WHITELIST".equals(type)) {
            where="type IN (?,?)";
            if(q.isEmpty()) args=new String[]{"WHITELIST","WHITELIST_PREFIX"};
            else { where += " AND (value LIKE ? OR value LIKE ? OR value LIKE ?)"; args=new String[]{"WHITELIST","WHITELIST_PREFIX","%"+q+"%","%"+normalized+"%","%"+local234+"%"}; }
        } else {
            where="type=?";
            if(q.isEmpty()) args=new String[]{type};
            else { where += " AND (value LIKE ? OR value LIKE ? OR value LIKE ?)"; args=new String[]{type,"%"+q+"%","%"+normalized+"%","%"+local234+"%"}; }
        }
        Cursor c=getReadableDatabase().query("rules",null,where,args,null,null,"id DESC");
        while(c.moveToNext()) out.add(new Rule(c.getLong(c.getColumnIndexOrThrow("id")),c.getString(c.getColumnIndexOrThrow("value")),c.getString(c.getColumnIndexOrThrow("type")),c.getInt(c.getColumnIndexOrThrow("enabled"))==1));
        c.close(); return out;
    }

    public ArrayList<Rule> getAllRules() {
        ArrayList<Rule> out=new ArrayList<>();
        Cursor c=getReadableDatabase().query("rules",null,null,null,null,null,"id ASC");
        while(c.moveToNext()) out.add(new Rule(c.getLong(0),c.getString(1),c.getString(2),c.getInt(3)==1));
        c.close(); return out;
    }

    public void logBlocked(String number,String reason) {
        ContentValues v=new ContentValues(); v.put("number",number); v.put("reason",reason); v.put("timestamp",System.currentTimeMillis());
        getWritableDatabase().insert("blocked_calls",null,v);
    }

    public ArrayList<BlockedCall> getBlockedCalls(String search) {
        ArrayList<BlockedCall> out=new ArrayList<>();
        Cursor c=getReadableDatabase().query("blocked_calls",null,"number LIKE ? OR reason LIKE ?",new String[]{"%"+search+"%","%"+search+"%"},null,null,"timestamp DESC");
        while(c.moveToNext()) out.add(new BlockedCall(c.getLong(0),c.getString(1),c.getString(2),c.getLong(3)));
        c.close(); return out;
    }

    public int countRules(String type) {
        Cursor c;
        if ("BLACKLIST".equals(type)) {
            // Blacklist entries are stored as PREFIX or EXACT.
            c=getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM rules WHERE type IN (?,?)",
                new String[]{"PREFIX","EXACT"});
        } else if ("WHITELIST".equals(type)) {
            c=getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM rules WHERE type IN (?,?)",
                new String[]{"WHITELIST","WHITELIST_PREFIX"});
        } else {
            c=getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM rules WHERE type=?",
                new String[]{type});
        }
        c.moveToFirst(); int n=c.getInt(0); c.close(); return n;
    }

    public int countBlocked() {
        Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM blocked_calls",null);
        c.moveToFirst(); int n=c.getInt(0); c.close(); return n;
    }

    public static class Rule {
        public long id; public String value,type; public boolean enabled;
        public Rule(long i,String v,String t,boolean e){id=i;value=v;type=t;enabled=e;}
    }
    public static class BlockedCall {
        public long id; public String number,reason; public long timestamp;
        public BlockedCall(long i,String n,String r,long t){id=i;number=n;reason=r;timestamp=t;}
    }
}
