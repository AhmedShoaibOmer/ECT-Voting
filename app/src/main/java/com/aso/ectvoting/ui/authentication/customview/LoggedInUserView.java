package com.aso.ectvoting.ui.authentication.customview;

/**
 * Class exposing authenticated user details to the UI.
 */
public class LoggedInUserView {
    private final String fullName;
    private final String base64Face;
    //... other data fields that may be accessible to the UI

    public LoggedInUserView(String fullName, String base64Face) {
        this.fullName = fullName;
        this.base64Face = base64Face;
    }

    public String getFullName() {
        return fullName;
    }

    public String getBase64Face() {
        return base64Face;
    }
}