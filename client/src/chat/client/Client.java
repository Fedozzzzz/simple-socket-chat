package chat.client;

import chat.network.TCPConnection;
import chat.network.TCPConnectionListener;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Client extends JFrame implements  TCPConnectionListener{

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }

    private final JTextArea text = new JTextArea();
    private final JTextField nickname = new JTextField("user");
    private final JTextField input = new JTextField();

    private TCPConnection connection;

    private class Send implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String msg = input.getText();
            if (msg.equals("")) {
                return;
            }
            input.setText(null);
            Date date = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("HH:mm:ss");
            connection.sendMessage("(" + formatForDateNow.format(date) + ") " + nickname.getText() + " : " + msg);
        }
    }

    private Client() {
        super("Simple chat");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);

        text.setEditable(false);
        text.setLineWrap(true);

        input.addActionListener(new Send());

        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener(new Send());
        add(nickname, BorderLayout.NORTH);
        add(text, BorderLayout.CENTER);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(input);
        panel.add(sendBtn);

        add(panel, BorderLayout.SOUTH);
        setVisible(true);

        try {
            connection = new TCPConnection(this, "localhost", 3000);
        } catch (IOException e) {
            printMessage("Connection exception: " + e);
        }
    }

    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMessage("Connection ready...");
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMessage("Connection close");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMessage(value);
        sound();
    }

    private void sound() {
        File file = new File("source/sound.wav");

        AudioInputStream ais = null;
        try {
            ais = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Clip clip = AudioSystem.getClip();

            clip.open(ais);

            FloatControl vc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            vc.setValue(-8);

            clip.setFramePosition(0);
            clip.start();

        } catch (IOException | LineUnavailableException exc) {
            exc.printStackTrace();
        }
    }

    private synchronized void printMessage(String value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                text.append(value + '\n');
                text.setCaretPosition(text.getDocument().getLength());
            }
        });
    }
}
