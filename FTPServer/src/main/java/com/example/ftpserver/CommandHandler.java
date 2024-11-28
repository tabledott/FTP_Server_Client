package com.example.ftpserver;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

public class CommandHandler {
    private final PrintWriter out;
    private final BufferedReader in;

    public CommandHandler(Socket clientSocket) throws IOException {
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void handleCommand(String command, boolean authenticated) throws IOException {
        StringTokenizer tokenizer = new StringTokenizer(command);
        String cmd = tokenizer.nextToken().toUpperCase();

        switch (cmd) {
            case "LIST":
                if (authenticated) {
                    handleList();
                } else {
                    out.println("530 Not logged in. Please authenticate first.");
                }
                break;

            case "RETR":
                if (authenticated) {
                    handleRetr(tokenizer);
                } else {
                    out.println("530 Not logged in. Please authenticate first.");
                }
                break;

            case "DELE":
                handleDelete(tokenizer);
                break;

            case "QUIT":
                handleQuit();
                break;

            default:
                out.println("500 Command not recognized");
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
    
    private void handleDelete(StringTokenizer tokenizer) {
        if (!tokenizer.hasMoreTokens()) {
            out.println("501 Missing file name");
            return;
        }

        String fileName = tokenizer.nextToken();
        File file = new File(fileName);

        if (file.exists() && !file.isDirectory()) {
            if (file.delete()) {
                out.println("250 File deleted successfully");
            } else {
                out.println("550 Unable to delete file. Permission denied.");
            }
        } else {
            out.println("550 File not found or is a directory.");
        }
    }


    private void handleQuit() {
        out.println("221 Goodbye");
    }
}
