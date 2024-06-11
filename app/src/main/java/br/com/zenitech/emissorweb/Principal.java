package br.com.zenitech.emissorweb;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.zenitech.emissorweb.adapters.PedidosAdapter;
import br.com.zenitech.emissorweb.domains.DomainPrincipal;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PedidosNFE;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.Sincronizador;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.interfaces.IPrincipal;
import br.com.zenitech.emissorweb.interfaces.ISincronizar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import stone.application.StoneStart;
import stone.user.UserModel;

public class Principal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private final String TAG = "Principal";
    private DatabaseHelper bd;
    ClassAuxiliar aux;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;
    AlertDialog alerta;
    VerificarOnline verificarOnline;
    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    ArrayList<PedidosNFE> pedidosNFE;
    private ArrayList<Pedidos> pedidos;
    PedidosAdapter adapter;
    RecyclerView recyclerView;
    private Context context;
    ArrayList<Pedidos> elementosPedidos;
    ArrayList<ItensPedidos> elementosItens;
    ArrayList<PosApp> elementosPos;
    PosApp posApp;
    ArrayList<Unidades> elementosUnidades;
    Unidades unidades;
    LinearLayout btnSincronizarNotasPrincipal;
    ImageView imgEmissor;
    TextView txtNFCeVinculada, txtTotMemoria;
    RelativeLayout LLlistaPedidos;
    List<UserModel> userList;
    TextView textView, txtTransmitida, txtContigencia, txtStatusTransmissao, txtVersao, txtEmpresa, txtCodUnidade, txtDataUltimoSinc;
    AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.principal);
        setSupportActionBar(toolbar);

        //
        context = this;
        verificarOnline = new VerificarOnline();

        imgEmissor = findViewById(R.id.imgEmissor);
        LLlistaPedidos = findViewById(R.id.LLlistaPedidos);

        txtNFCeVinculada = findViewById(R.id.txtNFCeVinculada);
        txtTotMemoria = findViewById(R.id.txtTotMemoria);
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable;
        bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        long megAvailable = bytesAvailable / (1024 * 1024);
        if (megAvailable < 50) {
            txtTotMemoria.setText("Atenção:\nSeu aparelho está com pouca memória! \nPara um bom funcionamento do App Emissor, libere mais espaço na memória interna o quanto antes.");
        }

        //
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();

        //
        bd = new DatabaseHelper(this);
        aux = new ClassAuxiliar();

        //
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // KLEILSON - ATENÇÃO PARA AESSE CÓDIGO ELE PODE ESTAR DANIFICANDO OS PEDIDOS
        try {
            //
            pedidos = bd.getPedidos();

            if (pedidos.size() != 0) {
                for (int i = 0; i < pedidos.size(); i++) {
                    //Verifica os pedidos sem itens
                    elementosItens = bd.getItensPedido(pedidos.get(i).getId());

                    if (elementosItens.size() == 0) {
                        bd.deletePedido(pedidos.get(i).getId());
                    }
                }
            }
        } catch (Exception e) {
            //Timber.d(Objects.requireNonNull(e.getMessage()));

            //APAGA O BANCO DE DADOS E VAI PARA TELA INICIAL DE SINCRONIZAÇÃO
            context.deleteDatabase("emissorwebDB");

            //ABRI A TELA DE SINCRONIZAR
            Intent i = new Intent(Principal.this, Sincronizar.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

            finish();
        }

        pedidos = bd.getPedidos();
        adapter = new PedidosAdapter(this, pedidos);
        recyclerView.setAdapter(adapter);

        // NAVIGATION DRAWER
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // BOTTOM NAVIGATION

        BottomNavigationView navView = findViewById(R.id.nav_view_b);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        //
        elementosPos = bd.getPos();
        posApp = elementosPos.get(0);
        if (posApp.getNota_remessa() != null) {
            txtNFCeVinculada.setText(String.format("Vinculado à NFC-e: %s", posApp.getNota_remessa()));
        }
        elementosPedidos = bd.getPedidosTransmitirFecharDia();
        elementosUnidades = bd.getUnidades();
        unidades = elementosUnidades.get(0);
        Objects.requireNonNull(getSupportActionBar()).setTitle("PRINCIPAL");
        btnSincronizarNotasPrincipal = findViewById(R.id.btnSincronizarNotasPrincipal);
        if (elementosPedidos.size() != 0) {
            btnSincronizarNotasPrincipal.setVisibility(View.VISIBLE);
        }
        findViewById(R.id.btnSincronizarNotasNFC).setOnClickListener(v2 -> {
            //
            Intent i = new Intent(context, GerenciarBancoProducao.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        // SALVAR IMPRESSORA

        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                if (!Objects.requireNonNull(prefs.getString("enderecoBlt", "")).equalsIgnoreCase(params.getString("enderecoBlt"))) {
                    prefs.edit().putBoolean("naoPerguntarImpressora", false).apply();
                }

                if (!Objects.requireNonNull(params.getString("enderecoBlt")).equalsIgnoreCase("") &&
                        !prefs.getBoolean("naoPerguntarImpressora", false)) {

                    if (!Objects.requireNonNull(prefs.getString("enderecoBlt", "")).equalsIgnoreCase(params.getString("enderecoBlt"))) {
                        callDialog(params.getString("enderecoBlt"));
                    }
                }
            }
        }


        // DESATIVAR O MENU DA STONE CASO NÃO TENHA STONE CODE
        if (unidades.getCodloja().equalsIgnoreCase("")) {
            Menu menu = navigationView.getMenu();
            for (int menuItemIndex = 0; menuItemIndex < menu.size(); menuItemIndex++) {
                MenuItem menuItem = menu.getItem(menuItemIndex);
                if (menuItem.getItemId() == R.id.menuStone) {
                    menuItem.setVisible(false);
                }
            }
        }

        //
        findViewById(R.id.btnNovaNfcePrinc).setOnClickListener(view -> NovaNFCe());
        txtEmpresa = findViewById(R.id.txtEmpresa);
        txtCodUnidade = findViewById(R.id.txtCodUnidade);
        txtEmpresa.setText(unidades.getRazao_social());
        txtCodUnidade.setText(posApp.getUnidade());
        //
        textView = findViewById(R.id.text_home);
        txtTransmitida = findViewById(R.id.txtTransmitida);
        txtContigencia = findViewById(R.id.txtContigencia);
        txtStatusTransmissao = findViewById(R.id.txtStatusTransmissao);
        textView.setText(String.format("%s | %s", posApp.getSerial(), posApp.getSerie()));
        txtVersao = findViewById(R.id.txtVersao);
        //txtVersao.setText(String.format("Versão %s", BuildConfig.VERSION_NAME));
        txtVersao.setText(BuildConfig.VERSION_NAME);
        txtDataUltimoSinc = findViewById(R.id.txtDataUltimoSinc);
        txtDataUltimoSinc.setText(prefs.getString("data_sincronizado", ""));

        if (unidades.getUf().equalsIgnoreCase(new Configuracoes().GetUFCeara())) {
            txtStatusTransmissao.setText("Não Transmitida(s)");
        } else {
            txtStatusTransmissao.setText("Contigências");
        }

        //
        try {
            userList = StoneStart.init(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // APAGA TODOS OS PAGAMENTOS PIX COM STATUS 1
        bd.deleteFormPagPIX();

        atualizar();
    }


    @Override
    protected void onResume() {
        super.onResume();
        totNFE();
        atualizar();
    }

    // GRAVAR NOVO PEDIDO NO BANCO DE DADOS
    private void NovaNFCe() {
        try {
            bd.testeCampoDesconto();
        } catch (Exception e) {
            alertaAtualizarBancoDeDados();
            Log.e("testeCampoDesconto:", e.getMessage());
            return;
        }

        Intent i = new Intent(context, FormPedidos.class);
        i.putExtra("EditarProduto", false);
        startActivity(i);
        finish();
        //startActivity(new Intent(context, FinanceiroNFCe.class));
    }

    private void alertaAtualizarBancoDeDados() {

        //
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_emissor_web);
        builder.setCancelable(true);
        //define o titulo
        builder.setTitle("Atenção!");
        //define a mensagem
        builder.setMessage("Uma nova versão do banco de dados do app está disponível, pedimos que atualize antes de iniciar uma nova NFC-e!");

        if (bd.getPedidosTransmitirFecharDia().size() == 0) {
            //define um botão como positivo
            builder.setPositiveButton("Finalizar Remessa e Atualizar", (arg0, arg1) -> {
                _finalizarApp(true);
            });
        } else {

            //define um botão como negativo.
            builder.setNegativeButton("Sincronizar NFC-e", (arg0, arg1) -> {
                Intent i = new Intent(context, GerenciarBancoProducao.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            });
        }
        alerta = builder.create();
        alerta.show();
    }

    private void totNFE() {
        //ArrayList<PedidosNFE> pedidosNFE = bd.getPedidosNFE();
        //PedidosNFE p = pedidosNFE.get(0);
        //Toast.makeText(context, pedidosNFE.size() + " | " + p.getId(), Toast.LENGTH_LONG).show();
    }

    private void atualizar() {
        //bd.PedidoFracionadosSemFinanceiro();
        bd.listPedidosSemPagamento();

        //
        pedidos = bd.getPedidos();
        elementosPedidos = bd.getPedidosTransmitirFecharDia();
        btnSincronizarNotasPrincipal = findViewById(R.id.btnSincronizarNotasPrincipal);
        txtTransmitida.setText(String.valueOf(pedidos.size() - elementosPedidos.size()));

        if (elementosPedidos.size() != 0) {
            txtContigencia.setText(String.valueOf(elementosPedidos.size()));
            btnSincronizarNotasPrincipal.setVisibility(View.VISIBLE);
        } else {
            txtContigencia.setText(String.valueOf(elementosPedidos.size()));
            btnSincronizarNotasPrincipal.setVisibility(View.GONE);
        }

        _pedidoFinaceiroDiferente();
    }

    private void pedidoNaoFinalizadoDialog() {

        //
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_emissor_web);
        builder.setCancelable(false);
        //define o titulo
        builder.setTitle("Atenção!");
        //define a mensagem
        builder.setMessage("Encontramos um pedido não finalizado!");

        //define um botão como positivo
        builder.setPositiveButton("Finalizar Pedido", (arg0, arg1) -> {
            //Intent i = new Intent(Principal.this, EditarPedido.class);
            //Intent i = new Intent(context, FinanceiroNFCe.class);
            Intent i = new Intent(this, FormPedidos.class);
            i.putExtra("EditarProduto", true);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);

            finish();
        });
        alerta = builder.create();
        alerta.show();
    }

    private void _pedidoFinaceiroDiferente() {
        //ABRI A TELA DE SINCRONIZAR
        try {
            //getVerificarFinanceiroUltimoPedido
            //asfdsfdsfasd
            if (bd.getFinanceiroUltimoPedido(bd.getIdPedidoTemp())) {
                //makeText(context, "Teste Edit", LENGTH_SHORT).show();
                pedidoNaoFinalizadoDialog();
            }
        } catch (Exception ignored) {

        }
    }

    // VERIFICA SE EXISTE NOTA PENDENTE DE SINCRONISMO
    private boolean _verificarNotasPendentes() {
        //
        elementosPedidos = bd.getPedidosTransmitirFecharDia();

        if (elementosPedidos.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    // VERIFICA SE EXISTE PEDIDOS COM FINANCEIRO DIFERENTE PARA FINALIZAR
    // KLEILSON
    private boolean _verificarFinanceiroUltimoPedido() {
        //
        //elementosPedidos = bd.getVerificarFinanceiroUltimoPedido();

        if (elementosPedidos.size() == 0) {
            return false;
        } else {
            return true;
        }
    }

    private void callDialog(String impressora) {

        //
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("Nova Impressora");
        //define a mensagem
        builder.setMessage("Deseja salvar como impressora padrão do app?");

        //define um botão como positivo
        builder.setPositiveButton("Sim", (arg0, arg1) -> {
            prefs.edit().putString("enderecoBlt", impressora).apply();
        });

        //define um botão como negativo.
        builder.setNegativeButton("Não", (arg0, arg1) -> {
            //Toast.makeText(InformacoesVagas.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
            prefs.edit().putBoolean("naoPerguntarImpressora", true).apply();
        });

        //define um botão como negativo.
        builder.setNeutralButton("Depois", (arg0, arg1) -> {
            //Toast.makeText(InformacoesVagas.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
            //prefs.edit().putBoolean("naoPerguntarImpressora", true).apply();
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();
    }

    @Override
    public void onBackPressed() {
        //DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_filtrar_pedidos:

                break;
            case R.id.action_intro_principal:
                introducao();
                break;
        }

        return super.onOptionsItemSelected(item);
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

        final SpannableString sassyDesc = new SpannableString("Abra o menu para ver mais opções, como Nova Nota, Relatórios e Sincronizar NFC-e.");
        sassyDesc.setSpan(new StyleSpan(Typeface.ITALIC), 0, sassyDesc.length(), 0);


        // We have a sequence of targets, so lets build it!
        final TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        // BOTÃO DO MENU
                        TapTarget.forToolbarNavigationIcon(toolbar, "Menu", sassyDesc).id(1),

                        //
                        TapTarget.forToolbarOverflow(toolbar, "Toque", "Abre outras opções")
                                .id(2)/*,

                        // BOTAO NOVO PEDIDO
                        TapTarget.forView(findViewById(R.id.fabNovoPedido), "Novo Pedido", "Toque para iniciar um novo pedido!")
                                .dimColor(android.R.color.black)
                                .outerCircleColor(R.color.colorAccent)
                                .targetCircleColor(android.R.color.black)
                                .textColor(android.R.color.white)
                                .transparentTarget(true)
                                .id(3)*/
                )
                .listener(new TapTargetSequence.Listener() {
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
                        final AlertDialog dialog = new AlertDialog.Builder(Principal.this)
                                .setTitle("Uh oh")
                                .setMessage("Você cancelou a seqüência")
                                .setPositiveButton("Sair", null).show();
                        TapTargetView.showFor(dialog,
                                TapTarget.forView(dialog.getButton(DialogInterface.BUTTON_POSITIVE), "Uh oh!", "Você cancelou a seqüência no passo " + lastTarget.id())
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
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            NovaNFCe();
            //startActivity(new Intent(getBaseContext(), FormPedidos.class));
        } else if (id == R.id.nav_nfe) {
            if (verificarOnline.isOnline(context)) {
                _verificarPermNFE();
            } else {
                Toast.makeText(context, "Verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.nav_consultar_nfe) {
            //
            startActivity(new Intent(context, ConsultarNFE.class));
        } else if (id == R.id.nav_gallery) {
            startActivity(new Intent(getBaseContext(), Relatorios.class));
        } else if (id == R.id.nav_slideshow) {
            //
            Intent i = new Intent(context, GerenciarBancoProducao.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } else if (id == R.id.nav_status) {
            pedidosNFE = bd.getPedidosNFE();
            //
            if (pedidos.size() > 0 || pedidosNFE.size() > 0) {
                startActivity(new Intent(context, Status.class));
            } else {
                Toast.makeText(context, "Nenhuma NFC-e e/ou NF-e, foi emitida até o momento! Volte aqui mais tarde.", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.nav_finalizar) {
            // VERIFICA SE EXISTE NFC-e PENDENTE
            if (_verificarNotasPendentes()) {
                //
                alertaNotasPendentes();
            } else {
                //
                alertaFinalizar();
            }
        } else if (id == R.id.nav_suporte) {
            if (verificarOnline.isOnline(context)) {
                startActivity(new Intent(context, Suporte.class));
            } else {
                Toast.makeText(context, "Verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.nav_sobre) {
            //
            startActivity(new Intent(context, Sobre.class));
        } else if (id == R.id.nav_configuracoes) {
            //
            startActivity(new Intent(context, ConfiguracoesApp.class));
        }
        // Reimprimir comprovante pagamento logista
        else if (id == R.id.nav_reimprimir) {
            if (bd.getAutorizacaoPinpad() != null) {
                Configuracoes configuracoes = new Configuracoes();
                if (configuracoes.GetDevice()) {
                    Intent i = new Intent(context, ImpressoraPOS.class);
                    i.putExtra("imprimir", "reimpressao_comprovante");
                    startActivity(i);
                } else {
                    //
                    Intent i = new Intent(context, Impressora.class);
                    i.putExtra("imprimir", "reimpressao_comprovante");
                    startActivity(i);
                }
            } else
                makeText(context, "Nada para imprimir", LENGTH_SHORT).show();
        }
        // Reimprimir comprovante pagamento logista
        else if (id == R.id.nav_reimprimir_pix) {
            Configuracoes configuracoes = new Configuracoes();

            if (bd.ultimoPIX() != null) {
                Intent i;
                if (configuracoes.GetDevice())
                    i = new Intent(context, ImpressoraPOS.class);
                else
                    i = new Intent(context, Impressora.class);
                i.putExtra("imprimir", "comprovante_pix_reimp");
                i.putExtra("impressao_pix", false);
                startActivity(i);
            } else
                makeText(context, "Nada para imprimir", LENGTH_SHORT).show();
        }
        // Cancelar pagamento cartão
        else if (id == R.id.nav_cancelar_pag) {
            if (bd.getAutorizacaoPinpad() == null) {
                Toast.makeText(context, "Primeiro realize um pagamento com cartão para poder cancelar!", Toast.LENGTH_SHORT).show();
            } else {
                Intent i;
                i = new Intent(context, Seguranca.class);

                startActivity(i);

            /*
            Intent i;
            Configuracoes configuracoes = new Configuracoes();
            if (configuracoes.GetDevice())
                i = new Intent(context, CancelarPagamentoCartaoPOS.class);
            else
                i = new Intent(context, CancelarPagamentoCartao.class);

            startActivity(i);
             */
            }
        }

        //DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void alertaNotasPendentes() {

        //
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("Atenção!");
        String str = "Existe nota(s) pendente(s) pendente de sincronismo!";
        //define a mensagem
        builder.setMessage(str);

        //define um botão como positivo
        builder.setPositiveButton("SINCRONIZAR AGORA", (arg0, arg1) -> {
            //
            Intent i = new Intent(context, GerenciarBancoProducao.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();
    }

    private void alertaFinalizar() {

        //
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("Atenção!");
        String str = "Ao finalizar a remessa, todas as notas serão apagadas deste dispositivo. Esta ação não poderá ser desfeita.\n\nDeseja realmente finalizar a remessa agora?";
        //define a mensagem
        builder.setMessage(str);

        //define um botão como positivo
        builder.setPositiveButton("Sim", (arg0, arg1) -> _finalizarApp(false));

        //define um botão como negativo.
        builder.setNeutralButton("Não", (arg0, arg1) -> {
            //Toast.makeText(context, "Ok", Toast.LENGTH_SHORT).show();
        });

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();
    }

    // VERIFICA SE O SERIAL PODE EMITIR NFE
    private void _verificarPermNFE() {
        //
        final IPrincipal iPrincipal = IPrincipal.retrofit.create(IPrincipal.class);

        final Call<DomainPrincipal> call = iPrincipal.verificarPermNFE("600", prefs.getString("serial_app", ""));

        call.enqueue(new Callback<DomainPrincipal>() {
            @Override
            public void onResponse(@NonNull Call<DomainPrincipal> call, @NonNull Response<DomainPrincipal> response) {

                //
                final DomainPrincipal principal = response.body();
                if (principal != null) {
                    if (principal.getErro().equalsIgnoreCase("OK")) {
                        runOnUiThread(() -> startActivity(new Intent(getBaseContext(), FormPedidosNFE.class)));
                    } else {
                        runOnUiThread(() -> Toast.makeText(context, "MODULO NÃO HABILITADO", Toast.LENGTH_LONG).show());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DomainPrincipal> call, @NonNull Throwable t) {
                Log.i(TAG, Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    // FINALIZAR REMESSA
    private void _finalizarApp(boolean initAuto) {
        //
        final ISincronizar iSincronizar = ISincronizar.retrofit.create(ISincronizar.class);

        final Call<Sincronizador> call = iSincronizar.ativarDesativarPOS("desativar", prefs.getString("serial_app", ""));

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Sincronizador> call, @NonNull Response<Sincronizador> response) {
                if (response.isSuccessful()) {
                    //
                    final Sincronizador sincronizacao = response.body();
                    if (sincronizacao != null) {

                        Log.i(TAG, sincronizacao.getErro());

                        if (sincronizacao.getErro().equalsIgnoreCase("0")) {

                            //
                            runOnUiThread(() -> {

                                prefs.edit().putBoolean("sincronizado", false).apply();
                                prefs.edit().putInt("id_pedido", 0).apply();

                                Toast.makeText(context, "Remessa finalizada com sucesso!", Toast.LENGTH_LONG).show();

                                //APAGA O BANCO DE DADOS E VAI PARA TELA INICIAL DE SINCRONIZAÇÃO
                                bd.FecharConexao(context);

                                //Intent i = new Intent(context, Sincronizar.class);
                                Intent i = new Intent(context, AppFinalizado.class);
                                if (initAuto) {
                                    i.putExtra("initAuto", true);
                                }
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            });
                        } else {
                            Toast.makeText(getBaseContext(), "Não conseguimos finalizar, tente novamente.", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    // A requisição não foi bem-sucedida, trate o erro conforme necessário
                    try {
                        String errorMessage = "Erro: " + response.errorBody().string() + "\nMensagem: " + response.message() + "\n" + response.code();
                        Log.e("FinalizarRemessa", "Erro na requisição: " + errorMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Sincronizador> call, @NonNull Throwable t) {
                Log.i("ERRO_SIN", Objects.requireNonNull(t.getMessage()));

                Toast.makeText(getBaseContext(), "Não conseguimos finalizar, tente novamente.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
