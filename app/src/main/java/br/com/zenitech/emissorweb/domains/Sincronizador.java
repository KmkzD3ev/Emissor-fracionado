package br.com.zenitech.emissorweb.domains;

public class Sincronizador {
    private String serial;
    private String erro;
    private String codigo_instalacao;

    public Sincronizador(String serial, String erro, String codigo_instalacao) {
        this.serial = serial;
        this.erro = erro;
        this.codigo_instalacao = codigo_instalacao;
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

    public String getCodigo_instalacao() {
        return codigo_instalacao;
    }

    public void setCodigo_instalacao(String codigo_instalacao) {
        this.codigo_instalacao = codigo_instalacao;
    }
}
