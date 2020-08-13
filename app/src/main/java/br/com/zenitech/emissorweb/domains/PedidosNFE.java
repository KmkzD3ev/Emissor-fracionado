package br.com.zenitech.emissorweb.domains;


public class PedidosNFE {
    private String id;
    private String situacao;
    private String protocolo;
    private String data;
    private String hora;
    private String valor_total;
    private String cliente;

    public PedidosNFE(String id, String situacao, String protocolo, String data, String hora, String valor_total, String cliente) {
        this.id = id;
        this.situacao = situacao;
        this.protocolo = protocolo;
        this.data = data;
        this.hora = hora;
        this.valor_total = valor_total;
        this.cliente = cliente;
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
}
