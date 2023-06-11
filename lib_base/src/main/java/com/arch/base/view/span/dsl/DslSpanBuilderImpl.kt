package com.guadou.lib_baselib.view.span.dsl

import android.graphics.Color
import android.graphics.Typeface


class DslSpanBuilderImpl : DslSpanBuilder {
    var issetColor = false
    var textColor: Int = Color.BLACK

    var isonClick = false
    var isuseUnderLine = false
    var onClick: (() -> Unit)? = null

    var issetScale = false
    var scaleSize = 1.0f

    var issetTypeface = false
    var typefaces: Typeface = Typeface.DEFAULT

    var issetStrikethrough = false

    var issetBackground = false
    var textBackgroundColor = 0

    override fun setColor(color: Int) {
        issetColor = true
        textColor = color
    }

    override fun setClick(useUnderLine: Boolean, onClick: (() -> Unit)?) {
        isonClick = true
        isuseUnderLine = useUnderLine
        this.onClick = onClick
    }

    override fun setScale(scale: Float) {
        issetScale = true
        scaleSize = scale
    }

    override fun setTypeface(typeface: Typeface) {
        issetTypeface = true
        typefaces = typeface
    }

    override fun setStrikethrough(isStrikethrough: Boolean) {
        issetStrikethrough = isStrikethrough
    }

    override fun setBackground(color: Int) {
        issetBackground = true
        textBackgroundColor = color
    }

}