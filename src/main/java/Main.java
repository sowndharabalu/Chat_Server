import client.ChatClient;
import server.ChatServer;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if(args.length>0 && args[0].equals("server")){
            try {
                 ChatServer server = new ChatServer(9090);
                 server.start();
            }catch(Exception e){
                e.printStackTrace();
            }
        }else {
            Scanner sc=new Scanner(System.in);
            System.out.println("Enter username:");
            String username=sc.nextLine();
            System.out.println("Enter server host(default localhost):");
            String host=sc.nextLine();
            if(host.trim().isEmpty()){
                host="localhost";
            }
            System.out.println("Enter server port:");
            String portStr=sc.nextLine();
            int port=9090;
            if(!portStr.trim().isEmpty()) {
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port number(use default 9090):" + portStr);
                }
            }
            ChatClient c=new ChatClient(host,port,username);
            c.start();
        }
    }
}
