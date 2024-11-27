package server;
import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class FTPServer {
    private static final int PORT = 21;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("FTP server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Handle client in a new thread
                new Thread(new FTPClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting FTP server: " + e.getMessage());
        }
    }
}

class FTPClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    public FTPClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("220 Simple FTP Server Ready");

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Received: " + line);

                StringTokenizer tokenizer = new StringTokenizer(line);
                String command = tokenizer.nextToken().toUpperCase();

                switch (command) {
                    case "USER":
                        handleUser(tokenizer);
                        break;
                    case "PASS":
                        handlePass(tokenizer);
                        break;
                    case "LIST":
                        handleList(tokenizer);
                        break;
                    case "RETR":
                        handleRetr(tokenizer);
                        break;
                    case "QUIT":
                        handleQuit();
                        return;
                    default:
                        out.println("500 Command not recognized");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Client connection error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void handleUser(StringTokenizer tokenizer) {
        out.println("331 Username OK, need password");
    }

    private void handlePass(StringTokenizer tokenizer) {
        out.println("230 User logged in");
    }

    private void handleList(StringTokenizer tokenizer) {
        File currentDir = new File(".");
        String[] files = currentDir.list();

        if (files != null) {
            for (String file : files) {
                out.println(file);
            }
        }
        out.println("226 List complete");
    }

    private void handleRetr(StringTokenizer tokenizer) {
        if (tokenizer.hasMoreTokens()) {
            String fileName = tokenizer.nextToken();
            File file = new File(fileName);

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
        } else {
            out.println("501 Missing file name");
        }
    }

    private void handleQuit() {
        out.println("221 Goodbye");
    }
}