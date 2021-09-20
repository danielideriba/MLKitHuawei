package com.ml.mlkithuawei

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val setting = MLFaceAnalyzerSetting.Factory()
            .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
            .setPerformanceType(MLFaceAnalyzerSetting.TYPE_SPEED)
            .allowTracing()
            .create()
        var analyzer = MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting)
    }
}