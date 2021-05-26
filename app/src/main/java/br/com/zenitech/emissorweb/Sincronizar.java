package br.com.zenitech.emissorweb;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.StatFs;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.Sincronizador;
import br.com.zenitech.emissorweb.interfaces.ISincronizar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Sincronizar extends AppCompatActivity {

    final String TAG = "Sincronizar";
    private SharedPreferences prefs;
    DatabaseHelper db;
    ArrayList<Pedidos> pedidos;
    private DownloadManager mgr = null;
    private long lastDownload = -1L;
    public static final int REQUEST_PERMISSIONS_CODE = 128;
    VerificarOnline online;
    AlertDialog alerta;
    EditText serial;
    TextView txtTotMemoria, txt_msg_sincronizando, txtAppFinalizado;
    LinearLayout ll_sincronizar, ll_sincronizando, ll_sucesso, ll_erro;
    Context context;
    boolean erro = false;
    String msgErro = "", msgErroTec;
    FloatingActionButton fabWhatsapp;
    Configuracoes configuracoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronizar);

        //
        context = this;
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        online = new VerificarOnline();
        fabWhatsapp = findViewById(R.id.fabWhatsapp);
        txt_msg_sincronizando = findViewById(R.id.txt_msg_sincronizando);
        txtAppFinalizado = findViewById(R.id.txtAppFinalizado);
        ll_sincronizar = findViewById(R.id.ll_sincronizar);
        ll_sincronizando = findViewById(R.id.ll_sincronizando);
        ll_sucesso = findViewById(R.id.ll_sucesso);
        ll_erro = findViewById(R.id.ll_erro);
        txtTotMemoria = findViewById(R.id.txtTotMemoria);
        serial = findViewById(R.id.serial);

        //
        if (!Objects.requireNonNull(prefs.getString("serial_app", "")).equalsIgnoreCase("")) {
            serial.setEnabled(false);
            txtAppFinalizado.setVisibility(View.VISIBLE);
        }
        serial.setText(prefs.getString("serial_app", ""));
        _verificarTotalArmazenamento();

        //
        mgr = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(onNotificationClick,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

        //
        findViewById(R.id.btn_sincronizar).setOnClickListener(view -> _iniciarVerificacoes());

        //
        fabWhatsapp.setOnClickListener(view -> enviarWhatsApp(msgErro + "\n\n" + (msgErroTec != null ? "Info.: " + msgErroTec : "")));

        //
        _limparDadosSincronizacao(true);

        //
        /*File dbfile = new File("data/data/br.com.zenitech.emissorweb/databases", "emissorwebDB");

        /SE O BANCO NÃO EXISITR
        if (dbfile.exists()) {
            Intent i = new Intent(Sincronizar.this, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }*/

        configuracoes = new Configuracoes();

        if (!configuracoes.GetDevice()) {
            if (prefs.getBoolean("mostrar_alerta_versao", true)) {
                _verificarVersaoAtual();
            }
        }

        findViewById(R.id.btnResetApp).setOnClickListener(view -> {
            prefs.edit().putBoolean("reset", true).apply();
            //
            Intent i = new Intent(this, SplashScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(onComplete);
        unregisterReceiver(onNotificationClick);
    }

    // VERIFICA O TOTAL DE ARMAZENAMENTO DO APARELHO
    void _verificarTotalArmazenamento() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable;
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        } else {
            bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        }
        long megAvailable = bytesAvailable / (1024 * 1024);
        if (megAvailable < 50) {
            txtTotMemoria.setText("Atenção:\nSeu aparelho está com pouca memória! \nPara um bom funcionamento do App Emissor, libere mais espaço na memória interna o quanto antes.");
        }
        Log.e(TAG, "Available MB : " + megAvailable);
    }

    // INICIA AS VERIFICAÇÕES DO SINCRONISMO
    void _iniciarVerificacoes() {
        erro = false;
        fabWhatsapp.setVisibility(View.GONE);
        txtTotMemoria.setText("");

        // ESCONDE O TECLADO
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            //Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }

        // VERIFICA SE O USUÁRIO INSERIU O SERIAL
        if (serial.getText().toString().equals("") || serial.getText().toString().length() <= 8) {
            txtTotMemoria.setText(R.string.informe_um_serial);

        } else {
            // SE JÁ TIVER PERMISSÃO PARA MEMÓRIA INTERNA INICIA O SINCRONISMO
            if (_verificarPermissoes()) {
                //
                _limparDadosSincronizacao(true);

                if (online.isOnline(context)) {
                    txtAppFinalizado.setVisibility(View.GONE);
                    _iniciarSincronismo();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Verifique sua conexão com a internet!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                }
            }
        }
    }

    // VERIFICA AS PERMISSÕES DO APP
    boolean _verificarPermissoes() {
        //VERIFICA SE O USUÁRIO DEU PERMISSÃO PARA ACESSAR O SDCARD
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            /*if (ActivityCompat.shouldShowRequestPermissionRationale(Sincronizar.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                callDialog(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
            } else {
                ActivityCompat.requestPermissions(Sincronizar.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE);
            }*/

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE);

            return false;
        }

        return true;
    }

    // INICIA O PROCESSO DE SINCRONIZAR O BANCO DE DADOS
    void _iniciarSincronismo() {
        //
        txtTotMemoria.setText("");
        ll_sincronizar.setVisibility(View.GONE);
        ll_sincronizando.setVisibility(View.VISIBLE);

        //
        _verificarSerial();
    }

    // EXIBI A MENAGEM DE CONFIRMAÇÕES DAS PERMISSÕES
    private void callDialog(final String[] permissions) {
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("Permissão");
        //define a mensagem
        builder.setMessage("Conceder Permissão Para Acessar Dados Externos.");
        //define um botão como positivo
        builder.setPositiveButton("Conceder", (arg0, arg1) -> ActivityCompat.requestPermissions(Sincronizar.this, permissions, REQUEST_PERMISSIONS_CODE));
        //cria o AlertDialog
        alerta = builder.create();
        //Exibe
        alerta.show();
    }

    void _verificarSerial() {
        txt_msg_sincronizando.setText(R.string.verificando_serial);

        final ISincronizar iSincronizar = ISincronizar.retrofit.create(ISincronizar.class);

        final Call<Sincronizador> call = iSincronizar.verificarSerial("verificar_serial_emissor", serial.getText().toString());

        call.enqueue(new Callback<Sincronizador>() {
            @Override
            public void onResponse(@NonNull Call<Sincronizador> call, @NonNull Response<Sincronizador> response) {

                //
                final Sincronizador sincronizacao = response.body();
                if (!Objects.requireNonNull(sincronizacao).getErro().equalsIgnoreCase("erro")) {
                    gerarBancoOnline(serial.getText().toString());
                } else {
                    //
                    erro = true;
                    msgErro = "O serial é inválido ou já está sendo usado em outro aparelho! \nVerifique o serial e tente novamente.";
                    _limparDadosSincronizacao(false);
                    _resetarSincronismo(5000, true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Sincronizador> call, @NonNull Throwable t) {
                Log.i(TAG, Objects.requireNonNull(t.getMessage()));
                //
                erro = true;
                msgErro = "Serial inválido! Verifique o serial e tente novamente.";
                _limparDadosSincronizacao(false);
                _resetarSincronismo(5000, true);
            }
        });
    }

    public void gerarBancoOnline(final String serial) {
        //GERAR O BANCO ATUALIZADO ONLINE
        txt_msg_sincronizando.setText(R.string.gerando_banco_de_dados);

        //
        final ISincronizar iSincronizar = ISincronizar.retrofit.create(ISincronizar.class);
        final Call<Sincronizador> call = iSincronizar.sincronizar(serial);
        call.enqueue(new Callback<Sincronizador>() {
            @Override
            public void onResponse(@NonNull Call<Sincronizador> call, @NonNull Response<Sincronizador> response) {

                //
                final Sincronizador sincronizacao = response.body();
                if (sincronizacao != null) {
                    runOnUiThread(() -> {

                        if ("2".equals(sincronizacao.getErro())) {
                            //
                            erro = true;
                            msgErro = "Não foi possível gerar o banco de dados no app. \nNOTAS PENDENTES DE ENVIO NO EMISSOR WEB!";
                            _limparDadosSincronizacao(false);
                            _resetarSincronismo(10000, true);
                        } else {
                            startDownload(serial);
                        }
                    });
                } else {
                    //
                    erro = true;
                    msgErro = "Não foi possível gerar o banco.";
                    _limparDadosSincronizacao(false);
                    _resetarSincronismo(3000, true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Sincronizador> call, @NonNull Throwable t) {

                Log.i(TAG, Objects.requireNonNull(t.getMessage()));

                //
                erro = true;
                msgErro = "Não foi possível gerar o banco.";
                _limparDadosSincronizacao(false);
                _resetarSincronismo(3000, true);
            }
        });
    }

    // LIMPA OS DADOS DA SINCRONIZAÇÃO
    void _limparDadosSincronizacao(boolean apagarBanco) {
        //PEGA O CAMINHO DA PASTA DOWNLOAD DO APARELHO PARA VERIFICAR SE O BANCO EXISTE
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File arquivo = new File(path + "/emissorwebDB.db");
        //APAGA O BANCO DA PASTA DOWNLOADS
        if (arquivo.isFile()) arquivo.delete();

        // Retorna o caminho da imagem do qrcode
        File sdcard = Environment.getExternalStorageDirectory().getAbsoluteFile();
        File dir = new File(sdcard, "Emissor_Web/BD/emissorwebDB.db");
        dir.delete();

        // APAGAR BANCO DE DADOS IMPORTADO
        if (apagarBanco) {
            //APAGA O BANCO DE DADOS E VAI PARA TELA INICIAL DE SINCRONIZAÇÃO
            context.deleteDatabase("emissorwebDB");
        }
    }

    // MOSTRAR OS CAMPOS PARA SINCRONIZAR NOVAMENTE
    void _resetarSincronismo(long time, boolean erro) {
        if (erro) {
            txtTotMemoria.setText(msgErro);
            ll_erro.setVisibility(View.VISIBLE);
            ll_sincronizando.setVisibility(View.GONE);
            ll_sincronizar.setVisibility(View.GONE);
        }

        new Handler().postDelayed(() -> {
            txtTotMemoria.setText("");
            ll_erro.setVisibility(View.GONE);
            ll_sincronizando.setVisibility(View.GONE);
            ll_sincronizar.setVisibility(View.VISIBLE);

            if (erro) {
                if (!configuracoes.GetDevice()) {
                    fabWhatsapp.setVisibility(View.VISIBLE);

                    if (!prefs.getBoolean("introBtnWhats", false)) {
                        introducao();
                    }
                }
            }
        }, time);
    }

    public void startDownload(final String serial) {
        txt_msg_sincronizando.setText(R.string.fazendo_dowloand_do_banco);

        String url = new Configuracoes().GetUrlServer() + "/POSSIAC/banco_" + serial + ".db";
        Uri uri = Uri.parse(url);
        /*String p = String.valueOf();

        String nomeArquivo = "Emissor_Web/BD/";//emissorwebDB.db";
        String pasta = Environment.getExternalStorageDirectory() + "/" + nomeArquivo;
        Log.i(TAG, p);
        Log.i(TAG, pasta);*/

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();

        lastDownload = mgr.enqueue(new DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle("emissorwebDB")
                .setDescription("BD EMISSOR WEB.")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        "emissorwebDB.db"));
        // kleilson
        //importarBD();

    }

    public void queryStatus(View v) {
        Cursor c = mgr.query(new DownloadManager.Query().setFilterById(lastDownload));

        if (c == null) {
            Toast.makeText(this, "Download not found!", Toast.LENGTH_LONG).show();
        } else {
            c.moveToFirst();

            Log.d(getClass().getName(), "COLUMN_ID: " +
                    c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)));
            Log.d(getClass().getName(), "COLUMN_BYTES_DOWNLOADED_SO_FAR: " +
                    c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
            Log.d(getClass().getName(), "COLUMN_LAST_MODIFIED_TIMESTAMP: " +
                    c.getLong(c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)));
            Log.d(getClass().getName(), "COLUMN_LOCAL_URI: " +
                    c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
            Log.d(getClass().getName(), "COLUMN_STATUS: " +
                    c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)));
            Log.d(getClass().getName(), "COLUMN_REASON: " +
                    c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));

            Toast.makeText(this, statusMessage(c), Toast.LENGTH_LONG).show();
        }
    }

    public void viewLog(View v) {
        startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
    }

    private String statusMessage(Cursor c) {
        String msg;

        switch (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            case DownloadManager.STATUS_FAILED:
                msg = "Download failed!";
                break;

            case DownloadManager.STATUS_PAUSED:
                msg = "Download paused!";
                break;

            case DownloadManager.STATUS_PENDING:
                msg = "Download pending!";
                break;

            case DownloadManager.STATUS_RUNNING:
                msg = "Download in progress!";
                break;

            case DownloadManager.STATUS_SUCCESSFUL:
                msg = "Download complete!";
                break;

            default:
                msg = "Download is nowhere in sight";
                break;
        }

        return (msg);
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

            new Handler().postDelayed(() -> {
                //IMPORTAR BANCO DE DADOS
                importarBD();
            }, 2000);
        }
    };

    BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Toast.makeText(ctxt, "Baixando Banco de Dados...", Toast.LENGTH_LONG).show();
        }
    };

    // ATIVAR POS, INFORMA QUE O POS ESTÁ EM USO
    private void ativarPos() {
        txt_msg_sincronizando.setText(R.string.ativando_serial);

        final ISincronizar iSincronizar = ISincronizar.retrofit.create(ISincronizar.class);

        final Call<Sincronizador> call = iSincronizar.ativarDesativarPOS("ativar", serial.getText().toString());

        call.enqueue(new Callback<Sincronizador>() {
            @Override
            public void onResponse(@NonNull Call<Sincronizador> call, @NonNull Response<Sincronizador> response) {

                //
                final Sincronizador sincronizacao = response.body();
                if (sincronizacao != null) {
                    _limparDadosSincronizacao(false);
                    _finalizarSincronizacao();
                } else {
                    //
                    erro = true;
                    msgErro = "Não conseguimos ativar o app! Tente novamente em alguns instantes.";
                    _limparDadosSincronizacao(false);
                    _resetarSincronismo(3000, true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Sincronizador> call, @NonNull Throwable t) {
                Log.i(TAG, Objects.requireNonNull(t.getMessage()));
                //
                erro = true;
                msgErro = "Não conseguimos ativar o app! Tente novamente em alguns instantes.";
                _limparDadosSincronizacao(false);
                _resetarSincronismo(3000, true);
            }
        });
    }

    int totVer = 0;

    private void importarBD() {
        txt_msg_sincronizando.setText(R.string.inportando_banco);

        // ESPERA 1 SEGUNDOS PARA
        new Handler().postDelayed(() -> {

            //PEGA O CAMINHO DA PASTA DOWNLOAD DO APARELHO PARA VERIFICAR SE O BANCO EXISTE
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            //CRIA O ARQUIVO DO BANCO - POR ALGUM MOTIVO O BANCO É SALVO EM .TXT
            File arquivo = new File(path + "/emissorwebDB.db"); //.txt pasta);

            //SE O BANCO EXISTIR FAZ A IMPORTAÇÃO PARA O APP
            if (!arquivo.exists()) {

                if (totVer <= 50) {
                    totVer++;
                    //CHAMA A IMPORTAÇÃO NOVAMENTE
                    importarBD();
                } else {
                    erro = true;
                    msgErro = "Importação do banco de dados falhou! Tente novamente.";
                }

            } else {

                //CRIA UMA INSTANCIA DO BANCO
                db = new DatabaseHelper(context);
                try {
                    db.createDataBase();
                } catch (Exception ioe) {
                    erro = true;
                    msgErro = "Não foi possível criar o banco de dados!";
                    msgErroTec = ioe.getMessage();
                    throw new Error("Não foi possível criar o banco de dados!");
                }
                try {
                    db.openDataBase();
                } catch (SQLException sqle) {
                    Log.d(TAG, Objects.requireNonNull(sqle.getMessage()));
                    erro = true;
                    msgErro = "Não foi possível ler o banco de dados.";
                    msgErroTec = sqle.getMessage();
                }

                if (!erro) {
                    try {
                        prefs.edit().putInt("id_pedido", Integer.parseInt(db.ultimoIdPedido())).apply();
                        prefs.edit().putString("serial_app", serial.getText().toString()).apply();
                        ativarPos();
                    } catch (Exception e) {
                        msgErro = "Importação do banco de dados falhou! Tente novamente.";
                        msgErroTec = e.getMessage();
                        _limparDadosSincronizacao(true);
                        _resetarSincronismo(5000, true);
                        Log.d(TAG, Objects.requireNonNull(e.getMessage()));
                    }
                } else {
                    msgErro = "Importação do banco de dados falhou! Tente novamente.";
                    _limparDadosSincronizacao(true);
                    _resetarSincronismo(5000, true);
                }
            }

        }, 3000);
    }

    void _finalizarSincronizacao() {
        try {
            pedidos = db.getPedidos();
            new Handler().postDelayed(this::_sucesso, 2000);
        } catch (Exception e) {
            msgErro = "Importação do banco de dados falhou! Tente novamente.";
            msgErroTec = e.getMessage() + " - Metodo -> _finalizarSincronizacao()";
            Log.i(TAG, e.getMessage() + " - Metodo -> _finalizarSincronizacao()");
            _limparDadosSincronizacao(false);
            _resetarSincronismo(5000, true);
        }
    }

    void _sucesso() {
        txtTotMemoria.setText("");
        ll_sincronizar.setVisibility(View.GONE);
        ll_sincronizando.setVisibility(View.GONE);
        ll_sucesso.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            prefs.edit().putBoolean("sincronizado", true).apply();

            //ABRI A TELA PRINCIPAL
            Intent i = new Intent(Sincronizar.this, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

            finish();

        }, 2000);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                _iniciarVerificacoes();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (resultCode == RESULT_OK) {
                _iniciarVerificacoes();
            }
        }
    }

    private void introducao() {
        prefs.edit().putBoolean("introBtnWhats", true).apply();

        final SpannableString sassyDesc = new SpannableString("Toque aqui, para enviar informações sobre o erro ao suporte.");
        sassyDesc.setSpan(new StyleSpan(Typeface.ITALIC), 0, sassyDesc.length(), 0);


        // We have a sequence of targets, so lets build it!
        final TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        // BOTAO NOVO PEDIDO
                        TapTarget.forView(fabWhatsapp, "Encontrou um erro?", sassyDesc)
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.colorAccent)
                                .targetCircleColor(android.R.color.black)
                                .textColor(android.R.color.white)
                                .transparentTarget(true)
                                .id(1)
                )
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        //((TextView) findViewById(R.id.texto)).setText("Parabéns! Agora voce já sabe como usar o Emissor Web!");
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        final AlertDialog dialog = new AlertDialog.Builder(context)
                                .setTitle("Uh oh")
                                .setMessage("Você cancelou a seqüência")
                                .setPositiveButton("Sair", null).show();
                        TapTargetView.showFor(dialog,
                                TapTarget.forView(dialog.getButton(DialogInterface.BUTTON_POSITIVE), "Uh oh!", "Você cancelou a seqüência no passo " + lastTarget.id())
                                        .cancelable(false)
                                        .tintTarget(false), new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        super.onTargetClick(view);
                                        dialog.dismiss();
                                    }
                                });
                    }
                });

        sequence.start();
    }

    /*
    public void enviarWhatsApp_(String mensagem) {
        PackageManager pm = getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");

            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, mensagem);
            startActivity(waIntent);

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp não instalado", Toast.LENGTH_SHORT).show();
        }
    }

     */

    public void enviarWhatsApp(String mensagem) {
        if (online.isOnline(context)) {
            String msgWhats = "Erro, App Emissor: Serial(" + serial.getText().toString() + ").\n\nMsg.: " + mensagem;
            try {
                String toNumber = "+558498309990";

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + toNumber + "&text=" + msgWhats));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Verifique sua conexão com a internet!", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
    }

    // FINALIZAR REMESSA
    private void _verificarVersaoAtual() {
        //
        final ISincronizar iSincronizar = ISincronizar.retrofit.create(ISincronizar.class);

        final Call<Sincronizador> call = iSincronizar.verificarVersaoApp("verificar_versao_app");

        call.enqueue(new Callback<Sincronizador>() {
            @Override
            public void onResponse(@NonNull Call<Sincronizador> call, @NonNull Response<Sincronizador> response) {

                //
                final Sincronizador sincronizacao = response.body();
                if (sincronizacao != null) {

                    Log.i(TAG, sincronizacao.getErro());

                    if (!sincronizacao.getErro().equalsIgnoreCase(BuildConfig.VERSION_NAME)) {

                        _alertaNovaVersão();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Sincronizador> call, @NonNull Throwable t) {
                Log.i("ERRO_SIN", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void _alertaNovaVersão() {

        //
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("Ei, Psiu! Olha a novidade.   :)");
        String str = "O EmissorWeb Mobile, está ainda melhor! Clique e atualize!";
        //define a mensagem
        builder.setMessage(str);

        //define um botão como positivo
        builder.setPositiveButton("Atualizar", (arg0, arg1) -> {
            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });

        //define um botão como negativo.
        builder.setNeutralButton("Avise-me depois", (arg0, arg1) -> {
            Toast.makeText(context, "Ok, depois te avisaremos dessa novidade!", Toast.LENGTH_SHORT).show();
            //prefs.edit().putBoolean("mostrar_alerta_versao", false).apply();
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();
    }
}
