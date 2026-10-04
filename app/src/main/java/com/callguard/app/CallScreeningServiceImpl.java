package com.callguard.app;

import android.telecom.Call;
import android.telecom.CallScreeningService;

public class CallScreeningServiceImpl extends CallScreeningService {
    @Override public void onScreenCall(Call.Details details) {
        if (details == null || details.getCallDirection() != Call.Details.DIRECTION_INCOMING) return;
        String number = "";
        try {
            if (details.getHandle() != null) number = details.getHandle().getSchemeSpecificPart();
            CallDecision d = RuleEngine.decide(getApplicationContext(), number);
            if (d.block) {
                try { new DatabaseHelper(getApplicationContext()).logBlocked(NumberUtils.normalize(number), d.reason); } catch (Exception ignored) {}
            }
            CallScreeningService.CallResponse response = new CallScreeningService.CallResponse.Builder()
                .setDisallowCall(d.block)
                .setRejectCall(d.block)
                .setSkipNotification(d.block)
                .build();
            respondToCall(details, response);
        } catch (Exception e) {
            // Telecom must always receive a response; never let an exception kill screening.
            try {
                respondToCall(details, new CallScreeningService.CallResponse.Builder().build());
            } catch (Exception ignored) {}
        }
    }
}
