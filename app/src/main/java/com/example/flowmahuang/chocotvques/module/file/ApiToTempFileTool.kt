package com.example.flowmahuang.chocotvques.module.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.example.flowmahuang.chocotvques.module.network.apidrama.Drama
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by flowmahuang on 2018/3/28.
 */
class ApiToTempFileTool {
    companion object {
        val FOLDER_PATH = Environment.getExternalStorageDirectory().path
        val FOLDER_NAME = "/chocotv_temp"
        val POJO_FILE_NAME = "drama_file.txt"

        fun pojoToFile(drama: Drama): Boolean {
            val allPath = FOLDER_PATH + FOLDER_NAME
            try {

                val fileDir = File(allPath)
                if (!fileDir.exists()) {
                    fileDir.mkdir()
                }

                val file = File(allPath + File.separator + POJO_FILE_NAME)
                file.createNewFile()
                val outputStream = FileOutputStream(file)
                val gson = Gson()
                outputStream.write(drama.toString(gson).toByteArray())
                outputStream.flush()
                outputStream.close()

                return true
            } catch (e: Exception) {
                return false
            }
        }

        fun thumbToFile(bitmap: Bitmap, thumbName: String): Boolean {
            val allPath = FOLDER_PATH + FOLDER_NAME
            try {
                val fileDir = File(allPath)
                if (!fileDir.exists()) {
                    fileDir.mkdir()
                }

                val file = File(allPath + File.separator + thumbName)
                file.createNewFile()
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                outputStream.flush()
                outputStream.close()

                return true
            } catch (e: Exception) {
                return false
            }
        }

        fun fileToPojo(): String {
            try {
                val file = File(FOLDER_PATH + FOLDER_NAME + File.separator + POJO_FILE_NAME)
                val inputStream = FileInputStream(file)
                val buffer = ByteArray(inputStream.available())
                inputStream.read(buffer)

                inputStream.close()
                return String(buffer)
            } catch (e: IOException) {
                return ""
            }
        }

        fun fileToThumb(fileName: String): Bitmap {
            return BitmapFactory.decodeFile(FOLDER_PATH + FOLDER_NAME + File.separator + fileName)
        }

        fun isFileExists(fileName: String): Boolean {
            return File(FOLDER_PATH + FOLDER_NAME + File.separator + fileName).exists()
        }
    }
}