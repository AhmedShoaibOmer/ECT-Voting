package com.aso.ectvoting.ui.vote.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aso.ectvoting.R;
import com.aso.ectvoting.core.exception.NetworkErrorException;
import com.aso.ectvoting.data.Result;
import com.aso.ectvoting.data.authentication.AuthenticationRepository;
import com.aso.ectvoting.data.models.Candidate;
import com.aso.ectvoting.data.models.User;
import com.aso.ectvoting.data.voting.VotingRepository;
import com.aso.ectvoting.ui.vote.CandidatesResult;
import com.aso.ectvoting.ui.vote.LogoutResult;
import com.aso.ectvoting.ui.vote.VoteResult;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<CandidatesResult> candidatesResult = new MutableLiveData<>();
    private final MutableLiveData<VoteResult> voteResult = new MutableLiveData<>();
    private final MutableLiveData<LogoutResult> logoutResult = new MutableLiveData<>();
    private final AuthenticationRepository authenticationRepository;
    private final VotingRepository votingRepository;

    HomeViewModel(AuthenticationRepository authenticationRepository, VotingRepository votingRepository1) {
        this.authenticationRepository = authenticationRepository;
        this.votingRepository = votingRepository1;
    }

    public MutableLiveData<CandidatesResult> getCandidatesResult() {
        return candidatesResult;
    }
    public MutableLiveData<VoteResult> getVoteResult() {
        return voteResult;
    }
    public LiveData<LogoutResult> getLogoutResult() {
        return logoutResult;
    }

    public User getCurrentUser() {
        return authenticationRepository.user;
    }

    public void getCandidates() {
        // can be launched in a separate asynchronous job
        votingRepository.getCandidates(result -> {
            if (result instanceof Result.Success) {
                List<Candidate> candidates = ((Result.Success<List<Candidate>>) result).getData();
                candidatesResult.setValue(new CandidatesResult(candidates));
            } else {
                if (((Result.Error<List<Candidate>>) result).getError() instanceof NetworkErrorException) {
                    candidatesResult.setValue(new CandidatesResult(R.string.network_error));
                } else {
                    candidatesResult.setValue(new CandidatesResult(R.string.fetching_candidates_failed));
                }
            }
        });
    }

    public void vote(Candidate c,  Candidate oldC) {
        // can be launched in a separate asynchronous job
        votingRepository.vote(c, oldC, result -> {
            if (result instanceof Result.Success) {
                Candidate candidate = ((Result.Success<Candidate>) result).getData();
                voteResult.setValue(new VoteResult(candidate));
            } else {
                if (((Result.Error<Candidate>) result).getError() instanceof NetworkErrorException) {
                    candidatesResult.setValue(new CandidatesResult(R.string.network_error));
                } else {
                    candidatesResult.setValue(new CandidatesResult(R.string.voting_failed));
                }
            }
        });
    }

    public void logout() {
        // can be launched in a separate asynchronous job
        authenticationRepository.logout(result -> {
            if (result instanceof Result.Success) {
                logoutResult.setValue(new LogoutResult());
            } else {
                if (((Result.Error<Void>) result).getError() instanceof NetworkErrorException) {
                    logoutResult.setValue(new LogoutResult(R.string.network_error));
                } else {
                    logoutResult.setValue(new LogoutResult(R.string.voting_failed));
                }
            }
        });
    }
}