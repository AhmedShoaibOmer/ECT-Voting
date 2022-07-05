package com.aso.ectvoting.data;

/**
 * A helper interface that holds a result success w/ data or an error exception.
 * and used with asynchronously working apis like firebase tasks.
 */
public interface ResultCallback<T> {
    void onComplete(Result<T> result);
}
