package com.example.face_detection_application.ui.log;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.face_detection_application.databinding.FragmentLogBinding;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LogFragment extends Fragment {

    private FragmentLogBinding binding;
    private ListView logs;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        logs = binding.logs;
        ImageListAdapter adapter = new ImageListAdapter(requireActivity(), new ArrayList<>());
        logs.setAdapter(adapter);
        return root;
    }

    private void getImages() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.174:5000") //TODO Replace with Pi's IP
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface apiService = retrofit.create(retrofitInterface.class);
        Call<Map<String, List<String>>> imageListCall = apiService.getImageList();

        imageListCall.enqueue(new Callback<Map<String, List<String>>>() {
            @Override
            public void onResponse(Call<Map<String, List<String>>> call, Response<Map<String, List<String>>> response) {
                if (response.isSuccessful()) {
                    Map<String, List<String>> responseBody = response.body();
                    //TODO FIX unexpected end of stream error
                    //System.out.println("Response code: " + response.code());
                    //System.out.println("Response body: " + response.body());

                    if (responseBody != null && responseBody.containsKey("image_list")) {
                        List<Map.Entry<String, String>> imageEntries = new ArrayList<>();


                        for (String filename : responseBody.get("image_list")) {
                            // Construct the full image URL
                            String imageUrl = "http://192.168.1.174:5000/images/" + filename;  //TODO Change the IP to the raspberry pis IP
                            System.out.println(imageUrl);
                            // Create a Map.Entry for each image
                            Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>(filename, imageUrl);

                            // Add the entry to the list
                            imageEntries.add(entry);
                        }

                        // Update the custom adapter
                        ImageListAdapter adapter = new ImageListAdapter(requireContext(), imageEntries);
                        logs.setAdapter(adapter);
                    } else {
                        System.out.println("Response body is null or does not contain 'image_list'");
                    }
                    System.out.println("Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<String>>> call, Throwable t) {
                System.out.println("Fail: " + t.getMessage());
            }
        });
    }
    @Override
    public void onResume() {
        //Reloads the log each time the fragment is reloaded
        super.onResume();
        getImages();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}