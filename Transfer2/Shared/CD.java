package Transfer2.Shared;

import java.io.File;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import Transfer2.ClientEventHandler;
import Transfer2.ServerEventHandler;

public class CD extends Command {

    private ArrayList<Command> Commands;

    public CD(ClientEventHandler cHandler) {
        super("cd",cHandler);
    }

    public CD(ServerEventHandler sHandler,String root,ArrayList<Command> commands) {
        super("cd",sHandler,root);
        this.Commands = commands;
    }

    @Override
    public void executeClientProcedure(String input, Socket serverSocket) throws Exception {
        this.sendText(input, serverSocket);
        int flag = this.ReceiveStatusFlag(serverSocket);
        long len = this.ReceiveInputDataLenght(serverSocket);
        Exception e = null;
        if(flag==-1){
            e = this.ReceieveException(serverSocket, len);
            this.clientEventHandler.ServerExceptionOccured(e);
        }else{
            String data = this.ReceiveText(serverSocket, len);
            this.clientEventHandler.TextReceived(data);
        }
    }

    @Override
    public void executeServerProcedure(String input, Socket clientSocket) throws Exception {
        int flag = 0;
        byte[] buffer;

        String[] vals = input.split(" ");
        String newRoot = this.getNewRoot(vals[1]);
        File f = new File(newRoot);
        if(f.exists()){
            try {
                this.applyNewRoot(newRoot);
                buffer = this.GetFileFolders(this.Root).getBytes();
                flag = 1;
            } catch (Exception e) {
                flag = -1;
                buffer = this.ObjectToByteArray(e);
            }
        }else{
            flag = -1;
            buffer = this.ObjectToByteArray(new Exception("File/Folder doesn't exist !"));
        }

        clientSocket.getOutputStream().write(ByteBuffer.allocate(4).putInt(flag).array());
        clientSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong((long)buffer.length).array());
        clientSocket.getOutputStream().write(buffer);
    }

    private void applyNewRoot(String newRoot) {
        for(Command cmd : this.Commands){
            cmd.Root = newRoot;
        }
        this.Root = newRoot;
    }

    private String getNewRoot(String string) throws Exception {
        String newRoot = this.Root;
        if(string.equals("")){
            return newRoot;
        }
        if(string.contains("..")){
            if(newRoot.equals(this.serverRoot())){
                return newRoot;
            }else{
                String[] sp = this.Root.split(File.separator);
                newRoot = String.join(File.separator,this.remove(sp,sp.length-1));
                return newRoot;
            }
        }else{
            newRoot += File.separator+string;
            return newRoot;
        }
    }

    @Override
    public String getInfo() {
        return "cd  -> change directory | cannot go above root | cd 'folder_name' / cd ..\n";
    }
}
