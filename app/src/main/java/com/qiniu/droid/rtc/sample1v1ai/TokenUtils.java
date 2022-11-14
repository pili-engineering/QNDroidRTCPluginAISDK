package com.qiniu.droid.rtc.sample1v1ai;

import android.util.Log;

import com.qiniu.util.Auth;

import java.io.UnsupportedEncodingException;

public class TokenUtils {

    private static final String ENCODING = "UTF-8";

    static String appKey = "QxZugR8TAhI38AiJ_cptTl3RbzLyca3t-AAiH-Hh";
    static String appId = "testApp";
    static String appSecretKey = "4yv8mE9kFeoE31PVlIjWvi3nfTytwT0JiAxWjCDa";

    public static String resetToken() {
        return "QD QxZugR8TAhI38AiJ_cptTl3RbzLyca3t-AAiH-Hh:jzx6H3eRaBbh-bYQfPS9wgpc_D4=:dGVzdEFwcDoxNjU2MDUxOTQy";
    }

    public static String signUrlToToken(String url) {
        return Auth.create(appKey, appSecretKey).sign(url);// "${appKey}:${sha1}"
    }
}