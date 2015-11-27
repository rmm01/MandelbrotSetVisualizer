package com.yckir.mandelbrotsetvisualizer;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;

public class DrawBitmapTask extends AsyncTask<Bitmap, Integer, Bitmap> {

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
    public DrawBitmapTask(Model model, MandelbrotView.ProgressListener listener, int progressPercent){
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
    protected Bitmap doInBackground(Bitmap... params) {
        if(params.length==0){
            Log.e(TAG, "error, canvas passed to DrawingAsyncTask.execute is null");
            return null;
        }

        Bitmap bitmap = params[0];
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        long startTime = System.currentTimeMillis();
        int previousProgress = 0;
        int currentProgress = previousProgress;

        while( currentProgress < 100 ) {

            previousProgress = currentProgress;
            currentProgress += mProgressPercent;

            if( currentProgress > 100 )
                currentProgress = 100;

            mModel.partialDrawCanvas( previousProgress, currentProgress, canvas );
            publishProgress( currentProgress );
        }

        Log.v(TAG, "total task time =: " + ( System.currentTimeMillis() - startTime ) + " ms");
        return bitmap;
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if( mListener != null )
            mListener.onProgressUpdate(progress);
    }


    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if( mListener != null )
            mListener.onProgressFinished();
    }
}
