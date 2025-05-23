package br.com.zenitech.emissorweb.domains;

import java.util.List;

public class ValidarNFE {
    private String protocolo;
    private String erro;
    private String cnpj_dest;
    private String ie_dest;
    private String barcode;
    private String nome;
    private String endereco_dest;
    private String nnf;
    private String serie;
    private String chave;
    private List<Produtos> prods_nota;
    private String total_nota;
    private String inf_cpl;
    private String nat_op;
    private String tp_nf;

    public List<Produtos> desc_produtos;
    public List<Produtos> info_produtos;
    public String data_emissao;

    public ValidarNFE(String protocolo, String erro, String cnpj_dest, String ie_dest, String barcode, String nome, String endereco_dest, String nnf, String serie, String chave, List<Produtos> prods_nota, String total_nota, String inf_cpl, String nat_op, String tp_nf) {
        this.protocolo = protocolo;
        this.erro = erro;
        this.cnpj_dest = cnpj_dest;
        this.ie_dest = ie_dest;
        this.barcode = barcode;
        this.nome = nome;
        this.endereco_dest = endereco_dest;
        this.nnf = nnf;
        this.serie = serie;
        this.chave = chave;
        this.prods_nota = prods_nota;
        this.total_nota = total_nota;
        this.inf_cpl = inf_cpl;
        this.nat_op = nat_op;
        this.tp_nf = tp_nf;
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

    public String getCnpj_dest() {
        return cnpj_dest;
    }

    public void setCnpj_dest(String cnpj_dest) {
        this.cnpj_dest = cnpj_dest;
    }

    public String getIe_dest() {
        return ie_dest;
    }

    public void setIe_dest(String ie_dest) {
        this.ie_dest = ie_dest;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEndereco_dest() {
        return endereco_dest;
    }

    public void setEndereco_dest(String endereco_dest) {
        this.endereco_dest = endereco_dest;
    }

    public String getNnf() {
        return nnf;
    }

    public void setNnf(String nnf) {
        this.nnf = nnf;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public List<Produtos> getProds_nota() {
        return prods_nota;
    }

    public void setProds_nota(List<Produtos> prods_nota) {
        this.prods_nota = prods_nota;
    }

    public String getTotal_nota() {
        return total_nota;
    }

    public void setTotal_nota(String total_nota) {
        this.total_nota = total_nota;
    }

    public String getInf_cpl() {
        return inf_cpl;
    }

    public void setInf_cpl(String inf_cpl) {
        this.inf_cpl = inf_cpl;
    }

    public String getNat_op() {
        return nat_op;
    }

    public void setNat_op(String nat_op) {
        this.nat_op = nat_op;
    }

    public String getTp_nf() {
        return tp_nf;
    }

    public void setTp_nf(String tp_nf) {
        this.tp_nf = tp_nf;
    }
}
