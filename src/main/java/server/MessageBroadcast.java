package server;

import model.ChatMessage;

import java.util.Set;

public class MessageBroadcast {
    private final Set<ClientHandler> clients;

    public MessageBroadcast(Set<ClientHandler> clients) {
        this.clients = clients;
    }

    public void broadcast(ChatMessage message, ClientHandler exclude) {
        for(ClientHandler client:clients){
            if(client!=(exclude) && client.getUsername()!=null){
                client.sendMsg(message.toDisplay());
            }
        }
    }

    public boolean sendToUser(String username, ChatMessage message) {
        for (ClientHandler client:clients){
            if(client.getUsername()!=null && client.getUsername().equalsIgnoreCase(username)){
                client.sendMsg("[Private]"+message.toDisplay());
                return true;
            }
        }
        return false;
    }
}