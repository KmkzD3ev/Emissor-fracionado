package br.com.zenitech.emissorweb.util;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MyCountDownTimer extends CountDownTimer {
    private Context context;
    private TextView tv;
    private long timeInFuture;
    private long interval;

    public MyCountDownTimer(Context context, TextView tv, long timeInFuture, long interval) {
        super(timeInFuture, interval);
        this.context = context;
        this.tv = tv;
        this.timeInFuture = timeInFuture;
        this.interval = interval;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        timeInFuture = millisUntilFinished;
        tv.setText(String.format("%s:%s", getCorretcTimer(true, millisUntilFinished), getCorretcTimer(false, millisUntilFinished)));
    }

    @Override
    public void onFinish() {
        timeInFuture -= 1000;
        tv.setText(String.format("%s:%s", getCorretcTimer(true, timeInFuture), getCorretcTimer(false, timeInFuture)));

        Toast.makeText(context, "Tempo Acabou! Gere outro QrCode para pagamento!", Toast.LENGTH_SHORT).show();
        ((Activity) (context)).finish();
    }

    private String getCorretcTimer(boolean isMinute, long millisUntilFinished) {
        String aux;
        int constCalendar = isMinute ? Calendar.MINUTE : Calendar.SECOND;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millisUntilFinished);

        aux = c.get(constCalendar) < 10 ? "0" + c.get(constCalendar) : "" + c.get(constCalendar);
        return (aux);
    }
}
