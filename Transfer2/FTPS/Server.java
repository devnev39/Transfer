package Transfer2.FTPS;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
//import java.util.ArrayList;

import Transfer2.ServerEventHandler;

public class Server {

    private int Port = 8999;
    private ServerEventHandler Handler;
    private String rootFolderPath = System.getProperty("user.dir");
    //private ArrayList<Thread> Connections;

    public Server(int Port,ServerEventHandler Handler){
        this.Port = Port;
        this.Handler = Handler;
    }

    public Server(ServerEventHandler handler){
        this.Handler = handler;
    }

    public Server(int Port,ServerEventHandler Handler,String path) throws Exception{
        this.Port = Port;
        this.Handler = Handler;
        this.checkPath(path);
    }

    public Server(ServerEventHandler handler,String path) throws Exception{
        this.Handler = handler;
        this.checkPath(path);
    }

    public void Start(){
        try {
            ServerSocket ss = new ServerSocket(this.Port);
            Handler.ServerStartedEventHandler(ss);
            //this.Connections = new ArrayList<Thread>();
            while(true){
                Socket accept = ss.accept();
                Thread t = new Client(accept,rootFolderPath);
                Handler.newConnectionEventHandler(accept);
                // this.Connections.add(t);
                t.start();
                Thread.sleep(0);
            }
        } catch (Exception e) {
            Handler.exceptionInStartEventHandler(e);
        }
    }

    private void checkPath(String path) throws Exception{
        File f = new File(path);
        if(f.exists()){
            this.rootFolderPath = path;
        }
        else{
            throw new Exception("Invalid Path !");
        }
    }
}
