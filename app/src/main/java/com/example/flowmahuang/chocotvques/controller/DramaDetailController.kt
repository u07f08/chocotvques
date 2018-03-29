package com.example.flowmahuang.chocotvques.controller

import android.content.Context
import android.graphics.Bitmap
import com.example.flowmahuang.chocotvques.module.GetSmallBitmapTool
import com.example.flowmahuang.chocotvques.module.file.ApiToTempFileTool
import com.example.flowmahuang.chocotvques.module.network.CheckInternetIsConnectTool
import com.example.flowmahuang.chocotvques.module.network.RetrofitBuildTool
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by flowmahuang on 2018/3/28.
 */
class DramaDetailController(val callback: DramaDetailControllerCallback) {
    interface DramaDetailControllerCallback {
        fun thumbLoad(bitmap: Bitmap?)
    }

    fun getBitmap(url: String, context: Context) {
        if (CheckInternetIsConnectTool.isConnect(context)) {
            val mApiModule = RetrofitBuildTool.getInstance()

            val call = mApiModule.getThumbFromUrlCall(url)
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                    if (response != null) {
                        callback.thumbLoad(GetSmallBitmapTool.getOriginalSizeBitmap(response.body()!!))
                    }
                }

                override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                    callback.thumbLoad(null)
                }
            })
        } else {
            callback.thumbLoad(ApiToTempFileTool.fileToThumb(getFilterUrlString(url)))
        }
    }

    private fun getFilterUrlString(url: String): String {
        return url.replace("[^\\w]".toRegex(), "")
    }
}