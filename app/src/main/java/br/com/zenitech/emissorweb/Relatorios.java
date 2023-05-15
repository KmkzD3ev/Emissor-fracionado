package br.com.zenitech.emissorweb;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.Unidades;
import stone.application.StoneStart;

public class Relatorios extends AppCompatActivity {
    FloatingActionButton fab;
    Context context;
    DatabaseHelper bd;
    ClassAuxiliar cAux;
    SharedPreferences prefs;
    AlertDialog alerta;
    ArrayList<Unidades> elementos;
    Unidades unidades;
    ArrayList<PosApp> elementosPos;
    PosApp posApp;
    ArrayList<Pedidos> elementosPedidos;
    Configuracoes configuracoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorios);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        context = this;
        cAux = new ClassAuxiliar();
        bd = new DatabaseHelper(this);
        configuracoes = new Configuracoes();

        elementos = bd.getUnidades();
        unidades = elementos.get(0);

        elementosPos = bd.getPos();
        posApp = elementosPos.get(0);

        elementosPedidos = bd.getPedidosRelatorio();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            if (elementosPedidos.size() != 0) {
                if (new Configuracoes().GetDevice()) {
                    imprimir();
                } else {
                    if (prefs.getString("tamPapelImpressora", "").equalsIgnoreCase("")) {
                        selectTamPapImpressora();
                    } else {
                        imprimir();
                    }
                }
            } else {
                Toast.makeText(context, "Não tem nada para imprimir!", Toast.LENGTH_LONG).show();
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        128);
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        128);
            }
        }


        // SE O APARELHO FOR UM POS
        /*if (configuracoes.GetDevice()) {
            //
            iniciarStone();
        } else new AtivarDesativarBluetooth().enableBT(context, this);*/
    }

    // Iniciar o Stone
    void iniciarStone() {
        // O primeiro passo é inicializar o SDK.
        StoneStart.init(getApplicationContext());
    }

    private void selectTamPapImpressora() {
        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_emissor_web);
        //define o titulo
        builder.setTitle("");
        //define a mensagem
        builder.setMessage("Qual o tamanho do papel de sua impressora?");

        //define um botão como positivo
        builder.setNeutralButton("Papel de 58mm", (arg0, arg1) -> prefs.edit().putString("tamPapelImpressora", "58mm").apply());

        builder.setPositiveButton("Papel de 80mm", (arg0, arg1) -> prefs.edit().putString("tamPapelImpressora", "80mm").apply());

        /*//define um botão como negativo.
        builder.setNegativeButton("Negativo", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(InformacoesVagas.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
            }
        });*/

        //cria o AlertDialog
        alerta = builder.create();

        //Exibe
        alerta.show();
    }

    public void imprimir() {
        Intent i;
        if (new Configuracoes().GetDevice()) {
            i = new Intent(context, ImpressoraPOS.class);
        } else {
            i = new Intent(context, Impressora.class);
        }
        i.setFlags(0);
        i.putExtra("imprimir", "relatorio");
        startActivityForResult(i, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Relatorios.RESULT_OK) {

                Bundle params = data.getExtras();
                if (params != null) {

                    if (!Objects.requireNonNull(params.get("retorno")).equals("")) {
                        Log.i("Relatorio: ", Objects.requireNonNull(params.get("retorno")).toString());
                    }
                }
            } else {
                Log.e("teste", "não voltou");
            }
        }
    }

    @Override
    public void onBackPressed() {
        Sair();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Sair();
        }

        return super.onOptionsItemSelected(item);
    }

    void Sair() {
        new AtivarDesativarBluetooth().disableBT(context,this);

        Intent i = new Intent(this, Principal.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
