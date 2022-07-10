package com.aso.ectvoting.ui.authentication.register.viewmodel;

import android.util.Patterns;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aso.ectvoting.R;
import com.aso.ectvoting.core.exception.AuthEmailInUseException;
import com.aso.ectvoting.core.exception.NetworkErrorException;
import com.aso.ectvoting.data.Result;
import com.aso.ectvoting.data.authentication.AuthenticationRepository;
import com.aso.ectvoting.data.models.User;
import com.aso.ectvoting.ui.authentication.customview.LoggedInUserView;
import com.aso.ectvoting.ui.authentication.register.RegisterFormState;
import com.aso.ectvoting.ui.authentication.register.RegisterResult;

public class RegisterViewModel extends ViewModel {

    private final MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    private final MutableLiveData<RegisterResult> registerResult = new MutableLiveData<>();
    private final AuthenticationRepository authenticationRepository;

    RegisterViewModel(AuthenticationRepository authenticationRepository) {
        this.authenticationRepository = authenticationRepository;
    }

    public LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    public LiveData<RegisterResult> getRegisterResult() {
        return registerResult;
    }

    public void register(String email, String password, String fullName, String base64Face, String natID) {
        // can be launched in a separate asynchronous job
        authenticationRepository.register(email, password, fullName, base64Face, natID, result -> {
            if (result instanceof Result.Success) {
                User data = ((Result.Success<User>) result).getData();
                registerResult.setValue(new RegisterResult(new LoggedInUserView(data.getFullName(), base64Face)));
            } else {
                if (((Result.Error<User>) result).getError() instanceof AuthEmailInUseException) {
                    registerResult.setValue(new RegisterResult(R.string.email_in_use));
                } else if (((Result.Error<User>) result).getError() instanceof NetworkErrorException) {
                    registerResult.setValue(new RegisterResult(R.string.network_error));
                } else {
                    registerResult.setValue(new RegisterResult(R.string.register_failed));
                }
            }
        });

    }

    // TODO : Complete this
    public void registerDataChanged(String email, String password, String confirmPassword, String fullNAme, String natID) {
        @Nullable
        Integer emailError = null;
        @Nullable
        Integer passwordError = null;
        @Nullable
        Integer confirmPasswordError = null;
        @Nullable
        Integer fullNameError = null;
        @Nullable
        Integer natIDError = null;

        if (!isEmailValid(email)) {
            emailError = R.string.invalid_email;
        }
        if (!isPasswordValid(password)) {
            passwordError = R.string.invalid_password;
        }
        if (!isConfirmPasswordValid(password, confirmPassword)) {
            confirmPasswordError = R.string.confirm_password_mismatch;
        }
        if (!(fullNAme != null && !fullNAme.isEmpty())) {
            fullNameError = R.string.full_name_cant_be_empty;
        }
        if (!(natID != null && !natID.isEmpty())) {
            natIDError = R.string.nat_id_name_cant_be_empty;
        }
        if(
                emailError == null &&
                fullNameError == null &&
                natIDError == null &&
                passwordError == null &&
                confirmPasswordError == null
        ){
            registerFormState.setValue(new RegisterFormState(true));
            return;
        }
        registerFormState.setValue(new RegisterFormState(emailError,
                passwordError,
                confirmPasswordError,
                fullNameError,
                natIDError
        ));
    }

    // A placeholder username validation check
    private boolean isEmailValid(String email) {
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

    private boolean isConfirmPasswordValid(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }
}