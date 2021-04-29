package br.com.zenitech.emissorweb;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import java.io.File;
import java.util.Objects;

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
