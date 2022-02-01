package br.com.zenitech.emissorweb.domains;


public class ClientesNFE {
    private String id;
    private String cliente;

    public ClientesNFE(String id, String cliente) {
        this.id = id;
        this.cliente = cliente;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }
}
