package com.example.ftpclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
        if (command == null || command.trim().isEmpty()) {
            System.out.println("Error: Command cannot be null or empty.");
            return;
        }

    	
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
    
    public void handleUpload(String command) {
        if (command == null || command.trim().isEmpty()) {
            System.out.println("Error: Command cannot be null or empty.");
            return;
        }
    	
    	String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            System.out.println("Usage: UPL <filename>");
            return;
        }
        
        String fileName = parts[1];
        File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            System.out.println("Error: File not found or is a directory.");
            return;
        }
        
        out.println("UPL " + fileName);
        
        try {
	        String response = in.readLine();
	        System.out.println(response);
	
	        if (response.startsWith("150")) { // Server is ready to receive file
	            try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
	                String line;
	                while ((line = fileReader.readLine()) != null) {
	                    out.println(line);
	                }
	                out.println("EOF"); // End-of-file marker
	                System.out.println(in.readLine()); // 226 Transfer complete
	            }    
	        }
        }
        catch(Exception ex) {
        	System.out.println("Caught exception: " + ex.toString());
        }
    }
    
    public void handleDelete(String command) throws IOException {
        String[] parts = command.split(" ", 2);
        if (parts.length < 2) {
            System.out.println("Usage: DELE <filename>");
            return;
        }

        String fileName = parts[1];
        out.println("DELE " + fileName);

        // Read server response
        String response = in.readLine();
        System.out.println("Server: " + response);

        if (response.startsWith("250")) {
            System.out.println("File '" + fileName + "' deleted successfully.");
        } else {
            System.out.println("Failed to delete file: " + response);
        }
    }

    public void quit() throws IOException {
        out.println("QUIT");
        System.out.println("Server: " + in.readLine());
    }
}
