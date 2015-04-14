package it.dupps.network;

import it.dupps.server.ClientHandler;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by dupps on 01.04.15.
 */
public class Client implements IOHandler {
    private final IOThread thread;
    private final ClientHandler handler;
    private String username;

    public Client(Socket socket, ClientHandler handler) throws IOException {
        this.thread = new IOThread(this, socket);
        this.handler = handler;
    }

    public void close() throws IOException {
        this.thread.close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Client client = (Client) o;

        return !(thread != null ? !thread.equals(client.thread) : client.thread != null);
    }

    @Override
    public int hashCode() {
        return thread != null ? thread.hashCode() : 0;
    }

    public void handle(IOThread source, String message) {
        handler.handle(this, message);
    }

    public void onExit(IOThread source) {
        handler.onExit(this);
    }

    public void send(String message) {
        this.thread.send(message);
    }

    public int getID() {
        return thread.getID();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
