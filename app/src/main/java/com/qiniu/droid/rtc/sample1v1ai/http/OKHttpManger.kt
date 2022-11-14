package com.qiniu.droid.rtc.sample1v1ai.http

import okhttp3.OkHttpClient

object OKHttpManger {
    var okHttp: OkHttpClient = OkHttpClient.Builder()
        .build()
        private set
}