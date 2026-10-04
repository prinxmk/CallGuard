package com.callguard.app;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import java.util.ArrayList;

public final class CallLogHelper {
    private CallLogHelper() {}
    public static ArrayList<Entry> getRecent(Context c, String search) {
        ArrayList<Entry> out=new ArrayList<>();
        String q=search==null?"":search.trim();
        String sel=q.isEmpty()?null:CallLog.Calls.NUMBER+" LIKE ?";
        String[] args=q.isEmpty()?null:new String[]{"%"+q+"%"};
        Cursor cur=null;
        try {
            cur=c.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                    new String[]{CallLog.Calls.NUMBER,CallLog.Calls.TYPE,CallLog.Calls.DATE,CallLog.Calls.DURATION,CallLog.Calls.CACHED_NAME},
                    sel,args,CallLog.Calls.DATE+" DESC LIMIT 100");
            if(cur!=null) while(cur.moveToNext()) out.add(new Entry(cur.getString(0),cur.getInt(1),cur.getLong(2),cur.getLong(3),cur.getString(4)));
        } catch(Exception ignored) {} finally { if(cur!=null) cur.close(); }
        return out;
    }
    public static class Entry { public final String number,name; public final int type; public final long date,duration; Entry(String n,int t,long d,long du,String name){number=n;type=t;date=d;duration=du;this.name=name;} }
}
