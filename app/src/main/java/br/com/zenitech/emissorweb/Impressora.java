package br.com.zenitech.emissorweb;

import static br.com.zenitech.emissorweb.ClassAuxiliar.getSha1Hex;
import static br.com.zenitech.emissorweb.MaskUtil.maskCnpj;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.datecs.api.BuildInfo;
import com.datecs.api.card.FinancialCard;
import com.datecs.api.emsr.EMSR;
import com.datecs.api.printer.Printer;
import com.datecs.api.printer.PrinterInformation;
import com.datecs.api.printer.ProtocolAdapter;
import com.datecs.api.rfid.ContactlessCard;
import com.datecs.api.rfid.FeliCaCard;
import com.datecs.api.rfid.ISO14443Card;
import com.datecs.api.rfid.ISO15693Card;
import com.datecs.api.rfid.RC663;
import com.datecs.api.rfid.STSRICard;
import com.datecs.api.universalreader.UniversalReader;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import br.com.zenitech.emissorweb.controller.PrintViewHelper;
import br.com.zenitech.emissorweb.domains.AutorizacoesPinpad;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PedidosNFE;
import br.com.zenitech.emissorweb.domains.PrintPixDomain;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.network.PrinterServer;
import br.com.zenitech.emissorweb.util.HexUtil;

public class Impressora extends AppCompatActivity {

    private static final String LOG_TAG = "Impressora";
    public static boolean liberaImpressao;

    // Pedido para obter o dispositivo bluetooth
    private static final int REQUEST_GET_DEVICE = 0;

    // Pedido para obter o dispositivo bluetooth
    private static final int DEFAULT_NETWORK_PORT = 9100;
    private boolean impressao_pix = false;

    // Interface, usado para invocar a operação da impressora assíncrona.
    private interface PrinterRunnable {
        void run(ProgressDialog dialog, Printer printer) throws IOException;
    }

    // Variáveis-membro
    private ProtocolAdapter mProtocolAdapter;
    private ProtocolAdapter.Channel mPrinterChannel;
    private ProtocolAdapter.Channel mUniversalChannel;
    private Printer mPrinter;
    private EMSR mEMSR;
    private PrinterServer mPrinterServer;
    private BluetoothSocket mBtSocket;
    private Socket mNetSocket;
    private RC663 mRC663;

    //
    private DatabaseHelper bd;
    private ClassAuxiliar cAux;

    //DADOS PARA IMPRESSÃO
    String pedido, cliente, id_produto, produto, protocolo, chave, quantidade,
            valor, valorUnit, tributos, tributosN, tributosE, tributosM, posicao, tipoImpressao, form_pagamento;

    TextView total;
    public TextView imprimindo;

    public static String[] linhaProduto;

    ArrayList<Unidades> elementosUnidade;
    Unidades unidades;

    //ArrayList<PosApp> elementosPos;
    //PosApp posApp;

    // NFC-e
    ArrayList<Pedidos> elementosPedidos;
    Pedidos pedidos;

    ArrayList<ItensPedidos> elementosItens;
    ItensPedidos itensPedidos;

    // NF-e
    ArrayList<PedidosNFE> elementosPedidosNFE;
    PedidosNFE pedidosNFE;

    Context context;
    ImageView qrcode;

    String enderecoBlt = "";
    String tamFont = "";
    SharedPreferences prefs;

    //
    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
    File myDir = new File(root + "/Emissor_Web");

    //
    String dataHoraCan, codAutCan;

    //
    private boolean impComPagViaCliente = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impressora);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);

        if (prefs.getString("tamPapelImpressora", "").equalsIgnoreCase("58mm")) {
            tamFont = "{s}";
        }

        liberaImpressao = false;

        bd = new DatabaseHelper(this);
        //
        cAux = new ClassAuxiliar();
        context = this;

        imprimindo = findViewById(R.id.imprimindo);
        total = findViewById(R.id.total);
        qrcode = findViewById(R.id.qrcode);

        // Show Android device information and API version.
        final TextView txtVersion = findViewById(R.id.txt_version);
        String txt = Build.MANUFACTURER + " " + Build.MODEL + ", Datecs API " + BuildInfo.VERSION;
        txtVersion.setText(txt);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {

                posicao = params.getString("posicao");
                pedido = params.getString("pedido");
                cliente = params.getString("cliente");
                id_produto = params.getString("id_produto");
                produto = params.getString("produto");
                protocolo = params.getString("protocolo");
                chave = params.getString("chave");
                quantidade = params.getString("quantidade");
                valor = params.getString("valor");
                valorUnit = params.getString("valorUnit");
                tributos = params.getString("tributos");
                tributosN = params.getString("tributosN");
                tributosE = params.getString("tributosE");
                tributosM = params.getString("tributosM");
                tipoImpressao = params.getString("imprimir");
                form_pagamento = params.getString("form_pagamento");
                impressao_pix = params.getBoolean("impressao_pix");

                linhaProduto = new String[]{
                        "1 " + id_produto + "      " + produto,
                        "" + quantidade + "     " + "UN     " + valorUnit + "   " + valor,
                        valor,
                        protocolo,
                        cliente,
                        tributos,
                        chave,
                        params.getString("razao_social"),
                        params.getString("cnpj"),
                        params.getString("endereco"),
                        params.getString("bairro"),
                        params.getString("cep"),
                        form_pagamento,
                        tributosN,
                        tributosE,
                        tributosM
                };

                // COMPROVANTE CANCELAMENTO CARTÃO
                dataHoraCan = params.getString("dataHoraCan");
                codAutCan = params.getString("codAutCan");

            } else {
                Toast.makeText(context, "Envie algo para imprimir!", Toast.LENGTH_LONG).show();
            }
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            ativarBluetooth();
            printNFCE(linhaProduto);
        });

        //CRIA A LISTA DE PEDIDOS PARA IMPRESSÃO
        elementosPedidos = bd.getPedidosRelatorio();
        elementosPedidosNFE = bd.getPedidosRelatorioNFE();

        CheckPermission();

        ativarBluetooth();

        if (!prefs.getString("enderecoBlt", "").equalsIgnoreCase("")) {
            establishBluetoothConnection(prefs.getString("enderecoBlt", ""));
        } else {
            waitForConnection();
        }

        tempo(1000);
    }

    private void CheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        128);
            }
        }

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                    128);
        }
    }

    private void ativarBluetooth() {
        new AtivarDesativarBluetooth().enableBT(context, this);
    }

    public void tempo(int tempo) {

        //
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            //Log.i(LOG_TAG, "Relatório");

            //
            if (liberaImpressao) {
                if (tipoImpressao.equals("relatorio")) {
                    //Log.i(LOG_TAG, "Relatório");

                    //Imprimir relatório de notas fiscais eletronica

                    if (prefs.getString("tamPapelImpressora", "").equalsIgnoreCase("58mm")) {
                        printRelatorioNFCE58mm();
                    } else {
                        printRelatorioNFCE();
                    }
                    //printPage();
                } else if (tipoImpressao.equals("nfe")) {

                    //Imprimir nota fiscal eletronica

                    if (prefs.getString("tamPapelImpressora", "").equalsIgnoreCase("58mm")) {
                        printNFE58mm(linhaProduto);
                    } else {
                        printNFE(linhaProduto);
                    }

                    //printImage();
                    //printBarcode();

                } else if (tipoImpressao.equals("reimpressao_comprovante")) {

                    //Imprimir comprovante do pagamento cartão
                    tempoImprCompViaEsta(1000, true);

                } else if (tipoImpressao.equals("comprovante_cancelamento")) {

                    //Imprimir comprovante do pagamento cartão
                    imprimirComprovanteCancelCartaoCliente();
                    tempoImprCompCancelEsta(5000);
                } else if (tipoImpressao.equals("comprovante_pix_reimp")) {

                    ComprovantePixReimpressao();
                } else {

                    //Imprimir nota fiscal eletronica
                    if (prefs.getString("tamPapelImpressora", "").equalsIgnoreCase("58mm")) {
                        printNFCE58mm(linhaProduto);
                        /*if (form_pagamento.equalsIgnoreCase("CARTAO DE CREDITO") || form_pagamento.equalsIgnoreCase("CARTAO DE DEBITO")) {
                            tempoImprCompViaCli(linhaProduto);
                        }*/
                        tempoImprCompViaCli();

                    } else {
                        printNFCE(linhaProduto);
                        /*if (form_pagamento.equalsIgnoreCase("CARTAO DE CREDITO") || form_pagamento.equalsIgnoreCase("CARTAO DE DEBITO")) {
                            tempoImprCompViaCli(linhaProduto);
                        }*/

                        tempoImprCompViaCli();
                    }

                    //printImage();
                    //printBarcode();

                }
                liberaImpressao = false;
            } else {
                //
                tempo(2000);
            }
        }, tempo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GET_DEVICE) {
            if (resultCode == DeviceListActivity.RESULT_OK) {
                String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // address = "192.168.11.136:9100";
                if (BluetoothAdapter.checkBluetoothAddress(address)) {
                    Log.d(LOG_TAG, "establishBluetoothConnection(" + address + ")");
                    establishBluetoothConnection(address);
                } else {
                    Log.d(LOG_TAG, "establishNetworkConnection(" + address + ")");
                    establishNetworkConnection(address);
                }
            } else {
                finish();
            }
        }
    }

    private void toast(final String text) {
        Log.d(LOG_TAG, text);

        runOnUiThread(() -> Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show());
    }

    private void error(final String text) {
        Log.w(LOG_TAG, text);

        runOnUiThread(() -> Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show());
    }

    private void dialog(final int iconResId, final String title, final String msg) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Impressora.this);
            builder.setIcon(iconResId);
            builder.setTitle(title);
            builder.setMessage(msg);
            builder.setPositiveButton(android.R.string.ok,
                    (dialog, which) -> dialog.dismiss());

            AlertDialog dlg = builder.create();
            dlg.show();
        });
    }

    private void status(final String text) {
        runOnUiThread(() -> {
            if (text != null) {
                findViewById(R.id.panel_status).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.txt_status)).setText(text);
            } else {
                findViewById(R.id.panel_status).setVisibility(View.INVISIBLE);
            }
        });
    }

    private void runTask(final PrinterRunnable r, final int msgResId) {
        final ProgressDialog dialog = new ProgressDialog(Impressora.this);
        dialog.setTitle(getString(R.string.title_please_wait));
        dialog.setMessage(getString(msgResId));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Thread t = new Thread(() -> {
            try {
                r.run(dialog, mPrinter);
            } catch (IOException e) {
                e.printStackTrace();
                error("I/O error occurs: " + e.getMessage());
                Log.d(LOG_TAG, e.getMessage(), e);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(LOG_TAG, e.getMessage(), e);
                error("Critical error occurs: " + e.getMessage());
                //dialog.dismiss();
                finish();
            } finally {
                dialog.dismiss();
            }
        });
        t.start();
    }

    protected void initPrinter(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        Log.d(LOG_TAG, "Initialize printer...");

        // Here you can enable various debug information
        //ProtocolAdapter.setDebug(true);
        Printer.setDebug(true);
        EMSR.setDebug(true);

        // Check if printer is into protocol mode. Ones the object is created it can not be released
        // without closing base streams.
        mProtocolAdapter = new ProtocolAdapter(inputStream, outputStream);
        if (mProtocolAdapter.isProtocolEnabled()) {
            Log.d(LOG_TAG, "Protocol mode is enabled");

            // Into protocol mode we can callbacks to receive printer notifications
            mProtocolAdapter.setPrinterListener(new ProtocolAdapter.PrinterListener() {
                @Override
                public void onThermalHeadStateChanged(boolean overheated) {
                    if (overheated) {
                        Log.d(LOG_TAG, "Thermal head is overheated");
                        status("OVERHEATED");
                    } else {
                        status(null);
                    }
                }

                @Override
                public void onPaperStateChanged(boolean hasPaper) {
                    if (hasPaper) {
                        Log.d(LOG_TAG, "Event: Paper out");
                        status("PAPER OUT");
                    } else {
                        status(null);
                    }
                }

                @Override
                public void onBatteryStateChanged(boolean lowBattery) {
                    if (lowBattery) {
                        Log.d(LOG_TAG, "Low battery");
                        status("LOW BATTERY");
                    } else {
                        status(null);
                    }
                }
            });

            mProtocolAdapter.setBarcodeListener(() -> {
                Log.d(LOG_TAG, "On read barcode");
                runOnUiThread(() -> readBarcode(0));
            });

            mProtocolAdapter.setCardListener(encrypted -> {
                Log.d(LOG_TAG, "On read card(entrypted=" + encrypted + ")");

                if (encrypted) {
                    runOnUiThread(this::readCardEncrypted);
                } else {
                    runOnUiThread(this::readCard);
                }
            });

            // Get printer instance
            mPrinterChannel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_PRINTER);
            mPrinter = new Printer(mPrinterChannel.getInputStream(), mPrinterChannel.getOutputStream());

            // Check if printer has encrypted magnetic head
            ProtocolAdapter.Channel emsrChannel = mProtocolAdapter
                    .getChannel(ProtocolAdapter.CHANNEL_EMSR);
            try {
                // Close channel silently if it is already opened.
                try {
                    emsrChannel.close();
                } catch (IOException ignored) {
                }

                // Try to open EMSR channel. If method failed, then probably EMSR is not supported
                // on this device.
                emsrChannel.open();

                mEMSR = new EMSR(emsrChannel.getInputStream(), emsrChannel.getOutputStream());
                EMSR.EMSRKeyInformation keyInfo = mEMSR.getKeyInformation(EMSR.KEY_AES_DATA_ENCRYPTION);
                if (!keyInfo.tampered && keyInfo.version == 0) {
                    Log.d(LOG_TAG, "Missing encryption key");
                    // If key version is zero we can load a new key in plain mode.
                    byte[] keyData = CryptographyHelper.createKeyExchangeBlock(0xFF,
                            EMSR.KEY_AES_DATA_ENCRYPTION, 1, CryptographyHelper.AES_DATA_KEY_BYTES,
                            null);
                    mEMSR.loadKey(keyData);
                }
                mEMSR.setEncryptionType(EMSR.ENCRYPTION_TYPE_AES256);
                mEMSR.enable();
                Log.d(LOG_TAG, "Encrypted magnetic stripe reader is available");
            } catch (IOException e) {
                if (mEMSR != null) {
                    mEMSR.close();
                    mEMSR = null;
                }
            }

            // Check if printer has encrypted magnetic head
            ProtocolAdapter.Channel rfidChannel = mProtocolAdapter
                    .getChannel(ProtocolAdapter.CHANNEL_RFID);

            try {
                // Close channel silently if it is already opened.
                try {
                    rfidChannel.close();
                } catch (IOException ignored) {
                }

                // Try to open RFID channel. If method failed, then probably RFID is not supported
                // on this device.
                rfidChannel.open();

                mRC663 = new RC663(rfidChannel.getInputStream(), rfidChannel.getOutputStream());
                mRC663.setCardListener(this::processContactlessCard);
                mRC663.enable();
                Log.d(LOG_TAG, "RC663 o leitor está disponível");
            } catch (IOException e) {
                if (mRC663 != null) {
                    mRC663.close();
                    mRC663 = null;
                }
            }

            // Check if printer has encrypted magnetic head
            mUniversalChannel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_UNIVERSAL_READER);
            new UniversalReader(mUniversalChannel.getInputStream(), mUniversalChannel.getOutputStream());

        } else {
            Log.d(LOG_TAG, "O modo de protocolo está desativado");

            // Protocol mode it not enables, so we should use the row streams.
            mPrinter = new Printer(mProtocolAdapter.getRawInputStream(),
                    mProtocolAdapter.getRawOutputStream());
        }

        mPrinter.setConnectionListener(() -> {
            toast("A impressora está desconectada");

            runOnUiThread(() -> {
                if (!isFinishing()) {
                    waitForConnection();
                }
            });
        });

    }

    private synchronized void waitForConnection() {
        //status(null);

        closeActiveConnection();

        // Show dialog to select a Bluetooth device.
        startActivityForResult(new Intent(this, DeviceListActivity.class), REQUEST_GET_DEVICE);

        // Start server to listen for network connection.
        try {
            mPrinterServer = new PrinterServer(socket -> {
                Log.d(LOG_TAG, "Aceitar conexão de "
                        + socket.getRemoteSocketAddress().toString());

                // Close Bluetooth selection dialog
                finishActivity(REQUEST_GET_DEVICE);

                mNetSocket = socket;
                try {
                    InputStream in = socket.getInputStream();
                    OutputStream out = socket.getOutputStream();
                    initPrinter(in, out);
                } catch (IOException e) {
                    e.printStackTrace();
                    error("Falha na inicialização: " + e.getMessage());
                    waitForConnection();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void establishBluetoothConnection(final String address) {
        final ProgressDialog dialog = new ProgressDialog(Impressora.this);
        dialog.setTitle(getString(R.string.title_please_wait));
        dialog.setMessage(getString(R.string.msg_connecting));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        closePrinterServer();

        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        final Thread t = new Thread(() -> {
            Log.d(LOG_TAG, "BluetoothConnection - Conectando à " + address + "...");

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.BLUETOOTH_SCAN},
                            128);
                }
            }
            btAdapter.cancelDiscovery();

            try {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                BluetoothDevice btDevice = btAdapter.getRemoteDevice(address);

                InputStream in;
                OutputStream out;

                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            ActivityCompat.requestPermissions(this,
                                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                    128);
                        }
                    }
                    BluetoothSocket btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
                    btSocket.connect();

                    mBtSocket = btSocket;
                    in = mBtSocket.getInputStream();
                    out = mBtSocket.getOutputStream();
                } catch (IOException e) {
                    error("Falhou ao conectar: " + e.getMessage());
                    waitForConnection();
                    return;
                }

                try {
                    initPrinter(in, out);
                } catch (IOException e) {
                    error("Falha na inicialização: " + e.getMessage());
                    return;
                }

                if (in != null && out != null) {

                    liberaImpressao = true;
                    enderecoBlt = address;
                }

            } finally {
                dialog.dismiss();
            }
        });
        t.start();
    }

    private void establishNetworkConnection(final String address) {
        closePrinterServer();

        final ProgressDialog dialog = new ProgressDialog(Impressora.this);
        dialog.setTitle(getString(R.string.title_please_wait));
        dialog.setMessage(getString(R.string.msg_connecting));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        closePrinterServer();

        final Thread t = new Thread(() -> {
            Log.d(LOG_TAG, "NetworkConnection - Conectando à " + address + "...");
            try {
                Socket s;
                try {
                    String[] url = address.split(":");
                    int port = DEFAULT_NETWORK_PORT;

                    try {
                        if (url.length > 1) {
                            port = Integer.parseInt(url[1]);
                        }
                    } catch (NumberFormatException e) {
                        Log.i(LOG_TAG, Objects.requireNonNull(e.getMessage()), e);
                    }

                    s = new Socket(url[0], port);
                    s.setKeepAlive(true);
                    s.setTcpNoDelay(true);
                } catch (UnknownHostException e) {
                    error("Falhou ao conectar: " + e.getMessage());
                    waitForConnection();
                    return;
                } catch (IOException e) {
                    error("Falhou ao conectar: " + e.getMessage());
                    waitForConnection();
                    return;
                }

                InputStream in;
                OutputStream out;

                try {
                    mNetSocket = s;
                    in = mNetSocket.getInputStream();
                    out = mNetSocket.getOutputStream();
                } catch (IOException e) {
                    error("Falhou ao conectar: " + e.getMessage());
                    waitForConnection();
                    return;
                }

                try {
                    initPrinter(in, out);
                } catch (IOException e) {
                    error("Falha na inicialização: " + e.getMessage());
                    return;
                }


                if (s != null && in != null && out != null) {

                    liberaImpressao = true;
                }
            } finally {
                dialog.dismiss();
            }
        });
        t.start();
    }

    private synchronized void closePrinterConnection() {
        if (mRC663 != null) {
            try {
                mRC663.disable();
            } catch (IOException e) {
                Log.i(LOG_TAG, e.getMessage());
            }

            mRC663.close();
        }

        if (mEMSR != null) {
            mEMSR.close();
        }

        if (mPrinter != null) {
            mPrinter.close();
        }

        if (mProtocolAdapter != null) {
            mProtocolAdapter.close();
        }
    }

    private synchronized void closeBluetoothConnection() {
        // Close Bluetooth connection
        BluetoothSocket s = mBtSocket;
        mBtSocket = null;
        if (s != null) {
            Log.d(LOG_TAG, "Close Bluetooth socket");
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void closeNetworkConnection() {
        // Close network connection
        Socket s = mNetSocket;
        mNetSocket = null;
        if (s != null) {
            Log.d(LOG_TAG, "Close Network socket");
            try {
                s.shutdownInput();
                s.shutdownOutput();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void closePrinterServer() {
        closeNetworkConnection();

        // Close network server
        PrinterServer ps = mPrinterServer;
        mPrinterServer = null;
        if (ps != null) {
            Log.d(LOG_TAG, "Close Network server");
            try {
                ps.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void closeActiveConnection() {
        closePrinterConnection();
        closeBluetoothConnection();
        closeNetworkConnection();
        closePrinterServer();
    }

    private void readCard() {
        Log.d(LOG_TAG, "Read card");

        runTask((dialog, printer) -> {
            PrinterInformation pi = printer.getInformation();
            String[] tracks;
            FinancialCard card = null;
            Printer.setDebug(true);
            if (pi.getName().startsWith("CMP-10")) {
                // The printer CMP-10 can read only two tracks at once.
                tracks = printer.readCard(true, true, false, 15000);
            } else {
                tracks = printer.readCard(true, true, true, 15000);
            }

            if (tracks != null) {
                StringBuffer textBuffer = new StringBuffer();

                if (tracks[0] == null && tracks[1] == null && tracks[2] == null) {
                    textBuffer.append(getString(R.string.no_card_read));
                } else {
                    if (tracks[0] != null) {
                        card = new FinancialCard(tracks[0]);
                    } else if (tracks[1] != null) {
                        card = new FinancialCard(tracks[1]);
                    }

                    if (card != null) {
                        textBuffer.append(getString(R.string.card_no) + ": " + card.getNumber());
                        textBuffer.append("\n");
                        textBuffer.append(getString(R.string.holder) + ": " + card.getName());
                        textBuffer.append("\n");
                        textBuffer.append(getString(R.string.exp_date)
                                + ": "
                                + String.format("%02d/%02d", card.getExpiryMonth(),
                                card.getExpiryYear()));
                        textBuffer.append("\n");
                    }

                    if (tracks[0] != null) {
                        textBuffer.append("\n");
                        textBuffer.append(tracks[0]);

                    }
                    if (tracks[1] != null) {
                        textBuffer.append("\n");
                        textBuffer.append(tracks[1]);
                    }
                    if (tracks[2] != null) {
                        textBuffer.append("\n");
                        textBuffer.append(tracks[2]);
                    }
                }

                dialog(R.drawable.ic_card, getString(R.string.card_info), textBuffer.toString());
            }
        }, R.string.msg_reading_magstripe);
    }

    private void readCardEncrypted() {
        Log.d(LOG_TAG, "Read card encrypted");

        runTask((dialog, printer) -> {
            byte[] buffer = mEMSR.readCardData(EMSR.MODE_READ_TRACK1 | EMSR.MODE_READ_TRACK2
                    | EMSR.MODE_READ_TRACK3 | EMSR.MODE_READ_PREFIX);
            StringBuffer textBuffer = new StringBuffer();

            int encryptionType = (buffer[0] >>> 3);
            // Trim extract encrypted block.
            byte[] encryptedData = new byte[buffer.length - 1];
            System.arraycopy(buffer, 1, encryptedData, 0, encryptedData.length);

            if (encryptionType == EMSR.ENCRYPTION_TYPE_OLD_RSA
                    || encryptionType == EMSR.ENCRYPTION_TYPE_RSA) {
                try {
                    String[] result = CryptographyHelper.decryptTrackDataRSA(encryptedData);
                    textBuffer.append("Track2: " + result[0]);
                    textBuffer.append("\n");
                } catch (Exception e) {
                    error("Failed to decrypt RSA data: " + e.getMessage());
                    return;
                }
            } else if (encryptionType == EMSR.ENCRYPTION_TYPE_AES256) {
                try {
                    String[] result = CryptographyHelper.decryptAESBlock(encryptedData);

                    textBuffer.append("Random data: " + result[0]);
                    textBuffer.append("\n");
                    textBuffer.append("Serial number: " + result[1]);
                    textBuffer.append("\n");
                    if (result[2] != null) {
                        textBuffer.append("Track1: " + result[2]);
                        textBuffer.append("\n");
                    }
                    if (result[3] != null) {
                        textBuffer.append("Track2: " + result[3]);
                        textBuffer.append("\n");
                    }
                    if (result[4] != null) {
                        textBuffer.append("Track3: " + result[4]);
                        textBuffer.append("\n");
                    }
                } catch (Exception e) {
                    error("Failed to decrypt AES data: " + e.getMessage());
                    return;
                }
            } else if (encryptionType == EMSR.ENCRYPTION_TYPE_IDTECH) {
                try {
                    String[] result = CryptographyHelper.decryptIDTECHBlock(encryptedData);

                    textBuffer.append("Card type: " + result[0]);
                    textBuffer.append("\n");
                    if (result[1] != null) {
                        textBuffer.append("Track1: " + result[1]);
                        textBuffer.append("\n");
                    }
                    if (result[2] != null) {
                        textBuffer.append("Track2: " + result[2]);
                        textBuffer.append("\n");
                    }
                    if (result[3] != null) {
                        textBuffer.append("Track3: " + result[3]);
                        textBuffer.append("\n");
                    }
                } catch (Exception e) {
                    error("Failed to decrypt IDTECH data: " + e.getMessage());
                    return;
                }
            } else {
                textBuffer.append("Encrypted block: " + HexUtil.byteArrayToHexString(buffer));
                textBuffer.append("\n");
            }

            dialog(R.drawable.ic_card, getString(R.string.card_info), textBuffer.toString());
        }, R.string.msg_reading_magstripe);
    }

    private void readBarcode(final int timeout) {
        Log.d(LOG_TAG, "Read Barcode");

        runTask((dialog, printer) -> {
            String barcode = printer.readBarcode(timeout);

            if (barcode != null) {
                dialog(R.drawable.ic_read_barcode, getString(R.string.barcode), barcode);
            }
        }, R.string.msg_reading_barcode);
    }

    private void processContactlessCard(ContactlessCard contactlessCard) {
        final StringBuffer msgBuf = new StringBuffer();

        if (contactlessCard instanceof ISO14443Card) {
            ISO14443Card card = (ISO14443Card) contactlessCard;
            msgBuf.append("ISO14 card: " + HexUtil.byteArrayToHexString(card.uid) + "\n");
            msgBuf.append("ISO14 type: " + card.type + "\n");

            if (card.type == ContactlessCard.CARD_MIFARE_DESFIRE) {
                ProtocolAdapter.setDebug(true);
                mPrinterChannel.suspend();
                mUniversalChannel.suspend();
                try {
                    // KLEILSON
                    card.getATS();
                    Log.d(LOG_TAG, "Select application");
                    card.DESFire().selectApplication(0x78E127);
                    Log.d(LOG_TAG, "Application is selected");
                    msgBuf.append("DESFire Application: " + Integer.toHexString(0x78E127) + "\n");
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Select application", e);
                } finally {
                    ProtocolAdapter.setDebug(false);
                    mPrinterChannel.resume();
                    mUniversalChannel.resume();
                }
            }
            /*
             // 16 bytes reading and 16 bytes writing
             // Try to authenticate first with default key
            byte[] key= new byte[] {-1, -1, -1, -1, -1, -1};
            // It is best to store the keys you are going to use once in the device memory,
            // then use AuthByLoadedKey function to authenticate blocks rather than having the key in your program
            card.authenticate('A', 8, key);

            // Write data to the card
            byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                    0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };
            card.write16(8, input);

            // Read data from card
            byte[] result = card.read16(8);
            */
        } else if (contactlessCard instanceof ISO15693Card) {
            ISO15693Card card = (ISO15693Card) contactlessCard;

            msgBuf.append("ISO15 card: " + HexUtil.byteArrayToHexString(card.uid) + "\n");
            msgBuf.append("Block size: " + card.blockSize + "\n");
            msgBuf.append("Max blocks: " + card.maxBlocks + "\n");

            /*
            if (card.blockSize > 0) {
                byte[] security = card.getBlocksSecurityStatus(0, 16);
                ...

                // Write data to the card
                byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03 };
                card.write(0, input);
                ...

                // Read data from card
                byte[] result = card.read(0, 1);
                ...
            }
            */
        } else if (contactlessCard instanceof FeliCaCard) {
            FeliCaCard card = (FeliCaCard) contactlessCard;

            msgBuf.append("FeliCa card: " + HexUtil.byteArrayToHexString(card.uid) + "\n");

            /*
            // Write data to the card
            byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                    0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };
            card.write(0x0900, 0, input);
            ...

            // Read data from card
            byte[] result = card.read(0x0900, 0, 1);
            ...
            */
        } else if (contactlessCard instanceof STSRICard) {
            STSRICard card = (STSRICard) contactlessCard;

            msgBuf.append("STSRI card: " + HexUtil.byteArrayToHexString(card.uid) + "\n");
            msgBuf.append("Block size: " + card.blockSize + "\n");

            /*
            // Write data to the card
            byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03 };
            card.writeBlock(8, input);
            ...

            // Try reading two blocks
            byte[] result = card.readBlock(8);
            ...
            */
        } else {
            msgBuf.append("Cartão sem contato: " + HexUtil.byteArrayToHexString(contactlessCard.uid));
        }

        dialog(R.drawable.ic_tag, getString(R.string.tag_info), msgBuf.toString());

        // Wait silently to remove card
        try {
            contactlessCard.waitRemove();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // DESATIVAR BLUETOOTH
    private void desativarBluetooth() {
        new AtivarDesativarBluetooth().disableBT(context, this);
    }

    /***************************** - IMPRESSÃO - *********************************/

    // ** SALVA A IMAGEM COM O QCODE OU COD. BARRA
    private void SaveImage(Bitmap finalBitmap) {

        myDir.mkdirs();

        String fname = "qrcode.png";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- IMPRESSÃO DE NFCe 80 E 58 mm ---------------------------

    // ** NFC-e 80mm
    private void printNFCE(final String[] texto) {
        //Log.d(LOG_TAG, "Print NFC-e");

        runTask((dialog, printer) -> {
            printer.reset();

            String serie = bd.getSeriePOS();
            elementosUnidade = bd.getUnidades();

            String urlConsulta, urlQRCode, idCSC, CSC, hashSHA1;
            unidades = elementosUnidade.get(0);

            urlConsulta = unidades.getUrl_consulta();
            urlQRCode = unidades.getUrl_qrcode();
            idCSC = unidades.getIdCSC();
            CSC = unidades.getCSC();

            String url;
            if (texto[3].equalsIgnoreCase("EMITIDA EM CONTINGENCIA")) {
                // Chave de Acesso da NFC-e
                // Versão do QR Code
                // Identificação do Ambiente (1 – Produção, 2 – Homologação)
                // Dia da data de emissão
                // Valor Total da NFC-e
                // DigestValue da NFCe
                // Identificador do CSC (Código de Segurança do Contribuinte no Banco de Dados da SEFAZ)
                // Código Hash dos Parâmetros

                hashSHA1 = texto[6] + "|" + "2" + "|" + "1" + "|" + idCSC + CSC;
                hashSHA1 = getSha1Hex(hashSHA1);

                url = urlQRCode + "?p=" + texto[6] + "|2|1|" + idCSC + "|" + hashSHA1;
            } else {
                // Chave de Acesso da NFC-e
                // Versão do QR Code
                // Identificação do Ambiente (1 – Produção, 2 – Homologação)
                // Identificador do CSC (Código de Segurança do Contribuinte no Banco de Dados da SEFAZ)
                // Código Hash dos Parâmetros

                hashSHA1 = texto[6] + "|" + "2" + "|" + "1" + "|" + idCSC + CSC;
                hashSHA1 = getSha1Hex(hashSHA1);

                url = urlQRCode + "?p=" + texto[6] + "|2|1|" + idCSC + "|" + hashSHA1;
            }

            Bitmap bp = null;
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, 250, 250);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                bp = barcodeEncoder.createBitmap(bitMatrix);
                //Bitmap bp = barcodeEncoder.createBitmap(bitMatrix);

                //SaveImage(bp);
            } catch (WriterException e) {
                e.printStackTrace();
            }

            // Retorna o caminho da imagem do qrcode
            /*File sdcard = Environment.getExternalStorageDirectory().getAbsoluteFile();
            File dir = new File(sdcard, "Emissor_Web/");

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;

            FileInputStream inputStream;
            BufferedInputStream bufferedInputStream;

            inputStream = new FileInputStream(dir.getPath() + "/qrcode.png");
            bufferedInputStream = new BufferedInputStream(inputStream);
            Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream, null, options);*/

            StringBuilder textBuffer = new StringBuilder();

            //IMPRIMIR CABEÇALHO
            textBuffer.append("{reset}{center}").append(tamFont).append(texto[7]).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(texto[8]).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(texto[9]).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(texto[10]).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(texto[11]).append("{br}");

            //DANFE NFC-e
            textBuffer.append("{reset}").append(tamFont).append("------------------------------------------------{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append("DANFE NFC-e - DOCUMENTO AUXILIAR DA NOTA FISCAL{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append("DE CONSUMIDOR ELETRONICA{br}");
            textBuffer.append("{reset}").append(tamFont).append("------------------------------------------------{br}");

            //INFOR. PEDIDO
            textBuffer.append("{reset}").append(tamFont).append("# CODIGO DESCRICAO QTDE. UN.  VL.UNIT.  VL.TOTAL{br}");
            //PRODUTOS
            elementosItens = bd.getItensPedido(pedido);
            for (int i = 0; i < elementosItens.size(); i++) {
                itensPedidos = elementosItens.get(i);
                String produto = bd.getProduto(itensPedidos.getProduto());
                /*
                "1 " + id_produto + "      " + produto,
                        "" + quantidade + "     " + "UN     " + valorUnit + "   " + valor
                * */
                textBuffer.append(tamFont).
                        append(i + 1).
                        append(" ").
                        append(itensPedidos.getProduto()).
                        append("  ").
                        append(produto).append("  ").
                        append(itensPedidos.getQuantidade()).
                        append("  ").
                        append("UN  ").
                        append(cAux.maskMoney(new BigDecimal(itensPedidos.getValor()))).
                        append("  ").
                        append(cAux.maskMoney(new BigDecimal(itensPedidos.getTotal()))).
                        append("{br}");
            }
            //textBuffer.append(tamFont).append(texto[0]).append("{br}");
            //textBuffer.append(tamFont).append(texto[1]).append("{br}");

            /*textBuffer.append("{reset}{left}").append(tamFont).append(texto[0]).append("{br}");
            textBuffer.append("{reset}{right}").append(tamFont).append(texto[1]).append("{br}");*/
            textBuffer.append("{reset}").append(tamFont).append("------------------------------------------------{br}");

            //INFOR. VALORES
            textBuffer.append("{reset}").append(tamFont).append("Qtde. Total de Itens                           ").append(elementosItens.size()).append("{br}");//quantidade
            textBuffer.append("{reset}").append(tamFont).append("Valor Total                           ").append(texto[2].trim()).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("FORMA DE PAGAMENTO                    VALOR PAGO{br}");
            textBuffer.append("{reset}").append(tamFont).append(cAux.removerAcentos(texto[12])).append("                              ").append(texto[2].trim()).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("------------------------------------------------{br}");

            //TRIBUTOS TOTAIS
            textBuffer.append("{reset}").append(tamFont).append("Tributos totais incidentes{br}");
            textBuffer.append("{reset}").append(tamFont).append("(Lei Federal 12.741/2012)                ").append(texto[5].trim()).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("TRIBUTOS FEDERAIS                        ").append(texto[13].trim()).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("TRIBUTOS ESTADUAIS                       ").append(texto[14].trim()).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("TRIBUTOS MUNICIPAIS                      ").append(texto[15].trim()).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("------------------------------------------------{br}");

            //EMISSÃO
            textBuffer.append("{reset}").append(tamFont).append("Numero:").append(pedido).append(" Serie:").append(serie).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("Emissao:").append(cAux.exibirDataAtual()).append(" ").append(cAux.horaAtual()).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("------------------------------------------------{br}");

            //COSULTA NA RECEITA
            textBuffer.append("{reset}{center}").append(tamFont).append("Consulte pela Chave de Acesso em{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(urlConsulta).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append(" {br}");
            textBuffer.append("{reset}{center}").append(tamFont).append("Chave de Acesso{br}");
            //textBuffer.append("{reset}{center}").append(tamFont).append(texto[6]).append("{br}");
            String chaveNota = bd.gerarChave(Integer.parseInt(pedido));
            textBuffer.append(tamFont).append(chaveNota).append("{br}");
            /*String c = texto[6];
            String cl1 = c.substring(0, 4) + " " + c.substring(4, 8) + " " + c.substring(8, 12) + " " + c.substring(12, 16) + " " + c.substring(16, 20);
            String cl2 = c.substring(20, 24) + " " + c.substring(24, 28) + " " + c.substring(28, 32) + " " + c.substring(32, 36);
            textBuffer.append(tamFont).append(cl1).append("{br}");
            textBuffer.append(tamFont).append(cl2).append("{br}");*/
            textBuffer.append("{reset}{center}").append(tamFont).append("Protocolo de autorizacao{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(texto[3]).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("------------------------------------------------{br}");

            //CONSUMIDOR
            textBuffer.append("{reset}{center}").append(tamFont).append(texto[4]).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("------------------------------------------------");

            printer.reset();
            printer.printTaggedText(textBuffer.toString());
            printer.feedPaper(38);

            //---------------
            final int width = Objects.requireNonNull(bp).getWidth();
            final int height = bp.getHeight();
            final int[] argb = new int[width * height];
            bp.getPixels(argb, 0, width, 0, 0, width, height);
            bp.recycle();

            printer.reset();
            printer.printImage(argb, width, height, Printer.ALIGN_CENTER, true);
            printer.feedPaper(120);
            printer.flush();

            // Apaga a imgem anterior
            /*File imgQrC = new File(sdcard, "Emissor_Web/qrcode.png");
            imgQrC.delete();*/

            //IMPRIMIR COMPROVANTE DE PAGAMENTO CARTÃO
            StringBuilder textBuffer2 = new StringBuilder();
            textBuffer2.append("{reset}{center}").append(tamFont).append(texto[4]).append("{br}");
            textBuffer2.append("{reset}").append(tamFont).append("------------------------------------------------");

            printer.reset();
            printer.printTaggedText(textBuffer2.toString());
            printer.feedPaper(38);

            Intent i = new Intent(Impressora.this, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("nomeImpressoraBlt", enderecoBlt);
            i.putExtra("enderecoBlt", enderecoBlt);
            startActivity(i);
            finish();
        }, R.string.msg_printing_nfce);
    }

    // ** FNC-e 58mm KLEILSON
    private void printNFCE58mm(final String[] texto) {
        //Log.d(LOG_TAG, "Print NFC-e");

        runTask((dialog, printer) -> {
            String serie = bd.getSeriePOS();
            elementosUnidade = bd.getUnidades();

            String urlConsulta, urlQRCode, idCSC, CSC, hashSHA1;
            unidades = elementosUnidade.get(0);

            urlConsulta = unidades.getUrl_consulta();
            urlQRCode = unidades.getUrl_qrcode();
            idCSC = unidades.getIdCSC();
            CSC = unidades.getCSC();

            String url;
            if (texto[3].equalsIgnoreCase("EMITIDA EM CONTINGENCIA")) {
                // Chave de Acesso da NFC-e
                // Versão do QR Code
                // Identificação do Ambiente (1 – Produção, 2 – Homologação)
                // Dia da data de emissão
                // Valor Total da NFC-e
                // DigestValue da NFCe
                // Identificador do CSC (Código de Segurança do Contribuinte no Banco de Dados da SEFAZ)
                // Código Hash dos Parâmetros

                hashSHA1 = texto[6] + "|" + "2" + "|" + "1" + "|" + idCSC + CSC;
                hashSHA1 = getSha1Hex(hashSHA1);

                url = urlQRCode + "?p=" + texto[6] + "|2|1|" + idCSC + "|" + hashSHA1;
            } else {
                // Chave de Acesso da NFC-e
                // Versão do QR Code
                // Identificação do Ambiente (1 – Produção, 2 – Homologação)
                // Identificador do CSC (Código de Segurança do Contribuinte no Banco de Dados da SEFAZ)
                // Código Hash dos Parâmetros

                hashSHA1 = texto[6] + "|" + "2" + "|" + "1" + "|" + idCSC + CSC;
                hashSHA1 = getSha1Hex(hashSHA1);

                url = urlQRCode + "?p=" + texto[6] + "|2|1|" + idCSC + "|" + hashSHA1;
            }

            Bitmap bp = null;
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, 250, 250);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                bp = barcodeEncoder.createBitmap(bitMatrix);

                //SaveImage(bp);
            } catch (WriterException e) {
                e.printStackTrace();
            }

            StringBuilder textBuffer = new StringBuilder();

            //IMPRIMIR CABEÇALHO
            textBuffer.append(tamFont).append("{br}");
            textBuffer.append(tamFont).append(texto[7]).append("{br}");
            textBuffer.append(tamFont).append(texto[8]).append("{br}");
            textBuffer.append(tamFont).append(texto[9]).append("{br}");
            textBuffer.append(tamFont).append(texto[10]).append("{br}");
            textBuffer.append(tamFont).append(texto[11]).append("{br}");

            //DANFE NFC-e
            textBuffer.append(tamFont).append("-----------------------------------------{br}");
            textBuffer.append(tamFont).append("DANFE NFC-e - DOCUMENTO AUXILIAR DA{br}");
            textBuffer.append(tamFont).append("NOTA FISCAL DE CONSUMIDOR ELETRONICA{br}");
            textBuffer.append(tamFont).append("-----------------------------------------{br}");

            //INFOR. PEDIDO
            textBuffer.append(tamFont).append("# COD. DESC. QTDE. UN.  VL.UNIT.  VL.TOTAL{br}");
            //PRODUTOS
            elementosItens = bd.getItensPedido(pedido);
            for (int i = 0; i < elementosItens.size(); i++) {
                itensPedidos = elementosItens.get(i);
                String produto = bd.getProduto(itensPedidos.getProduto());
                /*
                "1 " + id_produto + "      " + produto,
                        "" + quantidade + "     " + "UN     " + valorUnit + "   " + valor
                * */
                textBuffer.append(tamFont).
                        append(i + 1).
                        append(" ").
                        append(itensPedidos.getProduto()).
                        append("  ").
                        append(produto).append("  ").
                        append(itensPedidos.getQuantidade()).
                        append("  ").
                        append("UN  ").
                        append(cAux.maskMoney(new BigDecimal(itensPedidos.getValor()))).
                        append("  ").
                        append(cAux.maskMoney(new BigDecimal(itensPedidos.getTotal()))).
                        append("{br}");
            }
            //textBuffer.append(tamFont).append(texto[0]).append("{br}");
            //textBuffer.append(tamFont).append(texto[1]).append("{br}");
            textBuffer.append(tamFont).append("-----------------------------------------{br}");

            //INFOR. VALORES
            textBuffer.append(tamFont).append("Qtde. Total de Itens                  ").append(elementosItens.size()).append("{br}");//quantidade
            textBuffer.append(tamFont).append("Valor Total                     ").append(texto[2].trim()).append("{br}");
            textBuffer.append(tamFont).append("FORMA DE PAGAMENTO            VALOR PAGO{br}");
            textBuffer.append(tamFont).append(cAux.removerAcentos(texto[12])).append("                       ").append(texto[2].trim()).append("{br}");
            textBuffer.append(tamFont).append("-----------------------------------------{br}");

            //TRIBUTOS TOTAIS
            textBuffer.append(tamFont).append("Tributos totais incidentes{br}");
            textBuffer.append(tamFont).append("(Lei Federal 12.741/2012)         ").append(texto[5].trim()).append("{br}");
            textBuffer.append(tamFont).append("TRIBUTOS FEDERAIS                 ").append(texto[13].trim()).append("{br}");
            textBuffer.append(tamFont).append("TRIBUTOS ESTADUAIS                ").append(texto[14].trim()).append("{br}");
            textBuffer.append(tamFont).append("TRIBUTOS MUNICIPAIS               ").append(texto[15].trim()).append("{br}");
            textBuffer.append(tamFont).append("-----------------------------------------{br}");

            //EMISSÃO
            textBuffer.append(tamFont).append("Numero:").append(pedido).append(" Serie:").append(serie).append("{br}");
            textBuffer.append(tamFont).append("Emissao:").append(cAux.exibirDataAtual()).append(" ").append(cAux.horaAtual()).append("{br}");
            textBuffer.append(tamFont).append("-----------------------------------------{br}");

            //COSULTA NA RECEITA
            textBuffer.append(tamFont).append("Consulte pela Chave de Acesso em{br}");
            textBuffer.append(tamFont).append(urlConsulta).append("{br}");
            textBuffer.append(tamFont).append(" {br}");
            textBuffer.append(tamFont).append("Chave de Acesso{br}");
            String chaveNota = bd.gerarChave(Integer.parseInt(pedido));
            textBuffer.append(tamFont).append(chaveNota).append("{br}");

            textBuffer.append(tamFont).append(" {br}");
            textBuffer.append(tamFont).append("Protocolo de autorizacao{br}");
            textBuffer.append(tamFont).append(texto[3]).append("{br}");
            textBuffer.append(tamFont).append("-----------------------------------------{br}");

            //CONSUMIDOR
            textBuffer.append(tamFont).append(texto[4]).append("{br}");
            textBuffer.append(tamFont).append("-----------------------------------------");

            printer.selectPageMode();
            printer.setPageXY(0, 0);
            printer.setAlign(1);
            printer.printTaggedText(textBuffer.toString());
            printer.feedPaper(38);

            //---------------
            final int width = Objects.requireNonNull(bp).getWidth();
            final int height = bp.getHeight();
            final int[] argb = new int[width * height];
            bp.getPixels(argb, 0, width, 0, 0, width, height);
            bp.recycle();

            printer.printImage(argb, width, height, Printer.ALIGN_CENTER, true);
            printer.feedPaper(120);
            printer.flush();

            if ((cAux.removerAcentos(texto[12]).contains("CARTAO DE CREDITO") || cAux.removerAcentos(texto[12]).contains("CARTAO DE DEBITO")) && !unidades.getCodloja().equalsIgnoreCase("")) {
                //
                impComPagViaCliente = true;
            } else {
                finalizarImpressao();
            }

        }, R.string.msg_printing_nfce);
    }

    void imprimirComprovantePagCartao() {
        runTask((dialog, printer) -> {
            final BitmapFactory.Options optionsStone = new BitmapFactory.Options();
            optionsStone.inScaled = false;

            final AssetManager assetManagerStone = getApplicationContext().getAssets();
            final Bitmap bitmapStone = BitmapFactory.decodeStream(assetManagerStone.open("stone.png"),
                    null, optionsStone);
            final int widthStone = Objects.requireNonNull(bitmapStone).getWidth();
            final int heightStone = bitmapStone.getHeight();
            final int[] argbStone = new int[widthStone * heightStone];
            bitmapStone.getPixels(argbStone, 0, widthStone, 0, 0, widthStone, heightStone);
            bitmapStone.recycle();

            //printer.reset();
            printer.printImage(argbStone, widthStone, heightStone, Printer.ALIGN_CENTER, true);
            printer.feedPaper(0);

            //Unidades unidades;
            //elementosUnidade = bd.getUnidades();
            AutorizacoesPinpad pinpad = bd.getAutorizacaoPinpad();

            StringBuilder textBuffer = new StringBuilder();

            //IMPRIMIR CABEÇALHO
            textBuffer.append(tamFont).append("{br}");
            textBuffer.append(tamFont).append("Via Cliente{br}{br}");
            textBuffer.append(tamFont).append(cAux.removerAcentos(pinpad.getNomeEmpresa())).append("{br}");
            textBuffer.append(tamFont).append(cAux.removerAcentos(pinpad.getEnderecoEmpresa())).append("{br}");
            textBuffer.append(tamFont).append(cAux.exibirData(pinpad.getDate()))
                    .append(" ").append(pinpad.getTime()).append(" CNPJ:")
                    .append(pinpad.getCnpjEmpresa()).append("{br}");
            textBuffer.append(tamFont).append("------------------------------------------").append("{br}");
            textBuffer.append(tamFont).append(pinpad.getTypeOfTransactionEnum()).append("                       RS ")
                    .append(cAux.maskMoney(cAux.converterValores(pinpad.getAmount()))).append("{br}");
            textBuffer.append(tamFont).append("------------------------------------------").append("{br}");
            textBuffer.append(tamFont).append(pinpad.getCardBrand()).append(" - ")
                    .append(pinpad.getCardHolderNumber().substring(pinpad.getCardHolderNumber().length() - 8))
                    .append("  AUT: ").append(pinpad.getAuthorizationCode()).append("{br}");

            textBuffer.append(tamFont).append(pinpad.getCardHolderName()).append("{br}");
            textBuffer.append(tamFont).append("Aprovado com senha").append("{br}");
            textBuffer.append(tamFont).append("SN: ").append(prefs.getString("serial_app", ""))
                    .append(" - ").append(BuildConfig.VERSION_NAME).append("{br}");

            /*String txtCompPag = "{br}" +
                    tamFont + "Via Cliente{br}{br}" +
                    tamFont + cAux.removerAcentos(pinpad.getNomeEmpresa()) + "{br}" +
                    tamFont + cAux.removerAcentos(pinpad.getEnderecoEmpresa()) + "{br}" +
                    tamFont + cAux.exibirData(pinpad.getDate()) + " " + pinpad.getTime() + " CNPJ:" + pinpad.getCnpjEmpresa() + "{br}" +
                    tamFont + "------------------------------------------{br}" +
                    pinpad.getTypeOfTransactionEnum() + "                       RS " + cAux.maskMoney(cAux.converterValores(pinpad.getAmount())).trim() + "{br}" +
                    tamFont + "------------------------------------------{br}" +
                    tamFont + pinpad.getCardBrand() + " - " + pinpad.getCardHolderNumber().substring(pinpad.getCardHolderNumber().length() - 8) + "  AUT: " + pinpad.getAuthorizationCode() + "{br}" +
                    tamFont + pinpad.getCardHolderName() + "{br}" +
                    tamFont + "Aprovado com senha{br}" +
                    tamFont + "SN: " + prefs.getString("serial_app", "") + " - " + BuildConfig.VERSION_NAME + "{br}";*/
            //printer.reset();
            printer.printTaggedText(textBuffer.toString());
            printer.feedPaper(120);

            /*String txtCompPag1 = tamFont + "CREDITO                       {right}{b}{h}R{w}$ 50,00{br}";
            //printer.reset();
            printer.printTaggedText(txtCompPag1);*/
            printer.flush();

        }, R.string.msg_printing_comp_pag_cartao_nfce);
    }

    void imprimirComprovantePagCartaoEsta(boolean reimpressao) {
        runTask((dialog, printer) -> {
            final BitmapFactory.Options optionsStone = new BitmapFactory.Options();
            optionsStone.inScaled = false;

            // Logo Stone
            final AssetManager assetManagerStone = getApplicationContext().getAssets();
            final Bitmap bitmapStone = BitmapFactory.decodeStream(assetManagerStone.open("stone.png"),
                    null, optionsStone);
            final int widthStone = Objects.requireNonNull(bitmapStone).getWidth();
            final int heightStone = bitmapStone.getHeight();
            final int[] argbStone = new int[widthStone * heightStone];
            bitmapStone.getPixels(argbStone, 0, widthStone, 0, 0, widthStone, heightStone);
            bitmapStone.recycle();

            //printer.reset();
            printer.printImage(argbStone, widthStone, heightStone, Printer.ALIGN_CENTER, true);
            printer.feedPaper(0);

            // Reimpressao
            final BitmapFactory.Options optionsReimpressao = new BitmapFactory.Options();
            optionsReimpressao.inScaled = false;
            final AssetManager assetManagerReimpressao = getApplicationContext().getAssets();
            final Bitmap bitmapReimpressao = BitmapFactory.decodeStream(assetManagerReimpressao.open("reimpressao.png"),
                    null, optionsReimpressao);
            final int widthReimpressao = Objects.requireNonNull(bitmapReimpressao).getWidth();
            final int heightReimpressao = bitmapReimpressao.getHeight();
            final int[] argbReimpressao = new int[widthReimpressao * heightReimpressao];
            bitmapReimpressao.getPixels(argbReimpressao, 0, widthReimpressao, 0, 0, widthReimpressao, heightReimpressao);
            bitmapReimpressao.recycle();

            //Unidades unidades;
            //elementosUnidade = bd.getUnidades();
            AutorizacoesPinpad pinpad = bd.getAutorizacaoPinpad();

            //
            String txtCompPag = "{br}" + tamFont + "Via do Lojista{br}{br}";
            printer.printTaggedText(txtCompPag);
            printer.feedPaper(38);

            //
            if (reimpressao) {
                printer.printImage(argbReimpressao, widthReimpressao, heightReimpressao, Printer.ALIGN_CENTER, true);
                printer.feedPaper(0);
            }

            //
            String txtCompPag2 = tamFont + cAux.removerAcentos(pinpad.getNomeEmpresa()) + "{br}" +
                    tamFont + cAux.removerAcentos(pinpad.getEnderecoEmpresa()) + "{br}" +
                    tamFont + cAux.exibirData(pinpad.getDate()) + " " + pinpad.getTime() + " CNPJ:" + pinpad.getCnpjEmpresa() + "{br}" +
                    tamFont + "------------------------------------------{br}" +
                    pinpad.getTypeOfTransactionEnum() + "                       RS " + cAux.maskMoney(cAux.converterValores(pinpad.getAmount())).trim() + "{br}" +
                    tamFont + "------------------------------------------{br}" +
                    tamFont + pinpad.getCardBrand() + " - " + pinpad.getCardHolderNumber().substring(pinpad.getCardHolderNumber().length() - 8) + "  AUT: " + pinpad.getAuthorizationCode() + "{br}" +
                    tamFont + pinpad.getCardHolderName() + "{br}" +
                    tamFont + pinpad.getRecipientTransactionIdentification() + "{br}" +
                    tamFont + "Aprovado com senha{br}" +
                    tamFont + "SN: " + prefs.getString("serial_app", "") + " - " + BuildConfig.VERSION_NAME + "{br}";
            //printer.reset();
            printer.printTaggedText(txtCompPag2);
            printer.feedPaper(120);

            /*String txtCompPag1 = tamFont + "CREDITO                       {right}{b}{h}R{w}$ 50,00{br}";
            //printer.reset();
            printer.printTaggedText(txtCompPag1);*/
            printer.flush();

        }, R.string.msg_printing_comp_pag_cartao_nfce);
    }

    void ComprovantePixReimpressao() {
        // --------------------     COMPROVANTE PIX         --------------------------------------------

        //
        LinearLayoutCompat MdelComprovantePix = findViewById(R.id.modelComprovantePix);
        TextView txtEmpresaPix, txtCNPJPix, txtBancoPix, txtValorPix, txtNPedido,
                txtIdentificadoPix, txtDataHoraPix, txtReimpressaoPix;

        txtEmpresaPix = findViewById(R.id.txtEmpresaPix);
        txtCNPJPix = findViewById(R.id.txtCNPJPix);
        txtBancoPix = findViewById(R.id.txtBancoPix);
        txtValorPix = findViewById(R.id.txtValorPix);
        txtNPedido = findViewById(R.id.txtNPedido);
        txtIdentificadoPix = findViewById(R.id.txtIdentificadoPix);
        txtDataHoraPix = findViewById(R.id.txtDataHoraPix);
        txtReimpressaoPix = findViewById(R.id.txtReimpressaoPix);
        PrintPixDomain printPixDomain;
        if (!impressao_pix) {
            txtReimpressaoPix.setVisibility(View.VISIBLE);
            printPixDomain = bd.ultimoPIXFinanceiroNfce();
        } else {
            printPixDomain = bd.ultimoPIX();
        }

        elementosUnidade = bd.getUnidades();
        unidades = elementosUnidade.get(0);
        ClassAuxiliar aux = new ClassAuxiliar();


        txtEmpresaPix.setText(unidades.getRazao_social());
        txtCNPJPix.setText("CNPJ: " + maskCnpj(unidades.getCnpj()));
        txtBancoPix.setText(unidades.getBanco_pix());
        txtValorPix.setText(aux.maskMoney(new BigDecimal(printPixDomain.valor)));
        txtNPedido.setText(printPixDomain.id_pedido);
        txtIdentificadoPix.setText(printPixDomain.id_cobranca_pix);
        //aux.exibirData(printPixDomain.data) + " - " + printPixDomain.hora
        txtDataHoraPix.setText(prefs.getString("DataHoraPix", ""));
        // GERAR IMAGEM DE IMPRESSÃO

        PrintViewHelper printViewHelper = new PrintViewHelper();
        Bitmap bitmapCabecalhoNFCe = printViewHelper.createBitmapFromView(MdelComprovantePix, 140, 150);

        //
        final int widthStone = Objects.requireNonNull(bitmapCabecalhoNFCe).getWidth();
        final int heightStone = bitmapCabecalhoNFCe.getHeight();
        final int[] argbStone = new int[widthStone * heightStone];
        bitmapCabecalhoNFCe.getPixels(argbStone, 0, widthStone, 0, 0, widthStone, heightStone);
        bitmapCabecalhoNFCe.recycle();

        runTask((dialog, printer) -> {
            final BitmapFactory.Options optionsStone = new BitmapFactory.Options();
            optionsStone.inScaled = false;
            String strPix = "";
            if (!impressao_pix) {
                strPix = tamFont + "          REIMPRESSAO" + "{br}";
            }

            //printer.reset();
            printer.printImage(argbStone, widthStone, heightStone, Printer.ALIGN_CENTER, true);
            printer.feedPaper(0);


            //
            /*String txtCompPag2 = tamFont + "{br}" +
                    tamFont + unidades.getRazao_social() + "{br}" +
                    tamFont + unidades.getCnpj() + "{br}" +
                    tamFont + "{br}" +
                    tamFont + "Banco: " + "{br}" +
                    tamFont + "-----------------------------------------{br}" +
                    tamFont + "        Comprovante Pix" + "{br}" +
                    tamFont + "-----------------------------------------{br}" +
                    tamFont + "        Via do Lojista" + "{br}" +
                    strPix +
                    tamFont + "Pedido: " + printPixDomain.id_pedido + "{br}" +
                    tamFont + "Identificador: {br}" + printPixDomain.id_cobranca_pix + "{br}" +
                    tamFont + "Valor: " + aux.maskMoney(new BigDecimal(printPixDomain.valor)) + "{br}" +
                    tamFont + "Data/Hora: " + aux.exibirData(printPixDomain.data) + " - " + printPixDomain.hora + "{br}";*/
            //printer.reset();
            //printer.printText(txtCompPag2);
            printer.feedPaper(120);
            printer.flush();

            finish();

        }, R.string.msg_printing_comp_pag_pix);
    }

    void tempoImprCompViaCli() {
        // ESPERA 2.3 SEGUNDOS PARA  SAIR DO SPLASH
        new Handler().postDelayed(() -> {

            if (impComPagViaCliente) {
                imprimirComprovantePagCartao();
                tempoImprCompViaEsta(6000, false);
                //finalizarImpressao();
            } else {
                tempoImprCompViaCli();
            }
        }, 3000);
    }

    void tempoImprCompViaEsta(long tempo, boolean reimpressao) {
        // ESPERA 2.3 SEGUNDOS PARA  SAIR DO SPLASH
        new Handler().postDelayed(() -> {

            imprimirComprovantePagCartaoEsta(reimpressao);
            finalizarImpressao();
        }, tempo);
    }

    // IMPRESSÃO COMPROVANTE DO CANCELAMENTO
    void imprimirComprovanteCancelCartaoCliente() {
        runTask((dialog, printer) -> {
            final BitmapFactory.Options optionsStone = new BitmapFactory.Options();
            optionsStone.inScaled = false;

            // Logo Stone
            final AssetManager assetManagerStone = getApplicationContext().getAssets();
            final Bitmap bitmapStone = BitmapFactory.decodeStream(assetManagerStone.open("stone.png"),
                    null, optionsStone);
            final int widthStone = Objects.requireNonNull(bitmapStone).getWidth();
            final int heightStone = bitmapStone.getHeight();
            final int[] argbStone = new int[widthStone * heightStone];
            bitmapStone.getPixels(argbStone, 0, widthStone, 0, 0, widthStone, heightStone);
            bitmapStone.recycle();

            //printer.reset();
            printer.printImage(argbStone, widthStone, heightStone, Printer.ALIGN_CENTER, true);
            printer.feedPaper(0);

            // Reimpressao
            final BitmapFactory.Options optionsReimpressao = new BitmapFactory.Options();
            optionsReimpressao.inScaled = false;
            final AssetManager assetManagerReimpressao = getApplicationContext().getAssets();
            final Bitmap bitmapReimpressao = BitmapFactory.decodeStream(assetManagerReimpressao.open("cancelamento.png"),
                    null, optionsReimpressao);
            final int widthReimpressao = Objects.requireNonNull(bitmapReimpressao).getWidth();
            final int heightReimpressao = bitmapReimpressao.getHeight();
            final int[] argbReimpressao = new int[widthReimpressao * heightReimpressao];
            bitmapReimpressao.getPixels(argbReimpressao, 0, widthReimpressao, 0, 0, widthReimpressao, heightReimpressao);
            bitmapReimpressao.recycle();

            AutorizacoesPinpad pinpad = bd.getAutorizacaoPinpad();

            //
            String txtCompPag = "{br}" + tamFont + "Via do Cliente{br}{br}";
            printer.printTaggedText(txtCompPag);
            printer.feedPaper(38);


            printer.printImage(argbReimpressao, widthReimpressao, heightReimpressao, Printer.ALIGN_CENTER, true);
            printer.feedPaper(0);


            //
            String txtCompPag2 = tamFont + "Data/Hora Cancelamento " + dataHoraCan + "{br}" +
                    tamFont + pinpad.getCardBrand() + " - " + pinpad.getCardHolderNumber().substring(pinpad.getCardHolderNumber().length() - 8) + "{br}" +
                    tamFont + pinpad.getCardHolderName() + "{br}" +
                    tamFont + "------------------------------------------{br}" +
                    tamFont + cAux.removerAcentos(pinpad.getNomeEmpresa()) + "{br}" +
                    tamFont + "CNPJ:" + pinpad.getCnpjEmpresa() + "       AUT: " + codAutCan + "{br}" +
                    tamFont + pinpad.getRecipientTransactionIdentification() + "{br}" +
                    tamFont + "Serial: " + prefs.getString("serial_app", "") + " | " + BuildConfig.VERSION_NAME + "{br}" +
                    tamFont + "------------------------------------------{br}" +
                    pinpad.getTypeOfTransactionEnum() + "                       RS " + cAux.maskMoney(cAux.converterValores(pinpad.getAmount())).trim() + "{br}" +
                    tamFont + "------------------------------------------{br}";
            //printer.reset();
            printer.printTaggedText(txtCompPag2);
            printer.feedPaper(120);

            /*String txtCompPag1 = tamFont + "CREDITO                       {right}{b}{h}R{w}$ 50,00{br}";
            //printer.reset();
            printer.printTaggedText(txtCompPag1);*/
            printer.flush();

        }, R.string.msg_printing_comp_pag_cartao_nfce);
    }

    //
    void imprimirComprovanteCancelCartaoEstabelecimento() {
        runTask((dialog, printer) -> {
            final BitmapFactory.Options optionsStone = new BitmapFactory.Options();
            optionsStone.inScaled = false;

            // Logo Stone
            final AssetManager assetManagerStone = getApplicationContext().getAssets();
            final Bitmap bitmapStone = BitmapFactory.decodeStream(assetManagerStone.open("stone.png"),
                    null, optionsStone);
            final int widthStone = Objects.requireNonNull(bitmapStone).getWidth();
            final int heightStone = bitmapStone.getHeight();
            final int[] argbStone = new int[widthStone * heightStone];
            bitmapStone.getPixels(argbStone, 0, widthStone, 0, 0, widthStone, heightStone);
            bitmapStone.recycle();

            //printer.reset();
            printer.printImage(argbStone, widthStone, heightStone, Printer.ALIGN_CENTER, true);
            printer.feedPaper(0);

            // Reimpressao
            final BitmapFactory.Options optionsReimpressao = new BitmapFactory.Options();
            optionsReimpressao.inScaled = false;
            final AssetManager assetManagerReimpressao = getApplicationContext().getAssets();
            final Bitmap bitmapReimpressao = BitmapFactory.decodeStream(assetManagerReimpressao.open("cancelamento.png"),
                    null, optionsReimpressao);
            final int widthReimpressao = Objects.requireNonNull(bitmapReimpressao).getWidth();
            final int heightReimpressao = bitmapReimpressao.getHeight();
            final int[] argbReimpressao = new int[widthReimpressao * heightReimpressao];
            bitmapReimpressao.getPixels(argbReimpressao, 0, widthReimpressao, 0, 0, widthReimpressao, heightReimpressao);
            bitmapReimpressao.recycle();

            AutorizacoesPinpad pinpad = bd.getAutorizacaoPinpad();

            //
            String txtCompPag = "{br}" + tamFont + "Via do Lojista{br}{br}";
            printer.printTaggedText(txtCompPag);
            printer.feedPaper(38);


            printer.printImage(argbReimpressao, widthReimpressao, heightReimpressao, Printer.ALIGN_CENTER, true);
            printer.feedPaper(0);


            //
            String txtCompPag2 = tamFont + "Data/Hora Cancelamento " + dataHoraCan + "{br}" +
                    tamFont + pinpad.getCardBrand() + " - " + pinpad.getCardHolderNumber().substring(pinpad.getCardHolderNumber().length() - 8) + "{br}" +
                    tamFont + pinpad.getCardHolderName() + "{br}" +
                    tamFont + "------------------------------------------{br}" +
                    tamFont + cAux.removerAcentos(pinpad.getNomeEmpresa()) + "{br}" +
                    tamFont + "CNPJ:" + pinpad.getCnpjEmpresa() + "       AUT: " + codAutCan + "{br}" +
                    tamFont + pinpad.getRecipientTransactionIdentification() + "{br}" +
                    tamFont + "Serial: " + prefs.getString("serial_app", "") + " | " + BuildConfig.VERSION_NAME + "{br}" +
                    tamFont + "------------------------------------------{br}" +
                    pinpad.getTypeOfTransactionEnum() + "                       RS " + cAux.maskMoney(cAux.converterValores(pinpad.getAmount())).trim() + "{br}" +
                    tamFont + "------------------------------------------{br}";
            //printer.reset();
            printer.printTaggedText(txtCompPag2);
            printer.feedPaper(120);

            /*String txtCompPag1 = tamFont + "CREDITO                       {right}{b}{h}R{w}$ 50,00{br}";
            //printer.reset();
            printer.printTaggedText(txtCompPag1);*/
            printer.flush();

        }, R.string.msg_printing_comp_pag_cartao_nfce);
    }


    void tempoImprCompCancelEsta(long tempo) {
        new Handler().postDelayed(() -> {
            imprimirComprovanteCancelCartaoEstabelecimento();
            finalizarImpressao();
        }, tempo);
    }

    private void finalizarImpressao() {
        Intent i = new Intent(Impressora.this, Principal.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("nomeImpressoraBlt", enderecoBlt);
        i.putExtra("enderecoBlt", enderecoBlt);
        startActivity(i);
        finish();
    }

    // -------------------- IMPRESSÃO DE NFe 80 E 58 mm ---------------------------

    // ** NF-e 80mm
    private void printNFE(final String[] texto) {
        //Log.d(LOG_TAG, "Print NFC-e");

        runTask((dialog, printer) -> {
            printer.reset();

            tamFont = "{s}";
            String linhas = "---------------";

            elementosUnidade = bd.getUnidades();
            unidades = elementosUnidade.get(0);

            StringBuilder textBuffer;
            textBuffer = new StringBuilder();

            //DANFE NF-e
            textBuffer.append("{reset}").append(tamFont).append(linhas).append("------------------------------------------------{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append("Recebemos de ").append(prefs.getString("nome", "")).append(" os produtos constantes da NF-e ").append(prefs.getString("nnf", "")).append(" Serie ").append(prefs.getString("serie", "")).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("_________________________________________  _____/_____/________{br}");
            textBuffer.append("{reset}").append(tamFont).append(linhas).append("------------------------------------------------{br}");

            // IMPRIMIR CABECALHO
            printer.reset();
            printer.printTaggedText(textBuffer.toString());
            printer.feedPaper(38);

            textBuffer.append("{reset}").append(tamFont).append(linhas).append("------------------------------------------------{br}");

            textBuffer = new StringBuilder();
            textBuffer.append("{reset}{center}").append(tamFont).append("N ").append(prefs.getString("nnf", "")).append("               -               SERIE ").append(prefs.getString("serie", "")).append("{br}");


            String c = prefs.getString("chave", "");// texto[6];
            String cl1 = c.substring(0, 4) + " " + c.substring(4, 8) + " " + c.substring(8, 12) + " " + c.substring(12, 16) + " " + c.substring(16, 20) + " " + c.substring(20, 24);
            String cl2 = c.substring(24, 28) + " " + c.substring(28, 32) + " " + c.substring(32, 36) + " " + c.substring(36, 40) + " " + c.substring(40, 44);
            textBuffer.append(cl1).append("{br}");
            textBuffer.append(cl2).append("{br}");

            // IMPRIMIR CABECALHO
            printer.reset();
            printer.printTaggedText(textBuffer.toString());
            printer.feedPaper(38);

            // IMPRIMIR COD BARRA
            Bitmap bp = null;
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(prefs.getString("chave", ""), BarcodeFormat.CODE_128, 400, 120);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                bp = barcodeEncoder.createBitmap(bitMatrix);

                //SaveImage(bp);
            } catch (WriterException e) {
                e.printStackTrace();
            }

            final int width = Objects.requireNonNull(bp).getWidth();
            final int height = bp.getHeight();
            final int[] argb = new int[width * height];
            bp.getPixels(argb, 0, width, 0, 0, width, height);
            bp.recycle();

            printer.reset();
            printer.printImage(argb, width, height, Printer.ALIGN_CENTER, true);
            printer.feedPaper(38);

            textBuffer = new StringBuilder();
            textBuffer.append("{reset}{center}").append(tamFont).append(linhas).append("------------------------------------------------{br}");
            //
            textBuffer.append("{reset}{center}").append("DADOS DO EMITENTE{br}");
            textBuffer.append("{reset}").append(tamFont).append("NOME/RAZAO SOCIAL:{br}");
            textBuffer.append("{reset}").append(tamFont).append(texto[7]).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("ENDERECO:{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(texto[9]).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(texto[10]).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(texto[11]).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(texto[8]).append("{br}");
            //
            textBuffer.append("{reset}{center}").append(tamFont).append(linhas).append("------------------------------------------------{br}");
            //
            textBuffer.append("{reset}{center}").append("DESTINATARIO{br}");
            textBuffer.append("{reset}").append(tamFont).append("NOME/RAZAO SOCIAL:{br}");
            textBuffer.append("{reset}").append(tamFont).append(prefs.getString("nome", "")).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("ENDERECO:{br}");
            textBuffer.append("{reset}").append(tamFont).append(prefs.getString("endereco_dest", "")).append("{br}");
            textBuffer.append("{reset}").append(tamFont).append("CNPJ:    IE:     {br}");
            textBuffer.append("{reset}").append(tamFont).append(prefs.getString("cnpj_dest", "")).append("          -          ").append(prefs.getString("ie_dest", "")).append("{br}");
            //
            textBuffer.append("{reset}{center}").append(tamFont).append(linhas).append("------------------------------------------------{br}");
            //
            textBuffer.append("{reset}{center}").append(tamFont).append("NATUREZA DA OPRACAO{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(prefs.getString("nat_op", "")).append("{br}");
            //
            textBuffer.append("{reset}{center}").append(tamFont).append(linhas).append("------------------------------------------------{br}");
            //
            textBuffer.append("{reset}{center}").append("PRODUTOS{br}");
            textBuffer.append("{reset}{center}").append("DESC PROD | UN | QTD | VL UN | VL TOTAL{br}");

            textBuffer.append("{reset}{center}").append(prefs.getString("prods_nota", "")).append("{br}{br}");
            textBuffer.append("{reset}{center}").append("VALOR TOTAL DA NOTA => ").append(prefs.getString("total_nota", "")).append("{br}");
            //
            textBuffer.append("{reset}{center}").append(tamFont).append(linhas).append("------------------------------------------------{br}");
            //
            textBuffer.append("{reset}{center}").append("DADOS ADICIONAIS{br}");
            /*
            "( DECLARAMOS QUE OD PRODUTOS ESTAO ADEQUADAMENTE " +
                    "ACONDICIONADOS E ESTIVADOS PARA SUPORTAR OS RISCOS NORMAIS DAS ETAPAS NECESSARIAS A OPERACAO " +
                    "DE TRANSPORTE (CARREGAMENTO, DESCARREGAMENTO, TRANSBORDO E TRANSPORTE) E QUE ATENDEM A REGULAMENTACAO " +
                    "EM VIGOR. DATA: " + cAux.exibirDataAtual() + " .. . .. DECLARAMOS QUE A EXPEDICAO NAO CONTEM EMBALAGENS VAZIAS E " +
                    "NAO LIMPAS DE PRODUTOS PERIGOSOS QUE APRESENTAM VALOR DE QUANTIDADE LIMITADA POR VEICULO (" +
                    "COLUNA 8 DA RELACAO DE PRODUTOS PERIGOSOS) IGUAL A ZERO. DATA: " + cAux.exibirDataAtual() + " .. . .. {br}ASSINATURA: " +
                    "__________________________________________________ {br}" +
            * */
            textBuffer.append("{reset}{center}").append(tamFont).append(prefs.getString("inf_cpl", "")).append("{br}");
            //
            textBuffer.append("{reset}{center}").append(tamFont).append(linhas).append("------------------------------------------------{br}");
            //
            textBuffer.append("{reset}{center}").append(tamFont).append("RESERVADO AO FISCO{br}{br}{br}");
            //


            printer.reset();
            printer.printTaggedText(textBuffer.toString());
            printer.feedPaper(38);

            printer.flush();

            Intent i = new Intent(Impressora.this, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("nomeImpressoraBlt", enderecoBlt);
            i.putExtra("enderecoBlt", enderecoBlt);
            startActivity(i);
            finish();
        }, R.string.msg_printing_nfce);
    }

    // ** NF-e 58mm
    private void printNFE58mm(final String[] texto) {
        //Log.d(LOG_TAG, "Print NFC-e");

        runTask((dialog, printer) -> {
            printer.reset();

            //tamFont = "{s}";
            String linhas = "";

            elementosUnidade = bd.getUnidades();
            unidades = elementosUnidade.get(0);

            StringBuilder textBuffer;
            textBuffer = new StringBuilder();

            //DANFE NF-e
            textBuffer.append(tamFont).append(" {br}");
            textBuffer.append(tamFont).append(linhas).append("-----------------------------------------{br}");
            textBuffer.append(tamFont).append("Recebemos de ").append(prefs.getString("nome", "")).append(" os produtos constantes da NF-e ").append(prefs.getString("nnf", "")).append(" Serie ").append(prefs.getString("serie", "")).append("{br}");
            textBuffer.append(tamFont).append("{br}");
            textBuffer.append(tamFont).append(linhas).append("___________________ _____/_____/________{br}");
            textBuffer.append(tamFont).append(linhas).append("-----------------------------------------{br}");

            textBuffer.append(tamFont).append(linhas).append("-----------------------------------------{br}");

            //textBuffer = new StringBuilder();
            textBuffer.append(tamFont).append("N ").append(prefs.getString("nnf", "")).append("          -          SERIE ").append(prefs.getString("serie", "")).append("{br}");


            String c = prefs.getString("chave", "");// texto[6];
            String cl1 = c.substring(0, 4) + " " + c.substring(4, 8) + " " + c.substring(8, 12) + " " + c.substring(12, 16) + " " + c.substring(16, 20) + " " + c.substring(20, 24);
            String cl2 = c.substring(24, 28) + " " + c.substring(28, 32) + " " + c.substring(32, 36) + " " + c.substring(36, 40) + " " + c.substring(40, 44);
            textBuffer.append(cl1).append("{br}");
            textBuffer.append(cl2).append("{br}");

            // IMPRIMIR CABECALHO
            printer.reset();
            printer.selectPageMode();
            printer.setPageXY(0, 0);
            printer.setAlign(1);
            printer.printTaggedText(textBuffer.toString(), "UTF-8");
            printer.feedPaper(38);

            // IMPRIMIR COD BARRA
            Bitmap bp = null;
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(prefs.getString("chave", ""), BarcodeFormat.CODE_128, 400, 120);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                bp = barcodeEncoder.createBitmap(bitMatrix);

                //SaveImage(bp);
            } catch (WriterException e) {
                e.printStackTrace();
            }

            final int width = Objects.requireNonNull(bp).getWidth();
            final int height = bp.getHeight();
            final int[] argb = new int[width * height];
            bp.getPixels(argb, 0, width, 0, 0, width, height);
            bp.recycle();

            // IMPRIMIR CÓDIGO DE BARRAS
            printer.reset();
            printer.printImage(argb, width, height, Printer.ALIGN_CENTER, true);
            printer.feedPaper(38);

            textBuffer = new StringBuilder();
            textBuffer.append(tamFont).append("");
            textBuffer.append(tamFont).append(linhas).append("{br}-----------------------------------------{br}");
            //
            textBuffer.append("DADOS DO EMITENTE{br}");
            textBuffer.append(tamFont).append("NOME/RAZAO SOCIAL:{br}");
            textBuffer.append(tamFont).append(texto[7]).append("{br}");
            textBuffer.append(tamFont).append("ENDERECO:{br}");
            textBuffer.append(tamFont).append(texto[9]).append("{br}");
            textBuffer.append(tamFont).append(texto[10]).append("{br}");
            textBuffer.append(tamFont).append(texto[11]).append("{br}");
            textBuffer.append(tamFont).append(texto[8]).append("{br}");
            //
            textBuffer.append(tamFont).append(linhas).append("-----------------------------------------{br}");
            //
            textBuffer.append("DESTINATARIO{br}");
            textBuffer.append(tamFont).append("NOME/RAZAO SOCIAL:{br}");
            textBuffer.append(tamFont).append(prefs.getString("nome", "")).append("{br}");
            textBuffer.append(tamFont).append("ENDERECO:{br}");
            textBuffer.append(tamFont).append(prefs.getString("endereco_dest", "")).append("{br}");
            textBuffer.append(tamFont).append("CNPJ:    IE:     {br}");
            textBuffer.append(tamFont).append(prefs.getString("cnpj_dest", "")).append("     -     ").append(prefs.getString("ie_dest", "")).append("{br}");
            //
            textBuffer.append(tamFont).append(linhas).append("-----------------------------------------{br}");
            //
            textBuffer.append(tamFont).append("NATUREZA DA OPRACAO{br}");
            textBuffer.append(tamFont).append(prefs.getString("nat_op", "")).append("{br}");
            //
            textBuffer.append(tamFont).append(linhas).append("-----------------------------------------{br}");
            //
            textBuffer.append("PRODUTOS{br}");
            textBuffer.append(tamFont).append("DESC PROD | UN | QTD | VL UN | VL TOTAL{br}");
            textBuffer.append(tamFont).append(prefs.getString("prods_nota", "")).append("{br}{br}");
            textBuffer.append(tamFont).append("VALOR TOTAL DA NOTA => ").append(prefs.getString("total_nota", "")).append("{br}");
            //
            textBuffer.append(tamFont).append(linhas).append("-----------------------------------------{br}");
            //
            textBuffer.append("DADOS ADICIONAIS{br}");
            /*
            "( DECLARAMOS QUE OS PRODUTOS ESTAO ADEQUADAMENTE " +
                    "ACONDICIONADOS E ESTIVADOS PARA SUPORTAR OS RISCOS NORMAIS DAS ETAPAS NECESSARIAS A OPERACAO " +
                    "DE TRANSPORTE (CARREGAMENTO, DESCARREGAMENTO, TRANSBORDO E TRANSPORTE) E QUE ATENDEM A REGULAMENTACAO " +
                    "EM VIGOR. DATA: " + cAux.exibirDataAtual() + " .. . .. DECLARAMOS QUE A EXPEDICAO NAO CONTEM EMBALAGENS VAZIAS E " +
                    "NAO LIMPAS DE PRODUTOS PERIGOSOS QUE APRESENTAM VALOR DE QUANTIDADE LIMITADA POR VEICULO (" +
                    "COLUNA 8 DA RELACAO DE PRODUTOS PERIGOSOS) IGUAL A ZERO. DATA: " + cAux.exibirDataAtual() + " .. . .. {br}ASSINATURA: " +
                    "____________________________________________________________________ {br}" +
            * */
            textBuffer.append(tamFont).append(prefs.getString("inf_cpl", "")).append("{br}");
            //
            textBuffer.append(tamFont).append(linhas).append("-----------------------------------------{br}");
            //
            textBuffer.append(tamFont).append("RESERVADO AO FISCO{br}{br}{br}");
            //

            // IMPRIMIR CORPO NFE
            printer.reset();
            printer.selectPageMode();
            printer.setPageXY(0, 0);
            printer.setAlign(1);
            printer.printTaggedText(textBuffer.toString(), "UTF-8");
            printer.feedPaper(38);

            printer.flush();

            Intent i = new Intent(Impressora.this, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("nomeImpressoraBlt", enderecoBlt);
            i.putExtra("enderecoBlt", enderecoBlt);
            startActivity(i);
            finish();
        }, R.string.msg_printing_nfce);
    }

    // -------------------- IMPRESSÃO DE RELATÓRIOS 80 E 58 mm ---------------------------

    // ** RELATÓRIO 80mm
    private void printRelatorioNFCE() {

        runTask((dialog, printer) -> {
            Log.d(LOG_TAG, "Print Relatório NFC-e");
            printer.reset();

            String serie = bd.getSeriePOS();
            elementosUnidade = bd.getUnidades();
            unidades = elementosUnidade.get(0);
            StringBuilder textBuffer = new StringBuilder();

            //String tamFont = "";
            //if (prefs.getString("tamPapelImpressora", "").equalsIgnoreCase("58mm")) {
            // tamFont = "{s}";
            //}

            int posicaoNota;

            //IMPRIMIR CABEÇALHO
            textBuffer.append("{reset}{center}").append(tamFont).append(unidades.getRazao_social()).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append("CNPJ: ").append(unidades.getCnpj()).append(" I.E.: ").append(unidades.getIe()).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(unidades.getEndereco()).append(", ").append(unidades.getNumero()).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append(unidades.getBairro()).append(", ").append(unidades.getCidade()).append(", ").append(unidades.getUf()).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append("CEP: ").append(unidades.getCep()).append("  ").append(unidades.getTelefone()).append("{br}");

            textBuffer.append("{reset}{center}").append(tamFont).append("------------------------------------------------{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append("Serie: ").append(serie).append("  ").append(unidades.getTelefone()).append("{br}");
            textBuffer.append("{reset}{center}").append(tamFont).append("------------------------------------------------{br}");

            textBuffer.append("{reset}{center}").append(tamFont).append("NFC-e").append("{br}{br}");

            // TOTAL DE PRODUTOS
            int totalProdutos = 0;
            int totalProdutosNFE = 0;

            //DADOS DAS NOTAS
            for (int n = 0; n < elementosPedidos.size(); n++) {

                posicaoNota = n + 1;

                //DADOS DOS PEDIDO
                pedidos = elementosPedidos.get(n);
                elementosItens = bd.getItensPedido(pedidos.getId());
                itensPedidos = elementosItens.get(0);

                String dataEmissao = cAux.exibirData(pedidos.getData());
                String horaEmissao = pedidos.getHora();

                //
                textBuffer.append("{reset}{center}").append(tamFont).append("Numero:").append(pedidos.getId()).append("      Emissao:").append(dataEmissao).append(" ").append(horaEmissao).append("{br}");
                textBuffer.append("{reset}{center}").append(tamFont).append("Protocolo: ").append(pedidos.getProtocolo().equals(" ") ? "EMITIDA EM CONTIGENCIA" : pedidos.getProtocolo()).append("{br}");
                textBuffer.append("{reset}{center}").append(tamFont).append("Chave: ").append("{br}");
                //textBuffer.append("{reset}{center}").append(tamFont).append(bd.gerarChave(Integer.parseInt(pedidos.getId()))).append("{br}");

                String c = bd.gerarChave(Integer.parseInt(pedidos.getId()));
                String cl1 = c.substring(0, 4) + " " + c.substring(4, 8) + " " + c.substring(8, 12) + " " + c.substring(12, 16) + " " + c.substring(16, 20);
                String cl2 = c.substring(20, 24) + " " + c.substring(24, 28) + " " + c.substring(28, 32) + " " + c.substring(32, 36);
                textBuffer.append("{reset}{center}").append(tamFont).append(cl1).append("{br}");
                textBuffer.append("{reset}{center}").append(tamFont).append(cl2).append("{br}");

                //
                linhaProduto = new String[]{
                        "1 " + itensPedidos.getProduto() + "      " + bd.getProduto(itensPedidos.getProduto()) + "\n",
                        "" + itensPedidos.getQuantidade() + "     " + "UN     " + cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(itensPedidos.getValor())))) + "   " +
                                cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(pedidos.getValor_total())))) + "\n"
                };

                //IMPRIMIR TEXTO
                textBuffer.append("{reset}{left}").append(tamFont).append("# CODIGO DESCRICAO QTDE. UN.  VL.UNIT.  VL.TOTAL{br}");
                textBuffer.append("{reset}{left}").append(tamFont).append(linhaProduto[0]);
                textBuffer.append("{reset}{right}").append(tamFont).append(linhaProduto[1]);
                textBuffer.append("{reset}{center}").append(tamFont).append("------------------------------------------------{br}");

                try {
                    String[] sum = {String.valueOf(n), "1"};
                    imprimindo.setText(String.valueOf(cAux.somar(sum)));
                } catch (Exception ignored) {

                }

                totalProdutos += Integer.parseInt(itensPedidos.getQuantidade());
            }

            textBuffer.append("{reset}{center}").append(tamFont).append("NF-e").append("{br}{br}");

            if (elementosPedidosNFE.size() > 0) {
                //DADOS DAS NOTAS NF-e
                for (int n = 0; n < elementosPedidosNFE.size(); n++) {

                    //DADOS DOS PEDIDO
                    pedidosNFE = elementosPedidosNFE.get(n);
                    elementosItens = bd.getItensPedidoNFE(pedidosNFE.getId());
                    itensPedidos = elementosItens.get(0);

                    String dataEmissao = pedidosNFE.getData();//cAux.exibirData(pedidosNFE.getData());
                    String horaEmissao = pedidosNFE.getHora();

                    //
                    textBuffer.append("{reset}{center}").append(tamFont).append("Numero:").append(pedidosNFE.getId()).append("      Emissao:").append(dataEmissao).append(" ").append(horaEmissao).append("{br}");
                    textBuffer.append("{reset}{center}").append(tamFont).append("Protocolo: ").append(pedidosNFE.getProtocolo()).append("{br}");
                    textBuffer.append("{reset}{center}").append(tamFont).append("Chave: ").append("{br}");
                    //textBuffer.append(tamFont).append(bd.gerarChave(Integer.parseInt(pedidos.getId()))).append("{br}");

                    String c = bd.gerarChave(Integer.parseInt(pedidosNFE.getId()));
                    String cl1 = c.substring(0, 4) + " " + c.substring(4, 8) + " " + c.substring(8, 12) + " " + c.substring(12, 16) + " " + c.substring(16, 20);
                    String cl2 = c.substring(20, 24) + " " + c.substring(24, 28) + " " + c.substring(28, 32) + " " + c.substring(32, 36);
                    textBuffer.append(tamFont).append(cl1).append("{br}");
                    textBuffer.append(tamFont).append(cl2).append("{br}");

                    //
                    linhaProduto = new String[]{
                            "1 " + itensPedidos.getProduto() + "      " + bd.getProduto(itensPedidos.getProduto()) + "\n",
                            "" + itensPedidos.getQuantidade() + "     " + "UN     " + cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(itensPedidos.getValor())))) + "   " +
                                    cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(pedidosNFE.getValor_total())))) + "\n"
                    };

                    //IMPRIMIR TEXTO
                    textBuffer.append("{reset}{left}").append(tamFont).append("# CODIGO DESC. QTDE. UN.  VL.UNIT.  VL.TOTAL{br}");
                    textBuffer.append("{reset}{left}").append(tamFont).append(linhaProduto[0]);
                    textBuffer.append("{reset}{right}").append(tamFont).append(linhaProduto[1]);
                    textBuffer.append("{reset}{center}").append(tamFont).append("-----------------------------------------{br}");

                    try {
                        String[] sum = {String.valueOf(n), "1"};
                        imprimindo.setText(String.valueOf(cAux.somar(sum)));
                    } catch (Exception ignored) {

                    }
                    totalProdutosNFE += Integer.parseInt(itensPedidos.getQuantidade());
                }
            }

            textBuffer.append(tamFont).append("{reset}Total de Produtos NFC-e: ").append(totalProdutos).append("{br}");
            textBuffer.append(tamFont).append("{reset}Total de Produtos NF-e: ").append(totalProdutosNFE).append("{br}");
            String[] somar = {String.valueOf(totalProdutos), String.valueOf(totalProdutosNFE)};
            textBuffer.append("{reset}Total de Produtos: ").append(Math.round(Float.parseFloat(String.valueOf(cAux.somar(somar))))).append("{br}");

            textBuffer.append("{reset}{br}");

            printer.reset();
            //printer.setAlign(1);
            printer.printTaggedText(textBuffer.toString());
            printer.feedPaper(100);
            printer.flush();

            //
            desativarBluetooth();

            Intent i = new Intent(Impressora.this, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("nomeImpressoraBlt", enderecoBlt);
            i.putExtra("enderecoBlt", enderecoBlt);
            startActivity(i);
            finish();

        }, R.string.msg_printing_relatorio);
    }

    // ** RELATÓRIO 58mm
    private void printRelatorioNFCE58mm() {

        runTask((dialog, printer) -> {
            Log.d(LOG_TAG, "Print Relatório NFC-e");
            printer.reset();

            String serie = bd.getSeriePOS();
            elementosUnidade = bd.getUnidades();
            unidades = elementosUnidade.get(0);
            StringBuilder textBuffer = new StringBuilder();
            StringBuilder textBufferCabecalho = new StringBuilder();

            int posicaoNota;

            //IMPRIMIR CABEÇALHO
            textBufferCabecalho.append(tamFont).append("{br}");
            textBufferCabecalho.append(tamFont).append(unidades.getRazao_social()).append("{br}");
            textBufferCabecalho.append(tamFont).append("CNPJ: ").append(unidades.getCnpj()).append(" I.E.: ").append(unidades.getIe()).append("{br}");
            textBufferCabecalho.append(tamFont).append(unidades.getEndereco()).append(", ").append(unidades.getNumero()).append("{br}");
            textBufferCabecalho.append(tamFont).append(unidades.getBairro()).append(", ").append(unidades.getCidade()).append(", ").append(unidades.getUf()).append("{br}");
            textBufferCabecalho.append(tamFont).append("CEP: ").append(unidades.getCep()).append("  ").append(unidades.getTelefone()).append("{br}");

            textBufferCabecalho.append(tamFont).append("-----------------------------------------{br}");
            textBufferCabecalho.append(tamFont).append("Serie: ").append(serie).append("  ").append(unidades.getTelefone()).append("{br}");
            textBufferCabecalho.append(tamFont).append("-----------------------------------------{br}");

            textBuffer.append(tamFont).append("{br}");
            textBuffer.append(tamFont).append("NFC-e").append("{br}{br}");

            // TOTAL DE PRODUTOS
            int totalProdutos = 0;
            int totalProdutosNFE = 0;

            //DADOS DAS NOTAS NFC-e
            if (elementosPedidos.size() > 0) {
                for (int n = 0; n < elementosPedidos.size(); n++) {

                    posicaoNota = n + 1;

                    //DADOS DOS PEDIDO
                    pedidos = elementosPedidos.get(n);
                    elementosItens = bd.getItensPedido(pedidos.getId());


                    Log.i("auxKle", pedidos.getData() + " - " + pedidos.getHora());

                    String dataEmissao = cAux.exibirData(pedidos.getData());
                    String horaEmissao = pedidos.getHora();

                    //
                    textBuffer.append(tamFont).append("Numero:").append(pedidos.getId()).append("   Emissao:").append(dataEmissao).append(" ").append(horaEmissao).append("{br}");
                    textBuffer.append(tamFont).append("Protocolo: ").append(pedidos.getProtocolo().equals(" ") ? "EMITIDA EM CONTIGENCIA" : pedidos.getProtocolo()).append("{br}");
                    textBuffer.append(tamFont).append("Chave: ");
                    //textBuffer.append(tamFont).append(bd.gerarChave(Integer.parseInt(pedidos.getId()))).append("{br}");

                    /*String c = bd.gerarChave(Integer.parseInt(pedidos.getId()));
                    String cl1 = c.substring(0, 4) + " " + c.substring(4, 8) + " " + c.substring(8, 12) + " " + c.substring(12, 16) + " " + c.substring(16, 20);
                    String cl2 = c.substring(20, 24) + " " + c.substring(24, 28) + " " + c.substring(28, 32) + " " + c.substring(32, 36);
                    textBuffer.append(tamFont).append(cl1);
                    textBuffer.append(tamFont).append(cl2).append("{br}");*/

                    String chaveNota = bd.gerarChave(Integer.parseInt(pedidos.getId()));
                    textBuffer.append(tamFont).append(chaveNota).append("{br}");

                    textBuffer.append(tamFont).append("# COD. PROD. QTD. UN.  V.UNI.  DESC  TOTAL{br}");
                    textBuffer.append(tamFont).append(n + 1);
                    for (int i = 0; i < elementosItens.size(); i++) {
                        itensPedidos = elementosItens.get(i);
                        String vUnit, vDesc, vTotal;
                        vUnit = cAux.maskMoney(new BigDecimal(itensPedidos.getValor()));
                        vDesc = cAux.maskMoney(new BigDecimal(itensPedidos.getDesconto()));
                        vTotal = cAux.maskMoney(new BigDecimal(itensPedidos.getTotal()));
                        textBuffer.append(tamFont).append("  ").append(itensPedidos.getProduto()).append("    ").append(bd.getProduto(itensPedidos.getProduto())).append("{br}");
                        textBuffer.append(tamFont).append("    ").append(itensPedidos.getQuantidade()).append("     ").append("UN     ").append(vUnit).append(" ").append(vDesc).append(" ").append(vTotal).append("{br}");

                        totalProdutos += Integer.parseInt(itensPedidos.getQuantidade());
                    }
                    //
                    /*linhaProduto = new String[]{
                            "1 " + itensPedidos.getProduto() + "      " + bd.getProduto(itensPedidos.getProduto()) + "\n",
                            "" + itensPedidos.getQuantidade() + "     " + "UN     " + cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(itensPedidos.getValor())))) + "   " +
                                    cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(pedidos.getValor_total())))) + "\n"
                    };

                    //IMPRIMIR TEXTO
                    textBuffer.append(tamFont).append(linhaProduto[0]);
                    textBuffer.append(tamFont).append(linhaProduto[1]);*/
                    textBuffer.append(tamFont).append("------------------------------------------{br}");

                    try {
                        String[] sum = {String.valueOf(n), "1"};
                        imprimindo.setText(String.valueOf(cAux.somar(sum)));
                    } catch (Exception ignored) {

                    }
                }
            }

            textBuffer.append(tamFont).append("NF-e").append("{br}{br}");

            if (elementosPedidosNFE.size() > 0) {
                //DADOS DAS NOTAS NF-e
                for (int n = 0; n < elementosPedidosNFE.size(); n++) {

                    //DADOS DOS PEDIDO
                    pedidosNFE = elementosPedidosNFE.get(n);
                    ArrayList<ItensPedidos> itensNFe = bd.getItensPedidoNFE(pedidosNFE.getId());

                    String dataEmissao = pedidosNFE.getData();
                    String horaEmissao = pedidosNFE.getHora();

                    //
                    textBuffer.append(tamFont).append("Numero:").append(pedidosNFE.getId()).append("      Emissao:").append(dataEmissao).append(" ").append(horaEmissao).append("{br}");
                    textBuffer.append(tamFont).append("Protocolo: ").append(pedidosNFE.getProtocolo()).append("{br}");
                    textBuffer.append(tamFont).append("Chave: ");
                    //textBuffer.append(tamFont).append(bd.gerarChave(Integer.parseInt(pedidos.getId()))).append("{br}");

                    /*String c = bd.gerarChave(Integer.parseInt(pedidosNFE.getId()));
                    String cl1 = c.substring(0, 4) + " " + c.substring(4, 8) + " " + c.substring(8, 12) + " " + c.substring(12, 16) + " " + c.substring(16, 20);
                    String cl2 = c.substring(20, 24) + " " + c.substring(24, 28) + " " + c.substring(28, 32) + " " + c.substring(32, 36);
                    textBuffer.append(tamFont).append(cl1).append("{br}");
                    textBuffer.append(tamFont).append(cl2).append("{br}");*/

                    String chaveNota = bd.gerarChave(Integer.parseInt(pedidosNFE.getId()));
                    textBuffer.append(tamFont).append(chaveNota).append("{br}");

                    textBuffer.append(tamFont).append("# COD. DESC. QTDE. UN.  VL.UNIT.  VL.TOTAL{br}");
                    textBuffer.append(tamFont).append(n + 1);

                    for (int i = 0; i < itensNFe.size(); i++) {
                        itensPedidos = itensNFe.get(i);
                        String vUnit, vTotal;
                        vUnit = cAux.maskMoney(new BigDecimal(itensPedidos.getValor()));
                        vTotal = cAux.maskMoney(new BigDecimal(itensPedidos.getTotal()));
                        textBuffer.append(tamFont).append("  ").append(itensPedidos.getProduto()).append("    ").append(bd.getProduto(itensPedidos.getProduto())).append("{br}");
                        textBuffer.append(tamFont).append("    ").append(itensPedidos.getQuantidade()).append("     ").append("UN     ").append(vUnit).append(" ").append(vTotal).append("{br}");

                        totalProdutosNFE += Integer.parseInt(itensPedidos.getQuantidade());
                    }

                    /*//
                    linhaProduto = new String[]{
                            "1 " + itensPedidos.getProduto() + "      " + bd.getProduto(itensPedidos.getProduto()) + "\n",
                            "" + itensPedidos.getQuantidade() + "     " + "UN     " + cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(itensPedidos.getValor())))) + "   " +
                                    cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(pedidosNFE.getValor_total())))) + "\n"
                    };

                    //IMPRIMIR TEXTO
                    textBuffer.append(tamFont).append(linhaProduto[0]);
                    textBuffer.append(tamFont).append(linhaProduto[1]);*/
                    textBuffer.append(tamFont).append("------------------------------------------{br}");

                    /*try {
                        //String[] sum = {String.valueOf(n), "1"};
                        //imprimindo.setText(String.valueOf(cAux.somar(sum)));
                    } catch (Exception ignored) {

                    }*/
                }
            }

            textBuffer.append(tamFont).append("Total de Produtos NFC-e: ").append(totalProdutos).append("{br}");
            textBuffer.append(tamFont).append("Total de Produtos NF-e: ").append(totalProdutosNFE).append("{br}");
            String[] somar = {String.valueOf(totalProdutos), String.valueOf(totalProdutosNFE)};
            textBuffer.append(tamFont).append("Total de Produtos: ").append(Math.round(Float.parseFloat(String.valueOf(cAux.somar(somar))))).append("{br}");

            textBuffer.append("{br}");

            printer.reset();
            printer.selectPageMode();
            printer.setPageXY(0, 0);
            //
            printer.setAlign(1);
            printer.printTaggedText(textBufferCabecalho.toString());

            //
            printer.setAlign(0);
            printer.printTaggedText(textBuffer.toString());
            printer.feedPaper(100);
            printer.flush();

            desativarBluetooth();

            Intent i = new Intent(Impressora.this, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("nomeImpressoraBlt", enderecoBlt);
            i.putExtra("enderecoBlt", enderecoBlt);
            startActivity(i);
            finish();

        }, R.string.msg_printing_relatorio);
    }
}
