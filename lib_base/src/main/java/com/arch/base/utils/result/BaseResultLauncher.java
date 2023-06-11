package com.arch.base.utils.result;


import android.annotation.SuppressLint;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * 对Result-Api的封装，支持各种输入与输出，使用泛型定义
 */
@SuppressWarnings("unused")
public class BaseResultLauncher<I, O> {

    private final androidx.activity.result.ActivityResultLauncher<I> launcher;
    private final ActivityResultCaller caller;
    private ActivityResultCallback<O> callback;
    private MutableLiveData<O> unprocessedResult;

    public BaseResultLauncher(@NonNull ActivityResultCaller caller, @NonNull ActivityResultContract<I, O> contract) {
        this.caller = caller;
        launcher = caller.registerForActivityResult(contract, (result) -> {
            if (callback != null) {
                callback.onActivityResult(result);
                callback = null;
            }
        });
    }

    public void launch(@SuppressLint("UnknownNullness") I input, @NonNull ActivityResultCallback<O> callback) {
        launch(input, null, callback);
    }

    public void launch(@SuppressLint("UnknownNullness") I input, @Nullable ActivityOptionsCompat options, @NonNull ActivityResultCallback<O> callback) {
        this.callback = callback;
        launcher.launch(input, options);
    }

}

