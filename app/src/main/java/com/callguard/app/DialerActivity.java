package com.callguard.app;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.view.View;
import android.widget.*;

public class DialerActivity extends Activity {
    private EditText number;
    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_dialer);
        number=findViewById(R.id.number);
        Intent in=getIntent();
        Uri data=in.getData();
        if(data!=null) number.setText(data.getSchemeSpecificPart());
        findViewById(R.id.call).setOnClickListener(v->placeCall());
    }
    private void placeCall(){
        String n=number.getText().toString().trim();
        if(n.isEmpty()){Toast.makeText(this,"Enter a number",Toast.LENGTH_SHORT).show();return;}
        try{
            TelecomManager tm=(TelecomManager)getSystemService(TELECOM_SERVICE);
            tm.placeCall(Uri.parse("tel:"+Uri.encode(n)),null);
        }catch(Exception e){Toast.makeText(this,"Unable to place call: "+e.getMessage(),Toast.LENGTH_LONG).show();}
    }
}
