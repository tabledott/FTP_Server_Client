package com.example.ftpclient;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;

public class AuthenticationManager {
    private final PrintWriter out;
    private final BufferedReader in;

    public AuthenticationManager(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
    }

    public boolean authenticate(String username, String password) throws IOException {
        out.println("USER " + username);
        System.out.println("Server: " + in.readLine());

        out.println("PASS " + password);
        String authResponse = in.readLine();
        System.out.println("Server: " + authResponse);

        return authResponse.startsWith("230");
    }
}
