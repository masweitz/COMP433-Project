package com.example.COMP433_Project;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

public class MyDrawingArea extends View {
    public MyDrawingArea(Context context) {
        super(context);
    }

    public MyDrawingArea(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDrawingArea(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyDrawingArea(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    Path path = new Path();
    int cx = 450, cy = 150;
    int dx = 5, dy = 5;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(20f);
        p.setTextSize(50);
        canvas.drawRect(0,0,getWidth(),getHeight(),p);
        p.setStrokeWidth(2f);
        p.setStyle(Paint.Style.FILL);
        canvas.drawText("Drawing Area", cx, cy, p);
        cx+=dx;
        cy+=dy;

        if((cx+300) > getWidth() || (cx-5) <0){
            dx = -dx;
        }

        if((cy-5) > getHeight() || (cy-20) <0){
            dy = -dy;
        }

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(5f);
        canvas.drawPath(path, p);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        cx=1000;
        cy=1000;
        dx=0;
        dy=0;
        float x = event.getX(), y = event.getY();
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN){
            path.moveTo(x, y);
        }
        else if(action == MotionEvent.ACTION_MOVE){
            path.lineTo(x, y);
        }
        return true;
    }


    public void clear(){
        cx=450;
        cy=250;
        dx=5;
        dy=5;
        path.reset();
        invalidate();
    }

    Bitmap bmp;
    public Bitmap getBitmap(){
        bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setStyle(Paint.Style.STROKE);
        p.setAntiAlias(true);
        p.setStrokeWidth(5f);
        c.drawColor(Color.WHITE);
        c.drawPath(path, p);
        return bmp;
    }


}
