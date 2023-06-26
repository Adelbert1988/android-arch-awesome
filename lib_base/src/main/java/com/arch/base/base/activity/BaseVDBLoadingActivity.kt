package com.arch.base.base.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.guadou.lib_baselib.base.vm.BaseViewModel
import com.arch.base.bean.DataBindingConfig
import com.arch.base.bean.LoadAction
import com.arch.base.utils.NetWorkUtil
import com.arch.base.view.LoadingDialogManager
import com.arch.base.view.gloading.Gloading
import com.guadou.lib_baselib.ext.getVMCls

/**
 * 加入ViewModel与LoadState
 * 默认为布局加载的方式
 */
abstract class BaseVDBLoadingActivity<VM : BaseViewModel, VDB : ViewDataBinding> : AbsActivity() {

    protected lateinit var mViewModel: VM
    protected lateinit var mBinding: VDB

    protected val mGLoadingHolder by lazy {
        generateGLoading()
    }

    //如果要替换GLoading，重写次方法
    open protected fun generateGLoading(): Gloading.Holder {
        return Gloading.getDefault().wrap(this).withRetry {
            onGoadingRetry()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startObserve()
    }

    //使用这个方法简化ViewModewl的Hilt依赖注入获取
    protected inline fun <reified VM : BaseViewModel> getViewModel(): VM {
        val viewModel: VM by viewModels()
        return viewModel
    }

    //反射获取ViewModel实例
    open protected fun createViewModel(): VM {
        return ViewModelProvider(this).get(getVMCls(this))
    }

    override fun setContentView() {
        mViewModel = createViewModel()
        //观察网络数据状态
        mViewModel.getActionLiveData().observe(this, stateObserver)

        val config = getDataBindingConfig()
        mBinding = DataBindingUtil.setContentView(this, config.getLayout())
        mBinding.lifecycleOwner = this

        if (config.getVmVariableId() != 0) {
            mBinding.setVariable(
                config.getVmVariableId(),
                config.getViewModel()
            )
        }

        val bindingParams = config.getBindingParams()
        bindingParams.forEach { key, value ->
            mBinding.setVariable(key, value)
        }
    }

    abstract fun getDataBindingConfig(): DataBindingConfig
    abstract fun startObserve()
    protected open fun onGoadingRetry() {
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean, networkType: NetWorkUtil.NetworkType?) {
    }

    // ================== 网络状态的监听 ======================

    private var stateObserver: Observer<LoadAction> = Observer { loadAction ->
        if (loadAction != null) {

            when (loadAction.action) {
                LoadAction.STATE_NORMAL -> showStateNormal()
                LoadAction.STATE_ERROR -> showStateError(loadAction.message)
                LoadAction.STATE_SUCCESS -> showStateSuccess()
                LoadAction.STATE_LOADING -> showStateLoading()
                LoadAction.STATE_NO_DATA -> showStateNoData()
                LoadAction.STATE_PROGRESS -> showStateProgress()
                LoadAction.STATE_HIDE_PROGRESS -> hideStateProgress()
            }

        }
    }

    protected open fun showStateNormal() {}

    protected open fun showStateLoading() {
        mGLoadingHolder.showLoading()
    }

    protected open fun showStateSuccess() {
        mGLoadingHolder.showLoadSuccess()
    }

    protected open fun showStateError(message: String?) {
        mGLoadingHolder.showLoadFailed(message)
    }

    protected open fun showStateNoData() {
        mGLoadingHolder.showEmpty()
    }

    protected fun showStateProgress() {
        LoadingDialogManager.get().showLoading(mActivity)
    }

    protected fun hideStateProgress() {
        LoadingDialogManager.get().dismissLoading()
    }

}