package com.example.jorda.magnetart.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jorda.magnetart.R;
import com.example.jorda.magnetart.activity.UserPreferencesActivity;

import java.io.File;

/**
 * Created by jordan on 03/08/2017.
 */

public class MainFragment extends Fragment {

    public LinearLayout gallery;
    public FrameLayout viewdrawingframe;
    public HorizontalScrollView scrollview;
    public TextView nodrawingstext;
    public FloatingActionButton fab, delete;
    public String filepath;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);
        Initialise(rootView);//initialise variables
        updateGallery();//updatet the gallery with saved images

        return rootView;
    }

    private void Initialise(View rootView) {

        gallery = (LinearLayout) rootView.findViewById(R.id.gallery);
        viewdrawingframe = (FrameLayout) rootView.findViewById(R.id.viewsaveddoc);
        scrollview = (HorizontalScrollView) rootView.findViewById(R.id.saveddocumentsscrollview);
        nodrawingstext = (TextView) rootView.findViewById(R.id.nodrawingstextview);
        //floating action button to add new drawings
        fab = (FloatingActionButton) rootView.findViewById(R.id.adddrawingfab);
        //floating action button to delete selected drawing
        delete = (FloatingActionButton)rootView.findViewById(R.id.deletedrawingfab);
        delete.setVisibility(View.INVISIBLE);
        getActivity().setTitle(getString(R.string.app_title));

        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new NewDrawingFragment());
                    ft.addToBackStack(null);
                    ft.commit();
                }catch(NullPointerException e){
                    Toast.makeText(getActivity(), R.string.nds_not_loaded, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the main menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.help_screen:
                try {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new HelpFragment());
                    ft.addToBackStack(null);
                    ft.commit();
                }catch(NullPointerException e){
                    Toast.makeText(getActivity(), R.string.help_not_loaded, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            case R.id.settings:
                try {
                    Intent goToSettings = new Intent(getActivity(), UserPreferencesActivity.class);
                    startActivity(goToSettings);
                }catch (NullPointerException e){
                    Toast.makeText(getActivity(), R.string.settings_not_loaded, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

    /**
     * Method to retrieve saved files from memory
     */
    private void updateGallery() {

        gallery.removeAllViews();
        //Returns absolute path to directory where files have been created using FileOutputStream
        File targetDirector = getActivity().getFilesDir();
        //An array of pathnames denoting files in the directory
        File[] files = targetDirector.listFiles();

        //If there are files found, show the scrollview and frame to display the drawings
        if (files.length > 0) {
            scrollview.setVisibility(View.VISIBLE);
            viewdrawingframe.setVisibility(View.VISIBLE);
            //Hide text that says there are no drawings
            nodrawingstext.setVisibility(View.GONE);

            for (File file : files) {
                filepath = file.getAbsolutePath();
                gallery.addView(createLayoutForPhoto(filepath));//Iterate through array and add files to gallery
            }

        } else {
            //If no drawings are found, hide the scrollview and gallery and display a message to the user
            scrollview.setVisibility(View.GONE);
            viewdrawingframe.setVisibility(View.GONE);
            nodrawingstext.setVisibility(View.VISIBLE);
        }
    }

    private View createLayoutForPhoto(String path) {//called when updating gallery if images are found

        Bitmap bm = retrieveScaledBitmap(path, 200, 200);//get Bitmap from filepath

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LinearLayout.LayoutParams(250, 250));
        layout.setGravity(Gravity.CENTER);

        ImageView imageView = new ImageView(getActivity());//place the bitmap in an imageview
        imageView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bm);
        imageView.setOnClickListener(new DrawingSelectedOnClickListener(path));

        layout.addView(imageView);
        return layout;
    }

    private Bitmap retrieveScaledBitmap(String path, int rwidth, int rheight) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;//First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.decodeFile(path, options);

        //calculate inSampleSize
        options.inSampleSize = calculateScaledSize(options, rwidth, rheight);

        //Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }

    /**
     * Method to calculate size of bitmap after scaling
     * @param options
     * @param rwidth
     * @param rheight
     * @return
     */
    private int calculateScaledSize(BitmapFactory.Options options, int rwidth, int rheight) {
        //raw width and height of bitmaps
        final int height = options.outHeight;
        final int width = options.outWidth;
        int scaledSize = 1;

        if (width > rwidth || height > rheight) {

            //Ratios of actual height and width to required height and width
            final int heightRatio = Math.round((float) height / (float) rheight);
            final int widthRatio = Math.round((float) width / (float) rwidth);
        //Choose the smaller ratio as scaled size
            scaledSize = width>height?heightRatio:widthRatio;
        }
        return scaledSize;
    }


    /**
     * Inner class to define actions when an image is selected
     */
    private class DrawingSelectedOnClickListener implements View.OnClickListener {

        public String imagePath;

        public DrawingSelectedOnClickListener(String path) {
            imagePath = path;
        }

        public void onClick(View v) {

            viewdrawingframe.removeAllViews();

            try {
                //get actual bitmap size
                final BitmapFactory.Options options = new BitmapFactory.Options();
                //avoids memory allocation-avoids memory allocations while decoding file
                //Allows us to read the dimensions and type of image data before constructing bitmap
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options);

                //send the bitmap size to decoding sampled bitmap from Uri
                Bitmap bm = retrieveScaledBitmap(imagePath, options.outWidth, options.outHeight);

            //save the bitmap in an image view
            final ImageView imageView = new ImageView(getActivity());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(options.outWidth, options.outHeight));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bm);

            viewdrawingframe.addView(imageView);
            viewdrawingframe.invalidate();
            //make delete button visible when drawing is selected
            delete.setVisibility(View.VISIBLE);
            //Use delete floating action button to delete the selected drawing
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteImage();
                }
            });
            }catch(Exception e){
            Toast.makeText(getActivity(), R.string.image_not_disp, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
        }
        }

        /**
         * Method used to delete the image from device storage
         */
        public void deleteImage() {
            //Dialog to check if user wishes to delete image
            //Dialog built here because of nature of callbacks in inner class- did not use interface
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setMessage(R.string.dialog_confirmdelete)
                    .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                File file = new File(imagePath);
                                file.delete();
                                Toast.makeText(getActivity(), R.string.file_deleted, Toast.LENGTH_SHORT).show();
                                viewdrawingframe.removeAllViews();
                                updateGallery();
                                delete.setVisibility(View.INVISIBLE);
                            }catch (Exception e){
                                Toast.makeText(getActivity(), R.string.delete_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
        }
    }


