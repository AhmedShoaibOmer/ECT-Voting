package com.aso.ectvoting.ui.authentication.register;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
public class RegisterFormState {
    @Nullable
    private final Integer emailError;
    @Nullable
    private final Integer passwordError;
    @Nullable
    private final Integer confirmPasswordError;
    @Nullable
    private final Integer fullNameError;
    @Nullable
    private final Integer natIDError;

    @Nullable
    public Integer getConfirmPasswordError() {
        return confirmPasswordError;
    }

    private final boolean isDataValid;

    public RegisterFormState(@Nullable Integer emailError,
                             @Nullable Integer passwordError,
                             @Nullable Integer confirmPasswordError,
                             @Nullable Integer fullNameError,
                             @Nullable Integer natIDError) {
        this.emailError = emailError;
        this.passwordError = passwordError;
        this.confirmPasswordError = confirmPasswordError;
        this.fullNameError = fullNameError;
        this.natIDError = natIDError;
        this.isDataValid = false;
    }

    public RegisterFormState(boolean isDataValid) {
        this.confirmPasswordError = null;
        this.natIDError = null;
        this.fullNameError = null;
        this.emailError = null;
        this.passwordError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    Integer getEmailError() {
        return emailError;
    }

    @Nullable
    public Integer getFullNameError() {
        return fullNameError;
    }

    @Nullable
    public Integer getNatIDError() {
        return natIDError;
    }

    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}