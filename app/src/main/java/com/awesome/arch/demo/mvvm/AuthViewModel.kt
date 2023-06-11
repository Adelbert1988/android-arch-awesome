package com.awesome.arch.demo.mvvm

import androidx.lifecycle.SavedStateHandle
//import com.awesome.arch.demo.mvvm.AuthRepository
//import com.guadou.lib_baselib.base.vm.BaseViewModel
//import com.guadou.lib_baselib.ext.toast
//import dagger.hilt.android.lifecycle.HiltViewModel
//import javax.inject.Inject

//@HiltViewModel
//class AuthViewModel @Inject constructor(
//    private val mAuthRepository: AuthRepository,
//    val savedState: SavedStateHandle
//) : BaseViewModel() {
//
//    fun getServiceTime() {
//
//        launchOnUI {
//
//            loadStartProgress()
//
//            val serverTime = mAuthRepository.getServerTime()
//
//            serverTime.checkSuccess {
//                toast(it?.timestamps.toString())
//            }
//
//            loadSuccess()
//
//        }
//
//    }
//
//}