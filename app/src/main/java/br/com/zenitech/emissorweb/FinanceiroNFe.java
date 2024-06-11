package br.com.zenitech.emissorweb;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.lvrenyang.io.Pos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.emissorweb.adapters.FinanceiroNFeAdapter;
import br.com.zenitech.emissorweb.domains.FinanceiroNFeDomain;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.interfaces.IFinanceiroNFeObserver;
import br.com.zenitech.emissorweb.pagamentos.PagamentoPix;

public class FinanceiroNFe extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        IFinanceiroNFeObserver {

    static final int PAGAMENTO_REQUEST = 1;
    static final int PAGAMENTO_PIX_REQUEST = 2;
    //
    DatabaseHelper bd;
    SharedPreferences prefs;
    ClassAuxiliar aux;
    Configuracoes config;

    // ADAPTERS
    FinanceiroNFeAdapter financeiroNFeAdapter;
    LinearLayout llParcelasDuplicata;
    //
    ArrayList<Unidades> listUnidades;
    Unidades unidades;
    ArrayList<PosApp> listPos;
    PosApp pos;
    private TextInputLayout TiNsuCeara;
    EditText etCodAutorizacao, etNsuCeara;

    //
    ArrayList<FinanceiroNFeDomain> listaFinanceiroNFe;
    private RecyclerView rvFinanceiro;
    Spinner spFormasPagamento, spDescricaoCredenciadora, spBandeiraCredenciadora, spParcelas;
    LinearLayout bgTotal, llCredenciadora, llParcelas, formFinanceiroPedido;

    // BTNs **
    Button btnAddF, btnPagamento, btnPagCartao;

    //
    TextView txtValTotalPagar, txtTotalPago;
    EditText txtVencimentoFormaPagamento, txtValorFormaPagamento;
    TextInputLayout tilVencimento;
    TextView txtTotalFinanceiro;
    TextView txtTotalItemFinanceiro;
    boolean api_asaas = false;
    int idTemp;

    String cpfCnpj_cliente, formaPagamento, produto, qnt, vlt, vltTotal,
            statusNota, protocoloNota, dataHoraNota;

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
    private boolean mostrarFpgDuplicata;
    private Spinner spParcelasDuplicata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financeiro_nfe);

        //
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        bd = new DatabaseHelper(this);
        aux = new ClassAuxiliar();
        config = new Configuracoes();
        idTemp = 1;
        mostrarFpgDuplicata = false;

        // APAGA O FINANCEIRO ANTERIOR PARA INICIAR UM NOVO
        bd.resetFinanceiroNFe();

        // Unidades
        listUnidades = bd.getUnidades();
        unidades = listUnidades.get(0);
        if (!unidades.getApi_key_asaas().equalsIgnoreCase("")) {
            api_asaas = true;
        }
        listPos = bd.getPos();
        pos = listPos.get(0);

        try {
            if (pos.getDuplicatas_notas_fiscais().equals("1")) {
                mostrarFpgDuplicata = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //
        rvFinanceiro = findViewById(R.id.rvFinanceiro);
        rvFinanceiro.setLayoutManager(new LinearLayoutManager(this));

        //
        bgTotal = findViewById(R.id.bgTotal);

        tilVencimento = findViewById(R.id.tilVencimento);

        //
        listaFinanceiroNFe = bd.getFinanceiroNFe(1);
        financeiroNFeAdapter = new FinanceiroNFeAdapter(this, listaFinanceiroNFe);

        // Registra a Activity como Observador
        financeiroNFeAdapter.registerObserver(this);
        //rvFinanceiro.setAdapter(financeiroNFeAdapter);
        llParcelasDuplicata = findViewById(R.id.llParcelasDuplicata);

        // FORMAS DE PAGAMENTO
        ArrayAdapter<String> adapterFormasPagamento = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aux.FormasDePagamentoEmissorNFe(mostrarFpgDuplicata));
        adapterFormasPagamento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFormasPagamento = findViewById(R.id.spFormasPagamentoCliente);
        spFormasPagamento.setAdapter(adapterFormasPagamento);
        spFormasPagamento.setOnItemSelectedListener(this);

        llParcelas = findViewById(R.id.llParcelas);
        llCredenciadora = findViewById(R.id.llCredenciadora);

        //
        TiNsuCeara = findViewById(R.id.TiNsuCeara);
        etNsuCeara = findViewById(R.id.etNsuCeara);

        //
        etCodAutorizacao = findViewById(R.id.etCodAutorizacao);

        txtValorFormaPagamento = findViewById(R.id.txtValorFormaPagamento);
        txtValorFormaPagamento.addTextChangedListener(new ClassAuxiliar.MoneyTextWatcher(txtValorFormaPagamento));

        txtVencimentoFormaPagamento = findViewById(R.id.txtVencimentoFormaPagamento);
        //
        btnAddF = findViewById(R.id.btnAddF);
        btnAddF.setOnClickListener(v -> {
            //SE O USUÁRIO NÃO ADICIONAR NENHUM VALOR
            if (txtValorFormaPagamento.getText().toString().equals("") || txtValorFormaPagamento.getText().toString().equals("R$0,00")) {
                ShowMsgToast("Adicione uma valor para esta forma de pagamento.");
            } else {
                AddFormaPagamento(etCodAutorizacao.getText().toString(), aux.getIdBandeira(spBandeiraCredenciadora.getSelectedItem().toString()), etNsuCeara.getText().toString());
            }
        });
        btnPagCartao = findViewById(R.id.btnPagCartao);
        btnPagCartao.setOnClickListener(v -> VerificarCamposIniciarPedido(true));

        //
        llCredenciadora = findViewById(R.id.llCredenciadora);
        formFinanceiroPedido = findViewById(R.id.formFinanceiroPedido);
        txtTotalFinanceiro = findViewById(R.id.txtTotalFinanceiro);
        txtTotalItemFinanceiro = findViewById(R.id.txtTotalItemFinanceiro);
        idCredenciadoras = bd.getIdCredenciadora();
        listaCredenciadoras = bd.getCredenciadora();
        listaCredenciadoras.add("");
        ArrayAdapter adapterCredenciadora = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaCredenciadoras);
        adapterCredenciadora.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDescricaoCredenciadora =

                findViewById(R.id.spDescricaoCredenciadora);
        spDescricaoCredenciadora.setAdapter(adapterCredenciadora);

        //
        ArrayAdapter adapterBandeiraCredenciadora = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaBandeirasCredenciadoras);
        adapterBandeiraCredenciadora.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBandeiraCredenciadora = findViewById(R.id.spBandeiraCredenciadora);
        spBandeiraCredenciadora.setAdapter(adapterBandeiraCredenciadora);

        // PARCELAS
        ArrayAdapter adapterParcelas = new ArrayAdapter(this, android.R.layout.simple_spinner_item, aux.ParcelasCartaoCredito());
        adapterParcelas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spParcelas = findViewById(R.id.spParcelas);
        spParcelas.setAdapter(adapterParcelas);

        // PARCELAS DUPLICATAS
        ArrayAdapter adapterParcelasDuplicata = new ArrayAdapter(this, android.R.layout.simple_spinner_item, aux.ParcelasDuplicatas());
        adapterParcelasDuplicata.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spParcelasDuplicata = findViewById(R.id.spParcelasDuplicata);
        spParcelasDuplicata.setAdapter(adapterParcelasDuplicata);

        // RECEBE AS INFORMAÇÕES DO FORMULÁRIO
        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {

                cpfCnpj_cliente = params.getString("cpfCnpj_cliente");
                /*formaPagamento = params.getString("formaPagamento");
                produto = params.getString("produto");
                qnt = params.getString("qnt");

                vlt = params.getString("vlt");

                vltTotal = String.valueOf(
                        aux.multiplicar(
                                new String[]{
                                        params.getString("qnt"),
                                        String.valueOf(aux.converterValores(params.getString("vlt")))
                                }
                        )
                );

                txtTotalFinanceiro.setText(aux.maskMoney(new BigDecimal(vltTotal)));*/

                String vltTotal = bd.getValorTotalPedidoNFe("1");
                txtTotalFinanceiro.setText(aux.maskMoney(new BigDecimal(vltTotal)));
            }
        }
        //
        btnPagamento = findViewById(R.id.btnPagamento);
        btnPagamento.setOnClickListener(v ->

        {
            if (txtTotalItemFinanceiro.getText().equals("0,00")) {
                //
                Toast.makeText(this, "Adicione pelo menos uma forma de pagamento ao financeiro.", Toast.LENGTH_LONG).show();
            } else if (!aux.soNumeros((String) txtTotalItemFinanceiro.getText()).equals(aux.soNumeros((String) txtTotalFinanceiro.getText()))) {
                //
                Toast.makeText(this, "O valor do financeiro está diferente da venda.", Toast.LENGTH_LONG).show();
            } else {
                confirmar();
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        atualizarListaFormPag();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        financeiroNFeAdapter.unregisterObserver(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String str = parent.getItemAtPosition(position).toString();
        SelectPagamentoCartao(str);
        ShowMsgToast(str);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    void SelectPagamentoCartao(String FormPG) {
        boolean cartaoCredito = FormPG.equalsIgnoreCase("CARTÃO DE CRÉDITO");
        boolean cartaoDebito = FormPG.equalsIgnoreCase("CARTÃO DE DÉBITO");
        boolean duplicata = FormPG.equalsIgnoreCase("DUPLICATA MERCANTIL");
        boolean boleto = FormPG.equalsIgnoreCase("BOLETO");

        tilVencimento.setVisibility(View.GONE);
        llParcelasDuplicata.setVisibility(View.GONE);

        if (duplicata) {
            tilVencimento.setVisibility(View.VISIBLE);
            llParcelasDuplicata.setVisibility(View.VISIBLE);
            txtVencimentoFormaPagamento.setText(aux.dataFutura(1));
        }

        if (boleto) {
            tilVencimento.setVisibility(View.VISIBLE);
            llParcelasDuplicata.setVisibility(View.GONE);
            spParcelas.setSelection(0);
            txtVencimentoFormaPagamento.setText(aux.dataFutura(5));
        }

        // SE EXISTIR STONE CODE OU OUTRO MEIO DE PAGAMENTO COM A MAQUININHA
        boolean stoneCode = !unidades.getCodloja().equalsIgnoreCase("");
        if (stoneCode) {
            if (cartaoCredito || cartaoDebito) {
                llCredenciadora.setVisibility(View.GONE);

                //
                btnAddF.setVisibility(View.GONE);
                btnPagCartao.setVisibility(View.VISIBLE);
            } else {
                //
                btnAddF.setVisibility(View.VISIBLE);
                btnPagCartao.setVisibility(View.GONE);
            }
        }

        // SE NÃO EXISTIR OUTRO MEIO DE PAGAMENTO POR MAQUININHA
        else {
            if (cartaoCredito || cartaoDebito) {
                llCredenciadora.setVisibility(View.VISIBLE);
                if (unidades.getUf().equalsIgnoreCase(new Configuracoes().GetUFCeara())) {
                    TiNsuCeara.setVisibility(View.VISIBLE);
                } else {
                    TiNsuCeara.setVisibility(View.GONE);
                }

                //
                btnAddF.setVisibility(View.VISIBLE);
                btnPagCartao.setVisibility(View.GONE);
            } else {
                //
                llCredenciadora.setVisibility(View.GONE);
                //
                btnAddF.setVisibility(View.VISIBLE);
                btnPagCartao.setVisibility(View.GONE);
            }
        }
    }

    boolean TipoPagamento(String slct, String fpg) {
        return slct.equalsIgnoreCase(fpg);
    }

    private void AddFormaPagamento(String authorizationCode, String cardBrand, String nsu) {

        FecharTeclado();

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

        //
        String _formPG = spFormasPagamento.getSelectedItem().toString();
        boolean txtFormaPagamento = TipoPagamento(_formPG, "FORMA PAGAMENTO");
        boolean cartaoCredito = TipoPagamento(_formPG, "CARTÃO DE CRÉDITO");
        boolean cartaoDebito = TipoPagamento(_formPG, "CARTÃO DE DÉBITO");
        boolean pagamentoPix = TipoPagamento(_formPG, "PAGAMENTO INSTANTÂNEO (PIX)");

        if (txtFormaPagamento) {
            ShowMsgToast("Selecione a forma de pagamento.");
        } else if (val.equalsIgnoreCase("") || val.equalsIgnoreCase("000")) {
            ShowMsgToast("Adicione um valor");
        } else if (pagamentoPix) {
            if (!cliente_id_transfeera.equalsIgnoreCase("") &&
                    !cliente_secret_transfeera.equalsIgnoreCase("") &&
                    !pix_key_transfeera.equalsIgnoreCase("")) {
                // * INICIA O PAGAMENTO PIX POR QRCODE
                iniciarPagamentoPIX();
            } else {
                // * ADICIONA O PAGAMENTO PIX NORMAL
                AddFormaPagamentoPIX();
            }
        } else {
            if (unidades.getCodloja().equalsIgnoreCase("")) {
                //
                if (cartaoCredito || cartaoDebito) {

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
            bd.addFinanceiroNFe(new FinanceiroNFeDomain(
                    "",
                    String.valueOf(idTemp), //ID PEDIDO
                    aux.getIdFormaPagamento(spFormasPagamento.getSelectedItem().toString()),
                    "" + aux.converterValores(aux.soNumeros(txtValorFormaPagamento.getText().toString())),
                    authorizationCode,
                    cardBrand,
                    nsu,
                    "",
                    "0",
                    spParcelasDuplicata.getSelectedItem().toString(),
                    txtVencimentoFormaPagamento.getText().toString()
            ));

            //
            /*listaFinanceiroNFe = bd.getFinanceiroNFe(idTemp);
            financeiroNFeAdapter = new FinanceiroNFeAdapter(this, listaFinanceiroNFe);
            financeiroNFeAdapter.registerObserver(this);
            rvFinanceiro.setAdapter(financeiroNFeAdapter);*/


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
                txtValorFormaPagamento.setText(R.string.zero_reais);
            } else {
                bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.transparente));
            }

            spFormasPagamento.setSelection(0);
            etCodAutorizacao.setText("");

            atualizarListaFormPag();
        }
    }

    void atualizarListaFormPag() {
        //
        listaFinanceiroNFe = bd.getFinanceiroNFe(idTemp);
        financeiroNFeAdapter = new FinanceiroNFeAdapter(this, listaFinanceiroNFe);
        financeiroNFeAdapter.registerObserver(this);
        rvFinanceiro.setAdapter(financeiroNFeAdapter);

        //
        String tif = aux.maskMoney(new BigDecimal(bd.getValorTotalFinanceiroNFE()));
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
            txtValorFormaPagamento.setText(R.string.zero_reais);
        } else {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.transparente));
        }
        spFormasPagamento.setSelection(0);
        etCodAutorizacao.setText("");
    }

    private void AddFormaPagamentoPIX() {

        bd.addFinanceiroNFe(new FinanceiroNFeDomain(
                "",
                String.valueOf(idTemp),
                aux.getIdFormaPagamento(spFormasPagamento.getSelectedItem().toString()),
                "" + aux.converterValores(aux.soNumeros(txtValorFormaPagamento.getText().toString())),
                "",
                "",
                "",
                "",
                "0",
                "",
                ""
        ));

        /*//
        listaFinanceiroNFe = bd.getFinanceiroNFe(idTemp);
        financeiroNFeAdapter = new FinanceiroNFeAdapter(this, listaFinanceiroNFe);
        financeiroNFeAdapter.registerObserver(this);
        rvFinanceiro.setAdapter(financeiroNFeAdapter);*/
        atualizarListaFormPag();

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
            txtValorFormaPagamento.setText(R.string.zero_reais);
        } else {
            bgTotal.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.transparente));
        }
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
    }

    void FecharTeclado() {
        //ESCODER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void VerificarCamposIniciarPedido(boolean pagamento) {
        FecharTeclado();
        //
        if (!compararValorRestante()) return;

        //
        if (spFormasPagamento.getSelectedItem().toString().equals("FORMA PAGAMENTO")) {
            ShowMsgToast("Selecione a forma de pagamento.");
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
        //
        Intent i = new Intent(getBaseContext(), ConfirmarDadosPedidoNFE.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("cpfCnpj_cliente", cpfCnpj_cliente);
        i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());
        i.putExtra("produto", produto);
        i.putExtra("qnt", qnt);
        i.putExtra("vlt", vlt);

        startActivity(i);
        finish();
    }

    private void iniciarPagamentoPIX() {
        Intent iPix = new Intent(getBaseContext(), PagamentoPix.class);
        iPix.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        iPix.putExtra("valor", txtValorFormaPagamento.getText().toString());
        iPix.putExtra("apiKey", unidades.getApi_key_asaas());
        iPix.putExtra("cliCob", unidades.getCliente_cob_asaas());
        iPix.putExtra("pedido", "" + idTemp);

        pixLauncher.launch(iPix);
    }

    private void iniciarPagamento() {

        Configuracoes configuracoes = new Configuracoes();

        Intent i;
        //
        if (configuracoes.GetDevice()) {
            i = new Intent(getBaseContext(), GerenciarPagamentoCartaoPOS.class);
        } else {
            i = new Intent(getBaseContext(), GerenciarPagamentoCartao.class);
        }

        i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());
        i.putExtra("qnt", "1");
        i.putExtra("vlt", txtValorFormaPagamento.getText().toString());

        cartaoLauncher.launch(i);
    }

    // Launcher de resultado para o pagamento PIX
    ActivityResultLauncher<Intent> pixLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Verifique se o resultado é bem-sucedido (RESULT_OK) e se os dados do resultado não são nulos
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    AddFormaPagamentoPIX();
                } else {
                    ShowMsgToast("Operação Cancelada!");
                }
            }
    );

    // Launcher de resultado para o pagamento CARTÃO
    ActivityResultLauncher<Intent> cartaoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Verifique se o resultado é bem-sucedido (RESULT_OK) e se os dados do resultado não são nulos
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();

                    etCodAutorizacao.setText(data.getStringExtra("authorizationCode"));
                    etCodAutorizacao.setEnabled(false);
                    etNsuCeara.setText(data.getStringExtra("nsu"));
                    etNsuCeara.setEnabled(false);

                    AddFormaPagamento(data.getStringExtra("authorizationCode"), aux.getIdBandeira(data.getStringExtra("cardBrand")), data.getStringExtra("nsu"));
                } else {
                    ShowMsgToast("Operação Cancelada!");
                }
            }
    );

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            voltar();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        voltar();
    }

    private void voltar() {
        if (listaFinanceiroNFe != null && listaFinanceiroNFe.size() > 0) {
            if (txtTotalItemFinanceiro.getText().equals("0,00")) {
                Toast.makeText(this, "Adicione pelo menos uma forma de pagamento ao financeiro.", Toast.LENGTH_LONG).show();
            } else if (!txtTotalItemFinanceiro.getText().equals(txtTotalFinanceiro.getText())) {
                Toast.makeText(this, "O valor do financeiro está diferente da venda.", Toast.LENGTH_LONG).show();
            } else {
                confirmar();
            }
        } else {
            finish();
        }
    }

    @Override
    public void onFinanceiroNFeChanged() {
        runOnUiThread(this::atualizarListaFormPag);
    }
}