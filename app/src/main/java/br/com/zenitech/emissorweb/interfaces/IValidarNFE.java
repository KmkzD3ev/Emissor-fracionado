package br.com.zenitech.emissorweb.interfaces;

import static br.com.zenitech.emissorweb.Configuracoes.okHttpClient;

import br.com.zenitech.emissorweb.Configuracoes;
import br.com.zenitech.emissorweb.domains.ValidarNFE;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IValidarNFE {

    // TRANSMITIR NOTAS NFC-E
    @FormUrlEncoded
    @POST("indexApp.php")
    Call<ValidarNFE> validarNotaNFE(
            @Field("TELA") String tela,
            @Field("QTDS") String qtd,
            @Field("SERIAL") String serial,
            @Field("PRODS") String produto,
            @Field("VLRS") String vlr,
            @Field("FORMAPAG") String formap,
            @Field("CODCLI") String cpfcli,
            @Field("VLRSPAG") String vlrspag,
            @Field("CREDENCIADORA") String credenciadora,
            @Field("CAUTS") String codaut,
            @Field("BANDEIRA") String bandeira,
            @Field("VERSAO") int VERSAO
    );

    // REIMPRIMIR NOTA
    @FormUrlEncoded
    @POST("indexApp.php")
    Call<ValidarNFE> reimprimirNotaNFE(
            @Field("TELA") String tela,
            @Field("CODPED") String CODPED,
            @Field("SERIE") String SERIE,
            @Field("SERIAL") String SERIAL,
            @Field("VERSAO") int VERSAO
    );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(String.format("%s%s", new Configuracoes().GetUrlServer(), "/POSSIAC/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

}
