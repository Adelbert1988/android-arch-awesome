package com.guadou.lib_baselib.utils.log.interceptor

import android.util.Log
import com.guadou.lib_baselib.utils.log.LogInterceptChain

/**
 * 使用Android Log 打印日志
 */
open class LogPrintInterceptor(private val isEnable: Boolean) : LogInterceptChain() {

    override fun intercept(priority: Int, tag: String, logMsg: String?) {

        if (isEnable) {
            Log.println(priority, tag, logMsg ?: "-")
        }

        super.intercept(priority, tag, logMsg)
    }


}