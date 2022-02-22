package br.com.zenitech.emissorweb;

import static br.com.zenitech.emissorweb.ClassAuxiliar.getSha1Hex;
import static br.com.zenitech.emissorweb.GerenciarPagamentoCartao.getApplicationName;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

import br.com.stone.posandroid.providers.PosPrintProvider;
import br.com.zenitech.emissorweb.controller.PrintViewHelper;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.Unidades;
import stone.application.StoneStart;
import stone.application.interfaces.StoneActionCallback;
import stone.application.interfaces.StoneCallbackInterface;
import stone.user.UserModel;
import stone.utils.Stone;

public class TesteConexaoImpressora extends AppCompatActivity {
    Context context;
    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
    File myDir = new File(root + "/Emissor_Web");
    String dataHoraCan, codAutCan;
    PrintViewHelper printViewHelper;
    PosPrintProvider ppp;
    private DatabaseHelper bd;
    private ClassAuxiliar cAux;
    SharedPreferences prefs;
    ArrayList<Unidades> elementosUnidade;
    Unidades unidades;
    String[] linhaProduto;

    //DADOS PARA IMPRESSÃO
    String pedido, cliente, id_produto, produto, protocolo, chave, quantidade,
            valor, valorUnit, tributos, tributosN, tributosE, tributosM, posicao, tipoImpressao, form_pagamento;

    TextView total;
    public TextView imprimindo;
    ImageView qrcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teste_conexao_impressora);
        context = this;

        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);

        bd = new DatabaseHelper(this);
        //
        cAux = new ClassAuxiliar();

        imprimindo = findViewById(R.id.imprimindo);
        total = findViewById(R.id.total);
        qrcode = findViewById(R.id.qrcode);

        printViewHelper = new PrintViewHelper();
        ppp = new PosPrintProvider(this);
        linhaProduto = new String[]{
                "1 1      P13",
                "1     " + "UN     100,00   100,00",
                "100,00",
                "10052210",
                "060.328.534-11",
                "23,50",
                "2422 0210 5528 5000 0156 6150",
                "ZENITECH",
                "10552820000165",
                "Rua do Chmbo, 125 ",
                "Lagoa Nova",
                "59290-731",
                "DINHEIRO",
                "0,00",
                "0,00",
                "0,00",
                "1", // 16
                "P13",    // 17
                "1", // 18
                "100,00",  // 19
                "100,00"       // 20
        };
        iniciarStone();

        //findViewById(R.id.btnPareados).setOnClickListener(v -> startActivity(new Intent(getBaseContext(), SearchBTActivity.class)));
        findViewById(R.id.panel_status).setOnClickListener(v -> {
            try {
                printNFCE(linhaProduto);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

    }

    private void iniciar() {
        PosPrintProvider customPosPrintProvider = new PosPrintProvider(context);
        customPosPrintProvider.addLine("PAN : ");
        customPosPrintProvider.addLine("DATE/TIME : ");
        customPosPrintProvider.addLine("AMOUNT : ");
        customPosPrintProvider.addLine("ATK : ");
        customPosPrintProvider.addLine("Signature");
        customPosPrintProvider.addBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.logo));
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
        customPosPrintProvider.execute();
    }

    // Iniciar o Stone
    void iniciarStone() {
        // O primeiro passo é inicializar o SDK.
        StoneStart.init(context);
        /*Em seguida, é necessário chamar o método setAppName da classe Stone,
        que recebe como parâmetro uma String referente ao nome da sua aplicação.*/
        Stone.setAppName(getApplicationName(context));
        //Ambiente de Sandbox "Teste"
        //Stone.setEnvironment(new Configuracoes().Ambiente());

        // Esse método deve ser executado para inicializar o SDK
        List<UserModel> userList = StoneStart.init(context);

        /*// Quando é retornado null, o SDK ainda não foi ativado
        if (userList != null) {
            // O SDK já foi ativado.
            _pinpadAtivado();

        } else {
            // Inicia a ativação do SDK
            ativarStoneCode();
        }*/
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
        txtInfoVal1.setText("1");
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
        txtEmissao1.setText("1618");
        txtEmissao2.setText(serie);
        txtEmissao3.setText(cAux.exibirDataAtual());
        txtEmissao4.setText(cAux.horaAtual());

        // ********************** COSULTA NA RECEITA
        TextView txtConsRec1 = findViewById(R.id.txtConsRec1);
        TextView txtConsRec2 = findViewById(R.id.txtConsRec2);
        TextView txtConsRec4 = findViewById(R.id.txtConsRec4);
        //
        String c = bd.gerarChave(Integer.parseInt("1618"));
        txtConsRec1.setText(urlConsulta);
        txtConsRec2.setText(c);
        txtConsRec4.setText(texto[3]);

        //CONSUMIDOR
        TextView txtConsumidor = findViewById(R.id.txtConsumidor);
        txtConsumidor.setText(texto[4]);

        // GERAR IMAGEM DE IMPRESSÃO
        //
        LinearLayout CabecalhoNFCe = findViewById(R.id.CabecalhoNFCe);
        /*//Bitmap bitmapCabecalhoNFCe = printViewHelper.createBitmapFromView(CabecalhoNFCe, 260, 104);
        Bitmap bitmapCabecalhoNFCe = printViewHelper.createBitmapFromView(CabecalhoNFCe, 260, 104);
        //
        LinearLayout impressora = findViewById(R.id.teste);
        //Bitmap bitmap1 = printViewHelper.createBitmapFromView(impressora, 260, 454);
        Bitmap bitmap1 = printViewHelper.createBitmapFromView(impressora, 260, 330);//45
        //
        LinearLayout impressoraChave = findViewById(R.id.teste3);
        //Bitmap bitmap1 = printViewHelper.createBitmapFromView(impressora, 260, 454);
        Bitmap bitmap3 = printViewHelper.createBitmapFromView(impressoraChave, 260, 200);//45*/

        //Bitmap bitmapCabecalhoNFCe = printViewHelper.createBitmapFromView(CabecalhoNFCe, 260, 104);
        Bitmap bitmapCabecalhoNFCe = printViewHelper.createBitmapFromView(CabecalhoNFCe, 190, 118);
        //
        LinearLayout impressora = findViewById(R.id.teste);
        //Bitmap bitmap1 = printViewHelper.createBitmapFromView(impressora, 260, 454);
        Bitmap bitmap1 = printViewHelper.createBitmapFromView(impressora, 190, 240);//45
        //
        LinearLayout impressoraChave = findViewById(R.id.teste3);
        //Bitmap bitmap1 = printViewHelper.createBitmapFromView(impressora, 260, 454);
        Bitmap bitmap3 = printViewHelper.createBitmapFromView(impressoraChave, 190, 130);//45
        //
        LinearLayout impressora1 = findViewById(R.id.teste1);
        Bitmap bitmap2 = printViewHelper.createBitmapFromView(impressora1, 190, 120);

        ppp.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Recibo impresso", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError() {
                Toast.makeText(context, "Erro ao imprimir: " + ppp.getListOfErrors(), Toast.LENGTH_SHORT).show();
            }
        });

        ppp.addBitmap(bitmapCabecalhoNFCe);
        ppp.addBitmap(bitmap1);
        ppp.addBitmap(bitmap3);
        ppp.addBitmap(bitmap2);
        ppp.execute();

        // Apaga a imgem anterior
        File imgQrC = new File(sdcard, "Emissor_Web/qrcode.png");
        imgQrC.delete();
    }

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
}
