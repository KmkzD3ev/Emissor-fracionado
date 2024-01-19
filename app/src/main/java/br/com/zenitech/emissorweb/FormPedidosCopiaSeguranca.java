package br.com.zenitech.emissorweb;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
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

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.textfield.TextInputLayout;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import br.com.zenitech.emissorweb.adapters.FormasPagamentoPedidosAdapter;
import br.com.zenitech.emissorweb.adapters.ProdutosPedidoAdapter;
import br.com.zenitech.emissorweb.domains.FormaPagamentoPedido;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PedidosTemp;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.ProdutosPedidoDomain;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.interfaces.IProdutosPedidoObserver;

public class FormPedidosCopiaSeguranca extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,
        IProdutosPedidoObserver {
    static final int PAGAMENTO_REQUEST = 1;
    static final int PAGAMENTO_PIX_REQUEST = 2;
    private DatabaseHelper bd;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;
    boolean api_asaas = false;

    //"FORMA PAGAMENTO",
    //"OUTROS"
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
    private FormasPagamentoPedidosAdapter adapter;
    private RecyclerView rvFinanceiro, rvProdutosPedido;

    // LISTA PRODUTOS
    private ArrayList<ProdutosPedidoDomain> listaProdutosPedido;
    ProdutosPedidoAdapter produtosPedidoAdapter;

    private Spinner spProduto, spFormasPagamento, spDescricaoCredenciadora, spBandeiraCredenciadora;
    private EditText cpf_cnpj_cliente, etQuantidade, etPreco, etCodAutorizacao, etNsuCeara, etDesconto;
    private TextInputLayout TiNsuCeara;
    LinearLayout llCredenciadora, infoDadosPedido, formFinanceiroPedido;
    LinearLayoutCompat llFormAddFormasPag, formDadosPedido, formVenda;

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
    Button btnPagamentoCartaoNFCE, btnAvancarNFCE, btnEscolheFP, btnAddFormPag, btnPagamentoForm, btnAddProdutoLista;
    //private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    int id = 0;
    int idTemp = 0;

    //QUANTIDADE FRAGMENTADA
    int quantidade = 0;
    int random;
    //int transmitir;
    //int transmitindo;
    // SE 1 INFORMA QUE A NOTA FOI FRACIONADA
    String NotaFracionada = "0";

    boolean calcularDescto = true;

    ArrayList<PosApp> elementosPos;
    PosApp posApp;
    String codigoProduto;
    //boolean _fracionar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_pedidos);
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_form_pedido);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //_fracionar = false;
        codigoProduto = "";

        //
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();

        //
        bd = new DatabaseHelper(this);
        aux = new ClassAuxiliar();

        elementosPos = bd.getPos();
        posApp = elementosPos.get(0);

        //
        rvFinanceiro = findViewById(R.id.rvFinanceiro);
        rvFinanceiro.setLayoutManager(new LinearLayoutManager(this));

        //
        rvProdutosPedido = findViewById(R.id.rvProdutosPedido);
        rvProdutosPedido.setLayoutManager(new LinearLayoutManager(this));

        //
        bgTotal = findViewById(R.id.bgTotal);

        //MULTIPLICA O VALOR PELA QUANTIDADE
        idTemp = bd.getProximoIdPedido();
        //Log.e("IdTemp", String.valueOf(idTemp));

        //
        bd.addPedidosTemp(String.valueOf(idTemp));

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
        formVenda = findViewById(R.id.formVenda);
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
        etQuantidade.addTextChangedListener(new FormPedidosCopiaSeguranca.VerifQuant(etQuantidade));

        //
        etPreco = findViewById(R.id.etPreco);
        etPreco.addTextChangedListener(new FormPedidosCopiaSeguranca.MoneyTextWatcher(etPreco));

        //
        etDesconto = findViewById(R.id.etDesconto);
        etDesconto.addTextChangedListener(new FormPedidosCopiaSeguranca.MoneyTextWatcher(etDesconto));
        etDesconto.setText("0,00");
        try {
            if (posApp.getDesconto_app_emissor().equalsIgnoreCase("1")) {
                /*
                 * TRABALHAR NA PARTE DE DESCONTO COM O NOVO MODELO COM MAIS DE 1 PRODUTO,
                 * O VALOR DO DESCONTO SÓ APARECE NO FORMULÁRIO DE PRODUTOS ISSO É UM ERRO,
                 * TEM QUE CONTABILIZAR NO FINANCEIRO E NA TRANSMISSÃO DA NOTA*/
                //etDesconto.setVisibility(View.VISIBLE);
            }
        } catch (Exception ignored) {

        }


        //
        btnAddFormPag = findViewById(R.id.btnAddFormPag);
        btnAddFormPag.setOnClickListener(v -> AddFormaPagamento(etCodAutorizacao.getText().toString(), aux.getIdBandeira(spBandeiraCredenciadora.getSelectedItem().toString()), etNsuCeara.getText().toString()));

        //
        txtCpfCnpjCli = findViewById(R.id.txtCpfCnpjCli);
        txtProduto = findViewById(R.id.txtProduto);
        txtQuant = findViewById(R.id.txtQuant);
        txtValTotalPagar = findViewById(R.id.txtValTotalPagar);
        txtTotalPago = findViewById(R.id.txtTotalPago);

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
        btnPagamentoForm = findViewById(R.id.btnPagamentoForm);
        btnAddProdutoLista = findViewById(R.id.btnAddProdutoLista);
        infoDadosPedido = findViewById(R.id.infoDadosPedido);
        llFormAddFormasPag = findViewById(R.id.llFormAddFormasPag);

        //btnEscolheFP.setOnClickListener(v -> ValidarCampFormPedido());ValidarCampFormPedido()
        btnPagamentoForm.setOnClickListener(v -> {

            TextView txt = findViewById(R.id.msgErroFracionar);

            if (!listaProdutosPedido.isEmpty()) {
                /*
                    REGRAS PARA FRACIONAMENTO:
                    # NÃO PODE TER MAIS DE 1 PRODUTO
                    # O PRODUTO PRECISA SER GÁS COM NCM = 27111910
                    # A QUANTIDADE PRECISA SER MAIOR QUE 5 E NEMOR QUE 100
                */

                // RECEBE A QUANTIDADE DE PRODUTOS COM O NCM = 27111910
                quantidade = bd.getQuantProdutosPedidoNCMGas(idTemp);                      // QUANTIDADE DE GÁS
                int quantProdutoPedido = listaProdutosPedido.size();                    // QUANTIDADE DE PRODUTOS NA LISTA
                int quantProdutosDiverso = bd.getQuantProdutosPedidoDiverso(idTemp);    // PRODUTOS QUE NÃO SÃO GÁS GLP

                // SE A QUANTIDADE DE GÁS FOR MAIOR QUE 0 E MENOR IGUAL A 5 NÃO PRECISA FRACIONAR
                if (quantidade > 0 && quantidade <= 5) {
                    StringBuilder str = new StringBuilder();
                    boolean erro = false;
                    boolean avancar = true;
                    List<String> listPro = bd.getQuantProdutosPedidoNCM(idTemp);
                    if (!listPro.isEmpty()) {
                        for (int i = 0; listPro.size() > i; i++) {
                            String[] iList = listPro.get(i).split(",");
                            List<String> minMaxFrac = bd.getMinMaxFracionamentoProduto(iList[0]);
                            if (!minMaxFrac.get(0).equals("0") && !minMaxFrac.get(1).equals("0")) {
                                int min = Integer.parseInt(minMaxFrac.get(0));
                                int max = Integer.parseInt(minMaxFrac.get(1));

                                if (Integer.parseInt(iList[1]) > max) {
                                    avancar = false;
                                    erro = true;

                                    String sProduto = bd.getProduto(iList[0]);
                                    str.append("Atenção:\nPara o produto ").append(sProduto).append(" a quantidade máxima por pedido é de ").append(max).append(" unidades.\n");
                                }
                            }
                        }

                        if (erro) {
                            txt.setVisibility(View.VISIBLE);
                            txt.setText(str.toString());
                        }
                    }

                    if (avancar)
                        formsView();
                }
                // SE A QUATIDADE DE PRODUTOS NA LISTA FOR IGUAL 1 E A QUANTIDADE DO PRODUTO FOR MENOR QUE 101 PODE FRACIONAR
                else if (quantProdutoPedido == 1 && quantProdutosDiverso < 101) {
                    //_fracionar = true;
                    // PARA FRACIONAR NOTAS DE ÁGUA E OUTROS PRODUTOS SEM SER GÁS
                    quantidade = quantProdutosDiverso;
                    formsView();
                }
                // SE A QUATIDADE DE PRODUTOS NA LISTA FOR IGUAL 1 E A QUANTIDADE DO PRODUTO FOR MENOR QUE 101 PODE FRACIONAR
                else if (quantProdutoPedido == 1) {
                    // VERIFICA SE A QUANTIDADE DE GÁS É MAIOR QUE 5
                    if (quantidade > 5) {
                        txt.setVisibility(View.VISIBLE);
                        String proPedido = bd.getProdutosPedidoNCMGas(idTemp);
                        txt.setText(MessageFormat.format("Atenção:\nPara quantidades acima de 5 unidades do(s) produto(s) {0}não é permitido adicionar outros itens na nota. Favor emitir em notas separadas.", proPedido));
                        return;
                    }

                    // PARA FRACIONAR NOTAS DE ÁGUA E OUTROS PRODUTOS SEM SER GÁS
                    //quantidade = quantProdutosDiverso;
                    StringBuilder str = new StringBuilder();
                    boolean erro = false;
                    boolean avancar = true;
                    List<String> listPro = bd.getQuantProdutosPedidoNCM(idTemp);
                    if (!listPro.isEmpty()) {
                        for (int i = 0; listPro.size() > i; i++) {
                            String[] iList = listPro.get(i).split(",");
                            List<String> minMaxFrac = bd.getMinMaxFracionamentoProduto(iList[0]);
                            if (!minMaxFrac.get(0).equals("0") && !minMaxFrac.get(1).equals("0")) {
                                int min = Integer.parseInt(minMaxFrac.get(0));
                                int max = Integer.parseInt(minMaxFrac.get(1));

                                    avancar = false;
                                    erro = true;

                                    String sProduto = bd.getProduto(iList[0]);
                                    str.append("Atenção:\nPara o produto ").append(sProduto).append(" a quantidade máxima por pedido é de ").append(max).append(" unidades.\n");

                            }
                        }

                        if (erro) {
                            txt.setVisibility(View.VISIBLE);
                            txt.setText(str.toString());
                        }
                    }

                    if (avancar)
                        formsView();
                }else if (quantProdutoPedido > 1) {
                    // VERIFICA SE A QUANTIDADE DE GÁS É MAIOR QUE 5
                    if (quantidade > 5) {
                        txt.setVisibility(View.VISIBLE);
                        String proPedido = bd.getProdutosPedidoNCMGas(idTemp);
                        txt.setText(MessageFormat.format("Atenção:\nPara quantidades acima de 5 unidades do(s) produto(s) {0}não é permitido adicionar outros itens na nota. Favor emitir em notas separadas.", proPedido));
                        return;
                    }

                    // PARA FRACIONAR NOTAS DE ÁGUA E OUTROS PRODUTOS SEM SER GÁS
                    //quantidade = quantProdutosDiverso;
                    StringBuilder str = new StringBuilder();
                    boolean erro = false;
                    boolean avancar = true;
                    List<String> listPro = bd.getQuantProdutosPedidoNCM(idTemp);
                    if (!listPro.isEmpty()) {
                        for (int i = 0; listPro.size() > i; i++) {
                            String[] iList = listPro.get(i).split(",");
                            List<String> minMaxFrac = bd.getMinMaxFracionamentoProduto(iList[0]);
                            if (!minMaxFrac.get(0).equals("0") && !minMaxFrac.get(1).equals("0")) {
                                int min = Integer.parseInt(minMaxFrac.get(0));
                                int max = Integer.parseInt(minMaxFrac.get(1));

                                //Toast.makeText(this, iList[1] + " | " + max, Toast.LENGTH_SHORT).show();

                                if (Integer.parseInt(iList[1]) > max) {
                                    avancar = false;
                                    erro = true;

                                    String sProduto = bd.getProduto(iList[0]);
                                    str.append("Atenção:\nPara o produto ").append(sProduto).append(" a quantidade máxima por pedido é de ").append(max).append(" unidades.\n");
                                }
                            }
                        }

                        if (erro) {
                            txt.setVisibility(View.VISIBLE);
                            txt.setText(str.toString());
                        }
                    }

                    if (avancar)
                        formsView();
                } else {

                    StringBuilder str = new StringBuilder();
                    boolean erro = false;
                    List<String> listPro = bd.getQuantProdutosPedidoNCM(idTemp);
                    if (!listPro.isEmpty()) {
                        for (int i = 0; listPro.size() > i; i++) {
                            String[] iList = listPro.get(i).split(",");
                            List<String> minMaxFrac = bd.getMinMaxFracionamentoProduto(iList[0]);
                            if (!minMaxFrac.get(0).equals("0") && !minMaxFrac.get(1).equals("0")) {
                                int min = Integer.parseInt(minMaxFrac.get(0));
                                int max = Integer.parseInt(minMaxFrac.get(1));

                                if (Integer.parseInt(iList[1]) > max) {
                                    erro = true;

                                    String sProduto = bd.getProduto(iList[0]);
                                    str.append("Atenção:\nLimite de quantidade para o produto ").append(sProduto).append(": \nQuantidade máxima por pedido: ").append(max).append(" unidades.\n").append("Quantidade máxima por lote: 100 unidades.");

                                }
                            }
                        }

                        if (erro) {
                            txt.setVisibility(View.VISIBLE);
                            txt.setText(str.toString());
                        }
                    } else {
                        txt.setVisibility(View.VISIBLE);
                        txt.setText(R.string.msg_erro_fracionamento);
                    }
                }
            } else {
                Toast.makeText(this, "Adicione pelo menos um produto", Toast.LENGTH_LONG).show();
            }
        });

        txtValorFormaPagamento = findViewById(R.id.txtValorFormaPagamento);
        txtValorFormaPagamento.addTextChangedListener(new FormPedidosCopiaSeguranca.MoneyTextWatcher(txtValorFormaPagamento));

        // ADD PRODUTO AO PEDIDO
        btnAddProdutoLista.setOnClickListener(v -> ValidarCampFormPedido());
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


        listaProdutosPedido = bd.getProdutosPedido(idTemp);
        produtosPedidoAdapter = new ProdutosPedidoAdapter(this, listaProdutosPedido, bd);
        // Registra a Activity como Observador
        produtosPedidoAdapter.registerObserver(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        atualizarListaFormPag();
        atualizarListaProdutos();
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
            ShowMsgToast("Selecione a forma de pagamento.");
        } else if (val.equalsIgnoreCase("") || val.equalsIgnoreCase("000")) {
            ShowMsgToast("Adicione um valor");
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


            if (bd.getPedidosTemp(String.valueOf(idTemp)).size() > 1) {
                // PARA NOTAS FRACIONADAS
                for (Pedidos pedido : bd.getPedidosTemp(String.valueOf(idTemp))) {
                    ItensPedidos itemPedido = bd.getItensPedido(pedido.getId()).get(0);
                    //String valFinanceiro = aux.multiplicar();
                    bd.addFormasPagamentoPedidosTemp(new FormaPagamentoPedido(
                            "",
                            pedido.getId(),//String.valueOf(idTemp), //ID PEDIDO
                            aux.getIdFormaPagamento(spFormasPagamento.getSelectedItem().toString()),
                            "" + itemPedido.getTotal(),// + aux.converterValores(aux.soNumeros(txtValorFormaPagamento.getText().toString())),
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
                        aux.getIdFormaPagamento(spFormasPagamento.getSelectedItem().toString()),
                        "" + aux.converterValores(aux.soNumeros(txtValorFormaPagamento.getText().toString())),
                        authorizationCode,
                        cardBrand,
                        nsu,
                        "",
                        "0"
                ));
            }

            //
            listaFinanceiroCliente = bd.getFinanceiroCliente(idTemp);
            adapter = new FormasPagamentoPedidosAdapter(this, listaFinanceiroCliente, elementos, idTemp, bd);
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
    }

    void atualizarListaFormPag() {
        //
        bd.deleteFormPagPIX();

        //
        listaFinanceiroCliente = bd.getFinanceiroCliente(idTemp);
        adapter = new FormasPagamentoPedidosAdapter(this, listaFinanceiroCliente, elementos, idTemp, bd);
        rvFinanceiro.setAdapter(adapter);

        //
        String tif = aux.maskMoney(new BigDecimal(bd.getValorTotalFinanceiro(String.valueOf(idTemp), api_asaas)));
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

    private void AddFormaPagamentoPIX(String authorizationCode, String cardBrand, String nsu) {

        String val = aux.soNumeros(txtValorFormaPagamento.getText().toString());

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
        adapter = new FormasPagamentoPedidosAdapter(this, listaFinanceiroCliente, elementos, idTemp, bd);
        rvFinanceiro.setAdapter(adapter);

        //
        String tif = aux.maskMoney(new BigDecimal(bd.getValorTotalFinanceiro(String.valueOf(idTemp), api_asaas)));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        produtosPedidoAdapter.unregisterObserver(this);
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

    private void ValidarCampFormPedido() {
        //ESCODER O TECLADO
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Log.i("Valor", String.valueOf(aux.converterValores(etPreco.getText().toString())));

        String valEtPreco = "";
        if (!etPreco.getText().toString().equals("")) {
            valEtPreco = String.valueOf(aux.converterValores(etPreco.getText().toString()));
        }

        String valEtDesconto = "";
        if (!etDesconto.getText().toString().equals("")) {
            valEtDesconto = String.valueOf(aux.converterValores(etDesconto.getText().toString()));
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

            if (calcularDescto) {

                if (!precoMinimo.equals("0.0")) {
                    //Objects.requireNonNull(getSupportActionBar()).setSubtitle("Preço Mín. " + aux.maskMoney(new BigDecimal(precoMinimo)) + " | Max. " + aux.maskMoney(new BigDecimal(precoMaximo)));

                    String _valUnitario = String.valueOf(aux.converterValores(etPreco.getText().toString()));
                    String _valDesconto = String.valueOf(aux.converterValores(etDesconto.getText().toString()));
                    String[] desc = {_valUnitario, _valDesconto};

                    if (aux.subitrair(desc).floatValue() > Float.parseFloat(precoMaximo) || aux.subitrair(desc).floatValue() < Float.parseFloat(precoMinimo)) {
                        aux.ShowMsgToast(this, "O valor informado não pode ultrapassar o preço mínimo ou máximo!");

                        return;
                    } else {
                        aux.ShowMsgToast(this, "Menor: " + aux.subitrair(desc).toString() + " | " + Float.parseFloat(precoMaximo));
                    }
                }
            }

            String[] ars = {precoMinimo, String.valueOf(aux.converterValores(etPreco.getText().toString()))};
            int vComp = aux.comparar(ars);
            //
            String[] arsMax = {precoMaximo, String.valueOf(aux.converterValores(etPreco.getText().toString()))};
            int vCompMax = aux.comparar(arsMax);

            // Verifica a quantidade máxima permeitida
            /*if (Integer.parseInt(etQuantidade.getText().toString()) > 1) {
                //ShowMsgToast("Quantidade Máxima 100 Unidades.");
                etDesconto.setText("0,00");
                etDesconto.setEnabled(false);
            } else {
                etDesconto.setText("0,00");
                etDesconto.setEnabled(true);
            }*/

            // Verifica se o valor informado é igual ou maior ao preço minimo
            //else
            if (!precoMinimo.equalsIgnoreCase("0.0")) {

                if (vComp > 0) {
                    ShowMsgToast("O Valor não pode ser menor que o preço mínimo!");
                } else if (vCompMax == -1) {
                    ShowMsgToast("O Valor não pode ser maior que o preço máximo!");
                } else {
                    //formsView();
                    AddProdutoPedido();
                }
            } else {
                //formsView();
                AddProdutoPedido();
            }
        }
    }

    //
    private void AddProdutoPedido() {
        ProdutosPedidoDomain produto = new ProdutosPedidoDomain();

        //
        String valorUnit = String.valueOf(aux.converterValores(etPreco.getText().toString()));

        //MULTIPLICA O VALOR PELA QUANTIDADE
        String[] multiplicar = {valorUnit, etQuantidade.getText().toString()};
        String total = String.valueOf(aux.multiplicar(multiplicar));

        String valorDesc = String.valueOf(aux.converterValores(etDesconto.getText().toString()));
        String[] multiplicarDesc = {valorDesc, etQuantidade.getText().toString()};
        String desconto = String.valueOf(aux.multiplicar(multiplicarDesc));

        String[] subtrair = {total, desconto};
        total = String.valueOf(aux.subitrair(subtrair));

        produto.id_pedido = String.valueOf(idTemp);
        produto.produto = spProduto.getSelectedItem().toString();
        produto.quantidade = etQuantidade.getText().toString();
        produto.valor = valorUnit;
        produto.total = total;
        produto.desconto = valorDesc;
        bd.addProdutoPedido(produto);

        etDesconto.setText("0,00");

        //
        atualizarListaProdutos();
    }

    void atualizarListaProdutos() {
        findViewById(R.id.msgErroFracionar).setVisibility(View.GONE);

        //
        listaProdutosPedido = bd.getProdutosPedido(idTemp);
        produtosPedidoAdapter = new ProdutosPedidoAdapter(this, listaProdutosPedido, bd);
        // Registra a Activity como Observador
        produtosPedidoAdapter.registerObserver(this);
        rvProdutosPedido.setAdapter(produtosPedidoAdapter);

        spProduto.setSelection(0);
        etQuantidade.setText("");
        etPreco.setText("0,00");
    }

    private void formsView() {

        /*// Verifica a quantidade máxima permeitida
        if (quantidade > 5) {
            ShowMsgToast("Maior que 5");
            txtValorFormaPagamento.setEnabled(false);
            spFormasPagamento.setAdapter(null);
            spFormasPagamento.setAdapter(adapterFormasPagamentoDinheiro);
        }*/

        //
        fracionar();

        // Verifica a quantidade máxima permeitida
        if (NotaFracionada.equals("1")) {
            txtValorFormaPagamento.setEnabled(false);
            spFormasPagamento.setAdapter(null);
            spFormasPagamento.setAdapter(adapterFormasPagamentoDinheiro);
        }

        txtCpfCnpjCli.setText(String.format("Cpf/Cnpj: %s", cpf_cnpj_cliente.getText().toString()));
        String total = bd.getValorTotalPedido(String.valueOf(idTemp), String.valueOf(aux.converterValores(etDesconto.getText().toString())));
        //
        txtTotalFinanceiro.setText(aux.maskMoney(new BigDecimal(total)));
        txtValorFormaPagamento.setText(aux.maskMoney(new BigDecimal(total)));
        //
        formVenda.setVisibility(View.GONE);
        formFinanceiroPedido.setVisibility(View.VISIBLE);
    }

    private void VerificarCamposIniciarPedido(boolean pagamento) {

        //ESCODER O TECLADO
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception ignored) {
        }

        //
        if (!compararValorRestante()) return;

        String valEtPreco = "";
        if (!etPreco.getText().toString().equals("")) {
            valEtPreco = String.valueOf(aux.converterValores(etPreco.getText().toString()));
        }

        //
        if (etPreco.getText().toString().equals("")
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

        if (!bd.getPedidosTransmitirFecharDia().isEmpty()) {
            // APAGA TODOS OS PAGAMENTOS PIX COM STATUS 1
            bd.deleteFormPagPIX();

            //
            Intent i = new Intent(getBaseContext(), ConfirmarDadosPedido.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("cpfCnpj_cliente", cpf_cnpj_cliente.getText().toString());
            i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());
            i.putExtra("produto", spProduto.getSelectedItem().toString());
            i.putExtra("qnt", quantidade);
            i.putExtra("vlt", aux.converterValores(txtTotalItemFinanceiro.getText().toString()));
            i.putExtra("credenciadora", idCredenciadoras.get(spDescricaoCredenciadora.getSelectedItemPosition()));//spDescricaoCredenciadora.getSelectedItem().toString()
            i.putExtra("bandeira", aux.getIdBandeira(spBandeiraCredenciadora.getSelectedItem().toString()));
            i.putExtra("cod_aut", etCodAutorizacao.getText().toString());
            i.putExtra("nsu", etNsuCeara.getText().toString());
            i.putExtra("desconto", etDesconto.getText().toString());

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
                                .id(3)//,

                        // BOTAO NOVO PEDIDO
                        /*TapTarget.forView(findViewById(R.id.fabAvancar), "Avançar", sassyDesc3)
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.colorAccent)
                                .targetCircleColor(android.R.color.white)
                                .transparentTarget(true)
                                .textColor(android.R.color.white)
                                .id(3)*/
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
                        final AlertDialog dialog = new AlertDialog.Builder(FormPedidosCopiaSeguranca.this)
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

    @Override
    public void onProdutosPedidoChanged() {
        runOnUiThread(this::atualizarListaProdutos);
        runOnUiThread(this::atualizarListaFormPag);
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
        alerta = builder.create();
        alerta.show();
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

                AddFormaPagamento(data.getStringExtra("authorizationCode"), aux.getIdBandeira(data.getStringExtra("cardBrand")), data.getStringExtra("nsu"));
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                ShowMsgToast("Operação Cancelada!");
            }
        }
        if (requestCode == PAGAMENTO_PIX_REQUEST) {

            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                // AddFormaPagamentoPIX("", "", "");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                ShowMsgToast("Operação Cancelada!");
            }
        }
    }

    // Fracionar o pedido caso a quantidade seja maior que 5
    boolean dd = false;

    public void fracionar() {
        //if (_fracionar) {
        if (bd.getQuantProdutosPedidoNCMGas(idTemp) > 0) {
            if (quantidade > 5) {
                Random r = new Random();
                random = r.nextInt(6 - 1) + 1;
                NotaFracionada = "1";
            } else {
                random = quantidade;
            }
        } else {
            List<String> listPro = bd.getQuantProdutosPedidoNCM(idTemp);
            String[] iList = listPro.get(0).split(",");
            List<String> minMaxFrac = bd.getMinMaxFracionamentoProduto(iList[0]);
            if (!minMaxFrac.get(0).equals("0") && !minMaxFrac.get(1).equals("0")) {
                int min = Integer.parseInt(minMaxFrac.get(0));
                int max = Integer.parseInt(minMaxFrac.get(1));
                if (quantidade > max) {
                    Random r = new Random();
                    random = r.nextInt(max - min) + 1;
                    NotaFracionada = "1";
                } else {
                    random = quantidade;
                }
                //Toast.makeText(this, "Fracionar produto diferente de gás", Toast.LENGTH_SHORT).show();
            } else {
                random = quantidade;
            }
        }

        /*} else {
            random = quantidade;
        }*/

        String[] sub = {String.valueOf(quantidade), String.valueOf(random)};
        quantidade = aux.subitrair(sub).intValue();

        //
        String data = aux.exibirDataAtual();
        String hora = aux.horaAtual();

        if (!dd) {
            id = idTemp;
            dd = true;
        } else {
            ed.putInt("id_pedido", (Integer.parseInt(bd.getUltimoIdPedidos()) + 1)).apply();
            id = prefs.getInt("id_pedido", 1);
        }

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
                "" + bd.getValorTotalPedido(String.valueOf(id), etDesconto.getText().toString()),/// aux.converterValores(aux.soNumeros(total)),//VALOR TOTAL
                dataProtocolo,//DATA PROTOCOLO - "28042017"
                horaProtocolo,//HORA PROTOCOLO - "151540"
                cpf,//CPF/CNPJ CLIENTE
                FPagamento,//FORMA PAGAMENTO
                "",
                String.valueOf(idTemp), //bd.getUltimoIdPedido()
                NotaFracionada,
                spDescricaoCredenciadora.getSelectedItem().toString()
        ));

        for (ProdutosPedidoDomain produto : bd.getProdutosPedido(idTemp)) {
            //
            String _quant = "0";
            if (NotaFracionada.equals("1")) {
                _quant = quantidade;
            } else {
                _quant = produto.quantidade;
            }
            bd.addItensPedidos(new ItensPedidos(
                    "" + id,//ID PEDIDO
                    "" + produto.id_produto,
                    "" + _quant,// produto.quantidade,
                    "" + produto.valor,
                    "" + aux.converterValores(aux.soNumeros(etDesconto.getText().toString())),
                    "" // + aux.converterValores(aux.soNumeros(etDesconto.getText().toString()))
            ));
            String[] mValor = {produto.valor, _quant};
            String vTotal = String.valueOf(aux.multiplicar(mValor));
            bd.updateValorPedido(String.valueOf(id), vTotal);

            /*
            //
            bd.addItensPedidos(new ItensPedidos(
                    "" + id,//ID PEDIDO
                    "" + bd.getIdProduto(spProduto.getSelectedItem().toString()),
                    "" + quantidade,
                    "" + aux.converterValores(etPreco.getText().toString()),//aux.soNumeros(String.valueOf(aux.converterValores(etPreco.getText().toString()))),
                    "" + aux.converterValores(aux.soNumeros(etDesconto.getText().toString())),
                    "" // + aux.converterValores(aux.soNumeros(etDesconto.getText().toString()))
            ));
            */
        }
    }
}
