package com.guadou.lib_baselib.view.titlebar;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.guadou.basiclib.R;
import com.guadou.lib_baselib.utils.CommUtils;


/**
 * 6.0以下低版本兼容的状态栏展示View
 * 获取父布局的颜色值
 * 如果当前是白色的颜色-那么6.0一下设置灰色 6.0以上设置白色
 * 如果当前是非白色-那么设置为透明颜色
 */
public class StatusbarGrayView extends View {

    public StatusbarGrayView(Context context) {
        super(context);
    }

    public StatusbarGrayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StatusbarGrayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec); //得到宽度设置模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec); //得到宽度设置模式
        int heightMode = MeasureSpec.getMode(heightMeasureSpec); //得到高度设置模式

        //如果设置高度为wrap-content自适应 那么固定设置为状态栏高度
        if (heightMode == MeasureSpec.AT_MOST) {
            if (widthMode == MeasureSpec.EXACTLY) {
                setMeasuredDimension(widthSize, EasyUtil.getStateBarHeight(getContext()));
            } else {
                setMeasuredDimension(1, EasyUtil.getStateBarHeight(getContext()));
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        ViewGroup parent = (ViewGroup) getParent();
        if (parent == null) return;
        Drawable drawable = parent.getBackground();
        if (drawable instanceof ColorDrawable) {
            int color = ((ColorDrawable) drawable).getColor();

            if (color == Color.WHITE || color == CommUtils.getColor(R.color.white)) {

                setBackgroundColor(Build.VERSION.SDK_INT < 23 ? CommUtils.getColor(R.color.status_bar_gray_bg) : Color.WHITE);

            } else {
                setBackgroundColor(Color.TRANSPARENT);
            }
        } else {
            setBackgroundColor(Color.TRANSPARENT);
        }

    }

}
