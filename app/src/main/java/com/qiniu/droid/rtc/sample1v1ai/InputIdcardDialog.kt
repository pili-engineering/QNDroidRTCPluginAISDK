package com.qiniu.droid.rtc.sample1v1ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment


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
        view.findViewById<View>(R.id.btOk).setOnClickListener {
            val name =  view.findViewById<EditText>(R.id.etName).text.toString()
            val card =  view.findViewById<EditText>(R.id.etIdCard).text.toString()
            call.invoke(name,card)
            dismiss()
        }
    }
}