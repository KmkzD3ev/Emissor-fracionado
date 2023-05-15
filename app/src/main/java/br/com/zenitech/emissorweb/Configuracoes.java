package br.com.zenitech.emissorweb;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import okhttp3.OkHttpClient;
import stone.environment.Environment;

import static stone.environment.Environment.PRODUCTION;
import static stone.environment.Environment.SANDBOX;

import java.util.concurrent.TimeUnit;

public class Configuracoes {

    // FALSE PARA DEFINIR PRODUÇÃO
    final boolean ambinteTeste = false;

    // INFORMA SE O APARELHO UTILIZADO É UM POS
    // SEMPRE RETORNAR FALSE CONFORME FOR GERADO O BUILD PARA PLAYSTORE
    public boolean GetDevice() {
        return true;
        //return false;
    }

    // RETORNASE O AMBIENTE É DE PRODUÇÃO OU DE TESTE
    public Environment Ambiente() {
        if (ambinteTeste)
            return SANDBOX;
        else
            return PRODUCTION;
    }

    public String GetUrlServer() {
        return "https://emissorweb.com.br/";
    }

    public String GetUFCeara() {
        return "CE";
    }

    public String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    //
    public static String token_authorization;



    public static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build();
}
