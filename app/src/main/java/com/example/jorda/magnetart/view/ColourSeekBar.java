package com.example.jorda.magnetart.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ColourSeekBar extends View {


    private int progress;// 0-100 value representing distance along the seekbar
    private Paint mPaint, mSliderpaint;
    int colouratposition;// Paint settings

    // CONSTRUCTOR
    public ColourSeekBar(Context context, AttributeSet attrs){
        super(context,attrs);
        setupSlider();
    }

    /**
     *Create the slider
     */
    private void setupSlider(){
        progress = 0;
        mPaint = new Paint();
        mSliderpaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
    }

    // Draw Method
    @Override
    protected void onDraw(Canvas canvas){

        float start = getWidth()*.1f;//10% along the width
        float end = getWidth()*.9f;//90% along the width
        float top = getHeight()*.4f;//40% of the height
        float bottom = getHeight()*.80f;//80% of the height

        // Draw the color palette- 0 is at start of rectangle, 100 is at end
        for(int i = 0; i <= 100; i++){
            colouratposition = convertValueToColor(i);//convert each position to an rgb colour value
            mPaint.setColor(colouratposition);//
            //Draw a rectangle with colour gradient by drawing a rectangle with each iteration
            canvas.drawRect(start + ((float)i/100.0f)*(end-start),top,start + ((float)(i+1)/100.0f)*(end-start),bottom,mPaint);
        }
        drawCursor(canvas,start,end,top,bottom);
    }

    /**Draw the cursor for the currently selected colour (this is a rectangle with a gap in the middle to allow the user to see the
     * selected colour- made up of four smaller rectangles
     */
    protected void drawCursor(Canvas canvas, float start, float end, float top, float bottom) {

        mSliderpaint.setColor(Color.BLACK);
        float progressPercent = progress/100.0f;

        //4 rectangles used to make rectangle with hole in middle
        canvas.drawRect(start + (progressPercent)*(end-start)-15, top, start + (progressPercent)*(end-start)-5, bottom, mSliderpaint);
        canvas.drawRect(start + (progressPercent)*(end-start)+5, top, start + (progressPercent)*(end-start)+15, bottom, mSliderpaint);
        canvas.drawRect(start + (progressPercent)*(end-start)-15, top-10, start + (progressPercent)*(end-start)+15, top,mSliderpaint);
        canvas.drawRect(start + (progressPercent)*(end-start)-15, bottom, start + (progressPercent)*(end-start)+15, bottom+10, mSliderpaint);

    }

    /**
     * Setter for the progress variable
     * @param progress 0-100
     */
    public void setProgress(int progress){
        this.progress = progress;
    }

    /**
     * Convert value along bar (0-100) to a color int (0-255)
     * @param val 0-100
     * @return colour 0-255
     */
    public static int convertValueToColor(float val){

        float red,green,blue;//Each colour is in the range 0-255 in rgb scale

        float i = 16*(val/100);
        red = (float)Math.sin(0.3*i)*127+128;//colour gradient is made using 3 out of phase sine waves
        green = (float)Math.sin(0.3*i+2)*127+128;
        blue = (float)Math.sin(0.3*i+4)*127+128;
        //returns a colour int made up of red, green and blue components
        return Color.rgb((int) red, (int) green, (int) blue);

    }

}
