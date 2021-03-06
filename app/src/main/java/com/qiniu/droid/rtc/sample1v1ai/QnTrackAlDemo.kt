package com.qiniu.droid.rtc.sample1v1ai

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.hapi.mediapicker.ImagePickCallback
import com.hapi.mediapicker.PicPickHelper
import com.qiniu.droid.rtc.QNTrack
import com.qiniu.droid.rtc.ai.*
import com.qiniu.droid.rtc.ai.audio.QNAudioToText
import com.qiniu.droid.rtc.ai.authoritativeFace.QNAuthoritativeFaceParam
import com.qiniu.droid.rtc.ai.faceCompare.QNFaceCompareParam
import com.qiniu.droid.rtc.ai.faceDetect.QNFaceDetectParam
import com.qiniu.droid.rtc.ai.faceactlive.QNFaceActAction
import com.qiniu.droid.rtc.ai.faceactlive.QNFaceActLiveParam
import com.qiniu.droid.rtc.ai.ocr.OCRDetectParam
import com.qiniu.droid.rtc.ai.orc.QNIDCardDetectParam
import com.qiniu.droid.rtc.ai.tts.QNAudioEncoding
import com.qiniu.droid.rtc.ai.tts.QNTextToSpeak
import com.qiniu.droid.rtc.ai.tts.QNTTSParam
import com.qiniu.util.Json
import kotlinx.android.synthetic.main.fragment_ai_demo.*
import kotlinx.coroutines.*
import java.io.FileOutputStream


class QnTrackAlDemo : Fragment() {

    lateinit var localAudioTrack: QNTrack
    lateinit var localVideoTrack: QNTrack
    var mQNAudioToTextAnalyzer: QNAudioToTextAnalyzer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_ai_demo, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioRcMe.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mQNAudioToTextAnalyzer = QNAudioToTextAnalyzer.start(
                    localAudioTrack,
//                    QNAudioToTextParam().apply {
//                                               hotWords="??????,1;sdasd,2"
//                    },
                    null,
                    object :
                        QNAudioToTextAnalyzer.QNAudioToTextCallback {
                        /**
                         * ????????????
                         */
                        override fun onStart() {}

                        /**
                         * ??????
                         *
                         * @param code ?????????
                         * @param msg  ????????????
                         */
                        override fun onError(code: Int, msg: String?) {
                            audioRcMe.isChecked = false
                        }

                        /**
                         * ??????????????????
                         */
                        override fun onStop() {
                            audioRcMe.isChecked = false
                        }

                        /**
                         * ????????????????????????
                         *
                         * @param audioToText ?????????????????????????????????
                         */
                        override fun onAudioToText(audioToText: QNAudioToText) {
                            tvText.text = audioToText.transcript ?: ""
                        }
                    })
            } else {
                mQNAudioToTextAnalyzer?.stop()
            }
        }

        btFaceLive.setOnClickListener {
            if (llFace.visibility == View.VISIBLE) {
                llFace.visibility = View.GONE
            } else {
                llFace.visibility = View.VISIBLE
            }
        }
        btNob.setOnClickListener {
            faceLive(btNob, "????????????", QNFaceActAction.NOD)
        }
        btShake.setOnClickListener {
            faceLive(btShake, "????????????", QNFaceActAction.SHAKE)
        }
        btBlink.setOnClickListener {
            faceLive(btBlink, "????????????", QNFaceActAction.BLINK)
        }
        btMouth.setOnClickListener {
            faceLive(btMouth, "????????????", QNFaceActAction.MOUTH)
        }

        btnFlashLive.setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                val qnFaceFlashLiveDetector = QNFaceFlashLiveDetector.start(localVideoTrack)
                GlobalScope.launch(Dispatchers.Main) {
                    tvTip.visibility = View.VISIBLE
                    delay(1000)
                    tvTip.text = " 2"
                    delay(1000)
                    tvTip.text = "1"
                    delay(1000)
                    qnFaceFlashLiveDetector.commit {
                        tvText.text = Json.encode(it)
                    }
                    tvTip.visibility = View.GONE
                }
            }
        }

        btOCR.setOnClickListener {
            QNIDCardDetector.run(
                localVideoTrack, QNIDCardDetectParam().apply {
                    retPortrait = true
                    retImage = true
                }
            )
            /**
             * ????????????
             * @param idCardDetect ???????????????
             */
            {
                it.imageResult.idcard = ""
                it.imageResult.portrait = ""
                tvText.text = Json.encode(it)
            }
        }

        btFaceDetect.setOnClickListener {
            QNFaceDetector.run(
                localVideoTrack,
                QNFaceDetectParam()
            )
            /**
             * ??????????????????
             *
             * @param faceDetect ????????????
             */
            {
                it.face?.forEach {
                    it.faceAlignedB64 = ""
                }
                tvText.text = Json.encode(it)
            }
        }

        btFaceCompare.setOnClickListener {
            PicPickHelper(requireActivity()).fromLocal(null, object : ImagePickCallback {
                override fun onSuccess(result: String?, url: Uri?) {
                    url?.let {
                        val bm = MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            url
                        );
                        bm?.let {
                            QNFaceComparer.run(
                                localVideoTrack, bm,
                                QNFaceCompareParam()
                            ) {
                                tvText.text = Json.encode(it)
                            }
                        }
                    }
                }
            })
        }

        btnTTS.setOnClickListener {
            val etText = etTTS.text.toString()
            if (etText.isEmpty()) {
                return@setOnClickListener
            }
            QNTextToSpeakAnalyzer.run(QNTTSParam().apply {
                text = etText
                audioEncoding = QNAudioEncoding.MP3
                volume = 100
            }
            ) {
                tvText.text = "????????????${it.errorCode} ${it.errorMsg}"
                play(it)
            }
            etTTS.setText("")
        }

        btQNAuthoriteActionFaceComparer.setOnClickListener {
            var invokeCall: (name: String, idcard: String) -> Unit = { name, idcard ->
                val tip = "????????????"
                btQNAuthoriteActionFaceComparer.isClickable = false
                tip.asToast()
                tvTip.text = "${tip} 3"
                tvTip.visibility = View.VISIBLE
                val param = QNFaceActLiveParam()
                param.actionTypes = listOf(QNFaceActAction.SHAKE)
                val qnAuthoriteActionFaceComparer =
                    QNAuthorityActionFaceComparer.start(localVideoTrack, param,
                        QNAuthoritativeFaceParam().apply {
                            realName = name
                            idCard = idcard
                        }
                    )
                GlobalScope.launch(Dispatchers.Main) {
                    tvTip.text = "${tip} 2"
                    delay(1000)
                    tvTip.text = "${tip} 1"
                    delay(800)
                    tvTip.text = ""
                    btQNAuthoriteActionFaceComparer.isClickable = true
                    Log.d("faceLive", "faceLive start")
                    qnAuthoriteActionFaceComparer.commit { faceActLive, authoritativeFace ->
                        faceActLive.bestFrames?.forEach {
                            it.imageB64 = "??????"
                        }
                        Log.d("faceLive", "faceLive stop")
                        tvText.text =
                            Json.encode(faceActLive) + "\n" + Json.encode(authoritativeFace)
                    }
                }
            }
            InputIdcardDialog().apply {
                call = { name, idcard ->
                    invokeCall.invoke(name, idcard)
                }

            }.show(childFragmentManager, "")
        }

        btnQWFace.setOnClickListener {
            var invokeCall: (name: String, idcard: String) -> Unit = { name, idcard ->
                QNAuthoritativeFaceComparer.run(localVideoTrack, QNAuthoritativeFaceParam().apply {
                    realName = name
                    idCard = idcard
                }
                ) {
                    tvText.text = Json.encode(it)
                }
            }
            InputIdcardDialog().apply {
                call = { name, idcard ->
                    invokeCall.invoke(name, idcard)
                }
            }.show(childFragmentManager, "")
        }

        btorc.setOnClickListener {
            QNOCRDetector.run(localVideoTrack, OCRDetectParam().apply {
                // ???????????????????????????
                // ??????????????????????????? ????????????????????????????????????,?????????????????????
                isMirror =true
            }, QNOCRDetector.QNOCRDetectorCallback {
                tvText.text = Json.encode(it)
            })
        }
    }

    private fun faceLive(bt: Button, tip: String, faceActAction: QNFaceActAction) {
        bt.isClickable = false
        tip.asToast()
        tvTip.text = "${tip} 3"
        tvTip.visibility = View.VISIBLE
        val param = QNFaceActLiveParam()
        param.actionTypes = listOf(faceActAction)
        val qnFaceActionLiveDetector = QNFaceActionLiveDetector.start(localVideoTrack, param)
        GlobalScope.launch(Dispatchers.Main) {
            tvTip.text = "${tip} 2"
            delay(1000)
            tvTip.text = "${tip} 1"
            delay(800)
            tvTip.text = ""
            bt.isClickable = true
            Log.d("faceLive", "faceLive start")
            qnFaceActionLiveDetector.commit {
                it.bestFrames?.forEach {
                    it.imageB64 = "??????"
                }
                Log.d("faceLive", "faceLive stop")
                tvText.text = Json.encode(it)
            }
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
        textToSpeak.audioBytes ?: return

        val audioFilePath =
            requireActivity().cacheDir.absolutePath + "/${System.currentTimeMillis()}.mp3"
        val outputStream = FileOutputStream(audioFilePath)
        try {
            outputStream.write(textToSpeak.audioBytes)
            outputStream.flush()
            outputStream.close()
            mMediaPlayer.reset()
            mMediaPlayer.setDataSource(
                audioFilePath
            )
            mMediaPlayer.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}