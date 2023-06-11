/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arch.base.utils.navigation;

import static androidx.fragment.app.NavContainerFragment.REAL_FRAGMENT;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.guadou.lib_baselib.utils.navigation.NavOptions;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.NavContainerFragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigator;
import androidx.navigation.fragment.FragmentNavigator;

import java.util.ArrayDeque;
import java.util.Map;

@Navigator.Name("ignore")
public class MyFragmentNavigator extends FragmentNavigator {
    private static final String TAG = "MyFragmentNavigator";
    private static final String KEY_BACK_STACK_IDS = "myFragmentNavigator:backStackIds";
    private ArrayDeque<Integer> mBackStack = new ArrayDeque<>();

    private final Context mContext;
    private final FragmentManager mFragmentManager;
    private final int mContainerId;


    public MyFragmentNavigator(@NonNull Context context, @NonNull FragmentManager manager, int containerId) {
        super(context, manager, containerId);
        mContext = context;
        mFragmentManager = manager;
        mContainerId = containerId;
    }


    @Override
    public boolean popBackStack() {
        if (mBackStack.isEmpty()) {
            return false;
        }
        if (mFragmentManager.isStateSaved()) {
            Log.i(TAG, "Ignoring popBackStack() call: FragmentManager has already"
                    + " saved its state");
            return false;
        }

        if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStack(
                    generateBackStackName(mBackStack.size(), mBackStack.peekLast()),
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            //已经是RootFragment了 无需再返回了
        }
        mBackStack.removeLast();

        return true;
    }


    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    @NonNull
    public Fragment instantiateFragment(@NonNull Context context,
                                        @NonNull FragmentManager fragmentManager,
                                        @NonNull String className,
                                        @SuppressWarnings("unused") @Nullable Bundle args) {
        //Unable to instantiate fragment Demo11OneFragment3: could not find Fragment constructor
        //  这里想要使用构造方法的Fragment初始化，使用了包装类Fragment,让包装类默认空参初始化，
        // 内部的fragment再加载我们真正的Fragment,从而实现构造的Fragment可用

        Fragment fragment = super.instantiateFragment(context, fragmentManager, "androidx.fragment.app.NavContainerFragment", args);
        if (fragment instanceof NavContainerFragment) {
            Bundle bundle = new Bundle();
            bundle.putString(REAL_FRAGMENT, className);
            if (args != null) bundle.putAll(args);
            fragment.setArguments(bundle);
        } else {
            fragment.setArguments(args);
        }
        return fragment;
    }


    @SuppressWarnings("deprecation") /* Using instantiateFragment for forward compatibility */
    @Nullable
    @Override
    public NavDestination navigate(@NonNull FragmentNavigator.Destination destination, @Nullable Bundle args,
                                   @Nullable NavOptions navOptions, @Nullable Navigator.Extras navigatorExtras) {

        if (mFragmentManager.isStateSaved()) {
            return null;
        }
        String className = destination.getClassName();
        if (className.charAt(0) == '.') {
            className = mContext.getPackageName() + className;
        }

        final Fragment frag = instantiateFragment(mContext, mFragmentManager, className, args);
//        frag.setArguments(args);

        final FragmentTransaction ft = mFragmentManager.beginTransaction();

        int enterAnim = navOptions != null ? navOptions.getEnterAnim() : -1;
        int exitAnim = navOptions != null ? navOptions.getExitAnim() : -1;
        int popEnterAnim = navOptions != null ? navOptions.getPopEnterAnim() : -1;
        int popExitAnim = navOptions != null ? navOptions.getPopExitAnim() : -1;
        if (enterAnim != -1 || exitAnim != -1 || popEnterAnim != -1 || popExitAnim != -1) {
            enterAnim = enterAnim != -1 ? enterAnim : 0;
            exitAnim = exitAnim != -1 ? exitAnim : 0;
            popEnterAnim = popEnterAnim != -1 ? popEnterAnim : 0;
            popExitAnim = popExitAnim != -1 ? popExitAnim : 0;
            ft.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim);
        }

//        ft.replace(mContainerId, frag);
        //Add的方式替换replace方式，并处理生命周期
        if (mFragmentManager.getFragments().size() > 0) {
            Fragment lastFragment = mFragmentManager.getFragments().get(mFragmentManager.getFragments().size() - 1);
            ft.hide(lastFragment);
            ft.setMaxLifecycle(lastFragment, Lifecycle.State.STARTED);
            if (frag.isAdded()) {
                ft.show(frag);
            } else {
                ft.add(mContainerId, frag);
            }
        } else {
            ft.replace(mContainerId, frag);
        }

        ft.setPrimaryNavigationFragment(frag);

        final @IdRes int destId = destination.getId();
        final boolean initialNavigation = mBackStack.isEmpty();

        // Build first class singleTop behavior for fragments
        final boolean isSingleTopReplacement = navOptions != null && !initialNavigation
                && navOptions.shouldLaunchSingleTop()
                && mBackStack.peekLast() == destId;

        boolean isAdded;
        if (initialNavigation) {
            isAdded = true;
        } else if (isSingleTopReplacement) {

            if (mBackStack.size() > 1) {

                mFragmentManager.popBackStack(
                        generateBackStackName(mBackStack.size(), mBackStack.peekLast()),
                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                ft.addToBackStack(generateBackStackName(mBackStack.size(), destId));
            }
            isAdded = false;
        } else {
            ft.addToBackStack(generateBackStackName(mBackStack.size() + 1, destId));
            isAdded = true;
        }
        if (navigatorExtras instanceof FragmentNavigator.Extras) {
            FragmentNavigator.Extras extras = (FragmentNavigator.Extras) navigatorExtras;
            for (Map.Entry<View, String> sharedElement : extras.getSharedElements().entrySet()) {
                ft.addSharedElement(sharedElement.getKey(), sharedElement.getValue());
            }
        }
        ft.setReorderingAllowed(true);
        ft.commit();

        // The commit succeeded, update our view of the world
        if (isAdded) {
            mBackStack.add(destId);
            return destination;
        } else {
            return null;
        }
    }

    @Override
    @Nullable
    public Bundle onSaveState() {
        Bundle b = new Bundle();
        int[] backStack = new int[mBackStack.size()];
        int index = 0;
        for (Integer id : mBackStack) {
            backStack[index++] = id;
        }
        b.putIntArray(KEY_BACK_STACK_IDS, backStack);
        return b;
    }

    @Override
    public void onRestoreState(@Nullable Bundle savedState) {
        if (savedState != null) {
            int[] backStack = savedState.getIntArray(KEY_BACK_STACK_IDS);
            if (backStack != null) {
                mBackStack.clear();
                for (int destId : backStack) {
                    mBackStack.add(destId);
                }
            }
        }
    }

    @NonNull
    private String generateBackStackName(int backStackIndex, int destId) {
        return backStackIndex + "-" + destId;
    }

}
