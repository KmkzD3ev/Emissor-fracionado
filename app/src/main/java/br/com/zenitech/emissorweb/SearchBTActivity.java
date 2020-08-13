package br.com.zenitech.emissorweb;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lvrenyang.io.BTPrinting;
import com.lvrenyang.io.IOCallBack;
import com.lvrenyang.io.Label;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchBTActivity extends Activity implements OnClickListener,
        IOCallBack {

    private LinearLayout linearlayoutdevices;
    private ProgressBar progressBarSearchStatus;

    private BroadcastReceiver broadcastReceiver = null;
    private IntentFilter intentFilter = null;

    Button btnSearch, btnDisconnect, btnPrint;
    SearchBTActivity mActivity;

    ExecutorService es = Executors.newScheduledThreadPool(30);
    Label mLabel = new Label();
    BTPrinting mBt = new BTPrinting();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchbt);

        mActivity = this;

        progressBarSearchStatus = findViewById(R.id.progressBarSearchStatus);
        linearlayoutdevices = findViewById(R.id.linearlayoutdevices);

        btnSearch = findViewById(R.id.buttonSearch);
        btnDisconnect = findViewById(R.id.buttonDisconnect);
        btnPrint = findViewById(R.id.buttonPrint);
        btnSearch.setOnClickListener(this);
        btnDisconnect.setOnClickListener(this);
        btnPrint.setOnClickListener(this);
        btnSearch.setEnabled(true);
        btnDisconnect.setEnabled(false);
        btnPrint.setEnabled(false);

        mLabel.Set(mBt);
        mBt.SetCallBack(this);

        initBroadcast();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uninitBroadcast();
        btnDisconnect.performClick();
    }

    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        switch (arg0.getId()) {
            case R.id.buttonSearch: {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (null == adapter) {
                    finish();
                    break;
                }

                if (!adapter.isEnabled()) {
                    if (adapter.enable()) {
                        while (!adapter.isEnabled()) ;
                    } else {
                        finish();
                        break;
                    }
                }

                adapter.cancelDiscovery();
                linearlayoutdevices.removeAllViews();
                adapter.startDiscovery();
                break;
            }

            case R.id.buttonDisconnect:
                es.submit(new TaskClose(mBt));
                break;

            case R.id.buttonPrint:
                btnPrint.setEnabled(false);
                es.submit(new TaskPrint(mLabel));
                break;
        }
    }

    private void initBroadcast() {
        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                String action = intent.getAction();
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    if (device == null)
                        return;
                    final String address = device.getAddress();
                    String name = device.getName();
                    if (name == null)
                        name = "BT";
                    else if (name.equals(address))
                        name = "BT";
                    Button button = new Button(context);
                    button.setText(String.format("%s: %s", name, address));

                    for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
                        Button btn = (Button) linearlayoutdevices.getChildAt(i);
                        if (btn.getText().equals(button.getText())) {
                            return;
                        }
                    }

                    button.setGravity(android.view.Gravity.CENTER_VERTICAL | Gravity.LEFT);
                    button.setOnClickListener(arg0 -> {
                        // TODO Auto-generated method stub
                        Toast.makeText(mActivity, "Connecting...",
                                Toast.LENGTH_SHORT).show();
                        btnSearch.setEnabled(false);
                        linearlayoutdevices.setEnabled(false);
                        for (int i = 0; i < linearlayoutdevices
                                .getChildCount(); ++i) {
                            Button btn = (Button) linearlayoutdevices
                                    .getChildAt(i);
                            btn.setEnabled(false);
                        }
                        btnDisconnect.setEnabled(false);
                        btnPrint.setEnabled(false);
                        es.submit(new TaskOpen(mBt, address, mActivity));
                    });
                    button.getBackground().setAlpha(100);
                    linearlayoutdevices.addView(button);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED
                        .equals(action)) {
                    progressBarSearchStatus.setIndeterminate(true);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                        .equals(action)) {
                    progressBarSearchStatus.setIndeterminate(false);
                }

            }

        };
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void uninitBroadcast() {
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
    }

    public class TaskOpen implements Runnable {
        BTPrinting bt;
        String address;
        Context context;

        public TaskOpen(BTPrinting bt, String address, Context context) {
            this.bt = bt;
            this.address = address;
            this.context = context;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            bt.Open(address, context);
        }
    }

    public class TaskPrint implements Runnable {
        Label label;

        public TaskPrint(Label label) {
            this.label = label;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub

            final boolean bPrintResult = Prints.PrintTicket(getApplicationContext(), label, AppStart.nPrintWidth, AppStart.nPrintHeight, AppStart.nPrintCount, null);
            final boolean bIsOpened = label.GetIO().IsOpened();

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Toast.makeText(
                            mActivity.getApplicationContext(),
                            bPrintResult ? getResources().getString(
                                    R.string.printsuccess) : getResources()
                                    .getString(R.string.printfailed),
                            Toast.LENGTH_SHORT).show();
                    mActivity.btnPrint.setEnabled(bIsOpened);
                }
            });

        }
    }

    public class TaskClose implements Runnable {
        BTPrinting bt;

        public TaskClose(BTPrinting bt) {
            this.bt = bt;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            bt.Close();
        }

    }

    @Override
    public void OnOpen() {
        // TODO Auto-generated method stub
        this.runOnUiThread(() -> {
            btnDisconnect.setEnabled(true);
            btnPrint.setEnabled(true);
            btnSearch.setEnabled(false);
            linearlayoutdevices.setEnabled(false);
            for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
                Button btn = (Button) linearlayoutdevices.getChildAt(i);
                btn.setEnabled(false);
            }
            Toast.makeText(mActivity, "Connected", Toast.LENGTH_SHORT)
                    .show();
        });
    }

    @Override
    public void OnOpenFailed() {
        // TODO Auto-generated method stub
        this.runOnUiThread(() -> {
            btnDisconnect.setEnabled(false);
            btnPrint.setEnabled(false);
            btnSearch.setEnabled(true);
            linearlayoutdevices.setEnabled(true);
            for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
                Button btn = (Button) linearlayoutdevices.getChildAt(i);
                btn.setEnabled(true);
            }
            Toast.makeText(mActivity, "Failed", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void OnClose() {
        // TODO Auto-generated method stub
        this.runOnUiThread(() -> {
            btnDisconnect.setEnabled(false);
            btnPrint.setEnabled(false);
            btnSearch.setEnabled(true);
            linearlayoutdevices.setEnabled(true);
            for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
                Button btn = (Button) linearlayoutdevices.getChildAt(i);
                btn.setEnabled(true);
            }
        });
    }

}
