package com.example.jorda.magnetart.dialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.example.jorda.magnetart.interfaces.BackgroundColourListener;
import com.example.jorda.magnetart.R;
import com.example.jorda.magnetart.view.ColourSeekBar;
import static com.example.jorda.magnetart.view.ColourSeekBar.convertValueToColor;

/**
 * Class to define a dialog which allows the user to choose the background colour for the application
 */
public class BackgroundColourFragment extends DialogFragment {

    private BackgroundColourListener mListener;// Instance of interface which defines methods for handling dialog callbacks
    public ColourSeekBar backgroundcoloursb;// Instance of the ColourSeekBar custom view object
    private float currentBackgroundColour;// The selected colour

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Checks that the calling fragment implements the listener interface
        try{
            mListener = (BackgroundColourListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getString(R.string.bcf_classcastexception));
        }

        currentBackgroundColour = 0.0f;//set the initial value of the colour

    }

    /**
     * Class to define the dialog
     * @param savedInstanceState
     * @return
     */
    public Dialog onCreateDialog(Bundle savedInstanceState){

        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_changebgcolour,null,false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_selectbgc).setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {// Confirm selected
                    //when confirm button is pressed the associated method is called in the calling fragment
            @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onBackgroundPositiveClick(BackgroundColourFragment.this);
                    }
                })//when cancel button is pressed the associated method is called in the calling fragment
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {// Cancel selected
                        mListener.onBackgroundNegativeClick(BackgroundColourFragment.this);
                    }
                })

                .setView(rootView);

        backgroundcoloursb = (ColourSeekBar) rootView.findViewById(R.id.backgroundcolourbar);

        return builder.create();
    }

    /**
     * Getter for the selected background colour
     * The colour is saved as an integer
     * @return
     */
    public int getCurrentBackgroundColour() {
        return convertValueToColor(currentBackgroundColour);
    }

    // SETTER (also sets value in colourseekbar)
    public void setCurrentBackgroundColour(float value) {
        currentBackgroundColour = value;
        if(backgroundcoloursb != null) {
            backgroundcoloursb.setProgress((int) value);
            backgroundcoloursb.invalidate();
        }
    }
}
