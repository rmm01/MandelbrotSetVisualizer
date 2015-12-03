package com.yckir.mandelbrotsetvisualizer;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;

import java.io.File;
import java.util.LinkedList;

public class MandelbrotView extends SurfaceView implements SurfaceHolder.Callback{

    public  static final String     TAG                         =   "MANDELBROT_VIEW";
    private static final String     FIRST_MANDELBROT_FILE_NAME  =   "DefaultMandelbrot" ;
    public  static final int        SINGLE_STATE                =   0;
    public  static final int        CENTER_STATE                =   1;
    public  static final int        END_STATE                   =   2;
    public  static final int        FRONT_STATE                 =   3;
    public  static final int        LOADED_STATE                =   4;

    private Model                   mCurrentModel;
    private LinkedList<Model>       mModelHistory;
    private SurfaceHolder           mHolder;
    private ProgressListener        mProgressListener;
    private AnimationDrawable       mAnimationDrawable;
    private File                    mPicDir;
    private File                    mAnimationDir;
    private File                    mNavDir;
    private File                    mCurrentAnimationDir;
    private Bitmap                  mMyBitmap;

    private int     mWidth;
    private int     mHeight;
    private int     mImageLength;
    private int     mPaddingX;
    private int     mNumModels;
    private int     mCurrentModelIndex;
    private int     mState;
    private int     mProgressParts;

    public MandelbrotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //set dimensions
        mHeight = getHeight();
        mWidth  = getWidth();
        mImageLength = Math.min(mWidth, mHeight);
        mPaddingX = mWidth-mImageLength;

        //initialize model list
        resetModelList();

        //get view holder for drawing on separate thread
        mHolder = getHolder( );
        mHolder.addCallback( this );

        //set empty listener
        mProgressListener = new ProgressListener( ) {
            @Override
            public void onProgressStart(int maxProgress) {

            }

            @Override
            public void onProgressUpdate(int progress) {

            }

            @Override
            public void onProgressFinished() {

            }
        };
        mProgressParts = 0;

        //create storage directories
        Utility.makeStorageDirectories(context);
        mPicDir       = Utility.getExternalPictureDir();
        mAnimationDir = Utility.getExternalAnimationDir();
        mNavDir       = Utility.getInternalNavigationDir(context);

        mAnimationDrawable = null;
        mMyBitmap = null;
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
     * Removes the current model from list. Should only be called if canDelete returns true.
     */
    private void removeCurrentModelFromList(){
        mModelHistory.remove(mCurrentModelIndex);
        mNumModels--;
        mCurrentModelIndex--;
        mCurrentModel=mModelHistory.get(mCurrentModelIndex);
    }


    /**
     * resets the model list and recreates the default first entry.
     */
    private void resetModelList(){
        Model.setDefaultValues(getContext());
        mState = SINGLE_STATE;
        mNumModels=1;
        mCurrentModelIndex=0;
        mModelHistory = new LinkedList<>();
        mCurrentModel = new Model(mImageLength);
        mCurrentModel.changeFileName(FIRST_MANDELBROT_FILE_NAME);
        mModelHistory.addFirst(mCurrentModel);
    }


    /**
     * Update the canvas of the surface view using the currently selected model. If the file has not
     * been drawn yet, it will be drawn on a separate thread. This method terminating does not
     * indicate that the view has been drawn.
     */
    private void drawView() {
        File currentModelFile = new File( mNavDir, mCurrentModel.getFileName());
        if(currentModelFile.exists())
            displayMandelbrotImage(currentModelFile);
        else
            writeAndDisplayMandelbrotImage(currentModelFile);
    }


    /**
     * Displays on the surface view the image at the given file location. Error is logged if the
     * file could not be read.
     *
     * @param file the file location.
     */
    private void displayMandelbrotImage(File file){
        Bitmap myBitmap = BitmapFactory.decodeFile(file.getPath());
        if (myBitmap == null) {
            Log.e(TAG, "DisplayBitmap: could not read " + file.getName());
            return;
        }

        Canvas canvas = mHolder.lockCanvas();
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(myBitmap, mPaddingX, 0, null);
        mHolder.unlockCanvasAndPost(canvas);
    }
    //private static int currentFrame =0;


    /**
     * Recursive function that calls itself, creating the animation frames for the current model list.
     * First Frame should be frame zero.
     *
     * @param fps frames per second
     * @param lastFrameNum total number of frames
     * @param currentFrame the current frame being animated.
     */
    private void createAnimationFrame(final int fps, final int lastFrameNum,final int currentFrame){
        int modelNum = currentFrame /fps;
        int frameNum = currentFrame - modelNum*fps;
        double zoom = (double)frameNum/fps;
        ProgressListener listener = new ProgressListener() {
            @Override
            public void onProgressStart(int maxProgress) {
            }

            @Override
            public void onProgressUpdate(int progress) {
            }

            @Override
            public void onProgressFinished() {
                mProgressListener.onProgressUpdate(currentFrame);
                File saveFile = new File(mCurrentAnimationDir, "Mandel" + currentFrame + ".png");
                Utility.writeBitmapToPNG(mMyBitmap, saveFile);
                Utility.sentMediaStoreBroadcast(saveFile, getContext());
                createAnimationFrame(fps, lastFrameNum,currentFrame+1);
            }
        };

        Log.v("RECORD","fps = " + fps + ", currentFrame = " + currentFrame + ", lastFrameNum = " + lastFrameNum +
                ", modelNum = " + modelNum + ", frameNum= " + frameNum + ", zoom = " + zoom);

        //image exits already, copy,rename,and move it.
        if(frameNum==0) {
            mProgressListener.onProgressUpdate(currentFrame + 1);
            File saveFile = new File(mCurrentAnimationDir, "Mandel" + currentFrame + ".png");
            String path =mNavDir.getPath() + "/" + mModelHistory.get(modelNum).getFileName();
            Log.v("RECORD", "path for nav model = " + path);
            Utility.writeBitmapToPNG(BitmapFactory.decodeFile(path), saveFile);
            Utility.sentMediaStoreBroadcast(saveFile, getContext());
            if(currentFrame ==lastFrameNum) {
                //- case if for when fps=1
                mProgressListener.onProgressFinished();
                return;
            }
            else {
                mProgressListener.onProgressUpdate(currentFrame);
                createAnimationFrame(fps,lastFrameNum,currentFrame+1);
                return;
            }
        }


        Model zoomedModel = Model.zoom(mModelHistory.get(modelNum), mModelHistory.get(modelNum + 1), zoom);

        DrawBitmapTask task = new DrawBitmapTask(zoomedModel, 1,listener);
        task.execute(mMyBitmap);
    }


    /**
     * Draws the image on a separate thread, writes the image, and displays it on surfaceView.
     * This method returning does not indicate that all these steps have completed.
     *
     * @param file the file to write to
     */
    private void writeAndDisplayMandelbrotImage(final File file){
        ProgressListener listener = new ProgressListener() {
            @Override
            public void onProgressStart(int maxProgress) {mProgressListener.onProgressStart(maxProgress);}

            @Override
            public void onProgressUpdate(int progress) {mProgressListener.onProgressUpdate(progress);}

            @Override
            public void onProgressFinished() {
                Utility.writeBitmapToPNG(mMyBitmap, file);
                displayMandelbrotImage(file);
                mProgressListener.onProgressFinished();
            }
        };
        DrawBitmapTask task = new DrawBitmapTask(mCurrentModel,mProgressParts,listener);
        task.execute(mMyBitmap);
    }


    /**
     * update the current state
     */
    private void updateState(){
        if(mNumModels == 1)
            mState= SINGLE_STATE;
        else if(canBackward() && canForward())
            mState=CENTER_STATE;
        else if(canForward())
            mState=FRONT_STATE;
        else //(canBackward())
            mState=END_STATE;

    }


    /**
     * Move backwards in the mandelbrot image list and redraws the view. Logs error if currently
     * viewing the first image.
     */
    public void backward(){
        if( !canBackward() ){
            Log.e(TAG, "cannot go backward");
            return;
        }
        mCurrentModelIndex--;
        mCurrentModel=mModelHistory.get(mCurrentModelIndex);
        drawView();
        updateState();
    }


    /**
     * Move forward in the mandelbrot image list and redraws the view. Logs error if currently
     * viewing the last image.
     */
    public void forward(){
        if( !canForward() ){
            Log.e(TAG, "cannot go forward");
            return;
        }
        mCurrentModelIndex++;
        mCurrentModel=mModelHistory.get(mCurrentModelIndex);
        drawView();
        updateState();
    }


    /**
     * create a new list with the default image and redraws the view.
     */
    public void reset(){
        resetModelList();
        drawView();
        updateState();
    }


    /**
     * Delete the currently displayed Mandelbrot image. Logs error if cannot delete current image.
     */
    public void delete(){
        if(!canDelete()){
            Log.e(TAG, "cannot delete last remaining element");
            return;
        }
        removeCurrentModelFromList();
        drawView();
        updateState();
    }


    /**
     * Saved the currently viewed image the apps external storage directory. Sends a broadcast for
     * the MediaStore content provider that a new image has been created and it should  be handled.
     *
     * @return A string message describing the error that occurred, or "File Saved"
     */
    public String saveCurrentImage(String fileName){
        //TODO: change toast messages to be inside string resources

        if( !Utility.isExternalStorageWritable() )
            return "Cannot save, media is not currently available";

        File internalFile = new File(mNavDir,mCurrentModel.getFileName());
        File savedFile = new File(mPicDir,fileName+".png");

        if(!Utility.imageSpaceExists(mPicDir))
            return "Insufficient available storage space";

        if (!Utility.writeBitmapToPNG(BitmapFactory.decodeFile(internalFile.getPath()), savedFile))
            return "Could not write to file location";

        Utility.sentMediaStoreBroadcast(savedFile, getContext());

        updateState();
        return "File Saved";
    }


    /**
     * Create the animation frames for the current model list. The frames are created on a separate
     * thread one after the other. The total number of frames created will be
     * fps * duration * (numModels-1) + 1. It is expected to take a large amount of time to create
     * the frames.
     *
     * @param fps frames per second
     * @param duration number of seconds between the model list. the total duration of the animation
     *                 will be duration * ( numModels - 1 )
     * @param animationName the name of the animation
     */
    public void recordAnimation(final int fps, final int duration, final String animationName){
        //TODO: make return string for toast messages
        final int lastFrameNumber = duration * fps * (mNumModels-1);
        int delay = duration*1000/fps;
        mCurrentAnimationDir = new File(mAnimationDir,animationName);
        if(!mCurrentAnimationDir.mkdirs()){
            Log.e(TAG, "animation name exists: " + animationName);
            return;
        }
        Utility.writeAnimationDetails(delay, lastFrameNumber, mCurrentAnimationDir);
        mProgressListener.onProgressStart(lastFrameNumber-1);
        createAnimationFrame(fps, lastFrameNumber,0);
    }


    /**
     * Loads the selected animation. NOTE: this animation object requires
     * a large amount of memory and can possibly cause an out of memory exception. You Must call
     * closeAnimation Prior to loading a new animation ro recycle the bitmaps it has.
     *
     * @param which index for the selected animation in animation directory
     */
    public void load(int which){
        //TODO: make utility function String getAnimationFileName(int i)
        //Todo: make this function do work on separate thread
        String animationName = getAnimationTitles()[which];
        mAnimationDrawable = new AnimationDrawable();
        Resources resources = getResources();
        BitmapDrawable drawable;
        File animationLocation = new File(mAnimationDir, animationName);

        String[] contents = Utility.splitAnimationFileDetails(Utility.readFile(animationLocation));

        if(contents.length!=2) {
            Log.e(TAG, "error reading animation, details length = " + contents.length);
            return;
        }
        int delay = Utility.getAnimationFileDelay(contents);
        int lastFrameNum = Utility.getAnimationFileLastFrame(contents);

        for (int i = 0;i<=lastFrameNum;i++){
            drawable = new BitmapDrawable(resources,animationLocation.getPath() + "/Mandel" + i + ".png");
            mAnimationDrawable.addFrame(drawable,delay);
        }

        mState=LOADED_STATE;
    }


    /**
     * Plays the AnimationDrawable stored in the MandelbrotView from the beginning. You should
     * attach the AnimationDrawable to another view prior to this call. use getAnimationDrawable to
     * get a reference.
     */
    public void playAnimation(){
        if(mAnimationDrawable.isRunning())
            mAnimationDrawable.stop();
        mAnimationDrawable.start();
    }


    /**
     * stops the currently loaded animation and recycles its bitmaps. This should always be called
     * in between loadAnimation calls or risk out of memory exception.
     */
    public void closeAnimation(){
        if(mAnimationDrawable.isRunning())
            mAnimationDrawable.stop();
        for(int i = 0; i<mAnimationDrawable.getNumberOfFrames();i++){
            ((BitmapDrawable)mAnimationDrawable.getFrame(i)).getBitmap().recycle();
        }
        mAnimationDrawable = null;
        updateState();
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
        updateState();
        return true;
    }


    /**
     * @return the id of the current state of the view
     */
    public int getNavigationState() {
        Log.v(TAG, "State = " + getStateString());
        return mState;
    }


    /**
     * @return the string of the current state of the view
     */
    public String getStateString(){
        switch (mState){
            case SINGLE_STATE:
                return "SINGLE_STATE";
            case FRONT_STATE:
                return "FRONT_STATE";
            case CENTER_STATE:
                return "CENTER_STATE";
            case END_STATE:
                return "END_STATE";
            case LOADED_STATE:
                return "LOADED_STATE";
            default:
                return"UNKNOWN";
        }
    }


    /**
     * Get the titles of the animations stored in the animation directory.
     *
     * @return titles of the animations
     */
    public String[] getAnimationTitles(){
        return mAnimationDir.list();
    }


    /**
     * Get a reference to the AnimationDrawable of the loaded animation. The receiver should only
     * use the reference to call View.getAnimationDrawable( AnimationDrawable).
     *
     * @return the AnimationDrawable of the loaded animation.
     */
    public AnimationDrawable getAnimationDrawable(){
        return mAnimationDrawable;
    }


    /**
     * @return true if not currently viewing first image in list.
     */
    public boolean canBackward() {
        return mCurrentModelIndex != 0;
    }


    /**
     * @return true if not currently viewing last image in list.
     */
    public boolean canForward() {
        return mCurrentModelIndex != mNumModels-1;
    }


    /**
     * Checks to see if the currently selected mandelbrot image can be deleted.
     *
     * @return true if the currently selected mandelbrot image is not the first in the list.
     */
    public boolean canDelete(){
        return mCurrentModelIndex > 0;
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
     * @param numParts the listener will be notified this many times  throughout the duration
     *                 of task about how much progress has been made.
     */
    public void setProgressIncrementListener(ProgressListener listener, int numParts){
        mProgressListener = listener;
        mProgressParts = numParts;
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
        //Todo: make it so that changing the fields on the default image does not override the default image
        //Todo: make is sot that the surface changes dosn't reset the list
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
        mMyBitmap = Bitmap.createBitmap(mImageLength, mImageLength, Bitmap.Config.ARGB_8888);
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
         * Called when the image is about to be drawn.
         *
         * @param maxProgress set how many times you want to get notified about progress.
         */
        void onProgressStart(int maxProgress);


        /**
         * Called when the image has made progress.
         *
         * @param progress the amount of progress out of maxProgress that has been made
         */
        void onProgressUpdate(int progress);


        /**
         * Called when the Image has finished drawing.
         */
        void onProgressFinished();
    }
}
