package com.callguard.app;

import android.content.Context;
import android.provider.ContactsContract;
import java.util.*;

public final class NumberUtils {
    private NumberUtils() {}

    public static String normalize(String input) {
        if (input == null) return "";
        String s=input.trim().replaceAll("[^0-9+]","");
        if (s.startsWith("+")) s=s.substring(1);
        if (s.startsWith("234") && s.length() >= 13) s="0"+s.substring(3);
        return s;
    }

    public static boolean isPrefixMatch(String number, String prefix) {
        String n=normalize(number), p=normalize(prefix);
        return !n.isEmpty() && !p.isEmpty() && n.startsWith(p);
    }

    public static boolean isContact(Context c,String number) {
        if (number == null || number.isEmpty()) return false;
        try {
            android.net.Uri uri=android.net.Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, android.net.Uri.encode(number));
            android.database.Cursor cur=c.getContentResolver().query(uri,new String[]{ContactsContract.PhoneLookup._ID},null,null,null);
            boolean found=cur!=null && cur.moveToFirst();
            if(cur!=null) cur.close();
            return found;
        } catch(Exception e) { return false; }
    }
}
