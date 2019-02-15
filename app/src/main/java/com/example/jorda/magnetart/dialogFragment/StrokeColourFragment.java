package com.example.jorda.magnetart.dialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.example.jorda.magnetart.R;
import com.example.jorda.magnetart.interfaces.StrokeColourListener;
import com.example.jorda.magnetart.view.ColourSeekBar;

import static com.example.jorda.magnetart.view.ColourSeekBar.convertValueToColor;

public class StrokeColourFragment extends DialogFragment {

    // Listener Instance
    private StrokeColourListener mListener;

    // View Instance
    public ColourSeekBar strokecolourseekbar;

    // Current Color Value
    private float currentStrokeColour;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure calling fragment uses the listener interface
        try{
            mListener = (StrokeColourListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement the StrokeColourListener Interface");
        }

        currentStrokeColour = 0.0f;

    }

    public Dialog onCreateDialog(Bundle savedInstanceState){

        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_changestrokecolour,null,false);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_stroke)
                // Confirm selected
                .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onStrokePositiveClick(StrokeColourFragment.this);
                    }
                })
                // Cancel selected
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onStrokeNegativeClick(StrokeColourFragment.this);
                    }
                })
                .setView(rootView);
        strokecolourseekbar = (ColourSeekBar) rootView.findViewById(R.id.strokecolourbar);
        return builder.create();
    }

    // Return color int
    public int getCurrentStrokeColour() {
        return convertValueToColor(currentStrokeColour);
    }

    // SETTER (also sets value in colourseekbar)
    public void setCurrentStrokeColour(float value) {
        currentStrokeColour = value;
        if(strokecolourseekbar != null) {
            strokecolourseekbar.setProgress((int) value);
            strokecolourseekbar.invalidate();
        }
    }
}
