package com.aso.ectvoting.data.authentication;

import com.aso.ectvoting.data.Result;
import com.aso.ectvoting.data.models.User;
import com.aso.ectvoting.utils.Logger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class AuthenticationDataSource {

    private static final Logger LOGGER = new Logger();

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Result<User> login(String email, String password) {

        Map<String, Object> userData = new HashMap<>();
        final boolean[] succeeded = {false};
        AtomicReference<Exception> cause = new AtomicReference<>();

        try {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                    (task -> {
                        String id = Objects.requireNonNull(task.getResult().getUser()).getUid();
                        if (task.isSuccessful()) {
                            db.collection("users").document(id).get().addOnCompleteListener(
                                    (task1 -> {
                                        if(task1.isSuccessful()) {
                                            userData.putAll(Objects.requireNonNull(task1.getResult().getData()));
                                        } else {
                                            succeeded[0] = false;
                                            cause.set(task1.getException());
                                        }

                                    })
                            );
                        } else {
                            succeeded[0] = false;
                            cause.set(task.getException());
                        }
                    })
            );
        } catch (Exception e) {
            return new Result.Error<User>(new IOException("Error logging in", e));
        }
        if (succeeded[0]) {
            return new Result.Success<User>(User.fromMap(userData));
        } else {
            return new Result.Error<User>(new IOException("Error logging in", cause.get()));
        }
    }

    public Result<User> register(String email, String password, String fullName, String base64Face, String natId) {
        final boolean[] succeeded = {false};
        AtomicReference<Exception> cause = new AtomicReference<>();
        User user = new User(
                natId,
                fullName,
                email,
                base64Face
        );
        try {
            mAuth.createUserWithEmailAndPassword(user.getEmail(), password).

                    addOnCompleteListener
                    (task -> {
                        if (task.isSuccessful()) {
                            String id = Objects.requireNonNull(task.getResult().getUser()).getUid();
                            db.collection("users")
                                    .document(id)
                                    .set(user)
                                    .addOnSuccessListener(documentReference -> {
                                        LOGGER.d("DocumentSnapshot added with ID: " + id);
                                        succeeded[0] = true;
                                    })
                                    .addOnFailureListener(e -> {
                                        LOGGER.w("Error adding document", e);
                                        succeeded[0] = false;
                                        cause.set(e);
                                    });
                        } else {
                            succeeded[0] = false;
                            cause.set(task.getException());
                        }
                    });

        } catch (Exception e) {
            return new Result.Error<User>(new IOException("Error Creating new user", e));
        }
        if (succeeded[0]) {
            return new Result.Success<User>(user);
        } else {
            return new Result.Error<User>(new IOException("Error Creating new user", cause.get()));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}