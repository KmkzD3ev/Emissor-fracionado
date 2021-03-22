package br.com.zenitech.emissorweb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import br.com.zenitech.emissorweb.domains.AutorizacoesPinpad;
import br.com.zenitech.emissorweb.domains.Unidades;
import stone.application.StoneStart;
import stone.application.enums.CardBrandEnum;
import stone.application.enums.EntryMode;
import stone.application.enums.InstalmentTransactionEnum;
import stone.application.enums.TransactionStatusEnum;
import stone.application.enums.TypeOfTransactionEnum;
import stone.application.interfaces.StoneCallbackInterface;
import stone.application.xml.enums.InstalmentTypeEnum;
import stone.database.transaction.TransactionDAO;
import stone.database.transaction.TransactionObject;
import stone.environment.Environment;
import stone.providers.ActiveApplicationProvider;
import stone.providers.CancellationProvider;
import stone.providers.TransactionProvider;
import stone.user.UserModel;
import stone.utils.Stone;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static br.com.zenitech.emissorweb.GerenciarPagamentoCartao.getApplicationName;

public class CancelarPagamentoCartao extends AppCompatActivity {

    // ** STONE MODULO **
    TransactionProvider provider;
    TransactionObject transactionObject;
    TransactionDAO transactionDAO;

    String STONE_CODE;
    //ZENITECH TESTE - String STONE_CODE = "177391172";
    //String STONE_CODE = "111111111";
    /*// Pedido para obter o dispositivo bluetooth
    private static final int REQUEST_GET_DEVICE = 0;
    // Pedido para obter o dispositivo bluetooth
    private static final int DEFAULT_NETWORK_PORT = 9100;
    private BluetoothSocket mBtSocket;*/

    ArrayList<Unidades> elementos;
    Unidades unidades;
    //
    Context context;
    List<UserModel> userList;
    private DatabaseHelper bd;
    AutorizacoesPinpad pinpad;

    ClassAuxiliar cAux;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancelar_pagamento_cartao);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        context = this;
        bd = new DatabaseHelper(this);
        cAux = new ClassAuxiliar();
        pinpad = bd.getAutorizacaoPinpad();

        elementos = bd.getUnidades();
        unidades = elementos.get(0);
        STONE_CODE = unidades.getCodloja();




        /*transactionObject.setActionCode("");
        transactionObject.setAid("");
        transactionObject.setAmount("");
        transactionObject.setAppLabel("");
        transactionObject.setArcq("");
        transactionObject.setActionCode("");
        transactionObject.setBalance("");
        transactionObject.setAuthorizationCode("");
        transactionObject.setCancellationDate(new Date());
        transactionObject.setCapture(true);
        transactionObject.setCardBrand(Enum.valueOf(CardBrandEnum.class, ""));
        transactionObject.setCardExpireDate("");
        transactionObject.setCardHolderName("");
        transactionObject.setCardHolderNumber("");
        transactionObject.setCardSequenceNumber("");
        transactionObject.setCne("");
        transactionObject.setCommandActionCode("");
        transactionObject.setCvm("");
        transactionObject.setCvv("");
        transactionObject.setDate("");
        transactionObject.setEmailSent("");
        transactionObject.setEntryMode(EntryMode.valueOf(""));
        transactionObject.setExternalId("");
        transactionObject.setFallbackTransaction(false);
        transactionObject.setIccRelatedData("");
        transactionObject.setIdFromBase(0);*/
        iniciarStone();

        findViewById(R.id.fab).setOnClickListener(view -> iniciarTranzacao());
    }

    // Iniciar o Stone
    void iniciarStone() {
        // O primeiro passo é inicializar o SDK.
        StoneStart.init(context);
        /*Em seguida, é necessário chamar o método setAppName da classe Stone,
        que recebe como parâmetro uma String referente ao nome da sua aplicação.*/
        Stone.setAppName(getApplicationName(context));
        //Ambiente de Sandbox "Teste"
        Stone.setEnvironment(new Configuracoes().Ambiente());

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
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
            //activeApplicationProvider.useDefaultUI(true);
            activeApplicationProvider.setConnectionCallback(new StoneCallbackInterface() {

                public void onSuccess() {
                    // SDK ativado com sucesso
                    _pinpadAtivado();
                }

                public void onError() {
                    // Ocorreu algum erro na ativação
                    Toast.makeText(context, "Ocorreu algum erro na ativação", Toast.LENGTH_SHORT).show();
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
        iniciarTranzacao();
    }

    //
    int transactionId;

    void iniciarTranzacao() {
        // Verifica se o bluetooth esta ligado e se existe algum pinpad conectado.
        if (Stone.getPinpadListSize() > 0) {
            // Cria o DAO object
            transactionDAO = new TransactionDAO(context);
            // Pega o id da última transação
            transactionId = transactionDAO.getLastTransactionId();
            // Cria o TransactionObject da última transação
            TransactionObject to = transactionDAO.findTransactionWithId(transactionId);
            // Chama o cancelamento passando o TransactionObject
            cancelarPagamento(to);
        } else {
            Intent devicesIntent = new Intent(context, DevicesActivityPinPad.class);
            startActivity(devicesIntent);
        }
    }

    // Cancelar Pagamento
    private void cancelarPagamento(TransactionObject transactionObject) {
        final CancellationProvider provider = new CancellationProvider(context, transactionObject);

        provider.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                //Transação Cancelada com sucesso
                toastMsg("Transação Cancelada com sucesso");
                //

                // Cria o DAO object
                transactionDAO = new TransactionDAO(context);
                // Pega o id da última transação
                transactionId = transactionDAO.getLastTransactionId();
                // Cria o TransactionObject da última transação
                TransactionObject to = transactionDAO.findTransactionWithId(transactionId);
                Log.i("Cancelar", Objects.requireNonNull(to).getCancellationDate().toString());

                String dataHora = cAux.exibirData(to.getDate()) + " " + to.getTime();

                Intent i = new Intent(context, Impressora.class);
                i.putExtra("imprimir", "comprovante_cancelamento");
                i.putExtra("dataHoraCan", dataHora);
                i.putExtra("codAutCan", to.getAuthorizationCode());
                startActivity(i);
                //finish();
            }

            @Override
            public void onError() {
                //Ocorreu um erro no cancelamento da transacao
                //Método que retorna o código referente ao erro da operação
                //getActionCode();
                toastMsg("Ocorreu um erro no cancelamento da transacao");

                //Log.i("Stone", getActionCode());
            }
        });
        provider.execute();
    }

    private void toastMsg(String msg) {
        Toast toast = makeText(context, msg, LENGTH_SHORT);
        toast.setGravity(1, 0, 0);
        toast.show();
    }
}