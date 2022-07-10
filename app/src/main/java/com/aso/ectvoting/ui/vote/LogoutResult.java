package com.aso.ectvoting.ui.vote;

import androidx.annotation.Nullable;

public class LogoutResult {
    private boolean success;
    @Nullable
    private Integer error;

    public LogoutResult(@Nullable Integer error) {
        this.error = error;
    }

    public LogoutResult() {
        this.success = true;
    }

    boolean getSuccess() {
        return success;
    }

    @Nullable
    Integer getError() {
        return error;
    }
}
