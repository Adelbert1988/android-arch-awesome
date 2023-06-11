package com.guadou.lib_baselib.utils.navigation


interface IOnBackPressed {
    // Fragment的返回事件处理
    // 返回值 -> 是否穿透事件,交给Activity处理
    fun onBackPressed(): Boolean = false
}