package com.ml.mlkithuawei.translator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TranslatorViewModel: ViewModel() {
    var translate = MutableLiveData<Int>()

    fun callTranslator(){
        translate.value = 1
    }
}