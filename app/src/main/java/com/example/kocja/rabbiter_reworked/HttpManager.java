package com.example.kocja.rabbiter_reworked;

import android.os.AsyncTask;


import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpManager {
    private static OkHttpClient client;
    private static MediaType postType = MediaType.parse("application/json; charset=utf-8");
    public static void initHttpClient(){
        client = new OkHttpClient();
    }

    public static OkHttpClient getHttpClient(){
        return client;
    }
    public static void getRequest(String requestType,GetReturnBody body){
        new AsyncTask<Void,Void,Response>(){
            @Override
            protected Response doInBackground(Void... voids) {
                Request request = new Request.Builder()
                        .url("http://192.168.0.130:8081/"+requestType)
                        .build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }

            @Override
            protected void onPostExecute(Response response) {
                super.onPostExecute(response);
                body.GetReturn(response);

            }
        }.execute();
    }
    public static void postRequest(String requestType, String data, PostReturnBody body){
        new AsyncTask<Void,Void,Response>(){
            @Override
            protected Response doInBackground(Void... voids){

                RequestBody reqBody = RequestBody.create(postType,data);
                Request req = new Request.Builder()
                        .url("http://192.168.0.130:8081/"+requestType)
                        .post(reqBody)
                        .build();
                try {
                    return client.newCall(req).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Response response) {
                super.onPostExecute(response);
                body.PostReturn(response);
            }
        }.execute();

    }
    public interface GetReturnBody{
        void GetReturn(Response response);
    }
    public interface PostReturnBody{
        void PostReturn(Response response);
    }

}
