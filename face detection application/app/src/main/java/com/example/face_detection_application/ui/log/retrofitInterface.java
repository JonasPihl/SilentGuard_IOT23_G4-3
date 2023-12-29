package com.example.face_detection_application.ui.log;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface retrofitInterface {

    //Simple interface for expedience
    @GET("/get_image_list")
    Call<Map<String, List<String>>> getImageList();

    @POST("/on_off")
    Call<Void> on_off(@Query("value") boolean value);

    @GET("/state_of_server")
    Call<Boolean> state_of_server();

    @GET("/start_stream")
    Call<Void> start_stream();

    @GET("/stop_stream")
    Call<Void> stop_stream();

    @POST("/updateStartTime")
    Call<Integer> updateStartTime(@Query("startTime") int start);

    @POST("/updateEndTime")
    Call<Integer> updateEndTime(@Query("endTime") int end);
    @POST("/updateColor")
    Call<Float> updateColor(@Query("colorX") float colorX, @Query("colorY") float colorY);





}

