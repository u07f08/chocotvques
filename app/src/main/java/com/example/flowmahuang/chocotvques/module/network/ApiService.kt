package com.example.flowmahuang.chocotvques.module.network

import com.example.flowmahuang.chocotvques.module.network.apidrama.Drama
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Created by flowmahuang on 2018/3/27.
 */
interface ApiService {
    @GET("5a97c59c30000047005c1ed2")
    fun getDramaInformation() : Call<Drama>

    @GET()
    fun getDramaThumb(@Url thumbUrl: String):Call<ResponseBody>
}