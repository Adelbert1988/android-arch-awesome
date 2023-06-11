package com.guadou.lib_baselib.utils.log

/**
 *
 */
class LogInterceptChainHandler {

    private var _interceptFirst: LogInterceptChain? = null

    fun add(interceptChain: LogInterceptChain?) {
        if (_interceptFirst == null) {
            _interceptFirst = interceptChain
            return
        }

        var node: LogInterceptChain? = _interceptFirst

        while (true) {
            if (node?.next == null) {
                node?.next = interceptChain
                break
            }

            node = node.next
        }
    }

    fun intercept(priority: Int, tag: String, logMsg: String?) {
        _interceptFirst?.intercept(priority, tag, logMsg)
    }

}