package com.yckir.mandelbrotsetvisualizer;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ProgressBar;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class DrawingAsyncTask extends AsyncTask<Void, Integer, Void> {

    public static String TAG = "DRAWING_ASYNC_TASK";
    private Model mModel;
    private SurfaceHolder mHolder;
    //private Canvas mCanvas;
    //private Bitmap mBitmap;
    private long mTime;
    private MaterialProgressBar mProgressBar;

    public DrawingAsyncTask(Model model, SurfaceHolder holder,MaterialProgressBar progressBar, long time){
        mModel = model;
        mHolder = holder;
        mTime = time;
        mProgressBar = progressBar;
        //mBitmap = Bitmap.createBitmap(mModel.getNumPixels(),mModel.getNumPixels(), Bitmap.Config.ARGB_8888);
        //mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressBar.setProgress(0);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(Void... params) {

        long startTime = System.currentTimeMillis();

        if(mTime>200){
            synchronized (this){
                try {
                    wait(mTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Canvas canvas = mHolder.lockCanvas();
        canvas.drawColor(Color.WHITE);
        //mModel.drawCanvas(canvas);

        for(int i = 5; i <= 100; i+=5){
            mModel.partialDrawCanvas(i - 5, i, canvas);
            Log.v(TAG, "finished " + i + "%");
            publishProgress(i);
        }


        mHolder.unlockCanvasAndPost(canvas);


        long elapsedTime = System.currentTimeMillis() - startTime;
        Log.v(TAG, "total task time =: " + elapsedTime + " ms");


        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];

        mProgressBar.setProgress(progress);

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mProgressBar.setVisibility(View.GONE);
    }
}
