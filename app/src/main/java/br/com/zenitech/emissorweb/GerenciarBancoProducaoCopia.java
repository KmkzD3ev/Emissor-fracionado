package br.com.zenitech.emissorweb;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import br.com.zenitech.emissorweb.domains.Sincronizador;
import br.com.zenitech.emissorweb.ftps.MyFTPClientFunctions;
import br.com.zenitech.emissorweb.interfaces.ISincronizar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GerenciarBancoProducaoCopia extends AppCompatActivity {


    //
    private SharedPreferences prefs;
    ClassAuxiliar classAuxiliar;
    private DatabaseHelper bd;

    private static final String TAG = "Sincronizar";
    private Context context = null;

    private MyFTPClientFunctions ftpclient = null;

    private Button btnLoginFtp, btnUploadFile;
    private ProgressDialog pd;

    private String[] fileList;

    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            if (pd != null && pd.isShowing()) {
                try {
                    pd.dismiss();
                } catch (Exception e) {

                }

            }

            //SE A MENSAGEM FOR 0 FAZ A EXPORTAÇÃO DO BANCO
            if (msg.what == 0) {

                //EXPORTA O BANCO DE DADOS
                exportDB();

                //CARREGA AS PASTA DO SERVIDOR
                //getFTPFileList();
            } else if (msg.what == 1) {
                showCustomDialog(fileList);
            } else if (msg.what == 2) {
                Toast.makeText(context, "Banco enviado com sucesso!",
                        Toast.LENGTH_LONG).show();

                //APAGA O BANCO DE DADOS E VAI PARA TELA INICIAL DE SINCRONIZAÇÃO
               /* context.deleteDatabase("siacmobileDB");
                Intent i = new Intent(context, Sincronizar.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);*/

            } else if (msg.what == 3) {
                Toast.makeText(context, "Desconectado com sucesso!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Não é possível executar esta ação!",
                        Toast.LENGTH_LONG).show();
            }

        }

    };


    public static final int REQUEST_PERMISSIONS_CODE = 128;

    private AlertDialog alerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_banco_producao);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        prefs = getSharedPreferences("preferencias", this.MODE_PRIVATE);
        bd = new DatabaseHelper(context);
        ftpclient = new MyFTPClientFunctions();

        findViewById(R.id.btnSincronizarNotas).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CRIA UM DIRETÓRIO PARA BAIXAR O BANCO ONLINE
                if (getDirFromSDCard() == null) {
                    Toast.makeText(GerenciarBancoProducaoCopia.this, "Não foi possivél criar o Diretório!", Toast.LENGTH_LONG).show();
                }

                //VERIFICA SE O USUÁRIO DEU PERMISSÃO PARA ACESSAR O SDCARD
                if (ActivityCompat.checkSelfPermission(GerenciarBancoProducaoCopia.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    if (ActivityCompat.shouldShowRequestPermissionRationale(GerenciarBancoProducaoCopia.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        callDialog("Conceder Permissão Para Acessar Dados Externos.", new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});


                    } else {
                        ActivityCompat.requestPermissions(GerenciarBancoProducaoCopia.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE);
                    }
                }

                if (isOnline(context)) {
                    connectToFTPAddress();
                } else {
                    Toast.makeText(context,
                            "Verifique a sua Conexão à Internet!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void callDialog(String message, final String[] permissions) {

        //
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("Permissão");
        //define a mensagem
        builder.setMessage("Conceder Permissão Para Acessar a Memória Externa.");

        //define um botão como positivo
        builder.setPositiveButton("Conceder", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                //String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

                ActivityCompat.requestPermissions(GerenciarBancoProducaoCopia.this, permissions, REQUEST_PERMISSIONS_CODE);
                //Toast.makeText(InformacoesVagas.this, "positivo=" + arg1, Toast.LENGTH_SHORT).show();
            }
        });

        /*//define um botão como negativo.
        builder.setNegativeButton("Negativo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(InformacoesVagas.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
            }
        });*/

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();

        /*mMaterialDialog = new MaterialDialog(this)
                .setTitle("Permission")
                .setMessage(message)
                .setPositiveButton("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ActivityCompat.requestPermissions(Sincronizar.this, permissions, REQUEST_PERMISSIONS_CODE);
                        mMaterialDialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });
        mMaterialDialog.show();*/
    }

    private File getDirFromSDCard() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File sdcard = Environment.getExternalStorageDirectory()
                    .getAbsoluteFile();
            File dir = new File(sdcard, "Emissor_Web" + File.separator + "BD");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return dir;
        } else {
            return null;
        }
    }

    //exporting database
    private void exportDB() {
        // TODO Auto-generated method stub

        String currentDBPath = "//data//" + "br.com.zenitech.emissorweb"
                + "//databases//" + "emissorwebDB";
        String backupDBPath = "/Emissor_Web/BD/emissorwebDB.db";

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

                //
                /*Toast.makeText(getBaseContext(), backupDB.toString(),
                        Toast.LENGTH_LONG).show();*/


                try {
                    //ENVIA O BANCO PARA O SERVIDOR ONLINE
                    uploadDB();

                } catch (Exception e) {

                }
            }

        } catch (Exception e) {
            Log.i("BD", currentDBPath.toString());
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG)
                    .show();

        }
    }

    private void uploadDB() {
        pd = ProgressDialog.show(context, "Aguarde", "Enviando dados...",
                true, false);
        new Thread(new Runnable() {
            public void run() {
                boolean status = false;

                String nomeArquivo = "/Emissor_Web/BD/emissorwebDB.db";
                String pasta = Environment.getExternalStorageDirectory() + "/" + nomeArquivo;

                /**
                 MFTPClient:
                 objeto de conexão de cliente FTP (consulte Exemplo de conexão FTP)

                 SrcFilePath:
                 caminho do arquivo de origem no sdcard

                 desFileName: nome do arquivo a ser Armazenado no servidor FTP

                 desDirectory:
                 caminho do diretório onde o arquivo Ser carregado para
                 */

                //public boolean ftpUpload(
                // String srcFilePath,
                // String desFileName,
                // String desDirectory,
                // Context context
                // ) {

                //kleilson
                //ftpclient.ftpChangeDirectory("/public_html/apps/zenitech/siacmobile/");
                //ftpclient.ftpChangeDirectory("/html/sistemas/apps/siac_mobile");
                //ftpclient.ftpChangeDirectory("/");
                //ftpclient.ftpChangeDirectory("/bds_recebidos/");
                //ftpclient.ftpChangeDirectory("sistemas/apps/emissor_web/");
                ftpclient.ftpChangeDirectory("POSSIAC/envios/");

                //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                //String millisInString  = dateFormat.format(new Date());
                //-" + millisInString + "
                String serial = bd.getSerialPOS();
                status = ftpclient.ftpUpload(
                        pasta,
                        serial + ".db",
                        "/",
                        context
                );

                if (status == true) {

                    //SALVA OS DADOS DO USUÁRIO
                    //prefs.edit().putString("unidade_usuario", "").apply();

                    Log.d(TAG, "Dados enviado com sucesso!");
                    handler.sendEmptyMessage(2);

                    //Kleilson
                    //
                    //
                    final ISincronizar iSincronizar = ISincronizar.retrofit.create(ISincronizar.class);

                    final Call<Sincronizador> call = iSincronizar.ativarDesativarPOS("desativar", prefs.getString("serial_app", ""));

                    call.enqueue(new Callback<Sincronizador>() {
                        @Override
                        public void onResponse(Call<Sincronizador> call, Response<Sincronizador> response) {

                            //
                            final Sincronizador sincronizacao = response.body();
                            if (sincronizacao != null) {

                                //
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                       // Toast.makeText(getBaseContext(), "Ativo", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<Sincronizador> call, Throwable t) {


                            Log.i("ERRO_SIN", t.getMessage());

                            //Toast.makeText(getBaseContext(), "Não foi possível gerar o banco.", Toast.LENGTH_LONG).show();

                        }
                    });

                    //APAGA O BANCO DE DADOS E VAI PARA TELA INICIAL DE SINCRONIZAÇÃO
                    context.deleteDatabase("emissorwebDB");
                    Intent i = new Intent(context, Sincronizar.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                    //handler.sendEmptyMessage(2);
                } else {
                    Log.d(TAG, "Falha ao enviar os dados!");
                    handler.sendEmptyMessage(-1);
                }
            }
        }).start();
    }

    private void showCustomDialog(String[] fileList) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.custom);
        dialog.setTitle("/ Directory File List");

        TextView tvHeading = (TextView) dialog.findViewById(R.id.tvListHeading);
        tvHeading.setText(":: File List ::");

        if (fileList != null && fileList.length > 0) {
            ListView listView = (ListView) dialog
                    .findViewById(R.id.lstItemList);
            ArrayAdapter<String> fileListAdapter = new ArrayAdapter<String>(
                    context, android.R.layout.simple_list_item_1, fileList);
            listView.setAdapter(fileListAdapter);
        } else {
            tvHeading.setText(":: No Files ::");
        }

        Button dialogButton = (Button) dialog.findViewById(R.id.btnOK);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void connectToFTPAddress() {

        final String host = "177.153.22.33";
        final String username = "cloudftp";
        final String password = "N0v342Cl02d!";

        if (host.length() < 1) {
            Toast.makeText(context, "Please Enter Host Address!",
                    Toast.LENGTH_LONG).show();
        } else if (username.length() < 1) {
            Toast.makeText(context, "Please Enter User Name!",
                    Toast.LENGTH_LONG).show();
        } else if (password.length() < 1) {
            Toast.makeText(context, "Please Enter Password!",
                    Toast.LENGTH_LONG).show();
        } else {

            //
            pd = ProgressDialog.show(context, "", "Conectando...",
                    true, false);

            new Thread(new Runnable() {
                public void run() {
                    boolean status = false;
                    status = ftpclient.ftpConnect(host, username, password, 21);
                    if (status == true) {
                        Log.d(TAG, "Sucesso da conexão");
                        handler.sendEmptyMessage(0);
                    } else {
                        Log.d(TAG, "Falha na conexão");
                        handler.sendEmptyMessage(-1);
                    }
                }
            }).start();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                Intent i = new Intent(this, Principal.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }

        return false;
    }

}
