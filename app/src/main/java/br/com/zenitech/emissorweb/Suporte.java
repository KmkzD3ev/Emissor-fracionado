package br.com.zenitech.emissorweb;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Objects;

public class Suporte extends AppCompatActivity {

    WebView webView;
    ProgressBar progressBar;
    SharedPreferences prefs;
    LinearLayout llSuporte, llNomeUsuario;
    ImageView imgVoltarSuporte, imgFecharChat;
    AlertDialog alerta;
    EditText etNomeUsuario;
    Button btnSalvar;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suporte);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);

        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.INVISIBLE);

        imgVoltarSuporte = findViewById(R.id.imgVoltarSuporte);
        imgVoltarSuporte.setOnClickListener(view -> alertaFinalizar());

        etNomeUsuario = findViewById(R.id.etNomeUsuario);
        etNomeUsuario.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if (!etNomeUsuario.getText().toString().equalsIgnoreCase("")) {
                    prefs.edit().putString("nomeUsuario", etNomeUsuario.getText().toString()).apply();
                    llNomeUsuario.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, "Informe seu nome!", Toast.LENGTH_SHORT).show();
                }
                handled = true;
            }
            return handled;
        });
        btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(view -> {
            if (!etNomeUsuario.getText().toString().equalsIgnoreCase("")) {
                prefs.edit().putString("nomeUsuario", etNomeUsuario.getText().toString()).apply();
                llNomeUsuario.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "Informe seu nome!", Toast.LENGTH_SHORT).show();
            }
        });

        imgFecharChat = findViewById(R.id.imgFecharChat);
        //imgFecharChat.setOnClickListener(view -> Sair());

        llNomeUsuario = findViewById(R.id.llNomeUsuario);
        llNomeUsuario.setOnClickListener(view -> {
        });

        if (Objects.requireNonNull(prefs.getString("nomeUsuario", "")).equalsIgnoreCase("")) {
            llNomeUsuario.setVisibility(View.VISIBLE);
        }

        llSuporte = findViewById(R.id.llSuporte);
        llSuporte.setOnClickListener(view -> {
        });

        webView = findViewById(R.id.wvSuporte);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "Android");
        webView.loadUrl(new Configuracoes().GetUrlServer() + "POSSIAC/chat_app_emissorweb.php?nome=" + prefs.getString("nomeUsuario", "") + "&serial=" + prefs.getString("serial_app", ""));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE); // mostra a progress
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.INVISIBLE); // esconde a progress
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> webView.loadUrl(new Configuracoes().GetUrlServer() + "POSSIAC/chat_app_emissorweb.php?serial=" + prefs.getString("serial_app", "") + "&abrir=ok"));
    }

    @Override
    public void onBackPressed() {
        alertaFinalizar();
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

    private void alertaFinalizar() {

        //
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("Atenção!");
        String str = "Deseja finalizar o suporte?";
        //define a mensagem
        builder.setMessage(str);

        //define um botão como positivo
        builder.setPositiveButton("Sim", (arg0, arg1) -> {
            //
            Sair();
        });

        //define um botão como negativo.
        builder.setNeutralButton("Não", (arg0, arg1) -> {
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();
    }

    @JavascriptInterface
    public void chatOffLine() {

        //
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("O suporte está offline!");
        String str = "Deseja finalizar o suporte?";
        //define a mensagem
        builder.setMessage(str);

        //define um botão como positivo
        builder.setPositiveButton("Sim", (arg0, arg1) -> {
            //
            Sair();
        });

        //define um botão como negativo.
        builder.setNeutralButton("Não", (arg0, arg1) -> {
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();
    }

    @JavascriptInterface
    public void jsShowToast(String toast) {
        new AlertDialog.Builder(this)
                .setTitle("Dialog")
                .setMessage(toast)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

}
