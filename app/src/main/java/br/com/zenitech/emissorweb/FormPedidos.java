package br.com.zenitech.emissorweb;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import java.util.Objects;

import br.com.zenitech.emissorweb.domains.Unidades;
import timber.log.Timber;

public class FormPedidos extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    static final int PAGAMENTO_REQUEST = 1;

    //
    String[] listaFormasPagamento = {
            "FORMA PAGAMENTO",
            "DINHEIRO",
            "CARTÃO DE CRÉDITO",
            "CARTÃO DE DÉBITO"
    };

    private Spinner spProduto, spFormasPagamento, spDescricaoCredenciadora, spBandeiraCredenciadora;
    private EditText cpf_cnpj_cliente, etQuantidade, etPreco, etCodAutorizacao, etNsuCeara;
    private TextInputLayout TiNsuCeara;
    private DatabaseHelper bd;
    private LinearLayout llCredenciadora;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;

    private Toolbar toolbar;
    AlertDialog alerta;

    private String precoMinimo, precoMaximo;
    private ClassAuxiliar aux;

    private int quant = 0;

    ArrayList<Unidades> elementos;
    Unidades unidades;

    // BTNs **
    Button btnPagamentoCartaoNFCE, btnAvancarNFCE;

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
                    Toast.makeText(getBaseContext(), "" + quant, Toast.LENGTH_LONG).show();
                    //Toast.makeText(getBaseContext(), "" + bd.getQuantProdutoRemessa(parent.getItemAtPosition(position).toString()), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getBaseContext(), precoMinimo + "|" + precoMaximo, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> adapterFormasPagamento = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaFormasPagamento);
        adapterFormasPagamento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFormasPagamento = findViewById(R.id.spFormasPagamento);
        spFormasPagamento.setAdapter(adapterFormasPagamento);
        spFormasPagamento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                // SE EXISTIR STONE CODE OU OUTRO MEIO DE PAGAMENTO COM A MAQUININHA
                if (!unidades.getCodloja().equalsIgnoreCase("")) {
                    if (
                            (parent.getItemAtPosition(position).toString().equalsIgnoreCase("CARTÃO DE CRÉDITO") ||
                                    parent.getItemAtPosition(position).toString().equalsIgnoreCase("CARTÃO DE DÉBITO")) && !unidades.getCodloja().equalsIgnoreCase("")
                    ) {
                        llCredenciadora.setVisibility(View.GONE);
                        /*if (unidades.getUf().equalsIgnoreCase(new Configuracoes().GetUFCeara())) {
                            TiNsuCeara.setVisibility(View.VISIBLE);
                        } else {
                            TiNsuCeara.setVisibility(View.GONE);
                        }*/

                        //
                        btnAvancarNFCE.setVisibility(View.GONE);
                        btnPagamentoCartaoNFCE.setVisibility(View.VISIBLE);

                        // Retirar quando for usar o pinpad
                        //btnAvancarNFCE.setVisibility(View.VISIBLE);
                    }

                    /* else {
                        //
                        llCredenciadora.setVisibility(View.VISIBLE);
                        //
                        btnPagamentoCartaoNFCE.setVisibility(View.GONE);
                        btnAvancarNFCE.setVisibility(View.VISIBLE);
                    }*/
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
                        btnPagamentoCartaoNFCE.setVisibility(View.GONE);
                        btnAvancarNFCE.setVisibility(View.VISIBLE);

                        // Retirar quando for usar o pinpad
                        //btnAvancarNFCE.setVisibility(View.VISIBLE);
                    } else {
                        //
                        llCredenciadora.setVisibility(View.GONE);
                        //
                        btnPagamentoCartaoNFCE.setVisibility(View.GONE);
                        btnAvancarNFCE.setVisibility(View.VISIBLE);
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //LISTA DE PRODUTOS
        ArrayList<String> listaCredenciadoras = bd.getCredenciadora();
        ArrayAdapter adapterCredenciadora = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaCredenciadoras);
        adapterCredenciadora.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDescricaoCredenciadora = findViewById(R.id.spDescricaoCredenciadora);
        spDescricaoCredenciadora.setAdapter(adapterCredenciadora);

        //
        /*spBandeiraCredenciadora = findViewById(R.id.spBandeiraCredenciadora);
        spBandeiraCredenciadora.setAdapter(adapterFormasPagamento);*/

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
        Intent i = new Intent(getBaseContext(), ConfirmarDadosPedido.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("cpfCnpj_cliente", cpf_cnpj_cliente.getText().toString());
        i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());
        i.putExtra("produto", spProduto.getSelectedItem().toString());
        i.putExtra("qnt", etQuantidade.getText().toString());
        i.putExtra("vlt", etPreco.getText().toString());
        i.putExtra("credenciadora", spDescricaoCredenciadora.getSelectedItem().toString());
        i.putExtra("cod_aut", etCodAutorizacao.getText().toString());
        i.putExtra("nsu", etNsuCeara.getText().toString());

        startActivity(i);
        finish();
    }

    private void iniciarPagamento() {
        Intent i = new Intent(getBaseContext(), GerenciarPagamentoCartao.class);
        /*i.putExtra("qnt", etQuantidade.getText().toString());
        i.putExtra("vlt", etPreco.getText().toString());
        i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());*/
        i.putExtra("cpfCnpj_cliente", cpf_cnpj_cliente.getText().toString());
        i.putExtra("formaPagamento", spFormasPagamento.getSelectedItem().toString());
        i.putExtra("produto", spProduto.getSelectedItem().toString());
        i.putExtra("qnt", etQuantidade.getText().toString());
        i.putExtra("vlt", etPreco.getText().toString());

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
                finish();
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

    //String ;

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

                confirmar();
                //Timber.tag("Stone").i(Objects.requireNonNull(data.getStringExtra("authorizationCode")));
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                ShowMsgToast("Operação Cancelada!");
            }
        }
    }

}
