package br.com.zenitech.emissorweb.interfaces;

import br.com.zenitech.emissorweb.Configuracoes;
import br.com.zenitech.emissorweb.domains.ValidarNFCe;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IValidarNFCe {

    // TRANSMITIR NOTAS NFC-E
    @FormUrlEncoded
    @POST("autorizador_app_novo_v1.php")
    Call<ValidarNFCe> validarNota(
            @Field("PEDIDO") String pedido,
            @Field("QTD") String qtd,
            @Field("SERIAL") String serial,
            @Field("PRODUTO") String produto,
            @Field("VLR") String vlr,
            @Field("FORMAP") String formap,
            @Field("CPFCLI") String cpfcli,
            @Field("CREDENCIADORA") String credenciadora,
            @Field("CAUTS") String codaut,
            @Field("NSU") String nsu,
            @Field("VLRFORMAP") String vlrformap,
            @Field("NAUTOCARTAO") String nautocartao,
            @Field("FRACIONADA") String fracionada
    );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(String.format("%s%s", new Configuracoes().GetUrlServer(), "/POSSIAC/AUTORIZADOR/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build();

}
