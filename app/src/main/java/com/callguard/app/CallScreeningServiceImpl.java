package com.callguard.app;

import android.telecom.Call;
import android.telecom.CallScreeningService;

public class CallScreeningServiceImpl extends CallScreeningService {
    @Override public void onScreenCall(Call.Details details) {
        if (details == null) return;
        if (details.getCallDirection() != Call.Details.DIRECTION_INCOMING) return;

        String number = "";
        try {
            if (details.getHandle() != null) number = details.getHandle().getSchemeSpecificPart();
        } catch (Exception ignored) {}

        CallDecision decision;
        try {
            decision = RuleEngine.decide(getApplicationContext(), number);
        } catch (Throwable ignored) {
            decision = new CallDecision(false, "Safe fallback: call allowed");
        }

        boolean block = decision != null && decision.block;
        try {
            CallScreeningService.CallResponse response = new CallScreeningService.CallResponse.Builder()
                    .setDisallowCall(block)
                    .setRejectCall(block)
                    .setSkipNotification(block)
                    .build();
            respondToCall(details, response);
        } catch (Throwable ignored) {
            // Never allow an application exception to escape the telecom callback.
        }

        // Logging is deliberately after the telecom response so it cannot delay/crash screening.
        if (block) {
            try {
                new DatabaseHelper(getApplicationContext()).logBlocked(NumberUtils.normalize(number), decision.reason);
            } catch (Throwable ignored) {}
        }
    }
}
