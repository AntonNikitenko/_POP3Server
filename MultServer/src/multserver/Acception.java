package multserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Acception extends Thread{
ServerSocket servers = null;
Socket fromclient = null;
    public Acception(ServerSocket serverSoket ) {
        servers = serverSoket;
    }

    @Override
    public void run() {
        System.out.println("Waiting for a client...");
        try {
            while (true)
            {
                fromclient = servers.accept();
                System.out.println("Client with ip " + fromclient.getInetAddress() + " and port " + fromclient.getPort() + " was connected");
                ClientThread newClient = new ClientThread(fromclient);
                newClient.start();
            }

        } catch (IOException ex) {
            System.out.println("Program close. Accept");
            System.exit(-2);
        }

        try {
            fromclient.close();
            servers.close();
        } catch (IOException ex) {
            System.out.println("Can't close socket");
            System.exit(-3);
        }
    }
}
