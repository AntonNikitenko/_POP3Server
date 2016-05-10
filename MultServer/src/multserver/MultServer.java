package multserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;



public class MultServer {
 public static ServerSocket servers = null;

    public static void exit() throws IOException
    {
        System.out.println("Exit subprogram is running");
        servers.close();
    }


    public static void main(String[] args) throws IOException {

        BufferedReader in = null;
        PrintWriter out = null;
        String input, output;


        // создаем серверный сокет
        try {
            servers = new ServerSocket(110);
        } catch (IOException ex) {
            System.out.println("Couldn't listen to port 4444");
            System.exit(-1);
        }

        // Создаем потоки принятия клиентов
        Acception acc = new Acception(servers);
        acc.start();

        BufferedReader inu = null;
        inu = new BufferedReader(new InputStreamReader(System.in));

        String fuser;

        while(true)
        {
            fuser = inu.readLine();
            if (fuser.equalsIgnoreCase("exit"))
            {
                exit();
                break;
            }

        }
    }
}
