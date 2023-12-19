package com.example.face_detection_application.ui.log;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

public interface retrofitInterface {

    //Simple interface for expedience
    @GET("/get_image_list")
    Call<Map<String, List<String>>> getImageList();
}

