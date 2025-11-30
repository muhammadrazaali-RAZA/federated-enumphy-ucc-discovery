package at.univie.federated.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server class: Entry point for the federated server application.
 *
 * This server listens on TCP port 1234 and accepts incoming client connections.
 * For each connected client:
 * - Reads messages sent by the client.
 * - Receives and processes file data (UCC results).
 * - Prints the received data to the server console.
 * - Responds with confirmation messages.
 * - Terminates the session if the client sends "QUIT".
 *
 * Key Features:
 * - Multi-threaded server handling multiple clients concurrently.
 * - Persistent server socket listening for multiple clients.
 * - Receives and processes file data from clients.
 * - Buffered I/O streams for efficient communication.
 * - Graceful resource cleanup after each client session.
 */
public class Server
{

    public void activateServer() throws IOException {

        System.out.println( "Socket Server Started !" );

        ServerSocket serverSocket = new ServerSocket(1234);
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected from: " + socket.getRemoteSocketAddress());
                
                // Handle each client in a separate thread
                executorService.submit(new ClientHandler(socket));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Inner class to handle individual client connections in separate threads
     */
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            InputStreamReader inputStreamReader = null;
            OutputStreamWriter outputStreamWriter = null;
            BufferedReader bufferedReader = null;
            BufferedWriter bufferedWriter = null;

            try {

                inputStreamReader = new InputStreamReader(socket.getInputStream());
                outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

                bufferedReader = new BufferedReader(inputStreamReader);
                bufferedWriter = new BufferedWriter(outputStreamWriter);

                while (true) {
                    String messageFromClient = bufferedReader.readLine();

                    // If client closed the connection
                    if (messageFromClient == null) {
                        System.out.println("Client disconnected.");
                        break;
                    }

                    // Handle greeting message
                    if (messageFromClient.startsWith("HELLO")) {
                        System.out.println("Client: " + messageFromClient);
                        bufferedWriter.write("Hello received. Ready for data.");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                    // Handle file data transmission
                    else if (messageFromClient.equals("FILE_DATA")) {
                        String fileName = bufferedReader.readLine();
                        String lineCountStr = bufferedReader.readLine();
                        int lineCount = Integer.parseInt(lineCountStr);
                        
                        System.out.println("Receiving file: " + fileName);
                        System.out.println("Expected lines: " + lineCount);
                        
                        List<String> fileData = new ArrayList<>();
                        for (int i = 0; i < lineCount; i++) {
                            String line = bufferedReader.readLine();
                            if (line != null) {
                                fileData.add(line);
                            }
                        }
                        
                        // Print received file data
                        System.out.println("Received file data from client:");
                        System.out.println("--- File: " + fileName + " ---");
                        for (String line : fileData) {
                            System.out.println(line);
                        }
                        System.out.println("--- End of file ---");
                        
                        bufferedWriter.write("File data received successfully. Total lines: " + fileData.size());
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                    // Handle file not found
                    else if (messageFromClient.equals("FILE_NOT_FOUND")) {
                        System.out.println("Client: File not found on client side.");
                        bufferedWriter.write("File not found acknowledged.");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                    // Handle quit
                    else if (messageFromClient.equalsIgnoreCase("QUIT")) {
                        System.out.println("Client requested quit.");
                        bufferedWriter.write("Session terminated. Goodbye.");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        break;
                    }
                    // Handle unknown messages
                    else {
                        System.out.println("Client: " + messageFromClient);
                        bufferedWriter.write("Message Received.");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) socket.close();
                    if (inputStreamReader != null) inputStreamReader.close();
                    if (outputStreamWriter != null) outputStreamWriter.close();
                    if (bufferedReader != null) bufferedReader.close();
                    if (bufferedWriter != null) bufferedWriter.close();
                    System.out.println("Connection closed.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
