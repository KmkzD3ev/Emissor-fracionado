package br.com.zenitech.emissorweb.domains;


public class StatusPedidosNFE {
    // Tabela Pedidos
    private String id;
    private String situacao;
    private String protocolo;
    private String data;
    private String hora;
    private String valor_total;
    private String cliente;

    // Tabela Itens_Pedidos
    private String pedido;
    private String produto;
    private String quantidade;
    private String valor;

    // Tabela Produtos
    private String codigo;
    private String nome;
    private String tributos;
    private String valor_minimo;
    private String valor_maximo;
    private String qtd_revenda;

    public StatusPedidosNFE(String id, String situacao, String protocolo, String data, String hora, String valor_total, String cliente, String pedido, String produto, String quantidade, String valor, String codigo, String nome, String tributos, String valor_minimo, String valor_maximo, String qtd_revenda) {
        this.id = id;
        this.situacao = situacao;
        this.protocolo = protocolo;
        this.data = data;
        this.hora = hora;
        this.valor_total = valor_total;
        this.cliente = cliente;
        this.pedido = pedido;
        this.produto = produto;
        this.quantidade = quantidade;
        this.valor = valor;
        this.codigo = codigo;
        this.nome = nome;
        this.tributos = tributos;
        this.valor_minimo = valor_minimo;
        this.valor_maximo = valor_maximo;
        this.qtd_revenda = qtd_revenda;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getValor_total() {
        return valor_total;
    }

    public void setValor_total(String valor_total) {
        this.valor_total = valor_total;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTributos() {
        return tributos;
    }

    public void setTributos(String tributos) {
        this.tributos = tributos;
    }

    public String getValor_minimo() {
        return valor_minimo;
    }

    public void setValor_minimo(String valor_minimo) {
        this.valor_minimo = valor_minimo;
    }

    public String getValor_maximo() {
        return valor_maximo;
    }

    public void setValor_maximo(String valor_maximo) {
        this.valor_maximo = valor_maximo;
    }

    public String getQtd_revenda() {
        return qtd_revenda;
    }

    public void setQtd_revenda(String qtd_revenda) {
        this.qtd_revenda = qtd_revenda;
    }
}
