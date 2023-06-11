package com.guadou.lib_baselib.view.span.dsl


interface DslSpannableStringBuilder {
    //增加一段文字
    fun addText(text: String, method: (DslSpanBuilder.() -> Unit)? = null)

    //添加一个图标
    fun addImage(imageRes: Int, verticalAlignment: Int = 0, maginLeft: Int = 0, marginRight: Int = 0, width: Int = 0, height: Int = 0)
}