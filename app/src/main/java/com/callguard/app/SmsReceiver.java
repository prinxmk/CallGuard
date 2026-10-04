package com.callguard.app;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import java.util.HashMap;

public class SmsReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action=intent.getAction();
        if (!Telephony.Sms.Intents.SMS_DELIVER_ACTION.equals(action) && !Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(action)) return;
        if (!SmsRoleHelper.isDefault(context) && Telephony.Sms.Intents.SMS_DELIVER_ACTION.equals(action)) return;
        try {
            Bundle b=intent.getExtras(); if(b==null) return;
            Object[] pdus=(Object[])b.get("pdus"); if(pdus==null) return;
            String format=b.getString("format");
            HashMap<String,StringBuilder> parts=new HashMap<>();
            HashMap<String,Long> times=new HashMap<>();
            for(Object p:pdus){
                SmsMessage m;
                if(android.os.Build.VERSION.SDK_INT>=23) m=SmsMessage.createFromPdu((byte[])p,format); else m=SmsMessage.createFromPdu((byte[])p);
                if(m==null) continue;
                String sender=m.getOriginatingAddress()==null?"":m.getOriginatingAddress();
                String key=sender+"|"+m.getTimestampMillis();
                if(!parts.containsKey(key)) parts.put(key,new StringBuilder());
                parts.get(key).append(m.getMessageBody()==null?"":m.getMessageBody());
                times.put(key,m.getTimestampMillis());
            }
            DatabaseHelper db=new DatabaseHelper(context);
            for(String key:parts.keySet()){
                String sender=key.split("\\|",2)[0]; String body=parts.get(key).toString();
                CallDecision d=RuleEngine.decideMessage(context,sender);
                if(d.block && Settings.blockMessages(context) && SmsRoleHelper.isDefault(context)){
                    db.logBlockedMessage(NumberUtils.normalize(sender),body,d.reason);
                    NotificationHelper.showBlocked(context,"Message blocked","Blocked SMS from "+sender+" • "+d.reason, 3000+(int)(System.currentTimeMillis()%1000));
                    if(isOrderedBroadcast()) abortBroadcast();
                } else if(Telephony.Sms.Intents.SMS_DELIVER_ACTION.equals(action) && SmsRoleHelper.isDefault(context)) {
                    ContentValues v=new ContentValues(); v.put("address",sender); v.put("body",body); v.put("date",times.get(key)); v.put("read",0); v.put("seen",0); v.put("type",1);
                    try { context.getContentResolver().insert(Uri.parse("content://sms/inbox"),v); } catch(Exception ignored) {}
                }
            }
        } catch(Throwable ignored) {}
    }
}
