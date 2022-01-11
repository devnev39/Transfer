package Transfer2;

import java.net.InetAddress;
import java.util.Scanner;
import Transfer2.Client.Client;
import Transfer2.FTPS.Server;

public class Main {
    public static void main(String[] args) {

        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Choose : \n1.Client\n2.Server");
            int choice = sc.nextInt();
            sc.nextLine();
            switch (choice) {
                case 1:
                    System.out.println("Enter address of server : ");
                    String addr = sc.nextLine();
                    Client c = new Client(addr, 8999, new ClientEventHandler());
                    break;
                
                case 2:
                    PrintIp();
                    System.out.println("Enter root path for directory : ");
                    String path = sc.nextLine();
                    Server s = new Server(8999, new ServerEventHandler(), path);
                    s.Start();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void PrintIp(){
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            InetAddress addrs[] = InetAddress.getAllByName(hostName);
            System.out.println(addrs[1].getHostAddress());
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        
    }
}
