package it.dupps.client;

/**
 * Created by dupps on 28.03.15.
 */

import it.dupps.data.Message;
import it.dupps.data.User;
import it.dupps.network.Client;
import it.dupps.server.ClientHandler;

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
    private Label title;
    private String serverName;
    private int serverPort;
    private User user = null;
    private ClientPersistance persistance;

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
        title = new Label("Simple Chat Client Applet", Label.CENTER);
        title.setFont(new Font("Helvetica", Font.BOLD, 14));
        setLayout(new BorderLayout());
        add("North", title);
        add("Center", display);
        add("South", south);
        quit.setEnabled(false);
        send.setEnabled(false);
        connect.setEnabled(false);
        persistance = new ClientPersistance();

        if(loginWasSuccessful()) {
            getParameters();
            title.setText("Welcome " + user.getEmail() + "!");
            connect(serverName, serverPort);
            connect.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    display.setText("");
                    connect(serverName, serverPort);
                }
            });
            input.addKeyListener(new KeyListener() {
                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                }

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

        } else {
            display.setText("Login failed.");
        }
    }

    private boolean loginWasSuccessful() {
        LoginGUI loginGUI = new LoginGUI(new Frame(""));
        requestFocus();
        if (loginGUI.isLoginPerformed()) {
            String username = loginGUI.username.getText().trim();
            String password = loginGUI.password.getText().trim();
            this.user = persistance.getUser(username, password);
            if (this.user != null) return true;
        }
        loginGUI.dispose();
        return false;
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
        List<Message> messages = persistance.getHistoryMessages(amount);
        for (int i = (messages.size() - 1); i >= 0; i--) {
            Date date = messages.get(i).getMessageTimestamp();
            String formattedDate = new SimpleDateFormat("HH:mm:ss").format(date);

            println(messages.get(i).getMessageSource() +
                    " (" + formattedDate + ") " +
                    messages.get(i).getMessageText());
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