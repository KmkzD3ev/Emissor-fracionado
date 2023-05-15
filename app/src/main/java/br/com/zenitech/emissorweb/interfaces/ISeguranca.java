package br.com.zenitech.emissorweb.interfaces;

import static br.com.zenitech.emissorweb.Configuracoes.okHttpClient;

import br.com.zenitech.emissorweb.Configuracoes;
import br.com.zenitech.emissorweb.domains.SegurancaDomain;
import br.com.zenitech.emissorweb.domains.Sincronizador;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ISeguranca {

    //SINCRONIZAR
    @FormUrlEncoded
    @POST("seguranca_app.php")
    Call<SegurancaDomain> validarCodigo(
            @Field("opcao") String opcao,
            @Field("serial") String serial,
            @Field("codigo") String codigo);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(String.format("%s%s", new Configuracoes().GetUrlServer(), "POSSIAC/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
