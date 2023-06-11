package com.guadou.lib_baselib.utils.result

import androidx.activity.result.ActivityResultCaller


class SAFLauncher : ISAFLauncher {

    private var safLauncher: GetSAFLauncher? = null

    override fun <T : ActivityResultCaller> T.initLauncher() {
        safLauncher = GetSAFLauncher(this)
    }

    override fun getLauncher(): GetSAFLauncher? = safLauncher

}