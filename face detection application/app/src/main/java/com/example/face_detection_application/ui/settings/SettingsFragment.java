package com.example.face_detection_application.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.face_detection_application.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    boolean systemEnabled;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        systemEnabled = getSystemState();
        binding.disableButton.setChecked(systemEnabled);

        ///
        binding.disableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                systemEnabled = !systemEnabled;

                if (systemEnabled) {
                    System.out.println("Enabling system");
                    binding.disableButton.setChecked(true);
                    //Start up the system
                } else {
                    System.out.println("Disabling system");
                    binding.disableButton.setChecked(false);
                    //Shutdown the system
                }
            }
        });
        ///

        final TextView textView = binding.textNotifications;
        settingsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
}