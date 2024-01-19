package br.com.zenitech.emissorweb.util;

public class AuxFinanceiroNFCe {
    //region ID REQUEST PAGAMENTOS
    public static int PAGAMENTO_REQUEST = 1;
    public static int PAGAMENTO_PIX_REQUEST = 2;
    //endregion

    //region FORMAS DE PAGAMENTO (A FORMA DE PAGAMENTO "OUTROS" FOI RETIRADA)
    public String[] listaFormasPagamento = {
            "DINHEIRO",
            "CARTÃO DE CRÉDITO",
            "CARTÃO DE DÉBITO",
            "PAGAMENTO INSTANTÂNEO (PIX)",
            "BOLETO"
    };
    //endregion

    //region FORMAS DE PAGAMENTO DINHEIRO E PIX (PARA PAGAMENTO DE NOTAS FRACIONADAS)
    public String[] listaFormasPagamentoDinheiro = {
            "DINHEIRO",
            "PAGAMENTO INSTANTÂNEO (PIX)"
    };
    //endregion

    //region BANDEIRAS DAS CREDENCIADORAS
    public String[] listaBandeirasCredenciadoras = {
            "BANDEIRA",
            "Visa",
            "Mastercard",
            "American Express",
            "Sorocred",
            "Diners Club",
            "Elo",
            "Hipercard",
            "Aura",
            "Cabal",
            "Outros"
    };
    //endregion
}
