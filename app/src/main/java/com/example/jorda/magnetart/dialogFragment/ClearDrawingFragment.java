package com.example.jorda.magnetart.dialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.jorda.magnetart.R;
import com.example.jorda.magnetart.interfaces.ClearDrawingListener;

/**
 * Created by jordan on 04/08/2017.
 */

/**
 * Class to define a dialog called when user wishes to clear the canvas
 */
public class ClearDrawingFragment extends DialogFragment {

    private ClearDrawingListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            mListener = (ClearDrawingListener)getTargetFragment();
        }catch (ClassCastException e){
            throw new ClassCastException("Calling fragment does not implement interface");
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState){

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.clear_message)
                // Confirm selected
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onClearPositiveClick(ClearDrawingFragment.this);
                    }
                })
                // Cancel selected
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onClearNegativeClick(ClearDrawingFragment.this);
                    }
                });

        return builder.create();
    }
}
