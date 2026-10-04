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

        findViewById(R.id.btnEnable).setOnClickListener(v->requestScreeningAccess());
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

    private boolean isCallGuardActive(){
        TelecomManager tm=(TelecomManager)getSystemService(TELECOM_SERVICE);
        if(Build.VERSION.SDK_INT>=29){
            RoleManager rm=getSystemService(RoleManager.class);
            return rm!=null && rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
        }
        return tm!=null && getPackageName().equals(tm.getDefaultDialerPackage());
    }

    private void refresh(){
        boolean active=isCallGuardActive();
        String message;
        if(Build.VERSION.SDK_INT>=29)
            message=active ? "● Protection is ON" : "● Protection is OFF — enable CallGuard screening";
        else
            message=active ? "● Protection is ON — Android 9 mode" : "● Protection is OFF — make CallGuard the default Phone app";
        status.setText(message);
        status.setTextColor(getResources().getColor(active?R.color.green:R.color.red));
        counts.setText("Blacklist: "+db.countRules("BLACKLIST")+
                "    Whitelist: "+db.countRules("WHITELIST")+
                "    Blocked: "+db.countBlocked());
    }

    private void requestScreeningAccess(){
        if(Build.VERSION.SDK_INT>=29){
            RoleManager rm=getSystemService(RoleManager.class);
            if(rm!=null && rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)){
                startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING),100);
                return;
            }
            showDiagnostics();
            return;
        }

        // Android 9 has CallScreeningService (API 24+) but does not expose
        // RoleManager/ROLE_CALL_SCREENING (introduced in API 29). Do not
        // silently replace the user's Phone app. Show the exact diagnostic
        // state and available legacy path instead.
        showDiagnostics();
    }

    private void openPhoneSettings(){
        try{ startActivity(new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)); }
        catch(Exception e){ startActivity(new Intent(Settings.ACTION_SETTINGS)); }
    }

    private void showDiagnostics(){
        TelecomManager tm=(TelecomManager)getSystemService(TELECOM_SERVICE);
        String defaultDialer=tm!=null ? tm.getDefaultDialerPackage() : null;
        boolean isDefaultDialer=getPackageName().equals(defaultDialer);
        boolean screeningServiceDeclared=false;
        try{
            android.content.Intent probe=new android.content.Intent("android.telecom.CallScreeningService");
            probe.setPackage(getPackageName());
            screeningServiceDeclared=getPackageManager().queryIntentServices(probe,0).size()>0;
        }catch(Exception ignored){}

        StringBuilder b=new StringBuilder();
        b.append("Android version: ").append(Build.VERSION.RELEASE).append("\n");
        b.append("API level: ").append(Build.VERSION.SDK_INT).append("\n\n");
        b.append("CallScreeningService declared: ").append(screeningServiceDeclared?"YES":"NO").append("\n");
        b.append("Call screening role API: ").append(Build.VERSION.SDK_INT>=29?"AVAILABLE":"NOT AVAILABLE on Android 9").append("\n");
        b.append("Default Phone package: ").append(defaultDialer==null?"Unknown":defaultDialer).append("\n");
        b.append("CallGuard is default Phone: ").append(isDefaultDialer?"YES":"NO").append("\n\n");

        if(Build.VERSION.SDK_INT<29){
            b.append("Android 9 does not expose the public RoleManager call-screening selection API.\n\n");
            b.append("CallGuard's screening service is installed, but this diagnostic cannot claim that XOS has selected it as the active third-party screening provider.\n\n");
            b.append("Your phone currently uses the system Phone app. We will not replace it automatically.\n\n");
            b.append("Next test: place an incoming call after adding a test prefix. If this diagnostic records a screening callback, we can confirm the service is active on this XOS build.");
        }else{
            b.append(isCallGuardActive()?"CallGuard currently holds the screening role.":"CallGuard does not currently hold the screening role.");
        }

        new AlertDialog.Builder(this).setTitle("CallGuard Diagnostics")
            .setMessage(b.toString())
            .setPositiveButton("OK",null)
            .setNeutralButton("Default Phone Settings",(d,w)->openPhoneSettings())
            .show();
    }

    private void showAddDialog(String type){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(30,10,30,10);
        Spinner kind=new Spinner(this);
        String[] kinds=type.equals("WHITELIST")?new String[]{"PREFIX","EXACT"}:new String[]{"PREFIX","EXACT"};
        kind.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,kinds));
        EditText input=new EditText(this); input.setHint("e.g. 0803 or 08031234567"); input.setInputType(2);
        box.addView(kind); box.addView(input);
        String title=type.equals("WHITELIST")?"Add Whitelist Rule":"Add Blacklist Rule";
        new AlertDialog.Builder(this).setTitle(title).setView(box)
            .setPositiveButton("Save",(d,w)->{
                String value=NumberUtils.normalize(input.getText().toString());
                String k=(String)kind.getSelectedItem();
                if(value.length()==0){toast("Enter a number or prefix");return;}
                String dbType=type.equals("WHITELIST")?("PREFIX".equals(k)?"WHITELIST_PREFIX":"WHITELIST"):("PREFIX".equals(k)?"PREFIX":"EXACT");
                if(db.addRule(value,dbType)) toast("Rule saved"); else toast("Rule already exists");
                refresh();
            }).setNegativeButton("Cancel",null).show();
    }

    private void showRules(String type){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(20,10,20,10);
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL);
        EditText search=new EditText(this); search.setHint("Search number or prefix"); search.setSingleLine(true); search.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        actions.addView(search,new LinearLayout.LayoutParams(0,-2,1));
        Button add=new Button(this); add.setText("Add"); actions.addView(add,new LinearLayout.LayoutParams(-2,-2));
        box.addView(actions);
        TextView empty=new TextView(this); empty.setText("No entries found."); empty.setGravity(Gravity.CENTER); empty.setPadding(10,30,10,30); empty.setVisibility(View.GONE); box.addView(empty);
        ListView list=new ListView(this);
        int listHeight=(int)(getResources().getDisplayMetrics().density*390);
        box.addView(list,new LinearLayout.LayoutParams(-1,listHeight));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(type.equals("BLACKLIST")?"Blacklist":"Whitelist").setView(box).setNegativeButton("Close",null).create();
        add.setOnClickListener(v->showAddDialog(type));

        final Runnable[] reload=new Runnable[1];
        reload[0]=()->{
            ArrayList<DatabaseHelper.Rule> data=db.getRules(type,search.getText().toString().trim());
            empty.setVisibility(data.isEmpty()?View.VISIBLE:View.GONE);
            list.setVisibility(data.isEmpty()?View.GONE:View.VISIBLE);
            BaseAdapter adapter=new BaseAdapter(){
                @Override public int getCount(){return data.size();}
                @Override public Object getItem(int position){return data.get(position);}
                @Override public long getItemId(int position){return data.get(position).id;}
                @Override public View getView(int position,View convertView,android.view.ViewGroup parent){
                    DatabaseHelper.Rule r=data.get(position);
                    LinearLayout row=new LinearLayout(MainActivity.this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(10,12,4,12);
                    LinearLayout info=new LinearLayout(MainActivity.this); info.setOrientation(LinearLayout.VERTICAL);
                    TextView value=new TextView(MainActivity.this); value.setText(r.value); value.setTextSize(17); value.setTextColor(getResources().getColor(R.color.primary)); value.setTypeface(null,android.graphics.Typeface.BOLD);
                    TextView meta=new TextView(MainActivity.this); meta.setText(ruleLabel(r)+" • "+(r.enabled?"Enabled":"Disabled"));
                    info.addView(value); info.addView(meta); row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
                    Button toggle=new Button(MainActivity.this); toggle.setText(r.enabled?"Disable":"Enable");
                    toggle.setOnClickListener(v->{db.setRuleEnabled(r.id,!r.enabled); reload[0].run(); refresh();});
                    row.addView(toggle,new LinearLayout.LayoutParams(-2,-2));
                    Button del=new Button(MainActivity.this); del.setText("Delete");
                    del.setOnClickListener(v->new AlertDialog.Builder(MainActivity.this).setTitle("Delete rule?").setMessage(r.value+"\n\nThis cannot be undone.").setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{db.deleteRule(r.id); reload[0].run(); refresh();}).show());
                    row.addView(del,new LinearLayout.LayoutParams(-2,-2));
                    row.setOnClickListener(v->showRuleActions(r,type,reload[0]));
                    return row;
                }
            };
            list.setAdapter(adapter);
        };
        search.addTextChangedListener(new android.text.TextWatcher(){
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){reload[0].run();}
            public void afterTextChanged(android.text.Editable e){}
        });
        dialog.setOnShowListener(d->reload[0].run());
        dialog.show();
    }

    private void showRuleActions(DatabaseHelper.Rule r,String listType,Runnable reload){
        ArrayList<String> options=new ArrayList<>();
        options.add(r.enabled?"Disable":"Enable");
        options.add("Delete");
        if(!r.type.startsWith("WHITELIST")) options.add("Add to Whitelist");
        new AlertDialog.Builder(this).setTitle(r.value).setMessage(ruleLabel(r)).setItems(options.toArray(new String[0]),(di,which)->{
            if(which==0) db.setRuleEnabled(r.id,!r.enabled);
            else if(which==1) db.deleteRule(r.id);
            else db.addRule(r.value, "PREFIX".equals(r.type)?"WHITELIST_PREFIX":"WHITELIST");
            reload.run(); refresh();
        }).show();
    }

    private String ruleLabel(DatabaseHelper.Rule r){
        if("PREFIX".equals(r.type)||"WHITELIST_PREFIX".equals(r.type)) return "Prefix rule";
        return "Exact number";
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
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT); i.setType("text/csv"); i.putExtra(Intent.EXTRA_TITLE,"callguard_rules.csv"); startActivityForResult(i,701);
    }
    private void importRules(){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("text/*"); i.addCategory(Intent.CATEGORY_OPENABLE); startActivityForResult(i,702);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==100 || requestCode==110){refresh(); return;}
        if(resultCode!=RESULT_OK || data==null) return;
        if(requestCode==701){
            try(OutputStream out=getContentResolver().openOutputStream(data.getData()); PrintWriter pw=new PrintWriter(out)){
                pw.println("type,value,enabled");
                for(DatabaseHelper.Rule r:db.getAllRules()) pw.println(r.type+","+r.value+","+(r.enabled?1:0));
                toast("Rules exported");
            }catch(Exception e){toast("Export failed: "+e.getMessage());}
        }else if(requestCode==702){
            int added=0;
            try(BufferedReader br=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(data.getData())))){
                String line; boolean first=true;
                while((line=br.readLine())!=null){
                    if(first){first=false;continue;}
                    String[] a=line.split(",");
                    if(a.length>=2){
                        String t=a[0].trim(), value=NumberUtils.normalize(a[1]);
                        if((t.equals("PREFIX")||t.equals("EXACT")||t.equals("WHITELIST")||t.equals("WHITELIST_PREFIX"))&&!value.isEmpty()&&db.addRule(value,t)) added++;
                    }
                }
                toast("Imported "+added+" new rules"); refresh();
            }catch(Exception e){toast("Import failed: "+e.getMessage());}
        }
    }
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
