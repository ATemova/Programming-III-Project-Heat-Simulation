package Distributed;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Client extends UnicastRemoteObject {

    public Client() throws RemoteException {
        super();
    }
    public double[][] computeChunk(double[][] grid, int fromRow, int toRow) throws RemoteException {
        int height = grid[0].length;  // Get the height of the grid
        double[][] newGrid = new double[toRow - fromRow][height];  // Initialize a new grid for the computed chunk

        // Iterate over the specified chunk of rows
        for (int i = fromRow; i < toRow; i++) {
            for (int j = 1; j < height - 1; j++) {
                // Compute the average temperature for the current cell
                newGrid[i - fromRow][j] = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4.0;
            }
        }

        return newGrid;  // Return the computed chunk of the grid
    }

    public static void main(String[] args) {
        try {
            // Create an instance of the Client and bind it to the RMI registry
            Client worker = new Client();
            Naming.rebind("//localhost/Worker", worker);
            System.out.println("Worker ready.");
        } catch (Exception e) {
            // Print stack trace for any exceptions that occur
            e.printStackTrace();
        }
    }
}