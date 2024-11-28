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
            System.out.println("Server: " + in.readLine());

            // Authenticate with username and password
            out.println("USER " + username);
            System.out.println("Server: " + in.readLine());
            out.println("PASS " + password);
            String authResponse = in.readLine();
            System.out.println("Server: " + authResponse);

            if (!authResponse.startsWith("230")) {
                System.out.println("Authentication failed. Exiting.");
                return;
            }

            // Enter FTP commands
            String command;
            while (true) {
                System.out.print("ftp> ");
                command = scanner.nextLine();

                if (command.equalsIgnoreCase("QUIT")) {
                    out.println(command);
                    System.out.println("Server: " + in.readLine());
                    break;
                }

                if (command.startsWith("RETR")) {
                    handleRetr(command, out, in);
                } else if (command.startsWith("LIST")) {
                    handleList(out, in);
                } else {
                    // Send generic command and print server response
                    out.println(command);
                    System.out.println("Server: " + in.readLine());
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleList(PrintWriter out, BufferedReader in) throws IOException {
        out.println("LIST");
        String response;
        while (!(response = in.readLine()).equals("226 Directory listing completed")) {
            System.out.println(response);
        }
        System.out.println("Server: " + response);
    }

    private static void handleRetr(String command, PrintWriter out, BufferedReader in) throws IOException {
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
            try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName))) {
                String line;
                while (!(line = in.readLine()).equals("226 Transfer complete")) {
                    fileWriter.write(line);
                    fileWriter.newLine();
                }
                System.out.println("Server: " + line);
                System.out.println("File '" + fileName + "' downloaded successfully.");
            } catch (IOException e) {
                System.err.println("Error saving file: " + e.getMessage());
            }
        } else {
            System.out.println("Failed to retrieve file: " + serverResponse);
        }
    }
}