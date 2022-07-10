package com.aso.ectvoting.data.authentication;

import com.aso.ectvoting.core.exception.AuthEmailInUseException;
import com.aso.ectvoting.core.exception.AuthNoUserFoundException;
import com.aso.ectvoting.core.exception.AuthWrongCredentialsException;
import com.aso.ectvoting.core.exception.NetworkErrorException;
import com.aso.ectvoting.data.Result;
import com.aso.ectvoting.data.ResultCallback;
import com.aso.ectvoting.data.models.User;
import com.aso.ectvoting.utils.Logger;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
                        if (task.isSuccessful()) {
                            String id = Objects.requireNonNull(task.getResult().getUser()).getUid();
                            db.collection("users").document(id).get().addOnCompleteListener(
                                    (task1 -> {
                                        if (task1.isSuccessful()) {
                                            Map<String, Object> userData = new HashMap<>(Objects.requireNonNull(task1.getResult().getData()));
                                            userData.put("id", id);
                                            callback.onComplete(new Result.Success<>(User.fromMap(userData)));
                                        } else {
                                            callback.onComplete(new Result.Error<>(new IOException("Error logging in" + task1.getException())));
                                        }
                                    })
                            );
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                callback.onComplete(new Result.Error<>(new AuthWrongCredentialsException()));
                            } else if(task.getException() instanceof FirebaseAuthInvalidUserException){
                                callback.onComplete(new Result.Error<>(new AuthNoUserFoundException()));
                            } else if(task.getException() instanceof FirebaseNetworkException){
                                callback.onComplete(new Result.Error<>(new NetworkErrorException()));
                            } else {
                                Objects.requireNonNull(task.getException()).printStackTrace();
                                callback.onComplete(new Result.Error<>(new IOException("Error logging in")));
                            }
                        }
                    })
            );
        } catch (Exception e) {
            callback.onComplete(new Result.Error<>(new IOException("Error logging in" + e)));
        }
    }

    public void register(String email, String password, String fullName, String base64Face, String natId,
                         ResultCallback<User> callback) {


        try {
            LOGGER.d("Authenticating....");
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener
                            (task -> {
                                if (task.isSuccessful()) {
                                    String id = Objects.requireNonNull(task.getResult().getUser()).getUid();
                                    User user = new User(
                                            id, natId,
                                            fullName,
                                            email,
                                            base64Face,
                                            null);
                                    db.collection("users")
                                            .document(id)
                                            .set(user.toMap())
                                            .addOnSuccessListener(documentReference -> {
                                                LOGGER.d("Document added with ID: " + id);
                                                callback.onComplete(new Result.Success<>(user));
                                            })
                                            .addOnFailureListener(e -> {
                                                e.printStackTrace();
                                                LOGGER.w("Error adding document", e);
                                                callback.onComplete(new Result.Error<>(new IOException("Error Creating new user", e)));
                                            });
                                } else {
                                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                        callback.onComplete(new Result.Error<>(new AuthEmailInUseException()));
                                    } else if(task.getException() instanceof FirebaseNetworkException){
                                        callback.onComplete(new Result.Error<>(new NetworkErrorException()));
                                    } else {
                                        Objects.requireNonNull(task.getException()).printStackTrace();
                                        callback.onComplete(new Result.Error<>(new IOException("Error Creating new user", task.getException())));
                                    }
                                }
                            });

        } catch (Exception e) {
            //LOGGER.w("Error adding User" + cause.get().toString());
            callback.onComplete(new Result.Error<>(new IOException("Error Creating new user", e)));
        }
    }

    public void logout(ResultCallback<Void> callback) {
        mAuth.signOut();
        callback.onComplete(new Result.Success<>(null));
    }
}