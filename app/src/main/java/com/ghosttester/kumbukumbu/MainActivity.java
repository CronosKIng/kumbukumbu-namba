package com.ghosttester.kumbukumbu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;
    private static final int ALL_PERMISSIONS_REQUEST_CODE = 101;

    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);

        // Check and request ALL permissions automatically on app start
        checkAndRequestAllPermissions();
    }

    private void checkAndRequestAllPermissions() {
        Log.d(TAG, "🔐 Checking and requesting all permissions...");

        // All required permissions for SMS reading
        String[] requiredPermissions = {
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET
        };

        // For Android 6.0+ we need to request runtime permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean allPermissionsGranted = true;
            
            // Check which permissions are not granted
            for (String permission : requiredPermissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                Log.d(TAG, "📋 Requesting permissions...");
                ActivityCompat.requestPermissions(this, requiredPermissions, ALL_PERMISSIONS_REQUEST_CODE);
            } else {
                Log.d(TAG, "✅ All permissions already granted");
                startSmsService();
                updateStatus("Permissions zimepeanwa. App iko tayari kusoma SMS!");
            }
        } else {
            // For older Android versions, permissions are granted at install time
            Log.d(TAG, "📱 Android version below 6.0, permissions granted at install");
            startSmsService();
            updateStatus("App iko tayari kusoma SMS!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ALL_PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = true;
            
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Log.d(TAG, "✅ All permissions granted by user");
                startSmsService();
                updateStatus("Permissions zimepeanwa. App iko tayari kusoma SMS!");
                Toast.makeText(this, "Permissions zimekubaliwa. App iko tayari!", Toast.LENGTH_LONG).show();
            } else {
                Log.w(TAG, "❌ Some permissions were denied");
                updateStatus("Baadhi ya ruhusa hazijakubaliwa. App haitafanya kazi vizuri.");
                Toast.makeText(this, "Tafadhali ruhusu permissions zote kwenye Settings > Apps > Kumbukumbu > Permissions", Toast.LENGTH_LONG).show();
                
                // Show which permissions were denied
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Log.w(TAG, "Permission denied: " + permissions[i]);
                    }
                }
            }
        }
    }

    private void startSmsService() {
        try {
            Intent serviceIntent = new Intent(this, SmsService.class);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            
            Log.d(TAG, "🚀 SMS Service started successfully");
            updateStatus("Service imeanza. App iko tayari kusoma SMS zote!");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting SMS service: " + e.getMessage());
            updateStatus("Hitilafu kuanzisha service. Tafadhali restart app.");
        }
    }

    private void updateStatus(String message) {
        if (statusText != null) {
            statusText.setText(message);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check permissions when app comes to foreground
        checkAndRequestAllPermissions();
    }
}
