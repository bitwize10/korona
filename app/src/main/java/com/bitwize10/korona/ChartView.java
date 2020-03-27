package com.bitwize10.korona;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
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
    private int mColor1o, mColor2o; // orange
    private int mColor1y, mColor2y; // yellow
    private int mColor1g, mColor2g; // green

    private int mWidth = 0;
    private int mHeight = 0;

    private Country mCountry;
    private boolean showChange = false;


    public ChartView(Context context) {
        super(context);
        init();
    }


    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {

        mColor1r = getResources().getColor(R.color.red);
        mColor2r = getResources().getColor(R.color.darkerRedTransparent);
        mColor1o = getResources().getColor(R.color.orange);
        mColor2o = getResources().getColor(R.color.darkerOrangeTransparent);
        mColor1y = getResources().getColor(R.color.yellow);
        mColor2y = getResources().getColor(R.color.darkerYellowTransparent);
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

    public void setCountry(Country country) {
        mCountry = country;
    }

    public void showChange(boolean showChange) {
        this.showChange = showChange;
    }

    public boolean toggleChange() {
        this.showChange = !this.showChange;
        return this.showChange;
    }

    private void drawData(Canvas canvas) {
        if (mCountry == null) return;

        int[] data = showChange ? mCountry.getChangeData() : mCountry.getData();

        final int w = mWidth-getPaddingLeft()-getPaddingRight();
        final int h = mHeight-getPaddingBottom()-getPaddingTop();

        final float factorX = (data.length>1)? (float) w/(data.length-1) : 1f;

        int maxValue = 0;
        for (int d : data) if (d > maxValue) maxValue = d;
        final float factorY = (maxValue>0)? (float) h/maxValue : 1f;


        final Path path = new Path();
        int x, y;
        int color1, color2;

        int daysNoChange = mCountry.getDaysNoChange();
        if (daysNoChange >= 10) {
            color1 = mColor1g;
            color2 = mColor2g;
        } else if (daysNoChange >= 6) {
            color1 = mColor1y;
            color2 = mColor2y;
        } else if (daysNoChange >= 2) {
            color1 = mColor1o;
            color2 = mColor2o;
        } else {
            color1 = mColor1r;
            color2 = mColor2r;
        }

        // move to first point ...
        x = getPaddingLeft();
        y = h - (int)(data[0]*factorY) + getPaddingTop();
        path.moveTo(x, y);

        // ... and line to the rest
        for (int i = 1; i < data.length; i++) {
            x = (int)(i*factorX) + getPaddingLeft();
            y = h - (int)(data[i]*factorY) + getPaddingTop();
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
        //y = h - (int)(data[0]*factorY) + getPaddingTop();
        path.lineTo(getPaddingLeft(), h+halfWidth+getPaddingTop()); // bottom left
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
