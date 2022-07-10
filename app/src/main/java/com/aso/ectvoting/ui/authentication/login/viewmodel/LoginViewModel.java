package com.aso.ectvoting.ui.authentication.login.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Patterns;

import com.aso.ectvoting.R;
import com.aso.ectvoting.core.exception.AuthNoUserFoundException;
import com.aso.ectvoting.core.exception.AuthWrongCredentialsException;
import com.aso.ectvoting.core.exception.NetworkErrorException;
import com.aso.ectvoting.data.authentication.AuthenticationRepository;
import com.aso.ectvoting.data.Result;
import com.aso.ectvoting.data.models.User;
import com.aso.ectvoting.ui.authentication.customview.LoggedInUserView;
import com.aso.ectvoting.ui.authentication.login.LoginFormState;
import com.aso.ectvoting.ui.authentication.login.LoginResult;
import com.aso.ectvoting.ui.authentication.register.RegisterResult;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final AuthenticationRepository authenticationRepository;

    LoginViewModel(AuthenticationRepository authenticationRepository) {
        this.authenticationRepository = authenticationRepository;
    }

    public LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String email, String password) {
        // can be launched in a separate asynchronous job
        authenticationRepository.login(email, password, result -> {
            if (result instanceof Result.Success) {
                User data = ((Result.Success<User>) result).getData();
                loginResult.setValue(new LoginResult(new LoggedInUserView(data.getFullName(), data.getUserFaceBase64())));
            } else {
                if (((Result.Error<User>) result).getError() instanceof AuthWrongCredentialsException) {
                    loginResult.setValue(new LoginResult(R.string.wrong_credentials));
                } else if (((Result.Error<User>) result).getError() instanceof AuthNoUserFoundException) {
                    loginResult.setValue(new LoginResult(R.string.no_user_found));
                }  else if (((Result.Error<User>) result).getError() instanceof NetworkErrorException) {
                    loginResult.setValue(new LoginResult(R.string.network_error));
                } else {
                    loginResult.setValue(new LoginResult(R.string.login_failed));
                }
            }
        });
    }

    public void loginDataChanged(String email, String password) {
        @Nullable
        Integer emailError = null;
        @Nullable
        Integer passwordError = null;
        if (!isUserNameValid(email)) {
            emailError = R.string.invalid_email;
        }
        if (!isPasswordValid(password)) {
            passwordError =  R.string.invalid_password;
        }
        if(emailError == null && passwordError == null){
            loginFormState.setValue(new LoginFormState(true));
            return;
        }
        loginFormState.setValue(new LoginFormState(emailError, passwordError));
    }

    // A placeholder email validation check
    private boolean isUserNameValid(String email) {
        if (email == null) {
            return false;
        }
        if (email.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        } else {
            return !email.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}