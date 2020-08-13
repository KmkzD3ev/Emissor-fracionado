package br.com.zenitech.emissorweb;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.lvrenyang.io.Label;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class Prints {

    public static boolean PrintTicket(Context ctx, Label label, int nPrintWidth, int nPrintHeight, int nPrintCount, String[] texto) {
        //boolean bPrintResult = false;
        boolean bPrintResult;

        int w = nPrintWidth;
        int h = nPrintHeight;

        //Bitmap logo = getImageFromAssetsFile(ctx, "cabecalho_nota_nfce.png");

        //label.PageBegin(0, 0, w, h, 1);
        //label.DrawBox(0, 0, w - 1, h - 1, 1, 1);
        //label.DrawBitmap(0, 0, logo.getWidth(), logo.getHeight(), 0, logo, 0);
        try {
            //label.DrawPlainText(10, 50, 24, 0, "型号：P58A+".getBytes("GBK"));
            //label.DrawPlainText(10, 80, 24, 0, "版本：V4.0".getBytes("GBK"));

            //label.DrawQRCode(100, 100, 1, 1, 4, 0, "teste".getBytes());

            label.DrawPlainText(10, 20, 20, 0, "# CODIGO DESCRICAO QTDE. UN. VL.UNIT. VL.TOTAL".getBytes("GBK"));
            label.DrawPlainText(10, 50, 10, 0, texto[0].getBytes("GBK"));
            label.DrawPlainText(10, 80, 10, 0, texto[1].getBytes("GBK"));
            label.DrawPlainText(10, 100, 10, 0, "- - - - - - - - - - - - - - - - - - - - - - -".getBytes("GBK"));
            label.DrawPlainText(10, 130, 10, 0, "".getBytes("GBK"));
            //label.DrawPlainText(10, 80, 20, 0, " 1 1      GPL ENVASADO 13KG".getBytes("GBK"));
            //label.DrawPlainText(10, 110, 20, 0, "                    1,00  UN    50,00     50,00".getBytes("GBK"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //String cod = "http://nfce.set.rn.gov.br/portalDFE/NFCe/mDadosNFCe.aspx?chNFe=24170408248916000762650010000027871000027872&nVersao=100&tpAmb=1&dhEmi=323031372d30342d32345430363a30323a33362d30333a3030&vNF=50.00&vICMS=0.00&digVal=325347523258674351746b374850304c3531556e37595562546e303d&cIdToken=000001&cHashQRCode=99F87D8EB34F1F622E2FD44BCF734DBC80F3E05D";
        String cod = "123456";
        //label.DrawBarcode(10, 150, 8, 50, 2, 0, cod.getBytes());
        //label.DrawQRCode(200, 50, 0, 1, 4, 0, cod.getBytes());

        label.PageEnd();
        label.PagePrint(nPrintCount);

        bPrintResult = label.GetIO().IsOpened();

        return bPrintResult;
    }

    /**
     * Leia a imagem dos activos
     */
    public static Bitmap getImageFromAssetsFile(Context ctx, String fileName) {
        Bitmap image = null;
        AssetManager am = ctx.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        // load the origial Bitmap
        Bitmap BitmapOrg = bitmap;

        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        // calculate the scale
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the Bitmap
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);

        // make a Drawable from Bitmap to allow to set the Bitmap
        // to the ImageView, ImageButton or what ever
        return resizedBitmap;
    }
}
