package Client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client
{
    final static int ServerPort = 8000;

    public static void main(String args[]) throws UnknownHostException, IOException
    {
        Scanner scn = new Scanner(System.in);

        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        // obtaining input and out streams
        DataInputStream dis = new DataInputStream(s.getInputStream());
        PrintWriter dos = new PrintWriter(s.getOutputStream(),true);

        // sendMessage thread
        Thread sendMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {
                while (true) {

                    // read the message to deliver.
                    String msg = scn.nextLine();

                    // write on the output stream
                    dos.println(msg);
                }
            }
        });

        // readMessage thread
        Thread readMessage = new Thread(new Runnable()
        {
            @Override
            public void run() {


                while (true) {
                    try {
                        // read the message sent to this client
                        String msg = dis.readLine();
                        System.out.println(msg);

                        if(msg.equals("CLOSE#0") || msg.equals("CLOSE#1") || msg.equals("CLOSE#2")){

                            s.close();
                            System.exit(0);
                            break;

                        }

                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                }
            }
        });

        sendMessage.start();
        readMessage.start();

    }
}

