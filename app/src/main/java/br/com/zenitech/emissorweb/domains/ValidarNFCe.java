package br.com.zenitech.emissorweb.domains;

public class ValidarNFCe {
    private String protocolo;
    private String erro;

    public ValidarNFCe(String protocolo, String erro) {
        this.protocolo = protocolo;
        this.erro = erro;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }
}
