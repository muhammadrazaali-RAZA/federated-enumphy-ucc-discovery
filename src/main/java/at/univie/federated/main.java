package at.univie.federated;

import at.univie.federated.client.Client;
import at.univie.federated.enumhyp.GenerateEnumerate;
import at.univie.federated.server.Server;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws Exception {

        boolean runEnumhyp = false;

        if (runEnumhyp) {
                GenerateEnumerate runGenerateEnumerate = new GenerateEnumerate();
                runGenerateEnumerate.generateEnumerate();
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

        // 4. Start clients in separate threads to allow concurrent execution
        Client client1 = new Client();
        Thread client1Thread = new Thread(() -> {
            client1.activateClient("abalone_p1.txt", "part_1");
        });

        Client client2 = new Client();
        Thread client2Thread = new Thread(() -> {
            client2.activateClient("abalone_p2.txt", "part_2");
        });

        // Start both clients concurrently
        client1Thread.start();
        client2Thread.start();

        // Wait for clients to finish
        client1Thread.join();
        client2Thread.join();

        System.out.println("All clients completed. Server continues running...");
    }
}
