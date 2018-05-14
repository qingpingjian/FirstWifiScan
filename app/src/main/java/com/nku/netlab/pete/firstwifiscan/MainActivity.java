package com.nku.netlab.pete.firstwifiscan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private Context m_appContext;
    private WifiManager m_wifiManager;
    private WiFiScanner m_wifiScanner;
    private TextView m_tvWiFi;
    private Button m_btnControl;
    private boolean m_controlFlag;
    private boolean m_saveFlag;
    private int m_isChecked;
    private String[] m_saveItems;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_settings, menu);
//        return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.save_item:
//                Toast.makeText(MainActivity.this, "Hello save Flag", Toast.LENGTH_LONG).show();
                buildSettingAlertDialog().show();
                break;
            case R.id.scan_frequency:
                Toast.makeText(MainActivity.this, "Hello scan frequency", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
        return true;
    }

    private Dialog buildSettingAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.btn_dlg_first_title);
//        builder.setIcon()

        builder.setSingleChoiceItems(m_saveItems, m_isChecked, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_isChecked = which;
            }
        });
        builder.setPositiveButton(R.string.btn_dlg_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_saveFlag = m_isChecked == 0 ? true : false;
            }
        });
        builder.setNegativeButton(R.string.btn_dlg_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_isChecked = m_saveFlag ? 0 : 1;
            }
        });
        return builder.create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_appContext = getApplicationContext();
        m_wifiManager = (WifiManager) m_appContext.getSystemService(WIFI_SERVICE);
        m_wifiScanner = new WiFiScanner(this, m_wifiManager);
        m_tvWiFi = findViewById(R.id.tvWifi);
        m_btnControl = findViewById(R.id.btnControl);
        m_controlFlag = false;
        m_btnControl.setOnClickListener(new OnControlClickListener());
        m_saveFlag = false;
        m_isChecked = 1;
        m_saveItems = getResources().getStringArray(R.array.mulck_save_flag);
    }

    class OnControlClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (m_controlFlag) { // Stop scanning
                m_controlFlag = false;
                m_btnControl.setText(R.string.btn_start);
                m_wifiScanner.stopScan();
                if (m_saveFlag) {
                    SaveFileThread saveFileThread = new SaveFileThread();
                    saveFileThread.start();
                }
            }
            else { // Start to scan
                m_controlFlag = true;
                m_btnControl.setText(R.string.btn_stop);
                m_wifiScanner.startScan();
            }
        }
    }

    public void updateUIWiFi(final String wifiResult) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_tvWiFi.setText(wifiResult);
            }
        });
    }

    public void showToast(final String toastMsg)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    class SaveFileThread extends Thread {
        @Override
        public void run() {
            boolean saveFlag = m_wifiScanner.toExternalStorage();
            if (saveFlag) {
                showToast(getString(R.string.save_file_ok));
            }
            else {
                showToast(getString(R.string.save_file_error));
            }
        }
    }
}
