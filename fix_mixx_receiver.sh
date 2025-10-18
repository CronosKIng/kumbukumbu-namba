#!/bin/bash

echo "🔧 Inaboresha SmsReceiver kwa Mixx by YAS..."

# Badilisha SmsReceiver.java
cat > app/src/main/java/com/ghosttester/kumbukumbu/SmsReceiver.java << 'JAVAEOF'
package com.ghosttester.kumbukumbu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import okhttp3.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "MixxSMSReceiver";
    
    // ✅ SERVER URL SAHIHI
    private static final String SERVER_URL = "https://ghosttester.pythonanywhere.com/api/mixx-sms-payment";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    String messageBody = sms.getMessageBody();
                    String sender = sms.getOriginatingAddress();

                    Log.d(TAG, "SMS Received - Sender: " + sender);
                    Log.d(TAG, "SMS Content: " + messageBody);

                    // Check if this is Mixx by Yas SMS
                    if (isMixxPaymentSms(messageBody)) {
                        Log.d(TAG, "✅ Mixx SMS Found! Processing...");
                        processMixxPayment(messageBody, sender);
                    } else {
                        Log.d(TAG, "❌ Not a Mixx SMS - Skipping");
                    }
                }
            }
        }
    }

    private boolean isMixxPaymentSms(String messageBody) {
        if (messageBody == null) return false;
        
        // Check kama ni SMS ya Mixx by YAS
        boolean isMixx = messageBody.contains("TSh") && 
                         messageBody.contains("Kumbukumbu") && 
                         (messageBody.contains("Umepokea") || messageBody.contains("Umetuma"));
        
        Log.d(TAG, "Is Mixx SMS: " + isMixx);
        return isMixx;
    }

    private void processMixxPayment(String messageBody, String sender) {
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            
            Log.d(TAG, "📱 Processing Mixx SMS from: " + sender);
            Log.d(TAG, "📝 SMS Content: " + messageBody);
            Log.d(TAG, "⏰ Timestamp: " + timestamp);

            // Tuma SMS yote kwenye server - app.py itachambua
            sendToServer(messageBody, sender, timestamp);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing Mixx SMS: " + e.getMessage());
        }
    }

    private void sendToServer(String smsContent, String senderNumber, String timestamp) {
        OkHttpClient client = new OkHttpClient();

        try {
            // ✅ JSON STRUCTURE SAHIHI - kama inavyotarajiwa na server
            String json = String.format("{\"sms_content\": \"%s\", \"sender_number\": \"%s\", \"timestamp\": \"%s\"}",
                    smsContent.replace("\"", "\\\""), // Escape quotes
                    senderNumber,
                    timestamp);

            Log.d(TAG, "📤 Sending to server: " + SERVER_URL);
            Log.d(TAG, "📦 JSON Payload: " + json);

            RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "❌ Failed to send to server: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Payment data sent successfully to server");
                        Log.d(TAG, "📨 Server Response: " + responseBody);
                    } else {
                        Log.e(TAG, "❌ Server error: " + response.code() + " - " + responseBody);
                    }
                    response.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating request: " + e.getMessage());
        }
    }
}
JAVAEOF

echo "✅ SmsReceiver.java imeboreshwa kwa Mixx by YAS!"
echo "📱 Sasa itasoma SMS za Mixx tu na kuzituma kwenye server yako"
echo "🔗 Server URL: https://ghosttester.pythonanywhere.com/api/mixx-sms-payment"

