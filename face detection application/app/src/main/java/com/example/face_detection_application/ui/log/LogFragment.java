package com.example.face_detection_application.ui.log;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.face_detection_application.databinding.FragmentLogBinding;
import com.example.face_detection_application.ui.retrofitInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class LogFragment extends Fragment {

    private FragmentLogBinding binding;
    private ListView logs;
    private String serverAddress;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        serverAddress = fetchServerIP(requireContext());

        binding = FragmentLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        logs = binding.logs;
        ImageListAdapter adapter = new ImageListAdapter(requireActivity(), new ArrayList<>());
        logs.setAdapter(adapter);
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

    private void getImagesWithRetry() {
        getImagesWithRetryRecursive();
    }

    private void getImagesWithRetryRecursive() {

        //Prepares to talk to server
        Retrofit retrofit = new Retrofit.Builder().baseUrl(serverAddress).addConverterFactory(GsonConverterFactory.create()).build();
        retrofitInterface apiService = retrofit.create(retrofitInterface.class);
        Call<Map<String, List<String>>> imageListCall = apiService.getImageList();

        imageListCall.enqueue(new Callback<Map<String, List<String>>>() {
            @Override
            public void onResponse(Call<Map<String, List<String>>> call, Response<Map<String, List<String>>> response) {
                if (response.isSuccessful()) {
                    Map<String, List<String>> responseBody = response.body();
                    System.out.println(responseBody);
                    if (responseBody != null && responseBody.containsKey("image_list")) {
                        List<Map.Entry<String, String>> imageEntries = new ArrayList<>();

                        for (String filename : responseBody.get("image_list")) {
                            // Construct the full image URL
                            String imageUrl =  serverAddress + ":5000/images/" + filename;
                            // Create a Map.Entry for each image
                            Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>(filename, imageUrl);
                            // Add the entry to the list
                            imageEntries.add(entry);
                        }

                        // Custom sorter that sorts the images by the their filename dates.
                        Collections.sort(imageEntries, new Comparator<Map.Entry<String, String>>() {
                            @Override
                            public int compare(Map.Entry<String, String> entry1, Map.Entry<String, String> entry2) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy(HH:mm)", Locale.getDefault());
                                try {
                                    Date date1 = dateFormat.parse(entry1.getKey());
                                    Date date2 = dateFormat.parse(entry2.getKey());
                                    return date1.compareTo(date2);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return 0;
                                }
                            }
                        });

                        Collections.reverse(imageEntries);


                        // Update the custom adapter with the new content (Image and corresponding text)
                        ImageListAdapter adapter = new ImageListAdapter(requireContext(), imageEntries);
                        logs.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } else {
                        System.out.println("Response body is null or does not contain 'image_list'");
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, List<String>>> call, Throwable t) {
                System.out.println("Fail: " + t.getMessage());
                // Retry indefinitely on failure
                getImagesWithRetryRecursive();
            }
        });
    }


    @Override
    public void onResume() {
        //Reloads the log each time the fragment is reloaded
        super.onResume();
        getImagesWithRetry();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}