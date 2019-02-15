package com.example.jorda.magnetart.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * This class forms a custom view which will be used for drawing with the magnetic stylus.
 * The input is taken from the screen co-ordinates calculated in the onSensorChanged method of the NewDrawingFragment class
 */
public class NewDrawingView extends View {

    //These are the possible shapes that the user can draw
    public enum DrawingShape{
        CIRCLE, TRIANGLE, RECTANGLE, SPOT, SMOOTHLINE
    }

    public static final float TOUCH_TOLERANCE = 4;
    public static final float TOUCH_STROKE_WIDTH = 5;

    public DrawingShape activeShape;

    protected Path pathToDraw;
    public Paint initialPaint;//must be public to allow access to change colour
    public Paint endingPaint;
    protected Bitmap bitmapImage;
    protected Canvas drawingCanvas;

    /* Check for drawing */
    protected boolean drawingActive = false;

    protected float beginX;
    protected float beginY;

    protected float xPos;
    protected float yPos;

    public float screenX = 0;//screen X and Y co-ordinates - these are set in the NewDrawingFragment
    public float screenY = 0;

    //Constructor
    public NewDrawingView(Context context) {
        super(context);
        initialise();//initialise
    }

    public NewDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    public NewDrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise();
    }

    protected void initialise() {

        pathToDraw = new Path();

        initialPaint = new Paint(Paint.DITHER_FLAG);
        initialPaint.setAntiAlias(true);
        initialPaint.setDither(true);
        initialPaint.setColor(Color.BLACK);//default colour
        initialPaint.setStyle(Paint.Style.STROKE);
        initialPaint.setStrokeJoin(Paint.Join.ROUND);
        initialPaint.setStrokeCap(Paint.Cap.ROUND);
        initialPaint.setStrokeWidth(TOUCH_STROKE_WIDTH);

        //this remains on screen when finger has been lifted
        endingPaint = new Paint(Paint.DITHER_FLAG);
        endingPaint.setAntiAlias(true);
        endingPaint.setDither(true);;
        endingPaint.setStyle(Paint.Style.STROKE);//change to fill shape with colour
        endingPaint.setStrokeJoin(Paint.Join.ROUND);
        endingPaint.setStrokeCap(Paint.Cap.ROUND);
        endingPaint.setStrokeWidth(TOUCH_STROKE_WIDTH);
    }

    @Override
    protected void onSizeChanged(int width, int height, int prevW, int prevH) {
        super.onSizeChanged(width, height, prevW, prevH);
        bitmapImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        drawingCanvas = new Canvas(bitmapImage);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmapImage, 0, 0, initialPaint);

        //default is to draw a spot on screen
        if(activeShape == DrawingShape.SPOT) {
            drawingCanvas.drawCircle(screenX, screenY, 10, endingPaint);
        }

        if (drawingActive){
            switch (activeShape) {
                case RECTANGLE:
                    onDrawRectangle(canvas);
                    break;
                case CIRCLE:
                    onDrawCircle(canvas);
                    break;
                case TRIANGLE:
                    onDrawTriangle(canvas);
                    break;
            }
        }
    }



    public void reset() {
        pathToDraw = new Path();
        countTouch=0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        xPos = screenX;
        yPos = screenY;
        switch (activeShape) {
            case SMOOTHLINE:
                onTouchEventSmoothLine(event);
                break;
            case RECTANGLE:
                onTouchEventRectangle(event);
                break;
            case CIRCLE:
                onTouchEventCircle(event);
                break;
            case TRIANGLE:
                onTouchEventTriangle(event);
                break;
        }
        return true;
    }

    // Drawable line
    private void onTouchEventSmoothLine(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawingActive = true;
                beginX = xPos;
                beginY = yPos;

                pathToDraw.reset();
                pathToDraw.moveTo(xPos, yPos);

                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:

                float distanceX = Math.abs(xPos - beginX);
                float distanceY = Math.abs(yPos - beginY);
                if (distanceX >= TOUCH_TOLERANCE || distanceY >= TOUCH_TOLERANCE) {
                    pathToDraw.quadTo(beginX, beginY, (xPos + beginX) / 2, (yPos + beginY) / 2);
                    beginX = xPos;
                    beginY = yPos;
                }
                drawingCanvas.drawPath(pathToDraw, initialPaint);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                drawingActive = false;
                pathToDraw.lineTo(beginX, beginY);
                drawingCanvas.drawPath(pathToDraw, endingPaint);
                pathToDraw.reset();
                invalidate();
                break;
        }

    }

    // Two stroke triangle

    int countTouch = 0;
    float basexTriangle = 0;
    float baseyTriangle = 0;

    private void onDrawTriangle(Canvas canvas){

        if (countTouch<3){
            canvas.drawLine(beginX,beginY,xPos,yPos,initialPaint);
        }else if (countTouch==3){
            canvas.drawLine(xPos,yPos,beginX,beginY,initialPaint);
            canvas.drawLine(xPos,yPos,basexTriangle,baseyTriangle,initialPaint);
        }
    }

    private void onTouchEventTriangle(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                countTouch++;
                if (countTouch==1){
                    drawingActive = true;
                    beginX = xPos;
                    beginY = yPos;
                } else if (countTouch==3){
                    drawingActive = true;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                countTouch++;
                drawingActive = false;
                if (countTouch<3){
                    basexTriangle=xPos;
                    baseyTriangle=yPos;
                    drawingCanvas.drawLine(beginX,beginY,xPos,yPos,endingPaint);
                } else{
                    drawingCanvas.drawLine(xPos,yPos,beginX,beginY,endingPaint);
                    drawingCanvas.drawLine(xPos,yPos,basexTriangle,baseyTriangle,endingPaint);
                    countTouch =0;
                }
                invalidate();
                break;
        }
    }

    // Midpoint drawable circle

    private void onDrawCircle(Canvas canvas){
        canvas.drawCircle(beginX, beginY, calculateRadius(beginX, beginY, xPos, yPos), initialPaint);
    }

    private void onTouchEventCircle(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawingActive = true;
                beginX = xPos;
                beginY = yPos;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                drawingActive = false;
                drawingCanvas.drawCircle(beginX, beginY, calculateRadius(beginX,beginY,xPos,yPos), endingPaint);
                invalidate();
                break;
        }
    }


    /**
     * Method to calculate radius of circle
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    protected float calculateRadius(float x1, float y1, float x2, float y2) {

        return (float) Math.sqrt(
                Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    // Rectangle

    private void onDrawRectangle(Canvas canvas) {
        drawRectangle(canvas,initialPaint);
    }

    private void onTouchEventRectangle(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawingActive = true;
                beginX = xPos;
                beginY = yPos;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                drawingActive = false;
                drawRectangle(drawingCanvas,endingPaint);
                invalidate();
                break;
        }
    }

    private void drawRectangle(Canvas canvas,Paint paint){
        float right = beginX > xPos ? beginX : xPos;
        float left = beginX > xPos ? xPos : beginX;
        float bottom = beginY > yPos ? beginY : yPos;
        float top = beginY > yPos ? yPos : beginY;
        canvas.drawRect(left, top , right, bottom, paint);
    }

    public void clearDrawing(){
        //erase the canvas
        bitmapImage.eraseColor(Color.WHITE);
        drawingCanvas.drawBitmap(bitmapImage, 0, 0, endingPaint);
    }

    public Bitmap getBitmap(){
        setDrawingCacheEnabled(true);
        buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(getDrawingCache());
        setDrawingCacheEnabled(false);
        return bitmap;
    }

}