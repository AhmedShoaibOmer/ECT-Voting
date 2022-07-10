package com.aso.ectvoting.data.voting;

import com.aso.ectvoting.core.exception.NetworkErrorException;
import com.aso.ectvoting.data.Result;
import com.aso.ectvoting.data.ResultCallback;
import com.aso.ectvoting.data.models.Candidate;
import com.aso.ectvoting.utils.Logger;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VotingDataSource {
    private static final Logger LOGGER = new Logger();

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getCandidates(ResultCallback<List<Candidate>> callback) {
        final CollectionReference colRef = db.collection("candidates");
        colRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                callback.onComplete(new Result.Error<>(error));
                return;
            }
            ArrayList<Candidate> list = new ArrayList<>();
            if (value != null && !value.isEmpty()) {
                LOGGER.d("Candidates List Size : " + value.getDocuments().size());
                for (DocumentSnapshot snapshot : value.getDocuments()) {
                    if(snapshot.exists()){
                        Map<String, Object> data = new HashMap<>(Objects.requireNonNull(snapshot.getData()));
                        LOGGER.d("Candidate Map : " + data);
                        if(data.containsKey("fullName") && data.containsKey("numOfVoters")) {
                            data.put("id", snapshot.getId());
                            list.add(Candidate.fromMap(data));
                            LOGGER.d("Candidate Map : " + data);
                        } else {
                            callback.onComplete(new Result.Error<>(new IOException()));
                        }
                    }
                }
            }
            callback.onComplete(new Result.Success<>(list));
        });
    }

    public void vote(Candidate candidate, Candidate oldCandidate, ResultCallback<Candidate> callback) {
        if (oldCandidate != null) {
            db.collection("candidates").document(oldCandidate.getId()).update("numOfVoters", FieldValue.increment(-1)).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    vote(candidate, callback);
                } else {
                    if (task.getException() instanceof FirebaseNetworkException) {
                        callback.onComplete(new Result.Error<>(new NetworkErrorException()));
                    } else {
                        callback.onComplete(new Result.Error<>(new IOException()));
                    }
                }
            });
        } else {
            vote(candidate, callback);
        }

    }

    private void vote(Candidate candidate, ResultCallback<Candidate> callback) {
        db.collection("candidates").document(candidate.getId()).update("numOfVoters", FieldValue.increment(1)).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Map<String, Object> data = new HashMap<>();
                data.put("votedToID" , candidate.getId());
                db.collection("users").document(Objects.requireNonNull(mAuth.getUid())).update(data).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()) {
                        callback.onComplete(new Result.Success<>(candidate));
                    }
                    else {
                        if (task1.getException() instanceof FirebaseNetworkException) {
                            callback.onComplete(new Result.Error<>(new NetworkErrorException()));
                        } else {
                            callback.onComplete(new Result.Error<>(new IOException()));
                        }
                    }
                });
            } else {
                if (task.getException() instanceof FirebaseNetworkException) {
                    callback.onComplete(new Result.Error<>(new NetworkErrorException()));
                } else {
                    callback.onComplete(new Result.Error<>(new IOException()));
                }
            }
        });
    }
}
