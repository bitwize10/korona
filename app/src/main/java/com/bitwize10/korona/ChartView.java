package com.bitwize10.korona;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class ChartView extends View {

    private Paint mPaint1, mPaint2;
    private int mColor1r, mColor2r; // red
    private int mColor1g, mColor2g; // green

    private int mWidth = 0;
    private int mHeight = 0;

    private int[] mData; // data for a given country

    public ChartView(Context context) {
        super(context);
        init();
    }

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {

        mColor1r = getResources().getColor(R.color.colorAccent);
        mColor2r = getResources().getColor(R.color.colorPrimaryTransparent);

        mColor1g = getResources().getColor(R.color.green);
        mColor2g = getResources().getColor(R.color.darkerGreenTransparent);

        mPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint1.setStyle(Paint.Style.STROKE);
        mPaint1.setStrokeWidth(dp2px(2));
        mPaint1.setStrokeCap(Paint.Cap.ROUND);
        mPaint1.setStrokeJoin(Paint.Join.MITER);

        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint2.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint2.setStrokeWidth(dp2px(1));

    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawData(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        setMeasuredDimension(mWidth, mHeight);

    }

    public void setData(int[] data) {
        mData = data;
    }

    private void drawData(Canvas canvas) {
        if (mData == null) return;

        final int w = mWidth-getPaddingLeft()-getPaddingRight();
        final int h = mHeight-getPaddingBottom()-getPaddingTop();

        final float factorX = (mData.length>1)? (float) w/(mData.length-1) : 1f;

        int maxValue = 0;
        for (int data : mData) if (data > maxValue) maxValue = data;
        final float factorY = (maxValue>0)? (float) h/maxValue : 1f;


        final Path path = new Path();
        int x, y;
        int color1 = mColor1r;
        int color2 = mColor2r;

        // change color if there is no change in last 3 days
        if (mData.length > 2 && mData[mData.length-1] - mData[mData.length-3] == 0) {
            color1 = mColor1g;
            color2 = mColor2g;
        }

        // move to first point ...
        x = getPaddingLeft();
        y = h - (int)(mData[0]*factorY) + getPaddingTop();
        path.moveTo(x, y);

        // ... and line to the rest
        for (int i = 1; i < mData.length; i++) {
            x = (int)(i*factorX) + getPaddingLeft();
            y = h - (int)(mData[i]*factorY) + getPaddingTop();
            path.lineTo(x, y);
        }


        // draw line
        mPaint1.setColor(color1);
        canvas.drawPath(path, mPaint1);

        // draw area
        final int halfWidth = dp2px(1); // half stroke width
        x += halfWidth;
        path.lineTo(x, y); // top right
        path.lineTo(x, h+getPaddingTop()+halfWidth); // bottom right
        y = h - (int)(mData[0]*factorY) + getPaddingTop();
        path.lineTo(getPaddingLeft(), y+halfWidth); // bottom left
        path.close(); // start point
        LinearGradient gradient = new LinearGradient(
                0, 0,
                0, h,
                color1, color2,
                Shader.TileMode.CLAMP);
        mPaint2.setShader(gradient);
        canvas.drawPath(path, mPaint2);

    }

    private static int dp2px(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private static void log(String msg) {
        Log.i("ChartView", msg);
    }

}
