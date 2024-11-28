package com.example.ftpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServer {
    private static final int PORT = 21;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("FTP server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Handle each client in a new thread
                new Thread(new ClientSession(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting FTP server: " + e.getMessage());
        }
    }
}
