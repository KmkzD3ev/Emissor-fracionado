package br.com.zenitech.emissorweb;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.emissorweb.adapters.StatusPedidosAdapter;
import br.com.zenitech.emissorweb.adapters.StatusPedidosNFEAdapter;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.StatusPedidos;
import br.com.zenitech.emissorweb.domains.StatusPedidosNFE;
import br.com.zenitech.emissorweb.domains.Unidades;

public class StatusCopiaSeguranca extends AppCompatActivity {

    //
    DatabaseHelper bd;
    Context context;
    TextView txtCNPJ, txtSerie, txtUltimaNota, txtUltimaNotaNFe, txtQuant, txtTotal;

    //
    ArrayList<Unidades> elementosUnidade;
    Unidades unidades;
    ArrayList<PosApp> elementosPos;
    PosApp posApp;

    //
    ArrayList<StatusPedidos> pedidos;
    StatusPedidosAdapter adapter;
    RecyclerView recyclerView;

    //
    ArrayList<StatusPedidosNFE> pedidosNFE;
    StatusPedidosNFEAdapter adapterNFE;
    RecyclerView recyclerViewNFE;

    ClassAuxiliar aux;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //
        context = this;
        aux = new ClassAuxiliar();

        txtCNPJ = findViewById(R.id.txtCNPJ);
        txtSerie = findViewById(R.id.txtSerie);
        txtUltimaNota = findViewById(R.id.txtUltimaNota);
        txtUltimaNotaNFe = findViewById(R.id.txtUltimaNotaNFe);
        txtQuant = findViewById(R.id.txtQuant);
        txtTotal = findViewById(R.id.txtTotal);

        //
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setNestedScrollingEnabled(false);

        //
        recyclerViewNFE = findViewById(R.id.recyclerViewNFE);
        recyclerViewNFE.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewNFE.setNestedScrollingEnabled(false);


        bd = new DatabaseHelper(this);

        String serie = bd.getSeriePOS();
        txtSerie.setText(serie);
        elementosUnidade = bd.getUnidades();
        unidades = elementosUnidade.get(0);
        elementosPos = bd.getPos();
        posApp = elementosPos.get(0);

        //
        txtCNPJ.setText(aux.mask(unidades.getCnpj()));
        txtUltimaNota.setText(bd.ultimaNFCE());
        txtUltimaNotaNFe.setText(bd.ultimaNFE());

        //
        pedidos = bd.getStatusPedidos();
        adapter = new StatusPedidosAdapter(this, pedidos,bd);
        if (pedidos.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(adapter);
        }

        //
        pedidosNFE = bd.getStatusPedidosNFE();
        adapterNFE = new StatusPedidosNFEAdapter(this, pedidosNFE, bd);
        if (pedidosNFE.size() > 0) {
            recyclerViewNFE.setVisibility(View.VISIBLE);
            recyclerViewNFE.setAdapter(adapterNFE);
        }

        //txtTotal.setText(bd.getValorTotal());
        //txtQuant.setText(bd.getQuantTotal());

        at();
    }

    void at(){
        Handler  handler = new Handler();

        final Runnable r = () -> {
            txtTotal.setText(bd.getValorTotal());
            txtQuant.setText(bd.getQuantTotal());
            //handler.postDelayed(this, 1000);
        };

        handler.postDelayed(r, 1000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
