package com.nku.netlab.pete.firstwifiscan;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Base64;

import java.util.ArrayList;
import java.util.List;

public class WiFiScanner {
    private static final String TAG = "WiFiScanner";
    private static final String HEAD_LINE = "userid,floorid,timestamp,wifiinfos\n";
    private static final String SENSOR_ID = "wifi";
    private static final int DEFAULT_WIFI_SCAN_INTERVAL = 500;

    private final MainActivity m_mainActivity;
    private final WifiManager m_wifiManager;
    private WiFiScanBroadcastReceiver m_wifiScanReceiver;
    private ArrayList<String> m_wifiScanResultList;
    private SDHandler m_fileHandler;
    private boolean m_wifiState;
    private CountDownTimer m_wifiScanTimer;

    public WiFiScanner(MainActivity activity, WifiManager wfManager) {
        m_mainActivity = activity;
        m_wifiManager = wfManager;
        m_wifiScanReceiver = null;
        m_wifiScanResultList = new ArrayList<>();
        m_fileHandler = new SDHandler();
    }

    private void resetWifiData() {
        synchronized (this) {
            m_wifiScanResultList.clear();
        }
    }
    private void openWiFi() {
        m_wifiState = m_wifiManager.isWifiEnabled();
        if (!m_wifiState) {
            m_wifiManager.setWifiEnabled(true);
        }
    }

    private String analysisResults(List<ScanResult> resultList) {
        if (resultList.size() < 1) {
            return "The Scan Result is empty";
        }
        StringBuilder resultBuilder = new StringBuilder();
        for (int i=0; i < resultList.size(); i++) {
            ScanResult record = resultList.get(i);
            resultBuilder.append(record.BSSID);
            resultBuilder.append(" ");
            resultBuilder.append(String.format("% 4d", record.level));
            resultBuilder.append(" ");
            resultBuilder.append(record.frequency);
            if (i < resultList.size() - 1) {
                resultBuilder.append("\n");
            }
        }
        return resultBuilder.toString();
    }

    private String analysisResultsV2(List<ScanResult> resultList) {
        if (resultList.size() < 1) {
            return "";
        }
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(Base64.encodeToString((Build.BOARD + "-" + Build.MODEL).getBytes(), Base64.DEFAULT).trim());
        resultBuilder.append(",1305,");
        resultBuilder.append(System.currentTimeMillis());
        resultBuilder.append(",");
        for (int i=0; i < resultList.size(); i++) {
            ScanResult record = resultList.get(i);
            resultBuilder.append(record.BSSID + "|" + record.level + "|" + record.frequency);
            if (i < resultList.size() - 1) {
                resultBuilder.append(";");
            }
        }
        resultBuilder.append('\n');
        return resultBuilder.toString();
    }

    class WiFiScanBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> resultList = m_wifiManager.getScanResults();
                m_mainActivity.updateUIWiFi(analysisResults(resultList));
                if (resultList.size() > 1) {
                    m_wifiScanResultList.add(analysisResultsV2(resultList));
                }
                m_wifiScanTimer.start();
            }
        }
    }

    public void startScan() {
        m_mainActivity.updateUIWiFi("");
        resetWifiData();
        openWiFi();
        // Count Down Timer for WiFi Scan
        m_wifiScanTimer = new CountDownTimer(DEFAULT_WIFI_SCAN_INTERVAL, 20) {
            @Override
            public void onTick(long millisUntilFinished) {
                // TODO: There is nothing to do.
            }

            @Override
            public void onFinish() {
                // Restart scanning and wait for results available
                // time for available + count down timer = wifi interval
                m_wifiManager.startScan();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        m_wifiScanReceiver = new WiFiScanBroadcastReceiver();
        Context appContext = m_mainActivity.getApplicationContext();
        appContext.registerReceiver(m_wifiScanReceiver, intentFilter);
        m_wifiManager.startScan();
    }

    public void stopScan() {
        Context appContext = m_mainActivity.getApplicationContext();
        appContext.unregisterReceiver(m_wifiScanReceiver);
        // restore the wifi state
        m_wifiManager.setWifiEnabled(m_wifiState);
    }

    public boolean toExternalStorage() {
        StringBuilder wifiBuilder = new StringBuilder();
        wifiBuilder.append(HEAD_LINE);
        for (String record : m_wifiScanResultList)
            wifiBuilder.append(record);
        return m_fileHandler.doWriteToFile(SENSOR_ID, wifiBuilder.toString());
    }
}
