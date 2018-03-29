package com.example.flowmahuang.chocotvques.module

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.ResponseBody
import java.io.InputStream

/**
 * Created by flowmahuang on 2018/3/27.
 */
class GetSmallBitmapTool {
    companion object {
        fun getSmallBitmap(responseBody: ResponseBody): Bitmap? {
            try {
                val inputStream: InputStream = responseBody.byteStream()
                val opts = BitmapFactory.Options()
                opts.inSampleSize = 4

                val bmp = BitmapFactory.decodeStream(inputStream, null, opts)
                inputStream.close()

                return bmp
            } catch (err: Exception) {
                return null
            }
        }

        fun getOriginalSizeBitmap(responseBody: ResponseBody): Bitmap? {
            try {
                val inputStream: InputStream = responseBody.byteStream()

                val bmp = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                return bmp
            } catch (err: Exception) {
                return null
            }
        }
    }
}