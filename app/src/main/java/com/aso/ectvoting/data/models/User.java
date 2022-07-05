package com.aso.ectvoting.data.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class User {

    private final String natID;
    private final String fullName;
    private final String email;
    private final String userFaceBase64;

    public User(String natID, String fullName, String email, String userFaceBase64) {
        this.natID = natID;
        this.fullName = fullName;
        this.email = email;
        this.userFaceBase64 = userFaceBase64;
    }

    public String getNatID() {
        return natID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String userFaceBase64() {
        return userFaceBase64;
    }

    public Map<String, String> toMap() {
        Map<String, String> user = new HashMap<>();
        user.put("natID", natID);
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("userFaceBase64", userFaceBase64);
        return user;
    }

    public static User fromMap(Map<String, Object> user) {
        return new User(
                (String) user.get("natID"),
                (String) user.get("fullName"),
                (String) user.get("email"),
                (String) user.get("userFaceBase64")
        );
    }
}