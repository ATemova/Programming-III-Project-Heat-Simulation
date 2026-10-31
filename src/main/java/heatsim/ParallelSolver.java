package heatsim;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Shared-memory solver: each thread relaxes one band of rows per step. */
public final class ParallelSolver implements Solver {

    private final int threads;
    private final ExecutorService pool;

    public ParallelSolver(int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("threads must be >= 1");
        }
        this.threads = threads;
        this.pool = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r, "heatsim-worker");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public String name() {
        return "parallel (" + threads + " threads)";
    }

    @Override
    public double step(Grid grid) throws Exception {
        double[][] src = grid.current();
        double[][] dst = grid.next();
        boolean[][] fixed = grid.fixed();
        List<Callable<Double>> tasks = new ArrayList<>();
        for (int[] band : Kernel.bands(grid.height(), threads)) {
            tasks.add(() -> Kernel.relax(src, fixed, dst, band[0], band[1]));
        }
        double maxDelta = 0.0;
        for (Future<Double> f : pool.invokeAll(tasks)) {
            maxDelta = Math.max(maxDelta, f.get());
        }
        return maxDelta;
    }

    @Override
    public void close() {
        pool.shutdownNow();
    }
}
