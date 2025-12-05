package com.pr0gramm3r101.utils;

import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

public final class BetterActivityResult<I, R> {
    /**
     * Register activity result using a {@link ActivityResultContract} and an in-place activity result callback like
     * the default approach. You can still customise callback using {@link #launch(Object, OnActivityResult)}.
     */
    @NonNull
    public static <I, R> BetterActivityResult<I, R> registerForActivityResult(
            @NonNull ActivityResultCaller caller,
            @NonNull ActivityResultContract<I, R> contract,
            @Nullable OnActivityResult<R> onActivityResult) {
        return new BetterActivityResult<>(caller, contract, onActivityResult);
    }

    /**
     * Same as {@link #registerForActivityResult(ActivityResultCaller, ActivityResultContract, OnActivityResult)} except
     * the last argument is set to {@code null}.
     */
    @NonNull
    public static <I, R> BetterActivityResult<I, R> registerForActivityResult(
            @NonNull ActivityResultCaller caller,
            @NonNull ActivityResultContract<I, R> contract) {
        return registerForActivityResult(caller, contract, null);
    }

    /**
     * Specialised method for launching new activities.
     */
    @NonNull
    public static BetterActivityResult<Intent, ActivityResult> registerActivityForResult(
            @NonNull ActivityResultCaller caller) {
        return registerForActivityResult(caller, new ActivityResultContracts.StartActivityForResult());
    }

    /**
     * Callback interface
     */
    @FunctionalInterface
    public interface OnActivityResult<O> {
        /**
         * Called after receiving a result from the target activity
         */
        void onActivityResult(O result);
    }

    private final ActivityResultLauncher<I> launcher;
    @Nullable
    private OnActivityResult<R> onActivityResult;

    private BetterActivityResult(@NonNull ActivityResultCaller caller,
                                 @NonNull ActivityResultContract<I, R> contract,
                                 @Nullable OnActivityResult<R> onActivityResult) {
        this.onActivityResult = onActivityResult;
        this.launcher = caller.registerForActivityResult(contract, this::callOnActivityResult);
    }

    /**
     * Launch activity, same as {@link ActivityResultLauncher#launch(Object)} except that it allows a callback
     * executed after receiving a result from the target activity.
     */
    public void launch(I input, @Nullable OnActivityResult<R> onActivityResult) {
        this.onActivityResult = onActivityResult;
        launcher.launch(input);
    }

    public void launch(I input, ActivityOptionsCompat options, @Nullable OnActivityResult<R> onActivityResult) {
        this.onActivityResult = onActivityResult;
        launcher.launch(input, options);
    }

    /**
     * Same as {@link #launch(Object, OnActivityResult)} with last parameter set to {@code null}.
     * @noinspection unused
     */
    public void launch(I input) {
        launch(input, (OnActivityResult<R>) null);
    }

    /** @noinspection unused*/
    public void launch(I input, ActivityOptionsCompat options) {
        launch(input, options, null);
    }

    private void callOnActivityResult(R result) {
        if (onActivityResult != null) onActivityResult.onActivityResult(result);
    }
}
