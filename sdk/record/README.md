# 本地录制插件

## 快速接入

### 依赖配置

```
 implementation (name:'qndroid-rtc-localRecord-x.x.x',ext:"aar")
 implementation (name:'qdroid-avcodec-x.x.x',ext:"aar")
  
 implementation files('libs/qndroid-rtc-4.0.0.jar') 版本及以上
```

### 权限

```
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## api

```java

/**
 * 本地录制插件
 */
class QNRTCLocalRecordPlugin {

    //获取单例
    static QNRTCLocalRecordPlugin getInstance()

    /**
     * 开始 屏幕录制
     *
     * @param activity 权限请求activity
     * @param fileDir  文件保存路径
     * @param fileName 文件保存名字
     * @param width 分辨率宽
     * @param height 分辨率高
     * @param frameRate 帧率
     * @param bitrate 码率
     * @param callback callback
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @RequiresPermission(anyOf = {READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
    //需要存储权限
    void startMediaRecorder(QNRTCClient client, final FragmentActivity activity, final String fileDir, final String fileName, final int width, final int height, int frameRate, int bitrate, final QNLocalRecorderCallback callback)

    /**
     * 停止 屏幕录制
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void stopMediaRecorder(QNRTCClient client, Context context)

    /**
     * 创建屏幕共享轨道
     * @param activity
     * @param config
     * @param callback
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void createScreenShareTrack(final FragmentActivity activity, final QNCustomVideoTrackConfig config, final QNScreenShareCallback callback)


    /**
     * 销毁屏幕共享轨道
     * @param context
     */
    void releaseScreenShareTrack(Context context)
}
```

```java
/**
 * 媒体录制回调
 */
interface QNLocalRecorderCallback {

    /**
     * 启动成功
     * @param filePath 录制后的File
     */
    void onStart(String filePath);

    /**
     * 录制完成
     */
    void onStop();

    /**
     * 失败
     */
    void onError(int code, String msg);
}


/**
 * 屏幕共享回调
 */
interface QNScreenShareCallback {

    /**
     * 启动成功
     * @param screenShareTrack 屏幕共享轨道
     */
    void onCreateTrack(QNCustomVideoTrack screenShareTrack);

    /**
     * 失败
     */
    void onError(int code, String msg);
}
```

## 错误码

```
 // 用户不同意录制权限
    static int ERROR_CODE_NO_PERMISSION = 1;
    //创建文件失败
    static int ERROR_CODE_RECORD_DIR_ERROR = 2;
    //前台服务绑定失败
    static int ERROR_CODE_SERVICE_DIE = 3;
    //已经有正在开始的录制服务正在运行
    static int ERROR_CODE_ALREADY_RECODING = 4;
```
