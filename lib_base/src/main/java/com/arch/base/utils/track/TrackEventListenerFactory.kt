package com.guadou.lib_baselib.utils.track

import okhttp3.Call
import okhttp3.EventListener

/**
 * 每个请求申请独立的事件监听器
 */
object TrackEventListenerFactory : EventListener.Factory {
    override fun create(call: Call): EventListener {
        val callId = call.request().tag() as? Long // 获取请求id
        return TrackEventListener(callId) // 将请求id传递给事件监听器
    }
}