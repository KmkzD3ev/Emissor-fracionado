package br.com.zenitech.emissorweb.interfaces;

import br.com.zenitech.emissorweb.Configuracoes;
import br.com.zenitech.emissorweb.domains.PixDomain;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IPixAssas {

    // CRIA UMA COBRANÇA E RECEBE O QRCODE
    @FormUrlEncoded
    @POST("pixApp.php")
    Call<PixDomain> getImgQrCode(
            @Field("opcao") String opcao,
            @Field("apiKey") String apiKey,
            @Field("cliente") String cliente,
            @Field("pedido") String pedido,
            @Field("valor") String valor
    );

    //SINCRONIZAR
    @FormUrlEncoded
    @POST("pixApp.php")
    Call<PixDomain> getStatusCobranca(
            @Field("opcao") String opcao,
            @Field("apiKey") String apiKey,
            @Field("id") String id
    );

    //SINCRONIZAR
    @FormUrlEncoded
    @POST("pixApp.php")
    Call<PixDomain> pegarQrCode(
            @Field("opcao") String opcao,
            @Field("apiKey") String apiKey,
            @Field("id") String id
    );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(String.format("%s%s", new Configuracoes().GetUrlServer(), "/POSSIAC/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
