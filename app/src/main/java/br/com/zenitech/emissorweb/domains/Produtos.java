package br.com.zenitech.emissorweb.domains;

public class Produtos {

    public String desc_produtos;
    public String info_produtos;
    private String codigo;
    private String nome;
    private String tributos;
    private String valor_minimo;
    private String valor_maximo;
    private String qtd_revenda;

    public Produtos(String codigo, String nome, String tributos, String valor_minimo, String valor_maximo, String qtd_revenda) {
        this.codigo = codigo;
        this.nome = nome;
        this.tributos = tributos;
        this.valor_minimo = valor_minimo;
        this.valor_maximo = valor_maximo;
        this.qtd_revenda = qtd_revenda;
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
