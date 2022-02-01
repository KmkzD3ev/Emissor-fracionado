package br.com.zenitech.emissorweb.adapters;

import static br.com.zenitech.emissorweb.FormPedidosNFE.idCli;
import static br.com.zenitech.emissorweb.FormPedidosNFE.llClienteNFE;
import static br.com.zenitech.emissorweb.FormPedidosNFE.llIdNomeCli;
import static br.com.zenitech.emissorweb.FormPedidosNFE.txtNomeCliente;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import br.com.zenitech.emissorweb.ClassAuxiliar;
import br.com.zenitech.emissorweb.R;
import br.com.zenitech.emissorweb.domains.ClientesNFE;
import br.com.zenitech.emissorweb.domains.Pedidos;

public class ClientesNfeAdapter extends RecyclerView.Adapter<ClientesNfeAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ClientesNFE> elementos;

    public ClientesNfeAdapter(Context context, ArrayList<ClientesNFE> elementos) {
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
        View view = inflater.inflate(R.layout.item_clientes_nfe, parent, false);

        //
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        //
        final ClientesNFE cli = elementos.get(position);

        /*TextView txtCodigo = holder.txtNome.setText();
        txtCodigo.setTextColor(Color.parseColor("#333333"));*/

        holder.txtNome.setText(cli.getCliente());
        holder.llCliNfe.setOnClickListener(view -> {
            idCli = cli.getId();
            txtNomeCliente.setText(cli.getCliente());
            llClienteNFE.setVisibility(View.GONE);
            llIdNomeCli.setVisibility(View.GONE);
        });


        /*if(pedidos.getSituacao().equals("OFF")){
            txtCodigo.setText(pedidos.getId() + " - Emitida em contigência.");
            txtCodigo.setTextColor(Color.parseColor("#ff0000"));
        }else{
            txtCodigo.setText(pedidos.getId());
        }*/

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

        LinearLayout llCliNfe;
        TextView txtNome;

        public ViewHolder(View itemView) {
            super(itemView);

            llCliNfe = itemView.findViewById(R.id.llCliNfe);
            txtNome = itemView.findViewById(R.id.txtNome);
        }
    }
}
