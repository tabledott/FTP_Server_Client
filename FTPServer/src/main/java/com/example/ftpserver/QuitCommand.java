package com.example.ftpserver;

import java.io.PrintWriter;

public class QuitCommand implements Command {
    private final PrintWriter out;

    public QuitCommand(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void execute(boolean authenticated) {
        out.println("221 Goodbye");
    }
}
