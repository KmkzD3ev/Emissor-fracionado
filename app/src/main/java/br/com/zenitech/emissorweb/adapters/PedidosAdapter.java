package br.com.zenitech.emissorweb.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import br.com.zenitech.emissorweb.ClassAuxiliar;
import br.com.zenitech.emissorweb.R;
import br.com.zenitech.emissorweb.domains.Pedidos;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.ViewHolder> {

    private ClassAuxiliar classAuxiliar;
    private final Context context;
    private final ArrayList<Pedidos> elementos;

    public PedidosAdapter(Context context, ArrayList<Pedidos> elementos) {
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
        View view = inflater.inflate(R.layout.item_timeline, parent, false);

        //
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        //
        final Pedidos pedidos = elementos.get(position);
        classAuxiliar = new ClassAuxiliar();

        TextView txtCodigo = holder.txtCodigo;
        txtCodigo.setTextColor(Color.parseColor("#333333"));

        if(pedidos.getSituacao().equals("OFF")){
            txtCodigo.setText(pedidos.getId() + " - Emitida em contigência.");
            txtCodigo.setTextColor(Color.parseColor("#ff0000"));
        }else{
            txtCodigo.setText(pedidos.getId());
        }

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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llRelatorioVendas;
        TextView txtCodigo, txtProduto, txtQuantidade, txtValor, txtTotal;
        ImageButton btnExcluirVenda;

        public ViewHolder(View itemView) {
            super(itemView);

            txtCodigo = itemView.findViewById(R.id.txtSubTituloTimeLine);
            /*/
            llRelatorioVendas = (LinearLayout) itemView.findViewById(R.id.llRelatorioVendas);
            txtProduto = (TextView) itemView.findViewById(R.id.txtProduto);
            txtQuantidade = (TextView) itemView.findViewById(R.id.txtQuantidade);
            txtValor = (TextView) itemView.findViewById(R.id.txtValor);
            txtTotal = (TextView) itemView.findViewById(R.id.txtTotal);
            btnExcluirVenda = (ImageButton) itemView.findViewById(R.id.btnExcluirVenda);
            */
        }
    }
}
