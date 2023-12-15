package com.example.face_detection_application.ui.log;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.face_detection_application.databinding.FragmentLogBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LogFragment extends Fragment {

    private FragmentLogBinding binding;
    private ListView logs;
    private ArrayAdapter<String> adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogViewModel logViewModel = new ViewModelProvider(this).get(LogViewModel.class);

        binding = FragmentLogBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Use binding to access the logs ListView
        logs = binding.logs;

        adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1);
        logs.setAdapter(adapter);  // Set the adapter to the ListView

        readLinesFromFile();

        final TextView textView = binding.textNotifications;
        logViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }


    private void readLinesFromFile() {
        // clear the adapter to start fresh
        adapter.clear();

        try {
            // reads the text file and gets neccesary info
            //TODO change so its reads image name and takes that info along with showing the image.
            InputStream inputStream = requireActivity().getAssets().open("logs.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            int lineCount = 0;

            while ((line = reader.readLine()) != null && lineCount < 3) {
                adapter.add(line);
                System.out.println(line);
                lineCount++;
            }

            reader.close();
            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onResume() {
        super.onResume();
        // Read the file each time the fragment is resumed
        readLinesFromFile();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}