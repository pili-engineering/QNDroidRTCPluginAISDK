package com.qiniu.droid.rtc.sample1v1ai

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.qiniu.droid.rtc.ai.QNRtcAISdkManager
import com.qiniu.droid.rtc.ai.core.util.JsonUtils
import com.qiniu.droid.rtc.sample1v1ai.http.OKHttpManger
import com.qiniu.droid.rtc.sample1v1ai.http.Token
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.Request

object AISdkManager {

    private fun getToken(url: String): Token {
        var token: Token? = null
        try {
            val request = Request.Builder()
                .url(
                    if (TextUtils.isEmpty(url)) {
                        "https://niucube-api.qiniu.com/v1/exam/aiToken"
                    } else {
                        "https://niucube-api.qiniu.com/v1/exam/aiToken?url=$url"
                    }
                )
                .get()
                .build();
            val call = OKHttpManger.okHttp.newCall(request);
            val resp = call.execute()
            val code = resp.code
            val tokenJson = resp.body?.string()
            Log.d("AISdkManager", "AISdkManager $tokenJson")
            token = JsonUtils.parseObject(tokenJson, Token::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return token ?: Token()
    }

    private var isInit = false
    fun init() {
        if (isInit) {
            return
        }
        isInit = true
        QNRtcAISdkManager.setSignCallback {
            getToken(it).data.signToken
        }
        Thread {
            QNRtcAISdkManager.setToken(getToken("").data.aiToken)
        }.start()

    }
}