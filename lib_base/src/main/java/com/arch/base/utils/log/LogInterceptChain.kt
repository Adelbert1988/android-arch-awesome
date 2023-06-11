package com.guadou.lib_baselib.utils.log

abstract class LogInterceptChain {

    var next: LogInterceptChain? = null

    open fun intercept(priority: Int, tag: String, logMsg: String?) {
        next?.intercept(priority, tag, logMsg)
    }

}