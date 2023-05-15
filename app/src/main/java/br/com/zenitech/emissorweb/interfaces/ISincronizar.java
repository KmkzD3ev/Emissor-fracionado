package br.com.zenitech.emissorweb.interfaces;

import static br.com.zenitech.emissorweb.Configuracoes.okHttpClient;

import java.util.concurrent.TimeUnit;

import br.com.zenitech.emissorweb.Configuracoes;
import br.com.zenitech.emissorweb.domains.Sincronizador;
import br.com.zenitech.emissorweb.domains.ValidarNFCe;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ISincronizar {

    //SINCRONIZAR
    @FormUrlEncoded
    @POST("sincronizar_banco_app_emissor_3.php")
    Call<Sincronizador> sincronizar(
            @Field("SERIAL") String SERIAL,
            @Field("modelo_pos_app") String modelo_pos_app,
            @Field("numero_serie_pos_app") String numero_serie_pos_app,
            @Field("ultimo_sincronismo_banco_pos_app") String ultimo_sincronismo_banco_pos_app
    );

    //SINCRONIZAR
    @FormUrlEncoded
    @POST("ativar_desativa_pos_app.php")
    Call<Sincronizador> ativarDesativarPOS(@Field("opcao") String opcao, @Field("serial") String serial);

    //SINCRONIZAR
    @FormUrlEncoded
    @POST("ativar_desativa_pos_app.php")
    Call<Sincronizador> verificarSerial(@Field("opcao") String opcao, @Field("serial") String serial);

    //RESETAR APP
    @FormUrlEncoded
    @POST("ativar_desativa_pos_app.php")
    Call<Sincronizador> resetApp(@Field("opcao") String opcao, @Field("serial") String serial, @Field("codigo") String codigo);

    //RESETAR APP
    @FormUrlEncoded
    @POST("ativar_desativa_pos_app.php")
    Call<Sincronizador> forcarResetApp(@Field("opcao") String opcao, @Field("serial") String serial);

    //SINCRONIZAR
    @FormUrlEncoded
    @POST("ativar_desativa_pos_app.php")
    Call<Sincronizador> verificarVersaoApp(@Field("opcao") String opcao);

    /*OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)
            .build();*/

    //.baseUrl("http://177.153.22.33/POSSIAC/")
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(String.format("%s%s", new Configuracoes().GetUrlServer(), "/POSSIAC/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
