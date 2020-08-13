package br.com.zenitech.emissorweb.domains;

//percentual = total da nota

public class PosApp {
    private String codigo;
    private String serial;
    private String unidade;
    private String serie;
    private String ultnfce;
    private String ultboleto;
    private String nota_remessa;
    private String serie_remessa;

    public PosApp(String codigo, String serial, String unidade, String serie, String ultnfce, String ultboleto, String nota_remessa, String serie_remessa) {
        this.codigo = codigo;
        this.serial = serial;
        this.unidade = unidade;
        this.serie = serie;
        this.ultnfce = ultnfce;
        this.ultboleto = ultboleto;
        this.nota_remessa = nota_remessa;
        this.serie_remessa = serie_remessa;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getUltnfce() {
        return ultnfce;
    }

    public void setUltnfce(String ultnfce) {
        this.ultnfce = ultnfce;
    }

    public String getUltboleto() {
        return ultboleto;
    }

    public void setUltboleto(String ultboleto) {
        this.ultboleto = ultboleto;
    }

    public String getNota_remessa() {
        return nota_remessa;
    }

    public void setNota_remessa(String nota_remessa) {
        this.nota_remessa = nota_remessa;
    }

    public String getSerie_remessa() {
        return serie_remessa;
    }

    public void setSerie_remessa(String serie_remessa) {
        this.serie_remessa = serie_remessa;
    }
}
