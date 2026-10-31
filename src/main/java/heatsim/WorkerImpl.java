package heatsim;

import java.util.Arrays;

/** Worker implementation; stateless, so one instance can serve any band. */
public final class WorkerImpl implements Worker {

    @Override
    public BandResult relax(double[][] rows, boolean[][] fixed) {
        int n = rows.length;
        double[][] out = new double[n][];
        for (int i = 1; i < n - 1; i++) {
            out[i] = new double[rows[i].length];
        }
        double maxDelta = Kernel.relax(rows, fixed, out, 1, n - 1);
        return new BandResult(Arrays.copyOfRange(out, 1, n - 1), maxDelta);
    }
}
