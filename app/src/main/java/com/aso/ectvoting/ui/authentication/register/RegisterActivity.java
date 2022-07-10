package com.aso.ectvoting.ui.authentication.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;

import com.aso.ectvoting.R;
import com.aso.ectvoting.databinding.ActivityRegisterBinding;
import com.aso.ectvoting.ui.authentication.customview.LoggedInUserView;
import com.aso.ectvoting.ui.authentication.login.LoginActivity;
import com.aso.ectvoting.ui.authentication.register.viewmodel.RegisterViewModel;
import com.aso.ectvoting.ui.authentication.register.viewmodel.RegisterViewModelFactory;
import com.aso.ectvoting.ui.customview.CustomProgressDialog;
import com.aso.ectvoting.ui.home.HomeActivity;
import com.aso.ectvoting.ui.vote.VoteActivity;
import com.aso.ectvoting.ui.recognition.DetectorActivity;
import com.aso.ectvoting.utils.Logger;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private static final Logger LOGGER = new Logger();

    private RegisterViewModel registerViewModel;

    private TextInputEditText fullNameEditText;
    private TextInputEditText natIDEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;

    private String embeedings;

    ActivityResultLauncher<Intent> mGetBase64Face = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        embeedings = result.getData().getStringExtra("embeedings");
                        LOGGER.d("User Embeedings String : " + embeedings);
                        registerViewModel.registerDataChanged(emailEditText.getText().toString(),
                                passwordEditText.getText().toString(),
                                confirmPasswordEditText.getText().toString(),
                                fullNameEditText.getText().toString(),
                                natIDEditText.getText().toString()
                        );
                    } else {
                        LOGGER.d("Use Embeedings String retrieval failed ");
                    }
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityRegisterBinding binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Needed for the loading overlay
        CustomProgressDialog progressDialog = new CustomProgressDialog(this);

        registerViewModel = new ViewModelProvider(this, new RegisterViewModelFactory())
                .get(RegisterViewModel.class);

        fullNameEditText = binding.editTextFullName;
        natIDEditText = binding.editTextNatID;
        emailEditText = binding.editTextEmail;
        passwordEditText = binding.editTextPassword;
        confirmPasswordEditText = binding.editTextConfirmPassword;
        final Button faceCaptureButton = binding.faceCaptureButton;
        final AppCompatButton registerButton = binding.cirRegisterButton;
        final Button signInButton = binding.signInBtn;

        registerViewModel.getRegisterFormState().observe(this, registerFormState -> {
            if (registerFormState == null) {
                return;
            }
            registerButton.setEnabled(registerFormState.isDataValid());
            if (registerFormState.getEmailError() != null) {
                emailEditText.setError(getString(registerFormState.getEmailError()));
            }
            if (registerFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(registerFormState.getPasswordError()));
                confirmPasswordEditText.setError(getString(registerFormState.getPasswordError()));
            }
            if (registerFormState.getConfirmPasswordError() != null) {
                confirmPasswordEditText.setError(getString(registerFormState.getConfirmPasswordError()));
            }
            if (registerFormState.getFullNameError() != null) {
                fullNameEditText.setError(getString(registerFormState.getFullNameError()));
            }
            if (registerFormState.getNatIDError() != null) {
                natIDEditText.setError(getString(registerFormState.getNatIDError()));
            }
        });

        registerViewModel.getRegisterResult().observe(this, registerResult -> {
            if (registerResult == null) {
                return;
            }
            progressDialog.stop();
            if (registerResult.getError() != null) {
                showRegistrationFailed(registerResult.getError());
            }
            if (registerResult.getSuccess() != null) {
                Intent switchActivity = new Intent(this, HomeActivity.class);
                startActivity(switchActivity);
                finish();
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
                registerViewModel.registerDataChanged(emailEditText.getText().toString(),
                        passwordEditText.getText().toString(),
                        confirmPasswordEditText.getText().toString(),
                        fullNameEditText.getText().toString(),
                        natIDEditText.getText().toString()
                );
            }
        };
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        confirmPasswordEditText.addTextChangedListener(afterTextChangedListener);
        fullNameEditText.addTextChangedListener(afterTextChangedListener);
        natIDEditText.addTextChangedListener(afterTextChangedListener);

        faceCaptureButton.setOnClickListener(
                v -> {
                    Intent switchActivityIntent = new Intent(this, DetectorActivity.class);
                    mGetBase64Face.launch(switchActivityIntent);
                }
        );
        registerButton.setOnClickListener(v -> {
            if (embeedings != null && !embeedings.isEmpty()) {
                progressDialog.start(getString(R.string.registering));
                registerViewModel.register(emailEditText.getText().toString(),
                        passwordEditText.getText().toString(),
                        fullNameEditText.getText().toString(),
                        embeedings,
                        natIDEditText.getText().toString()
                );
            } else {
                showRegistrationFailed(R.string.capture_face_prompt
                );
            }
        });

        signInButton.setOnClickListener(v -> {
            Intent switchActivityIntent = new Intent(this, LoginActivity.class);
            startActivity(switchActivityIntent);
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getFullName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showRegistrationFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}