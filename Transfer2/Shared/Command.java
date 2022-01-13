package Transfer2.Shared;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import Transfer2.ClientEventHandler;
import Transfer2.ServerEventHandler;

public abstract class Command {
    private String Specifier;
    protected ClientEventHandler clientEventHandler;
    protected ServerEventHandler serverEventHandler;

    protected String Root;
    private String serverRoot;

    public Command(String specifier,ClientEventHandler cHandler){
        this.Specifier = specifier;
        this.clientEventHandler = cHandler;
    }

    public Command(String specifier,ServerEventHandler sHandler,String root){
        this.Specifier = specifier;
        this.serverEventHandler = sHandler;
        this.Root = root;
        this.serverRoot = root;
    }

    public Command(String specifier,ServerEventHandler sHandler){
        this.Specifier = specifier;
        this.serverEventHandler = sHandler;
    }

    public static ArrayList<Command> generateClientCommands(ClientEventHandler EventHandler){
        ArrayList<Command> clientCommands = new ArrayList<Command>();
        LS ls = new LS(EventHandler);
        Get get = new Get(EventHandler);
        CD cd = new CD(EventHandler);
        Where where = new Where(EventHandler);
        Status status = new Status(EventHandler);
        clientCommands.add(ls);
        clientCommands.add(get);
        clientCommands.add(cd);
        clientCommands.add(where);
        clientCommands.add(status);
        return clientCommands;
    }

    public static ArrayList<Command>  generateServerCommands(ServerEventHandler serverEventHandler,String Root){
        ArrayList<Command> serverCommands = new ArrayList<Command>();
        Get get = new Get(serverEventHandler, Root);
        LS ls = new LS(serverEventHandler,Root);
        Where where = new Where(serverEventHandler,Root);
        CD cd = new CD(serverEventHandler,Root,new ArrayList<Command>(){{
                add(ls);
                add(get);
                add(where);
            }});
        Status status = new Status(serverEventHandler,new ArrayList<Command>(){{
                add(get);
                add(ls);
                add(where);
                add(cd);
            }});
        serverCommands.add(where);
        serverCommands.add(get);
        serverCommands.add(ls);
        serverCommands.add(cd);
        serverCommands.add(status);
        return serverCommands;
    }

    private <T> T getObject(byte[] buffer) throws Exception{
        ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = new ObjectInputStream(bis);
        T obj = (T) ois.readObject();
        bis.close();
        ois.close();
        return obj;
    }   

    protected <T> byte[] ObjectToByteArray(T object) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(object);
        byte[] data = bos.toByteArray();
        bos.close();
        os.close();
        return data;
    }

    protected byte[] getBuffer(Socket s, long len) throws Exception{
        /**    
         * Client Side Method to get buffer
        */ 
        byte[] buffer = new byte[(int)len];
        for(int i=0;i<buffer.length;i++){
            buffer[i] = (byte) s.getInputStream().read();
        }
        return buffer;
    }

    protected int ReceiveStatusFlag(Socket s) throws Exception{
        byte[] flag = this.getBuffer(s, 4);
        int value = 0;
        for (byte b : flag) {
            value = (value << 8) + (b & 0xFF);
        }
        return value;
    }

    protected long ReceiveInputDataLenght(Socket s) throws Exception{
        byte[] len = this.getBuffer(s, 8);
        ByteBuffer wrapper = ByteBuffer.wrap(len);
        return wrapper.getLong();
    }

    protected int ReceiveInputDataLenghtInt(Socket s) throws Exception{
        byte[] len = this.getBuffer(s, 4);
        ByteBuffer wrapper = ByteBuffer.wrap(len);
        return wrapper.getInt();
    }

    protected String ReceiveText(Socket s,long len) throws Exception{
        byte[] data = this.getBuffer(s, len);
        return new String(data);
    }

    protected Exception ReceieveException(Socket s,long len) throws Exception{
        byte[] data = this.getBuffer(s, len);
        Exception e = this.getObject(data);
        return e;
    }

    protected void sendText(String msg,Socket socket) throws IOException{
        byte[] data = msg.getBytes();
        socket.getOutputStream().write(ByteBuffer.allocate(4).putInt(data.length).array());
        socket.getOutputStream().write(data);
    }

    protected String GetFileFolders(String root) throws Exception{
        File file = new File(root);
        if(file.exists()){
            String files = "";
            String folders = "";
            for(File f : file.listFiles()){
                if(f.isFile()){
                    files += f.getName()+"\n";
                }else{
                    folders+=f.getName()+"\n";
                }
            }
            return "======== Files ========\n"+files+"======== Folders ========\n"+folders;
        }else{
            throw new Exception("Directory doesn't exist !");
        }
    }

    protected String[] remove(String[] sp, int ind) throws Exception{
        if(sp.length==1){
            return sp;
        }
        String[] arr = new String[sp.length-1];
        int j = 0;
        for(int i=0;i<sp.length;i++){
            if(i==ind){
                continue;
            }
            arr[j++] = sp[i];
        }
        return arr;
    }

    public String serverRoot(){
        return this.serverRoot;
    }

    public abstract void executeClientProcedure(String input,Socket serverSocket) throws Exception;
    public abstract void executeServerProcedure(String input,Socket clientSocket) throws Exception;
    public abstract String getInfo();
    
    @Override
    public String toString() {
        return this.Specifier;
    }
}