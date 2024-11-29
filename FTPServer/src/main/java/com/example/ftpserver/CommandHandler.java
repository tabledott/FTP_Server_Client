package com.example.ftpserver;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CommandHandler {
    private final PrintWriter out;
    private final BufferedReader in;
    private static final Lock fileLock = new ReentrantLock();
    private final Map<String, Command> commands = new HashMap<>();

    public CommandHandler(Socket clientSocket) throws IOException {
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
    
    public void initializeCommands(StringTokenizer tokenizer, boolean authenticated, String sessionId) {
        commands.put("LIST", new ListCommand(out, fileLock));
        commands.put("RETR", new RetrieveCommand(out, tokenizer, sessionId, fileLock));
        commands.put("UPL", new UploadCommand(in, out, tokenizer, sessionId, fileLock));
        commands.put("DELE", new DeleteCommand(out, tokenizer, sessionId, fileLock));
        commands.put("QUIT", new QuitCommand(out, sessionId));
    }

    public void handleCommand(String command, boolean authenticated, String sessionId) throws IOException {
        StringTokenizer tokenizer = new StringTokenizer(command);
        String cmd = tokenizer.nextToken().toUpperCase();

        initializeCommands(tokenizer, authenticated, sessionId);

        Command commandHandler = commands.get(cmd);
        if (commandHandler != null) {
            commandHandler.execute(authenticated);
        } else {
            out.println("500 Command not recognized");
        }
    }
}
