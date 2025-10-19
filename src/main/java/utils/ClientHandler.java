package utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler implements Runnable{

    private Socket clientSocket;

    public ClientHandler(Socket socket){
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try{

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));


            String line ; // e.g. *1


            while((line = in.readLine()) != null){

//                System.out.println(line);
//                System.out.println(line.trim().startsWith("*"));

                // If the command starts with '*', it’s in RESP format
                if (line.trim().startsWith("*")) {
                    in.readLine(); // skip $4
                    String command = in.readLine(); // read "PING"
//                    System.out.println("cmd " + command);

                    if (command != null && command.trim().equalsIgnoreCase("PING")) {
                        out.write("+PONG\r\n");
                        out.flush();
                    } else {
                        out.write("-ERR unknown command\r\n");
                        out.flush();
                    }
                } else if (line.trim().equalsIgnoreCase("PING")) {
                    // For plain telnet testing
                    out.write("+PONG\r\n");
                    out.flush();
                } else {
                    out.write("-ERR unknown command\r\n");
                    out.flush();
                }

            }

        }catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }finally {
            if(clientSocket != null){
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
