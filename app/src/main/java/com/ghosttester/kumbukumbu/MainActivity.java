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
            Manifest.permission.READ_SMS
        };

        boolean allPermissionsGranted = true;
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, requiredPermissions, SMS_PERMISSION_REQUEST_CODE);
        } else {
            updateStatus("Permissions zimepeanwa. App iko tayari!");
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
                updateStatus("Permissions zimepeanwa. App iko tayari!");
            } else {
                updateStatus("Ruhusa hazijapewa. App haitafanya kazi.");
                Toast.makeText(this, "Tafadhali ruhusu SMS permissions kwenye settings", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateStatus(String message) {
        if (statusText != null) {
            statusText.setText(message);
        }
    }
}
