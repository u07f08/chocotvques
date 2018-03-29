package com.example.flowmahuang.chocotvques.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.flowmahuang.chocotvques.R
import com.example.flowmahuang.chocotvques.controller.MainController
import com.example.flowmahuang.chocotvques.module.network.apidrama.Datum
import com.google.gson.Gson


class MainActivity : AppCompatActivity() {
    private lateinit var mDramaSearchEditText: EditText
    private lateinit var mDramaSearchButton: Button
    private lateinit var mDramaListRecyclerView: RecyclerView

    private lateinit var mController: MainController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDramaSearchEditText = findViewById(R.id.main_drama_search_text)
        mDramaSearchButton = findViewById(R.id.main_drama_search_button)
        mDramaListRecyclerView = findViewById(R.id.main_drama_list)

        mController = MainController(this, mDramaListRecyclerView, controllerCallback())
        mController.checkPermission(this)
        mController.setRecyclerView()
        mController.refreshDramaList()

        mDramaSearchButton.setOnClickListener(onclickListener())
        mDramaSearchEditText.setOnEditorActionListener(editorActionListener())
        mDramaSearchEditText.clearFocus()

        setLastTimeKeyWords()
    }

    override fun onStop() {
        mController.saveNowKeyWords()
        super.onStop()
    }

    private fun startDetailActivity(data: Datum){
        val intent = Intent()
        val bundle = Bundle()
        bundle.putString("drama", Gson().toJson(data))
        intent.setClass(this@MainActivity, DramaDetailActivity::class.java)
        intent.putExtra("drama_data", bundle)
        startActivity(intent)
    }

    private fun setLastTimeKeyWords() {
        val lastTimeKeyWords = mController.getKeyWords()

        if (!lastTimeKeyWords.isEmpty()) {
            mDramaSearchEditText.setText(lastTimeKeyWords, TextView.BufferType.EDITABLE)
        }
    }

    private fun closeKeyBoard(editText: EditText) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    private fun onclickListener(): View.OnClickListener {
        return View.OnClickListener {
            mController.searchInDramaList(mDramaSearchEditText.text.toString())
            closeKeyBoard(mDramaSearchEditText)
            mDramaSearchEditText.clearFocus()
        }
    }

    private fun editorActionListener(): TextView.OnEditorActionListener {
        return TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mController.searchInDramaList(v.text.toString())
                closeKeyBoard(mDramaSearchEditText)
                mDramaSearchEditText.clearFocus()
                return@OnEditorActionListener true
            }
            return@OnEditorActionListener false
        }
    }

    private fun controllerCallback(): MainController.MainControllerCallback {
        return object : MainController.MainControllerCallback {
            override fun itemClick(data: Datum) {
                startDetailActivity(data)
            }

            override fun apiFailure(message: String) {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }

            override fun noDataTemp() {
                Toast.makeText(this@MainActivity, "無暫存可以顯示", Toast.LENGTH_SHORT).show()
            }

            override fun dataLoadComplete() {
                val uri = intent.data as Uri?
                if (uri != null) {
                    val path = uri.path
                    if (path.contains("/dramas/")) {
                        val id = uri.pathSegments[1].replace("[\\D+]".toRegex(),"")
                        if (!id.isEmpty()) {
                            val datum = mController.getDatumWithDramaId(id.toInt())
                            if (datum == null) {
                                Toast.makeText(this@MainActivity,"無效的ID",Toast.LENGTH_SHORT).show()
                            } else {
                                startDetailActivity(datum)
                            }
                        } else {
                            Toast.makeText(this@MainActivity,"無效的ID",Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                if (!mDramaSearchEditText.text.isEmpty()) {
                    mController.searchInDramaList(mDramaSearchEditText.text.toString())
                }
            }
        }
    }
}
