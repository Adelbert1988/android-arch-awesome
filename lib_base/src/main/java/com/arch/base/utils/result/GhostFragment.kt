package com.guadou.lib_baselib.utils.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * 封装Activity Result的API
 * 使用空Fragemnt的形式调用startActivityForResult并返回回调
 *
 * Activty/Fragment——>add GhostFragment——>onAttach中startActivityForResult
 * ——>GhostFragment onActivityResult接收结果——>callback回调给Activty/Fragment
 */
class GhostFragment : Fragment() {

    private var requestCode = -1
    private var intent: Intent? = null
    private var callback: ((result: Intent?) -> Unit)? = null

    fun init(requestCode: Int, intent: Intent, callback: ((result: Intent?) -> Unit)) {
        this.requestCode = requestCode
        this.intent = intent
        this.callback = callback
    }

    private var activityStarted = false

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (!activityStarted) {
            activityStarted = true
            intent?.let { startActivityForResult(it, requestCode) }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!activityStarted) {
            activityStarted = true
            intent?.let { startActivityForResult(it, requestCode) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == this.requestCode) {
            callback?.let { it1 -> it1(data) }
        }
    }

    override fun onDetach() {
        super.onDetach()
        intent = null
        callback = null
    }

}