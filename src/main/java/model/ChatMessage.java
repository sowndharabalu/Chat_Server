package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ChatMessage( String sender, String content, LocalDateTime timestamp, MessageType type) {
    private final static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss a");
    public enum MessageType {
        BROADCAST,
        PRIVATE,
        SYSTEM
    }
    public ChatMessage{
        if (sender == null || sender.isBlank()) {
            throw new IllegalArgumentException("sender cannot be Empty");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content cannot be Empty");
        }
    }
    public static ChatMessage create(String sender, String content, MessageType type) {
        return new ChatMessage(sender, content, LocalDateTime.now(), type);
    }
    public String toDisplay(){
        return String.format("[%s] %s: %s", timestamp.format(dtf), sender, content);
    }
    public String toFile(){
        return String.format("%s|%s|%s|%s",sender, type, timestamp.format(dtf), content);
    }
}
