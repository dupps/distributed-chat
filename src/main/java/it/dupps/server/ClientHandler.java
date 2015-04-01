package it.dupps.server;

import it.dupps.network.Client;

/**
 * Created by dupps on 01.04.15.
 */
public interface ClientHandler {
    void handle(Client source, String message);
    void onExit(Client source);
}
