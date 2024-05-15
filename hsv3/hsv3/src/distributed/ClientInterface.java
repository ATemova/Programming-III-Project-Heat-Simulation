package distributed;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    double[][] computeChunk(double[][] grid, int fromRow, int toRow) throws RemoteException;
}
