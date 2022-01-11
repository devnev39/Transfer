package Transfer2;

import java.net.ServerSocket;
import java.net.Socket;

import Transfer2.FTPS.ServerEvents;

public class ServerEventHandler implements ServerEvents{

    @Override
    public void ServerStartedEventHandler(ServerSocket ss) {
        System.out.println("Server Started .....");
    }

    @Override
    public void exceptionInStartEventHandler(Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
    }

    @Override
    public void newConnectionEventHandler(Socket accept) {
        System.out.println("New Connection from : "+accept.getRemoteSocketAddress().toString());
    }

    public void commandReceived(String msg, Socket clientSocket) {
        System.out.println("Command received from "+clientSocket.getRemoteSocketAddress().toString()+" : "+msg);
    }
    
}
