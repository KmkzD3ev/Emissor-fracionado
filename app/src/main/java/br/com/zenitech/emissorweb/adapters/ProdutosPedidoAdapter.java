package br.com.zenitech.emissorweb.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;

import br.com.zenitech.emissorweb.ClassAuxiliar;
import br.com.zenitech.emissorweb.DatabaseHelper;
import br.com.zenitech.emissorweb.R;
import br.com.zenitech.emissorweb.domains.ProdutosPedidoDomain;
import br.com.zenitech.emissorweb.interfaces.IFinanceiroNFeObserver;
import br.com.zenitech.emissorweb.interfaces.IProdutosPedidoObserver;

public class ProdutosPedidoAdapter extends RecyclerView.Adapter<ProdutosPedidoAdapter.ViewHolder> {

    private DatabaseHelper bd;
    private ClassAuxiliar classAuxiliar;
    private final Context context;
    private final ArrayList<ProdutosPedidoDomain> elementos;
    ArrayList<IProdutosPedidoObserver> observers = new ArrayList<>();

    public ProdutosPedidoAdapter(Context context, ArrayList<ProdutosPedidoDomain> elementos, DatabaseHelper bd) {
        this.context = context;
        this.elementos = elementos;
        this.bd = bd;
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
        View view = inflater.inflate(R.layout.item_produtos_pedido, parent, false);

        //
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        //
        final ProdutosPedidoDomain produtoDomain = elementos.get(position);
        classAuxiliar = new ClassAuxiliar();

        //
        TextView produto = holder.txtProduto;
        produto.setText(produtoDomain.produto);
        //
        try {
            holder.txtDesconto.setText(classAuxiliar.maskMoney(new BigDecimal(produtoDomain.desconto)));
        } catch (Exception e) {
            holder.txtDesconto.setText("0,00");
            e.printStackTrace();
        }
        //
        TextView codigo = holder.txtQuantidade;
        codigo.setText(produtoDomain.quantidade);
        //
        TextView valor = holder.txtValor;
        valor.setText(classAuxiliar.maskMoney(new BigDecimal(produtoDomain.valor)));
        //
        TextView total = holder.txtTotal;
        total.setText(classAuxiliar.maskMoney(new BigDecimal(produtoDomain.total)));

        holder.btnExcluirVenda.setOnClickListener(v -> excluirItem(produtoDomain.id, position));
    }

    @Override
    public int getItemCount() {
        return elementos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        //LinearLayout LlList;
        TextView txtProduto, txtQuantidade, txtValor, txtTotal, txtDesconto;
        ImageButton btnExcluirVenda;

        public ViewHolder(View itemView) {
            super(itemView);

            txtProduto = itemView.findViewById(R.id.txtProduto);
            txtQuantidade = itemView.findViewById(R.id.txtQuantidade);
            txtValor = itemView.findViewById(R.id.txtValor);
            txtTotal = itemView.findViewById(R.id.txtTotal);
            btnExcluirVenda = itemView.findViewById(R.id.btnExcluirVenda);
            txtDesconto = itemView.findViewById(R.id.txtDesconto);
        }
    }

    public void registerObserver(IProdutosPedidoObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unregisterObserver(IProdutosPedidoObserver observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    public void excluirItem(String codigo, int position) {
        bd.deleteProdutoPedido(codigo);

        elementos.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, elementos.size());

        // Notifica os observadores sobre a mudança nos dados
        for (IProdutosPedidoObserver observer : observers) {
            observer.onProdutosPedidoChanged();
        }
    }
}
