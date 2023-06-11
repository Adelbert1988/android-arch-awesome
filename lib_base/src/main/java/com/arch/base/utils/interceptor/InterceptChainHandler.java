package com.guadou.lib_baselib.utils.interceptor;

/**
 * 拦截器处理器
 */
public class InterceptChainHandler<T> {

    InterceptChain _interceptFirst;

    public void add(InterceptChain interceptChain) {
        if (_interceptFirst == null) {
            _interceptFirst = interceptChain;
            return;
        }

        InterceptChain node = _interceptFirst;

        while (true) {
            if (node.next == null) {
                node.next = interceptChain;
                break;
            }
            node = node.next;
        }
    }

    public void intercept(T data) {
        _interceptFirst.intercept(data);
    }

}
