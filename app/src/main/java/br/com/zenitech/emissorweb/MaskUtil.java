package br.com.zenitech.emissorweb;


import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

public class MaskUtil {

    private static final String CPFMask = "###.###.###-##";
    private static final String CNPJMask = "##.###.###/####-##";

    public static String unmask(String s) {
        return s.replaceAll("[^0-9]*", "");
    }

    private static String getDefaultMask(String str) {
        String defaultMask = CPFMask;
        if (str.length() > 11) {
            defaultMask = CNPJMask;
        }
        return defaultMask;
    }

    public enum MaskType {
        CPF,
        CNPJ,
        AUTO
    }

    public static TextWatcher insert(final EditText editText, final MaskType maskType) {
        return new TextWatcher() {

            boolean isUpdating;
            String oldValue = "";

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String value = MaskUtil.unmask(s.toString());
                String mask;
                switch (maskType) {
                    case CPF:
                        mask = CPFMask;
                        break;
                    case CNPJ:
                        mask = CNPJMask;
                        break;
                    case AUTO:
                        mask = getDefaultMask(value);
                        break;
                    default:
                        mask = getDefaultMask(value);
                        break;
                }

                String maskAux = "";
                if (isUpdating) {
                    oldValue = value;
                    isUpdating = false;
                    return;
                }
                int i = 0;
                for (char m : mask.toCharArray()) {
                    if ((m != '#' && value.length() > oldValue.length()) || (m != '#' && value.length() < oldValue.length() && value.length() != i)) {
                        maskAux += m;
                        continue;
                    }

                    try {
                        maskAux += value.charAt(i);
                    } catch (Exception e) {
                        break;
                    }
                    i++;
                }
                isUpdating = true;
                editText.setText(maskAux);
                editText.setSelection(maskAux.length());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        };
    }

    public static String maskCnpj(String cnpj) {
        String cleanedCnpj = unmask(cnpj);
        StringBuilder maskAux = new StringBuilder();

        int cnpjIndex = 0;
        for (char m : CNPJMask.toCharArray()) {
            if (m != '#') {
                maskAux.append(m);
            } else {
                if (cnpjIndex < cleanedCnpj.length()) {
                    maskAux.append(cleanedCnpj.charAt(cnpjIndex));
                    cnpjIndex++;
                } else {
                    // Break se não houver mais dígitos no CNPJ
                    break;
                }
            }
        }

        return maskAux.toString();
    }
}
