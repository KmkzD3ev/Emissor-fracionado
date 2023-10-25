package br.com.zenitech.emissorweb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;

import br.com.zenitech.emissorweb.ClassAuxiliar;
import br.com.zenitech.emissorweb.R;
import br.com.zenitech.emissorweb.domains.StatusPedidos;


public class DescricaoItensPedidoAdapter extends RecyclerView.Adapter<DescricaoItensPedidoAdapter.ViewHolder> {

    ClassAuxiliar aux;
    ArrayList<StatusPedidos> elementos;

    public DescricaoItensPedidoAdapter(ClassAuxiliar aux, ArrayList<StatusPedidos> elementos) {
        this.aux = aux;
        this.elementos = elementos;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //
        View view = inflater.inflate(R.layout.item_desc_itens_pedidos_status, parent, false);

        //
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        //
        final StatusPedidos item = elementos.get(position);

        holder.txtProduto.setText(aux.removerAcentos(item.getNome()));
        holder.txtQuant.setText(item.getQuantidade());
        holder.txtValUni.setText(aux.maskMoney(new BigDecimal(item.getValor())));
        holder.txtValDesc.setText(aux.maskMoney(new BigDecimal(item.getDesconto())));//aux.maskMoney(new BigDecimal(produto.valor))
        holder.txtTotal.setText(aux.maskMoney(new BigDecimal(item.getValor_total())));//aux.maskMoney(new BigDecimal(produto.total))
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
