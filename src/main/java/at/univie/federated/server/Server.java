package at.univie.federated.server;

import java.io.*;
import java.net.*;

/**
 * Server class: Entry point for the federated server application.
 *
 * This server listens on TCP port 1234 and accepts incoming client connections.
 * For each connected client:
 * - Reads messages sent by the client.
 * - Prints the received message to the server console.
 * - Responds with a confirmation message ("Message Received.").
 * - Terminates the session if the client sends "QUIT".
 *
 * Key Features:
 * - Persistent server socket listening for multiple clients.
 * - Buffered I/O streams for efficient communication.
 * - Graceful resource cleanup after each client session.
 */
public class Server
{

    public static void main( String[] args ) throws IOException {

        System.out.println( "Socket Server Started !" );

        Socket socket = null;

        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;

        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        ServerSocket serverSocket = new ServerSocket(1234);

        while (true) {
            try{
                socket = serverSocket.accept();

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

                    System.out.println("Client: " + messageFromClient + " ;");

                    bufferedWriter.write("Message Received.");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                    if (messageFromClient.equalsIgnoreCase("QUIT")) {
                        System.out.println("Client requested quit.");
                        break;
                    }
                }

                socket.close();
                inputStreamReader.close();
                outputStreamWriter.close();
                bufferedReader.close();
                bufferedWriter.close();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
