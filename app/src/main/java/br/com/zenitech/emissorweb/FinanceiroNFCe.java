package br.com.zenitech.emissorweb;

import static br.com.zenitech.emissorweb.util.AuxFinanceiroNFCe.PAGAMENTO_PIX_REQUEST;
import static br.com.zenitech.emissorweb.util.AuxFinanceiroNFCe.PAGAMENTO_REQUEST;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import br.com.zenitech.emissorweb.adapters.FormasPagamentoPedidosAdapter;
import br.com.zenitech.emissorweb.domains.FormaPagamentoPedido;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.ProdutosPedidoDomain;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.interfaces.IFinanceiroNFCeObserver;
import br.com.zenitech.emissorweb.util.AuxFinanceiroNFCe;

public class FinanceiroNFCe extends AppCompatActivity implements IFinanceiroNFCeObserver {

    //
    Context context;
    DatabaseHelper bd;
    SharedPreferences prefs;
    ClassAuxiliar cAux;
    AuxFinanceiroNFCe fAux;
    boolean api_asaas = false;
    int idTemp;
    int id = 0;

    // Fracionar o pedido caso a quantidade seja maior que 5
    boolean dd = false;
    int quantidade = 0;
    int random;
    // SE 1 INFORMA QUE A NOTA FOI FRACIONADA
    String NotaFracionada = "0";

    //region ARRAYS, ADAPTERS, RECYCLERVIEW E BUTTONS

    // *** Arrays
    ArrayList<String> listaCredenciadoras;
    ArrayList<String> idCredenciadoras;
    ArrayList<FormaPagamentoPedido> listaFinanceiroCliente;
    ArrayList<Unidades> elementos;

    // *** Adapters
    ArrayAdapter<String> adapterFormasPagamentoDinheiro;
    ArrayAdapter<String> adapterFormasPagamento;
    FormasPagamentoPedidosAdapter adapterFPG;

    // *** Recyclerviews
    RecyclerView rvFinanceiro;

    // *** Buttons
    Button btnAddF, btnFinalizarFinanceiro, btnPagCartao, btnPagamentoCartaoNFCE, btnAvancarNFCE, btnAddFormPag;

    // *** TextViews
    TextView txtTotalFinanceiro, txtTotalItemFinanceiro, textIdTemp;

    // *** LinearLayouts
    LinearLayout llCredenciadora;
    LinearLayoutCompat bgTotal;

    EditText txtVencimentoFormaPagamento, txtValorFormaPagamento;

    // *** Domains
    Unidades unidades;

    //endregion

    private Spinner spFormasPagamento, spDescricaoCredenciadora, spBandeiraCredenciadora;
    private EditText etCodAutorizacao, etNsuCeara;
    private TextInputLayout TiNsuCeara;

    //region START CONFIGURACOES
    private void startConfig() {
        //
        context = this;
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        bd = new DatabaseHelper(context);
        cAux = new ClassAuxiliar();
        fAux = new AuxFinanceiroNFCe();

        // RECEBE O ID DO PEDIDO TEMPORÁRIO
        idTemp = Integer.parseInt(bd.getIdPedidoTemp());

        //
        elementos = bd.getUnidades();
        unidades = elementos.get(0);
        if (!unidades.getApi_key_asaas().equalsIgnoreCase("")) {
            api_asaas = true;
        }
    }
    //endregion

    //region START IDS DOS ELEMENTOS DA UI
    private void startIds() {
        //
        spFormasPagamento = findViewById(R.id.spFormasPagamentoCliente);
        rvFinanceiro = findViewById(R.id.rvFinanceiro);
        btnAddF = findViewById(R.id.btnAddF);
        btnPagCartao = findViewById(R.id.btnPagCartao);
        btnFinalizarFinanceiro = findViewById(R.id.btnFinalizarFinanceiro);
        txtTotalFinanceiro = findViewById(R.id.txtTotalFinanceiro);
        txtTotalItemFinanceiro = findViewById(R.id.txtTotalItemFinanceiro);
        textIdTemp = findViewById(R.id.textIdTemp);
        bgTotal = findViewById(R.id.bgTotal);
        spDescricaoCredenciadora = findViewById(R.id.spDescricaoCredenciadora);
        spBandeiraCredenciadora = findViewById(R.id.spBandeiraCredenciadora);
        btnPagamentoCartaoNFCE = findViewById(R.id.btnPagamentoCartaoNFCE);
        btnAvancarNFCE = findViewById(R.id.btnAvancarNFCE);
        etCodAutorizacao = findViewById(R.id.etCodAutorizacao);
        TiNsuCeara = findViewById(R.id.TiNsuCeara);
        etNsuCeara = findViewById(R.id.etNsuCeara);
        txtValorFormaPagamento = findViewById(R.id.txtValorFormaPagamento);
        llCredenciadora = findViewById(R.id.llCredenciadora);
        //
        rvFinanceiro.setLayoutManager(new LinearLayoutManager(this));
        txtValorFormaPagamento.addTextChangedListener(new ClassAuxiliar.MoneyTextWatcher(txtValorFormaPagamento));
    }
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financeiro_nfce);

        // INICIA AS CLASSES AUXILIARES
        startConfig();

        // INICIA OS IDS DOS ELEMENTOS DA UI
        startIds();

        textIdTemp.setText(MessageFormat.format("Id Temp: \n{0}", idTemp));


        ////////////////////////////////////////////////////////////////////////////////////////////
        if (bd.getQuantProdutosPedidoNCMGas(idTemp) > 5) {
            quantidade = bd.getQuantProdutosPedidoNCMGas(idTemp);
            NotaFracionada = "1";
        } else {
            List<String> listPro = bd.getQuantProdutosPedidoNCM(idTemp);
            if (listPro != null) {
                try {
                    String[] iList = listPro.get(0).split(",");
                    quantidade = Integer.parseInt(iList[1]);
                    List<String> minMaxFrac = bd.getMinMaxFracionamentoProduto(iList[0]);


                    if (!minMaxFrac.get(0).equals("0") && !minMaxFrac.get(1).equals("0")) {
                        int min = Integer.parseInt(minMaxFrac.get(0));
                        int max = Integer.parseInt(minMaxFrac.get(1));
                        if (quantidade > max) {
                            NotaFracionada = "1";
                        }
                    }

                    //cAux.ShowMsgToast(context, "quantidade: " + quantidade + ", Max: " + minMaxFrac.get(1) + "Fracionar: " + NotaFracionada);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        //
        idCredenciadoras = bd.getIdCredenciadora();
        listaCredenciadoras = bd.getCredenciadora();
        listaCredenciadoras.add("");
        ArrayAdapter adapterCredenciadora = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaCredenciadoras);
        adapterCredenciadora.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDescricaoCredenciadora.setAdapter(adapterCredenciadora);
        //
        ArrayAdapter adapterBandeiraCredenciadora = new ArrayAdapter(this, android.R.layout.simple_spinner_item, fAux.listaBandeirasCredenciadoras);
        adapterBandeiraCredenciadora.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBandeiraCredenciadora.setAdapter(adapterBandeiraCredenciadora);

        //region **************** CÓDIGO ANTIGO PARA REFAZER ***************************************

        // VERIFICAR SE ESSE BOTÃO É PARA SAVAR O FINANCEIRO SEM PASSAR O CARTÃO
        //btnAddFormPag = findViewById(R.id.btnAddFormPag);
        //btnAddFormPag.setOnClickListener(v -> addFormaPagamento(etCodAutorizacao.getText().toString(), cAux.getIdBandeira(spBandeiraCredenciadora.getSelectedItem().toString()), etNsuCeara.getText().toString()));

        ArrayAdapter<String> adapterFormasPagamento;
        // Verifica a quantidade máxima permeitida
        if (NotaFracionada.equals("1")) {
            //cAux.ShowMsgToast(context, "Fracionar");
            adapterFormasPagamento = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fAux.listaFormasPagamentoDinheiro);
        } else {
            adapterFormasPagamento = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fAux.listaFormasPagamento);
        }
        adapterFormasPagamento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFormasPagamento = findViewById(R.id.spFormasPagamentoCliente);
        spFormasPagamento.setAdapter(adapterFormasPagamento);
        spFormasPagamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // SE EXISTIR STONE CODE OU OUTRO MEIO DE PAGAMENTO COM A MAQUININHA
                if (!unidades.getCodloja().equalsIgnoreCase("")) {
                    if (
                            (parent.getItemAtPosition(position).toString().equalsIgnoreCase("CARTÃO DE CRÉDITO") ||
                                    parent.getItemAtPosition(position).toString().equalsIgnoreCase("CARTÃO DE DÉBITO"))
                                    && !unidades.getCodloja().equalsIgnoreCase("")
                    ) {
                        llCredenciadora.setVisibility(View.GONE);

                        //
                        btnAddF.setVisibility(View.GONE);
                        btnPagCartao.setVisibility(View.VISIBLE);
                        //btnAvancarNFCE.setVisibility(View.GONE);
                        //btnPagamentoCartaoNFCE.setVisibility(View.VISIBLE);

                        // Retirar quando for usar o pinpad
                        //btnAvancarNFCE.setVisibility(View.VISIBLE);
                    }/* else if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("PAGAMENTO INSTANTÂNEO (PIX)")) {
                        //

                        Intent a = new Intent(getBaseContext(), Pix.class);
                        a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        a.putExtra("valor", txtValorFormaPagamento.getText().toString());
                        a.putExtra("apiKey", unidades.getApi_key_asaas());
                        a.putExtra("cliCob", unidades.getCliente_cob_asaas());
                        a.putExtra("pedido", "" + idTemp);
                        startActivity(a);
                        //finish();
                    }*/ else {
                        //
                        //llCredenciadora.setVisibility(View.VISIBLE);
                        //
                        btnAddF.setVisibility(View.VISIBLE);
                        btnPagCartao.setVisibility(View.GONE);
                        //btnPagamentoCartaoNFCE.setVisibility(View.GONE);
                        //btnAvancarNFCE.setVisibility(View.VISIBLE);
                    }
                }

                // SE NÃO EXISTIR OUTRO MEIO DE PAGAMENTO POR MAQUININHA
                else {
                    if (parent.getItemAtPosition(position).toString().equalsIgnoreCase("CARTÃO DE CRÉDITO") ||
                            parent.getItemAtPosition(position).toString().equalsIgnoreCase("CARTÃO DE DÉBITO")
                    ) {
                        llCredenciadora.setVisibility(View.VISIBLE);
                        if (unidades.getUf().equalsIgnoreCase(new Configuracoes().GetUFCeara())) {
                            TiNsuCeara.setVisibility(View.VISIBLE);
                        } else {
                            TiNsuCeara.setVisibility(View.GONE);
                        }

                        //
                        btnAddF.setVisibility(View.VISIBLE);
                        btnPagCartao.setVisibility(View.GONE);
                        //btnPagamentoCartaoNFCE.setVisibility(View.GONE);
                        //btnAvancarNFCE.setVisibility(View.VISIBLE);

                        // Retirar quando for usar o pinpad
                        //btnAvancarNFCE.setVisibility(View.VISIBLE);
                    } else {
                        //
                        llCredenciadora.setVisibility(View.GONE);
                        //
                        btnAddF.setVisibility(View.VISIBLE);
                        btnPagCartao.setVisibility(View.GONE);
                        //btnPagamentoCartaoNFCE.setVisibility(View.GONE);
                        //btnAvancarNFCE.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //
        btnAddF.setOnClickListener(v -> {
            //SE O USUÁRIO NÃO ADICIONAR NENHUM VALOR
            if (txtValorFormaPagamento.getText().toString().equals("") || txtValorFormaPagamento.getText().toString().equals("R$0,00")) {
                //
                cAux.ShowMsgToast(context, "Adicione uma valor para esta forma de pagamento.");
            } else {

                addFormaPagamento(etCodAutorizacao.getText().toString(), cAux.getIdBandeira(spBandeiraCredenciadora.getSelectedItem().toString()), etNsuCeara.getText().toString());
            }
        });

        btnPagCartao.setOnClickListener(v -> iniciarPagamento());

        txtTotalFinanceiro = findViewById(R.id.txtTotalFinanceiro);
        txtTotalItemFinanceiro = findViewById(R.id.txtTotalItemFinanceiro);
        //
        btnFinalizarFinanceiro.setOnClickListener(v -> {
            if (txtTotalItemFinanceiro.getText().equals("0,00")) {
                //
                cAux.ShowMsgToast(context, "Adicione pelo menos uma forma de pagamento ao financeiro.");
            } else if (!txtTotalItemFinanceiro.getText().equals(txtTotalFinanceiro.getText())) {
                //
                cAux.ShowMsgToast(context, "O valor do financeiro está diferente da venda.");
            } else {
                // VER ESSA PARTE DEPOIS DO FRACIONAMENTO
                fracionar();

                // VER ESSA PARTE DEPOIS DO FRACIONAMENTO
                //confirmar();
            }
        });


        /*listaFinanceiroCliente = bd.getFinanceiroPedidoTemp(idTemp);
        adapterFPG = new FormasPagamentoPedidosAdapter(this, listaFinanceiroCliente, elementos, idTemp, bd);
        // Registra o observador
        adapterFPG.registerObserver(this);
        rvFinanceiro.setAdapter(adapterFPG);*/
        //endregion

        // Add the callback to the back stack
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        //
        //pedidos = bd.ultimoPedido();
        //itensPedidos = bd.getItensPedido(pedidos.getId_pedido_temp()).get(0);
        String totFin = bd.getValorTotalPedido(String.valueOf(idTemp), "0.00");

        //
        txtTotalFinanceiro.setText(cAux.maskMoney(new BigDecimal(totFin)));
    }

    final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            sair();
        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        atualizarListaFormPag();
    }

    @Override
    public void onFinanceiroNFCeChanged() {
        runOnUiThread(this::atualizarListaFormPag);
    }

    private void addFormaPagamento(String authorizationCode, String cardBrand, String nsu) {

        //ESCONDER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        //
        if (!compararValorRestante()) return;

        String val = cAux.soNumeros(txtValorFormaPagamento.getText().toString());

        //
        String cliente_id_transfeera = "";
        String cliente_secret_transfeera = "";
        String pix_key_transfeera = "";
        try {
            if (unidades.getCliente_id_transfeera() != null) {
                cliente_id_transfeera = unidades.getCliente_id_transfeera();
                cliente_secret_transfeera = unidades.getCliente_secret_transfeera();
                pix_key_transfeera = unidades.getPix_key_transfeera();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (spFormasPagamento.getSelectedItem().toString().equals("FORMA PAGAMENTO")) {
            cAux.ShowMsgToast(context, "Selecione a forma de pagamento.");
        } else if (val.equalsIgnoreCase("") || val.equalsIgnoreCase("000")) {
            cAux.ShowMsgToast(context, "Adicione um valor");
        } else if (!cliente_id_transfeera.equalsIgnoreCase("") &&
                !cliente_secret_transfeera.equalsIgnoreCase("") &&
                !pix_key_transfeera.equalsIgnoreCase("") &&
                spFormasPagamento.getSelectedItem().toString().equalsIgnoreCase("PAGAMENTO INSTANTÂNEO (PIX)")) {

            //
            iniciarPagamentoPIX();
        } else {
            if (unidades.getCodloja().equalsIgnoreCase("")) {
                //
                if (spFormasPagamento.getSelectedItem().toString().equals("CARTÃO DE CRÉDITO") || spFormasPagamento.getSelectedItem().toString().equals("CARTÃO DE DÉBITO")) {

                    if (spDescricaoCredenciadora.getSelectedItem().toString().equals("CREDENCIADORA")) {
                        cAux.ShowMsgToast(context, "Selecione a credenciadora.");
                        return;
                    } else if (spBandeiraCredenciadora.getSelectedItem().toString().equals("BANDEIRA")) {
                        cAux.ShowMsgToast(context, "Selecione a bandeira.");
                        return;
                    } else if (etCodAutorizacao.getText().toString().equals("")) {
                        cAux.ShowMsgToast(context, "Informe o código de autorização.");
                        return;
                    }
                }
            }


            if (bd.getPedidosTemp(String.valueOf(idTemp)).size() > 1) {
                // PARA NOTAS FRACIONADAS
                for (Pedidos pedido : bd.getPedidosTemp(String.valueOf(idTemp))) {
                    ItensPedidos itemPedido = bd.getItensPedido(pedido.getId()).get(0);
                    //String valFinanceiro = cAux.multiplicar();
                    bd.addFormasPagamentoPedidosTemp(new FormaPagamentoPedido(
                            "",
                            pedido.getId(),//String.valueOf(idTemp), //ID PEDIDO
                            cAux.getIdFormaPagamento(spFormasPagamento.getSelectedItem().toString()),
                            "" + itemPedido.getTotal(),// + cAux.converterValores(cAux.soNumeros(txtValorFormaPagamento.getText().toString())),
                            authorizationCode,
                            cardBrand,
                            nsu,
                            "",
                            "0"
                    ));
                }
            } else {
                bd.addFormasPagamentoPedidosTemp(new FormaPagamentoPedido(
                        "",
                        String.valueOf(idTemp), //ID PEDIDO
                        cAux.getIdFormaPagamento(spFormasPagamento.getSelectedItem().toString()),
                        String.valueOf(cAux.converterValores(cAux.soNumeros(txtValorFormaPagamento.getText().toString()))),
                        authorizationCode,
                        cardBrand,
                        nsu,
                        "",
                        "0"
                ));
            }

            //
            listaFinanceiroCliente = bd.getFinanceiroPedidoTemp(idTemp);
            adapterFPG = new FormasPagamentoPedidosAdapter(this, listaFinanceiroCliente, elementos, idTemp, bd);
            // Registra o observador
            adapterFPG.registerObserver(this);
            rvFinanceiro.setAdapter(adapterFPG);

            //
            String tif = cAux.maskMoney(new BigDecimal(bd.getTotalFinanceiro(String.valueOf(idTemp), api_asaas)));
            txtTotalItemFinanceiro.setText(tif);

            //
            String valorFinanceiro = String.valueOf(cAux.converterValores(txtTotalFinanceiro.getText().toString()));
            String valorFinanceiroAdd = String.valueOf(cAux.converterValores(txtTotalItemFinanceiro.getText().toString()));

            //SUBTRAIR O VALOR PELA QUANTIDADE
            String[] subtracao = {valorFinanceiro, valorFinanceiroAdd};
            String total = String.valueOf(cAux.subitrair(subtracao));

            txtValorFormaPagamento.setText(total);

            //
            if (comparar()) {
                bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.erro));
                txtValorFormaPagamento.setText(R.string.zero_reais);
            } else {
                bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.transparente));
            }

            //
            spFormasPagamento.setSelection(0);
            etCodAutorizacao.setText("");
        }
    }

    void atualizarListaFormPag() {
        //
        bd.deleteFormPagPIX();

        //cAux.ShowMsgToast(context, "Atualizar lista do financeiro");

        //
        listaFinanceiroCliente = bd.getFinanceiroPedidoTemp(idTemp);
        adapterFPG = new FormasPagamentoPedidosAdapter(this, listaFinanceiroCliente, elementos, idTemp, bd);
        // Registra o observador
        adapterFPG.registerObserver(this);
        rvFinanceiro.setAdapter(adapterFPG);

        //
        String tif = cAux.maskMoney(new BigDecimal(bd.getTotalFinanceiro(String.valueOf(idTemp), api_asaas)));
        txtTotalItemFinanceiro.setText(tif);

        //
        //!txtTotalItemFinanceiro.getText().equals(txtTotalFinanceiro.getText()

        //
        String valorFinanceiro = String.valueOf(cAux.converterValores(txtTotalFinanceiro.getText().toString()));
        String valorFinanceiroAdd = String.valueOf(cAux.converterValores(txtTotalItemFinanceiro.getText().toString()));

        //SUBTRAIR O VALOR PELA QUANTIDADE
        String[] subtracao = {valorFinanceiro, valorFinanceiroAdd};
        String total = String.valueOf(cAux.subitrair(subtracao));

        txtValorFormaPagamento.setText(total);

        //
        if (comparar()) {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.erro));
            txtValorFormaPagamento.setText(R.string.zero_reais);
        } else {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.transparente));
        }

        //
            /*txtDocumentoFormaPagamento.setText("");
            tilDocumento.setVisibility(View.VISIBLE);*/
        spFormasPagamento.setSelection(0);
        etCodAutorizacao.setText("");
    }

    private void addFormaPagamentoPIX(String authorizationCode, String cardBrand, String nsu) {

        String val = cAux.soNumeros(txtValorFormaPagamento.getText().toString());

        bd.addFormasPagamentoPedidosTemp(new FormaPagamentoPedido(
                "",
                String.valueOf(idTemp), //ID PEDIDO
                cAux.getIdFormaPagamento(spFormasPagamento.getSelectedItem().toString()),
                "" + cAux.converterValores(cAux.soNumeros(txtValorFormaPagamento.getText().toString())),
                authorizationCode,
                cardBrand,
                nsu,
                "",
                "1"
        ));

        //
        listaFinanceiroCliente = bd.getFinanceiroPedidoTemp(idTemp);
        adapterFPG = new FormasPagamentoPedidosAdapter(this, listaFinanceiroCliente, elementos, idTemp, bd);
        // Registra o observador
        adapterFPG.registerObserver(this);
        rvFinanceiro.setAdapter(adapterFPG);

        //
        String tif = cAux.maskMoney(new BigDecimal(bd.getTotalFinanceiro(String.valueOf(idTemp), api_asaas)));
        txtTotalItemFinanceiro.setText(tif);

        //
        //!txtTotalItemFinanceiro.getText().equals(txtTotalFinanceiro.getText()

        //
        String valorFinanceiro = String.valueOf(cAux.converterValores(txtTotalFinanceiro.getText().toString()));
        String valorFinanceiroAdd = String.valueOf(cAux.converterValores(txtTotalItemFinanceiro.getText().toString()));

        //SUBTRAIR O VALOR PELA QUANTIDADE
        String[] subtracao = {valorFinanceiro, valorFinanceiroAdd};
        String total = String.valueOf(cAux.subitrair(subtracao));

        txtValorFormaPagamento.setText(total);

        //
        if (comparar()) {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.erro));
            txtValorFormaPagamento.setText("0,00");
        } else {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.transparente));
        }

        //
            /*txtDocumentoFormaPagamento.setText("");
            tilDocumento.setVisibility(View.VISIBLE);*/
        spFormasPagamento.setSelection(0);
        etCodAutorizacao.setText("");
    }

    //COMPARAR O VALOR DO FINANCEIRO COM O VALOR ADICIONADO
    private boolean comparar() {

        //
        BigDecimal valorFinanceiro = new BigDecimal(String.valueOf(cAux.converterValores(txtTotalFinanceiro.getText().toString())));
        BigDecimal valorFinanceiroAdd = new BigDecimal(String.valueOf(cAux.converterValores(txtTotalItemFinanceiro.getText().toString())));

        if (valorFinanceiroAdd.compareTo(valorFinanceiro) > 0) {
            //
            if (valorFinanceiro.toString().equals(valorFinanceiroAdd.toString())) {

                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapterFPG.unregisterObserver(this);
    }

    //COMPARAR O VALOR DO FINANCEIRO COM O VALOR ADICIONADO
    private boolean compararValorRestante() {
        // txtValorFormaPagamento txtTotalPago
        Integer valFormPag = Integer.parseInt(cAux.soNumeros(txtValorFormaPagamento.getText().toString()));
        Integer valTotPago = Integer.parseInt(cAux.soNumeros(txtTotalItemFinanceiro.getText().toString()));
        Integer valTotPagar = Integer.parseInt(cAux.soNumeros(txtTotalFinanceiro.getText().toString()));

        int tot = (valFormPag + valTotPago);

        Log.d("Comparar1", String.valueOf(valFormPag));
        Log.d("Comparar2", String.valueOf(valTotPago));
        Log.d("Comparar3", String.valueOf(valTotPagar));
        Log.d("Comparar4", String.valueOf(tot));

        if (tot > valTotPagar) {
            return false;
        }
        return true;
    }

    private void formsView() {

        /*// Verifica a quantidade máxima permeitida
        if (quantidade > 5) {
            cAux.ShowMsgToast(context,"Maior que 5");
            txtValorFormaPagamento.setEnabled(false);
            spFormasPagamento.setAdapter(null);
            spFormasPagamento.setAdapter(adapterFormasPagamentoDinheiro);
        }*/

        //
        fracionar();

        // Verifica a quantidade máxima permeitida
        if (NotaFracionada.equals("1")) {
            //txtValorFormaPagamento.setEnabled(false);
            spFormasPagamento.setAdapter(null);
            adapterFormasPagamentoDinheiro = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fAux.listaFormasPagamentoDinheiro);
            spFormasPagamento.setAdapter(adapterFormasPagamentoDinheiro);
        }

        String total = bd.getValorTotalPedido(String.valueOf(idTemp), String.valueOf(cAux.converterValores("0,00")));
        //
        txtTotalFinanceiro.setText(cAux.maskMoney(new BigDecimal(total)));
        txtValorFormaPagamento.setText(cAux.maskMoney(new BigDecimal(total)));
    }

    private void confirmar() {

        if (!bd.getPedidosTransmitirFecharDia().isEmpty()) {
            // APAGA TODOS OS PAGAMENTOS PIX COM STATUS 1
            bd.deleteFormPagPIX();

            //
            Intent i = new Intent(getBaseContext(), ConfirmarDadosPedido.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("cpfCnpj_cliente", prefs.getString("cpf_cnpj", ""));
            i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());
            i.putExtra("produto", "");
            i.putExtra("qnt", "");
            i.putExtra("vlt", cAux.converterValores(txtTotalItemFinanceiro.getText().toString()));
            i.putExtra("credenciadora", idCredenciadoras.get(spDescricaoCredenciadora.getSelectedItemPosition()));//spDescricaoCredenciadora.getSelectedItem().toString()
            i.putExtra("bandeira", cAux.getIdBandeira(spBandeiraCredenciadora.getSelectedItem().toString()));
            i.putExtra("cod_aut", etCodAutorizacao.getText().toString());
            i.putExtra("nsu", etNsuCeara.getText().toString());
            i.putExtra("desconto", "");

            startActivity(i);
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Encontramos um problema com esse pedido. Precisa refazer!", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarPagamentoPIX() {

        Intent a = new Intent(getBaseContext(), Pix.class);
        a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        a.putExtra("valor", txtValorFormaPagamento.getText().toString());
        a.putExtra("apiKey", unidades.getApi_key_asaas());
        a.putExtra("cliCob", unidades.getCliente_cob_asaas());
        a.putExtra("pedido", "" + idTemp);

        addFormaPagamentoPIX("", "", "");
        a.putExtra("idForPagPix", bd.ultimoIdFormPagPIX(String.valueOf(idTemp)));

        startActivityForResult(a, PAGAMENTO_PIX_REQUEST);
    }

    private void iniciarPagamento() {
        Intent i;
        Configuracoes configuracoes = new Configuracoes();
        //
        if (configuracoes.GetDevice()) {
            i = new Intent(getBaseContext(), GerenciarPagamentoCartaoPOS.class);
        } else {
            i = new Intent(getBaseContext(), GerenciarPagamentoCartao.class);
        }

        i.putExtra("cpfCnpj_cliente", "");
        i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());
        i.putExtra("produto", "");
        i.putExtra("qnt", "1");
        i.putExtra("vlt", txtValorFormaPagamento.getText().toString());

        startActivityForResult(i, PAGAMENTO_REQUEST);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            sair();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sair() {
        //
        if (listaFinanceiroCliente == null) {
            finish();
        } else {
            //
            if (listaFinanceiroCliente.size() > 0) {
                //confirmar();
                //VerificarCamposIniciarPedido(false);
                if (txtTotalItemFinanceiro.getText().equals("0,00")) {
                    //
                    cAux.ShowMsgToast(context, "Adicione pelo menos uma forma de pagamento ao financeiro.");
                } else if (!txtTotalItemFinanceiro.getText().equals(txtTotalFinanceiro.getText())) {
                    //
                    cAux.ShowMsgToast(context, "O valor do financeiro está diferente da venda.");
                } else {
                    confirmar();
                }
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAGAMENTO_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {

                etCodAutorizacao.setText(data.getStringExtra("authorizationCode"));
                etCodAutorizacao.setEnabled(false);
                etNsuCeara.setText(data.getStringExtra("nsu"));
                etNsuCeara.setEnabled(false);

                addFormaPagamento(data.getStringExtra("authorizationCode"), cAux.getIdBandeira(data.getStringExtra("cardBrand")), data.getStringExtra("nsu"));
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                cAux.ShowMsgToast(context, "Operação Cancelada!");
            }
        }
        if (requestCode == PAGAMENTO_PIX_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                // AddFormaPagamentoPIX("", "", "");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                cAux.ShowMsgToast(context, "Operação Cancelada!");
            }
        }
    }

    public void fracionar() {

        if (bd.getQuantProdutosPedidoNCMGas(idTemp) > 5) {
            if (quantidade > 5) {
                Random r = new Random();
                random = r.nextInt(6 - 1) + 1;
            } else {
                random = quantidade;
            }
        } else {
            List<String> listPro = bd.getQuantProdutosPedidoNCM(idTemp);
            if (!listPro.isEmpty()) {
                String[] iList = listPro.get(0).split(",");
                List<String> minMaxFrac = bd.getMinMaxFracionamentoProduto(iList[0]);
                if (!minMaxFrac.get(0).equals("0") && !minMaxFrac.get(1).equals("0")) {
                    int min = Integer.parseInt(minMaxFrac.get(0));
                    int max = Integer.parseInt(minMaxFrac.get(1));
                    if (quantidade > max) {
                        Random r = new Random();
                        random = r.nextInt(max - min) + 1;
                    } else {
                        random = quantidade;
                    }
                    //cAux.ShowMsgToast(context, "Fracionar produto diferente de gás");
                } else {
                    random = quantidade;
                }
            } else {
                random = quantidade;
            }
        }

        String[] sub = {String.valueOf(quantidade), String.valueOf(random)};
        quantidade = cAux.subitrair(sub).intValue();

        //
        String data = cAux.exibirDataAtual();
        String hora = cAux.horaAtual();

        if (!dd) {
            id = idTemp;
            dd = true;
        } else {
            prefs.edit().putInt("id_pedido", (Integer.parseInt(bd.getUltimoIdPedidos()) + 1)).apply();
            id = prefs.getInt("id_pedido", 1);
        }

        //INSERI O PEDIDO NO BANCO DE DADOS
        addPedido(
                cAux.inserirData(data),
                hora,
                prefs.getString("cpf_cnpj", ""),
                String.valueOf(random)//quantidade
        );

        if (quantidade > 0) {
            fracionar();
        } else {
            // VER ESSA PARTE DEPOIS DO FRACIONAMENTO
            bd.upadtePedidoTemp(String.valueOf(idTemp));
            confirmar();
        }
    }

    private void addPedido(
            String dataEmissao,
            String horaEmissao,
            String cpf,
            String quantidade
    ) {
        bd.addPedidos(new Pedidos(
                String.valueOf(id),//ID PEDIDO
                "OFF",//SITUAÇÃO
                "",//PROTOCOLO
                dataEmissao,//DATA EMISSÃO
                horaEmissao,//HORA EMISSÃO
                bd.getValorTotalPedido(String.valueOf(id), "0,00"),//VALOR TOTAL
                "",//DATA PROTOCOLO - "28042017"
                "",//HORA PROTOCOLO - "151540"
                cpf,//CPF/CNPJ CLIENTE
                "",//FORMA PAGAMENTO
                "",
                String.valueOf(idTemp), //
                NotaFracionada,
                unidades.getCredenciadora()
        ));

        for (ProdutosPedidoDomain produto : bd.getProdutosPedido(idTemp)) {
            //
            String _quant;
            if (NotaFracionada.equals("1")) {
                _quant = quantidade;
            } else {
                _quant = produto.quantidade;
            }
            bd.addItensPedidos(new ItensPedidos(
                    String.valueOf(id),//ID PEDIDO
                    produto.id_produto,
                    _quant,// produto.quantidade,
                    produto.valor,
                    String.valueOf(cAux.converterValores(cAux.soNumeros(produto.desconto))),
                    ""
            ));
            String[] mValor = {produto.valor, _quant};
            String vTotal = String.valueOf(cAux.multiplicar(mValor));
            bd.updateValorPedido(String.valueOf(id), vTotal);
        }
        if (NotaFracionada.equals("1")) {

            //region REGRA PARA FRACIONAR O FINANCEIRO EM DINHEIRO
         /*
         SE O VALOR DA FORMA DE PAGAMENTO DINEIRO FOR MAIOR OU IGUAL AO TOTAL DA NOTA ADICIONA DINHEIRO
         E SUBTRAI O VALOR DA FORMA DE PAGAMENTO DINHEIRO
         */
            //endregion
            //region REGRA PARA FRACIONAR O FINANCEIRO EM PIX
         /*
         SE O VALOR DA FORMA DE PAGAMENTO PIX FOR MAIOR OU IGUAL AO TOTAL DA NOTA ADICIONA PIX
         E SUBTRAI O VALOR DA FORMA DE PAGAMENTO PIX
         */
            //endregion
            //region REGRA PARA ADICIONAR O FINANCEIRO DA ULTIMA NOTA
         /*
         PEGAR O VALOR RESTANTE DA FORMAS DE PAGAMENTO DINHEIRO E PIX E SOMAR CRIANDO O VALOR TOTAL
         DA ULTIMA NOTA. ZERAR AS DUAS FORMAS DE PAGAMENTO NAS FORMAS DE PAGAMENTO DO PEDIDO.
         */
            //endregion

            // TOTAL FINANCEIRO DINHEIRO
            String _totalDinheiro = bd.getTotalFinanceiroDinheiro(String.valueOf(idTemp), api_asaas);
            // TOTAL FINANCEIRO PIX
            String _totalPix = bd.getTotalFinanceiroPix(String.valueOf(idTemp), api_asaas);
            //  TOTAL DO ITEM
            String _total_item = bd.getTotalItem(String.valueOf(id));
            // RECEBE O ID DA COBRANCA PIX
            String id_cobranca_pix = bd.getIdCobrancaPix(String.valueOf(idTemp));

            cAux.ShowMsgLog("FinNFCe", "---------");
            cAux.ShowMsgLog("FinNFCe", "DINHEIRO: " + _totalDinheiro);
            cAux.ShowMsgLog("FinNFCe", "PIX: " + _totalPix);
            cAux.ShowMsgLog("FinNFCe", "ITEM: " + _total_item);
            cAux.ShowMsgLog("FinNFCe", "ID COB. PIX: " + id_cobranca_pix);
            cAux.ShowMsgLog("FinNFCe", "---------");

            FormaPagamentoPedido fpp = new FormaPagamentoPedido(null, null, null, null, null, null, null, null, null);
            fpp.id_pedido = String.valueOf(id);
            fpp.status_pix = "0";

            int compararDinheiro = new BigDecimal(_totalDinheiro).compareTo(new BigDecimal(_total_item));
            int compararPix = new BigDecimal(_totalPix).compareTo(new BigDecimal(_total_item));

            String _valFinanceiro;
            // VERIFICA SE O VALOR É MAIOR OU IGUAL A FORMA DE PAGAMENTO DINHEIRO
            if (compararDinheiro >= 0) {
                _valFinanceiro = _total_item;
                String[] mValor = {_totalDinheiro, _total_item};
                String vD = String.valueOf(cAux.subitrair(mValor));
                cAux.ShowMsgLog("FinNFCe", "RESTANTE DINHEIRO: " + vD);
                fpp.id_forma_pagamento = "1";

                bd.updateValorFinanceiroPedidoDinheiro(String.valueOf(idTemp), vD);

                //
                fpp.valor = String.valueOf(_valFinanceiro);
                bd.addFinanceiroNFCe(fpp);
            }
            // VERIFICA SE O VALOR É MAIOR OU IGUAL A FORMA DE PAGAMENTO PIX
            else if (compararPix >= 0) {
                _valFinanceiro = _total_item;
                String[] mValor = {_totalPix, _total_item};
                String vP = String.valueOf(cAux.subitrair(mValor));
                cAux.ShowMsgLog("FinNFCe", "RESTANTE PIX: " + vP);
                fpp.id_forma_pagamento = "17";
                fpp.id_cobranca_pix = id_cobranca_pix;
                bd.updateValorFinanceiroPedidoPix(String.valueOf(idTemp), vP);

                //
                fpp.valor = String.valueOf(_valFinanceiro);
                bd.addFinanceiroNFCe(fpp);
            }
            // SOMA O RESTANTE DAS DUAS FORMAS DE PAGAMENTO
            else {
                // SE TIVER SOBRADO VALOR EM DINHEIRO ADICIONA O RESTANTE
                if (!_totalDinheiro.equals("0.00")) {
                    fpp.id_forma_pagamento = "1";
                    fpp.valor = _totalDinheiro;
                    bd.addFinanceiroNFCe(fpp);
                }
                // SE TIVER SOBRADO VALOR EM PIX ADICIONA O RESTANTE
                if (!_totalPix.equals("0.00")) {
                    fpp.id_forma_pagamento = "17";
                    fpp.id_cobranca_pix = id_cobranca_pix;
                    fpp.valor = _totalPix;
                    bd.addFinanceiroNFCe(fpp);
                }

                // ZERA O FINANCEIRO TEMPORARIO
                bd.zerarFinanceiroPedidoTemp(String.valueOf(idTemp));
            }

            // ADICIONAR O FINANCEIRO DA NFCe

        /*
            values.put("id_pedido", formaPagamentoPedido.id_pedido);
            values.put("id_forma_pagamento", formaPagamentoPedido.id_forma_pagamento);
            values.put("valor", formaPagamentoPedido.valor);
            values.put("codigo_autorizacao", formaPagamentoPedido.codigo_autorizacao);
            values.put("bandeira", formaPagamentoPedido.cardBrand);
            values.put("nsu", formaPagamentoPedido.codigo_autorizacao);
            values.put("id_cobranca_pix", formaPagamentoPedido.id_cobranca_pix);
            values.put("status_pix", formaPagamentoPedido.status_pix);
        */
        } else {
            for (FormaPagamentoPedido fpp : bd.getFinanceiroCliente(idTemp)) {
                bd.addFinanceiroNFCe(fpp);
            }

            // ZERA O FINANCEIRO TEMPORARIO
            bd.zerarFinanceiroPedidoTemp(String.valueOf(idTemp));
        }

    }
}