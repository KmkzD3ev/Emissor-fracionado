package br.com.zenitech.emissorweb.adapters;

import android.content.Context;
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

import br.com.zenitech.emissorweb.R;
import br.com.zenitech.emissorweb.ClassAuxiliar;
import br.com.zenitech.emissorweb.DatabaseHelper;
import br.com.zenitech.emissorweb.domains.FinanceiroNFeDomain;
import br.com.zenitech.emissorweb.interfaces.IFinanceiroNFeObserver;

public class FinanceiroNFeAdapter extends RecyclerView.Adapter<FinanceiroNFeAdapter.ViewHolder> {

    private final String TAG = "Observer";
    ClassAuxiliar classAuxiliar;
    Context context;
    ArrayList<FinanceiroNFeDomain> finNFe;
    ArrayList<IFinanceiroNFeObserver> observers = new ArrayList<>();

    public FinanceiroNFeAdapter(Context context, ArrayList<FinanceiroNFeDomain> listFinanceiro) {
        this.context = context;
        this.finNFe = listFinanceiro;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //
        View view = inflater.inflate(R.layout.item_form_pag_pedidos, parent, false);

        //
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        //
        final FinanceiroNFeDomain finNFeDom = finNFe.get(position);
        classAuxiliar = new ClassAuxiliar();
//
        LinearLayout llFormPg = holder.llFormPg;
        llFormPg.setBackgroundResource(R.color.transparente);
        holder.btnExcluirFinanceiro.setVisibility(View.VISIBLE);

        if (!finNFeDom.id_cobranca_pix.equals("")) {
            holder.btnExcluirFinanceiro.setVisibility(View.GONE);
        }
        String nomeFPG = classAuxiliar.getNomeFormaPagamento(finNFeDom.id_forma_pagamento);
        TextView txtFormaPagamento = holder.txtFormaPagamento;
        txtFormaPagamento.setText(nomeFPG);

        //
        TextView total = holder.txtFinanceiro;
        total.setText(classAuxiliar.maskMoney(new BigDecimal(finNFeDom.valor)));
        holder.btnExcluirFinanceiro.setVisibility(View.VISIBLE);

        if (!finNFeDom.id_cobranca_pix.equals("")) {
            holder.btnExcluirFinanceiro.setVisibility(View.GONE);
        }

        if (!finNFeDom.codigo_autorizacao.equals("")) {
            holder.btnExcluirFinanceiro.setVisibility(View.GONE);
        }

        holder.btnExcluirFinanceiro.setOnClickListener(v -> {
            final String getId = finNFeDom.id;
            final int positionItem = position;

            _excluirFpg(getId, positionItem);

        });

        /*// Notifica os observadores sobre a mudança nos dados
        for (IFinanceiroNFeObserver observer : observers) {
            observer.onFinanceiroNFeChanged();

            Log.i(TAG, "Notifica os observadores sobre a mudança nos dados");
        }*/
    }

    @Override
    public int getItemCount() {
        return finNFe.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llFormPg;
        TextView txtFormaPagamento, txtFinanceiro;
        ImageButton btnExcluirFinanceiro;

        public ViewHolder(View itemView) {
            super(itemView);
            //
            llFormPg = itemView.findViewById(R.id.llFormPg);
            txtFormaPagamento = itemView.findViewById(R.id.txtFormaPagamento);
            txtFinanceiro = itemView.findViewById(R.id.txtFinanceiro);
            btnExcluirFinanceiro = itemView.findViewById(R.id.btnExcluirFinanceiro);
        }
    }

    public void registerObserver(IFinanceiroNFeObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            //observer.onFinanceiroNFeChanged();

            Log.i(TAG, "Observer registrado!");
        }
    }

    public void unregisterObserver(IFinanceiroNFeObserver observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);

            Log.i(TAG, "Observer removido!");
        }
    }

    public void excluirItem(String codigo, int position) {

        DatabaseHelper bd;
        bd = new DatabaseHelper(context);
        bd.deleteRegFinanceiroNFe(codigo);

        finNFe.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, finNFe.size());

        // Notifica os observadores sobre a mudança nos dados
        for (IFinanceiroNFeObserver observer : observers) {
            observer.onFinanceiroNFeChanged();
            Log.i(TAG, "Observer Notificado!");
        }
    }

    private void _excluirFpg(String getId, int positionItem) {
        excluirItem(getId, positionItem);
    }
}
