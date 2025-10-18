package com.ghosttester.kumbukumbu;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create UI programmatically without any resources
        TextView textView = new TextView(this);
        textView.setText("Mixx SMS Receiver\n\nReady to scan Mixx by YAS payments\n\nServer: ghosttester.pythonanywhere.com");
        textView.setTextSize(16);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(50, 50, 50, 50);
        setContentView(textView);
    }
}
