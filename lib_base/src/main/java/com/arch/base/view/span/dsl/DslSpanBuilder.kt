package com.guadou.lib_baselib.view.span.dsl

import android.graphics.Color
import android.graphics.Typeface

interface DslSpanBuilder {
    //设置文字颜色
    fun setColor(color: Int = 0)

    //设置点击事件
    fun setClick(useUnderLine: Boolean = true, onClick: (() -> Unit)?)

    //设置缩放大小
    fun setScale(scale: Float = 1.0f)

    //设置自定义字体
    fun setTypeface(typeface: Typeface)

    //是否需要中划线
    fun setStrikethrough(isStrikethrough: Boolean = false)

    //设置背景
    fun setBackground(color: Int = Color.TRANSPARENT)

}