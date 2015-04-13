package it.dupps.server;

/**
 * Created by dupps on 28.03.15.
 */

import com.google.gson.Gson;
import it.dupps.communication.Communication;
import it.dupps.network.Client;
import it.dupps.persistance.data.Message;
import it.dupps.persistance.utils.HibernateUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

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
    private static SessionFactory sessionFactory;

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

    private void handleAuth(Client source, Communication com) {
        // TODO: implement method
    }

    private void handleMessage(Client source, Communication com) {
        if (com.getPayload().equals(".bye")) {
            source.send(".bye");
            try {
                source.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            remove(source);

        } else {
            persistMessage(com.getPayload(), source.getID());
            for (Client client : clients) {
                // TODO: mapping of username and socket port
                String message = com.getUsername() + ": " + com.getPayload();
                String json = new Gson().toJson(message);
                client.send(json);
            }
        }
    }

    private void handleHistory(Client source, Communication com) {
        // TODO: implement method
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
        else {
            new ChatServer(Integer.parseInt(args[0]));
            HibernateUtils.INSTANCE.getSessionFactory();
        }
    }

    private int persistMessage(String message, Integer clientID) {
        sessionFactory = HibernateUtils.INSTANCE.getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Integer messageId = null;

        try {
            tx = session.beginTransaction();
            Message messageObject = new Message();
            messageObject.setMessageText(message);
            messageObject.setMessageSource(clientID.toString());
            messageId = (Integer) session.save(messageObject);
            tx.commit();

        } catch (HibernateException e) {
            if (tx!=null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return messageId;
    }
}