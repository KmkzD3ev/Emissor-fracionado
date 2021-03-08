package br.com.zenitech.emissorweb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.datecs.api.BuildInfo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import br.com.stone.posandroid.providers.PosPrintProvider;
import br.com.zenitech.emissorweb.controller.PrintViewHelper;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PedidosNFE;
import br.com.zenitech.emissorweb.domains.Unidades;
import stone.application.interfaces.StoneCallbackInterface;

import static br.com.zenitech.emissorweb.ClassAuxiliar.getSha1Hex;

public class ImpressoraPOS extends AppCompatActivity {

    private static final String LOG_TAG = "Impressora";

    //
    private DatabaseHelper bd;
    private ClassAuxiliar cAux;

    //DADOS PARA IMPRESSÃO
    String pedido, cliente, id_produto, produto, protocolo, chave, quantidade,
            valor, valorUnit, tributos, posicao, tipoImpressao, form_pagamento;

    TextView total;
    public TextView imprimindo;

    public static String[] linhaProduto;

    ArrayList<Unidades> elementosUnidade;
    Unidades unidades;

    //ArrayList<PosApp> elementosPos;
    //PosApp posApp;

    // NFC-e
    ArrayList<Pedidos> elementosPedidos;
    Pedidos pedidos;

    ArrayList<ItensPedidos> elementosItens;
    ItensPedidos itensPedidos;

    // NF-e
    ArrayList<PedidosNFE> elementosPedidosNFE;
    PedidosNFE pedidosNFE;

    Context context;
    ImageView qrcode;

    String enderecoBlt = "";
    String tamFont = "";
    SharedPreferences prefs;

    //
    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
    File myDir = new File(root + "/Emissor_Web");

    //
    String dataHoraCan, codAutCan;
    PrintViewHelper printViewHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impressora);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // **************************** IMPRESSÃO POS **********************************************
        /*PosPrintProvider customPosPrintProvider = new PosPrintProvider(this);
        customPosPrintProvider.addLine("TESTE");
        customPosPrintProvider.addLine("");
        //customPosPrintProvider.addBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo_emissor_web));
        customPosPrintProvider.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Recibo impresso", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError() {
                Toast.makeText(context, "Erro ao imprimir: " + customPosPrintProvider.getListOfErrors(), Toast.LENGTH_SHORT).show();
            }
        });
        customPosPrintProvider.execute();*/

        // *****************************************************************************************

        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);

        bd = new DatabaseHelper(this);
        //
        cAux = new ClassAuxiliar();
        context = this;

        imprimindo = findViewById(R.id.imprimindo);
        total = findViewById(R.id.total);
        qrcode = findViewById(R.id.qrcode);

        printViewHelper = new PrintViewHelper();

        // Show Android device information and API version.
        final TextView txtVersion = findViewById(R.id.txt_version);
        String txt = Build.MANUFACTURER + " " + Build.MODEL + ", Datecs API " + BuildInfo.VERSION;
        txtVersion.setText(txt);
        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {

                posicao = params.getString("posicao");
                pedido = params.getString("pedido");
                cliente = params.getString("cliente");
                id_produto = params.getString("id_produto");
                produto = params.getString("produto");
                protocolo = params.getString("protocolo");
                chave = params.getString("chave");
                quantidade = params.getString("quantidade");
                valor = params.getString("valor");
                valorUnit = params.getString("valorUnit");
                tributos = params.getString("tributos");
                tipoImpressao = params.getString("imprimir");
                form_pagamento = params.getString("form_pagamento");

                linhaProduto = new String[]{
                        "1 " + id_produto + "      " + produto,
                        "" + quantidade + "     " + "UN     " + valorUnit + "   " + valor,
                        valor,
                        protocolo,
                        cliente,
                        tributos,
                        chave,
                        params.getString("razao_social"),
                        params.getString("cnpj"),
                        params.getString("endereco"),
                        params.getString("bairro"),
                        params.getString("cep"),
                        form_pagamento
                };

                // COMPROVANTE CANCELAMENTO CARTÃO
                dataHoraCan = params.getString("dataHoraCan");
                codAutCan = params.getString("codAutCan");

            } else {
                Toast.makeText(context, "Envie algo para imprimir!", Toast.LENGTH_LONG).show();
            }
        }

        //CRIA A LISTA DE PEDIDOS PARA IMPRESSÃO
        elementosPedidos = bd.getPedidosRelatorio();
        elementosPedidosNFE = bd.getPedidosRelatorioNFE();

        try {
            printNFCE58mm(linhaProduto);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void printP() {
        /////////////////////////////////////////////
        PosPrintProvider p = new PosPrintProvider(this);
        //ppp.addBitmap(new PrintViewHelper().generateBitmapFromView(LLlistaPedidos));
        p.addLine("-----------------------------------------");
        p.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Recibo impresso", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError() {
                Toast.makeText(context, "Erro ao imprimir: " + p.getListOfErrors(), Toast.LENGTH_SHORT).show();
            }
        });
        p.execute();
    }

    private void toast(final String text) {
        Log.d(LOG_TAG, text);

        runOnUiThread(() -> Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show());
    }

    /***************************** - IMPRESSÃO - *********************************/

    // ** SALVA A IMAGEM COM O QCODE OU COD. BARRA
    private void SaveImage(Bitmap finalBitmap) {

        myDir.mkdirs();

        String fname = "qrcode.png";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --------------------     IMPRESSÃO DE NFC-e      --------------------------------------------
    private void printNFCE58mm(final String[] texto) throws FileNotFoundException {

        String serie = bd.getSeriePOS();
        elementosUnidade = bd.getUnidades();

        String urlConsulta, urlQRCode, idCSC, CSC, hashSHA1;
        unidades = elementosUnidade.get(0);

        urlConsulta = unidades.getUrl_consulta();
        urlQRCode = unidades.getUrl_qrcode();
        idCSC = unidades.getIdCSC();
        CSC = unidades.getCSC();

        String url;
        if (texto[3].equalsIgnoreCase("EMITIDA EM CONTINGENCIA")) {
            // Chave de Acesso da NFC-e
            // Versão do QR Code
            // Identificação do Ambiente (1 – Produção, 2 – Homologação)
            // Dia da data de emissão
            // Valor Total da NFC-e
            // DigestValue da NFCe
            // Identificador do CSC (Código de Segurança do Contribuinte no Banco de Dados da SEFAZ)
            // Código Hash dos Parâmetros

            hashSHA1 = texto[6] + "|" + "2" + "|" + "1" + "|" + idCSC + CSC;
            hashSHA1 = getSha1Hex(hashSHA1);

            url = urlQRCode + "?p=" + texto[6] + "|2|1|" + idCSC + "|" + hashSHA1;
        } else {
            // Chave de Acesso da NFC-e
            // Versão do QR Code
            // Identificação do Ambiente (1 – Produção, 2 – Homologação)
            // Identificador do CSC (Código de Segurança do Contribuinte no Banco de Dados da SEFAZ)
            // Código Hash dos Parâmetros

            hashSHA1 = texto[6] + "|" + "2" + "|" + "1" + "|" + idCSC + CSC;
            hashSHA1 = getSha1Hex(hashSHA1);

            url = urlQRCode + "?p=" + texto[6] + "|2|1|" + idCSC + "|" + hashSHA1;
        }

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE, 250, 250);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bp = barcodeEncoder.createBitmap(bitMatrix);

            SaveImage(bp);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        // Retorna o caminho da imagem do qrcode
        File sdcard = Environment.getExternalStorageDirectory().getAbsoluteFile();
        File dir = new File(sdcard, "Emissor_Web/");

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        FileInputStream inputStream;
        BufferedInputStream bufferedInputStream;

        inputStream = new FileInputStream(dir.getPath() + "/qrcode.png");
        bufferedInputStream = new BufferedInputStream(inputStream);
        Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream, null, options);

        //
        PosPrintProvider ppp = new PosPrintProvider(this);
        Bitmap bitmap1 = printViewHelper.generateBitmapFromView(findViewById(R.id.t));
        ppp.addBitmap(bitmap1);

        /*//IMPRIMIR CABEÇALHO
        ppp.addLine(texto[7]);
        ppp.addLine(texto[8]);
        ppp.addLine(texto[9]);
        ppp.addLine(texto[10]);
        ppp.addLine(texto[11]);

        //DANFE NFC-e
        ppp.addLine("-----------------------------------------");
        ppp.addLine("DANFE NFC-e - DOCUMENTO AUXILIAR DA");
        ppp.addLine("NOTA FISCAL DE CONSUMIDOR ELETRONICA");
        ppp.addLine("-----------------------------------------");

        //INFOR. PEDIDO
        ppp.addLine("# COD. DESC. QTDE. UN.  VL.UNIT.  VL.TOTAL");
        ppp.addLine(texto[0]);
        ppp.addLine(texto[1]);
        ppp.addLine("-----------------------------------------");

        //INFOR. VALORES
        ppp.addLine("Qtde. Total de Itens                  " + quantidade);
        ppp.addLine("Valor Total                        " + texto[2]);
        ppp.addLine("FORMA DE PAGAMENTO            VALOR PAGO");
        ppp.addLine(cAux.removerAcentos(texto[12]) + "                         " + texto[2]);
        ppp.addLine("-----------------------------------------");

        //TRIBUTOS TOTAIS
        ppp.addLine("Tributos totais incidentes");
        ppp.addLine("(Lei Federal 12.741/2012)         " + texto[5]);
        ppp.addLine("-----------------------------------------");

        //EMISSÃO
        ppp.addLine("Numero:" + pedido + " Serie:" + serie);
        ppp.addLine("Emissao:" + cAux.exibirDataAtual() + " " + cAux.horaAtual());
        ppp.addLine("-----------------------------------------");

        //COSULTA NA RECEITA
        ppp.addLine("Consulte pela Chave de Acesso em");
        ppp.addLine(urlConsulta);
        ppp.addLine("");
        ppp.addLine("Chave de Acesso");
        String c = texto[6];
        String cl1 = c.substring(0, 4) + " " + c.substring(4, 8) + " " + c.substring(8, 12) + " " + c.substring(12, 16) + " " + c.substring(16, 20);
        String cl2 = c.substring(20, 24) + " " + c.substring(24, 28) + " " + c.substring(28, 32) + " " + c.substring(32, 36);
        ppp.addLine(cl1);
        ppp.addLine(cl2);
        ppp.addLine("");
        ppp.addLine("Protocolo de autorizacao");
        ppp.addLine(texto[3]);
        ppp.addLine("-----------------------------------------");

        //CONSUMIDOR
        ppp.addLine(texto[4]);
        ppp.addLine("-----------------------------------------");


        //ppp.addBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo_emissor_web));
        ppp.addBitmap(bitmap);*/
        ppp.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Recibo impresso", Toast.LENGTH_SHORT).show();
                finalizarImpressao();
            }

            @Override
            public void onError() {
                Toast.makeText(context, "Erro ao imprimir: " + ppp.getListOfErrors(), Toast.LENGTH_SHORT).show();
            }
        });
        ppp.execute();

        // Apaga a imgem anterior
        File imgQrC = new File(sdcard, "Emissor_Web/qrcode.png");
        imgQrC.delete();
    }

    // --------------------     IMPRESSÃO DE NF-e       --------------------------------------------


    // --------------------     IMPRESSÃO DE RELATÓRIOS     ----------------------------------------

    //
    private void finalizarImpressao() {
        Intent i = new Intent(ImpressoraPOS.this, Principal.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("nomeImpressoraBlt", enderecoBlt);
        i.putExtra("enderecoBlt", enderecoBlt);
        startActivity(i);
        finish();
    }
}
