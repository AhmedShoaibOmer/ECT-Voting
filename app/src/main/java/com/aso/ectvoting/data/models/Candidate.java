package com.aso.ectvoting.data.models;

import java.util.HashMap;
import java.util.Map;

public class Candidate {
    private final String id;
    private final String fullName;
    private final long numOfVoters;

    public Candidate(String id, String fullName, long numOfVoters) {
        this.id = id;
        this.fullName = fullName;
        this.numOfVoters = numOfVoters;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public long getNumOfVoters() {
        return numOfVoters;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> candidate = new HashMap<>();
        candidate.put("fullName", fullName);
        candidate.put("numOFVoters", numOfVoters);
        return candidate;
    }

    public static Candidate fromMap(Map<String, Object> candidate) {
        return new Candidate(
                (String) candidate.get("id"),
                (String) candidate.get("fullName"),
                (long) candidate.get("numOfVoters")
        );
    }
}
