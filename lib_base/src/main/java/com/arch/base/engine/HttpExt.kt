package com.guadou.lib_baselib.engine

import com.guadou.lib_baselib.base.ApiException
import com.guadou.lib_baselib.base.vm.BaseRepository
import com.guadou.lib_baselib.bean.OkResult
import com.arch.base.bean.BaseBean

/**
 * 引擎类
 * 网络请求的封装
 */

//可用引擎切换 - 默认用于 Retrofit + suspend方式
suspend fun <T : Any> BaseRepository.extRequestHttp(call: suspend () -> BaseBean<T>): OkResult<T> {

    //两种方式-但是下面哪一种更适合一点，可以知道是哪一种Exception
//    runCatching {
//        call.invoke()
//    }.onSuccess { response: BaseBean<T> ->
//        if (response.code == 200) {
//            OkResult.Success(response.data)
//        } else {
//            OkResult.Error(ApiException(response.code, response.message))
//        }
//    }.onFailure { e ->
//        e.printStackTrace()
//        OkResult.Error(handleExceptionMessage(Exception(e.message, e)))
//    }

    return try {

        val response = call()

        if (response.code == 200) {
            OkResult.Success(response.data)
        } else {
            OkResult.Error(ApiException(response.code, response.message))
        }

    } catch (e: Exception) {

        e.printStackTrace()
        OkResult.Error(handleExceptionMessage(e))
    }

}
