package Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Tester {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost",8000);
        PrintWriter pw = new PrintWriter(socket.getOutputStream(),true);
        Scanner scanner = new Scanner(socket.getInputStream());
        pw.println("CONNECT#Anders");
        String msg = scanner.nextLine();
        System.out.println(msg);
        System.out.println(msg.equals("ONLINE#Anders"));
        pw.println("SEND#Anders#hej");
        msg = scanner.nextLine();
        System.out.println(msg);
        System.out.println(msg.equals("MESSAGE#Anders#hej"));
    }


}
