package server;// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java


import java.io.*;
import java.util.*;
import java.net.*;

// TODO Add syntax to messages. #SEND#(User)#(Message)

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

                        break;

                    }
                    i++;

                    if (i == userList.size()){

                        dos.writeUTF("username doesn't exist");
                        s.close();
                        break;
                    }



                }


            } else {
                s.close();
            }


        }
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

    @Override
    public void run() {

        String received;
        while (true) {
            try {
                // receive the string
                received = dis.readUTF();

                System.out.println(received);

                if (received.equals("logout")) {
                    this.isloggedin = false;
                    this.s.close();
                    break;
                }


                // break the string into message and recipient part
                StringTokenizer st = new StringTokenizer(received, "#");
                String recipient = st.nextToken();
                String MsgToSend = st.nextToken();

                // search for the recipient in the connected devices list.
                // ar is the vector storing client of active users
                for (ClientHandler mc : Server.ar) {
                    // if the recipient is found, write on its
                    // output stream
                    if (mc.name.equals(recipient) && mc.isloggedin == true) {
                        mc.dos.writeUTF(this.name + " : " + MsgToSend);
                        break;
                    }
                }
            } catch (IOException e) {

                e.printStackTrace();
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




