package heatsim;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/** Remote worker that relaxes one band of rows per call. */
public interface Worker extends Remote {

    String REGISTRY_NAME = "heatsim-worker";

    /**
     * @param rows  the band plus one halo row above and below
     * @param fixed fixed-cell mask aligned with {@code rows}
     * @return the relaxed band without halo rows and its largest change
     */
    BandResult relax(double[][] rows, boolean[][] fixed) throws RemoteException;

    record BandResult(double[][] rows, double maxDelta) implements Serializable {
    }
}
