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
    //private DataInputStream dis;

    public Client(Socket s,String root) throws Exception{
        this.clientSocket = s;
        this.Root = root;
        this.serverEventHandler = new ServerEventHandler();
        this.CreateCommandList();
        //dis = new DataInputStream(this.clientSocket.getInputStream());
    }

    private void CreateCommandList(){
        this.Commands = new ArrayList<Command>();
        Get get = new Get(this.serverEventHandler, this.Root);
        LS ls = new LS(this.serverEventHandler,this.Root);
        Where where = new Where(this.serverEventHandler,this.Root);
        CD cd = new CD(this.serverEventHandler,this.Root,new ArrayList<Command>(){{
                add(ls);
                add(get);
                add(where);
            }});
        Status status = new Status(this.serverEventHandler,new ArrayList<Command>(){{
                add(get);
                add(ls);
                add(where);
                add(cd);
            }});
        this.Commands.add(where);
        this.Commands.add(get);
        this.Commands.add(ls);
        this.Commands.add(cd);
        this.Commands.add(status);
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
