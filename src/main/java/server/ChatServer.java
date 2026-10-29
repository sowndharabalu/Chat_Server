package server;

import model.ChatMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ChatServer {
    private final int port;
    private final Set<ClientHandler> clients;
    private final ExecutorService executors;
    private final MessageBroadcast broadcaster;
    private final ChatHistory history;
    private volatile boolean running = true;
    public ChatServer(int port) {
        this.port = port;
        this.clients = ConcurrentHashMap.newKeySet();
        this.executors = Executors.newCachedThreadPool();
        this.broadcaster = new MessageBroadcast(clients);
        this.history=new ChatHistory("src/main/chat_history.txt");
    }

    public void start(){
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        try (ServerSocket server=new ServerSocket(port)){
            System.out.println("Server started. Listening on port "+port);
            while (running) {
                Socket client=server.accept();
                System.out.println("New client connected: "+client.getRemoteSocketAddress());
                ClientHandler handler=new ClientHandler(client,this);
                executors.submit(handler);
            }
        }catch (IOException e){
            if(running){
                System.err.println("Server error: "+e.getMessage());
            }
        }
    }

    public void broadcast(String content, ClientHandler sender){
        String senderName=(sender==null) ? "SERVER" : sender.getUsername();
        ChatMessage.MessageType type=(sender==null) ? ChatMessage.MessageType.SYSTEM : ChatMessage.MessageType.BROADCAST;
        ChatMessage msg=ChatMessage.create(senderName,content,type);
        broadcaster.broadcast(msg,sender);
        history.save(msg);
    }

    public void privateMsg(String receiver, String message, ClientHandler sender){
        ChatMessage msg=ChatMessage.create(sender.getUsername(),message,ChatMessage.MessageType.PRIVATE);
        boolean delivered=broadcaster.sendToUser(receiver,msg);
        if(!delivered){
            sender.sendMsg("User "+sender.getUsername()+" is not online");
        }else {
            history.save(msg);
        }
    }

    public boolean addUser(ClientHandler handler){
        String username=handler.getUsername();
        for(ClientHandler client:clients){
            if(client.getUsername()!=null && client.getUsername().equalsIgnoreCase(username)){
                return false;
            }
        }
        clients.add(handler);
        broadcast(username+" has joined the chat.",null);
        return true;
    }

    public void removeUser(ClientHandler client){
        if(clients.remove(client)){
            if(client.getUsername()!=null){
                broadcast(client.getUsername()+" has left the chat.",null);
            }
        }
    }

    public Set<String> getUsernames(){
        return clients.stream().map(ClientHandler::getUsername).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public void shutdown(){
        running = false;
        for(ClientHandler client:clients){
            client.sendMsg("Server is shutting down...");
        }
        clients.clear();
        executors.shutdown();
        System.out.println("Server shut down complete.");
    }
}
