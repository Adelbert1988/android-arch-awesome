package com.guadou.lib_baselib.utils.track

import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp拦截器的方式获取网络请求信息，保存起来
 */
class TrackInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val callId = chain.request().tag() as? Long
        callId?.let {
            TrackEventListener.put(it, "code", response.code())
            TrackEventListener.put(it, "protocol", response.protocol())
            TrackEventListener.put(it, "url", response.request().url())
            TrackEventListener.put(it, "method", response.request().method())
        }

        return response
    }
}
