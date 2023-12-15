package com.example.face_detection_application.ui.stream;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StreamViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public StreamViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}