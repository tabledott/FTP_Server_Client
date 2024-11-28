package com.example.ftpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class FTPCommandExecutor {
    private final PrintWriter out;
    private final BufferedReader in;

    public FTPCommandExecutor(PrintWriter out, BufferedReader in) {
        this.out = out;
        this.in = in;
    }

    public void executeCommand(String command) throws IOException {
        out.println(command);
        String response = in.readLine();
        System.out.println("Server: " + response);
    }

    public void handleList() throws IOException {
        out.println("LIST");
        String response;
        while (!(response = in.readLine()).equals("226 Directory listing completed")) {
            System.out.println(response);
        }
        System.out.println("Server: " + response);
    }

    public void handleRetr(String command) throws IOException {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            System.out.println("Usage: RETR <filename>");
            return;
        }

        String fileName = parts[1];
        out.println("RETR " + fileName);

        // Read server response
        String serverResponse = in.readLine();
        System.out.println("Server: " + serverResponse);

        if (serverResponse.startsWith("150")) { // File transfer about to start
            FileManager fileManager = new FileManager(fileName, in);
            fileManager.downloadFile();
        } else {
            System.out.println("Failed to retrieve file: " + serverResponse);
        }
    }

    public void quit() throws IOException {
        out.println("QUIT");
        System.out.println("Server: " + in.readLine());
    }
}
