package com.guadou.lib_baselib.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.guadou.lib_baselib.utils.result.Ghost

/**
 * Activity相关
 */

//内联函数+标注泛型 = 泛型实例化

inline fun <reified T> Fragment.gotoActivity(
    flag: Int = -1,
    bundle: Array<out Pair<String, Any?>>? = null
) {
    activity?.gotoActivity<T>(flag, bundle)
}

inline fun <reified T> View.gotoActivity(
    flag: Int = -1,
    bundle: Array<out Pair<String, Any?>>? = null
) {
    context.gotoActivity<T>(flag, bundle)
}

inline fun <reified T> Context.gotoActivity(
    flag: Int = -1,
    bundle: Array<out Pair<String, Any?>>? = null
) {
    val intent = Intent(this, T::class.java).apply {
        if (flag != -1) {
            this.addFlags(flag)
        }
        if (this !is Activity) {
            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (bundle != null) {
            putExtras(bundle.toBundle()!!)
        }
    }
    startActivity(intent)
}


inline fun <reified T> Fragment.gotoActivityForResult(
    flag: Int = -1,
    bundle: Array<out Pair<String, Any?>>? = null,
    crossinline callback: ((result: Intent?) -> Unit)
) {
    activity?.gotoActivityForResult<T>(flag, bundle, callback)
}

inline fun <reified T> View.gotoActivityForResult(
    flag: Int = -1,
    bundle: Array<out Pair<String, Any?>>? = null,
    crossinline callback: ((result: Intent?) -> Unit)
) {
    (context as FragmentActivity).gotoActivityForResult<T>(flag, bundle, callback)
}

//真正执行AcytivityForResult的方法，使用Ghost的方式执行
inline fun <reified T> FragmentActivity.gotoActivityForResult(
    flag: Int = -1,
    bundle: Array<out Pair<String, Any?>>? = null,
    crossinline callback: ((result: Intent?) -> Unit)
) {
    val intent = Intent(this, T::class.java).apply {
        if (flag != -1) {
            this.addFlags(flag)
        }
        if (bundle != null) {
            //调用自己的扩展方法-数组转Bundle
            putExtras(bundle.toBundle()!!)
        }
    }
    Ghost.launchActivityForResult(this, intent, callback)
}

/** dp和px转换 **/
fun Context.dp2px22(dpValue: Float): Int {
    return (dpValue * resources.displayMetrics.density + 0.5f).toInt()
}

