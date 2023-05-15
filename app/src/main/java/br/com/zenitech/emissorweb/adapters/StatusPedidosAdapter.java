package br.com.zenitech.emissorweb.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;

import br.com.zenitech.emissorweb.ClassAuxiliar;
import br.com.zenitech.emissorweb.R;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.StatusPedidos;

public class StatusPedidosAdapter extends RecyclerView.Adapter<StatusPedidosAdapter.ViewHolder> {

    private ClassAuxiliar aux;
    private Context context;
    private ArrayList<StatusPedidos> elementos;

    public StatusPedidosAdapter(Context context, ArrayList<StatusPedidos> elementos) {
        this.context = context;
        this.elementos = elementos;
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
        final StatusPedidos pedidos = elementos.get(position);
        aux = new ClassAuxiliar();

        TextView txtProduto = holder.txtProduto;
        TextView txtQuant = holder.txtQuant;
        TextView txtValUni = holder.txtValUni;
        TextView txtValDesc = holder.txtValDesc;
        TextView txtTotal = holder.txtTotal;

        if (pedidos.getSituacao().equalsIgnoreCase("") || pedidos.getSituacao().equalsIgnoreCase("OFF")) {
            txtProduto.setText(String.format("* %s  |  %s", pedidos.getPedido(), pedidos.getNome()));
        } else {
            txtProduto.setText(String.format("%s  |  %s", pedidos.getPedido(), pedidos.getNome()));
        }
        txtQuant.setText(pedidos.getQuantidade());

        txtValDesc.setText(aux.maskMoney(new BigDecimal(pedidos.getDesconto())));

        //
        String valorUnit = String.valueOf(aux.converterValores(pedidos.getValor()));
        txtValUni.setText(String.format("%s", aux.maskMoney(new BigDecimal(valorUnit))));

        //MULTIPLICA O VALOR PELA QUANTIDADE
        String[] multiplicar = {valorUnit, pedidos.getQuantidade()};
        String total = String.valueOf(aux.multiplicar(multiplicar));
        //txtTotal.setText(String.format("%s", aux.maskMoney(new BigDecimal(total))));
        txtTotal.setText(String.format("%s", aux.maskMoney(new BigDecimal(pedidos.getValor_total()))));//valor_total


        /*/
        TextView produto = holder.txtProduto;
        produto.setText(pedidos.getProtocolo());
        //
        TextView codigo = holder.txtQuantidade;
        codigo.setText(pedidos.getSituacao());
        //
        String[] vls_media = {pedidos.getValor_total()};
        String media = String.valueOf(classAuxiliar.somar(vls_media));
        TextView total = holder.txtTotal;
        total.setText("R$ " + classAuxiliar.maskMoney(new BigDecimal(media)));
        */
    }

    @Override
    public int getItemCount() {
        return elementos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtProduto, txtQuant, txtValUni, txtValDesc, txtTotal;

        ViewHolder(View itemView) {
            super(itemView);

            txtProduto = itemView.findViewById(R.id.txtProduto);
            txtQuant = itemView.findViewById(R.id.txtQuant);
            txtValUni = itemView.findViewById(R.id.txtValUni);
            txtValDesc = itemView.findViewById(R.id.txtValDesc);
            txtTotal = itemView.findViewById(R.id.txtTotal);
        }
    }
}
