package br.com.zenitech.emissorweb.util;

public class ActionCodeStone {
    //
    public String getError(String error) {
        String result = "";
        if (error.equalsIgnoreCase("UNKNOWN")) {
            result = "Ocorreu um erro antes de ser enviada para o autorizador.";
        }
        //
        else if (error.equalsIgnoreCase("DECLINED")) {
            result = "Transação negada.";
        }
        //
        else if (error.equalsIgnoreCase("DECLINED_BY_CARD")) {
            result = "Transação negada pelo cartão.";
        }
        //
        else if (error.equalsIgnoreCase("CANCELLED")) {
            result = "Transação cancelada (ocorre no cancelamento, ou seja, CancellationProvider).";
        }
        //
        else if (error.equalsIgnoreCase("PARTIAL_APPROVED")) {
            result = "Transação foi parcialmente aprovada.";
        }
        //
        else if (error.equalsIgnoreCase("TECHNICAL_ERROR")) {
            result = "Erro técnico (ocorreu um erro ao processar a mensagem no autorizador).";
        }
        //
        else if (error.equalsIgnoreCase("REJECTED")) {
            result = "Transação rejeitada.";
        }
        //
        else if (error.equalsIgnoreCase("WITH_ERROR")) {
            result = "Transação não completada com sucesso. O Provedor de Reversão irá desfazer as transações com este status.";
        }
        //
        else if (error.equalsIgnoreCase("PENDING")) {
            result = "O provider de transação está em andamento.";
        }
        //
        else if (error.equalsIgnoreCase("REVERSED")) {
            result = "A transação foi cancelada automaticamente devido à algum erro ou interrupção no fluxo de pagamento.";
        }
        //
        else if (error.equalsIgnoreCase("PENDING_REVERSAL")) {
            result = "Transação foi interrompida por algum motivo e será revertida pelo provider de reversão.";
        }

        return result;// String.format("%s\nQualquer dúvida ou necessitando de mais informações, contate o suporte.", );
    }
}
