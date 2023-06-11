package com.guadou.lib_baselib.engine

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import com.guadou.basiclib.R

import com.guadou.lib_baselib.ext.toast
import com.guadou.lib_baselib.view.FangIOSDialog
import com.hjq.permissions.IPermissionInterceptor
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.util.*

/**
 * 申请权限的引擎类
 * Activity的申请权限
 */
fun Activity.extRequestPermission(
    vararg permissions: String,
    block: () -> Unit
) {

    XXPermissions.with(this)
        .permission(permissions)
        .interceptor(PermissionInterceptor())
        .request { _, all ->
            if (all) {
                block()
            }
        }
}

/**
 * 申请权限的引擎
 * Fragment的申请权限
 */
fun Fragment.extRequestPermission(
    vararg permissions: String,
    block: () -> Unit
) {

    XXPermissions.with(this)
        .permission(permissions)
        .interceptor(PermissionInterceptor())
        .request { _, all ->
            if (all) {
                block()
            }
        }
}

internal class PermissionInterceptor : IPermissionInterceptor {

    override fun requestPermissions(activity: Activity, callback: OnPermissionCallback?, allPermissions: MutableList<String>?) {
        super.requestPermissions(activity, callback, allPermissions)
        //申请权限之前的拦截操作
    }

    override fun grantedPermissions(
        activity: Activity,
        allPermissions: MutableList<String>?,
        grantedPermissions: MutableList<String>?,
        all: Boolean,
        callback: OnPermissionCallback?
    ) {
        //申请成功的拦截
        callback?.onGranted(grantedPermissions, all)
    }

    override fun deniedPermissions(
        activity: Activity,
        allPermissions: MutableList<String>?,
        deniedPermissions: MutableList<String>?,
        never: Boolean,
        callback: OnPermissionCallback?
    ) {
        //申请失败的拦截
        callback?.onDenied(deniedPermissions, never)

        if (never) {
            showPermissionDialog(activity, deniedPermissions)
            return
        }

        if (deniedPermissions!!.size == 1 && Permission.ACCESS_BACKGROUND_LOCATION == deniedPermissions[0]) {
            toast(R.string.common_permission_fail_4)
            return
        }

        toast(R.string.common_permission_fail_1)

        if (callback == null) {
            return
        }
        callback.onDenied(deniedPermissions, never)
    }

    //展示失败的弹窗
    private fun showPermissionDialog(activity: Activity, permissions: MutableList<String>?) {
        //使用弹窗再次确认
        FangIOSDialog(activity).apply {
            setTitle(R.string.common_permission_hint)
            setMessage(getPermissionHint(activity, permissions))
            setNegativeButton(R.string.common_permission_denied) {
                dismiss()
            }
            setPositiveButton(R.string.common_permission_granted) {
                dismiss()
                XXPermissions.startPermissionActivity(activity, permissions)
            }
            setCanceledOnTouchOutside(true)
            show()
        }

    }

    /**
     * 根据权限获取提示
     */
    private fun getPermissionHint(context: Context, permissions: List<String?>?): String? {
        if (permissions == null || permissions.isEmpty()) {
            return context.getString(R.string.common_permission_fail_2)
        }
        val hints: MutableList<String> = ArrayList()
        for (permission in permissions) {
            when (permission) {
                Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE, Permission.MANAGE_EXTERNAL_STORAGE -> {
                    val hint = context.getString(R.string.common_permission_storage)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.CAMERA -> {
                    val hint = context.getString(R.string.common_permission_camera)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.RECORD_AUDIO -> {
                    val hint = context.getString(R.string.common_permission_microphone)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_BACKGROUND_LOCATION -> {
                    val hint: String = if (!permissions.contains(Permission.ACCESS_FINE_LOCATION) &&
                        !permissions.contains(Permission.ACCESS_COARSE_LOCATION)
                    ) {
                        context.getString(R.string.common_permission_location_background)
                    } else {
                        context.getString(R.string.common_permission_location)
                    }
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.BLUETOOTH_SCAN, Permission.BLUETOOTH_CONNECT, Permission.BLUETOOTH_ADVERTISE -> {
                    if (Build.VERSION.SDK_INT >= 31) {
                        val hint = context.getString(R.string.common_permission_bluetooth)
                        if (!hints.contains(hint)) {
                            hints.add(hint)
                        }
                    }
                }
                Permission.READ_PHONE_STATE, Permission.CALL_PHONE, Permission.ADD_VOICEMAIL, Permission.USE_SIP, Permission.READ_PHONE_NUMBERS, Permission.ANSWER_PHONE_CALLS -> {
                    val hint = context.getString(R.string.common_permission_phone)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.GET_ACCOUNTS, Permission.READ_CONTACTS, Permission.WRITE_CONTACTS -> {
                    val hint = context.getString(R.string.common_permission_contacts)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.READ_CALENDAR, Permission.WRITE_CALENDAR -> {
                    val hint = context.getString(R.string.common_permission_calendar)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.READ_CALL_LOG, Permission.WRITE_CALL_LOG, Permission.PROCESS_OUTGOING_CALLS -> {
                    val hint =
                        context.getString(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) R.string.common_permission_call_log else R.string.common_permission_phone)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.BODY_SENSORS -> {
                    val hint = context.getString(R.string.common_permission_sensors)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.ACTIVITY_RECOGNITION -> {
                    val hint = context.getString(R.string.common_permission_activity_recognition)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.SEND_SMS, Permission.RECEIVE_SMS, Permission.READ_SMS, Permission.RECEIVE_WAP_PUSH, Permission.RECEIVE_MMS -> {
                    val hint = context.getString(R.string.common_permission_sms)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.REQUEST_INSTALL_PACKAGES -> {
                    val hint = context.getString(R.string.common_permission_install)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.SYSTEM_ALERT_WINDOW -> {
                    val hint = context.getString(R.string.common_permission_window)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.WRITE_SETTINGS -> {
                    val hint = context.getString(R.string.common_permission_setting)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.NOTIFICATION_SERVICE -> {
                    val hint = context.getString(R.string.common_permission_notification)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                Permission.PACKAGE_USAGE_STATS -> {
                    val hint = context.getString(R.string.common_permission_task)
                    if (!hints.contains(hint)) {
                        hints.add(hint)
                    }
                }
                else -> {
                }
            }
        }
        if (hints.isNotEmpty()) {
            val builder = StringBuilder()
            for (text in hints) {
                if (builder.isEmpty()) {
                    builder.append(text)
                } else {
                    builder.append("、")
                        .append(text)
                }
            }
            builder.append(" ")
            return context.getString(R.string.common_permission_fail_3, builder.toString())
        }
        return context.getString(R.string.common_permission_fail_2)
    }

}
