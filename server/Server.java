package server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 5063;
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    static final ConcurrentHashMap<String, Socket> activePassengers = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, Socket> activeBusOwners = new ConcurrentHashMap<>();
    static final ConcurrentHashMap<String, Socket> activeAdmins = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);

//         Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            pool.shutdown();
            try {
                if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
                serverSocket.close();
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        }));

        while (true) {
            try {
                System.out.println("Waiting for connection...");
                Socket client = serverSocket.accept();
                System.out.println("Connection accepted");
                pool.execute(new ClientHandler(client));

            } catch (IOException e) {
                System.err.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }
}