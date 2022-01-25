package br.com.zenitech.emissorweb.domains;


public class FormaPagamentoPedido {
    public String id;
    public String id_pedido;
    public String id_forma_pagamento;
    public String valor;
    public String codigo_autorizacao;
    public String cardBrand;
    public String nsu;
    public String id_cobranca_pix;
    public String status_pix;

    public FormaPagamentoPedido(String id, String id_pedido, String id_forma_pagamento, String valor, String codigo_autorizacao, String cardBrand, String nsu, String id_cobranca_pix, String status_pix) {
        this.id = id;
        this.id_pedido = id_pedido;
        this.id_forma_pagamento = id_forma_pagamento;
        this.valor = valor;
        this.codigo_autorizacao = codigo_autorizacao;
        this.cardBrand = cardBrand;
        this.nsu = nsu;
        this.id_cobranca_pix = id_cobranca_pix;
        this.status_pix = status_pix;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId_pedido() {
        return id_pedido;
    }

    public void setId_pedido(String id_pedido) {
        this.id_pedido = id_pedido;
    }

    public String getId_forma_pagamento() {
        return id_forma_pagamento;
    }

    public void setId_forma_pagamento(String id_forma_pagamento) {
        this.id_forma_pagamento = id_forma_pagamento;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getCodigo_autorizacao() {
        return codigo_autorizacao;
    }

    public void setCodigo_autorizacao(String codigo_autorizacao) {
        this.codigo_autorizacao = codigo_autorizacao;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public String getNsu() {
        return nsu;
    }

    public void setNsu(String nsu) {
        this.nsu = nsu;
    }

    public String getId_cobranca_pix() {
        return id_cobranca_pix;
    }

    public void setId_cobranca_pix(String id_cobranca_pix) {
        this.id_cobranca_pix = id_cobranca_pix;
    }

    public String getStatus_pix() {
        return status_pix;
    }

    public void setStatus_pix(String status_pix) {
        this.status_pix = status_pix;
    }
}
