package Transfer2.Client;

import java.net.Socket;

public interface ClientEvents {
    public void TextReceived(String text);
    public void ServerExceptionOccured(Exception e);
    public void ClientExceptionOccured(Socket socket, Exception e);
    public String GetCommandFromUser();
    public void CommandNotFound(String cmd);
    public void FileNotExistServerError(String string);
}
