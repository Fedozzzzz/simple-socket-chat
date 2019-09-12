package chat.server;

import chat.network.TCPConnection;//maven, ant, gradle
import chat.network.TCPConnectionListener;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements TCPConnectionListener {//logger

    private final ArrayList<TCPConnection> TCPConnections = new ArrayList<>();

    public static Story story;

    private static Logger logger = Logger.getLogger(Server.class.getName());


    public static void main(String[] args) {
        new Server();
    }

    private Server() {
        logger.info("server on listening");
        story = new Story();
        try (ServerSocket serverSocket = new ServerSocket(3000)) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException err) {
                    logger.log(Level.WARNING, "TCPConnection exception: ", err);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "TCPConnection exception: ", e);

        }
    }

    private void sendToAllConnections(String value) {
        logger.info(value);
        for (TCPConnection node : TCPConnections) {
            node.sendMessage(value);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        TCPConnections.add(tcpConnection);
        story.printStory(tcpConnection.out);
        sendToAllConnections("A new user joined the chat!!! Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        TCPConnections.remove(tcpConnection);
        sendToAllConnections("User left the chat((( Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        story.addToStory(value);
        sendToAllConnections(value);
    }
}


class Story {
    private LinkedList<String> story = new LinkedList<>();

    private SavedStory savedStory;

    private static Logger logger = Logger.getLogger(Story.class.getName());

    private final static int MAX_SIZE = 1000;
    private final static int HISTORY_SIZE = 10;


    public Story() {

        try {
            FileInputStream fileInputStream = null;
            fileInputStream = new FileInputStream("save.ser");
            ObjectInputStream objectInputStream = null;
            objectInputStream = new ObjectInputStream(fileInputStream);
            this.savedStory = (SavedStory) objectInputStream.readObject();

        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "File not found", e);
        } catch (IOException e) {
            logger.log(Level.WARNING, "error", e);
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "Class not found", e);
        }
        try {
            this.addToStory(savedStory.getSavedStory());
        } catch (NullPointerException e) {
            logger.log(Level.WARNING, "Null pointer", e);
        }
    }

    private void addToStory(LinkedList<String> list) {
        for (String el :
                list) {
            this.addToStory(el);

        }
    }

    public void addToStory(String value) {
        if (story.size() >= MAX_SIZE) {
            story.removeFirst();
            story.add(value);
        } else {
            story.add(value);
        }
        savedStory = new SavedStory(story);
        try {
            FileOutputStream outputStream = new FileOutputStream("save.ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(savedStory);
            objectOutputStream.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "error", e);
        }
    }

    public void printStory(BufferedWriter wr) {
        if (story.size() > 0) {
            try {
                wr.write("History messages: " + "\n");
                int storySize = story.size();
                int i = storySize > HISTORY_SIZE ? storySize - HISTORY_SIZE : 0;//
                for (i = i; i < storySize; i++) {
                    wr.write(story.get(i) + "\n");
                }
                wr.write("/........." + "\n");
                wr.flush();
            } catch (IOException e) {
                logger.log(Level.WARNING, "error", e);
            }
        }
    }
}


class SavedStory implements Serializable {
    private static final long serialVersionUID = 1L;

    private LinkedList<String> savedStory;

    public SavedStory(LinkedList<String> story) {
        savedStory = new LinkedList<>(story);
    }

    public LinkedList<String> getSavedStory() {
        return savedStory;
    }
}