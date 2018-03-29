package com.example.flowmahuang.chocotvques.module

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by flowmahuang on 2018/3/28.
 */
class UtcTimeFormater {
    companion object {
        fun timeFormat(date: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.TAIWAN)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.timeZone = TimeZone.getTimeZone("Asia/Taipei")

            val outputDate: Date = inputFormat.parse(date)
            return outputFormat.format(outputDate).toString()
        }
    }
}