package br.com.zenitech.emissorweb;

public class Configuracoes {

    // INFORMA SE O APARELHO UTILIZADO É UM POS
    // SEMPRE RETORNAR FALSE CONFORME FOR GERADO O BUILD PARA PLAYSTORE
    public boolean GetDevice(){
        return true;
    }

    public String GetUrlServer() {
        //return "http://pos2.zenitech.com.br:8080";
        return  "https://emissorweb.com.br/";
    }

    public String GetUFCeara(){
        return "CE";
    }
}
