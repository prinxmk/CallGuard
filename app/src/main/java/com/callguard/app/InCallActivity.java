package com.callguard.app;

import android.app.*;
import android.os.Bundle;
import android.telecom.Call;
import android.view.View;
import android.widget.*;

public class InCallActivity extends Activity {
    private TextView title;
    @Override protected void onCreate(Bundle b){super.onCreate(b); setContentView(R.layout.activity_in_call); title=findViewById(R.id.callTitle); render();}
    @Override protected void onResume(){super.onResume(); render();}
    private void render(){
        Call c=CallGuardInCallService.getCurrentCall();
        if(c==null){finish();return;}
        String n=c.getDetails()!=null && c.getDetails().getHandle()!=null ? c.getDetails().getHandle().getSchemeSpecificPart() : "Unknown number";
        title.setText(n);
        findViewById(R.id.answer).setVisibility(c.getState()==Call.STATE_RINGING?View.VISIBLE:View.GONE);
        findViewById(R.id.reject).setVisibility(c.getState()==Call.STATE_RINGING?View.VISIBLE:View.GONE);
        findViewById(R.id.hangup).setVisibility(c.getState()!=Call.STATE_RINGING?View.VISIBLE:View.GONE);
        findViewById(R.id.answer).setOnClickListener(v->{c.answer(0); render();});
        findViewById(R.id.reject).setOnClickListener(v->{c.disconnect(); finish();});
        findViewById(R.id.hangup).setOnClickListener(v->{c.disconnect(); finish();});
    }
}
