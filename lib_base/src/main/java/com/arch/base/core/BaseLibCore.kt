package com.arch.base.core

import android.app.Application
import android.graphics.Color
import android.os.Environment
import android.os.Handler
import androidx.core.content.ContextCompat
import com.arch.base.receiver.ConnectivityReceiver
import com.arch.base.utils.CommUtils
import com.arch.base.utils.ThreadPoolUtils
import com.arch.base.view.gloading.Gloading
import com.arch.base.view.gloading.GloadingGlobalAdapter
import com.arch.base.view.titlebar.EasyTitleBar
import com.example.lib_base.BuildConfig
import com.example.lib_base.R
import com.guadou.lib_baselib.utils.log.YYLogUtils
import com.guadou.lib_baselib.utils.log.interceptor.Log2FileInterceptor
import com.guadou.lib_baselib.utils.log.interceptor.LogDecorateInterceptor
import com.guadou.lib_baselib.utils.log.interceptor.LogPrintInterceptor
import java.io.File

/**
 * 底层的模组依赖库都在这里初始化
 * 当前类在Application中初始化
 */
object BaseLibCore {
    //static 代码段可以防止内存泄露
    //    static {
    //        //设置全局的Header构建器
    //        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
    //            @Override
    //            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
    //                layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);//全局设置主题颜色
    //                return new ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
    //            }
    //        });
    //    }
    /**
     * 初始化全局工具类和图片加载引擎
     */
    fun init(application: Application, handler: Handler?, mainThread: Int) {

        //CommUtil初始化
        CommUtils.init(application, handler, mainThread)

        //初始化全局通用的线程池
        ThreadPoolUtils.init()

        //EaseTitleBar的初始化
        EasyTitleBar.init()
            .backIconRes(R.mipmap.back_black)
            .backgroud(Color.WHITE)
            .titleSize(18)
            .showLine(false)
            .lineHeight(1)
            .menuImgSize(23)
            .menuTextSize(16)
            .lineColor(ContextCompat.getColor(application.applicationContext, R.color.divider_color))
            .titleColor(ContextCompat.getColor(application.applicationContext, R.color.black))
            .viewPadding(10)
            .titleBarHeight(46)

        //全局的Loading状态默认配置
        Gloading.initDefault(GloadingGlobalAdapter())

        //配置Log的拦截器，只有Debug下才生效
        if (BuildConfig.DEBUG) {

            val logPath = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
                application.applicationContext.getExternalFilesDir("log")?.absolutePath
                    ?: (application.applicationContext.filesDir.absolutePath + "/log/")
            else
                application.applicationContext.filesDir.absolutePath + "/log/"

            val dir = File(logPath)
            if (!dir.exists()) {
                dir.mkdirs()
            }

            YYLogUtils.addInterceptor(LogDecorateInterceptor(false))
            YYLogUtils.addInterceptor(LogPrintInterceptor(true))
            YYLogUtils.addInterceptor(Log2FileInterceptor.getInstance(logPath, true))
        }

    }

    /**
     * 注册网络监听
     */
    fun registerNetworkObserver(application: Application?) {
        ConnectivityReceiver.registerReceiver(application!!)
    }

    fun unregisterNetworkObserver(application: Application?) {
        ConnectivityReceiver.unregisterReceiver(application!!)
    }
}