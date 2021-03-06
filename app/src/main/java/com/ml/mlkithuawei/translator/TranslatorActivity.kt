package com.ml.mlkithuawei.translator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlplugin.asr.MLAsrCaptureActivity
import com.huawei.hms.mlplugin.asr.MLAsrCaptureConstants
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslateSetting
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslator
import com.huawei.hms.mlsdk.tts.*
import com.ml.mlkithuawei.R
import com.ml.mlkithuawei.databinding.ActivityTranslatorBinding


class TranslatorActivity: AppCompatActivity() {

    private lateinit var translatorBinding: ActivityTranslatorBinding
    private lateinit var translatorViewModel: TranslatorViewModel
    private val TAG: String = TranslatorActivity::class.java.simpleName
    private var API_KEY: String = "client/api_key"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApiKey()
        requestPermissions()

        translatorBinding = DataBindingUtil.setContentView(this, R.layout.activity_translator)
        translatorBinding.lifecycleOwner = this
        translatorViewModel = ViewModelProvider(this).get(TranslatorViewModel::class.java)
        translatorBinding.translatorViewModel = translatorViewModel
        supportActionBar?.title = getString(R.string.actionbar_name)
        translatorViewModel.translate.observe(this, Observer {
            startASR()
        })
    }

    private fun startASR() {
        val intent = Intent(this, MLAsrCaptureActivity::class.java)
            .putExtra(MLAsrCaptureConstants.LANGUAGE, "es-ES")
//            .putExtra(MLAsrCaptureConstants.LANGUAGE, "en-US")
            .putExtra(MLAsrCaptureConstants.FEATURE, MLAsrCaptureConstants.FEATURE_WORDFLUX)
        startActivityForResult(intent, 100)
    }

    private fun requestPermissions() {
        val permission = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA)

        ActivityCompat.requestPermissions(this, permission,1)
    }

    private fun setApiKey(){
        MLApplication.getInstance().apiKey = "CwEAAAAAXnKyG7Xgne5scjB5EGLLAYuff7Hclseq9fekDMH+FUfdQmXPDTwhnoT9Hkg3kGiMD1kuayk6gvuvHeLSj7Bz8hkLw8c="
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            when (resultCode) {
                MLAsrCaptureConstants.ASR_SUCCESS -> if (data != null) {
                    val bundle = data.extras
                    if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_RESULT)) {
                        val text = bundle.getString(MLAsrCaptureConstants.ASR_RESULT).toString()
                        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
                        startTranslate(text)
                    }
                }
                MLAsrCaptureConstants.ASR_FAILURE -> if (data != null) {
                    val bundle = data.extras
                    if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_ERROR_CODE)) {
                        val errorCode = bundle.getInt(MLAsrCaptureConstants.ASR_ERROR_CODE)
                        Log.d(TAG, errorCode.toString())
                    }
                    if (bundle != null && bundle.containsKey(MLAsrCaptureConstants.ASR_ERROR_MESSAGE)) {
                        val errorMsg = bundle.getString(MLAsrCaptureConstants.ASR_ERROR_MESSAGE)
                        Toast.makeText(this, "Error Code $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
                else -> {
                    Toast.makeText(this, "Failed to get data", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun startTranslate(sourceText: String){
        val setting: MLRemoteTranslateSetting =
            MLRemoteTranslateSetting.Factory()
                .setSourceLangCode("en")
                .setTargetLangCode("zh")
                .create()
        val mlRemoteTranslator: MLRemoteTranslator =
            MLTranslatorFactory.getInstance().getRemoteTranslator(setting)

        if(!TextUtils.isEmpty(sourceText)){
            val task: Task<String> =
                mlRemoteTranslator.asyncTranslate(sourceText)
            task.addOnSuccessListener {
                startTTS(it)
            }.addOnFailureListener {
                //Got Error
            }
        } else {
            //Empty string
            Toast.makeText(applicationContext, "Ingresa texto para hablar",Toast.LENGTH_LONG).show()
        }
    }

    private fun startTTS(translatedText: String){
        val mlConfigs = MLTtsConfig()
            .setLanguage(MLTtsConstants.TTS_ZH_HANS)
            .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_ZH)
            .setSpeed(1.0f)
            .setVolume(1.0f)
        val mlTtsEngine = MLTtsEngine(mlConfigs)
        mlTtsEngine.setTtsCallback(callback)
        mlTtsEngine.speak(translatedText, MLTtsEngine.QUEUE_APPEND)
    }

    private var callback: MLTtsCallback = object :  MLTtsCallback {
        override fun onError(taskId: String, err: MLTtsError) {

        }

        override fun onWarn(taskId: String, warn: MLTtsWarn) {

        }

        override fun onRangeStart(taskId: String, start: Int, end: Int) {

        }

        override fun onAudioAvailable(p0: String?, p1: MLTtsAudioFragment?, p2: Int, p3: android.util.Pair<Int, Int>?, p4: Bundle?) {

        }

        override fun onEvent(taskId: String, eventName: Int, bundle: Bundle?) {
            if (eventName == MLTtsConstants.EVENT_PLAY_STOP) {
                val isStop = bundle?.getBoolean(MLTtsConstants.EVENT_PLAY_STOP_INTERRUPTED)
                if (isStop!!)
                    Log.d(TAG, "Manually stopped")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(
                        this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            val intent = Intent(context, TranslatorActivity::class.java)
            return intent
        }
    }
}