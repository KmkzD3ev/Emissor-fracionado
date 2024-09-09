package br.com.zenitech.emissorweb;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.zenitech.emissorweb.domains.Autorizacoes;
import br.com.zenitech.emissorweb.domains.AutorizacoesPinpad;
import br.com.zenitech.emissorweb.domains.FinanceiroNFeDomain;
import br.com.zenitech.emissorweb.domains.FormaPagamentoPedido;
import br.com.zenitech.emissorweb.domains.ItensPedidos;
import br.com.zenitech.emissorweb.domains.Pedidos;
import br.com.zenitech.emissorweb.domains.PedidosNFE;
import br.com.zenitech.emissorweb.domains.PosApp;
import br.com.zenitech.emissorweb.domains.PrintPixDomain;
import br.com.zenitech.emissorweb.domains.Produtos;
import br.com.zenitech.emissorweb.domains.ProdutosDescricaoNFCe;
import br.com.zenitech.emissorweb.domains.ProdutosPedidoDomain;
import br.com.zenitech.emissorweb.domains.StatusPedidos;
import br.com.zenitech.emissorweb.domains.StatusPedidosNFE;
import br.com.zenitech.emissorweb.domains.Unidades;

import static android.content.Context.MODE_PRIVATE;


public class DatabaseHelper extends SQLiteOpenHelper {

    private final String TAG = "DatabaseHelper";
    private final String DB_PATH;
    private static final String DB_NAME = "emissorwebDB";
    private SQLiteDatabase myDataBase;
    private final Context context;
    private final SharedPreferences prefs;
    //private ClassAuxiliar cAux;


    //@SuppressLint("SdCardPath")
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 12);
        this.context = context;
        prefs = context.getSharedPreferences("preferencias", MODE_PRIVATE);
        //cAux = new ClassAuxiliar();
        //this.DB_PATH = context.getFilesDir().getPath() + "/" + context.getPackageName() + "/" + "databases/";
        //this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
        this.DB_PATH = context.getDatabasePath(DB_NAME).getPath();
        /*if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            this.DB_PATH = context.getDatabasePath(DB_NAME).getPath();// + File.separator
            //this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";

        } else {
            //String DB_PATH = Environment.getDataDirectory() + "/data/my.trial.app/databases/";
            //myPath = DB_PATH + dbName;
            this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
        }*/

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
        } else {

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
        String outFileName = DB_PATH;// + DB_NAME;
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
        String myPath = DB_PATH;// + DB_NAME;
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

        cursor.close();

        return listaProdutos;
    }

    ArrayList<String> getProdutos() {
        ArrayList<String> list = new ArrayList<>();
        myDataBase = this.getReadableDatabase();
        String selectQuery = "Select * From " + TABELA_PRODUTOS + " ORDER BY nome";
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        list.add("PRODUTO");
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));
                    list.add(nome);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cursor.close();

        return list;
    }

    String getProduto(String id) {
        String produto = null;
        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();
        String selectQuery = "Select " + NOME_PRODUTO + " From " + TABELA_PRODUTOS + " where " + CODIGO_PRODUTO + " = '" + id + "'";
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    produto = cursor.getString(cursor.getColumnIndexOrThrow(NOME_PRODUTO));
                }
            }
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        cursor.close();

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

        cursor.close();

        return id;
    }

    //
    public boolean getProdutoNcmGas(String produto) {
        myDataBase = this.getWritableDatabase();

        //
        Cursor produtos;
        String query_pos = "SELECT pro.nome " +
                " FROM produtos pro " +
                "WHERE pro.nome = '" + produto + "' AND pro.ncm = '27111910' LIMIT 1";
        produtos = myDataBase.rawQuery(query_pos, null);
        if (produtos.getCount() > 0) {
            //produtos.moveToFirst();
            produtos.close();
            return true;
        }
        produtos.close();
        return false;
    }

    //
    public boolean getProdutoNcmOutros(String produto) {
        myDataBase = this.getWritableDatabase();

        //
        Cursor produtos;
        String query_pos = "SELECT pro.nome " +
                " FROM produtos pro " +
                "WHERE pro.nome = '" + produto + "' AND pro.ncm != '27111910' AND pro.ncm != '' LIMIT 1";
        produtos = myDataBase.rawQuery(query_pos, null);
        if (produtos.getCount() > 0) {
            //produtos.moveToFirst();
            produtos.close();
            return true;
        }
        produtos.close();
        return false;
    }

    //
    public String getNomeProduto(String idProduto) {
        myDataBase = this.getWritableDatabase();
        String id = null;

        //
        Cursor produtos;
        String query_pos = "SELECT " + NOME_PRODUTO + " FROM " + TABELA_PRODUTOS + " WHERE " + CODIGO_PRODUTO + " = '" + idProduto + "' LIMIT 1";
        produtos = myDataBase.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {

                id = produtos.getString(produtos.getColumnIndexOrThrow(NOME_PRODUTO));

            } while (produtos.moveToNext());
        }

        produtos.close();

        return id;
    }

    //
    public String getTributosProduto(String Produto, String ValTotal) {
        myDataBase = this.getWritableDatabase();
        String tributo = "0.00";
        Cursor produtos;
        String query_pos = "SELECT (((pro.tributose + pro.tributosn + pro.tributosm) / 100) * " + ValTotal + ") as tributos FROM produtos pro " +
                "WHERE pro.nome = '" + Produto + "' LIMIT 1";
        Log.e(TAG, query_pos);
        produtos = myDataBase.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            tributo = produtos.getString(produtos.getColumnIndexOrThrow(TRIBUTOS_PRODUTO));
        }
        produtos.close();

        return tributo;
    }

    //
    double[] getTributosProdutosPedido(String idPedido, String ValTotal) {
        myDataBase = this.getWritableDatabase();
        ClassAuxiliar aux = new ClassAuxiliar();
        double[] tributos = {0.0, 0.0, 0.0, 0.0};
        String tributo = "";
        //
        Cursor produtos;
        String query_pos = "SELECT " +
                "SUM((((pro.tributose + pro.tributosn + pro.tributosm) / 100) * " + ValTotal + ")) as tributos, " +
                "SUM((((pro.tributosn) / 100) * " + ValTotal + ")) as tributoN, " +
                "SUM((((pro.tributose) / 100) * " + ValTotal + ")) as tributoE, " +
                "SUM((((pro.tributosm) / 100) * " + ValTotal + ")) as tributoM " +
                "FROM itens_pedidos ipe " +
                "INNER JOIN produtos pro ON pro.codigo = ipe.produto " +
                "WHERE ipe.pedido = '" + idPedido + "'";
        Log.e(TAG, query_pos);
        produtos = myDataBase.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {
                tributos[0] = Double.parseDouble(produtos.getString(produtos.getColumnIndexOrThrow("tributos")));
                tributos[1] = Double.parseDouble(produtos.getString(produtos.getColumnIndexOrThrow("tributoN")));
                tributos[2] = Double.parseDouble(produtos.getString(produtos.getColumnIndexOrThrow("tributoE")));
                tributos[3] = Double.parseDouble(produtos.getString(produtos.getColumnIndexOrThrow("tributoM")));
            } while (produtos.moveToNext());
        }

        produtos.close();

        return tributos;
    }

    //
    public String getTributosNProduto(String Produto, String ValTotal) {
        myDataBase = this.getWritableDatabase();
        String tributo = "0.00";
        Cursor produtos;
        String query_pos = "SELECT (((pro.tributosn) / 100) * " + ValTotal + ") as tributos FROM produtos pro " + "WHERE pro.nome = '" + Produto + "' LIMIT 1";
        Log.e(TAG, query_pos);
        produtos = myDataBase.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            if (!produtos.getString(produtos.getColumnIndexOrThrow(TRIBUTOS_PRODUTO)).isEmpty()) {
                tributo = produtos.getString(produtos.getColumnIndexOrThrow(TRIBUTOS_PRODUTO));
            }
        }
        produtos.close();

        return tributo;
    }

    //
    public String getTributosEProduto(String Produto, String ValTotal) {
        myDataBase = this.getWritableDatabase();
        String tributo = "0.00";
        Cursor produtos;
        String query_pos = "SELECT (((pro.tributose) / 100) * " + ValTotal + ") as tributos FROM produtos pro " + "WHERE pro.nome = '" + Produto + "' LIMIT 1";
        Log.e(TAG, query_pos);
        produtos = myDataBase.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            if (!produtos.getString(produtos.getColumnIndexOrThrow(TRIBUTOS_PRODUTO)).isEmpty()) {
                tributo = produtos.getString(produtos.getColumnIndexOrThrow(TRIBUTOS_PRODUTO));
            }
        }
        produtos.close();

        return tributo;
    }

    //
    public String getTributosMProduto(String Produto, String ValTotal) {
        myDataBase = this.getWritableDatabase();
        String tributo = "0.00";
        Cursor produtos;
        String query_pos = "SELECT (((pro.tributosm) / 100) * " + ValTotal + ") as tributos FROM produtos pro " + "WHERE pro.nome = '" + Produto + "' LIMIT 1";
        Log.e(TAG, query_pos);
        produtos = myDataBase.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            if (!produtos.getString(produtos.getColumnIndexOrThrow(TRIBUTOS_PRODUTO)).isEmpty()) {
                tributo = produtos.getString(produtos.getColumnIndexOrThrow(TRIBUTOS_PRODUTO));
            }
        }
        produtos.close();

        return tributo;
    }

    //
    double getPrecoMinimoProduto(String Produto) {
        SQLiteDatabase db = this.getWritableDatabase();
        String tributo = "";

        //
        Cursor produtos;
        String query_pos = "SELECT " + VALOR_MINIMO_PRODUTO + " FROM " + TABELA_PRODUTOS + " WHERE " + NOME_PRODUTO + " = '" + Produto + "' LIMIT 1";
        produtos = db.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {

                tributo = produtos.getString(produtos.getColumnIndexOrThrow(VALOR_MINIMO_PRODUTO));

            } while (produtos.moveToNext());
        }

        produtos.close();

        //Log.i("TOTAL= ", tributo);

        //String t = String.valueOf(aux.converterValores(tributo));
        //String t = tributo;

        return Double.parseDouble(tributo);
    }

    //
    double getPrecoMaximoProduto(String Produto) {
        myDataBase = this.getWritableDatabase();
        String tributo = "";

        //
        Cursor produtos;
        String query_pos = "SELECT " + VALOR_MAXIMO_PRODUTO + " FROM " + TABELA_PRODUTOS + " WHERE " + NOME_PRODUTO + " = '" + Produto + "' LIMIT 1";
        produtos = myDataBase.rawQuery(query_pos, null);
        if (produtos.moveToFirst()) {
            do {

                tributo = produtos.getString(produtos.getColumnIndexOrThrow(VALOR_MAXIMO_PRODUTO));

            } while (produtos.moveToNext());
        }

        produtos.close();

        return Double.parseDouble(tributo);
    }

    //
    String getPrecoProduto(String Produto) {
        myDataBase = this.getWritableDatabase();
        String valor = "0";

        try {
            //
            Cursor produtos;
            String query_pos = "SELECT valor FROM " + TABELA_PRODUTOS + " WHERE " + NOME_PRODUTO + " = '" + Produto + "' LIMIT 1";
            produtos = myDataBase.rawQuery(query_pos, null);
            if (produtos.moveToFirst()) {
                valor = produtos.getString(produtos.getColumnIndexOrThrow("valor"));
            }

            produtos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return valor;
    }

    //
    int getQuantProdutoRemessa(String Produto) {
        myDataBase = this.getWritableDatabase();
        ClassAuxiliar aux = new ClassAuxiliar();
        String quatProd = "0", quantInt = "0", quantIntNFE = "0";

        try {
            //
            Cursor produtos;
            String query_pos = "SELECT " + QTD_REVENDA + " FROM " + TABELA_PRODUTOS + " WHERE " + NOME_PRODUTO + " = '" + Produto + "' LIMIT 1";
            produtos = myDataBase.rawQuery(query_pos, null);
            if (produtos.moveToFirst()) {
                do {
                    quatProd = produtos.getString(produtos.getColumnIndexOrThrow(QTD_REVENDA));
                } while (produtos.moveToNext());
            }

            produtos.close();
        } catch (Exception ignored) {
        }

        try {
            //
            Cursor intProdutos;
            String query_intPro = "SELECT SUM(quantidade) quantidade FROM itens_pedidos WHERE produto = " + getIdProduto(Produto);
            Log.i("SQL_BD", query_intPro);
            intProdutos = myDataBase.rawQuery(query_intPro, null);
            if (intProdutos.moveToFirst()) {
                do {
                    quantInt = intProdutos.getString(intProdutos.getColumnIndexOrThrow("quantidade"));
                } while (intProdutos.moveToNext());
            }

            intProdutos.close();
        } catch (Exception ignored) {
        }

        try {
            //
            Cursor intProdutosNFE;
            String query_intProNFE = "SELECT SUM(quantidade) quantidade FROM itens_pedidosNFE WHERE produto = " + getIdProduto(Produto);
            Log.i("SQL_BD", query_intProNFE);
            intProdutosNFE = myDataBase.rawQuery(query_intProNFE, null);
            if (intProdutosNFE.moveToFirst()) {
                do {
                    quantIntNFE = intProdutosNFE.getString(intProdutosNFE.getColumnIndexOrThrow("quantidade"));
                } while (intProdutosNFE.moveToNext());
            }

            intProdutosNFE.close();
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
        ItensPedidos itensPedidos = new ItensPedidos(null, "", null, null, null, null);
        //
        itensPedidos.setPedido(cursor.getString(0));
        itensPedidos.setProduto(cursor.getString(1));
        itensPedidos.setQuantidade(cursor.getString(2));
        itensPedidos.setValor(cursor.getString(3));
        try {
            itensPedidos.setDesconto(cursor.getString(4));
        } catch (Exception ignored) {
        }
        itensPedidos.setTotal(cursor.getString(5));
        return itensPedidos;
    }

    //
    ArrayList<ItensPedidos> getItensPedidoNFE(String nPedido) {
        //String id = null;

        ArrayList<ItensPedidos> listaItensPedidos = new ArrayList<>();

        String query = "SELECT nfe.pedido, nfe.produto, nfe.quantidade,nfe.valor / 100 AS valor, '' AS desconto, (valor * quantidade) / 100 AS total FROM itens_pedidosNFE nfe WHERE nfe.pedido = '" + nPedido + "'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                ItensPedidos itensPedidos = cursorToItensPedidos(cursor);
                listaItensPedidos.add(itensPedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaItensPedidos;
    }

    //
    ArrayList<ItensPedidos> getItensPedido(String nPedido) {
        //String id = null;

        ArrayList<ItensPedidos> listaItensPedidos = new ArrayList<>();

        String query = "SELECT pedido, produto, quantidade, valor, (desconto * quantidade) AS desconto, (valor * quantidade) - (desconto * quantidade) AS total " +
                "FROM itens_pedidos WHERE pedido = '" + nPedido + "'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                //ItensPedidos itensPedidos = cursorToItensPedidos(cursor);
                listaItensPedidos.add(cursorToItensPedidos(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaItensPedidos;
    }

    //
    ArrayList<ItensPedidos> getItensPedidoTransmitir(String nPedido) {
        //String id = null;

        ArrayList<ItensPedidos> listaItensPedidos = new ArrayList<>();

        String query = "SELECT pedido, produto, quantidade, valor * 100 AS valor, (desconto * quantidade) AS desconto, (valor * quantidade) - (desconto * quantidade) AS total FROM itens_pedidos WHERE pedido = '" + nPedido + "'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                //ItensPedidos itensPedidos = cursorToItensPedidos(cursor);
                listaItensPedidos.add(cursorToItensPedidos(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaItensPedidos;
    }

    //
    String testeCampoDesconto() {
        String id = "";

        String query = "SELECT SUM(desconto) AS desconto FROM itens_pedidos";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);
        cursor.close();

        return id;
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
        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();
        String id = "0";

        try {

            String query = "SELECT aut.id FROM " + TABELA_AUTORIZACOES + " aut " +
                    " ORDER BY aut.id DESC";

            Cursor cursor = myDataBase.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(0);
            }

            cursor.close();
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //db.endTransaction();
        //db.close();

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
        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();
        String id = "0";

        try {

            String query = "SELECT aut.id FROM " + TABELA_AUTORIZACOES_PINPAD + " aut " +
                    " ORDER BY aut.id DESC LIMIT 1";

            Cursor cursor = myDataBase.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(0);
            }

            cursor.close();
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*db.endTransaction();
        db.close();*/

        return id;
    }

    //ULTIMA NFC-E EMITIDA
    public AutorizacoesPinpad getAutorizacaoPinpad() {
        myDataBase = this.getReadableDatabase();

        AutorizacoesPinpad autorizacoesPinpad = null;// new AutorizacoesPinpad(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        try {

            String query = "SELECT * FROM " + TABELA_AUTORIZACOES_PINPAD + " aut " +
                    " ORDER BY aut.id DESC LIMIT 1";

            Cursor cursor = myDataBase.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                autorizacoesPinpad = new AutorizacoesPinpad(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

                autorizacoesPinpad = cursorToAutorizacoesPinpad(cursor);
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
    private String[] COLUNAS_PEDIDOS = {
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
        try {
            myDataBase = this.getWritableDatabase();

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
            myDataBase.insert(TABELA_PEDIDOS, null, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    // ADD PEDIDO TEMPORARIO
    void addPedidosTemp(String idPedido) {
        try {
            myDataBase = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(ID_PEDIDOS, idPedido);
            values.put(SITUACAO_PEDIDOS, "OFF");
            values.put(PROTOCOLO_PEDIDOS, "");
            values.put(DATA_PEDIDOS, "");
            values.put(HORA_PEDIDOS, "");
            values.put(VALOR_TOTAL_PEDIDOS, "");
            values.put(DATA_PROTOCOLO_PEDIDOS, "");
            values.put(HORA_PROTOCOLO_PEDIDOS, "");
            values.put(CPF_CLIENTE_PEDIDOS, "");
            values.put(FORMA_PAGAMENTO_PEDIDOS, "");
            myDataBase.insert("pedidos_temp", null, values);
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    // ADD FORMAS PEDIDO TEMPORARIO
    void addFormasPagamentoPedidosTemp(FormaPagamentoPedido formaPagamentoPedido) {
        String valor = getFormasPagamento(formaPagamentoPedido.id_pedido, formaPagamentoPedido.id_forma_pagamento);
        if (!valor.equals("")) {
            ClassAuxiliar cAux = new ClassAuxiliar();
            String[] somar = {valor, formaPagamentoPedido.valor};
            String total = String.valueOf(cAux.somar(somar));
            upadteFormaPagamentoFinanceiroNfce(total, formaPagamentoPedido.id_forma_pagamento, formaPagamentoPedido.id_pedido);
        } else {

            try {
                myDataBase = this.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put("id_pedido", formaPagamentoPedido.id_pedido);
                values.put("id_forma_pagamento", formaPagamentoPedido.id_forma_pagamento);
                values.put("valor", formaPagamentoPedido.valor);
                values.put("codigo_autorizacao", formaPagamentoPedido.codigo_autorizacao);
                values.put("bandeira", formaPagamentoPedido.cardBrand);
                values.put("nsu", formaPagamentoPedido.codigo_autorizacao);
                values.put("id_cobranca_pix", formaPagamentoPedido.id_cobranca_pix);
                values.put("status_pix", formaPagamentoPedido.status_pix);
                myDataBase.insert("formas_pagamento_pedidos", null, values);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private String getFormasPagamento(String id_pedido, String idFpg) {
        String valor = "";

        String query = "SELECT valor " +
                "FROM formas_pagamento_pedidos " +
                "WHERE id_pedido = '" + id_pedido + "' AND id_forma_pagamento = '" + idFpg + "'";
        Log.e("SQL", "getProdutosPedido - " + query);
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            valor = cursor.getString(0);
        }

        cursor.close();
        return valor;
    }

    public void upadteFormaPagamentoFinanceiroNfce(
            String valor,
            String id,
            String idPedTemp) {
        try {
            myDataBase = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("valor", valor);
            myDataBase.update("formas_pagamento_pedidos", values, "id_forma_pagamento=" + id + " AND id_pedido=" + idPedTemp, null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    void addItensPedidos(ItensPedidos itensPedidos) {
        try {
            myDataBase = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("pedido", itensPedidos.getPedido());
            values.put("produto", itensPedidos.getProduto());
            values.put("quantidade", itensPedidos.getQuantidade());
            values.put("valor", itensPedidos.getValor());
            values.put("desconto", itensPedidos.getDesconto());
            myDataBase.insert("itens_pedidos", null, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    void addProdutoPedido(ProdutosPedidoDomain produto) {
        try {
            myDataBase = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("id_pedido", produto.id_pedido);
            values.put("id_produto", this.getIdProduto(produto.produto));
            values.put("quantidade", produto.quantidade);
            values.put("valor", produto.valor);
            values.put("total", produto.total);
            values.put("produto", produto.produto);
            try {
                values.put("desconto", produto.desconto);
            } catch (Exception e) {
                e.printStackTrace();
            }
            myDataBase.insert("produtos_pedido", null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ADICIONAR FINANCEIRO PEDIDO NFCe
    public void addFinanceiroNFCe(FormaPagamentoPedido formaPagamentoPedido) {
        try {
            myDataBase = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("id_pedido", formaPagamentoPedido.id_pedido);
            values.put("id_forma_pagamento", formaPagamentoPedido.id_forma_pagamento);
            values.put("valor", formaPagamentoPedido.valor);
            values.put("codigo_autorizacao", formaPagamentoPedido.codigo_autorizacao);
            values.put("bandeira", formaPagamentoPedido.cardBrand);
            values.put("nsu", formaPagamentoPedido.codigo_autorizacao);
            values.put("id_cobranca_pix", formaPagamentoPedido.id_cobranca_pix);
            values.put("status_pix", formaPagamentoPedido.status_pix);
            myDataBase.insert("financeiro_nfce", null, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    void addProdutoPedidoNFe(ProdutosPedidoDomain produto) {
        try {
            myDataBase = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("id_pedido", produto.id_pedido);
            values.put("id_produto", this.getIdProduto(produto.produto));
            values.put("quantidade", produto.quantidade);
            values.put("valor", produto.valor);
            values.put("total", produto.total);
            values.put("produto", produto.produto);
            try {
                values.put("desconto", produto.desconto);
            } catch (Exception e) {
                e.printStackTrace();
            }
            myDataBase.insert("produtos_pedido_nfe", null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ProdutosPedidoDomain cursorProdutoPedido(Cursor cursor) {
        ProdutosPedidoDomain produto = new ProdutosPedidoDomain();

        //
        produto.id = cursor.getString(0);
        produto.id_pedido = cursor.getString(1);
        produto.id_produto = cursor.getString(2);
        produto.quantidade = cursor.getString(3);
        produto.valor = cursor.getString(4);
        produto.total = cursor.getString(5);
        produto.produto = cursor.getString(6);
        try {
            produto.desconto = cursor.getString(7);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return produto;
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public ArrayList<ProdutosPedidoDomain> getProdutosPedido(int id_pedido) {
        ArrayList<ProdutosPedidoDomain> produtos = new ArrayList<>();

        String query = "SELECT * FROM produtos_pedido WHERE id_pedido = '" + id_pedido + "'";
        Log.e("SQL", "getProdutosPedido - " + query);
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                produtos.add(cursorProdutoPedido(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return produtos;
    }

    public ProdutosPedidoDomain getProdutoPedidoTemp(int id_pedido, String produto) {
        ProdutosPedidoDomain produtoPedido = new ProdutosPedidoDomain();

        try {
            String idProduto = this.getIdProduto(produto);
            String query = "SELECT id_produto, quantidade, valor " +
                    "FROM produtos_pedido " +
                    "WHERE id_pedido = '" + id_pedido + "' AND id_produto = '" + idProduto + "'";
            Log.e("SQL", "getProdutosPedido - " + query);
            myDataBase = this.getReadableDatabase();
            Cursor cursor = myDataBase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                produtoPedido.id_produto = cursor.getString(0);
                produtoPedido.quantidade = cursor.getString(1);
                produtoPedido.valor = cursor.getString(2);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return produtoPedido;
    }

    public ArrayList<ProdutosPedidoDomain> getProdutosPedidoNFe(String id_pedido) {
        ArrayList<ProdutosPedidoDomain> produtos = new ArrayList<>();

        String query = "SELECT * FROM produtos_pedido_nfe WHERE id_pedido = '" + id_pedido + "'";
        Log.e("SQL", "getProdutosPedido - " + query);
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                produtos.add(cursorProdutoPedido(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return produtos;
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getProdutosPedidoConfirmacao(int id_pedido, ClassAuxiliar aux) {
        StringBuilder produtos = new StringBuilder();

        String query = "SELECT * FROM produtos_pedido WHERE id_pedido = '" + id_pedido + "'";
        Log.e("SQL", "getProdutosPedido - " + query);
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                ProdutosPedidoDomain pro = cursorProdutoPedido(cursor);
                produtos.append(pro.produto).append(" ").append(pro.quantidade).append("x").append(aux.maskMoney(new BigDecimal(pro.valor))).append("\n");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return produtos.toString();
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public int getQuantProdutosPedidoNCMGas(int id_pedido) {
        int quant = 0;

        String query = "SELECT  SUM(prp.quantidade) " +
                "FROM produtos_pedido prp " +
                "INNER JOIN produtos pro ON pro.codigo = prp.id_produto " +
                "WHERE prp.id_pedido = '" + id_pedido + "' AND pro.ncm = '27111910'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                if (cursor.getString(0) != null) {
                    quant = Integer.parseInt(cursor.getString(0));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return quant;
    }

    public int getQuantProdutosPedidoNCMOutros(int id_pedido) {
        int quant = 0;

        String query = "SELECT  SUM(prp.quantidade) " +
                "FROM produtos_pedido prp " +
                "INNER JOIN produtos pro ON pro.codigo = prp.id_produto " +
                "WHERE prp.id_pedido = '" + id_pedido + "' AND pro.ncm != '27111910'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                if (cursor.getString(0) != null) {
                    quant = Integer.parseInt(cursor.getString(0));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return quant;
    }

    public String getProdutosPedidoNCMGas(int id_pedido) {
        StringBuilder str = new StringBuilder();

        String query = "SELECT  pro.nome " +
                "FROM produtos_pedido prp " +
                "INNER JOIN produtos pro ON pro.codigo = prp.id_produto " +
                "WHERE prp.id_pedido = '" + id_pedido + "' AND pro.ncm = '27111910'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                str.append(cursor.getString(0)).append(", ");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return str.toString();
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO DIFERENTE DE GÁS
    public List<String> getQuantProdutosPedidoNCM(int id_pedido) {
        List<String> list = new ArrayList();

        String query = "SELECT pro.codigo, SUM(prp.quantidade) quantidade " +
                "FROM produtos_pedido prp " +
                "INNER JOIN produtos pro ON pro.codigo = prp.id_produto " +
                "WHERE prp.id_pedido = '" + id_pedido + "' AND pro.ncm != '27111910' " +
                "GROUP BY pro.codigo";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(0) != null) {
                    list.add(cursor.getString(0) + "," + cursor.getString(1));
                    //quant = Integer.parseInt(cursor.getString(0));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public int getQuantProdutosPedidoDiverso(int id_pedido) {
        int quant = 0;

        String query = "SELECT SUM(prp.quantidade) " +
                "FROM produtos_pedido prp " +
                "INNER JOIN produtos pro ON pro.codigo = prp.id_produto " +
                "WHERE prp.id_pedido = '" + id_pedido + "'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(0) != null) {
                    quant = Integer.parseInt(cursor.getString(0));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return quant;
    }

    //
    public int deleteProdutoPedido(String id) {
        myDataBase = this.getWritableDatabase();

        return myDataBase.delete(
                "produtos_pedido",
                "id = ?",
                new String[]{id}
        );
    }

    //
    public int deleteProdutoPedidoNFe(String id) {
        myDataBase = this.getWritableDatabase();

        return myDataBase.delete(
                "produtos_pedido_nfe",
                "id = ?",
                new String[]{id}
        );
    }


    void upadtePedidosTransmissao(
            String situacao,
            String protocolo,
            String data_protocolo,
            String hora_protocolo,
            String id) {


        try {
            myDataBase = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("situacao", situacao);
            values.put("protocolo", protocolo);
            values.put("data_protocolo", data_protocolo);
            values.put("hora_protocolo", hora_protocolo);
            myDataBase.update("pedidos", values, "id=" + id, null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
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


        myDataBase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("situacao", situacao);
        values.put("protocolo", protocolo);
        values.put("data_protocolo", data_protocolo);
        values.put("hora_protocolo", hora_protocolo);
        myDataBase.update("pedidosNFE", values, "id=" + id, null);
        //db.close();
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

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                PedidosNFE pedidos = cursorToPedidosNFE(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

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
                " GROUP BY ped.id ORDER BY " + ID_PEDIDOS_NFE + " DESC";

        Log.i("QUERY", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                StatusPedidosNFE pedidos = cursorToStatusPedidosNFE(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

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

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Pedidos pedidos = cursorToPedidos(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaPedidos;
    }

    //LISTAR TODOS OS PEDIDOS
    public ArrayList<Pedidos> getPedidosTemp(String idPedidoTemp) {
        ArrayList<Pedidos> listaPedidos = new ArrayList<>();

        String query = "SELECT * FROM pedidos WHERE id_pedido_temp = " + idPedidoTemp;

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Pedidos pedidos = cursorToPedidos(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaPedidos;
    }

    //LISTAR TODOS OS PEDIDOS
    public Pedidos getPedido(String idPedido) {
        Pedidos pedido = null;

        String query = "SELECT * FROM pedidos WHERE id = " + idPedido;

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                pedido = cursorToPedidos(cursor);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return pedido;
    }

    //LISTAR TODOS OS PEDIDOS RELATÓRIO
    ArrayList<Pedidos> getPedidosRelatorio() {
        ArrayList<Pedidos> listaPedidos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PEDIDOS + " ORDER BY " + ID_PEDIDOS + " ASC";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Pedidos pedidos = cursorToPedidos(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaPedidos;
    }

    //LISTAR TODOS OS PEDIDOS RELATÓRIO
    ArrayList<PedidosNFE> getPedidosRelatorioNFE() {
        ArrayList<PedidosNFE> listaPedidos = new ArrayList<>();

        String query = "SELECT * FROM " + TABELA_PEDIDOS_NFE + " ORDER BY " + ID_PEDIDOS_NFE + " ASC";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                PedidosNFE pedidos = cursorToPedidosNFE(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaPedidos;
    }

    //CURSOR PEDIDOS
    private StatusPedidos cursorToStatusPedidos(Cursor cursor) {
        StatusPedidos pedidos = new StatusPedidos("", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
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
        try {
            pedidos.setDesconto(cursor.getString(13));
        } catch (Exception ignored) {

        }
        pedidos.setValor(cursor.getString(14));
        pedidos.setNome(cursor.getString(15));
        return pedidos;
    }

    //LISTAR TODOS OS PEDIDOS
    public ArrayList<StatusPedidos> getStatusPedidos() {
        ArrayList<StatusPedidos> listaPedidos = new ArrayList<>();

        /*String query = "SELECT ped.id, ped.situacao, ped.protocolo, ped.data, ped.hora, (ped.valor_total - (desconto * quantidade)) AS valor_total, ped.data_protocolo, ped.hora_protocolo, ped.cpf_cliente, ped.forma_pagamento, ipe.pedido, ipe.produto, \n" +
                "ipe.quantidade, (desconto * quantidade) AS desconto, ipe.valor * 100 as valor, pro.nome FROM " + TABELA_PEDIDOS + " ped " +
                " INNER JOIN itens_pedidos ipe ON ipe.pedido = ped.id " +
                " INNER JOIN produtos pro ON pro.codigo = ipe.produto " +
                " GROUP BY ped.id " +
                " ORDER BY " + ID_PEDIDOS + " DESC";*/
        String query = "SELECT ped.id, ped.situacao, ped.protocolo, ped.data, ped.hora, (ped.valor_total - (desconto * quantidade)) AS valor_total, ped.data_protocolo, ped.hora_protocolo, ped.cpf_cliente, ped.forma_pagamento, ipe.pedido, ipe.produto, " +
                "ipe.quantidade, (desconto * quantidade) AS desconto, ipe.valor * 100 as valor, pro.nome FROM " + TABELA_PEDIDOS + " ped " +
                " INNER JOIN itens_pedidos ipe ON ipe.pedido = ped.id " +
                " INNER JOIN produtos pro ON pro.codigo = ipe.produto " +
                " GROUP BY ped.id ORDER BY " + ID_PEDIDOS + " DESC";

        Log.i("KLEILSON QUERY", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                StatusPedidos pedidos = cursorToStatusPedidos(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaPedidos;
    }

    public ArrayList<StatusPedidos> getDescricaoItensStatusPedidos(String idPedido) {
        ArrayList<StatusPedidos> listaPedidos = new ArrayList<>();

        String query = "SELECT ped.id, ped.situacao, ped.protocolo, ped.data, ped.hora, ((ipe.valor * quantidade) - (desconto * quantidade)) AS valor_total, ped.data_protocolo, ped.hora_protocolo, ped.cpf_cliente, ped.forma_pagamento, ipe.pedido, ipe.produto, \n" +
                "ipe.quantidade, (desconto * quantidade) AS desconto, ipe.valor as valor, pro.nome FROM Pedidos ped " +
                " INNER JOIN itens_pedidos ipe ON ipe.pedido = ped.id " +
                " INNER JOIN produtos pro ON pro.codigo = ipe.produto " +
                " WHERE ped.id = " + idPedido +
                " ORDER BY ped.id DESC";

        Log.i("KLEILSON QUERY", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                StatusPedidos pedidos = cursorToStatusPedidos(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaPedidos;
    }

    public ArrayList<StatusPedidosNFE> getDescricaoItensStatusPedidosNFe(String idPedido) {
        ArrayList<StatusPedidosNFE> listaPedidos = new ArrayList<>();
        new StatusPedidosNFE(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        String query = "SELECT ped.id, ped.situacao, ped.protocolo, ped.data, ped.hora, (ipe.valor * quantidade) / 100 AS valor_total, ped.cliente, ipe.pedido, ipe.produto, ipe.quantidade, ipe.valor / 100 as valor, pro.codigo, pro.nome, pro.tributos, pro.valor_minimo, pro.valor_maximo, pro.qtd_revenda \n" +
                "                FROM pedidosNFE ped \n" +
                "                INNER JOIN itens_pedidosNFE ipe ON ipe.pedido = ped.id \n" +
                "                 INNER JOIN produtos pro ON pro.codigo = ipe.produto \n" +
                "                 WHERE ped.id = " + idPedido +
                "                 ORDER BY ped.id DESC";

        Log.i("KLEILSON QUERY", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                StatusPedidosNFE pedidos = cursorToStatusPedidosNFE(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaPedidos;
    }

    /***************** ALTERAÇAO PARA RETORNO NULL DE CONSULTA *****************/

    // PEGAR O VALOR TOTAL
    public String getValorTotalPedido(String id, String desconto) {

        ClassAuxiliar aux = new ClassAuxiliar();
        desconto = String.valueOf(aux.converterValores(desconto));

        myDataBase = this.getReadableDatabase();
        String total = "0.00";

        try {
            String query = "SELECT SUM((prp.valor * prp.quantidade)) - " + desconto + " " +
                    "FROM produtos_pedido prp " +
                    "WHERE prp.id_pedido = " + id;

            Log.i("SQL", query);

            Cursor cursor = myDataBase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                String value = cursor.getString(0);
                if (value != null) {
                    total = String.valueOf(Double.parseDouble(value));
                } else {
                    Log.e("DatabaseHelper", "Valor nulo retornado na consulta para getValorTotalPedido.");
                }
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }

    /*********************** ALTERAÇAO NULLPOINTER **************************/

    public String getValorTotalPedidoNFe(String id) {
        ClassAuxiliar aux = new ClassAuxiliar();
        myDataBase = this.getReadableDatabase();
        String total = "0.00";

        try {
            String query = "SELECT SUM((prp.valor * prp.quantidade)) " + // / 100
                    "FROM produtos_pedido_nfe prp " +
                    "WHERE prp.id_pedido = " + id;

            Log.i("SQL", query);

            Cursor cursor = myDataBase.rawQuery(query, null);

            if (cursor.moveToFirst()) {  // Usar if em vez de getCount > 0 para ir diretamente ao primeiro registro
                double result = cursor.getDouble(0);  // Uso de getDouble para capturar diretamente o valor
                total = String.format("%.2f", result);  // Formatar a string para manter consistência
            } else {
                Log.e("DatabaseHelper", "Nenhum valor retornado para o pedido com ID: " + id);
            }

            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Erro ao calcular o valor total do pedido NFe", e);
        }

        return total;
    }

    //PEGAR O VALOR TOTAL
    public String getValorTotal() {

        ClassAuxiliar aux = new ClassAuxiliar();

        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();
        String total = "0.00", totalNFE = "0.00";

        try {
            String query = "SELECT SUM((ipe.valor * ipe.quantidade) - (ipe.desconto * ipe.quantidade)) " + // / 100
                    "FROM " + TABELA_PEDIDOS + " ped " +
                    " INNER JOIN itens_pedidos ipe ON ipe.pedido = ped.id " +
                    " INNER JOIN produtos pro ON pro.codigo = ipe.produto " +
                    " ORDER BY " + ID_PEDIDOS + " DESC";

            Log.i("SQL", query);

            Cursor cursor = myDataBase.rawQuery(query, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                total = String.valueOf(Double.parseDouble(cursor.getString(0)));
                //String[] a = total.split(".");

                //Log.i("TOTAL", "TOTAL NFC-e = " + a.length);

                Log.i("TOTAL", "TOTAL NFC-e = " + Double.parseDouble(cursor.getString(0)));
                Log.i("TOTAL", "TOTAL NFC-e = " + total);
            }

            cursor.close();
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String query = "SELECT SUM((ipe.valor * ipe.quantidade)) / 100 FROM itens_pedidosNFE ipe";

            Cursor cursor = myDataBase.rawQuery(query, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                totalNFE = String.valueOf(Double.parseDouble(cursor.getString(0)));
                Log.i("TOTAL", "TOTAL NF-e = " + cursor.getString(0));
            }

            cursor.close();
            //db.setTransactionSuccessful();
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


        /*db.endTransaction();
        db.close();*/

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
                    quatProd = produtos.getString(produtos.getColumnIndexOrThrow(QTD_REVENDA));
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
                    quantInt = intProdutos.getString(intProdutos.getColumnIndexOrThrow("quantidade"));
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
                    quantIntNFE = intProdutosNFE.getString(intProdutosNFE.getColumnIndexOrThrow("quantidade"));
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

        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();
        String id = "0";

        try {

            String query = "SELECT ped.id FROM " + TABELA_PEDIDOS + " ped " +
                    " ORDER BY ped.id DESC";

            Cursor cursor = myDataBase.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(0);
            }

            cursor.close();
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*db.endTransaction();
        db.close();*/

        return id;
    }

    //ULTIMA NFC-E EMITIDA
    public String ultimaNFE() {

        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();
        String id = "0";

        try {

            String query = "SELECT ped.id FROM " + TABELA_PEDIDOS_NFE + " ped " +
                    " ORDER BY ped.id DESC";

            Cursor cursor = myDataBase.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(0);
            }

            cursor.close();
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }


        /*db.endTransaction();
        db.close();*/

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

            cursor.close();
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

            cursorNFE.close();
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

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Pedidos pedidos = cursorToPedidos(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

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

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Pedidos pedidos = cursorToPedidos(cursor);
                listaPedidos.add(pedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaPedidos;
    }

    //LISTAR TODOS OS PEDIDOS A TRANSMITIR
    public Boolean getVerificarFinanceiroUltimoPedido() {

        float totPad = 0;
        float totFin = 0;

        /*//
        String queryPedido = "SELECT  ped.id_pedido_temp " +
                " FROM pedidos ped " +
                " WHERE situacao = 'OFF' OR situacao = '' " +
                " ORDER BY ped.id DESC LIMIT 1";
        String valItens;
        String valFinan;
        String idPedidoTemp;
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(queryPedido, null);

        if (cursor.moveToFirst()) {
            idPedidoTemp = cursor.getString(cursor.getColumnIndexOrThrow("id_pedido_temp"));
        }*/

        // * 100
        String query = "SELECT (" +
                "           SELECT SUM(itp.valor * itp.quantidade)" +
                "             FROM itens_pedidos itp" +
                "            WHERE itp.pedido = ped.id_pedido_temp" +
                "       )" +
                "       valor_total," +
                "       (" +
                "           SELECT SUM(fpp.valor)" +
                "             FROM formas_pagamento_pedidos fpp" +
                "            WHERE fpp.id_pedido = ped.id_pedido_temp AND fpp.status_pix = '0'" +
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
                totPad = Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow("valor_total")));
                totFin = Float.parseFloat(cursor.getString(cursor.getColumnIndexOrThrow("val_financeiro")));
            } while (cursor.moveToNext());
        }

        cursor.close();

        if (totPad > 0) {
            if (totPad != totFin) {
                return true;
            }
        }

        return false;
    }

/********** metodo pra atualizar valores evitando erro na tela pricipal  ****************/

    public void updatePedidoComFinanceiro(String idPedido, String total, String idFormaPagamento) {
        ContentValues cv = new ContentValues();
        cv.put("valor_total", total);
        cv.put("id_forma_pagamento", idFormaPagamento);

        // Atualiza a tabela pedidos_temp com as novas informações financeiras
        myDataBase.update("pedidos_temp", cv, "id = ?", new String[]{idPedido});
    }


    public Boolean getFinanceiroUltimoPedido(String idPedido) {
        boolean result = false;
        String query = "SELECT pet.id, prp.id_produto, fpp.id_forma_pagamento " +
                "FROM pedidos_temp pet " +
                "LEFT JOIN produtos_pedido prp ON prp.id_pedido = pet.id " +
                "LEFT JOIN formas_pagamento_pedidos fpp ON fpp.id_pedido = pet.id " +
                "WHERE pet.id = '" + idPedido + "' AND (pet.situacao = 'OFF' OR pet.situacao = '') " +
                "ORDER BY pet.id DESC " +
                "LIMIT 1";


        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        Log.e("FinanceiroUltimoPedido", query);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            Log.e("SQL:", cursor.getString(1) + " - " + cursor.getString(2));

            if (cursor.getString(1) != null || cursor.getString(2) != null) {
                result = true;
            }
        }
        cursor.close();

        return result;
    }

    //
    public ArrayList<ProdutosDescricaoNFCe> getItensPedidoIntegracao(String nPedido) {
        //String id = null;

        ArrayList<ProdutosDescricaoNFCe> listaItensPedidos = new ArrayList<>();

        String query = "SELECT pedido, produto, quantidade, valor, (desconto * quantidade) AS desconto, (valor * quantidade) - (desconto * quantidade) AS total FROM itens_pedidos WHERE pedido = '" + nPedido + "'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                ProdutosDescricaoNFCe itensPedidos = new ProdutosDescricaoNFCe();
                //String idProd = this.getIdProduto(cursor.getString(cursor.getColumnIndexOrThrow("produto")));
                String descProd = this.getProduto(cursor.getString(cursor.getColumnIndexOrThrow("produto")));
                itensPedidos.idProduto = cursor.getString(cursor.getColumnIndexOrThrow("produto"));
                itensPedidos.pedido = cursor.getString(cursor.getColumnIndexOrThrow("pedido"));
                itensPedidos.produto = descProd;
                itensPedidos.quantidade = cursor.getString(cursor.getColumnIndexOrThrow("quantidade"));
                itensPedidos.valor = cursor.getString(cursor.getColumnIndexOrThrow("valor"));
                itensPedidos.desconto = cursor.getString(cursor.getColumnIndexOrThrow("desconto"));
                itensPedidos.total = cursor.getString(cursor.getColumnIndexOrThrow("total"));
                listaItensPedidos.add(itensPedidos);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaItensPedidos;
    }


    //
    String ultimoIdPedido() {
        //BANCO DE DADOS
        myDataBase = this.getReadableDatabase();

        //
        String ultimoId = null;

        //
        Cursor pos;
        String query_pos = "SELECT * FROM pos";
        pos = myDataBase.rawQuery(query_pos, null);
        if (pos.moveToFirst()) {
            do {

                ultimoId = pos.getString(pos.getColumnIndexOrThrow("ultnfce"));

            } while (pos.moveToNext());
        }

        pos.close();

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
        myDataBase = this.getReadableDatabase();

        //CLASSE AUXILIAR
        ClassAuxiliar aux = new ClassAuxiliar();

        //CHAVE
        String chave;

        //CONTANTES CHAVE
        String chave_cUF = "";                // Código da UF do emitente do Documento Fiscal
        String chave_AAMM = aux.anoMesAtual();  // Ano e Mês de emissão da NF-e
        String chave_CNPJ = "";               // CNPJ do emitente
        String chave_mod = "65";                // Modelo do Documento Fiscal
        String chave_serie = "";              // Série do Documento Fiscal
        String chave_nNF;                       // Número do Documento Fiscal
        String chave_tpEmis = "1";              // Forma de emissão da NF-e
        String chave_cNF;                       // Código Numérico que compõe a Chave de Acesso
        //String chave_cDV = null;                // Dígito Verificador da Chave de Acesso

        //Código da UF - 2 caracteres
        Cursor unidades;
        String query_unidades = "SELECT * FROM unidades";
        unidades = myDataBase.rawQuery(query_unidades, null);
        if (unidades.moveToFirst()) {
            do {

                if (unidades.getColumnIndexOrThrow("cnpj") == 2) {
                    chave_CNPJ = aux.soNumeros(unidades.getString(unidades.getColumnIndexOrThrow("cnpj")));
                }
                if (unidades.getColumnIndexOrThrow("numero") == 4) {
                    chave_cUF = aux.idEstado(unidades.getString(unidades.getColumnIndexOrThrow("uf")));
                }

            } while (unidades.moveToNext());
        }

        unidades.close();

        //
        Cursor pos;
        String query_pos = "SELECT * FROM pos Limit 1";
        pos = myDataBase.rawQuery(query_pos, null);
        if (pos.moveToFirst()) {
            chave_serie = aux.soNumeros(pos.getString(pos.getColumnIndexOrThrow("serie")));
        }

        pos.close();

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
        int NF = (numeroNota * 133);//13001
        //NF = String.format("%08d", NF);
        //NF = NF.substring(0, 8);
        Log.e("CHAVE", String.valueOf(NF));
        Log.e("CHAVE", String.format("%08d", NF));
        Log.e("CHAVE", String.format("%08d", NF).substring(0, 8));
        chave_cNF = String.format("%08d", NF).substring(0, 8);// String.format("%08d", numeroNota);

        String chaveSD = chave_cUF + chave_AAMM + chave_CNPJ + chave_mod + chave_serie + chave_nNF + chave_tpEmis + chave_cNF;
        chave = aux.digitoVerificado(aux.soNumeros(chaveSD));
        //chave_cDV = aux.digitoVerificado(aux.soNumeros(chaveSD));

        StringBuilder chaveCompleta = new StringBuilder(chave);
        chaveCompleta.insert(chave.length() - 4, "  ");
        chaveCompleta.insert(chave.length() - 8, "  ");
        chaveCompleta.insert(chave.length() - 12, "  ");
        chaveCompleta.insert(chave.length() - 16, "  ");
        chaveCompleta.insert(chave.length() - 20, "  ");
        chaveCompleta.insert(chave.length() - 24, "  ");
        chaveCompleta.insert(chave.length() - 28, "  ");
        chaveCompleta.insert(chave.length() - 32, "  ");
        chaveCompleta.insert(chave.length() - 36, "  ");
        chaveCompleta.insert(chave.length() - 40, "  ");
        return chaveCompleta.toString();
    }

    //
    String getSeriePOS() {
        //BANCO DE DADOS
        myDataBase = this.getReadableDatabase();

        //CLASSE AUXILIAR
        ClassAuxiliar aux = new ClassAuxiliar();

        String serie = "";

        //
        Cursor pos;
        String query_pos = "SELECT * FROM pos Limit 1";
        pos = myDataBase.rawQuery(query_pos, null);
        if (pos.moveToFirst()) {
            serie = aux.soNumeros(pos.getString(pos.getColumnIndexOrThrow("serie")));
        }

        pos.close();

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
        myDataBase = this.getReadableDatabase();

        //CLASSE AUXILIAR
        ClassAuxiliar aux = new ClassAuxiliar();

        String serial = null;

        //
        Cursor pos;
        String query_pos = "SELECT serial FROM pos Limit 1";
        pos = myDataBase.rawQuery(query_pos, null);
        if (pos.moveToFirst()) {
            serial = aux.soNumeros(pos.getString(pos.getColumnIndexOrThrow("serial")));
        }

        pos.close();

        return serial;
    }

    //CURSOR PEDIDOS
    private Unidades cursorToUnidade(Cursor cursor) {
        Unidades unidades = new Unidades(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
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
        unidades.setApi_key_asaas(cursor.getString(14));
        unidades.setCliente_cob_asaas(cursor.getString(15));
        unidades.setIdCSC(cursor.getString(16));
        unidades.setCSC(cursor.getString(17));
        unidades.setUrl_qrcode(cursor.getString(18));
        try {
            unidades.setCliente_id_transfeera(cursor.getString(19));
            unidades.setCliente_secret_transfeera(cursor.getString(20));
            unidades.setPix_key_transfeera(cursor.getString(21));
        } catch (Exception ignored) {

        }
        try {
            unidades.setCredenciadora(cursor.getString(22));
        } catch (Exception ignored) {

        }
        try {
            unidades.setBanco_pix(cursor.getString(23));
        } catch (Exception ignored) {

        }
        return unidades;
    }

    //LISTAR TODAS AS UNIDADES
    public ArrayList<Unidades> getUnidades() {
        ArrayList<Unidades> listaUnidades = new ArrayList<>();

        String query = "SELECT * FROM unidades";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Unidades unidades = cursorToUnidade(cursor);
                listaUnidades.add(unidades);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaUnidades;
    }

    //CURSOR POS
    private PosApp cursorToPos(Cursor cursor) {
        PosApp posApp = new PosApp(null, null, null, null, null, null, null, null, null, null, null);

        //
        posApp.setCodigo(cursor.getString(0));
        posApp.setSerial(cursor.getString(1));
        posApp.setCliente(cursor.getString(2));
        posApp.setUnidade(cursor.getString(3));
        posApp.setSerie(cursor.getString(4));
        posApp.setUltnfce(cursor.getString(5));
        posApp.setUltboleto(cursor.getString(6));
        posApp.setNota_remessa(cursor.getString(7));
        posApp.setSerie_remessa(cursor.getString(8));
        try {
            posApp.setDesconto_app_emissor(cursor.getString(9));
        } catch (Exception ignored) {

        }
        try {
            posApp.setDuplicatas_notas_fiscais(cursor.getString(10));
        } catch (Exception ignored) {

        }
        return posApp;
    }

    //LISTAR TODAS AS UNIDADES
    public ArrayList<PosApp> getPos() {
        ArrayList<PosApp> listaPos = new ArrayList<>();

        String query = "SELECT * FROM pos";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                PosApp posApp = cursorToPos(cursor);
                listaPos.add(posApp);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return listaPos;
    }

    public PosApp getDadosPos() {
        PosApp dadosPos = null;
        String query = "SELECT * FROM pos LIMIT 1";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            dadosPos = cursorToPos(cursor);
        }
        cursor.close();
        return dadosPos;
    }

    // GET LIST CREDENCIADORAS

    ArrayList<String> getCredenciadora() {
        ArrayList<String> list = new ArrayList<>();
        myDataBase = this.getReadableDatabase();
        String selectQuery = "SELECT * From credenciadoras GROUP BY cnpj_credenciadora";
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        list.add("CREDENCIADORA");
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String nome = cursor.getString(cursor.getColumnIndexOrThrow("descricao_credenciadora"));
                    list.add(nome);
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    ArrayList<String> getIdCredenciadora() {
        ArrayList<String> list = new ArrayList<>();
        myDataBase = this.getReadableDatabase();
        String selectQuery = "SELECT codigo_credenciadora From credenciadoras GROUP BY cnpj_credenciadora";
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        list.add("CREDENCIADORA");
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String nome = cursor.getString(cursor.getColumnIndexOrThrow("codigo_credenciadora"));
                    list.add(nome);
                }
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    //CURSOR POS
    private FormaPagamentoPedido cursorFormaPagamentoPedido(Cursor cursor) {
        FormaPagamentoPedido formaPagamentoPedido = new FormaPagamentoPedido(null, null, null, null, null, null, null, null, null);

        //
        formaPagamentoPedido.setId(cursor.getString(0));
        formaPagamentoPedido.setId_pedido(cursor.getString(1));
        formaPagamentoPedido.setId_forma_pagamento(cursor.getString(2));
        formaPagamentoPedido.setValor(cursor.getString(3));
        formaPagamentoPedido.setCodigo_autorizacao(cursor.getString(4));
        formaPagamentoPedido.setCardBrand(cursor.getString(5));
        formaPagamentoPedido.setNsu(cursor.getString(6));
        formaPagamentoPedido.setId_cobranca_pix(cursor.getString(7));
        formaPagamentoPedido.setStatus_pix(cursor.getString(8));
        return formaPagamentoPedido;
    }

    // LISTA O FINANCEIRO DO PEDIDO TEMPORÁRIO
    public ArrayList<FormaPagamentoPedido> getFinanceiroCliente(int id_pedido) {
        ArrayList<FormaPagamentoPedido> listaFinanceiroVendas = new ArrayList<>();

        String query = "SELECT fpp.id, fpp.id_pedido, fpp.id_forma_pagamento, SUM(fpp.valor) AS valor, fpp.codigo_autorizacao, fpp.bandeira, fpp.nsu, fpp.id_cobranca_pix, fpp.status_pix " +
                "FROM pedidos ped " +
                "INNER JOIN formas_pagamento_pedidos fpp ON fpp.id_pedido = ped.id " +
                "WHERE ped.id_pedido_temp = '" + id_pedido + "' " +
                "GROUP BY fpp.id_forma_pagamento";
        //String query = "SELECT * FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        Log.e("SQL", "getFinanceiroCliente - " + query);
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                //FormaPagamentoPedido formaPagamentoPedido = cursorFormaPagamentoPedido(cursor);
                listaFinanceiroVendas.add(cursorFormaPagamentoPedido(cursor));
                //Log.e("SQL", "getFinanceiroCliente - " + formaPagamentoPedido.getId_pedido());
            } while (cursor.moveToNext());
        }

        cursor.close();

        //db.close();
        return listaFinanceiroVendas;
    }


    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public ArrayList<FormaPagamentoPedido> getFinanceiroPedidoTemp(int id_pedido) {
        ArrayList<FormaPagamentoPedido> listaFinanceiroVendas = new ArrayList<>();

        String query = "SELECT fpp.id, fpp.id_pedido, fpp.id_forma_pagamento, SUM(fpp.valor) AS valor, fpp.codigo_autorizacao, fpp.bandeira, fpp.nsu, fpp.id_cobranca_pix, fpp.status_pix " +
                "FROM formas_pagamento_pedidos fpp " +
                "WHERE fpp.id_pedido = '" + id_pedido + "' " +
                "GROUP BY fpp.id_forma_pagamento";
        //String query = "SELECT * FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        Log.e("SQL", "getFinanceiroCliente - " + query);
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                //FormaPagamentoPedido formaPagamentoPedido = cursorFormaPagamentoPedido(cursor);
                listaFinanceiroVendas.add(cursorFormaPagamentoPedido(cursor));
                //Log.e("SQL", "getFinanceiroCliente - " + formaPagamentoPedido.getId_pedido());
            } while (cursor.moveToNext());
        }

        cursor.close();

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

        cursor.close();
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
                if (!formasPag.toString().contains(aux.getNomeFormaPagamento(cursor.getString(0))))
                    formasPag.append(aux.getNomeFormaPagamento(cursor.getString(0))).append(", ");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }
    /*public String getFormasPagamentoPedidoPrint(String id_pedido) {
        ClassAuxiliar aux = new ClassAuxiliar();
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT id_forma_pagamento FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                if (!formasPag.toString().contains(aux.getNomeFormaPagamento(cursor.getString(0))))
                    formasPag.append(aux.getNomeFormaPagamento(cursor.getString(0))).append(", ");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }*/

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

        String query = "SELECT valor * 100 FROM financeiro_nfce WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }
    /*public String getValoresFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT valor * 100 FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }*/

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getIdFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT id_forma_pagamento FROM financeiro_nfce WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }
    /*public String getIdFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT id_forma_pagamento FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }*/

    //LISTAR TODOS OS IDS DOS PRODUTOS DO PEDIDO
    public String getIdsProdutosPedido(String id_pedido) {
        StringBuilder idsProdutos = new StringBuilder();

        String query = "SELECT produto FROM itens_pedidos WHERE pedido = '" + id_pedido + "'";
        //String query = "SELECT id_produto FROM produtos_pedido WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                idsProdutos.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return idsProdutos.toString();
    }

    //LISTAR TODOS OS IDS DOS PRODUTOS DO PEDIDO
    public String getIdsProdutosPedidoNFe(String id_pedido) {
        StringBuilder idsProdutos = new StringBuilder();

        //String query = "SELECT produto FROM itens_pedidos WHERE pedido = '" + id_pedido + "'";
        String query = "SELECT id_produto FROM produtos_pedido_nfe WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                idsProdutos.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return idsProdutos.toString();
    }

    public String getQuantidadesProdutosPedidoNFe(String id_pedido) {
        StringBuilder quantidadeProdutos = new StringBuilder();

        String query = "SELECT quantidade FROM produtos_pedido_nfe WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                quantidadeProdutos.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return quantidadeProdutos.toString();
    }

    public String getValorProdutosPedidoNFe(String id_pedido) {
        StringBuilder valorProdutos = new StringBuilder();

        String query = "SELECT valor * 100 FROM produtos_pedido_nfe WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                valorProdutos.append(cursor.getString(0).replace(".", "")).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return valorProdutos.toString();
    }

    //LISTAR TODOS OS IDS DOS PRODUTOS DO PEDIDO
    public String getNomeProdutosPedidoNFe(String id_pedido) {
        StringBuilder idsProdutos = new StringBuilder();

        //String query = "SELECT produto FROM itens_pedidos WHERE pedido = '" + id_pedido + "'";
        String query = "SELECT id_produto FROM produtos_pedido_nfe WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                idsProdutos.append(this.getNomeProduto(cursor.getString(0))).append(", ");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return idsProdutos.toString();
    }

    //PEGA O ULTIMO ID DA TABELA DE PEDIDOS
    public String getUltimoIdPedidoNFe() {

        myDataBase = this.getReadableDatabase();
        String selectQuery = "SELECT ped.id FROM pedidosNFE ped ORDER BY ped.id DESC LIMIT 1";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String idPedidoNFe = "";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                idPedidoNFe = cursor.getString(0);
            }

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return idPedidoNFe;
    }

    /*public String getIdsProdutosPedido(String id_pedido) {
        StringBuilder idsProdutos = new StringBuilder();

        String query = "SELECT id_produto FROM produtos_pedido WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                idsProdutos.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return idsProdutos.toString();
    }*/
    public String getQuantidadesProdutosPedido(String id_pedido) {
        StringBuilder quantidadeProdutos = new StringBuilder();

        String query = "SELECT quantidade FROM itens_pedidos WHERE pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                quantidadeProdutos.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return quantidadeProdutos.toString();
    }

    public String getTotalItensPedido(String id_pedido) {
        String TotalItens = "0";

        String query = "SELECT SUM(quantidade) FROM itens_pedidos WHERE pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            TotalItens = cursor.getString(0);
        }

        cursor.close();
        return TotalItens;
    }

    public String getValorProdutosPedido(String id_pedido) {
        StringBuilder valorProdutos = new StringBuilder();

        String query = "SELECT valor * 100 FROM itens_pedidos WHERE pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                valorProdutos.append(cursor.getString(0).replace(".", "")).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return valorProdutos.toString();
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getAutorizacaoFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT codigo_autorizacao FROM financeiro_nfce WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0) != null ? cursor.getString(0) : "").append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }
    /*public String getAutorizacaoFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT codigo_autorizacao FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }*/

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getBandeiraFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT bandeira FROM financeiro_nfce WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0) != null ? cursor.getString(0) : "").append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }
    /*public String getBandeiraFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT bandeira FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }*/

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getNSUFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT nsu FROM financeiro_nfce WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0) != null ? cursor.getString(0) : "").append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }
   /* public String getNSUFormasPagamentoPedido(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT nsu FROM formas_pagamento_pedidos WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0)).append(",");
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }*/

    // APAGA ITENS DO FINANCEIRO DO PEDIDO

    public void deleteItemFinanceiro(String idItemFin) {
        myDataBase = this.getWritableDatabase();

        myDataBase.delete("formas_pagamento_pedidos", "id = ?", new String[]{idItemFin});
        //myDataBase.close();
    }

    //
    public int deleteItemFormPagPedido(FormaPagamentoPedido formaPagamentoPedido) {
        /*String query = "SELECT id " +
                "FROM pedidos " +
                "WHERE id = '" + formaPagamentoPedido.getId_pedido() + "' " +
                "AND fracionado = '1'";*/
        String query = "SELECT ped.id " +
                "FROM pedidos ped " +
                "INNER JOIN formas_pagamento_pedidos fpp ON fpp.id_pedido = ped.id " +
                "WHERE ped.id_pedido_temp = '" + formaPagamentoPedido.getId_pedido() + "' ";

        Log.e("SQL", query);
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);
        int i = 0;

        SQLiteDatabase db = this.getWritableDatabase();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    //formasPag.append(cursor.getString(0)).append(",");

                    i = db.delete(
                            "formas_pagamento_pedidos",
                            "id_pedido = ?",
                            new String[]{String.valueOf(cursor.getString(0))}
                    );
                } while (cursor.moveToNext());

            }

            cursor.close();
        } else {

            i = db.delete(
                    "formas_pagamento_pedidos",
                    "id = ?",
                    new String[]{String.valueOf(formaPagamentoPedido.getId())}
            );
        }
        db.close();
        return i;
    }

    //SOMAR O VALOR DO FINANCEIRO
    public String getTotalFinanceiro(String idPedido, boolean api_asaas) {
        myDataBase = this.getReadableDatabase();

        String selectQuery;

        if (api_asaas) {
            selectQuery = "SELECT SUM(fpp.valor) AS valor " +
                    "FROM formas_pagamento_pedidos fpp " +
                    "WHERE fpp.id_pedido = '" + idPedido + "' AND fpp.status_pix = '0'";
        } else {
            selectQuery = "SELECT SUM(fpp.valor) AS valor " +
                    "FROM formas_pagamento_pedidos fpp " +
                    "WHERE fpp.id_pedido = '" + idPedido + "'";
        }

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "0.0";

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            total = cursor.getString(0);
        }

        cursor.close();
        if (total == null) {
            total = "0.0";
        }
        return total;
    }

    //SOMAR O VALOR DO FINANCEIRO
    public String getTotalFinanceiroDinheiro(String idPedido, boolean api_asaas) {
        myDataBase = this.getReadableDatabase();

        String selectQuery;

        if (api_asaas) {
            selectQuery = "SELECT SUM(fpp.valor) AS valor " +
                    "FROM formas_pagamento_pedidos fpp " +
                    "WHERE fpp.id_pedido = '" + idPedido + "' AND fpp.status_pix = '0' AND id_forma_pagamento = 1";
        } else {
            selectQuery = "SELECT SUM(fpp.valor) AS valor " +
                    "FROM formas_pagamento_pedidos fpp " +
                    "WHERE fpp.id_pedido = '" + idPedido + "' AND id_forma_pagamento = 1";
        }

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "0.00";

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            total = cursor.getString(0);
        }

        cursor.close();
        if (total == null) {
            total = "0.00";
        }
        return total;
    }

    //SOMAR O VALOR DO FINANCEIRO
    public String getTotalFinanceiroPix(String idPedido, boolean api_asaas) {
        myDataBase = this.getReadableDatabase();

        String selectQuery;

        if (api_asaas) {
            selectQuery = "SELECT SUM(fpp.valor) AS valor " +
                    "FROM formas_pagamento_pedidos fpp " +
                    "WHERE fpp.id_pedido = '" + idPedido + "' AND fpp.status_pix = '0' AND id_forma_pagamento = 17";
        } else {
            selectQuery = "SELECT SUM(fpp.valor) AS valor " +
                    "FROM formas_pagamento_pedidos fpp " +
                    "WHERE fpp.id_pedido = '" + idPedido + "' AND id_forma_pagamento = 17";
        }

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "0.00";

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            total = cursor.getString(0);
        }

        cursor.close();
        if (total == null) {
            total = "0.00";
        }
        return total;
    }

    // RETORNA O ID DA COBRANCA PIX
    public String getIdCobrancaPix(String idPedido) {
        myDataBase = this.getReadableDatabase();

        String selectQuery = "SELECT fpp.id_cobranca_pix " +
                "FROM formas_pagamento_pedidos fpp " +
                "WHERE fpp.id_pedido = '" + idPedido + "' AND id_forma_pagamento = 17";


        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String id_cobranca = "";

        if (cursor.moveToFirst()) {
            id_cobranca = cursor.getString(0);
        }
        cursor.close();
        if (id_cobranca == null) {
            id_cobranca = "";
        }
        return id_cobranca;
    }

    //VERIFICAR SE TEM FINANCEIRO DIFERENTE DE DINHEIRO E PIX NO PEDIDO TEMP
    public boolean getFianceiroDiferenteDeDinheiroPix(String idPedido) {
        myDataBase = this.getReadableDatabase();

        String selectQuery = "SELECT fpp.id_forma_pagamento " +
                "FROM formas_pagamento_pedidos fpp " +
                "WHERE fpp.id_pedido = '" + idPedido + "' AND id_forma_pagamento != 1 AND id_forma_pagamento != 17";


        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }

        cursor.close();
        return false;
    }

    //SOMAR O VALOR DO FINANCEIRO
    public String getTotalItem(String idPedido) {
        myDataBase = this.getReadableDatabase();
        String selectQuery = "SELECT SUM(ipe.valor * ipe.quantidade) AS valor " +
                "FROM itens_pedidos ipe " +
                "WHERE ipe.pedido = '" + idPedido + "'";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "0.0";

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            total = cursor.getString(0);
        }

        cursor.close();
        if (total == null) {
            total = "0.0";
        }
        return total;
    }

    //SOMAR O VALOR DO FINANCEIRO
    public String getValorTotalFinanceiro(String idPedido, boolean api_asaas) {
        myDataBase = this.getReadableDatabase();

        String selectQuery;

        if (api_asaas) {
            selectQuery = "SELECT SUM(fpp.valor) AS valor " +
                    "FROM pedidos ped " +
                    "INNER JOIN financeiro_nfce fpp ON fpp.id_pedido = ped.id " +
                    "WHERE ped.id_pedido_temp = '" + idPedido + "' AND fpp.status_pix = '0'";
        } else {
            selectQuery = "SELECT SUM(fpp.valor) AS valor " +
                    "FROM pedidos ped " +
                    "INNER JOIN financeiro_nfce fpp ON fpp.id_pedido = ped.id " +
                    "WHERE ped.id_pedido_temp = '" + idPedido + "'";
        }

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "0.0";

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            total = cursor.getString(0);
        }

        cursor.close();
        if (total == null) {
            total = "0.0";
        }
        return total;
    }
    /*public String getValorTotalFinanceiro(String idPedido, boolean api_asaas) {
        myDataBase = this.getReadableDatabase();

        String selectQuery;

        if (api_asaas) {
            *//*selectQuery = "SELECT SUM(valor) " +
                    "FROM formas_pagamento_pedidos " +
                    "WHERE id_pedido = '" + idPedido + "' AND status_pix = '0'";*//*
            selectQuery = "SELECT SUM(fpp.valor) AS valor " +
                    "FROM pedidos ped " +
                    "INNER JOIN formas_pagamento_pedidos fpp ON fpp.id_pedido = ped.id " +
                    "WHERE ped.id_pedido_temp = '" + idPedido + "' AND fpp.status_pix = '0'";
        } else {
            //selectQuery = "SELECT SUM(valor) FROM formas_pagamento_pedidos WHERE id_pedido = '" + idPedido + "'";
            selectQuery = "SELECT SUM(fpp.valor) AS valor " +
                    "FROM pedidos ped " +
                    "INNER JOIN formas_pagamento_pedidos fpp ON fpp.id_pedido = ped.id " +
                    "WHERE ped.id_pedido_temp = '" + idPedido + "'";
        }

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "0.0";

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            total = cursor.getString(0);
        }

        cursor.close();
        if (total == null) {
            total = "0.0";
        }
        return total;
    }*/

    public int getProximoIdPedido() {
        int id = 1;
        String uNfce = getUltimoIdPedido();
        myDataBase = this.getReadableDatabase();
        String selectQuery;

        if (uNfce.equalsIgnoreCase("")) {
            selectQuery = "SELECT (p.ultnfce + 1) id FROM pos p ORDER BY  p.codigo DESC LIMIT 1";
        } else {
            selectQuery = "SELECT (ped.id + 1) id FROM pedidos ped ORDER BY  ped.id DESC LIMIT 1";
        }
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            id = Integer.parseInt(cursor.getString(0));
        }

        cursor.close();

        return id;
    }

    public String getUltimoIdPedido() {
        myDataBase = this.getReadableDatabase();
        String sql = "SELECT ped.id FROM pedidos ped ORDER BY ped.id DESC LIMIT 1";
        Cursor cursor = myDataBase.rawQuery(sql, null);
        String total = "";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cursor.close();
        return total;
    }

    public String IdEditarPedido() {

        myDataBase = this.getReadableDatabase();
        String selectQuery = "SELECT ped.id_pedido_temp FROM pedidos ped WHERE ped.situacao = 'OFF' ORDER BY ped.id DESC LIMIT 1";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }

            cursor.close();
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }

    public int IdEditarPedidoTemp() {

        myDataBase = this.getReadableDatabase();
        String selectQuery = "SELECT ped.id FROM pedidos_temp ped WHERE ped.situacao = 'OFF' ORDER BY ped.id DESC LIMIT 1";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        int result = 0;
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = cursor.getInt(0);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    //PEGA O ULTIMO ID DA TABELA DE PEDIDOS
    public String getUltimoIdPedidos() {

        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();
        //pedidos_temp
        String selectQuery = "SELECT ped.id FROM pedidos ped ORDER BY  ped.id DESC LIMIT 1";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }

            cursor.close();
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return total;
    }

    //PEGA O ULTIMO ID DA TABELA DE PEDIDOS
    public String getIdPedidoTemp() {
        myDataBase = this.getReadableDatabase();
        String selectQuery = "SELECT ped.id FROM pedidos_temp ped ORDER BY ped.id DESC LIMIT 1";
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        String id = "";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(0);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    //SOMAR O VALOR DO FINANCEIRO
    public String getUltimaNotaPOS() {

        myDataBase = this.getReadableDatabase();
        //db.beginTransaction();

        String selectQuery = "SELECT ultnfce FROM pos LIMIT 1";

        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        String total = "";
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                total = cursor.getString(0);
            }

            cursor.close();
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
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

        cursor.close();
    }

    private void apagarPedidoSemPagamento(String idPedido) {
        myDataBase = this.getWritableDatabase();

        //
        myDataBase.delete(
                "formas_pagamento_pedidos",
                "id_pedido = ?",
                new String[]{idPedido}
        );

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

        cursor.close();

        return pedidos;
    }

    public void apagarPixSemPagamento() {
        String query = "DELETE FROM formas_pagamento_pedidos WHERE status_pix = '1'";

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        /*if (cursor.moveToFirst()) {
            do {
                pedidos = cursorToPedidos(cursor);
            } while (cursor.moveToNext());
        }*/

        cursor.close();
    }

    // ADICIONA FINANCEIRO PARA OS PEDIDOS FRACIONADOS
    public void PedidoFracionadosSemFinanceiro() {

        String query = "SELECT ped.id, ped.valor_total, fpp.id_pedido" +
                "    FROM Pedidos ped" +
                "    LEFT JOIN" +
                "    formas_pagamento_pedidos fpp ON fpp.id_pedido = ped.id" +
                "    WHERE ped.fracionado = 1" +
                "    ORDER BY ped.id DESC";

        /*String queryInsert = "INSERT INTO formas_pagamento_pedidos " +
                "(id_pedido, id_forma_pagamento, valor) VALUES('%s','1','%s')";

        String queryUpdate = "UPDATE formas_pagamento_pedidos" +
                " SET valor = '%s' WHERE id_pedido = '%s'";*/

        myDataBase = this.getReadableDatabase();
        //SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getCount() > 0) {
                    //myDataBase.close();
                    //myDataBase = this.getWritableDatabase();
                    SQLiteDatabase db = this.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    if (cursor.getString(2) == null) {
                        values.put("id_pedido", cursor.getString(0));
                        values.put("id_forma_pagamento", "1");
                        values.put("valor", cursor.getString(1));
                        db.insert("formas_pagamento_pedidos", null, values);
                    } else {
                        values.put("valor", cursor.getString(1));
                        db.update("formas_pagamento_pedidos", values, "id_pedido=" + cursor.getString(0), null);
                    }
                    db.close();
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    //
    PrintPixDomain ultimoPIX() {
        PrintPixDomain printPixDomain = null;
        myDataBase = this.getReadableDatabase();
        String query = "SELECT fpp.id_pedido, fpp.valor, fpp.id_cobranca_pix, ped.data, ped.hora " +
                "FROM financeiro_nfce fpp " +
                "INNER JOIN pedidos ped ON ped.id = fpp.id_pedido " +
                "WHERE fpp.id_cobranca_pix != '' AND fpp.status_pix = '0' " +
                "ORDER BY fpp.id DESC " +
                "LIMIT 1";
        Cursor cursor = myDataBase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            printPixDomain = new PrintPixDomain();
            do {
                printPixDomain.id_pedido = cursor.getString(cursor.getColumnIndexOrThrow("id_pedido"));
                printPixDomain.valor = cursor.getString(cursor.getColumnIndexOrThrow("valor"));
                printPixDomain.id_cobranca_pix = cursor.getString(cursor.getColumnIndexOrThrow("id_cobranca_pix"));
                printPixDomain.data = cursor.getString(cursor.getColumnIndexOrThrow("data"));
                printPixDomain.hora = cursor.getString(cursor.getColumnIndexOrThrow("hora"));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return printPixDomain;
    }

    //
    PrintPixDomain ultimoPIXFinanceiroNfce() {
        PrintPixDomain printPixDomain = null;
        myDataBase = this.getReadableDatabase();
        String query = "SELECT fpp.id_pedido, fpp.valor, fpp.id_cobranca_pix " +
                "FROM financeiro_nfce fpp " +
                "WHERE fpp.id_cobranca_pix != '' AND fpp.status_pix = '0' " +
                "ORDER BY fpp.id DESC " +
                "LIMIT 1";
        Cursor cursor = myDataBase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            printPixDomain = new PrintPixDomain();
            do {
                printPixDomain.id_pedido = cursor.getString(cursor.getColumnIndexOrThrow("id_pedido"));
                printPixDomain.valor = cursor.getString(cursor.getColumnIndexOrThrow("valor"));
                printPixDomain.id_cobranca_pix = cursor.getString(cursor.getColumnIndexOrThrow("id_cobranca_pix"));
                //printPixDomain.data = cursor.getString(cursor.getColumnIndexOrThrow("data"));
                //printPixDomain.hora = cursor.getString(cursor.getColumnIndexOrThrow("hora"));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return printPixDomain;
    }

    //
    String ultimoIdFormPagPIX(String idPedido) {
        myDataBase = this.getReadableDatabase();
        String id = null;
        String query = "SELECT * " +
                "FROM formas_pagamento_pedidos fpp " +
                "WHERE fpp.id_pedido = '" + idPedido + "' AND fpp.status_pix = 1 " +
                "ORDER BY fpp.id DESC " +
                "LIMIT 1";
        Cursor cursor = myDataBase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return id;
    }

    public void updateProdutoPedidoTemp(String idPedido, String idProduto, String quantidade, String total) {
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantidade", quantidade);
        values.put("total", total);
        myDataBase.update("produtos_pedido", values, "id_pedido=" + idPedido + " AND id_produto = '" + idProduto + "'", null);
    }

    void updateValorPedido(String idPedido, String valor) {
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("valor_total", valor);
        myDataBase.update("pedidos", values, "id=" + idPedido, null);
    }

    // DIMINUI O VALOR DO FINANCEIRO NFCE DINHEIRO
    void updateValorFinanceiroPedidoDinheiro(String idPedido, String valor) {
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("valor", valor);
        myDataBase.update("formas_pagamento_pedidos", values, "id_pedido=" + idPedido + " AND id_forma_pagamento = 1", null);
    }

    // DIMINUI O VALOR DO FINANCEIRO NFCE PIX
    void updateValorFinanceiroPedidoPix(String idPedido, String valor) {
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("valor", valor);
        myDataBase.update("formas_pagamento_pedidos", values, "id_pedido=" + idPedido + " AND id_forma_pagamento = 17", null);
    }

    // ZERAR O VALOR DO FINANCEIRO NFCE DINHEIRO E PIX
    void zerarFinanceiroPedidoTemp(String idPedido) {
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("valor", "0.00");
        myDataBase.update("formas_pagamento_pedidos", values, "id_pedido=" + idPedido, null);
    }

    // SALVA O ID DO PAGAMENTO PIX RETORNADO DA API ASAAS
    void updateFormPagPIX(String id_cobranca_pix, String id) {
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("id_cobranca_pix", id_cobranca_pix);
        myDataBase.update("formas_pagamento_pedidos", values, "id=" + id, null);
    }

    void updateFormPagPIXRecebido(String id) {
        myDataBase = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("status_pix", "0");
        myDataBase.update("formas_pagamento_pedidos", values, "id=" + id, null);
    }

    //
    public void deleteFormPagPIX() {
        myDataBase = this.getWritableDatabase();
        myDataBase.delete("formas_pagamento_pedidos", "status_pix" + "=?", new String[]{"1"});
    }

    //CURSOR POS
    private FinanceiroNFeDomain cursorFinanceiroNFeDomain(Cursor cursor) {
        return new FinanceiroNFeDomain(
                cursor.getString(0), cursor.getString(1),
                cursor.getString(2), cursor.getString(3),
                cursor.getString(4), cursor.getString(5),
                cursor.getString(6), cursor.getString(7),
                cursor.getString(8), cursor.getString(9),
                cursor.getString(10)
        );
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO DA NFE
    public ArrayList<FinanceiroNFeDomain> getFinanceiroNFe(int id_pedido) {
        ArrayList<FinanceiroNFeDomain> financeiroNFeDomains = new ArrayList<>();

        String query = "SELECT * FROM financeiro_nfe WHERE id_pedido = '" + id_pedido + "'";
        Log.e("SQL", "getFinanceiroNFe : " + query);
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                financeiroNFeDomains.add(cursorFinanceiroNFeDomain(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return financeiroNFeDomains;
    }

    // ADD FORMAS PEDIDO TEMPORARIO
    void addFinanceiroNFe(FinanceiroNFeDomain formaPagamentoPedido) {
        try {
            myDataBase = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("id_pedido", formaPagamentoPedido.id_pedido);
            values.put("id_forma_pagamento", formaPagamentoPedido.id_forma_pagamento);
            values.put("valor", formaPagamentoPedido.valor);
            values.put("codigo_autorizacao", formaPagamentoPedido.codigo_autorizacao);
            values.put("bandeira", formaPagamentoPedido.cardBrand);
            values.put("nsu", formaPagamentoPedido.codigo_autorizacao);
            values.put("id_cobranca_pix", formaPagamentoPedido.id_cobranca_pix);
            values.put("status_pix", formaPagamentoPedido.status_pix);
            values.put("parcelas", formaPagamentoPedido.parcelas);
            values.put("vencimento", formaPagamentoPedido.vencimento);
            myDataBase.insert("financeiro_nfe", null, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    //
    public int deleteRegFinanceiroNFe(String id) {
        myDataBase = this.getWritableDatabase();

        return myDataBase.delete(
                "financeiro_nfe",
                "id = ?",
                new String[]{id}
        );
    }

    //
    public int resetFinanceiroNFe() {
        myDataBase = this.getWritableDatabase();
        return myDataBase.delete("financeiro_nfe", null, null);
    }

    //
    public int resetProdutosPedidoNFe() {
        myDataBase = this.getWritableDatabase();
        return myDataBase.delete("produtos_pedido_nfe", null, null);
    }

    //SOMAR O VALOR DO FINANCEIRO
    public String getValorTotalFinanceiroNFE() {
        myDataBase = this.getReadableDatabase();

        String selectQuery = "SELECT SUM(valor) FROM financeiro_nfe";
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);
        String total = "0.0";

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            total = cursor.getString(0);
        }

        cursor.close();
        if (total == null) {
            total = "0.0";
        }
        return total;
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getValoresFinanceiroNFe(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT valor * 100 FROM financeiro_nfe WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        int q = cursor.getCount();
        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0));

                q--;
                if (q != 0) {
                    formasPag.append(",");
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getIdsFormasPagamentoNFe(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT id_forma_pagamento FROM financeiro_nfe WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        int q = cursor.getCount();
        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0));

                q--;
                if (q != 0) {
                    formasPag.append(",");
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }

    public String getParcelaFormasPagamentoNFe(String id_pedido) {
        StringBuilder parcelasPag = new StringBuilder();

        String query = "SELECT parcelas FROM financeiro_nfe WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        int q = cursor.getCount();
        if (cursor.moveToFirst()) {
            do {
                parcelasPag.append(cursor.getString(0));

                q--;
                if (q != 0) {
                    parcelasPag.append(",");
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return parcelasPag.toString();
    }

    public String getVencimentoFormasPagamentoNFe(String id_pedido) {
        StringBuilder vencimentoPag = new StringBuilder();

        String query = "SELECT vencimento FROM financeiro_nfe WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        int q = cursor.getCount();
        if (cursor.moveToFirst()) {
            do {
                vencimentoPag.append(cursor.getString(0));

                q--;
                if (q != 0) {
                    vencimentoPag.append(",");
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return vencimentoPag.toString();
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getNAutFinanceiroNfe(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT codigo_autorizacao FROM financeiro_nfe WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        int q = cursor.getCount();
        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0));

                q--;
                if (q != 0) {
                    formasPag.append(",");
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }

    //LISTAR TODOS OS ITENS DO FINANCEIRO
    public String getBandeirasFinanceiroNFe(String id_pedido) {
        StringBuilder formasPag = new StringBuilder();

        String query = "SELECT bandeira FROM financeiro_nfe WHERE id_pedido = '" + id_pedido + "'";
        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        int q = cursor.getCount();
        if (cursor.moveToFirst()) {
            do {
                formasPag.append(cursor.getString(0));

                q--;
                if (q != 0) {
                    formasPag.append(",");
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        return formasPag.toString();
    }

    // ATUALIZA O PEDIDO TEMPORARIO PARA ON, INFORMANDO QUE O FINANCEIRO FOI FINALIZADO
    public void upadtePedidoTemp(String idTemp) {
        try {
            myDataBase = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("situacao", "ON");
            myDataBase.update("pedidos_temp", values, "id=" + idTemp, null);
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    public List<String> getMinMaxFracionamentoProduto(String codigo) {
        List<String> minMax = new ArrayList<>();

        String query = "SELECT minfrac_produto, maxfrac_produto " +
                "FROM produtos pro " +
                "WHERE pro.codigo = '" + codigo + "'";

        Log.e("SQL", query);

        myDataBase = this.getReadableDatabase();
        Cursor cursor = myDataBase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(0) != null) {
                    minMax.add(cursor.getString(0));
                    minMax.add(cursor.getString(1));
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        if (minMax.isEmpty()) {
            minMax.add("0");
            minMax.add("0");
        }
        return minMax;
    }


    private void apagarDadosGerais(String idPedido) {
        myDataBase = this.getWritableDatabase();

        //
        myDataBase.delete(
                "formas_pagamento_pedidos",
                "id_pedido = ?",
                new String[]{idPedido}
        );

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

    public void deleteAllTables() {
        // Lista de todas as tabelas no seu banco de dados
        String[] allTables = {
                "android_metadata",
                "autorizacoes",
                "autorizacoes_pinpad",
                "boletos",
                "clientes",
                "contas_bancarias",
                "credenciadoras",
                "financeiro_nfce",
                "financeiro_nfe",
                "formas_pagamento_pedidos",
                "itens_pedidos",
                "itens_pedidosNFE",
                "pedidos",
                "pedidos_temp",
                "pedidosNFE",
                "pos",
                "produtos",
                "produtos_pedido",
                "produtos_pedido_nfe",
                "unidades"
        };

        // Iterar sobre a lista de tabelas e excluir cada uma delas
        for (String table : allTables) {
            String deleteTableQuery = "DROP TABLE IF EXISTS " + table + ";";
            myDataBase.execSQL(deleteTableQuery);
        }
    }

    public void FecharConexao(Context context) {
        try {
            // DELETA TODAS AS TABELAS
            deleteAllTables();
            // FECHAR A CONEXÃO COM BANCO
            myDataBase.close();
            // EXCLUI O BANCO DE DADOS
            context.deleteDatabase("emissorwebDB");
        } catch (Exception e) {
            Log.e("Banco", e.getMessage());
        }
    }

}
