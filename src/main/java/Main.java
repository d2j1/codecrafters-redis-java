import parser.RESPParser;
import utils.ClientHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");
//
//        String input =   "*2\r\n$4\r\nGET\r\n$-1\r\n";
//        InputStream in = new ByteArrayInputStream(input.getBytes());
//        RESPParser parser = new RESPParser();
//        List<String> command = null;
//        try {
//            command = parser.parse(in);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        System.out.println(command); // Output: [ECHO, hello]
//

        ServerSocket serverSocket = null;

        int port = 6379;

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);

            while (true) {
               Socket clientSocket = serverSocket.accept();

                Thread clientTread = new Thread(new ClientHandler(clientSocket));
                clientTread.start();
            }


        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}
