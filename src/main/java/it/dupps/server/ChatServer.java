package it.dupps.server;

/**
 * Created by dupps on 28.03.15.
 */

import com.google.gson.Gson;
import it.dupps.communication.ComType;
import it.dupps.communication.Communication;
import it.dupps.network.Client;
import it.dupps.persistance.Authenticator;
import it.dupps.persistance.MessageFacade;
import it.dupps.persistance.data.Message;
import it.dupps.persistance.utils.HibernateUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer implements Runnable, ClientHandler {

    private Set<Client> clients = Collections.synchronizedSet(new HashSet<Client>());
    private Map<UUID, Client> authenticatedClients = new HashMap<UUID, Client>();
    private ServerSocket server = null;
    private Thread thread = null;
    private MessageFacade messageFacade = new MessageFacade();

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

    public synchronized void handle(Client source, String json) {
        Communication com = null;
        try {
            com = new Gson().fromJson(json, Communication.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (com != null) {
            switch (com.getType()) {
                case AUTH:
                    handleAuth(source, com);
                    break;

                case MESSAGE:
                    handleMessage(source, com);
                    break;

                case HISTORY:
                    handleHistory(source, com);
                    break;
            }
        } else {
            System.out.println("Error while parsing JSON: " + json);
        }
    }

    private void handleAuth(Client client, Communication com) {
        UUID token;
        token = new Authenticator().authenticate(com.getUsername(), com.getPassword());
        if (token != null) {
            authenticatedClients.put(token, client);
            Communication comObj = new Communication(ComType.AUTH);
            comObj.setToken(token);
            String json = new Gson().toJson(comObj);
            client.send(json);

        } else {
            System.out.println("Authentication failed for user " + com.getUsername());
        }
    }

    private void handleMessage(Client source, Communication com) {
        if (com.getPayload().equals(".bye")) {
            Communication comObj = new Communication(ComType.MESSAGE);
            comObj.setPayload(com.getPayload());
            String json = new Gson().toJson(comObj);
            source.send(json);
            try {
                source.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            remove(source);

        } else {
            messageFacade.persistMessage(com.getPayload(), source.getID());
            for (Client client : authenticatedClients.values()) {
                Communication comObj = new Communication(ComType.MESSAGE);
                comObj.setUsername(Integer.toString(source.getID()));
                // TODO: mapping of username and socket port
                comObj.setPayload(com.getPayload());
                String json = new Gson().toJson(comObj);
                client.send(json);
            }
        }
    }

    private void handleHistory(Client client, Communication com) {
        Gson gson = new Gson();
        List<Message> messages = messageFacade.getHistoryMessages(com.getAmount());
        String msgJson = gson.toJson(messages);
        Communication comObj = new Communication(ComType.HISTORY);
        comObj.setPayload(msgJson);
        String comJson = gson.toJson(comObj);
        client.send(comJson);
    }

    public void onExit(Client source) {
        System.out.println("Disconnected client " + source.getID());
        remove(source);
    }

    public synchronized void remove(Client client) {
        clients.remove(client);
        authenticatedClients.values().remove(client);
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
        else {
            new ChatServer(Integer.parseInt(args[0]));
            HibernateUtils.INSTANCE.getSessionFactory();
        }
    }

}