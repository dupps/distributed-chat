package it.dupps.server;

import it.dupps.network.Client;

/**
 * Created by dupps on 01.04.15.
 */
public interface ClientHandler {
    public void handle(Client source, String message);
    public void onExit(Client source);
}
