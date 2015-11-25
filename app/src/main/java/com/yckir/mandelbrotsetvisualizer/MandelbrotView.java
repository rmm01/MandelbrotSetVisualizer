package com.yckir.mandelbrotsetvisualizer;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;

public class MandelbrotView extends SurfaceView implements SurfaceHolder.Callback{

    public static final String TAG = "MANDELBROT_VIEW";
    private static final int DEFAULT_PROGRESS_PERCENT = 100;
    private Model mModel;
    private SurfaceHolder mHolder;
    private ProgressListener mListener;
    private MyHandler mMyHandler;

    private int mProgressPercent;
    private int mWidth;
    private int mHeight;
    private int mImageLength;


    public MandelbrotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mModel = null;
        mListener = null;
        mMyHandler = new MyHandler(this);

        mHeight=getHeight();
        mWidth=getWidth();
        mImageLength=Math.min(mWidth, mHeight);
        mProgressPercent = DEFAULT_PROGRESS_PERCENT;

        mHolder = getHolder();
        mHolder.addCallback(this);
    }


    public void setProgressListener(ProgressListener listener, int progressPercent){
        mListener = listener;
        mProgressPercent = progressPercent;
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
    public void redraw(){
        DrawingAsyncTask task = new DrawingAsyncTask(mModel,mListener,mProgressPercent);
        task.execute(mHolder);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        mImageLength=Math.min(getWidth(),getHeight());
        mModel.setNumPixels(mImageLength);
        mModel.drawCanvas(canvas);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(TAG,"SurfaceCreated");
        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.CYAN);
        holder.unlockCanvasAndPost(canvas);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v(TAG,"SurfaceChanged");
        mHeight=height;
        mWidth=width;
        mImageLength=Math.min(mWidth,mHeight);
        mModel.setNumPixels(mImageLength);

        mMyHandler.sendEmptyMessageDelayed(0,300);
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
        state.addMember("mImageLength", mImageLength);
        return state.getString();
    }


    /**
     * Listener that will have its methods called while the Mandelbrot image is being constructed.
     */
    public interface ProgressListener{

        /**
         * Called when the the Mandelbrot image is about to be constructed.
         */
        void onProgressStart();


        /**
         * Called when progress has been made to the Mandelbrot image.
         *
         * @param progress how much total progress in percent has been made.
         */
        void onProgressUpdate(int progress);


        /**
         * Called when the Image has finished drawing.
         */
        void onProgressFinished();
    }


    /**
     * Handler that is used to make a delayed call to MandelbrotView.redraw().
     * This is used to construct the first mandelbrot image when teh activity is started.
     * The call to redraw must be delayed because otherwise, the AsyncTask requests the canvas
     * before the surfaceView has finished initializing, causing the main thread to wait.
     */
    private static class MyHandler extends Handler{

        private final WeakReference<MandelbrotView> mWeakMandelReference;

        public MyHandler(MandelbrotView view){
            mWeakMandelReference = new WeakReference<>(view);
        }


        @Override
        public void handleMessage(Message msg) {
            MandelbrotView mandelbrotView = mWeakMandelReference.get();
            if(mandelbrotView != null)
                mandelbrotView.redraw();
        }
    }
}
