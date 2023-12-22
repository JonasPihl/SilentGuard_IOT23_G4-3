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
import com.example.face_detection_application.ui.log.retrofitInterface;


import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private boolean systemEnabled;
    private static final String serverAdress = "http://192.168.1.174:5000";  // TODO Replace with Pi's IP


    ImageView colorWheel;
    Bitmap colorBitMap;
    String colorHexValue;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getSystemState();


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
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Handle failure
                        }
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
                        public void onFailure(Call<Void> call, Throwable t) {
                            // Handle failure
                        }
                    });
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