package com.guadou.lib_baselib.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;

import java.lang.reflect.Field;


/**
 * 状态栏透明,状态栏黑色文字，状态栏颜色，沉浸式状态栏
 */
public class StatusBarUtils {

    public static int DEFAULT_COLOR = 0;
    public static float DEFAULT_ALPHA = 0;

    /**
     * 设置状态栏背景颜色
     */
    public static void setColor(Activity activity, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup systemContent = activity.findViewById(android.R.id.content);
            View statusBarView = new View(activity);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
            statusBarView.setBackgroundColor(color);
            systemContent.getChildAt(0).setFitsSystemWindows(true);
            systemContent.addView(statusBarView, 0, lp);
        }
    }


    public static void immersive(Activity activity) {
        immersive(activity, DEFAULT_COLOR, DEFAULT_ALPHA);
    }

    public static void immersive(Activity activity, int color, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        immersive(activity.getWindow(), color, alpha);
    }

    public static void immersive(Activity activity, int color) {
        immersive(activity.getWindow(), color, 1f);
    }

    public static void immersive(Window window) {
        immersive(window, DEFAULT_COLOR, DEFAULT_ALPHA);
    }

    public static void immersive(Window window, int color) {
        immersive(window, color, 1f);
    }

    public static void immersive(Window window, int color, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        if (Build.VERSION.SDK_INT >= 21) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(mixtureColor(color, alpha));

            int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        } else if (Build.VERSION.SDK_INT >= 19) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            setTranslucentView((ViewGroup) window.getDecorView(), color, alpha);
        } else if (Build.VERSION.SDK_INT > 16) {
            int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        }
    }

    /**
     * 创建假的透明栏
     */
    public static void setTranslucentView(ViewGroup container, int color, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        if (Build.VERSION.SDK_INT >= 19) {
            int mixtureColor = mixtureColor(color, alpha);
            View translucentView = container.findViewById(android.R.id.custom);
            if (translucentView == null && mixtureColor != 0) {
                translucentView = new View(container.getContext());
                translucentView.setId(android.R.id.custom);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(container.getContext()));
                container.addView(translucentView, lp);
            }
            if (translucentView != null) {
                translucentView.setBackgroundColor(mixtureColor);
            }
        }
    }

    public static int mixtureColor(int color, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        int a = (color & 0xff000000) == 0 ? 0xff : color >>> 24;
        return (color & 0x00ffffff) | (((int) (a * alpha)) << 24);
    }

    // ========================  状态栏字体颜色设置  ↓ ================================


    /**
     * 设置状态栏黑色字体图标
     */
    public static boolean setStatusBarBlackText(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = activity.getWindow();
            View decorView = window.getDecorView();

            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            return true;
        }
        return false;
    }

    /**
     * 设置状态栏白色字体图标
     */
    public static boolean setStatusBarWhiteText(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = activity.getWindow();
            View decorView = window.getDecorView();

            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            return true;
        }
        return false;
    }

    // ========================  状态栏字体颜色设置  ↑================================


    // 在某些机子上存在不同的density值，所以增加两个虚拟值
    private static int sStatusBarHeight = -1;
    private static float sVirtualDensity = -1;
    private final static int STATUS_BAR_DEFAULT_HEIGHT_DP = 25; // 大部分状态栏都是25dp

    /**
     * 获取状态栏的高度。
     */
    public static int getStatusBarHeight(Context context) {
        if (sStatusBarHeight == -1) {
            initStatusBarHeight(context);
        }
        return sStatusBarHeight;
    }

    private static void initStatusBarHeight(Context context) {
        Class<?> clazz;
        Object obj = null;
        Field field = null;
        try {
            clazz = Class.forName("com.android.internal.R$dimen");
            obj = clazz.newInstance();
            if (DeviceUtils.isMeizu()) {
                try {
                    field = clazz.getField("status_bar_height_large");
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            if (field == null) {
                field = clazz.getField("status_bar_height");
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (field != null && obj != null) {
            try {
                int id = Integer.parseInt(field.get(obj).toString());
                sStatusBarHeight = context.getResources().getDimensionPixelSize(id);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (DeviceUtils.isTablet(context)
                && sStatusBarHeight > CommUtils.dip2px(STATUS_BAR_DEFAULT_HEIGHT_DP)) {
            //状态栏高度大于25dp的平板，状态栏通常在下方
            sStatusBarHeight = 0;
        } else {
            if (sStatusBarHeight <= 0) {
                if (sVirtualDensity == -1) {
                    sStatusBarHeight = CommUtils.dip2px(STATUS_BAR_DEFAULT_HEIGHT_DP);
                } else {
                    sStatusBarHeight = (int) (STATUS_BAR_DEFAULT_HEIGHT_DP * sVirtualDensity + 0.5f);
                }
            }
        }
    }


    // ========================  适配状态栏高度  ↓ ================================

    /**
     * 适配状态栏高度的View - 设置Padding
     */
    public static void fitsStatusBarViewPadding(View view) {
        //增加高度
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height += getStatusBarHeight(view.getContext());

        //设置PaddingTop
        view.setPadding(view.getPaddingLeft(),
                view.getPaddingTop() + getStatusBarHeight(view.getContext()),
                view.getPaddingRight(),
                view.getPaddingBottom());
    }

    /**
     * 适配状态栏高度的View - 设置Margin
     */
    public static void fitsStatusBarViewMargin(View view) {

        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {

            //已经添加过了不要再次设置
            if (view.getTag() != null && view.getTag().equals("fitStatusBar")) {
                return;
            }

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            int marginTop = layoutParams.topMargin;
            int setMarginTop = marginTop + getStatusBarHeight(view.getContext());
            view.setTag("fitStatusBar");
            layoutParams.topMargin = setMarginTop;
            view.requestLayout();
        }
    }

    /**
     * 适配状态栏高度的View - 使用布局包裹
     */
    public static void fitsStatusBarViewLayout(View view) {

        ViewParent fitParent = view.getParent();
        if (fitParent != null) {

            if (((fitParent instanceof LinearLayout) &&
                    ((ViewGroup) fitParent).getTag() != null &&
                    ((ViewGroup) fitParent).getTag().equals("fitLayout"))) {
                //已经添加过了不要再次设置
                return;
            }

            //给当前布局包装一个适应布局
            ViewGroup fitGroup = (ViewGroup) fitParent;
            fitGroup.removeView(view);

            LinearLayout fitLayout = new LinearLayout(view.getContext());
            fitLayout.setOrientation(LinearLayout.VERTICAL);
            fitLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            fitLayout.setTag("fitLayout");

            //先加一个状态栏高度的布局
            View statusView = new View(view.getContext());
            statusView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(view.getContext())));
            fitLayout.addView(statusView);

            ViewGroup.LayoutParams fitViewParams = view.getLayoutParams();
            fitLayout.addView(view);

            fitGroup.addView(fitLayout);
        }
    }

}
