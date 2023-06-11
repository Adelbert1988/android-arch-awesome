package com.guadou.lib_baselib.ext

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import java.lang.reflect.Constructor
import java.util.*

/**
 * Fragment 原生创建 ViewModel的扩展方法，支持添加构造方法的创建
 *
 *  使用：
 *  private val viewModel by viewModelByFactory(arguments)
 */
typealias CreateViewModel = (handle: SavedStateHandle) -> ViewModel

inline fun <reified VM : ViewModel> Fragment.viewModelByFactory(
    defaultArgs: Bundle? = null,
    noinline create: CreateViewModel = {
        val constructor = findMatchingConstructor(VM::class.java, arrayOf(SavedStateHandle::class.java))
        constructor!!.newInstance(it)
    }
): Lazy<VM> {
    return viewModels {
        createViewModelFactoryFactory(this, defaultArgs, create)
    }
}

inline fun <reified VM : ViewModel> Fragment.activityViewModelByFactory(
    defaultArgs: Bundle? = null,
    noinline create: CreateViewModel
): Lazy<VM> {
    return activityViewModels {
        createViewModelFactoryFactory(this, defaultArgs, create)
    }
}

fun createViewModelFactoryFactory(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle?,
    create: CreateViewModel
): ViewModelProvider.Factory {
    return object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
        override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            @Suppress("UNCHECKED_CAST")
            return create(handle) as? T ?: throw IllegalArgumentException("Unknown viewmodel class!")
        }
    }
}

@PublishedApi
internal fun <T> findMatchingConstructor(
    modelClass: Class<T>,
    signature: Array<Class<*>>
): Constructor<T>? {
    for (constructor in modelClass.constructors) {
        val parameterTypes = constructor.parameterTypes
        if (Arrays.equals(signature, parameterTypes)) {
            return constructor as Constructor<T>
        }
    }
    return null
}