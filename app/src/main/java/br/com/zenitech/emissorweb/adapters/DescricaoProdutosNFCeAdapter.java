package br.com.zenitech.emissorweb.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import br.com.zenitech.emissorweb.ClassAuxiliar;
import br.com.zenitech.emissorweb.R;
import br.com.zenitech.emissorweb.domains.ProdutosDescricaoNFCe;

public class DescricaoProdutosNFCeAdapter extends ArrayAdapter<ProdutosDescricaoNFCe> {

    ClassAuxiliar aux;

    public DescricaoProdutosNFCeAdapter(Context context, List<ProdutosDescricaoNFCe> produtos, ClassAuxiliar cAux) {
        super(context, 0, produtos);

        aux = cAux;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_descricao_produtos_nfce, parent, false);
        }

        ProdutosDescricaoNFCe produto = getItem(position);

        TextView txtNumItem, txtDescCod, txtDescDesc, txtDescQuant, txtDescValUnit, txtDescValTot;
        txtNumItem = view.findViewById(R.id.txtNumItem);
        txtDescCod = view.findViewById(R.id.txtDescCod);
        txtDescDesc = view.findViewById(R.id.txtDescDesc);
        txtDescQuant = view.findViewById(R.id.txtDescQuant);
        txtDescValUnit = view.findViewById(R.id.txtDescValUnit);
        txtDescValTot = view.findViewById(R.id.txtDescValTot);

        txtNumItem.setText(MessageFormat.format("{0}", position + 1));
        txtDescCod.setText(produto.idProduto);
        txtDescDesc.setText(aux.removerAcentos(produto.produto));
        txtDescQuant.setText(produto.quantidade);
        txtDescValUnit.setText(aux.maskMoney(new BigDecimal(produto.valor)));
        txtDescValTot.setText(aux.maskMoney(new BigDecimal(produto.total)));

        return view;
    }
}
