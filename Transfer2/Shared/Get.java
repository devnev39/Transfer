package Transfer2.Shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import Transfer2.ClientEventHandler;
import Transfer2.ServerEventHandler;

public class Get extends Command {
    private String RecPath;
    
    private long lastLen = 1;
    private long currentLen = 0;
    private Timer t;

    private final int BUFFER_LEN = 1024;

    private long Size = 0;

    public Get(ClientEventHandler cHandler) {
        super("get", cHandler);
        this.RecPath = "Received";
        this.CreateDirectory();
    }

    public Get(ServerEventHandler sHandler,String root) {
        super("get", sHandler,root);
        this.RecPath = "Received";
        this.CreateDirectory();
    }

    private void CreateDirectory() {
        File f = new File(this.RecPath);
        if(f.exists() == false){
            f.mkdir();
        }
    }

    private int append(File file,long size){
        if(file.exists()){
            if(file.length()<size){
                boolean des = this.clientEventHandler.getResponse(file.getName()+" exist ! Append to it directly ? (y/n)");
                if(des)
                    return 1;
            }else{
                return -1;
            }
        }
        return 0;
    }

    @Override
    public void executeClientProcedure(String input, Socket serverSocket) throws Exception {
        String[] vals = this.remove(input.split(" "),0);
        vals = String.join(" ", vals).split(",");
        if(vals.length==0){
            throw new Exception("Nothing to get !");
        }
        for(int i=0;i<vals.length;i++){
            this.sendText("get "+vals[i], serverSocket);
            int flag = this.ReceiveStatusFlag(serverSocket);
            if(flag==-1){
                this.clientEventHandler.FileNotExistServerError(vals[i]);
                continue;
            }else
            if(flag==2){
                long len = this.ReceiveInputDataLenght(serverSocket);
                HashMap<String,Long> files = this.getObject(this.getBuffer(serverSocket, len)); 
                files.forEach((fn,sz)->{
                    Size += sz;
                });
                float size = (float)this.Size / 1048576.0f;
                if(this.clientEventHandler.getResponse("Total Files : "+files.size()+" Size : "+size+" MiB\nProceed to get all files ? (y/n)")){
                    files.forEach((fn,sz)->{
                        try {
                            this.executeClientProcedure("get "+fn, serverSocket);
                        } catch (Exception e) {
                            this.clientEventHandler.ClientExceptionOccured(serverSocket, e);
                        }
                    });
                }
                this.Size = 0;
            }else{
                File f = new File(this.RecPath+File.separator+vals[i]);
                long size = this.ReceiveInputDataLenght(serverSocket);
                int ifAppend = this.append(f,size);
                if(ifAppend==1){
                    serverSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong(f.length()).array());
                    this.ReceiveFile(serverSocket,f,true);
                }else
                if(ifAppend==0){
                    serverSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong(0).array());
                    this.ReceiveFile(serverSocket,f,false);
                }
                else{
                    serverSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong(-1).array());
                    continue;
                }
            }
        }
    }

    private void ReceiveFile(Socket serverSocket, File f, boolean b) throws Exception {
        long len = this.ReceiveInputDataLenght(serverSocket);
        int buffer_len = this.ReceiveInputDataLenghtInt(serverSocket);  // Buffer length is int
        int loop_len = (int)(len/(long)buffer_len);
        int remainder_size = (int)(len%(long)this.BUFFER_LEN);
        if(!b){
            if(f.exists()){
                f.delete();
            }
            f.getParentFile().mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(f,b);
        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run(){
                long diff = currentLen - lastLen;
                lastLen = currentLen;
                clientEventHandler.updateSpeed(diff,t);
            }
        }, 0,1000);
        for(int i=1;i<=loop_len+1;i++){
            byte[] data;
            if(i==loop_len+1){
                if(remainder_size==0){
                    continue;
                }
                data = new byte[remainder_size];
            }else{
                data = new byte[buffer_len];
            }
            serverSocket.getInputStream().read(data);
            fos.write(data);
            if(i==loop_len+1){
                this.currentLen += buffer_len*i + remainder_size;
                this.clientEventHandler.byteReceived(i*buffer_len+remainder_size, len);
            }else{
                this.currentLen += buffer_len*i;
                this.clientEventHandler.byteReceived(i*buffer_len, len);
            }
        }
        fos.close();
    }

    @Override
    public void executeServerProcedure(String input, Socket clientSocket) throws Exception {
        String[] vals = remove(input.split(" "),0);
        String filename = String.join(" ", vals);
        File f = new File(this.Root+File.separator+filename);
        if(f.exists() && f.isFile()){
            clientSocket.getOutputStream().write(ByteBuffer.allocate(4).putInt(1).array());
            clientSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong(f.length()).array());
            long index = this.ReceiveInputDataLenght(clientSocket);
            if(index!=-1){
                long len = f.length() - index;
                clientSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong(len).array());
                clientSocket.getOutputStream().write(ByteBuffer.allocate(4).putInt(this.BUFFER_LEN).array());
                int loop_lenght = (int)(len/(long)this.BUFFER_LEN);
                int remainder_size = (int)(len%(long)this.BUFFER_LEN);
                FileInputStream fis = new FileInputStream(f);
                fis.skip(index);
                for(int i=1;i<=loop_lenght+1;i++){
                    byte[] data;
                    if(i==loop_lenght+1){
                        if(remainder_size==0){
                            continue;
                        }
                        data = new byte[remainder_size];
                    }else{
                        data = new byte[this.BUFFER_LEN];
                    }
                    fis.read(data);
                    clientSocket.getOutputStream().write(data);
                }
                fis.close();
            }
        }else
        if(f.exists() && f.isDirectory()){
            clientSocket.getOutputStream().write(ByteBuffer.allocate(4).putInt(2).array());
            HashMap<String,Long> h = new HashMap<String,Long>();
            this.GetToLastFiles(f.getPath()).forEach((name)->{
                h.put(name.replace(this.Root+File.separator, ""), new File(name).length());
            });
            byte[] data = this.ObjectToByteArray(h);
            clientSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong((long)data.length).array());
            clientSocket.getOutputStream().write(data);
        }
        else{
            clientSocket.getOutputStream().write(ByteBuffer.allocate(4).putInt(-1).array());
        }
    }

    @Override
    public String getInfo() {
        return "get -> get files | get 'filename','filename' | get 'filename'\n";
    }
    
}