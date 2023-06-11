package com.awesome.arch.demo.ui

//import android.content.Intent
//import com.guadou.cpt_main.R
//import com.awesome.arch.demo.mvvm.AuthViewModel
//import com.awesome.arch.demo.others.MemberServer
//import com.guadou.lib_baselib.base.activity.BaseVMActivity
//import com.guadou.lib_baselib.utils.CommUtils
//import com.guadou.lib_baselib.utils.log.YYLogUtils
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.android.synthetic.main.activity_auth.*
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class AuthActivity : BaseVMActivity<AuthViewModel>() {
//
//    @Inject
//    lateinit var userServer: MemberServer
//
//    companion object {
//        fun startInstance() {
//            val context = CommUtils.getContext()
//            val intent = Intent(context, AuthActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            context.startActivity(intent)
//        }
//    }
//
//    override fun startObserve() {
//    }
//
//    override fun getLayoutIdRes(): Int =
//        R.layout.activity_auth
//
//    override fun init() {
//
//
//        YYLogUtils.e("viewmodel:" + mViewModel.toString())
//        YYLogUtils.e("userServer:" + userServer.toString())
//        userServer.testUser()
//
//        btn_login.setOnClickListener {
//            mViewModel.getServiceTime()
//        }
//    }
//
//}