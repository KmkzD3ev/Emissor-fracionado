package br.com.zenitech.emissorweb.domains;


public class Autorizacoes {
    private String id;
    private String pedido;
    private String RESPAG;
    private String BINCARTAO;
    private String NOMEINST;
    private String NSUAUT;
    private String CAUT;
    private String NPARCEL;
    private String RADQ;
    private String TCAR;
    private String TIPOTRANS;
    private String RECLOJA;
    private String RECCLI;
    private String NINST;
    private String CARTAO;
    private String CODAUTORIZACAO;

    public Autorizacoes(String id, String pedido, String RESPAG, String BINCARTAO, String NOMEINST, String NSUAUT, String CAUT, String NPARCEL, String RADQ, String TCAR, String TIPOTRANS, String RECLOJA, String RECCLI, String NINST, String CARTAO, String CODAUTORIZACAO) {
        this.id = id;
        this.pedido = pedido;
        this.RESPAG = RESPAG;
        this.BINCARTAO = BINCARTAO;
        this.NOMEINST = NOMEINST;
        this.NSUAUT = NSUAUT;
        this.CAUT = CAUT;
        this.NPARCEL = NPARCEL;
        this.RADQ = RADQ;
        this.TCAR = TCAR;
        this.TIPOTRANS = TIPOTRANS;
        this.RECLOJA = RECLOJA;
        this.RECCLI = RECCLI;
        this.NINST = NINST;
        this.CARTAO = CARTAO;
        this.CODAUTORIZACAO = CODAUTORIZACAO;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPedido() {
        return pedido;
    }

    public void setPedido(String pedido) {
        this.pedido = pedido;
    }

    public String getRESPAG() {
        return RESPAG;
    }

    public void setRESPAG(String RESPAG) {
        this.RESPAG = RESPAG;
    }

    public String getBINCARTAO() {
        return BINCARTAO;
    }

    public void setBINCARTAO(String BINCARTAO) {
        this.BINCARTAO = BINCARTAO;
    }

    public String getNOMEINST() {
        return NOMEINST;
    }

    public void setNOMEINST(String NOMEINST) {
        this.NOMEINST = NOMEINST;
    }

    public String getNSUAUT() {
        return NSUAUT;
    }

    public void setNSUAUT(String NSUAUT) {
        this.NSUAUT = NSUAUT;
    }

    public String getCAUT() {
        return CAUT;
    }

    public void setCAUT(String CAUT) {
        this.CAUT = CAUT;
    }

    public String getNPARCEL() {
        return NPARCEL;
    }

    public void setNPARCEL(String NPARCEL) {
        this.NPARCEL = NPARCEL;
    }

    public String getRADQ() {
        return RADQ;
    }

    public void setRADQ(String RADQ) {
        this.RADQ = RADQ;
    }

    public String getTCAR() {
        return TCAR;
    }

    public void setTCAR(String TCAR) {
        this.TCAR = TCAR;
    }

    public String getTIPOTRANS() {
        return TIPOTRANS;
    }

    public void setTIPOTRANS(String TIPOTRANS) {
        this.TIPOTRANS = TIPOTRANS;
    }

    public String getRECLOJA() {
        return RECLOJA;
    }

    public void setRECLOJA(String RECLOJA) {
        this.RECLOJA = RECLOJA;
    }

    public String getRECCLI() {
        return RECCLI;
    }

    public void setRECCLI(String RECCLI) {
        this.RECCLI = RECCLI;
    }

    public String getNINST() {
        return NINST;
    }

    public void setNINST(String NINST) {
        this.NINST = NINST;
    }

    public String getCARTAO() {
        return CARTAO;
    }

    public void setCARTAO(String CARTAO) {
        this.CARTAO = CARTAO;
    }

    public String getCODAUTORIZACAO() {
        return CODAUTORIZACAO;
    }

    public void setCODAUTORIZACAO(String CODAUTORIZACAO) {
        this.CODAUTORIZACAO = CODAUTORIZACAO;
    }
}
