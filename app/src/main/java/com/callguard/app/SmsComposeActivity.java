package com.callguard.app;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

/**
 * Lightweight SMS composer used by the SENDTO role intent. It is intentionally
 * dependency-free so it remains compatible with Android 9/API 28.
 */
public class SmsComposeActivity extends Activity {
    private EditText number;
    private EditText body;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        String prefillNumber = "";
        String prefillBody = "";
        Intent in = getIntent();
        if (in != null) {
            Uri data = in.getData();
            if (data != null && data.getSchemeSpecificPart() != null) prefillNumber = data.getSchemeSpecificPart();
            if (in.getStringExtra("sms_body") != null) prefillBody = in.getStringExtra("sms_body");
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 28, 28, 28);

        TextView title = new TextView(this);
        title.setText("CallGuard Messages");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView info = new TextView(this);
        info.setText("Compose and send SMS while CallGuard is your default SMS app.");
        info.setTextSize(14);
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));

        number = new EditText(this);
        number.setHint("Recipient phone number");
        number.setInputType(InputType.TYPE_CLASS_PHONE);
        number.setText(prefillNumber);
        root.addView(number, new LinearLayout.LayoutParams(-1, -2));

        body = new EditText(this);
        body.setHint("Message");
        body.setGravity(Gravity.TOP);
        body.setMinLines(5);
        body.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        body.setText(prefillBody);
        root.addView(body, new LinearLayout.LayoutParams(-1, 0, 1f));

        Button send = new Button(this);
        send.setText("Send SMS");
        send.setOnClickListener(v -> sendSms());
        root.addView(send, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
    }

    private void sendSms() {
        String to = number.getText().toString().trim();
        String message = body.getText().toString().trim();
        if (to.length() == 0) { number.setError("Enter a phone number"); return; }
        if (message.length() == 0) { body.setError("Enter a message"); return; }
        try {
            SmsManager.getDefault().sendTextMessage(to, null, message, null, null);
            Toast.makeText(this, "SMS sent", Toast.LENGTH_SHORT).show();
            body.setText("");
        } catch (SecurityException e) {
            Toast.makeText(this, "SMS permission is required.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Unable to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
