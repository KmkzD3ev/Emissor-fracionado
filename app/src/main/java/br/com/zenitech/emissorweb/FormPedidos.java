package br.com.zenitech.emissorweb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.zenitech.emissorweb.adapters.ProdutosPedidoAdapter;
import br.com.zenitech.emissorweb.domains.ProdutosPedidoDomain;
import br.com.zenitech.emissorweb.interfaces.IProdutosPedidoObserver;

public class FormPedidos extends AppCompatActivity implements IProdutosPedidoObserver {
    //region ARRAYS, ADAPTERS, RECYCLERVIEW, BUTTONS E LINEAR LAYOUT COMPAT
    private Context context;
    private DatabaseHelper bd;
    private SharedPreferences prefs;
    private RecyclerView rvProdutosPedido;
    private ArrayList<ProdutosPedidoDomain> listaProdutosPedido;
    private ProdutosPedidoAdapter produtosPedidoAdapter;
    private Spinner spProduto;
    private EditText cpf_cnpj_cliente, etQuantidade, etPreco, etDesconto;
    private ClassAuxiliar aux;
    // *** TEXTVIEWS
    private TextView txtCpfCnpjCli, txtPrecoMinMax, msgErroFracionar;
    // *** BTNs
    private Button btnPagamentoForm, btnAddProdutoLista;
    // *** LINEAR LAYOUT COMPAT
    private LinearLayoutCompat llcInfoEditando;
    //endregion

    //region START CONFIGURACOES
    private void startConfig() {
        //
        context = this;
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        bd = new DatabaseHelper(context);
        aux = new ClassAuxiliar();

        llcInfoEditando.setVisibility(View.GONE);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                if (params.getBoolean("EditarProduto")) {
                    llcInfoEditando.setVisibility(View.VISIBLE);
                    idTemp = bd.IdEditarPedidoTemp();
                    //Toast.makeText(context, "Id Temp = " + idTemp, Toast.LENGTH_SHORT).show();
                } else {
                    //
                    idTemp = bd.getProximoIdPedido();
                    //
                    bd.addPedidosTemp(String.valueOf(idTemp));
                }
            }
        } else {
            //
            idTemp = bd.getProximoIdPedido();
            //
            bd.addPedidosTemp(String.valueOf(idTemp));
        }
    }
    //endregion

    //region START IDS DOS ELEMENTOS DA UI
    private void startIds() {
        //
        rvProdutosPedido = findViewById(R.id.rvProdutosPedido);
        cpf_cnpj_cliente = findViewById(R.id.cpf_cnpj_cliente);
        etQuantidade = findViewById(R.id.etQuantidade);
        etPreco = findViewById(R.id.etPreco);
        msgErroFracionar = findViewById(R.id.msgErroFracionar);
        etDesconto = findViewById(R.id.etDesconto);
        txtCpfCnpjCli = findViewById(R.id.txtCpfCnpjCli);
        txtPrecoMinMax = findViewById(R.id.txtPrecoMinMax);
        btnPagamentoForm = findViewById(R.id.btnPagamentoForm);
        btnAddProdutoLista = findViewById(R.id.btnAddProdutoLista);
        spProduto = findViewById(R.id.spProdutos);
        llcInfoEditando = findViewById(R.id.llcInfoEditando);
    }
    //endregion

    //region VARIAVEIS
    private int quant = 0;
    int id = 0;
    int idTemp = 0;
    int quantidade = 0; //QUANTIDADE FRAGMENTADA
    boolean calcularDescto = true;
    private String precoMinimo, precoMaximo;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_pedidos);
        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // INICIA OS IDS DOS ELEMENTOS DA UI
        startIds();
        // INICIA AS CLASSES AUXILIARES
        startConfig();

        rvProdutosPedido.setLayoutManager(new LinearLayoutManager(this));
        cpf_cnpj_cliente.addTextChangedListener(MaskUtil.insert(cpf_cnpj_cliente, MaskUtil.MaskType.AUTO));

        etQuantidade.setText(R.string.numero_um);
        etQuantidade.addTextChangedListener(new ClassAuxiliar.VerifyQuaint(etQuantidade));

        //
        etPreco.addTextChangedListener(new ClassAuxiliar.MoneyTextWatcher(etPreco));
        //
        etDesconto.addTextChangedListener(new ClassAuxiliar.MoneyTextWatcher(etDesconto));
        etDesconto.setText(R.string.zero_reais);
        /*try {
            if (bd.getDadosPos().getDesconto_app_emissor().equalsIgnoreCase("1")) {
                 colocar o comentario aqui
                 * TRABALHAR NA PARTE DE DESCONTO COM O NOVO MODELO COM MAIS DE 1 PRODUTO,
                 * O VALOR DO DESCONTO SÓ APARECE NO FORMULÁRIO DE PRODUTOS ISSO É UM ERRO,
                 * TEM QUE CONTABILIZAR NO FINANCEIRO E NA TRANSMISSÃO DA NOTA
                //etDesconto.setVisibility(View.VISIBLE);
            }
        } catch (Exception ignored) {

        }*/

        //LISTA DE PRODUTOS
        ArrayList<String> listaProdutos = bd.getProdutos();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaProdutos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

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
                    //
                    if (!precoMinimo.equals("0.0")) {
                        txtPrecoMinMax.setVisibility(View.VISIBLE);
                        txtPrecoMinMax.setText(MessageFormat.format("Preço Mín. {0} | Máx. {1}", aux.maskMoney(new BigDecimal(precoMinimo)), aux.maskMoney(new BigDecimal(precoMaximo))));
                    } else {
                        txtPrecoMinMax.setText("");
                        txtPrecoMinMax.setVisibility(View.GONE);
                    }

                    //kleilson
                    quant = bd.getQuantProdutoRemessa(parent.getItemAtPosition(position).toString());
                } else
                    txtPrecoMinMax.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnPagamentoForm.setOnClickListener(v -> {
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
                            msgErroFracionar.setVisibility(View.VISIBLE);
                            msgErroFracionar.setText(str.toString());
                        }
                    }
                    if (avancar)
                        formsView();
                }
                // SE A QUATIDADE DE PRODUTOS NA LISTA FOR IGUAL 1 E A QUANTIDADE DO PRODUTO FOR MENOR QUE 101 PODE FRACIONAR
                else if (quantProdutoPedido == 1 && quantProdutosDiverso < 101) {
                    // PARA FRACIONAR NOTAS DE ÁGUA E OUTROS PRODUTOS SEM SER GÁS
                    quantidade = quantProdutosDiverso;
                    formsView();
                }
                // SE A QUATIDADE DE PRODUTOS NA LISTA FOR IGUAL 1 E A QUANTIDADE DO PRODUTO FOR MENOR QUE 101 PODE FRACIONAR
                else if (quantProdutoPedido == 1) {
                    // VERIFICA SE A QUANTIDADE DE GÁS É MAIOR QUE 5
                    if (quantidade > 5) {
                        msgErroFracionar.setVisibility(View.VISIBLE);
                        String proPedido = bd.getProdutosPedidoNCMGas(idTemp);
                        msgErroFracionar.setText(MessageFormat.format("Atenção:\nPara quantidades acima de 5 unidades do(s) produto(s) {0}não é permitido adicionar outros itens na nota. Favor emitir em notas separadas.", proPedido));
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
                            msgErroFracionar.setVisibility(View.VISIBLE);
                            msgErroFracionar.setText(str.toString());
                        }
                    }
                    if (avancar)
                        formsView();
                } else if (quantProdutoPedido > 1) {
                    // VERIFICA SE A QUANTIDADE DE GÁS É MAIOR QUE 5
                    if (quantidade > 5) {
                        msgErroFracionar.setVisibility(View.VISIBLE);
                        String proPedido = bd.getProdutosPedidoNCMGas(idTemp);
                        msgErroFracionar.setText(MessageFormat.format("Atenção:\nPara quantidades acima de 5 unidades do(s) produto(s) {0}não é permitido adicionar outros itens na nota. Favor emitir em notas separadas.", proPedido));
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
                            msgErroFracionar.setVisibility(View.VISIBLE);
                            msgErroFracionar.setText(str.toString());
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
                            msgErroFracionar.setVisibility(View.VISIBLE);
                            msgErroFracionar.setText(str.toString());
                        }
                    } else {
                        msgErroFracionar.setVisibility(View.VISIBLE);
                        msgErroFracionar.setText(R.string.msg_erro_fracionamento);
                    }
                }
            } else
                aux.ShowMsgToast(context, "Adicione pelo menos um produto");
        });

        // ADD PRODUTO AO PEDIDO
        btnAddProdutoLista.setOnClickListener(v -> ValidarCampFormPedido());

        listaProdutosPedido = bd.getProdutosPedido(idTemp);
        produtosPedidoAdapter = new ProdutosPedidoAdapter(this, listaProdutosPedido, bd);
        // Registra a Activity como Observador
        produtosPedidoAdapter.registerObserver(this);

        // Add the callback to the back stack
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Intent i = new Intent(context, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        AtualizarListaProdutos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        produtosPedidoAdapter.unregisterObserver(this);
    }

    private void ValidarCampFormPedido() {
        //ESCODER O TECLADO
        EsconderTeclado();
        //
        String valEtPreco = "";
        if (!etPreco.getText().toString().equals("")) {
            valEtPreco = String.valueOf(aux.converterValores(etPreco.getText().toString()));
        }
        //
        String valEtDesconto = "";
        if (!etDesconto.getText().toString().equals("")) {
            valEtDesconto = String.valueOf(aux.converterValores(etDesconto.getText().toString()));
        }
        //
        if (spProduto.getSelectedItem().toString().equals("PRODUTO")) {
            aux.ShowMsgToast(context, "Selecione um produto.");
        } else if (etQuantidade.getText().toString().equals("") || etQuantidade.getText().toString().equals("0")) {
            aux.ShowMsgToast(context, "Informe a quantidade.");
        } else if (quant == 0) {
            msgErroFracionar.setText(MessageFormat.format("Os produtos {0} estão atualmente esgotados.", quant));
            msgErroFracionar.setVisibility(View.VISIBLE);
        } else if (Integer.parseInt(etQuantidade.getText().toString()) > quant) {
            msgErroFracionar.setText(MessageFormat.format("A quantidade disponível é de apenas {0} itens. Por favor, ajuste a quantidade para um valor menor.", quant));
            msgErroFracionar.setVisibility(View.VISIBLE);
        } else if (etPreco.getText().toString().equals("") || valEtPreco.equals("R$ 0,00") || valEtPreco.equals("0.0") || valEtPreco.equals("0.00")) {
            aux.ShowMsgToast(context, "Informe o valor unitário.");
        } else {
            if (calcularDescto) {
                if (!precoMinimo.equals("0.0")) {
                    String _valUnitario = String.valueOf(aux.converterValores(etPreco.getText().toString()));
                    String _valDesconto = String.valueOf(aux.converterValores(etDesconto.getText().toString()));
                    String[] desc = {_valUnitario, _valDesconto};
                    if (aux.subitrair(desc).floatValue() > Float.parseFloat(precoMaximo) || aux.subitrair(desc).floatValue() < Float.parseFloat(precoMinimo)) {
                        aux.ShowMsgToast(context, "O valor informado não pode ultrapassar o preço mínimo ou máximo!");
                        return;
                    }
                    //else
                    //aux.ShowMsgToast(context, "Menor: " + aux.subitrair(desc).toString() + " | " + Float.parseFloat(precoMaximo));
                }
            }
            String[] ars = {precoMinimo, String.valueOf(aux.converterValores(etPreco.getText().toString()))};
            int vComp = aux.comparar(ars);
            //
            String[] arsMax = {precoMaximo, String.valueOf(aux.converterValores(etPreco.getText().toString()))};
            int vCompMax = aux.comparar(arsMax);
            // Verifica a quantidade máxima permeitida
            /*if (Integer.parseInt(etQuantidade.getText().toString()) > 1) {
                //aux.ShowMsgToast(context, "Quantidade Máxima 100 Unidades.");
                etDesconto.setText("0,00");
                etDesconto.setEnabled(false);
            } else {
                etDesconto.setText("0,00");
                etDesconto.setEnabled(true);
            }*/
            // Verifica se o valor informado é igual ou maior ao preço minimo
            if (!precoMinimo.equalsIgnoreCase("0.0")) {
                if (vComp > 0) {
                    aux.ShowMsgToast(context, "O Valor não pode ser menor que o preço mínimo!");
                } else if (vCompMax == -1) {
                    aux.ShowMsgToast(context, "O Valor não pode ser maior que o preço máximo!");
                } else
                    AddProdutoPedido();
            } else
                AddProdutoPedido();
        }
    }

    private void AddProdutoPedido() {
        ProdutosPedidoDomain produto = new ProdutosPedidoDomain();
        //
        String valorUnit = String.valueOf(aux.converterValores(etPreco.getText().toString()));
        // MULTIPLICA O VALOR PELA QUANTIDADE
        String[] multiplicar = {valorUnit, etQuantidade.getText().toString()};
        String total = String.valueOf(aux.multiplicar(multiplicar));
        //
        String valorDesc = String.valueOf(aux.converterValores(etDesconto.getText().toString()));
        String[] multiplicarDesc = {valorDesc, etQuantidade.getText().toString()};
        String desconto = String.valueOf(aux.multiplicar(multiplicarDesc));
        //
        String[] subtrair = {total, desconto};
        total = String.valueOf(aux.subitrair(subtrair));
        //
        produto.id_pedido = String.valueOf(idTemp);
        produto.produto = spProduto.getSelectedItem().toString();
        produto.quantidade = etQuantidade.getText().toString();
        produto.valor = valorUnit;
        produto.total = total;
        produto.desconto = valorDesc;

        //
        if (VerificarFracionamento()) {
            //Toast.makeText(context, "Vai fracionar", Toast.LENGTH_SHORT).show();

            if (bd.getFianceiroDiferenteDeDinheiroPix(String.valueOf(idTemp))) {
                //Toast.makeText(context, "Tem Financeiro diferente", Toast.LENGTH_SHORT).show();
                msgErroFracionar.setText("Um método de pagamento diferente de dinheiro ou Pix já foi adicionado, impossibilitando o fracionamento.");
                msgErroFracionar.setVisibility(View.VISIBLE);
                return;
            }
        }

        // VERIFICA SE O PRODUTO JÁ FOI INSERIDO E ESTÁ COM MESMO VALOR

        ProdutosPedidoDomain comp = bd.getProdutoPedidoTemp(idTemp, produto.produto);
        if (comp.valor != null) {

            if (aux.comparar(new String[]{comp.valor, valorUnit}) == 0) {
                int quantComp = Integer.parseInt(produto.quantidade) + Integer.parseInt(comp.quantidade);

                //Toast.makeText(context, "Quantidade Restante: " + quantComp, Toast.LENGTH_LONG).show();
                if (quantComp > quant) {
                    msgErroFracionar.setText(MessageFormat.format("A quantidade disponível é de apenas {0} itens. Por favor, ajuste a quantidade para um valor menor.", quant));
                    msgErroFracionar.setVisibility(View.VISIBLE);
                    return;
                }

                //String val = String.valueOf(aux.somar(new String[]{comp.valor, valorUnit}));
                String tot = String.valueOf(aux.multiplicar(new String[]{valorUnit, String.valueOf(quantComp)}));
                bd.updateProdutoPedidoTemp(
                        String.valueOf(idTemp),
                        comp.id_produto,
                        String.valueOf(quantComp),
                        tot
                );

                //
                etDesconto.setText(R.string.zero_reais);
                //
                AtualizarListaProdutos();
                return;
            } else {
                msgErroFracionar.setText(MessageFormat.format("Atenção: O produto {0} já está presente na lista. Por favor, considere excluí-lo antes de adicionar novamente, ou certifique-se de adicionar com o mesmo valor unitário para evitar duplicatas.", produto.produto));
                msgErroFracionar.setVisibility(View.VISIBLE);
                return;
            }
        } else {
            bd.addProdutoPedido(produto);
            //
            etDesconto.setText(R.string.zero_reais);
            //
            AtualizarListaProdutos();
        }
    }

    private void AtualizarListaProdutos() {
        msgErroFracionar.setVisibility(View.GONE);
        //
        listaProdutosPedido = bd.getProdutosPedido(idTemp);
        produtosPedidoAdapter = new ProdutosPedidoAdapter(this, listaProdutosPedido, bd);
        // Registra a Activity como Observador
        produtosPedidoAdapter.registerObserver(this);
        rvProdutosPedido.setAdapter(produtosPedidoAdapter);

        spProduto.setSelection(0);
        etQuantidade.setText(R.string.numero_um);
        etPreco.setText(R.string.zero_reais);
    }

    private boolean VerificarFracionamento() {

        String prod = spProduto.getSelectedItem().toString();

        if (bd.getProdutoNcmGas(prod)) {
            // RECEBE A QUANTIDADE DE PRODUTOS COM O NCM = 27111910
            int quantGas = bd.getQuantProdutosPedidoNCMGas(idTemp);

            // QUANTIDADE DE GÁS
            if (quantGas > 0) {
                quantGas = bd.getQuantProdutosPedidoNCMGas(idTemp) + Integer.parseInt(etQuantidade.getText().toString());
                if (quantGas > 5) {
                    return true;
                }
            } else {

                if (bd.getProdutoNcmGas(prod) && Integer.parseInt(etQuantidade.getText().toString()) > 5) {
                    return true;
                }
            }
        }

        if (bd.getProdutoNcmOutros(prod)) {

            // RECEBE A QUANTIDADE DE PRODUTOS COM O NCM != 27111910
            List<String> listPro = bd.getQuantProdutosPedidoNCM(idTemp);
            if (!listPro.isEmpty()) {
                for (int i = 0; listPro.size() > i; i++) {
                    String[] iList = listPro.get(i).split(",");
                    List<String> minMaxFrac = bd.getMinMaxFracionamentoProduto(iList[0]);
                    int quant = Integer.parseInt(iList[1]) + Integer.parseInt(etQuantidade.getText().toString());                      // QUANTIDADE DE GÁS
                    Toast.makeText(context, "" + quant, Toast.LENGTH_SHORT).show();
                    int max = Integer.parseInt(minMaxFrac.get(1));
                    if (quant > max) {
                        return true;
                    }
                    /*if (!minMaxFrac.get(0).equals("0") && quant != 0) {


                        if (Integer.parseInt(iList[1]) > max) {
                            return true;
                        }
                    }*/
                }
            } else {
                String idProduto = bd.getIdProduto(prod);
                int maxFrac = Integer.parseInt(bd.getMinMaxFracionamentoProduto(idProduto).get(1));
                if (Integer.parseInt(etQuantidade.getText().toString()) > maxFrac) {
                    return true;
                }
            }
        }

        // SE TIVER MAIS DE UM PRODUTO RETORNA VERDADEIRO E NÃO PODE ADICIONAR
        /*ArrayList<ProdutosPedidoDomain> prodPedido = bd.getProdutosPedido(idTemp);
        if (!prodPedido.get(0).produto.equals(prod)) {
            //return true;
        }*/

        return false;
    }

    private void formsView() {
        prefs.edit().putString("cpf_cnpj", cpf_cnpj_cliente.getText().toString()).apply();
        startActivity(new Intent(context, FinanceiroNFCe.class));
        Sair();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent i = new Intent(context, Principal.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void Sair() {
        finish();
    }

    @Override
    public void onProdutosPedidoChanged() {
        runOnUiThread(this::AtualizarListaProdutos);
    }

    public void EsconderTeclado() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
