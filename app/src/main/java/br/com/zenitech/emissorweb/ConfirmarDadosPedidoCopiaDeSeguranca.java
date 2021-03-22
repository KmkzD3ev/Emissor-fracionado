package br.com.zenitech.emissorweb;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lvrenyang.io.BTPrinting;
import com.lvrenyang.io.IOCallBack;
import com.lvrenyang.io.Label;
import com.lvrenyang.io.Pos;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.domains.ValidarNFCe;
import br.com.zenitech.emissorweb.interfaces.IValidarNFCe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmarDadosPedidoCopiaDeSeguranca extends AppCompatActivity implements View.OnClickListener,
        IOCallBack {

    public static int nPrintWidth = 384;
    public static boolean bCutter = false;
    public static boolean bDrawer = false;
    public static boolean bBeeper = true;
    public static int nPrintCount = 1;
    public static int nCompressMethod = 0;
    public static boolean bAutoPrint = false;
    public static int nPrintContent = 1;
    public static boolean bCheckReturn = false;


    private ProgressDialog pd;
    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }

        }

    };
    private Context contexto;

    //int nPedido = 2526;

    //IMPRESSORA BLUETHOO
    private LinearLayout linearlayoutdevices;
    private ProgressBar progressBarSearchStatus;

    private BroadcastReceiver broadcastReceiver = null;
    private IntentFilter intentFilter = null;

    Button btnPrintNotaCont, btnPrint, btn_sair, btn_fechar;
    ConfirmarDadosPedidoCopiaDeSeguranca mActivity;

    ExecutorService es = Executors.newScheduledThreadPool(30);

    Pos mPos = new Pos();//MODELO 1
    BTPrinting mBt = new BTPrinting();
    //------------X-----------------//

    TextView cpfCnpj_cliente, formaPagamento, produto, qnt, vlt, vltTotal,
            statusNota, protocoloNota, dataHoraNota;


    public static String[] linhaProduto;

    //
    private SharedPreferences prefs;
    private SharedPreferences.Editor ed;
    private DatabaseHelper bd;
    int id = 0;
    private ClassAuxiliar classAuxiliar;
    private String total, valorUnit;


    private LinearLayout NSdadosPedidos, NSdadosNFCe;
    private RelativeLayout NSdadosNFCeCont;
    private FloatingActionButton fabPesqImp;

    ArrayList<Unidades> elementos;
    Unidades unidades;

    ArrayList<PosApp> elementosPos;
    PosApp posApp;

    private String idFormaPagamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar_dados_pedido);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActivity = this;

        /* 启动WIFI */
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

        NSdadosPedidos = (LinearLayout) findViewById(R.id.NSdadosPedidos);
        NSdadosNFCe = (LinearLayout) findViewById(R.id.NSdadosNFCe);
        NSdadosNFCeCont = (RelativeLayout) findViewById(R.id.NSdadosNFCeCont);
        statusNota = (TextView) findViewById(R.id.statusNota);
        protocoloNota = (TextView) findViewById(R.id.protocoloNota);
        dataHoraNota = (TextView) findViewById(R.id.dataHoraNota);

        //
        classAuxiliar = new ClassAuxiliar();

        //
        prefs = getSharedPreferences("preferencias", this.MODE_PRIVATE);
        ed = prefs.edit();

        //
        bd = new DatabaseHelper(this);

        elementos = bd.getUnidades();
        unidades = elementos.get(0);

        elementosPos = bd.getPos();
        posApp = elementosPos.get(0);

        //IMPRESSORA BLUETHOO

        progressBarSearchStatus = (ProgressBar) findViewById(R.id.progressBarSearchStatus);
        linearlayoutdevices = (LinearLayout) findViewById(R.id.linearlayoutdevices);
        fabPesqImp = (FloatingActionButton) findViewById(R.id.fabPesqImp);
        fabPesqImp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //consultar Bluetooth
                consultarBluetooth();
            }
        });

        mPos.Set(mBt);//MODELO 1
        mBt.SetCallBack(this);

        initBroadcast();

        //consultar Bluetooth
        consultarBluetooth();
        //------------X-----------------//

        contexto = ConfirmarDadosPedidoCopiaDeSeguranca.this;


        btnPrint = (Button) findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(this);

        btnPrintNotaCont = (Button) findViewById(R.id.btnPrintNotaCont);
        btnPrintNotaCont.setOnClickListener(this);

        btn_sair = (Button) findViewById(R.id.btn_sair);

        btn_fechar = (Button) findViewById(R.id.btn_fechar);
        btn_fechar.setOnClickListener(this);

        cpfCnpj_cliente = (TextView) findViewById(R.id.cpfCnpj_cliente);
        formaPagamento = (TextView) findViewById(R.id.formaPagamento);
        produto = (TextView) findViewById(R.id.produto);
        qnt = (TextView) findViewById(R.id.qnt);
        vlt = (TextView) findViewById(R.id.vlt);
        vltTotal = (TextView) findViewById(R.id.vltTotal);

        //
        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {

                //-------CRIA UM ID PARA O PEDIDO------//
                ed.putInt("id_pedido", (prefs.getInt("id_pedido", 0) + 1)).apply();
                id = prefs.getInt("id_pedido", 1);

                boolean siac = false;

                try {
                    if (params.getString("siac").equals("1")) {
                        siac = true;
                    }
                } catch (Exception e) {

                }

                if (siac) {

                    //DADOS SIAC WEB
                    cpfCnpj_cliente.setText("CONSUMIDOR NAO IDENTIFICADO");
                    produto.setText(params.getString("produto"));
                    qnt.setText(params.getString("quantidade"));
                    vlt.setText("R$" + classAuxiliar.maskMoney(new BigDecimal(params.getString("valor_unit"))));
                    idFormaPagamento = "1";
                    btn_sair.setVisibility(View.GONE);
                    btn_fechar.setVisibility(View.VISIBLE);

                } else {

                    cpfCnpj_cliente.setText(params.getString("cpfCnpj_cliente"));
                    formaPagamento.setText(params.getString("formaPagamento"));
                    produto.setText(params.getString("produto"));
                    qnt.setText(params.getString("qnt"));
                    vlt.setText(params.getString("vlt"));

                    //FORMAS DE PAGAMENTO
                    String s = params.getString("formaPagamento");
                    if (s.equals("DINHEIRO")) {
                        idFormaPagamento = "1";

                    } else if (s.equals("CHEQUE")) {
                        idFormaPagamento = "2";

                    } else if (s.equals("CARTÃO DE CRÉDITO")) {
                        idFormaPagamento = "3";

                    } else if (s.equals("CARTÃO DE DÉBITO")) {
                        idFormaPagamento = "4";

                    } else if (s.equals("CRÉDITO LOJA")) {
                        idFormaPagamento = "5";

                    } else if (s.equals("VALE ALIMENTAÇÃO")) {
                        idFormaPagamento = "6";

                    } else if (s.equals("VALE REFEIÇÃO")) {
                        idFormaPagamento = "7";

                    } else if (s.equals("VALE PRESENTE")) {
                        idFormaPagamento = "8";

                    } else if (s.equals("VALE COMBUSTÍVEL")) {
                        idFormaPagamento = "9";

                    } else if (s.equals("OUTROS")) {
                        idFormaPagamento = "10";

                    }

                }

                //
                valorUnit = String.valueOf(classAuxiliar.converterValores(vlt.getText().toString()));

                //MULTIPLICA O VALOR PELA QUANTIDADE
                String[] multiplicar = {valorUnit, qnt.getText().toString()};
                total = String.valueOf(classAuxiliar.multiplicar(multiplicar));
                vltTotal.setText("R$" + classAuxiliar.maskMoney(new BigDecimal(String.valueOf(total))));

            }
        }


        findViewById(R.id.btn_transmitir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //TRANSMITIR
                transmitirNota();

            }
        });

        findViewById(R.id.btn_reTransmitir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //TRANSMITIR
                transmitirNota();

            }
        });

        findViewById(R.id.btn_sair).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //desconecta
                es.submit(new TaskClose(mBt));

                Intent i = new Intent(contexto, Principal.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        });
    }

    private void transmitirNota() {
        //ESCODER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        //MOSTRA A MENSAGEM DE SINCRONIZAÇÃO
        pd = ProgressDialog.show(contexto, "Transmitindo...", "Aguarde um momento.",
                true, false);

        /*String t = String.valueOf(id) + "\n" +
        qnt.getText().toString() + "\n" +
                posApp.getSerial() + "\n" +
                bd.getIdProduto(produto.getText().toString()) + "\n" +
                String.valueOf(classAuxiliar.converterValores(vltTotal.getText().toString())) + "\n" +
                idFormaPagamento;

        Toast.makeText(contexto, t, Toast.LENGTH_LONG).show();*/

        //
        final IValidarNFCe iValidarNFCe = IValidarNFCe.retrofit.create(IValidarNFCe.class);

        //http://177.153.22.33/POSSIAC/AUTORIZADOR/AUTORIZADOR.php?PEDIDO=2527&QTD=5&SERIAL=123456789&PRODUTO=1&VLR=3800&FORMAP=1
        //String vu = String.valueOf(classAuxiliar.converterValores(valorUnit));


        //Toast.makeText(contexto, valorUnit, Toast.LENGTH_LONG).show();
        final Call<ValidarNFCe> call = iValidarNFCe.validarNota(
                String.valueOf(id),
                qnt.getText().toString(),
                posApp.getSerial(),
                bd.getIdProduto(produto.getText().toString()),
                valorUnit.replace(".", ""),
                idFormaPagamento,
                cpfCnpj_cliente.getText().toString(),
                "",
                "",
                ""
        );

        call.enqueue(new Callback<ValidarNFCe>() {
            @Override
            public void onResponse(Call<ValidarNFCe> call, Response<ValidarNFCe> response) {

                //
                final ValidarNFCe sincronizacao = response.body();
                if (sincronizacao != null) {

                    //
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                            if (pd != null && pd.isShowing()) {
                                pd.dismiss();
                            }

                            /*
                            Snackbar.make(v, sincronizacao.getProtocolo() + " - " + sincronizacao.getErro(), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            */

                            //es.submit(new TaskPrint(mLabel));

                            if (!sincronizacao.getProtocolo().isEmpty() && sincronizacao.getProtocolo().length() > 3) {

                                //
                                String data = classAuxiliar.exibirDataAtual();
                                String hora = classAuxiliar.horaAtual();
                                statusNota.setText("Autorizada");
                                protocoloNota.setText(sincronizacao.getProtocolo());
                                dataHoraNota.setText(classAuxiliar.exibirDataAtual() + " " + classAuxiliar.horaAtual());

                                //INSERI O PEDIDO NO BANCO DE DADOS
                                addPedido(
                                        "ON",
                                        protocoloNota.getText().toString(),
                                        classAuxiliar.inserirData(data),
                                        hora,
                                        total,
                                        classAuxiliar.soNumeros(classAuxiliar.inserirDataAtual()),
                                        classAuxiliar.soNumeros(classAuxiliar.horaAtual()),
                                        cpfCnpj_cliente.getText().toString(),
                                        "1"
                                );

                                NSdadosNFCe.setVisibility(View.VISIBLE);
                                NSdadosNFCeCont.setVisibility(View.GONE);
                                NSdadosPedidos.setVisibility(View.GONE);
                            } else {

                                /*
                                //
                                String data = classAuxiliar.exibirDataAtual();
                                String hora = classAuxiliar.horaAtual();
                                statusNota.setText("Em Contigencia");
                                protocoloNota.setText("Emitida em contigencia");
                                dataHoraNota.setText(data + " - " + hora);

                                //INSERI O PEDIDO NO BANCO DE DADOS
                                addPedido(
                                        "OFF",
                                        protocoloNota.getText().toString(),
                                        classAuxiliar.inserirData(data),
                                        hora,
                                        total,
                                        classAuxiliar.soNumeros(classAuxiliar.inserirDataAtual()),
                                        classAuxiliar.soNumeros(classAuxiliar.horaAtual()),
                                        cpfCnpj_cliente.getText().toString(),
                                        "1"
                                );
                                */

                                NSdadosNFCeCont.setVisibility(View.VISIBLE);
                                NSdadosNFCe.setVisibility(View.GONE);
                                NSdadosPedidos.setVisibility(View.GONE);
                            }

                            //Toast.makeText(contexto, sincronizacao.getProtocolo(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ValidarNFCe> call, Throwable t) {


                NSdadosNFCeCont.setVisibility(View.VISIBLE);
                NSdadosNFCe.setVisibility(View.GONE);
                NSdadosPedidos.setVisibility(View.GONE);
                        /*
                        Snackbar.make(v, "" + t, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        */

                Log.i("ERRO", "" + t);

                //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
            }
        });
    }

    private void addPedido(
            String status,
            String protocolo,
            String dataEmissao,
            String horaEmissao,
            String vlTotal,
            String dataProtocolo,
            String horaProtocolo,
            String cpf,
            String FPagamento
    ) {

        //double v = Double.parseDouble(total);
        //double v1 = bd.getTributosProduto(produto.getText().toString()) / 100;
        //double tributo = v - (v - (v1 * v));
        double tributo = bd.getTributosProduto(produto.getText().toString(), total);

        linhaProduto = new String[]{
                "1 1      " + produto.getText().toString() + "\n",
                "                   " + qnt.getText().toString() + "     " + "UN   " + classAuxiliar.maskMoney(new BigDecimal(valorUnit)) + "   " + classAuxiliar.maskMoney(new BigDecimal(total)) + "\n",
                classAuxiliar.maskMoney(new BigDecimal(total)),
                protocoloNota.getText().toString(),
                (cpfCnpj_cliente.getText().toString().equals("") ? "CONSUMIDOR NAO IDENTIFICADO" : cpfCnpj_cliente.getText().toString()),
                classAuxiliar.maskMoney(new BigDecimal(String.valueOf(tributo)))
        };

        //
        bd.addPedidos(new Pedidos(
                String.valueOf(id),//ID PEDIDO
                status,//SITUAÇÃO
                protocolo,//PROTOCOLO
                dataEmissao,//DATA EMISSÃO
                horaEmissao,//HORA EMISSÃO
                classAuxiliar.soNumeros(vlTotal),//VALOR TOTAL
                dataProtocolo,//DATA PROTOCOLO - "28042017"
                horaProtocolo,//HORA PROTOCOLO - "151540"
                cpf,//CPF/CNPJ CLIENTE
                FPagamento//FORMA PAGAMENTO
        ));

        //
        bd.addItensPedidos(new ItensPedidos(
                String.valueOf(id),//ID PEDIDO
                bd.getIdProduto(produto.getText().toString()),
                qnt.getText().toString(),
                classAuxiliar.soNumeros(String.valueOf(classAuxiliar.converterValores(vlt.getText().toString())))
        ));

        //Toast.makeText(getBaseContext(), classAuxiliar.soNumeros(vlTotal), Toast.LENGTH_LONG).show();
        //Toast.makeText(getBaseContext(), classAuxiliar.soNumeros(String.valueOf(classAuxiliar.converterValores(vlt.getText().toString()))), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uninitBroadcast();
        //btnDisconnect.performClick();
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
                    button.setText(name + ": " + address);

                    for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
                        Button btn = (Button) linearlayoutdevices.getChildAt(i);
                        if (btn.getText().equals(button.getText())) {
                            return;
                        }
                    }

                    button.setGravity(Gravity.CENTER_VERTICAL
                            | Gravity.LEFT);
                    button.setOnClickListener(new View.OnClickListener() {

                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            Toast.makeText(mActivity, "Conectando Impressora...",
                                    Toast.LENGTH_SHORT).show();
                            //btnSearch.setEnabled(false);
                            linearlayoutdevices.setEnabled(false);
                            for (int i = 0; i < linearlayoutdevices
                                    .getChildCount(); ++i) {
                                Button btn = (Button) linearlayoutdevices
                                        .getChildAt(i);
                                btn.setEnabled(false);
                            }
                            //btnDisconnect.setEnabled(false);
                            //btnPrint.setEnabled(false);
                            es.submit(new TaskOpen(mBt, address, mActivity));
                        }
                    });
                    button.getBackground().setAlpha(100);
                    linearlayoutdevices.addView(button);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED
                        .equals(action)) {
                    progressBarSearchStatus.setIndeterminate(true);
                    progressBarSearchStatus.setVisibility(View.VISIBLE);
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                        .equals(action)) {
                    progressBarSearchStatus.setIndeterminate(false);
                    progressBarSearchStatus.setVisibility(View.GONE);
                }

            }

        };
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void consultarBluetooth() {
        //desconecta
        es.submit(new TaskClose(mBt));

        try {

            //consulta
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (null == adapter) {
                finish();
            }

            if (!adapter.isEnabled()) {
                if (adapter.enable()) {
                    while (!adapter.isEnabled())
                        ;
                } else {
                    finish();
                }
            }

            adapter.cancelDiscovery();
            linearlayoutdevices.removeAllViews();
            adapter.startDiscovery();
        } catch (Exception e) {

        }
    }

    @Override
    public void onClick(View v) {

        //
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnPrint: {
                //es.submit(new TaskPrint(mLabel));
                //es.submit(new TaskPrintPOS(mPos));
                //double v0 = Double.parseDouble(total);
                //double v1 = bd.getTributosProduto(produto.getText().toString()) / 100;
                double tributo = bd.getTributosProduto(produto.getText().toString(), total);

                PackageManager packageManager = getPackageManager();
                String packageName = "br.com.zenitech.impressora";
                Intent i = packageManager.getLaunchIntentForPackage(packageName);
                i.putExtra("pedido", "" + id);
                i.putExtra("cliente", (!cpfCnpj_cliente.getText().toString().equals("") ? cpfCnpj_cliente.getText().toString() : "CONSUMIDOR NAO IDENTIFICADO"));
                i.putExtra("id_produto", "" + bd.getIdProduto(produto.getText().toString()));
                i.putExtra("produto", produto.getText().toString());
                i.putExtra("protocolo", (
                        protocoloNota.getText().toString().equals("EMITIDA EM CONTINGENCIA") ?
                                "EMITIDA EM CONTINGENCIA" :
                                protocoloNota.getText().toString() + " - " + dataHoraNota.getText().toString()
                ));
                i.putExtra("quantidade", qnt.getText().toString());
                i.putExtra("valor", "" + classAuxiliar.maskMoney(new BigDecimal(String.valueOf(total))));
                i.putExtra("valorUnit", "" + classAuxiliar.maskMoney(new BigDecimal(String.valueOf(valorUnit))));
                i.putExtra("tributos", "" + classAuxiliar.maskMoney(new BigDecimal(String.valueOf(tributo))));

                Log.e("LLL",
                        "pedido: " + id + "\n" +
                                "cliente: " + (!cpfCnpj_cliente.getText().toString().equals("") ? cpfCnpj_cliente.getText().toString() : "CONSUMIDOR NAO IDENTIFICADO") + "\n" +
                                "id_produto: " + bd.getIdProduto(produto.getText().toString()) + "\n" +
                                "produto: " + produto.getText().toString() + "\n" +
                                "protocolo: " + protocoloNota.getText().toString() + "\n" +
                                "quantidade: " + qnt.getText().toString() + "\n" +
                                "valor: " + "" + classAuxiliar.maskMoney(new BigDecimal(String.valueOf(total))) + "\n" +
                                "valorUnit: " + "" + classAuxiliar.maskMoney(new BigDecimal(String.valueOf(valorUnit))) + "\n" +
                                "tributos: " + "" + classAuxiliar.maskMoney(new BigDecimal(String.valueOf(tributo)))
                );
                if (null != i) {
                    startActivity(i);
                }
                break;
            }
            case R.id.btnPrintNotaCont: {


                //
                String data = classAuxiliar.exibirDataAtual();
                String hora = classAuxiliar.horaAtual();
                statusNota.setText("Em Contigencia");
                protocoloNota.setText("Emitida em contigencia");
                dataHoraNota.setText(data + " " + hora);

                //INSERI O PEDIDO NO BANCO DE DADOS
                addPedido(
                        "OFF",
                        protocoloNota.getText().toString(),
                        classAuxiliar.inserirData(data),
                        hora,
                        total,
                        classAuxiliar.soNumeros(classAuxiliar.inserirDataAtual()),
                        classAuxiliar.soNumeros(classAuxiliar.horaAtual()),
                        cpfCnpj_cliente.getText().toString(),
                        "1"
                );

                NSdadosNFCeCont.setVisibility(View.GONE);
                NSdadosNFCe.setVisibility(View.VISIBLE);
                NSdadosPedidos.setVisibility(View.GONE);
                break;
            }
            case R.id.btn_fechar: {
                finish();
                break;
            }
        }
    }

    private void uninitBroadcast() {
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
    }

    public class TaskOpen implements Runnable {
        BTPrinting bt = null;
        String address = null;
        Context context = null;

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

    static int dwWriteIndex = 1;

    public class TaskPrintPOS implements Runnable {
        Pos pos = null;

        public TaskPrintPOS(Pos pos) {
            this.pos = pos;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub

            final boolean bPrintResult = PrintTicketPOS(
                    nPrintWidth,
                    bCutter,
                    bDrawer,
                    bBeeper,
                    nPrintCount,
                    nPrintContent,
                    nCompressMethod,
                    bCheckReturn,
                    linhaProduto
            );
            final boolean bIsOpened = pos.GetIO().IsOpened();

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Toast.makeText(mActivity.getApplicationContext(), bPrintResult ? getResources().getString(R.string.printsuccess) : getResources().getString(R.string.printfailed), Toast.LENGTH_SHORT).show();
                    mActivity.btnPrint.setEnabled(bIsOpened);
                }
            });

        }

        public boolean PrintTicketPOS(
                int nPrintWidth,
                boolean bCutter,
                boolean bDrawer,
                boolean bBeeper,
                int nCount,
                int nPrintContent,
                int nCompressMethod,
                boolean bCheckReturn,
                String[] texto
        ) {
            boolean bPrintResult = false;

            byte[] status = new byte[1];
            if (!bCheckReturn || (bCheckReturn && pos.POS_QueryStatus(status, 3000, 2))) {

                //Bitmap bm1 = mActivity.getTestImage1(nPrintWidth, nPrintWidth);
                //Bitmap bm2 = mActivity.getTestImage2(nPrintWidth, nPrintWidth);
                //Bitmap bmBlackWhite = getImageFromAssetsFile("blackwhite.png");
                //Bitmap bmIu = getImageFromAssetsFile("iu.jpeg");
                //Bitmap bmYellowmen = getImageFromAssetsFile("yellowmen.png");

                Bitmap nfce = getImageFromAssetsFile("nfce.jpg");
                for (int i = 0; i < nCount; ++i) {
                    if (!pos.GetIO().IsOpened())
                        break;

                    if (nPrintContent >= 1) {

                        String serie = bd.getSeriePOS();

                        //IMPRIMIR CABEÇALHO
                        pos.POS_S_Align(1);
                        pos.POS_PrintPicture(nfce, 200, 1, nCompressMethod);
                        //
                        pos.POS_S_TextOut(unidades.getRazao_social() + "\r\n" +
                                "CNPJ: " + unidades.getCnpj() + " I.E.: " + unidades.getIe() + "\r\n" +
                                unidades.getEndereco() + ", nº " + unidades.getNumero() + "\r\n", 0, 0, 0, 0, 0);
                        //
                        pos.POS_S_TextOut(unidades.getBairro() + ", " + unidades.getCidade() + ", " + unidades.getUf() + "\r\n" +
                                "CEP: " + unidades.getCep() + "  " + unidades.getTelefone() + "\r\n", 0, 0, 0, 0, 0);

                        //DANFE NFC-e
                        pos.POS_S_TextOut("------------------------------------------------\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_TextOut("DANFE NFC-e - DOCUMENTO AUXILIAR DA NOTA FISCAL\r\n" +
                                "DE CONSUMIDOR ELETRONICA\n", 0, 0, 0, 0, 0);
                        pos.POS_S_TextOut("------------------------------------------------\r\n", 0, 0, 0, 0, 0);

                        //INFOR. PEDIDO
                        //pos.POS_FeedLine();
                        pos.POS_S_Align(0);

                        //IMPRIMIR TEXTO
                        pos.POS_S_TextOut("# CODIGO DESCRICAO QTDE. UN.  VL.UNIT.  VL.TOTAL\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_TextOut(texto[0], 0, 0, 0, 0, 0);
                        pos.POS_S_Align(2);
                        pos.POS_S_TextOut(texto[1], 0, 0, 0, 0, 0);
                        pos.POS_S_Align(0);
                        pos.POS_S_TextOut("------------------------------------------------\r\n", 0, 0, 0, 0, 0);

                        //INFOR
                        pos.POS_S_TextOut("Qtde. Total de Itens                           1\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_Align(2);
                        pos.POS_S_TextOut("Valor Total R$                           " + texto[2] + "\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_TextOut("FORMA DE PAGAMENTO                    VALOR PAGO\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_Align(2);
                        pos.POS_S_TextOut("Dinheiro                                 " + texto[2] + "\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_TextOut("------------------------------------------------\r\n", 0, 0, 0, 0, 0);

                        //TRIBUTOS TOTAIS
                        pos.POS_S_Align(0);
                        pos.POS_S_TextOut("Tributos totais incidentes\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_TextOut("(Lei Federal 12.741/2012)                " + texto[5] + "\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_TextOut("------------------------------------------------\r\n", 0, 0, 0, 0, 0);

                        //COSULTA NA RECEITA
                        pos.POS_S_Align(1);
                        pos.POS_S_TextOut("Numero:" + id + " Serie:" + serie + " Emissao:" + classAuxiliar.exibirDataAtual() + " " + classAuxiliar.horaAtual() + "\r\n", 0, 0, 0, 0, 0);//
                        pos.POS_S_TextOut("Consulte pela Chave de Acesso em\r\n", 0, 0, 0, 0, 0);
                        //pos.POS_S_TextOut("http://nfce.set.rn.gov.br/portalDFE/NFCe/ConsultaNFCe.aspx\r\n", 0, 0, 0, 0, 0);//
                        pos.POS_S_TextOut(unidades.getUrl_consulta() + "\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_TextOut("Chave de Acesso\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_TextOut(bd.gerarChave(id) + "\r\n", 0, 0, 0, 0, 0);//
                        //pos.POS_S_TextOut("2417 0408 2489 1600 0762 6500 1000 0027 8710 0002 7872\r\n", 0, 0, 0, 0, 0);//
                        pos.POS_S_TextOut("Protocolo de autorizacao:\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_TextOut(texto[3] + " - " + classAuxiliar.exibirDataAtual() + " " + classAuxiliar.horaAtual() + "\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_TextOut("------------------------------------------------\r\n", 0, 0, 0, 0, 0);

                        //CONSUMIDOR
                        pos.POS_S_TextOut(texto[4] + "\r\n", 0, 0, 0, 0, 0);//
                        pos.POS_S_TextOut("------------------------------------------------\r\n", 0, 0, 0, 0, 0);

                        //IMPRIMIR QRCode
                        String cod = "" +
                                "http://nfce.set.rn.gov.br/portalDFE/NFCe/mDadosNFCe.aspx?" +
                                "chNFe=" + bd.gerarChave(id) +
                                "&nVersao=100" +
                                "&tpAmb=1" +
                                "&dhEmi=323031372d30342d32345430363a30323a33362d30333a3030" +
                                "&vNF=" + classAuxiliar.converterValores(texto[2]) +
                                "&vICMS=0.00" +
                                "&digVal=325347523258674351746b374850304c3531556e37595562546e303d" +
                                "&cIdToken=000001" +
                                "&cHashQRCode=99F87D8EB34F1F622E2FD44BCF734DBC80F3E05D" +
                                "";
                        pos.POS_S_Align(1);
                        pos.POS_S_SetQRcode(cod, 5, 0, 3);

                        //pos.POS_S_TextOut("------------------------------------------------\r\n", 0, 0, 0, 0, 0);
                        pos.POS_S_TextOut(" \r\n\n\n", 0, 0, 0, 0, 0);
                        //IMPRIMIR LINHA EM VÁSIA
                        pos.POS_FeedLine();
                        /*
                            //pos.POS_S_TextOut("REC" + String.format("%03d", i) + "\r\nCaysn Printer\r\n测试页\r\n\r\n", 0, 1, 1, 0, 0x100);
                            //pos.POS_S_TextOut("扫二维码下载苹果APP\r\n", 0, 0, 0, 0, 0x100);

                            //IMPRIMIR LINHA EM VÁSIA
                            pos.POS_FeedLine();
                            //pos.POS_S_SetBarcode("20160618", 0, 72, 3, 60, 0, 2); //CÓDIGO DE BARRAS

                            //IMPRIMIR LINHA EM VÁSIA
                            pos.POS_FeedLine();
                         */
                    }

                    /*
                    if (nPrintContent >= 2) {
                        if (bm1 != null) {
                            pos.POS_PrintPicture(bm1, nPrintWidth, 1, nCompressMethod);
                        }
                        if (bm2 != null) {
                            pos.POS_PrintPicture(bm2, nPrintWidth, 1, nCompressMethod);
                        }
                    }

                    if (nPrintContent >= 3) {
                        if (bmBlackWhite != null) {
                            pos.POS_PrintPicture(bmBlackWhite, nPrintWidth, 1, nCompressMethod);
                        }
                        if (bmIu != null) {
                            pos.POS_PrintPicture(bmIu, nPrintWidth, 0, nCompressMethod);
                        }
                        if (bmYellowmen != null) {
                            pos.POS_PrintPicture(bmYellowmen, nPrintWidth, 0, nCompressMethod);
                        }
                    }
                    */
                }

                if (bBeeper)
                    pos.POS_Beep(1, 5);
                if (bCutter)
                    pos.POS_CutPaper();
                if (bDrawer)
                    pos.POS_KickDrawer(0, 100);

                if (bCheckReturn) {
                    int dwTicketIndex = dwWriteIndex++;
                    bPrintResult = pos.POS_TicketSucceed(dwTicketIndex, 30000);
                } else {
                    bPrintResult = pos.GetIO().IsOpened();
                }
            }

            return bPrintResult;
        }
    }


    //MODELO DE IMPRESSÃO ANTERIOR MODELO 2
    public class TaskPrint implements Runnable {
        Label label = null;

        public TaskPrint(Label label) {
            this.label = label;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub

            //final boolean bPrintResult = Prints.PrintTicket(getApplicationContext(), label, AppStart.nPrintWidth, AppStart.nPrintHeight, AppStart.nPrintCount);
            final boolean bPrintResult = Prints.PrintTicket(getApplicationContext(), label, 384, 110, 1, linhaProduto);
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
        BTPrinting bt = null;

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
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //btnDisconnect.setEnabled(true);
                //btnPrint.setEnabled(true);
                //btnSearch.setEnabled(false);
                linearlayoutdevices.setEnabled(false);
                for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
                    Button btn = (Button) linearlayoutdevices.getChildAt(i);
                    btn.setEnabled(false);
                }
                Toast.makeText(mActivity, "Impressora Conectada", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    public void OnOpenFailed() {
        // TODO Auto-generated method stub
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                //btnDisconnect.setEnabled(false);
                //btnPrint.setEnabled(false);
                //btnSearch.setEnabled(true);
                linearlayoutdevices.setEnabled(true);
                for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
                    Button btn = (Button) linearlayoutdevices.getChildAt(i);
                    btn.setEnabled(true);
                }
                Toast.makeText(mActivity, "Falha ao conectar a impressora", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void OnClose() {
        // TODO Auto-generated method stub
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                /*
                btnDisconnect.setEnabled(false);
                btnPrint.setEnabled(false);
                btnSearch.setEnabled(true);
                linearlayoutdevices.setEnabled(true);
                for (int i = 0; i < linearlayoutdevices.getChildCount(); ++i) {
                    Button btn = (Button) linearlayoutdevices.getChildAt(i);
                    btn.setEnabled(true);
                }
                */
            }
        });
    }


    /**
     * PARA PEGAR AS IMAGENS
     * 从Assets中读取图片
     */
    public Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;

    }

    public Bitmap getTestImage1(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, width, height, paint);

        paint.setColor(Color.BLACK);
        for (int i = 0; i < 8; ++i) {
            for (int x = i; x < width; x += 8) {
                for (int y = i; y < height; y += 8) {
                    canvas.drawPoint(x, y, paint);
                }
            }
        }
        return bitmap;
    }

    public Bitmap getTestImage2(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, width, height, paint);

        paint.setColor(Color.BLACK);
        for (int y = 0; y < height; y += 4) {
            for (int x = y % 32; x < width; x += 32) {
                canvas.drawRect(x, y, x + 4, y + 4, paint);
            }
        }
        return bitmap;
    }
}
