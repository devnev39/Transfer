package Transfer2.Shared;

import java.net.Socket;
import java.nio.ByteBuffer;

import Transfer2.ClientEventHandler;
import Transfer2.ServerEventHandler;

public class Where extends Command {

    public Where(ServerEventHandler sHandler, String root) {
        super("where", sHandler, root);
    }

    public Where(ClientEventHandler cHandler){
        super("where", cHandler);
    }

    @Override
    public void executeClientProcedure(String input, Socket serverSocket) throws Exception {
        this.sendText(input, serverSocket);
        long len = this.ReceiveInputDataLenght(serverSocket);
        String root = this.ReceiveText(serverSocket, len);
        this.clientEventHandler.TextReceived(root);
    }

    @Override
    public void executeServerProcedure(String input, Socket clientSocket) throws Exception {
        byte[] data = this.Root.getBytes();
        clientSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong((long)data.length).array());
        clientSocket.getOutputStream().write(data);
    }

    @Override
    public String getInfo() {
        return "where   ->  current root for you\n";
    }
    
}
