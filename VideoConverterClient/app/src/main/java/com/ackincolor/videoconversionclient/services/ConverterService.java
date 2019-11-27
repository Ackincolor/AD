package com.ackincolor.videoconversionclient.services;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import com.ackincolor.videoconversionclient.entities.PathConversion;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface ConverterService {
    @POST("convert")
    Call<JsonObject> convert(@Body PathConversion object);
    @GET("directories")
    Call<JsonArray> directories();
}
