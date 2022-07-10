package com.aso.ectvoting.data.voting;

import com.aso.ectvoting.data.Result;
import com.aso.ectvoting.data.ResultCallback;
import com.aso.ectvoting.data.authentication.AuthenticationDataSource;
import com.aso.ectvoting.data.authentication.AuthenticationRepository;
import com.aso.ectvoting.data.models.Candidate;
import com.aso.ectvoting.data.models.User;

import java.util.List;

public class VotingRepository {
    private static volatile VotingRepository instance;

    private final VotingDataSource dataSource;

    // private constructor : singleton access
    private VotingRepository(VotingDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static VotingRepository getInstance(VotingDataSource dataSource) {
        if (instance == null) {
            instance = new VotingRepository(dataSource);
        }
        return instance;
    }

    public void getCandidates(ResultCallback<List<Candidate>> callback) {
        // handle login
        dataSource.getCandidates(callback);
    }

    public void vote(Candidate candidate, Candidate oldCandidate, ResultCallback<Candidate> callback) {
        // handle login
        dataSource.vote(candidate, oldCandidate, callback);
    }

}
