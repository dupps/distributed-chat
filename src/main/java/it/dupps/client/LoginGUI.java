package it.dupps.client;

/**
 * Created by dupps on 02.04.15.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class LoginGUI extends Dialog {

    private boolean loginPerformed = false;
    private Button login, cancel;
    protected TextField username, password;

    public LoginGUI(Frame frame){
        super(frame, "Please Login", true);
        setLayout(new FlowLayout());
        username = new TextField(15);
        password = new TextField(15);
        password.setEchoChar('*');
        add(new Label("User:"));
        add(username);
        add(new Label("Password:"));
        add(password);
        addButtonPanel();
        createFrame();
        pack();
        setVisible(true);
    }

    private void addButtonPanel() {
        Panel panel = new Panel();
        panel.setLayout(new FlowLayout());
        createButtons(panel);
        add(panel);
    }

    private void createButtons(Panel panel) {
        panel.add(login = new Button("Login"));
        login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        login.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) { }
            public void keyPressed(KeyEvent e) { }
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        panel.add(cancel = new Button("Cancel"));
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginPerformed = false;
                setVisible(false);
            }
        });
    }

    private void performLogin() {
        loginPerformed = true;
        setVisible(false);
    }

    private void createFrame() {
        Dimension d = getToolkit().getScreenSize();
        setLocation(d.width/4, d.height/3);
    }

    public boolean isLoginPerformed() {
        return loginPerformed;
    }
}