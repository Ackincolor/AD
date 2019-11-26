package com.ackincolor.videoconversionclient.controller;

import android.util.Log;
import android.view.View;

import com.ackincolor.videoconversionclient.MainActivity;
import com.ackincolor.videoconversionclient.UnsafeOkHttpClient;
import com.ackincolor.videoconversionclient.entities.PathConversion;
import com.ackincolor.videoconversionclient.services.ConverterService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConvertController {
    private final String BASE_URL = "https://35.224.228.254:42308";
    final private MainActivity v;
    public ConvertController(MainActivity v){
        this.v = v;
    }
    public void start(String path){
        OkHttpClient okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient();
        Gson gson = new GsonBuilder().serializeNulls().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        ConverterService converterService = retrofit.create(ConverterService.class);
        PathConversion pc = new PathConversion();
        pc.path = path;
        final Call<JsonObject> call = converterService.convert(pc);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if(response.isSuccessful()){
                    System.out.println("conversion demmarée");
                    v.setStatusConversion("conversion demmarée");
                    System.out.println(response.body());
                    //ouverture du websocket
                    getStatus(response.body().get("uuid").getAsString());
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
    private void getStatus(final String uuid){
        OkHttpClient clientStatus =  UnsafeOkHttpClient.getUnsafeOkHttpClient();
        Request requestStatus = new Request.Builder().url(BASE_URL+"/conversion_status").build();
        WebSocketListener webSocketListenerStatus = new WebSocketListener() {
            String TAG = "DEBUG";

            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                webSocket.send("07e456b5-7526-49a0-8726-df2b8fef93d2");
                Log.e(TAG, "onOpen");
                //webSocket.send(uuid);
                super.onOpen(webSocket, response);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                super.onFailure(webSocket, t, response);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.e(TAG, "MESSAGE: Avancement :" + text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.e(TAG, "MESSAGE: " + bytes.hex());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                webSocket.cancel();
                Log.e(TAG, "CLOSE: " + code + " " + reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                System.out.println("socket closed :"+reason);
                //TODO: stuff
            }
        };

        clientStatus.newWebSocket(requestStatus, webSocketListenerStatus);
        clientStatus.dispatcher().executorService().shutdown();
    }
}
