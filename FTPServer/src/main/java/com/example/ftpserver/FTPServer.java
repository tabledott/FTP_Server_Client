package com.example.ftpserver;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class FTPServer {
    private static final int PORT = 21;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("FTP server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting FTP server: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                out.println("220 Simple FTP Server Ready");

                String commandLine;
                while ((commandLine = in.readLine()) != null) {
                    System.out.println("Received: " + commandLine);

                    StringTokenizer tokenizer = new StringTokenizer(commandLine);
                    String command = tokenizer.nextToken().toUpperCase();

                    switch (command) {
                        case "USER":
                            handleUser();
                            break;
                        case "PASS":
                            handlePass();
                            break;
                        case "LIST":
                            handleList();
                            break;
                        case "QUIT":
                            handleQuit();
                            return;
                        default:
                            out.println("500 Command not recognized");
                            break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }

        private void handleUser() {
            out.println("331 Username OK, need password");
        }

        private void handlePass() {
            out.println("230 User logged in");
        }

        private void handleList() {
            File currentDir = new File(".");
            String[] files = currentDir.list();

            if (files != null) {
                for (String file : files) {
                    out.println(file);
                }
            }
            out.println("226 Directory listing completed");
        }

        private void handleQuit() {
            out.println("221 Goodbye");
        }
    }
}
