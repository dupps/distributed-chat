package it.dupps.network;

/**
 * Created by dupps on 01.04.15.
 */
public interface IOHandler {
    void handle(IOThread source, String message);
    void onExit(IOThread source);
}
