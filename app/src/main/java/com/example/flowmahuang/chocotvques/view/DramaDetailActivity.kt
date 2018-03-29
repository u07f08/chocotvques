package com.example.flowmahuang.chocotvques.view

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.flowmahuang.chocotvques.R
import com.example.flowmahuang.chocotvques.controller.DramaDetailController
import com.example.flowmahuang.chocotvques.module.UtcTimeFormater
import com.example.flowmahuang.chocotvques.module.network.apidrama.Datum
import com.google.gson.Gson

/**
 * Created by flowmahuang on 2018/3/28.
 */
class DramaDetailActivity : AppCompatActivity() {
    private lateinit var mDramaThumbImageView: ImageView
    private lateinit var mDramaNameTextView: TextView
    private lateinit var mDramaRatingTextView: TextView
    private lateinit var mDramaTotalTextView: TextView
    private lateinit var mDramaCreateAtTextView: TextView

    private lateinit var mContoller: DramaDetailController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drama_detail)

        mDramaThumbImageView = findViewById(R.id.drama_detail_thumb)
        mDramaNameTextView = findViewById(R.id.drama_detail_name)
        mDramaRatingTextView = findViewById(R.id.drama_detail_rating)
        mDramaTotalTextView = findViewById(R.id.drama_detail_views)
        mDramaCreateAtTextView = findViewById(R.id.drama_detail_create_at)
        mContoller = DramaDetailController(controllerCallback())

        initInformation()
    }

    private fun initInformation() {
        val intent = this.intent
        val bundle = intent.getBundleExtra("drama_data")
        val gsonData = Gson().fromJson(bundle.getString("drama"), Datum::class.java)

        mDramaNameTextView.text = gsonData.name
        mDramaRatingTextView.text = getString(R.string.item_drama_rating_title, gsonData.rating.toString())
        mDramaTotalTextView.text = getString(R.string.item_drama_total_views, gsonData.total_views)
        mDramaCreateAtTextView.text = getString(R.string.item_drama_create_at_title, UtcTimeFormater.timeFormat(gsonData.created_at))

        mContoller.getBitmap(gsonData.thumb, this)
    }

    private fun controllerCallback(): DramaDetailController.DramaDetailControllerCallback {
        return object : DramaDetailController.DramaDetailControllerCallback {
            override fun thumbLoad(bitmap: Bitmap?) {
                if (bitmap != null) {
                    mDramaThumbImageView.setImageBitmap(bitmap)
                } else {
                    Toast.makeText(this@DramaDetailActivity, "無法顯示封面圖", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}