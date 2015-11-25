package com.yckir.mandelbrotsetvisualizer;


import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;

public class DrawingAsyncTask extends AsyncTask<SurfaceHolder, Integer, Void> {

    public static String TAG = "DRAWING_ASYNC_TASK";
    private Model mModel;
    private MandelbrotView.ProgressListener mListener;
    private int mProgressPercent;

    /**
     *
     * @param model has the details on ow to draw
     * @param listener the listener to notify when progress begins, is being made, and finishes
     * @param progressPercent the listener should be when a multiple of this progress value is made.
     *                        must be between > 0 and <=100
     */
    public DrawingAsyncTask(Model model, MandelbrotView.ProgressListener listener, int progressPercent){
        mModel = model;
        mListener=listener;
        mProgressPercent = progressPercent;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if( mListener != null )
            mListener.onProgressStart();
    }


    @Override
    protected Void doInBackground(SurfaceHolder... params) {

        SurfaceHolder holder = params[0];
        if(holder == null) {
            Log.e(TAG,"error, holder passed to DrawingAsyncTask.execute is null");
            return null;
        }

        long startTime = System.currentTimeMillis();
        int previousProgress = 0;
        int currentProgress = previousProgress;

        Canvas canvas = holder.lockCanvas();
        canvas.drawColor(Color.WHITE);

        while( currentProgress < 100 ) {

            previousProgress = currentProgress;
            currentProgress += mProgressPercent;

            if( currentProgress > 100 )
                currentProgress = 100;

            mModel.partialDrawCanvas( previousProgress, currentProgress, canvas );
            publishProgress( currentProgress );
        }

        holder.unlockCanvasAndPost(canvas);
        Log.v(TAG, "total task time =: " + ( System.currentTimeMillis() - startTime ) + " ms");
        return null;
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if( mListener != null )
            mListener.onProgressUpdate(progress);
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if( mListener != null )
            mListener.onProgressFinished();
    }
}
