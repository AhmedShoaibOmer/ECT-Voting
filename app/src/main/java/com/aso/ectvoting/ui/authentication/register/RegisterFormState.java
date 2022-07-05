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
    public Integer getConfirmPasswordError() {
        return confirmPasswordError;
    }

    @Nullable
    private final Integer natIDError;
    @Nullable
    private final Integer base64FaceError;
    private final boolean isDataValid;

    public RegisterFormState(@Nullable Integer emailError, @Nullable Integer passwordError, @Nullable Integer confirmPasswordError, @Nullable Integer fullNameError, @Nullable Integer natIDError, @Nullable Integer base64FaceError) {
        this.emailError = emailError;
        this.passwordError = passwordError;
        this.confirmPasswordError = confirmPasswordError;
        this.fullNameError = fullNameError;
        this.natIDError = natIDError;
        this.base64FaceError = base64FaceError;
        this.isDataValid = false;
    }

    public RegisterFormState(boolean isDataValid) {
        this.confirmPasswordError = null;
        this.base64FaceError = null;
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
    public Integer getBase64FaceError() {
        return base64FaceError;
    }

    @Nullable
    Integer getPasswordError() {
        return passwordError;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}