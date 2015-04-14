package it.dupps.client;

/**
 * Created by dupps on 28.03.15.
 */

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.dupps.communication.ComType;
import it.dupps.communication.Communication;
import it.dupps.network.Client;
import it.dupps.persistance.data.Message;
import it.dupps.server.ClientHandler;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    private UUID token = null;

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

        getParameters();
        connect(serverName, serverPort);
        performLogin();
    }

    private void performLogin() {
        LoginGUI loginGUI = new LoginGUI(new Frame(""));
        requestFocus();
        if (loginGUI.isLoginPerformed()) {
            String username = loginGUI.username.getText().trim();
            String password = loginGUI.password.getText().trim();
            Communication com = new Communication(ComType.AUTH);
            com.setUsername(username);
            com.setPassword(password);
            String json = new Gson().toJson(com);
            client.send(json);
        }
        loginGUI.dispose();
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

    private void showHistory(Integer amount) {
        Communication com = new Communication(ComType.HISTORY);
        com.setAmount(amount);
        String json = new Gson().toJson(com);
        client.send(json);
    }

    private void send() {
        Communication com = new Communication(ComType.MESSAGE);
        com.setPayload(input.getText());
        String json = new Gson().toJson(com);
        client.send(json);
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

    public void handle(Client source, String json) {
        Communication com = null;
        try {
            com = new Gson().fromJson(json, Communication.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (com != null) {
            switch (com.getType()) {
                case AUTH:
                    handleAuth(com);
                    break;

                case MESSAGE:
                    handleMessage(com);
                    break;

                case HISTORY:
                    handleHistory(com);
                    break;
            }
        } else {
            System.out.println("Error while parsing JSON: " + json);
        }
    }

    private void handleAuth(Communication com) {
        if(com.getToken() != null) {
            this.token = com.getToken();
            showHistory(5);
            //title.setText("Welcome " + this.token + "!");
            connect.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    display.setText("");
                    connect(serverName, serverPort);
                }
            });
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

        } else {
            display.setText("Login failed.");
        }
    }

    private void handleMessage(Communication com) {
        if (com.getPayload().equals(".bye")) {
            println("Good bye.");
            close();
        } else println(com.getUsername() + ": " + com.getPayload());
    }

    private void handleHistory(Communication com) {
        Type listType = new TypeToken<ArrayList<Message>>() {}.getType();
        List<Message> messages = new Gson().fromJson(com.getPayload(), listType);
        for (int i = (messages.size() - 1); i >= 0; i--) {
            Date date = messages.get(i).getMessageTimestamp();
            String formattedDate = new SimpleDateFormat("HH:mm:ss").format(date);

            println(messages.get(i).getMessageSource() +
                    " (" + formattedDate + ") " +
                    messages.get(i).getMessageText());
        }
    }

    public void onExit(Client source) {
        close();
    }
}