package com.callguard.app;

import android.content.Context;
import java.util.*;

public final class RuleEngine {
    private RuleEngine(){}

    public static CallDecision decide(Context context,String rawNumber) {
        String number=NumberUtils.normalize(rawNumber);
        DatabaseHelper db=new DatabaseHelper(context);

        // Whitelist always wins.
        for(DatabaseHelper.Rule r: db.getRules("WHITELIST","")) {
            if(r.enabled && NumberUtils.normalize(r.value).equals(number))
                return new CallDecision(false,"Whitelisted number");
        }

        for(DatabaseHelper.Rule r: db.getRules("BLACKLIST","")) {
            if(!r.enabled) continue;
            String rule=NumberUtils.normalize(r.value);
            if("EXACT".equals(r.type) && rule.equals(number))
                return new CallDecision(true,"Matched exact blacklist number: "+r.value);
        }

        for(DatabaseHelper.Rule r: db.getRules("BLACKLIST","")) {
            if(!r.enabled) continue;
            if("PREFIX".equals(r.type) && NumberUtils.isPrefixMatch(number,r.value))
                return new CallDecision(true,"Matched blacklist prefix: "+r.value);
        }

        if(Settings.blockUnknown(context) && !NumberUtils.isContact(context,number))
            return new CallDecision(true,"Number is not saved in Contacts");

        return new CallDecision(false,"No blocking rule matched");
    }
}
