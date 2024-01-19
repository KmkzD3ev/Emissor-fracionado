package br.com.zenitech.emissorweb;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ClassAuxiliar {

    //FORMATAR DATA - INSERIR E EXIBIR
    private SimpleDateFormat inserirDataFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat exibirDataFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat exibirDataFormat_dataHora = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat anoMesChaveNFCE = new SimpleDateFormat("yyMM", Locale.getDefault());
    //FORMATAR HORA
    private SimpleDateFormat dateFormat_hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private Date data = new Date();
    private Calendar cal = Calendar.getInstance();


    //ANO E MÊS ATUAL DO SISTEMA - pt-BR / PARA A CHAVE DA NOTA NFC-E
    public String anoMesAtual() {
        cal.setTime(data);
        Date data_atual = cal.getTime();

        return anoMesChaveNFCE.format(data_atual);
    }

    //EXIBIR DATA ATUAL DO SISTEMA - pt-BR
    public String exibirDataAtual() {
        cal.setTime(data);
        Date data_atual = cal.getTime();

        return exibirDataFormat.format(data_atual);
    }

    //INSERIR DATA ATUAL DO SISTEMA
    public String inserirDataAtual() {
        cal.setTime(data);
        Date data_atual = cal.getTime();

        return inserirDataFormat.format(data_atual);
    }

    //EXIBIR DATA
    public String exibirData(String data) {
        String CurrentString = data;
        String[] separated = CurrentString.split("-");
        data = separated[2] + "/" + separated[1] + "/" + separated[0];

        return data;
    }

    //INSERIR DATA
    public String inserirData(String data) {
        String CurrentString = data;
        String[] separated = CurrentString.split("/");
        data = separated[2] + "-" + separated[1] + "-" + separated[0];

        return data;
    }

    //EXIBIR HORA ATUAL
    public String horaAtual() {
        cal.setTime(data);
        Date data_atual = cal.getTime();

        return dateFormat_hora.format(data_atual);
    }

    //TIMESTAMP
    public String timeStamp() {
        //String timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";

        //long millis = new Date().getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss", Locale.getDefault());

        return dateFormat.format(new Date());
    }

    //SOMAR VALORES
    public BigDecimal somar(String[] args) {
        BigDecimal valor = new BigDecimal("0.0");

        //
        for (String v : args) {
            valor = new BigDecimal(String.valueOf(valor)).add(new BigDecimal(v));
            //
            Log.e("TOTAL", "SOMAR = " + valor);
        }
        return valor;
    }

    //SOMAR VALORES
    public BigDecimal somar_valores(String[] args) {
        //BigDecimal valor = new BigDecimal("0.0");

        //
        /*for (String v : args) {
            valor = new BigDecimal(String.valueOf(valor)).add(new BigDecimal(v));
            //
            Log.e("TOTAL", "SOMAR = " + valor);
        }*/
        BigDecimal valor = new BigDecimal(args[1]).add(new BigDecimal(args[0]));
        Log.e("TOTAL", "SOMAR NOVO = " + valor);
        return valor;
    }

    //SUBTRAIR VALORES
    public BigDecimal subitrair(String[] args) {
        BigDecimal valor = new BigDecimal(args[0]).subtract(new BigDecimal(args[1]));

        //
        Log.e("TOTAL", "SUBTRAIR" + valor);
        return valor;
    }

    //MULTIPLICAR VALORES
    public BigDecimal multiplicar(String[] args) {
        BigDecimal valor = new BigDecimal(args[0]).multiply(new BigDecimal(args[1]));

        //
        Log.e("TOTAL", "MULTIPLICAR: " + valor);
        return valor;
    }

    //DIVIDIR VALORES
    public BigDecimal dividir(String[] args) {
        BigDecimal valor = new BigDecimal(args[0]).divide(new BigDecimal(args[1]), 3, RoundingMode.UP);

        //
        Log.e("TOTAL", "DIVIDIR: " + valor);
        return valor;
    }

    //COMPARAR VALORES
    public int comparar(String[] args) {
        int valor = new BigDecimal(args[0]).compareTo(new BigDecimal(args[1]));
        //
        Log.e("TOTAL", "COMPARAR: " + valor);
        return valor;
    }

    //CONVERTER VALORES PARA CALCULO E INSERÇÃO NO BANCO DE DADOS
    public BigDecimal converterValores(String value) {
        BigDecimal parsed = null;
        try {
            //String cleanString = value.replaceAll("[R,$,.]", "");
            parsed = new BigDecimal(this.soNumeros(value)).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);

            Log.e("TOTAL", "FORAMATAR NUMERO: " + parsed);
        } catch (Exception e) {
            Log.e("sua_tag", e.getMessage(), e);
        }
        return parsed;
    }

    //CONVERTER VALORES PARA CALCULO E INSERÇÃO NO BANCO DE DADOS
    public String converterValoresNota(String value) {
        String text = String.valueOf(this.converterValores(value));

        return text.replaceAll(".", "");
    }

    //
    public String maskMoney(BigDecimal valor) {
        /*NumberFormat formato1 = NumberFormat.getCurrencyInstance();
        NumberFormat formato2 = NumberFormat.getCurrencyInstance(new Locale("en", "EN"));
        NumberFormat formato3 = NumberFormat.getIntegerInstance();
        NumberFormat formato4 = NumberFormat.getPercentInstance();
        NumberFormat formato5 = new DecimalFormat(".##");
        NumberFormat formato6 = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        //
        String valorFormat = valor;

        valorFormat = formato5.format(valor);*/

        //
        //texto.setText(formato1.format(valor));
        /*Log.i("Moeda atual", formato1.format(valor));
        Log.i("Moeda EUA", formato2.format(valor));
        Log.i("Número inteiro", formato3.format(valor));
        Log.i("Porcentagem", formato4.format(valor));
        Log.i("Decimal", formato5.format(valor));
*/
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) nf).getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol("");
        ((DecimalFormat) nf).setDecimalFormatSymbols(decimalFormatSymbols);
        nf.setMinimumFractionDigits(2);
        //System.out.println(nf.format(12345.124).trim());

        return nf.format(valor);
    }

    /*public static void main(String[] args) {
        System.out.println("Subtrai");
        System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.1")));

        System.out.println("");
        System.out.println("Soma");
        System.out.println(new BigDecimal("2.00").add(new BigDecimal("1.2")));

        System.out.println("");
        System.out.println("Compara");
        System.out.println(new BigDecimal("2.00").compareTo(new BigDecimal("1.3")));

        System.out.println("");
        System.out.println("Divide");
        System.out.println(new BigDecimal("2.00").divide(new BigDecimal("2.00")));

        System.out.println("");
        System.out.println("Máximo");
        System.out.println(new BigDecimal("2.00").max(new BigDecimal("1.5")));

        System.out.println("");
        System.out.println("Mínimo");
        System.out.println(new BigDecimal("2.00").min(new BigDecimal("1.6")));

        System.out.println("");
        System.out.println("Potência");
        System.out.println(new BigDecimal("2.00").pow(2));

        System.out.println("");
        System.out.println("Multiplica");
        System.out.println(new BigDecimal("2.00").multiply(new BigDecimal("1.8")));

    }*/

    //DEIXAR A PRIMEIRA LETRA DA STRING EM MAIUSCULO
    public String maiuscula1(String palavra) {
        //betterIdea = Character.toUpperCase(userIdea.charAt(0)) + userIdea.substring(1);
        palavra = palavra.trim();
        palavra = Character.toUpperCase(palavra.charAt(0)) + palavra.substring(1);
        //return palavra.substring(0, 1).toUpperCase() + palavra.substring(1);
        return palavra;
    }

    //SÓ NÚMEROS
    public String soNumeros(String txt) {
        String numero = txt;

        numero = numero.replaceAll("[^0-9]*", "");

        return numero;
    }

    //MASCARA DATA PROTOCOLO
    String exibirDataProtocolo(String data) {

        Log.i("auxKle", data);

        String dat = "";
        String dia = "", mes = "", ano = "";
        //
        for (char d : data.toCharArray()) {
            dat = dat + d;

            if (dat.length() <= 4) {
                ano = ano + d;
            } else if (dat.length() <= 6) {
                mes = mes + d;
            } else if (dat.length() <= 8) {
                dia = dia + d;
            }
        }

        dat = dia + "/" + mes + "/" + ano;

        return dat;
    }

    //MASCARA DATA PROTOCOLO
    public String exibirHoraProtocolo(String hora) {

        Log.i("auxKle", hora);

        String hor = "";
        String h = "", m = "", s = "";
        //
        for (char d : hora.toCharArray()) {
            hor = hor + d;

            if (hor.length() <= 2) {
                h = h + d;
            } else if (hor.length() <= 4) {
                m = m + d;
            } else if (hor.length() <= 62) {
                s = s + d;
            }
        }

        hor = h + ":" + m + ":" + s;

        return hor;
    }

    //MODULO 11 PARA GERAR O DIGITO VERIFICADOR DA NOTA NFC-E
    private static int MODULO11 = 11;

    public String digitoVerificado(String chave) {
        int[] pesos = {4, 3, 2, 9, 8, 7, 6, 5};
        int somaPonderada = 0;
        for (int i = 0; i < chave.length(); i++) {
            somaPonderada += pesos[i % 8] * (Integer.parseInt(chave.substring(i, i + 1)));
        }
        int DV = (MODULO11 - somaPonderada % MODULO11);
        if (DV >= 10 || DV == 0 || DV == 1) {
            DV = 0;
        }
        return chave + DV;
    }

    //RETORNA O ID DO ESTADO
    String idEstado(String uf) {
        String id = null;
        //Codificação da UF definida pelo IBGE:
        switch (uf) {
            //11-Rondônia
            case "RO":
                id = "11";
                break;
            //12-Acre
            case "AC":
                id = "12";
                break;
            //13-Amazonas
            case "AM":
                id = "13";
                break;
            //14-Roraima
            case "RR":
                id = "14";
                break;
            //15-Pará
            case "PA":
                id = "15";
                break;
            //16-Amapá
            case "AP":
                id = "16";
                break;
            //17-Tocantins
            case "TO":
                id = "17";
                break;
            //21-Maranhão
            case "MA":
                id = "21";
                break;
            //22-Piauí
            case "PI":
                id = "22";
                break;
            //23-Ceará
            case "CE":
                id = "23";
                break;
            //24-Rio Grande do Norte
            case "RN":
                id = "24";
                break;
            //25-Paraíba
            case "PB":
                id = "25";
                break;
            //26-Pernambuco
            case "PE":
                id = "26";
                break;
            //27-Alagoas
            case "AL":
                id = "27";
                break;
            //28-Sergipe
            case "SE":
                id = "28";
                break;
            //29-Bahia
            case "BH":
                id = "29";
                break;
            //31-Minas Gerais
            case "MG":
                id = "31";
                break;
            //32-Espírito Santo
            case "ES":
                id = "32";
                break;
            //33-Rio de Janeiro
            case "RJ":
                id = "33";
                break;
            //35-São Paulo
            case "SP":
                id = "35";
                break;
            //41-Paraná
            case "PR":
                id = "41";
                break;
            //42-Santa Catarina
            case "SC":
                id = "42";
                break;
            //43-Rio Grande do Sul
            case "RS":
                id = "43";
                break;
            //50-Mato Grosso do Sul
            case "MS":
                id = "50";
                break;
            //51-Mato Grosso
            case "MT":
                id = "51";
                break;
            //52-Goiás
            case "GO":
                id = "52";
                break;
            //53-Distrito Federal
            case "DF":
                id = "53";
                break;
        }

        return id;
    }

    static String getSha1Hex(String clearString) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(clearString.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes) {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    private String unmask(String s) {
        return s.replaceAll("[^0-9]*", "");
    }

    public String mask(String txt) {

        String maskCNPJ = "##.###.###/####-##";
        String oldValue = "";

        String str = unmask(txt);

        StringBuilder mascara = new StringBuilder();
        int i = 0;
        for (char m : maskCNPJ.toCharArray()) {
            if ((m != '#' && str.length() > oldValue.length()) || (m != '#' && str.length() < oldValue.length() && str.length() != i)) {
                mascara.append(m);
                continue;
            }

            try {
                mascara.append(str.charAt(i));
            } catch (Exception e) {
                break;
            }
            i++;
        }

        return mascara.toString();
    }

    public String getIdFormaPagamento(String s) {
        s = this.removerAcentos(s);
        String idFormaPagamento = "";
        if (Objects.requireNonNull(s).equalsIgnoreCase("DINHEIRO")) {
            idFormaPagamento = "1";

        } else if (s.equalsIgnoreCase("CHEQUE")) {
            idFormaPagamento = "2";

        } else if (s.equalsIgnoreCase("CARTAO DE CREDITO")) {
            idFormaPagamento = "3";

        } else if (s.equalsIgnoreCase("CARTAO DE DEBITO")) {
            idFormaPagamento = "4";

        } else if (s.equalsIgnoreCase("CREDITO LOJA")) {
            idFormaPagamento = "5";

        } else if (s.equalsIgnoreCase("VALE ALIMENTACAO")) {
            idFormaPagamento = "10";

        } else if (s.equalsIgnoreCase("VALE REFEICAO")) {
            idFormaPagamento = "11";

        } else if (s.equalsIgnoreCase("VALE PRESENTE")) {
            idFormaPagamento = "12";

        } else if (s.equalsIgnoreCase("VALE COMBUSTIVEL")) {
            idFormaPagamento = "13";

        } else if (s.equalsIgnoreCase("BOLETO")) {
            idFormaPagamento = "15";

        } else if (s.equalsIgnoreCase("PAGAMENTO INSTANTANEO (PIX)")) {
            idFormaPagamento = "17";

        } else if (s.equalsIgnoreCase("OUTROS")) {
            idFormaPagamento = "99";
        } else if (s.equalsIgnoreCase("DUPLICATA MERCANTIL")) {
            idFormaPagamento = "0";
        }

        return idFormaPagamento;
    }

    public String getNomeFormaPagamento(String s) {
        s = this.removerAcentos(s);
        String nomeGPG = "";
        if (Objects.requireNonNull(s).equalsIgnoreCase("1")) {
            nomeGPG = "DINHEIRO";

        } else if (s.equalsIgnoreCase("2")) {
            nomeGPG = "CHEQUE";

        } else if (s.equalsIgnoreCase("3")) {
            nomeGPG = "CARTAO DE CREDITO";

        } else if (s.equalsIgnoreCase("4")) {
            nomeGPG = "CARTAO DE DEBITO";

        } else if (s.equalsIgnoreCase("5")) {
            nomeGPG = "CREDITO LOJA";

        } else if (s.equalsIgnoreCase("10")) {
            nomeGPG = "VALE ALIMENTACAO";

        } else if (s.equalsIgnoreCase("11")) {
            nomeGPG = "VALE REFEICAO";

        } else if (s.equalsIgnoreCase("12")) {
            nomeGPG = "VALE PRESENTE";

        } else if (s.equalsIgnoreCase("13")) {
            nomeGPG = "VALE COMBUSTIVEL";

        } else if (s.equalsIgnoreCase("15")) {
            nomeGPG = "BOLETO";

        } else if (s.equalsIgnoreCase("17")) {
            nomeGPG = "PAGAMENTO INSTANTANEO (PIX)";

        } else if (s.equalsIgnoreCase("99")) {
            nomeGPG = "OUTROS";
        } else if (s.equalsIgnoreCase("0")) {
            nomeGPG = "DUPLICATA MERCANTIL";
        }

        return nomeGPG;
    }

    public String getIdBandeira(String s) {
        s = this.removerAcentos(s);
        String idFormaPagamento = "";
        if (Objects.requireNonNull(s).equalsIgnoreCase("Visa")) {
            idFormaPagamento = "1";

        } else if (s.equalsIgnoreCase("Mastercard")) {
            idFormaPagamento = "2";

        } else if (s.equalsIgnoreCase("American Express")) {
            idFormaPagamento = "3";

        } else if (s.equalsIgnoreCase("Sorocred")) {
            idFormaPagamento = "4";

        } else if (s.equalsIgnoreCase("Diners Club")) {
            idFormaPagamento = "5";

        } else if (s.equalsIgnoreCase("Elo")) {
            idFormaPagamento = "6";

        } else if (s.equalsIgnoreCase("Hipercard")) {
            idFormaPagamento = "7";

        } else if (s.equalsIgnoreCase("Aura")) {
            idFormaPagamento = "8";

        } else if (s.equalsIgnoreCase("Cabal")) {
            idFormaPagamento = "9";

        } else if (s.equalsIgnoreCase("Outros")) {
            idFormaPagamento = "99";
        }

        return idFormaPagamento;
    }

    public void ShowMsgSnackbar(View view, String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void ShowMsgToast(Context context, String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void ShowMsgLog(String tag, String msg) {
        Log.e(tag, msg);
    }

    // Retorna a lista de formas de pagamento aceita pela SEFAZ
    public String[] FormasDePagamentoEmissor() {
        return new String[]{"DINHEIRO", "CARTÃO DE CRÉDITO", "CARTÃO DE DÉBITO",
                "PAGAMENTO INSTANTÂNEO (PIX)", "BOLETO"};
    }

    // FPRMAS DE PAGAMENTO EMISSOR NFE
    public String[] FormasDePagamentoEmissorNFe(boolean duplicata) {
        if (duplicata) {
            return new String[]{
                    "DINHEIRO", "CARTÃO DE CRÉDITO", "CARTÃO DE DÉBITO",
                    "PAGAMENTO INSTANTÂNEO (PIX)", "BOLETO", "DUPLICATA MERCANTIL"};
        } else {
            return new String[]{
                    "DINHEIRO", "CARTÃO DE CRÉDITO", "CARTÃO DE DÉBITO",
                    "PAGAMENTO INSTANTÂNEO (PIX)", "BOLETO"};
        }
    }


    // Retorna a lista de credenciadoras
    public String[] BandeirasCredenciadoras() {
        return new String[]{"BANDEIRA", "Visa", "Mastercard", "American Express", "Sorocred",
                "Diners Club", "Elo", "Hipercard", "Aura", "Cabal", "Outros"};
    }

    // Retorna a lista de parcelas do cartao de credito
    public String[] ParcelasCartaoCredito() {
        return new String[]{"1X (À VISTA)", "2X SEM JUROS", "3X SEM JUROS", "4X SEM JUROS",
                "5X SEM JUROS", "6X SEM JUROS", "7X SEM JUROS", "8X SEM JUROS", "9X SEM JUROS",
                "10X SEM JUROS", "11X SEM JUROS", "12X SEM JUROS"};
    }
    public String[] ParcelasDuplicatas() {
        return new String[]{
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10",
                "11",
                "12",
        };
    }

    public String dataFutura(int dias) {

        cal.setTime(data);
        cal.add(Calendar.DAY_OF_MONTH, dias);
        Date dataFutura = cal.getTime();
        String dataReturn = exibirDataFormat.format(dataFutura);
        Log.i("DataFutura", exibirDataFormat.format(cal.getTime()));
        return dataReturn;// exibirDataFormat.format(cal.getTime());
    }

    public static class MoneyTextWatcher implements TextWatcher {
        private final WeakReference<EditText> editTextWeakReference;

        public MoneyTextWatcher(EditText editText) {
            editTextWeakReference = new WeakReference<>(editText);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            EditText editText = editTextWeakReference.get();
            if (editText == null) return;
            String s = editable.toString();
            editText.removeTextChangedListener(this);
            String cleanString = s.replaceAll("[^0-9]", "");
            BigDecimal parsed = new BigDecimal(cleanString).setScale(2, RoundingMode.FLOOR).divide(new BigDecimal(100), RoundingMode.FLOOR);
            String formatted = NumberFormat.getCurrencyInstance().format(parsed);
            editText.setText(formatted);
            editText.setSelection(formatted.length());
            editText.addTextChangedListener(this);
        }
    }

    public static class VerifyQuaint implements TextWatcher {
        private final WeakReference<EditText> editTextWeakReference;

        public VerifyQuaint(EditText editText) {
            editTextWeakReference = new WeakReference<>(editText);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            EditText editText = editTextWeakReference.get();
            if (editText == null) return;
            String s = editable.toString();
            editText.removeTextChangedListener(this);
            String cleanString = s.replaceAll("[^0-9]", "");
            editText.setText(cleanString);
            editText.setSelection(cleanString.length());
            editText.addTextChangedListener(this);
        }
    }
}
