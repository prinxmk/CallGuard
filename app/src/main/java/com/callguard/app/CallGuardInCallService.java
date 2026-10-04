package com.callguard.app;

import android.telecom.Call;
import android.telecom.InCallService;

public class CallGuardInCallService extends InCallService {
    private static Call currentCall;
    public static Call getCurrentCall(){ return currentCall; }
    @Override public void onCallAdded(Call call){
        super.onCallAdded(call);
        currentCall = call;
        // Do not launch a custom activity from the telecom callback. On Android 9/OEM
        // dialers this can race the system incoming-call UI and terminate the app.
    }
    @Override public void onCallRemoved(Call call){
        super.onCallRemoved(call);
        if(currentCall==call) currentCall=null;
    }
}
