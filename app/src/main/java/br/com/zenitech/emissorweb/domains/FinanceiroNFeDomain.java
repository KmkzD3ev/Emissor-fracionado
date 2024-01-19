package br.com.zenitech.emissorweb.domains;

public class FinanceiroNFeDomain {
    public String id;
    public String id_pedido;
    public String id_forma_pagamento;
    public String valor;
    public String codigo_autorizacao;
    public String cardBrand;
    public String nsu;
    public String id_cobranca_pix;
    public String status_pix;
    public String parcelas;
    public String vencimento;

    public FinanceiroNFeDomain(String id, String id_pedido, String id_forma_pagamento, String valor, String codigo_autorizacao, String cardBrand, String nsu, String id_cobranca_pix, String status_pix, String parcelas, String vencimento) {
        this.id = id;
        this.id_pedido = id_pedido;
        this.id_forma_pagamento = id_forma_pagamento;
        this.valor = valor;
        this.codigo_autorizacao = codigo_autorizacao;
        this.cardBrand = cardBrand;
        this.nsu = nsu;
        this.id_cobranca_pix = id_cobranca_pix;
        this.status_pix = status_pix;
        this.parcelas = parcelas;
        this.vencimento = vencimento;
    }
}
