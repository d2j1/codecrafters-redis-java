import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from your program will appear here!");

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);

            while (true) {
                clientSocket = serverSocket.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                String line = in.readLine(); // e.g. *1
                if (line == null) {
                    clientSocket.close();
                    continue;
                }

                // If the command starts with '*', it’s in RESP format
                if (line.startsWith("*")) {
                    in.readLine(); // skip $4
                    String command = in.readLine(); // read "PING"

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

                clientSocket.close();
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}
