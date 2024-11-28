package com.example.ftpserver;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class FTPServer {
    private static final int PORT = 21;

    // Predefined username-password combinations for authentication
    private static final Map<String, String> credentials = new HashMap<>();

    static {
        credentials.put("user1", "password1");
        credentials.put("user2", "password2");
        credentials.put("user3", "password3");
        credentials.put("user4", "password4");
        credentials.put("user5", "password5");
        credentials.put("admin", "adminpass");
    }
    
    public static boolean hasUser(String userName) {
    	return credentials.containsKey(userName);
    }
    
    public static boolean isPasswordCorrect(String userName, String password){
    	return credentials.get(userName).equals(password);
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("FTP server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Handle each client in a new thread
                new Thread(new FTPClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting FTP server: " + e.getMessage());
        }
    }
}

class FTPClientHandler implements Runnable {
    private final Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    private boolean authenticated = false;
    private String currentUsername;

    public FTPClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("220 Simple FTP Server Ready");

            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                StringTokenizer tokenizer = new StringTokenizer(line);
                String command = tokenizer.nextToken().toUpperCase();

                if (!authenticated) {                	
                    if (command.equals("USER")) {
                        handleUser(tokenizer);
                    } else if (command.equals("PASS")) {
                        handlePass(tokenizer);
                    } else {
                        out.println("530 Not logged in. Please authenticate first.");
                    }
                } else {
                	switch (command) {
                        case "LIST":
                            handleList();
                            break;
                        case "RETR":
                            handleRetr(tokenizer);
                            break;
                        case "QUIT":
                            handleQuit();
                            return;
                        default:
                            out.println("500 Command not recognized");
                            break;
                    }
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

    private void handleUser(StringTokenizer tokenizer) {
        if (!tokenizer.hasMoreTokens()) {
            out.println("501 Missing username");
            return;
        }

        currentUsername = tokenizer.nextToken();
        if (FTPServer.hasUser(currentUsername)) {
            out.println("331 Username OK, need password");
        } else {
            out.println("530 Invalid username");
            currentUsername = null;
        }
    }

    private void handlePass(StringTokenizer tokenizer) {
        if (currentUsername == null) {
            out.println("503 Bad sequence of commands. Provide username first.");
            return;
        }

        if (!tokenizer.hasMoreTokens()) {
            out.println("501 Missing password");
            return;
        }

        String password = tokenizer.nextToken();
        if (FTPServer.isPasswordCorrect(currentUsername, password)) {
            authenticated = true;
            out.println("230 User logged in");
        } else {
            out.println("530 Authentication failed. Invalid password.");
            currentUsername = null;
        }
    }

    private void handleList() {
        File currentDir = new File(".");
        String[] files = currentDir.list();

        if (files != null) {
            out.println("150 Here comes the directory listing");
            for (String file : files) {
                out.println(file);
            }
            out.println("226 Directory listing completed");
        } else {
            out.println("450 Unable to list directory");
        }
    }

    private void handleRetr(StringTokenizer tokenizer) {
        if (!tokenizer.hasMoreTokens()) {
            out.println("501 Missing file name");
            return;
        }

        String fileName = tokenizer.nextToken();
        File file = new File(fileName);

        if (file.exists() && !file.isDirectory()) {
            out.println("150 Opening data connection for file transfer");
            try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    out.println(line);
                }
                out.println("226 Transfer complete");
            } catch (IOException e) {
                out.println("451 Error reading file");
            }
        } else {
            out.println("550 File not found");
        }
    }

    private void handleQuit() {
        out.println("221 Goodbye");
    }
}