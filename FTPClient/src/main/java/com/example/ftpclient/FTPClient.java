package com.example.ftpclient;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FTPClient {
    private static final String SERVER = "127.0.0.1"; // FTP server address
    private static final int PORT = 21; // FTP server port

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java FTPClient <username> <password>");
            return;
        }

        String username = args[0];
        String password = args[1];

        try (Socket socket = new Socket(SERVER, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to FTP server at " + SERVER + ":" + PORT);

            // Read and display the server's welcome message
            String welcomeMessage = in.readLine();
            System.out.println("Server: " + welcomeMessage);

            // Authenticate with username and password
            AuthenticationManager authManager = new AuthenticationManager(out, in);
            if (!authManager.authenticate(username, password)) {
                System.out.println("Authentication failed. Exiting.");
                return;
            }

            System.out.println("Possible commands: QUIT, RETR, UPL, DELE, LIST");
            
            // Handle FTP commands
            FTPCommandExecutor commandExecutor = new FTPCommandExecutor(out, in);
            while (true) {
                System.out.print("ftp> ");
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("QUIT")) {
                    commandExecutor.quit();
                    break;
                }
                if (command.startsWith("RETR")) {
                    commandExecutor.handleRetr(command);
                } else if (command.startsWith("DELE")) {
                    commandExecutor.handleDelete(command);
                }
                else if (command.startsWith("LIST")) {
                    commandExecutor.handleList();
                } else if (command.startsWith("UPL")) {
                	commandExecutor.handleUpload(command);
                }
                else {
                    commandExecutor.executeCommand(command);
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
