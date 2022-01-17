package com.qiniu.droid.rtc.sample1v1ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_input_idcard.*

class InputIdcardDialog : DialogFragment() {


    var call:(name:String,idcard:String)->Unit ={name, idcard ->  }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_input_idcard,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btOk.setOnClickListener {
            val name = etName.text.toString()
            val card = etIdCard.text.toString()
            call.invoke(name,card)
            dismiss()
        }
    }
}