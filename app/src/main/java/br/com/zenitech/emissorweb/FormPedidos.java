package br.com.zenitech.emissorweb;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.textfield.TextInputLayout;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import br.com.zenitech.emissorweb.adapters.FormasPagamentoPedidosAdapter;
import br.com.zenitech.emissorweb.domains.FormaPagamentoPedido;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PedidosTemp;
import br.com.zenitech.emissorweb.domains.Unidades;
import stone.application.StoneStart;
import stone.application.interfaces.StoneCallbackInterface;
import stone.providers.BluetoothConnectionProvider;
import stone.user.UserModel;
import stone.utils.PinpadObject;
import stone.utils.Stone;

import static br.com.zenitech.emissorweb.GerenciarPagamentoCartao.getApplicationName;

public class FormPedidos extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    static final int PAGAMENTO_REQUEST = 1;
    private DatabaseHelper bd;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;

    //"FORMA PAGAMENTO",
    //"OUTROS"
    String[] listaFormasPagamento = {
            "DINHEIRO",
            "CARTÃO DE CRÉDITO",
            "CARTÃO DE DÉBITO",
            "PAGAMENTO INSTANTÂNEO (PIX)"
    };
    String[] listaFormasPagamentoDinheiro = {
            "DINHEIRO"
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
    private FormasPagamentoPedidosAdapter adapter;
    private RecyclerView rvFinanceiro;

    private Spinner spProduto, spFormasPagamento, spDescricaoCredenciadora, spBandeiraCredenciadora;
    private EditText cpf_cnpj_cliente, etQuantidade, etPreco, etCodAutorizacao, etNsuCeara;
    private TextInputLayout TiNsuCeara;
    private LinearLayout llCredenciadora, infoDadosPedido, formFinanceiroPedido;
    private LinearLayoutCompat llFormAddFormasPag, formDadosPedido;

    private Toolbar toolbar;
    AlertDialog alerta;

    private String precoMinimo, precoMaximo;
    ClassAuxiliar aux;

    private int quant = 0;

    ArrayList<Unidades> elementos;
    Unidades unidades;

    TextView txtCpfCnpjCli, txtProduto, txtQuant, txtValTotalPagar, txtTotalPago;
    public static EditText txtVencimentoFormaPagamento, txtValorFormaPagamento;
    public static TextView txtTotalFinanceiro;
    public static TextView txtTotalItemFinanceiro;
    public static LinearLayout bgTotal;

    // BTNs **
    private Button btnAddF, btnPagamento, btnPagCartao;
    Button btnPagamentoCartaoNFCE, btnAvancarNFCE, btnEscolheFP, btnAddFormPag;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    int id = 0;
    int idTemp = 0;

    //QUANTIDADE FRAGMENTADA
    int quantidade = 0;
    int random;
    //int transmitir;
    //int transmitindo;
    // SE 1 INFORMA QUE A NOTA FOI FRACIONADA
    String NotaFracionada = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_pedidos);
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_form_pedido);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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
        /*ed.putInt("id_pedido", (prefs.getInt("id_pedido", 0) + 1)).apply();
        idTemp = prefs.getInt("id_pedido", 1);*/

        //MULTIPLICA O VALOR PELA QUANTIDADE
        idTemp = bd.getProximoIdPedido();
        /*if (prefs.getBoolean("primeiro_pedido", true)) {
            ed.putBoolean("primeiro_pedido", false).apply();
        }*/

        //
        bd.addPedidosTemp(new PedidosTemp(
                String.valueOf(idTemp),//ID PEDIDO
                "",//SITUAÇÃO
                "",//PROTOCOLO
                "",//DATA EMISSÃO
                "",//HORA EMISSÃO
                "",//VALOR TOTAL
                "",//DATA PROTOCOLO - "28042017"
                "",//HORA PROTOCOLO - "151540"
                "",//CPF/CNPJ CLIENTE
                ""//FORMA PAGAMENTO
        ));

        elementos = bd.getUnidades();
        unidades = elementos.get(0);

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
        cpf_cnpj_cliente = findViewById(R.id.cpf_cnpj_cliente);
        cpf_cnpj_cliente.addTextChangedListener(MaskUtil.insert(cpf_cnpj_cliente, MaskUtil.MaskType.AUTO));

        //
        etQuantidade = findViewById(R.id.etQuantidade);
        etQuantidade.setText("");
        etQuantidade.addTextChangedListener(new FormPedidos.VerifQuant(etQuantidade));

        //
        etPreco = findViewById(R.id.etPreco);
        etPreco.addTextChangedListener(new FormPedidos.MoneyTextWatcher(etPreco));

        //
        btnAddFormPag = findViewById(R.id.btnAddFormPag);
        btnAddFormPag.setOnClickListener(v -> AddFormaPagamento(etCodAutorizacao.getText().toString(), aux.getIdBandeira(spBandeiraCredenciadora.getSelectedItem().toString()), etNsuCeara.getText().toString()));

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

        //LISTA DE PRODUTOS
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
                    //Toast.makeText(getBaseContext(), "" + quant, Toast.LENGTH_LONG).show();
                    //Toast.makeText(getBaseContext(), "" + bd.getQuantProdutoRemessa(parent.getItemAtPosition(position).toString()), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getBaseContext(), precoMinimo + "|" + precoMaximo, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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

        findViewById(R.id.btn_finalizar).setOnClickListener(v -> VerificarCamposIniciarPedido(false));

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

        btnEscolheFP.setOnClickListener(v -> ValidarCampFormPedido());

        txtValorFormaPagamento = findViewById(R.id.txtValorFormaPagamento);
        txtValorFormaPagamento.addTextChangedListener(new FormPedidos.MoneyTextWatcher(txtValorFormaPagamento));
        //
        btnAddF = findViewById(R.id.btnAddF);
        btnAddF.setOnClickListener(v -> {
            //SE O USUÁRIO NÃO ADICIONAR NENHUM VALOR
            if (txtValorFormaPagamento.getText().toString().equals("") || txtValorFormaPagamento.getText().toString().equals("R$0,00")) {
                //
                Toast.makeText(this, "Adicione uma valor para esta forma de pagamento.", Toast.LENGTH_LONG).show();
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

        if (spFormasPagamento.getSelectedItem().toString().equals("FORMA PAGAMENTO")) {
            ShowMsgToast("Selecione a forma de pagamento.");
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
                    nsu
            ));

            //
            listaFinanceiroCliente = bd.getFinanceiroCliente(idTemp);
            adapter = new FormasPagamentoPedidosAdapter(this, listaFinanceiroCliente, elementos);
            rvFinanceiro.setAdapter(adapter);

            //
            String tif = aux.maskMoney(new BigDecimal(bd.getValorTotalFinanceiro(String.valueOf(idTemp))));
            txtTotalItemFinanceiro.setText(tif);

            //
            //!txtTotalItemFinanceiro.getText().equals(txtTotalFinanceiro.getText()

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
            /*txtDocumentoFormaPagamento.setText("");
            tilDocumento.setVisibility(View.VISIBLE);*/
            spFormasPagamento.setSelection(0);
            etCodAutorizacao.setText("");
        }
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


        // txtValorFormaPagamento txtTotalPago
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

        /*BigDecimal valorFinanceiro = new BigDecimal(String.valueOf(aux.converterValores(txtTotalFinanceiro.getText().toString())));
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
        }*/

    }

    private void addFinanceiro() {

        /*//
        id = id + 1;
        ed.putInt("id_financeiro_venda", id).apply();

        //
        String[] fPag = spFormasPagamentoCliente.getSelectedItem().toString().split(" _ ");

        String sql = "";
        sql += id + "\n";//CODIGO_FINANCEIRO
        sql += prefs.getString("unidade", "UNIDADE TESTE") + "\n";//UNIDADE_FINANCEIRO
        sql += classAuxiliar.inserirDataAtual() + "\n";//DATA_FINANCEIRO
        sql += codigo_cliente + "\n";//CODIGO_CLIENTE_FINANCEIRO
        sql += fPag[0] + "\n";//sql += spFormasPagamentoCliente.getSelectedItem().toString() + "\n";//FPAGAMENTO_FINANCEIRO
        sql += txtDocumentoFormaPagamento.getText().toString() + "\n";//DOCUMENTO_FINANCEIRO
        sql += String.valueOf(classAuxiliar.inserirData(classAuxiliar.formatarData(classAuxiliar.soNumeros(txtVencimentoFormaPagamento.getText().toString())))) + "\n";//VENCIMENTO_FINANCEIRO
        sql += String.valueOf(classAuxiliar.converterValores(txtValorFormaPagamento.getText().toString())) + "\n";//VALOR_FINANCEIRO
        sql += "0" + "\n";//STATUS_AUTORIZACAO
        sql += "0" + "\n";//PAGO
        sql += "0" + "\n";//VASILHAME_REF
        sql += "0" + "\n";//USUARIO_ATUAL_FINANCEIRO
        sql += classAuxiliar.inserirDataAtual() + "\n";//DATA_INCLUSAO
        sql += "" + "\n";//NOSSO_NUMERO_FINANCEIRO
        sql += "" + prefs.getInt("id_vendedor", 1) + "\n";//ID_VENDEDOR_FINANCEIRO
        sql += "" + prefs.getInt("id_venda_app", 1) + "\n";

        //SETAR O SQL NO LOG PARA CONSULTA
        Log.e("SQL", sql);

        //INSERIR FINANCEIRO
        bd.addFinanceiro(new FinanceiroVendasDomain(
                String.valueOf(id),//CODIGO_FINANCEIRO
                prefs.getString("unidade", "UNIDADE TESTE"),//UNIDADE_FINANCEIRO
                classAuxiliar.inserirDataAtual(),//DATA_FINANCEIRO
                codigo_cliente,//CODIGO_CLIENTE_FINANCEIRO
                fPag[0],//spFormasPagamentoCliente.getSelectedItem().toString(),//FPAGAMENTO_FINANCEIRO
                txtDocumentoFormaPagamento.getText().toString(),//DOCUMENTO_FINANCEIRO
                String.valueOf(classAuxiliar.inserirData(classAuxiliar.formatarData(classAuxiliar.soNumeros(txtVencimentoFormaPagamento.getText().toString())))),//VENCIMENTO_FINANCEIRO
                String.valueOf(classAuxiliar.converterValores(txtValorFormaPagamento.getText().toString())),//VALOR_FINANCEIRO
                "0",//STATUS_AUTORIZACAO
                "0",//PAGO
                "0",//VASILHAME_REF
                "0",//USUARIO_ATUAL_FINANCEIRO
                "" + classAuxiliar.inserirDataAtual(),//DATA_INCLUSAO
                "",//NOSSO_NUMERO_FINANCEIRO
                "" + prefs.getInt("id_vendedor", 1),//ID_VENDEDOR_FINANCEIRO
                "" + prefs.getInt("id_venda_app", 1)
        ));

        //
        listaFinanceiroCliente = bd.getFinanceiroCliente(prefs.getInt("id_venda_app", 1));
        adapter = new FinanceiroVendasAdapter(this, listaFinanceiroCliente);
        rvFinanceiro.setAdapter(adapter);

        //
        String tif = classAuxiliar.maskMoney(new BigDecimal(bd.getValorTotalFinanceiro(String.valueOf(prefs.getInt("id_venda_app", 1)))));
        txtTotalItemFinanceiro.setText(tif);

        //
        //!txtTotalItemFinanceiro.getText().equals(txtTotalFinanceiro.getText()

        //
        String valorFinanceiro = String.valueOf(classAuxiliar.converterValores(txtTotalFinanceiro.getText().toString()));
        String valorFinanceiroAdd = String.valueOf(classAuxiliar.converterValores(txtTotalItemFinanceiro.getText().toString()));

        //SUBTRAIR O VALOR PELA QUANTIDADE
        String[] subtracao = {valorFinanceiro, valorFinanceiroAdd};
        String total = String.valueOf(classAuxiliar.subitrair(subtracao));

        txtValorFormaPagamento.setText(total);

        //
        if (comparar()) {

            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.erro));
            txtValorFormaPagamento.setText("0,00");
        } else {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.transparente));
        }

        //
        txtDocumentoFormaPagamento.setText("");
        tilDocumento.setVisibility(View.VISIBLE);
        spFormasPagamentoCliente.setSelection(0);

        //ESCONDER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }*/
    }

    private void ValidarCampFormPedido() {
        //ESCODER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        //Log.i("Valor", String.valueOf(aux.converterValores(etPreco.getText().toString())));

        String valEtPreco = "";
        if (!etPreco.getText().toString().equals("")) {
            valEtPreco = String.valueOf(aux.converterValores(etPreco.getText().toString()));
        }

        //
        if (spProduto.getSelectedItem().toString().equals("PRODUTO")) {
            ShowMsgToast("Selecione um produto.");
        } else if (etQuantidade.getText().toString().equals("") || etQuantidade.getText().toString().equals("0")) {
            ShowMsgToast("Informe a quantidade.");
        } else if (Integer.parseInt(etQuantidade.getText().toString()) > quant) {
            ShowMsgToast("Restam apenas " + quant + " itens. Diminua a quantidade!");
        } else if (etPreco.getText().toString().equals("")
                || valEtPreco.equals("R$ 0,00")
                || valEtPreco.equals("0.0")
                || valEtPreco.equals("0.00")) {
            ShowMsgToast("Informe o valor unitário.");
        } else {

            String[] ars = {precoMinimo, String.valueOf(aux.converterValores(etPreco.getText().toString()))};
            int vComp = aux.comparar(ars);
            //
            String[] arsMax = {precoMaximo, String.valueOf(aux.converterValores(etPreco.getText().toString()))};
            int vCompMax = aux.comparar(arsMax);

            // Verifica a quantidade máxima permeitida
            if (Integer.parseInt(etQuantidade.getText().toString()) > 300) {
                ShowMsgToast("Quantidade Máxima 300 Unidades.");
            }
            // Verifica se o valor informado é igual ou maior ao preço minimo
            else if (!precoMinimo.equalsIgnoreCase("0.0")) {

                if (vComp > 0) {
                    ShowMsgToast("O Valor não pode ser menor que o preço mínimo!");
                } else if (vCompMax == -1) {
                    ShowMsgToast("O Valor não pode ser maior que o preço máximo!");
                } else {
                    formsView();
                }
            } else {
                formsView();
            }
        }
    }

    private void formsView() {
        //
        quantidade = Integer.parseInt(etQuantidade.getText().toString());
        fracionar();

        // Verifica a quantidade máxima permeitida
        if (Integer.parseInt(etQuantidade.getText().toString()) > 5) {
            //ShowMsgToast("Maior que 5");
            txtValorFormaPagamento.setEnabled(false);
            spFormasPagamento.setAdapter(null);
            spFormasPagamento.setAdapter(adapterFormasPagamentoDinheiro);
        }
        //
        String valorUnit = String.valueOf(aux.converterValores(etPreco.getText().toString()));

        //MULTIPLICA O VALOR PELA QUANTIDADE
        String[] multiplicar = {valorUnit, etQuantidade.getText().toString()};
        String total = String.valueOf(aux.multiplicar(multiplicar));
        //
        txtCpfCnpjCli.setText(String.format("Cpf/Cnpj: %s", cpf_cnpj_cliente.getText().toString()));
        txtProduto.setText(String.format("Produto: %s", spProduto.getSelectedItem().toString()));
        txtQuant.setText(String.format("Quantidade: %s", etQuantidade.getText().toString()));
        txtTotalFinanceiro.setText(aux.maskMoney(new BigDecimal(total)));
        txtValorFormaPagamento.setText(aux.maskMoney(new BigDecimal(total)));
        //
        formDadosPedido.setVisibility(View.GONE);
        formFinanceiroPedido.setVisibility(View.VISIBLE);
    }

    private void VerificarCamposIniciarPedido(boolean pagamento) {

        //ESCODER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        //
        if (!compararValorRestante()) return;

        //Log.i("Valor", String.valueOf(aux.converterValores(etPreco.getText().toString())));

        String valEtPreco = "";
        if (!etPreco.getText().toString().equals("")) {
            valEtPreco = String.valueOf(aux.converterValores(etPreco.getText().toString()));
        }

        //
        if (spFormasPagamento.getSelectedItem().toString().equals("FORMA PAGAMENTO")) {
            ShowMsgToast("Selecione a forma de pagamento.");
        } else if (spProduto.getSelectedItem().toString().equals("PRODUTO")) {
            ShowMsgToast("Selecione um produto.");
        } else if (etQuantidade.getText().toString().equals("") || etQuantidade.getText().toString().equals("0")) {
            ShowMsgToast("Informe a quantidade.");
        } else if (Integer.parseInt(etQuantidade.getText().toString()) > quant) {
            ShowMsgToast("Restam apenas " + quant + " itens. Diminua a quantidade!");
        } else if (etPreco.getText().toString().equals("")
                || valEtPreco.equals("R$ 0,00")
                || valEtPreco.equals("0.0")
                || valEtPreco.equals("0.00")) {
            ShowMsgToast("Informe o valor unitário.");
        } else {


            String[] ars = {precoMinimo, String.valueOf(aux.converterValores(etPreco.getText().toString()))};
            int vComp = aux.comparar(ars);
            //
            String[] arsMax = {precoMaximo, String.valueOf(aux.converterValores(etPreco.getText().toString()))};
            int vCompMax = aux.comparar(arsMax);

            // Verifica a quantidade máxima permeitida
            if (Integer.parseInt(etQuantidade.getText().toString()) > 300) {
                ShowMsgToast("Quantidade Máxima 300 Unidades.");
            }
            // Verifica se o valor informado é igual ou maior ao preço minimo
            else if (!precoMinimo.equalsIgnoreCase("0.0")) {
                if (vComp > 0) {
                    ShowMsgToast("O Valor não pode ser menor que o preço mínimo!");
                } else if (vCompMax == -1) {
                    ShowMsgToast("O Valor não pode ser maior que o preço máximo!");
                } else {
                    if (pagamento) {
                        iniciarPagamento();
                    } else {
                        confirmar();
                    }
                }
            } else {
                if (pagamento) {
                    iniciarPagamento();
                } else {
                    confirmar();
                }
            }
        }
    }

    private void ShowMsgToast(String msg) {
        Toast toast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void confirmar() {

        if (bd.getPedidosTransmitirFecharDia().size() > 0) {
            Intent i = new Intent(getBaseContext(), ConfirmarDadosPedido.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("cpfCnpj_cliente", cpf_cnpj_cliente.getText().toString());
            i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());
            i.putExtra("produto", spProduto.getSelectedItem().toString());
            i.putExtra("qnt", etQuantidade.getText().toString());
            i.putExtra("vlt", etPreco.getText().toString());
            i.putExtra("credenciadora", idCredenciadoras.get(spDescricaoCredenciadora.getSelectedItemPosition()));//spDescricaoCredenciadora.getSelectedItem().toString()
            i.putExtra("bandeira", aux.getIdBandeira(spBandeiraCredenciadora.getSelectedItem().toString()));
            i.putExtra("cod_aut", etCodAutorizacao.getText().toString());
            i.putExtra("nsu", etNsuCeara.getText().toString());

            startActivity(i);
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Encontramos um problema com esse pedido. Precisa refazer!", Toast.LENGTH_SHORT).show();
        }
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

        /*i.putExtra("qnt", etQuantidade.getText().toString());
        i.putExtra("vlt", etPreco.getText().toString());
        i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());*/
        i.putExtra("cpfCnpj_cliente", cpf_cnpj_cliente.getText().toString());
        i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());
        i.putExtra("produto", spProduto.getSelectedItem().toString());
        i.putExtra("qnt", "1");
        i.putExtra("vlt", txtValorFormaPagamento.getText().toString());

        startActivityForResult(i, PAGAMENTO_REQUEST);
    }

    private void introducao() {
        // We load a drawable and create a location to show a tap target here
        // We need the display to get the width and height at this point in time
        //final Display display = getWindowManager().getDefaultDisplay();
        // Load our little droid guy
        //final Drawable droid = ContextCompat.getDrawable(this, R.drawable.logo_emissor_web);
        // Tell our droid buddy where we want him to appear
        //final Rect droidTarget = new Rect(0, 0, droid.getIntrinsicWidth() * 2, droid.getIntrinsicHeight() * 2);
        // Using deprecated methods makes you look way cool
        //droidTarget.offset(display.getWidth() / 2, display.getHeight() / 2);

        final SpannableString sassyDesc = new SpannableString("Volte para lista de elementosPedidos quando quiser!");
        sassyDesc.setSpan(new StyleSpan(Typeface.ITALIC), 0, sassyDesc.length(), 0);

        final SpannableString sassyDesc2 = new SpannableString("Preencha os campos com os dados do pedido!");
        sassyDesc2.setSpan(new StyleSpan(Typeface.ITALIC), 0, sassyDesc2.length(), 0);

        final SpannableString sassyDesc3 = new SpannableString("Toque para adicionar as informaçoes ao pedido!");
        sassyDesc3.setSpan(new StyleSpan(Typeface.ITALIC), 0, sassyDesc3.length(), 0);

        // We have a sequence of targets, so lets build it!
        final TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        // BOTÃO VOLTAR
                        TapTarget.forToolbarNavigationIcon(toolbar, "Voltar", sassyDesc)
                                .id(1),

                        // BOTÃO DE FILTRO TOOBAR
                        TapTarget.forToolbarMenuItem(toolbar, R.id.action_infor_novo_pedido, "Dicas", "Toque para saber como preencher os dados do pedido!")
                                .textTypeface(Typeface.defaultFromStyle(Typeface.ITALIC))
                                .id(2),

                        // BOTAO NOVO PEDIDO
                        TapTarget.forView(findViewById(R.id.cpf_cnpj_cliente), "Dados do Pedido", sassyDesc2)
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.colorAccent)
                                .targetCircleColor(android.R.color.white)
                                .transparentTarget(true)
                                .textColor(android.R.color.white)
                                .id(3),

                        // BOTAO NOVO PEDIDO
                        TapTarget.forView(findViewById(R.id.fabAvancar), "Avançar", sassyDesc3)
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.colorAccent)
                                .targetCircleColor(android.R.color.white)
                                .transparentTarget(true)
                                .textColor(android.R.color.white)
                                .id(3)
                )
                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                        //((TextView) findViewById(R.id.texto)).setText("Parabéns! Agora voce já sabe como usar o Emissor Web!");
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        final AlertDialog dialog = new AlertDialog.Builder(FormPedidos.this)
                                .setTitle("Uh oh")
                                .setMessage("You canceled the sequence")
                                .setPositiveButton("Oops", null).show();
                        TapTargetView.showFor(dialog,
                                TapTarget.forView(dialog.getButton(DialogInterface.BUTTON_POSITIVE), "Uh oh!", "Você cancelou a sequência na etapa " + lastTarget.id())
                                        .cancelable(false)
                                        .tintTarget(false), new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        super.onTargetClick(view);
                                        dialog.dismiss();
                                    }
                                });
                    }
                });

        sequence.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_form_pedido, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                //
                voltar();
                break;
            case R.id.action_infor_novo_pedido:
                mostrarMsg();
                break;
            case R.id.action_intro_principal:
                introducao();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        voltar();
    }

    private void voltar() {
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

    public class MoneyTextWatcher implements TextWatcher {
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

    public class VerifQuant implements TextWatcher {
        private final WeakReference<EditText> editTextWeakReference;

        VerifQuant(EditText editText) {
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
            editText.setText(cleanString);
            editText.setSelection(cleanString.length());
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
        /*//define um botão como negativo.
        builder.setNegativeButton("Negativo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(InformacoesVagas.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
            }
        });*/
        //cria o AlertDialog
        alerta = builder.create();
        //Exibe alerta
        alerta.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAGAMENTO_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                //ShowMsgToast(data.getStringExtra("result"));

                etCodAutorizacao.setText(data.getStringExtra("authorizationCode"));
                etCodAutorizacao.setEnabled(false);
                etNsuCeara.setText(data.getStringExtra("nsu"));
                etNsuCeara.setEnabled(false);

                //confirmar();
                //ShowMsgToast(data.getStringExtra("result"));
                AddFormaPagamento(data.getStringExtra("authorizationCode"), aux.getIdBandeira(data.getStringExtra("cardBrand")), data.getStringExtra("nsu"));
                //Timber.tag("Stone").i(Objects.requireNonNull(data.getStringExtra("authorizationCode")));
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                ShowMsgToast("Operação Cancelada!");
            }
        }
    }

    // Iniciar o Stone
    void iniciarStone() {
        // O primeiro passo é inicializar o SDK.
        StoneStart.init(getApplicationContext());
        /*Em seguida, é necessário chamar o método setAppName da classe Stone,
        que recebe como parâmetro uma String referente ao nome da sua aplicação.*/
        Stone.setAppName(getApplicationName(getApplicationContext()));
        //Ambiente de Sandbox "Teste"
        Stone.setEnvironment(new Configuracoes().Ambiente());
        //Ambiente de Produção
        //Stone.setEnvironment((Environment.PRODUCTION));

        // Esse método deve ser executado para inicializar o SDK
        List<UserModel> userList = StoneStart.init(getApplicationContext());

        // Quando é retornado null, o SDK ainda não foi ativado
        /*if (userList != null) {
            // O SDK já foi ativado.
            _pinpadAtivado();

        } else {
            // Inicia a ativação do SDK
            ativarStoneCode();
        }*/
    }

    public void turnBluetoothOn() {
        try {
            mBluetoothAdapter.enable();
            do {
            } while (!mBluetoothAdapter.isEnabled());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pinpadConnection() {

        // Pega o pinpad selecionado do ListView.
        //String[] pinpadInfo = listView.getAdapter().getItem(position).toString().split("_");
        //PinpadObject pinpadSelected = new PinpadObject(pinpadInfo[0], pinpadInfo[1], false);
        PinpadObject pinpadSelected = new PinpadObject("PAX-6A801896", "34:81:F4:04:BF:37", false);

        // Passa o pinpad selecionado para o provider de conexão bluetooth.
        final BluetoothConnectionProvider bluetoothConnectionProvider = new BluetoothConnectionProvider(FormPedidos.this, pinpadSelected);
        bluetoothConnectionProvider.setDialogMessage("Criando conexao com o pinpad selecionado"); // Mensagem exibida do dialog.
        bluetoothConnectionProvider.useDefaultUI(false); // Informa que haverá um feedback para o usuário.
        bluetoothConnectionProvider.setConnectionCallback(new StoneCallbackInterface() {

            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Pinpad conectado", Toast.LENGTH_SHORT).show();
                //btConnected = true;
                //finish();
            }

            public void onError() {
                Toast.makeText(getApplicationContext(), "Erro durante a conexao. Verifique a lista de erros do provider para mais informacoes", Toast.LENGTH_SHORT).show();
                //Timber.e("onError: %s", bluetoothConnectionProvider.getListOfErrors());
            }
        });
        bluetoothConnectionProvider.execute(); // Executa o provider de conexão bluetooth.
    }

    // Fracionar o pedido caso a quantidade seja maior que 5
    boolean dd = false;

    public void fracionar() {
        if (quantidade > 5) {
            Random r = new Random();
            random = r.nextInt(6 - 1) + 1;
            NotaFracionada = "1";
        } else {
            random = quantidade;
        }

        String[] sub = {String.valueOf(quantidade), String.valueOf(random)};
        quantidade = aux.subitrair(sub).intValue();

        //
        String data = aux.exibirDataAtual();
        String hora = aux.horaAtual();
        //statusNota.setText("");
        //protocoloNota.setText("");
        //dataHoraNota.setText(String.format("%s %s", cAux.exibirDataAtual(), cAux.horaAtual()));
        //dataHoraNota.setText(String.format("%s %s", data, hora));

        //-------CRIA UM ID PARA O PEDIDO------//
        /*if(prefs.getInt("id_pedido", 0) == 0){
            ed.putInt("id_pedido", (Integer.parseInt(bd.getUltimoIdPedido()) + 1)).apply();
        }else{
            ed.putInt("id_pedido", (prefs.getInt("id_pedido", 0) + 1)).apply();
        }
        dhfghgdg*/
        if (!dd) {
            id = idTemp;//prefs.getInt("id_pedido", 1);
            dd = true;
        } else {
            ed.putInt("id_pedido", (Integer.parseInt(bd.getUltimoIdPedidos()) + 1)).apply();
            id = prefs.getInt("id_pedido", 1);
        }


        //id = Integer.parseInt(bd.getUltimoIdPedido());


        //INSERI O PEDIDO NO BANCO DE DADOS
        addPedido(
                "OFF",
                "",
                aux.inserirData(data),
                hora,
                aux.soNumeros(txtValTotalPagar.getText().toString()),
                "",
                "",
                cpf_cnpj_cliente.getText().toString(),
                "",
                String.valueOf(random)
        );

        if (quantidade > 0) {
            fracionar();
        } /*else {
            //elementosPedidos = bd.getPedidosTransmitir();
            elementosPedidos = bd.getPedidosTransmitirFecharDia();
            transmitir = elementosPedidos.size();
            transmitindo = elementosPedidos.size();

            Log.i(TAG, String.valueOf(elementosPedidos.size()));
        }*/

        //id += 1;
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
        String total, valorUnit;

        //
        valorUnit = String.valueOf(aux.converterValores(etPreco.getText().toString()));

        //MULTIPLICA O VALOR PELA QUANTIDADE
        String[] multiplicar = {valorUnit, String.valueOf(random)};
        total = String.valueOf(aux.multiplicar(multiplicar));

        //
        bd.addPedidos(new Pedidos(
                String.valueOf(id),//ID PEDIDO
                status,//SITUAÇÃO
                protocolo,//PROTOCOLO
                dataEmissao,//DATA EMISSÃO
                horaEmissao,//HORA EMISSÃO
                "" + aux.converterValores(aux.soNumeros(total)),//VALOR TOTAL
                dataProtocolo,//DATA PROTOCOLO - "28042017"
                horaProtocolo,//HORA PROTOCOLO - "151540"
                cpf,//CPF/CNPJ CLIENTE
                FPagamento,//FORMA PAGAMENTO
                "",
                bd.getUltimoIdPedido(),
                NotaFracionada,
                spDescricaoCredenciadora.getSelectedItem().toString()
        ));

        //
        bd.addItensPedidos(new ItensPedidos(
                String.valueOf(id),//ID PEDIDO
                bd.getIdProduto(spProduto.getSelectedItem().toString()),
                quantidade,
                aux.soNumeros(String.valueOf(aux.converterValores(etPreco.getText().toString()))),
                null
        ));
    }
}
