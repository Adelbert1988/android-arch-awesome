package com.arch.base.utils.interceptor;

/**
 * 每一个的拦截器
 */
public abstract class InterceptChain<T> {

    public InterceptChain next;

    public void intercept(T data) {
        if (next != null) {
            next.intercept(data);
        }
    }

}
