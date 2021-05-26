package br.com.zenitech.emissorweb.domains;


public class PedidosTemp {
    private String id;
    private String situacao;
    private String protocolo;
    private String data;
    private String hora;
    private String valor_total;
    private String data_protocolo;
    private String hora_protocolo;
    private String cpf_cliente;
    private String forma_pagamento;

    public PedidosTemp(String id, String situacao, String protocolo, String data, String hora, String valor_total, String data_protocolo, String hora_protocolo, String cpf_cliente, String forma_pagamento) {
        this.id = id;
        this.situacao = situacao;
        this.protocolo = protocolo;
        this.data = data;
        this.hora = hora;
        this.valor_total = valor_total;
        this.data_protocolo = data_protocolo;
        this.hora_protocolo = hora_protocolo;
        this.cpf_cliente = cpf_cliente;
        this.forma_pagamento = forma_pagamento;
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

    public String getData_protocolo() {
        return data_protocolo;
    }

    public void setData_protocolo(String data_protocolo) {
        this.data_protocolo = data_protocolo;
    }

    public String getHora_protocolo() {
        return hora_protocolo;
    }

    public void setHora_protocolo(String hora_protocolo) {
        this.hora_protocolo = hora_protocolo;
    }

    public String getCpf_cliente() {
        return cpf_cliente;
    }

    public void setCpf_cliente(String cpf_cliente) {
        this.cpf_cliente = cpf_cliente;
    }

    public String getForma_pagamento() {
        return forma_pagamento;
    }

    public void setForma_pagamento(String forma_pagamento) {
        this.forma_pagamento = forma_pagamento;
    }
}
