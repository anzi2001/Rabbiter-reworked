package com.example.kocja.rabbiter_online.managers

import android.os.Handler
import android.os.Looper

import java.io.File
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response

object HttpManager {
    private var client: OkHttpClient? = null
    private val postType = MediaType.parse("application/json; charset=utf-8")

    val handler = Handler(Looper.getMainLooper())

    fun initHttpClient() {
        client = OkHttpClient()
    }

    private fun createRequest(path: String): Request {
        return Request.Builder()
                .url("https://kocjancic.ddns.net/$path")
                .build()
    }

    private fun createRequest(body: RequestBody, path: String): Request {
        return Request.Builder()
                .url("https://kocjancic.ddns.net/$path")
                .post(body)
                .build()
    }

    fun getRequest(path: String, body: (string : String)->Unit) {

        client!!.newCall(createRequest(path)).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        //body.GetReturn(response.body()!!.string())
                        body(response.body()!!.string())
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }

            }
        })
    }


    fun postRequest(path: String, data: String, body: (response : String?,bytes : ByteArray?)->Unit) {

        val reqBody = RequestBody.create(postType, data)

        client!!.newCall(createRequest(reqBody, path)).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (path == "searchForImage") {
                        val bytes = response.body()!!.bytes()
                        //body.PostReturn(null, bytes)
                        body(null,bytes)
                    } else {
                        val responseBody = response.body()!!.string()
                        //body.PostReturn(responseBody, null)
                        body(responseBody,null)
                    }

                } catch (e: IOException) {

                    e.printStackTrace()
                }

            }
        })

    }


    fun postRequest(path: String, data: String, image: File?, body: (response : String, bytes : ByteArray?) -> Unit) {
        val mulPart = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("postEntry", data)
        if (image != null) {
            mulPart.addFormDataPart("entryImage", image.name, RequestBody.create(MediaType.parse("multipart/form-data"), image))
        }
        val mulReq = mulPart.build()


        client!!.newCall(createRequest(mulReq, path)).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                //body.PostReturn("OK", null)
                body("OK",null)
            }
        })
    }

    interface GetReturnBody {
        fun GetReturn(response: String)
    }

    interface PostReturnBody {
        fun PostReturn(response: String?, bytes: ByteArray?)
    }

}