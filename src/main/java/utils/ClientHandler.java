package utils;

import parser.RESPParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable{

    private Socket clientSocket;
    private RESPParser respParser;

    public ClientHandler(Socket socket){
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try{

            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            String line ; // e.g. *1
            respParser = new RESPParser();

            while (true) {

                List<String> command = respParser.parse(input);

                if(command == null || command.isEmpty()) break;

                String response;

                if(command.get(0).equalsIgnoreCase("PING")){
                    response = "+PONG\r\n";
                } else if (command.get(0).equalsIgnoreCase("ECHO")) {
                    response = "$" + command.get(1).length() + "\r\n" + command.get(1) + "\r\n";
                }else{
                    response = "-ERR unknown command\r\n";
                }

                output.write(response.getBytes());
                output.flush();
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
