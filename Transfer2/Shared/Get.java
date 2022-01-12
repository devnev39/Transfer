package Transfer2.Shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import Transfer2.ClientEventHandler;
import Transfer2.ServerEventHandler;

public class Get extends Command {
    private String RecPath;
    
    private long lastLen = 0;
    private long currentLen = 0;

    private final int BUFFER_LEN = 1024;

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

    private boolean[] exist(String[] files){
        boolean[] exist = new boolean[files.length];
        int i = 0;
        for(String file : files){
            File f = new File(this.RecPath+File.separator+file);
            if(f.exists()){
                exist[i] = true;
            }else{
                exist[i] = false;
            }
            i++;
        }
        return exist;
    }

    private boolean[] append(String[] files){
        boolean[] exist = this.exist(files);
        for(int i=0;i<files.length;i++){
            if(exist[i]){
                exist[i] = this.clientEventHandler.getAppendResponse(files[i]);
            }
        }
        return exist;
    }

    @Override
    public void executeClientProcedure(String input, Socket serverSocket) throws Exception {
        String[] vals = this.remove(input.split(" "),0);
        vals = String.join(" ", vals).split(",");
        if(vals.length==0){
            throw new Exception("Nothing to get !");
        }
        boolean[] ifAppend = this.append(vals);
        for(int i=0;i<vals.length;i++){
            this.sendText("get "+vals[i], serverSocket);
            int flag = this.ReceiveStatusFlag(serverSocket);
            if(flag==-1){
                this.clientEventHandler.FileNotExistServerError(vals[i]);
                continue;
            }else{
                File f = new File(this.RecPath+File.separator+vals[i]);
                if(ifAppend[i]){
                    serverSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong(f.length()).array());
                    this.ReceiveFile(serverSocket,f,true);
                }else{
                    serverSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong(0).array());
                    this.ReceiveFile(serverSocket,f,false);
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
        }
        FileOutputStream fos = new FileOutputStream(f,b);
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run(){
                long diff = currentLen - lastLen;
                lastLen = currentLen;
                clientEventHandler.updateSpeed(diff,this);
            }
        }, 100,1000);
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
            long index = this.ReceiveInputDataLenght(clientSocket);
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
        }else{
            clientSocket.getOutputStream().write(ByteBuffer.allocate(4).putInt(-1).array());
        }
    }

    @Override
    public String getInfo() {
        return "get -> get files | get 'filename','filename' | get 'filename'\n";
    }
    
}