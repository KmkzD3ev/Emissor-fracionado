package br.com.zenitech.emissorweb;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.emissorweb.adapters.FormasPagamentoEditarPedidosAdapter;
import br.com.zenitech.emissorweb.domains.FormaPagamentoPedido;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.Unidades;

public class EditarPedido extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    static final int PAGAMENTO_REQUEST = 1;
    static final int PAGAMENTO_PIX_REQUEST = 2;
    private DatabaseHelper bd;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;
    boolean api_asaas = false;

    //"FORMA PAGAMENTO",
    String[] listaFormasPagamento = {
            "DINHEIRO",
            "CARTÃO DE CRÉDITO",
            "CARTÃO DE DÉBITO",
            "PAGAMENTO INSTANTÂNEO (PIX)",
            "BOLETO"
    };
    String[] listaFormasPagamentoDinheiro = {
            "DINHEIRO",
            "PAGAMENTO INSTANTÂNEO (PIX)"
    };
    ArrayAdapter<String> adapterFormasPagamentoDinheiro;
    ArrayList<String> listaCredenciadoras;
    ArrayList<String> idCredenciadoras;
    //"FORMA PAGAMENTO",
    String[] listaBandeirasCredenciadoras = {
            "BANDEIRA",
            "Visa",
            "Mastercard",
            "American Express",
            "Sorocred",
            "Diners Club",
            "Elo",
            "Hipercard",
            "Aura",
            "Cabal",
            "Outros"
    };

    private ArrayList<FormaPagamentoPedido> listaFinanceiroCliente;
    private FormasPagamentoEditarPedidosAdapter adapter;
    private RecyclerView rvFinanceiro;
    Spinner spFormasPagamento, spDescricaoCredenciadora, spBandeiraCredenciadora;
    EditText etCodAutorizacao, etNsuCeara;
    private TextInputLayout TiNsuCeara;
    LinearLayout llCredenciadora, infoDadosPedido, formFinanceiroPedido;
    LinearLayoutCompat llFormAddFormasPag, formDadosPedido;

    Toolbar toolbar;
    AlertDialog alerta;
    ClassAuxiliar aux;
    ArrayList<Unidades> elementos;
    Unidades unidades;

    TextView txtCpfCnpjCli, txtProduto, txtQuant, txtValTotalPagar, txtTotalPago;
    public static EditText txtValorFormaPagamento;
    public static TextView txtTotalFinanceiro;
    public static TextView txtTotalItemFinanceiro;
    public static LinearLayout bgTotal;

    // BTNs **
    Button btnAddF, btnPagamento, btnPagCartao;
    Button btnPagamentoCartaoNFCE, btnAvancarNFCE, btnEscolheFP;
    int id = 0;
    int idTemp = 0;

    //
    Pedidos pedidos;
    ItensPedidos itensPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_pedido);
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_form_pedido);
        setSupportActionBar(toolbar);
        //Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //
        //
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();

        //
        bd = new DatabaseHelper(this);
        aux = new ClassAuxiliar();

        //
        rvFinanceiro = findViewById(R.id.rvFinanceiro);
        rvFinanceiro.setLayoutManager(new LinearLayoutManager(this));

        //
        bgTotal = findViewById(R.id.bgTotal);

        //-------CRIA UM ID PARA O PEDIDO------//
        //ed.putInt("id_pedido_temp", (prefs.getInt("id_pedido_temp", 0) + 1)).apply();

        //ShowMsgToast(String.valueOf(prefs.getInt("id_pedido", 0)));

        /*if (prefs.getInt("id_pedido", 0) == 0) {
            ed.putInt("id_pedido", (Integer.parseInt(bd.getUltimaNotaPOS()) + 1)).apply();
        } else {
            //ed.putInt("id_pedido", (Integer.parseInt(bd.getUltimoIdPedido()) + 1)).apply();
        }*/

        //idTemp = prefs.getInt("id_pedido_temp", 1);
        //ed.putInt("id_pedido", (prefs.getInt("id_pedido", 0) + 1)).apply();
        idTemp = Integer.parseInt(bd.IdEditarPedido());//bd.getUltimoIdPedido()  prefs.getInt("id_pedido", 1);
        id = idTemp;

        elementos = bd.getUnidades();
        unidades = elementos.get(0);
        if (!unidades.getApi_key_asaas().equalsIgnoreCase("")) {
            api_asaas = true;
        }

        //
        btnPagamentoCartaoNFCE = findViewById(R.id.btnPagamentoCartaoNFCE);
        btnPagamentoCartaoNFCE.setOnClickListener(v -> VerificarCamposIniciarPedido(true));

        //
        btnAvancarNFCE = findViewById(R.id.btnAvancarNFCE);
        btnAvancarNFCE.setOnClickListener(v -> VerificarCamposIniciarPedido(false));

        //
        llCredenciadora = findViewById(R.id.llCredenciadora);
        formDadosPedido = findViewById(R.id.formDadosPedido);
        formFinanceiroPedido = findViewById(R.id.formFinanceiroPedido);

        //
        etCodAutorizacao = findViewById(R.id.etCodAutorizacao);

        //
        TiNsuCeara = findViewById(R.id.TiNsuCeara);
        etNsuCeara = findViewById(R.id.etNsuCeara);

        //
        listaFinanceiroCliente = bd.getFinanceiroCliente(idTemp);
        adapter = new FormasPagamentoEditarPedidosAdapter(this, listaFinanceiroCliente, elementos, idTemp);
        rvFinanceiro.setAdapter(adapter);

        /*/
        cpf_cnpj_cliente = findViewById(R.id.cpf_cnpj_cliente);
        cpf_cnpj_cliente.addTextChangedListener(MaskUtil.insert(cpf_cnpj_cliente, MaskUtil.MaskType.AUTO));

        //
        etQuantidade = findViewById(R.id.etQuantidade);
        etQuantidade.setText("");
        etQuantidade.addTextChangedListener(new EditarPedido.VerifQuant(etQuantidade));

        //
        etPreco = findViewById(R.id.etPreco);
        etPreco.addTextChangedListener(new EditarPedido.MoneyTextWatcher(etPreco));

        //
        btnAddFormPag = findViewById(R.id.btnAddFormPag);
        btnAddFormPag.setOnClickListener(v -> AddFormaPagamento(etCodAutorizacao.getText().toString(), aux.getIdBandeira(spBandeiraCredenciadora.getSelectedItem().toString()), etNsuCeara.getText().toString()));
*/
        //
        txtCpfCnpjCli = findViewById(R.id.txtCpfCnpjCli);
        txtProduto = findViewById(R.id.txtProduto);
        txtQuant = findViewById(R.id.txtQuant);
        txtValTotalPagar = findViewById(R.id.txtValTotalPagar);
        txtTotalPago = findViewById(R.id.txtTotalPago);

        /*etPreco.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                VerificarCamposIniciarPedido();
                handled = true;
            }
            return handled;
        });*/

        /*//LISTA DE PRODUTOS
        ArrayList<String> listaProdutos = bd.getProdutos();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaProdutos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProduto = findViewById(R.id.spProdutos);
        spProduto.setAdapter(adapter);
        spProduto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).toString().equals("PRODUTO")) {
                    double precoMinProd = bd.getPrecoMinimoProduto(parent.getItemAtPosition(position).toString());
                    precoMinimo = String.valueOf(precoMinProd);

                    //
                    double precoMaxProd = bd.getPrecoMaximoProduto(parent.getItemAtPosition(position).toString());
                    precoMaximo = String.valueOf(precoMaxProd);

                    if (!precoMinimo.equals("0.0")) {
                        Objects.requireNonNull(getSupportActionBar()).setSubtitle("Preço Mín. " + aux.maskMoney(new BigDecimal(precoMinimo)) + " | Max. " + aux.maskMoney(new BigDecimal(precoMaximo)));
                    } else {
                        Objects.requireNonNull(getSupportActionBar()).setSubtitle("");
                    }

                    quant = bd.getQuantProdutoRemessa(parent.getItemAtPosition(position).toString());
                    Toast.makeText(getBaseContext(), "" + quant, Toast.LENGTH_LONG).show();
                    //Toast.makeText(getBaseContext(), "" + bd.getQuantProdutoRemessa(parent.getItemAtPosition(position).toString()), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getBaseContext(), precoMinimo + "|" + precoMaximo, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
*/
        ArrayAdapter<String> adapterFormasPagamento = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaFormasPagamento);
        adapterFormasPagamentoDinheiro = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaFormasPagamentoDinheiro);
        adapterFormasPagamentoDinheiro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
                    } else {
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


        idCredenciadoras = bd.getIdCredenciadora();
        listaCredenciadoras = bd.getCredenciadora();
        listaCredenciadoras.add("");
        ArrayAdapter adapterCredenciadora = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaCredenciadoras);
        adapterCredenciadora.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDescricaoCredenciadora = findViewById(R.id.spDescricaoCredenciadora);
        spDescricaoCredenciadora.setAdapter(adapterCredenciadora);

        //
        ArrayAdapter adapterBandeiraCredenciadora = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaBandeirasCredenciadoras);
        adapterBandeiraCredenciadora.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBandeiraCredenciadora = findViewById(R.id.spBandeiraCredenciadora);
        spBandeiraCredenciadora.setAdapter(adapterBandeiraCredenciadora);

        //findViewById(R.id.btn_finalizar).setOnClickListener(v -> VerificarCamposIniciarPedido(false));

        /*FloatingActionButton fab = findViewById(R.id.fabAvancar);
        fab.setOnClickListener(view -> VerificarCamposIniciarPedido());*/

        //INTRODUÇÃO
        if (prefs.getInt("introFormPedidos", 0) == 0) {
            //
            ed.putInt("introFormPedidos", 1).apply();

            //INCIAR INTRODUÇÃO
            //introducao();
        }

        btnEscolheFP = findViewById(R.id.btnEscolheFP);
        infoDadosPedido = findViewById(R.id.infoDadosPedido);
        llFormAddFormasPag = findViewById(R.id.llFormAddFormasPag);

        //btnEscolheFP.setOnClickListener(v -> ValidarCampFormPedido());

        txtValorFormaPagamento = findViewById(R.id.txtValorFormaPagamento);
        txtValorFormaPagamento.addTextChangedListener(new MoneyTextWatcher(txtValorFormaPagamento));
        //
        btnAddF = findViewById(R.id.btnAddF);
        btnAddF.setOnClickListener(v -> {
            //SE O USUÁRIO NÃO ADICIONAR NENHUM VALOR
            /*if (txtValorFormaPagamento.getText().toString().equals("") || txtValorFormaPagamento.getText().toString().equals("R$0,00")) {
                //
                Toast.makeText(this, "Adicione uma valor para esta forma de pagamento.", Toast.LENGTH_LONG).show();
            }*/
            String valEtPreco = txtValorFormaPagamento.getText().toString();
            if (valEtPreco.equals("")
                    || valEtPreco.equals("R$ 0,00")
                    || valEtPreco.equals("0.0")
                    || valEtPreco.equals("0.00")) {
                ShowMsgToast("Adicione uma valor para esta forma de pagamento.");
            } else {

                AddFormaPagamento(etCodAutorizacao.getText().toString(), aux.getIdBandeira(spBandeiraCredenciadora.getSelectedItem().toString()), etNsuCeara.getText().toString());
            }
        });
        btnPagCartao = findViewById(R.id.btnPagCartao);
        btnPagCartao.setOnClickListener(v -> VerificarCamposIniciarPedido(true));

        txtTotalFinanceiro = findViewById(R.id.txtTotalFinanceiro);
        txtTotalItemFinanceiro = findViewById(R.id.txtTotalItemFinanceiro);
        //
        btnPagamento = findViewById(R.id.btnPagamento);
        btnPagamento.setOnClickListener(v -> {
            if (txtTotalItemFinanceiro.getText().equals("0,00")) {
                //
                Toast.makeText(this, "Adicione pelo menos uma forma de pagamento ao financeiro.", Toast.LENGTH_LONG).show();
            } else if (!txtTotalItemFinanceiro.getText().equals(txtTotalFinanceiro.getText())) {
                //
                Toast.makeText(this, "O valor do financeiro está diferente da venda.", Toast.LENGTH_LONG).show();
            } else {
                confirmar();
            }
        });


        //
        pedidos = bd.ultimoPedido();
        itensPedidos = bd.getItensPedido(pedidos.getId_pedido_temp()).get(0);
        String totFin = bd.getValorTotalPedido(pedidos.getId_pedido_temp(), "0.00");

        //
        txtTotalFinanceiro.setText(aux.maskMoney(new BigDecimal(totFin)));

        //
        atualizarDadosFinanceiro();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        atualizarListaFormPag();
    }

    private void AddFormaPagamento(String authorizationCode, String cardBrand, String nsu) {

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

        String val = aux.soNumeros(txtValorFormaPagamento.getText().toString());

        if (spFormasPagamento.getSelectedItem().toString().equals("FORMA PAGAMENTO")) {
            ShowMsgToast("Selecione a forma de pagamento.");
        } else if (val.equalsIgnoreCase("") || val.equalsIgnoreCase("000")) {
            ShowMsgToast("Adicione um valor");
        } else if (!unidades.getApi_key_asaas().equalsIgnoreCase("") && spFormasPagamento.getSelectedItem().toString().equalsIgnoreCase("PAGAMENTO INSTANTÂNEO (PIX)")) {
            // PIX
            iniciarPagamentoPIX();
        } else {
            if (unidades.getCodloja().equalsIgnoreCase("")) {
                //
                if (spFormasPagamento.getSelectedItem().toString().equals("CARTÃO DE CRÉDITO") || spFormasPagamento.getSelectedItem().toString().equals("CARTÃO DE DÉBITO")) {

                    if (spDescricaoCredenciadora.getSelectedItem().toString().equals("CREDENCIADORA")) {
                        ShowMsgToast("Selecione a credenciadora.");
                        return;
                    } else if (spBandeiraCredenciadora.getSelectedItem().toString().equals("BANDEIRA")) {
                        ShowMsgToast("Selecione a bandeira.");
                        return;
                    } else if (etCodAutorizacao.getText().toString().equals("")) {
                        ShowMsgToast("Informe o código de autorização.");
                        return;
                    }
                }
            }

            bd.addFormasPagamentoPedidosTemp(new FormaPagamentoPedido(
                    "",
                    String.valueOf(idTemp), //ID PEDIDO
                    aux.getIdFormaPagamento(spFormasPagamento.getSelectedItem().toString()),
                    "" + aux.converterValores(aux.soNumeros(txtValorFormaPagamento.getText().toString())),
                    authorizationCode,
                    cardBrand,
                    nsu,
                    "",
                    "0"
            ));

            //
            atualizarDadosFinanceiro();
        }
    }

    private void atualizarDadosFinanceiro() {
        //
        listaFinanceiroCliente = bd.getFinanceiroCliente(idTemp);
        adapter = new FormasPagamentoEditarPedidosAdapter(this, listaFinanceiroCliente, elementos, idTemp);
        rvFinanceiro.setAdapter(adapter);

        //
        String tif = aux.maskMoney(new BigDecimal(bd.getValorTotalFinanceiro(String.valueOf(idTemp), api_asaas)));
        txtTotalItemFinanceiro.setText(tif);

        //
        String valorFinanceiro = String.valueOf(aux.converterValores(txtTotalFinanceiro.getText().toString()));
        String valorFinanceiroAdd = String.valueOf(aux.converterValores(txtTotalItemFinanceiro.getText().toString()));

        //SUBTRAIR O VALOR PELA QUANTIDADE
        String[] subtracao = {valorFinanceiro, valorFinanceiroAdd};
        String total = String.valueOf(aux.subitrair(subtracao));

        txtValorFormaPagamento.setText(total);

        //
        if (comparar()) {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.erro));
            txtValorFormaPagamento.setText("0,00");
        } else {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.transparente));
        }

        //
        spFormasPagamento.setSelection(0);
        etCodAutorizacao.setText("");

        //ESCONDER O TECLADO
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void atualizarListaFormPag() {
        //
        bd.deleteFormPagPIX();

        //
        listaFinanceiroCliente = bd.getFinanceiroCliente(idTemp);
        adapter = new FormasPagamentoEditarPedidosAdapter(this, listaFinanceiroCliente, elementos, idTemp);
        rvFinanceiro.setAdapter(adapter);

        //
        String tif = aux.maskMoney(new BigDecimal(bd.getValorTotalFinanceiro(String.valueOf(idTemp), api_asaas)));
        txtTotalItemFinanceiro.setText(tif);

        //
        String valorFinanceiro = String.valueOf(aux.converterValores(txtTotalFinanceiro.getText().toString()));
        String valorFinanceiroAdd = String.valueOf(aux.converterValores(txtTotalItemFinanceiro.getText().toString()));

        //SUBTRAIR O VALOR PELA QUANTIDADE
        String[] subtracao = {valorFinanceiro, valorFinanceiroAdd};
        String total = String.valueOf(aux.subitrair(subtracao));

        txtValorFormaPagamento.setText(total);

        //
        if (comparar()) {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.erro));
            txtValorFormaPagamento.setText("0,00");
        } else {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.transparente));
        }

        //
        spFormasPagamento.setSelection(0);
        etCodAutorizacao.setText("");
    }

    private void AddFormaPagamentoPIX(String authorizationCode, String cardBrand, String nsu) {

        bd.addFormasPagamentoPedidosTemp(new FormaPagamentoPedido(
                "",
                String.valueOf(idTemp), //ID PEDIDO
                aux.getIdFormaPagamento(spFormasPagamento.getSelectedItem().toString()),
                "" + aux.converterValores(aux.soNumeros(txtValorFormaPagamento.getText().toString())),
                authorizationCode,
                cardBrand,
                nsu,
                "",
                "1"
        ));

        //
        listaFinanceiroCliente = bd.getFinanceiroCliente(idTemp);
        adapter = new FormasPagamentoEditarPedidosAdapter(this, listaFinanceiroCliente, elementos, idTemp);
        rvFinanceiro.setAdapter(adapter);

        //
        String tif = aux.maskMoney(new BigDecimal(bd.getValorTotalFinanceiro(String.valueOf(idTemp), api_asaas)));
        txtTotalItemFinanceiro.setText(tif);

        //
        String valorFinanceiro = String.valueOf(aux.converterValores(txtTotalFinanceiro.getText().toString()));
        String valorFinanceiroAdd = String.valueOf(aux.converterValores(txtTotalItemFinanceiro.getText().toString()));

        //SUBTRAIR O VALOR PELA QUANTIDADE
        String[] subtracao = {valorFinanceiro, valorFinanceiroAdd};
        String total = String.valueOf(aux.subitrair(subtracao));

        txtValorFormaPagamento.setText(total);

        //
        if (comparar()) {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.erro));
            txtValorFormaPagamento.setText("0,00");
        } else {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.transparente));
        }

        //
        spFormasPagamento.setSelection(0);
        etCodAutorizacao.setText("");
    }

    //COMPARAR O VALOR DO FINANCEIRO COM O VALOR ADICIONADO
    private boolean comparar() {

        //
        BigDecimal valorFinanceiro = new BigDecimal(String.valueOf(aux.converterValores(txtTotalFinanceiro.getText().toString())));
        BigDecimal valorFinanceiroAdd = new BigDecimal(String.valueOf(aux.converterValores(txtTotalItemFinanceiro.getText().toString())));

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

    //COMPARAR O VALOR DO FINANCEIRO COM O VALOR ADICIONADO
    private boolean compararValorRestante() {

        //
        Integer valFormPag = Integer.parseInt(aux.soNumeros(txtValorFormaPagamento.getText().toString()));
        Integer valTotPago = Integer.parseInt(aux.soNumeros(txtTotalItemFinanceiro.getText().toString()));
        Integer valTotPagar = Integer.parseInt(aux.soNumeros(txtTotalFinanceiro.getText().toString()));

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

    private void VerificarCamposIniciarPedido(boolean pagamento) {

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

        String valEtPreco = "";
        if (!txtValorFormaPagamento.getText().toString().equals("")) {
            valEtPreco = String.valueOf(aux.converterValores(txtValorFormaPagamento.getText().toString()));
        }

        //
        if (spFormasPagamento.getSelectedItem().toString().equals("FORMA PAGAMENTO")) {
            ShowMsgToast("Selecione a forma de pagamento.");
        } else if (txtValorFormaPagamento.getText().toString().equals("")
                || valEtPreco.equals("R$ 0,00")
                || valEtPreco.equals("0.0")
                || valEtPreco.equals("0.00")) {
            ShowMsgToast("Informe o valor unitário.");
        } else {

            if (pagamento) {
                iniciarPagamento();
            } else {
                confirmar();
            }
        }
    }

    private void ShowMsgToast(String msg) {
        Toast toast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void confirmar() {
        // APAGA TODOS OS PAGAMENTOS PIX COM STATUS 1
        bd.deleteFormPagPIX();

        //
        Intent i = new Intent(getBaseContext(), ConfirmarDadosPedido.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("cpfCnpj_cliente", pedidos.getCpf_cliente());// cpf_cnpj_cliente.getText().toString());
        i.putExtra("formaPagamento", pedidos.getForma_pagamento());// spFormasPagamento.getSelectedItem().toString());
        i.putExtra("produto", bd.getProduto(bd.getItensPedido(pedidos.getId()).get(0).getProduto()));//spProduto.getSelectedItem().toString());
        i.putExtra("qnt", bd.getItensPedido(pedidos.getId()).get(0).getQuantidade());
        i.putExtra("vlt", aux.maskMoney(new BigDecimal(bd.getItensPedido(pedidos.getId()).get(0).getValor())));
        // aux.converterValores()
        // kleilson analisar código
        i.putExtra("credenciadora", idCredenciadoras.get(spDescricaoCredenciadora.getSelectedItemPosition()));//spDescricaoCredenciadora.getSelectedItem().toString()
        i.putExtra("bandeira", aux.getIdBandeira(spBandeiraCredenciadora.getSelectedItem().toString()));
        i.putExtra("cod_aut", etCodAutorizacao.getText().toString());
        i.putExtra("nsu", etNsuCeara.getText().toString());
        i.putExtra("desconto", aux.maskMoney(new BigDecimal(bd.getItensPedido(pedidos.getId()).get(0).getDesconto())));

        startActivity(i);
        finish();
    }

    private void iniciarPagamentoPIX() {

        Intent a = new Intent(getBaseContext(), Pix.class);
        a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        a.putExtra("valor", txtValorFormaPagamento.getText().toString());
        a.putExtra("apiKey", unidades.getApi_key_asaas());
        a.putExtra("cliCob", unidades.getCliente_cob_asaas());
        a.putExtra("pedido", "" + idTemp);

        AddFormaPagamentoPIX("", "", "");
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

        i.putExtra("cpfCnpj_cliente", pedidos.getCpf_cliente());
        i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());
        i.putExtra("produto", bd.getProduto(bd.getItensPedido(pedidos.getId()).get(0).getProduto()));
        i.putExtra("qnt", "1");
        i.putExtra("vlt", txtValorFormaPagamento.getText().toString());

        startActivityForResult(i, PAGAMENTO_REQUEST);
    }

    @Override
    public void onBackPressed() {
        voltar();
    }

    private void voltar() {
        //
        if (listaFinanceiroCliente == null) {
            finish();
        } else {
            //
            if (listaFinanceiroCliente.size() > 0) {
                if (txtTotalItemFinanceiro.getText().equals("0,00")) {
                    //
                    Toast.makeText(this, "Adicione pelo menos uma forma de pagamento ao financeiro.", Toast.LENGTH_LONG).show();
                } else if (!txtTotalItemFinanceiro.getText().equals(txtTotalFinanceiro.getText())) {
                    //
                    Toast.makeText(this, "O valor do financeiro está diferente da venda.", Toast.LENGTH_LONG).show();
                } else {
                    confirmar();
                }
            } else {
                finish();
            }
        }
    }

    //Selecionar itens spProdutos
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    //Selecionar itens spProdutos
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public static class MoneyTextWatcher implements TextWatcher {
        private final WeakReference<EditText> editTextWeakReference;

        MoneyTextWatcher(EditText editText) {
            editTextWeakReference = new WeakReference<>(editText);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            EditText editText = editTextWeakReference.get();
            if (editText == null) return;
            String s = editable.toString();
            editText.removeTextChangedListener(this);
            String cleanString = s.replaceAll("[^0-9]", "");
            BigDecimal parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
            String formatted = NumberFormat.getCurrencyInstance().format(parsed);
            editText.setText(formatted);
            editText.setSelection(formatted.length());
            editText.addTextChangedListener(this);
        }
    }

    public void mostrarMsg() {

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("Dicas");
        //define a mensagem
        String msg = "Preencha os campos com as informações do pedido e toque no botão para adicionar os dados ao pedido.";
        builder.setMessage(msg);
        //define um botão como positivo
        builder.setPositiveButton("OK", (arg0, arg1) -> {
            //Toast.makeText(InformacoesVagas.this, "positivo=" + arg1, Toast.LENGTH_SHORT).show();
        });
        alerta = builder.create();
        alerta.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAGAMENTO_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {
                etCodAutorizacao.setText(data.getStringExtra("authorizationCode"));
                etCodAutorizacao.setEnabled(false);
                etNsuCeara.setText(data.getStringExtra("nsu"));
                etNsuCeara.setEnabled(false);

                AddFormaPagamento(data.getStringExtra("authorizationCode"), aux.getIdBandeira(data.getStringExtra("cardBrand")), data.getStringExtra("nsu"));
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                ShowMsgToast("Operação Cancelada!");
            }
        }
    }
}