package br.com.zenitech.emissorweb;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.PedidosNFE;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.ProdutosPedidoDomain;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.domains.ValidarNFE;
import br.com.zenitech.emissorweb.interfaces.IValidarNFE;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmarDadosPedidoNFE extends AppCompatActivity implements View.OnClickListener {

    // ATIVA O MODO TESTE
    private boolean modo_teste = false;
    String TAG = "ConfirmarDadosPedidos";
    private ProgressDialog pd;
    private Context context;
    AlertDialog alerta;

    VerificarOnline verificarOnline;

    Button btnPrint, btn_sair;
    ConfirmarDadosPedidoNFE mActivity;

    TextView cpfCnpj_cliente, formaPagamento, produto, qnt, vlt, vltTotal,
            statusNota, protocoloNota, dataHoraNota;

    //
    private SharedPreferences prefs;
    private DatabaseHelper bd;
    int id = 0;
    private ClassAuxiliar cAux;
    private String total, valorUnit;


    private LinearLayout NSdadosPedidos, NSdadosNFCe;
    private RelativeLayout NSdadosNFCeCont;
    ArrayList<Unidades> elementos;
    Unidades unidades;

    ArrayList<PosApp> elementosPos;
    PosApp posApp;

    String idFormaPagamento;

    //QUANTIDADE FRAGMENTADA
    int quantidade = 0;
    int random;

    // INFORMAR ERRO
    private boolean erroTransmitir = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar_dados_pedido_nfe);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActivity = this;

        NSdadosPedidos = findViewById(R.id.NSdadosPedidos);
        NSdadosNFCe = findViewById(R.id.NSdadosNFe);
        NSdadosNFCeCont = findViewById(R.id.NSdadosNFCeCont);
        statusNota = findViewById(R.id.statusNota);
        protocoloNota = findViewById(R.id.protocoloNota);
        dataHoraNota = findViewById(R.id.dataHoraNota);

        //
        cAux = new ClassAuxiliar();

        //
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);

        //
        bd = new DatabaseHelper(this);

        elementos = bd.getUnidades();
        unidades = elementos.get(0);

        elementosPos = bd.getPos();
        posApp = elementosPos.get(0);

        // SE O SERIAL FOR DE TESTE ATIVA O MODO TESTE
        if (posApp.getSerial().equals("000")) {
            modo_teste = true;
        }

        context = ConfirmarDadosPedidoNFE.this;
        verificarOnline = new VerificarOnline();

        btn_sair = findViewById(R.id.btn_sair);

        cpfCnpj_cliente = findViewById(R.id.cpfCnpj_cliente);
        formaPagamento = findViewById(R.id.formaPagamento);
        produto = findViewById(R.id.produto);
        qnt = findViewById(R.id.qnt);
        vlt = findViewById(R.id.vlt);
        vltTotal = findViewById(R.id.vltTotal);

        //
        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {

                cpfCnpj_cliente.setText(params.getString("cpfCnpj_cliente"));
                /*formaPagamento.setText(params.getString("formaPagamento"));
                produto.setText(params.getString("produto"));
                qnt.setText(params.getString("qnt"));
                vlt.setText(params.getString("vlt"));

                //FORMAS DE PAGAMENTO
                String s = params.getString("formaPagamento");
                if (Objects.requireNonNull(s).equalsIgnoreCase("DINHEIRO")) {
                    idFormaPagamento = "1";
                }

                //
                valorUnit = String.valueOf(cAux.converterValores(vlt.getText().toString()));

                //MULTIPLICA O VALOR PELA QUANTIDADE
                String[] multiplicar = {valorUnit, qnt.getText().toString()};
                total = String.valueOf(cAux.multiplicar(multiplicar));
                vltTotal.setText(String.format("R$%s", cAux.maskMoney(new BigDecimal(total))));


                quantidade = Integer.parseInt(qnt.getText().toString());*/

                String pro = bd.getNomeProdutosPedidoNFe("1");
                produto.setText(pro);


                String v =  bd.getValorTotalPedidoNFe("1");
                vltTotal.setText(String.format("R$%s", cAux.maskMoney(new BigDecimal(v))));

                /*String v2 =  bd.getValorTotalPedidoNFe("0");
                vltTotal.setText(String.format("R$%s", cAux.maskMoney(new BigDecimal(v2))));*/

                btnPrint = findViewById(R.id.btnPrint);
                btnPrint.setOnClickListener(this);
            }
        }

        //TRANSMITIR
        findViewById(R.id.btn_transmitir).setOnClickListener(v -> transmitirNota());

        findViewById(R.id.btn_reTransmitir).setOnClickListener(v -> transmitirNota());

        findViewById(R.id.btn_finalizar).setOnClickListener(v -> {
            //
            Intent i = new Intent(context, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btn_sair).setOnClickListener(v -> {
            //
            Intent i = new Intent(context, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        CheckPermission();
        ativarBluetooth();
    }

    private void CheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT},
                        128);
            }
        }

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                    128);
        }
    }

    private void ativarBluetooth() {
        new AtivarDesativarBluetooth().enableBT(context, this);
    }

    private void transmitirNota() {
        //ESCODER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (verificarOnline.isOnline(context)) {

            NSdadosPedidos.setVisibility(View.GONE);

            //MOSTRA A MENSAGEM DE SINCRONIZAÇÃO
            pd = ProgressDialog.show(context, "NF-e", "Transmitindo...",
                    true, false);

            try {

                String valorFormaPGPedido = bd.getValoresFinanceiroNFe("1").replace(".", "");
                String idFormaPGPedido = bd.getIdsFormasPagamentoNFe("1").replace(".", "");
                String bandeiraFPG = bd.getBandeirasFinanceiroNFe("1").replace(".", "");
                String nAutoCartao = bd.getNAutFinanceiroNfe("1").replace(".", "");

                //
                String parcela = bd.getParcelaFormasPagamentoNFe("1").replace(".", "");
                String vencimento = bd.getVencimentoFormasPagamentoNFe("1").replace(".", "");


                String idPedidNFe = "1";// bd.getUltimoIdPedidoNFe();
                String idsProdutosPedido = bd.getIdsProdutosPedidoNFe(idPedidNFe).replace(".", "");
                String quantidadesProdutosPedido = bd.getQuantidadesProdutosPedidoNFe(idPedidNFe).replace(".", "");
                String valorProdutosPedido = bd.getValorProdutosPedidoNFe(idPedidNFe).replace(".", "");

                String credenciadora = "";
                // SE FOR PINPAD OU POS A CREDENCIADORA SERÁ STONE
                if (!unidades.getCodloja().equalsIgnoreCase("")) {
                    credenciadora = "STONE";
                }

                Log.e("Dados NFe", "idsProdutosPedido: " + idsProdutosPedido);
                Log.e("Dados NFe", "quantidadesProdutosPedido: " + quantidadesProdutosPedido);
                Log.e("Dados NFe", "valorProdutosPedido: " + valorProdutosPedido);
                Log.e("Dados NFe", "valorFormaPGPedido: " + valorFormaPGPedido);
                Log.e("Dados NFe", "idFormaPGPedido: " + idFormaPGPedido);
                Log.e("Dados NFe", "bandeiraFPG: " + bandeiraFPG);
                Log.e("Dados NFe", "nAutoCartao: " + nAutoCartao);
                Log.e("Dados NFe", "parcelas: " + parcela);
                Log.e("Dados NFe", "vencimento: " + vencimento);
                //
                final IValidarNFE iValidarNFE = IValidarNFE.retrofit.create(IValidarNFE.class);
                final Call<ValidarNFE> call = iValidarNFE.validarNotaNFE(
                        "779",
                        quantidadesProdutosPedido,//qnt.getText().toString()
                        posApp.getSerial(),
                        idsProdutosPedido,//bd.getIdProduto(produto.getText().toString())
                        valorProdutosPedido,//cAux.soNumeros(vlt.getText().toString())
                        idFormaPGPedido,
                        cpfCnpj_cliente.getText().toString(),
                        valorFormaPGPedido,
                        credenciadora,
                        nAutoCartao,
                        bandeiraFPG,
                        76,
                        parcela,
                        vencimento,
                        prefs.getString("nfeDocumento", "")
                );

                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<ValidarNFE> call, @NonNull Response<ValidarNFE> response) {

                        //
                        final ValidarNFE sincronizacao = response.body();
                        if (sincronizacao != null) {

                            //
                            runOnUiThread(() -> {

                                //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                                if (pd != null && pd.isShowing()) {
                                    pd.dismiss();
                                }

                                // CASO O MODO TESTE ESTEJA ATIVO, FAZ O UPDATE PARA "ON" MESMO SEM PROTOCOLO
                                if (!modo_teste) {

                                }

                                if (!sincronizacao.getProtocolo().isEmpty() && sincronizacao.getProtocolo().length() >= 10) {

                                    //
                                    statusNota.setText(getString(R.string.autorizada));
                                    protocoloNota.setText(sincronizacao.getProtocolo());
                                    dataHoraNota.setText(String.format("%s %s", cAux.exibirDataAtual(), cAux.horaAtual()));

                                    //
                                    prefs.edit().putString("barcode", sincronizacao.getBarcode()).apply();
                                    prefs.edit().putString("nome", sincronizacao.getNome()).apply();
                                    prefs.edit().putString("endereco_dest", sincronizacao.getEndereco_dest()).apply();
                                    prefs.edit().putString("cnpj_dest", sincronizacao.getCnpj_dest()).apply();
                                    prefs.edit().putString("ie_dest", sincronizacao.getIe_dest()).apply();
                                    prefs.edit().putString("nnf", sincronizacao.getNnf()).apply();
                                    prefs.edit().putString("serie", sincronizacao.getSerie()).apply();
                                    prefs.edit().putString("chave", sincronizacao.getChave()).apply();
                                    StringBuilder textBuffer = new StringBuilder();
                                    for (int ind = 0; ind < sincronizacao.desc_produtos.size(); ind++) {
                                        textBuffer.append(sincronizacao.desc_produtos.get(ind).desc_produtos).append("\n");
                                        textBuffer.append(sincronizacao.info_produtos.get(ind).info_produtos).append("\n");
                                    }
                                    prefs.edit().putString("prods_nota", textBuffer.toString()).apply();
                                    prefs.edit().putString("total_nota", sincronizacao.getTotal_nota()).apply();
                                    prefs.edit().putString("inf_cpl", sincronizacao.getInf_cpl()).apply();
                                    prefs.edit().putString("nat_op", sincronizacao.getNat_op()).apply();

                                    prefs.edit().putString("data_emissao", sincronizacao.data_emissao).apply();
                                    prefs.edit().putString("protocolo", sincronizacao.getProtocolo()).apply();
                                    prefs.edit().putString("tp_nf", sincronizacao.getTp_nf()).apply();
                                    //
                                    addPedido(
                                            sincronizacao.getNnf(),
                                            getString(R.string.autorizada),
                                            sincronizacao.getProtocolo(),
                                            cAux.inserirDataAtual(),
                                            cAux.horaAtual(),
                                            sincronizacao.getTotal_nota(),
                                            cpfCnpj_cliente.getText().toString()
                                    );

                                    msg("NFe transmitida com sucesso!");

                                } else {
                                    erroTransmitir = true;
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ValidarNFE> call, @NonNull Throwable t) {

                        NSdadosNFCeCont.setVisibility(View.VISIBLE);
                        NSdadosNFCe.setVisibility(View.GONE);
                        NSdadosPedidos.setVisibility(View.GONE);

                        erroTransmitir = true;

                        //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                        if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }
                    }
                });

            } catch (Exception e) {
                //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }

                Log.i(TAG, Objects.requireNonNull(e.getMessage()));
                erroTransmitir = true;
            }

            if (erroTransmitir) {
                msg("Não foi possível transmitir esta nota!");
            }
        } else {
            msg("Verifique sua conexão com a internet!");
        }
    }

    private void msg(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private void addPedido(
            String idPed,
            String status,
            String protocolo,
            String dataEmissao,
            String horaEmissao,
            String vlTotal,
            String cliente
    ) {
        //
        valorUnit = String.valueOf(cAux.converterValores(vlt.getText().toString()));

        //MULTIPLICA O VALOR PELA QUANTIDADE
        String[] multiplicar = {valorUnit, String.valueOf(random)};
        total = String.valueOf(cAux.multiplicar(multiplicar));

        Log.i(TAG + " - addPedidosNFE", idPed + "|" + status + "|" + protocolo + "|" + dataEmissao + "|" + horaEmissao + "|" + cAux.soNumeros(vlTotal) + "|" + cliente);
        Log.i(TAG + " - addItensPedidosNFE", idPed + "|" + bd.getIdProduto(produto.getText().toString()) + "|" + quantidade + "|" + cAux.soNumeros(String.valueOf(cAux.converterValores(vlt.getText().toString()))));

        //
        bd.addPedidosNFE(new PedidosNFE(
                idPed,
                status,
                protocolo,
                dataEmissao,
                horaEmissao,
                cAux.soNumeros(vlTotal),
                cliente
        ));

        //
        /*bd.addItensPedidosNFE(new ItensPedidos(
                idPed,//ID PEDIDO
                bd.getIdProduto(produto.getText().toString()),
                quantidade + "",
                cAux.soNumeros(String.valueOf(cAux.converterValores(vlt.getText().toString()))),
                null,
                ""
        ));*/

        for (ProdutosPedidoDomain produto : bd.getProdutosPedidoNFe("1")) {
            bd.addItensPedidosNFE(new ItensPedidos(
                    idPed,//ID PEDIDO
                    produto.id_produto,
                    produto.quantidade,
                    cAux.soNumeros(produto.valor),
                    null,
                    ""
            ));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnPrint) {
            if (prefs.getString("tamPapelImpressora", "").equalsIgnoreCase("")) {
                selectTamPapImpressora();
            } else {
                imprimirPedido();
            }
        }
    }

    private void imprimirPedido() {
        //double v0 = Double.parseDouble(total);
        //double v1 = bd.getTributosProduto(produto.getText().toString()) / 100;
        String tributo = bd.getTributosProduto(produto.getText().toString(), total);
        Intent i;
        if (new Configuracoes().GetDevice()) {
            prefs.edit().putString("tamPapelImpressora", "58mm").apply();
            i = new Intent(context, ImpressoraPOS.class);
        } else {
            i = new Intent(context, Impressora.class);
        }

        ArrayList<Unidades> elementosUnidade = bd.getUnidades();
        unidades = elementosUnidade.get(0);

        //UNIDADE
        i.putExtra("razao_social", unidades.getRazao_social());
        i.putExtra("cnpj", "CNPJ: " + unidades.getCnpj() + " I.E.: " + unidades.getIe());
        i.putExtra("endereco", unidades.getEndereco() + ", " + unidades.getNumero());
        i.putExtra("bairro", unidades.getBairro() + ", " + unidades.getCidade() + ", " + unidades.getUf());
        i.putExtra("cep", unidades.getCep() + "  " + unidades.getTelefone());

        //NOTA
        i.putExtra("imprimir", "nfe");
        i.putExtra("pedido", "" + id);
        i.putExtra("cliente", (!cpfCnpj_cliente.getText().toString().equals("") ? cpfCnpj_cliente.getText().toString() : "CONSUMIDOR NAO IDENTIFICADO"));
        i.putExtra("id_produto", "" + bd.getIdProduto(produto.getText().toString()));
        i.putExtra("produto", produto.getText().toString());
        i.putExtra("chave", bd.gerarChave(id));
        i.putExtra("protocolo", (
                protocoloNota.getText().toString().equals("EMITIDA EM CONTINGENCIA") ?
                        "EMITIDA EM CONTINGENCIA" :
                        protocoloNota.getText().toString() + " - {br}" + dataHoraNota.getText().toString()
        ));
        i.putExtra("quantidade", qnt.getText().toString());
        i.putExtra("valor", "" + cAux.maskMoney(new BigDecimal(String.valueOf(total))));
        i.putExtra("valorUnit", "" + cAux.maskMoney(new BigDecimal(String.valueOf(valorUnit))));
        i.putExtra("tributos", "" + cAux.maskMoney(new BigDecimal(String.valueOf(tributo))));
        i.putExtra("form_pagamento", "" + formaPagamento.getText().toString());

        startActivity(i);
    }

    private void selectTamPapImpressora() {
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("");
        //define a mensagem
        builder.setMessage("Qual o tamanho do papel de sua impressora?");

        //define um botão como positivo
        builder.setNeutralButton("Papel de 58mm", (arg0, arg1) -> {
            prefs.edit().putString("tamPapelImpressora", "58mm").apply();
            imprimirPedido();
        });

        builder.setPositiveButton("Papel de 80mm", (arg0, arg1) -> {
            prefs.edit().putString("tamPapelImpressora", "80mm").apply();
            imprimirPedido();
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();
    }

}
