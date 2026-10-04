package com.callguard.app;

import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;

public final class SmsRoleHelper {
    private SmsRoleHelper() {}
    public static boolean isDefault(Context c) {
        try { return c.getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(c)); } catch(Exception e) { return false; }
    }
    public static void request(Context c, int requestCode, android.app.Activity activity) {
        if (Build.VERSION.SDK_INT >= 29) {
            RoleManager rm=activity.getSystemService(RoleManager.class);
            if(rm!=null && rm.isRoleAvailable(RoleManager.ROLE_SMS)) activity.startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_SMS),requestCode);
        } else {
            try {
                Intent i=new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                i.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,c.getPackageName());
                activity.startActivityForResult(i,requestCode);
            } catch(Exception e) { activity.startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)); }
        }
    }
}
