package com.ghosttester.kumbukumbu;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class SmsService extends Service {
    private static final String TAG = "SmsService";
    private SmsReceiver smsReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SMS Service Created");
        
        // Register SMS receiver
        smsReceiver = new SmsReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(1000);
        
        registerReceiver(smsReceiver, filter);
        Log.d(TAG, "SMS Receiver registered");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
            Log.d(TAG, "SMS Receiver unregistered");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
