package br.com.zenitech.emissorweb;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import br.com.zenitech.emissorweb.domains.Sincronizador;
import br.com.zenitech.emissorweb.interfaces.ISincronizar;
import br.com.zenitech.emissorweb.util.ConnectionHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {
    //
    private SharedPreferences prefs;
    private SharedPreferences.Editor ed;
    //private DatabaseHelper bd;
    private int time = 2300;
    private Bundle params;
    TextView txtSerial;

    //@TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();
        txtSerial = findViewById(R.id.txtSerial);

        if (prefs.getBoolean("reset", false)) {
            //
            Intent i = new Intent(this, ResetApp.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
            return;
        }

        if(!prefs.getString("serial_app", "").equalsIgnoreCase("")){
            txtSerial.setVisibility(View.VISIBLE);
            txtSerial.setText(String.format("SERIAL\n%s", prefs.getString("serial_app", "")));
        }

        //
        //bd = new DatabaseHelper(this);

        //
        Intent intent = getIntent();

        if (intent != null) {
            params = intent.getExtras();

            if (params != null) {
                try {
                    if (Objects.requireNonNull(params.getString("siac")).equals("1")) {
                        time = 0;
                    }
                } catch (Exception ignored) {

                }
            }
        }

        /*ConnectionHelper.checkInternetConnection(isConnected -> {
            if (isConnected) {
                // Conexão com a internet está disponível
            } else {
                // Não foi possível conectar à internet
            }
        });*/

        // ESPERA 2.3 SEGUNDOS PARA  SAIR DO SPLASH
        /*new Handler().postDelayed(() -> {

            avancar();

        }, time);*/


        final ISincronizar iSincronizar = ISincronizar.retrofit.create(ISincronizar.class);

        final Call<Sincronizador> call = iSincronizar.forcarResetApp(
                "forcar_reset_app",
                prefs.getString("serial_app", "")
        );

        call.enqueue(new Callback<Sincronizador>() {
            @Override
            public void onResponse(@NonNull Call<Sincronizador> call, @NonNull Response<Sincronizador> response) {

                if(response.isSuccessful()) {
                    final Sincronizador sincronizacao = response.body();
                    if (Objects.requireNonNull(sincronizacao).getErro().equalsIgnoreCase("ok")) {
                        resetarApp();
                    } else {
                    //txtMsgReset.setText("Não foi possível resetar o App, verifique as informações e tente novamente.");
                    //erro();
                        avancar();
                    }
                }else{
                    // A requisição não foi bem-sucedida, trate o erro conforme necessário
                    try {
                        String errorMessage = "Erro: "+response.errorBody().string() + "\nMensagem: " + response.message() + "\n" + response.code();
                        Log.e("ResetApp", "Erro na requisição: " + errorMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("ResetApp", "Erro na requisição: ");
                    }
                    avancar(); // Ou realize outra ação
                }
            }

            @Override
            public void onFailure(@NonNull Call<Sincronizador> call, @NonNull Throwable t) {
                Log.i("ResetApp", Objects.requireNonNull(t.getMessage()));
                avancar();
            }
        });

    }

    private void clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
            } else {
                String packageName = getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear " + packageName);

                Toast.makeText(getBaseContext(), "O App foi resetado com sucesso!", Toast.LENGTH_LONG).show();
            }

            //prefs.edit().putBoolean("reset", false).apply();
            Intent i = new Intent(getBaseContext(), SplashScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

            finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void resetarApp() {
        clearAppData();
    }

    void avancar() {
        // ESPERA 2.3 SEGUNDOS PARA  SAIR DO SPLASH
        new Handler().postDelayed(() -> {

            //
            if (Objects.requireNonNull(prefs.getString("primeiro_acesso", "")).equals("")) {

                //CRIA UM DIRETÓRIO PARA BAIXAR O BANCO ONLINE
                if (getDirFromSDCard() == null) {
                    Toast.makeText(SplashScreen.this, "Não foi possivél criar o Diretório!", Toast.LENGTH_LONG).show();
                }

                //APLICA O PRIMEIRO ACESSO
                ed.putString("primeiro_acesso", "true").apply();

                //
                Intent i = new Intent(getBaseContext(), Sincronizar.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

            } else {

                try {

                    //VERIFICA SE O BANCO EXISTEbd.checkDataBase()
                    if (prefs.getBoolean("sincronizado", false)) {
                        //SE O BANCO ESXISTIR VAI PARA O LOGIN
                        Intent i;
                        if (time == 0) {

                            i = new Intent(getBaseContext(), GerenciarPagamentoCartaoPOS.class);
                            //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            //i.putExtra("produto", params.getString("produto"));
                            i.putExtra("siac", "1");
                            /*i.putExtra("quantidade", params.getString("quantidade"));
                            i.putExtra("produto", params.getString("produto"));
                            i.putExtra("valor_unit", params.getString("valor_unit"));
                            i.putExtra("forma_pagamento", "DINHEIRO");*/

                            i.putExtra("cpfCnpj_cliente", "000.000.000-00");
                            i.putExtra("formaPagamento", params.getString("formaPagamento"));
                            i.putExtra("produto", params.getString("produto"));
                            i.putExtra("qnt", "1");
                            i.putExtra("vlt", params.getString("vlt"));
                        } else {

                            i = new Intent(getBaseContext(), Principal.class);
                            //i = new Intent(getBaseContext(), MainActivityPrincipal.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        }
                        startActivity(i);

                    } else {
                        //SE O BANCO NÃO EXISTIR VAI PARA SINCRONIZAÇÃO
                        Intent i = new Intent(getBaseContext(), Sincronizar.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }


            finish();

        }, time);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}
