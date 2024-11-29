package com.example.ftpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FTPServer {
    private static final int PORT = 21;
    private static final int THREAD_POOL_SIZE = 30;
    
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("FTP server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                executor.submit(new ClientSession(clientSocket)); 
            }
        } catch (IOException e) {
            System.err.println("Error starting FTP server: " + e.getMessage());
        }
        finally {
            executor.shutdown();
        }
    }
}
