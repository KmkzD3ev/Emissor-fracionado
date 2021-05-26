package br.com.zenitech.emissorweb.interfaces;

import br.com.zenitech.emissorweb.Configuracoes;
import br.com.zenitech.emissorweb.domains.Sincronizador;
import br.com.zenitech.emissorweb.domains.ValidarNFCe;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ISincronizar {

    //SINCRONIZAR
    @FormUrlEncoded
    @POST("sincronizar_banco_app_teste.php")
    Call<Sincronizador> sincronizar(@Field("SERIAL") String SERIAL);

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

    //SINCRONIZAR
    @FormUrlEncoded
    @POST("ativar_desativa_pos_app.php")
    Call<Sincronizador> verificarVersaoApp(@Field("opcao") String opcao);

    //.baseUrl("http://177.153.22.33/POSSIAC/")
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(String.format("%s%s", new Configuracoes().GetUrlServer(), "/POSSIAC/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
