package br.com.zenitech.emissorweb.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Objects;

import br.com.zenitech.emissorweb.Configuracoes;
import br.com.zenitech.emissorweb.DatabaseHelper;
import br.com.zenitech.emissorweb.FormPedidos;
import br.com.zenitech.emissorweb.FormPedidosNFE;
import br.com.zenitech.emissorweb.R;
import br.com.zenitech.emissorweb.VerificarOnline;
import br.com.zenitech.emissorweb.domains.DomainPrincipal;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PedidosNFE;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.Unidades;
import br.com.zenitech.emissorweb.interfaces.IPrincipal;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {
    Context context;
    VerificarOnline verificarOnline;
    SharedPreferences prefs;

    private HomeViewModel homeViewModel;
    private DatabaseHelper bd;

    //
    ArrayList<Pedidos> elementosPedidos;
    ArrayList<ItensPedidos> elementosItens;

    ArrayList<PosApp> elementosPos;
    PosApp posApp;

    ArrayList<Unidades> elementosUnidades;
    Unidades unidades;

    //
    ArrayList<PedidosNFE> pedidosNFE;
    private ArrayList<Pedidos> pedidos;

    private TextView textView, txtTransmitida, txtContigencia, txtStatusTransmissao;
    LinearLayout btnNovaNFCEP, btnNovaNFEP;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        context = root.getContext();

        //
        prefs = context.getSharedPreferences("preferencias", MODE_PRIVATE);

        verificarOnline = new VerificarOnline();

        textView = root.findViewById(R.id.text_home);
        txtTransmitida = root.findViewById(R.id.txtTransmitida);
        txtContigencia = root.findViewById(R.id.txtContigencia);
        txtStatusTransmissao = root.findViewById(R.id.txtStatusTransmissao);

        //
        btnNovaNFCEP = root.findViewById(R.id.btnNovaNFCEP);
        btnNovaNFEP = root.findViewById(R.id.btnNovaNFEP);

        btnNovaNFCEP.setOnClickListener(v -> startActivity(new Intent(root.getContext(), FormPedidos.class)));
        btnNovaNFEP.setOnClickListener(v -> {
            if (verificarOnline.isOnline(context)) {
                _verificarPermNFE();
            } else {
                Toast.makeText(context, "Verifique sua conexão com a internet!", Toast.LENGTH_LONG).show();
            }
        });

        //
        bd = new DatabaseHelper(getContext());

        //
        pedidos = bd.getPedidos();
        //
        elementosPos = bd.getPos();
        posApp = elementosPos.get(0);
        textView.setText(posApp.getSerial());

        //
        elementosPedidos = bd.getPedidosTransmitirFecharDia();

        elementosUnidades = bd.getUnidades();
        unidades = elementosUnidades.get(0);

        txtTransmitida.setText(String.valueOf(pedidos.size() - elementosPedidos.size()));
        txtContigencia.setText(String.valueOf(elementosPedidos.size()));
        if (unidades.getUf().equalsIgnoreCase(new Configuracoes().GetUFCeara())) {
            txtStatusTransmissao.setText("Não Transmitida(s)");
            //Objects.requireNonNull(getSupportActionBar()).setTitle(elementosPedidos.size() + "/" + pedidos.size() + " não transmitida(s)");
        } else {
            txtStatusTransmissao.setText("Contigências");
            //Objects.requireNonNull(getSupportActionBar()).setTitle(elementosPedidos.size() + "/" + pedidos.size() + " contigências");
        }

        return root;
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
                       requireActivity().runOnUiThread(() -> startActivity(new Intent(context, FormPedidosNFE.class)));
                    } else {
                        requireActivity().runOnUiThread(() -> Toast.makeText(context, "MODULO NÃO HABILITADO", Toast.LENGTH_LONG).show());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DomainPrincipal> call, @NonNull Throwable t) {
                //Timber.i(Objects.requireNonNull(t.getMessage()));
            }
        });
    }
}
