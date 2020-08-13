package br.com.zenitech.emissorweb.domains;

public class DomainPrincipal {
    private String serial;
    private String erro;
    private String nome_cliente;

    public DomainPrincipal(String serial, String erro, String nome_cliente) {
        this.serial = serial;
        this.erro = erro;
        this.nome_cliente = nome_cliente;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }

    public String getNome_cliente() {
        return nome_cliente;
    }

    public void setNome_cliente(String nome_cliente) {
        this.nome_cliente = nome_cliente;
    }
}
