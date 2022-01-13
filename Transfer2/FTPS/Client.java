package Transfer2.FTPS;

import Transfer2.Shared.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import Transfer2.ServerEventHandler;

public class Client extends Thread {
    
    private Socket clientSocket;
    private ArrayList<Command> Commands;
    private String Root;
    private ServerEventHandler serverEventHandler;

    public Client(Socket s,String root) throws Exception{
        this.clientSocket = s;
        this.Root = root;
        this.serverEventHandler = new ServerEventHandler();
        this.Commands = Command.generateServerCommands(serverEventHandler, this.Root);
    }

    public void run(){
        try {
            while(true){
                String msg = readText();
                String[] vals = msg.split(" ");
                for (Command command : this.Commands) {
                    if(vals[0].equals(command.toString())){
                        this.serverEventHandler.commandReceived(msg,this.clientSocket);
                        command.executeServerProcedure(msg, this.clientSocket);
                    }
                }
            }
        } catch (Exception e) {
            this.interrupt();
            e.printStackTrace();
        }
    }

    private String readText() throws IOException {
        byte[] data = new byte[4];
        for(int i=0;i<4;i++){
            data[i] = (byte) this.clientSocket.getInputStream().read();
        }
        int value = 0;
        for (byte b : data) {
            value = (value << 8) + (b & 0xFF);
        }
        data = new byte[value];
        for(int i=0;i<data.length;i++){
            data[i] = (byte) this.clientSocket.getInputStream().read();
        }
        return new String(data);
    }
}
