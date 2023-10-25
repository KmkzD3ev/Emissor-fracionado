package br.com.zenitech.emissorweb.pagamentos;

import static br.com.zenitech.emissorweb.Configuracoes.getApplicationName;

import android.content.Context;
import android.widget.Toast;

import java.util.List;

import stone.application.StoneStart;
import stone.application.interfaces.StoneCallbackInterface;
import stone.providers.ActiveApplicationProvider;
import stone.user.UserModel;
import stone.utils.Stone;

public class IniciarStone {

    public static IniciarStone instance;
    Context context;
    String STONE_CODE;

    public IniciarStone(Context context, String STONE_CODE) {
        this.context = context;
        this.STONE_CODE = STONE_CODE;

        iniciar(false);
    }

    public static IniciarStone getInstance(Context context, String STONE_CODE) {
        if (instance == null)
            instance = new IniciarStone(context, STONE_CODE);
        return instance;
    }

    // Iniciar o Stone
    public UserModel iniciar(boolean ativar) {

        List<UserModel> userList = StoneStart.init(context);
        Stone.setAppName(getApplicationName(context));

        //ativarStoneCode();
        // Quando é retornado null, o SDK ainda não foi ativado
        if (userList == null && ativar) {
            // Inicia a ativação do SDK
            if (ativarStoneCode())
                return Stone.sessionApplication.getUserModelList().get(0);
            else
                return null;

        } else
            return userList != null ? userList.get(0) : null;
    }

    //
    private boolean ativarStoneCode() {
        final boolean[] ativo = {false};
        ActiveApplicationProvider activeApplicationProvider = new ActiveApplicationProvider(context);
        activeApplicationProvider.setDialogMessage("Ativando o Stone Code");
        activeApplicationProvider.setDialogTitle("Aguarde");
        activeApplicationProvider.useDefaultUI(true);
        activeApplicationProvider.setConnectionCallback(new StoneCallbackInterface() {

            public void onSuccess() {
                // SDK ativado com sucesso
                //Toast.makeText(context, "Stone Code:" + STONE_CODE + " ativado com sucesso!", Toast.LENGTH_SHORT).show();
                ativo[0] = true;
            }

            public void onError() {
                Toast.makeText(context, "Não foi possível ativar o Stone Code:" + STONE_CODE, Toast.LENGTH_SHORT).show();
            }
        });

        Toast.makeText(context, STONE_CODE, Toast.LENGTH_SHORT).show();
        activeApplicationProvider.activate(STONE_CODE);

        return ativo[0];
    }
}
