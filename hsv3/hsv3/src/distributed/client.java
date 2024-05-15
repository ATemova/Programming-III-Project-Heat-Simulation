package distributed;
import distributed.ClientInterface;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class client extends UnicastRemoteObject implements ClientInterface {
    protected client() throws RemoteException {
        super();
    }

    @Override
    public double[][] computeChunk(double[][] grid, int fromRow, int toRow) throws RemoteException {
        int height = grid[0].length;
        double[][] newGrid = new double[toRow - fromRow][height];

        for (int i = fromRow; i < toRow; i++) {
            for (int j = 1; j < height - 1; j++) {
                newGrid[i - fromRow][j] = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4.0;
            }
        }

        return newGrid;
    }

    public static void main(String[] args) {
        try {
            client worker = new client();
            Naming.rebind("//localhost/Worker", worker);
            System.out.println("Worker ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
