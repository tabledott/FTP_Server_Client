package com.example.ftpserver;

import java.io.File;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;

public class DeleteCommand implements Command {
    private final PrintWriter out;
    private final StringTokenizer tokenizer;
    private final Lock fileLock;
    private final String sessionID;

    public DeleteCommand(PrintWriter out, StringTokenizer tokenizer, 
    					 String sessionId, Lock fileLock) {
        this.out = out;
        this.tokenizer = tokenizer;
        this.sessionID = sessionId;
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
                if (file.delete()) {
                    MonitoringService.getInstance().logActivity(sessionID, "del " + fileName);
                    out.println("250 File deleted successfully");
                } else {
                    out.println("550 Unable to delete file. Permission denied.");
                }
            } else {
                out.println("550 File not found or is a directory.");
            }
        } finally {
            fileLock.unlock();
        }
    }
}
