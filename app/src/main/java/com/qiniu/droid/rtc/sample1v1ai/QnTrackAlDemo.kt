package com.qiniu.droid.rtc.sample1v1ai

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hapi.mediapicker.ImagePickCallback
import com.hapi.mediapicker.PicPickHelper
import com.qiniu.droid.rtc.QNTrack
import com.qiniu.droid.rtc.ai.*
import com.qiniu.droid.rtc.ai.audio.QNAudioToText
import com.qiniu.droid.rtc.ai.audio.QNAudioToTextParam
import com.qiniu.droid.rtc.ai.authoritativeFace.QNAuthoritativeFaceParam
import com.qiniu.droid.rtc.ai.core.util.JsonUtils
import com.qiniu.droid.rtc.ai.core.util.QNBitMapUtil
import com.qiniu.droid.rtc.ai.faceCompare.QNFaceCompareParam
import com.qiniu.droid.rtc.ai.faceDetect.QNFaceDetectParam
import com.qiniu.droid.rtc.ai.faceactlive.QNFaceActAction
import com.qiniu.droid.rtc.ai.faceactlive.QNFaceActLiveCode
import com.qiniu.droid.rtc.ai.faceactlive.QNFaceActLiveCodeParam
import com.qiniu.droid.rtc.ai.faceactlive.QNFaceActLiveParam
import com.qiniu.droid.rtc.ai.ocr.OCRDetect
import com.qiniu.droid.rtc.ai.ocr.OCRDetectParam
import com.qiniu.droid.rtc.ai.orc.QNIDCardDetectParam
import com.qiniu.droid.rtc.ai.tts.QNTextToSpeak
import com.qiniu.droid.rtc.ai.tts.QNTTSParam
import com.qiniu.droid.rtc.sample1v1ai.databinding.FragmentAiDemoBinding
import com.qiniu.util.Json

import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream


class QnTrackAlDemo : Fragment() {

    lateinit var localAudioTrack: QNTrack
    lateinit var localVideoTrack: QNTrack
    var mQNAudioToTextAnalyzer: QNAudioToTextAnalyzer? = null
    private lateinit var binding: FragmentAiDemoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_ai_demo, container, false)
        binding = FragmentAiDemoBinding.bind(v)
        return v
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.audioRcMe.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mQNAudioToTextAnalyzer = QNAudioToTextAnalyzer.start(
                    localAudioTrack,
                    QNAudioToTextParam().apply {
                        voiceID = System.currentTimeMillis().toString()
                    }, object : QNAudioToTextAnalyzer.QNAudioToTextCallback {
                        /**
                         * 开始成功
                         */
                        override fun onStart() {}

                        /**
                         * 错误
                         *
                         * @param code 错误码
                         * @param msg  错误提示
                         */
                        override fun onError(code: Int, msg: String?) {
                            binding.audioRcMe.isChecked = false
                        }

                        /**
                         * 实时转化结束
                         */
                        override fun onStop() {
                            binding.audioRcMe?.isChecked = false
                        }

                        /**
                         * 实时转化文字数据
                         *
                         * @param audioToText 当前片段的结果文字数据
                         */
                        override fun onAudioToText(audioToText: QNAudioToText) {
                            binding.tvText.text = JsonUtils.toJson(audioToText)
                        }
                    })
            } else {
                mQNAudioToTextAnalyzer?.stop()
            }
        }

        binding.btFaceLive.setOnClickListener {

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
                    binding.tvTip.visibility = View.VISIBLE
                    binding.tvTip.text = "准备动作"
                    //对校验码里的每个动作显示提示
                    code.result.faceActions.forEach {
                        //等待x秒-用户完成动作
                        binding.tvTip.text = it.getTip()
                        delay(2000)

                    }
                    //提交获取结果
                    actionFaceComparer.commit { faceActLive ->
                        binding.tvTip.text = ""
                        binding.tvText.text =
                            faceActLive.errorCode.toString() + " " + faceActLive.errorMsg + "actionVerify:" + faceActLive.result?.actionVerify + "" + " score:" + faceActLive.result?.score
                    }
                }
            }
        }

        binding.btOCR.setOnClickListener {
            QNIDCardDetector.run(
                localVideoTrack, QNIDCardDetectParam().apply {
                    retPortrait = true
                    retImage = true
                }
            )
            /**
             * 结果回调
             * @param idCardDetect 身份证数据
             */
            {
                it.imageResult.idcard = ""
                it.imageResult.portrait = ""
                binding.tvText.text = Json.encode(it)
            }
        }

        binding.btFaceDetect.setOnClickListener {
            QNFaceDetector.run(
                localVideoTrack,
                QNFaceDetectParam()
            )
            /**
             * 人脸检测结果
             *
             * @param faceDetect 人脸数据
             */
            {
                binding.tvText.text = Json.encode(it)
            }
        }

        binding.btFaceCompare.setOnClickListener {
            PicPickHelper(requireActivity() as AppCompatActivity).fromLocal(
                null,
                object : ImagePickCallback {
                    override fun onSuccess(result: String?, url: Uri?) {
                        result?.let {
                            QNFaceComparer.run(
                                localVideoTrack, Uri.fromFile(File(result)),
                                QNFaceCompareParam(), QNFaceCompareParam()
                            ) {
                                binding.tvText.text = Json.encode(it)
                            }
                        }
                    }
                })
        }

        binding.btnTTS.setOnClickListener {
            val etText = binding.etTTS.text.toString()
            if (etText.isEmpty()) {
                return@setOnClickListener
            }
            QNTextToSpeakAnalyzer.run(QNTTSParam().apply {
                content = etText
            }) {
                binding.tvText.text = "文转音：${it.errorCode} ${it.errorMsg}"
                play(it)
            }
            binding.etTTS.setText("")
        }

        binding.btQNAuthoriteActionFaceComparer.setOnClickListener {
            val invokeCall: (name: String, idcard: String) -> Unit = { name, idcard ->

                //创建权威动作
                val actionFaceComparer = QNAuthorityActionFaceComparer.create()
                actionFaceComparer.getRequestCode(
                    QNFaceActLiveCodeParam()
                ) { code ->
                    actionFaceComparer.start(
                        localVideoTrack,
                        QNFaceActLiveParam().apply {
                            sessionID = code.result.sessionID
                        },
                        QNAuthoritativeFaceParam().apply {
                            realName = name
                            idCard = idcard
                        }
                    )
                    GlobalScope.launch(Dispatchers.Main) {
                        binding.tvTip.visibility = View.VISIBLE
                        binding.tvTip.text = "准备动作"
                        code.result.faceActions.forEach {
                            binding.tvTip.text = it.getTip()
                            delay(2000)
                        }
                        actionFaceComparer.commit { faceActLive, authoritativeFace ->
                            binding.tvText.text = faceActLive.errorCode.toString() + " " +
                                    "" + faceActLive.errorMsg + " " +
                                    "score:" + faceActLive.result?.score + "" +
                                    "actionVerify:" + faceActLive.result?.actionVerify + "" +
                                    "/" + JsonUtils.toJson(authoritativeFace)

                            binding.tvTip.text = ""
                        }
                    }
                }
            }
            InputIdcardDialog().apply {
                call = { name, idcard ->
                    invokeCall.invoke(name, idcard)
                }

            }.show(childFragmentManager, "")
        }

        binding.btnQWFace.setOnClickListener {
            val invokeCall: (name: String, idcard: String) -> Unit = { name, idcard ->
                QNAuthoritativeFaceComparer.run(localVideoTrack, QNAuthoritativeFaceParam().apply {
                    realName = name
                    idCard = idcard
                }
                ) {
                    binding.tvText.text = Json.encode(it)
                }
            }
            InputIdcardDialog().apply {
                call = { name, idcard ->
                    invokeCall.invoke(name, idcard)
                }
            }.show(childFragmentManager, "")
        }

        binding.btOCR2.setOnClickListener {
            QNOCRDetector.run(
                localVideoTrack, OCRDetectParam()
            ) {
                binding.tvText.text = Json.encode(it)
            }
        }
        QNBitMapUtil.prevImg = binding.prevImg
    }

    private fun QNFaceActAction.getTip(): String {
        return when (this) {
            QNFaceActAction.BLINK -> "请眨眨眼"
            QNFaceActAction.LIFT_UP -> "请抬抬头"
            QNFaceActAction.LOW_DOWN -> "请低头"
            QNFaceActAction.SHAKE -> "请左右摇头"
        }
    }

    fun String.asToast() {
        Toast.makeText(requireContext(), this, Toast.LENGTH_SHORT).show()
    }

    private val mMediaPlayer by lazy {
        val mp = MediaPlayer()
        mp.setOnPreparedListener {
            android.util.Log.d("mMediaPlayer", "OnPrepared")
            mp.start()
        }
        mp.setOnInfoListener { mp, what, extra ->
            Log.d("mMediaPlayer", "OnInfoListener ${what} ${extra}")
            true
        }
        mp.setOnErrorListener { mp, what, extra ->
            Log.d("mMediaPlayer", "OnError ${what} ${extra} ")
            true
        }
        mp
    }

    private fun play(textToSpeak: QNTextToSpeak) {
        if (TextUtils.isEmpty(textToSpeak.result?.audioUrl)) {
            return
        }
        try {
            mMediaPlayer.setDataSource(
                textToSpeak.result?.audioUrl ?: ""
            )
            mMediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}