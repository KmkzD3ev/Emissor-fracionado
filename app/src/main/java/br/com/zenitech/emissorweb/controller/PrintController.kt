package br.com.zenitech.emissorweb.controller

import android.content.Context
import android.widget.Toast
import br.com.stone.posandroid.providers.PosPrintProvider
import br.com.stone.posandroid.providers.PosPrintReceiptProvider
import br.com.stone.posandroid.providers.PosReprintReceiptProvider
import stone.application.interfaces.StoneCallbackInterface

class PrintController(private val context: Context,
                      private val provider: PosPrintReceiptProvider,
                      private val str: String) {

    fun print() {
        provider.addLine(str)
        provider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                Toast.makeText(context, "Recibo impresso", Toast.LENGTH_SHORT).show()
            }

            override fun onError() {
                Toast.makeText(context, "Erro ao imprimir: "
                        + provider.listOfErrors, Toast.LENGTH_SHORT).show()
            }
        }

        provider.execute()
    }
}

class PrintControllerReprint(private val context: Context,
                           private val provider: PosReprintReceiptProvider,
                           private val str: String) {

    fun print() {
        provider.addLine(str)
        provider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                Toast.makeText(context, "Recibo impresso", Toast.LENGTH_SHORT).show()
            }

            override fun onError() {
                Toast.makeText(context, "Erro ao imprimir: "
                        + provider.listOfErrors, Toast.LENGTH_SHORT).show()
            }
        }

        provider.execute()
    }
}