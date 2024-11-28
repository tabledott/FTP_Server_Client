package com.example.ftpclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileManager {
    private final String fileName;
    private final BufferedReader in;

    public FileManager(String fileName, BufferedReader in) {
        this.fileName = fileName;
        this.in = in;
    }

    public void downloadFile() {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName))) {
            String line;
            while (!(line = in.readLine()).equals("226 Transfer complete")) {
                fileWriter.write(line);
                fileWriter.newLine();
            }
            System.out.println("File '" + fileName + "' downloaded successfully.");
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }
}
