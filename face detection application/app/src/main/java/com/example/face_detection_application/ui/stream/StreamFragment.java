package com.example.face_detection_application.ui.stream;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.face_detection_application.databinding.FragmentStreamBinding;
import com.example.face_detection_application.ui.retrofitInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class StreamFragment extends Fragment {

    private  String serverAddress;

    private FragmentStreamBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStreamBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        serverAddress = fetchServerIP(requireContext());
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

    private void startStream(){
        //Prepares to talk to server
        Retrofit retrofit = new Retrofit.Builder().baseUrl(serverAddress + ":5000").build();
        retrofitInterface apiService = retrofit.create(retrofitInterface.class);
        Call<Void> startStream = apiService.start_stream();
        startStream.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                //Binds the webview and adds the link it should search for
                WebView myWebView = binding.webview;
                myWebView.loadUrl(serverAddress + ":8080/?action=stream");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                t.getMessage();
            }
        });
    }

    private void stopStream(){
        //Prepares to talk to server
        Retrofit retrofit = new Retrofit.Builder().baseUrl(serverAddress + ":5000").build();
        retrofitInterface apiService = retrofit.create(retrofitInterface.class);
        Call<Void> stopStream = apiService.stop_stream();
        stopStream.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopStream();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        startStream();
    }

    public void onPause() {
        super.onPause();
        stopStream();
    }
}
