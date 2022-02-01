package br.com.zenitech.emissorweb.interfaces;

import br.com.zenitech.emissorweb.Configuracoes;
import br.com.zenitech.emissorweb.domains.LogCartao;
import br.com.zenitech.emissorweb.domains.PixDomain;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ILogCartao {

    // CRIA UMA COBRANÇA E RECEBE O QRCODE
    @FormUrlEncoded
    @POST("log_cartao_app_emissor.php")
    Call<LogCartao> enviarLogCartao(
            @Field("opcao") String opcao,
            @Field("serial") String serial,
            @Field("log") String log
    );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(String.format("%s%s", new Configuracoes().GetUrlServer(), "/POSSIAC/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
