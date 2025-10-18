#!/bin/bash

# Backup the original file
cp app/src/main/java/com/ghosttester/kumbukumbu/SmsReceiver.java app/src/main/java/com/ghosttester/kumbukumbu/SmsReceiver.java.backup

# Create the fixed version
cat > app/src/main/java/com/ghosttester/kumbukumbu/SmsReceiver.java << 'FIXED'
package com.ghosttester.kumbukumbu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "MixxSMSReceiver";
    private static final String SERVER_URL = "https://GhostTester.pythonanywhere.com/api/mixx-sms-payment";

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

                    // Check if this is Mixx by Yas SMS
                    if (isMixxPaymentSms(messageBody)) {
                        Log.d(TAG, "✅ Mixx SMS Found! Processing...");
                        processMixxPayment(messageBody, sender);
                    }
                }
            }
        }
    }

    private boolean isMixxPaymentSms(String messageBody) {
        if (messageBody == null) return false;
        return messageBody.contains("TSh") &&
               messageBody.contains("Kumbukumbu") &&
               (messageBody.contains("Umepokea") || messageBody.contains("Umetuma"));
    }

    private void processMixxPayment(String messageBody, String sender) {
        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            Log.d(TAG, "📱 Processing Mixx SMS from: " + sender);
            Log.d(TAG, "📝 SMS Content: " + messageBody);

            // Tuma SMS yote kwenye server
            sendToServer(messageBody, sender, timestamp);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing Mixx SMS: " + e.getMessage());
        }
    }

    private void sendToServer(String smsContent, String senderNumber, String timestamp) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Use JSONObject for proper JSON formatting
            JSONObject jsonData = new JSONObject();
            jsonData.put("sms_content", smsContent);
            jsonData.put("sender_number", senderNumber);
            jsonData.put("timestamp", timestamp);

            String json = jsonData.toString();
            
            Log.d(TAG, "📤 Sending to server: " + SERVER_URL);
            Log.d(TAG, "📦 JSON Data: " + json);

            RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "MixxSMS-Android-App")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "❌ Network Error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "📨 Server Response - Status: " + response.code() + ", Body: " + responseBody);
                    
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Payment data sent successfully to server!");
                    } else {
                        Log.e(TAG, "❌ Server returned error: " + response.code());
                    }
                    response.close();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating JSON request: " + e.getMessage());
        }
    }
}
FIXED

echo "✅ SmsReceiver.java has been fixed!"
echo "🔧 Changes made:"
echo "   - Added JSONObject for proper JSON formatting"
echo "   - Fixed string escaping issues"
echo "   - Added better logging"
echo "   - Added User-Agent header"
