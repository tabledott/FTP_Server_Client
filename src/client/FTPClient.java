package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FTPClient {
    private static final String SERVER = "127.0.0.1"; // Change to your FTP server address
    private static final int PORT = 21;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to FTP server at " + SERVER + ":" + PORT);

            // Read and display the server's welcome message
            System.out.println("Server: " + in.readLine());

            String command;
            while (true) {
                System.out.print("ftp> ");
                command = scanner.nextLine();

                if (command.equalsIgnoreCase("QUIT")) {
                    out.println(command);
                    System.out.println("Server: " + in.readLine());
                    break;
                }

                out.println(command);

                // Handle LIST and RETR separately to display their output
                if (command.startsWith("LIST")) {
                    String response;
                    while (!(response = in.readLine()).equals("226 List complete")) {
                        System.out.println(response);
                    }
                    System.out.println("Server: " + response);
                } else if (command.startsWith("RETR")) {
                    String fileName = command.split(" ", 2)[1];
                    receiveFile(fileName, in);
                } else {
                    // Generic response for other commands
                    System.out.println("Server: " + in.readLine());
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void receiveFile(String fileName, BufferedReader in) {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(fileName))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("226 Transfer complete")) {
                    System.out.println("Server: " + line);
                    break;
                }
                fileWriter.write(line);
                fileWriter.newLine();
            }
            System.out.println("File " + fileName + " received successfully.");
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }
}