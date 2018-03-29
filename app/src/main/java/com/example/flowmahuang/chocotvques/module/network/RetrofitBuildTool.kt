package com.example.flowmahuang.chocotvques.module.network

import com.example.flowmahuang.chocotvques.module.network.apidrama.Drama
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitBuildTool {

    companion object {
        fun getInstance() = Holder.instance
    }

    private object Holder {
        val instance = RetrofitBuildTool()
    }
    private val API_BASE_URL = "http://www.mocky.io/v2/"
    private val mRetrofit by lazy { create() }

    private fun create(): ApiService {
        val retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        return retrofit.create(ApiService::class.java)
    }

    fun getDramaInformation(success: (Drama) -> Unit, failure: (String) -> Unit) {
        val call = mRetrofit.getDramaInformation()
        call.enqueue(object : Callback<Drama> {
            override fun onResponse(call: Call<Drama>?, response: Response<Drama>?) {
                if (response != null) {
                    success(response.body()!!)
                }
            }

            override fun onFailure(call: Call<Drama>?, t: Throwable?) {
                if (t != null) {
                    failure(t.message!!)
                }
            }
        })
    }

    fun getThumbFromUrlCall(url: String):Call<ResponseBody> {
        return mRetrofit.getDramaThumb(url)
    }
}