package com.callguard.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.*;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/** Privacy-conscious community lookup. It sends only a number explicitly submitted on this screen. */
public class CommunityCallerIdActivity extends Activity {
    // Replace with your HTTPS PHP API base URL after hosting the backend.
    private static final String API_BASE="https://YOUR-DOMAIN.example/callguard-api";
    private static final String API_KEY="REPLACE_WITH_YOUR_API_KEY";
    private int navy=Color.rgb(10,35,70), blue=Color.rgb(21,101,192), gray=Color.rgb(90,100,115);
    private EditText number; private TextView result; private CheckBox consent;
    private TextView tv(String s,int z,int c,boolean b){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setTypeface(Typeface.DEFAULT,b?Typeface.BOLD:Typeface.NORMAL);t.setPadding(0,5,0,8);return t;}
    private Button btn(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setOnClickListener(v->r.run());return b;}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(navy);ScrollView scroll=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(22,22,22,30);root.setBackgroundColor(Color.rgb(246,248,252));scroll.addView(root);setContentView(scroll);
        root.addView(tv("Community Caller ID",27,navy,true));root.addView(tv("Look up a number reported by the CallGuard community, or voluntarily submit a report to help other users identify suspected spam.",15,gray,false));
        number=new EditText(this);number.setHint("Phone number with country code");number.setSingleLine(true);number.setInputType(InputType.TYPE_CLASS_PHONE);root.addView(number);
        result=tv("Community lookup is not connected until you host the included PHP API and set API_BASE to its HTTPS URL.",14,gray,false);root.addView(result);
        root.addView(btn("Look up number",()->lookup()));root.addView(tv("Community privacy",19,navy,true));root.addView(tv("CallGuard will not upload your contacts or call history. Only the number you enter and the report you choose to submit are sent. Reports can be wrong; treat results as community signals, not verified identity.",13,gray,false));
        consent=new CheckBox(this);consent.setText("I agree to submit this number and report category to the CallGuard community database.");root.addView(consent);
        root.addView(btn("Report suspected spam",()->report("spam")));root.addView(btn("Report business / organisation",()->report("business")));root.addView(btn("Report safe / wanted caller",()->report("safe")));
    }
    private String clean(){return number.getText().toString().trim().replaceAll("[^+0-9]","");}
    private void lookup(){String n=clean();if(n.length()<5){result.setText("Enter a valid phone number first.");return;}request("GET","/lookup.php?number="+url(n),null, false);}
    private void report(String category){String n=clean();if(n.length()<5){result.setText("Enter a valid phone number first.");return;}if(!consent.isChecked()){new AlertDialog.Builder(this).setTitle("Consent required").setMessage("Please tick the consent box before submitting a community report.").setPositiveButton("OK",null).show();return;}try{JSONObject j=new JSONObject();j.put("number",n);j.put("category",category);request("POST","/report.php",j.toString(),true);}catch(Exception e){result.setText("Could not prepare report.");}}
    private String url(String s){try{return java.net.URLEncoder.encode(s,"UTF-8");}catch(Exception e){return s;}}
    private void request(String method,String path,String body,boolean report){if(API_BASE.contains("YOUR-DOMAIN")){result.setText("API not configured yet. Host the included backend, then replace API_BASE in CommunityCallerIdActivity.java with your HTTPS API URL.");return;}result.setText("Contacting community service…");new Thread(()->{HttpURLConnection c=null;String out;try{URL u=new URL(API_BASE+path);c=(HttpURLConnection)u.openConnection();c.setRequestMethod(method);c.setConnectTimeout(5000);c.setReadTimeout(5000);c.setRequestProperty("Accept","application/json");c.setRequestProperty("X-CallGuard-Key",API_KEY);if(body!=null){c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=utf-8");try(OutputStream os=c.getOutputStream()){os.write(body.getBytes(StandardCharsets.UTF_8));}}int code=c.getResponseCode();InputStream is=code<400?c.getInputStream():c.getErrorStream();ByteArrayOutputStream buf=new ByteArrayOutputStream();byte[] b=new byte[1024];int k;while((k=is.read(b))>0)buf.write(b,0,k);JSONObject j=new JSONObject(new String(buf.toByteArray(),StandardCharsets.UTF_8));if(code>=400)throw new Exception(j.optString("error","Request failed"));if(report)out="Thank you. Your community report was submitted.";else{String label=j.optString("label","Unknown / not yet reported");int reports=j.optInt("reports",0);out="Community result: "+label+"\nReports: "+reports+"\nConfidence: community-reported, not independently verified.";}}catch(Exception e){out="Community service unavailable: "+e.getMessage();}finally{if(c!=null)c.disconnect();}final String msg=out;runOnUiThread(()->result.setText(msg));}).start();}
}
