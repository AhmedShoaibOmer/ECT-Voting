package com.aso.ectvoting.data.authentication;

import com.aso.ectvoting.data.Result;
import com.aso.ectvoting.data.models.User;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class AuthenticationRepository {

    private static volatile AuthenticationRepository instance;

    private final AuthenticationDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private User user = null;

    // private constructor : singleton access
    private AuthenticationRepository(AuthenticationDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static AuthenticationRepository getInstance(AuthenticationDataSource dataSource) {
        if (instance == null) {
            instance = new AuthenticationRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    private void setLoggedInUser(User user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public Result<User> login(String email, String password) {
        // handle login
        Result<User> result = dataSource.login(email, password);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<User>) result).getData());
        }
        return result;
    }

    public Result<User> register(String email, String password, String fullName, String base64Face, String natID) {
        // handle login
        Result<User> result = dataSource.register(email, password, fullName, base64Face, natID);
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<User>) result).getData());
        }
        return result;
    }
}