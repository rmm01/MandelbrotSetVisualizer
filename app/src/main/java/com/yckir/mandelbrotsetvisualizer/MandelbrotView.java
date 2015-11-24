package com.yckir.mandelbrotsetvisualizer;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ProgressBar;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MandelbrotView extends SurfaceView implements SurfaceHolder.Callback{

    public static final String TAG = "MANDELBROT_VIEW";
    private int mWidth;
    private int mHeight;
    private int mLength;
    private Paint mBlackPaint;
    private Paint mRedPaint;
    private Model mModel;
    private SurfaceHolder mHolder;
    private MaterialProgressBar mProgressBar;

    public MandelbrotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBlackPaint= new Paint();
        mRedPaint= new Paint();
        mBlackPaint.setColor(Color.BLACK);
        mRedPaint.setColor(Color.RED);

        mHeight=getHeight();
        mWidth=getWidth();
        mLength=Math.min(mWidth, mHeight);

        mModel = null;
        mProgressBar = null;

        mHolder = getHolder();
        mHolder.addCallback(this);
    }


    public void setProgressBar(MaterialProgressBar progressBar){
        Log.v(TAG,"setProgressBar");
        mProgressBar = progressBar;
    }


    /**
     * set the model that holds drawing details
     * @param model the model that holds drawing details
     */
    public void setModel(Model model) {
        mModel = model;
    }


    /**
     * Update the view using the current Model to draw on.
     */
    public void redraw(long delay){
        //Canvas canvas = mHolder.lockCanvas();
        //mModel.drawCanvas(canvas);
        //mHolder.unlockCanvasAndPost(canvas);
        DrawingAsyncTask task = new DrawingAsyncTask(mModel, mHolder, mProgressBar, delay);
        task.execute();
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mLength=Math.min(getWidth(),getHeight());
        mModel.setNumPixels(mLength);
        mModel.drawCanvas(canvas);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(TAG,"SurfaceCreated");
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v(TAG,"SurfaceChanged");
        mHeight=height;
        mWidth=width;
        mLength=Math.min(mWidth,mHeight);
        mModel.setNumPixels(mLength);

        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.CYAN);
        holder.unlockCanvasAndPost(canvas);

        redraw(300);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(TAG,"SurfaceDestroyed");
    }


    @Override
    public String toString() {
        ClassStateString state = new ClassStateString(TAG);
        state.addMember("mWidth", mWidth);
        state.addMember("mHeight", mHeight);
        state.addMember("mLength", mLength);
        return state.getString();
    }
}
