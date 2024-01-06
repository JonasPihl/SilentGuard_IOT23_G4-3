package com.example.face_detection_application.ui.settings;

import static java.lang.Math.pow;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.face_detection_application.R;
import com.example.face_detection_application.databinding.FragmentSettingsBinding;
import com.example.face_detection_application.ui.retrofitInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private Button saveColorButton;
    private boolean systemEnabled;
    private String serverAddress;
    private ImageView colorWheel;
    private PopupWindow popupWindow;
    private Bitmap colorBitMap;
    private Color completeColor;
    private Button timeStartButton;
    private Button timeEndButton;
    private int startHour, startMin;
    private int endHour, endMin;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        serverAddress = fetchServerIP(requireContext());

        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        LayoutInflater popupInflater = (LayoutInflater) requireActivity().getLayoutInflater();;
        View popupView = popupInflater.inflate(R.layout.color_popup, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.popup_background));
        popupWindow.setElevation(16);
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        getSystemState();



//        colorWheel = binding.colorWheel;
//        colorWheel.setVisibility(View.INVISIBLE);

        timeStartButton = binding.timeStartButton;
        timeEndButton = binding.timeEndButton;

        timeStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createTimePickerDialog(true);
                System.out.println(startHour + startMin);

            }
        });
        timeEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createTimePickerDialog(false);
            }
        });

        binding.disableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                systemEnabled = !systemEnabled;
                Retrofit retrofit = new Retrofit.Builder().baseUrl(serverAddress).build();
                retrofitInterface apiService = retrofit.create(retrofitInterface.class);
                Call<Void> onOff = apiService.on_off(systemEnabled);

                if (systemEnabled) {
                    onOff.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            // Handle success

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

                            System.out.println("Disabling system");
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {}
                    });
                }
            }
        });

        binding.colorButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility") //Suppress accessibility warning
            @Override
            public void onClick(View v) {

                popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);
                colorWheel = popupView.findViewById(R.id.color_wheel);
                saveColorButton = popupView.findViewById(R.id.saveColor);
                saveColorButton.setEnabled(false);

            saveColorButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){

                    List<Double> XYValues = getRGBtoHueXY(completeColor);
                    System.out.println("After getRGBtoHueXY - This is x: "+ XYValues.get(0) + " This is y: "+ XYValues.get(1));

                    Retrofit retrofit = new Retrofit.Builder().baseUrl(serverAddress).addConverterFactory(ScalarsConverterFactory.create()).build();

                    retrofitInterface apiService = retrofit.create(retrofitInterface.class);
                    Call<Double> updateColor = apiService.updateColor(XYValues.get(0), XYValues.get(1));

                    updateColor.enqueue(new Callback<Double>() {
                        @Override
                        public void onResponse(Call<Double> call, Response<Double> response) {}

                        @Override
                        public void onFailure(Call<Double> call, Throwable t) {}
                    });

                    if (completeColor != null){
                        getRGBtoHueXY(completeColor);
                    }
                    popupWindow.dismiss();
                }
            });

//
//                boolean isVisible = colorWheel.getVisibility() == View.VISIBLE;
//
//                if (isVisible){
//                    colorWheel.setVisibility(View.INVISIBLE);
//
//                    List<Double> XYValues = getRGBtoHueXY(completeColor);
//                    System.out.println("After getRGBtoHueXY - This is x: "+ XYValues.get(0) + " This is y: "+ XYValues.get(1));
//
//                    Retrofit retrofit = new Retrofit.Builder().baseUrl(serverAddress).addConverterFactory(ScalarsConverterFactory.create()).build();
//
//                    retrofitInterface apiService = retrofit.create(retrofitInterface.class);
//                    Call<Double> updateColor = apiService.updateColor(XYValues.get(0), XYValues.get(1));
//
//                    updateColor.enqueue(new Callback<Double>() {
//                        @Override
//                        public void onResponse(Call<Double> call, Response<Double> response) {}
//
//                        @Override
//                        public void onFailure(Call<Double> call, Throwable t) {}
//                    });
//
//                    if (completeColor != null){
//                        getRGBtoHueXY(completeColor);
//                    }
//
//                } else {
//                    colorWheel.setVisibility(View.VISIBLE);
//                }
//
                colorWheel.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int x = (int)event.getX();
                        int y = (int)event.getY();
                        saveColorButton.setEnabled(true);
                        //todo Limit rate of retrieved hexValues from bitmap
                        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                            colorBitMap = getBitMapFromView(colorWheel);
                        }

                        completeColor = colorBitMap.getColor(x, y);

                        return true;
                    }
                });
//
            }
        });

        final TextView textView = binding.textNotifications;
        settingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    private String fetchServerIP(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData.getString("server_ip");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null; // Handle the error or return a default value
        }
    }

    public static List<Double> getRGBtoHueXY(Color completeColor) {
        // https://developers.meethue.com/develop/application-design-guidance/color-conversion-formulas-rgb-to-xy-and-back/
        double red, green, blue;

        //Breakout rgb values from color
        red = completeColor.red();
        green = completeColor.green();
        blue = completeColor.blue();

        //Gamma correction for colors
        red = (red > 0.04045f) ? pow((red + 0.055f) / (1.0f + 0.055f), 2.4f) : (red / 12.92f);
        green = (green > 0.04045f) ? pow((green + 0.055f) / (1.0f + 0.055f), 2.4f) : (green / 12.92f);
        blue = (blue > 0.04045f) ? pow((blue + 0.055f) / (1.0f + 0.055f), 2.4f) : (blue / 12.92f);

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

    private void getSystemState(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl(serverAddress)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        retrofitInterface apiService = retrofit.create(retrofitInterface.class);
        Call<Boolean> state_of_server = apiService.state_of_server();
        state_of_server.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    // Set status of the btn according to server response
                    System.out.println(response.body());
                    systemEnabled = response.body();
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

    private void createTimePickerDialog(Boolean isStartTimeButton){
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (isStartTimeButton){
                    startHour = hourOfDay;
                    startMin = minute;
                    timeStartButton.setText(String.format("%02d:%02d", startHour, startMin));

                    Retrofit retrofit = new Retrofit.Builder().baseUrl(serverAddress).addConverterFactory(ScalarsConverterFactory.create()).build();
                    retrofitInterface apiService = retrofit.create(retrofitInterface.class);
                    Call<Integer> updateStartTime = apiService.updateStartTime(startHour, startMin);

                    updateStartTime.enqueue(new Callback<Integer>() {
                        @Override
                        public void onResponse(Call<Integer> call, Response<Integer> response) {

                        }

                        @Override
                        public void onFailure(Call<Integer> call, Throwable t) {

                        }
                    });

                } else {
                    endHour = hourOfDay;
                    endMin = minute;
                    timeEndButton.setText(String.format("%02d:%02d", endHour, endMin));

                    Retrofit retrofit = new Retrofit.Builder().baseUrl(serverAddress).addConverterFactory(ScalarsConverterFactory.create()).build();
                    retrofitInterface apiService = retrofit.create(retrofitInterface.class);
                    Call<Integer> updateEndTime = apiService.updateEndTime(endHour, endMin);

                    updateEndTime.enqueue(new Callback<Integer>() {
                        @Override
                        public void onResponse(Call<Integer> call, Response<Integer> response) {

                        }

                        @Override
                        public void onFailure(Call<Integer> call, Throwable t) {

                        }
                    });
                }
            }
        };

        int dialogTheme = AlertDialog.THEME_HOLO_LIGHT;
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), dialogTheme, onTimeSetListener, startHour, startMin, true);
        timePickerDialog.updateTime(0, 0);
        timePickerDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume(){
        super.onResume();
        getSystemState();
    }
}