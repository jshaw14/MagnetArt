package com.example.jorda.magnetart.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.jorda.magnetart.fragment.MainFragment;
import com.example.jorda.magnetart.R;


/**
 * This main activity controls the fragments that make up the home screen- There will be a toolbar at the top, a scrollview
 * of the saved documents and a floating action button to create a new document
 * When the application is opened, the main_fragment will be displayed
 */
public class MainActivity extends AppCompatActivity {

    //Starting point for the activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Layout associated with this activity is defined in activity_main.xml
        //This contains a container view for fragments
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            try {
                getFragmentManager().beginTransaction().add(R.id.container, new MainFragment()).commit();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.Fragment_not_Displayed, Toast.LENGTH_SHORT).show();
            }
        }
    }

}