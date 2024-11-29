package com.example.ftpserver;

import java.io.*;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;

public class UploadCommand implements Command {
    private final PrintWriter out;
    private final BufferedReader in;
    private final StringTokenizer tokenizer;
    private final Lock fileLock;
    private final String sessionId;
    
    public UploadCommand(BufferedReader in, PrintWriter out, 
    					 StringTokenizer tokenizer, String sessionId, Lock fileLock) {
        this.in = in;
    	this.out = out;
        this.tokenizer = tokenizer;
        this.sessionId = sessionId;
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
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file))) {
            out.println("350 Ready to receive file");

            String line;
            while (!(line = in.readLine()).equals("EOF")) { // Expect "EOF" as the end-of-file marker
                fileWriter.write(line);
                fileWriter.newLine();
            }

            out.println("226 File upload complete");
            MonitoringService.getInstance().logActivity(sessionId, "retr " + fileName);
        } catch (IOException e) {
            out.println("451 Error writing file: " + e.getMessage());
        } finally {
            fileLock.unlock();
        }
    }
}
