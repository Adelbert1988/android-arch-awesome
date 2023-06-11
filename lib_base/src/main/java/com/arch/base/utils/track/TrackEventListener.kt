package com.guadou.lib_baselib.utils.track

import android.os.Build
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.Protocol
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.ConcurrentLinkedQueue

class TrackEventListener(private val callId: Long?) : EventListener() {

    private var callStartMillis: Long? = null // 请求开始毫秒时
    private var dnsStartMillis: Long? = null // dns开始毫秒时
    private var tcpConnectStartMillis: Long? = null // tcp连接开始毫秒时
    private var tlsConnectStartMillis: Long? = null // tls连接开始毫秒时
    private var callDuration = 0L // 请求耗时
    private var dnsDuration = 0L // dns耗时
    private var tcpDuration = 0L // tcp耗时
    private var tlsDuration = 0L // tls耗时

    companion object {
        // 数据容器 CopyOnWriteArrayList 容器
        // 它是线程安全的。写数据操作上锁了，即不允许并发写。执行写操作时会将原先数组拷贝一份，并在新数组尾部插入数据，最后将数组引用指向新数组。这样设计的目的是实现“读与写的并发
        // 数据容器 ConcurrentLinkedQueue 容器
        //是一个以单链表为存储介质的线性队列，它是线程安全的。读写操作都没有上锁，可实现真正意义上的并发写，采用 CAS + volatile 实现线程安全
        private val trackers = ConcurrentLinkedQueue<Triple<Long, String, Any>>()

        // 写数据
        fun put(callId: Long?, key: String, value: Any) {
            trackers.add(Triple(callId ?: 0, key, value))
        }

        // 消费数据：读取一个请求的所有数据，并组织成 map
        fun get(callId: Long?): Map<String, Any> =
            trackers.filter { it.first == callId }
                .map { it.second to it.third }
                .let { mapOf(*it.toTypedArray()) }

        // 移除一个请求的所有数据
        fun removeAll(callId: Long?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                trackers.removeIf { it.first == callId }
            } else {
                synchronized(trackers) {
                    trackers.removeAll { it.first == callId }
                }
            }
        }

        // 回调数据给上层的接口
        var networkTrackCallback: NetworkTrackCallback? = null
    }


    private fun finishHandleData() {
        // 将数据回调给上层
        networkTrackCallback?.onCallEnd(get(callId))

        // 移除当前请求的所有数据
        removeAll(callId)
    }

    override fun callStart(call: Call) {
        callStartMillis = System.currentTimeMillis()
    }

    override fun callEnd(call: Call) {
        callStartMillis = callStartMillis ?: System.currentTimeMillis()
        callDuration = System.currentTimeMillis() - callStartMillis!!

        // 写数据
        put(callId, "duration", callDuration)

        //完成数据处理
        finishHandleData()
    }

    override fun callFailed(call: Call, ioe: IOException) {
        callStartMillis = callStartMillis ?: System.currentTimeMillis()
        callDuration = System.currentTimeMillis() - callStartMillis!!

        // 写数据
        put(callId, "duration", callDuration)

        //完成数据处理
        finishHandleData()
    }

    override fun dnsStart(call: Call, domainName: String) {
        dnsStartMillis = System.currentTimeMillis()
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
        dnsStartMillis = dnsStartMillis ?: System.currentTimeMillis()
        dnsDuration = System.currentTimeMillis() - dnsStartMillis!!

        // 写数据
        put(callId, "dnsDuration", dnsDuration)
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        tcpConnectStartMillis = System.currentTimeMillis()
    }

    override fun secureConnectStart(call: Call) {
        tlsConnectStartMillis = tlsConnectStartMillis ?: System.currentTimeMillis()
        tcpDuration = System.currentTimeMillis() - tcpConnectStartMillis!!

        // 写数据
        put(callId, "tcpDuration", tcpDuration)
    }

    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        tlsDuration = System.currentTimeMillis() - tlsConnectStartMillis!!

        // 写数据
        put(callId, "tlsDuration", tlsDuration)
    }

    override fun connectFailed(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?, ioe: IOException) {
        tcpDuration = System.currentTimeMillis() - tcpConnectStartMillis!!

        // 写数据
        put(callId, "tcpDuration", tcpDuration)

        //完成数据处理
        finishHandleData()
    }

    // 网络数据回调
    fun interface NetworkTrackCallback {
        fun onCallEnd(map: Map<String, Any>)
    }
}
