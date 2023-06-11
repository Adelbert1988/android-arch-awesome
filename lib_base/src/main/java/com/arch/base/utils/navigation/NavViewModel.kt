package com.guadou.lib_baselib.utils.navigation

import androidx.collection.SparseArrayCompat
import androidx.collection.keyIterator
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination

internal class NavViewModel : ViewModel() {
    val nodes = SparseArrayCompat<NavDestination>()
}

internal fun FragmentActivity.saveToViewModel(destination: NavDestination) {
    val vm = ViewModelProvider(this)[NavViewModel::class.java]
    if (vm.nodes.keyIterator().asSequence().any {
            it == destination.id
        }) return
    vm.nodes.put(destination.id, destination)
}


internal fun FragmentActivity.removeFromViewModel(id: Int) {
    val vm = ViewModelProvider(this)[NavViewModel::class.java]
    vm.nodes.remove(id)
}