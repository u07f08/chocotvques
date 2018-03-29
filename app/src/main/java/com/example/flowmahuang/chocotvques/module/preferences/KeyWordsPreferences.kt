package com.example.flowmahuang.chocotvques.module.preferences

import android.content.Context

/**
 * Created by flowmahuang on 2018/3/29.
 */
class KeyWordsPreferences(context: Context) : PreferencesHelper(context) {
    private val SP_FiLE_NAME = KeyWordsPreferences::class.java.name
    private val SP_KEY_WORDS = "SP_KEY_WORDS"

    fun saveSearchingKeyWords(keyWords: String) {
        save(PreferencesHelper.Type.STRING, SP_KEY_WORDS, keyWords)
    }

    fun getSearchingKeyWords(): String {
        return get(SP_KEY_WORDS, PreferencesHelper.Type.STRING) as String
    }

    override fun getClassName(): String {
        return SP_FiLE_NAME
    }
}