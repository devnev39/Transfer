package Transfer2.Client;

import java.net.Socket;
import java.util.ArrayList;

import Transfer2.ClientEventHandler;
import Transfer2.Shared.*;

public class Client {
    private Socket socket;
    private ClientEventHandler EventHandler;
    private ArrayList<Command> Commands;
    public Client(String addr,int port,ClientEventHandler handler) throws Exception{
        this.EventHandler = handler;
        socket = new Socket(addr, port);
        if(socket.isConnected()){
        }else{
            throw new Exception("socket.isConnected() = false !");
        }
        this.Commands = Command.generateClientCommands(handler);
        this.Start();
    }

    private void Start() throws Exception{
        while(true){
            String cmd = this.EventHandler.GetCommandFromUser();
            int found = 0;
            try {
                String[] vals = cmd.split(" ");
                for(Command command : this.Commands){
                    if(vals[0].equals(command.toString())){
                        found = 1;
                        command.executeClientProcedure(cmd, this.socket);
                    }
                }
                if(found==0){
                    this.EventHandler.CommandNotFound(cmd);
                }
            } catch (Exception e) {
                this.EventHandler.ClientExceptionOccured(socket,e);
            }
        }
    }
}
