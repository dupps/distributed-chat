package it.dupps.client;

/**
 * Created by dupps on 28.03.15.
 */

import it.dupps.data.Message;
import it.dupps.network.Client;
import it.dupps.server.ClientHandler;
import it.dupps.utils.HibernateUtils;
import org.hibernate.*;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ClientGUI extends Applet implements ClientHandler {

    private Client client = null;
    private TextArea display = new TextArea();
    private TextField input = new TextField();
    private Button send = new Button("Send"),
                   connect = new Button("Connect"),
                   quit = new Button("Bye");
    private String serverName;
    private int serverPort;
    private static SessionFactory sessionFactory;

    public void init() {
        Panel keys = new Panel();
        keys.setLayout(new GridLayout(1, 2));
        keys.add(quit);
        keys.add(connect);
        Panel south = new Panel();
        south.setLayout(new BorderLayout());
        south.add("West", keys);
        south.add("Center", input);
        south.add("East", send);
        Label title = new Label("Simple Chat Client Applet", Label.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 14));
        setLayout(new BorderLayout());
        add("North", title);
        add("Center", display);
        add("South", south);
        quit.setEnabled(false);
        send.setEnabled(false);

        input.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) { }

            public void keyPressed(KeyEvent e) { }

            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    send();
                }
            }
        });
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
                input.requestFocus();
            }
        });
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                input.setText(".bye");
                send();
                quit.setEnabled(false);
                send.setEnabled(false);
                connect.setEnabled(true);
            }
        });
        connect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connect(serverName, serverPort);
            }
        });

        getParameters();
    }

    public void connect(String serverName, int serverPort) {
        println("Establishing connection. Please wait ...");
        try {
            client = new Client(new Socket(serverName, serverPort), this);
            println("Connected.");
            showHistory(5);
            send.setEnabled(true);
            connect.setEnabled(false);
            quit.setEnabled(true);
        } catch (UnknownHostException uhe) {
            println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            println("Unexpected exception: " + ioe.getMessage());
        }
    }

    private void showHistory(Integer amount) {
        List<Message> messages = getHistoryMessages(amount);
        for (int i = (messages.size() - 1); i >= 0; i--) {
            Date date = messages.get(i).getMessageTimestamp();
            String formattedDate = new SimpleDateFormat("HH:mm:ss").format(date);

            println(messages.get(i).getMessageSource() +
                    " (" + formattedDate + ") " +
                    messages.get(i).getMessageText());
        }
    }

    private List<Message> getHistoryMessages(Integer amount) {
        sessionFactory = HibernateUtils.INSTANCE.getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        List<Message> messages = null;

        try {
            Query query = session.createQuery(
                    "FROM Message ORDER BY messageTimestamp DESC");
            query.setFirstResult(0);
            query.setMaxResults(amount);
            messages = (List<Message>) query.list();
            if (tx != null) tx.commit();

        } catch (HibernateException he) {
            if (tx != null) tx.rollback();
            he.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return messages;
    }

    private void send() {
        client.send(input.getText());
        input.setText("");
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void println(String msg) {
        display.append(msg + "\n");
    }

    public void getParameters() {
        try {
            serverName = getParameter("host");
            serverPort = Integer.parseInt(getParameter("port"));
        } catch (Exception e) {
            serverName = "localhost";
            serverPort = 5000;
            println("Use default configuration " + serverName + ":" + serverPort);
        }
    }

    public void handle(Client source, String message) {
        if (message.equals(".bye")) {
            println("Good bye.");
            close();
        } else println(message);
    }

    public void onExit(Client source) {
        close();
    }
}