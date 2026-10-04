package com.callguard.app;

import android.content.Context;

public final class RuleEngine {
    private RuleEngine() {}

    public static CallDecision decideMessage(Context context, String rawNumber) {
        String number = NumberUtils.normalize(rawNumber);
        if (number.isEmpty()) return new CallDecision(false, "No sender number was supplied");
        try {
            DatabaseHelper db = new DatabaseHelper(context.getApplicationContext());
            for (DatabaseHelper.Rule r : db.getRules("WHITELIST", "")) {
                if (!r.enabled) continue;
                String rule=NumberUtils.normalize(r.value);
                if (("WHITELIST".equals(r.type) || "LEGACY_WHITELIST".equals(r.type)) && rule.equals(number)) return new CallDecision(false,"Whitelisted sender");
                if ("WHITELIST_PREFIX".equals(r.type) && NumberUtils.isPrefixMatch(number,rule)) return new CallDecision(false,"Whitelisted prefix: "+r.value);
            }
            for (DatabaseHelper.Rule r : db.getRules("BLACKLIST", "")) {
                if (!r.enabled) continue;
                String rule=NumberUtils.normalize(r.value);
                if (("EXACT".equals(r.type) || "BLACKLIST".equals(r.type)) && rule.equals(number)) return new CallDecision(true,"Matched exact blacklist number: "+r.value);
                if ("PREFIX".equals(r.type) && NumberUtils.isPrefixMatch(number,rule)) return new CallDecision(true,"Matched blacklist prefix: "+r.value);
            }
            return new CallDecision(false,"No blocking rule matched");
        } catch(Exception e) { return new CallDecision(false,"Message rule check unavailable; message allowed safely"); }
    }

    public static CallDecision decide(Context context, String rawNumber) {
        String number = NumberUtils.normalize(rawNumber);
        if (number.isEmpty()) return new CallDecision(false, "No caller number was supplied");
        try {
            DatabaseHelper db = new DatabaseHelper(context.getApplicationContext());

            for (DatabaseHelper.Rule r : db.getRules("WHITELIST", "")) {
                if (!r.enabled) continue;
                String rule = NumberUtils.normalize(r.value);
                if (("WHITELIST".equals(r.type) || "LEGACY_WHITELIST".equals(r.type)) && rule.equals(number))
                    return new CallDecision(false, "Whitelisted number");
                if ("WHITELIST_PREFIX".equals(r.type) && NumberUtils.isPrefixMatch(number, rule))
                    return new CallDecision(false, "Whitelisted prefix: " + r.value);
            }
            for (DatabaseHelper.Rule r : db.getRules("BLACKLIST", "")) {
                if (!r.enabled) continue;
                String rule = NumberUtils.normalize(r.value);
                if (("EXACT".equals(r.type) || "BLACKLIST".equals(r.type)) && rule.equals(number))
                    return new CallDecision(true, "Matched exact blacklist number: " + r.value);
                if (("PREFIX".equals(r.type)) && NumberUtils.isPrefixMatch(number, rule))
                    return new CallDecision(true, "Matched blacklist prefix: " + r.value);
            }
            if (Settings.blockUnknown(context) && !NumberUtils.isContact(context, number))
                return new CallDecision(true, "Number is not saved in Contacts");
            return new CallDecision(false, "No blocking rule matched");
        } catch (Exception e) {
            // Never crash the telecom process because of a local rule/database problem.
            return new CallDecision(false, "Rule check unavailable; call allowed safely");
        }
    }
}
