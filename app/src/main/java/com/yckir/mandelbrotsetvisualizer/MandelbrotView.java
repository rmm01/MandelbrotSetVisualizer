package com.yckir.mandelbrotsetvisualizer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;

import java.io.File;
import java.util.LinkedList;

public class MandelbrotView extends SurfaceView implements SurfaceHolder.Callback{

    public  static final String     TAG                         =   "MANDELBROT_VIEW";
    private static final String     FIRST_MANDELBROT_FILE_NAME  =   "DefaultMandelbrot.png" ;
    private static final int        DEFAULT_PROGRESS_PERCENT    =    100;

    private Model               mCurrentModel;
    private LinkedList<Model>   mModelHistory;
    private SurfaceHolder       mHolder;
    private ProgressListener    mProgressListener;

    private int     mNumModels;
    private int     mCurrentModelIndex;
    private int     mProgressPercent;
    private int     mWidth;
    private int     mHeight;
    private int     mImageLength;
    private int     mPaddingX;

    public MandelbrotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHeight = getHeight( );
        mWidth  = getWidth();
        mPaddingX = 0;
        mImageLength = Math.min(mWidth, mHeight);
        Log.v( TAG, "constructor w = " + mWidth + ", h = " + mHeight );

        resetModelList( );

        mHolder = getHolder( );
        mHolder.addCallback( this );

        mProgressPercent = DEFAULT_PROGRESS_PERCENT;
        mProgressListener = new ProgressListener( ) {
            @Override
            public void onProgressStart() {

            }

            @Override
            public void onProgressUpdate(int progress) {

            }

            @Override
            public void onProgressFinished() {

            }
        };
    }


    /**
     * Adds a model to the next position in list.
     *
     * @param model the model that will be saved
     */
    private void addModelToList(Model model){
        mCurrentModelIndex++;
        mNumModels=mCurrentModelIndex+1;
        mModelHistory.add(mCurrentModelIndex, model);
        mCurrentModel=model;
    }


    /**
     * resets the model list and recreates the default first entry.
     */
    private void resetModelList(){
        Model.setDefaultValues(getContext());
        mNumModels=1;
        mCurrentModelIndex=0;
        mModelHistory = new LinkedList<>();
        mCurrentModel = new Model(mImageLength);
        mCurrentModel.changeFileName(FIRST_MANDELBROT_FILE_NAME);
        mModelHistory.addFirst(mCurrentModel);
    }


    /**
     * Update the canvas of the surface view using the currently selected model.
     */
    private void drawView(){
        Log.v(TAG, "searching for file " + mCurrentModel.getFileName());
        File file = new File( getContext().getFilesDir(), mCurrentModel.getFileName());
        if(!file.exists()) {
            writeAndDisplayMandelbrotImage(file, mImageLength);
            return;
        }

        displayMandelbrotImage(file);
        Utility.logFileDetails(file);

        //DrawingAsyncTask task = new DrawingAsyncTask(mCurrentModel, mProgressListener,mProgressPercent);
        //task.execute(mHolder);
    }


    /**
     * Displays on the surface view the image at the given file location. Nothing happens if the
     * file could not be read.
     *
     * @param file the file location.
     */
    private void displayMandelbrotImage(File file){
        Bitmap myBitmap = Utility.getFileBitmap(file);
        if(myBitmap == null) {
            Log.e("FILE", "could not read " + file.getName());
            return;
        }

        Canvas canvas = mHolder.lockCanvas();
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(myBitmap, mPaddingX, 0, null);
        mHolder.unlockCanvasAndPost(canvas);
    }


    /**
     * Draws the image on a separate thread, writes the image, and displays it on surfaceView.
     * This method returning does not indicate that all these steps have completed.
     *
     * @param file the file to write to
     * @param imageSize the image size
     */
    private void writeAndDisplayMandelbrotImage(final File file, int imageSize){
        Log.v(TAG, "writeAndDisplayMandelbrotImage");
        final Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
        ProgressListener listener = new ProgressListener() {
            @Override
            public void onProgressStart() {mProgressListener.onProgressStart();}

            @Override
            public void onProgressUpdate(int progress) {mProgressListener.onProgressUpdate(progress);}

            @Override
            public void onProgressFinished() {
                Utility.writeBitmapToPNG(bitmap, file);
                displayMandelbrotImage(file);
                Utility.logFileDetails(file);
                mProgressListener.onProgressFinished();
            }
        };
        DrawBitmapTask task = new DrawBitmapTask(mCurrentModel,listener,mProgressPercent);
        task.execute(bitmap);
    }


    /**
     * Move backwards in the mandelbrot image list and redraws the view.
     */
    public void backward(){
        if( mCurrentModelIndex == 0 ){
            Log.e(TAG, "cannot go backward");
            return;
        }
        mCurrentModelIndex--;
        mCurrentModel=mModelHistory.get(mCurrentModelIndex);
        drawView();
    }


    /**
     * Move forward in the mandelbrot image list and redraws the view.
     */
    public void forward(){
        if( mCurrentModelIndex == mNumModels - 1 ){
            Log.e(TAG, "cannot go forward");
            return;
        }
        mCurrentModelIndex++;
        mCurrentModel=mModelHistory.get(mCurrentModelIndex);
        drawView();
    }


    /**
     * create a new list with the default image and redraws the view.
     */
    public void reset(){
        resetModelList();
        drawView();
    }


    /**
     * Zoom into the selected pixel and redraws the view.
     *
     * @param xPixel x coordinate
     * @param yPixel y coordinate
     * @return true if the pixel was valid
     */
    public boolean zoom(int xPixel, int yPixel){
        xPixel=xPixel-mPaddingX;
        if( xPixel<0 || xPixel > mImageLength )
            return false;

        Model copyModel = mCurrentModel.makeCopy();
        copyModel.recenterZoom(xPixel, yPixel);
        addModelToList(copyModel);
        drawView();
        return true;
    }


    /**
     * @return true if not currently viewing first image in list.
     */
    public boolean canBackward() {
        Log.v(TAG, "can backwards current index is " + mCurrentModelIndex);
        return mCurrentModelIndex != 0;
    }


    /**
     * @return true if not currently viewing last image in list.
     */
    public boolean canForward() {
        Log.v(TAG, "can forward current index is " + mCurrentModelIndex + "numModels = " + mNumModels);
        return mCurrentModelIndex != mNumModels-1;
    }


    /**
     * Updates the editText mandelbrot values from the views mandelbrot values.
     *
     * @param centerRealField editText for center real coordinate
     * @param centerImaginaryField editText for center imaginary coordinate
     * @param edgeLengthField editText for center edgeLength
     * @param iterationLimitField editText for iteration limit field
     */
    public void getModelValues(EditText centerRealField, EditText centerImaginaryField,
                               EditText edgeLengthField, EditText iterationLimitField){

        centerRealField.setText(Double.toString(mCurrentModel.getCenterReal()));
        centerImaginaryField.setText(Double.toString(mCurrentModel.getCenterImaginary()));
        edgeLengthField.setText(Double.toString(mCurrentModel.getEdgeLength()));
        iterationLimitField.setText(Integer.toString(mCurrentModel.getIterationLimit()));
    }


    /**
     * Updates the views mandelbrot values from the editText mandelbrot values. The
     * list does not grow in size.
     *
     * @param centerReal value of center real editText field
     * @param centerImaginary value of center Imaginary editText field
     * @param edgeLength value of edge length editText field
     * @param iterationLimit value of iteration limit editText field
     */
    public void setModelValues(double centerReal, double centerImaginary, double edgeLength,
                               int iterationLimit){

        mCurrentModel = new Model(centerReal,centerImaginary,edgeLength,iterationLimit, mCurrentModel.getNumPixels());
        mModelHistory.add(mCurrentModelIndex, mCurrentModel);
        mModelHistory.remove(mCurrentModelIndex + 1);
        drawView();
    }


    /**
     * Set a listener that will be notified about progress events.
     *
     * @param listener the listener that will have its methods called during progress events
     * @param progressPercent the listener will be notified when a multiple of this value is the
     *                        current progress. if the value is 5, then the listener will be
     *                        notified 20 times, at 5,10,..95,100.
     */
    public void setProgressListener(ProgressListener listener, int progressPercent){
        //TODO: set progress listener to work in increments instead of percent
        mProgressListener = listener;
        mProgressPercent = progressPercent;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        mImageLength=Math.min(getWidth(),getHeight());
        mCurrentModel.setNumPixels(mImageLength);
        mCurrentModel.drawCanvas(canvas);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v(TAG, "SurfaceCreated");
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //Todo: make it so that changing teh fields on the default image does not override the default image
        //Todo: make is sot that the surface changes dosnt reset the list
        if( mWidth == width && mHeight == height ) {
            Log.v(TAG, "surface unchanged w = " +mWidth + ", h = " +mHeight );
            return;
        }
        mHeight=height;
        mWidth=width;
        mImageLength=Math.min(mWidth,mHeight);
        mPaddingX=(mWidth-mImageLength)/2;

        Log.v(TAG, "Surface Changed w = " + mWidth + ", h = " + mHeight);

        //resetModelList();

        mCurrentModel.setNumPixels(mImageLength);
        mCurrentModel.changeFileName(FIRST_MANDELBROT_FILE_NAME);
        //createDefaultImage();
        drawView();
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.v(TAG, "SurfaceDestroyed");
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
}
