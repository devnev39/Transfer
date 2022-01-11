package Transfer2.Shared;

import java.net.Socket;
import java.nio.ByteBuffer;

import Transfer2.ClientEventHandler;
import Transfer2.ServerEventHandler;

public class LS extends Command {

    public LS(ClientEventHandler cHandler) {
        super("ls",cHandler);
    }

    public LS(ServerEventHandler sHandler,String root) {
        super("ls",sHandler,root);
    }

    @Override
    public void executeClientProcedure(String input, Socket serverSocket) throws Exception{
        this.sendText(input,serverSocket);
        try {
            int flag = this.ReceiveStatusFlag(serverSocket);
            long len = this.ReceiveInputDataLenght(serverSocket);
            if(flag==-1){
                Exception e = this.ReceieveException(serverSocket, len);
                this.clientEventHandler.ServerExceptionOccured(e);
            }else{
                String data = this.ReceiveText(serverSocket, len);
                this.clientEventHandler.TextReceived(data);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void executeServerProcedure(String input, Socket clientSocket) throws Exception {
        int flag = 0;
        Exception exc = null;
        String out = "";
        byte[] buffer = null;
        try {
            out = this.GetFileFolders(this.Root);
            flag = 1;
        } catch (Exception e) {
            exc = e;
            flag = -1;
        }
        if(flag==-1){
            buffer = this.ObjectToByteArray(exc);
        }else{
            buffer = out.getBytes();
        }
        clientSocket.getOutputStream().write(ByteBuffer.allocate(4).putInt(flag).array());
        clientSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong((long)buffer.length).array());
        clientSocket.getOutputStream().write(buffer);
    }

    @Override
    public String getInfo() {
        return "ls  ->  list directory\n";
    }
}
