package br.com.zenitech.emissorweb;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import android.view.MenuItem;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.zenitech.emissorweb.controller.LogCartaoControllerKT;
import br.com.zenitech.emissorweb.domains.Autorizacoes;
import br.com.zenitech.emissorweb.domains.AutorizacoesPinpad;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.util.ActionCodeStone;
import stone.application.StoneStart;
import stone.application.enums.Action;
import stone.application.enums.InstalmentTransactionEnum;
import stone.application.enums.ReceiptType;
import stone.application.enums.TransactionStatusEnum;
import stone.application.enums.TypeOfTransactionEnum;
import stone.application.interfaces.StoneActionCallback;
import stone.application.interfaces.StoneCallbackInterface;
import stone.database.transaction.TransactionDAO;
import stone.database.transaction.TransactionObject;
import stone.environment.Environment;
import stone.providers.ActiveApplicationProvider;
import stone.providers.BaseTransactionProvider;
import stone.providers.CancellationProvider;
import stone.providers.SendEmailTransactionProvider;
import stone.providers.TransactionProvider;
import stone.repository.remote.email.pombo.email.Contact;
import stone.user.UserModel;
import stone.utils.Stone;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

// implements StoneActionCallback
public class GerenciarPagamentoCartao extends AppCompatActivity implements StoneActionCallback {

    // ** STONE MODULO **
    TransactionProvider provider;
    TransactionObject transactionObject;
    TransactionDAO transactionDAO;

    String STONE_CODE;
    //ZENITECH TESTE -
    //String STONE_CODE = "177391172";
    //String STONE_CODE = "208931932";
    /*// Pedido para obter o dispositivo bluetooth
    private static final int REQUEST_GET_DEVICE = 0;
    // Pedido para obter o dispositivo bluetooth
    private static final int DEFAULT_NETWORK_PORT = 9100;
    private BluetoothSocket mBtSocket;*/

    ArrayList<Unidades> elementos;
    Unidades unidades;

    //
    String[] listaTotalParcelas = {
            "1x (à vista)",
            "2x sem juros",
            "3x sem juros",
            "4x sem juros",
            "5x sem juros",
            "6x sem juros",
            "7x sem juros",
            "8x sem juros",
            "9x sem juros",
            "10x sem juros",
            "11x sem juros",
            "12x sem juros"
    };
    Spinner spParcelas;
    //
    Context context;
    private DatabaseHelper bd;
    TextView txtTotalPagar, txtStatusPagamento, txtMsgCausaErro, txtMsgErro, txtErroDiverso;
    ClassAuxiliar cAux;
    LinearLayoutCompat llDebito, llCredito, llPagamentoImprimir, llPagamentoPedirCartao, llProcessandoPagamento;
    LinearLayoutCompat llPagamentoAprovado, llPagamentoReprovado, llErroDiversoPg;
    AlertDialog alerta;
    SharedPreferences prefs;

    AppCompatButton btnEnviarTrazacao, btnFinalizarPagamento, btnEnviarComprovante, btnCancelarFinalizarPagamento;

    List<UserModel> userList;
    private String totalAPagar;

    // **
    String cpfCnpj_cliente;
    String formaPagamento;
    String produto;
    String valorUnit;
    String total;
    int transactionId;
    TextView txtFalha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_pagamento_cartao);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // **
        context = this;
        bd = new DatabaseHelper(this);
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        cAux = new ClassAuxiliar();

        elementos = bd.getUnidades();
        unidades = elementos.get(0);
        STONE_CODE = unidades.getCodloja();

        // **
        txtTotalPagar = findViewById(R.id.txtTotalPagarCartao);
        txtStatusPagamento = findViewById(R.id.txtStatusPagamento);
        txtMsgCausaErro = findViewById(R.id.txtMsgCausaErro);
        txtMsgErro = findViewById(R.id.txtMsgErro);
        txtErroDiverso = findViewById(R.id.txtErroDiverso);
        txtFalha = findViewById(R.id.txtFalha);
        llDebito = findViewById(R.id.llDebito);
        llCredito = findViewById(R.id.llCredito);
        llPagamentoImprimir = findViewById(R.id.llPagamentoImprimir);
        //
        llPagamentoPedirCartao = findViewById(R.id.llPagamentoPedirCartao);
        llPagamentoPedirCartao.setVisibility(View.GONE);
        //
        llProcessandoPagamento = findViewById(R.id.llProcessandoPagamento);
        llProcessandoPagamento.setVisibility(View.GONE);
        //
        llErroDiversoPg = findViewById(R.id.llErroDiversoPg);
        llErroDiversoPg.setVisibility(View.GONE);
        //
        llPagamentoAprovado = findViewById(R.id.llPagamentoAprovado);
        llPagamentoReprovado = findViewById(R.id.llPagamentoReprovado);
        llPagamentoReprovado.setVisibility(View.GONE);
        //
        btnEnviarTrazacao = findViewById(R.id.btnEnviarTrazacao);

        // ** Concluir o pagamento
        btnFinalizarPagamento = findViewById(R.id.btnFinalizarPagamento);
        btnFinalizarPagamento.setOnClickListener(v -> _finalizarPagamento());
        // ** Enviar comprovante
        btnEnviarComprovante = findViewById(R.id.btnEnviarComprovante);
        btnEnviarComprovante.setOnClickListener(v -> enviarComprovantePorEmail());
        // ** Cancelar pagamento
        btnCancelarFinalizarPagamento = findViewById(R.id.btnCancelarFinalizarPagamento);
        btnCancelarFinalizarPagamento.setOnClickListener(v -> cancelarPagamento());
        findViewById(R.id.btnSairPag).setOnClickListener(view -> Sair());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaTotalParcelas);
        spParcelas = findViewById(R.id.spParcelas);
        spParcelas.setAdapter(adapter);
        spParcelas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!parent.getItemAtPosition(position).toString().equals("PRODUTO")) {
                    /*double precoMinProd = bd.getPrecoMinimoProduto(parent.getItemAtPosition(position).toString());
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
                    //Toast.makeText(getBaseContext(), "" + bd.getQuantProdutoRemessa(parent.getItemAtPosition(position).toString()), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getBaseContext(), precoMinimo + "|" + precoMaximo, Toast.LENGTH_LONG).show();*/
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //
        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                //
                cpfCnpj_cliente = params.getString("cpfCnpj_cliente");
                formaPagamento = cAux.removerAcentos(params.getString("formaPagamento"));
                produto = params.getString("produto");
                valorUnit = String.valueOf(cAux.converterValores(params.getString("vlt")));

                //MULTIPLICA O VALOR PELA QUANTIDADE
                String[] multiplicar = {valorUnit, params.getString("qnt")};
                total = String.valueOf(cAux.multiplicar(multiplicar));
                txtTotalPagar.setText(cAux.maskMoney(new BigDecimal(total)));
                totalAPagar = cAux.soNumeros(cAux.maskMoney(new BigDecimal(total)));

                // Mostra a forma de pagamento escolhida
                if (formaPagamento.equalsIgnoreCase("CARTAO DE CREDITO")) {
                    llCredito.setVisibility(View.VISIBLE);

                } else if (formaPagamento.equalsIgnoreCase("CARTAO DE DEBITO")) {
                    llDebito.setVisibility(View.VISIBLE);
                }
            }
        }

        // APAGAR DEPOIS
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", "ok");
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });


        btnEnviarTrazacao.setOnClickListener(v -> iniciarTranzacao());

        iniciarStone();
    }

    //
    void iniciarTranzacao() {
        // Verifica se o bluetooth esta ligado e se existe algum pinpad conectado.
        if (Stone.getPinpadListSize() > 0) {
            /*Intent transactionIntent = new Intent(MainActivity.this, TransactionActivity.class);
            startActivity(transactionIntent);*/

            //makeText(getApplicationContext(), "Pinpad conectado.", LENGTH_SHORT).show();
            btnEnviarTrazacao.setVisibility(View.GONE);
            iniciarCaptura();
        } else {

            Intent devicesIntent = new Intent(GerenciarPagamentoCartao.this, DevicesActivityPinPad.class);
            startActivity(devicesIntent);


            /*// MODULO BOETOOTH **

            ativarBluetooth();

            if (!prefs.getString("enderecoBltPinPad", "").equalsIgnoreCase("")) {
                establishBluetoothConnection(prefs.getString("enderecoBltPinPad", ""));
            } else {
                waitForConnection();
            }
            makeText(getApplicationContext(), "Conecte-se a um pinpad.", LENGTH_SHORT).show();*/
        }
    }

    // INICIAR UMA CAPTURA DE PAGAMENTO COM O PINPAD
    private void iniciarCaptura() {
        transactionObject = new TransactionObject();
        //Definir o valor da transação em centavos
        transactionObject.setAmount(totalAPagar);//cAux.soNumeros(txtTotalPagar.toString())

        /* AVISO IMPORTANTE: Não é recomendado alterar o campo abaixo do ITK,
         * pois ele gera um valor único. Contudo, caso seja necessário
         * faça conforme a linha a seguir. */
        //transactionObject.setInitiatorTransactionKey("SEU_IDENTIFICADOR_UNICO");

        //Informar a quantidade de parcelas, veja a tabela de valores para o InstalmentTransactionEnum
        /*
        TWO_INSTALMENT_NO_INTEREST	2x sem juros
        THREE_INSTALMENT_NO_INTEREST	3x sem juros
        FOUR_INSTALMENT_NO_INTEREST	4x sem juros
        FIVE_INSTALMENT_NO_INTEREST	5x sem juros
        SIX_INSTALMENT_NO_INTEREST	6x sem juros
        SEVEN_INSTALMENT_NO_INTEREST	7x sem juros
        EIGHT_INSTALMENT_NO_INTEREST	8x sem juros
        NINE_INSTALMENT_NO_INTEREST	9x sem juros
        TEN_INSTALMENT_NO_INTEREST	10x sem juros
        ELEVEN_INSTALMENT_NO_INTEREST	11x sem juros
        TWELVE_INSTALMENT_NO_INTEREST	12x sem juros
         */
        if (spParcelas.getSelectedItem().toString().equals("2x sem juros")) {
            transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.TWO_INSTALMENT_NO_INTEREST);
        } else if (spParcelas.getSelectedItem().toString().equals("3x sem juros")) {
            transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.THREE_INSTALMENT_NO_INTEREST);
        } else if (spParcelas.getSelectedItem().toString().equals("4x sem juros")) {
            transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.FOUR_INSTALMENT_NO_INTEREST);
        } else if (spParcelas.getSelectedItem().toString().equals("5x sem juros")) {
            transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.FIVE_INSTALMENT_NO_INTEREST);
        } else if (spParcelas.getSelectedItem().toString().equals("6x sem juros")) {
            transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.SIX_INSTALMENT_NO_INTEREST);
        } else if (spParcelas.getSelectedItem().toString().equals("7x sem juros")) {
            transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.SEVEN_INSTALMENT_NO_INTEREST);
        } else if (spParcelas.getSelectedItem().toString().equals("8x sem juros")) {
            transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.EIGHT_INSTALMENT_NO_INTEREST);
        } else if (spParcelas.getSelectedItem().toString().equals("9x sem juros")) {
            transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.NINE_INSTALMENT_NO_INTEREST);
        } else if (spParcelas.getSelectedItem().toString().equals("10x sem juros")) {
            transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.TEN_INSTALMENT_NO_INTEREST);
        } else if (spParcelas.getSelectedItem().toString().equals("11x sem juros")) {
            transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.ELEVEN_INSTALMENT_NO_INTEREST);
        } else if (spParcelas.getSelectedItem().toString().equals("12x sem juros")) {
            transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.TWELVE_INSTALMENT_NO_INTEREST);
        } else {
            transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.ONE_INSTALMENT);
        }

        //Definir forma de pagamento
        if (formaPagamento.equalsIgnoreCase("CARTAO DE CREDITO")) {
            transactionObject.setTypeOfTransaction(TypeOfTransactionEnum.CREDIT);

        } else if (formaPagamento.equalsIgnoreCase("CARTAO DE DEBITO")) {
            transactionObject.setTypeOfTransaction(TypeOfTransactionEnum.DEBIT);
        }

        /*
        Nome de exibição no extrato do cliente (máximo de 14 caracteres). Deixar em branco caso queira que apareça o nome do estabelecimento cadastrado na Stone
         */
        //transactionObject.setShortName("TST_014");

        //Define se transação será feita com captura ou não.
        transactionObject.setCapture(true);

        //Timber.tag("PinPad_Teste: ").i(String.valueOf(Stone.getUserModel(0)));
        //Timber.tag("PinPad_Teste: ").i(String.valueOf(Stone.getPinpadFromListAt(0)));
        Log.i("PinPad_Teste", String.valueOf(Stone.getUserModel(0)));
        Log.i("PinPad_Teste1", String.valueOf(Stone.getPinpadFromListAt(0)));

        // Processo para envio da transação
        provider = new TransactionProvider(context, transactionObject, Stone.getUserModel(0), Stone.getPinpadFromListAt(0));

        provider.useDefaultUI(true);
        provider.setDialogTitle("Aguarde"); // Título do Dialog
        provider.setDialogMessage("Enviando..."); // Mensagem do Dialog
        provider.setConnectionCallback(this);
        /*provider.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                // Transação enviada com sucesso e salva no banco. Para acessar, use o TransactionDAO

                //
                transactionDAO = new TransactionDAO(context);
                // Pega o id da última transação
                transactionId = transactionDAO.getLastTransactionId();
                // Pega os dados da última transação
                //transactionObject = transactionDAO.findTransactionWithId(transactionId);
                //Log.i("Stone", String.valueOf(transactionDAO.findTransactionWithId(transactionId)));

                TransactionObject to = transactionDAO.findTransactionWithId(transactionId);

                // **
                String actionCode = Objects.requireNonNull(to).getActionCode();
                if (actionCode.equalsIgnoreCase("0000") ||
                        actionCode.equalsIgnoreCase("0001") ||
                        actionCode.equalsIgnoreCase("0002") ||
                        actionCode.equalsIgnoreCase("0003") ||
                        actionCode.equalsIgnoreCase("0004")
                ) {
                    // ** ADD
                    bd.addAutorizacoesPinPad(new AutorizacoesPinpad(
                            String.valueOf(transactionId),
                            "",
                            String.valueOf(Objects.requireNonNull(to).getIdFromBase()),
                            to.getAmount(),
                            to.getRequestId(),
                            to.getEmailSent(),
                            to.getTimeToPassTransaction(),
                            to.getInitiatorTransactionKey(),
                            to.getRecipientTransactionIdentification(),
                            to.getCardHolderNumber(),
                            to.getCardHolderName(),
                            to.getDate(),
                            to.getTime(),
                            to.getAid(),
                            to.getArcq(),
                            to.getAuthorizationCode(),
                            to.getIccRelatedData(),
                            to.getTransactionReference(),
                            to.getActionCode(),
                            to.getCommandActionCode(),
                            to.getPinpadUsed(),
                            to.getSaleAffiliationKey(),
                            to.getCne(),
                            to.getCvm(),
                            to.getBalance(),
                            to.getServiceCode(),
                            to.getSubMerchantCategoryCode(),
                            String.valueOf(to.getEntryMode()),
                            String.valueOf(to.getCardBrand()),
                            String.valueOf(to.getInstalmentTransaction()),
                            String.valueOf(to.getTransactionStatus()),
                            String.valueOf(to.getInstalmentType()),
                            String.valueOf(to.getTypeOfTransactionEnum()),
                            "",//String.valueOf(to.getSignature())
                            String.valueOf(to.getCancellationDate()),
                            String.valueOf(to.isCapture()),
                            to.getShortName(),
                            to.getSubMerchantAddress(),
                            "",//to.getUserModel().toString()
                            String.valueOf(to.isFallbackTransaction()),
                            to.getAppLabel(),
                            to.getUserModel().getMerchantName(),
                            to.getUserModel().getMerchantAddress().getCity() + "/" + to.getUserModel().getMerchantAddress().getDistric(),
                            to.getUserModel().getMerchantDocumentNumber()
                    ));

                    msg(true);
                } else {
                    //
                    //provider.getMessageFromAuthorize();
                    if (to.getActionCode().equalsIgnoreCase("1016")) {
                        txtMsgCausaErro.setText(String.format("%s\n%s", to.getActionCode(), provider.getMessageFromAuthorize()));//Saldo insuficiente
                    }
                    //
                    else if (to.getActionCode().equalsIgnoreCase("1017")) {
                        txtMsgCausaErro.setText(String.format("%s\nSenha inválida", to.getActionCode()));
                    }
                    //
                    else {
                        txtMsgCausaErro.setText(to.getActionCode());
                    }
                    msg(false);
                }
            }

            @Override
            public void onError() {
                // Erro na transação
                msg(false);
            }
        });*/
        provider.execute();
    }

    // ENVIAR COMPROVANTE POR EMAIL
    private void enviarComprovantePorEmail() {
        //
        // Exibir um popup para o usuário inserir um texto
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Informe os dados do cliente para o envio");
        final EditText editText = new EditText(context);
        final EditText editText1 = new EditText(context);
        final LinearLayoutCompat llc = new LinearLayoutCompat(context);
        final TextView tv = new TextView(context);
        final TextView tv1 = new TextView(context);
        tv.setText("Nome");
        tv1.setText("E-mail");

        llc.setOrientation(LinearLayoutCompat.VERTICAL);
        llc.setPadding(20, 20, 20, 20);
        llc.addView(tv);
        llc.addView(editText);
        llc.addView(tv1);
        llc.addView(editText1);
        builder.setView(llc);
        builder.setPositiveButton("ENVIAR", (dialog, which) -> {
            String text = editText.getText().toString();
            String text1 = editText1.getText().toString();

            //
            SendEmailTransactionProvider provider = new SendEmailTransactionProvider(context, transactionObject);
            provider.setReceiptType(ReceiptType.CLIENT);
            // Email cliente
            provider.addTo(new Contact(text1, text));
            // Email estabelecimento
            provider.setFrom(new Contact("siacnotasfiscais@gmail.com", "EmissorWeb"));
            provider.useDefaultUI(false);
            provider.setDialogMessage("Enviando comprovante");
            provider.setConnectionCallback(new StoneCallbackInterface() {
                public void onSuccess() {
                    //Comprovante enviado com sucesso
                    toastMsg("Comprovante enviado com sucesso!");
                }

                public void onError() {
                    //Comprovante não enviado
                    toastMsg("Comprovante não enviado!");
                }
            });
            provider.execute();
        });
        builder.setNeutralButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void Sair() {
        //
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    // Cancelar Pagamento
    private void cancelarPagamento() {
        final CancellationProvider provider = new CancellationProvider(context, transactionObject);
        provider.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                //Transação Cancelada com sucesso
                toastMsg("Transação Cancelada com sucesso");
            }

            @Override
            public void onError() {
                //Ocorreu um erro no cancelamento da transacao
                //Método que retorna o código referente ao erro da operação
                //getActionCode();
                toastMsg("Ocorreu um erro no cancelamento da transacao");
            }
        });
        provider.execute();
    }

    private void toastMsg(String msg) {
        Toast toast = makeText(context, msg, LENGTH_SHORT);
        toast.setGravity(1, 0, 0);
        toast.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            mostrarMsg();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mostrarMsg();
    }

    void sair() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    void msg(boolean statusPag) {
        //
        /*llPagamentoAprovado = findViewById(R.id.llPagamentoAprovado);
        llPagamentoReprovado = findViewById(R.id.llPagamentoReprovado);*/

        runOnUiThread(() -> {
            //
            if (statusPag) {
                llPagamentoAprovado.setVisibility(View.VISIBLE);
            } else {
                llPagamentoReprovado.setVisibility(View.VISIBLE);
            }

            // **
            int time = 4000;

            // ESPERA 2.3 SEGUNDOS PARA  SAIR DO SPLASH
            new Handler(Looper.myLooper()).postDelayed(() -> {
                //
                if (statusPag) {
                    //llPagamentoImprimir.setVisibility(View.VISIBLE);
                    _finalizarPagamento();
                } else {
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_CANCELED, returnIntent);
                    finish();
                }
            }, time);
        });
    }

    private void _finalizarPagamento() {
        //
        //Timber.tag("Stone").i(transactionObject.getAuthorizationCode());

        //Log.i("Stone", String.valueOf(transactionDAO.findTransactionWithId(transactionId)));

        //
        transactionDAO = new TransactionDAO(context);
        Log.e("transactionDAO", transactionDAO.toString());
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", "ok");
        // **
        returnIntent.putExtra("authorizationCode", String.valueOf(Objects.requireNonNull(transactionDAO.findTransactionWithId(transactionId)).getAuthorizationCode()));
        returnIntent.putExtra("cardBrand", String.valueOf(Objects.requireNonNull(transactionDAO.findTransactionWithId(transactionId)).getCardBrand()));
        returnIntent.putExtra("nsu", String.valueOf(Objects.requireNonNull(transactionDAO.findTransactionWithId(transactionId)).getActionCode()));

        //
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void mostrarMsg() {

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("Atenção");
        //define a mensagem
        String msg = "Você deseja realmente cancelar este pagamento?";
        builder.setMessage(msg);
        //define um botão como positivo
        builder.setPositiveButton("SIM", (arg0, arg1) -> {
            sair();
        });
        //define um botão como negativo.
        builder.setNegativeButton("NÃO", (arg0, arg1) -> {

            // **
            //msg(true);
        });
        //cria o AlertDialog
        alerta = builder.create();
        //Exibe alerta
        alerta.show();
    }

    /*private void error(final String text) {
        Timber.tag("Gere").w(text);

        runOnUiThread(() -> Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show());
    }*/

    // MODULO STONE **

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    // Iniciar o Stone
    void iniciarStone() {
        // O primeiro passo é inicializar o SDK.
        StoneStart.init(context);
        /*Em seguida, é necessário chamar o método setAppName da classe Stone,
        que recebe como parâmetro uma String referente ao nome da sua aplicação.*/
        Stone.setAppName(getApplicationName(context));
        //Ambiente de Sandbox "Teste"
        //Stone.setEnvironment(new Configuracoes().Ambiente());
        //Ambiente de Produção
        //Stone.setEnvironment((Environment.PRODUCTION));

        // Esse método deve ser executado para inicializar o SDK
        List<UserModel> userList = StoneStart.init(context);

        // Quando é retornado null, o SDK ainda não foi ativado
        if (userList != null) {
            // O SDK já foi ativado.
            _pinpadAtivado();

        } else {
            // Inicia a ativação do SDK
            ativarStoneCode();
        }
    }

    //
    void ativarStoneCode() {
        // Esse método deve ser executado para inicializar o SDK
        userList = StoneStart.init(context);

        // Quando é retornado null, o SDK ainda não foi ativado
        if (userList == null) {
            ActiveApplicationProvider activeApplicationProvider = new ActiveApplicationProvider(context);
            activeApplicationProvider.setDialogMessage("Ativando o Stone Code");
            activeApplicationProvider.setDialogTitle("Aguarde");
            activeApplicationProvider.useDefaultUI(true);
            activeApplicationProvider.setConnectionCallback(new StoneCallbackInterface() {

                public void onSuccess() {
                    // SDK ativado com sucesso
                    _pinpadAtivado();
                }

                public void onError() {
                    // Ocorreu algum erro na ativação
                    Toast.makeText(context, activeApplicationProvider.getListOfErrors().toString() + " ------ Ocorreu algum erro na ativação", Toast.LENGTH_SHORT).show();
                }
            });
            activeApplicationProvider.activate(STONE_CODE);
        } else {
            // O SDK já foi ativado.
            _pinpadAtivado();
        }
    }

    void _pinpadAtivado() {
        // O SDK já foi ativado.
        //Toast.makeText(context, "O SDK já foi ativado.", Toast.LENGTH_SHORT).show();
        btnEnviarTrazacao.setVisibility(View.VISIBLE);
        //iniciarTranzacao();
    }

    // Desativar Stone Code
    void desativarStoneCode() {
        ActiveApplicationProvider activeApplicationProvider = new ActiveApplicationProvider(context);
        activeApplicationProvider.setDialogMessage("Desativando o Stone Code");
        activeApplicationProvider.setDialogTitle("Aguarde");
        activeApplicationProvider.useDefaultUI(true);
        activeApplicationProvider.setConnectionCallback(new StoneCallbackInterface() {

            public void onSuccess() {
                // Operação executada com sucesso
                Toast.makeText(context, "Operação executada com sucesso", Toast.LENGTH_SHORT).show();

                //btnAtivarStoneCode.setVisibility(View.VISIBLE);
                //btnDesativarStoneCode.setVisibility(View.GONE);
                //btnNovaTransacao.setVisibility(View.GONE);
            }

            public void onError() {
                // Ocorreu algum erro na operação
                Toast.makeText(context, "Ocorreu algum erro na operação", Toast.LENGTH_SHORT).show();
            }
        });
        activeApplicationProvider.deactivate(STONE_CODE);
    }

    @Override
    public void onStatusChanged(Action action) {

    }

    @Override
    public void onSuccess() {
        AsyncTask.execute(() -> {
            //runOnUiThread(() -> {

            // Transação enviada com sucesso e salva no banco. Para acessar, use o TransactionDAO
            //
            transactionDAO = new TransactionDAO(context);
            Log.e("Stone", String.valueOf(transactionDAO.getLastTransactionId()));
            // Pega o id da última transação
            transactionId = transactionDAO.getLastTransactionId();
            // Pega os dados da última transação
            //transactionObject = transactionDAO.findTransactionWithId(transactionId);
            Log.i("Stone", String.valueOf(transactionDAO.findTransactionWithId(transactionId)));
            TransactionObject to = transactionDAO.findTransactionWithId(transactionId);

            // PAGAMENTO APROVADO
            if (Objects.requireNonNull(to).getTransactionStatus() == TransactionStatusEnum.APPROVED) {

                //imprimircomprovantePOS();

                // **
                //String actionCode = Objects.requireNonNull(to).getActionCode();
                String actionCode = transactionObject.getActionCode();
                Log.i("Stone", "ActionCode : " + actionCode);
                // ** ADD
                bd.addAutorizacoesPinPad(new AutorizacoesPinpad(
                        String.valueOf(transactionId),
                        "",
                        String.valueOf(Objects.requireNonNull(to).getIdFromBase()),
                        to.getAmount(),
                        to.getRequestId(),
                        to.getEmailSent(),
                        to.getTimeToPassTransaction(),
                        to.getInitiatorTransactionKey(),
                        to.getRecipientTransactionIdentification(),
                        to.getCardHolderNumber(),
                        to.getCardHolderName(),
                        to.getDate(),
                        to.getTime(),
                        to.getAid(),
                        to.getArcq(),
                        to.getAuthorizationCode(),
                        to.getIccRelatedData(),
                        to.getTransactionReference(),
                        to.getActionCode(),
                        to.getCommandActionCode(),
                        to.getPinpadUsed(),
                        to.getSaleAffiliationKey(),
                        to.getCne(),
                        to.getCvm(),
                        to.getBalance(),
                        to.getServiceCode(),
                        to.getSubMerchantCategoryCode(),
                        String.valueOf(to.getEntryMode()),
                        String.valueOf(to.getCardBrand()),
                        String.valueOf(to.getInstalmentTransaction()),
                        String.valueOf(to.getTransactionStatus()),
                        String.valueOf(to.getInstalmentType()),
                        String.valueOf(to.getTypeOfTransactionEnum()),
                        "",//String.valueOf(to.getSignature())
                        String.valueOf(to.getCancellationDate()),
                        String.valueOf(to.isCapture()),
                        to.getShortName(),
                        to.getSubMerchantAddress(),
                        "",//to.getUserModel().toString()
                        String.valueOf(to.isFallbackTransaction()),
                        to.getAppLabel(),
                        to.getUserModel().getMerchantName(),
                        to.getUserModel().getMerchantAddress().getCity() + "/" + to.getUserModel().getMerchantAddress().getDistric(),
                        to.getUserModel().getMerchantDocumentNumber()
                ));

                msg(true);
            }
            // OUTROS STATUS DO PAGAMENTO
            else {
                if (to.getTransactionStatus() == TransactionStatusEnum.UNKNOWN) {
                    txtMsgCausaErro.setText(String.format("%s\n%s", to.getActionCode(), "Ocorreu um erro antes de ser enviada para o autorizador."));
                }
                //
                else if (to.getTransactionStatus() == TransactionStatusEnum.DECLINED) {
                    txtMsgCausaErro.setText(String.format("%s\n%s", to.getActionCode(), "Transação negada."));
                }
                //
                else if (to.getTransactionStatus() == TransactionStatusEnum.DECLINED_BY_CARD) {
                    txtMsgCausaErro.setText(String.format("%s\n%s", to.getActionCode(), "Transação negada pelo cartão."));
                }
                //
                /*else if (to.getTransactionStatus() == TransactionStatusEnum.PARTIAL_APPROVED) {
                    txtMsgCausaErro.setText(String.format("%s\n%s", to.getActionCode(), "Transação foi parcialmente aprovada."));
                }*/
                //
                else if (to.getTransactionStatus() == TransactionStatusEnum.TECHNICAL_ERROR) {
                    txtMsgCausaErro.setText(String.format("%s\n%s", to.getActionCode(), "Erro técnico (ocorreu um erro ao processar a mensagem no autorizador)."));
                }
                //
                else if (to.getTransactionStatus() == TransactionStatusEnum.REJECTED) {
                    txtMsgCausaErro.setText(String.format("%s\n%s", to.getActionCode(), "Transação rejeitada."));
                }
                //
                else if (to.getTransactionStatus() == TransactionStatusEnum.WITH_ERROR) {
                    txtMsgCausaErro.setText(String.format("%s\n%s", to.getActionCode(), "Transação não completada com sucesso. O Provedor de Reversão irá desfazer as transações com este status."));
                }

                //logCC.enviarLogCartao(prefs.getString("serial", ""), transactionObject.toString());
                new LogCartaoControllerKT().enviarLogCartao(prefs.getString("serial_app", ""), transactionObject.toString());

                msg(false);
            }
            // Transação enviada com sucesso e salva no banco. Para acessar, use o TransactionDAO

            /*/
            transactionDAO = new TransactionDAO(context);
            // Pega o id da última transação
            transactionId = transactionDAO.getLastTransactionId();
            // Pega os dados da última transação
            //transactionObject = transactionDAO.findTransactionWithId(transactionId);
            Log.i("Stone", String.valueOf(transactionDAO.findTransactionWithId(transactionId)));
            Log.i("PinPad 2", String.valueOf(transactionDAO.findTransactionWithId(transactionId)));

            TransactionObject to = transactionDAO.findTransactionWithId(transactionId);

            if (to.getTransactionStatus() == TransactionStatusEnum.APPROVED) {

                // **
                String actionCode = Objects.requireNonNull(to).getActionCode();
                if (actionCode.equalsIgnoreCase("0000") ||
                        actionCode.equalsIgnoreCase("0001") ||
                        actionCode.equalsIgnoreCase("0002") ||
                        actionCode.equalsIgnoreCase("0003") ||
                        actionCode.equalsIgnoreCase("0004")
                ) {
                    // ** ADD
                    bd.addAutorizacoesPinPad(new AutorizacoesPinpad(
                            String.valueOf(transactionId),
                            "",
                            String.valueOf(Objects.requireNonNull(to).getIdFromBase()),
                            to.getAmount(),
                            to.getRequestId(),
                            to.getEmailSent(),
                            to.getTimeToPassTransaction(),
                            to.getInitiatorTransactionKey(),
                            to.getRecipientTransactionIdentification(),
                            to.getCardHolderNumber(),
                            to.getCardHolderName(),
                            to.getDate(),
                            to.getTime(),
                            to.getAid(),
                            to.getArcq(),
                            to.getAuthorizationCode(),
                            to.getIccRelatedData(),
                            to.getTransactionReference(),
                            to.getActionCode(),
                            to.getCommandActionCode(),
                            to.getPinpadUsed(),
                            to.getSaleAffiliationKey(),
                            to.getCne(),
                            to.getCvm(),
                            to.getBalance(),
                            to.getServiceCode(),
                            to.getSubMerchantCategoryCode(),
                            String.valueOf(to.getEntryMode()),
                            String.valueOf(to.getCardBrand()),
                            String.valueOf(to.getInstalmentTransaction()),
                            String.valueOf(to.getTransactionStatus()),
                            String.valueOf(to.getInstalmentType()),
                            String.valueOf(to.getTypeOfTransactionEnum()),
                            "",//String.valueOf(to.getSignature())
                            String.valueOf(to.getCancellationDate()),
                            String.valueOf(to.isCapture()),
                            to.getShortName(),
                            to.getSubMerchantAddress(),
                            "",//to.getUserModel().toString()
                            String.valueOf(to.isFallbackTransaction()),
                            to.getAppLabel(),
                            to.getUserModel().getMerchantName(),
                            to.getUserModel().getMerchantAddress().getCity() + "/" + to.getUserModel().getMerchantAddress().getDistric(),
                            to.getUserModel().getMerchantDocumentNumber()
                    ));

                    msg(true);
                }
            } else {
                //
                //provider.getMessageFromAuthorize();
                if (to.getActionCode().equalsIgnoreCase("1016")) {
                    txtMsgCausaErro.setText(String.format("%s\n%s", to.getActionCode(), provider.getMessageFromAuthorize()));//Saldo insuficiente
                }
                //
                else if (to.getActionCode().equalsIgnoreCase("1017")) {
                    txtMsgCausaErro.setText(String.format("%s\nSenha inválida", to.getActionCode()));
                }
                //
                else {
                    txtMsgCausaErro.setText(to.getActionCode());
                }
                msg(false);
            }*/
            //});
        });
    }

    @Override
    public void onError() {
        // Erro na transação
        //msg(false);
        AsyncTask.execute(() -> {
            try {

                transactionDAO = new TransactionDAO(context);
                // Pega o id da última transação
                transactionId = transactionDAO.getLastTransactionId();
                // Pega os dados da última transação
                TransactionObject to = transactionDAO.findTransactionWithId(transactionId);

                runOnUiThread(() -> {
                    btnEnviarTrazacao.setVisibility(View.GONE);
                    Log.e("StoneTO", to.toString());
                    llProcessandoPagamento.setVisibility(View.GONE);
                    llErroDiversoPg.setVisibility(View.VISIBLE);

                    //
                    ActionCodeStone acs = new ActionCodeStone();
                    String tipoErro = to.getTransactionStatus().name();
                    txtErroDiverso.setText(tipoErro);
                    txtMsgErro.setText(acs.getError(tipoErro));

                    // ENVIAR LOG PARA O SERVIDOR
                    new LogCartaoControllerKT().enviarLogCartao(prefs.getString("serial_app", ""), transactionObject.toString());
                });
                // Erro na transação
                //msg(false);
            } catch (Exception e) {
                msg(false);
            }
        });
    }

    /*@Override
    public void onStatusChanged(Action action) {
        switch (action){
            case TRANSACTION_WAITING_CARD:
                txtStatusPagamento.append("Aguardando o cartão ser inserido" + "\n");
                break;
            case TRANSACTION_WAITING_PASSWORD:
                txtStatusPagamento.append("Aguardando a senha do cartão" + "\n");
                break;
            case TRANSACTION_SENDING:
                txtStatusPagamento.append("Enviando a transação para a Stone" + "\n");
                break;
            case TRANSACTION_REMOVE_CARD:
                txtStatusPagamento.append("Aguardando o cartão ser retirado" + "\n");
                break;
            case TRANSACTION_CARD_REMOVED:
                txtStatusPagamento.append("Indica que o cartão foi removido" + "\n");
                break;
            case REVERSING_TRANSACTION_WITH_ERROR:
                txtStatusPagamento.append("Tentando reverter transação com status WITH_ERROR" + "\n");
                break;
        }
        //txtStatusPagamento.append(action.name() + "\n");
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError() {

        Toast.makeText(context, "Erro: " + provider.getListOfErrors(), Toast.LENGTH_SHORT).show();
    }*/
}


// ** MODULO BLUETOOTH ** //
/*

    // BLUETOOTH **
    private void ativarBluetooth() {
        new AtivarDesativarBluetooth().enableBT();
    }

    // BLUETOOTH **
    private synchronized void waitForConnection() {
        //status(null);

        closeActiveConnection();

        // Show dialog to select a Bluetooth device.
        startActivityForResult(new Intent(this, DeviceListActivity.class), REQUEST_GET_DEVICE);
    }

    // BLUETOOTH **
    private void establishBluetoothConnection(final String address) {
        final ProgressDialog dialog = new ProgressDialog(GerenciarPagamentoCartao.this);
        dialog.setTitle(getString(R.string.title_please_wait));
        dialog.setMessage(getString(R.string.msg_connecting));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        final Thread t = new Thread(() -> {
            Timber.d("BluetoothConnection - Conectando à " + address + "...");

            btAdapter.cancelDiscovery();

            try {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                BluetoothDevice btDevice = btAdapter.getRemoteDevice(address);

                try {
                    BluetoothSocket btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
                    btSocket.connect();

                    mBtSocket = btSocket;
                } catch (IOException e) {
                    error("Falhou ao conectar: " + e.getMessage());
                    waitForConnection();
                    return;
                }

            } finally {
                dialog.dismiss();
            }
        });
        t.start();
    }

    // BLUETOOTH **
    private synchronized void closeActiveConnection() {
        closeBluetoothConnection();
    }

    private synchronized void closeBluetoothConnection() {
        // Close Bluetooth connection
        BluetoothSocket s = mBtSocket;
        mBtSocket = null;
        if (s != null) {
            Timber.d("Close Bluetooth socket");
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
*/

// ** FIM DO MODULO BLUETOOTH ** //