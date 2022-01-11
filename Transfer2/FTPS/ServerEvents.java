package Transfer2.FTPS;

import java.net.ServerSocket;
import java.net.Socket;

public interface ServerEvents {
    public void ServerStartedEventHandler(ServerSocket ss);
    public void newConnectionEventHandler(Socket accept);
    public void exceptionInStartEventHandler(Exception e);
    
}
