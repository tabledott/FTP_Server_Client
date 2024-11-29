package com.example.ftpserver;

import java.io.PrintWriter;

public class QuitCommand implements Command {
    private final PrintWriter out;
    private final String sessionId;
    
    public QuitCommand(PrintWriter out, String sessionId) {
        this.out = out;
        this.sessionId = sessionId;
    }

    @Override
    public void execute(boolean authenticated) {
        out.println("221 Goodbye");
        MonitoringService.getInstance().userLoggedOut(sessionId);
    }
}
