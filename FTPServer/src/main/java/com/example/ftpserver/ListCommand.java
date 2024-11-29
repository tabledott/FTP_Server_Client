package com.example.ftpserver;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.locks.Lock;

public class ListCommand implements Command {
    private final PrintWriter out;
    private final Lock fileLock;
    
    public ListCommand(PrintWriter out, Lock fileLock) {
        this.out = out;
        this.fileLock = fileLock;
    }

    @Override
    public void execute(boolean authenticated) {
        if (!authenticated) {
            out.println("530 Not logged in. Please authenticate first.");
            return;
        }

        File currentDir = new File(".");
        String[] files = currentDir.list();

        fileLock.lock();
        try {
            if (files != null) {
                out.println("150 Here comes the directory listing");
                
                for (String file : files) {
                    out.println(file);
                }
                out.println("226 Directory listing completed");
            } else {
                out.println("450 Unable to list directory");
            }
        } finally {
            fileLock.unlock();
        }
    }
}
