package distributed;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    void distributeWork() throws RemoteException;
}
