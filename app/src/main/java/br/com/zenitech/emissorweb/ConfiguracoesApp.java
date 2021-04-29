package br.com.zenitech.emissorweb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Objects;

public class ConfiguracoesApp extends AppCompatActivity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_app);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);

        Button btnLimparCofigImpressora = findViewById(R.id.btnLimparCofigImpressora);
        btnLimparCofigImpressora.setOnClickListener(view -> {

            prefs.edit().putString("tamPapelImpressora", "").apply();

            Snackbar.make(view, "Impressora redefinida com sucesso!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });

        findViewById(R.id.btnReset).setOnClickListener(view -> {
            prefs.edit().putBoolean("reset", true).apply();
            //
            Intent i = new Intent(this, SplashScreen.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });
    }

    @Override
    public void onBackPressed() {
        Sair();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Sair();
        }

        return super.onOptionsItemSelected(item);
    }

    void Sair() {
        finish();
    }

}
