package com.callguard.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;

public class SmsComposeActivity extends Activity {
    @Override protected void onCreate(Bundle b){ super.onCreate(b); TextView t=new TextView(this); t.setText("CallGuard Messages\n\nMessage blocking is active. Use the phone's Messages app after returning it as the default SMS app if you need full messaging composition."); t.setTextSize(17); t.setTextColor(Color.DKGRAY); t.setGravity(Gravity.CENTER); t.setPadding(32,32,32,32); setContentView(t); }
}
