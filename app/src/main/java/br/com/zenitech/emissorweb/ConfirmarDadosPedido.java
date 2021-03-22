package br.com.zenitech.emissorweb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

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
    private String TAG = "ConfirmarDadosPedidos";
    private ProgressDialog pd;
    private Context contexto;
    AlertDialog alerta;

    Button btnPrintNotaCont, btnPrint, btn_sair, btn_fechar;
    ConfirmarDadosPedido mActivity;

    TextView cpfCnpj_cliente, formaPagamento, produto, qnt, vlt, vltTotal,
            statusNota, protocoloNota, dataHoraNota;

    //
    private SharedPreferences prefs;
    private SharedPreferences.Editor ed;
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

    ArrayList<ItensPedidos> elementosItens;
    ItensPedidos itensPedidos;

    String idFormaPagamento, credenciadora, cod_aut, nsu;

    //QUANTIDADE FRAGMENTADA
    int quantidade = 0;
    int random;
    int transmitir;
    int transmitindo;

    // INFORMAR ERRO
    private boolean erroTransmitir = false;

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
        if (posApp.getSerial().equals("005000002")) {
            modo_teste = true;
        }

        contexto = ConfirmarDadosPedido.this;

        btn_sair = findViewById(R.id.btn_sair);

        btn_fechar = findViewById(R.id.btn_fechar);
        btn_fechar.setOnClickListener(this);

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

                //-------CRIA UM ID PARA O PEDIDO------//
                //ed.putInt("id_pedido", (prefs.getInt("id_pedido", 0) + 1)).apply();
                //id = prefs.getInt("id_pedido", 1);

                boolean siac = false;

                try {
                    if (Objects.requireNonNull(params.getString("siac")).equals("1")) {
                        siac = true;
                    }
                } catch (Exception ignored) {

                }

                if (siac) {

                    //DADOS SIAC WEB
                    cpfCnpj_cliente.setText(getString(R.string.consumidor_nao_identificado));
                    produto.setText(params.getString("produto"));
                    qnt.setText(params.getString("quantidade"));
                    vlt.setText(String.format("R$%s", cAux.maskMoney(new BigDecimal(params.getString("valor_unit")))));
                    idFormaPagamento = "1";
                    btn_sair.setVisibility(View.GONE);
                    btn_fechar.setVisibility(View.VISIBLE);

                } else {

                    cpfCnpj_cliente.setText(params.getString("cpfCnpj_cliente"));
                    formaPagamento.setText(params.getString("formaPagamento"));
                    produto.setText(params.getString("produto"));
                    qnt.setText(params.getString("qnt"));
                    vlt.setText(params.getString("vlt"));

                    //FORMAS DE PAGAMENTO
                    String s = removerAcentos(params.getString("formaPagamento"));
                    Log.i(TAG + " removerAcentos - ", s);
                    if (Objects.requireNonNull(s).equalsIgnoreCase("DINHEIRO")) {
                        idFormaPagamento = "1";

                    } else if (s.equalsIgnoreCase("CHEQUE")) {
                        idFormaPagamento = "2";

                    } else if (s.equalsIgnoreCase("CARTAO DE CREDITO")) {
                        idFormaPagamento = "3";
                        Log.i(TAG + " ID Form Pag - ", idFormaPagamento);

                    } else if (s.equalsIgnoreCase("CARTAO DE DEBITO")) {
                        idFormaPagamento = "4";

                    } else if (s.equalsIgnoreCase("CREDITO LOJA")) {
                        idFormaPagamento = "5";

                    } else if (s.equalsIgnoreCase("VALE ALIMENTACAO")) {
                        idFormaPagamento = "6";

                    } else if (s.equalsIgnoreCase("VALE REFEICAO")) {
                        idFormaPagamento = "7";

                    } else if (s.equalsIgnoreCase("VALE PRESENTE")) {
                        idFormaPagamento = "8";

                    } else if (s.equalsIgnoreCase("VALE COMBUSTIVEL")) {
                        idFormaPagamento = "9";

                    } else if (s.equalsIgnoreCase("OUTROS")) {
                        idFormaPagamento = "10";
                    }

                    credenciadora = params.getString("credenciadora");
                    cod_aut = params.getString("cod_aut");
                    nsu = params.getString("nsu");
                }

                //
                valorUnit = String.valueOf(cAux.converterValores(vlt.getText().toString()));

                //MULTIPLICA O VALOR PELA QUANTIDADE
                String[] multiplicar = {valorUnit, qnt.getText().toString()};
                total = String.valueOf(cAux.multiplicar(multiplicar));
                vltTotal.setText(String.format("R$%s", cAux.maskMoney(new BigDecimal(total))));

                quantidade = Integer.parseInt(qnt.getText().toString());


                btnPrint = findViewById(R.id.btnPrint);
                btnPrint.setOnClickListener(this);

                btnPrintNotaCont = findViewById(R.id.btnPrintNotaCont);
                btnPrintNotaCont.setOnClickListener(this);

                if (unidades.getUf().equalsIgnoreCase(new Configuracoes().GetUFCeara())) {
                    btnPrintNotaCont.setText("Salvar e transmitir depois!");
                }

                if (quantidade > 5) {
                    btnPrint.setVisibility(View.GONE);
                    btnPrintNotaCont.setVisibility(View.GONE);
                }

                //SE A QUANTIDADE FOR MAIOR QUE 5 FRACIONA EM NOTAS DE ATÉ 5 UNIDADES
                fracionar();

            }
        }


        findViewById(R.id.btn_transmitir).setOnClickListener(v -> {
            //TRANSMITIR
            transmitirNota();
        });

        findViewById(R.id.btn_reTransmitir).setOnClickListener(v -> {
            //TRANSMITIR
            transmitirNota();
        });

        findViewById(R.id.btn_sair).setOnClickListener(v -> {
            //
            Intent i = new Intent(contexto, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });
    }

    public static String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    public void fracionar() {
        if (quantidade > 5) {
            Random r = new Random();
            random = r.nextInt(6 - 1) + 1;
        } else {
            random = quantidade;
        }

        //
        //Log.i("DIV", String.valueOf(random));

        String[] sub = {String.valueOf(quantidade), String.valueOf(random)};
        quantidade = cAux.subitrair(sub).intValue();

        //
        String data = cAux.exibirDataAtual();
        String hora = cAux.horaAtual();
        statusNota.setText("");
        protocoloNota.setText("");
        //dataHoraNota.setText(String.format("%s %s", cAux.exibirDataAtual(), cAux.horaAtual()));
        dataHoraNota.setText(String.format("%s %s", data, hora));

        //-------CRIA UM ID PARA O PEDIDO------//
        ed.putInt("id_pedido", (prefs.getInt("id_pedido", 0) + 1)).apply();
        id = prefs.getInt("id_pedido", 1);

        //INSERI O PEDIDO NO BANCO DE DADOS
        addPedido(
                "",
                protocoloNota.getText().toString(),
                cAux.inserirData(data),
                hora,
                total,
                "",
                "",
                cpfCnpj_cliente.getText().toString(),
                idFormaPagamento,
                String.valueOf(random)
        );

        if (quantidade > 0) {
            fracionar();
        } else {
            //elementosPedidos = bd.getPedidosTransmitir();
            elementosPedidos = bd.getPedidosTransmitirFecharDia();
            transmitir = elementosPedidos.size();
            transmitindo = elementosPedidos.size();

            Log.i(TAG, String.valueOf(elementosPedidos.size()));
        }
    }

    int count = 1;
    //int retransmitir = 0;

    int erro = 0;

    private void transmitirNota() {
        //ESCODER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (transmitindo != 0) {
            NSdadosPedidos.setVisibility(View.GONE);

            //MOSTRA A MENSAGEM DE SINCRONIZAÇÃO
            pd = ProgressDialog.show(contexto, count + "/" + transmitir, "Transmitindo...",
                    true, false);

            count++;


            String[] subNotaPed = {String.valueOf(transmitindo), "1"};
            //cAux.subitrair(subNotaPed);
            int linhaPed = cAux.subitrair(subNotaPed).intValue();

            //
            pedidos = elementosPedidos.get(linhaPed);
            //
            elementosItens = bd.getItensPedido(pedidos.getId());

            try {
                if (elementosItens.size() != 0) {
                    itensPedidos = elementosItens.get(0);

                    transmitindo = linhaPed;

                    //
                    final IValidarNFCe iValidarNFCe = IValidarNFCe.retrofit.create(IValidarNFCe.class);

                    //Toast.makeText(contexto, valorUnit, Toast.LENGTH_LONG).show();
                    final Call<ValidarNFCe> call = iValidarNFCe.validarNota(
                            pedidos.getId(),
                            itensPedidos.getQuantidade(),
                            posApp.getSerial(),
                            itensPedidos.getProduto(),
                            itensPedidos.getValor().replace(".", ""),
                            pedidos.getForma_pagamento(),
                            pedidos.getCpf_cliente(),
                            credenciadora,
                            cod_aut,
                            nsu
                    );

                    call.enqueue(new Callback<ValidarNFCe>() {
                        @Override
                        public void onResponse(@NonNull Call<ValidarNFCe> call, @NonNull Response<ValidarNFCe> response) {

                            //
                            final ValidarNFCe sincronizacao = response.body();
                            if (sincronizacao != null) {

                                //
                                runOnUiThread(() -> {

                                    //CANCELA A MENSAGEM DE SINCRONIZAÇÃO
                                    if (pd != null && pd.isShowing()) {
                                        pd.dismiss();
                                    }

                                    // MODO TESTE SERIAL 123456780
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
                                        } else {

                                            //
                                            bd.upadtePedidosTransmissao(
                                                    "OFF",
                                                    " ",
                                                    "",
                                                    "",
                                                    pedidos.getId()
                                            );

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

                            Log.i(TAG, "" + t);

                            //
                            bd.upadtePedidosTransmissao(
                                    "OFF",
                                    " ",
                                    "",
                                    "",
                                    pedidos.getId()
                            );

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
                Toast.makeText(contexto, "Encontramos erro ao transmitir uma mais notas!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(contexto, "NFC-e transmitida com sucesso!", Toast.LENGTH_LONG).show();
            }
            //finish();
        }
    }

    private void addPedido(
            String status,
            String protocolo,
            String dataEmissao,
            String horaEmissao,
            String vlTotal,
            String dataProtocolo,
            String horaProtocolo,
            String cpf,
            String FPagamento,
            String quantidade
    ) {


        //
        valorUnit = String.valueOf(cAux.converterValores(vlt.getText().toString()));

        //MULTIPLICA O VALOR PELA QUANTIDADE
        String[] multiplicar = {valorUnit, String.valueOf(random)};
        total = String.valueOf(cAux.multiplicar(multiplicar));
        //vltTotal.setText("R$" + cAux.maskMoney(new BigDecimal(String.valueOf(total))));

        //
        bd.addPedidos(new Pedidos(
                String.valueOf(id),//ID PEDIDO
                status,//SITUAÇÃO
                protocolo,//PROTOCOLO
                dataEmissao,//DATA EMISSÃO
                horaEmissao,//HORA EMISSÃO
                "" + cAux.converterValores(cAux.soNumeros(total)),//VALOR TOTAL
                dataProtocolo,//DATA PROTOCOLO - "28042017"
                horaProtocolo,//HORA PROTOCOLO - "151540"
                cpf,//CPF/CNPJ CLIENTE
                FPagamento//FORMA PAGAMENTO
        ));

        //
        bd.addItensPedidos(new ItensPedidos(
                String.valueOf(id),//ID PEDIDO
                bd.getIdProduto(produto.getText().toString()),
                quantidade,
                cAux.soNumeros(String.valueOf(cAux.converterValores(vlt.getText().toString())))
        ));
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
                    //double v0 = Double.parseDouble(total);
                    //double v1 = bd.getTributosProduto(produto.getText().toString());
                    //double tributo = v0 - (v0 - (v1 * v0));
                    double tributo = bd.getTributosProduto(produto.getText().toString(), total);
                    double tributoN = bd.getTributosNProduto(produto.getText().toString(), total);
                    double tributoE = bd.getTributosEProduto(produto.getText().toString(), total);
                    double tributoM = bd.getTributosMProduto(produto.getText().toString(), total);
                    Intent i = new Intent(contexto, ImpressoraPOS.class);

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
                    i.putExtra("pedido", "" + id);
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
                    i.putExtra("valor", cAux.maskMoney(new BigDecimal(String.valueOf(total))));
                    i.putExtra("valorUnit", cAux.maskMoney(new BigDecimal(String.valueOf(valorUnit))));
                    i.putExtra("tributos", cAux.maskMoney(new BigDecimal(String.valueOf(tributo))));
                    i.putExtra("tributosN", cAux.maskMoney(new BigDecimal(String.valueOf(tributoN))));
                    i.putExtra("tributosE", cAux.maskMoney(new BigDecimal(String.valueOf(tributoE))));
                    i.putExtra("tributosM", cAux.maskMoney(new BigDecimal(String.valueOf(tributoM))));
                    i.putExtra("form_pagamento", "" + formaPagamento.getText().toString());

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
                        //double v0 = Double.parseDouble(total);
                        //double v1 = bd.getTributosProduto(produto.getText().toString()) / 100;
                        //double tributo = v0 - (v0 - (v1 * v0));
                        double tributo = bd.getTributosProduto(produto.getText().toString(), total);
                        double tributoN = bd.getTributosNProduto(produto.getText().toString(), total);
                        double tributoE = bd.getTributosEProduto(produto.getText().toString(), total);
                        double tributoM = bd.getTributosMProduto(produto.getText().toString(), total);

                        /*
                        PackageManager packageManager = getPackageManager();
                        String packageName = "br.com.zenitech.impressora";
                        Intent i = packageManager.getLaunchIntentForPackage(packageName);
                        */

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
                        i.putExtra("valor", cAux.maskMoney(new BigDecimal(String.valueOf(total))));
                        i.putExtra("valorUnit", cAux.maskMoney(new BigDecimal(String.valueOf(valorUnit))));
                        i.putExtra("tributos", cAux.maskMoney(new BigDecimal(String.valueOf(tributo))));
                        i.putExtra("tributosN", cAux.maskMoney(new BigDecimal(String.valueOf(tributoN))));
                        i.putExtra("tributosE", cAux.maskMoney(new BigDecimal(String.valueOf(tributoE))));
                        i.putExtra("tributosM", cAux.maskMoney(new BigDecimal(String.valueOf(tributoM))));
                        i.putExtra("form_pagamento", "" + formaPagamento.getText().toString());

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
                        String.valueOf(id)
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
                finish();
                break;
            }
        }
    }

    private void selectTamPapImpressora() {

        //
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

        /*//define um botão como negativo.
        builder.setNegativeButton("Negativo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(InformacoesVagas.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
            }
        });*/

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();
    }
}
