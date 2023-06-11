package com.guadou.lib_baselib.utils.navigation

import androidx.fragment.app.Fragment
import java.util.concurrent.ConcurrentHashMap

@PublishedApi
internal object FragmentCaches : MutableMap<String, (() -> Fragment)?> by ConcurrentHashMap()