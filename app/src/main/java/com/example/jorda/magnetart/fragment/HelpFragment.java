package com.example.jorda.magnetart.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.jorda.magnetart.R;

/**
 * Created by jordan on 04/08/2017.
 */

/**
 * Fragment to define help screen
 */
public class HelpFragment extends Fragment {

    private TextView par1;
    private Button beginbutton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState){

        View rootView = inflater.inflate(R.layout.fragment_help, container, false);
        Initialise(rootView);
        setHasOptionsMenu(true);

        return rootView;
    }

    public void Initialise(View rootView){
        par1 = (TextView)rootView.findViewById(R.id.helpfirstpp);
        beginbutton = (Button)rootView.findViewById(R.id.helpreturnbutton);
        //Go to NewDrawingFragment
        beginbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, new NewDrawingFragment());
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        getActivity().setTitle(getString(R.string.help_title));

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the main menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.helpscreen_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.return_home://Return to main fragment- view drawings
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, new MainFragment());
                ft.addToBackStack(null);
                ft.commit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
