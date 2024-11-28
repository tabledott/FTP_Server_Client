package com.example.ftpserver;

public interface Command {
    void execute(boolean authenticated);
}
