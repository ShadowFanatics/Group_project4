package com.example.camera;
import java.io.ByteArrayOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SignatureView extends View {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private float mSignatureWidth = 8f;
    private int mSignatureColor = Color.BLACK;
    private boolean mCapturing = true;
    private Bitmap mSignature = null;

    private static final boolean GESTURE_RENDERING_ANTIALIAS = true;
    private static final boolean DITHER_FLAG = true;

    private Paint mPaint = new Paint();
    private Path mPath = new Path();

    private final Rect mInvalidRect = new Rect();

    private float mX;
    private float mY;

    private float mCurveEndX;
    private float mCurveEndY;

    private int mInvalidateExtraBorder = 10;

    public SignatureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public SignatureView(Context context) {
        super(context);
        init();
    }

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);  

        init();
    }

    private void init() {
        setWillNotDraw(false);

        mPaint.setAntiAlias(GESTURE_RENDERING_ANTIALIAS);
        mPaint.setColor(mSignatureColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mSignatureWidth);
        mPaint.setDither(DITHER_FLAG);
        mPath.reset();


    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mSignature != null) {
            canvas.drawBitmap(mSignature, null, new Rect(0, 0, getWidth(),
                    getHeight()), null);
        } else {
            canvas.drawPath(mPath, mPaint);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mCapturing) {
            processEvent(event);
            Log.d(VIEW_LOG_TAG, "dispatchTouchEvent");
            return true;
        } else {
            return false;
        }
    }

    private boolean processEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            touchDown(event);
            invalidate();
            return true;
        case MotionEvent.ACTION_MOVE:

            Rect rect = touchMove(event);
            if (rect != null) {
                invalidate(rect);
            }
            return true;

        case MotionEvent.ACTION_UP:

            touchUp(event, false);
            invalidate();
            return true;

        case MotionEvent.ACTION_CANCEL:

            touchUp(event, true);
            invalidate();
            return true;

        }

        return false;

    }

    private void touchUp(MotionEvent event, boolean b) {
        // TODO Auto-generated method stub

    }

    private Rect touchMove(MotionEvent event) {
        Rect areaToRefresh = null;

        final float x = event.getX();
        final float y = event.getY();

        final float previousX = mX;
        final float previousY = mY;

        areaToRefresh = mInvalidRect;

        // start with the curve end
        final int border = mInvalidateExtraBorder;
        areaToRefresh.set((int) mCurveEndX - border, (int) mCurveEndY - border,
                (int) mCurveEndX + border, (int) mCurveEndY + border);

        float cX = mCurveEndX = (x + previousX) / 2;
        float cY = mCurveEndY = (y + previousY) / 2;

        mPath.quadTo(previousX, previousY, cX, cY);

        // union with the control point of the new curve
        areaToRefresh.union((int) previousX - border, (int) previousY - border,
                (int) previousX + border, (int) previousY + border);

        // union with the end point of the new curve
        areaToRefresh.union((int) cX - border, (int) cY - border, (int) cX
                + border, (int) cY + border);

        mX = x;
        mY = y;

        return areaToRefresh;

    }

    private void touchDown(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        mX = x;
        mY = y;
        mPath.moveTo(x, y);

        final int border = mInvalidateExtraBorder;
        mInvalidRect.set((int) x - border, (int) y - border, (int) x + border,
                (int) y + border);

        mCurveEndX = x;
        mCurveEndY = y;

    }


    /**
     * Erases the signature.
     */
    public void clear() {
        mSignature = null;
        mPath.rewind();
        // Repaints the entire view.
        invalidate();
    }

    public boolean isCapturing() {
        return mCapturing;
    }

    public void setIsCapturing(boolean mCapturing) {
        this.mCapturing = mCapturing;
    }

    public void setSignatureBitmap(Bitmap signature) {
        mSignature = signature;
        invalidate();
    }

    public Bitmap getSignatureBitmap() {
        if (mSignature != null) {
            return mSignature;
        } else if (mPath.isEmpty()) {
            return null;
        } else {
            Bitmap bmp = Bitmap.createBitmap(getWidth(), getHeight(),
            Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            c.drawPath(mPath, mPaint);
            return bmp;
        }
    }

    public void setSignatureWidth(float width) {
        mSignatureWidth = width;
        mPaint.setStrokeWidth(mSignatureWidth);
        invalidate();
    }

    public float getSignatureWidth(){
        return mPaint.getStrokeWidth();
    }

    public void setSignatureColor(int color) {
        mSignatureColor = color;
    }

    /**
     * @return the byte array representing the signature as a PNG file format
     */
    public byte[] getSignaturePNG() {
        return getSignatureBytes(CompressFormat.PNG, 0);
    }

    /**
     * @param quality Hint to the compressor, 0-100. 0 meaning compress for small
     *            size, 100 meaning compress for max quality.
     * @return the byte array representing the signature as a JPEG file format
     */
    public byte[] getSignatureJPEG(int quality) {
        return getSignatureBytes(CompressFormat.JPEG, quality);
    }

    private byte[] getSignatureBytes(CompressFormat format, int quality) {
        Log.d(LOG_TAG, "getSignatureBytes() path is empty: " + mPath.isEmpty());
        Bitmap bmp = getSignatureBitmap();
        if (bmp == null) {
            return null;
        } else {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            getSignatureBitmap().compress(format, quality, stream);

            return stream.toByteArray();
        }
    }


}