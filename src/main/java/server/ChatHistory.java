package server;

import model.ChatMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ChatHistory {
    private final Path path;
    public ChatHistory(String path) {
        this.path = Paths.get(path);
    }
    public synchronized void save(ChatMessage message){
        try{
            Files.writeString(path,message.toFile(), StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            Files.writeString(path,"\n", StandardOpenOption.APPEND);
        }catch (IOException e){
            System.err.println("Failed to write: "+e.getMessage());
        }
    }
}
