package server;// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java


import java.io.*;
import java.util.*;
import java.net.*;

// TESTING GITPUSH

// Server class
public class Server {

    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();


    public static void main(String[] args) throws IOException {
        // list of approved users
        UserLogin userlogin = new UserLogin();
        List<String> userList = userlogin.listOfUsers();

        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);

        Socket s;

        // running infinite loop for getting
        // client request
        while (true) {
            // Accept the incoming request
            s = ss.accept();

            System.out.println("New client request received : " + s);

            // obtain input and output streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());


            try {
                StringTokenizer st = new StringTokenizer(dis.readUTF(), "#");
                String cmd = st.nextToken();
                String username = st.nextToken();

                if (cmd.equals("CONNECT")) {

                    int i = 0;

                    for (String users : userList) {


                        if (username.equals(users)) {

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

                            break;

                        }
                        i++;

                        if (i == userList.size()) {

                            dos.writeUTF("username doesn't exist");
                            s.close();
                            break;
                        }


                    }


                } else {
                    s.close();
                }

            } catch (Exception e) {
                s.close();

            }

        }
    }

    public static void onlineMessage() throws IOException {

        int vectorSize = Server.ar.size(); // sætter vores vector størerelse fast til en variabel
        int checks = 0;
        StringBuilder stringBuilder = new StringBuilder(); // laver string builder.
        for (ClientHandler allMc : Server.ar) { //gennemgår vores clientliste

            checks++;

            if (allMc.isloggedin == true && vectorSize == checks) {
                stringBuilder.append(allMc.getName());
                break;
            }

            if (allMc.isloggedin == true && vectorSize > checks) {
                stringBuilder.append(allMc.getName() + ",");


            }


        } // tilføjer navne til vores online besked.
        for (ClientHandler allMc : Server.ar) {
            if (allMc.isloggedin == true && vectorSize == checks) {
                allMc.dos.writeUTF("ONLINE#" + stringBuilder.toString());
            }
        } // sender online beskeden ud

    }

}

// ClientHandler class
class ClientHandler implements Runnable {
    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isloggedin;

    // constructor
    public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isloggedin = true;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {

        String received;
        while (true) {
            try {
                // receive the string
                received = dis.readUTF();

                System.out.println(received);

                if (received.equals("CLOSE#")) {
                    this.isloggedin = false;
                    dos.writeUTF("CLOSE");
                    this.s.close();
                    Server.onlineMessage();
                    break;
                }


                // break the string and check for "SEND" Command
                StringTokenizer st = new StringTokenizer(received, "#");
                String send = st.nextToken();

                if (!send.equals("SEND")){

                    this.isloggedin = false;
                    dos.writeUTF("CLOSE");
                    this.s.close();
                    Server.onlineMessage();

                }

                if (send.equals("SEND")) {
                // break the String into recipient & msg
                String recipient = st.nextToken();
                String MsgToSend = st.nextToken();
                    // search for the recipient in the connected devices list.
                    // ar is the vector storing client of active users
                    for (ClientHandler mc : Server.ar) {

                        if (recipient.contains(",")) {
                            st = new StringTokenizer(recipient, ",");
                            for (int i = 0; st.hasMoreTokens(); i++) {
                                recipient = st.nextToken();

                                for (ClientHandler allMc : Server.ar) {

                                    if (allMc.name.equals(recipient) && allMc.isloggedin == true) {
                                        allMc.dos.writeUTF("MESSAGE#" + this.name + "#" + MsgToSend);

                                        break;
                                    }
                                    break;
                                }

                            }


                        }


                        if (mc.name.equals(recipient) && mc.isloggedin == true) {
                            mc.dos.writeUTF("MESSAGE#" + this.name + "#" + MsgToSend);
                            break;
                        }

                        if (recipient.equals("*") && mc.isloggedin == true) {

                            for (ClientHandler allMc : Server.ar) {
                                allMc.dos.writeUTF("MESSAGE#" + this.name + "#" + MsgToSend);
                            }
                            break;

                        }
                    }
                }
            } catch (IOException e) {

                try {
                    this.isloggedin = false;
                    this.s.close();
                    Server.onlineMessage();
                    break;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        }
        try {
            // closing resources
            this.dis.close();
            this.dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




