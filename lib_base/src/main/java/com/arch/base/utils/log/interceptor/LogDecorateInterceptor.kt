package com.guadou.lib_baselib.utils.log.interceptor

import com.guadou.lib_baselib.utils.log.LogInterceptChain
import com.guadou.lib_baselib.utils.log.LogInterceptChainHandler
import com.guadou.lib_baselib.utils.log.YYLogUtils

/**
 * 对Log进行装饰，美化打印效果
 * 打印Log之前 封装打印的线程-打印的位置
 * 具体的打印输出由其他拦截器负责
 */
class LogDecorateInterceptor(private val isEnable: Boolean) : LogInterceptChain() {

    companion object {

        private const val TOP_LEFT_CORNER = '┏'
        private const val BOTTOM_LEFT_CORNER = '┗'
        private const val MIDDLE_CORNER = '┠'
        private const val LEFT_BORDER = '┃'
        private const val DOUBLE_DIVIDER = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        private const val SINGLE_DIVIDER = "──────────────────────────────────────────────────────"

        private const val TOP_BORDER = TOP_LEFT_CORNER.toString() + DOUBLE_DIVIDER
        private const val BOTTOM_BORDER = BOTTOM_LEFT_CORNER.toString() + DOUBLE_DIVIDER
        private const val MIDDLE_BORDER = MIDDLE_CORNER.toString() + SINGLE_DIVIDER

        private val blackList = listOf(
            LogDecorateInterceptor::class.java.name,
            YYLogUtils::class.java.name,
            LogPrintInterceptor::class.java.name,
            Log2FileInterceptor::class.java.name,
            LogInterceptChain::class.java.name,
            LogInterceptChainHandler::class.java.name,
        )
    }

    override fun intercept(priority: Int, tag: String, logMsg: String?) {
        if (isEnable) {
            super.intercept(priority, tag, TOP_BORDER)

            super.intercept(priority, tag, "$LEFT_BORDER [Thread] → " + Thread.currentThread().name)

            super.intercept(priority, tag, MIDDLE_BORDER)

            printStackInfo(priority, tag)

            super.intercept(priority, tag, MIDDLE_BORDER)

            super.intercept(priority, tag, "$LEFT_BORDER $logMsg")

            super.intercept(priority, tag, BOTTOM_BORDER)
        }else{
            super.intercept(priority, tag, logMsg)
        }

    }

    //获取调用栈信息
    private fun printStackInfo(priority: Int, tag: String) {
        var str = ""
        var line = 0
        val traces = Thread.currentThread().stackTrace.drop(3)
        for (i in traces.indices) {
            if (line >= 3) return   //这里只打印三行

            val element = traces[i]
            val perTrace = java.lang.StringBuilder(str)
            if (element.isNativeMethod) {
                continue
            }
            val className = element.className
            if (className.startsWith("android.")
                || className.contains("com.android")
                || className.contains("java.lang")
                || className.contains("com.youth.xframe")
                || className in blackList
            ) {
                continue
            }
            perTrace.append(element.className)
                .append('.')
                .append(element.methodName)
                .append("  (")
                .append(element.fileName)
                .append(':')
                .append(element.lineNumber)
                .append(")")
            str += "  "
            line++

            //打印日志
            next?.intercept(priority, tag, "$LEFT_BORDER $perTrace")

        }
    }

}