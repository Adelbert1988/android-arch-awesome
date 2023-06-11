package com.guadou.lib_baselib.utils.interceptor;

import android.os.Handler;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * 拦截是否登录的Task
 */
public abstract class LoginInterceptorTask {

    private static volatile Executor sDefaultExecutor = Executors.newSingleThreadExecutor();

    private final static int sDefaultWaitLong = 10 * 60 * 1000; //默认等待10分钟

    private static final Handler sHandler = new Handler();

    private volatile Status mStatus = Status.IDLE;

    public enum Status {
        RUNNING, IDLE,
    }

    //子类必须重写的方法
    public abstract boolean isLogin();

    //子类必须重写的方法
    public abstract void gotoLogin();

    //完成登录之后必须调用的方法
    public static void loginEnded() {
        synchronized (sHandler) {
            sHandler.notifyAll();
        }
    }

    public void setStatus(Status status) {
        mStatus = status;
    }

    public final void execute(Runnable r) {
        if (isLogin()) {
            sHandler.post(r);
            return;
        }
        if (mStatus != Status.RUNNING) {
            gotoLogin();
        }
        mStatus = Status.RUNNING;

        final WorkerCallable callable = new WorkerCallable() {

            @Override
            public Boolean call() throws Exception {
                if (isLogin()) {
                    return true;
                }

                if (mStatus == Status.IDLE) {
                    return false;
                }

                synchronized (sHandler) {
                    sHandler.wait(sDefaultWaitLong);
                    mStatus = Status.IDLE;
                    return isLogin();
                }
            }
        };

        FutureTask<Boolean> future = new FutureTask<Boolean>(callable) {
            @Override
            protected void done() {
                try {
                    final boolean result = get();
                    sHandler.removeCallbacksAndMessages(null);
                    if (result) {
                        sHandler.post(callable.runnable);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        callable.runnable = r;
        sDefaultExecutor.execute(future);
    }

    private static abstract class WorkerCallable implements Callable<Boolean> {
        Runnable runnable;
    }

}
