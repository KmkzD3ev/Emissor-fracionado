package br.com.zenitech.emissorweb.interfaces;

import br.com.zenitech.emissorweb.Configuracoes;
import br.com.zenitech.emissorweb.domains.PixDomain;
import br.com.zenitech.emissorweb.domains.Sincronizador;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IPix {

    // CRIA UMA COBRANÇA E RECEBE O QRCODE
    @FormUrlEncoded
    @POST("pixApp.php")
    Call<PixDomain> getImgQrCode(
            @Field("opcao") String opcao,
            @Field("pix_key") String pix_key,
            @Field("client_id") String client_id,
            @Field("client_secret") String client_secret,
            @Field("pedido") String pedido,
            @Field("valor") String valor,
            @Field("cliente") String cliente,
            @Field("user_agente") String user_agente
    );

    //SINCRONIZAR
    @FormUrlEncoded
    @POST("pixApp.php")
    Call<PixDomain> getStatusCobranca(
            @Field("opcao") String opcao,
            @Field("client_id") String client_id,
            @Field("client_secret") String client_secret,
            @Field("id") String id,
            @Field("token_authorization") String token_authorization,
            @Field("cliente") String cliente,
            @Field("user_agente") String user_agente
    );

    //SINCRONIZAR
    @FormUrlEncoded
    @POST("pixApp.php")
    Call<PixDomain> pegarQrCode(
            @Field("opcao") String opcao,
            @Field("client_id") String client_id,
            @Field("client_secret") String client_secret,
            @Field("id") String id,
            @Field("token_authorization") String token_authorization,
            @Field("cliente") String cliente,
            @Field("user_agente") String user_agente
    );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(String.format("%s%s", new Configuracoes().GetUrlServer(), "/POSSIAC/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
