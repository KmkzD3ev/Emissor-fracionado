package br.com.zenitech.emissorweb.domains;

public class Sincronizador {
    private String serial;
    private String erro;

    public Sincronizador(String serial, String erro) {
        this.serial = serial;
        this.erro = erro;
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
}
