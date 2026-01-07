package at.univie.federated;

import at.univie.federated.client.Client;
import at.univie.federated.enumhyp.GenerateEnumerate;
import at.univie.federated.server.Server;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        boolean runEnumhyp = false;

        // Variables for client management
        String data = "fdReduced";
        int length = 10;

        boolean genGraph   = true;
        boolean genEnuTxt  = true;

        if (runEnumhyp) {
                GenerateEnumerate runGenerateEnumerate = new GenerateEnumerate();
                runGenerateEnumerate.generateEnumerate(data , length, genGraph, genEnuTxt);
        }

        // Start server in a separate thread
        Server server = new Server();
        Thread serverThread = new Thread(() -> {
            try {
                server.activateServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Wait a bit for server to start
        Thread.sleep(1000);

        // Start clients in separate threads to allow concurrent execution
        List<Thread> clientThreads = new ArrayList<>();

        for (int index = 0; index < length; index++) {
            Client client = new Client();
            final int currentPart = index + 1;
            Thread clientThread = new Thread(() -> {
                client.activateClient(data + "/"+data + "_p" + currentPart + ".txt", "part_" + currentPart);
            });
            clientThreads.add(clientThread);
            clientThread.start();
        }

        // Wait for all clients to finish
        for (Thread thread : clientThreads) {
            thread.join();
        }

        System.out.println("All clients completed. Server continues running...");
    }
}
