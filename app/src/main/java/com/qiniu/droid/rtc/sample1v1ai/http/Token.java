package com.qiniu.droid.rtc.sample1v1ai.http;

public class Token {

    public int code;
    public String message;
    public TokenDao data= new TokenDao();
    public String requestId;

    public static class TokenDao {
        public String aiToken="";
        public String signToken="";
    }
}

