package com.callguard.app;

public class CallDecision {
    public final boolean block;
    public final String reason;
    public CallDecision(boolean b,String r){block=b;reason=r;}
}
