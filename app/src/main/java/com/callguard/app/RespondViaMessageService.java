package com.callguard.app;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;

/**
 * Required by Android's default SMS application eligibility checks.
 * Handles the system "respond via message" action used by callers such as
 * the phone app when replying to a call with an SMS.
 */
public class RespondViaMessageService extends Service {
    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (intent != null) {
                Uri data = intent.getData();
                String number = data == null ? null : data.getSchemeSpecificPart();
                if (TextUtils.isEmpty(number)) {
                    number = intent.getStringExtra("address");
                }
                Bundle extras = intent.getExtras();
                CharSequence cs = extras == null ? null : extras.getCharSequence(Intent.EXTRA_TEXT);
                if (TextUtils.isEmpty(cs)) {
                    cs = intent.getCharSequenceExtra("android.intent.extra.TEXT");
                }
                String message = cs == null ? "" : cs.toString();
                if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(message)) {
                    SmsManager.getDefault().sendTextMessage(number, null, message, null, null);
                }
            }
        } catch (Throwable ignored) {
            // The system caller should not be crashed by an SMS send failure.
        } finally {
            stopSelf(startId);
        }
        return START_NOT_STICKY;
    }
}
