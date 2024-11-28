package com.example.ftpserver;

import java.io.*;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;

public class RetrieveCommand implements Command {
    private final PrintWriter out;
    private final StringTokenizer tokenizer;
    private final Lock fileLock;

    public RetrieveCommand(PrintWriter out, StringTokenizer tokenizer, Lock fileLock) {
        this.out = out;
        this.tokenizer = tokenizer;
        this.fileLock = fileLock;
    }

    @Override
    public void execute(boolean authenticated) {
        if (!authenticated) {
            out.println("530 Not logged in. Please authenticate first.");
            return;
        }

        if (!tokenizer.hasMoreTokens()) {
            out.println("501 Missing file name");
            return;
        }

        String fileName = tokenizer.nextToken();
        File file = new File(fileName);

        fileLock.lock();
        try {
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
        } finally {
            fileLock.unlock();
        }
    }
}
