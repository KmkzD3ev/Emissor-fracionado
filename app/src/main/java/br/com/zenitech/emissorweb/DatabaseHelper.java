package br.com.zenitech.emissorweb;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;

import br.com.zenitech.emissorweb.domains.Autorizacoes;
import br.com.zenitech.emissorweb.domains.AutorizacoesPinpad;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PedidosNFE;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.Produtos;
import br.com.zenitech.emissorweb.domains.StatusPedidos;
import br.com.zenitech.emissorweb.domains.StatusPedidosNFE;
import br.com.zenitech.emissorweb.domains.Unidades;


public class DatabaseHelper extends SQLiteOpenHelper {

    private String TAG = "DatabaseHelper";
    private String DB_PATH;
    private static String DB_NAME = "emissorwebDB";
    private SQLiteDatabase myDataBase;
    final Context context;


    @SuppressLint("SdCardPath")
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 11);
        this.context = context;
        //this.DB_PATH = context.getFilesDir().getPath() + "/" + context.getPackageName() + "/" + "databases/";
        //this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            //this.DB_PATH = context.getDatabasePath(DB_NAME).getPath() + File.separator;
            this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";

        } else {
            //String DB_PATH = Environment.getDataDirectory() + "/data/my.trial.app/databases/";
            //myPath = DB_PATH + dbName;
            this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
        }

        //this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
        Log.e(TAG, " DatabaseHelper - " + DB_PATH);
    }


    void createDataBase() {
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDataBase();
            } catch (IOException e) {
                Log.i(TAG, "Error copying database: " + e.getMessage());
                throw new Error("Error copying database");
            }
        }
    }

    boolean checkDataBase() {
        /*
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.i(TAG, Objects.requireNonNull(e.getMessage()));
        }
        if (checkDB != null) {
            checkDB.close();
        }
        //checkDB != null ? true : false;
        return checkDB != null ? true : false;
        */
        File dbFile = context.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File arquivo = new File(path + "/emissorwebDB.db"); //.db pasta);
        FileInputStream myInput = new FileInputStream(arquivo);


        //InputStream myInput = myContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    // checar estado do banco
    public boolean isOpen() {
        return myDataBase != null && myDataBase.isOpen();
    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();

            }
    }

    //########## PRODUTOS ############

    //CONSTANTES
    private static final String TABELA_PRODUTOS = "produtos";
    private static final String CODIGO_PRODUTO = "codigo";
    private static final String NOME_PRODUTO = "nome";
    private static final String TRIBUTOS_PRODUTO = "tributos";
    private static final String VALOR_MINIMO_PRODUTO = "valor_minimo";
    private static final String VALOR_MAXIMO_PRODUTO = "valor_maximo";
    private static final String QTD_REVENDA = "qtd_revenda";

    //COLUNAS
    private static final String[] COLUNAS_PRODUTOS = {CODIGO_PRODUTO, NOME_PRODUTO, TRIBUTOS_PRODUTO, VALOR_MINIMO_PRODUTO, VALOR_MAXIMO_PRODUTO};


    //CURSOR PRODUTOS
    private Produtos cursorToProdutos(Cursor cursor) {
        Produtos produtos = new Produtos(null, null, null, null, null, null);
        produtos.setCodigo(cursor.getString(0));
        produtos.setNome(cursor.getString(1));
        produtos.setTributos(cursor.getString(2));
        produtos.setValor_minimo(cursor.getString(3));
        produtos.setValor_maximo(cursor.getString(4));
        return produtos;
    }

    //LISTAR TODOS OS PRODUTOS
    public ArrayList<Produtos> getAllProdutos() {
        ArrayList<Produtos> listaProdutos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PRODUTOS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                //Produtos prods = cursorToProdutos(cursor);
                //listaProdutos.add(prods);

                String codigo = cursor.getString(0);
                String nome = cursor.getString(1);
                String tributos = cursor.getString(2);
                String valor_minimo = cursor.getString(3);
                String valor_maximo = cursor.getString(4);
                String qtd_revenda = cursor.getString(5);
                Produtos prod = new Produtos(codigo, nome, tributos, valor_minimo, valor_maximo, qtd_revenda);
                listaProdutos.add(prod);
            } while (cursor.moveToNext());
        }

        return listaProdutos;
    }

    ArrayList<String> getProdutos() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String selectQuery = "Select * From " + TABELA_PRODUTOS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        list.add("PRODUTO");
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String codigo = cursor.getString(cursor.getColumnIndex("codigo"));
                    String nome = cursor.getString(cursor.getColumnIndex("nome"));
                    list.add(nome);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return list;
    }

    String getProduto(String id) {
        String produto = null;
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String selectQuery = "Select " + NOME_PRODUTO + " From " + TABELA_PRODUTOS + " where " + CODIGO_PRODUTO + " = '" + id + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    produto = cursor.getString(cursor.getColumnIndex(NOME_PRODUTO));
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return produto;
    }

    //
    String getIdProduto(String produto) {
        SQLiteDatabase db = this.getWritableDatabase();
        String id = null;

        //
        Cursor produtos;
        String query_pos = "SELECT " + CODIGO_PRODUTO + " FROM " + TABELA_PRODUTOS + " WHERE " + NOME_PRODUTO + " = '" + produto + "' LIMIT 1";
        produtos = db.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {

                id = produtos.getString(produtos.getColumnIndex(CODIGO_PRODUTO));

            } while (produtos.moveToNext());
        }

        return id;
    }

    //
    public String getNomeProduto(String idProduto) {
        SQLiteDatabase db = this.getWritableDatabase();
        String id = null;

        //
        Cursor produtos;
        String query_pos = "SELECT " + NOME_PRODUTO + " FROM " + TABELA_PRODUTOS + " WHERE " + CODIGO_PRODUTO + " = '" + idProduto + "' LIMIT 1";
        produtos = db.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {

                id = produtos.getString(produtos.getColumnIndex(NOME_PRODUTO));

            } while (produtos.moveToNext());
        }

        return id;
    }

    //
    double getTributosProduto(String Produto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ClassAuxiliar aux = new ClassAuxiliar();
        String tributo = "";

        //
        Cursor produtos;
        String query_pos = "SELECT " + TRIBUTOS_PRODUTO + " FROM " + TABELA_PRODUTOS + " WHERE " + NOME_PRODUTO + " = '" + Produto + "' LIMIT 1";
        produtos = db.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {

                tributo = produtos.getString(produtos.getColumnIndex(TRIBUTOS_PRODUTO));

            } while (produtos.moveToNext());
        }

        String t = String.valueOf(aux.converterValores(tributo));

        return Double.parseDouble(t);
    }

    //
    double getPrecoMinimoProduto(String Produto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ClassAuxiliar aux = new ClassAuxiliar();
        String tributo = "";

        //
        Cursor produtos;
        String query_pos = "SELECT " + VALOR_MINIMO_PRODUTO + " FROM " + TABELA_PRODUTOS + " WHERE " + NOME_PRODUTO + " = '" + Produto + "' LIMIT 1";
        produtos = db.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {

                tributo = produtos.getString(produtos.getColumnIndex(VALOR_MINIMO_PRODUTO));

            } while (produtos.moveToNext());
        }

        //Log.i("TOTAL= ", tributo);

        //String t = String.valueOf(aux.converterValores(tributo));
        //String t = tributo;

        return Double.parseDouble(tributo);
    }

    //
    double getPrecoMaximoProduto(String Produto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ClassAuxiliar aux = new ClassAuxiliar();
        String tributo = "";

        //
        Cursor produtos;
        String query_pos = "SELECT " + VALOR_MAXIMO_PRODUTO + " FROM " + TABELA_PRODUTOS + " WHERE " + NOME_PRODUTO + " = '" + Produto + "' LIMIT 1";
        produtos = db.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {

                tributo = produtos.getString(produtos.getColumnIndex(VALOR_MAXIMO_PRODUTO));

            } while (produtos.moveToNext());
        }
        db.close();

        return Double.parseDouble(tributo);
    }

    //
    int getQuantProdutoRemessa(String Produto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ClassAuxiliar aux = new ClassAuxiliar();
        String quatProd = "0", quantInt = "0", quantIntNFE = "0";

        try {
            //
            Cursor produtos;
            String query_pos = "SELECT " + QTD_REVENDA + " FROM " + TABELA_PRODUTOS + " WHERE " + NOME_PRODUTO + " = '" + Produto + "' LIMIT 1";
            produtos = db.rawQuery(query_pos, null);
            if (produtos.moveToFirst()) {
                do {
                    quatProd = produtos.getString(produtos.getColumnIndex(QTD_REVENDA));
                } while (produtos.moveToNext());
            }
        } catch (Exception ignored) {
        }

        try {
            //
            Cursor intProdutos;
            String query_intPro = "SELECT SUM(quantidade) quantidade FROM itens_pedidos WHERE produto = " + getIdProduto(Produto);
            Log.i("SQL_BD", query_intPro);
            intProdutos = db.rawQuery(query_intPro, null);
            if (intProdutos.moveToFirst()) {
                do {
                    quantInt = intProdutos.getString(intProdutos.getColumnIndex("quantidade"));
                } while (intProdutos.moveToNext());
            }
        } catch (Exception ignored) {
        }

        try {
            //
            Cursor intProdutosNFE;
            String query_intProNFE = "SELECT SUM(quantidade) quantidade FROM itens_pedidosNFE WHERE produto = " + getIdProduto(Produto);
            Log.i("SQL_BD", query_intProNFE);
            intProdutosNFE = db.rawQuery(query_intProNFE, null);
            if (intProdutosNFE.moveToFirst()) {
                do {
                    quantIntNFE = intProdutosNFE.getString(intProdutosNFE.getColumnIndex("quantidade"));
                } while (intProdutosNFE.moveToNext());
            }
        } catch (Exception ignored) {
        }

        String[] qs = {(quantInt != null ? quantInt : "0"), (quantIntNFE != null ? quantIntNFE : "0")};
        String q = String.valueOf(aux.somar(qs));

        Log.e("TOTAL Q", " ----- " + q);

        String[] t = {quatProd, q};
        float p = Float.parseFloat(String.valueOf(aux.subitrair(t)));

        Log.e("TOTAL P", " ----- " + p);
        return Math.round(p);
    }

    //CURSOR PEDIDOS
    private ItensPedidos cursorToItensPedidos(Cursor cursor) {
        ItensPedidos itensPedidos = new ItensPedidos(null, null, null, null);
        //
        itensPedidos.setPedido(cursor.getString(0));
        itensPedidos.setProduto(cursor.getString(1));
        itensPedidos.setQuantidade(cursor.getString(2));
        itensPedidos.setValor(cursor.getString(3));
        return itensPedidos;
    }

    //
    ArrayList<ItensPedidos> getItensPedidoNFE(String nPedido) {
        String id = null;

        ArrayList<ItensPedidos> listaItensPedidos = new ArrayList<>();

        String query = "SELECT * FROM itens_pedidosNFE WHERE pedido = '" + nPedido + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                ItensPedidos itensPedidos = cursorToItensPedidos(cursor);
                listaItensPedidos.add(itensPedidos);
            } while (cursor.moveToNext());
        }

        return listaItensPedidos;
    }

    //
    ArrayList<ItensPedidos> getItensPedido(String nPedido) {
        String id = null;

        ArrayList<ItensPedidos> listaItensPedidos = new ArrayList<>();

        String query = "SELECT * FROM itens_pedidos WHERE pedido = '" + nPedido + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                ItensPedidos itensPedidos = cursorToItensPedidos(cursor);
                listaItensPedidos.add(itensPedidos);
            } while (cursor.moveToNext());
        }

        return listaItensPedidos;
    }

    //########## AUTORIZAÇÕES ############
    //CONSTANTES
    private static final String TABELA_AUTORIZACOES = "autorizacoes";
    private static final String ID_AUTORIZACOES = "id";
    private static final String ID_PEDIDO_AUTORIZACOES = "pedido";
    private static final String RESPAG_AUTORIZACOES = "RESPAG";
    private static final String BINCARTAO_AUTORIZACOES = "BINCARTAO";
    private static final String NOMEINST_AUTORIZACOES = "NOMEINST";
    private static final String NSUAUT_AUTORIZACOES = "NSUAUT";
    private static final String CAUT_AUTORIZACOES = "CAUT";
    private static final String NPARCEL_AUTORIZACOES = "NPARCEL";
    private static final String RADQ_AUTORIZACOES = "RADQ";
    private static final String TCAR_AUTORIZACOES = "TCAR";
    private static final String TIPOTRANS_AUTORIZACOES = "TIPOTRANS";
    private static final String RECLOJA_AUTORIZACOES = "RECLOJA";
    private static final String RECCLI_AUTORIZACOES = "RECCLI";
    private static final String NINST_AUTORIZACOES = "NINST";
    private static final String CARTAO_AUTORIZACOES = "CARTAO";
    private static final String CODAUTORIZACAO_AUTORIZACOES = "CODAUTORIZACAO";

    // COLUNAS_AUTORIZACOES
    private static final String[] COLUNAS_AUTORIZACOES = {
            ID_AUTORIZACOES,
            ID_PEDIDO_AUTORIZACOES,
            RESPAG_AUTORIZACOES,
            BINCARTAO_AUTORIZACOES,
            NOMEINST_AUTORIZACOES,
            NSUAUT_AUTORIZACOES,
            CAUT_AUTORIZACOES,
            NPARCEL_AUTORIZACOES,
            RADQ_AUTORIZACOES,
            TCAR_AUTORIZACOES,
            TIPOTRANS_AUTORIZACOES,
            RECLOJA_AUTORIZACOES,
            RECCLI_AUTORIZACOES,
            NINST_AUTORIZACOES,
            CARTAO_AUTORIZACOES,
            CODAUTORIZACAO_AUTORIZACOES
    };

    public void addAutorizacoes(Autorizacoes autorizacoes) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ID_PEDIDO_AUTORIZACOES, autorizacoes.getPedido());
        values.put(RESPAG_AUTORIZACOES, autorizacoes.getRESPAG());
        values.put(BINCARTAO_AUTORIZACOES, autorizacoes.getBINCARTAO());
        values.put(NOMEINST_AUTORIZACOES, autorizacoes.getNOMEINST());
        values.put(NSUAUT_AUTORIZACOES, autorizacoes.getNSUAUT());
        values.put(CAUT_AUTORIZACOES, autorizacoes.getCAUT());
        values.put(NPARCEL_AUTORIZACOES, autorizacoes.getNPARCEL());
        values.put(RADQ_AUTORIZACOES, autorizacoes.getRADQ());
        values.put(TCAR_AUTORIZACOES, autorizacoes.getTCAR());
        values.put(TIPOTRANS_AUTORIZACOES, autorizacoes.getTIPOTRANS());
        values.put(RECLOJA_AUTORIZACOES, autorizacoes.getRECLOJA());
        values.put(RECCLI_AUTORIZACOES, autorizacoes.getRECCLI());
        values.put(NINST_AUTORIZACOES, autorizacoes.getNINST());
        values.put(CARTAO_AUTORIZACOES, autorizacoes.getCARTAO());
        values.put(CODAUTORIZACAO_AUTORIZACOES, autorizacoes.getCODAUTORIZACAO());
        db.insert(TABELA_AUTORIZACOES, null, values);
        db.close();
    }

    void upadteAutorizacoes(String pedido) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ID_PEDIDO_AUTORIZACOES, pedido);
        String id = ultimaAutorizacao();
        db.update(TABELA_AUTORIZACOES, values, "id=" + id, null);
        db.close();
    }

    //ULTIMA NFC-E EMITIDA
    public String ultimaAutorizacao() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String id = "0";

        try {

            String query = "SELECT aut.id FROM " + TABELA_AUTORIZACOES + " aut " +
                    " ORDER BY aut.id DESC";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.endTransaction();
        db.close();

        return id;
    }

    //########## AUTORIZAÇÕES PINPAD ############
    //CONSTANTES
    private static final String TABELA_AUTORIZACOES_PINPAD = "autorizacoes_pinpad";
    private static final String id_AUTORIZACOES_PINPAD = "id";
    private static final String pedido_AUTORIZACOES_PINPAD = "pedido";
    private static final String idFromBase_AUTORIZACOES_PINPAD = "idFromBase";
    private static final String amount_AUTORIZACOES_PINPAD = "amount";
    private static final String requestId_AUTORIZACOES_PINPAD = "requestId";
    private static final String emailSent_AUTORIZACOES_PINPAD = "emailSent";
    private static final String timeToPassTransaction_AUTORIZACOES_PINPAD = "timeToPassTransaction";
    private static final String initiatorTransactionKey_AUTORIZACOES_PINPAD = "initiatorTransactionKey";
    private static final String recipientTransactionIdentification_AUTORIZACOES_PINPAD = "recipientTransactionIdentification";
    private static final String cardHolderNumber_AUTORIZACOES_PINPAD = "cardHolderNumber";
    private static final String cardHolderName_AUTORIZACOES_PINPAD = "cardHolderName";
    private static final String date_AUTORIZACOES_PINPAD = "date";
    private static final String time_AUTORIZACOES_PINPAD = "time";
    private static final String aid_AUTORIZACOES_PINPAD = "aid";
    private static final String arcq_AUTORIZACOES_PINPAD = "arcq";
    private static final String authorizationCode_AUTORIZACOES_PINPAD = "authorizationCode";
    private static final String iccRelatedData_AUTORIZACOES_PINPAD = "iccRelatedData";
    private static final String transactionReference_AUTORIZACOES_PINPAD = "transactionReference";
    private static final String actionCode_AUTORIZACOES_PINPAD = "actionCode";
    private static final String commandActionCode_AUTORIZACOES_PINPAD = "commandActionCode";
    private static final String pinpadUsed_AUTORIZACOES_PINPAD = "pinpadUsed";
    private static final String saleAffiliationKey_AUTORIZACOES_PINPAD = "saleAffiliationKey";
    private static final String cne_AUTORIZACOES_PINPAD = "cne";
    private static final String cvm_AUTORIZACOES_PINPAD = "cvm";
    private static final String balance_AUTORIZACOES_PINPAD = "balance";
    private static final String serviceCode_AUTORIZACOES_PINPAD = "serviceCode";
    private static final String subMerchantCategoryCode_AUTORIZACOES_PINPAD = "subMerchantCategoryCode";
    private static final String entryMode_AUTORIZACOES_PINPAD = "entryMode";
    private static final String cardBrand_AUTORIZACOES_PINPAD = "cardBrand";
    private static final String instalmentTransaction_AUTORIZACOES_PINPAD = "instalmentTransaction";
    private static final String transactionStatus_AUTORIZACOES_PINPAD = "transactionStatus";
    private static final String instalmentType_AUTORIZACOES_PINPAD = "instalmentType";
    private static final String typeOfTransactionEnum_AUTORIZACOES_PINPAD = "typeOfTransactionEnum";
    private static final String signature_AUTORIZACOES_PINPAD = "signature";
    private static final String cancellationDate_AUTORIZACOES_PINPAD = "cancellationDate";
    private static final String capture_AUTORIZACOES_PINPAD = "capture";
    private static final String shortName_AUTORIZACOES_PINPAD = "shortName";
    private static final String subMerchantAddress_AUTORIZACOES_PINPAD = "subMerchantAddress";
    private static final String userModel_AUTORIZACOES_PINPAD = "userModel";
    private static final String isFallbackTransaction_AUTORIZACOES_PINPAD = "isFallbackTransaction";
    private static final String appLabel_AUTORIZACOES_PINPAD = "appLabel";
    private static final String nomeEmpresa_AUTORIZACOES_PINPAD = "nomeEmpresa";
    private static final String enderecoEmpresa_AUTORIZACOES_PINPAD = "enderecoEmpresa";
    private static final String cnpjEmpresa_AUTORIZACOES_PINPAD = "cnpjEmpresa";

    // COLUNAS_AUTORIZACOES
    private static final String[] COLUNAS_AUTORIZACOES_PINPAD = {
            id_AUTORIZACOES_PINPAD,
            pedido_AUTORIZACOES_PINPAD,
            idFromBase_AUTORIZACOES_PINPAD,
            amount_AUTORIZACOES_PINPAD,
            requestId_AUTORIZACOES_PINPAD,
            emailSent_AUTORIZACOES_PINPAD,
            timeToPassTransaction_AUTORIZACOES_PINPAD,
            initiatorTransactionKey_AUTORIZACOES_PINPAD,
            recipientTransactionIdentification_AUTORIZACOES_PINPAD,
            cardHolderNumber_AUTORIZACOES_PINPAD,
            cardHolderName_AUTORIZACOES_PINPAD,
            date_AUTORIZACOES_PINPAD,
            time_AUTORIZACOES_PINPAD,
            aid_AUTORIZACOES_PINPAD,
            arcq_AUTORIZACOES_PINPAD,
            authorizationCode_AUTORIZACOES_PINPAD,
            iccRelatedData_AUTORIZACOES_PINPAD,
            transactionReference_AUTORIZACOES_PINPAD,
            actionCode_AUTORIZACOES_PINPAD,
            commandActionCode_AUTORIZACOES_PINPAD,
            pinpadUsed_AUTORIZACOES_PINPAD,
            saleAffiliationKey_AUTORIZACOES_PINPAD,
            cne_AUTORIZACOES_PINPAD,
            cvm_AUTORIZACOES_PINPAD,
            balance_AUTORIZACOES_PINPAD,
            serviceCode_AUTORIZACOES_PINPAD,
            subMerchantCategoryCode_AUTORIZACOES_PINPAD,
            entryMode_AUTORIZACOES_PINPAD,
            cardBrand_AUTORIZACOES_PINPAD,
            instalmentTransaction_AUTORIZACOES_PINPAD,
            transactionStatus_AUTORIZACOES_PINPAD,
            instalmentType_AUTORIZACOES_PINPAD,
            typeOfTransactionEnum_AUTORIZACOES_PINPAD,
            signature_AUTORIZACOES_PINPAD,
            cancellationDate_AUTORIZACOES_PINPAD,
            capture_AUTORIZACOES_PINPAD,
            shortName_AUTORIZACOES_PINPAD,
            subMerchantAddress_AUTORIZACOES_PINPAD,
            userModel_AUTORIZACOES_PINPAD,
            isFallbackTransaction_AUTORIZACOES_PINPAD,
            appLabel_AUTORIZACOES_PINPAD,
            nomeEmpresa_AUTORIZACOES_PINPAD,
            enderecoEmpresa_AUTORIZACOES_PINPAD,
            cnpjEmpresa_AUTORIZACOES_PINPAD
    };

    //CURSOR PEDIDOS
    private AutorizacoesPinpad cursorToAutorizacoesPinpad(Cursor cursor) {
        AutorizacoesPinpad aP = new AutorizacoesPinpad(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        //
        aP.setId(cursor.getString(0));
        aP.setPedido(cursor.getString(1));
        aP.setIdFromBase(cursor.getString(2));
        aP.setAmount(cursor.getString(3));
        aP.setRequestId(cursor.getString(4));
        aP.setEmailSent(cursor.getString(5));
        aP.setTimeToPassTransaction(cursor.getString(6));
        aP.setInitiatorTransactionKey(cursor.getString(7));
        aP.setRecipientTransactionIdentification(cursor.getString(8));
        aP.setCardHolderNumber(cursor.getString(9));
        aP.setCardHolderName(cursor.getString(10));
        aP.setDate(cursor.getString(11));
        aP.setTime(cursor.getString(12));
        aP.setAid(cursor.getString(13));
        aP.setArcq(cursor.getString(14));
        aP.setAuthorizationCode(cursor.getString(15));
        aP.setIccRelatedData(cursor.getString(16));
        aP.setTransactionReference(cursor.getString(17));
        aP.setActionCode(cursor.getString(18));
        aP.setCommandActionCode(cursor.getString(19));
        aP.setPinpadUsed(cursor.getString(20));
        aP.setSaleAffiliationKey(cursor.getString(21));
        aP.setCne(cursor.getString(22));
        aP.setCvm(cursor.getString(23));
        aP.setBalance(cursor.getString(24));
        aP.setServiceCode(cursor.getString(25));
        aP.setSubMerchantCategoryCode(cursor.getString(26));
        aP.setEntryMode(cursor.getString(27));
        aP.setCardBrand(cursor.getString(28));
        aP.setInstalmentTransaction(cursor.getString(29));
        aP.setTransactionStatus(cursor.getString(30));
        aP.setInstalmentType(cursor.getString(31));
        aP.setTypeOfTransactionEnum(cursor.getString(32));
        aP.setSignature(cursor.getString(33));
        aP.setCancellationDate(cursor.getString(34));
        aP.setCapture(cursor.getString(35));
        aP.setShortName(cursor.getString(36));
        aP.setSubMerchantAddress(cursor.getString(37));
        aP.setUserModel(cursor.getString(38));
        aP.setIsFallbackTransaction(cursor.getString(39));
        aP.setAppLabel(cursor.getString(40));
        aP.setNomeEmpresa(cursor.getString(41));
        aP.setEnderecoEmpresa(cursor.getString(42));
        aP.setCnpjEmpresa(cursor.getString(43));
        return aP;
    }

    public void addAutorizacoesPinPad(AutorizacoesPinpad autorizacoesPinpad) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(pedido_AUTORIZACOES_PINPAD, autorizacoesPinpad.getPedido());
        values.put(idFromBase_AUTORIZACOES_PINPAD, autorizacoesPinpad.getIdFromBase());
        values.put(amount_AUTORIZACOES_PINPAD, autorizacoesPinpad.getAmount());
        values.put(requestId_AUTORIZACOES_PINPAD, autorizacoesPinpad.getRequestId());
        values.put(emailSent_AUTORIZACOES_PINPAD, autorizacoesPinpad.getEmailSent());
        values.put(timeToPassTransaction_AUTORIZACOES_PINPAD, autorizacoesPinpad.getTimeToPassTransaction());
        values.put(initiatorTransactionKey_AUTORIZACOES_PINPAD, autorizacoesPinpad.getInitiatorTransactionKey());
        values.put(recipientTransactionIdentification_AUTORIZACOES_PINPAD, autorizacoesPinpad.getRecipientTransactionIdentification());
        values.put(cardHolderNumber_AUTORIZACOES_PINPAD, autorizacoesPinpad.getCardHolderNumber());
        values.put(cardHolderName_AUTORIZACOES_PINPAD, autorizacoesPinpad.getCardHolderName());
        values.put(date_AUTORIZACOES_PINPAD, autorizacoesPinpad.getDate());
        values.put(time_AUTORIZACOES_PINPAD, autorizacoesPinpad.getTime());
        values.put(aid_AUTORIZACOES_PINPAD, autorizacoesPinpad.getAid());
        values.put(arcq_AUTORIZACOES_PINPAD, autorizacoesPinpad.getArcq());
        values.put(authorizationCode_AUTORIZACOES_PINPAD, autorizacoesPinpad.getAuthorizationCode());
        values.put(iccRelatedData_AUTORIZACOES_PINPAD, autorizacoesPinpad.getIccRelatedData());
        values.put(transactionReference_AUTORIZACOES_PINPAD, autorizacoesPinpad.getTransactionReference());
        values.put(actionCode_AUTORIZACOES_PINPAD, autorizacoesPinpad.getActionCode());
        values.put(commandActionCode_AUTORIZACOES_PINPAD, autorizacoesPinpad.getCommandActionCode());
        values.put(pinpadUsed_AUTORIZACOES_PINPAD, autorizacoesPinpad.getPinpadUsed());
        values.put(saleAffiliationKey_AUTORIZACOES_PINPAD, autorizacoesPinpad.getSaleAffiliationKey());
        values.put(cne_AUTORIZACOES_PINPAD, autorizacoesPinpad.getCne());
        values.put(cvm_AUTORIZACOES_PINPAD, autorizacoesPinpad.getCvm());
        values.put(balance_AUTORIZACOES_PINPAD, autorizacoesPinpad.getBalance());
        values.put(serviceCode_AUTORIZACOES_PINPAD, autorizacoesPinpad.getServiceCode());
        values.put(subMerchantCategoryCode_AUTORIZACOES_PINPAD, autorizacoesPinpad.getSubMerchantCategoryCode());
        values.put(entryMode_AUTORIZACOES_PINPAD, autorizacoesPinpad.getEntryMode());
        values.put(cardBrand_AUTORIZACOES_PINPAD, autorizacoesPinpad.getCardBrand());
        values.put(instalmentTransaction_AUTORIZACOES_PINPAD, autorizacoesPinpad.getInstalmentTransaction());
        values.put(transactionStatus_AUTORIZACOES_PINPAD, autorizacoesPinpad.getTransactionStatus());
        values.put(instalmentType_AUTORIZACOES_PINPAD, autorizacoesPinpad.getInstalmentType());
        values.put(typeOfTransactionEnum_AUTORIZACOES_PINPAD, autorizacoesPinpad.getTypeOfTransactionEnum());
        values.put(signature_AUTORIZACOES_PINPAD, autorizacoesPinpad.getSignature());
        values.put(cancellationDate_AUTORIZACOES_PINPAD, autorizacoesPinpad.getCancellationDate());
        values.put(capture_AUTORIZACOES_PINPAD, autorizacoesPinpad.getCapture());
        values.put(shortName_AUTORIZACOES_PINPAD, autorizacoesPinpad.getShortName());
        values.put(subMerchantAddress_AUTORIZACOES_PINPAD, autorizacoesPinpad.getSubMerchantAddress());
        values.put(userModel_AUTORIZACOES_PINPAD, autorizacoesPinpad.getUserModel());
        values.put(isFallbackTransaction_AUTORIZACOES_PINPAD, autorizacoesPinpad.getIsFallbackTransaction());
        values.put(appLabel_AUTORIZACOES_PINPAD, autorizacoesPinpad.getAppLabel());
        values.put(nomeEmpresa_AUTORIZACOES_PINPAD, autorizacoesPinpad.getNomeEmpresa());
        values.put(enderecoEmpresa_AUTORIZACOES_PINPAD, autorizacoesPinpad.getEnderecoEmpresa());
        values.put(cnpjEmpresa_AUTORIZACOES_PINPAD, autorizacoesPinpad.getCnpjEmpresa());

        Log.i("Stone BD", values.toString());
        db.insert(TABELA_AUTORIZACOES_PINPAD, null, values);
        db.close();
    }

    void upadteAutorizacoesPinpad(String pedido) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(pedido_AUTORIZACOES_PINPAD, pedido);
        String id = ultimaAutorizacaoPinpad();
        db.update(TABELA_AUTORIZACOES_PINPAD, values, "id=" + id, null);
        db.close();
    }

    //ULTIMA NFC-E EMITIDA
    public String ultimaAutorizacaoPinpad() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String id = "0";

        try {

            String query = "SELECT aut.id FROM " + TABELA_AUTORIZACOES_PINPAD + " aut " +
                    " ORDER BY aut.id DESC LIMIT 1";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.endTransaction();
        db.close();

        return id;
    }

    //ULTIMA NFC-E EMITIDA
    public AutorizacoesPinpad getAutorizacaoPinpad() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        AutorizacoesPinpad autorizacoesPinpad = new AutorizacoesPinpad(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        try {

            String query = "SELECT * FROM " + TABELA_AUTORIZACOES_PINPAD + " aut " +
                    " ORDER BY aut.id DESC LIMIT 1";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                autorizacoesPinpad = cursorToAutorizacoesPinpad(cursor);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.endTransaction();
        db.close();

        return autorizacoesPinpad;
    }

    //########## PEDIDOS ############
    //CONSTANTES
    private static final String TABELA_PEDIDOS = "Pedidos";
    private static final String ID_PEDIDOS = "id";
    private static final String SITUACAO_PEDIDOS = "situacao";
    private static final String PROTOCOLO_PEDIDOS = "protocolo";
    private static final String DATA_PEDIDOS = "data";
    private static final String HORA_PEDIDOS = "hora";
    private static final String VALOR_TOTAL_PEDIDOS = "valor_total";
    private static final String DATA_PROTOCOLO_PEDIDOS = "data_protocolo";
    private static final String HORA_PROTOCOLO_PEDIDOS = "hora_protocolo";
    private static final String CPF_CLIENTE_PEDIDOS = "cpf_cliente";
    private static final String FORMA_PAGAMENTO_PEDIDOS = "forma_pagamento";

    //COLUNAS
    private static final String[] COLUNAS_PEDIDOS = {
            ID_PEDIDOS,
            SITUACAO_PEDIDOS,
            PROTOCOLO_PEDIDOS,
            DATA_PEDIDOS,
            HORA_PEDIDOS,
            VALOR_TOTAL_PEDIDOS,
            DATA_PROTOCOLO_PEDIDOS,
            HORA_PROTOCOLO_PEDIDOS,
            CPF_CLIENTE_PEDIDOS,
            FORMA_PAGAMENTO_PEDIDOS
    };

    void addPedidos(Pedidos pedidos) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SITUACAO_PEDIDOS, pedidos.getSituacao());
        values.put(PROTOCOLO_PEDIDOS, pedidos.getProtocolo());
        values.put(DATA_PEDIDOS, pedidos.getData());
        values.put(HORA_PEDIDOS, pedidos.getHora());
        values.put(VALOR_TOTAL_PEDIDOS, pedidos.getValor_total());
        values.put(DATA_PROTOCOLO_PEDIDOS, pedidos.getData_protocolo());
        values.put(HORA_PROTOCOLO_PEDIDOS, pedidos.getHora_protocolo());
        values.put(CPF_CLIENTE_PEDIDOS, pedidos.getCpf_cliente());
        values.put(FORMA_PAGAMENTO_PEDIDOS, pedidos.getForma_pagamento());
        db.insert(TABELA_PEDIDOS, null, values);
        db.close();
    }

    void addItensPedidos(ItensPedidos itensPedidos) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("pedido", itensPedidos.getPedido());
        values.put("produto", itensPedidos.getProduto());
        values.put("quantidade", itensPedidos.getQuantidade());
        values.put("valor", itensPedidos.getValor());
        db.insert("itens_pedidos", null, values);
        db.close();
    }


    void upadtePedidosTransmissao(
            String situacao,
            String protocolo,
            String data_protocolo,
            String hora_protocolo,
            String id) {


        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("situacao", situacao);
        values.put("protocolo", protocolo);
        values.put("data_protocolo", data_protocolo);
        values.put("hora_protocolo", hora_protocolo);
        db.update("pedidos", values, "id=" + id, null);
        db.close();
    }

    //########## PEDIDOS NFE ############
    //CONSTANTES
    private static final String TABELA_PEDIDOS_NFE = "pedidosNFE";
    private static final String ID_PEDIDOS_NFE = "id";
    private static final String SITUACAO_PEDIDOS_NFE = "situacao";
    private static final String PROTOCOLO_NFE = "protocolo";
    private static final String DATA_PEDIDOS_NFE = "data";
    private static final String HORA_PEDIDOS_NFE = "hora";
    private static final String VALOR_TOTAL_PEDIDOS_NFE = "valor_total";
    private static final String CLIENTE_PEDIDOS_NFE = "cliente";

    void addPedidosNFE(PedidosNFE pedidos) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ID_PEDIDOS_NFE, pedidos.getId());
        values.put(SITUACAO_PEDIDOS_NFE, pedidos.getSituacao());
        values.put(PROTOCOLO_NFE, pedidos.getProtocolo());
        values.put(DATA_PEDIDOS_NFE, pedidos.getData());
        values.put(HORA_PEDIDOS_NFE, pedidos.getHora());
        values.put(VALOR_TOTAL_PEDIDOS_NFE, pedidos.getValor_total());
        values.put(CLIENTE_PEDIDOS_NFE, pedidos.getCliente());
        db.insert(TABELA_PEDIDOS_NFE, null, values);
        db.close();
    }

    void addItensPedidosNFE(ItensPedidos itensPedidos) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("pedido", itensPedidos.getPedido());
        values.put("produto", itensPedidos.getProduto());
        values.put("quantidade", itensPedidos.getQuantidade());
        values.put("valor", itensPedidos.getValor());
        db.insert("itens_pedidosNFE", null, values);
        db.close();
    }

    void upadtePedidosTransmissaoNFE(
            String situacao,
            String protocolo,
            String data_protocolo,
            String hora_protocolo,
            String id) {


        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("situacao", situacao);
        values.put("protocolo", protocolo);
        values.put("data_protocolo", data_protocolo);
        values.put("hora_protocolo", hora_protocolo);
        db.update("pedidosNFE", values, "id=" + id, null);
        db.close();
    }

    //CURSOR PEDIDOS
    private PedidosNFE cursorToPedidosNFE(Cursor cursor) {
        PedidosNFE pedidos = new PedidosNFE(null, null, null, null, null, null, null);
        //
        pedidos.setId(cursor.getString(0));
        pedidos.setSituacao(cursor.getString(1));
        pedidos.setProtocolo(cursor.getString(2));
        pedidos.setData(cursor.getString(3));
        pedidos.setHora(cursor.getString(4));
        pedidos.setValor_total(cursor.getString(5));
        pedidos.setCliente(cursor.getString(6));
        return pedidos;
    }

    //LISTAR TODOS OS PEDIDOS NFE
    public ArrayList<PedidosNFE> getPedidosNFE() {
        ArrayList<PedidosNFE> listaPedidos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PEDIDOS_NFE + " ORDER BY id DESC";
        Log.i(TAG + " - getPedidosNFE()", query);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                PedidosNFE pedidos = cursorToPedidosNFE(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        return listaPedidos;
    }

    //CURSOR PEDIDOS
    private StatusPedidosNFE cursorToStatusPedidosNFE(Cursor cursor) {
        StatusPedidosNFE pedidosNFE = new StatusPedidosNFE(null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
        //
        pedidosNFE.setId(cursor.getString(0));
        pedidosNFE.setSituacao(cursor.getString(1));
        pedidosNFE.setProtocolo(cursor.getString(2));
        pedidosNFE.setData(cursor.getString(3));
        pedidosNFE.setHora(cursor.getString(4));
        pedidosNFE.setValor_total(cursor.getString(5));
        pedidosNFE.setCliente(cursor.getString(6));
        pedidosNFE.setPedido(cursor.getString(7));
        pedidosNFE.setProduto(cursor.getString(8));
        pedidosNFE.setQuantidade(cursor.getString(9));
        pedidosNFE.setValor(cursor.getString(10));
        pedidosNFE.setCodigo(cursor.getString(11));
        pedidosNFE.setNome(cursor.getString(12));
        pedidosNFE.setTributos(cursor.getString(13));
        pedidosNFE.setValor_minimo(cursor.getString(14));
        pedidosNFE.setValor_maximo(cursor.getString(15));
        pedidosNFE.setQtd_revenda(cursor.getString(16));
        return pedidosNFE;
    }


    //LISTAR TODOS OS PEDIDOS
    public ArrayList<StatusPedidosNFE> getStatusPedidosNFE() {
        ArrayList<StatusPedidosNFE> listaPedidos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PEDIDOS_NFE + " ped " +
                "INNER JOIN itens_pedidosNFE ipe ON ipe.pedido = ped.id " +
                "INNER JOIN produtos pro ON pro.codigo = ipe.produto " +
                "ORDER BY " + ID_PEDIDOS_NFE + " DESC";

        Log.i("QUERY", query);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                StatusPedidosNFE pedidos = cursorToStatusPedidosNFE(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        return listaPedidos;
    }

    //CURSOR PEDIDOS
    private Pedidos cursorToPedidos(Cursor cursor) {
        Pedidos pedidos = new Pedidos(null, null, null, null, null, null, null, null, null, null);
        //
        pedidos.setId(cursor.getString(0));
        pedidos.setSituacao(cursor.getString(1));
        pedidos.setProtocolo(cursor.getString(2));
        pedidos.setData(cursor.getString(3));
        pedidos.setHora(cursor.getString(4));
        pedidos.setValor_total(cursor.getString(5));
        pedidos.setData_protocolo(cursor.getString(6));
        pedidos.setHora_protocolo(cursor.getString(7));
        pedidos.setCpf_cliente(cursor.getString(8));
        pedidos.setForma_pagamento(cursor.getString(9));
        return pedidos;
    }

    //LISTAR TODOS OS PEDIDOS
    public ArrayList<Pedidos> getPedidos() {
        ArrayList<Pedidos> listaPedidos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PEDIDOS + " ORDER BY " + ID_PEDIDOS + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Pedidos pedidos = cursorToPedidos(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        return listaPedidos;
    }

    //LISTAR TODOS OS PEDIDOS RELATÓRIO
    ArrayList<Pedidos> getPedidosRelatorio() {
        ArrayList<Pedidos> listaPedidos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PEDIDOS + " ORDER BY " + ID_PEDIDOS + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Pedidos pedidos = cursorToPedidos(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        return listaPedidos;
    }

    //LISTAR TODOS OS PEDIDOS RELATÓRIO
    ArrayList<PedidosNFE> getPedidosRelatorioNFE() {
        ArrayList<PedidosNFE> listaPedidos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PEDIDOS_NFE + " ORDER BY " + ID_PEDIDOS_NFE + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                PedidosNFE pedidos = cursorToPedidosNFE(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        return listaPedidos;
    }

    //CURSOR PEDIDOS
    private StatusPedidos cursorToStatusPedidos(Cursor cursor) {
        StatusPedidos pedidos = new StatusPedidos(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        //
        pedidos.setId(cursor.getString(0));
        pedidos.setSituacao(cursor.getString(1));
        pedidos.setProtocolo(cursor.getString(2));
        pedidos.setData(cursor.getString(3));
        pedidos.setHora(cursor.getString(4));
        pedidos.setValor_total(cursor.getString(5));
        pedidos.setData_protocolo(cursor.getString(6));
        pedidos.setHora_protocolo(cursor.getString(7));
        pedidos.setCpf_cliente(cursor.getString(8));
        pedidos.setForma_pagamento(cursor.getString(9));
        pedidos.setPedido(cursor.getString(11));
        pedidos.setProduto(cursor.getString(12));
        pedidos.setQuantidade(cursor.getString(13));
        pedidos.setValor(cursor.getString(14));
        pedidos.setNome(cursor.getString(16));
        return pedidos;
    }

    //LISTAR TODOS OS PEDIDOS
    public ArrayList<StatusPedidos> getStatusPedidos() {
        ArrayList<StatusPedidos> listaPedidos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PEDIDOS + " ped " +
                " INNER JOIN itens_pedidos ipe ON ipe.pedido = ped.id " +
                " INNER JOIN produtos pro ON pro.codigo = ipe.produto " +
                " ORDER BY " + ID_PEDIDOS + " DESC";

        Log.i("QUERY", query);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                StatusPedidos pedidos = cursorToStatusPedidos(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        return listaPedidos;
    }

    //PEGAR O VALOR TOTAL
    public String getValorTotal() {

        ClassAuxiliar aux = new ClassAuxiliar();

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String total = "0", totalNFE = "0";

        try {
            String query = "SELECT SUM((ipe.valor * ipe.quantidade)) / 100 " +
                    "FROM " + TABELA_PEDIDOS + " ped " +
                    " INNER JOIN itens_pedidos ipe ON ipe.pedido = ped.id " +
                    " INNER JOIN produtos pro ON pro.codigo = ipe.produto " +
                    " ORDER BY " + ID_PEDIDOS + " DESC";

            Log.i("SQL", query);

            Cursor cursor = db.rawQuery(query, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                total = String.valueOf(Double.parseDouble(cursor.getString(0)));
                //String[] a = total.split(".");

                //Log.i("TOTAL", "TOTAL NFC-e = " + a.length);

                Log.i("TOTAL", "TOTAL NFC-e = " + Double.parseDouble(cursor.getString(0)));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String query = "SELECT SUM((ipe.valor * ipe.quantidade)) / 100 FROM itens_pedidosNFE ipe";

            Cursor cursor = db.rawQuery(query, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                totalNFE = String.valueOf(Double.parseDouble(cursor.getString(0)));
                Log.i("TOTAL", "TOTAL NF-e = " + cursor.getString(0));
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*;

        db.endTransaction();
        db.close();
        */


        String[] qs = {
                (total != null ? String.valueOf(aux.converterValores(total)) : "0"),
                (totalNFE != null ? String.valueOf(aux.converterValores(totalNFE)) : "0")
        };
        String q = String.valueOf(aux.somar_valores(qs));

        Log.i("TOTAL", q);
        //String valorUnit = String.valueOf(aux.converterValores(q));


        db.endTransaction();
        db.close();

        return String.format("R$ %s", aux.maskMoney(new BigDecimal(q)));
    }

    /*
    //
    int getQuantProdutoRemessa(String Produto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ClassAuxiliar aux = new ClassAuxiliar();
        String quatProd = "0", quantInt = "0", quantIntNFE = "0";

        try {
            //
            Cursor produtos;
            String query_pos = "SELECT " + QTD_REVENDA + " FROM " + TABELA_PRODUTOS + " WHERE " + NOME_PRODUTO + " = '" + Produto + "' LIMIT 1";
            produtos = db.rawQuery(query_pos, null);
            if (produtos.moveToFirst()) {
                do {
                    quatProd = produtos.getString(produtos.getColumnIndex(QTD_REVENDA));
                } while (produtos.moveToNext());
            }
        } catch (Exception ignored) {
        }

        try {
            //
            Cursor intProdutos;
            String query_intPro = "SELECT SUM(quantidade) quantidade FROM itens_pedidos";
            intProdutos = db.rawQuery(query_intPro, null);
            if (intProdutos.moveToFirst()) {
                do {
                    quantInt = intProdutos.getString(intProdutos.getColumnIndex("quantidade"));
                } while (intProdutos.moveToNext());
            }
        } catch (Exception ignored) {
        }

        try {
            //
            Cursor intProdutosNFE;
            String query_intProNFE = "SELECT SUM(quantidade) quantidade FROM itens_pedidosNFE";
            intProdutosNFE = db.rawQuery(query_intProNFE, null);
            if (intProdutosNFE.moveToFirst()) {
                do {
                    quantIntNFE = intProdutosNFE.getString(intProdutosNFE.getColumnIndex("quantidade"));
                } while (intProdutosNFE.moveToNext());
            }
        } catch (Exception ignored) {
        }

        String[] qs = {(quantInt != null ? quantInt : "0"), (quantIntNFE != null ? quantIntNFE : "0")};
        String q = String.valueOf(aux.somar(qs));

        Log.e("TOTAL Q", " ----- " + q);

        String[] t = {quatProd, q};
        float p = Float.parseFloat(String.valueOf(aux.subitrair(t)));

        Log.e("TOTAL P", " ----- " + p);
        return Math.round(p);
    }
     */

    //ULTIMA NFC-E EMITIDA
    public String ultimaNFCE() {

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String id = "0";

        try {

            String query = "SELECT ped.id FROM " + TABELA_PEDIDOS + " ped " +
                    " ORDER BY ped.id DESC";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }


        db.endTransaction();
        db.close();

        return id;
    }

    //ULTIMA NFC-E EMITIDA
    public String ultimaNFE() {

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String id = "0";

        try {

            String query = "SELECT ped.id FROM " + TABELA_PEDIDOS_NFE + " ped " +
                    " ORDER BY ped.id DESC";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }


        db.endTransaction();
        db.close();

        return id;
    }

    //PEGAR O VALOR TOTAL
    public String getQuantTotal() {

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        ClassAuxiliar aux = new ClassAuxiliar();
        String total = "0", totalNFE = "0";


        try {

            String query = "SELECT SUM(ipe.quantidade) FROM " + TABELA_PEDIDOS + " ped " +
                    " INNER JOIN itens_pedidos ipe ON ipe.pedido = ped.id " +
                    " INNER JOIN produtos pro ON pro.codigo = ipe.produto " +
                    " ORDER BY " + ID_PEDIDOS + " DESC";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ///////////

        try {
            String queryNFE = "SELECT SUM(ipe.quantidade) FROM " + TABELA_PEDIDOS_NFE + " ped " +
                    " INNER JOIN itens_pedidosNFE ipe ON ipe.pedido = ped.id " +
                    " INNER JOIN produtos pro ON pro.codigo = ipe.produto " +
                    " ORDER BY ipe.pedido DESC";

            Cursor cursorNFE = db.rawQuery(queryNFE, null);
            if (cursorNFE.getCount() > 0) {
                cursorNFE.moveToFirst();
                totalNFE = cursorNFE.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] qs = {(total != null ? total : "0"), (totalNFE != null ? totalNFE : "0")};
        String q = String.valueOf(aux.somar(qs));


        db.endTransaction();
        db.close();

        return String.valueOf(Math.round(Float.parseFloat(q)));
    }

    //LISTAR TODOS OS PEDIDOS A TRANSMITIR
    public ArrayList<Pedidos> getPedidosTransmitir() {
        ArrayList<Pedidos> listaPedidos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PEDIDOS + " WHERE " + PROTOCOLO_PEDIDOS + " = '' ORDER BY " + ID_PEDIDOS + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Pedidos pedidos = cursorToPedidos(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        return listaPedidos;
    }

    //LISTAR TODOS OS PEDIDOS A TRANSMITIR
    public ArrayList<Pedidos> getPedidosTransmitirFecharDia() {
        ArrayList<Pedidos> listaPedidos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PEDIDOS + " WHERE " + SITUACAO_PEDIDOS + " = 'OFF' OR " + SITUACAO_PEDIDOS + " = ''  ORDER BY " + ID_PEDIDOS + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Pedidos pedidos = cursorToPedidos(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        return listaPedidos;
    }

    //
    String ultimoIdPedido() {
        //BANCO DE DADOS
        SQLiteDatabase db = this.getReadableDatabase();

        //
        String ultimoId = null;

        //
        Cursor pos;
        String query_pos = "SELECT * FROM pos";
        pos = db.rawQuery(query_pos, null);
        if (pos.moveToFirst()) {
            do {

                ultimoId = pos.getString(pos.getColumnIndex("ultnfce"));

            } while (pos.moveToNext());
        }

        return ultimoId;
    }

    // APAGAR PEDIDO
    int deletePedido(String idPedido) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                "pedidos",
                ID_PEDIDOS + " = ?",
                new String[]{idPedido}
        );
        db.close();
        return i;
    }

    //GERAR CHAVE DE ACESSO DA NOTA NFC-E
    String gerarChave(int numeroNota) {
        //BANCO DE DADOS
        SQLiteDatabase db = this.getReadableDatabase();

        //CLASSE AUXILIAR
        ClassAuxiliar aux = new ClassAuxiliar();

        //CHAVE
        String chave;

        //CONTANTES CHAVE
        String chave_cUF = null;                    // Código da UF do emitente do Documento Fiscal
        String chave_AAMM = aux.anoMesAtual();  // Ano e Mês de emissão da NF-e
        String chave_CNPJ = null;               // CNPJ do emitente
        String chave_mod = "65";                // Modelo do Documento Fiscal
        String chave_serie = null;              // Série do Documento Fiscal
        String chave_nNF = null;                // Número do Documento Fiscal
        String chave_tpEmis = "1";             // Forma de emissão da NF-e
        String chave_cNF = null;                // Código Numérico que compõe a Chave de Acesso
        String chave_cDV = null;                // Dígito Verificador da Chave de Acesso

        //Código da UF - 2 caracteres
        Cursor unidades;
        String query_unidades = "SELECT * FROM unidades";
        unidades = db.rawQuery(query_unidades, null);
        if (unidades.moveToFirst()) {
            do {

                if (unidades.getColumnIndex("cnpj") == 2) {
                    chave_CNPJ = aux.soNumeros(unidades.getString(unidades.getColumnIndex("cnpj")));
                }
                if (unidades.getColumnIndex("numero") == 4) {
                    chave_cUF = aux.idEstado(unidades.getString(unidades.getColumnIndex("uf")));
                }

            } while (unidades.moveToNext());
        }

        //
        Cursor pos;
        String query_pos = "SELECT * FROM pos";
        pos = db.rawQuery(query_pos, null);
        if (pos.moveToFirst()) {
            do {

                if (pos.getColumnIndex("serie") == 3) {
                    chave_serie = aux.soNumeros(pos.getString(pos.getColumnIndex("serie")));
                }

            } while (pos.moveToNext());
        }

        //VERIFICA A QUANTIDADE DE CARACTERES DA SERIE
        switch (chave_serie.length()) {
            case 1:
                chave_serie = "00" + chave_serie;
                break;
            case 2:
                chave_serie = "0" + chave_serie;
                break;
        }

        //ACRESENTA ZEROS NA FRENTE DO NÚMERO DA NOTA PARA COMPLETAR 9 CARACTERES
        chave_nNF = String.format("%09d", numeroNota);

        //ACRESENTA ZEROS NA FRENTE DO NÚMERO DA NOTA PARA COMPLETAR 9 CARACTERES
        chave_cNF = String.format("%08d", numeroNota);

        String chaveSD = chave_cUF + chave_AAMM + chave_CNPJ + chave_mod + chave_serie + chave_nNF + chave_tpEmis + chave_cNF;
        chave = aux.digitoVerificado(aux.soNumeros(chaveSD));
        return chave;
    }

    //
    String getSeriePOS() {
        //BANCO DE DADOS
        SQLiteDatabase db = this.getReadableDatabase();

        //CLASSE AUXILIAR
        ClassAuxiliar aux = new ClassAuxiliar();

        String serie = null;

        //
        Cursor pos;
        String query_pos = "SELECT * FROM pos";
        pos = db.rawQuery(query_pos, null);
        if (pos.moveToFirst()) {
            do {

                if (pos.getColumnIndex("serie") == 3) {
                    serie = aux.soNumeros(pos.getString(pos.getColumnIndex("serie")));
                }

            } while (pos.moveToNext());
        }

        //VERIFICA A QUANTIDADE DE CARACTERES DA SERIE
        switch (serie.length()) {
            case 1:
                serie = "00" + serie;
                break;
            case 2:
                serie = "0" + serie;
                break;
        }

        return serie;
    }

    //
    String getSerialPOS() {
        //BANCO DE DADOS
        SQLiteDatabase db = this.getReadableDatabase();

        //CLASSE AUXILIAR
        ClassAuxiliar aux = new ClassAuxiliar();

        String serial = null;

        //
        Cursor pos;
        String query_pos = "SELECT serial FROM pos";
        pos = db.rawQuery(query_pos, null);
        if (pos.moveToFirst()) {
            do {

               /* if (pos.getColumnIndex("serial") == 3) {
                    serie = aux.soNumeros(pos.getString(pos.getColumnIndex("serie")));
                }*/
                serial = aux.soNumeros(pos.getString(pos.getColumnIndex("serial")));

            } while (pos.moveToNext());
        }

        return serial;
    }

    //CURSOR PEDIDOS
    private Unidades cursorToUnidade(Cursor cursor) {
        Unidades unidades = new Unidades(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        //
        unidades.setCodigo(cursor.getString(0));
        unidades.setRazao_social(cursor.getString(1));
        unidades.setCnpj(cursor.getString(2));
        unidades.setEndereco(cursor.getString(3));
        unidades.setNumero(cursor.getString(4));
        unidades.setBairro(cursor.getString(5));
        unidades.setCep(cursor.getString(6));
        unidades.setTelefone(cursor.getString(7));
        unidades.setIe(cursor.getString(8));
        unidades.setCidade(cursor.getString(9));
        unidades.setUf(cursor.getString(10));
        unidades.setCodigo_ibge(cursor.getString(11));
        unidades.setUrl_consulta(cursor.getString(12));
        unidades.setCodloja(cursor.getString(13));
        unidades.setIdCSC(cursor.getString(14));
        unidades.setCSC(cursor.getString(15));
        unidades.setUrl_qrcode(cursor.getString(16));
        return unidades;
    }

    //LISTAR TODAS AS UNIDADES
    public ArrayList<Unidades> getUnidades() {
        ArrayList<Unidades> listaUnidades = new ArrayList<>();

        String query = "SELECT * FROM unidades";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Unidades unidades = cursorToUnidade(cursor);
                listaUnidades.add(unidades);
            } while (cursor.moveToNext());
        }

        return listaUnidades;
    }

    //CURSOR POS
    private PosApp cursorToPos(Cursor cursor) {
        PosApp posApp = new PosApp(null, null, null, null, null, null, null, null);

        //
        posApp.setCodigo(cursor.getString(0));
        posApp.setSerial(cursor.getString(1));
        posApp.setUnidade(cursor.getString(2));
        posApp.setSerie(cursor.getString(3));
        posApp.setUltnfce(cursor.getString(4));
        posApp.setUltboleto(cursor.getString(5));
        return posApp;
    }

    //LISTAR TODAS AS UNIDADES
    public ArrayList<PosApp> getPos() {
        ArrayList<PosApp> listaPos = new ArrayList<>();

        String query = "SELECT * FROM pos";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                PosApp posApp = cursorToPos(cursor);
                listaPos.add(posApp);
            } while (cursor.moveToNext());
        }

        return listaPos;
    }

    // GET LIST CREDENCIADORAS

    ArrayList<String> getCredenciadora() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String selectQuery = "Select * From credenciadoras";
        Cursor cursor = db.rawQuery(selectQuery, null);
        list.add("CREDENCIADORA");
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String nome = cursor.getString(cursor.getColumnIndex("descricao_credenciadora"));
                    list.add(nome);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return list;
    }

    /*

    //
    //CONSTANTES CLIENTES
    private static final String TABELA_CLIENTES = "clientes";
    private static final String CODIGO_CLIENTE = "codigo_cliente";
    private static final String NOME_CLIENTE = "nome_cliente";

    private static final String[] COLUNAS_CLIENTES = {CODIGO_CLIENTE, NOME_CLIENTE};

    public void addCliente(Clientes clientes) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NOME_CLIENTE, clientes.getNome());
        db.insert(TABELA_CLIENTES, null, values);
        db.close();
    }

    //CONSULTAR CLIENTE
    public Clientes getCliente(String codigo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABELA_CLIENTES, // TABELA
                COLUNAS_CLIENTES, // COLUNAS
                " codigo = ?", // COLUNAS PARA COMPARAR
                new String[]{String.valueOf(codigo)}, // PARAMETROS
                null, // GROUP BY
                null, // HAVING
                null, // ORDER BY
                null // LIMIT
        );

        //
        if (cursor == null) {
            return null;
        } else {
            cursor.moveToFirst();
            Clientes clientes = cursorToCliente(cursor);
            return clientes;
        }
    }

    //
    private Clientes cursorToCliente(Cursor cursor) {
        Clientes clientes = new Clientes(null, null);
        //clientes.setCodigo(Integer.parseInt(cursor.getString(0)));
        clientes.setCodigo(cursor.getString(0));
        clientes.setNome(cursor.getString(1));
        return clientes;
    }

    //LISTAR TODOS OS CLIENTES
    public ArrayList<Clientes> getAllClientes() {
        ArrayList<Clientes> listaClientes = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_CLIENTES + " ORDER BY " + NOME_CLIENTE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Clientes clientes = cursorToCliente(cursor);
                listaClientes.add(clientes);
            } while (cursor.moveToNext());
        }

        return listaClientes;
    }

    //ALTERAR CLIENTE
    public int updateCliete(Clientes clientes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NOME_CLIENTE, clientes.getNome());

        int i = db.update(
                TABELA_CLIENTES,
                values,
                CODIGO_CLIENTE + " = ?",
                new String[]{String.valueOf(clientes.getCodigo())}
        );
        db.close();
        return i;
    }

    //
    public int deleteCliente(Clientes clientes) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                TABELA_CLIENTES,
                CODIGO_CLIENTE + " = ?",
                new String[]{String.valueOf(clientes.getCodigo())}
        );
        db.close();
        return i;
    }

    ///////


    //SOMAR O VALOR DO FINANCEIRO
    public String getIdUnidade(String unidade) {

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        String selectQuery = "SELECT id_unidade FROM unidades WHERE descricao_unidade = '" + unidade + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        String id = "";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return id;
    }


    //########## PRODUTOS ############

    //
    private Produtos cursorToProdutos(Cursor cursor) {
        Produtos produtos = new Produtos(null, null);
        produtos.setCodigo_produto(cursor.getString(0));
        produtos.setDescricao_produto(cursor.getString(1));
        return produtos;
    }

    //LISTAR TODOS OS CLIENTES
    public ArrayList<Produtos> getAllProdutos() {
        ArrayList<Produtos> listaProdutos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PRODUTOS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                //Produtos prods = cursorToProdutos(cursor);
                //listaProdutos.add(prods);

                String codigo_produto = cursor.getString(0);
                String descricao_produto = cursor.getString(1);
                Produtos prod = new Produtos(codigo_produto, descricao_produto);
                listaProdutos.add(prod);
            } while (cursor.moveToNext());
        }

        return listaProdutos;
    }

    public ArrayList<String> getProdutos() {
        ArrayList<String> list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String selectQuery = "Select * From " + TABELA_PRODUTOS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String codigo_produto = cursor.getString(cursor.getColumnIndex("codigo_produto"));
                    String descricao_produto = cursor.getString(cursor.getColumnIndex("descricao_produto"));
                    //list.add(codigo_produto + " " + descricao_produto);
                    list.add(descricao_produto);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return list;
    }

    //############### VENDAS ###############

    //CONSTANTES VENDAS
    private static final String TABELA_VENDAS = "vendas_app";
    private static final String CODIGO_VENDA = "codigo_venda";
    private static final String CODIGO_CLIENTE_VENDA = "codigo_cliente";
    private static final String UNIDADE_VENDA = "unidade_venda";
    private static final String PRODUTO_VENDA = "produto_venda";
    private static final String DATA_MOVIMENTO = "data_movimento";
    private static final String QUANTIDADE_VENDA = "quantidade_venda";
    private static final String PRECO_UNITARIO = "preco_unitario";
    private static final String VALOR_TOTAL = "valor_total";
    private static final String VENDEDOR_VENDA = "vendedor_venda";
    private static final String STATUS_AUTORIZACAO_VENDA = "status_autorizacao_venda";
    private static final String ENTREGA_FUTURA_VENDA = "entrega_futura_venda";
    private static final String ENTREGA_FUTURA_REALIZADA = "entrega_futura_realizada";
    private static final String USUARIO_ATUAL = "usuario_atual";
    private static final String DATA_CADASTRO = "data_cadastro";
    private static final String CODIGO_VENDA_APP = "codigo_venda_app";
    private static final String VENDA_FINALIZADA_APP = "venda_finalizada_app";

    private static final String[] COLUNAS_VENDAS = {
            CODIGO_VENDA,
            CODIGO_CLIENTE_VENDA,
            UNIDADE_VENDA,
            PRODUTO_VENDA,
            DATA_MOVIMENTO,
            QUANTIDADE_VENDA,
            PRECO_UNITARIO,
            VALOR_TOTAL,
            VENDEDOR_VENDA,
            STATUS_AUTORIZACAO_VENDA,
            ENTREGA_FUTURA_VENDA,
            ENTREGA_FUTURA_REALIZADA,
            USUARIO_ATUAL,
            DATA_CADASTRO,
            CODIGO_VENDA_APP,
            VENDA_FINALIZADA_APP
    };

    //
    private VendasDomain cursorToVendas(Cursor cursor) {
        VendasDomain vendas = new VendasDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        vendas.setCodigo_venda(cursor.getString(0));
        vendas.setCodigo_cliente(cursor.getString(1));
        vendas.setUnidade_venda(cursor.getString(2));
        vendas.setProduto_venda(cursor.getString(3));
        vendas.setData_movimento(cursor.getString(4));
        vendas.setQuantidade_venda(cursor.getString(5));
        vendas.setPreco_unitario(cursor.getString(6));
        vendas.setValor_total(cursor.getString(7));
        vendas.setVendedor_venda(cursor.getString(8));
        vendas.setStatus_autorizacao_venda(cursor.getString(9));
        vendas.setEntrega_futura_venda(cursor.getString(10));
        vendas.setEntrega_futura_realizada(cursor.getString(11));
        vendas.setUsuario_atual(cursor.getString(12));
        vendas.setData_cadastro(cursor.getString(13));
        vendas.setCodigo_venda_app(cursor.getString(14));
        vendas.setVenda_finalizada_app(cursor.getString(15));
        return vendas;
    }

    //LISTAR TODOS OS CLIENTES
    public ArrayList<VendasDomain> getAllVendas() {
        ArrayList<VendasDomain> listaVendas = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_VENDAS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                VendasDomain vendas = cursorToVendas(cursor);
                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }

        return listaVendas;
    }

    //LISTAR TODOS OS ITENS DA VENDA
    public ArrayList<VendasDomain> getVendasCliente(int codigo_venda_app) {
        ArrayList<VendasDomain> listaVendas = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_VENDAS + " WHERE codigo_venda_app = '" + codigo_venda_app + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                VendasDomain vendas = cursorToVendas(cursor);
                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }

        //db.close();
        return listaVendas;
    }

    //LISTAR TODOS OS ITENS DA VENDA
    public String[] getUltimaVendasCliente() {

        String query = "SELECT " +
                "ven." + CODIGO_VENDA + ", " +
                "ven." + CODIGO_VENDA_APP + ", " +
                "cli." + CODIGO_CLIENTE + ", " +
                "cli." + NOME_CLIENTE +
                " FROM " + TABELA_VENDAS + " ven" +
                " INNER JOIN " + TABELA_CLIENTES + " cli ON cli." + CODIGO_CLIENTE + " = ven." + CODIGO_CLIENTE_VENDA +
                " ORDER BY " + "ven." + CODIGO_VENDA_APP + " DESC" +
                " LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        String[] id = {};
        try {
            id = new String[]{
                    cursor.getString(cursor.getColumnIndex(CODIGO_VENDA)),
                    cursor.getString(cursor.getColumnIndex(CODIGO_VENDA_APP)),
                    cursor.getString(cursor.getColumnIndex(CODIGO_CLIENTE)),
                    cursor.getString(cursor.getColumnIndex(NOME_CLIENTE))
            };
        } catch (Exception e) {

        }
        return id;
    }

    //
    public void addVenda(VendasDomain vendas) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CODIGO_VENDA, vendas.getCodigo_venda());
        values.put(CODIGO_CLIENTE_VENDA, vendas.getCodigo_cliente());
        values.put(UNIDADE_VENDA, vendas.getUnidade_venda());
        values.put(PRODUTO_VENDA, vendas.getProduto_venda());
        values.put(DATA_MOVIMENTO, vendas.getData_movimento());
        values.put(QUANTIDADE_VENDA, vendas.getQuantidade_venda());
        values.put(PRECO_UNITARIO, vendas.getPreco_unitario());
        values.put(VALOR_TOTAL, vendas.getValor_total());
        values.put(VENDEDOR_VENDA, vendas.getVendedor_venda());
        values.put(STATUS_AUTORIZACAO_VENDA, vendas.getStatus_autorizacao_venda());
        values.put(ENTREGA_FUTURA_VENDA, vendas.getEntrega_futura_venda());
        values.put(ENTREGA_FUTURA_REALIZADA, vendas.getEntrega_futura_realizada());
        values.put(USUARIO_ATUAL, vendas.getUsuario_atual());
        values.put(DATA_CADASTRO, vendas.getData_cadastro());
        values.put(CODIGO_VENDA_APP, vendas.getCodigo_venda_app());
        values.put(VENDA_FINALIZADA_APP, vendas.getVenda_finalizada_app());
        db.insert(TABELA_VENDAS, null, values);
        db.close();
    }

    //LISTAR TODOS OS CLIENTES
    public String getValorTotalVenda(String codigo_venda_app) {

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        String selectQuery = "SELECT SUM(valor_total) FROM " + TABELA_VENDAS + " WHERE " + CODIGO_VENDA_APP + " = '" + codigo_venda_app + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        String total = "0.0";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return total;
    }

    //CONSULTAR VENDA
    public VendasDomain getVenda(String codigo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABELA_VENDAS, // TABELA
                COLUNAS_VENDAS, // COLUNAS
                " codigo_venda = ?", // COLUNAS PARA COMPARAR
                new String[]{String.valueOf(codigo)}, // PARAMETROS
                null, // GROUP BY
                null, // HAVING
                null, // ORDER BY
                null // LIMIT
        );

        //
        if (cursor == null) {
            return null;
        } else {
            cursor.moveToFirst();
            VendasDomain vendas = cursorToVendas(cursor);
            return vendas;
        }
    }

    //
    public int deleteItemVenda(VendasDomain vendasDomain) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                TABELA_VENDAS,
                CODIGO_VENDA + " = ?",
                new String[]{String.valueOf(vendasDomain.getCodigo_venda())}
        );
        db.close();
        return i;
    }

    //
    public int deleteVenda(int codigo_venda_app) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                TABELA_VENDAS,
                CODIGO_VENDA_APP + " = ?",
                new String[]{String.valueOf(codigo_venda_app)}
        );
        db.close();
        return i;
    }


    //########## RELATÓRIOS DE VENDA ############
    //LISTAR TODOS OS CLIENTES
    public ArrayList<VendasDomain> getRelatorioVendas() {
        ArrayList<VendasDomain> listaVendas = new ArrayList<>();

        String query = "SELECT " +
                "codigo_venda, " +
                "codigo_cliente, " +
                "unidade_venda, " +
                "produto_venda, " +
                "data_movimento, " +
                "SUM(quantidade_venda) quantidade_venda, " +
                "preco_unitario, " +
                "SUM(valor_total) valor_total, " +
                "vendedor_venda, " +
                "status_autorizacao_venda, " +
                "entrega_futura_venda, " +
                "entrega_futura_realizada, " +
                "usuario_atual, " +
                "data_cadastro, " +
                "codigo_venda_app, " +
                "venda_finalizada_app " +
                "FROM " + TABELA_VENDAS + " WHERE venda_finalizada_app = '1' GROUP BY " + PRODUTO_VENDA;

        //Log.e("SQL = ", query);


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                VendasDomain vendas = cursorToVendas(cursor);
                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }

        return listaVendas;
    }

    //
    public ArrayList<RelatorioVendasClientesDomain> getRelatorioVendasClientes(String produto) {
        RelatorioVendasClientesDomain dRelatorio = new RelatorioVendasClientesDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        ArrayList<RelatorioVendasClientesDomain> listaVendasS = new ArrayList<>();

        String query = "SELECT " +
                "ven.codigo_venda codigo_venda, " +
                "ven.codigo_cliente codigo_cliente, " +
                "ven.unidade_venda unidade_venda, " +
                "ven.produto_venda produto_venda, " +
                "ven.data_movimento data_movimento, " +
                "SUM(ven.quantidade_venda) quantidade_venda, " +
                "ven.preco_unitario preco_unitario, " +
                "SUM(ven.valor_total) valor_total, " +
                "ven.vendedor_venda vendedor_venda, " +
                "ven.status_autorizacao_venda status_autorizacao_venda, " +
                "ven.entrega_futura_venda entrega_futura_venda, " +
                "ven.entrega_futura_realizada entrega_futura_realizada, " +
                "ven.usuario_atual usuario_atual, " +
                "ven.data_cadastro data_cadastro, " +
                "ven.codigo_venda_app codigo_venda_app, " +
                "(SELECT cli.nome_cliente FROM " + TABELA_CLIENTES + " AS cli WHERE cli.codigo_cliente = ven.codigo_cliente) nome " +
                " FROM " + TABELA_VENDAS + " AS ven " +
                " WHERE ven.produto_venda = '" + produto + "' AND ven.venda_finalizada_app = '1' " +
                " GROUP BY ven.codigo_cliente " +
                " ORDER BY nome";

        Log.e("SQL ", query);


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                dRelatorio = new RelatorioVendasClientesDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

                dRelatorio.setCodigo_venda(cursor.getString(cursor.getColumnIndex("codigo_venda")));
                dRelatorio.setCodigo_cliente(cursor.getString(cursor.getColumnIndex("codigo_cliente")));
                dRelatorio.setUnidade_venda(cursor.getString(cursor.getColumnIndex("unidade_venda")));
                dRelatorio.setProduto_venda(cursor.getString(cursor.getColumnIndex("produto_venda")));
                dRelatorio.setData_movimento(cursor.getString(cursor.getColumnIndex("data_movimento")));
                dRelatorio.setQuantidade_venda(cursor.getString(cursor.getColumnIndex("quantidade_venda")));
                dRelatorio.setPreco_unitario(cursor.getString(cursor.getColumnIndex("preco_unitario")));
                dRelatorio.setValor_total(cursor.getString(cursor.getColumnIndex("valor_total")));
                dRelatorio.setVendedor_venda(cursor.getString(cursor.getColumnIndex("vendedor_venda")));
                dRelatorio.setStatus_autorizacao_venda(cursor.getString(cursor.getColumnIndex("status_autorizacao_venda")));
                dRelatorio.setEntrega_futura_venda(cursor.getString(cursor.getColumnIndex("entrega_futura_venda")));
                dRelatorio.setEntrega_futura_realizada(cursor.getString(cursor.getColumnIndex("entrega_futura_realizada")));
                dRelatorio.setUsuario_atual(cursor.getString(cursor.getColumnIndex("usuario_atual")));
                dRelatorio.setData_cadastro(cursor.getString(cursor.getColumnIndex("data_cadastro")));
                dRelatorio.setCodigo_venda_app(cursor.getString(cursor.getColumnIndex("codigo_venda_app")));
                dRelatorio.setNome(cursor.getString(cursor.getColumnIndex("nome")));

                listaVendasS.add(dRelatorio);
            } while (cursor.moveToNext());
        }

        return listaVendasS;
    }

    //
    public ArrayList<FinanceiroVendasDomain> getRelatorioContasReceber() {
        ArrayList<FinanceiroVendasDomain> listaVendas = new ArrayList<>();

        String query = "SELECT " +
                "codigo_financeiro, " +
                "unidade_financeiro, " +
                "data_financeiro, " +
                "codigo_cliente_financeiro, " +
                "fpagamento_financeiro, " +
                "documento_financeiro, " +
                "vencimento_financeiro, " +
                "SUM(valor_financeiro) valor_financeiro, " +
                "status_autorizacao, " +
                "pago, " +
                "vasilhame_ref, " +
                "usuario_atual, " +
                "data_inclusao, " +
                "nosso_numero_financeiro, " +
                "id_vendedor_financeiro, " +
                "id_financeiro_app " +
                "FROM recebidos " +
                "GROUP BY fpagamento_financeiro";

        //Log.e("SQL = ", query);


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FinanceiroVendasDomain vendas = cursorToFinanceiroVendasDomain(cursor);
                listaVendas.add(vendas);
            } while (cursor.moveToNext());
        }

        return listaVendas;
    }


    //########## FORMAS PAGAMENTO CLIENTE ############

    //CONSTANTES FORMAS PAGAMENTO CLIENTE
    private static final String TABELA_FORMAS_PAGAMENTO = "formas_pagamento";
    private static final String CODIGO_PAGAMENTO = "codigo_pagamento";
    private static final String DESCRICAO_FORMA_PAGAMENTO = "descricao_forma_pagamento";
    private static final String TIPO_FORMA_PAGAMENTO = "tipo_forma_pagamento";
    private static final String AUTO_NUM_PAGAMENTO = "auto_num_pagamento";
    private static final String BAIXA_FORMA_PAGAMENTO = "baixa_forma_pagamento";
    private static final String USUARIO_ATUAL_FORMA_PAGAMENTO = "usuario_atual";
    private static final String DATA_CADASTRO_FORMA_PAGAMENTO = "data_cadastro";
    private static final String ATIVO_FORMA_PAGAMENTO = "ativo";
    private static final String CONTA_BANCARIA_FORMA_PAGAMENTO = "conta_bancaria";


    private static final String[] COLUNAS_FORMAS_PAGAMENTO = {
            CODIGO_PAGAMENTO,
            DESCRICAO_FORMA_PAGAMENTO,
            TIPO_FORMA_PAGAMENTO,
            AUTO_NUM_PAGAMENTO,
            BAIXA_FORMA_PAGAMENTO,
            USUARIO_ATUAL_FORMA_PAGAMENTO,
            DATA_CADASTRO_FORMA_PAGAMENTO,
            ATIVO_FORMA_PAGAMENTO,
            CONTA_BANCARIA_FORMA_PAGAMENTO
    };

    //
    private FormasPagamentoDomain cursorToFormasPagamentoDomain(Cursor cursor) {
        FormasPagamentoDomain formasPagamentoDomain = new FormasPagamentoDomain(null, null, null, null, null, null, null, null, null);

        formasPagamentoDomain.setCodigo_pagamento(cursor.getString(0));
        formasPagamentoDomain.setDescricao_forma_pagamento(cursor.getString(1));
        formasPagamentoDomain.setTipo_forma_pagamento(cursor.getString(2));
        formasPagamentoDomain.setAuto_num_pagamento(cursor.getString(3));
        formasPagamentoDomain.setBaixa_forma_pagamento(cursor.getString(4));
        formasPagamentoDomain.setUsuario_atual(cursor.getString(5));
        formasPagamentoDomain.setData_cadastro(cursor.getString(6));
        formasPagamentoDomain.setAtivo(cursor.getString(7));
        formasPagamentoDomain.setConta_bancaria(cursor.getString(8));

        return formasPagamentoDomain;
    }


//########## FORMAS PAGAMENTO CLIENTE ############

    //CONSTANTES FORMAS PAGAMENTO CLIENTE
    private static final String TABELA_FORMAS_PAGAMENTO_CLIENTE = "formas_pagamento_cliente";
    private static final String CODIGO_PAGAMENTO_CLIENTE = "codigo_pagamento_cliente";
    private static final String PAGAMENTO_CLIENTE = "pagamento_cliente";
    private static final String PAGAMENTO_PRAZO_CLIENTE = "pagamento_prazo_cliente";
    private static final String CLIENTE_PAGAMENTO = "cliente_pagamento";
    private static final String USUARIO = "usuario";

    private static final String[] COLUNAS_FORMAS_PAGAMENTO_CLIENTE = {
            CODIGO_PAGAMENTO_CLIENTE,
            PAGAMENTO_CLIENTE,
            PAGAMENTO_PRAZO_CLIENTE,
            CLIENTE_PAGAMENTO,
            USUARIO
    };

    //
    private FormasPagamentoClienteDomain cursorToFormasPagamentoClienteDomain(Cursor cursor) {
        FormasPagamentoClienteDomain formasPagamentoClienteDomain = new FormasPagamentoClienteDomain(null, null, null, null, null);

        formasPagamentoClienteDomain.setCodigo_pagamento_cliente(cursor.getString(0));
        formasPagamentoClienteDomain.setPagamento_cliente(cursor.getString(1));
        formasPagamentoClienteDomain.setPagamento_prazo_cliente(cursor.getString(2));
        formasPagamentoClienteDomain.setCliente_pagamento(cursor.getString(3));
        formasPagamentoClienteDomain.setUsuario(cursor.getString(4));

        return formasPagamentoClienteDomain;
    }

    public ArrayList<String> getFormasPagamentoCliente(String codigoCliente) {
        ArrayList<String> list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        //
        String selectQuery = "SELECT fpg.codigo_pagamento, fpg.descricao_forma_pagamento, fpg.tipo_forma_pagamento, fpg.auto_num_pagamento, fpg.baixa_forma_pagamento,\n" +
                "fpg.usuario_atual, fpg.data_cadastro, fpg.ativo, fpg.conta_bancaria\n" +
                "FROM formas_pagamento fpg\n" +
                "WHERE fpg.tipo_forma_pagamento = 'A VISTA'\n" +
                "UNION ALL\n" +
                "SELECT fpg.codigo_pagamento, fpg.descricao_forma_pagamento, fpg.tipo_forma_pagamento, fpg.auto_num_pagamento, fpg.baixa_forma_pagamento,\n" +
                "fpg.usuario_atual, fpg.data_cadastro, fpg.ativo, fpg.conta_bancaria\n" +
                "FROM formas_pagamento fpg\n" +
                "INNER JOIN formas_pagamento_cliente fpc ON fpc.pagamento_cliente = fpg.descricao_forma_pagamento\n" +
                "WHERE fpc.cliente_pagamento = '" + codigoCliente + "'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        //list.add("DINHEIRO" + " _ " + "A VISTA");// + " _ " + "1"
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    //String codigo_pagamento_cliente = cursor.getString(cursor.getColumnIndex("codigo_pagamento_cliente"));
                    String pagamento_cliente = cursor.getString(cursor.getColumnIndex("descricao_forma_pagamento"));
                    String tipo_pagamento = cursor.getString(cursor.getColumnIndex("tipo_forma_pagamento"));
                    list.add(
                            pagamento_cliente + " _ " +
                                    tipo_pagamento + " _ " +
                                    cursor.getString(cursor.getColumnIndex("auto_num_pagamento")) + " _ " +
                                    cursor.getString(cursor.getColumnIndex("baixa_forma_pagamento"))
                    );
                    //list.add(descricao_produto);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.endTransaction();
            //db.close();
        }

        db.endTransaction();
        db.close();
        return list;
    }

//************ FINANCEIRO **************

    //CONSTANTES FINANCEIRO
    private static final String TABELA_FINANCEIRO = "financeiro";
    private static final String CODIGO_FINANCEIRO = "codigo_financeiro";
    private static final String UNIDADE_FINANCEIRO = "unidade_financeiro";
    private static final String DATA_FINANCEIRO = "data_financeiro";
    private static final String CODIGO_CLIENTE_FINANCEIRO = "codigo_cliente_financeiro";
    private static final String FPAGAMENTO_FINANCEIRO = "fpagamento_financeiro";
    private static final String DOCUMENTO_FINANCEIRO = "documento_financeiro";
    private static final String VENCIMENTO_FINANCEIRO = "vencimento_financeiro";
    private static final String VALOR_FINANCEIRO = "valor_financeiro";
    private static final String STATUS_AUTORIZACAO = "status_autorizacao";
    private static final String PAGO = "pago";
    private static final String VASILHAME_REF = "vasilhame_ref";
    private static final String USUARIO_ATUAL_FINANCEIRO = "usuario_atual";
    private static final String DATA_INCLUSAO = "data_inclusao";
    private static final String NOSSO_NUMERO_FINANCEIRO = "nosso_numero_financeiro";
    private static final String ID_VENDEDOR_FINANCEIRO = "id_vendedor_financeiro";
    private static final String ID_FINANCEIRO_APP = "id_financeiro_app";

    private static final String[] COLUNAS_FINANCEIRO = {
            CODIGO_FINANCEIRO,
            UNIDADE_FINANCEIRO,
            DATA_FINANCEIRO,
            CODIGO_CLIENTE_FINANCEIRO,
            FPAGAMENTO_FINANCEIRO,
            DOCUMENTO_FINANCEIRO,
            VENCIMENTO_FINANCEIRO,
            VALOR_FINANCEIRO,
            STATUS_AUTORIZACAO,
            PAGO,
            VASILHAME_REF,
            USUARIO_ATUAL_FINANCEIRO,
            DATA_INCLUSAO,
            NOSSO_NUMERO_FINANCEIRO,
            ID_VENDEDOR_FINANCEIRO,
            ID_FINANCEIRO_APP
    };

    //
    private FinanceiroVendasDomain cursorToFinanceiroVendasDomain(Cursor cursor) {
        FinanceiroVendasDomain financeiroVendasDomain = new FinanceiroVendasDomain(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        financeiroVendasDomain.setCodigo_financeiro(cursor.getString(0));
        financeiroVendasDomain.setUnidade_financeiro(cursor.getString(1));
        financeiroVendasDomain.setData_financeiro(cursor.getString(2));
        financeiroVendasDomain.setCodigo_cliente_financeiro(cursor.getString(3));
        financeiroVendasDomain.setFpagamento_financeiro(cursor.getString(4));
        financeiroVendasDomain.setDocumento_financeiro(cursor.getString(5));
        financeiroVendasDomain.setVencimento_financeiro(cursor.getString(6));
        financeiroVendasDomain.setValor_financeiro(cursor.getString(7));
        financeiroVendasDomain.setStatus_autorizacao(cursor.getString(8));
        financeiroVendasDomain.setPago(cursor.getString(9));
        financeiroVendasDomain.setVasilhame_ref(cursor.getString(10));
        financeiroVendasDomain.setUsuario_atual(cursor.getString(11));
        financeiroVendasDomain.setData_inclusao(cursor.getString(12));
        financeiroVendasDomain.setNosso_numero_financeiro(cursor.getString(13));
        financeiroVendasDomain.setId_vendedor_financeiro(cursor.getString(14));
        financeiroVendasDomain.setId_financeiro_app(cursor.getString(15));

        return financeiroVendasDomain;
    }

    //
    public void addFinanceiro(FinanceiroVendasDomain financeiroVendasDomain) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CODIGO_FINANCEIRO, financeiroVendasDomain.getCodigo_financeiro());
        values.put(UNIDADE_FINANCEIRO, financeiroVendasDomain.getUnidade_financeiro());
        values.put(DATA_FINANCEIRO, financeiroVendasDomain.getData_financeiro());
        values.put(CODIGO_CLIENTE_FINANCEIRO, financeiroVendasDomain.getCodigo_cliente_financeiro());
        values.put(FPAGAMENTO_FINANCEIRO, financeiroVendasDomain.getFpagamento_financeiro());
        values.put(DOCUMENTO_FINANCEIRO, financeiroVendasDomain.getDocumento_financeiro());
        values.put(VENCIMENTO_FINANCEIRO, financeiroVendasDomain.getVencimento_financeiro());
        values.put(VALOR_FINANCEIRO, financeiroVendasDomain.getValor_financeiro());
        values.put(STATUS_AUTORIZACAO, financeiroVendasDomain.getStatus_autorizacao());
        values.put(PAGO, financeiroVendasDomain.getPago());
        values.put(VASILHAME_REF, financeiroVendasDomain.getVasilhame_ref());
        values.put(USUARIO_ATUAL_FINANCEIRO, financeiroVendasDomain.getUsuario_atual());
        values.put(DATA_INCLUSAO, financeiroVendasDomain.getData_inclusao());
        values.put(NOSSO_NUMERO_FINANCEIRO, financeiroVendasDomain.getNosso_numero_financeiro());
        values.put(ID_VENDEDOR_FINANCEIRO, financeiroVendasDomain.getId_vendedor_financeiro());
        values.put(ID_FINANCEIRO_APP, financeiroVendasDomain.getId_financeiro_app());
        db.insert(TABELA_FINANCEIRO, null, values);
        db.close();
    }

    //
    public void addFinanceiroRecebidos(FinanceiroVendasDomain financeiroVendasDomain) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CODIGO_FINANCEIRO, financeiroVendasDomain.getCodigo_financeiro());
        values.put(UNIDADE_FINANCEIRO, financeiroVendasDomain.getUnidade_financeiro());
        values.put(DATA_FINANCEIRO, financeiroVendasDomain.getData_financeiro());
        values.put(CODIGO_CLIENTE_FINANCEIRO, financeiroVendasDomain.getCodigo_cliente_financeiro());
        values.put(FPAGAMENTO_FINANCEIRO, financeiroVendasDomain.getFpagamento_financeiro());
        values.put(DOCUMENTO_FINANCEIRO, financeiroVendasDomain.getDocumento_financeiro());
        values.put(VENCIMENTO_FINANCEIRO, financeiroVendasDomain.getVencimento_financeiro());
        values.put(VALOR_FINANCEIRO, financeiroVendasDomain.getValor_financeiro());
        values.put(STATUS_AUTORIZACAO, financeiroVendasDomain.getStatus_autorizacao());
        values.put(PAGO, financeiroVendasDomain.getPago());
        values.put(VASILHAME_REF, financeiroVendasDomain.getVasilhame_ref());
        values.put(USUARIO_ATUAL_FINANCEIRO, financeiroVendasDomain.getUsuario_atual());
        values.put(DATA_INCLUSAO, financeiroVendasDomain.getData_inclusao());
        values.put(NOSSO_NUMERO_FINANCEIRO, financeiroVendasDomain.getNosso_numero_financeiro());
        values.put(ID_VENDEDOR_FINANCEIRO, financeiroVendasDomain.getId_vendedor_financeiro());
        values.put(ID_FINANCEIRO_APP, financeiroVendasDomain.getId_financeiro_app());
        db.insert("recebidos", null, values);
        db.close();
    }


    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public ArrayList<FinanceiroVendasDomain> getFinanceiroCliente(int id_financeiro_app) {
        ArrayList<FinanceiroVendasDomain> listaFinanceiroVendas = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_FINANCEIRO + " WHERE id_financeiro_app = '" + id_financeiro_app + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FinanceiroVendasDomain financeiro = cursorToFinanceiroVendasDomain(cursor);
                listaFinanceiroVendas.add(financeiro);
            } while (cursor.moveToNext());
        }

        //db.close();
        return listaFinanceiroVendas;
    }


    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public ArrayList<FinanceiroVendasDomain> getFinanceiroClienteRecebidos(int id_financeiro_app) {
        ArrayList<FinanceiroVendasDomain> listaFinanceiroVendas = new ArrayList<>();

        String query = "SELECT * FROM " + "recebidos" + " WHERE id_financeiro_app = '" + id_financeiro_app + "'";
        Log.e("SQL: ", query);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FinanceiroVendasDomain financeiro = cursorToFinanceiroVendasDomain(cursor);
                listaFinanceiroVendas.add(financeiro);
            } while (cursor.moveToNext());
        }

        //db.close();
        return listaFinanceiroVendas;
    }


    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public ArrayList<FinanceiroReceberClientes> getContasReceberCliente(String id_cliente) {
        ArrayList<FinanceiroReceberClientes> listaFinanceiroVendas = new ArrayList<>();

        String query = "SELECT * FROM financeiro_receber WHERE codigo_cliente = '" + id_cliente + "' AND status_app = '1'" +
                " AND baixa_finalizada_app = '0'";
        Log.e("SQL: ", query);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FinanceiroReceberClientes financeiro = cursorToContasReceberCliente(cursor);
                listaFinanceiroVendas.add(financeiro);
            } while (cursor.moveToNext());
        }

        //db.close();
        return listaFinanceiroVendas;
    }

    //
    private FinanceiroReceberClientes cursorToContasReceberCliente(Cursor cursor) {
        FinanceiroReceberClientes clientes = new FinanceiroReceberClientes(null, null, null, null, null, null, null, null, null, null, null, null, null);
        //
        clientes.setCodigo_financeiro(cursor.getString(0));
        clientes.setNosso_numero_financeiro(cursor.getString(1));
        clientes.setData_financeiro(cursor.getString(2));
        clientes.setCodigo_cliente(cursor.getString(3));
        clientes.setNome_cliente(cursor.getString(4));
        clientes.setDocumento_financeiro(cursor.getString(5));
        clientes.setFpagamento_financeiro(cursor.getString(6));
        clientes.setVencimento_financeiro(cursor.getString(7));
        clientes.setValor_financeiro(cursor.getString(8));
        clientes.setTotal_pago(cursor.getString(9));
        clientes.setCodigo_pagamento(cursor.getString(10));
        clientes.setStatus_app(cursor.getString(11));
        clientes.setBaixa_finalizada_app(cursor.getString(12));
        return clientes;
    }

    //LISTAR TODOS OS CLIENTES
    public ArrayList<FinanceiroReceberClientes> getAllClientesContasReceber() {
        ArrayList<FinanceiroReceberClientes> listaClientes = new ArrayList<>();

        //
        String query = "SELECT * FROM financeiro_receber " +
                "INNER JOIN " + TABELA_CLIENTES + " ON " +
                TABELA_CLIENTES + "." + CODIGO_CLIENTE + " = financeiro_receber.codigo_cliente" +
                " WHERE status_app = '1'" +
                " GROUP BY " + TABELA_CLIENTES + "." + CODIGO_CLIENTE +
                " ORDER BY " + TABELA_CLIENTES + "." + NOME_CLIENTE;
        Log.e("SQL", query);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        //
        if (cursor.moveToFirst()) {
            do {
                FinanceiroReceberClientes clientes = cursorToContasReceberCliente(cursor);
                listaClientes.add(clientes);
            } while (cursor.moveToNext());
        }

        return listaClientes;
    }


    //ALTERAR CLIENTE
    public int updateFinanceiroReceber(String codigo_financeiro, String status, int id_baixa_app) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status_app", status);
        values.put("id_baixa_app", String.valueOf(id_baixa_app));

        int i = db.update(
                "financeiro_receber",
                values,
                "codigo_financeiro" + " = ?",
                new String[]{String.valueOf(codigo_financeiro)}
        );
        db.close();
        return i;
    }


    //ALTERAR CLIENTE
    public int updateFinalizarVenda(String codigo_venda_app) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(VENDA_FINALIZADA_APP, "1");

        int i = db.update(
                TABELA_VENDAS,
                values,
                CODIGO_VENDA_APP + " = ?",
                new String[]{String.valueOf(codigo_venda_app)}
        );
        db.close();
        return i;
    }

    //SOMAR O VALOR DO FINANCEIRO
    public String getValorTotalFinanceiro(String codigo_financeiro_app) {

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        String selectQuery = "SELECT SUM(valor_financeiro) FROM " + TABELA_FINANCEIRO + " WHERE " + ID_FINANCEIRO_APP + " = '" + codigo_financeiro_app + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        String total = "0.0";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return total;
    }

    //SOMAR O VALOR DO FINANCEIRO A RECEBER
    public String getValorTotalFinanceiroReceber(String codigo_financeiro_app) {

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        String selectQuery = "SELECT SUM(valor_financeiro) FROM " + "recebidos" + " WHERE " + ID_FINANCEIRO_APP + " = '" + codigo_financeiro_app + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);

        String total = "0.0";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return total;
    }

    //
    public int deleteFinanceiroRecebidos(int id_baixa_app) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                "recebidos",
                ID_FINANCEIRO_APP + " = ?",
                new String[]{String.valueOf(id_baixa_app)}
        );

        //
        updateFinanceiroReceber(id_baixa_app);

        //
        db.close();
        return i;
    }

    //
    private int updateFinanceiroReceber(int id_baixa_app) {
        SQLiteDatabase db = this.getWritableDatabase();

        //
        ContentValues values = new ContentValues();
        values.put("status_app", "1");
        values.put("id_baixa_app", "0");
        int a = 0;

        try {
            a = db.update(
                    "financeiro_receber",
                    values,
                    "id_baixa_app" + " = ?",
                    new String[]{String.valueOf(id_baixa_app)}
            );
        } catch (Exception e) {

        }

        //
        db.close();
        return a;
    }

    //
    public int deleteItemFinanceiro(FinanceiroVendasDomain financeiroVendasDomain) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                TABELA_FINANCEIRO,
                CODIGO_FINANCEIRO + " = ?",
                new String[]{String.valueOf(financeiroVendasDomain.getCodigo_financeiro())}
        );
        db.close();
        return i;
    }

    //
    public int deleteItemFinanceiroReceber(FinanceiroVendasDomain financeiroVendasDomain) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                "recebidos",
                CODIGO_FINANCEIRO + " = ?",
                new String[]{String.valueOf(financeiroVendasDomain.getCodigo_financeiro())}
        );
        db.close();
        return i;
    }

    //################### GERENCIAR VENDAS #####################
    //
    public int apagarVendasNaoFinalizadas(FinanceiroVendasDomain financeiroVendasDomain) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                TABELA_VENDAS,
                CODIGO_FINANCEIRO + " = ?",
                new String[]{String.valueOf(financeiroVendasDomain.getCodigo_financeiro())}
        );
        db.close();
        return i;
    }

    //
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return myDataBase.query(table, null, null, null, null, null, null);
    }
    */

}
