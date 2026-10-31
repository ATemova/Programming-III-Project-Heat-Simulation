package heatsim;

import java.rmi.NoSuchObjectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Message-passing solver over Java RMI. Each step the master splits the interior rows
 * into one band per worker, sends every band with its halo rows, and gathers the results.
 */
public final class DistributedSolver implements Solver {

    private final List<Worker> workers;
    private final String description;
    private final ExecutorService pool;
    private final List<WorkerImpl> localWorkers = new ArrayList<>();
    private Registry localRegistry;

    private DistributedSolver(List<Worker> workers, String description) {
        if (workers.isEmpty()) {
            throw new IllegalArgumentException("At least one worker is required");
        }
        this.workers = workers;
        this.description = description;
        this.pool = Executors.newFixedThreadPool(workers.size(), r -> {
            Thread t = new Thread(r, "heatsim-rmi-call");
            t.setDaemon(true);
            return t;
        });
    }

    /** Starts {@code count} workers in this JVM behind a private RMI registry and talks to them over RMI. */
    public static DistributedSolver local(int count) throws Exception {
        Registry registry = LocateRegistry.createRegistry(0);
        List<WorkerImpl> impls = new ArrayList<>();
        List<Worker> stubs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            WorkerImpl impl = new WorkerImpl();
            Worker exported = (Worker) UnicastRemoteObject.exportObject(impl, 0);
            String name = Worker.REGISTRY_NAME + "-" + i;
            registry.rebind(name, exported);
            impls.add(impl);
            stubs.add((Worker) registry.lookup(name));
        }
        DistributedSolver solver = new DistributedSolver(stubs, "distributed (" + count + " local RMI workers)");
        solver.localWorkers.addAll(impls);
        solver.localRegistry = registry;
        return solver;
    }

    /** Connects to workers started with {@code heat-sim worker --port P}, given as {@code host:port}. */
    public static DistributedSolver connect(List<String> endpoints) throws Exception {
        List<Worker> stubs = new ArrayList<>();
        for (String endpoint : endpoints) {
            String[] parts = endpoint.trim().split(":");
            String host = parts[0];
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : Registry.REGISTRY_PORT;
            stubs.add((Worker) LocateRegistry.getRegistry(host, port).lookup(Worker.REGISTRY_NAME));
        }
        return new DistributedSolver(stubs, "distributed (" + stubs.size() + " remote RMI workers)");
    }

    @Override
    public String name() {
        return description;
    }

    @Override
    public double step(Grid grid) throws Exception {
        double[][] src = grid.current();
        double[][] dst = grid.next();
        boolean[][] fixed = grid.fixed();
        int[][] bands = Kernel.bands(grid.height(), workers.size());

        List<Callable<Double>> calls = new ArrayList<>();
        for (int i = 0; i < bands.length; i++) {
            Worker worker = workers.get(i);
            int from = bands[i][0];
            int to = bands[i][1];
            calls.add(() -> {
                Worker.BandResult result = worker.relax(
                        Arrays.copyOfRange(src, from - 1, to + 1),
                        Arrays.copyOfRange(fixed, from - 1, to + 1));
                for (int y = from; y < to; y++) {
                    System.arraycopy(result.rows()[y - from], 0, dst[y], 0, dst[y].length);
                }
                return result.maxDelta();
            });
        }

        double maxDelta = 0.0;
        for (Future<Double> f : pool.invokeAll(calls)) {
            maxDelta = Math.max(maxDelta, f.get());
        }
        return maxDelta;
    }

    @Override
    public void close() {
        pool.shutdownNow();
        for (WorkerImpl impl : localWorkers) {
            unexport(impl);
        }
        if (localRegistry != null) {
            unexport(localRegistry);
        }
    }

    private static void unexport(java.rmi.Remote obj) {
        try {
            UnicastRemoteObject.unexportObject(obj, true);
        } catch (NoSuchObjectException ignored) {
            // already gone
        }
    }

    /** Runs a standalone worker in this JVM until it is killed. */
    public static void serveWorker(int port) throws Exception {
        Registry registry = LocateRegistry.createRegistry(port);
        WorkerImpl impl = new WorkerImpl();
        registry.rebind(Worker.REGISTRY_NAME, UnicastRemoteObject.exportObject(impl, 0));
        System.out.println("Worker ready on port " + port + " (Ctrl+C to stop)");
        synchronized (DistributedSolver.class) {
            DistributedSolver.class.wait();
        }
    }
}
