package com.guadou.lib_baselib.view.span.dsl

import android.text.SpannableStringBuilder
import com.guadou.lib_baselib.ext.*


class DslSpannableStringBuilderImpl : DslSpannableStringBuilder {

    private val builder = SpannableStringBuilder()

    //添加文本
    override fun addText(text: String, method: (DslSpanBuilder.() -> Unit)?) {

        val spanBuilder = DslSpanBuilderImpl()
        method?.let { spanBuilder.it() }

        var charSeq: CharSequence = text

        spanBuilder.apply {
            if (issetColor) {
                charSeq = charSeq.toColorSpan(0..text.length, textColor)
            }
            if (issetBackground) {
                charSeq = charSeq.toBackgroundColorSpan(0..text.length, textBackgroundColor)
            }
            if (issetScale) {
                charSeq = charSeq.toSizeSpan(0..text.length, scaleSize)
            }
            if (isonClick) {
                charSeq = charSeq.toClickSpan(0..text.length, textColor, isuseUnderLine, onClick)
            }
            if (issetTypeface) {
                charSeq = charSeq.toCustomTypeFaceSpan(typefaces, 0..text.length)
            }
            if (issetStrikethrough) {
                charSeq = charSeq.toStrikeThrougthSpan(0..text.length)
            }

            builder.append(charSeq)
        }
    }

    //添加图标
    override fun addImage(imageRes: Int, verticalAlignment: Int, maginLeft: Int, marginRight: Int, width: Int, height: Int) {
        var charSeq: CharSequence = "1"
        charSeq = charSeq.toImageSpan(imageRes, 0..1, verticalAlignment, maginLeft, marginRight, width, height)
        builder.append(charSeq)
    }

    fun build(): SpannableStringBuilder {
        return builder
    }

}