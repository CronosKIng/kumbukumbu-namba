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
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;
    private static final int ALL_PERMISSIONS_REQUEST_CODE = 101;
    private static final String SERVER_URL = "https://GhostTester.pythonanywhere.com/api/mixx-sms-payment";

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
                updateStatus("Permissions zimepeanwa. Inasoma SMS za MIXX BY YAS...");
                // Start reading existing MIXX SMS from inbox
                readAllMixxSmsFromInbox();
            }
        } else {
            // For older Android versions, permissions are granted at install time
            Log.d(TAG, "📱 Android version below 6.0, permissions granted at install");
            startSmsService();
            updateStatus("Inasoma SMS za MIXX BY YAS...");
            // Start reading existing MIXX SMS from inbox
            readAllMixxSmsFromInbox();
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
                updateStatus("Permissions zimekubaliwa. Inasoma SMS za MIXX BY YAS...");
                Toast.makeText(this, "Permissions zimekubaliwa. Inasoma SMS za MIXX...", Toast.LENGTH_LONG).show();
                // Start reading existing MIXX SMS from inbox
                readAllMixxSmsFromInbox();
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
            updateStatus("Service imeanza. Inatafuta SMS za MIXX BY YAS...");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting SMS service: " + e.getMessage());
            updateStatus("Hitilafu kuanzisha service. Tafadhali restart app.");
        }
    }

    private void readAllMixxSmsFromInbox() {
        Log.d(TAG, "📂 READING ALL MIXX BY YAS SMS FROM INBOX");
        updateStatus("Inasoma inbox ya SMS...");
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Read SMS from inbox
                    android.net.Uri uri = android.net.Uri.parse("content://sms/inbox");
                    android.database.Cursor cursor = getContentResolver().query(
                            uri,
                            null,
                            null,
                            null,
                            "date DESC"  // Sort by date, newest first
                    );

                    if (cursor != null && cursor.moveToFirst()) {
                        int count = 0;
                        int mixxCount = 0;
                        
                        do {
                            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                            String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                            long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                            
                            count++;
                            
                            // Check if it's MIXX BY YAS SMS
                            if (body != null && isMixxSms(body)) {
                                mixxCount++;
                                Log.d(TAG, "✅ FOUND MIXX SMS #" + mixxCount + " - From: " + address);
                                
                                // Update status on UI thread
                                final int currentCount = mixxCount;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateStatus("Imepata SMS " + currentCount + " za MIXX BY YAS...");
                                    }
                                });
                                
                                // Send to server
                                sendSmsToServer(address, body, date);
                                
                                // Small delay to avoid overwhelming the server
                                Thread.sleep(500);
                            }
                            
                        } while (cursor.moveToNext());
                        
                        cursor.close();
                        
                        Log.d(TAG, "📊 SCAN COMPLETE: Found " + mixxCount + " MIXX SMS out of " + count + " total SMS");
                        updateStatus("Imesoma SMS " + mixxCount + " za MIXX BY YAS kutoka kwenye inbox!");
                    } else {
                        Log.d(TAG, "❌ No SMS found in inbox or error reading inbox");
                        updateStatus("Hakuna SMS kwenye inbox au hitilafu kusoma");
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "❌ ERROR reading SMS inbox: " + e.getMessage());
                    updateStatus("Hitilafu kusoma inbox ya SMS");
                }
            }
        }).start();
    }

    private boolean isMixxSms(String body) {
        if (body == null) return false;
        
        String cleanBody = body.toUpperCase();
        return cleanBody.contains("MIXX") || 
               cleanBody.contains("BY YAS") ||
               cleanBody.contains("YAS") ||
               (cleanBody.contains("TSH") && cleanBody.contains("KUMBUKUMBU"));
    }

    private void sendSmsToServer(final String sender, final String messageBody, final long timestamp) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(SERVER_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);
                    conn.setDoOutput(true);

                    JSONObject jsonData = new JSONObject();
                    jsonData.put("sms_content", messageBody);
                    jsonData.put("sender_number", sender);
                    jsonData.put("timestamp", timestamp);
                    jsonData.put("from_inbox", true); // Mark as from inbox

                    String jsonString = jsonData.toString();

                    Log.d(TAG, "📤 Sending inbox SMS to server: " + sender);

                    OutputStream os = conn.getOutputStream();
                    os.write(jsonString.getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    
                    if (responseCode == 200) {
                        Log.d(TAG, "✅ Inbox SMS sent successfully: " + sender);
                    } else {
                        Log.e(TAG, "❌ Failed to send inbox SMS: " + responseCode);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "🚨 ERROR sending inbox SMS: " + e.getMessage());
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
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
