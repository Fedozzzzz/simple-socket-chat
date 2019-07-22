package chat.server;

import chat.network.TCPConnection;
import chat.network.TCPConnectionListener;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.LinkedList;

public class Server implements TCPConnectionListener {

    private final ArrayList<TCPConnection> TCPConnections = new ArrayList<>();

    public static Story story;

    public static void main(String[] args) {
        new Server();
    }

    private Server() {
        System.out.println("server running...");
        story = new Story();
        try (ServerSocket serverSocket = new ServerSocket(3000)) {
            while (true) {
                try {
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException err) {
                    System.out.println("TCPConnection exception: " + err);
                }
            }
        } catch (IOException e) {
            System.out.println("TCPConnection exception: " + e);
        }
    }

    private void sendToAllConnections(String value) {
        System.out.println(value);
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

    private final int MAX_SIZE = 1000;

    public void addToStory(String value) {
        if (story.size() >= MAX_SIZE) {
            story.removeFirst();
            story.add(value);
        } else {
            story.add(value);
        }
        savedStory = new SavedStory(story);
        try {
            FileOutputStream outputStream = new FileOutputStream("C:\\Users\\FedozZz\\Мои документы\\chat_java\\save.ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(savedStory);
            objectOutputStream.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void printStory(BufferedWriter wr) {
        if (story.size() > 0) {
            try {
                wr.write("History messages: " + "\n");
                int storySize = story.size();
                int i = storySize > 10 ? storySize - 10 : 0;
                for (i = i; i < storySize; i++) {
                    wr.write(story.get(i) + "\n");
                }
                wr.write("/........." + "\n");
                wr.flush();
            } catch (IOException e) {
                e.printStackTrace();
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