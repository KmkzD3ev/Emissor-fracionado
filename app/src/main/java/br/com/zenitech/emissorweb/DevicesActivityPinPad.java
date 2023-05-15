package br.com.zenitech.emissorweb;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Set;

import stone.application.interfaces.StoneCallbackInterface;
import stone.providers.BluetoothConnectionProvider;
import stone.utils.PinpadObject;

public class DevicesActivityPinPad extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private boolean btConnected = false;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_pin_pad);
        listView = findViewById(R.id.listDevicesActivity);
        listView.setOnItemClickListener(this);
        turnBluetoothOn();
        listBluetoothDevices();
        CheckPermissionn();
    }

    private void CheckPermissionn() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        128);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 128);
        }

    }

    private void PedirPermissao() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADMIN}, 128);
    }

    public void listBluetoothDevices() {

        // Lista de Pinpads para passar para o BluetoothConnectionProvider.
        ArrayAdapter<String> btArrayAdapter = new ArrayAdapter<>(this, R.layout.list_item_bluetooths);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        128);
                return;
            }
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // Lista todos os dispositivos pareados.
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                btArrayAdapter.add(String.format("%s_%s", device.getName(), device.getAddress()));
            }
        }

        // Exibe todos os dispositivos da lista.
        listView.setAdapter(btArrayAdapter);
    }

    public void turnBluetoothOn() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.


                PedirPermissao();
                return;
            }
            mBluetoothAdapter.enable();
            do {
            } while (!mBluetoothAdapter.isEnabled());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // Pega o pinpad selecionado do ListView.
        String[] pinpadInfo = listView.getAdapter().getItem(position).toString().split("_");
        PinpadObject pinpadSelected = new PinpadObject(pinpadInfo[0], pinpadInfo[1], false);

        // Passa o pinpad selecionado para o provider de conexão bluetooth.
        final BluetoothConnectionProvider bluetoothConnectionProvider = new BluetoothConnectionProvider(DevicesActivityPinPad.this, pinpadSelected);
        bluetoothConnectionProvider.setDialogMessage("Criando conexao com o pinpad selecionado"); // Mensagem exibida do dialog.
        bluetoothConnectionProvider.useDefaultUI(false); // Informa que haverá um feedback para o usuário.
        bluetoothConnectionProvider.setConnectionCallback(new StoneCallbackInterface() {

            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Pinpad conectado", Toast.LENGTH_SHORT).show();
                btConnected = true;
                finish();
            }

            public void onError() {
                Toast.makeText(getApplicationContext(), "Erro durante a conexao. Verifique a lista de erros do provider para mais informacoes", Toast.LENGTH_SHORT).show();
                //Timber.e("onError: %s", bluetoothConnectionProvider.getListOfErrors());
            }
        });
        bluetoothConnectionProvider.execute(); // Executa o provider de conexão bluetooth.
    }
}
