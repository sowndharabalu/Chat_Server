package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer chatServer;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, ChatServer chatServer) {
        this.socket = socket;
        this.chatServer = chatServer;
    }
    @Override
    public void run() {
        try {
            in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            boolean isAdded=false;
            while (!isAdded && running) {
                out.println("Enter username:");
                String name = in.readLine();
                if (name == null) {
                    return;
                }
                username = name.trim();
                if (username.isEmpty()) {
                    out.println("username can't be empty");
                    continue;
                }
                isAdded=chatServer.addUser(this);
                if (!isAdded) {
                    out.println("username "+username+" already taken. Try again");
                }
            }
            out.println("Welcome to the server, "+username+"! Type /list to see users.");
            String input;
            while (running && (input = in.readLine()) != null) {
                handleCommand(input.trim());
            }
        }catch (IOException e) {
            System.err.println("Connection error with "+username+": "+e.getMessage());
        }finally {
            handleDisconnect();
        }
    }

    public void sendMsg(String msg){
        if (out != null) {
            out.println(msg);
        }
    }

    public String getUsername() {
        return username;
    }

    private void handleCommand(String input) {
        if(input.isEmpty()) return;
        if(input.startsWith("/")){
            String[] parts=input.split(" ",3);
            String command=parts[0].toLowerCase();
            switch (command){
                case "/msg":
                    if(parts.length==3){
                        String msg=parts[2];
                        String user=parts[1];
                        chatServer.privateMsg(parts[1],parts[2],this);
                    }else {
                        sendMsg("SERVER: Invalid syntax. Use /msg <username> <message>");
                    }
                    break;
                case "/list":
                    sendMsg("SERVER: Connected users ->"+chatServer.getUsernames());
                    break;
                case "/quit":
                    running=false;
                    break;
                default:
                    sendMsg("Invalid command, please try again {use -> /list,/msg,/quit}");
            }
        }else {
            chatServer.broadcast(input,this);
        }
    }

    private void handleDisconnect() {
        running=false;
        chatServer.removeUser(this);
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        }catch (IOException e) {
            System.err.println("User "+username+" has been disconnected from Server: "+e.getMessage());
        }
    }
}
