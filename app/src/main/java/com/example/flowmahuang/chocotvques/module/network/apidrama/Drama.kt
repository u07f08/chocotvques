package com.example.flowmahuang.chocotvques.module.network.apidrama

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by flowmahuang on 2018/3/27.
 */
data class Drama(@SerializedName("data")
                 @Expose
                 val data: ArrayList<Datum>) {
    fun toString(gson: Gson): String {
        return gson.toJson(this)
    }
}