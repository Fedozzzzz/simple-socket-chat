package chat.server;

import chat.network.TCPConnection;
import chat.network.TCPConnectionListener;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class Server implements TCPConnectionListener {

    private final ArrayList<TCPConnection> TCPConnections = new ArrayList<>();

    public static void main(String[] args) {
        new Server();
    }

    private Server() {
        System.out.println("server running...");
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
        sendToAllConnections("A new user joined the chat!!! Client connected: " + tcpConnection);
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        TCPConnections.remove(tcpConnection);
        sendToAllConnections("User left the chat((( Client disconnected: " + tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
    }
}
