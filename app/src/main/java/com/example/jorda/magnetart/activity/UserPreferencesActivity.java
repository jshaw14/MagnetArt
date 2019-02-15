package com.example.jorda.magnetart.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.jorda.magnetart.R;

/**
 * Allows the user to select an algorithm with which to calibrate the application from a list.
 * The algorithm will be saved as an integer in SharedPreferences
 */
public class UserPreferencesActivity extends AppCompatActivity {

    private static final String KEY = "algoprefs";
    private static final String INT_KEY = "Chosen Algorithm";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();

        try {
            //Hide the arrow in actionbar, o ensure the user returns to previous fragment on back press
            actionBar.setDisplayHomeAsUpEnabled(false);
        }catch(NullPointerException e){
            Toast.makeText(this, R.string.error_actionbar, Toast.LENGTH_SHORT).show();
        }
        setContentView(R.layout.activity_preferences);
        createRadioButtons();
    }

    /**
     * Method which iterates through the radiogroup to create a radio button for each algorithm named in layout file
     */
    private void createRadioButtons() {

        RadioGroup rg = (RadioGroup) findViewById(R.id.radio_group_algo);
        //algorithm names taken from resource
        String[] algorithmOptions = getResources().getStringArray(R.array.algo_list);

        for (int i = 0; i < algorithmOptions.length; i++) {

            final String algoname = algorithmOptions[i];//Get algorithm name
            final int algo = i + 1;//Algorithm number- starts at 1
            //Radio buttons are produced by iterating through the algorithm names
            RadioButton rb = new RadioButton(this);
            rb.setText(algoname);
            rb.setTextAppearance(this, android.R.style.TextAppearance_DeviceDefault_Medium);

            //When the button is pressed, get the integer associated with that algorithm, and send to SharedPreferences using
            // save AlgorithmSelected method
            rb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(UserPreferencesActivity.this, getString(R.string.chosen_algo) +algo + " : " + algoname, Toast.LENGTH_SHORT).show();
                    saveAlgorithmSelected(algo);
                }
            });
            rg.addView(rb);

            //select default setting- ensures there is always a selected item
            if (algo == getAlgorithmSelected(this)) {
                rb.setChecked(true);
            }else{//If user has not selected one- ensure that first button is checked
                saveAlgorithmSelected(1);
                RadioButton  r = (RadioButton)rg.getChildAt(0);
                r.setChecked(true);

            }
        }


    }

    /**
     * Saves the algorithm integer in SharedPreferences
     * @param algo
     */
    private void saveAlgorithmSelected(int algo) {
        try {
            SharedPreferences sp = this.getSharedPreferences(KEY, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(INT_KEY, algo);
            editor.apply();
        } catch (ClassCastException e) {
            Toast.makeText(this, R.string.variable_wrong_type, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method to get the algorithm number from SharedPreferences- this method can be called from other fragments
     */
    public static int getAlgorithmSelected(Context context) {

        int getalgo = 0;
        SharedPreferences sp = context.getSharedPreferences(KEY, MODE_PRIVATE);
        try {
            getalgo = sp.getInt(INT_KEY, 0);//assign to value from sharedpreferences
        } catch (NullPointerException e) {
            Toast.makeText(context, R.string.algorithm_nullpointer, Toast.LENGTH_SHORT).show();
        }
        return getalgo;
    }

    /**
     * Method to ensure that pressing the device back button returns to previous fragment
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}