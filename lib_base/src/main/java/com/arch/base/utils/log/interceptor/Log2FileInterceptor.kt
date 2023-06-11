package com.guadou.lib_baselib.utils.log.interceptor

import android.annotation.SuppressLint
import android.os.Handler
import android.os.HandlerThread
import com.guadou.lib_baselib.utils.log.LogInterceptChain
import okio.BufferedSink
import okio.appendingSink
import okio.buffer
import okio.gzip
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 接收上面拦截器传递的数据-把Log信息保存到本地File
 */
class Log2FileInterceptor private constructor(
    private val dir: String, private val isEnable: Boolean
) : LogInterceptChain() {

    private val handlerThread = HandlerThread("log_to_file_thread")
    private val handler: Handler
    private var startTime = System.currentTimeMillis()
    private var bufferedSink: BufferedSink? = null
    private var logFile = File(getFileName())

    val callback = Handler.Callback { message ->
        val sink = checkSink()
        when (message.what) {
            TYPE_FLUSH -> {
                sink.use {
                    it.flush()
                    bufferedSink = null
                }
            }
            TYPE_LOG -> {
                val log = message.obj as String
                sink.writeUtf8(log)
                sink.writeUtf8("\n")
            }
        }
        false
    }

    companion object {
        private const val TYPE_FLUSH = -1
        private const val TYPE_LOG = 1
        private const val FLUSH_LOG_DELAY_MILLIS = 3000L

        @Volatile
        private var INSTANCE: Log2FileInterceptor? = null

        fun getInstance(dir: String, isEnable: Boolean): Log2FileInterceptor = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Log2FileInterceptor(dir, isEnable).apply { INSTANCE = this }
        }
    }

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper, callback)
    }


    override fun intercept(priority: Int, tag: String, logMsg: String?) {

        if (isEnable) {
            if (!handlerThread.isAlive) handlerThread.start()
            handler.run {
                removeMessages(TYPE_FLUSH)
                obtainMessage(TYPE_LOG, "[$tag] $logMsg").sendToTarget()
                val flushMessage = handler.obtainMessage(TYPE_FLUSH)
                sendMessageDelayed(flushMessage, FLUSH_LOG_DELAY_MILLIS)
            }
        }

        super.intercept(priority, tag, logMsg)

    }

    @SuppressLint("SimpleDateFormat")
    private fun getToday(): String =
        SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time)

    private fun getFileName() = "$dir${File.separator}${getToday()}.log"

    private fun checkSink(): BufferedSink {
        if (bufferedSink == null) {
            bufferedSink = logFile.appendingSink().gzip().buffer()
        }
        return bufferedSink!!
    }

}