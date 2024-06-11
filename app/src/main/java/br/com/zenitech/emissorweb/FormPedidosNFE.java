package br.com.zenitech.emissorweb;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.emissorweb.adapters.ClientesNfeAdapter;
import br.com.zenitech.emissorweb.adapters.ProdutosPedidoAdapter;
import br.com.zenitech.emissorweb.adapters.ProdutosPedidoNFeAdapter;
import br.com.zenitech.emissorweb.domains.ClientesNFE;
import br.com.zenitech.emissorweb.domains.DomainPrincipal;
import br.com.zenitech.emissorweb.domains.ProdutosPedidoDomain;
import br.com.zenitech.emissorweb.interfaces.IPrincipal;
import br.com.zenitech.emissorweb.interfaces.IProdutosPedidoObserver;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormPedidosNFE extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        IProdutosPedidoObserver {

    private String TAG = "FormPedidosNFE";
    String[] listaFormasPagamento = {"DINHEIRO"};

    private Spinner spProduto, spFormasPagamento;
    private EditText cpf_cnpj_cliente, nome_cliente, etQuantidade, etPreco, etDocumento;
    private DatabaseHelper bd;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;
    AlertDialog alerta;

    private String precoMinimo, precoMaximo, idTemp;
    private ClassAuxiliar aux;
    private Context context;
    private int quant = 0;

    RecyclerView rvClienteNFE, rvProdutosPedido;
    ClientesNfeAdapter adapter;
    public static LinearLayoutCompat llClienteNFE, llIdNomeCli;
    Button btnAddProdutoLista;

    // LISTA PRODUTOS
    private ArrayList<ProdutosPedidoDomain> listaProdutosPedido;
    ProdutosPedidoNFeAdapter produtosPedidoAdapter;

    public static String idCli = "";
    public static TextView txtNomeCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_pedidos_nfe);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //
        context = this;
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();

        prefs.edit().putString("nfeDocumento", "").apply();
        //
        bd = new DatabaseHelper(this);
        aux = new ClassAuxiliar();

        // APAGA OS PRODUTOS DO PEDIDO ANTERIOR PARA INICIAR UM NOVO
        bd.resetProdutosPedidoNFe();


        txtNomeCliente = findViewById(R.id.txtNomeCliente);
        nome_cliente = findViewById(R.id.nome_cliente);
        rvClienteNFE = findViewById(R.id.rvClienteNFE);
        rvClienteNFE.setLayoutManager(new LinearLayoutManager(context));
        rvClienteNFE.setNestedScrollingEnabled(false);

        llClienteNFE = findViewById(R.id.llClienteNFE);
        llIdNomeCli = findViewById(R.id.llIdNomeCli);
        //
        rvProdutosPedido = findViewById(R.id.rvProdutosPedido);
        rvProdutosPedido.setLayoutManager(new LinearLayoutManager(this));

        etDocumento = findViewById(R.id.etDocumento);


        //
        //LISTA DE PRODUTOS
        ArrayList<String> listaProdutos = bd.getProdutos();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaProdutos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProduto = findViewById(R.id.spProdutos);
        spProduto.setAdapter(adapter);
        spProduto.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).toString().equals("PRODUTO")) {

                    //
                    double precoMinProd = bd.getPrecoMinimoProduto(parent.getItemAtPosition(position).toString());
                    precoMinimo = String.valueOf(precoMinProd);

                    //
                    double precoMaxProd = bd.getPrecoMaximoProduto(parent.getItemAtPosition(position).toString());
                    precoMaximo = String.valueOf(precoMaxProd);

                    if (!precoMinimo.equals("0.0")) {
                        Objects.requireNonNull(getSupportActionBar()).setSubtitle("Preço Mín. " + aux.maskMoney(new BigDecimal(precoMinimo)) + " - Máx. " + aux.maskMoney(new BigDecimal(precoMaximo)));
                    } else {
                        Objects.requireNonNull(getSupportActionBar()).setSubtitle("");
                    }

                    quant = bd.getQuantProdutoRemessa(parent.getItemAtPosition(position).toString());
                    Toast.makeText(getBaseContext(), "" + bd.getQuantProdutoRemessa(parent.getItemAtPosition(position).toString()), Toast.LENGTH_LONG).show();

                    etPreco.setText(aux.maskMoney(new BigDecimal(bd.getPrecoProduto(parent.getItemAtPosition(position).toString()))));
                } else {
                    etPreco.setText(aux.maskMoney(new BigDecimal("0")));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnAddProdutoLista = findViewById(R.id.btnAddProdutoLista);

        // ADD PRODUTO AO PEDIDO
        btnAddProdutoLista.setOnClickListener(v -> ValidarCampFormPedido());

        /*ArrayAdapter adapterFormasPagamento = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaFormasPagamento);
        adapterFormasPagamento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFormasPagamento = findViewById(R.id.spFormasPagamento);
        spFormasPagamento.setAdapter(adapterFormasPagamento);*/

        //
        cpf_cnpj_cliente = findViewById(R.id.cpf_cnpj_cliente);
        cpf_cnpj_cliente.setText("");
        //cpf_cnpj_cliente.addTextChangedListener(MaskUtil.insert(cpf_cnpj_cliente, MaskUtil.MaskType.AUTO));

        //
        etQuantidade = findViewById(R.id.etQuantidade);
        etQuantidade.setText("");

        //
        etPreco = findViewById(R.id.etPreco);
        etPreco.addTextChangedListener(new FormPedidosNFE.MoneyTextWatcher(etPreco));

        etPreco.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {

                VerificarCamposIniciarPedido();

                handled = true;
            }
            return handled;
        });

        findViewById(R.id.btnConsultarCliNFE).setOnClickListener(v -> ConsultarCodCliente());

        findViewById(R.id.btn_finalizar).setOnClickListener(v -> VerificarCamposIniciarPedido());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> VerificarCamposIniciarPedido());

        findViewById(R.id.btnPagamentoForm).setOnClickListener(view -> VerificarCamposIniciarPedido());
    }

    @Override
    protected void onResume() {
        super.onResume();


        atualizarListaProdutos();
    }

    private void ConsultarCodCliente() {
        //ESCODER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (!nome_cliente.getText().toString().equalsIgnoreCase("")) {

            final IPrincipal iPrincipal = IPrincipal.retrofit.create(IPrincipal.class);

            final Call<ArrayList<ClientesNFE>> call = iPrincipal.consultarCliNome(
                    "2003",
                    prefs.getString("serial_app", ""),
                    nome_cliente.getText().toString());

            call.enqueue(new Callback<ArrayList<ClientesNFE>>() {
                @Override
                public void onResponse(@NonNull Call<ArrayList<ClientesNFE>> call, @NonNull Response<ArrayList<ClientesNFE>> response) {

                    //
                    final ArrayList<ClientesNFE> clientesNFES = response.body();
                    if (clientesNFES != null) {

                        adapter = new ClientesNfeAdapter(context, clientesNFES);
                        rvClienteNFE.setAdapter(adapter);
                        llClienteNFE.setVisibility(View.VISIBLE);


                        findViewById(R.id.btnConsultarCliNFE).setVisibility(View.GONE);
                        findViewById(R.id.llFormNFE).setVisibility(View.VISIBLE);

                        /*if (principal.getErro().equalsIgnoreCase("OK")) {
                            TextView txtNomeCliente = findViewById(R.id.txtNomeCliente);
                            txtNomeCliente.setText(principal.getNome_cliente());
                            findViewById(R.id.btnConsultarCliNFE).setVisibility(View.GONE);
                            findViewById(R.id.llFormNFE).setVisibility(View.VISIBLE);

                            //Toast.makeText(context, "Cliente encontrado", Toast.LENGTH_LONG).show();
                            //
                            //runOnUiThread(() -> startActivity(new Intent(getBaseContext(), FormPedidosNFE.class)));
                        } else {
                            Toast.makeText(context, "Não conseguimos encontrar nenhum cliente com esse id.", Toast.LENGTH_LONG).show();
                        }*/
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ArrayList<ClientesNFE>> call, @NonNull Throwable t) {
                    //Log.i(TAG, Objects.requireNonNull(t.getMessage()));

                    Toast.makeText(getBaseContext(), "Não conseguimos encontrar nenhum cliente com esse id.", Toast.LENGTH_LONG).show();
                }
            });
        } else {

            //
            final IPrincipal iPrincipal = IPrincipal.retrofit.create(IPrincipal.class);

            final Call<DomainPrincipal> call = iPrincipal.consultarCli(
                    "601",
                    prefs.getString("serial_app", ""),
                    cpf_cnpj_cliente.getText().toString());

            call.enqueue(new Callback<DomainPrincipal>() {
                @Override
                public void onResponse(@NonNull Call<DomainPrincipal> call, @NonNull Response<DomainPrincipal> response) {

                    //
                    final DomainPrincipal principal = response.body();
                    if (principal != null) {

                        if (principal.getErro().equalsIgnoreCase("OK")) {
                            idCli = cpf_cnpj_cliente.getText().toString();
                            txtNomeCliente.setText(principal.getNome_cliente());
                            findViewById(R.id.btnConsultarCliNFE).setVisibility(View.GONE);
                            llIdNomeCli.setVisibility(View.GONE);
                            findViewById(R.id.llFormNFE).setVisibility(View.VISIBLE);

                            //Toast.makeText(context, "Cliente encontrado", Toast.LENGTH_LONG).show();
                            //
                            //runOnUiThread(() -> startActivity(new Intent(getBaseContext(), FormPedidosNFE.class)));
                        } else {
                            Toast.makeText(context, "Não conseguimos encontrar nenhum cliente com esse id.", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DomainPrincipal> call, @NonNull Throwable t) {
                    //Log.i(TAG, Objects.requireNonNull(t.getMessage()));

                    Toast.makeText(getBaseContext(), "Não conseguimos encontrar nenhum cliente com esse id.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void VerificarCamposIniciarPedido() {
        confirmar();

        //ESCODER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        //
        /*if (spFormasPagamento.getSelectedItem().toString().equals("FORMA PAGAMENTO")) {
            Toast.makeText(getBaseContext(), "Selecione a forma de pagamento.", Toast.LENGTH_LONG).show();
        } else */
        /*if (spProduto.getSelectedItem().toString().equals("PRODUTO")) {
            Toast.makeText(getBaseContext(), "Selecione um produto.", Toast.LENGTH_LONG).show();
        } else if (etQuantidade.getText().toString().equals("") || etQuantidade.getText().toString().equals("0")) {
            Toast.makeText(getBaseContext(), "Informe a quantidade.", Toast.LENGTH_LONG).show();
        } else if (Integer.parseInt(etQuantidade.getText().toString()) > quant) {
            Toast.makeText(getBaseContext(), "Restam apenas " + quant + " itens. Diminua a quantidade!", Toast.LENGTH_LONG).show();
        } else if (etPreco.getText().toString().equals("")) {
            Toast.makeText(getBaseContext(), "Informe o valor unitário.", Toast.LENGTH_LONG).show();
        } else {


            //int vComp = Double.compare(Double.parseDouble(), precoMinimo);

            String[] ars = {precoMinimo, String.valueOf(aux.converterValores(etPreco.getText().toString()))};
            int vComp = aux.comparar(ars);
            //
            String[] arsMax = {precoMaximo, String.valueOf(aux.converterValores(etPreco.getText().toString()))};
            int vCompMax = aux.comparar(arsMax);

            // Verifica a quantidade máxima permeitida
            if (Integer.parseInt(etQuantidade.getText().toString()) > 300) {
                Toast.makeText(getBaseContext(), "Quantidade Máxima 300 Unidades.", Toast.LENGTH_LONG).show();
            }
            // Verifica se o valor informado é igual ou maior ao preço minimo
            else if (!precoMinimo.equalsIgnoreCase("0.0")) {
                if (vComp > 0) {
                    ShowMsgToast("O Valor não pode ser menor que o preço mínimo!");
                } else if (vCompMax == -1) {
                    ShowMsgToast("O Valor não pode ser maior que o preço máximo!");
                } else {
                    confirmar();
                }
            } else {
                confirmar();
            }
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

            if (!precoMinimo.equalsIgnoreCase("0.0")) {

                if (vComp > 0) {
                    ShowMsgToast("O Valor não pode ser menor que o preço mínimo!");
                } else if (vCompMax == -1) {
                    ShowMsgToast("O Valor não pode ser maior que o preço máximo!");
                } else {
                    AddProdutoPedido();
                }
            } else {
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

        /*String valorDesc = String.valueOf(aux.converterValores(etDesconto.getText().toString()));
        String[] multiplicarDesc = {valorDesc, etQuantidade.getText().toString()};
        String desconto = String.valueOf(aux.multiplicar(multiplicarDesc));

        String[] subtrair = {total, desconto};
        total = String.valueOf(aux.subitrair(subtrair));*/

        produto.id_pedido = "1";
        produto.produto = spProduto.getSelectedItem().toString();
        produto.quantidade = etQuantidade.getText().toString();
        produto.valor = valorUnit;
        produto.total = total;
        //produto.desconto = valorDesc;
        bd.addProdutoPedidoNFe(produto);

        //
        atualizarListaProdutos();
    }

    void atualizarListaProdutos() {

        //
        listaProdutosPedido = bd.getProdutosPedidoNFe("1");
        produtosPedidoAdapter = new ProdutosPedidoNFeAdapter(this, listaProdutosPedido, bd);
        // Registra a Activity como Observador
        produtosPedidoAdapter.registerObserver(this);
        rvProdutosPedido.setAdapter(produtosPedidoAdapter);

        spProduto.setSelection(0);
        etQuantidade.setText("");
        etPreco.setText("0,00");
    }

    private void ShowMsgToast(String msg) {
        Toast toast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void confirmar() {
        prefs.edit().putString("nfeDocumento", etDocumento.getText().toString()).apply();
        //Intent i = new Intent(getBaseContext(), ConfirmarDadosPedidoNFE.class);
        Intent i = new Intent(getBaseContext(), FinanceiroNFe.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("cpfCnpj_cliente", idCli);
        //i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());
        i.putExtra("produto", spProduto.getSelectedItem().toString());
        i.putExtra("qnt", etQuantidade.getText().toString());
        i.putExtra("vlt", etPreco.getText().toString());

        startActivity(i);
        finish();
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
                finish();
                break;
            case R.id.action_infor_novo_pedido:
                mostrarMsg();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
    public void onProdutosPedidoChanged() {
        runOnUiThread(this::atualizarListaProdutos);
    }

}
