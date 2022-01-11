package Transfer2.Client;

import java.net.Socket;
import java.util.ArrayList;

import Transfer2.ClientEventHandler;
import Transfer2.Shared.*;

public class Client {
    private Socket socket;
    private ClientEventHandler EventHandler;
    private ArrayList<Command> Commands;
    //private DataOutputStream dos;
    public Client(String addr,int port,ClientEventHandler handler) throws Exception{
        this.EventHandler = handler;
        socket = new Socket(addr, port);
        if(socket.isConnected()){
        }else{
            throw new Exception("socket.isConnected() = false !");
        }
        this.CreateCommandList();
        //dos = new DataOutputStream(socket.getOutputStream());
        this.Start();
    }

    private void CreateCommandList(){
        this.Commands = new ArrayList<Command>();
        LS ls = new LS(this.EventHandler);
        Get get = new Get(this.EventHandler);
        CD cd = new CD(this.EventHandler);
        Where where = new Where(this.EventHandler);
        Status status = new Status(this.EventHandler);
        this.Commands.add(ls);
        this.Commands.add(get);
        this.Commands.add(cd);
        this.Commands.add(where);
        this.Commands.add(status);
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
                        //this.dos.writeUTF(cmd);
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
