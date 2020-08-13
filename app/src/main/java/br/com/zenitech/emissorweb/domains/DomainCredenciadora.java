package br.com.zenitech.emissorweb.domains;

public class DomainCredenciadora {
    private String codigo_credenciadora;
    private String descricao_credenciadora;
    private String cnpj_credenciadora;
    private String bandeira_credenciadora;

    public DomainCredenciadora(String codigo_credenciadora, String descricao_credenciadora, String cnpj_credenciadora, String bandeira_credenciadora) {
        this.codigo_credenciadora = codigo_credenciadora;
        this.descricao_credenciadora = descricao_credenciadora;
        this.cnpj_credenciadora = cnpj_credenciadora;
        this.bandeira_credenciadora = bandeira_credenciadora;
    }

    public String getCodigo_credenciadora() {
        return codigo_credenciadora;
    }

    public void setCodigo_credenciadora(String codigo_credenciadora) {
        this.codigo_credenciadora = codigo_credenciadora;
    }

    public String getDescricao_credenciadora() {
        return descricao_credenciadora;
    }

    public void setDescricao_credenciadora(String descricao_credenciadora) {
        this.descricao_credenciadora = descricao_credenciadora;
    }

    public String getCnpj_credenciadora() {
        return cnpj_credenciadora;
    }

    public void setCnpj_credenciadora(String cnpj_credenciadora) {
        this.cnpj_credenciadora = cnpj_credenciadora;
    }

    public String getBandeira_credenciadora() {
        return bandeira_credenciadora;
    }

    public void setBandeira_credenciadora(String bandeira_credenciadora) {
        this.bandeira_credenciadora = bandeira_credenciadora;
    }
}
