package it.dupps.network;

/**
 * Created by dupps on 01.04.15.
 */
public interface IOHandler {
    public void handle(IOThread source, String message);
    public void onExit(IOThread source);
}
