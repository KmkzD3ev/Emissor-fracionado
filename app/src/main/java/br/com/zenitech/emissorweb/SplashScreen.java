package br.com.zenitech.emissorweb;

import android.annotation.TargetApi;
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
import android.widget.Toast;

import java.io.File;
import java.util.Objects;

import br.com.zenitech.emissorweb.domains.Sincronizador;
import br.com.zenitech.emissorweb.interfaces.ISincronizar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreen extends AppCompatActivity {
    //
    private SharedPreferences prefs;
    private SharedPreferences.Editor ed;
    //private DatabaseHelper bd;
    private int time = 2300;
    private Bundle params;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();

        if (prefs.getBoolean("reset", false)) {
            //
            Intent i = new Intent(this, ResetApp.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
            return;
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
                final Sincronizador sincronizacao = response.body();
                if (Objects.requireNonNull(sincronizacao).getErro().equalsIgnoreCase("ok")) {
                    resetarApp();
                } else {
                    /*txtMsgReset.setText("Não foi possível resetar o App, verifique as informações e tente novamente.");
                    erro();*/
                    avancar();
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

                Toast.makeText(getBaseContext(), "O App foi resetado com sucesso!", Toast.LENGTH_LONG);
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

                            i = new Intent(getBaseContext(), ConfirmarDadosPedido.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            //i.putExtra("produto", params.getString("produto"));
                            i.putExtra("siac", "1");
                            i.putExtra("quantidade", params.getString("quantidade"));
                            i.putExtra("produto", params.getString("produto"));
                            i.putExtra("valor_unit", params.getString("valor_unit"));
                            i.putExtra("forma_pagamento", "DINHEIRO");
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
