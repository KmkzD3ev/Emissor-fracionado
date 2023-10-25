package br.com.zenitech.emissorweb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.Sincronizador;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.domains.ValidarNFCe;
import br.com.zenitech.emissorweb.interfaces.ISincronizar;
import br.com.zenitech.emissorweb.interfaces.IValidarNFCe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GerenciarBancoProducao extends AppCompatActivity {

    // ATIVA O MODO TESTE
    private boolean modo_teste = false;
    //
    private SharedPreferences prefs;
    private ClassAuxiliar cAux;
    private DatabaseHelper bd;

    private static final String TAG = "GerenciarBancoProducao";
    private ProgressDialog pd;
    private Context context = null;

    ArrayList<Unidades> elementos;
    Unidades unidades;

    ArrayList<PosApp> elementosPos;
    PosApp posApp;

    ArrayList<Pedidos> elementosPedidos;
    Pedidos pedidos;

    ArrayList<ItensPedidos> elementosItens;
    ItensPedidos itensPedidos;

    //QUANTIDADE FRAGMENTADA
    int quantidade = 0;
    int transmitir;
    int transmitindo;
    int count = 1;

    ImageView imgSincronizarNotas;
    TextView txtSincronizarNotas;
    LinearLayout btnSincronizarNotas;
    LinearLayout btnSincronizarNotasFinalizar;

    String credenciadora, cod_aut, nsu, bandeira;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_banco_producao);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sincronizar NFC-e");

        context = this;
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        bd = new DatabaseHelper(context);
        cAux = new ClassAuxiliar();

        //
        elementos = bd.getUnidades();
        unidades = elementos.get(0);

        //
        elementosPos = bd.getPos();
        posApp = elementosPos.get(0);

        // SE O SERIAL FOR DE TESTE ATIVA O MODO TESTE
        if (posApp.getSerial().equals("005000002")) {
            modo_teste = true;
        }

        //
        elementosPedidos = bd.getPedidosTransmitirFecharDia();
        transmitir = elementosPedidos.size();
        transmitindo = elementosPedidos.size();

        Log.i(TAG, transmitindo + "/" + transmitir);

        //
        imgSincronizarNotas = findViewById(R.id.imgSincronizarNotas);
        txtSincronizarNotas = findViewById(R.id.txtSincronizarNotas);
        btnSincronizarNotas = findViewById(R.id.btnSincronizarNotas);
        btnSincronizarNotasFinalizar = findViewById(R.id.btnSincronizarNotasFinalizar);

        if (elementosPedidos.size() != 0) {
            btnSincronizarNotas.setVisibility(View.VISIBLE);
        } else {
            // btnSincronizarNotasFinalizar.setVisibility(View.VISIBLE);
        }

        if (isOnline(context)) {

            //TRANSMITIR
            transmitirNota();

        } else {
            Toast.makeText(context,
                    "Verifique a sua Conexão à Internet!",
                    Toast.LENGTH_LONG).show();
        }

        btnSincronizarNotas.setOnClickListener(view -> {

            if (isOnline(context)) {

                //TRANSMITIR
                transmitirNota();

            } else {
                Toast.makeText(context,
                        "Verifique a sua Conexão à Internet!",
                        Toast.LENGTH_LONG).show();
            }
        });

        btnSincronizarNotasFinalizar.setOnClickListener(view -> {
            //
            final ISincronizar iSincronizar = ISincronizar.retrofit.create(ISincronizar.class);

            final Call<Sincronizador> call = iSincronizar.ativarDesativarPOS("desativar", prefs.getString("serial_app", ""));

            call.enqueue(new Callback<Sincronizador>() {
                @Override
                public void onResponse(@NonNull Call<Sincronizador> call, @NonNull Response<Sincronizador> response) {

                    //
                    final Sincronizador sincronizacao = response.body();
                    if (sincronizacao != null) {

                        //
                        runOnUiThread(() -> {

                            prefs.edit().putBoolean("sincronizado", false).apply();

                            //APAGA O BANCO DE DADOS E VAI PARA TELA INICIAL DE SINCRONIZAÇÃO
                            context.deleteDatabase("emissorwebDB");
                            Intent i = new Intent(context, Sincronizar.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        });
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Sincronizador> call, @NonNull Throwable t) {
                    Log.i("ERRO_SIN", Objects.requireNonNull(t.getMessage()));

                    Toast.makeText(getBaseContext(), "Não conseguimos finalizar, tente novamente.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent i = new Intent(this, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    // VERIFICA SE EXISTE CONEXÃO COM A INTERNET
    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    int erro = 0;
    int erroSinc = 0;

    private void transmitirNota() {
        //ESCODER O TECLADO
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (transmitindo != 0) {

            //MOSTRA A MENSAGEM DE SINCRONIZAÇÃO
            pd = ProgressDialog.show(context, count + "/" + transmitir, "Transmitindo...",
                    true, false);

            count++;


            String[] subNotaPed = {String.valueOf(transmitindo), "1"};
            //cAux.subitrair(subNotaPed);
            int linhaPed = cAux.subitrair(subNotaPed).intValue();

            //
            pedidos = elementosPedidos.get(linhaPed);
            //
            //elementosItens = bd.getItensPedido(pedidos.getId());
            elementosItens = bd.getItensPedidoTransmitir(pedidos.getId());

            try {
                if (elementosItens.size() != 0) {
                    itensPedidos = elementosItens.get(0);
                    transmitindo = linhaPed;

                    //
                    final IValidarNFCe iValidarNFCe = IValidarNFCe.retrofit.create(IValidarNFCe.class);
                    //
                    String valorFormaPGPedido, idFormaPGPedido, nAutoCartao, bandeiraFPG, nsuFPG;

                    valorFormaPGPedido = bd.getValoresFormasPagamentoPedido(pedidos.getId()).replace(".", "");
                    nsuFPG = bd.getNSUFormasPagamentoPedido(pedidos.getId()).replace(".", "");
                    idFormaPGPedido = bd.getIdFormasPagamentoPedido(pedidos.getId()).replace(".", "");
                    bandeiraFPG = bd.getBandeiraFormasPagamentoPedido(pedidos.getId()).replace(".", "");
                    nAutoCartao = bd.getAutorizacaoFormasPagamentoPedido(pedidos.getId()).replace(".", "");

                    String idsProdutosPedido = bd.getIdsProdutosPedido(pedidos.getId()).replace(".", "");
                    String quantidadesProdutosPedido = bd.getQuantidadesProdutosPedido(pedidos.getId()).replace(".", "");
                    String valorProdutosPedido = bd.getValorProdutosPedido(pedidos.getId()).replace(".", "");

                    // SE FOR PINPAD OU POS A CREDENCIADORA SERÁ STONE
                    if (!unidades.getCodloja().equalsIgnoreCase("")) {
                        credenciadora = "STONE";
                    }

                    final Call<ValidarNFCe> call = iValidarNFCe.validarNota(
                            pedidos.getId(),
                            quantidadesProdutosPedido,
                            posApp.getSerial(),
                            idsProdutosPedido,
                            valorProdutosPedido,
                            idFormaPGPedido,
                            pedidos.getCpf_cliente(),
                            credenciadora,
                            cod_aut,
                            nsuFPG,
                            valorFormaPGPedido,
                            nAutoCartao,
                            bandeiraFPG,
                            pedidos.getFracionado(),
                            itensPedidos.getDesconto()
                    );

                    call.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<ValidarNFCe> call, @NonNull Response<ValidarNFCe> response) {

                            //
                            final ValidarNFCe sincronizacao = response.body();
                            if (sincronizacao != null) {

                                Log.i(TAG, sincronizacao.getProtocolo());
                                Log.i(TAG, sincronizacao.getErro());

                                //
                                runOnUiThread(() -> {

                                    //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                                    if (pd != null && pd.isShowing()) {
                                        pd.dismiss();
                                    }

                                    // MODO TESTE SERIAL 123456780
                                    // CASO O MODO TESTE ESTEJA ATIVO, FAZ O UPDATE PARA "ON" MESMO SEM PROTOCOLO
                                    if (!modo_teste) {

                                    /*// SE O PROTOCOLO FOR IGUAL A 0000000000 EXCLUIR DO BANCO DE DADOS - NOTA INUTILIZADA
                                    if (!sincronizacao.getProtocolo().isEmpty() && sincronizacao.getProtocolo().equalsIgnoreCase("0000000000")) {
                                        bd.deletePedido();
                                    } else {


                                    }*/
                                        if (!sincronizacao.getProtocolo().isEmpty() && sincronizacao.getProtocolo().length() >= 10) {

                                            //INSERI O PEDIDO NO BANCO DE DADOS
                                            bd.upadtePedidosTransmissao(
                                                    "ON",
                                                    sincronizacao.getProtocolo(),
                                                    cAux.soNumeros(cAux.inserirDataAtual()),
                                                    cAux.soNumeros(cAux.horaAtual()),
                                                    pedidos.getId()
                                            );

                                        } else {

                                            //
                                            erroSinc++;

                                            //
                                            bd.upadtePedidosTransmissao(
                                                    "OFF",
                                                    " ",
                                                    "",
                                                    "",
                                                    pedidos.getId()
                                            );

                                        }
                                    } else {
                                        //
                                        bd.upadtePedidosTransmissao(
                                                "ON",
                                                " ",
                                                "",
                                                "",
                                                pedidos.getId()
                                        );
                                    }

                                    transmitirNota();
                                });
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ValidarNFCe> call, @NonNull Throwable t) {

                            //NSdadosNFCeCont.setVisibility(View.VISIBLE);
                            //NSdadosNFCe.setVisibility(View.GONE);

                            Log.i("ERRO", "" + t);

                            //
                            bd.upadtePedidosTransmissao(
                                    "OFF",
                                    " ",
                                    "",
                                    "",
                                    pedidos.getId()
                            );

                            //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                            if (pd != null && pd.isShowing()) {
                                pd.dismiss();
                            }

                            // VERIFICA A QUANTIDADE DE ERROS
                            if (erro > 3) {
                                erro++;
                                transmitirNota();
                            } else {
                                Log.i(TAG, "Mais de 3 erros");
                            }
                        }
                    });
                }
            } catch (Exception e) {
                //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                /*if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }*/

                Log.i(TAG, Objects.requireNonNull(e.getMessage()));

                //erroTransmitir = true;

            }
        } else {
            //Toast.makeText(context, "NFC-e transmitida com sucesso!", Toast.LENGTH_LONG).show();

            if (erroSinc > 0) {
                runOnUiThread(() -> {
                    imgSincronizarNotas.setImageDrawable(getResources().getDrawable(R.drawable.ic_sentiment_dissatisfied));
                    txtSincronizarNotas.setText("1 ou mais notas não foram sincronizadas. Contate um atendente!");
                    btnSincronizarNotas.setVisibility(View.VISIBLE);
                });
            } else {
                //
                runOnUiThread(() -> {
                    imgSincronizarNotas.setImageDrawable(getResources().getDrawable(R.drawable.ic_sentiment_very_satisfied));
                    txtSincronizarNotas.setText(getString(R.string.notas_sincronizadas));
                    btnSincronizarNotas.setVisibility(View.GONE);
                    //btnSincronizarNotasFinalizar.setVisibility(View.VISIBLE);
                    finalizar();
                    //Log.i(TAG, String.valueOf(erro));
                });
            }
        }
    }

    private void finalizar() {
        // ESPERA 2.3 SEGUNDOS PARA  SAIR DO SPLASH
        new Handler().postDelayed(() -> {

            Intent i = new Intent(context, Principal.class);
            startActivity(i);
            finish();
        }, 2000);
    }
}
