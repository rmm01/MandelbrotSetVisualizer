package com.yckir.mandelbrotsetvisualizer;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MandelbrotActivity extends AppCompatActivity implements View.OnTouchListener, View.OnKeyListener{

    public static String TAG = "MANDELBROT_ACTIVITY";

    private Model model;
    private GestureDetector mGestureDetector;
    private ClickListener mClickListener;

    private MandelbrotView mMandelbrotView;

    private MaterialProgressBar mProgressBar;

    private EditText centerRealTextField;
    private EditText centerImageTextField;
    private EditText edgeLengthTextField;
    private EditText iterationLimitTextField;
    private EditText frameRateTextField;
    private EditText recordTimeTextField;
    private EditText numFramesTextField;

    private Button forwardButton;
    private Button backButton;
    private Button deleteButton;
    private Button homeButton;
    private Button saveButton;
    private Button recordButton;
    private Button playButton;
    private Button stopButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mandelbrot);



        mClickListener = new ClickListener();
        mGestureDetector = new GestureDetector(this,mClickListener);

        mMandelbrotView = (MandelbrotView) findViewById(R.id.mandelbrot_view);
        mProgressBar = (MaterialProgressBar) findViewById(R.id.progress_bar);

        //mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        //Log.v(TAG, "is indeterminate = " + mProgressBar.isIndeterminate());
        //mProgressBar.setIndeterminate(false);
        //Log.v(TAG, "is indeterminate = " + mProgressBar.isIndeterminate());

        centerRealTextField = (EditText) findViewById(R.id.centerRealTextField);
        centerImageTextField = (EditText) findViewById(R.id.centerImageTextField);
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
        recordButton = (Button) findViewById(R.id.recordButton);
        playButton = (Button) findViewById(R.id.playButton);
        stopButton = (Button) findViewById(R.id.stopButton);



        centerRealTextField.setOnKeyListener(this);
        centerImageTextField.setOnKeyListener(this);
        edgeLengthTextField.setOnKeyListener(this);
        iterationLimitTextField.setOnKeyListener(this);
        //frameRateTextField.setOnKeyListener(this);
        //recordTimeTextField.setOnKeyListener(this);
        //numFramesTextField.setOnKeyListener(this);

        mMandelbrotView.setOnTouchListener(this);

        model = new Model(this);
        model.setNumPixels(300);

        mMandelbrotView.setModel(model);

        mMandelbrotView.setProgressBar(mProgressBar);

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public void forwardButtonClick(View view) {
        Log.v(TAG, "forwardButtonClick");
    }


    public void backButtonClick(View view) {
        Log.v(TAG, "backButtonClick");
    }


    public void deleteButtonClick(View view) {
        Log.v(TAG, "deleteButtonClick");
    }


    public void homeButtonClick(View view) {
        Log.v(TAG, "homeButtonClick");
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


/*
    public void centerRealTextFieldEvent(View view) {
        Log.v(TAG, "centerRealTextFieldEvent");
        //boolean b =validateDrawingFields();
        //if(b) {
        //    updateDrawingFields();
       // }
    }

    public void centerImaginaryTextFieldEvent(View view) {
        Log.v(TAG, "centerImaginaryTextFieldEvent");
     /*   boolean b =validateDrawingFields();
        if(b) {
            updateDrawingFields();
        }* /
    }

    public void edgeLengthTextFieldEvent(View view) {
        Log.v(TAG, "edgeLengthTextFieldEvent");
       // boolean b =validateDrawingFields();
       // if(b) {
       //     updateDrawingFields();
       // }
    }

    public void iterationLimitTextFieldEvent(View view) {
        Log.v(TAG, "iterationLimitTextFieldEvent");
       /* boolean b =validateDrawingFields();
        if(b) {
            updateDrawingFields();
        }* /

    }

    public void frameRateTextFieldEvent(View view) {
        Log.v(TAG, "frameRateTextFieldEvent");
        int fps = Integer.parseInt( frameRateTextField.getText().toString() );
        int time = Integer.parseInt( recordTimeTextField.getText().toString() );

        numFramesTextField.setText( Integer.toString(fps * time) );

    }

    public void recordTimeTextFieldEvent(View view) {
        Log.v(TAG, "recordTimeTextFieldEvent");
        int fps = Integer.parseInt( frameRateTextField.getText().toString() );
        int time = Integer.parseInt( recordTimeTextField.getText().toString() );

        numFramesTextField.setText(Integer.toString(fps * time));
    }
*/
    public void updateFrameFields(View view){
        Log.v(TAG, "recordTimeTextFieldEvent");
        int fps = Integer.parseInt( frameRateTextField.getText().toString() );
        int time = Integer.parseInt( recordTimeTextField.getText().toString() );

        numFramesTextField.setText(Integer.toString(fps * time));
    }

   // public void numFramesTextFieldEvent(View view) {
   //     Log.v(TAG, "numFramesTextFieldEvent");
   // }

    public boolean validateDrawingFields(){
        boolean b1 = validateRealCoordinate();
        boolean b2 = validateImagCoordinate();
        boolean b3 = validateEdgeLengthCoordinate();
        boolean b4 = validateIterationLimitCoordinate();
        return (b1 && b2 && b3 && b4);

    }


    private boolean validateRealCoordinate() {
        return true;
    }


    private boolean validateImagCoordinate() {
        return true;
    }


    private boolean validateEdgeLengthCoordinate() {
        double value = Double.parseDouble(edgeLengthTextField.getText().toString());
        if ( value <= 0 ){
            Log.v(TAG, "error, iteration limit <= 0");
            //edgeLengthTextField.setText ( Double.toString( model.getEdgeLength() ) );
            return false;
        }
        return true;

    }


    private boolean validateIterationLimitCoordinate() {
        int value = Integer.parseInt( iterationLimitTextField.getText().toString() );
        if ( value <= 0 ){
            Log.v(TAG, "error, iteration limit <= 0");
            //iterationLimitTextField.setText ( Integer.toString( model.getIterationLimit() ) );
            return false;
        }
        return true;
    }


    public void updateDrawingFields(){

        int value = Integer.parseInt(iterationLimitTextField.getText().toString());
        model.setIterationLimit(value);

        double coordinateR = Double.parseDouble( centerRealTextField.getText().toString() );
        model.setCenterReal(coordinateR);

        double coordinateI = Double.parseDouble( centerImageTextField.getText().toString() );
        model.setCenterImag(coordinateI);

        double edge = Double.parseDouble(edgeLengthTextField.getText().toString());
        model.setEdgeLength(edge);

        mMandelbrotView.redraw(0);
        //centerRealTextField.setText(Double.toString(model.getCenterReal()));
        //centerImageTextField.setText(Double.toString(model.getCenterImag()));
        //edgeLengthTextField.setText(Double.toString(model.getEdgeLength()));
        //iterationLimitTextField.setText(Integer.toString(model.getIterationLimit()));
    }


    public void zoom(double x, double y){
        model.recenter((int)x,(int)y);
        centerRealTextField.setText(Double.toString(model.getCenterReal()));
        centerImageTextField.setText(Double.toString(model.getCenterImag()));
        edgeLengthTextField.setText(Double.toString(model.getEdgeLength()));
        iterationLimitTextField.setText(Integer.toString(model.getIterationLimit()));
        mMandelbrotView.redraw(0);
    }


    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if(e.getAction()!= MotionEvent.ACTION_UP)
            return true;
        double x = e.getX();
        double y = e.getY();

        Log.v( TAG,"X = " + x + ", Y = " + y );
        zoom(x, y);
        return true;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if( keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP ){
            Log.v(TAG, "was enter action down");
            boolean b =validateDrawingFields();
            if(!b)
                return false;
            updateDrawingFields();
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            return true;
        }
        return false;
    }

    private class ClickListener extends GestureDetector.SimpleOnGestureListener{
        public String TAG = "ClickListener";
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            double x = e.getX();
            double y = e.getY();
            Log.v( TAG,"X = " + x + ", Y = " + y );
            return true;
        }
    }
}
