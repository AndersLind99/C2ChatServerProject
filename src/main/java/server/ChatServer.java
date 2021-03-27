package server;


import java.io.*;
import java.util.*;
import java.net.*;


// Server class
public class ChatServer {
    // Vector to store active clients
    static Vector<ClientHandler> ar = new Vector<>();

    public static void enqueue(Runnable r) {
        Thread t = new Thread(r);
        t.start();
    }

    /**
     * sendMessage attempts to send a message to the given destination.
     */
    public static void sendMessage(ClientHandler source, String destination, String message) {
        StringBuilder builder = new StringBuilder();
        builder.append("MESSAGE#");
        if (destination == "*") {
            builder.append("*");
        } else {
            builder.append(source.getName());
        }

        builder.append("#");
        builder.append(message);


        String msg = builder.toString();
        for (ClientHandler mc : ChatServer.ar) {
            if (!mc.nameMatches(pattern)) {
                continue;
            }

            mc.sendMessage(msg);
        }
    }

    public static void main(String[] args) throws IOException {
        UserService userService = new UserService();
        ServerSocket ss = new ServerSocket(8000);
        System.out.println("Server started");

        while (true) {
            Socket s = ss.accept();
            System.out.println("New client request received : " + s);
            ClientHandler handler = ClientHandler.tryAccept(s, userService);
            if (handler == null) {
                // We failed to add the client.
                s.close();
                continue;
            }

            System.out.println("Adding this client to active client list");
            ar.add(handler);
            ChatServer.enqueue(handler);
            onlineMessage();
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


    /**
     * Try to accept the connection. Returns false if the connection is rejected. for any reason.
     * @param userService
     * @return
     */
    static ClientHandler tryAccept(Socket s, UserService userService) {
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

                return new ClientHandler(s, usenrame, dis, dos);
            }

            dos.println("CLOSE#1");
        } catch (Exception e) {
            // Intentionally swallowing the exception.
        } finally {
            // Hitting here means that the client failed to be accepted.
            return null;
        }
    }

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

    // nameMatches indicates whether or not the given pattern matches the ClientHandlers name.
    public boolean nameMatches(String pattern) {
        if (pattern.contains("*")) {
            return true;
        }

        if (pattern.equals(this.name)) {
            return true;
        }

        if (pattern.contains(",")) {
            st = new StringTokenizer(pattern, ",");
            for (int i = 0; st.hasMoreTokens(); i++) {
                if (this.name.equals(st.nextToken())) {
                    return true;
                }
            }
        }

        return false;
    }

    // sendMessage attempts to send a message to this ClientHandler.
    // This will silently fail if the user is not logged in.
    public void sendMessage(String message) {
        if (this.isloggedin == false) {
            return;
        }

        this.dos.write(message);
    }

    public void close(int i) throws IOException {
        switch (i) {
            case 0:
                dos.println("CLOSE#0");
                break;
            case 1:
                dos.println("CLOSE#1");
                break;
            case 2:
                dos.println("CLOSE#2");
                break;
        }

        this.isloggedin = false;
        this.s.close();
        this.dis.close();
        this.dos.close();
        ChatServer.onlineMessage();
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
                    String messageToSend = st.nextToken();

                    ChatServer.sendMessage(this, recipient, messageToSend);
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




