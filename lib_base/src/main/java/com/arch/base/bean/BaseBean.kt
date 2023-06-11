package com.arch.base.bean

data class BaseBean<out T>(val code: Int, val message: String, val data: T)