
package multserver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;
import java.io.File;
import java.util.Random;


public class ClientThread extends Thread {
Socket  clientSocket;
BufferedReader in = null;
PrintWriter out = null;
static String CAPA = "CAPA";
static String USER = "USER";
static String PASS = "PASS";
static String STAT = "STAT";
static String LIST = "LIST";
static String UIDL = "UIDL";
static String RETR = "RETR";
static String QUIT = "QUIT";
static String TOP  = "TOP";
static String NOOP = "NOOP";

static String CRLF = "\r\n";
static String EOM = ".\r\n";

static String password = "";
static String user_inbox = "";
static String path_to_box = "INBOX/";
boolean authorization = true, transaction = false, update = false;

    public ClientThread(Socket clientsocket) {
        this.clientSocket = clientsocket;
    }
    public void CAPA_available_cmds()
    {
        out.println("+OK CAPA list follows" + CRLF);
        out.println(USER + CRLF );
 //       out.print(TOP + CRLF );
        out.println(STAT + CRLF );
        out.println(LIST + CRLF );
        out.println(QUIT + CRLF );
        out.println(EOM);
    }
    public void USER_parse_login(String cmd_from_client)
    {
        String line = "";
        FileReader myFile = null;
        BufferedReader buff = null;
        StringTokenizer st = new StringTokenizer(cmd_from_client," ");
        st.nextToken();
        user_inbox = st.nextToken();
        try {
            myFile = new FileReader("login.txt");
        } catch (FileNotFoundException ex) {
            System.out.println("Can't find the login file");
            System.exit(-1);
        }
        buff = new BufferedReader(myFile);
        while(true)
        {
            try {
                line = buff.readLine();
            } catch (IOException ex) {
                System.out.println("Can't read the login file");
                System.exit(-2);
            }

            if (line == null) out.println("-ERR never heard of mailbox name");

            StringTokenizer st1 = new StringTokenizer(line,"USERPA: \n");
            while(st1.hasMoreTokens())
            {
                 if (user_inbox.equals(st1.nextToken()))
                 {
                     password = st1.nextToken();
                     out.println("+OK name is a valid mailbox" );
                     return;
                 }
                else st1.nextToken();
            }
       }


    }
    public void PASS_password_control(String cmd_from_client)
    {
        StringTokenizer st = new StringTokenizer(cmd_from_client," ");
        st.nextToken();
        String client_pass = st.nextToken();
        if (client_pass.equals(password))
        {
            out.println("+OK maildrop locked and ready");
            authorization = false;
            transaction = true;
            path_to_box += user_inbox + "/";
        }
        else out.print("-ERR invalid password");
    }
    public void STAT_discovery_dir()
    {
        File path = new File(path_to_box);
        File[] list = path.listFiles();

        int size = 0;
        for (int i = 0 ; i < list.length; i++)
        {
            size += list[i].length();
        }
        System.out.print("+OK " + list.length + " " + size);
        out.println("+OK " + list.length + " " + size);
        //out.print("+OK 1 100");
    }
    public void LIST_size_list()
    {

        File path = new File(path_to_box);
        File[] list = path.listFiles();

        // количество байт всех сообщений
        int size = 0;
        for (int i = 0 ; i < list.length; i++)
        {
            size += list[i].length();
        }
        out.println("+OK " + list.length + " messages (" + size + " octets)" + CRLF);
        System.out.println("+OK " + list.length + " messages (" + size + " octets)" + CRLF);
        for (int i = 1 ; i <= list.length ; i++)
        {
            out.println(i + " " + list[i-1].length() + CRLF);
            System.out.print(i + " " + list[i-1].length() + CRLF);
        }
        out.println(EOM);


//        out.println("+OK 1 message (100 octets)\r\n");
//                        out.println("1 100\r\n");
//                        out.println(".\r\n");
    }
    public void UIDL_uniq_number()
    {
        File path = new File(path_to_box);
        File[] list = path.listFiles();
        Random rand = new Random();

        // количество байт всех сообщений
        int size = 0;
        for (int i = 0 ; i < list.length; i++)
        {
            size += list[i].length();
        }
        out.print("+OK " + list.length + " messages (" + size + " octets)" + CRLF);
        System.out.print("+OK " + list.length + " messages (" + size + " octets)" + CRLF);
        for (int i = 1 ; i <= list.length ; i++)
        {
            out.println(i + " " + i + CRLF);
            //out.print(i + " " + rand.nextInt(10000) + CRLF);
            System.out.print(i + " " + i + CRLF);
        }
        out.println(EOM);
        System.out.println(EOM);



//                                out.println("+OK 1 messages (100 octets)\r\n");
//                        out.println("1 1\r\n");
//                        out.println(".\r\n");
    }
    public void RETR_transmit_message(String cmd_from_client)
    {
        out.println("+OK message follows\r\n");

        FileReader myFile = null;
        BufferedReader buff = null;

        File path = new File(path_to_box);
        File[] list = path.listFiles();

        String line = "";

        // парсим строку, выбирая номер сообщения для скачивания
        StringTokenizer st = new StringTokenizer(cmd_from_client," ");
        st.nextToken();
        int number_of_message =  Integer.parseInt(st.nextToken());

        // открываем файл сообщения
        try {
            myFile = new FileReader(path_to_box + list[number_of_message - 1].getName());

        } catch (FileNotFoundException ex) {
            System.out.print("Can't find chosen message file");
            System.exit(-3);
        }

        buff = new BufferedReader(myFile);

        while (true)
        {
            try {
                line = buff.readLine();
            } catch (IOException ex) {
                System.out.print("Can't read chosen message file");
                System.exit(-4);
            }
            if (line == null) break;
            if (!line.equals("")) out.println(line);
            else                    out.println(CRLF);
        }
        out.println(EOM);
    }
    public void QUIT_end_connection()
    {
        out.println("+OK");
        path_to_box = "INBOX/";
        authorization = true;
        transaction = false;
    }
    public void TOP(String cmd_from_client)
    {
        FileReader myFile = null;
        BufferedReader buff = null;

        File path = new File(path_to_box);
        File[] list = path.listFiles();

        String line = "";

        // парсим строку, выбирая номер сообщения
        StringTokenizer st = new StringTokenizer(cmd_from_client," ");
        st.nextToken();
        int number_of_message =  Integer.parseInt(st.nextToken());

        // открываем файл сообщения
        try {
            myFile = new FileReader(path_to_box + list[number_of_message - 1].getName());

        } catch (FileNotFoundException ex) {
            System.out.println("Can't find chosen message file");
            System.exit(-3);
        }

        buff = new BufferedReader(myFile);

        while (true)
        {
            try {
                line = buff.readLine();
            } catch (IOException ex) {
                System.out.println("Can't read chosen message file");
                System.exit(-4);
            }
            if (line == null) break;
            if (!line.equals("")) out.println(line + CRLF);
            else                  break;
        }
        out.println(EOM);

    }
    @Override
    public void run() {
        String input, output;
        FileReader myFile = null;
        BufferedReader buff = null;
        
         try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException ex) {
            System.out.println("Can't read socket");
            return; 
        }
        try {
            // приветственное сообщение
            out.println("+OK POP3 server ready");

            // TEMP
            int i = 0;

            while(true)
            {
                input = in.readLine();
                System.out.println("text from client with port " + clientSocket.getPort() + ":::  " + input);
                StringTokenizer st = new StringTokenizer(input, " ");
                String cmd = st.nextToken();
                if (authorization)
                {
                    if      (cmd.equals(CAPA))  CAPA_available_cmds();
                    else if (cmd.equals(USER))  USER_parse_login(input);
                    else if (cmd.equals(PASS))  PASS_password_control(input);
                }
                else if (transaction)
                {
                    if      (cmd.equals(STAT))  STAT_discovery_dir();
                    else if (cmd.equals(LIST))  LIST_size_list();
                    else if (cmd.equals(UIDL))  UIDL_uniq_number();
                    else if (cmd.equals(RETR))  RETR_transmit_message(input);
                    else if (cmd.equals(QUIT))
                    {
                        QUIT_end_connection();
                        System.out.println("Close client with port " + clientSocket.getPort());
                        break;
                    }
                    else if (cmd.equals(TOP)) TOP(input);
                    else if (cmd.equals(NOOP))    out.println("+OK");
                }

                
            }



//                while ((input = in.readLine()) != null)
//                {
//                    if (i == 0)
//                    {
//                        out.println("+OK CAPA list follows\r\n");
//                        out.println("USER\r\n");
//                        out.println("UIDL\r\n");
//                        out.println("TOP\r\n");
//                        out.println(".\r\n");
//                    }
//
//                    else if (i == 1) out.println("+OK name is a valid mailbox");
//                    else if (i == 2) out.println("+OK maildrop locked and ready");
//                    else if (i == 3) out.println("+OK 1 100");
//                    else if (i == 4)
//                    {
//                        out.println("+OK 1 message (100 octets)\r\n");
//                        out.println("1 100\r\n");
//                        out.println(".\r\n");
//                    }
//                  else if (i == 5)
//                    {
//                        out.println("+OK 1 messages (100 octets)\r\n");
//                        out.println("1 1\r\n");
//                        out.println(".\r\n");
//                    }
//
//                  else if (i == 6)
//                  {
//                    out.println("+OK 1 follows\r\n");
//
//                   out.println("Date: Fri, 6 Dec 2002 23:26:50 +0300 (MSK/MSD)\r\n"
//                           + "From: test1@localhost\r\n"
//                    + "To: test@localhost\r\n"
//                    + "cc: test@localhost\r\n"
//                    + "bcc:  test@localhost\r\n"
//                    + "Subject: Hello\r\n"
//                    + "Content-Type: text/plain\r\n\r\nI'm murlock\r\n" + ".\r\n");
//
//                  }
//                  else if (i == 7)
//                  {
//
//
//                      out.println("+OK\r\n");
//                      out.println("I'm murlock\r\n.\r\n");
//
//                  }
//                else if (i == 8) {out.println("+OK dewey POP3 server signing off"); break; }

               
                
                    

                


            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException ex) {
            System.out.println("Problem with streams");
            System.exit(-100);
        }

    }

   


}



