package server;


import java.io.*;
import java.util.*;
import java.net.*;


// Server class
public class ChatServer {

    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();


    public static void main(String[] args) throws IOException {
        UserService userService = new UserService();
        // server is listening on port 8000
        ServerSocket ss = new ServerSocket(8000);
        System.out.println("Server started");
        Socket s;

        // running infinite loop for getting
        // client request
        while (true) {
            // Accept the incoming request
            s = ss.accept();

            System.out.println("New client request received : " + s);

            // obtain input and output streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            PrintWriter dos = new PrintWriter(s.getOutputStream(),true);
            StringTokenizer st = new StringTokenizer(dis.readLine(), "#");
            String cmd = st.nextToken();

            try {
                if (cmd.equals("CONNECT")) {
                    String username = st.nextToken();
                    if (!userService.usernameExists(username)) {
                        dos.println("CLOSE#2");
                        s.close();
                        break;
                    }

                    System.out.println("Creating a new handler for this client...");
                    // Create a new handler object for handling this request.
                    ClientHandler mtch = new ClientHandler(s, username, dis, dos);
                    // Create a new Thread with this object.
                    Thread t = new Thread(mtch);
                    System.out.println("Adding this client to active client list");
                    // add this client to active clients list
                    ar.add(mtch);
                    // start the thread.
                    t.start();
                    onlineMessage();
                    return;
                }

                dos.println("CLOSE#1");
            } catch (Exception e) {
                // Intentionally swallowing the exception
            } finally {
                s.close();
            }
        }
    }

    public static void onlineMessage() throws IOException {

        int vectorSize = ChatServer.ar.size(); // sætter vores vector størerelse fast til en variabel
        int checks = 0;
        StringBuilder stringBuilder = new StringBuilder(); // laver string builder.
        for (ClientHandler allMc : ChatServer.ar) { //gennemgår vores clientliste

            checks++;

            // tilføjer navne til vores online besked.

            if (allMc.isloggedin == true && vectorSize == checks) {
                stringBuilder.append(allMc.getName());
                break;
            }

            //tilføjer det sidste navn I listen.

            if (allMc.isloggedin == true && vectorSize > checks) {
                stringBuilder.append(allMc.getName() + ",");
            }

        }

        // sender ONLINE besked ud til alle
        for (ClientHandler allMc : ChatServer.ar) {
            if (allMc.isloggedin == true && vectorSize == checks) {
                allMc.dos.println("ONLINE#" + stringBuilder.toString());
            }
        }

    }


}

// ClientHandler class
class ClientHandler implements Runnable {
    private String name;
    final DataInputStream dis;
    final PrintWriter dos;
    Socket s;
    boolean isloggedin;

    // constructor
    public ClientHandler(Socket s, String name, DataInputStream dis, PrintWriter dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isloggedin = true;

    }

    public String getName() {
        return name;
    }

    public void close(int i) throws IOException {

        switch (i) {

            // normal close
            case 0: {
                dos.println("CLOSE#0");
                this.isloggedin = false;
                this.s.close();
                this.dis.close();
                this.dos.close();
                ChatServer.onlineMessage();
                break;
            }
            // Illegal input was received
            case 1: {
                dos.println("CLOSE#1");
                this.isloggedin = false;
                this.s.close();
                this.dis.close();
                this.dos.close();
                ChatServer.onlineMessage();
                break;
            }
            // User not found
            case 2: {
                dos.println("CLOSE#2");
                this.isloggedin = false;
                this.s.close();
                this.dis.close();
                this.dos.close();
                ChatServer.onlineMessage();
                break;
            }
            // User closed client unexpectedly
            case 3: {
                this.isloggedin = false;
                this.s.close();
                this.dis.close();
                this.dos.close();
                ChatServer.onlineMessage();
                break;
            }


        }


    }

    @Override
    public void run() {

        String received;

        while (true) {
            try {
                // receive the string
                received = dis.readLine();

                System.out.println(received);

                if (received.equals("CLOSE#")) {
                    close(0);
                }


                // break the string and check for "SEND" Command
                StringTokenizer st = new StringTokenizer(received, "#");
                String send = st.nextToken();

                if (!send.equals("SEND")) {

                    close(1);

                }

                if (send.equals("SEND")) {
                    // break the String into recipient & msg
                    String recipient = st.nextToken();
                    String MsgToSend = st.nextToken();
                    // search for the recipient in the connected devices list.
                    // ar is the vector storing client of active users
                    for (ClientHandler mc : ChatServer.ar) {

                        if (recipient.equals("*") && mc.isloggedin == true) {
                            mc.dos.println("MESSAGE#" + "*" + "#" + MsgToSend);

                        }

                        if (recipient.contains(",")) {
                            st = new StringTokenizer(recipient, ",");
                            for (int i = 0; st.hasMoreTokens(); i++) {
                                String recipients = st.nextToken();

                                if (mc.name.equals(recipients) && mc.isloggedin == true) {
                                    mc.dos.println("MESSAGE#" + this.name + "#" + MsgToSend);

                                }
                            }

                        }

                        if (mc.name.equals(recipient) && mc.isloggedin == true) {
                            mc.dos.println("MESSAGE#" + this.name + "#" + MsgToSend);
                            break;
                        }

                    }
                }

            } catch (IOException e) {
                try {
                    close(3);
                    break;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            }

        }

    }
}




