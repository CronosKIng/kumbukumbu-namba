package com.ghosttester.kumbukumbu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MixxSMS", "SMS Receiver Active - Ready for Mixx by YAS");
    }
}
