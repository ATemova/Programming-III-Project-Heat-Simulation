package distributed;

import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        // Handle client logic here, such as exchanging messages
        // This could involve setting up input and output streams to read from and write to the socket
    }
}
