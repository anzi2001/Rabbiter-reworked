package com.example.kocja.rabbiter_online.managers;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpManager {
    private static OkHttpClient client;
    private final static MediaType postType = MediaType.parse("application/json; charset=utf-8");

    public static void initHttpClient() {
        client = new OkHttpClient();
    }

    public final static Handler handler = new Handler(Looper.getMainLooper());

    private static Request createRequest(String path) {
        return new Request.Builder()
                .url("http://nodejs-mongo-persistent-rabbit.a3c1.starter-us-west-1.openshiftapps.com/" + path)
                .build();
    }
    private static Request createRequest(RequestBody body, String path){
        return new Request.Builder()
                .url("http://nodejs-mongo-persistent-rabbit.a3c1.starter-us-west-1.openshiftapps.com/"+path)
                .post(body)
                .build();
    }

    public static void getRequest(String path, GetReturnBody body){

        client.newCall(createRequest(path)).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                if(response.isSuccessful()){
                    try {
                        body.GetReturn(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                        }
                }

            }
        });
    }


    public static void postRequest(String path, String data, PostReturnBody body){

        RequestBody reqBody = RequestBody.create(postType,data);

        client.newCall(createRequest(reqBody,path)).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) {
                try{
                    if(path.equals("searchForImage")){
                        byte[] bytes = response.body().bytes();
                        body.PostReturn(null,bytes);
                    }
                    else{
                        String responseBody = response.body().string();
                        body.PostReturn(responseBody,null);
                    }

                }
                catch(IOException e){

                    e.printStackTrace();
                }
            }
        });

    }


    public static void postRequest(String path, String data, File image, PostReturnBody body){
        MultipartBody.Builder mulPart = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("postEntry",data);
        if(image != null){
            mulPart.addFormDataPart("entryImage",image.getName(),RequestBody.create(MediaType.parse("multipart/form-data"),image));
        }
        RequestBody mulReq = mulPart.build();


        client.newCall(createRequest(mulReq,path)).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                body.PostReturn("OK",null);
            }
        });
    }

    public interface GetReturnBody{
        void GetReturn(String response);
    }
    public interface PostReturnBody{
        void PostReturn(@Nullable String response,@Nullable byte[] bytes);
    }

}