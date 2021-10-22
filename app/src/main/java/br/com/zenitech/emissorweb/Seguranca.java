package br.com.zenitech.emissorweb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

import br.com.zenitech.emissorweb.domains.SegurancaDomain;
import br.com.zenitech.emissorweb.domains.Sincronizador;
import br.com.zenitech.emissorweb.interfaces.ISeguranca;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Seguranca extends AppCompatActivity {
    Context context;
    private SharedPreferences prefs;
    private EditText codigo;
    private LinearLayoutCompat llCarregandoCodigo, llErroCodigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguranca);

        //
        context = this;
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);

        codigo = findViewById(R.id.codigo);
        llCarregandoCodigo = findViewById(R.id.llCarregandoCodigo);
        llErroCodigo = findViewById(R.id.llErroCodigo);

        findViewById(R.id.btnConfirmarCodigo).setOnClickListener(v -> {
            // ESCONDE O TECLADO
            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
            } catch (Exception e) {
                //Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            }

            llCarregandoCodigo.setVisibility(View.VISIBLE);
            validarCodigo();
        });
    }

    private void validarCodigo() {
        final ISeguranca iSeguranca = ISeguranca.retrofit.create(ISeguranca.class);

        final Call<SegurancaDomain> call = iSeguranca.validarCodigo(
                "seguranca",
                prefs.getString("serial_app", "000000000"),
                codigo.getText().toString());

        call.enqueue(new Callback<SegurancaDomain>() {
            @Override
            public void onResponse(@NonNull Call<SegurancaDomain> call, @NonNull Response<SegurancaDomain> response) {

                //
                final SegurancaDomain seguranca = response.body();

                if (Objects.requireNonNull(seguranca).getErro().equalsIgnoreCase("ok")) {
                    Intent i;
                    Configuracoes configuracoes = new Configuracoes();
                    if (configuracoes.GetDevice())
                        i = new Intent(context, CancelarPagamentoCartaoPOS.class);
                    else
                        i = new Intent(context, CancelarPagamentoCartao.class);

                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);

                    finish();
                } else {

                    llCarregandoCodigo.setVisibility(View.GONE);
                    llErroCodigo.setVisibility(View.VISIBLE);
                    avancar();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SegurancaDomain> call, @NonNull Throwable t) {
                llCarregandoCodigo.setVisibility(View.GONE);
                llErroCodigo.setVisibility(View.VISIBLE);

                avancar();
            }
        });
    }

    private void avancar() {
        new Handler().postDelayed(() -> {
            llCarregandoCodigo.setVisibility(View.GONE);
            llErroCodigo.setVisibility(View.GONE);
        }, 3000);
    }
}