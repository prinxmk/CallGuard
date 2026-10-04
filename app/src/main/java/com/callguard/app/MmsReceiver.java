package com.callguard.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Receives MMS delivery broadcasts while CallGuard is the default SMS app.
 * The receiver deliberately does not attempt to parse/store MMS parts itself;
 * it acknowledges the role-required broadcast and provides a safe extension
 * point for a future MMS conversation UI.
 */
public class MmsReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        if (!"android.provider.Telephony.WAP_PUSH_DELIVER".equals(intent.getAction())) return;
        Bundle extras = intent.getExtras();
        // MMS data is provider-managed on Android. Do not abort this broadcast.
        // Keeping this receiver lightweight prevents the default SMS role from
        // interfering with the phone's MMS delivery pipeline.
    }
}
