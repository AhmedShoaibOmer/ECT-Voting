package com.aso.ectvoting.ui.vote;

import androidx.annotation.Nullable;

import com.aso.ectvoting.data.models.Candidate;

public class VoteResult {
    @Nullable
    private Candidate success;
    @Nullable
    private Integer error;

    public VoteResult(@Nullable Integer error) {
        this.error = error;
    }

    public VoteResult(@Nullable Candidate success) {
        this.success = success;
    }

    @Nullable
    Candidate getSuccess() {
        return success;
    }

    @Nullable
    Integer getError() {
        return error;
    }
}
