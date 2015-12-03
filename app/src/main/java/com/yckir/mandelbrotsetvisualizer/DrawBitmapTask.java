package com.yckir.mandelbrotsetvisualizer;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

public class DrawBitmapTask extends AsyncTask<Bitmap, Integer, Bitmap> {

    public static String TAG = "DRAWING_ASYNC_TASK";
    private Model mModel;
    private MandelbrotView.ProgressListener mListener;
    private int mProgressParts;

    /**
     * Construct a task that will draw the mandelbrot image and notify a listener.
     *
     * @param model has the details on ow to draw
     * @param progressParts the listener should be notifies this many times throughout the progress
     *                      of the task.
     * @param listener the listener to notify when progress begins, is being made, and finishes
     */
    public DrawBitmapTask(Model model, int progressParts, MandelbrotView.ProgressListener listener){
        mModel = model;
        mListener=listener;
        mProgressParts = progressParts;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if( mListener != null )
            mListener.onProgressStart(mProgressParts);
    }


    @Override
    protected Bitmap doInBackground(Bitmap... params) {
        if(params.length==0){
            Log.e(TAG, "error, bitmap passed to DrawBitmapTask.execute is null");
            return null;
        }

        Bitmap bitmap = params[0];
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        long startTime = System.currentTimeMillis();

        double progressPercent = 100.0/mProgressParts;

        int previousProgress = 0;
        int currentProgress = previousProgress;
        int partNumber = 0;
        while( currentProgress < 100 ) {
            partNumber++;
            previousProgress = currentProgress;
            currentProgress = (int)(progressPercent*partNumber);

            if( currentProgress > 100 )
                currentProgress = 100;

            mModel.partialDrawCanvas( previousProgress, currentProgress, canvas );
            publishProgress( partNumber );
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
