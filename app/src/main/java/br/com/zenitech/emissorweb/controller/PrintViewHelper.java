package br.com.zenitech.emissorweb.controller;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;

public class PrintViewHelper {
    private final int WIDTH_DEFAULT = 360;
    private final int POSITION_DEFAULT = 0;
    private final int UNSPECIFIED_SIZE = 0;
    private final int DEFAULT_PADDING = 0;

    public Bitmap generateBitmapFromView(View view) {
        view.setPadding(DEFAULT_PADDING, DEFAULT_PADDING,
                DEFAULT_PADDING, DEFAULT_PADDING);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(View.MeasureSpec.makeMeasureSpec(WIDTH_DEFAULT, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(UNSPECIFIED_SIZE, View.MeasureSpec.UNSPECIFIED));
        view.layout(POSITION_DEFAULT, POSITION_DEFAULT, view.getMeasuredWidth(),
                view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
