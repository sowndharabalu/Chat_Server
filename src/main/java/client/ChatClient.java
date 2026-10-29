package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private final String host;
    private final int port;
    private final String username;

    public ChatClient(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public void start() {
        try (Socket client = new Socket(host, port);
             PrintWriter out = new PrintWriter(client.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));) {
            System.out.println("Connected to " + host + ":" + port);
            if (username == null) {
                throw new IllegalArgumentException("Username is null");
            }
            String serverMsg = in.readLine();
            if (serverMsg != null) {
                System.out.println("Server: " + serverMsg);
            }
            out.println(username);
            Thread reader = new Thread(() -> {
                try {
                    String newMsg;
                    while ((newMsg = in.readLine()) != null) {
                        System.out.println(newMsg);
                    }
                } catch (IOException e) {
                    System.out.println("Server connection lost");
                }
            });
            Thread writer = new Thread(() -> {
                try {
                    Scanner sc = new Scanner(System.in);
                    while (sc.hasNextLine()) {
                        String msg = sc.nextLine();
                        out.println(msg);
                        if ("/quit".equalsIgnoreCase(msg)) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            reader.start();
            writer.start();
            writer.join();
            System.out.println("Disconnected from " + host + ":" + port);
        } catch (InterruptedException | IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}