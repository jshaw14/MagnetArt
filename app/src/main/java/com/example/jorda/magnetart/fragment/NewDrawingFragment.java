package com.example.jorda.magnetart.fragment;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jorda.magnetart.R;
import com.example.jorda.magnetart.activity.UserPreferencesActivity;
import com.example.jorda.magnetart.dialogFragment.BackgroundColourFragment;
import com.example.jorda.magnetart.dialogFragment.ClearDrawingFragment;
import com.example.jorda.magnetart.dialogFragment.StrokeColourFragment;
import com.example.jorda.magnetart.interfaces.BackgroundColourListener;
import com.example.jorda.magnetart.interfaces.ClearDrawingListener;
import com.example.jorda.magnetart.interfaces.StrokeColourListener;
import com.example.jorda.magnetart.utilities.CalibrationCalculations;
import com.example.jorda.magnetart.utilities.MagnetCalculations;
import com.example.jorda.magnetart.utilities.MagnetPosition;
import com.example.jorda.magnetart.view.NewDrawingView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class NewDrawingFragment extends Fragment implements SensorEventListener, ClearDrawingListener,
        BackgroundColourListener, StrokeColourListener{

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private NewDrawingView dv;//Create a drawingview which will contain the canvas the user draws on

    private TextView earthx, earthy, earthz, sensorx, sensory, sensorz;
    private TextView tlreading, trreading, blreading, brreading;
    private TextView screenx, screeny, displayalgo;
    private Button btn_tl, btn_tr, btn_bl, btn_br;
    private double[] previousValues = {0,0,0};//used in low pass filter
    private double dwidth, dheight;
    private boolean calibrationcomplete = false;
    private boolean choosingBgColor = false, choosingStrokeColour = false, drawingactive = true;
    private boolean saved = false;
    private boolean highaccuracy = false;
    private int calibrationcounter = 0;
    private static int algorithm = 1;//chosen algorithm
    private BackgroundColourFragment bcf = new BackgroundColourFragment();
    private StrokeColourFragment scf = new StrokeColourFragment();
    private ClearDrawingFragment cdf = new ClearDrawingFragment();
    private static final String TAG = "MyActivity";
    private static final String dialog = "dialog";
    private MagnetPosition topleftp, toprightp, botleftp, botrightp, currentp, screenp, earthp;
    private ProgressDialog pd;
    private RelativeLayout mLayout;
    private Menu menu;
    private String mseconds = "magnetart";
    private MagnetCalculations cr = new MagnetCalculations();
    private Toast t1, t2;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_newdraw, container, false);
        setHasOptionsMenu(true);
        initializeVariables(rootView);
        mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);

        } else {
            Toast.makeText(getActivity(), R.string.no_sensor, Toast.LENGTH_SHORT).show();
            btn_tl.setEnabled(false);
            btn_tr.setEnabled(false);
            btn_bl.setEnabled(false);
            btn_br.setEnabled(false);
            pd.dismiss();
        }

        //when each button is pressed, the magnetic field reading is recorded for that corner of the drawing area and the button is then disabled
        btn_tl.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        btn_tl.setEnabled(false);
                        btn_tl.setBackgroundColor(0xff00ff00);
                        topleftp = new MagnetPosition(currentp.toDoubleArray());
                        tlreading.setText(String.format("TL X: %.3f  Y: %.3f Z: %.3f", topleftp.xPosition, topleftp.yPosition, topleftp.zPosition));
                        Log.d(TAG, "Top Left Calibrated- TL Readings are above");
                        calibrationcounter++;
                        checkCornersCalibrated();
                    }
                }
        );
        //top right button
        btn_tr.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        btn_tr.setEnabled(false);
                        btn_tr.setBackgroundColor(0xff00ff00);
                        toprightp = new MagnetPosition(currentp.toDoubleArray());
                        trreading.setText(String.format("TR X: %.3f  Y: %.3f Z: %.3f", toprightp.xPosition, toprightp.yPosition, toprightp.zPosition));
                        Log.d(TAG, "Top Right Calibrated- TR Readings are above");
                        calibrationcounter++;
                        checkCornersCalibrated();
                    }
                }
        );
        //bottom left button
        btn_bl.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        btn_bl.setEnabled(false);
                        btn_bl.setBackgroundColor(0xff00ff00);
                        botleftp = new MagnetPosition(currentp.toDoubleArray());
                        blreading.setText(String.format("BL X: %.3f  Y: %.3f Z: %.3f", botleftp.xPosition, botleftp.yPosition, botleftp.zPosition));
                        Log.d(TAG, "Bottom Left Calibrated- BL Readings are above");
                        calibrationcounter++;
                        checkCornersCalibrated();
                    }
                }
        );
        //bottom right button
        btn_br.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        btn_br.setEnabled(false);
                        btn_br.setBackgroundColor(0xff00ff00);
                        botrightp = new MagnetPosition(currentp.toDoubleArray());
                        brreading.setText(String.format("BR X: %.3f  Y: %.3f Z: %.3f", botrightp.xPosition, botrightp.yPosition, botrightp.zPosition));
                        Log.d(TAG, "Bottom Right Calibrated- BR Readings are above");
                        calibrationcounter++;
                        checkCornersCalibrated();
                    }
                }
        );
    return rootView;

    }

    /**
     * When 4 corners have been calibrated, the readings are hidden and a Toast is shown
     */
    public void checkCornersCalibrated(){
        if(calibrationcounter==4){
            toggleReadings();
            Toast.makeText(getActivity(), "Calibration Complete", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initialise all the variables for the fragment
     * @param rootView
     */
    private void initializeVariables(View rootView) {

        mLayout = (RelativeLayout)rootView.findViewById(R.id.buttonsandreadings);
        mLayout.setVisibility(View.VISIBLE);
        dv = (NewDrawingView)rootView.findViewById(R.id.drawingview);
        //Default shape is spot drawn at magnet position
        dv.activeShape = NewDrawingView.DrawingShape.SPOT;
        //textviews to display the terrestrial magnetic field readings
        earthx = (TextView)rootView.findViewById(R.id.earthx);
        earthy = (TextView)rootView.findViewById(R.id.earthy);
        earthz = (TextView)rootView.findViewById(R.id.earthz);
        //textviews to display the raw sensor readings
        sensorx = (TextView)rootView.findViewById(R.id.sensorx);
        sensory = (TextView)rootView.findViewById(R.id.sensory);
        sensorz = (TextView)rootView.findViewById(R.id.sensorz);
        //textviews to display the readings in each corner used in calibration
        tlreading = (TextView)rootView.findViewById(R.id.tlreadings);
        trreading = (TextView)rootView.findViewById(R.id.trreadings);
        blreading = (TextView)rootView.findViewById(R.id.blreadings);
        brreading = (TextView)rootView.findViewById(R.id.brreadings);
        //textviews to display the current screen co-ordinates
        screenx = (TextView)rootView.findViewById(R.id.screenx);
        screeny = (TextView)rootView.findViewById(R.id.screeny);
        //display algorithm chosen in settings
        displayalgo = (TextView)rootView.findViewById(R.id.displayAlgorithm);
        //buttons in each corner of the drawing area
        btn_tl = (Button)rootView.findViewById(R.id.tlbutton);
        btn_tr = (Button)rootView.findViewById(R.id.trbutton);
        btn_bl = (Button)rootView.findViewById(R.id.blbutton);
        btn_br = (Button)rootView.findViewById(R.id.brbutton);

        bcf.setTargetFragment(this,0);//set target fragment for the dialog fragments
        cdf.setTargetFragment(this,0);
        scf.setTargetFragment(this,0);

        //retrieve the algorithm chosen in the activity_preferences activity
        algorithm = UserPreferencesActivity.getAlgorithmSelected(getActivity());
        displayalgo.setText("Chosen algo: "+algorithm);
        //progress dialog shown when the buffer is filling with magnetic field readings
        pd = new ProgressDialog(getActivity());
        pd.setTitle("Initialising Magnetometer");
        pd.setMessage("Please do not place magnet on drawing surface");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setCancelable(false);
        pd.show();

        //Initialise toasts for screen dimensions here so they can be referenced outside onSensorChanged
        t1 = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        t2 = Toast.makeText(getActivity(),"", Toast.LENGTH_SHORT);
    }

    /**
     * Create the options menu
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the main menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.new_drawing, menu);
        this.menu=menu;
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.sensor_readings://Toggle if sensor readings are visible or not
                toggleReadings();
            break;

            case R.id.clear://clear the drawing area
                try {
                    cdf.show(getFragmentManager(), dialog);
                }catch (NullPointerException e){
                    Toast.makeText(getActivity(), R.string.dialog_null_pointer, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_spot://draw a spot on screen at magnet position- this is the default setting
                dv.activeShape = NewDrawingView.DrawingShape.SPOT;
                dv.reset();
                break;

            case R.id.action_smoothline:// draw a smoothline between magnet positions
                dv.activeShape = NewDrawingView.DrawingShape.SMOOTHLINE;
                dv.reset();
                break;

            case R.id.action_rectangle:
                dv.activeShape = NewDrawingView.DrawingShape.RECTANGLE;;
                dv.reset();
                break;

            case R.id.action_circle:
                dv.activeShape = NewDrawingView.DrawingShape.CIRCLE;
                dv.reset();
                break;

            case R.id.action_triangle:
                dv.activeShape = NewDrawingView.DrawingShape.TRIANGLE;
                dv.reset();
                break;

            case R.id.recalibrate://recalibrate the drawing area by retaking magnetic field readings at the four corners
                reCalibrate();
                break;

            case R.id.strokecolour://change colour of the stroke/shape
                if(calibrationcounter!=4){//warn the user that app is not calibrated
                    //User may change colour before calibration so colour will be set before use
                    Toast.makeText(getActivity(), R.string.calib_not_complete, Toast.LENGTH_SHORT).show();
                }
                choosingStrokeColour = true;
                drawingactive = false;
                try {
                    scf.show(getFragmentManager(), dialog);
                }catch(NullPointerException e){
                    Toast.makeText(getActivity(), R.string.dialog_null_pointer, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.bgcolour://change the background colour of the canvas
                if(calibrationcounter!=4){
                    Toast.makeText(getActivity(), R.string.calib_not_complete, Toast.LENGTH_SHORT).show();
                }
                choosingBgColor = true;
                drawingactive = false;
                try {
                    bcf.show(getFragmentManager(), dialog);
                }catch(NullPointerException e){
                    Toast.makeText(getActivity(), R.string.dialog_null_pointer, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.savetodevice://save the drawing
                if(calibrationcounter!=4){//cannot save without calibrating first
                    Toast.makeText(getActivity(), R.string.calib_not_complete, Toast.LENGTH_SHORT).show();
                }else {
                    saveDrawingToDevice();
                }
                break;

            case R.id.settingsfromdrawing://change calibration technique- for demo purposes
                try {
                    Intent goToSettings = new Intent(getActivity(), UserPreferencesActivity.class);
                    startActivity(goToSettings);
                }catch(NullPointerException e){
                    Toast.makeText(getActivity(), "Could not open settings", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method implemented due to SensorEventListener
     * @param sensor
     * @param accuracy
     */
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Check that the magnetic field sensor is of the required accuracy
        if(sensor==mSensor){

            switch(accuracy){
                case 0:
                    Log.d(TAG, getString(R.string.sensor_0));
                    highaccuracy = false;
                    break;
                case 1:
                    Log.d(TAG, getString(R.string.sensor_1));
                    highaccuracy = false;
                    break;
                case 2:
                    Log.d(TAG, getString(R.string.sensor_2));
                    highaccuracy = false;
                    break;
                case 3:
                    Log.d(TAG, getString(R.string.sensor_3));
                    highaccuracy = true;
                    break;
            }
        }
    }

    /**
     * This method will be called when the magnet is moved
     * @param event
     */
    @Override
    public final void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED && highaccuracy) {
            //Progress dialog is shown while the buffer is being filled to prevent the user attempting to draw

            MagnetPosition correct = cr.correctReadings(event.values);
//            Create an array of new corrected values
            double[] currentValues = correct.toDoubleArray();
            MagnetCalculations.lowPass(currentValues, previousValues);//apply a low pass filter to values which are then used to create a new magnetposition object
            currentp = new MagnetPosition(previousValues);
            earthp = cr.getEarthp();

            Log.d(TAG, "X: " + currentp.xPosition + " Y: " + currentp.yPosition);//log these values to be able to plot them over time, plot against position in grid
            //use squared paper and plot magnet position against field readings


            //dismiss dialog
            if (cr.isBufferfull()) {
                pd.dismiss();
            }

            earthx.setText(String.format("Earth X: %.3f", earthp.xPosition));
            earthy.setText(String.format("Earth Y: %.3f", earthp.yPosition));
            earthz.setText(String.format("Earth Z: %.3f", earthp.zPosition));

            sensorx.setText(String.format("X: %.3f ", currentp.xPosition));
            sensory.setText(String.format("Y: %.3f ", currentp.yPosition));
            sensorz.setText(String.format("Z: %.3f ", currentp.zPosition));//these are changing correctly

            getScreenDimensions();//get the screen dimensions

            //The magnetic field readings are used when choosing a colour
            if (choosingStrokeColour || choosingBgColor) {

                //Get a float from the seekbar which represents the chosen colour
                float colourValue = 100.0f - ((float) currentp.magnitude()) / 255 * 100.0f;
                if (colourValue > 100.0f) {
                    colourValue = 100.0f;//set to value at end of bar
                } else if (colourValue < 1.0f) {
                    colourValue = 1.0f;//set to value at start of bar
                }

                if (choosingStrokeColour) {//call to method in associated dialogfragment
                    scf.setCurrentStrokeColour(colourValue);
                } else {
                    bcf.setCurrentBackgroundColour(colourValue);
                }
            }

            //all corners have been calibrated
            if (calibrationcounter == 4) {

//                Algorithms 1 and 2 are assuming a linear field
                if (algorithm == 1 || algorithm == 2) {
                    screenp = CalibrationCalculations.Calibrate(algorithm, topleftp, toprightp, botleftp, botrightp, currentp, dwidth, dheight);
                }
                //non-linear field, calculating geometry using technique by Tetsuya Abe
                if (algorithm == 3) {
                    screenp = cr.geometryCalc(currentp);//calculate geometry of environment
                }
                calibrationcomplete = true;
//                Log.d(TAG,"X: "+currentp.xPosition+" Y: "+currentp.yPosition+ " ScreenX: " + screenp.xPosition + " ScreenY: " + screenp.yPosition);
                screenx.setText(Double.toString(screenp.xPosition));
                screeny.setText(Double.toString(screenp.yPosition));


            }

            if (calibrationcomplete && drawingactive) {//draw on screen by sending co-ordinates to the drawing class
                //check that estimated coordinates are within the screen dimensions
                //boolean drawingactive prevents drawing when changing colour
                dv.screenX = (float) screenp.xPosition;
                dv.screenY = (float) screenp.yPosition;

                //Toasts are shown for two seconds while sensor updates every 0.02 seconds - therefore the toast will remain shown
                //Even when magnet is returned- need to cancel it
                if (dv.screenX > dwidth||dv.screenX<0) {
                    t1.setText("Outside screen width");
                    t1.show();
                } else {
                    t1.cancel();
                }

                if (dv.screenY > dheight||dv.screenX<0) {
                    t2.setText("Outside screen height");
                    t2.show();
                } else {
                    t2.cancel();
                }
                dv.invalidate();
            }
        }
    }


    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);//change to game
        if (getView() == null) {
            return;
        }
        //When the phone back button is pressed, ask user if they wish to save changes
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //If the back button is pressed
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    //display dialog to ask if they wish to save before exiting
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setMessage("Do you wish to save before quitting?")
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    saveDrawingToDevice();//save drawing
                                    returnToMainFrag();//return to saved drawings
                                }
                            })
                            .setNegativeButton("Don't Save", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    returnToMainFrag();//return without saving
                                }
                            }).setNeutralButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //dismiss dialog, do not return t
                            dialog.dismiss();
                        }
                    }).show();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Method to return to main fragment- handles fragment transaction
     */
    public void returnToMainFrag() {
        try {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.container, new MainFragment());
            ft.addToBackStack(null);
            ft.commit();
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), "Could not return home", Toast.LENGTH_SHORT).show();
        }
        HideToasts();
    }

    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        HideToasts();
    }

    public void HideToasts(){
        //ensure Toasts are cancelled
        if(t1!=null){
            t1.cancel();
        }
        if(t2!=null){
            t2.cancel();
        }
    }


    /**
     * Method to get the screen dimensions using the size of the drawing view
     */
    public void getScreenDimensions(){
        dwidth = dv.getWidth();
        dheight = dv.getHeight();
        //1920x1080p on oneplus X
    }

    /**
     * Method to make magnetic field readings and calibration buttons visible or invisible
     */
    public void toggleReadings() {
        //Display sensor readings and buttons
        if (mLayout.getVisibility() == View.INVISIBLE) {
            mLayout.setVisibility(View.VISIBLE);
            menu.findItem(R.id.sensor_readings).setTitle("Hide sensor readings");

        } else if (mLayout.getVisibility() == View.VISIBLE) {
            mLayout.setVisibility(View.INVISIBLE);
            menu.findItem(R.id.sensor_readings).setTitle("Show sensor readings");
        }
        if (calibrationcounter < 4) {
            Toast.makeText(getActivity(), "Calibration Not Complete", Toast.LENGTH_SHORT).show();
            //buttons will be hidden so make user aware that calibration is not complete and app will not draw
        }
    }

    /**
     *Reset calibration values used- enable buttons and use onclicklistener again
     */
    public void reCalibrate(){

        calibrationcounter = 0;
        //Remove readings from textviews
        tlreading.setText("TL:");
        trreading.setText("TR:");
        blreading.setText("BL:");
        brreading.setText("BR:");

        //change all corner buttons to grey and enable
        for(int i=0;i<mLayout.getChildCount();i++){
            View v = mLayout.getChildAt(i);
            if(v instanceof Button){//get all view elements which are buttons
                v.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                v.setEnabled(true);
            }
        }

        //Set view to invisible first so that toggleReadings will change to VISIBLE and update the menu item
        mLayout.setVisibility(View.INVISIBLE);
        toggleReadings();
        //removes readings from the buffers to allow the terrestrial field to be recalculated
        cr.clearBuffers();
        pd.show();

    }

    /**
     * Method to save the drawing
     */
    public void saveDrawingToDevice(){

        boolean filecreated = false;

        //Check if the image has been saved before, if so then use the same filepath to update the saved image
        if(!saved) {
            java.util.Calendar c = Calendar.getInstance();
            //File names are produced using milliseconds from calendar to ensure they are unique
            mseconds = "magnetart " + c.get(Calendar.MILLISECOND) + ".png";
            saved = true;
        }

        File file = new File(getActivity().getFileStreamPath(mseconds)//use date and time to create unique stream
                .getPath());
        if (!file.exists()) {
            try {
                filecreated = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.ioexception, Toast.LENGTH_SHORT).show();
            }
        }

        try {

            FileOutputStream fos = new FileOutputStream(file);

            System.out.println(fos);

            Bitmap drawingViewBitmap = dv.getBitmap();
            Bitmap bitmapToBeSaved = Bitmap.createBitmap(drawingViewBitmap.getWidth(), drawingViewBitmap.getHeight(), Bitmap.Config.ARGB_8888);

            Paint paint = new Paint();
            paint.setColor(Color.WHITE);//Set background colour as white
            Canvas currentCanvas = new Canvas(bitmapToBeSaved);
            Rect drawingRect = new Rect(0,0,drawingViewBitmap.getWidth(),drawingViewBitmap.getHeight());

            //draw white rectangle in the background
            currentCanvas.drawRect(drawingRect, paint);

            //draw the actual bitmap on the canvas
            currentCanvas.drawBitmap(drawingViewBitmap, drawingRect, drawingRect, null);

            //save bitmap to the file as a PNG
            bitmapToBeSaved.compress(Bitmap.CompressFormat.PNG, 100, fos);

        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.nullpointer, Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.filenotfound, Toast.LENGTH_SHORT).show();
        }
        //Display a toast to confirm file is saved
        Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();

    }


//     Callback from ClearDrawingFragment- this clears the canvas when 'confirm' button is pressed
    @Override
    public void onClearPositiveClick(DialogFragment dialog){
        dv.clearDrawing();
        Toast.makeText(getActivity(), "Drawing Cleared", Toast.LENGTH_SHORT).show();
    }

//    Callback from ClearDrawingFragment- this is called when the user selects cancel on the clear dialog, dialog is dismissed
    @Override
    public void onClearNegativeClick(DialogFragment dialog){
        dialog.dismiss();
    }

//     Callback from StrokeColourFragment- used to change colour when 'confirm' is pressed
    @Override
    public void onStrokePositiveClick(DialogFragment dialog) {
        //The movement of the magnetic stylus is used when changing colour, therefore drawing must be disabled to ensure
        // that lines are not drawn on the canvas when the colour is being chosen.  When a colour is chosen the dialog is dismissed
        //and drawing is enabled
        drawingactive = true;
        choosingStrokeColour = false;
        dv.initialPaint.setColor(((StrokeColourFragment)dialog).getCurrentStrokeColour());
        dv.endingPaint.setColor(((StrokeColourFragment)dialog).getCurrentStrokeColour());
    }

//     Callback from StrokeColourFragment- used to change colour when 'cancel' is pressed and user does not wish to change strokecolour
    @Override
    public void onStrokeNegativeClick(DialogFragment dialog) {
        drawingactive = true;
        choosingStrokeColour = false;
    }


//      Callback from BackgroundColourFragment- used to change background colour when 'confirm' is selected
    @Override
    public void onBackgroundPositiveClick(DialogFragment dialog) {
        drawingactive = true;
        choosingBgColor = false;
        dv.setBackgroundColor(((BackgroundColourFragment)dialog).getCurrentBackgroundColour());
    }

//      Callback from BackgroundColourFragment when 'cancel' is selected
    @Override
    public void onBackgroundNegativeClick(DialogFragment dialog) {
        drawingactive = true;
        choosingBgColor = false;
    }
    }






