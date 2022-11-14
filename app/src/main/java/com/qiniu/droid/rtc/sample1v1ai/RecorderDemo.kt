package com.qiniu.droid.rtc.sample1v1ai

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.qiniu.droid.rtc.QNCustomVideoTrackConfig
import com.qiniu.droid.rtc.QNRTCClient
import com.qiniu.droid.rtc.QNTrack
import com.qiniu.droid.rtc.QNVideoFormat
import com.qiniu.droid.rtc.ai.core.util.MediaStoreUtils
import com.qiniu.droid.rtc.sample1v1ai.RoomActivity.getScreenHeight
import com.qiniu.droid.rtc.sample1v1ai.RoomActivity.getScreenWidth
import com.qiniu.droid.rtclocalrecord.QNRTCLocalRecordPlugin
import com.qiniu.droid.rtclocalrecord.interfaces.QNLocalRecorderCallback
import com.qiniu.droid.rtclocalrecord.interfaces.QNScreenShareCallback

class RecorderDemo : Fragment() {

    lateinit var client: QNRTCClient
    private var mScreenShareTrack: QNTrack? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_recorder, container, false)
        return v
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btn = view.findViewById<Button>(R.id.recoder)

        btn.setOnClickListener(View.OnClickListener {
            var url = ""
            url = if (Build.VERSION.SDK_INT > 29) {
                requireActivity().getCacheDir().getAbsolutePath()
            } else {
                Environment.getExternalStorageDirectory().absolutePath + "/A"
            }
            if (!btn.isSelected()) {
                QNRTCLocalRecordPlugin.getInstance().startMediaRecorder(
                    client,
                    requireActivity(),
                    url,
                    System.currentTimeMillis().toString() + "",
                    getScreenWidth(),
                    getScreenHeight(),
                    15,
                    1000 * 1000,
                    object : QNLocalRecorderCallback {
                        private var file = ""
                        /**
                         * 启动成功
                         *
                         * @param filePath 录制后的File
                         */
                        /**
                         * 启动成功
                         *
                         * @param filePath 录制后的File
                         */
                        override fun onStart(filePath: String) {
                            Log.d("mjl", "开始录制$filePath")
                            btn.setSelected(true)
                            btn.setText("结束")
                            file = filePath
                            Toast.makeText(requireContext(), "开始录制", Toast.LENGTH_SHORT).show()
                        }
                        /**
                         * 录制完成
                         */
                        /**
                         * 录制完成
                         */
                        override fun onStop() {
                            btn.setSelected(false)
                            if (Build.VERSION.SDK_INT >= 29) {
                                MediaStoreUtils.insertVideoToMediaStore(requireContext(), file)
                            }
                            btn.setText("开始录制")
                            Log.d("mjl", "onStop")
                            Toast.makeText(requireContext(), "完成录制", Toast.LENGTH_SHORT).show()
                        }
                        /**
                         * 失败
                         *
                         * @param code
                         * @param msg
                         */
                        /**
                         * 失败
                         *
                         * @param code
                         * @param msg
                         */
                        override fun onError(code: Int, msg: String) {
                            btn.setSelected(false)
                            btn.setText("开始录制")
                            Log.d("mjl", "onError$msg")
                            Toast.makeText(requireContext(), "录制出错", Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                QNRTCLocalRecordPlugin.getInstance().stopMediaRecorder(requireContext(), client)
            }
        })


        val btnShare = view.findViewById<Button>(R.id.pubScreen)
        btnShare.setOnClickListener(View.OnClickListener {
            if (!btnShare.isSelected()) {
                //创建共享参数
                val config = QNCustomVideoTrackConfig(RoomActivity.TAG_SCREEN)
                    .setVideoEncodeFormat(QNVideoFormat(720, 1280, 20))
                    .setBitrate(1500)
                //创建屏幕共享轨道
                QNRTCLocalRecordPlugin.getInstance().createScreenShareTrack(
                    requireActivity(),
                    config,
                    object : QNScreenShareCallback {
                        override fun onCreateTrack(screenShareTrack: QNTrack) {
                            mScreenShareTrack = screenShareTrack
                            client.publish(mScreenShareTrack)
                            btnShare.setSelected(true)
                            btnShare.setText("取消共享")
                        }

                        override fun onError(code: Int, msg: String) {
                            Log.d(RoomActivity.TAG, "屏幕共享轨道场景创建失败")
                        }
                    })
            } else {
                QNRTCLocalRecordPlugin.getInstance().releaseScreenShareTrack(requireContext())
                client.unpublish(mScreenShareTrack)
                btnShare.setSelected(false)
                btnShare.setText("屏幕共享")
            }
        })
    }

}