package com.callguard.app;

import android.telecom.Call;
import android.telecom.CallScreeningService;

public class CallScreeningServiceImpl extends CallScreeningService {
    @Override public void onScreenCall(Call.Details details) {
        if (details == null) return;

        // IMPORTANT: getCallDirection() was introduced in API 29.
        // This application supports Android 9 (API 28), so calling it
        // unconditionally causes NoSuchMethodError exactly when a call arrives.
        // CallScreeningService is invoked for calls that the screening service
        // is responsible for; do not use the API-29-only direction method here.
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
