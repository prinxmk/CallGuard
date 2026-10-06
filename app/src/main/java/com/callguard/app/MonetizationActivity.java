package com.callguard.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

/** Monetization hub. Provider IDs/products must be configured before live payments or ads can run. */
public class MonetizationActivity extends Activity {
    private int navy=Color.rgb(10,35,70), blue=Color.rgb(21,101,192), gray=Color.rgb(90,100,115);
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL);t.setPadding(0,5,0,8);return t;}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(18,16,18,16);GradientDrawable d=new GradientDrawable();d.setColor(Color.WHITE);d.setCornerRadius(24);c.setBackground(d);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,14);c.setLayoutParams(p);return c;}
    private Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setOnClickListener(v->r.run());return b;}
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(navy);ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(18,18,18,28);root.setBackgroundColor(Color.rgb(246,248,252));sc.addView(root);setContentView(sc);
        root.addView(text("CallGuard Plus",28,navy,true));root.addView(text("A sustainable way to support independent development while keeping essential protection accessible.",15,gray,false));
        LinearLayout free=card();free.addView(text("FREE • Core protection",19,navy,true));free.addView(text("Call and SMS rules, blacklist/whitelist, local history and basic notifications remain the foundation of CallGuard.",14,gray,false));root.addView(free);
        LinearLayout pro=card();pro.addView(text("PREMIUM • Planned benefits",19,navy,true));pro.addView(text("Ad-free use, advanced statistics, rule backup/restore, custom schedules and enhanced caller-ID insights are the proposed paid tier.",14,gray,false));pro.addView(button("Premium plans",()->new AlertDialog.Builder(this).setTitle("Premium subscriptions").setMessage("Billing is not activated yet. Before accepting payments, configure a Google Play Billing product ID and verify purchases. This screen does not charge you or unlock paid features yet.").setPositiveButton("OK",null).show()));root.addView(pro);
        LinearLayout reward=card();reward.addView(text("Rewarded advertisements",19,navy,true));reward.addView(text("Users should choose to watch an ad for an optional temporary perk. Ads are not loaded and no reward is granted until a supported ad SDK and your app/unit IDs are configured.",14,gray,false));reward.addView(button("Rewarded ad setup",()->new AlertDialog.Builder(this).setTitle("Rewarded ads not configured").setMessage("Choose an ad provider (for example Unity LevelPlay or AppLovin MAX), create an app and rewarded placement in its dashboard, then add the SDK and IDs. Never reward users merely for tapping this button.").setPositiveButton("OK",null).show()));root.addView(reward);
        LinearLayout affiliate=card();affiliate.addView(text("Partner offers",19,navy,true));affiliate.addView(text("Affiliate links can earn commission when users voluntarily visit or buy from a partner. Add only relevant, trustworthy security products and disclose that links may be affiliate links.",14,gray,false));affiliate.addView(button("Example partner offer",()->new AlertDialog.Builder(this).setTitle("Partner link not configured").setMessage("Add your approved partner URL in MonetizationActivity after joining an affiliate programme. This demo intentionally does not send users to an invented or unapproved offer.").setPositiveButton("OK",null).show()));root.addView(affiliate);
        root.addView(text("Payments, ad rewards and partner tracking are deliberately not simulated. Configure the relevant provider accounts and IDs before launch.",12,gray,false));
    }
}
