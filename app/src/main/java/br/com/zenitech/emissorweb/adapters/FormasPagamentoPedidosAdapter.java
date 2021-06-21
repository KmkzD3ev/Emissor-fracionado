package br.com.zenitech.emissorweb.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.stone.posandroid.providers.PosPrintReceiptProvider;
import br.com.stonesdk.sdkdemo.controller.PrintController;
import br.com.zenitech.emissorweb.ClassAuxiliar;
import br.com.zenitech.emissorweb.DatabaseHelper;
import br.com.zenitech.emissorweb.R;
import br.com.zenitech.emissorweb.domains.AutorizacoesPinpad;
import br.com.zenitech.emissorweb.domains.FormaPagamentoPedido;
import br.com.zenitech.emissorweb.domains.Unidades;
import stone.application.enums.ReceiptType;
import stone.application.interfaces.StoneCallbackInterface;
import stone.database.transaction.TransactionDAO;
import stone.database.transaction.TransactionObject;
import stone.providers.CancellationProvider;
import stone.user.UserModel;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static br.com.zenitech.emissorweb.FormPedidos.bgTotal;
import static br.com.zenitech.emissorweb.FormPedidos.txtTotalFinanceiro;
import static br.com.zenitech.emissorweb.FormPedidos.txtTotalItemFinanceiro;
import static br.com.zenitech.emissorweb.FormPedidos.txtValorFormaPagamento;

public class FormasPagamentoPedidosAdapter extends RecyclerView.Adapter<FormasPagamentoPedidosAdapter.ViewHolder> {

    ////////////////////////////////
    private ClassAuxiliar classAuxiliar;
    private Context context;
    private ArrayList<FormaPagamentoPedido> elementos;

    ArrayList<Unidades> elementosUnidades;
    Unidades unidades;

    /*String getId;
    String getId_pedido;
    String getValor;
    String codigoAutorizacao;
    int positionItem;*/

    public FormasPagamentoPedidosAdapter(Context context, ArrayList<FormaPagamentoPedido> elementos, ArrayList<Unidades> elementosUnidades) {
        this.context = context;
        this.elementos = elementos;
        this.elementosUnidades = elementosUnidades;
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
        View view = inflater.inflate(R.layout.item_form_pag_pedidos, parent, false);

        //
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        //
        final FormaPagamentoPedido formaPagamentoPedido = elementos.get(position);
        classAuxiliar = new ClassAuxiliar();

        //
        TextView txtFormaPagamento = holder.txtFormaPagamento;
        txtFormaPagamento.setText(classAuxiliar.getNomeFormaPagamento(formaPagamentoPedido.getId_forma_pagamento()));
        String nomeFPG = classAuxiliar.getNomeFormaPagamento(formaPagamentoPedido.getId_forma_pagamento());

        //
        TextView total = holder.txtFinanceiro;
        total.setText(classAuxiliar.maskMoney(new BigDecimal(formaPagamentoPedido.getValor())));

        holder.btnExcluirFinanceiro.setOnClickListener(v -> {
            final String getId = formaPagamentoPedido.getId();
            final String getId_pedido = formaPagamentoPedido.getId_pedido();
            final String getValor = formaPagamentoPedido.getValor();
            final String codigoAutorizacao = formaPagamentoPedido.getCodigo_autorizacao();
            final int positionItem = position;


            unidades = elementosUnidades.get(0);

            if ((nomeFPG.equalsIgnoreCase("CARTAO DE CREDITO") ||
                    nomeFPG.equalsIgnoreCase("CARTAO DE DEBITO"))
                    && !unidades.getCodloja().equalsIgnoreCase("")
            ) {
                //Log.e("Cancel", formaPagamentoPedido.getCodigo_autorizacao());
                iniciarTranzacao(codigoAutorizacao, getId, getId_pedido, getValor, positionItem);
            } else {
                _excluirFpg(getId, getId_pedido, getValor, positionItem);
            }

        });
    }

    @Override
    public int getItemCount() {
        return elementos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        //LinearLayout LlList;
        TextView txtFormaPagamento, txtFinanceiro;
        ImageButton btnExcluirFinanceiro;

        public ViewHolder(View itemView) {
            super(itemView);

            //
            //LlList = (LinearLayout) itemView.findViewById(R.id.LlList);
            txtFormaPagamento = (TextView) itemView.findViewById(R.id.txtFormaPagamento);
            txtFinanceiro = (TextView) itemView.findViewById(R.id.txtFinanceiro);
            btnExcluirFinanceiro = (ImageButton) itemView.findViewById(R.id.btnExcluirFinanceiro);
        }
    }

    public void excluirItem(String codigo, String codigo_financeiro_app, String totalVenda, int position) {
        FormaPagamentoPedido formaPagamentoPedido = new FormaPagamentoPedido(codigo, null, null, null, null);
        DatabaseHelper bd;
        bd = new DatabaseHelper(context);
        bd.deleteItemFormPagPedido(formaPagamentoPedido);


        elementos.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, elementos.size());

        //
        //txtTotalFinanceiro

        if (elementos.size() != 0) {
            String valor = bd.getValorTotalFinanceiro(codigo_financeiro_app);
            txtTotalItemFinanceiro.setText(classAuxiliar.maskMoney(new BigDecimal(valor)));
            //textTotalItens.setText(String.valueOf(elementos.size()));
        } else {
            txtTotalItemFinanceiro.setText(classAuxiliar.maskMoney(new BigDecimal("0.0")));
            //textTotalItens.setText("0");
        }
        //
        String valorFinanceiro = String.valueOf(classAuxiliar.converterValores(txtTotalFinanceiro.getText().toString()));
        String valorFinanceiroAdd = String.valueOf(classAuxiliar.converterValores(txtTotalItemFinanceiro.getText().toString()));

        //SUBTRAIR O VALOR DO FINANCEIRO PELO VALOR TOTAL DE ITENS
        String[] subtrair = {valorFinanceiro, valorFinanceiroAdd};
        String total = String.valueOf(classAuxiliar.subitrair(subtrair));

        txtValorFormaPagamento.setText(total);

        //
        if (comparar()) {

            bgTotal.setBackgroundColor(ContextCompat.getColor(context, R.color.erro));
            txtValorFormaPagamento.setText("0,00");
        } else {
            bgTotal.setBackgroundColor(ContextCompat.getColor(context, R.color.transparente));
        }
    }

    //COMPARAR O VALOR DO FINANCEIRO COM O VALOR ADICIONADO
    private boolean comparar() {

        //
        BigDecimal valorFinanceiro = new BigDecimal(String.valueOf(classAuxiliar.converterValores(txtTotalFinanceiro.getText().toString())));
        BigDecimal valorFinanceiroAdd = new BigDecimal(String.valueOf(classAuxiliar.converterValores(txtTotalItemFinanceiro.getText().toString())));

        if (valorFinanceiroAdd.compareTo(valorFinanceiro) == 1) {
            //
            if (valorFinanceiro.toString().equals(valorFinanceiroAdd.toString())) {

                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    //
    int transactionId;

    void iniciarTranzacao(String codigoAutorizacao, String getId, String getId_pedido, String getValor, int positionItem) {
        TransactionDAO transactionDAO = new TransactionDAO(context);
        TransactionObject to = transactionDAO.findTransactionWithAuthorizationCode(codigoAutorizacao);
        cancelarPagamento(to, getId, getId_pedido, getValor, positionItem);
    }

    // Cancelar Pagamento
    private void cancelarPagamento(TransactionObject _transactionObject, String getId, String getId_pedido, String getValor, int positionItem) {
        final CancellationProvider provider = new CancellationProvider(context, _transactionObject);

        provider.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {

                //Transação Cancelada com sucesso
                //toastMsg("Transação Cancelada com sucesso");
                //
                /*Log.i("cancel 2", provider.getResponseCodeEnum().toString() +", " + provider.getMessageFromAuthorize());
                final PrintController printMerchant =
                        new PrintController(context,
                                new PosPrintReceiptProvider(context,
                                        _transactionObject, ReceiptType.MERCHANT));

                printMerchant.print();*/

                _excluirFpg(getId, getId_pedido, getValor, positionItem);
            }

            @Override
            public void onError() {
                //Ocorreu um erro no cancelamento da transacao
                //Método que retorna o código referente ao erro da operação
                //getActionCode();
                toastMsg("Ocorreu um erro no cancelamento da transacao");
                //_finalizarCancelamento();

                //Log.i("Stone", getActionCode());
            }
        });
        provider.execute();
    }

    private void _excluirFpg(String getId, String getId_pedido, String getValor, int positionItem) {
        excluirItem(
                getId,
                getId_pedido,
                getValor,
                positionItem
        );
    }

    private void toastMsg(String msg) {
        Toast toast = makeText(context, msg, LENGTH_SHORT);
        toast.setGravity(1, 0, 0);
        toast.show();
    }
}
