package br.com.zenitech.emissorweb.interfaces;

import br.com.zenitech.emissorweb.Configuracoes;
import br.com.zenitech.emissorweb.domains.DomainPrincipal;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IPrincipal {

    //SINCRONIZAR
    @FormUrlEncoded
    @POST("indexApp.php")
    Call<DomainPrincipal> verificarPermNFE(@Field("TELA") String tela, @Field("SERIAL") String serial);

    // TRANSMITIR NOTAS NFC-E
    @FormUrlEncoded
    @POST("indexApp.php")
    Call<DomainPrincipal> consultarCli(
            @Field("TELA") String tela,
            @Field("SERIAL") String serial,
            @Field("CODCLI") String codcli
    );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(String.format("%s%s", new Configuracoes().GetUrlServer(), "/POSSIAC/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
