package com.aso.ectvoting.ui.authentication.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

import com.aso.ectvoting.R;
import com.aso.ectvoting.databinding.ActivityLoginBinding;
import com.aso.ectvoting.ui.authentication.customview.LoggedInUserView;
import com.aso.ectvoting.ui.authentication.login.viewmodel.LoginViewModel;
import com.aso.ectvoting.ui.authentication.login.viewmodel.LoginViewModelFactory;
import com.aso.ectvoting.ui.authentication.register.RegisterActivity;
import com.aso.ectvoting.ui.customview.CustomProgressDialog;
import com.aso.ectvoting.ui.vote.VoteActivity;
import com.aso.ectvoting.ui.recognition.RecognitionActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private LoginResult loginResult;

    ActivityResultLauncher<Intent> mCheckFace = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    updateUiWithUser(Objects.requireNonNull(loginResult.getSuccess()));
                    Intent switchActivity = new Intent(this, VoteActivity.class);
                    startActivity(switchActivity);
                    finish();
                } else {
                    // TODO : Face not recognized
                    showLoginFailed(R.string.face_recognition_failed);
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Needed for the loading overlay
        CustomProgressDialog progressDialog = new CustomProgressDialog(this);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final TextInputEditText emailEditText = binding.editTextEmail;
        final TextInputEditText passwordEditText = binding.editTextPassword;
        final AppCompatButton loginButton = binding.cirLoginButton;
        final Button registerButton = binding.registerBtn;

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getEmailError() != null) {
                emailEditText.setError(getString(loginFormState.getEmailError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == null) {
                return;
            }
            progressDialog.stop();
            if (loginResult.getError() != null) {
                showLoginFailed(loginResult.getError());
            }
            if (loginResult.getSuccess() != null) {
                this.loginResult = loginResult;
                Intent switchActivity = new Intent(this, RecognitionActivity.class);
                switchActivity.putExtra("embeedings", loginResult.getSuccess().getBase64Face());
                switchActivity.putExtra("fullName", loginResult.getSuccess().getFullName());
                mCheckFace.launch(switchActivity);
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(emailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(emailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
            return false;
        });

        loginButton.setOnClickListener(v -> {
            if(!emailEditText.getText().toString().isEmpty() || !passwordEditText.getText().toString().isEmpty()){
                progressDialog.start(getString(R.string.logging_in));
                loginViewModel.login(emailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getFullName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}