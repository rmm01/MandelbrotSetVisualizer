package com.yckir.mandelbrotsetvisualizer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import me.zhanghai.android.materialprogressbar.HorizontalProgressDrawable;

public class MandelbrotActivity extends AppCompatActivity implements View.OnTouchListener,
        View.OnKeyListener, MandelbrotView.ProgressListener{

    public static String TAG = "MANDELBROT_ACTIVITY";
    public static  int MAX_PROGRESS = 100;

    private MandelbrotView mMandelbrotView;
    private ProgressBar mProgressBar;

    private EditText centerRealTextField;
    private EditText centerImaginaryTextField;
    private EditText edgeLengthTextField;
    private EditText iterationLimitTextField;
    private EditText frameRateTextField;
    private EditText recordTimeTextField;
    private EditText numFramesTextField;

    private Button forwardButton;
    private Button backButton;
    private Button deleteButton;
    private Button homeButton;
    private Button loadButton;
    private Button saveButton;
    private Button recordButton;
    private Button playButton;
    private Button closeButton;

    private ImageView mAnimationView;

    private AlertDialog mRecordDialog;
    private AlertDialog mSaveFileDialog;
    private AlertDialog.Builder mLoadAnimationBuilder;
    private DialogInterface.OnClickListener mLoadListener;

    private DelayedRecordHandler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandelbrot);

        mMandelbrotView = (MandelbrotView) findViewById(R.id.mandelbrot_view);
        mAnimationView = (ImageView) findViewById(R.id.animationImageView);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setProgressDrawable(new HorizontalProgressDrawable(this));


        centerRealTextField = (EditText) findViewById(R.id.centerRealTextField);
        centerImaginaryTextField = (EditText) findViewById(R.id.centerImaginaryTextField);
        edgeLengthTextField = (EditText) findViewById(R.id.edgeLengthTextField);
        iterationLimitTextField = (EditText) findViewById(R.id.iterationLimitTextField);
        frameRateTextField = (EditText) findViewById(R.id.frameRateTextField);
        recordTimeTextField = (EditText) findViewById(R.id.recordTimeTextField);
        numFramesTextField = (EditText) findViewById(R.id.numFramesTextField);

        forwardButton = (Button) findViewById(R.id.forwardButton);
        backButton = (Button) findViewById(R.id.backButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        homeButton = (Button) findViewById(R.id.homeButton);
        saveButton = (Button) findViewById(R.id.saveButton);
        loadButton = (Button) findViewById(R.id.loadButton);
        recordButton = (Button) findViewById(R.id.recordButton);
        playButton = (Button) findViewById(R.id.playButton);
        closeButton = (Button) findViewById(R.id.closeButton);

        centerRealTextField.setOnKeyListener(this);
        centerImaginaryTextField.setOnKeyListener(this);
        edgeLengthTextField.setOnKeyListener(this);
        iterationLimitTextField.setOnKeyListener(this);
        frameRateTextField.setOnKeyListener(this);
        recordTimeTextField.setOnKeyListener(this);

        mMandelbrotView.setOnTouchListener(this);
        mMandelbrotView.setProgressIncrementListener(this, MAX_PROGRESS);
        mHandler = new DelayedRecordHandler(this);

        updateWidgetStates();
        createDialogs();
    }

    /**
     * Create and set Dialogs for load, and record.
     */
    private void createDialogs(){
        //file
        final EditText saveFileEditText = new EditText(this);
        AlertDialog.Builder saveFileDialogBuilder = new AlertDialog.Builder(this);
        saveFileDialogBuilder.setTitle("Save Image");
        saveFileDialogBuilder.setMessage("Image Name");
        saveFileDialogBuilder.setView(saveFileEditText);
        saveFileDialogBuilder.setNegativeButton("Cancel", null);
        saveFileDialogBuilder.setPositiveButton("accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = saveFileEditText.getText().toString();
                String message = mMandelbrotView.saveCurrentImage(text);
                Toast.makeText(MandelbrotActivity.this, message, Toast.LENGTH_SHORT).show();
                updateWidgetStates();
                saveFileEditText.setText("");
            }
        });
        mSaveFileDialog = saveFileDialogBuilder.create();

        //load
        mLoadAnimationBuilder = new AlertDialog.Builder(this);
        mLoadAnimationBuilder.setTitle("Load Animation");
        mLoadListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //mCurrentAnimationDir = mMandelbrotView.getAnimationTitles()[which];
                mMandelbrotView.load(which);
                mAnimationView.setImageDrawable(mMandelbrotView.getAnimationDrawable());
                updateWidgetStates();
            }
        };
        mLoadAnimationBuilder.setItems(mMandelbrotView.getAnimationTitles(), mLoadListener);
        mLoadAnimationBuilder.setNegativeButton("cancel", null);

        //animation
        AlertDialog.Builder saveAnimationDialogBuilder = new AlertDialog.Builder(this);
        final EditText saveAnimationEditText = new EditText(this);
        saveAnimationDialogBuilder.setTitle("Record Animation");
        saveAnimationDialogBuilder.setMessage("Animation Name");
        saveAnimationDialogBuilder.setView(saveAnimationEditText);
        saveAnimationDialogBuilder.setNegativeButton("Cancel", null);
        saveAnimationDialogBuilder.setPositiveButton("accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle b = new Bundle();
                b.putString(DelayedRecordHandler.FILENAME,saveAnimationEditText.getText().toString());
                Message message = Message.obtain();
                message.setData(b);
                mHandler.sendMessageDelayed(message,300);
                saveAnimationEditText.setText("");

            }
        });
        mRecordDialog = saveAnimationDialogBuilder.create();
    }


    public void forwardButtonClick(View view) {
        Log.v(TAG, "forwardButtonClick");
        mMandelbrotView.forward();
        mMandelbrotView.getModelValues(centerRealTextField, centerImaginaryTextField,
                edgeLengthTextField, iterationLimitTextField);

        updateWidgetStates();
    }


    public void backButtonClick(View view) {
        Log.v(TAG, "backButtonClick");
        mMandelbrotView.backward();
        mMandelbrotView.getModelValues(centerRealTextField, centerImaginaryTextField,
                edgeLengthTextField, iterationLimitTextField);

        updateWidgetStates();
    }


    public void deleteButtonClick(View view) {
        Log.v(TAG, "deleteButtonClick");
        mMandelbrotView.delete();
        mMandelbrotView.getModelValues(centerRealTextField, centerImaginaryTextField,
                edgeLengthTextField, iterationLimitTextField);

        updateWidgetStates();

    }


    public void homeButtonClick(View view) {
        Log.v(TAG, "homeButtonClick");
        mMandelbrotView.reset();
        mMandelbrotView.getModelValues(centerRealTextField, centerImaginaryTextField,
                edgeLengthTextField, iterationLimitTextField);

        updateWidgetStates();
    }


    public void saveButtonClick(View view) {
        Log.v(TAG, "saveButtonClick");
        mSaveFileDialog.show();
        updateWidgetStates();
    }


    public void recordButtonClick(View view) {
        Log.v(TAG, "recordButtonClick");
        mRecordDialog.show();
        updateWidgetStates();
    }


    public void loadButtonClick(View view) {
        Log.v(TAG, "loadButtonClick");
        mLoadAnimationBuilder.setItems(mMandelbrotView.getAnimationTitles(), mLoadListener);
        AlertDialog loadDialog = mLoadAnimationBuilder.create();
        loadDialog.show();
    }


    public void playButtonClick(View view) {
        Log.v(TAG, "playButtonClick");
        mAnimationView.setVisibility(View.VISIBLE);
        mMandelbrotView.playAnimation();
        updateWidgetStates();
    }


    public void closeButtonClick(View view) {
        Log.v(TAG, "closeButtonClick");
        mAnimationView.setVisibility(View.INVISIBLE);
        mMandelbrotView.closeAnimation();
        updateWidgetStates();
    }


    public void updateFrameFields(){
        Log.v(TAG, "recordTimeTextFieldEvent");
        int fps = Integer.parseInt( frameRateTextField.getText().toString() );
        int time = Integer.parseInt(recordTimeTextField.getText().toString());

        numFramesTextField.setText(Integer.toString(fps * time));
    }


    public void updateDrawingFields(){

        int iteration = Integer.parseInt(iterationLimitTextField.getText().toString());
        double coordinateR = Double.parseDouble( centerRealTextField.getText().toString() );
        double coordinateI = Double.parseDouble(centerImaginaryTextField.getText().toString());
        double edge = Double.parseDouble(edgeLengthTextField.getText().toString());

        mMandelbrotView.setModelValues(coordinateR, coordinateI, edge, iteration);
    }

    //Todo: put toast strings  and dialog strings into strings.xml
    //Todo: make all fields be validated when any text field hits enter
    //Todo: fix bug where view doesn't always scroll up when keyboard is made visible
    //TODO: implement toString for classes

    public boolean validateFrameFields(){
        String toastMessage = "";
        try{
            int fps = Integer.parseInt(frameRateTextField.getText().toString());
            if(fps == 0)
                toastMessage+="Frames Per Second cannot be 0\n";
        }catch (NumberFormatException e){
            toastMessage+="invalid format for Frames Per Second\n";
        }

        try{
            int time = Integer.parseInt(recordTimeTextField.getText().toString());
            if(time == 0)
                toastMessage+="Duration cannot be 0\n";
        }catch (NumberFormatException e){
            toastMessage+="invalid format for Duration\n";
        }

        if(toastMessage.isEmpty())
            return true;

        Toast.makeText(MandelbrotActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
        return false;

    }


    public boolean validateDrawingFields() {
        String toastMessage = "";

        try{
            double centerReal = Double.parseDouble(centerRealTextField.getText().toString());
        }catch (NumberFormatException e){
            toastMessage+="invalid format for Real Coordinate\n";
        }

        try{
            double centerImag = Double.parseDouble(centerImaginaryTextField.getText().toString());
        }catch (NumberFormatException e){
            toastMessage+="invalid format for Imaginary Coordinate\n";
        }

        try{
            double edgeLength = Double.parseDouble(edgeLengthTextField.getText().toString());
            if(edgeLength == 0)
                toastMessage+="Edge Length cannot be zero\n";
        }catch (NumberFormatException e){
            toastMessage+="invalid format for Edge Length\n";
        }

        try{
            int iterationLimit = Integer.parseInt(iterationLimitTextField.getText().toString());
        }catch (NumberFormatException e){
            toastMessage+="invalid format for Iteration Limit\n";
        }


        if(toastMessage.isEmpty())
            return true;
        Toast.makeText(MandelbrotActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
        return false;
    }


    public void updateWidgetStates(){
        int state = mMandelbrotView.getNavigationState();
        switch (state){
            case MandelbrotView.SINGLE_STATE:
                forwardButton.setEnabled( false );
                backButton.setEnabled(    false );
                deleteButton.setEnabled(  false );
                homeButton.setEnabled(    false );
                saveButton.setEnabled(    true );
                recordButton.setEnabled(  false );
                loadButton.setEnabled(    true);
                playButton.setEnabled(    false);
                closeButton.setEnabled(false);
                break;
            case MandelbrotView.CENTER_STATE:
                forwardButton.setEnabled( true );
                backButton.setEnabled(    true );
                deleteButton.setEnabled(  true );
                homeButton.setEnabled(    true );
                saveButton.setEnabled(    true );
                recordButton.setEnabled(  true );
                loadButton.setEnabled(    true);
                playButton.setEnabled(    false);
                closeButton.setEnabled(false);
                break;
            case MandelbrotView.END_STATE:
                forwardButton.setEnabled( false );
                backButton.setEnabled(    true );
                deleteButton.setEnabled(  true );
                homeButton.setEnabled(    true );
                saveButton.setEnabled(    true );
                recordButton.setEnabled(  true );
                loadButton.setEnabled(    true);
                playButton.setEnabled(    false);
                closeButton.setEnabled(   false);
                break;
            case MandelbrotView.FRONT_STATE:
                forwardButton.setEnabled( true );
                backButton.setEnabled(    false );
                deleteButton.setEnabled(  false );
                homeButton.setEnabled(    true );
                saveButton.setEnabled(    true );
                recordButton.setEnabled(  true );
                loadButton.setEnabled(    true);
                playButton.setEnabled(    false);
                closeButton.setEnabled(   false);
                break;
            case MandelbrotView.LOADED_STATE:
                forwardButton.setEnabled( false );
                backButton.setEnabled(    false );
                deleteButton.setEnabled(  false );
                homeButton.setEnabled(    false );
                saveButton.setEnabled(    false );
                recordButton.setEnabled(  false );
                loadButton.setEnabled(    false);
                playButton.setEnabled(    true);
                closeButton.setEnabled(   true);
                break;
        }

        if(state==MandelbrotView.LOADED_STATE) {
            centerRealTextField.setEnabled(false);
            centerImaginaryTextField.setEnabled(false);
            edgeLengthTextField.setEnabled(false);
            iterationLimitTextField.setEnabled(false);
            frameRateTextField.setEnabled(false);
            recordTimeTextField.setEnabled(false);
        }else
        {
            centerRealTextField.setEnabled(true);
            centerImaginaryTextField.setEnabled(true);
            edgeLengthTextField.setEnabled(true);
            iterationLimitTextField.setEnabled(true);
            frameRateTextField.setEnabled(true);
            recordTimeTextField.setEnabled(true);
        }
    }


    /**
     * Zooms onto a pixel location. Fails if pixel is not on the mandelbrot image.
     *
     * @param x x pixel coordinate
     * @param y y pixel coordinate
     */
    public void zoom(int x, int y){
        if(!mMandelbrotView.zoom(x, y))
            return;

        mMandelbrotView.getModelValues(centerRealTextField, centerImaginaryTextField,
                edgeLengthTextField, iterationLimitTextField);
    }


    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if(e.getAction()!= MotionEvent.ACTION_UP)
            return true;
        double x = e.getX();
        double y = e.getY();

        Log.v( TAG,"X = " + x + ", Y = " + y );
        zoom((int) x, (int) y);
        return true;
    }


    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if( keyCode != KeyEvent.KEYCODE_ENTER || event.getAction() != KeyEvent.ACTION_UP)
            return false;

        if( v == frameRateTextField || v == recordTimeTextField ) {
           if(validateFrameFields())
                updateFrameFields();
            return false;
        } else if( validateDrawingFields() )
            updateDrawingFields();

        return false;
    }


    @Override
    public void onProgressStart(int maxProgress) {
        Log.v("PRO","starting " + maxProgress);
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        mProgressBar.setProgress(0);
        mProgressBar.setMax(maxProgress);
        mProgressBar.setVisibility(View.VISIBLE);

        centerRealTextField.setEnabled(false);
        centerImaginaryTextField.setEnabled(false);
        edgeLengthTextField.setEnabled(false);
        iterationLimitTextField.setEnabled(false);
        frameRateTextField.setEnabled(false);
        recordTimeTextField.setEnabled(false);

        forwardButton.setEnabled(false);
        backButton.setEnabled(false);
        deleteButton.setEnabled(false);
        homeButton.setEnabled(false);
        saveButton.setEnabled(false);
        loadButton.setEnabled(false);
        recordButton.setEnabled(false);
        playButton.setEnabled(false);
        closeButton.setEnabled(false);

    }


    @Override
    public void onProgressUpdate(int progress) {
        //Log.v("PRO","progress = " + progress);
        mProgressBar.setProgress(progress);
    }


    @Override
    public void onProgressFinished() {
        Log.v("PRO","DONE");
        mProgressBar.setVisibility(View.GONE);
        centerRealTextField.setEnabled(true);
        centerImaginaryTextField.setEnabled(true);
        edgeLengthTextField.setEnabled(true);
        iterationLimitTextField.setEnabled(true);
        frameRateTextField.setEnabled(true);
        recordTimeTextField.setEnabled(true);
        updateWidgetStates();
    }

    /**
     * record dialog box was closing and progressbar opening at same time would cause progress
     * bar to remain invisible for the duration of the recording. This handler is to stagger the two
     * operations.
     */
    public static class DelayedRecordHandler extends Handler {
        private final WeakReference<MandelbrotActivity> mWeakMandelReference;
        public static final String FILENAME = "FILENAME";
        public DelayedRecordHandler(MandelbrotActivity activity){
            mWeakMandelReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MandelbrotActivity activity = mWeakMandelReference.get();
            if(activity == null)
                return;
            Bundle b = msg.getData();
            if(b==null)
                return;
            String fileName = b.getString(FILENAME);
            int fps = Integer.parseInt(activity.frameRateTextField.getText().toString());
            int duration = Integer.parseInt(activity.recordTimeTextField.getText().toString());
            activity.mMandelbrotView.recordAnimation(fps, duration, fileName);

        }
    }
}
