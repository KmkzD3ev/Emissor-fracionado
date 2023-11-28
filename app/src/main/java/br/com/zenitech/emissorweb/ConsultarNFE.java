package br.com.zenitech.emissorweb;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.domains.ValidarNFE;
import br.com.zenitech.emissorweb.interfaces.IValidarNFE;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import stone.application.StoneStart;
import stone.user.UserModel;
import stone.utils.Stone;

public class ConsultarNFE extends AppCompatActivity {

    private DatabaseHelper bd;
    SharedPreferences prefs;
    SharedPreferences.Editor ed;
    Context context;
    EditText nNota;

    ArrayList<Unidades> elementos;
    Unidades unidades;
    ArrayList<PosApp> elementosPos;
    PosApp posApp;
    Configuracoes configuracoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_nfe);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //
        context = this;
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();
        configuracoes = new Configuracoes();

        //
        bd = new DatabaseHelper(this);

        nNota = findViewById(R.id.nNota);
        findViewById(R.id.btnConsultarNFE).setOnClickListener(v -> VerificarCampos());

        // SE O APARELHO FOR UM POS
        if (configuracoes.GetDevice()) {
            //
            iniciarStone();
        }

        CheckPermission();
        ativarBluetooth();
    }

    private void CheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT},
                        128);
            }
        }

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                    128);
        }
    }

    private void ativarBluetooth() {
        new AtivarDesativarBluetooth().enableBT(context, this);
    }

    // Iniciar o Stone
    void iniciarStone() {
        // O primeiro passo é inicializar o SDK.
        StoneStart.init(getApplicationContext());
    }

    private void VerificarCampos() {
        //ESCODER O TECLADO
        // TODO Auto-generated method stub
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        //
        if (nNota.getText().toString().equals("")) {
            Toast.makeText(getBaseContext(), "Informe o nº da nota.", Toast.LENGTH_LONG).show();
        } else {
            consultarNota();
        }
    }

    private void consultarNota() {

        //
        final IValidarNFE iValidarNFE = IValidarNFE.retrofit.create(IValidarNFE.class);


        elementosPos = bd.getPos();
        posApp = elementosPos.get(0);
        String serie = bd.getSeriePOS();

        final Call<ValidarNFE> call = iValidarNFE.reimprimirNotaNFE(
                "778",
                nNota.getText().toString(),
                serie,
                posApp.getSerial(),
                76
        );

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ValidarNFE> call, @NonNull Response<ValidarNFE> response) {

                //
                final ValidarNFE sincronizacao = response.body();
                if (sincronizacao != null) {

                    //
                    runOnUiThread(() -> {
                        if (!sincronizacao.getProtocolo().isEmpty() && sincronizacao.getProtocolo().length() >= 10) {
                            Toast.makeText(ConsultarNFE.this, ""+sincronizacao.getProds_nota().size(), Toast.LENGTH_SHORT).show();
                            if (new Configuracoes().GetDevice()) {
                                prefs.edit().putString("barcode", sincronizacao.getBarcode()).apply();
                                prefs.edit().putString("nome", sincronizacao.getNome()).apply();
                                prefs.edit().putString("endereco_dest", sincronizacao.getEndereco_dest()).apply();
                                prefs.edit().putString("cnpj_dest", sincronizacao.getCnpj_dest()).apply();
                                prefs.edit().putString("ie_dest", sincronizacao.getIe_dest()).apply();
                                prefs.edit().putString("nnf", sincronizacao.getNnf()).apply();
                                prefs.edit().putString("serie", sincronizacao.getSerie()).apply();
                                prefs.edit().putString("chave", sincronizacao.getChave()).apply();
                                StringBuilder textBuffer = new StringBuilder();
                                for (int ind = 0; ind < sincronizacao.getProds_nota().size(); ind++) {
                                    textBuffer.append(sincronizacao.getProds_nota().get(ind).getNome()).append("{br}");
                                }
                                prefs.edit().putString("prods_nota", textBuffer.toString()).apply();
                                //prefs.edit().putString("prods_nota", sincronizacao.getProds_nota()).apply();
                                prefs.edit().putString("total_nota", sincronizacao.getTotal_nota()).apply();
                                prefs.edit().putString("inf_cpl", sincronizacao.getInf_cpl()).apply();
                                prefs.edit().putString("nat_op", sincronizacao.getNat_op()).apply();

                                Intent i = new Intent(context, ImpressoraPOS.class);

                                ArrayList<Unidades> elementosUnidade = bd.getUnidades();
                                unidades = elementosUnidade.get(0);

                                //UNIDADE
                                i.putExtra("razao_social", unidades.getRazao_social());
                                i.putExtra("cnpj", "CNPJ: " + unidades.getCnpj() + " I.E.: " + unidades.getIe());
                                i.putExtra("endereco", unidades.getEndereco() + ", " + unidades.getNumero());
                                i.putExtra("bairro", unidades.getBairro() + ", " + unidades.getCidade() + ", " + unidades.getUf());
                                i.putExtra("cep", unidades.getCep() + "  " + unidades.getTelefone());

                                //NOTA
                                i.putExtra("imprimir", "nfe");
                                i.putExtra("pedido", "");
                                i.putExtra("cliente", "CONSUMIDOR NAO IDENTIFICADO");
                                i.putExtra("id_produto", "");
                                i.putExtra("produto", "");
                                i.putExtra("chave", "");
                                i.putExtra("protocolo", "");
                                i.putExtra("quantidade", "");
                                i.putExtra("valor", "");
                                i.putExtra("valorUnit", "");
                                i.putExtra("tributos", "");
                                i.putExtra("form_pagamento", "");

                                startActivity(i);
                            } else {

                                prefs.edit().putString("barcode", sincronizacao.getBarcode()).apply();
                                prefs.edit().putString("nome", sincronizacao.getNome()).apply();
                                prefs.edit().putString("endereco_dest", sincronizacao.getEndereco_dest()).apply();
                                prefs.edit().putString("cnpj_dest", sincronizacao.getCnpj_dest()).apply();
                                prefs.edit().putString("ie_dest", sincronizacao.getIe_dest()).apply();
                                prefs.edit().putString("nnf", sincronizacao.getNnf()).apply();
                                prefs.edit().putString("serie", sincronizacao.getSerie()).apply();
                                prefs.edit().putString("chave", sincronizacao.getChave()).apply();
                                StringBuilder textBuffer = new StringBuilder();
                                for (int ind = 0; ind < sincronizacao.getProds_nota().size(); ind++) {
                                    textBuffer.append(sincronizacao.getProds_nota().get(ind).getNome()).append("{br}");
                                }
                                prefs.edit().putString("prods_nota", textBuffer.toString()).apply();
                                //prefs.edit().putString("prods_nota", sincronizacao.getProds_nota()).apply();
                                prefs.edit().putString("total_nota", sincronizacao.getTotal_nota()).apply();
                                prefs.edit().putString("inf_cpl", sincronizacao.getInf_cpl()).apply();
                                prefs.edit().putString("nat_op", sincronizacao.getNat_op()).apply();

                                Intent i = new Intent(context, Impressora.class);

                                ArrayList<Unidades> elementosUnidade = bd.getUnidades();
                                unidades = elementosUnidade.get(0);

                                //UNIDADE
                                i.putExtra("razao_social", unidades.getRazao_social());
                                i.putExtra("cnpj", "CNPJ: " + unidades.getCnpj() + " I.E.: " + unidades.getIe());
                                i.putExtra("endereco", unidades.getEndereco() + ", " + unidades.getNumero());
                                i.putExtra("bairro", unidades.getBairro() + ", " + unidades.getCidade() + ", " + unidades.getUf());
                                i.putExtra("cep", unidades.getCep() + "  " + unidades.getTelefone());

                                //NOTA
                                i.putExtra("imprimir", "nfe");
                                i.putExtra("pedido", "");
                                i.putExtra("cliente", "CONSUMIDOR NAO IDENTIFICADO");
                                i.putExtra("id_produto", "");
                                i.putExtra("produto", "");
                                i.putExtra("chave", "");
                                i.putExtra("protocolo", "");
                                i.putExtra("quantidade", "");
                                i.putExtra("valor", "");
                                i.putExtra("valorUnit", "");
                                i.putExtra("tributos", "");
                                i.putExtra("form_pagamento", "");

                                startActivity(i);
                            }
                        }

                        Log.i("CNFE", sincronizacao.getProtocolo());
                    });
                } else {
                    Toast.makeText(context, "NF-e Não encontrada!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ValidarNFE> call, @NonNull Throwable t) {
            }
        });
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
