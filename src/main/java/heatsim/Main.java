package heatsim;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

public final class Main {

    private static final String USAGE = """
            Usage: java -jar heat-sim.jar <mode> [options]

            Modes:
              sequential            single-threaded run
              parallel              multi-threaded run
              distributed           run over Java RMI workers
              worker                start a standalone RMI worker
              benchmark             time all three modes and check they agree

            Options:
              --width N             grid width               (default 200)
              --height N            grid height              (default 200)
              --points N            number of heat sources   (default 20)
              --seed N              random seed              (default 42)
              --threshold T         stop when max change < T (default 0.25)
              --max-iter N          iteration limit          (default 1000000)
              --threads N           parallel threads         (default: CPU cores)
              --workers N           local RMI workers        (default: CPU cores)
              --hosts h:p,h:p       remote RMI workers instead of local ones
              --port N              worker registry port     (default 1099)
              --gui                 show the plate while it is simulated
              --sizes a,b,c         benchmark grid sizes     (default 100,200,400)
              --runs N              benchmark repetitions    (default 3)
              --csv FILE            write benchmark results as CSV
            """;

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            System.out.print(USAGE);
            return;
        }
        try {
            Options o = Options.parse(Arrays.copyOfRange(args, 1, args.length));
            switch (args[0].toLowerCase()) {
                case "sequential", "parallel", "distributed" -> runOnce(args[0].toLowerCase(), o);
                case "worker" -> DistributedSolver.serveWorker(o.integer("port", 1099));
                case "benchmark" -> Benchmark.run(o);
                default -> throw new IllegalArgumentException("Unknown mode: " + args[0]);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            System.err.print(USAGE);
            System.exit(2);
        } catch (Exception e) {
            System.err.println("Error: " + e);
            System.exit(1);
        }
    }

    static Solver createSolver(String mode, Options o) throws Exception {
        int cores = Runtime.getRuntime().availableProcessors();
        return switch (mode) {
            case "sequential" -> new SequentialSolver();
            case "parallel" -> new ParallelSolver(o.integer("threads", cores));
            case "distributed" -> o.has("hosts")
                    ? DistributedSolver.connect(Arrays.asList(o.string("hosts").split(",")))
                    : DistributedSolver.local(o.integer("workers", cores));
            default -> throw new IllegalArgumentException("Unknown mode: " + mode);
        };
    }

    private static void runOnce(String mode, Options o) throws Exception {
        int width = o.integer("width", 200);
        int height = o.integer("height", 200);
        int points = o.integer("points", 20);
        Grid grid = new Grid(width, height, points, o.integer("seed", 42));

        HeatView view = null;
        if (o.flag("gui")) {
            HeatView[] holder = new HeatView[1];
            SwingUtilities.invokeAndWait(() -> holder[0] = new HeatView(width, height,
                    "Heat simulation - " + mode + " " + width + "x" + height));
            view = holder[0];
        }

        try (Solver solver = createSolver(mode, o)) {
            System.out.printf("Mode: %s%nGrid: %dx%d, %d heat sources%n", solver.name(), width, height, points);
            Simulation.Result r = Simulation.run(grid, solver,
                    o.decimal("threshold", Simulation.DEFAULT_THRESHOLD),
                    o.integer("max-iter", 1_000_000), view);
            System.out.printf("%s after %d iterations in %d ms (last max change %.4f)%n",
                    r.converged() ? "Stable" : "Stopped at iteration limit", r.iterations(), r.millis(), r.lastDelta());
            if (view != null) {
                view.finish(grid, r);
            }
        }
        if (view == null) {
            System.exit(0);
        }
    }

    static final class Options {
        private final Map<String, String> values = new HashMap<>();

        static Options parse(String[] args) {
            Options o = new Options();
            for (int i = 0; i < args.length; i++) {
                if (!args[i].startsWith("--")) {
                    throw new IllegalArgumentException("Unexpected argument: " + args[i]);
                }
                String key = args[i].substring(2);
                if (key.equals("gui")) {
                    o.values.put(key, "true");
                } else if (i + 1 < args.length) {
                    o.values.put(key, args[++i]);
                } else {
                    throw new IllegalArgumentException("Missing value for --" + key);
                }
            }
            return o;
        }

        boolean has(String key) {
            return values.containsKey(key);
        }

        boolean flag(String key) {
            return values.containsKey(key);
        }

        String string(String key) {
            return values.get(key);
        }

        int integer(String key, int fallback) {
            String v = values.get(key);
            try {
                int n = v == null ? fallback : Integer.parseInt(v);
                if (n < 0) {
                    throw new NumberFormatException();
                }
                return n;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("--" + key + " must be a non-negative integer");
            }
        }

        double decimal(String key, double fallback) {
            String v = values.get(key);
            try {
                return v == null ? fallback : Double.parseDouble(v);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("--" + key + " must be a number");
            }
        }

        List<Integer> integers(String key, List<Integer> fallback) {
            if (!has(key)) {
                return fallback;
            }
            List<Integer> out = new ArrayList<>();
            for (String s : values.get(key).split(",")) {
                try {
                    out.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("--" + key + " must be a comma-separated list of integers");
                }
            }
            return out;
        }

        Path path(String key) {
            return has(key) ? Path.of(values.get(key)) : null;
        }
    }

    static PrintWriter csvWriter(Path path) throws Exception {
        return new PrintWriter(Files.newBufferedWriter(path));
    }
}
