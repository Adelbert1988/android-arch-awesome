package com.guadou.lib_baselib.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.guadou.basiclib.R
import kotlinx.coroutines.*


/**
 *  异步加载布局的 ViewStub
 */
class AsyncViewStub @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    var layoutId: Int = 0
    var mView: View? = null

    init {
        initAttrs(attrs, context)//初始化属性
    }

    private fun initAttrs(attrs: AttributeSet?, context: Context?) {
        val typedArray = context!!.obtainStyledAttributes(
            attrs,
            R.styleable.AsyncViewStub
        )

        layoutId = typedArray.getResourceId(
            R.styleable.AsyncViewStub_layout,
            0
        )

        typedArray.recycle()
    }


    fun inflateAsync(block: ((View) -> Unit)? = null) {

        if (layoutId == 0) throw RuntimeException("没有找到加载的布局，你必须在xml中设置layout属性")

        launch {

            val view = withContext(Dispatchers.IO) {
                LayoutInflater.from(context).inflate(layoutId, null)
            }

            mView = view

            //添加到父布局
            val parent = parent as ViewGroup
            val index = parent.indexOfChild(this@AsyncViewStub)
            val vlp: ViewGroup.LayoutParams = layoutParams
            view.layoutParams = vlp //把 LayoutParams 给到新view

            parent.removeViewAt(index) //删除原来的占位View
            parent.addView(view, index) //把新有的View替换上去

            block?.invoke(view)
        }
    }

    fun isInflate(): Boolean {
        return mView != null
    }

    fun getInflatedView(): View? {
        return mView
    }

    override fun onDetachedFromWindow() {
        cancel()
        super.onDetachedFromWindow()
    }
}