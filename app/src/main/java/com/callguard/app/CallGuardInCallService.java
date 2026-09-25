package com.callguard.app;

import android.content.Intent;
import android.net.Uri;
import android.telecom.Call;
import android.telecom.InCallService;

public class CallGuardInCallService extends InCallService {
    private static Call currentCall;

    public static Call getCurrentCall(){ return currentCall; }

    @Override public void onCallAdded(Call call){
        super.onCallAdded(call);
        currentCall=call;
        Intent i=new Intent(this, InCallActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    @Override public void onCallRemoved(Call call){
        super.onCallRemoved(call);
        if(currentCall==call) currentCall=null;
    }
}
