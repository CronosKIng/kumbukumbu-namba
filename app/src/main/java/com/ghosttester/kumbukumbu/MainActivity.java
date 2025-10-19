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
    private static final String TAG = "MainActivity";
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;
    
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        statusText = findViewById(R.id.statusText);
        
        // Check and request SMS permissions automatically
        checkAndRequestSmsPermissions();
    }

    private void checkAndRequestSmsPermissions() {
        String[] requiredPermissions = {
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS
        };

        boolean allPermissionsGranted = true;
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            // All permissions already granted
            updateStatus("✅ Permissions granted - App is monitoring Mixx payments");
            Toast.makeText(this, "App is ready to monitor Mixx payments", Toast.LENGTH_LONG).show();
        } else {
            // Request permissions
            updateStatus("📱 Requesting SMS permissions...");
            ActivityCompat.requestPermissions(this, requiredPermissions, SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                updateStatus("✅ Permissions granted - Monitoring Mixx payments");
                Toast.makeText(this, "App is now monitoring Mixx payment SMS", Toast.LENGTH_LONG).show();
            } else {
                updateStatus("❌ Permissions denied - App cannot monitor SMS");
                Toast.makeText(this, "Please grant SMS permissions to monitor Mixx payments", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateStatus(String message) {
        if (statusText != null) {
            statusText.setText(message);
        }
    }
}
