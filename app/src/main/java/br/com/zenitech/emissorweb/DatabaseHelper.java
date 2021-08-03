package br.com.zenitech.emissorweb;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
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
import br.com.zenitech.emissorweb.domains.FormaPagamentoPedido;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PedidosNFE;
import br.com.zenitech.emissorweb.domains.PedidosTemp;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.Produtos;
import br.com.zenitech.emissorweb.domains.StatusPedidos;
import br.com.zenitech.emissorweb.domains.StatusPedidosNFE;
import br.com.zenitech.emissorweb.domains.Unidades;

import static android.content.Context.MODE_PRIVATE;


public class DatabaseHelper extends SQLiteOpenHelper {

    private String TAG = "DatabaseHelper";
    private String DB_PATH;
    private static String DB_NAME = "emissorwebDB";
    private SQLiteDatabase myDataBase;
    final Context context;
    SharedPreferences prefs;
    ClassAuxiliar cAux;


    @SuppressLint("SdCardPath")
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 11);
        this.context = context;
        prefs = context.getSharedPreferences("preferencias", MODE_PRIVATE);
        cAux = new ClassAuxiliar();
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
        myDataBase = this.getWritableDatabase();
        String id = null;

        //
        Cursor produtos;
        String query_pos = String.format("SELECT codigo FROM produtos WHERE nome = '%s' LIMIT 1", produto);
        Cursor cursor = myDataBase.rawQuery(query_pos, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            id = cursor.getString(0);
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
    double getTributosProduto(String Produto, String ValTotal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ClassAuxiliar aux = new ClassAuxiliar();
        String tributo = "";
        String tot = String.valueOf(aux.converterValores(ValTotal));
        //
        Cursor produtos;
        String query_pos = "" +
                "SELECT (((pro.tributose + pro.tributosn + pro.tributosm) / 100) * " + tot + ") as tributos FROM produtos pro " +
                "WHERE pro.nome = '" + Produto + "' LIMIT 1";
        Log.e(TAG, query_pos);
        produtos = db.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {

                tributo = produtos.getString(produtos.getColumnIndex(TRIBUTOS_PRODUTO));

            } while (produtos.moveToNext());
        }

        return Double.parseDouble(tributo);
    }

    //
    double getTributosNProduto(String Produto, String ValTotal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ClassAuxiliar aux = new ClassAuxiliar();
        String tributo = "";
        String tot = String.valueOf(aux.converterValores(ValTotal));
        //
        Cursor produtos;
        String query_pos = "" +
                "SELECT (((pro.tributosn) / 100) * " + tot + ") as tributos FROM produtos pro " +
                "WHERE pro.nome = '" + Produto + "' LIMIT 1";
        Log.e(TAG, query_pos);
        produtos = db.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {

                tributo = produtos.getString(produtos.getColumnIndex(TRIBUTOS_PRODUTO));

            } while (produtos.moveToNext());
        }

        return Double.parseDouble(tributo);
    }

    //
    double getTributosEProduto(String Produto, String ValTotal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ClassAuxiliar aux = new ClassAuxiliar();
        String tributo = "";
        String tot = String.valueOf(aux.converterValores(ValTotal));
        //
        Cursor produtos;
        String query_pos = "" +
                "SELECT (((pro.tributose) / 100) * " + tot + ") as tributos FROM produtos pro " +
                "WHERE pro.nome = '" + Produto + "' LIMIT 1";
        Log.e(TAG, query_pos);
        produtos = db.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {

                tributo = produtos.getString(produtos.getColumnIndex(TRIBUTOS_PRODUTO));

            } while (produtos.moveToNext());
        }

        return Double.parseDouble(tributo);
    }

    //
    double getTributosMProduto(String Produto, String ValTotal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ClassAuxiliar aux = new ClassAuxiliar();
        String tributo = "";
        String tot = String.valueOf(aux.converterValores(ValTotal));
        //
        Cursor produtos;
        String query_pos = "" +
                "SELECT (((pro.tributosm) / 100) * " + tot + ") as tributos FROM produtos pro " +
                "WHERE pro.nome = '" + Produto + "' LIMIT 1";
        Log.e(TAG, query_pos);
        produtos = db.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {

                tributo = produtos.getString(produtos.getColumnIndex(TRIBUTOS_PRODUTO));

            } while (produtos.moveToNext());
        }

        return Double.parseDouble(tributo);
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
        ItensPedidos itensPedidos = new ItensPedidos(null, null, null, null, null);
        //
        itensPedidos.setPedido(cursor.getString(0));
        itensPedidos.setProduto(cursor.getString(1));
        itensPedidos.setQuantidade(cursor.getString(2));
        itensPedidos.setValor(cursor.getString(3));
        itensPedidos.setTotal(cursor.getString(4));
        return itensPedidos;
    }

    //
    ArrayList<ItensPedidos> getItensPedidoNFE(String nPedido) {
        String id = null;

        ArrayList<ItensPedidos> listaItensPedidos = new ArrayList<>();

        String query = "SELECT *, (valor * quantidade) AS total FROM itens_pedidosNFE WHERE pedido = '" + nPedido + "'";

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

        String query = "SELECT *, (valor * quantidade) AS total FROM itens_pedidos WHERE pedido = '" + nPedido + "'";

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
        values.put(ID_PEDIDOS, pedidos.getId());
        values.put(SITUACAO_PEDIDOS, pedidos.getSituacao());
        values.put(PROTOCOLO_PEDIDOS, pedidos.getProtocolo());
        values.put(DATA_PEDIDOS, pedidos.getData());
        values.put(HORA_PEDIDOS, pedidos.getHora());
        values.put(VALOR_TOTAL_PEDIDOS, pedidos.getValor_total());
        values.put(DATA_PROTOCOLO_PEDIDOS, pedidos.getData_protocolo());
        values.put(HORA_PROTOCOLO_PEDIDOS, pedidos.getHora_protocolo());
        values.put(CPF_CLIENTE_PEDIDOS, pedidos.getCpf_cliente());
        values.put(FORMA_PAGAMENTO_PEDIDOS, pedidos.getForma_pagamento());
        values.put("id_pedido_temp", pedidos.getId_pedido_temp());
        values.put("fracionado", pedidos.getFracionado());
        values.put("credenciadora", pedidos.getCredenciadora());
        db.insert(TABELA_PEDIDOS, null, values);
        db.close();
    }

    // ADD PEDIDO TEMPORARIO
    void addPedidosTemp(PedidosTemp pedidos) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ID_PEDIDOS, pedidos.getId());
        values.put(SITUACAO_PEDIDOS, pedidos.getSituacao());
        values.put(PROTOCOLO_PEDIDOS, pedidos.getProtocolo());
        values.put(DATA_PEDIDOS, pedidos.getData());
        values.put(HORA_PEDIDOS, pedidos.getHora());
        values.put(VALOR_TOTAL_PEDIDOS, pedidos.getValor_total());
        values.put(DATA_PROTOCOLO_PEDIDOS, pedidos.getData_protocolo());
        values.put(HORA_PROTOCOLO_PEDIDOS, pedidos.getHora_protocolo());
        values.put(CPF_CLIENTE_PEDIDOS, pedidos.getCpf_cliente());
        values.put(FORMA_PAGAMENTO_PEDIDOS, pedidos.getForma_pagamento());
        db.insert("pedidos_temp", null, values);
        db.close();
    }

    // ADD FORMAS PEDIDO TEMPORARIO
    void addFormasPagamentoPedidosTemp(FormaPagamentoPedido formaPagamentoPedido) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id_pedido", formaPagamentoPedido.id_pedido);
        values.put("id_forma_pagamento", formaPagamentoPedido.id_forma_pagamento);
        values.put("valor", formaPagamentoPedido.valor);
        values.put("codigo_autorizacao", formaPagamentoPedido.codigo_autorizacao);
        values.put("bandeira", formaPagamentoPedido.cardBrand);
        values.put("nsu", formaPagamentoPedido.codigo_autorizacao);
        db.insert("formas_pagamento_pedidos", null, values);
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
        Log.i(String.format("%s - getPedidosNFE()", TAG), query);

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
        Pedidos pedidos = new Pedidos(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
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
        pedidos.setAutorizacao(cursor.getString(10));
        pedidos.setId_pedido_temp(cursor.getString(11));
        pedidos.setFracionado(cursor.getString(12));
        pedidos.setCredenciadora(cursor.getString(13));
        return pedidos;
    }

    //LISTAR TODOS OS PEDIDOS
    public ArrayList<Pedidos> getPedidos() {
        ArrayList<Pedidos> listaPedidos = new ArrayList<>();

        String query = "SELECT * FROM pedidos ORDER BY id DESC";

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
        pedidos.setPedido(cursor.getString(10));
        pedidos.setProduto(cursor.getString(11));
        pedidos.setQuantidade(cursor.getString(12));
        pedidos.setValor(cursor.getString(13));
        pedidos.setNome(cursor.getString(14));
        return pedidos;
    }

    //LISTAR TODOS OS PEDIDOS
    public ArrayList<StatusPedidos> getStatusPedidos() {
        ArrayList<StatusPedidos> listaPedidos = new ArrayList<>();

        String query = "SELECT ped.id, ped.situacao, ped.protocolo, ped.data, ped.hora, ped.valor_total, ped.data_protocolo, ped.hora_protocolo, ped.cpf_cliente, ped.forma_pagamento, ipe.pedido, ipe.produto, \n" +
                "ipe.quantidade, ipe.valor, pro.nome FROM " + TABELA_PEDIDOS + " ped " +
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
        String total = "0.00", totalNFE = "0.00";

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
                Log.i("TOTAL", "TOTAL NFC-e = " + total);
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


        /*String[] qs = {
                (total != null ? String.valueOf(aux.converterValores(total)) : "0.00"),
                (totalNFE != null ? String.valueOf(aux.converterValores(totalNFE)) : "0.00")
        };*/

        String[] qs = {
                (total != null ? total : "0.00"),
                (totalNFE != null ? totalNFE : "0.00")
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

        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();
        ClassAuxiliar aux = new ClassAuxiliar();
        String total = "0", totalNFE = "0";


        try {

            String query = "SELECT SUM(ipe.quantidade) FROM " + TABELA_PEDIDOS + " ped " +
                    " INNER JOIN itens_pedidos ipe ON ipe.pedido = ped.id " +
                    " INNER JOIN produtos pro ON pro.codigo = ipe.produto " +
                    " ORDER BY " + ID_PEDIDOS + " DESC";

            Cursor cursor = myDataBase.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ///////////

        try {
            String queryNFE = "SELECT SUM(ipe.quantidade) FROM " + TABELA_PEDIDOS_NFE + " ped " +
                    " INNER JOIN itens_pedidosNFE ipe ON ipe.pedido = ped.id " +
                    " INNER JOIN produtos pro ON pro.codigo = ipe.produto " +
                    " ORDER BY ipe.pedido DESC";

            Cursor cursorNFE = myDataBase.rawQuery(queryNFE, null);
            if (cursorNFE.getCount() > 0) {
                cursorNFE.moveToFirst();
                totalNFE = cursorNFE.getString(0);
            }
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] qs = {(total != null ? total : "0"), (totalNFE != null ? totalNFE : "0")};
        String q = String.valueOf(aux.somar(qs));


        //db.endTransaction();
        //db.close();

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

        //String query = "SELECT * FROM pedidos WHERE situacao = 'OFF' OR situacao = ''  ORDER BY id DESC";
        String query = "SELECT * " +
                "FROM pedidos ped " +
                "inner join formas_pagamento_pedidos fpp on fpp.id_pedido = ped.id_pedido_temp " +
                "WHERE situacao = 'OFF' OR situacao = '' " +
                "GROUP BY ped.id " +
                "ORDER BY ped.id DESC ";

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
    public Boolean getVerificarFinanceiroUltimoPedido() {

        int totPad = 0;
        int totFin = 0;

        String query = "SELECT (" +
                "           SELECT itp.valor * itp.quantidade" +
                "             FROM itens_pedidos itp" +
                "            WHERE itp.pedido = ped.id_pedido_temp" +
                "       )" +
                "       valor_total," +
                "       (" +
                "           SELECT SUM(fpp.valor) * 100" +
                "             FROM formas_pagamento_pedidos fpp" +
                "            WHERE fpp.id_pedido = ped.id_pedido_temp" +
                "       )" +
                "       val_financeiro" +
                "  FROM pedidos ped" +
                " WHERE situacao = 'OFF' OR " +
                "       situacao = ''" +
                " ORDER BY id DESC" +
                " LIMIT 1";

        //Log.e("FinanceiroUltimoPedido", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                totPad = Integer.parseInt(cursor.getString(cursor.getColumnIndex("valor_total")));
                totFin = Integer.parseInt(cursor.getString(cursor.getColumnIndex("val_financeiro")));
            } while (cursor.moveToNext());
        }

        if (totPad > 0) {
            if (totPad != totFin) {
                return true;
            }
        }

        return false;
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
        String chave_cUF = null;                // Código da UF do emitente do Documento Fiscal
        String chave_AAMM = aux.anoMesAtual();  // Ano e Mês de emissão da NF-e
        String chave_CNPJ = null;               // CNPJ do emitente
        String chave_mod = "65";                // Modelo do Documento Fiscal
        String chave_serie = null;              // Série do Documento Fiscal
        String chave_nNF = null;                // Número do Documento Fiscal
        String chave_tpEmis = "1";              // Forma de emissão da NF-e
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
        String selectQuery = "SELECT * From credenciadoras GROUP BY cnpj_credenciadora";
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

    ArrayList<String> getIdCredenciadora() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String selectQuery = "SELECT codigo_credenciadora From credenciadoras GROUP BY cnpj_credenciadora";
        Cursor cursor = db.rawQuery(selectQuery, null);
        list.add("CREDENCIADORA");
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String nome = cursor.getString(cursor.getColumnIndex("codigo_credenciadora"));
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

    //CURSOR POS
    private FormaPagamentoPedido cursorFormaPagamentoPedido(Cursor cursor) {
        FormaPagamentoPedido formaPagamentoPedido = new FormaPagamentoPedido(null, null, null, null, null, null, null);

        //
        formaPagamentoPedido.setId(cursor.getString(0));
        formaPagamentoPedido.setId_pedido(cursor.getString(1));
        formaPagamentoPedido.setId_forma_pagamento(cursor.getString(2));
        formaPagamentoPedido.setValor(cursor.getString(3));
        formaPagamentoPedido.setCodigo_autorizacao(cursor.getString(4));
        formaPagamentoPedido.setCardBrand(cursor.getString(5));
        formaPagamentoPedido.setNsu(cursor.getString(6));
        return formaPagamentoPedido;
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public ArrayList<FormaPagamentoPedido> getFinanceiroCliente(int id_pedido) {
        ArrayList<FormaPagamentoPedido> listaFinanceiroVendas = new ArrayList<>();

        String query = "SELECT * FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        Log.e("SQL", "getFinanceiroCliente - " + query);
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                FormaPagamentoPedido formaPagamentoPedido = cursorFormaPagamentoPedido(cursor);
                listaFinanceiroVendas.add(formaPagamentoPedido);
            } while (cursor.moveToNext());
        }

        //db.close();
        return listaFinanceiroVendas;
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT id_forma_pagamento FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }
        return formasPag.toString();
    }

    //LISTAR FORMAS PAGAMENTO PEDIDO
    public String getFormasPagamentoPedidoPrint(String id_pedido) {
        ClassAuxiliar aux = new ClassAuxiliar();
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT id_forma_pagamento FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(aux.getNomeFormaPagamento(cursor.getString(0))).append(",");
            } while (cursor.moveToNext());
        }
        return formasPag.toString();
    }

    /*public String getNomeFormaPagamento(String id_pedido) {
        String formasPag = "";

        String query = "SELECT id_forma_pagamento FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        return formasPag;
    }*/

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getValoresFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT valor * 100 FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }
        return formasPag.toString();
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getIdFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT id_forma_pagamento FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }
        return formasPag.toString();
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getAutorizacaoFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT codigo_autorizacao FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }
        return formasPag.toString();
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getBandeiraFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT bandeira FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }
        return formasPag.toString();
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getNSUFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT nsu FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }
        return formasPag.toString();
    }

    //
    public int deleteItemFormPagPedido(FormaPagamentoPedido formaPagamentoPedido) {
        SQLiteDatabase db = this.getWritableDatabase();

        int i = db.delete(
                "formas_pagamento_pedidos",
                "id = ?",
                new String[]{String.valueOf(formaPagamentoPedido.getId())}
        );
        db.close();
        return i;
    }

    //SOMAR O VALOR DO FINANCEIRO
    public String getValorTotalFinanceiro(String idPedido) {

        /*SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        String selectQuery = "SELECT SUM(valor) FROM formas_pagamento_pedidos WHERE id_pedido = '" + idPedido + "'";

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
        }*/

        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();

        String selectQuery = "SELECT SUM(valor) FROM formas_pagamento_pedidos WHERE id_pedido = '" + idPedido + "'";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "0.0";

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            total = cursor.getString(0);
        }
        if (total == null) {
            total = "0.0";
        }
        return total;
    }

    //SOMAR O VALOR DO FINANCEIRO
    public int getProximoIdPedido() {

        int id = 1;
        myDataBase = this.getReadableDatabase();
        String selectQuery;

        // SE FOR O PRIMEIRO PEDIDO PEGAR O ULTIMO ID NA TABELA POS
        /*if (prefs.getBoolean("primeiro_pedido", true)) {
            selectQuery = "SELECT (p.ultnfce + 1) id FROM pos p ORDER BY  p.codigo DESC LIMIT 1";
        } else {
            selectQuery = "SELECT (ped.id + 1) id FROM pedidos ped ORDER BY  ped.id DESC LIMIT 1";
        }*/

        if (getUltimoIdPedido().equalsIgnoreCase("")) {
            selectQuery = "SELECT (p.ultnfce + 1) id FROM pos p ORDER BY  p.codigo DESC LIMIT 1";
        } else {
            selectQuery = "SELECT (ped.id + 1) id FROM pedidos ped ORDER BY  ped.id DESC LIMIT 1";
        }
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            id = Integer.parseInt(cursor.getString(0));
        }

        return id;
    }

    //SOMAR O VALOR DO FINANCEIRO
    public String getUltimoIdPedido() {

        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();
        //pedidos_temp
        String selectQuery = "SELECT ped.id FROM pedidos_temp ped ORDER BY  ped.id DESC LIMIT 1";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }

    //PEGA O ULTIMO ID DA TABELA DE PEDIDOS
    public String getUltimoIdPedidos() {

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        //pedidos_temp
        String selectQuery = "SELECT ped.id FROM pedidos ped ORDER BY  ped.id DESC LIMIT 1";

        Cursor cursor = db.rawQuery(selectQuery, null);

        String total = "";
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

    //SOMAR O VALOR DO FINANCEIRO
    public String getUltimaNotaPOS() {

        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();

        String selectQuery = "SELECT ultnfce FROM pos LIMIT 1";

        Cursor cursor = db.rawQuery(selectQuery, null);

        String total = "";
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

    //LISTAR TODOS OS PEDIDOS A TRANSMITIR
    public void listPedidosSemPagamento() {

        String query = "SELECT ped.id, fpp.id " +
                "FROM pedidos ped " +
                "LEFT JOIN formas_pagamento_pedidos fpp ON fpp.id_pedido = ped.id_pedido_temp " +
                "WHERE ped.situacao = 'OFF' OR ped.situacao = '' " +
                "ORDER BY ped.id DESC";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getCount() > 0) {
                    if (cursor.getString(1) == null) {
                        apagarPedidoSemPagamento(cursor.getString(0));
                        prefs.edit().putInt("id_pedido", prefs.getInt("id_pedido", 0) - 1).apply();
                    }
                }
            } while (cursor.moveToNext());
        }
    }

    private void apagarPedidoSemPagamento(String idPedido) {
        myDataBase = this.getWritableDatabase();

        //
        myDataBase.delete(
                "itens_pedidos",
                "pedido = ?",
                new String[]{idPedido}
        );

        //
        myDataBase.delete(
                "pedidos_temp",
                "id = ?",
                new String[]{idPedido}
        );

        //
        myDataBase.delete(
                "pedidos",
                "id = ?",
                new String[]{idPedido}
        );


        /*int i = myDataBase.delete(
                "pedidos",
                ID_PEDIDOS + " = ?",
                new String[]{idPedido}
        );
        db.close();
        return i;*/
    }

    //ULTIMO PEDIDO
    public Pedidos ultimoPedido() {

        Pedidos pedidos = null;
        String query = "SELECT * FROM Pedidos ped ORDER BY ped.id DESC LIMIT 1";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                pedidos = cursorToPedidos(cursor);
            } while (cursor.moveToNext());
        }

        return pedidos;
    }

}
