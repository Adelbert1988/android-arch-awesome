package com.guadou.lib_baselib.utils.track


import okhttp3.Call
import okhttp3.Request
import java.util.concurrent.atomic.AtomicLong

/**
 * 用于包装OkhttpClient实例，内部绑定tag-callid
 */
class TrackCallFactory(private val factory: Call.Factory) : Call.Factory {
    private val callId = AtomicLong(1L)  // 唯一标识一个请求
    override fun newCall(request: Request): Call {
        val id = callId.getAndIncrement()  // 获取新请求id
        // 重构 Request 实例，并通过tag方式带上请求id
        val newRequest = request.newBuilder().tag(id).build()
        // 将新请求传递给被装饰的 factory
        return factory.newCall(newRequest)
    }
}
