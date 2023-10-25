package br.com.zenitech.emissorweb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;

import br.com.zenitech.emissorweb.ClassAuxiliar;
import br.com.zenitech.emissorweb.DatabaseHelper;
import br.com.zenitech.emissorweb.R;
import br.com.zenitech.emissorweb.domains.StatusPedidos;
import br.com.zenitech.emissorweb.domains.StatusPedidosNFE;

public class StatusPedidosNFEAdapter extends RecyclerView.Adapter<StatusPedidosNFEAdapter.ViewHolder> {

    private ClassAuxiliar aux;
    private Context context;
    private ArrayList<StatusPedidosNFE> elementos;
    private DatabaseHelper dBhelper;

    public StatusPedidosNFEAdapter(Context context, ArrayList<StatusPedidosNFE> elementos, DatabaseHelper bd) {
        this.context = context;
        this.elementos = elementos;
        this.dBhelper = bd;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //
        View view = inflater.inflate(R.layout.item_pedidos_status, parent, false);

        //
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        //
        final StatusPedidosNFE pedidos = elementos.get(position);
        aux = new ClassAuxiliar();

/*
        TextView txtProduto = holder.txtProduto;
        TextView txtQuant = holder.txtQuant;
        TextView txtValUni = holder.txtValUni;
        TextView txtTotal = holder.txtTotal;

        txtProduto.setText(pedidos.getNome());
        txtQuant.setText(pedidos.getQuantidade());

        //
        String valorUnit = String.valueOf(aux.converterValores(pedidos.getValor()));
        txtValUni.setText(String.format("%s", aux.maskMoney(new BigDecimal(valorUnit))));

        //MULTIPLICA O VALOR PELA QUANTIDADE
        String[] multiplicar = {valorUnit, pedidos.getQuantidade()};
        String total = String.valueOf(aux.multiplicar(multiplicar));
        txtTotal.setText(String.format("%s", aux.maskMoney(new BigDecimal(total))));
*/




        TextView txtProduto = holder.txtProduto;
        TextView txtQuant = holder.txtQuant;
        TextView txtValUni = holder.txtValUni;
        TextView txtTotal = holder.txtTotal;

        if (pedidos.getSituacao().equalsIgnoreCase("") || pedidos.getSituacao().equalsIgnoreCase("OFF")) {
            holder.txtIdPedido.setText(String.format("* %s", pedidos.getPedido()/*, pedidos.getNome()*/));
        } else {
            holder.txtIdPedido.setText(String.format("%s", pedidos.getPedido()/*, pedidos.getNome()*/));
        }
        txtQuant.setText(pedidos.getQuantidade());

        //txtValDesc.setText(aux.maskMoney(new BigDecimal(pedidos.getDesconto())));

        //
        String valorUnit = String.valueOf(aux.converterValores(pedidos.getValor()));
        txtValUni.setText(String.format("%s", aux.maskMoney(new BigDecimal(valorUnit))));

        //MULTIPLICA O VALOR PELA QUANTIDADE
        String[] multiplicar = {valorUnit, pedidos.getQuantidade()};
        String total = String.valueOf(aux.multiplicar(multiplicar));
        //txtTotal.setText(String.format("%s", aux.maskMoney(new BigDecimal(total))));
        txtTotal.setText(String.format("%s", aux.maskMoney(new BigDecimal(pedidos.getValor_total()))));//valor_total

        ArrayList<StatusPedidosNFE> descItens;
        descItens = dBhelper.getDescricaoItensStatusPedidosNFe(pedidos.getPedido());
        //Toast.makeText(context, "" + descItens.size(), Toast.LENGTH_SHORT).show();
        DescricaoItensPedidoNFeAdapter adapter = new DescricaoItensPedidoNFeAdapter(aux, descItens);

        RecyclerView rvDescItensPedido = holder.rvDescItensPedido;
        rvDescItensPedido.setLayoutManager(new LinearLayoutManager(context));
        rvDescItensPedido.setNestedScrollingEnabled(false);
        rvDescItensPedido.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return elementos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtIdPedido, txtProduto, txtQuant, txtValUni, txtTotal;
        RecyclerView rvDescItensPedido;

        ViewHolder(View itemView) {
            super(itemView);

            txtIdPedido = itemView.findViewById(R.id.txtIdPedido);
            txtProduto = itemView.findViewById(R.id.txtProduto);
            txtQuant = itemView.findViewById(R.id.txtQuant);
            txtValUni = itemView.findViewById(R.id.txtValUni);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            rvDescItensPedido = itemView.findViewById(R.id.rvDescItensPedido);
        }
    }
}
