package br.com.zenitech.emissorweb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.math.BigDecimal;
import java.util.Objects;

public class AppFinalizado extends AppCompatActivity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_finalizado);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);

        Button btnFecharApp = findViewById(R.id.btnFecharApp);
        btnFecharApp.setOnClickListener(view -> finish());

        Button btnNovaRemessa = findViewById(R.id.btnNovaRemessa);
        btnNovaRemessa.setOnClickListener(view -> {
            Intent i = new Intent(getBaseContext(), Sincronizar.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btnResetApp).setOnClickListener(view -> {
            prefs.edit().putBoolean("reset", true).apply();
            //
            Intent i = new Intent(this, SplashScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {

                if(params.getBoolean("initAuto")){
                    NovaRemessa();
                }
            }
        }
    }

    void NovaRemessa(){
        Intent i = new Intent(getBaseContext(), Sincronizar.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

}
