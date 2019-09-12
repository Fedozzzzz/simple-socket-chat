package chat.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPConnection {

    private final Socket socket;
    private final Thread rxThread;
    public final BufferedWriter out;
    private final BufferedReader in;

    private static Logger logger = Logger.getLogger(TCPConnection.class.getName());


    public TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.socket = socket;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));

        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));

        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()) {
                        eventListener.onReceiveString(TCPConnection.this, in.readLine());
                    }
                } catch (IOException err) {
                    logger.log(Level.WARNING, "TCPConnection exception: ", err);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();
    }

    public TCPConnection(TCPConnectionListener eventListener, String ip, int port) throws IOException {
        this(eventListener, new Socket(ip, port));
    }

    public synchronized void sendMessage(String value) {
        try {
            out.write(value + "\r\n");
            out.flush();
        } catch (IOException err) {
            logger.log(Level.WARNING, "TCPConnection exception: ", err);
            disconnect();
        }
    }

    private synchronized void disconnect() {
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException err) {
            logger.log(Level.WARNING, "TCPConnection exception: ", err);
        }
    }

    @Override
    public String toString() {
        return socket.getInetAddress() + ": " + socket.getPort();
    }
}
