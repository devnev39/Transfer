package Transfer2.Shared;

import java.net.Socket;
import java.util.ArrayList;

import Transfer2.ClientEventHandler;
import Transfer2.ServerEventHandler;

public class Help extends Command{
    
    private ArrayList<Command> Commands;

    public Help(ServerEventHandler sHandler,ArrayList<Command> commands) {
        super("help", sHandler);
        this.Commands = commands;
    }

    public Help(ClientEventHandler cHandler) {
        super("help", cHandler);
    }

    @Override
    public void executeClientProcedure(String input, Socket serverSocket) throws Exception {
        this.sendText(input, serverSocket);
        int len = this.ReceiveInputDataLenghtInt(serverSocket);
        String data = this.ReceiveText(serverSocket, (long)len);
        this.clientEventHandler.TextReceived(data);
    }

    @Override
    public void executeServerProcedure(String input, Socket clientSocket) throws Exception {
        String data = "";
        for(Command cmd : this.Commands){
            data += cmd.getInfo();
        }
        data+=this.getInfo();
        this.sendText(data, clientSocket);
    }

    @Override
    public String getInfo() {
        return "help  ->  get info about all commands\n";
    }
}