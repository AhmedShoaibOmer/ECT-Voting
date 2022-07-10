package com.aso.ectvoting.ui.vote;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.aso.ectvoting.R;
import com.aso.ectvoting.data.models.Candidate;
import com.aso.ectvoting.data.models.User;
import com.aso.ectvoting.databinding.ActivityVoteBinding;
import com.aso.ectvoting.ui.customview.CustomProgressDialog;
import com.aso.ectvoting.ui.vote.viewmodel.HomeViewModel;
import com.aso.ectvoting.ui.vote.viewmodel.HomeViewModelFactory;

import java.util.ArrayList;

public class VoteActivity extends AppCompatActivity {

    private User currentUser;
    private final ArrayList<Candidate> candidates = new ArrayList<>();
    private Candidate currentCandidate;
    private boolean isVoting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityVoteBinding binding = ActivityVoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Needed for the loading overlay
        CustomProgressDialog progressDialog = new CustomProgressDialog(this);

        HomeViewModel homeViewModel = new ViewModelProvider(this, new HomeViewModelFactory())
                .get(HomeViewModel.class);

        currentUser = homeViewModel.getCurrentUser();

        String welcome = getString(R.string.welcome) + currentUser.getFullName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();

        ConstraintLayout candidatesContainer, alreadyVotedContainer;
        candidatesContainer = binding.candidatesCContainer;
        alreadyVotedContainer = binding.alreadyVotedContainer;

        RadioGroup candidatesRadioGroup = binding.candidatesRadioGroup;
        TextView votedToTextView = binding.votedToTextView;
        progressDialog.start("Loading list of Candidates....");
        homeViewModel.getCandidatesResult().observe(this, candidatesResult -> {
            if (candidatesResult == null) {
                return;
            }
            if (!isVoting) {
                progressDialog.stop();
            }
            if (candidatesResult.getError() != null) {
                showOperationFailed(candidatesResult.getError());
            }
            if (candidatesResult.getSuccess() != null) {
                candidates.clear();
                candidates.addAll(candidatesResult.getSuccess());
                candidatesRadioGroup.removeAllViews();
                for (int i = 0; i < candidates.size(); i++) {
                    RadioButton radioButton = new RadioButton(this);
                    radioButton.setText(candidates.get(i).getFullName());
                    radioButton.setId(i);
                    candidatesRadioGroup.addView(radioButton);
                }
            }
        });
        homeViewModel.getCandidates();
        homeViewModel.getVoteResult().observe(this, voteResult -> {
            if (voteResult == null) {
                return;
            }
            if (voteResult.getError() != null) {
                showOperationFailed(voteResult.getError());
            }
            if (voteResult.getSuccess() != null) {
                showOperationFailed(R.string.voting_successful);
                currentUser = new User(currentUser.getId(),
                        currentUser.getNatID(),
                        currentUser.getFullName(),
                        currentUser.getEmail(),
                        currentUser.getUserFaceBase64(),
                        voteResult.getSuccess().getId());
                candidatesContainer.setVisibility(View.GONE);
                alreadyVotedContainer.setVisibility(View.VISIBLE);
                for (Candidate candidate : candidates) {
                    if (candidate.getId().equals(currentUser.getVotedToID())) {
                        currentCandidate = candidate;
                    }
                }

                votedToTextView.setText(currentCandidate.getFullName());
                isVoting = false;
                progressDialog.stop();
            }
        });
        homeViewModel.getLogoutResult().observe(this, logoutResult -> {
            if (logoutResult == null) {
                return;
            }
            progressDialog.stop();
            if (logoutResult.getError() != null) {
                showOperationFailed(logoutResult.getError());
            }
            if (logoutResult.getSuccess()) {
                showOperationFailed(R.string.logout_successful);
                finish();
            }
        });

        if (currentUser.getVotedToID() != null) {
            candidatesContainer.setVisibility(View.GONE);
            alreadyVotedContainer.setVisibility(View.VISIBLE);
            for (Candidate candidate : candidates) {
                if (candidate.getId().equals(currentUser.getVotedToID())) {
                    currentCandidate = candidate;
                }
            }
            votedToTextView.setText(currentCandidate.getFullName());
        }

        AppCompatButton submit, clear, editChoice;
        submit = binding.submit;
        clear = binding.clear;
        editChoice = binding.editChoiceBtn;

        AppCompatImageButton logout;
        logout = binding.logoutBtn;

        submit.setOnClickListener(v -> {
            int selectedId = candidatesRadioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                showOperationFailed(R.string.select_your_candidate);
            } else {
                isVoting = true;
                progressDialog.start(getString(currentCandidate == null ? R.string.voting : R.string.revoting));
                homeViewModel.vote(candidates.get(selectedId), currentCandidate);
            }
        });

        clear.setOnClickListener(v -> {
            candidatesRadioGroup.clearCheck();
        });

        editChoice.setOnClickListener(v -> {
            alreadyVotedContainer.setVisibility(View.GONE);
            candidatesContainer.setVisibility(View.VISIBLE);
            candidatesRadioGroup.clearCheck();
        });

        logout.setOnClickListener(view -> {
            progressDialog.start(getString(R.string.logging_out));
            homeViewModel.logout();
        });
    }

    private void showOperationFailed(@StringRes Integer error) {
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
    }
}