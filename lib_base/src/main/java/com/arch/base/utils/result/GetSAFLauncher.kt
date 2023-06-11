package com.guadou.lib_baselib.utils.result

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import com.guadou.lib_baselib.ext.commContext
import com.guadou.lib_baselib.ext.toBundle

/**
 * 一般我们用这一个-StartActivityForResult 的 Launcher
 */
class GetSAFLauncher(caller: ActivityResultCaller) :
    BaseResultLauncher<Intent, ActivityResult>(caller, ActivityResultContracts.StartActivityForResult()) {

    //封装另一种Intent的启动方式
    inline fun <reified T> launch(
        bundle: Array<out Pair<String, Any?>>? = null,
        @NonNull callback: ActivityResultCallback<ActivityResult>
    ) {

        val intent = Intent(commContext(), T::class.java).apply {
            if (bundle != null) {
                //调用自己的扩展方法-数组转Bundle
                putExtras(bundle.toBundle()!!)
            }
        }

        launch(intent, null, callback)

    }

}