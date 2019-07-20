package chat.network;

public interface TCPConnectionListener {

    void onConnectionReady(TCPConnection tcpConnection);

    void onDisconnect(TCPConnection tcpConnection);

    void onReceiveString(TCPConnection tcpConnection, String value);

}
