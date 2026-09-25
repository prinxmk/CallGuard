package com.callguard.app;

import android.app.*;
import android.app.role.RoleManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
    private DatabaseHelper db;
    private TextView status, counts;
    private Switch unknown;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        db=new DatabaseHelper(this);
        status=findViewById(R.id.tvStatus);
        counts=findViewById(R.id.tvCounts);
        unknown=findViewById(R.id.switchUnknown);

        unknown.setChecked(com.callguard.app.Settings.blockUnknown(this));
        unknown.setOnCheckedChangeListener((button, checked)->{
            if (checked && Build.VERSION.SDK_INT >= 23 &&
                    checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, 501);
            }
            com.callguard.app.Settings.setBlockUnknown(this,checked);
        });

        findViewById(R.id.btnEnable).setOnClickListener(v->requestScreeningRole());
        findViewById(R.id.btnAddBlacklist).setOnClickListener(v->showAddDialog("BLACKLIST"));
        findViewById(R.id.btnBlacklist).setOnClickListener(v->showRules("BLACKLIST"));
        findViewById(R.id.btnWhitelist).setOnClickListener(v->showRules("WHITELIST"));
        findViewById(R.id.btnHistory).setOnClickListener(v->showHistory());
        findViewById(R.id.btnTest).setOnClickListener(v->showTestDialog());
        findViewById(R.id.btnExport).setOnClickListener(v->exportRules());
        findViewById(R.id.btnImport).setOnClickListener(v->importRules());

        refresh();
    }

    @Override protected void onResume(){super.onResume(); if(db!=null) refresh();}

    private void refresh(){
        boolean active=false;
        if(Build.VERSION.SDK_INT>=29){
            RoleManager rm=getSystemService(RoleManager.class);
            active=rm!=null && rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
        }
        status.setText(active ? "● Protection is ON" : "● Protection is OFF — enable Call Screening");
        status.setTextColor(getResources().getColor(active?R.color.green:R.color.red));
        counts.setText("Blacklist: "+db.countRules("BLACKLIST")+
                "    Whitelist: "+db.countRules("WHITELIST")+
                "    Blocked: "+db.countBlocked());
    }

    private void requestScreeningRole(){
        if(Build.VERSION.SDK_INT>=29){
            RoleManager rm=getSystemService(RoleManager.class);
            if(rm!=null && rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)){
                startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING),100);
                return;
            }
        }
        new AlertDialog.Builder(this).setTitle("Call screening unavailable")
            .setMessage("This Android version/device does not expose the Call Screening role.")
            .setPositiveButton("OK",null).show();
    }

    private void showAddDialog(String type){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(30,10,30,10);
        Spinner kind=new Spinner(this);
        String[] kinds={"PREFIX","EXACT"};
        kind.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,kinds));
        EditText input=new EditText(this); input.setHint("e.g. 0700 or 08012345678"); input.setInputType(2);
        box.addView(kind); box.addView(input);
        new AlertDialog.Builder(this).setTitle("Add Blacklist Rule").setView(box)
            .setPositiveButton("Save",(d,w)->{
                String value=NumberUtils.normalize(input.getText().toString());
                String k=(String)kind.getSelectedItem();
                if(value.length()==0){toast("Enter a number or prefix");return;}
                if(db.addRule(value,"PREFIX".equals(k)?"PREFIX":"EXACT")) toast("Rule saved"); else toast("Rule already exists");
                refresh();
            }).setNegativeButton("Cancel",null).show();
    }

    private void showRules(String type){
        final ArrayList<DatabaseHelper.Rule> rules=db.getRules(type,"");
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(20,10,20,10);
        EditText search=new EditText(this); search.setHint("Search"); box.addView(search);
        ListView list=new ListView(this); box.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        Dialog dialog=new AlertDialog.Builder(this).setTitle(type.equals("BLACKLIST")?"Blacklist":"Whitelist").setView(box).create();

        final Runnable[] reload=new Runnable[1];
        reload[0]=()->{
            ArrayList<DatabaseHelper.Rule> data=db.getRules(type,search.getText().toString().trim());
            ArrayAdapter<String> a=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_2,android.R.id.text1){
                @Override public View getView(int p,View cv,android.view.ViewGroup parent){
                    View v=super.getView(p,cv,parent);
                    TextView t=v.findViewById(android.R.id.text1);
                    TextView s=v.findViewById(android.R.id.text2);
                    DatabaseHelper.Rule r=data.get(p);
                    t.setText(r.value+"  ["+r.type+"]");
                    s.setText(r.enabled?"Enabled":"Disabled");
                    return v;
                }
            };
            list.setAdapter(a);
            list.setOnItemClickListener((p,v,pos,id)->{
                DatabaseHelper.Rule r=data.get(pos);
                new AlertDialog.Builder(this).setTitle(r.value)
                    .setItems(new String[]{r.enabled?"Disable":"Enable","Delete","Add to Whitelist"},(di,which)->{
                        if(which==0) db.setRuleEnabled(r.id,!r.enabled);
                        else if(which==1) db.deleteRule(r.id);
                        else { db.addRule(r.value,"WHITELIST"); }
                        reload[0].run(); refresh();
                    }).show();
            });
        };
        search.addTextChangedListener(new android.text.TextWatcher(){
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){reload.run();}
            public void afterTextChanged(android.text.Editable e){}
        });
        reload[0].run();
        dialog.show();
    }

    private void showHistory(){
        ArrayList<DatabaseHelper.BlockedCall> data=db.getBlockedCalls("");
        String[] rows=new String[data.size()];
        for(int i=0;i<data.size();i++){
            DatabaseHelper.BlockedCall x=data.get(i);
            rows[i]=x.number+"\n"+x.reason+"\n"+new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(new Date(x.timestamp));
        }
        new AlertDialog.Builder(this).setTitle("Blocked Calls ("+data.size()+")")
            .setItems(rows,null).setPositiveButton("Close",null).show();
    }

    private void showTestDialog(){
        EditText input=new EditText(this); input.setHint("Enter number to test"); input.setInputType(2);
        new AlertDialog.Builder(this).setTitle("Test a Number").setView(input)
            .setPositiveButton("Test",(d,w)->{
                String n=NumberUtils.normalize(input.getText().toString());
                CallDecision x=RuleEngine.decide(this,n);
                new AlertDialog.Builder(this).setTitle(x.block?"BLOCKED":"ALLOWED")
                    .setMessage("Number: "+n+"\n\nReason: "+x.reason)
                    .setPositiveButton("OK",null).show();
            }).setNegativeButton("Cancel",null).show();
    }

    private void exportRules(){
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("text/csv");
        i.putExtra(Intent.EXTRA_TITLE,"callguard_rules.csv");
        startActivityForResult(i,701);
    }

    private void importRules(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("text/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i,702);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode!=RESULT_OK || data==null) return;
        if(requestCode==100){ refresh(); return; }

        if(requestCode==701){
            try(OutputStream out=getContentResolver().openOutputStream(data.getData());
                PrintWriter pw=new PrintWriter(out)){
                pw.println("type,value,enabled");
                for(DatabaseHelper.Rule r:db.getAllRules())
                    pw.println(r.type+","+r.value+","+(r.enabled?1:0));
                toast("Rules exported");
            }catch(Exception e){toast("Export failed: "+e.getMessage());}
        } else if(requestCode==702){
            int added=0;
            try(BufferedReader br=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(data.getData())))){
                String line; boolean first=true;
                while((line=br.readLine())!=null){
                    if(first){first=false;continue;}
                    String[] a=line.split(",");
                    if(a.length>=2){
                        String type=a[0].trim();
                        String value=NumberUtils.normalize(a[1]);
                        if((type.equals("PREFIX")||type.equals("EXACT")||type.equals("WHITELIST")) &&
                                !value.isEmpty() && db.addRule(value,type)) added++;
                    }
                }
                toast("Imported "+added+" new rules");
                refresh();
            }catch(Exception e){toast("Import failed: "+e.getMessage());}
        }
    }

    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
