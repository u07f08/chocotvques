package com.example.flowmahuang.chocotvques.controller

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.v4.util.LruCache
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.example.flowmahuang.chocotvques.R
import com.example.flowmahuang.chocotvques.module.GetSmallBitmapTool
import com.example.flowmahuang.chocotvques.module.adapter.DramaListRecyclerViewAdapter
import com.example.flowmahuang.chocotvques.module.file.ApiToTempFileTool
import com.example.flowmahuang.chocotvques.module.network.CheckInternetIsConnectTool
import com.example.flowmahuang.chocotvques.module.network.RetrofitBuildTool
import com.example.flowmahuang.chocotvques.module.network.apidrama.Datum
import com.example.flowmahuang.chocotvques.module.network.apidrama.Drama
import com.example.flowmahuang.chocotvques.module.permission.PermissionsActivity
import com.example.flowmahuang.chocotvques.module.preferences.KeyWordsPreferences
import com.example.flowmahuang.kotlinpractice.module.permission.PermissionsChecker
import com.google.gson.Gson
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by flowmahuang on 2018/3/27.
 */
class MainController(activity: Activity, recyclerView: RecyclerView, val mCallback: MainControllerCallback) {
    interface MainControllerCallback {
        fun itemClick(data: Datum)

        fun dataLoadComplete()

        fun apiFailure(message: String)

        fun noDataTemp()
    }

    private val mApiModule: RetrofitBuildTool
    private val mAdapter: DramaListRecyclerViewAdapter
    private val mPreferences: KeyWordsPreferences
    private val mLayoutManager: LinearLayoutManager
    private val mMemoryCache: LruCache<String, Bitmap>
    private val taskCollection: MutableSet<Thread>?
    private val mRecyclerView: RecyclerView
    private val mContext: Context
    private val mHandler: BitmapHandler

    private val ASK_PERMISSION_CODE: Int = 0
    private val checkPermissionList: Array<String> = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    )
    private var mAllDramaData: Drama? = null
    private var mSearchingDramaData: Drama? = null
    private var mFirstVisibleItem: Int = 0
    private var mVisibleItemCount: Int = 0
    private var isFirstEnter = true
    private var mSearchingKeyWords = ""

    init {
        val maxMemory = Runtime.getRuntime().maxMemory().toInt()
        val cacheSize = maxMemory / 8
        mMemoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String?, bitmap: Bitmap?): Int {
                return bitmap!!.byteCount / 300
            }
        }

        mContext = activity.applicationContext
        mApiModule = RetrofitBuildTool.getInstance()
        taskCollection = HashSet()
        mRecyclerView = recyclerView
        mAdapter = DramaListRecyclerViewAdapter(mContext,
                object : DramaListRecyclerViewAdapter.DramaListRecyclerCallback {
                    override fun onClick(data: Datum) {
                        mCallback.itemClick(data)
                    }

                    override fun onBindViewImageLoad(view: ImageView, cacheKey: String) {
                        val image = getBitmapFromMemoryCache(cacheKey)
                        if (image != null) {
                            view.setImageBitmap(image)
                        } else {
                            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorLightGray))
                        }
                    }
                })
        mLayoutManager = LinearLayoutManager(mContext)
        mHandler = BitmapHandler(activity, mRecyclerView)
        mPreferences = KeyWordsPreferences(mContext)
    }

    fun checkPermission(activity: Activity) {
        val permissionsChecker = PermissionsChecker(mContext)
        if (permissionsChecker.missingPermissions(* checkPermissionList)) {
            PermissionsActivity.startPermissionsForResult(activity,
                    ASK_PERMISSION_CODE,
                    * checkPermissionList)
        }
    }

    fun saveNowKeyWords() {
        mPreferences.saveSearchingKeyWords(mSearchingKeyWords)
    }

    fun getKeyWords(): String {
        return mPreferences.getSearchingKeyWords()
    }

    fun setRecyclerView() {
        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.adapter = mAdapter
        mRecyclerView.addOnScrollListener(mScrollListener())
    }

    fun refreshDramaList() {
        if (CheckInternetIsConnectTool.isConnect(mContext)) {
            mApiModule.getDramaInformation(
                    { drama ->
                        mAdapter.setDramaInformation(drama)
                        ApiToTempFileTool.pojoToFile(drama)
                        mAllDramaData = drama
                        mCallback.dataLoadComplete()
                    },
                    { message ->
                        mCallback.apiFailure(message)
                    })
        } else {
            if (ApiToTempFileTool.isFileExists(ApiToTempFileTool.POJO_FILE_NAME)) {
                val gson = Gson()
                val drama = gson.fromJson(ApiToTempFileTool.fileToPojo(), Drama::class.java)
                mAdapter.setDramaInformation(drama)
                mAllDramaData = drama
                mCallback.dataLoadComplete()
            } else {
                mCallback.noDataTemp()
            }
        }
    }

    fun searchInDramaList(keyWords: String) {
        if (mAllDramaData != null) {
            mAdapter.clearDramaInformation()
            if (keyWords.isEmpty()) {
                mAdapter.setDramaInformation(mAllDramaData!!)
                isFirstEnter = true
            } else {
                mSearchingDramaData =
                        if (keyWords.contains(mSearchingKeyWords) && !mSearchingKeyWords.isEmpty()) {
                            getSearchingDataFromOriginalData(keyWords, mSearchingDramaData!!)
                        } else {
                            getSearchingDataFromOriginalData(keyWords, mAllDramaData!!)
                        }
                mAdapter.setDramaInformation(mSearchingDramaData!!)
            }
            mSearchingKeyWords = keyWords
        }
    }

    fun getDatumWithDramaId(id: Int): Datum? {
        for (i in 0 until mAllDramaData!!.data.size) {
            if (mAllDramaData!!.data[i].drama_id == id) {
                return mAllDramaData!!.data[i]
            }
        }
        return null
    }

    private fun getFilterUrlString(url: String): String {
        return url.replace("[^\\w]".toRegex(), "")
    }

    private fun getSearchingDataFromOriginalData(keyWords: String, originalList: Drama): Drama {
        val tempList = Drama(ArrayList())
        for (i in 0 until originalList.data.size) {
            if (originalList.data[i].name.contains(keyWords)) {
                tempList.data.add(mAllDramaData!!.data[i])
            }
        }
        return tempList
    }

    /*
     *                           Image  Loader
     */
    private fun mScrollListener(): RecyclerView.OnScrollListener {
        return object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition()
                    mVisibleItemCount = mLayoutManager.findLastVisibleItemPosition() - mFirstVisibleItem + 1
                    if (mAllDramaData != null) {
                        loadBitmaps(mFirstVisibleItem, mVisibleItemCount)
                    }
                } else {
                    cancelAllTasks()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition()
                mVisibleItemCount = mLayoutManager.findLastVisibleItemPosition() - mFirstVisibleItem + 1
                if (isFirstEnter) {
                    if (mAllDramaData != null) {
                        mRecyclerView.postDelayed({
                            loadBitmaps(mFirstVisibleItem, mVisibleItemCount)
                        },100)
                    }
                    isFirstEnter = false
                }
            }
        }
    }

    private fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap)
        }
    }

    private fun getBitmapFromMemoryCache(key: String): Bitmap? {
        return mMemoryCache.get(key)
    }

    private fun loadBitmaps(firstVisibleItem: Int, visibleItemCount: Int) {
        try {
            for (i in firstVisibleItem until firstVisibleItem + visibleItemCount) {
                Log.e("asd",i.toString())
                val imageUrl = if (mSearchingKeyWords.isEmpty()) {
                    mAllDramaData!!.data[i].thumb
                } else {
                    mSearchingDramaData!!.data[i].thumb
                }
                val subUrl = getFilterUrlString(imageUrl)
                val bitmap = getBitmapFromMemoryCache(subUrl)
                if (bitmap == null) {
                    val task = mBitmapThread(imageUrl)
                    taskCollection?.add(task)
                    task.start()
                }
                mAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelAllTasks() {
        if (taskCollection != null) {
            for (task in taskCollection) {
                mHandler.removeCallbacks(task)
            }
        }
    }

    private fun mBitmapThread(url: String): Thread {
        return Thread(Runnable {
            var bitmap: Bitmap? = null
            val subUrl = getFilterUrlString(url)
            if (CheckInternetIsConnectTool.isConnect(mContext)) {
                val response = mApiModule.getThumbFromUrlCall(url).execute()
                if (response.isSuccessful) {
                    bitmap = GetSmallBitmapTool.getSmallBitmap(response.body()!!)!!
                    ApiToTempFileTool.thumbToFile(bitmap, subUrl)
                }
            } else {
                if (ApiToTempFileTool.isFileExists(subUrl)) {
                    bitmap = ApiToTempFileTool.fileToThumb(subUrl)
                }
            }

            if (bitmap != null) {
                addBitmapToMemoryCache(subUrl, bitmap)

                val bundle = Bundle()
                bundle.putParcelable("bitmap", bitmap)
                bundle.putString("key", subUrl)

                val message = Message()
                message.data = bundle
                mHandler.sendMessage(message)
            }
        })
    }

    private class BitmapHandler(activity: Activity, private val mRecyclerView: RecyclerView) : Handler() {
        private val mActivity: WeakReference<Activity> = WeakReference(activity)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val outerActivity = mActivity.get()
            if (outerActivity != null) {
                val bitmap = msg.data.getParcelable<Bitmap>("bitmap")
                val key = msg.data.getString("key")
                val imageView = mRecyclerView
                        .findViewWithTag<View>(key) as ImageView?
                if (imageView != null && bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }
}