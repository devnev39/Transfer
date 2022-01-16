package Transfer2.Shared;

import java.io.File;
import java.net.Socket;
import java.nio.ByteBuffer;

import Transfer2.ServerEventHandler;
import Transfer2.ClientEventHandler;

public class Size extends Command {

    public Size(ClientEventHandler cHandler) {
        super("size", cHandler);
    }

    public Size(ServerEventHandler cHandler, String root) {
        super("size", cHandler,root);
    }

    @Override
    public void executeClientProcedure(String input, Socket serverSocket) throws Exception {
        String[] vals = remove(input.split(" "),0);
        if(vals.length==0){
            throw new Exception("Nothing to get size !");
        }
        vals = String.join(" ", vals).split(",");
        for (String val : vals) {
            sendText("size "+val, serverSocket);
            int flag = this.ReceiveStatusFlag(serverSocket);
            if(flag==1){
                long len = this.ReceiveInputDataLenght(serverSocket);
                float meg = (float)len / 1048576.0f;
                this.clientEventHandler.TextReceived(val+"\t"+meg+" MB");
            }else{
                this.clientEventHandler.FileNotExistServerError(val);
            }
        }
    }

    @Override
    public void executeServerProcedure(String input, Socket clientSocket) throws Exception {
        String[] inp = remove(input.split(" "),0);        
        String val = String.join(" ", inp);
        File f = new File(this.Root+File.separator+val);
        if(f.exists()){
            long len = f.length();
            clientSocket.getOutputStream().write(ByteBuffer.allocate(4).putInt(1).array());
            clientSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong(len).array());
        }else{
            clientSocket.getOutputStream().write(ByteBuffer.allocate(4).putInt(-1).array());
        }
    }

    @Override
    public String getInfo() {
        return "size  ->  gets the size of the file | size 'filename' | size 'filename','filename'\n";
    }
    
}