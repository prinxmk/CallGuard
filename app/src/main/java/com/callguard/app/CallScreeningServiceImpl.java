package com.callguard.app;

import android.telecom.Call;
import android.telecom.CallScreeningService;

public class CallScreeningServiceImpl extends CallScreeningService {
    @Override public void onScreenCall(Call.Details details) {
        if(details.getCallDirection()!=Call.Details.DIRECTION_INCOMING) return;

        String number = details.getHandle()!=null ? details.getHandle().getSchemeSpecificPart() : "";
        CallDecision d = RuleEngine.decide(this, number);

        if(d.block) {
            new DatabaseHelper(this).logBlocked(NumberUtils.normalize(number), d.reason);
        }

        CallScreeningService.CallResponse response =
            new CallScreeningService.CallResponse.Builder()
                .setDisallowCall(d.block)
                .setRejectCall(d.block)
                .setSkipNotification(d.block)
                .build();

        respondToCall(details,response);
    }
}
