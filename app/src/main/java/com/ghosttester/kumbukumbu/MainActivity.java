package com.ghosttester.kumbukumbu;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create UI programmatically without layouts
        TextView textView = new TextView(this);
        textView.setText("Mixx SMS Receiver - Ready");
        setContentView(textView);
    }
}
