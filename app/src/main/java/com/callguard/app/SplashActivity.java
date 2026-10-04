package com.callguard.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;

public class SplashActivity extends Activity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(10,35,70));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER); root.setPadding(32,32,32,32);
        root.setBackgroundColor(Color.rgb(10,35,70));
        ImageView icon = new ImageView(this); icon.setImageResource(R.drawable.ic_launcher);
        int s=(int)(getResources().getDisplayMetrics().density*104); root.addView(icon,new LinearLayout.LayoutParams(s,s));
        TextView name=new TextView(this); name.setText("CallGuard"); name.setTextColor(Color.WHITE); name.setTextSize(34); name.setTypeface(Typeface.DEFAULT,Typeface.BOLD); name.setGravity(Gravity.CENTER); name.setPadding(0,18,0,4); root.addView(name);
        TextView ver=new TextView(this); ver.setText("Version 1.3.0"); ver.setTextColor(Color.rgb(190,215,245)); ver.setTextSize(16); ver.setGravity(Gravity.CENTER); root.addView(ver);
        TextView by=new TextView(this); by.setText("By Prince Gold Michael .A."); by.setTextColor(Color.rgb(190,215,245)); by.setTextSize(15); by.setGravity(Gravity.CENTER); by.setPadding(0,12,0,0); root.addView(by);
        TextView mycontact=new TextView(this); mycontact.setText("WhatsApp +234-803-766-3916"); mycontact.setTextColor(Color.rgb(190,215,245)); mycontact.setTextSize(15); mycontact.setGravity(Gravity.CENTER); mycontact.setPadding(0,12,0,0); root.addView(mycontact);
        setContentView(root);
        AnimationSet a=new AnimationSet(true); a.addAnimation(new AlphaAnimation(0f,1f)); a.addAnimation(new ScaleAnimation(.82f,1f,.82f,1f,1,0.5f,1,0.5f)); a.setDuration(800); icon.startAnimation(a); name.startAnimation(a);
        new Handler().postDelayed(()->{startActivity(new Intent(this,MainActivity.class)); finish(); overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);},1800);
    }
}
