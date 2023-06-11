package com.guadou.lib_baselib.utils.log

import android.text.TextUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import java.net.UnknownHostException
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

object YYLogUtils {

    var LINE_SEPARATOR = System.getProperty("line.separator")
    private const val DEBUG = 3
    private const val INFO = 4
    private const val WARN = 5
    private const val ERROR = 6

    val intercepts = LogInterceptChainHandler()

    @JvmStatic
    fun d(message: String, tag: String = "/", vararg args: Any) {
        log(DEBUG, message, tag, *args)
    }

    @JvmStatic
    fun d(message: String) {
        d(message, "/")
    }

    @JvmStatic
    fun e(message: String, tag: String = "/", vararg args: Any, throwable: Throwable? = null) {
        log(ERROR, message, tag, *args, throwable = throwable)
    }

    @JvmStatic
    fun e(message: String) {
        e(message, "/")
    }

    @JvmStatic
    fun w(message: String, tag: String = "/", vararg args: Any) {
        log(WARN, message, tag, *args)
    }

    @JvmStatic
    fun w(message: String) {
        w(message, "/")
    }

    @JvmStatic
    fun i(message: String, tag: String = "/", vararg args: Any) {
        log(INFO, message, tag, *args)
    }

    @JvmStatic
    fun i(message: String) {
        i(message, "/")
    }

    @JvmStatic
    fun json(json: String) {
        if (TextUtils.isEmpty(json)) {
            e("json 数据为空！")
            return
        }
        try {
            var message = ""
            if (json.startsWith("{")) {
                val jo = JSONObject(json)
                message = jo.toString(4)
            } else if (json.startsWith("[")) {
                val ja = JSONArray(json)
                message = ja.toString(4)
            }
            e(message)
        } catch (e: Exception) {
            e(e.cause!!.message + LINE_SEPARATOR + json)
        }
    }

    @JvmStatic
    fun xml(xml: String) {
        if (TextUtils.isEmpty(xml)) {
            e("xml 数据为空！")
            return
        }
        try {
            val xmlInput: Source = StreamSource(StringReader(xml))
            val xmlOutput = StreamResult(StringWriter())
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            transformer.transform(xmlInput, xmlOutput)
            val message =
                xmlOutput.writer.toString().replaceFirst(">".toRegex(), ">$LINE_SEPARATOR")
            e(message)
        } catch (e: TransformerException) {
            e(e.cause!!.message + LINE_SEPARATOR + xml)
        }
    }

    fun addInterceptor(interceptor: LogInterceptChain) {
        intercepts.add(interceptor)
    }

    @Synchronized
    private fun log(
        priority: Int,
        message: String,
        tag: String,
        vararg args: Any,
        throwable: Throwable? = null
    ) {
        var logMessage = message.format(*args)
        if (throwable != null) {
            logMessage += getStackTraceString(throwable)
        }

        intercepts.intercept(priority, tag, logMessage)
    }

    fun String.format(vararg args: Any) =
        if (args.isNullOrEmpty()) this else String.format(this, *args)

    private fun getStackTraceString(tr: Throwable?): String {
        if (tr == null) {
            return ""
        }
        var t = tr
        while (t != null) {
            if (t is UnknownHostException) {
                return ""
            }
            t = t.cause
        }
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }
}