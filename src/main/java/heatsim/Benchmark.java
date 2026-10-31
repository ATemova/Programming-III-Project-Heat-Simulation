package heatsim;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

/** Times every mode on several grid sizes and verifies they produce the same plate. */
final class Benchmark {

    private static final String[] MODES = {"sequential", "parallel", "distributed"};

    private Benchmark() {
    }

    static void run(Main.Options o) throws Exception {
        List<Integer> sizes = o.integers("sizes", List.of(100, 200, 400));
        int points = o.integer("points", 20);
        int seed = o.integer("seed", 42);
        int runs = Math.max(1, o.integer("runs", 3));
        double threshold = o.decimal("threshold", Simulation.DEFAULT_THRESHOLD);
        int maxIter = o.integer("max-iter", 1_000_000);
        Path csvPath = o.path("csv");

        System.out.printf("Heat sources: %d, seed: %d, best of %d runs, %d CPU cores%n%n",
                points, seed, runs, Runtime.getRuntime().availableProcessors());
        System.out.printf("%-10s %-40s %10s %10s %9s %s%n", "size", "mode", "iterations", "best ms", "speedup", "matches");

        PrintWriter csv = csvPath == null ? null : Main.csvWriter(csvPath);
        if (csv != null) {
            csv.println("size,mode,iterations,best_ms,speedup,matches_sequential");
        }

        boolean allMatch = true;
        try (Solver seq = Main.createSolver("sequential", o);
             Solver par = Main.createSolver("parallel", o);
             Solver dist = Main.createSolver("distributed", o)) {
            Solver[] solvers = {seq, par, dist};
            for (int size : sizes) {
                double[][] reference = null;
                long sequentialMs = 0;
                for (int m = 0; m < MODES.length; m++) {
                    long best = Long.MAX_VALUE;
                    Simulation.Result last = null;
                    double[][] plate = null;
                    for (int r = 0; r < runs; r++) {
                        Grid grid = new Grid(size, size, points, seed);
                        last = Simulation.run(grid, solvers[m], threshold, maxIter, null);
                        best = Math.min(best, last.millis());
                        plate = grid.current();
                    }
                    if (m == 0) {
                        reference = plate;
                        sequentialMs = best;
                    }
                    boolean matches = java.util.Arrays.deepEquals(reference, plate);
                    allMatch &= matches;
                    double speedup = best == 0 ? 0 : (double) sequentialMs / best;
                    String label = size + "x" + size;
                    System.out.printf("%-10s %-40s %10d %10d %8.2fx %s%n",
                            label, solvers[m].name(), last.iterations(), best, speedup, matches ? "yes" : "NO");
                    if (csv != null) {
                        csv.printf("%s,%s,%d,%d,%.3f,%b%n", label, MODES[m], last.iterations(), best, speedup, matches);
                    }
                }
            }
        } finally {
            if (csv != null) {
                csv.close();
                System.out.println("\nCSV written to " + csvPath);
            }
        }
        System.exit(allMatch ? 0 : 1);
    }
}
