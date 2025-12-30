package com.exam.onlineexamsystem.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {
    private final Map<String, String> tokenToUsername = new ConcurrentHashMap<>();

    public void put(String token, String username) {
        tokenToUsername.put(token, username);
    }

    public String getUsername(String token) {
        return tokenToUsername.get(token);
    }

    public boolean remove(String token) {
        return tokenToUsername.remove(token) != null;
    }
}
