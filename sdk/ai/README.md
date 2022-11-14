# 七牛rtc 接入 AI 能力

## 快速接入

### 依赖配置

  ```
   implementation (name:'qndroid-rtc-ai-x.x.x',ext:"aar")
   implementation (name:'qdroid-avcodec-x.x.x',ext:"aar")
   implementation files('libs/qndroid-rtc-3.1.0.jar') //依赖 rtc 3.1.0版本以上
   implementation "org.java-websocket:Java-WebSocket:1.4.0"
   implementation group: 'com.google.code.gson', name: 'gson', version: '2.8.9'
  ```

### 模块初始化

```kotlin
class App : Application() {
    override fun onCreate() {
        //初始化
        QNRtcAISdkManager.init(this)
        //设置语音识别签名回调
        QNRtcAISdkManager.setSignCallback { url ->
            //向接入业务服务器完成url签名
        }
        //设置aitoken
        QNRtcAISdkManager.setToken("your ai token")
    }
}
```

### aiToken 和url签名

#### aiToken

```
// app_id 加上过期时间
src="<app_id>:<expiration>"
encodedSrc=urlsafe_base64_encode(src)
// 计算HMAC-SHA1签名，并对签名结果做URL安全的Base64编码
sign=hmac_sha1(encodedSrc,"Your_Secret_Key")
encodedSign=urlsafe_base64_encode(sign)
// 拼接上述结果得到 token
token="QD "+Your_Access_Key+":"+encodedSign+":"+encodedSrc``
```

涉及到用户_Secret_Key 建议签名逻辑运行在接入方服务器端

### url签名

public String signUrlToToken(String url)中参数为待签名的url（url中已经包含了时间戳）
[将url签名成token 参考](https://developer.qiniu.com/kodo/1202/download-token)
涉及到用户_Secret_Key 建议此逻辑房间服务器端 涉及到用户_Secret_Key 建议签名逻辑运行在接入方服务器端

## api说明

### 活体检测

```java
 //动作活体检测
class QNFaceActionLiveDetector {

    //创建动作活体检测
    static QNFaceActionLiveDetector create();

    //获取校验码
    void getRequestCode(QNFaceActLiveCodeParam param,         // 参数
                        QNFaceActionLiveCodeCallback callback //回调
    );

    //开始录制 start -> commit 之间时间建议1-10秒
    void start(QNTrack videoTrack,      //视频轨道
               QNFaceActLiveParam param //参数
    );

    // 获取结果识别结果，如网络原因提交失败校验码没有过期可继续提交 
    void commit(QNFaceActionLiveCallback callBack);

    //取消
    void cancel();
}

//视频动作活体的验证码参数
class QNFaceActLiveCodeParam {
    int min_code_length = 1;  //视频动作活体的验证码最小长度：最大3 最小1 默认1
    int max_code_length = 1;  //视频动作活体的验证码最大长度：最大3 最小1 默认3
}

// 活体检测参数
class QNFaceActionLiveParams {
    String sessionID = "";  //会话ID, 获取方式参考随机校验码接口
    String faceField = "spoofing,quality"; //需要使用合成图功能时, 此项传入spoofing;需要使用图片质量信息时，则传入quality;字段之间使用,号分隔，eg：spoofing,quality
    int encodeWidth = 480;  //采集质量控制 - 视频宽
    int encodeHeight = 640; //采集质量控制 - 视频高
    int encodeBitRate = 180 * 1000;  //采集质量控制 - 码率 码率越高识别结果越准确同时请求相应时间变长
    int encodeFPS = 15;     //采集质量控制 - 帧率
}

//活体检测结果回调
interface QNFaceActionLiveCallback {
    void onResult(QNFaceActionLive faceActLive);
}

//校验码请求回调
interface QNFaceActionLiveCodeCallback {
    void onResult(QNFaceActLiveCode faceActLiveCode);
}

//活体检测结果
class QNFaceActionLive {
    String requestID;    //请求ID
    String serverLogID;  //请求日志ID
    int errorCode;       //请求返回码
    String errorMsg;     //提示
    Result result;       //请求结果

    //请求结果
    static class Result {
        double score;        //活体检测的总体打分 范围[0,1]，分数越高则活体的概率越大
        double maxSpoofing;  //返回的1-8张图片中合成图检测得分的最大值 范围[0,1]，分数越高则概率越大
        double spoofingScore;//返回的1-8张图片中合成图检测得分的中位数 范围[0,1]，分数越高则概率越大
        String actionVerify; //动作识别结果 pass代表动作验证通过，fail代表动作验证未通过
        Thresholds thresholds;//阈值 按活体检测分数>阈值来判定活体检测是否通过(阈值视产品需求选择其中一个)
        BestImage bestImage; //图片信息
        List<PicList> picList;//返回1-8张抽取出来的图片信息

        //阈值
        static class Thresholds {
            double frr_1e4;   //万分之一误拒率的阈值
            double frr_1e3;   //千分之一误拒率的阈值
            double frr_1e2;   //百分之一误拒率的阈值
        }

        //图片信息
        static class BestImage {
            String pic;      //base64编码后的图片信息
            String faceToken;//人脸图片的唯一标识
            String faceID;   //人脸ID
            double livenessScore; //此图片的活体分数，范围[0,1]
            double spoofing; //此图片的合成图分数，范围[0,1]
            double notSpoofing;
            Quality quality; //人脸质量信息。face_field包含quality时返回
            Angle angle;     //角度信息

            //人脸质量信息
            static class Quality {

                Occlusion occlusion; //人脸各部分遮挡的概率，范围[0~1]，0表示完整，1表示不完整
                double blur;         //人脸模糊程度，范围[0~1]，0表示清晰，1表示模糊
                double illumination; //取值范围在[0~255], 表示脸部区域的光照程度 越大表示光照越好
                double completeness; //人脸完整度，0或1, 0为人脸溢出图像边界，1为人脸都在图像边界内

                //人脸各部分遮挡的概率
                static class Occlusion {
                    double leftEye;    //左眼遮挡比例，[0-1] ，1表示完全遮挡
                    double rightEye;   //右眼遮挡比例，[0-1] ，1表示完全遮挡
                    double nose;       //鼻子遮挡比例，[0-1] ，1表示完全遮挡
                    double mouth;      //嘴遮挡比例，[0-1] ，1表示完全遮挡
                    double leftCheek;  //左脸颊遮挡比例，[0-1] ，1表示完全遮挡
                    double rightCheek; //右脸颊遮挡比例，[0-1] ，1表示完全遮挡
                    double chinContour;//下巴遮挡比例，[0-1] ，1表示完全遮挡
                }
            }

            static class Angle {
                double yaw;
                double pitch;
                double roll;
            }
        }

        //返回1-8张抽取出来的图片信息
        static class PicList {
            String pic;        //base64编码后的图片信息
            String faceToken;  //人脸图片的唯一标识
            String faceID;     //人脸ID
            double livenessScore;//此图片的活体分数，范围[0,1]
            double spoofing;   //此图片的合成图分数，范围[0,1]
            double notSpoofing;
            QualityX quality; //人脸质量信息。face_field包含quality时返回
            AngleX angle;     //角度信息

            //人脸质量信息。face_field包含quality时返回
            static class QualityX {
                OcclusionX occlusion; //人脸各部分遮挡的概率，范围[0~1]，0表示完整，1表示不完整
                double blur;          //人脸模糊程度，范围[0~1]，0表示清晰，1表示模糊
                double illumination;  //取值范围在[0~255], 表示脸部区域的光照程度 越大表示光照越好
                double completeness;  //人脸完整度，0或1, 0为人脸溢出图像边界，1为人脸都在图像边界内

                //人脸各部分遮挡的概率，
                static class OcclusionX {
                    double leftEye;    //左眼遮挡比例，[0-1] ，1表示完全遮挡
                    double rightEye;   //右眼遮挡比例，[0-1] ，1表示完全遮挡
                    double nose;       //鼻子遮挡比例，[0-1] ，1表示完全遮挡
                    double mouth;      //嘴遮挡比例，[0-1] ，1表示完全遮挡
                    double leftCheek;  //左脸颊遮挡比例，[0-1] ，1表示完全遮挡
                    double rightCheek; //右脸颊遮挡比例，[0-1] ，1表示完全遮挡
                    double chinContour;//下巴遮挡比例，[0-1] ，1表示完全遮挡
                }
            }

            static class AngleX {
                double yaw;
                double pitch;
                double roll;
            }
        }
    }
}

//动作活体校验码（包含用户需要录制的动作）
class QNFaceActLiveCode {
    int errorCode;    //请求返回码
    String errorMsg;  //提示
    String requestID; //请求ID
    String serverLogID;//日志ID
    Result result;    //结果

    //校验码结果
    static class Result {
        //随机校验码会话id，有效期5分钟，请提示用户在五分钟内完成全部操作验证码使用过即失效，每次使用视频活体前请重新拉取验证码
        String sessionID = "";
        //随机验证码，数字形式，1~6位数字；若为动作活体时，返回数字表示的动作对应关系为：0:眨眼 4:抬头 5:低头 7:左右转头(不区分先后顺序，分别向左和向右转头)
        String code = "";
        List<QNFaceActAction> faceActions;  //获取动作枚举
    }
}

//动作枚举
enum QNFaceAction {
    BLINK(0),   //眨眨眼
    LIFT_UP(4), //抬头
    LOW_DOWN(5),//低头
    SHAKE(7);   //摇摇头
}
```

```kotlin
//example
//创建动作活体检查
val actionFaceComparer = QNFaceActionLiveDetector.create()
//获取校验码
actionFaceComparer.getRequestCode(
    QNFaceActLiveCodeParam()
) { code ->
    //开始动作活体
    actionFaceComparer.start(
        localVideoTrack,
        QNFaceActLiveParam().apply {
            //设置校验码
            sessionID = code.result.sessionID
        }
    )
    GlobalScope.launch(Dispatchers.Main) {
        tvTip.visibility = View.VISIBLE
        tvTip.text = "准备动作"
        //对校验码里的每个动作显示提示
        code.result.faceActions.forEach {
            //等待x秒-用户完成动作
            delay(2000)
            tvTip.text = it.getTip()
        }
        //提交获取结果
        actionFaceComparer.commit { faceActLive ->
        }
    }
}
```

### 语音转文字

```java
class QNAudioToTextAnalyzer {
    // 开始语音实时识别
    static QNAudioToTextAnalyzer start(QNTrack audioTrack,         //音频轨道
                                       QNAudioToTextParams params, //参数
                                       QNAudioToTextCallback callback);

    // 停止语音实时识别
    void stop();
}

//实时语音转文字参数
class QNAudioToTextParam {
     int modelType = 1;     //识别语言，中文: 1, 英文: 2, 中英混合: 0; 默认 1
     String keyWords = "";  //识别关键字; 相同读音时优先识别为关键字。每个词 2-4 个字, 不同词用 , 分割
}

// 实时语音转文字回调
interface QNAudioToTextCallback {

    // 停止语音实时识别
    void onAudioToText(QNAudioToText audioToText);

    // 错误
    void onError(int code, String errMsg);

    // 开始成功
    void onStart();

    // 实时转化结束
    void onStop();
}

// 语音识别结果
// 当前片段的结果文字数据
class QNAudioToText {
    boolean isFinal;    //此识别结果是否为最终结果
    boolean isBegin;    //此识别结果是否为第一片
    BestTranscription bestTranscription; //最好的转写候选

    //最好的转写候选
    static class BestTranscription {
        String transcribedText;  //转写结果
        int beginTimestamp;      //句子的开始时间, 单位毫秒
        int endTimestamp;        //句子的结束时间, 单位毫秒
        List<KeyWordsType> keyWordsType; //转写结果中包含KeyWords内容
        List<Piece> piece;       //转写结果的分解（只对final状态结果有效，返回每个字及标点的详细信息）

        //结果中包含KeyWords
        static class KeyWordsType {
            String keyWords;      //命中的关键词KeyWords。返回不多于10个。
            double keyWordsScore; //命中的关键词KeyWords相应的分数。分数越高表示和关键词越相似，对应kws中的分数。
            int startTimestamp;   //关键词开始时间, 单位毫秒
            int endTimestamp;     //关键词结束时间, 单位毫秒
        }

        //转写结果的分解（只对final状态结果有效，返回每个字及标点的详细信息）
        static class Piece {
            String transcribedText;  //转写分解结果
            int beginTimestamp;      //分解开始时间(音频开始时间为0), 单位毫秒
            int endTimestamp;        //分解结束时间(音频开始时间为0), 单位毫秒
        }
    }
}
```

```kotlin
// example:
val mQNAudioToTextAnalyzer = QNAudioToTextAnalyzer.start(
    localAudioTrack,
    QNAudioToTextParam(), object : QNAudioToTextAnalyzer.QNAudioToTextCallback {
        override fun onAudioToText(audioToText: QNAudioToText) {
            tvText.text = audioToText.bestTranscription.transcribedText ?: ""
        }
    })
//停止语音转文字
audioToTextAnalyzer.stop()
```

### 人脸对比

```java
 class QNFaceComparer {

    //开始一次人脸对比
    static void run(QNTrack videoTrack,                 //视频轨道
                    Uri targetImg,                      //目标图片
                    QNFaceCompareParam targetImgParam,  //目标图片参数
                    QNFaceCompareParam captureImgParam, //采集人脸图片参数
                    QNFaceCompareCallback callback);

}

// 人脸对比参数
class QNFaceCompareParam {

    /**
     * 人脸的类型
     * LIVE：表示生活照：通常为手机、相机拍摄的人像图片、或从网络获取的人像图片等，
     * IDCARD：表示身份证芯片照：二代身份证内置芯片中的人像照片，
     * WATERMARK：表示带水印证件照：一般为带水印的小图，如公安网小图
     * CERT：表示证件照片：如拍摄的身份证、工卡、护照、学生证等证件图片
     * INFRARED：表示红外照片,使用红外相机拍摄的照片
     * HYBRID：表示混合类型，如果传递此值时会先对图片进行检测判断所属类型(生活照 or 证件照)（仅针对请求参数 image_type 为 BASE64 或 URL 时有效）
     * 默认LIVE
     */
    String faceType = "LIVE";
    /**
     * 图片质量控制
     * NONE: 不进行控制
     * LOW:较低的质量要求
     * NORMAL: 一般的质量要求
     * HIGH: 较高的质量要求
     * 默认 NONE
     * 若图片质量不满足要求，则返回结果中会提示质量检测失败
     */
    String qualityControl = "LOW";
    /**
     * 活体检测控制
     * NONE: 不进行控制
     * LOW:较低的活体要求(高通过率 低攻击拒绝率)
     * NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率)
     * HIGH: 较高的活体要求(高攻击拒绝率 低通过率)
     * 默认 NONE
     * 若活体检测结果不满足要求，则返回结果中会提示活体检测失败
     */
    String livenessControl = "HIGH";

    /**
     * 人脸检测排序类型
     * 0:代表检测出的人脸按照人脸面积从大到小排列
     * 1:代表检测出的人脸按照距离图片中心从近到远排列
     * 默认为0
     */
    String faceSortType = "0";

    /**
     * 合成图控制参数
     * NONE: 不进行控制
     * LOW:较低的合成图阈值数值，由于合成图判定逻辑为大于阈值视为合成图攻击，该项代表低通过率、高攻击拒绝率
     * NORMAL: 一般的合成图阈值数值，由于合成图判定逻辑为大于阈值视为合成图攻击，该项代表平衡的攻击拒绝率, 通过率
     * HIGH: 较高的合成图阈值数值，由于合成图判定逻辑为大于阈值视为合成图攻击，该项代表高通过率、低攻击拒绝率
     * 默认为NONE
     */
    String spoofingControl = "NONE";
    int captureWidth = 240;    //采集图片大小控制 - 目标宽
    int captureHeight = 320;   //采集图片大小控制 - 目标高
    int captureQuality = 90;   //采集图片质量控制
}

// 人脸对比结果
class QNFaceCompare {
    String requestID; //请求ID
    int errorCode;    //请求返回码
    String errorMsg;  //提示
    long logID;       //请求日志ID
    Result result;    //请求结果

    static class Result {
        double score;  //人脸相似度得分，推荐阈值80分
        List<FaceList> faceList; //人脸信息列表

        static class FaceList {
            String faceToken;  //人脸的唯一标志
        }
    }
}

// 人脸对比回调
interface QNFaceCompareCallback {
    void onResult(QNFaceCompare faceCompare); // 比较结果回调
}
```

```kotlin
// example:
QNFaceComparer.run(
    localVideoTrack,
    url,
    QNFaceCompareParam(),
    QNFaceCompareParam()
) { faceCompare ->
    //显示结果
}
```

### 人脸检测

```java
class QNFaceDetector {
    // 开始一次人脸检测
    static void run(QNTrack videoTrack,       //视频轨道
                    QNFaceDetectParams params,//参数
                    QNFaceDetectCallback callback);
}

// 人脸检测参数
class QNFaceDetectParam {

    //活体检测控制：NONE: 不进行控制， LOW:较低的活体要求(高通过率 低攻击拒绝率)，NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率)， HIGH: 较高的活体要求(高攻击拒绝率 低通过率)
    String livenessControl = "NONE";
    //人脸检测排序类型 0:代表检测出的人脸按照人脸面积从大到小排列，1:代表检测出的人脸按照距离图片中心从近到远排列
    int faceSortType = 0;
    // 是否显示检测人脸的裁剪图base64值 0：不显示（默认），1：显示
    int displayCorpImage = 0;
    // 人脸的类型 LIVE：表示生活照：通常为手机、相机拍摄的人像图片、或从网络获取的人像图片等，IDCARD：表示身份证芯片照：二代身份证内置芯片中的人像照片，
    // WATERMARK：表示带水印证件照：一般为带水印的小图，如公安网小图， CERT：表示证件照片：如拍摄的身份证、工卡、护照、学生证等证件图片
    String faceType = "LIVE";
    //包括age,expression,face_shape,gender,glasses,landmark,landmark150,quality,eye_status,emotion,face_type,mask,spoofing信息逗号分隔. 默认只返回face_token、人脸框、概率和旋转角度
    String faceField = "";
    int maxFaceNum = 1;     //最多处理人脸的数目，默认值为1，根据人脸检测排序类型检测图片中排序第一的人脸（默认为人脸面积最大的人脸），最大值120
    int captureWidth = 240; //采集图片大小控制 - 目标宽
    int captureHeight = 320;//采集图片大小控制 - 目标高
    int captureQuality = 90;//采集图片质量控制
}

// 人脸检测回调
interface QNFaceDetectCallback {
    void onResult(QNFaceDetect faceDetect); // 人脸检测结果
}

// 人脸检测结果
class QNFaceDetect {
    String requestID;//请求ID//请求ID
    String logID;   //请求日志ID
    int errorCode;  //请求返回码
    String errorMsg;//提示
    Result result;  //请求结果

    static class Result {
        int faceNum; //检测到的图片中的人脸数量
        List<FaceList> faceList; //人脸信息列表，具体包含的参数参考下面的列表。

        static class FaceList {
            String faceToken;    //人脸图片的唯一标识
            Location location;   //人脸在图片中的位置
            int faceProbability; //人脸置信度，范围【0~1】，代表这是一张人脸的概率，0最小、1最大。其中返回0或1时，数据类型为Integer
            Angle angle;         //人脸旋转角度参数
            double age;          //年龄 ，当face_field包含age时返回
            Expression expression;//表情，当 face_field包含expression时返回
            Gender gender;        //性别，face_field包含gender时返回
            Glasses glasses;      //是否带眼镜，face_field包含glasses时返回
            FaceShape faceShape;  //脸型，当face_field包含face_shape时返回
            Quality quality;      //人脸质量信息。face_field包含quality时返回
            List<Landmark> landmark;//4个关键点位置，左眼中心、右眼中心、鼻尖、嘴中心。face_field包含landmark时返回
            List<Landmark72> landmark72; //72个特征点位置 face_field包含landmark时返回

            static class Location {
                double left;   //人脸区域离左边界的距离
                double top;    //人脸区域离上边界的距离
                double width;  //人脸区域的宽度
                double height; //人脸区域的高度
                double rotation; //人脸框相对于竖直方向的顺时针旋转角，[-180,180]
            }

            static class Angle {
                double yaw;  //三维旋转之左右旋转角[-90(左), 90(右)]
                double pitch;//三维旋转之俯仰角度[-90(上), 90(下)]
                double roll; //平面内旋转角[-180(逆时针), 180(顺时针)]
            }

            static class Expression {
                String type;  //none:不笑；smile:微笑；laugh:大笑
                double probability;//表情置信度，范围【0~1】，0最小、1最大。
            }

            static class Gender {
                String type;       //square: 正方形 triangle:三角形 oval: 椭圆 heart: 心形 round: 圆形
                double probability;//置信度，范围【0~1】，0最小、1最大。
            }

            static class Glasses {
                String type;       //none:无眼镜，common:普通眼镜，sun:墨镜
                double probability;//置信度，范围【0~1】，0最小、1最大。
            }

            static class FaceShape {

                String type; //human: 真实人脸 cartoon: 卡通人脸
                double probability; //human: 真实人脸 cartoon: 卡通人脸
            }

            static class Quality {

                Occlusion occlusion; //人脸各部分遮挡的概率，范围[0~1]，0表示完整，1表示不完整
                int blur;            //人脸模糊程度，范围[0~1]，0表示清晰，1表示模糊
                int illumination;    //取值范围在[0~255], 表示脸部区域的光照程度 越大表示光照越好
                int completeness;    //人脸完整度，0或1, 0为人脸溢出图像边界，1为人脸都在图像边界内

                static class Occlusion {
                    int leftEye;    //左眼遮挡比例，[0-1] ，1表示完全遮挡
                    int rightEye;   //右眼遮挡比例，[0-1] ，1表示完全遮挡
                    int nose;       //鼻子遮挡比例，[0-1] ，1表示完全遮挡
                    int mouth;      //嘴遮挡比例，[0-1] ，1表示完全遮挡
                    int leftCheek;  //左脸颊遮挡比例，[0-1] ，1表示完全遮挡
                    int rightCheek; //右脸颊遮挡比例，[0-1] ，1表示完全遮挡
                    int chinContour;//下巴遮挡比例，[0-1] ，1表示完全遮挡
                }
            }

            static class Landmark {
                double x;
                double y;
            }

            static class Landmark72 {
                double x;
                double y;
            }
        }
    }
}
```

```kotlin
// example:
QNFaceDetector.run(localVideoTrack, QNFaceDetectParam()) { faceDetect ->
    //显示结果
}
```

### 身份证识别

```java
class QNIDCardDetector {
    // 开始一次身份证识别
    static void run(QNTrack videoTrack,        //视频轨道
                    QNIDCardDetectParam param, //参数
                    QNIDCardDetectCallback callback);
}

//身份证识别请求参数
class QNIDCardDetectParam {
    boolean isMirror = false;   //当前图片是不是镜像
    boolean retImage = false;   //是否返回识别后的切图(切图是指精确剪裁对齐后的身份证正反面图片)，返回格式为 JPEG 格式二进制图片使用 base64 编码后的字符串
    boolean retPortrait = false;//是否返回身份证(人像面)的人脸图 片，返回格式为 JPEG 格式二进制图片使用 base64 编码后的字符串
    String refSide = "Any";     //当图片中同时存在身份证正反面时，通过该参数指定识别的版面:取值'Any' - 识别人像面或国徽面，'F' - 仅 识别人像面，'B' - 仅识别国徽面
    boolean enableBorderCheck = false; //身份证遮挡检测开关，如果输入图片中的身份证卡片边框不完整则返回告警
    boolean enableDetectCopy = false;//复印件、翻拍件检测开关，如果输入图片中的身份证卡片是复印件，则返回告警
    int captureWidth = 240;     //采集图片大小控制 - 目标宽
    int captureHeight = 320;    //采集图片大小控制 - 目标高
    int captureQuality = 90;    //采集图片质量控制
}

// 身份证识别结果回调
interface QNIDCardDetectCallback {
    void onResult(QNIDCardDetect idCardDetect);
}

//身份证识别结果
class QNIDCardDetect {
    OcrResult ocrResult;//文字识别结果
    ImageResult imageResult; //图片检测结果
    String requestID;   //请求ID
    int errorCode;      //请求返回码
    String errorMsg;    //提示
    String[] warnMsg;   //多重警告码

    static class OcrResult {
        String side;  //F-身份证人像面，B-身份 证国徽面
        String idno;  //身份号码(人像面)
        String name;  //姓名(人像面)
        String nation;//民族(人像面)
        String gender;//性别(人像面)
        String address;//地址(人像面)
        String birthDate; //生日(人像面) eg. "19900111"
        String validthru; //有效期(国徽面) eg. "20001010-20101009"
        String issuedBy;  //签发机关(国徽面)
    }

    static class ImageResult {
        List<List<Integer>> idCardBox; //框坐标，格式为 [[x0, y0], [x1, y1], [x2, y2], [x3, y3]]
        String idcard;  //身份证区域图片，使用Base64 编码后的字符串， 是否返回由请求参数ret_image 决定
        String portrait;//	身份证人像照片，使用Base64 编码后的字符串， 是否返回由请求参数ret_portrait 决定
    }
}
```

```kotlin
// example:
QNIDCardDetector.run(localVideoTrack, QNIDCardDetectParam()) { idCardDetect ->
}
```

### 人脸对比

```java
//权威人脸对比
class QNAuthoritativeFaceComparer {
    //开始一次权威人脸
    static void run(QNTrack videoTrack,                      //视频轨道
                    @NonNull QNAuthoritativeFaceParam param, //参数
                    QNAuthoritativeFaceComparerCallback callback);
}

//权威人脸结果回调
interface QNAuthoritativeFaceComparerCallback {
    //结果回调
    void onResult(QNAuthoritativeFace authoritativeFace);
}

// 权威人脸比对参数
class QNAuthoritativeFaceParam {
    String sessionID = ""; // 自定义会话ID
    String realName;       // 真实名字
    String idCard;         // 身份证号码
    int captureWidth = 240;  // 采集图片大小控制 - 目标宽
    int captureHeight = 320; // 采集图片大小控制 - 目标高
    int captureQuality = 90; // 采集图片质量控制
}

// 权威人脸结果
class QNAuthoritativeFace {
    String requestID; // 请求id 用于排查日志
    String sessionID;  // 自定义会话ID
    int errorCode; // 请求返回码
    String errorMsg; // 提示
    double similarity;// 相似度
}
```

```kotlin
// example:
val param = QNAuthoritativeFaceParam().apply {
    realName = name //设置名字
    idCard = idcard //设置身份证号码
}
QNAuthoritativeFaceComparer.run(localVideoTrack, param) { authoritativeFace ->
}
```

### 权威人脸对比加动作活体

```java
//权威人脸对比加动作活体
class QNAuthorityActionFaceComparer {
    // 创建请求
    static QNAuthorityActionFaceComparer create();

    // 获取校验码
    void getRequestCode(QNFaceActLiveCodeParam param, //校验码参数
                        QNAuthorityActionLiveCodeCallback callback);

    //开始权威人脸对比  start -> commit 之间时间建议1-10秒
    void start(QNTrack videoTrack,  //视频轨道
               QNFaceActLiveParam faceActionParam,//动作请求参数
               QNAuthoritativeFaceParam authoritativeFaceParam //权威人脸参数
    );

    // 获取结果识别结果，如网络原因提交失败校验码没有过期可继续提交
    void commit(QNAuthorityActionFaceComparerCallback callback);

    // 取消
    void cancel();
}

//提交结果回调
interface QNAuthorityActionFaceComparerCallback {
    //结果
    void onResult(QNFaceActionLive faceActLive,        //动作识别结果
                  QNAuthoritativeFace authoritativeFace//权威人脸对比结果
    );
}

//校验码回调
interface QNAuthorityActionLiveCodeCallback {
    //活体检测校验码回调
    void onResult(QNFaceActLiveCode faceActLiveCode);
}
```

```kotlin
// example:
//创建权威动作
val actionFaceComparer = QNAuthorityActionFaceComparer.create()
//获取校验码
actionFaceComparer.getRequestCode(
    QNFaceActLiveCodeParam()
) { code ->
    actionFaceComparer.start(
        localVideoTrack,
        QNFaceActLiveParam().apply {
            //设置校验码
            sessionID = code.result.sessionID
        },
        QNAuthoritativeFaceParam().apply {
            realName = name //设置名字
            idCard = idcard //设置身份证号码
        }
    )
    GlobalScope.launch(Dispatchers.Main) {
        tvTip.visibility = View.VISIBLE
        tvTip.text = "准备动作"
        //对每个动作给两秒提示让用户完成动作
        code.result.faceActions.forEach {
            delay(2000)
            tvTip.text = it.getTip()
        }
        //提交结果
        actionFaceComparer.commit { faceActLive, authoritativeFace ->

        }
    }
}
```

### 文转音

```java
//文转音
class QNTextToSpeakAnalyzer {
    // 开始文字转语音
    static void run(QNTTSParam textToSpeakParam,   //参数
                    QNTextToSpeakCallback callback);
}

//文转音回调函数
interface QNTextToSpeakCallback {
    // 结果回调
    void onResult(QNTextToSpeak textToSpeak);
}

//文字转语音参数
class QNTTSParam {
    //需要进行语音合成的文本内容，最短1个字
    String content;
    //TTS 发音人标识音源 id 0-6,实际可用范围根据情况, 可以不设置,默认是 0; 其中0：女声（柔和）；1，女声（正式）；2，女生（柔和带正式）；3：男声（柔和），4：男声（柔和带正式）；5：男声（闽南话）；6：女生（闽南话）。
    int spkID = 0;
    // 音量大小，取值范围为 0.75 - 1.25，默认为1
    double volume = 1;
    // 语速，取值范围为 0.75 - 1.25，默认为1
    double speed = 1;
    /**
     * 可不填，不填时默认为 3。
     * audioType=3 返回 16K 采样率的 mp3
     * audioType=4 返回 8K 采样率的 mp3
     * audioType=5 返回 24K 采样率的 mp3
     * audioType=6 返回 48k采样率的mp3
     * audioType=7 返回 16K 采样率的 pcm 格式
     * audioType=8 返回 8K 采样率的 pcm 格式
     * audioType=9 返回 24k 采样率的pcm格式
     * audioType=10 返回 8K 采样率的 wav 格式
     * audioType=11 返回 16K 采样率的 wav 格式
     */
    int audioType = 3;
}

// 文转音结果
class QNTextToSpeak {
    int errorCode;  // 请求返回码
    String errorMsg;// 提示
    String requestID;// 请求ID
    Result result;  // 结果

    static class Result {
        String audioUrl;  // 播放地址
    }
}
```

```kotlin
val param = QNTTSParam().apply {
    content = etText //设置文本
}
QNTextToSpeakAnalyzer.run() {
    textToSpeak ->
    //播放结果
    play(textToSpeak.result?.audioUrl)
}
```

### 初始化

```java
class QNRtcAISdkManager {

    //初始化
    static void init(Context appContext);

    //如果过期可以单独重置token
    static void setToken(String aiToken);

    //设置url(实时语音识别)签名回调
    static void setSignCallback(SignCallback signCallback);

    interface SignCallback {
        // 将url签名成token
        String signUrlToToken(String url);
    }
}
```

### 错误码

```c
业务错误码	信息

//通用:
0	成功
1000	未知异常
1001	音频/视频轨道没有数据返回
1002	音频/视频数据异常

//语音转文字:
2000	网络异常连接中断

//身份证识别:
53090001	请求解析失败
53090002	图片解码错误
53090003	OCR 内部错误
53090004	无法识别的身份证(非中国身份证等)
53090005	参数错误
55060030	鉴权失败
53091001	黑白复印件
53091003	无法检测到人脸
53091004	证件信息缺失或错误
53091005	证件过期
53091006	身份证不完整

//人脸检测:
55060001	请求字段有非法传输
55060002	图片解码失败
55060006	人脸特征提取失败
55060018	人脸配准失败
55060019	人脸检测图片 Base64 解码失败
55060033	人脸图片无效

//人脸对比:
55060001	请求字段有非法传输
55060002	图片解码失败
55060028	人脸比对图片 A Base64 解码失败
55060029	人脸比对图片 B Base64 解码失败
55060040	图片A人脸检测失败
55060041	图片B人脸检测失败

// 动作活体检测:
55060001	请求字段有非法传输
55060002	图片解码失败
55060012	点头动作检测失败
55060013	摇头动作检测失败
55060014	眨眼动作检测失败
55060015	张嘴动作检测失败
55060016	不是活体
55060024	视频帧率过低
55060016	动作类型无效

//光线活体
55060001    请求字段有非法传输
55060002	图片解码失败
55060009	视频无效
55060011	视频中人脸检测失败
55060016	不是活体

//文转音
100        请求参数缺失
101        请求参数不合法，⽐如合成⽂本过⻓
102        服务不可⽤
103        语⾳合成错误

//权威人脸

55060001	ERROR_PARAMETER_INVALID	请求字段有非法传输
55060004	FACE_DETECT_FAILED	高清照人脸检测失败
55060006	FEATURE_EXTRACT_FAILED	人脸特征提取失败
55060019	IMAGE_BASE64_DECODE_FAILED	人脸检测图片 Base64 解码失败
55060029	FACE_IDENTIFY_FAILED	人脸鉴别失败
55060044	REALNAME_FORMAT_ERROR	姓名格式不正确
55060045	IDCARD_NUMBER_ERROR	身份证号码有误
55060046	PHOTO_SIZE_NOT_SUITABLE	照片大小不在1kb-30kb的范围内
55060047	AUTH_INFORMATION_NOT_EXISTED	认证信息不存在
55060048	IDCARD_PHOTO_NOT_EXISTED	证件照不存在
55060049	PHOTO_NOT_ACCEPTED	照片质量检验不合格
55060050	PHOTO_MULTIFACE_DETECTED	照片出现多张人脸
```