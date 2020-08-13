package br.com.zenitech.emissorweb.domains;


public class Unidades {
    private String codigo;
    private String razao_social;
    private String cnpj;
    private String endereco;
    private String numero;
    private String bairro;
    private String cep;
    private String telefone;
    private String ie;
    private String cidade;
    private String uf;
    private String codigo_ibge;
    private String url_consulta;
    private String codloja;
    private String idCSC;
    private String CSC;
    private String url_qrcode;

    public Unidades(String codigo, String razao_social, String cnpj, String endereco, String numero, String bairro, String cep, String telefone, String ie, String cidade, String uf, String codigo_ibge, String url_consulta, String codloja, String idCSC, String CSC, String url_qrcode) {
        this.codigo = codigo;
        this.razao_social = razao_social;
        this.cnpj = cnpj;
        this.endereco = endereco;
        this.numero = numero;
        this.bairro = bairro;
        this.cep = cep;
        this.telefone = telefone;
        this.ie = ie;
        this.cidade = cidade;
        this.uf = uf;
        this.codigo_ibge = codigo_ibge;
        this.url_consulta = url_consulta;
        this.codloja = codloja;
        this.idCSC = idCSC;
        this.CSC = CSC;
        this.url_qrcode = url_qrcode;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getRazao_social() {
        return razao_social;
    }

    public void setRazao_social(String razao_social) {
        this.razao_social = razao_social;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getIe() {
        return ie;
    }

    public void setIe(String ie) {
        this.ie = ie;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getCodigo_ibge() {
        return codigo_ibge;
    }

    public void setCodigo_ibge(String codigo_ibge) {
        this.codigo_ibge = codigo_ibge;
    }

    public String getUrl_consulta() {
        return url_consulta;
    }

    public void setUrl_consulta(String url_consulta) {
        this.url_consulta = url_consulta;
    }

    public String getCodloja() {
        return codloja;
    }

    public void setCodloja(String codloja) {
        this.codloja = codloja;
    }

    public String getIdCSC() {
        return idCSC;
    }

    public void setIdCSC(String idCSC) {
        this.idCSC = idCSC;
    }

    public String getCSC() {
        return CSC;
    }

    public void setCSC(String CSC) {
        this.CSC = CSC;
    }

    public String getUrl_qrcode() {
        return url_qrcode;
    }

    public void setUrl_qrcode(String url_qrcode) {
        this.url_qrcode = url_qrcode;
    }
}
