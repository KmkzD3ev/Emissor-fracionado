package br.com.zenitech.emissorweb.controller

import br.com.zenitech.emissorweb.domains.LogCartao
import br.com.zenitech.emissorweb.interfaces.ILogCartao
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogCartaoControllerKT {

    fun enviarLogCartao(serial: String?, log: String?) {

        //
        val iLogCartao = ILogCartao.retrofit.create(ILogCartao::class.java)
        val call = iLogCartao.enviarLogCartao("log_cartao", serial, log)
        call.enqueue(object : Callback<LogCartao?> {
            override fun onResponse(call: Call<LogCartao?>, response: Response<LogCartao?>) {
                //
                //final LogCartao logCartao = response.body();
            }

            override fun onFailure(call: Call<LogCartao?>, t: Throwable) {
                //Log.i("LogCartao", Objects.requireNonNull(t.getMessage()));
            }
        })
    }
}