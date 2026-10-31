package heatsim;

/** Iterates a solver until the largest change of a step drops below the threshold. */
public final class Simulation {

    public static final double DEFAULT_THRESHOLD = 0.25;

    public interface Listener {
        void onStep(int iteration, Grid grid, double maxDelta);
    }

    public record Result(int iterations, long millis, boolean converged, double lastDelta) {
    }

    private Simulation() {
    }

    public static Result run(Grid grid, Solver solver, double threshold, int maxIterations, Listener listener)
            throws Exception {
        long start = System.nanoTime();
        int iteration = 0;
        double delta = Double.POSITIVE_INFINITY;
        while (iteration < maxIterations) {
            delta = solver.step(grid);
            grid.swap();
            iteration++;
            if (listener != null) {
                listener.onStep(iteration, grid, delta);
            }
            if (delta < threshold) {
                break;
            }
        }
        long millis = (System.nanoTime() - start) / 1_000_000;
        return new Result(iteration, millis, delta < threshold, delta);
    }
}
