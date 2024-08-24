package Distributed;
import java.rmi.Naming;  // Import for RMI naming services
import java.rmi.RemoteException;  // Import for handling remote exceptions
import java.rmi.server.UnicastRemoteObject;  // Import for exporting remote objects
public class Client extends UnicastRemoteObject {
    // Constructor for the Client class
    public Client() throws RemoteException {
        super();  // Call the superclass constructor to export this object to the RMI runtime
    }
    public double[][] computeChunk(double[][] grid, int fromRow, int toRow) throws RemoteException {
        int height = grid[0].length;  // Get the height (number of columns) of the grid
        double[][] newGrid = new double[toRow - fromRow][height];  // Initialize a new grid for the computed chunk
        // Iterate over the specified chunk of rows
        for (int i = fromRow; i < toRow; i++) {
            for (int j = 1; j < height - 1; j++) {
                // Compute the average temperature for the current cell
                newGrid[i - fromRow][j] = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4.0;
            }
        }
        // Return the computed chunk of the grid
        return newGrid;
    }
    public static void main(String[] args) {
        try {
            // Create an instance of the Client class
            Client worker = new Client();
            // Bind the instance to the RMI registry with the name "Worker"
            Naming.rebind("//localhost/Worker", worker);
            System.out.println("Worker ready.");  // Indicate that the worker is ready to accept remote calls
        } catch (Exception e) {
            // Print the stack trace of any exceptions that occur
            e.printStackTrace();
        }
    }
}