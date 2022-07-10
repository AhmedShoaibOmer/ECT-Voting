package com.aso.ectvoting.ui.vote;

import androidx.annotation.Nullable;

import com.aso.ectvoting.data.models.Candidate;

import java.util.List;


public class CandidatesResult {
    @Nullable
    private List<Candidate> success;
    @Nullable
    private Integer error;

    public CandidatesResult(@Nullable Integer error) {
        this.error = error;
    }

    public CandidatesResult(@Nullable List<Candidate> success) {
        this.success = success;
    }

    @Nullable
    List<Candidate> getSuccess() {
        return success;
    }

    @Nullable
    Integer getError() {
        return error;
    }
}
