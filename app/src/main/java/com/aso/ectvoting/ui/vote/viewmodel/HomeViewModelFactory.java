package com.aso.ectvoting.ui.vote.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.aso.ectvoting.data.authentication.AuthenticationDataSource;
import com.aso.ectvoting.data.authentication.AuthenticationRepository;
import com.aso.ectvoting.data.voting.VotingDataSource;
import com.aso.ectvoting.data.voting.VotingRepository;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class HomeViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(AuthenticationRepository.getInstance(new AuthenticationDataSource()), 
                    VotingRepository.getInstance(new VotingDataSource()));
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}