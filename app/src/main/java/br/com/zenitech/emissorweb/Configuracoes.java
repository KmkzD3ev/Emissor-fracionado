package br.com.zenitech.emissorweb;

import stone.environment.Environment;

import static stone.environment.Environment.PRODUCTION;
import static stone.environment.Environment.SANDBOX;

public class Configuracoes {

    final boolean ambinteTeste = true;

    // INFORMA SE O APARELHO UTILIZADO É UM POS
    // SEMPRE RETORNAR FALSE CONFORME FOR GERADO O BUILD PARA PLAYSTORE
    public boolean GetDevice() {
        return false;
    }

    // RETORNASE O AMBIENTE É DE PRODUÇÃO OU DE TESTE
    public Environment Ambiente() {
        if (ambinteTeste)
            return SANDBOX;
        else
            return PRODUCTION;
    }

    public String GetUrlServer() {
        //return "http://pos2.zenitech.com.br:8080";
        return "https://emissorweb.com.br/";
    }

    public String GetUFCeara() {
        return "CE";
    }
}
