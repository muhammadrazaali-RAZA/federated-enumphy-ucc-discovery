package at.univie.federated.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;


/**
 * App class: Entry point for the federated client application.
 *
 * This client establishes a TCP connection to a server running on localhost:1234.
 * It enables interactive communication by sending user input to the server
 * and printing server responses back to the console.
 *
 * Key Features:
 * - Socket-based communication using java.net.Socket
 * - Buffered I/O streams for efficient data transfer
 * - Graceful resource cleanup in finally block
 * - Interactive loop with termination keyword ("QUIT")
 */
public class Client {
    public static void main(String[] args) {
        System.out.println("Socket Client Started !");

        String host = System.getenv().getOrDefault("SERVER_HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "1234"));
        String nodeId = System.getenv().getOrDefault("NODE_ID", "node");

        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {
            socket = new Socket(host, port);
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            // 1) Send a greeting / later this will be your local UCC result
            String msg1 = "HELLO from " + nodeId;
            bufferedWriter.write(msg1);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            System.out.println("Sent: " + msg1);
            System.out.println("Server: " + bufferedReader.readLine() + " ;");

            // 2) Send QUIT to close the session cleanly
            String msg2 = "QUIT";
            bufferedWriter.write(msg2);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            System.out.println("Sent: " + msg2);
            System.out.println("Server: " + bufferedReader.readLine() + " ;");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) socket.close();
                if (inputStreamReader != null) inputStreamReader.close();
                if (outputStreamWriter != null) outputStreamWriter.close();
                if (bufferedReader != null) bufferedReader.close();
                if (bufferedWriter != null) bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}