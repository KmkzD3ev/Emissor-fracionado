package br.com.zenitech.emissorweb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class Sobre extends AppCompatActivity {

    TextView txtVersao;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);

        txtVersao = findViewById(R.id.txtVersao);

        String info = prefs.getString("infoPos", "");
        txtVersao.setText(String.format("%s\nVersão %s", info, BuildConfig.VERSION_NAME));

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());*/
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
