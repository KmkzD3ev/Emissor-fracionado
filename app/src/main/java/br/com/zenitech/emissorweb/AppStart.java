package br.com.zenitech.emissorweb;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

public class AppStart extends Activity implements OnClickListener {

    public static int nPrintWidth = 384;
    public static int nPrintHeight = 300;
    public static int nPrintCount = 1;

    private RadioButton
            radio58, radio80,
            radioPrintCount1, radioPrintCount10, radioPrintCount100, radioPrintCount1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_private);

		/* 启动WIFI */
        @SuppressLint("WifiManagerLeak") WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        switch (wifiManager.getWifiState()) {
            case WifiManager.WIFI_STATE_DISABLED:
                wifiManager.setWifiEnabled(true);
                break;
            default:
                break;
        }

		/* 启动蓝牙 */
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null != adapter) {
            if (!adapter.isEnabled()) {
                if (!adapter.enable()) {
                    finish();
                    return;
                }
            }
        }

        radio58 = (RadioButton) findViewById(R.id.radioButtonTicket58);
        radio80 = (RadioButton) findViewById(R.id.radioButtonTicket80);
        radioPrintCount1 = (RadioButton) findViewById(R.id.radioButtonPrintCount1);
        radioPrintCount10 = (RadioButton) findViewById(R.id.radioButtonPrintCount10);
        radioPrintCount100 = (RadioButton) findViewById(R.id.radioButtonPrintCount100);
        radioPrintCount1000 = (RadioButton) findViewById(R.id.radioButtonPrintCount1000);

        findViewById(R.id.btnTestBT).setOnClickListener(this);
        findViewById(R.id.btnTestBLE).setOnClickListener(this);
        findViewById(R.id.btnTestUSB).setOnClickListener(this);
        findViewById(R.id.btnTestNET).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (radio58.isChecked())
            nPrintWidth = 384;
        else if (radio80.isChecked())
            nPrintWidth = 576;

        if (radioPrintCount1.isChecked())
            nPrintCount = 1;
        else if (radioPrintCount10.isChecked())
            nPrintCount = 10;
        else if (radioPrintCount100.isChecked())
            nPrintCount = 100;
        else if (radioPrintCount1000.isChecked())
            nPrintCount = 1000;

        switch (v.getId()) {
            case R.id.btnTestBT: {
                Intent intent = new Intent(AppStart.this, SearchBTActivity.class);
                startActivity(intent);
                break;
            }
/*
            case R.id.btnTestBLE: {
                Intent intent = new Intent(AppStart.this, SearchBLEActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.btnTestUSB: {
                Intent intent = new Intent(AppStart.this, ConnectUSBActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.btnTestNET: {
                Intent intent = new Intent(AppStart.this, ConnectIPActivity.class);
                startActivity(intent);
                break;
            }
            */
        }
    }

}
