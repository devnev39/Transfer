package Transfer2.Shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import Transfer2.ClientEventHandler;
import Transfer2.ServerEventHandler;

public class Get extends Command {
    private String RecPath;
    
    private long lastLen = 0;
    private long currentLen = 0;

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
        this.checkFileNameFormat(vals);
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

    private void checkFileNameFormat(String[] vals) throws Exception{   
        for(String val : vals){
            if(val.contains(".")){
                continue;
            }else{
                throw new Exception("No support for getting folder :"+val);
            }
        }
    }

    private void ReceiveFile(Socket serverSocket, File f, boolean b) throws Exception {
        long len = this.ReceiveInputDataLenght(serverSocket);
        long initial_len = f.length();
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
        for(this.currentLen=1;this.currentLen<=len;this.currentLen++){
            byte data = (byte) serverSocket.getInputStream().read();
            fos.write(data);
            this.clientEventHandler.byteReceived(this.currentLen+initial_len,initial_len+len);
        }
        fos.close();
    }

    @Override
    public void executeServerProcedure(String input, Socket clientSocket) throws Exception {
        String[] vals = remove(input.split(" "),0);
        String filename = String.join(" ", vals);
        File f = new File(this.Root+File.separator+filename);
        if(f.exists()){
            clientSocket.getOutputStream().write(ByteBuffer.allocate(4).putInt(1).array());
            long index = this.ReceiveInputDataLenght(clientSocket);
            long len = f.length() - index;
            clientSocket.getOutputStream().write(ByteBuffer.allocate(8).putLong(len).array());
            FileInputStream fis = new FileInputStream(f);
            fis.skip(index);
            for(long i=1;i<=len;i++){
                byte b = (byte) fis.read();
                clientSocket.getOutputStream().write(b);
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