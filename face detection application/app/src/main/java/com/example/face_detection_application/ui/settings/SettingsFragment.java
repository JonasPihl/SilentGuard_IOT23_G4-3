package com.example.face_detection_application.ui.settings;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
import com.example.face_detection_application.ui.log.retrofitInterface;


import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private boolean systemEnabled;
    private static final String serverAdress = "http://192.168.1.174:5000";  // TODO Replace with Pi's IP


    ImageView colorWheel;
    Bitmap colorBitMap;
    String colorHexValue;
    int red, green, blue;
    Color completeColor;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getSystemState();



        colorWheel = binding.colorWheel;
        colorWheel.setVisibility(View.INVISIBLE);

        binding.disableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                systemEnabled = !systemEnabled;
                binding.disableButton.setChecked(true);
                Retrofit retrofit = new Retrofit.Builder().baseUrl(serverAdress).build();
                retrofitInterface apiService = retrofit.create(retrofitInterface.class);
                Call<Void> onOff = apiService.on_off(systemEnabled);

                if (systemEnabled) {
                    onOff.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            // Handle success
                            // todo: Start up the system
                            System.out.println("Enabling system");
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {}
                    });

                } else {
                    onOff.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            // Handle success
                            binding.disableButton.setChecked(false);
                            // todo: Shutdown the system
                            System.out.println("Disabling system");
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {}
                    });
                }
            }
        });

        binding.colorButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility") //Supress accessability warning
            @Override
            public void onClick(View v) {
                colorWheel = binding.colorWheel;

                boolean isVisible = colorWheel.getVisibility() == View.VISIBLE;

                if (isVisible){
                    colorWheel.setVisibility(View.INVISIBLE);
                    sendHexToHue(colorHexValue);

                    if (completeColor != null){
                        getRGBtoHueXY(completeColor);
                    }

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

                        completeColor = colorBitMap.getColor(x, y);

                        ///
                        red = Color.red(colorPixels);
                        green = Color.green(colorPixels);
                        blue = Color.blue(colorPixels);
                        ///

                        colorHexValue = "#"+ Integer.toHexString(colorPixels);

                        if (colorHexValue.equals("#0")){
                            colorHexValue = startingColor;
                        }

                        System.out.println("r g b: " + red + " " + green + " " + blue);

                        System.out.println(colorPixels);

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
    ///
    public static List<Double> getRGBtoHueXY(Color completeColor) {
        // https://developers.meethue.com/develop/application-design-guidance/color-conversion-formulas-rgb-to-xy-and-back/
        // For the hue bulb the corners of the triangle are:
        // -Red: 0.675, 0.322
        // -Green: 0.4091, 0.518
        // -Blue: 0.167, 0.04
        double[] normalizedToOne = new double[3];
        float red, green, blue;

        red = completeColor.red();
        green = completeColor.green();
        blue = completeColor.blue();
        normalizedToOne[0] = (red / 255);
        normalizedToOne[1] = (green / 255);
        normalizedToOne[2] = (blue / 255);
        //float red, green, blue;

        // Gamma correction for colors
        if (normalizedToOne[0] > 0.04045) {
            red = (float) Math.pow(
                    (normalizedToOne[0] + 0.055) / (1.0 + 0.055), 2.4);
        } else {
            red = (float) (normalizedToOne[0] / 12.92);
        }

        if (normalizedToOne[1] > 0.04045) {
            green = (float) Math.pow((normalizedToOne[1] + 0.055)
                    / (1.0 + 0.055), 2.4);
        } else {
            green = (float) (normalizedToOne[1] / 12.92);
        }

        if (normalizedToOne[2] > 0.04045) {
            blue = (float) Math.pow((normalizedToOne[2] + 0.055)
                    / (1.0 + 0.055), 2.4);
        } else {
            blue = (float) (normalizedToOne[2] / 12.92);
        }

        //Convert RBG to XYZ with Wide RGB D65 formula
        float X = (float) (red * 0.649926 + green * 0.103455 + blue * 0.197109);
        float Y = (float) (red * 0.234327 + green * 0.743075 + blue * 0.022598);
        float Z = (float) (red * 0.0000000 + green * 0.053077 + blue * 1.035763);

        //Calculate XY values from XYZ
        float x = X / (X + Y + Z);
        float y = Y / (X + Y + Z);

        double[] xy = new double[2];
        xy[0] = x;
        xy[1] = y;
        List<Double> xyAsList = DoubleStream.of(xy).boxed().collect(Collectors.toList());

        System.out.println(x + " " + y);
        return xyAsList;
    }
    ///
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void sendHexToHue(String colorHexValue){
        //todo send colorHexValue to hue
        if (colorHexValue != null){
            System.out.println(colorHexValue + " sent to hue");
        } else {
            System.out.println("No color sent to hue.");
        }

    }

    private void getSystemState(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl(serverAdress)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitInterface apiService = retrofit.create(retrofitInterface.class);
        Call<Boolean> state_of_server = apiService.state_of_server();
        state_of_server.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    // Set status of the btn according to server repsonse
                    binding.disableButton.setChecked(response.body());
                }
            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                getSystemState();
            }
        });
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