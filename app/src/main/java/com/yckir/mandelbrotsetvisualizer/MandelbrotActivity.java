package com.yckir.mandelbrotsetvisualizer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MandelbrotActivity extends AppCompatActivity implements View.OnTouchListener,
        View.OnKeyListener, MandelbrotView.ProgressListener{

    public static String TAG = "MANDELBROT_ACTIVITY";

    private MandelbrotView mMandelbrotView;
    private MaterialProgressBar mProgressBar;

    private EditText centerRealTextField;
    private EditText centerImaginaryTextField;
    private EditText edgeLengthTextField;
    private EditText iterationLimitTextField;
    private EditText frameRateTextField;
    private EditText recordTimeTextField;
    private EditText numFramesTextField;

    private Button forwardButton;
    private Button backButton;
    //private Button deleteButton;
    private Button homeButton;
    //private Button saveButton;
    //private Button recordButton;
    //private Button playButton;
    //private Button stopButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandelbrot);

        mMandelbrotView = (MandelbrotView) findViewById(R.id.mandelbrot_view);
        mProgressBar = (MaterialProgressBar) findViewById(R.id.progress_bar);

        centerRealTextField = (EditText) findViewById(R.id.centerRealTextField);
        centerImaginaryTextField = (EditText) findViewById(R.id.centerImaginaryTextField);
        edgeLengthTextField = (EditText) findViewById(R.id.edgeLengthTextField);
        iterationLimitTextField = (EditText) findViewById(R.id.iterationLimitTextField);
        frameRateTextField = (EditText) findViewById(R.id.frameRateTextField);
        recordTimeTextField = (EditText) findViewById(R.id.recordTimeTextField);
        numFramesTextField = (EditText) findViewById(R.id.numFramesTextField);

        forwardButton = (Button) findViewById(R.id.forwardButton);
        backButton = (Button) findViewById(R.id.backButton);
        //deleteButton = (Button) findViewById(R.id.deleteButton);
        homeButton = (Button) findViewById(R.id.homeButton);
        //saveButton = (Button) findViewById(R.id.saveButton);
        //recordButton = (Button) findViewById(R.id.recordButton);
        //playButton = (Button) findViewById(R.id.playButton);
        //stopButton = (Button) findViewById(R.id.stopButton);

        centerRealTextField.setOnKeyListener(this);
        centerImaginaryTextField.setOnKeyListener(this);
        edgeLengthTextField.setOnKeyListener(this);
        iterationLimitTextField.setOnKeyListener(this);
        frameRateTextField.setOnKeyListener(this);
        recordTimeTextField.setOnKeyListener(this);

        mMandelbrotView.setOnTouchListener(this);
        mMandelbrotView.setProgressListener(this,5);

    }


    public void forwardButtonClick(View view) {
        Log.v(TAG, "forwardButtonClick");
        mMandelbrotView.forward();
        backButton.setEnabled(true);
        forwardButton.setEnabled(mMandelbrotView.canForward());
        mMandelbrotView.getModelValues(centerRealTextField, centerImaginaryTextField,
                edgeLengthTextField, iterationLimitTextField);
    }


    public void backButtonClick(View view) {
        Log.v(TAG, "backButtonClick");
        mMandelbrotView.backward();
        forwardButton.setEnabled(true);
        backButton.setEnabled(mMandelbrotView.canBackward());
        mMandelbrotView.getModelValues(centerRealTextField, centerImaginaryTextField,
                edgeLengthTextField, iterationLimitTextField);
    }


    public void deleteButtonClick(View view) {
        Log.v(TAG, "deleteButtonClick");
    }


    public void homeButtonClick(View view) {
        Log.v(TAG, "homeButtonClick");
        mMandelbrotView.reset();
        backButton.setEnabled(false);
        forwardButton.setEnabled(false);
        mMandelbrotView.getModelValues(centerRealTextField, centerImaginaryTextField,
                edgeLengthTextField, iterationLimitTextField);
    }


    public void saveButtonClick(View view) {
        Log.v(TAG, "saveButtonClick");
    }


    public void recordButtonClick(View view) {
        Log.v(TAG, "recordButtonClick");
    }


    public void playButtonClick(View view) {
        Log.v(TAG, "playButtonClick");
    }


    public void stopButtonClick(View view) {
        Log.v(TAG, "stopButtonClick");
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

    //Todo: put toast strings into strings.xml
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


    /**
     * Zooms onto a pixel locaton. Fails if pixel is not on the mandelbrot image.
     *
     * @param x x pixel coordinate
     * @param y y pixel coordinate
     */
    public void zoom(int x, int y){
        if(!mMandelbrotView.zoom(x, y))
            return;

        mMandelbrotView.getModelValues(centerRealTextField, centerImaginaryTextField,
                edgeLengthTextField, iterationLimitTextField);
        backButton.setEnabled(true);
        forwardButton.setEnabled(mMandelbrotView.canForward());
    }


    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if(e.getAction()!= MotionEvent.ACTION_UP)
            return true;
        double x = e.getX();
        double y = e.getY();

        Log.v( TAG,"X = " + x + ", Y = " + y );
        zoom((int)x, (int)y);
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
    public void onProgressStart() {
        mProgressBar.setProgress(0);
        mProgressBar.setVisibility(View.VISIBLE);

        centerRealTextField.setEnabled(false);
        centerImaginaryTextField.setEnabled(false);
        edgeLengthTextField.setEnabled(false);
        iterationLimitTextField.setEnabled(false);
        frameRateTextField.setEnabled(false);
        recordTimeTextField.setEnabled(false);

        forwardButton.setVisibility(View.INVISIBLE);
        backButton.setVisibility(View.INVISIBLE);
        //deleteButton.setVisibility(View.INVISIBLE);
        homeButton.setVisibility(View.INVISIBLE);
        //saveButton.setVisibility(View.INVISIBLE);
        //recordButton.setVisibility(View.INVISIBLE);
        //playButton.setVisibility(View.INVISIBLE);
        //stopButton.setVisibility(View.INVISIBLE);

    }


    @Override
    public void onProgressUpdate(int progress) {
        Log.v(TAG,"progress = " + progress);
        mProgressBar.setProgress(progress);
    }


    @Override
    public void onProgressFinished() {
        mProgressBar.setVisibility(View.GONE);
        centerRealTextField.setEnabled(true);
        centerImaginaryTextField.setEnabled(true);
        edgeLengthTextField.setEnabled(true);
        iterationLimitTextField.setEnabled(true);
        frameRateTextField.setEnabled(true);
        recordTimeTextField.setEnabled(true);


        forwardButton.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        //deleteButton.setVisibility(View.VISIBLE);
        homeButton.setVisibility(View.VISIBLE);
        //saveButton.setVisibility(View.VISIBLE);
        //recordButton.setVisibility(View.VISIBLE);
        //playButton.setVisibility(View.VISIBLE);
        //stopButton.setVisibility(View.VISIBLE);
    }
}
