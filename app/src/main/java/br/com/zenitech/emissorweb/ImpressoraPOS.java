package br.com.zenitech.emissorweb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.datecs.api.BuildInfo;
import com.datecs.api.printer.Printer;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.stone.posandroid.providers.PosPrintProvider;
import br.com.stone.posandroid.providers.PosTransactionProvider;
import br.com.zenitech.emissorweb.controller.PrintViewHelper;
import br.com.zenitech.emissorweb.domains.AutorizacoesPinpad;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PedidosNFE;
import br.com.zenitech.emissorweb.domains.Unidades;
import stone.application.StoneStart;
import stone.application.enums.Action;
import stone.application.enums.InstalmentTransactionEnum;
import stone.application.enums.TypeOfTransactionEnum;
import stone.application.interfaces.StoneActionCallback;
import stone.application.interfaces.StoneCallbackInterface;
import stone.database.transaction.TransactionObject;
import stone.user.UserModel;
import stone.utils.Stone;

import static br.com.zenitech.emissorweb.ClassAuxiliar.getSha1Hex;

public class ImpressoraPOS extends AppCompatActivity implements StoneActionCallback {

    private static final String LOG_TAG = "Impressora";

    //
    private DatabaseHelper bd;
    private ClassAuxiliar cAux;

    //DADOS PARA IMPRESSÃO
    String pedido, cliente, id_produto, produto, protocolo, chave, quantidade,
            valor, valorUnit, tributos, tributosN, tributosE, tributosM, posicao, tipoImpressao, form_pagamento;

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
    PosPrintProvider ppp;

    //
    boolean impressao1 = false, impressao2 = false, impressao3 = false, impressao4 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impressora);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);

        bd = new DatabaseHelper(this);
        //
        cAux = new ClassAuxiliar();
        context = this;

        imprimindo = findViewById(R.id.imprimindo);
        total = findViewById(R.id.total);
        qrcode = findViewById(R.id.qrcode);

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
                tributosN = params.getString("tributosN");
                tributosE = params.getString("tributosE");
                tributosM = params.getString("tributosM");
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
                        form_pagamento,
                        tributosN,
                        tributosE,
                        tributosM,
                        id_produto, // 16
                        produto,    // 17
                        quantidade, // 18
                        valorUnit,  // 19
                        valor       // 20
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

        printViewHelper = new PrintViewHelper();
        ppp = new PosPrintProvider(this);

        try {
            if (tipoImpressao.equals("relatorio")) {
                //Log.i(LOG_TAG, "Relatório");
                printRelatorio();

            } else if (tipoImpressao.equals("nfe")) {

                //Imprimir nota fiscal eletronica
                printNFE(linhaProduto);

            } else if (tipoImpressao.equals("reimpressao_comprovante")) {

                //Imprimir comprovante do pagamento cartão
                reimpressaoComprovante();

            } else if (tipoImpressao.equals("comprovante_cancelamento")) {

                //Imprimir comprovante do pagamento cartão
            } else {

                //Imprimir nota fiscal eletronica
                printNFCE(linhaProduto);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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

    //
    private void liberarImpressora() {
        impressao1 = true;
        impressao2 = true;
        impressao3 = true;
        impressao4 = true;
        finalizarImpressao();
    }

    // --------------------     REIMPRESSÃO             --------------------------------------------
    private void reimpressaoComprovante() {
        PosPrintProvider ppp = new PosPrintProvider(this);

        /*final BitmapFactory.Options optionsStone = new BitmapFactory.Options();
        optionsStone.inScaled = false;

        // Logo Stone
        final AssetManager assetManagerStone = getApplicationContext().getAssets();
        final Bitmap bitmapStone = BitmapFactory.decodeStream(assetManagerStone.open("stone.png"),
                null, optionsStone);
        final int widthStone = Objects.requireNonNull(bitmapStone).getWidth();
        final int heightStone = bitmapStone.getHeight();
        final int[] argbStone = new int[widthStone * heightStone];
        bitmapStone.getPixels(argbStone, 0, widthStone, 0, 0, widthStone, heightStone);
        bitmapStone.recycle();

        //printer.reset();
        printer.printImage(argbStone, widthStone, heightStone, Printer.ALIGN_CENTER, true);
        printer.feedPaper(0);*/

        // Reimpressao
        /*final BitmapFactory.Options optionsReimpressao = new BitmapFactory.Options();
        optionsReimpressao.inScaled = false;
        final AssetManager assetManagerReimpressao = getApplicationContext().getAssets();
        final Bitmap bitmapReimpressao = BitmapFactory.decodeStream(assetManagerReimpressao.open("reimpressao.png"),
                null, optionsReimpressao);
        final int widthReimpressao = Objects.requireNonNull(bitmapReimpressao).getWidth();
        final int heightReimpressao = bitmapReimpressao.getHeight();
        final int[] argbReimpressao = new int[widthReimpressao * heightReimpressao];
        bitmapReimpressao.getPixels(argbReimpressao, 0, widthReimpressao, 0, 0, widthReimpressao, heightReimpressao);
        bitmapReimpressao.recycle();*/

        //Unidades unidades;
        //elementosUnidade = bd.getUnidades();
        AutorizacoesPinpad pinpad = bd.getAutorizacaoPinpad();

        //Log.e("impressora", pinpad.getAid() + " - " + pinpad.getRequestId() + " - " + pinpad.getServiceCode());

        //
        String txtCompPag = "Via do Lojista";
        //ppp.addLine(txtCompPag);
        //ppp.addLine("REIMPRESSÃO");

        //
        String txtCompPag2 = cAux.removerAcentos(pinpad.getNomeEmpresa()) + "\n" +
                cAux.removerAcentos(pinpad.getEnderecoEmpresa()) + "\n" +
                cAux.exibirData(pinpad.getDate()) + " " + pinpad.getTime() + " CNPJ:" + pinpad.getCnpjEmpresa() + "\n" +
                "--------------------------------------------------------------\n" +
                pinpad.getTypeOfTransactionEnum() + "                                         RS " + pinpad.getAmount() + "\n" +
                "--------------------------------------------------------------\n" +
                pinpad.getCardBrand() + " - " + pinpad.getCardHolderNumber().substring(pinpad.getCardHolderNumber().length() - 8) + "  AUT: " + pinpad.getAuthorizationCode() + "\n" +
                pinpad.getCardHolderName() + "\n" +
                pinpad.getRecipientTransactionIdentification() + "\n" +
                "Aprovado com senha\n" +
                "SN: " + prefs.getString("serial_app", "") + " - " + BuildConfig.VERSION_NAME + "\n";
        //ppp.addLine(txtCompPag2);

        TextView txtReimpressao = findViewById(R.id.txtReimpressao);
        txtReimpressao.setText(txtCompPag2);

        LinearLayout impressora1 = findViewById(R.id.printReimpressao);
        Bitmap bitmap2 = printViewHelper.createBitmapFromView(impressora1, 260, 230);


        ppp.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                liberarImpressora();
            }

            @Override
            public void onError() {
                liberarImpressora();
                Toast.makeText(context, "Erro ao imprimir: " + ppp.getListOfErrors(), Toast.LENGTH_SHORT).show();
            }
        });
        ppp.addBitmap(bitmap2);
        ppp.execute();
    }

    // --------------------     IMPRESSÃO DE NFC-e      --------------------------------------------
    private void printNFCE(final String[] texto) throws FileNotFoundException {

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
        ImageView imgQrCode = findViewById(R.id.imgQrCode);
        imgQrCode.setImageBitmap(bitmap);

        // ********************** IMPRIMIR CABEÇALHO
        TextView txtCab1 = findViewById(R.id.txtCab1);
        TextView txtCab2 = findViewById(R.id.txtCab2);
        TextView txtCab3 = findViewById(R.id.txtCab3);
        //
        txtCab1.setText(texto[7]);
        txtCab2.setText(texto[8]);
        txtCab3.setText(String.format("%s %s %s", texto[9], texto[10], texto[11]));

        // ********************** INFOR. PEDIDO
        TextView txtDescCod = findViewById(R.id.txtDescCod);
        TextView txtDescDesc = findViewById(R.id.txtDescDesc);
        TextView txtDescQuant = findViewById(R.id.txtDescQuant);
        TextView txtDescValUnit = findViewById(R.id.txtDescValUnit);
        TextView txtDescValTot = findViewById(R.id.txtDescValTot);
        //
        txtDescCod.setText(texto[16]);
        txtDescDesc.setText(texto[17]);
        txtDescQuant.setText(texto[18]);
        txtDescValUnit.setText(texto[19]);
        txtDescValTot.setText(texto[20]);

        // ********************** INFOR. VALORES
        TextView txtInfoVal1 = findViewById(R.id.txtInfoVal1);
        TextView txtInfoVal2 = findViewById(R.id.txtInfoVal2);
        TextView txtInfoVal3 = findViewById(R.id.txtInfoVal3);
        TextView txtInfoVal4 = findViewById(R.id.txtInfoVal4);
        //
        txtInfoVal1.setText(quantidade);
        txtInfoVal2.setText(texto[2]);
        txtInfoVal3.setText(cAux.removerAcentos(texto[12]));
        txtInfoVal4.setText(texto[2]);

        // ********************** TRIBUTOS TOTAIS
        TextView txtTributos = findViewById(R.id.txtTributos);
        TextView txtTributosN = findViewById(R.id.txtTributosN);
        TextView txtTributosE = findViewById(R.id.txtTributosE);
        TextView txtTributosM = findViewById(R.id.txtTributosM);
        //
        txtTributos.setText(texto[5]);
        txtTributosN.setText(texto[13]);
        txtTributosE.setText(texto[14]);
        txtTributosM.setText(texto[15]);

        // ********************** EMISSÃO
        TextView txtEmissao1 = findViewById(R.id.txtEmissao1);
        TextView txtEmissao2 = findViewById(R.id.txtEmissao2);
        TextView txtEmissao3 = findViewById(R.id.txtEmissao3);
        TextView txtEmissao4 = findViewById(R.id.txtEmissao4);
        //
        txtEmissao1.setText(pedido);
        txtEmissao2.setText(serie);
        txtEmissao3.setText(cAux.exibirDataAtual());
        txtEmissao4.setText(cAux.horaAtual());

        // ********************** COSULTA NA RECEITA
        TextView txtConsRec1 = findViewById(R.id.txtConsRec1);
        TextView txtConsRec2 = findViewById(R.id.txtConsRec2);
        TextView txtConsRec3 = findViewById(R.id.txtConsRec3);
        TextView txtConsRec4 = findViewById(R.id.txtConsRec4);
        //
        String c = texto[6];
        String cl1 = c.substring(0, 4) + " " + c.substring(4, 8) + " " + c.substring(8, 12) + " " + c.substring(12, 16) + " " + c.substring(16, 20);
        String cl2 = c.substring(20, 24) + " " + c.substring(24, 28) + " " + c.substring(28, 32) + " " + c.substring(32, 36);
        txtConsRec1.setText(urlConsulta);
        txtConsRec2.setText(cl1);
        txtConsRec3.setText(cl2);
        txtConsRec4.setText(texto[3]);

        //CONSUMIDOR
        TextView txtConsumidor = findViewById(R.id.txtConsumidor);
        txtConsumidor.setText(texto[4]);

        // GERAR IMAGEM DE IMPRESSÃO
        //
        LinearLayout CabecalhoNFCe = findViewById(R.id.CabecalhoNFCe);
        Bitmap bitmapCabecalhoNFCe = printViewHelper.createBitmapFromView(CabecalhoNFCe, 260, 104);
        //
        LinearLayout impressora = findViewById(R.id.teste);
        Bitmap bitmap1 = printViewHelper.createBitmapFromView(impressora, 260, 424);
        LinearLayout impressora1 = findViewById(R.id.teste1);
        Bitmap bitmap2 = printViewHelper.createBitmapFromView(impressora1, 180, 100);

        ppp.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                liberarImpressora();
            }

            @Override
            public void onError() {
                liberarImpressora();
                Toast.makeText(context, "Erro ao imprimir: " + ppp.getListOfErrors(), Toast.LENGTH_SHORT).show();
            }
        });

        ppp.addBitmap(bitmapCabecalhoNFCe);
        ppp.addBitmap(bitmap1);
        ppp.addBitmap(bitmap2);
        ppp.execute();

        // Apaga a imgem anterior
        File imgQrC = new File(sdcard, "Emissor_Web/qrcode.png");
        imgQrC.delete();
    }

    // --------------------     IMPRESSÃO DE NF-e       --------------------------------------------
    private void printNFE(final String[] texto) throws FileNotFoundException {

        elementosUnidade = bd.getUnidades();
        unidades = elementosUnidade.get(0);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(prefs.getString("chave", ""), BarcodeFormat.CODE_128, 300, 80);
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
        ImageView imgNfeQrCode = findViewById(R.id.imgNfeQrCode);
        imgNfeQrCode.setImageBitmap(bitmap);

        // ********************** IMPRIMIR CABEÇALHO
        TextView txtRecebemos = findViewById(R.id.txtRecebemos);
        //
        txtRecebemos.setText(String.format("Recebemos de %s os produtos constantes da NF-e %s Serie %s", prefs.getString("nome", ""), prefs.getString("nnf", ""), prefs.getString("serie", "")));

        // ********************** INFOR. VALORES
        TextView txtSerie = findViewById(R.id.txtSerie);
        TextView txtNfeConsRec2 = findViewById(R.id.txtNfeConsRec2);
        TextView txtNfeConsRec3 = findViewById(R.id.txtNfeConsRec3);

        //
        String c = prefs.getString("chave", "");// texto[6];
        String cl1 = c.substring(0, 4) + " " + c.substring(4, 8) + " " + c.substring(8, 12) + " " + c.substring(12, 16) + " " + c.substring(16, 20) + " " + c.substring(20, 24);
        String cl2 = c.substring(24, 28) + " " + c.substring(28, 32) + " " + c.substring(32, 36) + " " + c.substring(36, 40) + " " + c.substring(40, 44);

        //
        txtSerie.setText(String.format("N %s          -          SERIE %s", prefs.getString("nnf", ""), prefs.getString("serie", "")));
        txtNfeConsRec2.setText(cl1);
        txtNfeConsRec3.setText(cl2);

        // ********************** DADOS DO EMITENTE
        TextView txtNfeCab1 = findViewById(R.id.txtNfeCab1);
        TextView txtNfeCab2 = findViewById(R.id.txtNfeCab2);
        TextView txtNfeCab50 = findViewById(R.id.txtNfeCab50);
        TextView txtNfeCab51 = findViewById(R.id.txtNfeCab51);
        //
        txtNfeCab1.setText(texto[7]);
        txtNfeCab2.setText(texto[8]);
        txtNfeCab50.setText(String.format("%s %s", texto[9], texto[10]));
        txtNfeCab51.setText(texto[11]);

        // ********************** DADOS DO DESTINATARIO
        TextView txtNfeCab3 = findViewById(R.id.txtNfeCab3);
        TextView txtNfeCab4 = findViewById(R.id.txtNfeCab4);
        TextView txtNfeCab5 = findViewById(R.id.txtNfeCab5);
        //
        txtNfeCab3.setText(prefs.getString("nome", ""));
        txtNfeCab4.setText(String.format("CNPJ: %s   IE: %s", prefs.getString("cnpj_dest", ""), prefs.getString("ie_dest", "")));
        txtNfeCab5.setText(prefs.getString("endereco_dest", ""));

        // ********************** PRODUTOS
        TextView txtNFeDescProduto = findViewById(R.id.txtNFeDescProduto);
        TextView txtNfeInfoVal2 = findViewById(R.id.txtNfeInfoVal2);
        //
        txtNFeDescProduto.setText(prefs.getString("prods_nota", ""));
        txtNfeInfoVal2.setText(prefs.getString("total_nota", ""));

        // ********************** DADOS ADICIONAIS
        TextView txtNFeDadosAdd = findViewById(R.id.txtNFeDadosAdd);
        //
        txtNFeDadosAdd.setText("( DECLARAMOS QUE OS PRODUTOS ESTAO ADEQUADAMENTE " +
                "ACONDICIONADOS E ESTIVADOS PARA SUPORTAR OS RISCOS NORMAIS DAS ETAPAS NECESSARIAS A OPERACAO " +
                "DE TRANSPORTE (CARREGAMENTO, DESCARREGAMENTO, TRANSBORDO E TRANSPORTE) E QUE ATENDEM A REGULAMENTACAO " +
                "EM VIGOR. DATA: " + cAux.exibirDataAtual() + " .. . .. DECLARAMOS QUE A EXPEDICAO NAO CONTEM EMBALAGENS VAZIAS E " +
                "NAO LIMPAS DE PRODUTOS PERIGOSOS QUE APRESENTAM VALOR DE QUANTIDADE LIMITADA POR VEICULO (" +
                "COLUNA 8 DA RELACAO DE PRODUTOS PERIGOSOS) IGUAL A ZERO. DATA: " + cAux.exibirDataAtual() + " .. . .. /nASSINATURA: " +
                "____________________________________________________________________ " +
                prefs.getString("inf_cpl", "") + ")");

        //
        LinearLayout impressora = findViewById(R.id.printNfe);
        Bitmap bitmap1 = printViewHelper.createBitmapFromView(impressora, 380, 150);
        //
        LinearLayout impressora1 = findViewById(R.id.printNfe1);
        Bitmap bitmap2 = printViewHelper.createBitmapFromView(impressora1, 280, 50);
        //
        LinearLayout impressora2 = findViewById(R.id.printNfe2);
        Bitmap bitmap3 = printViewHelper.createBitmapFromView(impressora2, 280, 350);
        //
        LinearLayout impressora3 = findViewById(R.id.printNfe3);
        Bitmap bitmap4 = printViewHelper.createBitmapFromView(impressora3, 280, 250);

        ppp.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                liberarImpressora();
            }

            @Override
            public void onError() {
                liberarImpressora();
                Toast.makeText(context, "Erro ao imprimir: " + ppp.getListOfErrors(), Toast.LENGTH_SHORT).show();
            }
        });

        ppp.addBitmap(bitmap1);
        ppp.addBitmap(bitmap2);
        ppp.addBitmap(bitmap3);
        ppp.addBitmap(bitmap4);
        ppp.execute();

        // Apaga a imgem anterior
        File imgQrC = new File(sdcard, "Emissor_Web/qrcode.png");
        imgQrC.delete();
    }

    // --------------------     IMPRESSÃO DE RELATÓRIOS     ----------------------------------------
    private void printRelatorio() {

        PosPrintProvider pppCab = new PosPrintProvider(this);
        PosPrintProvider pppCor = new PosPrintProvider(this);
        PosPrintProvider pppCorNfe = new PosPrintProvider(this);
        PosPrintProvider pppTotal = new PosPrintProvider(this);

        String serie = bd.getSeriePOS();
        elementosUnidade = bd.getUnidades();
        unidades = elementosUnidade.get(0);

        //IMPRIMIR CABEÇALHO
        TextView txtCabRel1 = findViewById(R.id.txtCabRel1);
        TextView txtCabRel2 = findViewById(R.id.txtCabRel2);
        TextView txtCabRel3 = findViewById(R.id.txtCabRel3);
        TextView txtCabRel4 = findViewById(R.id.txtCabRel4);
        TextView txtCabRel5 = findViewById(R.id.txtCabRel5);
        //
        txtCabRel1.setText(unidades.getRazao_social());
        txtCabRel2.setText(String.format("CNPJ: %s I.E.: %s", unidades.getCnpj(), unidades.getIe()));
        txtCabRel3.setText(String.format("%s, %s%s, %s, %s", unidades.getEndereco(), unidades.getNumero(), unidades.getBairro(), unidades.getCidade(), unidades.getUf()));
        txtCabRel4.setText(String.format("CEP: %s  %s", unidades.getCep(), unidades.getTelefone()));

        //
        txtCabRel5.setText(String.format("Serie: %s  %s", serie, unidades.getTelefone()));
        //
        LinearLayout impressora = findViewById(R.id.printCabRel);
        Bitmap bitmap1 = printViewHelper.createBitmapFromView(impressora, 260, 110);

        //
        pppCab.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                impressao1 = true;
                finalizarImpressao();
            }

            @Override
            public void onError() {
                impressao1 = true;
                finalizarImpressao();
                Toast.makeText(context, "Erro ao imprimir: " + ppp.getListOfErrors(), Toast.LENGTH_SHORT).show();
            }
        });
        pppCab.addBitmap(bitmap1);
        pppCab.execute();

        // TOTAL DE PRODUTOS
        int totalProdutos = 0;
        int totalProdutosNFE = 0;

        //DADOS DAS NOTAS NFC-e
        if (elementosPedidos.size() > 0) {
            //
            TextView txtCorpoRel1 = findViewById(R.id.txtCorpoRel1);
            TextView txtCorpoRel2 = findViewById(R.id.txtCorpoRel2);
            TextView txtCorpoRel3 = findViewById(R.id.txtCorpoRel3);
            TextView txtCorpoRel4 = findViewById(R.id.txtCorpoRel4);
            TextView txtCorpoRel5 = findViewById(R.id.txtCorpoRel5);
            TextView txtCorpoRel6 = findViewById(R.id.txtCorpoRel6);

            //
            for (int n = 0; n < elementosPedidos.size(); n++) {

                //DADOS DOS PEDIDO
                pedidos = elementosPedidos.get(n);
                elementosItens = bd.getItensPedido(pedidos.getId());
                itensPedidos = elementosItens.get(0);

                String dataEmissao = cAux.exibirData(pedidos.getData());
                String horaEmissao = pedidos.getHora();

                //
                txtCorpoRel1.setText(String.format("Numero:%s      Emissao:%s %s", pedidos.getId(), dataEmissao, horaEmissao));
                txtCorpoRel2.setText(String.format("Protocolo: %s", pedidos.getProtocolo().equals(" ") ? "EMITIDA EM CONTIGENCIA" : pedidos.getProtocolo()));

                String c = bd.gerarChave(Integer.parseInt(pedidos.getId()));
                String cl1 = c.substring(0, 4) + " " + c.substring(4, 8) + " " + c.substring(8, 12) + " " + c.substring(12, 16) + " " + c.substring(16, 20);
                String cl2 = c.substring(20, 24) + " " + c.substring(24, 28) + " " + c.substring(28, 32) + " " + c.substring(32, 36);
                //
                txtCorpoRel3.setText(cl1);
                txtCorpoRel4.setText(cl2);

                //
                /*linhaProduto = new String[]{
                        itensPedidos.getProduto() + "         " + bd.getProduto(itensPedidos.getProduto()),
                        "" + itensPedidos.getQuantidade() + "     " + "UN     " + cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(itensPedidos.getValor())))) + "   " +
                                cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(pedidos.getValor_total()))))
                };*/

                //IMPRIMIR TEXTO
                txtCorpoRel5.setText(String.format("%s               %s", itensPedidos.getProduto(), bd.getProduto(itensPedidos.getProduto())));
                txtCorpoRel6.setText(String.format("%s          UN          %s          %s", itensPedidos.getQuantidade(), cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(itensPedidos.getValor())))), cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(itensPedidos.getTotal()))))));

                LinearLayout impressora1 = findViewById(R.id.printRel2);
                Bitmap bitmap2 = printViewHelper.createBitmapFromView(impressora1, 260, 120);

                pppCor.addBitmap(bitmap2);

                try {
                    String[] sum = {String.valueOf(n), "1"};
                    imprimindo.setText(String.valueOf(cAux.somar(sum)));
                } catch (Exception ignored) {

                }
                totalProdutos += Integer.parseInt(itensPedidos.getQuantidade());
            }

            pppCor.setConnectionCallback(new StoneCallbackInterface() {
                @Override
                public void onSuccess() {
                    impressao2 = true;
                    finalizarImpressao();
                }

                @Override
                public void onError() {
                    impressao2 = true;
                    finalizarImpressao();
                    Toast.makeText(context, "Erro ao imprimir: " + ppp.getListOfErrors(), Toast.LENGTH_SHORT).show();
                }
            });
            pppCor.execute();
        } else {
            impressao2 = true;
            finalizarImpressao();
        }

        // NF-e
        if (elementosPedidosNFE.size() > 0) {
            //
            LinearLayout impressoraTxtNFe = findViewById(R.id.printRelNFe);
            Bitmap bitmapTxtNFe = printViewHelper.createBitmapFromView(impressoraTxtNFe, 260, 20);

            pppTotal.addBitmap(bitmapTxtNFe);
            //
            TextView txtCorpoRelNfe1 = findViewById(R.id.txtCorpoRelNfe1);
            TextView txtCorpoRelNfe2 = findViewById(R.id.txtCorpoRelNfe2);
            TextView txtCorpoRelNfe3 = findViewById(R.id.txtCorpoRelNfe3);
            TextView txtCorpoRelNfe4 = findViewById(R.id.txtCorpoRelNfe4);
            TextView txtCorpoRelNfe5 = findViewById(R.id.txtCorpoRelNfe5);
            TextView txtCorpoRelNfe6 = findViewById(R.id.txtCorpoRelNfe6);

            //DADOS DAS NOTAS NF-e
            for (int n = 0; n < elementosPedidosNFE.size(); n++) {

                //DADOS DOS PEDIDO
                pedidosNFE = elementosPedidosNFE.get(n);
                elementosItens = bd.getItensPedidoNFE(pedidosNFE.getId());
                itensPedidos = elementosItens.get(0);

                String dataEmissao = pedidosNFE.getData();
                String horaEmissao = pedidosNFE.getHora();

                //
                txtCorpoRelNfe1.setText(String.format("Numero:%s      Emissao:%s %s", pedidosNFE.getId(), dataEmissao, horaEmissao));
                txtCorpoRelNfe2.setText(String.format("Protocolo: %s", pedidosNFE.getProtocolo()));

                String c = bd.gerarChave(Integer.parseInt(pedidosNFE.getId()));
                String cl1 = c.substring(0, 4) + " " + c.substring(4, 8) + " " + c.substring(8, 12) + " " + c.substring(12, 16) + " " + c.substring(16, 20);
                String cl2 = c.substring(20, 24) + " " + c.substring(24, 28) + " " + c.substring(28, 32) + " " + c.substring(32, 36);
                txtCorpoRelNfe3.setText(cl1);
                txtCorpoRelNfe4.setText(cl2);

                //IMPRIMIR TEXTO
                txtCorpoRelNfe5.setText(String.format("%s               %s", itensPedidos.getProduto(), bd.getProduto(itensPedidos.getProduto())));
                txtCorpoRelNfe6.setText(String.format("%s          UN          %s          %s", itensPedidos.getQuantidade(), cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(itensPedidos.getValor())))), cAux.maskMoney(new BigDecimal(String.valueOf(cAux.converterValores(pedidosNFE.getValor_total()))))));

                //
                LinearLayout impressoraNFe = findViewById(R.id.printRel3);
                Bitmap bitmapNFe = printViewHelper.createBitmapFromView(impressoraNFe, 260, 120);

                pppCorNfe.addBitmap(bitmapNFe);

                try {
                    String[] sum = {String.valueOf(n), "1"};
                    imprimindo.setText(String.valueOf(cAux.somar(sum)));
                } catch (Exception ignored) {

                }
                totalProdutosNFE += Integer.parseInt(itensPedidos.getQuantidade());
            }

            //
            pppCorNfe.setConnectionCallback(new StoneCallbackInterface() {
                @Override
                public void onSuccess() {
                    impressao3 = true;
                    finalizarImpressao();
                }

                @Override
                public void onError() {
                    impressao3 = true;
                    finalizarImpressao();
                    Toast.makeText(context, "Erro ao imprimir: " + ppp.getListOfErrors(), Toast.LENGTH_SHORT).show();
                }
            });
            pppCorNfe.execute();
        } else {
            impressao3 = true;
            finalizarImpressao();
        }

        //
        TextView txtTotNfce = findViewById(R.id.txtTotNfce);
        TextView txtTotNfe = findViewById(R.id.txtTotNfe);
        TextView txtTotal = findViewById(R.id.txtTotal);
        //
        txtTotNfce.setText(String.valueOf(totalProdutos));
        txtTotNfe.setText(String.valueOf(totalProdutosNFE));
        String[] somar = {String.valueOf(totalProdutos), String.valueOf(totalProdutosNFE)};
        txtTotal.setText(String.valueOf(Math.round(Float.parseFloat(String.valueOf(cAux.somar(somar))))));

        pppTotal.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                impressao4 = true;
                finalizarImpressao();
            }

            @Override
            public void onError() {
                impressao4 = true;
                finalizarImpressao();
                Toast.makeText(context, "Erro ao imprimir: " + ppp.getListOfErrors(), Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout impressoraTot = findViewById(R.id.printTotais);
        Bitmap bitmapTot = printViewHelper.createBitmapFromView(impressoraTot, 260, 120);

        pppTotal.addBitmap(bitmapTot);
        pppTotal.execute();
    }

    //
    private void finalizarImpressao() {
        //
        if (!impressao1 || !impressao2 || !impressao3 || !impressao4) return;

        //
        Intent i = new Intent(ImpressoraPOS.this, Principal.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("nomeImpressoraBlt", enderecoBlt);
        i.putExtra("enderecoBlt", enderecoBlt);
        startActivity(i);
        finish();
    }

    @Override
    public void onStatusChanged(Action action) {

    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError() {

    }
}
