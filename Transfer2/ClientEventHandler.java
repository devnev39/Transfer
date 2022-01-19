package Transfer2;

import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.Timer;

import Transfer2.Client.ClientEvents;

public class ClientEventHandler implements ClientEvents {
    
    private final DecimalFormat decimalFormat = new DecimalFormat("0.000");

    @Override
    public void TextReceived(String text) {
        System.out.println(text);
    }

    @Override
    public void ServerExceptionOccured(Exception e) {
        System.out.println("Exception at server : ");
        System.out.println(e.getMessage());
    }

    @Override
    public String GetCommandFromUser() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter command : ");
        String inp = sc.nextLine();
        return inp;
    }

    @Override
    public void ClientExceptionOccured(Socket socket, Exception e) {
        System.out.println("Exception occured : "+e.getMessage());
        e.printStackTrace();
    }

    public void CommandNotFound(String cmd) {
        System.out.println(cmd+" command not found !");
    }

    public boolean getResponse(String string) {
        Scanner sc =  new Scanner(System.in);
        System.out.println(string);
        String inp = sc.nextLine();
        if(inp.equals("y") || inp.equals("Y")){
            return true;
        }
        return false;
    }

    public void FileNotExistServerError(String string) {
        System.out.println("Server Error : "+string+" doesn't exist !");
    }

    public void byteReceived(long i, long len) {
        float percent = ((float)i/(float)len)*100.0f;
        System.out.print(decimalFormat.format(percent)+" % \r");
        if(percent==100.0f){
            System.out.println("Completed !");
        }
    }

    public void updateSpeed(long diff, Timer t) {
        if(diff==0){
            t.cancel();
            t.purge();
            return;
        }
        float rate = (float)diff / 1048576.0f;
        System.out.print("\t\t"+rate+" MB/s\r");
    }
    
}
