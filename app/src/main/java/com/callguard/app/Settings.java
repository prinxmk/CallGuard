package com.callguard.app;

import android.content.Context;
import android.content.SharedPreferences;

public final class Settings {
    private static final String PREF="callguard";
    private Settings(){}
    public static boolean blockUnknown(Context c){return c.getSharedPreferences(PREF,0).getBoolean("block_unknown",false);}
    public static void setBlockUnknown(Context c,boolean v){c.getSharedPreferences(PREF,0).edit().putBoolean("block_unknown",v).apply();}
    public static boolean blockMessages(Context c){return c.getSharedPreferences(PREF,0).getBoolean("block_messages",false);}
    public static void setBlockMessages(Context c,boolean v){c.getSharedPreferences(PREF,0).edit().putBoolean("block_messages",v).apply();}
}
