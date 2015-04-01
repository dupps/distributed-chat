package it.dupps.client;

/**
 * Created by dupps on 28.03.15.
 */

import it.dupps.network.Client;
import it.dupps.server.ClientHandler;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientGUI extends Applet implements KeyListener, ClientHandler {

    private Client client = null;
    private TextArea display = new TextArea();
    private TextField input = new TextField();
    private Button send = new Button("Send"),
                   connect = new Button("Connect"),
                   quit = new Button("Bye");
    private String serverName;
    private int serverPort;

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
        input.addKeyListener(this);
        getParameters();
    }

    public boolean action(Event e, Object o) {
        if (e.target == quit) {
            input.setText(".bye");
            send();
            quit.setEnabled(false);
            send.setEnabled(false);
            connect.setEnabled(true);
        } else if (e.target == connect) {
            connect(serverName, serverPort);
        } else if (e.target == send) {
            send();
            input.requestFocus();
        }
        return true;
    }

    public void connect(String serverName, int serverPort) {
        println("Establishing connection. Please wait ...");
        try {
            client = new Client(new Socket(serverName, serverPort), this);
            println("Connected.");
            send.setEnabled(true);
            connect.setEnabled(false);
            quit.setEnabled(true);
        } catch (UnknownHostException uhe) {
            println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            println("Unexpected exception: " + ioe.getMessage());
        }
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

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) { }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            send();
        }
    }

    @Override
    public void handle(Client source, String message) {
        if (message.equals(".bye")) {
            println("Good bye.");
            close();
        } else println(message);
    }

    @Override
    public void onExit(Client source) {
        close();
    }
}