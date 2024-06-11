package br.com.zenitech.emissorweb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

import br.com.zenitech.emissorweb.util.MaskTelefone;
import stone.application.StoneStart;
import stone.user.UserModel;

public class PromocaoCopaEnergia extends AppCompatActivity {
    VerificarOnline verificarOnline;
    Context context;
    ImageView logo;
    List<UserModel> user;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_promocao_copa_energia);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        context = this;
        verificarOnline = new VerificarOnline();

        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);

        logo = findViewById(R.id.logo);

        //
        user = StoneStart.init(context);

        EditText cpf = findViewById(R.id.etCpf);
        cpf.addTextChangedListener(MaskUtil.insert(cpf, MaskUtil.MaskType.AUTO));
        cpf.setText(prefs.getString("cpfPromo", ""));

        EditText telefone = findViewById(R.id.etTelefone);
        telefone.addTextChangedListener(MaskTelefone.insert("(##)#####-####", telefone));

        EditText serieNF = findViewById(R.id.etSerie);
        serieNF.setText(prefs.getString("seriePromo", ""));

        EditText numNF = findViewById(R.id.etNumero);
        numNF.setText(prefs.getString("numeroPromo", ""));

        EditText quant = findViewById(R.id.etQuantidade);
        quant.setText(prefs.getString("quantPromo", ""));

        findViewById(R.id.btnCupons).setOnClickListener(v -> {
            Intent i = new Intent(this, ImpressoraPOS.class);
            //i.putExtra("serie_nf", serieNF.getText().toString());
            //i.putExtra("numero_nf", numNF.getText().toString());
            i.putExtra("imprimir", "promocao_copa_energia");
            startActivity(i);
            finish();
        });
    }
}