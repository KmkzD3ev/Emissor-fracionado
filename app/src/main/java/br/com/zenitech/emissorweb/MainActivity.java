package br.com.zenitech.emissorweb;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //LISTA DE PRODUTOS
    private ArrayList<String> listaProdutos;

    //
    String[] listaFormasPagamento = {
            "FORMA PAGAMENTO",
            "DINHEIRO"
    };

    private Spinner spProduto, spFormasPagamento;
    private EditText cpf_cnpj_cliente, etQuantidade, etPreco;
    private DatabaseHelper bd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //
        bd = new DatabaseHelper(this);

        //
        listaProdutos = bd.getProdutos();
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaProdutos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spProduto = (Spinner) findViewById(R.id.spProdutos);
        spProduto.setAdapter(adapter);

        ArrayAdapter adapterFormasPagamento = new ArrayAdapter(this, android.R.layout.simple_spinner_item, listaFormasPagamento);
        adapterFormasPagamento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFormasPagamento = (Spinner) findViewById(R.id.spFormasPagamento);
        spFormasPagamento.setAdapter(adapterFormasPagamento);

        //
        cpf_cnpj_cliente = (EditText) findViewById(R.id.cpf_cnpj_cliente);
        cpf_cnpj_cliente.addTextChangedListener(MaskUtil.insert(cpf_cnpj_cliente, MaskUtil.MaskType.AUTO));

        //
        etQuantidade = (EditText) findViewById(R.id.etQuantidade);
        etQuantidade.setText("");

        //
        etPreco = (EditText) findViewById(R.id.etPreco);
        etPreco.addTextChangedListener(new MoneyTextWatcher(etPreco));

        etPreco.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {

                    if (etQuantidade.getText().toString().equals("") || etQuantidade.getText().toString().equals("0") || etPreco.getText().toString().equals("") || etPreco.getText().toString().equals("R$0,00")) {
                        Toast.makeText(getBaseContext(), "Quantidade e Valor Unitário não podem ser vazios.", Toast.LENGTH_LONG).show();
                    } else {
                        //addVenda();
                    }

                    handled = true;
                }
                return handled;
            }
        });

        findViewById(R.id.btn_finalizar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                //ESCODER O TECLADO
                // TODO Auto-generated method stub
                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }

                Intent i = new Intent(getBaseContext(), ConfirmarDadosPedido.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("cpfCnpj_cliente", cpf_cnpj_cliente.getText().toString());
                i.putExtra("formaPagamento", spFormasPagamento.toString());
                i.putExtra("produto", spProduto.toString());
                i.putExtra("qnt", etQuantidade.getText().toString());
                i.putExtra("vlt", etPreco.getText().toString());

                startActivity(i);

                finish();
            }
        });

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), SearchBTActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class MoneyTextWatcher implements TextWatcher {
        private final WeakReference<EditText> editTextWeakReference;

        public MoneyTextWatcher(EditText editText) {
            editTextWeakReference = new WeakReference<EditText>(editText);
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
            String cleanString = s.toString().replaceAll("[^0-9]", "");
            BigDecimal parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
            String formatted = NumberFormat.getCurrencyInstance().format(parsed);
            editText.setText(formatted);
            editText.setSelection(formatted.length());
            editText.addTextChangedListener(this);
        }
    }
}
