package it.dupps.server;

/**
 * Created by dupps on 28.03.15.
 */

import it.dupps.network.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChatServer implements Runnable, ClientHandler {

    private Set<Client> clients = Collections.synchronizedSet(new HashSet<Client>());
    private ServerSocket server = null;
    private Thread thread = null;

    public ChatServer(int port) {
        try {
            System.out.println("Binding to port " + port + ", please wait  ...");
            server = new ServerSocket(port);
            System.out.println("Server started: " + server);
            start();
        } catch (IOException ioe) {
            System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
        }
    }

    public void run() {
        while (thread != null) {
            try {
                System.out.println("Waiting for a client ...");
                addThread(server.accept());
            } catch (IOException ioe) {
                System.out.println("Server accept error: " + ioe);
                stop();
            }
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public synchronized void handle(Client source, String message) {
        if (message.equals(".bye")) {
            source.send(".bye");
            try {
                source.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            remove(source);
        } else for (Client client : clients) {
            client.send(message);
        }
    }

    public void onExit(Client source) {
        System.out.println("Disconnected client " + source.getID());
        remove(source);
    }

    public synchronized void remove(Client client) {
        clients.remove(client);
    }

    private void addThread(Socket socket) {
        try {
            clients.add(new Client(socket, this));
            System.out.println("Client accepted on " + socket.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        if (args.length != 1) System.out.println("Usage: java ChatServer port");
        else new ChatServer(Integer.parseInt(args[0]));
    }
}