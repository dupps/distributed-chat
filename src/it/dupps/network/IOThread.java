package it.dupps.network;

/**
 * Created by dupps on 28.03.15.
 */

import java.io.*;
import java.net.Socket;

public class IOThread extends Thread {

    private final IOHandler ioHandler;
    private final Socket socket;
    private final DataInputStream streamIn;
    private final DataOutputStream streamOut;

    public IOThread(IOHandler ioHandler, Socket socket) throws IOException {
        super();
        this.ioHandler = ioHandler;
        this.socket = socket;
        streamIn = new DataInputStream(new
                BufferedInputStream(socket.getInputStream()));
        streamOut = new DataOutputStream(new
                BufferedOutputStream(socket.getOutputStream()));
        this.start();
    }

    public boolean equals(Object other) {
        if (!(other instanceof IOThread)) return false;
        IOThread otherThread = (IOThread) other;
        return getID() == otherThread.getID();
    }

    public int getID() {
        return socket.getPort();
    }

    public int hashCode() {
        return getID();
    }

    public void send(String msg) {
        try {
            streamOut.writeUTF(msg);
            streamOut.flush();
        } catch (IOException ioe) {
            System.out.println(getID() + " ERROR sending: " + ioe.getMessage());
            ioHandler.onExit(this);
            stop();
        }
    }

    public void run() {
        System.out.println("Client Thread " + getID() + " running.");
        while (!isInterrupted()) {
            try {
                ioHandler.handle(this, streamIn.readUTF());
            } catch (IOException ioe) {
                ioHandler.onExit(this);
                break;
            }
        }
    }

    public void close() throws IOException {
        if (socket != null) socket.close();
        if (streamIn != null) streamIn.close();
        if (streamOut != null) streamOut.close();
    }
}