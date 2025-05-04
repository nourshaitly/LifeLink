package com.example.lifelink.View;

import com.example.lifelink.Model.GeminiRequest;
import com.example.lifelink.Model.GeminiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GeminiAPI {

    @Headers({
            "Content-Type: application/json"
    })
    @POST("v1beta/models/gemini-1.5-flash:generateContent?key=AIzaSyDasrU4x-lClneVlomA4p-B5hs-kPf28O0")
    Call<GeminiResponse> sendPrompt(@Body GeminiRequest request);

}
