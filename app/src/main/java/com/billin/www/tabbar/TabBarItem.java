package com.billin.www.tabbar;

import android.animation.ArgbEvaluator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * hamburger button
 * <p/>
 * Created by Billin on 2016/12/26.
 */
public class TabBarItem extends View {

    private static final String TAG = "TabBarItem";

    /**
     * Use the level to control animation
     */
    private int mLevel = 0;

    /**
     * The width of item content width
     */
    private int mItemWidth;

    /**
     * The height of item content height
     */
    private int mItemHeight;

    private int mItemRound;

    private int mItemHeightOffset;

    private Bitmap mIconImage;

    private Rect mBitmapRect;

    private RectF mRingRect;

    /**************************************
     * paint tools under this annotation  *
     **************************************/
    private Paint mPaint;

    private ArgbEvaluator mArgbEvaluator;

    public TabBarItem(Context context) {
        super(context);
        initPaintTools();
    }

    public TabBarItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaintTools();
    }

    /**
     * Init paint and other draw tools
     */
    private void initPaintTools() {

        // init paint
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);

        mArgbEvaluator = new ArgbEvaluator();
    }

    private void initParam(int w, int h) {
        mItemHeight = (int) (h / 4.0 * 3);
        mItemWidth = w;
        mItemHeightOffset = h - mItemHeight;

        mItemRound = mItemHeight / 2;

        //get Ring Region base on 0
        mRingRect = new RectF(0, 0, mItemWidth, mItemHeight);
        mBitmapRect = getBitmapRect(mIconImage);
    }

    /**
     * Get Bitmap Rect base on Rect(0, 0, itemWidth, itemHeight)
     */
    private Rect getBitmapRect(Bitmap bitmap) {
        if (bitmap == null)
            return null;

        int bitmapHeight = bitmap.getHeight();
        int bitmapWidth = bitmap.getWidth();

        int heightOffset = (int) (mItemHeight / 8f);
        int height = mItemHeight - 2 * heightOffset;
        int width = (int) (height * 1.0 * (double) bitmapWidth / (double) bitmapHeight);

        int left = (mItemWidth - width) / 2;
        return new Rect(left, heightOffset, left + width, heightOffset + height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // if the measure mode is AT_MOST, setting height with 80dp and width with 48dp
        // height : width = 5 : 3
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {

            float density = getResources().getDisplayMetrics().density;

            width = (int) (80f * density);
            height = (int) (48f * density);

            setMeasuredDimension(width, height);
        } else if (heightMode == MeasureSpec.AT_MOST) {

            height = (int) (width * 3f / 5f);
            setMeasuredDimension(width, height);
        } else if (widthMode == MeasureSpec.AT_MOST) {

            width = (int) (height * 5f / 3f);
            setMeasuredDimension(width, height);
        } else {
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initParam(w, h);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mLevel < 0) {
            mPaint.setXfermode(null);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(5f);
            mPaint.setColor(Color.WHITE);
            mPaint.setAlpha(0xff);

            canvas.translate(0, mItemHeightOffset);

            // draw ring
            canvas.drawRoundRect(mRingRect, mItemRound, mItemRound, mPaint);

            // draw icon
            if (mIconImage != null) {
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                mPaint.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));

                canvas.drawBitmap(mIconImage,
                        new Rect(0, 0, mIconImage.getWidth(), mIconImage.getHeight()),
                        mBitmapRect,
                        mPaint);
            }

        } else {

            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

            int dy = mItemHeightOffset / 3;

            int layer = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);

            // draw first layer
            canvas.translate(0, mItemHeightOffset);
            mPaint.setAlpha(0x4c);
            canvas.drawRoundRect(mRingRect, mItemRound, mItemRound, mPaint);

            // draw second, third layer
            int level = mLevel;
            for (int i = 50; level >= 50; level -= 50, i += 50) {
                mPaint.setAlpha(i * (0xff - 0x4c) / 100 + 0x4c);
                canvas.translate(0, -dy);
                canvas.drawRoundRect(mRingRect, mItemRound, mItemRound, mPaint);
            }

            // draw middle layer
            mPaint.setAlpha(mLevel * (0xff - 0x4c) / 100 + 0x4c);
            canvas.translate(0, -2 * dy * (level / 100f));
            canvas.drawRoundRect(mRingRect, mItemRound, mItemRound, mPaint);

            // draw bitmap
            if (mIconImage != null) {
                mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                mPaint.setAlpha(0xff);
                mPaint.setColorFilter(new PorterDuffColorFilter(
                        (Integer) mArgbEvaluator.evaluate(mLevel / 100f, Color.WHITE,
                                Color.parseColor("#7aa9ff")),
                        PorterDuff.Mode.SRC_IN));

                canvas.drawBitmap(mIconImage,
                        new Rect(0, 0, mIconImage.getWidth(), mIconImage.getHeight()),
                        mBitmapRect,
                        mPaint);
            }

            canvas.restoreToCount(layer);
        }

        mPaint.setXfermode(null);
        mPaint.setColorFilter(null);
    }

    public void setLevel(int level) {
        mLevel = level;
        postInvalidate();
    }

    public void setIcon(Bitmap bitmap) {
        mIconImage = bitmap;
        mBitmapRect = getBitmapRect(bitmap);
        postInvalidate();
    }

    public void setIcon(int resId) {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), resId);
        setIcon(bm);
    }
}
