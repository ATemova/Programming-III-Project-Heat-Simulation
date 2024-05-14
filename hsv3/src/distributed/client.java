import java.net.Socket;
import java.io.IOException;

public class client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            System.out.println("Connected to server at " + SERVER_IP + ":" + SERVER_PORT);

            // You can now use the socket to communicate with the server
            // This could be reading from or writing to the socket's input and output streams
        } catch (IOException e) {
            System.out.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
