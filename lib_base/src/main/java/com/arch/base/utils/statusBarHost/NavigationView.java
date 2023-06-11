
package com.guadou.lib_baselib.utils.statusBarHost;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.guadou.lib_baselib.utils.log.YYLogUtils;

/**
 * 自定义底部导航栏的View，用于StatusBarHostLayout中使用
 */
class NavigationView extends View {

    private int mBarSize;

    public NavigationView(Context context) {
        this(context, null, 0);
    }

    public NavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        StatusBarHostUtils.getNavigationBarHeight(this, new HeightValueCallback() {
            @Override
            public void onHeight(int height) {

                mBarSize = height;
                YYLogUtils.w("获取到的高度：NavigationBarHeight1：" + height);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mBarSize);
        } else {
            setMeasuredDimension(0, 0);
        }
    }

    public int getBarSize() {
        return mBarSize;
    }

}