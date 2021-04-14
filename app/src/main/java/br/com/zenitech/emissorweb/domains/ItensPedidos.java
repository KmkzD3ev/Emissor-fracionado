package br.com.zenitech.emissorweb.domains;


public class ItensPedidos {
    private String pedido;
    private String produto;
    private String quantidade;
    private String valor;
    private String total;

    public ItensPedidos(String pedido, String produto, String quantidade, String valor, String total) {
        this.pedido = pedido;
        this.produto = produto;
        this.quantidade = quantidade;
        this.valor = valor;
        this.total = total;
    }

    public String getPedido() {
        return pedido;
    }

    public void setPedido(String pedido) {
        this.pedido = pedido;
    }

    public String getProduto() {
        return produto;
    }

    public void setProduto(String produto) {
        this.produto = produto;
    }

    public String getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(String quantidade) {
        this.quantidade = quantidade;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
