package com.ghosttester.kumbukumbu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);

        // Check SMS permissions
        if (hasSmsPermission()) {
            startSmsService();
            statusText.setText(getString(R.string.status_working));
        } else {
            requestSmsPermission();
        }
    }

    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_SMS},
                SMS_PERMISSION_CODE);
    }

    private void startSmsService() {
        // Service itaanza automatically
        Toast.makeText(this, "Mixx SMS Service Started", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSmsService();
                statusText.setText(getString(R.string.status_working));
            } else {
                statusText.setText(getString(R.string.status_no_permission));
            }
        }
    }
}
