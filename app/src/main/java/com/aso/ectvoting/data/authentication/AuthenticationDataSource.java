package com.aso.ectvoting.data.authentication;

import com.aso.ectvoting.data.Result;
import com.aso.ectvoting.data.ResultCallback;
import com.aso.ectvoting.data.models.User;
import com.aso.ectvoting.utils.Logger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


interface AuthRequestCallback {
    void onSuccess();

    void onFailure();
}

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class AuthenticationDataSource {

    private static final Logger LOGGER = new Logger();

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void login(String email, String password, ResultCallback<User> callback) {

        try {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                    (task -> {
                        String id = Objects.requireNonNull(task.getResult().getUser()).getUid();
                        if (task.isSuccessful()) {
                            db.collection("users").document(id).get().addOnCompleteListener(
                                    (task1 -> {
                                        if (task1.isSuccessful()) {
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.putAll(Objects.requireNonNull(task1.getResult().getData()));
                                            callback.onComplete(new Result.Success<User>(User.fromMap(userData)));
                                        } else {
                                            callback.onComplete(new Result.Error<User>(new IOException("Error logging in" + task1.getException())));
                                        }
                                    })
                            );
                        } else {
                            callback.onComplete(new Result.Error<User>(new IOException("Error logging in" + task.getException())));
                        }
                    })
            );
        } catch (Exception e) {
            callback.onComplete(new Result.Error<User>(new IOException("Error logging in" + e)));
        }
    }

    public void register(String email, String password, String fullName, String base64Face, String natId,
                         ResultCallback<User> callback) {


        try {
            LOGGER.d("Authenticating....");
            User user = new User(
                    natId,
                    fullName,
                    email,
                    base64Face
            );
            mAuth.createUserWithEmailAndPassword(user.getEmail(), password)
                    .addOnCompleteListener
                            (task -> {
                                if (task.isSuccessful()) {
                                    String id = Objects.requireNonNull(task.getResult().getUser()).getUid();
                                    db.collection("users")
                                            .document(id)
                                            .set(user.toMap())
                                            .addOnSuccessListener(documentReference -> {
                                                LOGGER.d("Document added with ID: " + id);
                                                callback.onComplete(new Result.Success<User>(user));
                                            })
                                            .addOnFailureListener(e -> {
                                                LOGGER.w("Error adding document", e);
                                                callback.onComplete(new Result.Error<User>(new IOException("Error Creating new user", e)));
                                            });
                                } else {
                                    callback.onComplete(new Result.Error<User>(new IOException("Error Creating new user", task.getException())));
                                }
                            });

        } catch (Exception e) {
            //LOGGER.w("Error adding User" + cause.get().toString());
            callback.onComplete(new Result.Error<User>(new IOException("Error Creating new user", e)));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}