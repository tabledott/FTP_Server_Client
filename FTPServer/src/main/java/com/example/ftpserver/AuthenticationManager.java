package com.example.ftpserver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class AuthenticationManager {
    private static final Map<String, String> credentials = new ConcurrentHashMap<>();

    static {
        credentials.put("user1", "password1");
        credentials.put("user2", "password2");
        credentials.put("user3", "password3");
        credentials.put("user4", "password4");
        credentials.put("user5", "password5");
        credentials.put("admin", "adminpass");
    }

    public boolean isUserValid(String username) {
        return credentials.containsKey(username);
    }

    public boolean authenticate(String username, String password) {
        return credentials.getOrDefault(username, "").equals(password);
    }
}
