package com.example.face_detection_application.ui.settings;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.face_detection_application.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    boolean systemEnabled;

    ImageView colorWheel;
    Bitmap colorBitMap;
    String colorHexValue;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        systemEnabled = getSystemState();
        binding.disableButton.setChecked(systemEnabled);

        binding.disableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                systemEnabled = !systemEnabled;

                if (systemEnabled) {
                    binding.disableButton.setChecked(true);

                    //todo Start up the system
                    System.out.println("Enabling system");

                } else {
                    binding.disableButton.setChecked(false);

                    //todo Shutdown the system
                    System.out.println("Disabling system");
                }
            }
        });

        binding.colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorWheel = binding.colorWheel;

                boolean isVisible = colorWheel.getVisibility() == View.VISIBLE;

                if (isVisible){
                    colorWheel.setVisibility(View.INVISIBLE);
                    sendHexToHue(colorHexValue);
                } else {
                    colorWheel.setVisibility(View.VISIBLE);
                }

                colorWheel.setOnTouchListener(new View.OnTouchListener() {
                    String startingColor = "#fffe4b3f"; //todo get live colorValue from hue system
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int x = (int)event.getX();
                        int y = (int)event.getY();

                        //todo Limit rate of retrieved hexValues from bitmap
                        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                            colorBitMap = getBitMapFromView(colorWheel);
                        }

                        int colorPixels = colorBitMap.getPixel(x, y);

                        colorHexValue = "#"+ Integer.toHexString(colorPixels);

                        if (colorHexValue.equals("#0")){
                            colorHexValue = startingColor;
                        }

                        System.out.println(colorHexValue);

                        return true;
                    }
                });

            }
        });

        final TextView textView = binding.textNotifications;
        settingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void sendHexToHue(String colorHexValue){
        //todo send colorHexValue to hue
        System.out.println(colorHexValue + " sent to hue");
    }

    private boolean getSystemState(){
        //todo Get the live system state here to reflect real state on Disable button
        /*if (systemState == true){
            return true;
        } else {
            return false;
        }*/
        return true; //temporary
    }

    private Bitmap getBitMapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}