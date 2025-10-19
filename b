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
    
    // BADILISHA HII KUWA URL YAKO HALISI
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
                    
                    // Check if this is Mixx SMS
                    if (isMixxPaymentSMS(sender, messageBody)) {
                        Log.d(TAG, "💰 MIXX PAYMENT SMS DETECTED");
                        sendToServer(sender, messageBody);
                    } else {
                        Log.d(TAG, "❌ NOT Mixx SMS - Ignoring");
                    }
                }
            }
        }
    }
    
    private boolean isMixxPaymentSMS(String sender, String messageBody) {
        // Check for Mixx SMS patterns
        String lowerBody = messageBody.toLowerCase();
        boolean isMixx = sender.contains("M-Pesa") || 
                         sender.contains("MIX") ||
                         sender.contains("MPESA") ||
                         lowerBody.contains("umetuma") ||
                         lowerBody.contains("umepokea") ||
                         lowerBody.contains("tsh") ||
                         lowerBody.contains("kumbukumbu") ||
                         lowerBody.contains("mpesa") ||
                         lowerBody.contains("muamala") ||
                         lowerBody.contains("salio") ||
                         lowerBody.contains("tigo") ||
                         lowerBody.contains("airtel") ||
                         lowerBody.contains("halopesa") ||
                         lowerBody.contains("mixx");
        
        Log.d(TAG, "🔍 Checking if Mixx SMS: " + isMixx);
        return isMixx;
    }
    
    private void sendToServer(final String sender, final String messageBody) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "🌐 CONNECTING TO SERVER: " + SERVER_URL);
                    
                    URL url = new URL(SERVER_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);
                    
                    // Create JSON data
                    String jsonData = "{" +
                            "\"sms_content\": \"" + messageBody.replace("\"", "\\\"").replace("\n", " ") + "\"," +
                            "\"sender_number\": \"" + sender + "\"," +
                            "\"timestamp\": \"" + System.currentTimeMillis() + "\"" +
                            "}";
                    
                    Log.d(TAG, "📤 Sending JSON: " + jsonData);
                    
                    // Send data
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonData.getBytes());
                    os.flush();
                    os.close();
                    
                    // Get response
                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "📥 Server Response Code: " + responseCode);
                    
                    BufferedReader br;
                    if (responseCode == 200) {
                        br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    } else {
                        br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    }
                    
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    
                    Log.d(TAG, "📄 Server Response: " + response.toString());
                    
                    if (responseCode == 200) {
                        Log.d(TAG, "✅ SMS DATA SENT SUCCESSFULLY TO SERVER");
                    } else {
                        Log.d(TAG, "❌ FAILED TO SEND SMS DATA. Response: " + responseCode);
                    }
                    
                    conn.disconnect();
                    
                } catch (Exception e) {
                    Log.e(TAG, "💥 ERROR SENDING TO SERVER: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
