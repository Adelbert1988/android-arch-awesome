package com.awesome.arch.demo.mvvm

//import com.guadou.cs_cptservices.Constants
//import com.guadou.lib_baselib.base.vm.BaseRepository
//import com.guadou.lib_baselib.bean.OkResult
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class AuthRepository @Inject constructor() : BaseRepository() {
//
//    suspend fun getServerTime(): OkResult<ServerTimeBean> {
//        return handleErrorApiCall({
//            handleApiErrorResponse(
//                MainRetrofit.apiService.getServerTime(
//                    Constants.NETWORK_CONTENT_TYPE,
//                    Constants.NETWORK_ACCEPT_V1
//                )
//            )
//
//        })
//    }
//
//}