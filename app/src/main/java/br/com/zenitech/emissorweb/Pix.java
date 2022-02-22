package br.com.zenitech.emissorweb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.Objects;

import br.com.zenitech.emissorweb.domains.PixDomain;
import br.com.zenitech.emissorweb.domains.Sincronizador;
import br.com.zenitech.emissorweb.interfaces.IPix;
import br.com.zenitech.emissorweb.interfaces.ISincronizar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Pix extends AppCompatActivity {

    private DatabaseHelper bd;
    ImageView imageView2;
    TextView textView, textView2;
    String apiKey, cliCob, pedido, valor;
    String idLisForPag, idPagamento, idForPagPix;
    AlertDialog alerta;
    Bitmap btm;
    SharedPreferences prefs;
    LinearLayoutCompat llcPagarPix, llcPixPago, llcPixNaoPago, llcConsultarPix;
    AppCompatButton btnMostrarQrCodePix, btnSairPix;
    boolean consulta = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pix);
        bd = new DatabaseHelper(this);
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        llcPagarPix = findViewById(R.id.llcPagarPix);
        llcPixPago = findViewById(R.id.llcPixPago);
        llcPixNaoPago = findViewById(R.id.llcPixNaoPago);
        llcConsultarPix = findViewById(R.id.llcConsultarPix);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        btnMostrarQrCodePix = findViewById(R.id.btnMostrarQrCodePix);
        btnSairPix = findViewById(R.id.btnSairPix);
        btnSairPix.setOnClickListener(v -> finish());

        imageView2 = findViewById(R.id.imageView2);
        //String cb64 = "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";

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                idLisForPag = params.getString("idLisForPag");
                textView2.setText(params.getString("valor"));
                apiKey = params.getString("apiKey");
                cliCob = params.getString("cliCob");
                valor = String.valueOf(new ClassAuxiliar().converterValores(params.getString("valor")));
                pedido = params.getString("pedido");//"Serial: " + prefs.getString("serial", "") + ", Pedido: " +
                idForPagPix = params.getString("idForPagPix");
                idPagamento = params.getString("idPagamento");
            }
        }

        if (idPagamento != null) {
            //getStatusPayment(idPagamento);
            llcPagarPix.setVisibility(View.GONE);
            llcConsultarPix.setVisibility(View.VISIBLE);
            verificarPagamento(idPagamento);
            pegarQrCode(idPagamento);
        } else {
            getQrCodePayment();
        }

        btnMostrarQrCodePix.setOnClickListener(view -> {
            llcPixPago.setVisibility(View.GONE);
            llcConsultarPix.setVisibility(View.GONE);
            llcPixNaoPago.setVisibility(View.GONE);
            llcPagarPix.setVisibility(View.VISIBLE);
            consulta = true;
            espera();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        VerificarActivityAtiva.activityResumed();
        espera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VerificarActivityAtiva.activityPaused();
    }

    private void getQrCodePayment() {

        //Log.e("PIX", "cobranca | " + " | " + apiKey + " | " + cliCob + " | " + pedido + " | " + valor);
        final IPix iPix = IPix.retrofit.create(IPix.class);
        final Call<PixDomain> call = iPix.getImgQrCode(
                "cobranca",
                apiKey,
                cliCob,
                pedido,
                valor);
        call.enqueue(new Callback<PixDomain>() {
            @Override
            public void onResponse(@NonNull Call<PixDomain> call, @NonNull Response<PixDomain> response) {

                try {
                    final PixDomain infoPix = response.body();
                    if (!infoPix.getEncodedImage().equalsIgnoreCase("")) {
                        Log.e("PIX", infoPix.getId());
                        btm = convertBase64ToBitmap(infoPix.getEncodedImage());
                        imageView2.setImageBitmap(btm);

                        idPagamento = infoPix.getId();

                        bd.updateFormPagPIX(idPagamento, idForPagPix);
                        espera();
                    }
                } catch (Exception ignored) {

                }
            }

            @Override
            public void onFailure(@NonNull Call<PixDomain> call, @NonNull Throwable t) {
            }
        });
    }

    private void getStatusPayment(String id) {
        final IPix iPix = IPix.retrofit.create(IPix.class);
        final Call<PixDomain> call = iPix.getStatusCobranca(
                "status",
                apiKey,
                id);
        call.enqueue(new Callback<PixDomain>() {
            @Override
            public void onResponse(@NonNull Call<PixDomain> call, @NonNull Response<PixDomain> response) {
                try {
                    //
                    final PixDomain infoPix = response.body();
                    if (infoPix != null) {

                        //PENDING
                        if (infoPix.getStatus().equalsIgnoreCase("RECEIVED")) {
                            if (consulta) {
                                bd.updateFormPagPIXRecebido(idLisForPag);
                            } else {
                                bd.updateFormPagPIXRecebido(idForPagPix);
                            }
                            llcPixNaoPago.setVisibility(View.GONE);
                            llcConsultarPix.setVisibility(View.GONE);
                            llcPagarPix.setVisibility(View.GONE);
                            llcPixPago.setVisibility(View.VISIBLE);
                            //_finalizarPagamento();
                            //infoPix.getStatus()
                            //Toast.makeText(getBaseContext(), "RECEBIDO", Toast.LENGTH_SHORT).show();
                            esperaFechar();
                        } else {
                            espera();
                        }
                    } else {
                        espera();
                    }
                } catch (Exception e) {
                    Log.e("PIX", e.getMessage());
                    espera();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PixDomain> call, @NonNull Throwable t) {
                espera();
            }
        });
    }

    private void verificarPagamento(String id) {
        final IPix iPix = IPix.retrofit.create(IPix.class);
        final Call<PixDomain> call = iPix.getStatusCobranca(
                "status",
                apiKey,
                id);
        call.enqueue(new Callback<PixDomain>() {
            @Override
            public void onResponse(@NonNull Call<PixDomain> call, @NonNull Response<PixDomain> response) {
                try {
                    //
                    final PixDomain infoPix = response.body();
                    if (infoPix != null) {

                        //bd.updateFormPagPIXRecebido(idLisForPag);

                        //finish();

                        //PENDING
                        if (infoPix.getStatus().equalsIgnoreCase("RECEIVED")) {
                            //Toast.makeText(getBaseContext(), "RECEBIDO", Toast.LENGTH_SHORT).show();
                            bd.updateFormPagPIXRecebido(idLisForPag);
                            llcConsultarPix.setVisibility(View.GONE);
                            llcPixPago.setVisibility(View.VISIBLE);

                            esperaFechar();
                            //finish();
                        } else {
                            llcConsultarPix.setVisibility(View.GONE);
                            llcPixNaoPago.setVisibility(View.VISIBLE);
                        }
                    } else {
                        llcConsultarPix.setVisibility(View.GONE);
                        llcPixNaoPago.setVisibility(View.VISIBLE); //TROCAR PARA UMA MESAGEM DE ERRO
                    }
                } catch (Exception e) {
                    Log.e("PIX", e.getMessage());
                    llcConsultarPix.setVisibility(View.GONE);
                    llcPixNaoPago.setVisibility(View.VISIBLE); //TROCAR PARA UMA MESAGEM DE ERRO
                }
            }

            @Override
            public void onFailure(@NonNull Call<PixDomain> call, @NonNull Throwable t) {
                Log.e("PIX", t.getMessage());
                llcConsultarPix.setVisibility(View.GONE);
                llcPixNaoPago.setVisibility(View.VISIBLE); //TROCAR PARA UMA MESAGEM DE ERRO
            }
        });
    }

    private void pegarQrCode(String id) {
        final IPix iPix = IPix.retrofit.create(IPix.class);
        final Call<PixDomain> call = iPix.pegarQrCode(
                "pegar_qrcode",
                apiKey,
                id);
        call.enqueue(new Callback<PixDomain>() {
            @Override
            public void onResponse(@NonNull Call<PixDomain> call, @NonNull Response<PixDomain> response) {
                try {
                    //
                    final PixDomain infoPix = response.body();
                    if (!infoPix.getEncodedImage().equalsIgnoreCase("")) {
                        btm = convertBase64ToBitmap(infoPix.getEncodedImage());
                        imageView2.setImageBitmap(btm);

                        idPagamento = infoPix.getId();

                        bd.updateFormPagPIX(idPagamento, idForPagPix);
                        espera();
                    }
                } catch (Exception e) {
                    Log.e("PIX", e.getMessage());
                    espera();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PixDomain> call, @NonNull Throwable t) {
                espera();
            }
        });
    }

    private Bitmap convertBase64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    void espera() {
        if (VerificarActivityAtiva.isActivityVisible()) {
            new Handler().postDelayed(() -> {
                if (VerificarActivityAtiva.isActivityVisible()) {

                    Log.e("PIX", "\n" + idLisForPag + "\n" + valor + "\n" + apiKey + "\n" + cliCob + "\n" + pedido + "\n" + idForPagPix + "\n" + idPagamento + "\n");
                    getStatusPayment(idPagamento);
                }
            }, 5000);
        }
    }

    void esperaFechar() {
        if (VerificarActivityAtiva.isActivityVisible()) {
            new Handler().postDelayed(() -> {
                if (VerificarActivityAtiva.isActivityVisible()) {
                    _finalizarPagamento();
                }
            }, 3000);
        }
    }

    private void _finalizarPagamento() {
        //
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", "ok");

        //
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        mostrarMsg();
    }

    void sair() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    public void mostrarMsg() {

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("Atenção");
        //define a mensagem
        String msg = "Você deseja realmente cancelar este pagamento?";
        builder.setMessage(msg);
        //define um botão como positivo
        builder.setPositiveButton("SIM", (arg0, arg1) -> {
            sair();
        });
        //define um botão como negativo.
        builder.setNegativeButton("NÃO", (arg0, arg1) -> {

            // **
            //msg(true);
        });
        //cria o AlertDialog
        alerta = builder.create();
        //Exibe alerta
        alerta.show();
    }
}