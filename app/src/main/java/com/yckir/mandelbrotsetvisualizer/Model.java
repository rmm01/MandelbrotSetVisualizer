package com.yckir.mandelbrotsetvisualizer;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class Model{
    public static String TAG = "MODEL";
    private final double DEFAULT_CENTER_REAL;
    private final double DEFAULT_CENTER_IMAGINARY;
    private final double DEFAULT_EDGE_LENGTH;
    private final int    DEFAULT_ITERATION_LIMIT;

    private double mCenterReal;
    private double mCenterImaginary;
    private double mEdgeLength;
    private int    mIterationLimit;
    private int    mNumPixels;


    Model(Context context) {

        Resources resources = context.getResources();
        DEFAULT_CENTER_REAL = Double.parseDouble( resources.getString( R.string.DEFAULT_CENTER_REAL ) );
        DEFAULT_CENTER_IMAGINARY = Double.parseDouble( resources.getString( R.string.DEFAULT_CENTER_IMAGINARY) );
        DEFAULT_EDGE_LENGTH = Double.parseDouble( resources.getString( R.string.DEFAULT_EDGE_LENGTH ) );
        DEFAULT_ITERATION_LIMIT = Integer.parseInt(resources.getString(R.string.DEFAULT_ITERATION_LIMIT));
        mCenterReal = DEFAULT_CENTER_REAL;
        mCenterImaginary = DEFAULT_CENTER_IMAGINARY;
        mEdgeLength = DEFAULT_EDGE_LENGTH;
        mIterationLimit = DEFAULT_ITERATION_LIMIT;
        mNumPixels=100;
    }


    Model( double centerReal, double centerImaginary, double edgeLength, int iterationLimit , Context context) {
        Resources resources = context.getResources();
        DEFAULT_CENTER_REAL = Double.parseDouble( resources.getString( R.string.DEFAULT_CENTER_REAL ) );
        DEFAULT_CENTER_IMAGINARY = Double.parseDouble( resources.getString( R.string.DEFAULT_CENTER_IMAGINARY) );
        DEFAULT_EDGE_LENGTH = Double.parseDouble(resources.getString(R.string.DEFAULT_EDGE_LENGTH));
        DEFAULT_ITERATION_LIMIT = Integer.parseInt(resources.getString(R.string.DEFAULT_ITERATION_LIMIT));
        mCenterReal = centerReal;
        mCenterImaginary = centerImaginary;
        mEdgeLength = edgeLength;
        mIterationLimit = iterationLimit;
        mNumPixels=100;
    }


    /**
     * Determines the color of a pixel on the mandelbrot set.
     *
     * @param iterationCount how many iterations were required to determine if the
     *                       point was or wasn't in the mandelbrot set.
     * @return the color of the pixel
     */
    private int getColor( int iterationCount ){
        if(iterationCount==mIterationLimit)
            return Color.BLACK;

        float fraction = ( float )iterationCount / ( float )mIterationLimit;
        int r = ( int )( fraction * 255 );
        int b = ( int )( ( 1-fraction ) * 255 );
        return Color.argb(255, r, 0, b);
    }


    /**
     * Determines how many iterations are required to determine if a point lies on
     * the mandelbrot set. This method checks up to the iteration Limit that was defined
     * when the object was constructed. Reference coded from
     * https://en.wikipedia.org/wiki/Mandelbrot_set#Escape_time_algorithm
     *
     * @param x0 the real coordinate
     * @param y0 the imaginary coordinate
     * @return how many iterations were required to determine if the point was or wasn't in the
     *         mandelbrot set.
     */
    private int getIterationCount( double x0, double y0 ) {
        double x = x0;
        double y = y0;
        final double ONE_FOURTH =0.25;
        final double ONE_SIXTEENTH = ONE_FOURTH * ONE_FOURTH;

        /* The 2 quick checks below (cardoid and period-2 bulb) see if the point is in
         * a known region of the Mandelbrot set, which may speed up the overall computation.
         */

        // in cardoid ?
        double xMinusOneFourth = x - ONE_FOURTH;
        double ySquared = y * y;
        double q = xMinusOneFourth * xMinusOneFourth + ySquared;
        if ( q * ( q + xMinusOneFourth ) < ONE_FOURTH * ySquared )
        {
            return mIterationLimit;
        }

        // in period-2 bulb ?
        double xPlusOne = x + 1.0;
        if ( xPlusOne * xPlusOne + ySquared < ONE_SIXTEENTH )
        {
            return mIterationLimit;
        }

        // perform typical iterationCount computation
        int iterationCount = 0;
        for ( double xtemp ; x*x + y*y <= 4.0 && iterationCount < mIterationLimit; iterationCount++ )
        {
            xtemp = x*x - y*y + x0;
            y = 2*x*y + y0;
            x = xtemp;
        }
        return iterationCount;

    }


    /**
     * Reset the object to its default values, excluding the image size.
     */
    void reset() {
        mCenterReal = DEFAULT_CENTER_REAL;
        mCenterImaginary = DEFAULT_CENTER_IMAGINARY;
        mEdgeLength = DEFAULT_EDGE_LENGTH;
        mIterationLimit = DEFAULT_ITERATION_LIMIT;
    }


    /**
     * Given a pixel on an image, this functions re-centers on the pixel and zooms in by a factor
     * of two.
     *
     * @param x the pixel in x direction that was selected
     * @param y the pixel in y direction that was selected
     */
    void recenterZoom(int x, int y) {
        //Log.v(TAG, "recenterZoom with x = " + x + ", y = " + y);
        //Log.v(TAG,this.toString());
        y = mNumPixels - y;
        double dPixels = mNumPixels;

        mCenterReal += mEdgeLength * ( x / dPixels - 0.5 );
        mCenterImaginary += mEdgeLength * ( y / dPixels - 0.5 );
        mEdgeLength = mEdgeLength / 2.0;
        //Log.v(TAG, this.toString());
    }



    /**
     * Draws the mandelbrot set onto the given canvas.
     *
     * @param canvas the canvas that will be drawn onto
     */
    void drawCanvas(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        long startTime = System.currentTimeMillis();
        Paint paint = new Paint();
        double delta = mEdgeLength / mNumPixels;
        double x = mCenterReal - mEdgeLength / 2.0;
        for ( int pixel_x = 0; pixel_x < mNumPixels; pixel_x++, x += delta )
        {
            double y = mCenterImaginary - mEdgeLength / 2.0;
            for ( int pixel_y = 0; pixel_y < mNumPixels; pixel_y++, y += delta )
            {
                paint.setColor(getColor(getIterationCount(x,y)));
                canvas.drawPoint(pixel_x, mNumPixels - 1 - pixel_y, paint);
            }
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        Log.v(TAG, "Model.drawCanvas: " + elapsedTime + " ms");
    }


    /**
     * Draw a percent of the image onto the canvas. the percents must follow this rule.
     * 0 <= startPercent < endPercent <= 100. This function should be used to draw the total
     * image in parts so that you can know how much progress is being made since the drawing takes
     * seconds to compleate.
     *
     * @param startPercent the portion of the image where the drawing will begin
     * @param endPercent the portion of the image where the drawing will end.
     * @param canvas the canvas to be drawn on.
     */
    void partialDrawCanvas(int startPercent, int endPercent, Canvas canvas){
        //long startTime = System.currentTimeMillis();

        if(startPercent < 0 || startPercent > 100){
            Log.e(TAG, "startPercent out of bounds");
            return;
        }
        if(endPercent < 0 || endPercent > 100){
            Log.e(TAG, "endPercent out of bounds");
            return;
        }
        if(endPercent < startPercent){
            Log.e(TAG, "endPercent less than start percent");
            return;
        }


        int startX = (int)(startPercent / 100.0 * mNumPixels);
        int endX = (int)(endPercent / 100.0 * mNumPixels) - 1;

        //Log.v(TAG,"x = " + startX + ", y = " + endX);
        Paint paint = new Paint();
        double delta = mEdgeLength / mNumPixels;
        double x = mCenterReal - mEdgeLength / 2.0 + delta*startX;
        for ( int pixel_x = startX; pixel_x <= endX; pixel_x++, x += delta )
        {
            double y = mCenterImaginary - mEdgeLength / 2.0;
            for ( int pixel_y = 0; pixel_y < mNumPixels; pixel_y++, y += delta )
            {
                paint.setColor(getColor(getIterationCount(x,y)));
                canvas.drawPoint(pixel_x, mNumPixels - 1 - pixel_y, paint);
            }
        }
    }


    /**
     * @param newCenterReal the x coordinate on the real axis
     */
    void setCenterReal(double newCenterReal) {
        mCenterReal = newCenterReal;
    }


    /**
     * @param newCenterImag the y coordinate on the imaginary axis
     */
    void setCenterImag(double newCenterImag) {
        mCenterImaginary = newCenterImag;
    }


    /**
     * @param newEdgeLength the length of a pixel on the mandelbrot image. This value can be thought
     *                      of as zoom, a small value results in zooming towards the image.
     */
    void setEdgeLength(double newEdgeLength) {
        mEdgeLength = newEdgeLength;
    }


    /**
     * @param newIterationLimit how many checks will be performed to determin if a point is on the
     *                          mandelbrot set.
     */
    void setIterationLimit(int newIterationLimit) {
        mIterationLimit = newIterationLimit;
    }


    /**
     * @param numPixels the number of pixels in the image.
     */
    void setNumPixels(int numPixels){
        mNumPixels = numPixels;
    }


    /**
     * @return the x coordinate on the real axis
     */
    double getCenterReal(){ return mCenterReal; }


    /**
     * @return the y coordinate on the imaginary axis
     */
    double getCenterImaginary(){	return mCenterImaginary; }


    /**
     * @return the length of a pixel on the mandelbrot image. This value can be thought
     *                      of as zoom, a small value results in zooming towards the image.
     */
    double getEdgeLength(){ return mEdgeLength; }


    /**
     * @return how many checks will be performed to determin if a point is on the
     *                          mandelbrot set.
     */
    int getIterationLimit(){ return mIterationLimit; }


    /**
     * @return the number of pixels in the image.
     */
    int getNumPixels(){ return mNumPixels; }


    @Override
    public String toString() {
        ClassStateString details = new ClassStateString("Model");
        details.addMember("mCenterReal", mCenterReal);
        details.addMember("mCenterImaginary", mCenterImaginary);
        details.addMember("mEdgeLength", mEdgeLength);
        details.addMember("mIterationLimit", mIterationLimit);
        details.addMember("mNumPixels", mNumPixels);
        return details.getString();
    }
}
