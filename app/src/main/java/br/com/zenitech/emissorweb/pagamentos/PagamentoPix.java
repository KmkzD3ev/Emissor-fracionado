package br.com.zenitech.emissorweb.pagamentos;

import static br.com.zenitech.emissorweb.MaskUtil.maskCnpj;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.com.stone.posandroid.providers.PosPrintProvider;
import br.com.zenitech.emissorweb.AtivarDesativarBluetooth;
import br.com.zenitech.emissorweb.Impressora;
import br.com.zenitech.emissorweb.ImpressoraPOS;
import br.com.zenitech.emissorweb.R;
import br.com.zenitech.emissorweb.ClassAuxiliar;
import br.com.zenitech.emissorweb.Configuracoes;
import br.com.zenitech.emissorweb.DatabaseHelper;
import br.com.zenitech.emissorweb.VerificarActivityAtiva;
import br.com.zenitech.emissorweb.controller.PrintViewHelper;
import br.com.zenitech.emissorweb.domains.PixDomain;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.interfaces.IPix;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import stone.application.interfaces.StoneCallbackInterface;

public class PagamentoPix extends AppCompatActivity {
    DatabaseHelper bd;
    ImageView imageView2;
    TextView textView, textView2, tvTime;
    String apiKey, pedido, valor;
    String idPagamento;
    AlertDialog alerta;
    Bitmap btm;
    SharedPreferences prefs;
    LinearLayoutCompat llcPagarPix, llcPixPago, llcPixNaoPago, llcConsultarPix;
    AppCompatButton btnMostrarQrCodePix, btnSairPix;
    boolean consulta = false;
    int totPrint = 0;

    ArrayList<PosApp> elementosPos;
    PosApp posApp;

    ArrayList<Unidades> elementosUnidades;
    Unidades unidades;

    PrintViewHelper printViewHelper;
    Map<String, Integer> map;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pix);
        context = this;
        bd = new DatabaseHelper(this);
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);

        elementosPos = bd.getPos();
        posApp = elementosPos.get(0);

        elementosUnidades = bd.getUnidades();
        unidades = elementosUnidades.get(0);

        //
        llcPagarPix = findViewById(R.id.llcPagarPix);
        llcPixPago = findViewById(R.id.llcPixPago);
        llcPixNaoPago = findViewById(R.id.llcPixNaoPago);
        llcConsultarPix = findViewById(R.id.llcConsultarPix);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        btnMostrarQrCodePix = findViewById(R.id.btnMostrarQrCodePix);
        btnSairPix = findViewById(R.id.btnSairPix);
        btnSairPix.setOnClickListener(v -> finish());
        tvTime = findViewById(R.id.tvTime);
        tvTime.setVisibility(View.GONE);
        imageView2 = findViewById(R.id.imageView2);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                textView2.setText(params.getString("valor"));
                apiKey = unidades.getPix_key_transfeera();// params.getString("apiKey");
                valor = String.valueOf(new ClassAuxiliar().converterValores(params.getString("valor")));
                pedido = params.getString("pedido");
            }
        }

        getQrCodePayment();

        btnMostrarQrCodePix.setOnClickListener(view -> {
            llcPixPago.setVisibility(View.GONE);
            llcConsultarPix.setVisibility(View.GONE);
            llcPixNaoPago.setVisibility(View.GONE);
            llcPagarPix.setVisibility(View.VISIBLE);
            consulta = true;
            espera();
        });


        printViewHelper = new PrintViewHelper();
    }

    @Override
    protected void onResume() {
        super.onResume();
        VerificarActivityAtiva.activityResumed();
        espera();

        new AtivarDesativarBluetooth().enableBT(context, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        VerificarActivityAtiva.activityPaused();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private void getQrCodePayment() {

        final IPix iPix = IPix.retrofit.create(IPix.class);
        final Call<PixDomain> call = iPix.getImgQrCode(
                "token",
                unidades.getPix_key_transfeera(),
                unidades.getCliente_id_transfeera(),
                unidades.getCliente_secret_transfeera(),
                pedido,
                valor,
                posApp.getCliente(),
                unidades.getRazao_social()
        );
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PixDomain> call, @NonNull Response<PixDomain> response) {

                try {
                    final PixDomain infoPix = response.body();
                    if (!infoPix.getEncodedImage().equalsIgnoreCase("")) {
                        Log.e("PIX", infoPix.getId());
                        btm = convertBase64ToBitmap(infoPix.getEncodedImage());
                        imageView2.setImageBitmap(btm);

                        idPagamento = infoPix.getId();
                        Configuracoes.token_authorization = infoPix.getTokenAuthorization();

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
                unidades.getCliente_id_transfeera(),
                unidades.getCliente_secret_transfeera(),
                id,
                Configuracoes.token_authorization,
                posApp.getCliente(),
                unidades.getRazao_social()
        );
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PixDomain> call, @NonNull Response<PixDomain> response) {
                try {
                    //
                    final PixDomain infoPix = response.body();
                    if (infoPix != null) {

                        //PENDING
                        if (infoPix.getStatus().equalsIgnoreCase("CONCLUIDA")) {
                            /*if (consulta) {
                                bd.updateFormPagPIXRecebido(idLisForPag);
                            } else {
                                bd.updateFormPagPIXRecebido(idForPagPix);
                            }*/
                            llcPixNaoPago.setVisibility(View.GONE);
                            llcConsultarPix.setVisibility(View.GONE);
                            llcPagarPix.setVisibility(View.GONE);
                            llcPixPago.setVisibility(View.VISIBLE);

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
        //Toast.makeText(this, token_authorization, Toast.LENGTH_SHORT).show();
        final IPix iPix = IPix.retrofit.create(IPix.class);
        final Call<PixDomain> call = iPix.getStatusCobranca(
                "status",
                unidades.getCliente_id_transfeera(),
                unidades.getCliente_secret_transfeera(),
                id,
                Configuracoes.token_authorization,
                posApp.getCliente(),
                unidades.getRazao_social()
        );
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PixDomain> call, @NonNull Response<PixDomain> response) {
                try {
                    //
                    final PixDomain infoPix = response.body();
                    if (infoPix != null) {

                        //PENDING
                        if (infoPix.getStatus().equalsIgnoreCase("CONCLUIDA")) {
                            //Toast.makeText(getBaseContext(), "RECEBIDO", Toast.LENGTH_SHORT).show();
                            //bd.updateFormPagPIXRecebido(idLisForPag);
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
                unidades.getCliente_id_transfeera(),
                unidades.getCliente_secret_transfeera(),
                id,
                Configuracoes.token_authorization,
                posApp.getCliente(),
                unidades.getRazao_social()
        );
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PixDomain> call, @NonNull Response<PixDomain> response) {
                try {
                    //
                    final PixDomain infoPix = response.body();
                    if (!infoPix.getEncodedImage().equalsIgnoreCase("")) {
                        btm = convertBase64ToBitmap(infoPix.getEncodedImage());
                        imageView2.setImageBitmap(btm);

                        idPagamento = infoPix.getId();

                        //bd.updateFormPagPIX(idPagamento, idForPagPix);
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

    // --------------------     COMPROVANTE PIX         --------------------------------------------

    void ImprimirComprovante() {
        ClassAuxiliar aux = new ClassAuxiliar();
        PrintViewHelper printViewHelper = new PrintViewHelper();
        PosPrintProvider ppp = new PosPrintProvider(this);
        ppp.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Toast.makeText(getBaseContext(), "Erro ao imprimir: " + ppp.getListOfErrors(), Toast.LENGTH_SHORT).show();
            }
        });

        //IMPRIMIR CABEÇALHO
        TextView txtEmpresaPix, txtCNPJPix, txtBancoPix, txtPOS;
        txtEmpresaPix = findViewById(R.id.txtEmpresaPix);
        txtCNPJPix = findViewById(R.id.txtCNPJPix);
        txtBancoPix = findViewById(R.id.txtBancoPix);
        txtPOS = findViewById(R.id.txtPOS);
        txtPOS.setText(prefs.getString("serial_app", ""));

        elementosUnidades = bd.getUnidades();
        unidades = elementosUnidades.get(0);
        txtEmpresaPix.setText(unidades.getRazao_social());
        txtCNPJPix.setText("CNPJ: " + maskCnpj(unidades.getCnpj()));
        txtBancoPix.setText(unidades.getBanco_pix());

        TextView txtPedido = findViewById(R.id.txtNPedido);
        TextView txtIdentificador = findViewById(R.id.txtIdentificadoPix);
        TextView txtValorPix = findViewById(R.id.txtValorPix);
        TextView txtDataHora = findViewById(R.id.txtDataHoraPix);
        //
        txtPedido.setText(pedido);
        txtIdentificador.setText(idPagamento);
        txtValorPix.setText(String.format("R$ %s", aux.maskMoney(new BigDecimal(valor))));
        txtDataHora.setText(String.format("%s - %s", aux.exibirDataAtual(), aux.horaAtual()));

        LinearLayout impressora = findViewById(R.id.print_comprovante_pix);
        Bitmap bitmap1 = printViewHelper.createBitmapFromView(impressora, 190, 180);

        ppp.addBitmap(bitmap1);
        ppp.addLine("");
        ppp.addLine("");
        ppp.addLine("");
        ppp.execute();
    }
    private Bitmap convertBase64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    void espera() {
        if (VerificarActivityAtiva.isActivityVisible()) {
            new Handler().postDelayed(() -> {
                if (VerificarActivityAtiva.isActivityVisible()) {

                    //Log.e("PIX", "\n" + idLisForPag + "\n" + valor + "\n" + apiKey + "\n" + cliCob + "\n" + pedido + "\n" + idForPagPix + "\n" + idPagamento + "\n");
                    getStatusPayment(idPagamento);
                }
            }, 5000);
        }
    }

    void esperaFechar() {
        if (VerificarActivityAtiva.isActivityVisible()) {
            if (totPrint == 0) {
                totPrint = 1;

                Configuracoes configuracoes = new Configuracoes();
                if (configuracoes.GetDevice()) {
                    ImprimirComprovante();
                } else {
                    //
                    Intent i = new Intent(context, Impressora.class);
                    i.putExtra("imprimir", "comprovante_pix_reimp");
                    i.putExtra("impressao_pix", true);
                    startActivity(i);
                }
            }

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
