package com.guadou.lib_baselib.view.web

import android.content.Context
import android.content.MutableContextWrapper
import android.view.ViewGroup
import java.util.*

/**
 * WebViewPool与WebViewPoolManager两种缓存类，选择一种即可
 */
class WebViewPool private constructor() {

    companion object {

        @Volatile
        private var instance: WebViewPool? = null

        fun getInstance(): WebViewPool {
            return instance ?: synchronized(this) {
                instance ?: WebViewPool().also { instance = it }
            }
        }
    }

    private val sPool = Stack<MyWebView>()
    private val lock = byteArrayOf()
    private var maxSize = 1

    /**
     * 设置 webview 池容量
     */
    fun setMaxPoolSize(size: Int) {
        synchronized(lock) { maxSize = size }
    }

    /**
     * 初始化webview 放在list中
     */
    fun init(context: Context, initSize: Int = maxSize) {
        for (i in 0 until initSize) {
            val view = MyWebView(MutableContextWrapper(context))
            sPool.push(view)
        }
    }

    /**
     * 获取webview
     */
    fun getWebView(context: Context): MyWebView {
        synchronized(lock) {
            val webView: MyWebView
            if (sPool.size > 0) {
                webView = sPool.pop()
            } else {
                webView = MyWebView(MutableContextWrapper(context))
            }

            val contextWrapper = webView.context as MutableContextWrapper
            contextWrapper.baseContext = context

            webView.clearHistory()
            webView.resumeTimers()

            return webView
        }
    }

    /**
     * 回收 WebView
     */
    fun recycle(webView: MyWebView) {

        try {
            webView.stopLoading()
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null)
            webView.clearHistory()
            webView.pauseTimers()
            webView.clearFormData()
            webView.removeJavascriptInterface("webkit")

            // 重置并回收当前的上下文对象，根据池容量判断是否销毁，也可以置换为ApplicationContext
            val contextWrapper = webView.context as MutableContextWrapper
            contextWrapper.baseContext = webView.context.applicationContext

            val parent = webView.parent
            if (parent != null) {
                (parent as ViewGroup).removeView(webView)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

            synchronized(lock) {
                if (sPool.size < maxSize) {
                    sPool.push(webView)
                } else {
                    webView.destroy()
                }
            }
        }
    }

}