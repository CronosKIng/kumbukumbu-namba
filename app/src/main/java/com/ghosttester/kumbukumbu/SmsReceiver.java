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
        Log.d(TAG, "SMS RECEIVED - STARTING PROCESS");

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    String messageBody = smsMessage.getMessageBody();

                    Log.d(TAG, "SMS From: " + sender);
                    Log.d(TAG, "SMS Body: " + messageBody);

                    if (isMixxPaymentSMS(sender, messageBody)) {
                        Log.d(TAG, "MIXX PAYMENT SMS DETECTED");
                        sendToServer(sender, messageBody);
                    }
                }
            }
        }
    }

    private boolean isMixxPaymentSMS(String sender, String messageBody) {
        String lowerBody = messageBody.toLowerCase();
        return sender.contains("M-Pesa") ||
               sender.contains("MIX") ||
               sender.contains("MPESA") ||
               lowerBody.contains("umetuma") ||
               lowerBody.contains("umepokea") ||
               lowerBody.contains("tsh") ||
               lowerBody.contains("kumbukumbu") ||
               lowerBody.contains("mpesa") ||
               lowerBody.contains("muamala") ||
               lowerBody.contains("salio");
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

                    String jsonData = "{" +
                            "\"sms_content\": \"" + messageBody.replace("\"", "\\\"").replace("\n", " ") + "\"," +
                            "\"sender_number\": \"" + sender + "\"," +
                            "\"timestamp\": \"" + System.currentTimeMillis() + "\"" +
                            "}";

                    OutputStream os = conn.getOutputStream();
                    os.write(jsonData.getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "Server Response Code: " + responseCode);

                    conn.disconnect();

                } catch (Exception e) {
                    Log.e(TAG, "ERROR: " + e.getMessage());
                }
            }
        }).start();
    }
}
