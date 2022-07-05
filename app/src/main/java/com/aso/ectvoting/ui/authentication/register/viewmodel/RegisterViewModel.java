package com.aso.ectvoting.ui.authentication.register.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aso.ectvoting.R;
import com.aso.ectvoting.data.Result;
import com.aso.ectvoting.data.authentication.AuthenticationRepository;
import com.aso.ectvoting.data.models.User;
import com.aso.ectvoting.ui.authentication.customview.LoggedInUserView;
import com.aso.ectvoting.ui.authentication.register.RegisterFormState;
import com.aso.ectvoting.ui.authentication.register.RegisterResult;

public class RegisterViewModel extends ViewModel {

    private final MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    private final MutableLiveData<RegisterResult> RegisterResult = new MutableLiveData<>();
    private final AuthenticationRepository authenticationRepository;

    RegisterViewModel(AuthenticationRepository authenticationRepository) {
        this.authenticationRepository = authenticationRepository;
    }

    public LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    public LiveData<RegisterResult> getRegisterResult() {
        return RegisterResult;
    }

    public void register(String email, String password, String fullName,  String base64Face, String natID) {
        // can be launched in a separate asynchronous job
        Result<User> result = authenticationRepository.register(email, password, fullName, base64Face,natID);

        if (result instanceof Result.Success) {
            User data = ((Result.Success<User>) result).getData();
            RegisterResult.setValue(new RegisterResult(new LoggedInUserView(data.getFullName(), base64Face)));
        } else {
            RegisterResult.setValue(new RegisterResult(R.string.login_failed));
        }
    }

    // TODO : Complete this
    public void registerDataChanged(String email, String password, String confirmPassword, String fullNAme, String natID, String base64Image) {
        if (!isEmailValid(email)) {
            //registerFormState.setValue(new RegisterFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            //registerFormState.setValue(new RegisterFormState(null, R.string.invalid_password, confirmPasswordError, fullNameError, natIDError, base64FaceError));
        } else {
            //registerFormState.setValue(new RegisterFormState(fullNameError, true));
        }
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