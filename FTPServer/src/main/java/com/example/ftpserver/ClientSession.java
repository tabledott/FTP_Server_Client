package com.example.ftpserver;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class ClientSession implements Runnable {
    private final Socket clientSocket;
    private final AuthenticationManager authManager;
    private boolean authenticated = false;
    private String currentUsername;
    private final String sessionId;

    public ClientSession(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.authManager = new AuthenticationManager();
        this.sessionId = UUID.randomUUID().toString(); 
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            CommandHandler commandHandler = new CommandHandler(clientSocket);
            out.println("220 Simple FTP Server Ready");

            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                if (!authenticated) {
                    handleAuthentication(line, out);
                } else {
                    commandHandler.handleCommand(line, authenticated, sessionId);
                }
            }
        } catch (IOException e) {
            System.err.println("Client connection error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void handleAuthentication(String line, PrintWriter out) {
        String[] parts = line.split(" ", 2);
        String command = parts[0].toUpperCase();

        if (command.equals("USER") && parts.length > 1) {
            currentUsername = parts[1];
            if (authManager.isUserValid(currentUsername)) {
                out.println("331 Username OK, need password");
            } else {
                out.println("530 Invalid username");
                currentUsername = null;
            }
        } else if (command.equals("PASS") && parts.length > 1) {
        	final String password = parts[1];
            if (currentUsername != null && authManager.authenticate(currentUsername, password)) {
                authenticated = true;
                MonitoringService.getInstance().userLoggedIn(currentUsername);
                out.println("230 User logged in");
            } else {
                out.println("530 Authentication failed. Invalid password.");
            }
        } else {
            out.println("530 Not logged in. Please authenticate first.");
        }
    }
}
