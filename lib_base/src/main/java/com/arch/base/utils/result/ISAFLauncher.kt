package com.guadou.lib_baselib.utils.result

import androidx.activity.result.ActivityResultCaller


/**
 * 定义是否需要SAFLauncher
 */
interface ISAFLauncher {

    fun <T : ActivityResultCaller> T.initLauncher()

    fun getLauncher(): GetSAFLauncher?

}