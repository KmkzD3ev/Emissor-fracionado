package br.com.zenitech.emissorweb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.domains.ValidarNFCe;
import br.com.zenitech.emissorweb.interfaces.IValidarNFCe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmarDadosPedido extends AppCompatActivity implements View.OnClickListener {
    // ATIVA O MODO TESTE
    private boolean modo_teste = false;
    private final String TAG = "ConfirmarDadosPedidos";
    private ProgressDialog pd;
    private Context contexto;
    VerificarOnline verificarOnline;
    AlertDialog alerta;
    Button btnPrintNotaCont, btnPrint, btn_sair, btn_fechar;
    ConfirmarDadosPedido mActivity;
    TextView cpfCnpj_cliente, formaPagamento, produto, qnt, vlt, vltTotal,
            statusNota, protocoloNota, dataHoraNota, desconto;
    private SharedPreferences prefs;
    SharedPreferences.Editor ed;
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
    ArrayList<Pedidos> elementosPedidos;
    Pedidos pedidos;
    Pedidos infoPedido;
    ArrayList<ItensPedidos> elementosItens;
    ItensPedidos itensPedidos;
    String idFormaPagamento, credenciadora, cod_aut, nsu, bandeira;
    int quantidade = 0;
    int transmitir;
    int transmitindo;
    int count = 1;
    int erro = 0;
    private boolean erroTransmitir = false;
    String idUltPedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar_dados_pedido);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActivity = this;

        NSdadosPedidos = findViewById(R.id.NSdadosPedidos);
        NSdadosNFCe = findViewById(R.id.NSdadosNFCe);
        NSdadosNFCeCont = findViewById(R.id.NSdadosNFCeCont);
        statusNota = findViewById(R.id.statusNota);
        protocoloNota = findViewById(R.id.protocoloNota);
        dataHoraNota = findViewById(R.id.dataHoraNota);

        //
        cAux = new ClassAuxiliar();

        //
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();

        //
        bd = new DatabaseHelper(this);

        elementos = bd.getUnidades();
        unidades = elementos.get(0);

        elementosPos = bd.getPos();
        posApp = elementosPos.get(0);

        // SE O SERIAL FOR DE TESTE ATIVA O MODO TESTE
        if (posApp.getSerial().equals("005000002*")) {
            modo_teste = true;
        }

        contexto = ConfirmarDadosPedido.this;
        verificarOnline = new VerificarOnline();

        btn_sair = findViewById(R.id.btn_sair);

        btn_fechar = findViewById(R.id.btn_fechar);
        btn_fechar.setOnClickListener(this);

        cpfCnpj_cliente = findViewById(R.id.cpfCnpj_cliente);
        formaPagamento = findViewById(R.id.formaPagamento);
        produto = findViewById(R.id.produto);
        qnt = findViewById(R.id.qnt);
        vlt = findViewById(R.id.vlt);
        vltTotal = findViewById(R.id.vltTotal);
        desconto = findViewById(R.id.desconto);

        //
        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {

                boolean siac = false;

                try {
                    if (Objects.requireNonNull(params.getString("siac")).equals("1")) {
                        siac = true;
                    }
                } catch (Exception ignored) {

                }

                if (siac) {
                    /*
                    String valorUnit = params.getString("valor_unit");
                    String quantidade = params.getString("quantidade");

                    Log.d(TAG, "RECEBENDO Valor unitário recebido: " + valorUnit);
                    Log.d(TAG, "RECEBEdoQuantidade recebida: " + quantidade);*/

                    //DADOS SIAC WEB
                    cpfCnpj_cliente.setText(getString(R.string.consumidor_nao_identificado));
                    produto.setText(params.getString("produto"));
                    qnt.setText(params.getString("quantidade"));
                    vlt.setText(String.format("R$%s", cAux.maskMoney(new BigDecimal(params.getString("valor_unit")))));
                    idFormaPagamento = "1";
                    btn_sair.setVisibility(View.GONE);
                    btn_fechar.setVisibility(View.VISIBLE);

                } else {
                    idUltPedido = bd.getUltimoIdPedido();
                    infoPedido = bd.getPedido(idUltPedido);
                    //cpfCnpj_cliente.setText(params.getString("cpfCnpj_cliente"));
                    try {
                        cpfCnpj_cliente.setText(infoPedido.getCpf_cliente());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    formaPagamento.setText(bd.getFormasPagamentoPedidoPrint(bd.getIdPedidoTemp()));
                    //produto.setText(params.getString("produto"));
                    produto.setText(bd.getProdutosPedidoConfirmacao(Integer.parseInt(bd.getIdPedidoTemp()), cAux));
                    //qnt.setText(params.getString("qnt"));
                    //vlt.setText(params.getString("vlt"));
                    //Log.e("Desconto", bd.getItensPedido(idUltPedido).get(0).getDesconto());
                    //desconto.setText(String.format("R$%s", cAux.maskMoney(new BigDecimal(bd.getItensPedido(idUltPedido).get(0).getDesconto()))));//des
                    idFormaPagamento = bd.getFormasPagamentoPedido(bd.getIdPedidoTemp());

                    String v = bd.getValorTotalFinanceiro(bd.getIdPedidoTemp(), false);
                    vltTotal.setText(String.format("R$%s", cAux.maskMoney(new BigDecimal(v))));
                    total = v;

                    credenciadora = params.getString("credenciadora");
                    cod_aut = params.getString("cod_aut");
                    nsu = params.getString("nsu");
                }

                /*//
                valorUnit = String.valueOf(cAux.converterValores(vlt.getText().toString()));

                Toast.makeText(contexto, "valor: "+ valorUnit, Toast.LENGTH_SHORT).show();
                Toast.makeText(contexto, "Quant: "+ qnt.getText().toString(), Toast.LENGTH_SHORT).show();

                //MULTIPLICA O VALOR PELA QUANTIDADE
                //String[] multiplicar = {valorUnit, qnt.getText().toString()};
                String[] multiplicar = {"100.00", "1"};
                total = String.valueOf(cAux.multiplicar(multiplicar));

                String[] subtrairTotDesc = {total, String.valueOf(cAux.converterValores(desconto.getText().toString()))};
                String totalComDesconto = String.valueOf(cAux.subitrair(subtrairTotDesc));
                vltTotal.setText(String.format("R$%s", cAux.maskMoney(new BigDecimal(totalComDesconto))));*/

                // RECEBE A QUANTIDADE DE PRODUTOS COM O NCM = 27111910
                //quantidade = bd.getQuantProdutosPedidoNCM(id);      // QUANTIDADE DE GÁS
                quantidade = 1;// Integer.parseInt(qnt.getText().toString());

                btnPrint = findViewById(R.id.btnPrint);
                btnPrint.setOnClickListener(this);

                btnPrintNotaCont = findViewById(R.id.btnPrintNotaCont);
                btnPrintNotaCont.setOnClickListener(this);

                if (unidades.getUf().equalsIgnoreCase(new Configuracoes().GetUFCeara())) {
                    btnPrintNotaCont.setText("Salvar e transmitir depois!");
                }

                /*if (quantidade > 5) {
                    btnPrint.setVisibility(View.GONE);
                    btnPrintNotaCont.setVisibility(View.GONE);
                }*/

                //SE A QUANTIDADE FOR MAIOR QUE 5 FRACIONA EM NOTAS DE ATÉ 5 UNIDADES
                //fracionar();

                elementosPedidos = bd.getPedidosTransmitirFecharDia();
                transmitir = elementosPedidos.size();
                transmitindo = elementosPedidos.size();

                if (elementosPedidos.size() > 1) {
                    btnPrint.setVisibility(View.GONE);
                    btnPrintNotaCont.setVisibility(View.GONE);
                }

                Log.i(TAG, String.valueOf(elementosPedidos.size()));
            }
        }


        findViewById(R.id.btn_transmitir).setOnClickListener(v -> {
            //TRANSMITIR
            transmitirNota();
            /*if (verificarOnline.isOnline(contexto))
                transmitirNota();
            else
                Toast.makeText(contexto, "Sem Internet", Toast.LENGTH_SHORT).show();*/
        });

        findViewById(R.id.btn_reTransmitir).setOnClickListener(v -> {
            //TRANSMITIR
            transmitirNota();
            /*if (verificarOnline.isOnline(contexto))
                transmitirNota();
            else
                Toast.makeText(contexto, "Sem Internet", Toast.LENGTH_SHORT).show();*/
        });

        findViewById(R.id.btn_sair).setOnClickListener(v -> {
            //
            Intent i = new Intent(contexto, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        // Add the callback to the back stack
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private void transmitirNota() {
        //ESCODER O TECLADO
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (transmitindo != 0) {
            NSdadosPedidos.setVisibility(View.GONE);

            //MOSTRA A MENSAGEM DE SINCRONIZAÇÃO
            pd = ProgressDialog.show(contexto, count + "/" + transmitir, "Transmitindo...",
                    true, false);

            count++;


            String[] subNotaPed = {String.valueOf(transmitindo), "1"};
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
                    valorUnit = String.valueOf(cAux.converterValores(vlt.getText().toString()));
                    String valorFormaPGPedido, idFormaPGPedido, nAutoCartao, bandeiraFPG, nsuFPG;

                    Log.e("IDTEMP", "" + pedidos.getId());

                    valorFormaPGPedido = bd.getValoresFormasPagamentoPedido(pedidos.getId()).replace(".", "");
                    nsuFPG = bd.getNSUFormasPagamentoPedido(pedidos.getId()).replace(".", "");
                    idFormaPGPedido = bd.getIdFormasPagamentoPedido(pedidos.getId()).replace(".", "");
                    bandeiraFPG = bd.getBandeiraFormasPagamentoPedido(pedidos.getId()).replace(".", "");
                    nAutoCartao = bd.getAutorizacaoFormasPagamentoPedido(pedidos.getId()).replace(".", "");

                    String idsProdutosPedido = bd.getIdsProdutosPedido(pedidos.getId()).replace(".", "");
                    String quantidadesProdutosPedido = bd.getQuantidadesProdutosPedido(pedidos.getId()).replace(".", "");
                    String valorProdutosPedido = bd.getValorProdutosPedido(pedidos.getId()).replace(".", "");

                    // SE FOR PINPAD OU POS A CREDENCIADORA SERÁ STONE
                   /* if (!unidades.getCodloja().equalsIgnoreCase("")) {
                        credenciadora = "STONE";
                    }*/

                    credenciadora = unidades.getCredenciadora();

                    final Call<ValidarNFCe> call = iValidarNFCe.transmitirNFCe(
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

                            final ValidarNFCe sincronizacao = response.body();
                            if (sincronizacao != null) {
                                runOnUiThread(() -> {

                                    //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                                    if (pd != null && pd.isShowing()) {
                                        pd.dismiss();
                                    }

                                    // CASO O MODO TESTE ESTEJA ATIVO, FAZ O UPDATE PARA "ON" MESMO SEM PROTOCOLO
                                    if (!modo_teste) {
                                        if (!sincronizacao.getProtocolo().isEmpty() && sincronizacao.getProtocolo().length() >= 10) {

                                            //
                                            statusNota.setText(getString(R.string.autorizada));
                                            protocoloNota.setText(sincronizacao.getProtocolo());
                                            dataHoraNota.setText(String.format("%s %s", cAux.exibirDataAtual(), cAux.horaAtual()));

                                            //
                                            bd.upadtePedidosTransmissao(
                                                    "ON",
                                                    sincronizacao.getProtocolo(),
                                                    cAux.soNumeros(cAux.inserirDataAtual()),
                                                    cAux.soNumeros(cAux.horaAtual()),
                                                    pedidos.getId()
                                            );

                                            //Toast.makeText(contexto, "NFC-e transmitida com sucesso!", Toast.LENGTH_LONG).show();
                                        } else {
                                            erroTransmitir = true;
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

                            NSdadosNFCeCont.setVisibility(View.VISIBLE);
                            NSdadosNFCe.setVisibility(View.GONE);
                            NSdadosPedidos.setVisibility(View.GONE);

                            erroTransmitir = true;

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
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }

                Log.i(TAG, Objects.requireNonNull(e.getMessage()));
                erroTransmitir = true;
            }
        } else {
            if (erroTransmitir) {
                Toast.makeText(contexto, "Encontramos erro ao transmitir uma ou mais notas!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(contexto, "NFC-e transmitida com sucesso!", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        //
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnPrint: {
                if (new Configuracoes().GetDevice()) {
                    prefs.edit().putString("tamPapelImpressora", "58mm").apply();

                    String tributo = bd.getTributosProduto(produto.getText().toString(), total);
                    String tributoN = bd.getTributosNProduto(produto.getText().toString(), total);
                    String tributoE = bd.getTributosEProduto(produto.getText().toString(), total);
                    String tributoM = bd.getTributosMProduto(produto.getText().toString(), total);
                    Intent i = new Intent(contexto, ImpressoraPOS.class);

                    ArrayList<Unidades> elementosUnidade = bd.getUnidades();
                    unidades = elementosUnidade.get(0);

                    //UNIDADE
                    i.putExtra("razao_social", unidades.getRazao_social());
                    i.putExtra("cnpj", "CNPJ: " + unidades.getCnpj() + " I.E.: " + unidades.getIe());
                    i.putExtra("endereco", unidades.getEndereco() + ", " + unidades.getNumero());
                    i.putExtra("bairro", unidades.getBairro() + ", " + unidades.getCidade() + ", " + unidades.getUf());
                    i.putExtra("cep", unidades.getCep() + "  " + unidades.getTelefone());

                    //NOTA
                    i.putExtra("imprimir", "nota");
                    i.putExtra("pedido", "" + idUltPedido);// id
                    i.putExtra("cliente", (!cpfCnpj_cliente.getText().toString().equals("") ? cpfCnpj_cliente.getText().toString() : "CONSUMIDOR NAO IDENTIFICADO"));
                    i.putExtra("id_produto", "" + bd.getIdProduto(produto.getText().toString()));
                    i.putExtra("produto", produto.getText().toString());
                    i.putExtra("chave", bd.gerarChave(id));
                    i.putExtra("protocolo", (
                            protocoloNota.getText().toString().equals("EMITIDA EM CONTINGENCIA") ?
                                    "EMITIDA EM CONTINGENCIA" :
                                    protocoloNota.getText().toString() + " - " + dataHoraNota.getText().toString()
                    ));
                    i.putExtra("quantidade", qnt.getText().toString());
                    i.putExtra("valor", cAux.maskMoney(new BigDecimal(total)));
                    i.putExtra("valorComDesconto", cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(vltTotal.getText().toString())))));
                    i.putExtra("valorUnit", cAux.maskMoney(new BigDecimal(String.valueOf(valorUnit))));
                    i.putExtra("tributos", cAux.maskMoney(new BigDecimal(String.valueOf(tributo))));
                    i.putExtra("tributosN", cAux.maskMoney(new BigDecimal(String.valueOf(tributoN))));
                    i.putExtra("tributosE", cAux.maskMoney(new BigDecimal(String.valueOf(tributoE))));
                    i.putExtra("tributosM", cAux.maskMoney(new BigDecimal(String.valueOf(tributoM))));
                    //i.putExtra("form_pagamento", "" + formaPagamento.getText().toString());
                    i.putExtra("form_pagamento", bd.getFormasPagamentoPedidoPrint(idUltPedido));
                    i.putExtra("desconto", cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(desconto.getText().toString())))));
                    //bd.getFormasPagamentoPedidoPrint(String.valueOf(id));

                    Log.e(TAG,
                            "pedido: " + id + "\n" +
                                    "cliente: " + (!cpfCnpj_cliente.getText().toString().equals("") ? cpfCnpj_cliente.getText().toString() : "CONSUMIDOR NAO IDENTIFICADO") + "\n" +
                                    "id_produto: " + bd.getIdProduto(produto.getText().toString()) + "\n" +
                                    "produto: " + produto.getText().toString() + "\n" +
                                    "protocolo: " + protocoloNota.getText().toString() + "\n" +
                                    "quantidade: " + qnt.getText().toString() + "\n" +
                                    "valor: " + "" + cAux.maskMoney(new BigDecimal(String.valueOf(total))) + "\n" +
                                    "valorUnit: " + "" + cAux.maskMoney(new BigDecimal(String.valueOf(valorUnit))) + "\n" +
                                    "tributos: " + "" + cAux.maskMoney(new BigDecimal(String.valueOf(tributo)))
                    );

                    startActivity(i);
                } else {
                    if (prefs.getString("tamPapelImpressora", "").equalsIgnoreCase("")) {
                        selectTamPapImpressora();
                    } else {
                        //double tributo = bd.getTributosProduto(produto.getText().toString(), total);
                        double[] tributos = bd.getTributosProdutosPedido(idUltPedido, total);
                        double tributo = tributos[0];// bd.getTributosProdutosPedido(idUltPedido, total);
                        double tributoN = tributos[1];// bd.getTributosNProduto(produto.getText().toString(), total);
                        double tributoE = tributos[2];//bd.getTributosEProduto(produto.getText().toString(), total);
                        double tributoM = tributos[3];//bd.getTributosMProduto(produto.getText().toString(), total);

                        Intent i = new Intent(contexto, Impressora.class);

                        String serie = bd.getSeriePOS();
                        ArrayList<Unidades> elementosUnidade = bd.getUnidades();
                        unidades = elementosUnidade.get(0);

                        //UNIDADE
                        i.putExtra("razao_social", unidades.getRazao_social());
                        i.putExtra("cnpj", "CNPJ: " + unidades.getCnpj() + " I.E.: " + unidades.getIe());
                        i.putExtra("endereco", unidades.getEndereco() + ", " + unidades.getNumero());
                        i.putExtra("bairro", unidades.getBairro() + ", " + unidades.getCidade() + ", " + unidades.getUf());
                        i.putExtra("cep", unidades.getCep() + "  " + unidades.getTelefone());

                        //NOTA
                        i.putExtra("imprimir", "nota");
                        i.putExtra("pedido", "" + idUltPedido);// id
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
                        i.putExtra("valor", cAux.maskMoney(new BigDecimal(String.valueOf(total))));
                        i.putExtra("valorUnit", cAux.maskMoney(new BigDecimal(String.valueOf(valorUnit))));
                        i.putExtra("tributos", cAux.maskMoney(new BigDecimal(String.valueOf(tributo))));
                        i.putExtra("tributosN", cAux.maskMoney(new BigDecimal(String.valueOf(tributoN))));
                        i.putExtra("tributosE", cAux.maskMoney(new BigDecimal(String.valueOf(tributoE))));
                        i.putExtra("tributosM", cAux.maskMoney(new BigDecimal(String.valueOf(tributoM))));
                        //i.putExtra("form_pagamento", "" + formaPagamento.getText().toString());

                        i.putExtra("form_pagamento", bd.getFormasPagamentoPedidoPrint(idUltPedido));

                        Log.e(TAG,
                                "pedido: " + id + "\n" +
                                        "cliente: " + (!cpfCnpj_cliente.getText().toString().equals("") ? cpfCnpj_cliente.getText().toString() : "CONSUMIDOR NAO IDENTIFICADO") + "\n" +
                                        "id_produto: " + bd.getIdProduto(produto.getText().toString()) + "\n" +
                                        "produto: " + produto.getText().toString() + "\n" +
                                        "protocolo: " + protocoloNota.getText().toString() + "\n" +
                                        "quantidade: " + qnt.getText().toString() + "\n" +
                                        "valor: " + cAux.maskMoney(new BigDecimal(String.valueOf(total))) + "\n" +
                                        "valorUnit: " + cAux.maskMoney(new BigDecimal(String.valueOf(valorUnit))) + "\n" +
                                        "tributos: " + cAux.maskMoney(new BigDecimal(String.valueOf(tributo)))
                        );

                        startActivity(i);
                    }
                }

                break;
            }
            case R.id.btnPrintNotaCont: {
                //
                statusNota.setText(getString(R.string.em_contigencia));
                protocoloNota.setText(getString(R.string.emitida_em_contigencia));
                dataHoraNota.setText(String.format("%s %s", cAux.exibirDataAtual(), cAux.horaAtual()));

                //
                bd.upadtePedidosTransmissao(
                        "OFF",
                        " ",
                        "",
                        "",
                        idUltPedido// idString.valueOf(id)
                );

                if (unidades.getUf().equalsIgnoreCase(new Configuracoes().GetUFCeara())) {
                    Toast.makeText(contexto, "Nota salva com sucesso!", Toast.LENGTH_SHORT).show();
                    //
                    Intent i = new Intent(contexto, Principal.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                } else {
                    //
                    NSdadosNFCeCont.setVisibility(View.GONE);
                    NSdadosNFCe.setVisibility(View.VISIBLE);
                    NSdadosPedidos.setVisibility(View.GONE);
                }
                break;
            }
            case R.id.btn_fechar: {
                Intent i = new Intent(contexto, Principal.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                break;
            }
        }
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
        builder.setNeutralButton("Papel de 58mm", (arg0, arg1) -> prefs.edit().putString("tamPapelImpressora", "58mm").apply());

        builder.setPositiveButton("Papel de 80mm", (arg0, arg1) -> prefs.edit().putString("tamPapelImpressora", "80mm").apply());

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();
    }

    OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Intent i = new Intent(contexto, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
    };
}
