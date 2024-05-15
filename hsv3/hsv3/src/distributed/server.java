package distributed;
import distributed.ServerInterface;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class server extends UnicastRemoteObject implements ServerInterface {
    private static int WIDTH = 800;
    private static int HEIGHT = 600;
    private static int N = 10;
    private static final double MAX_TEMP_CHANGE = 0.25;
    private double[][] grid;
    private double[][] newGrid;
    private final ExecutorService executor;
    private Random random;

    protected server() throws RemoteException {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        random = new Random(42); // Seeded random number generator
        initializeGrid();
    }

    private void initializeGrid() {
        grid = new double[WIDTH][HEIGHT];
        newGrid = new double[WIDTH][HEIGHT];

        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                grid[i][j] = 20;
            }
        }
        for (int i = 0; i < N; i++) {
            int x = random.nextInt(WIDTH - 2) + 1;
            int y = random.nextInt(HEIGHT - 2) + 1;
            grid[x][y] = 100;
        }
    }

    @Override
    public void distributeWork() throws RemoteException {
        boolean stable;
        long startTime = System.currentTimeMillis();
        do {
            stable = true;
            int chunkSize = WIDTH / Runtime.getRuntime().availableProcessors();
            AtomicBoolean isStable = new AtomicBoolean(true);

            for (int chunk = 0; chunk < Runtime.getRuntime().availableProcessors(); chunk++) {
                final int from = chunk * chunkSize;
                final int to = (chunk == Runtime.getRuntime().availableProcessors() - 1) ? WIDTH : from + chunkSize;

                executor.submit(() -> {
                    try {
                        ClientInterface client = (ClientInterface) Naming.lookup("//localhost/Worker");
                        double[][] result = client.computeChunk(grid, from, to);
                        synchronized (newGrid) {
                            for (int i = from; i < to; i++) {
                                for (int j = 1; j < HEIGHT - 1; j++) {
                                    if (Math.abs(result[i - from][j] - grid[i][j]) > MAX_TEMP_CHANGE) {
                                        isStable.set(false);
                                    }
                                    newGrid[i][j] = result[i - from][j];
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }

            stable = isStable.get();
            double[][] temp = grid;
            grid = newGrid;
            newGrid = temp;
        } while (!stable);
        long endTime = System.currentTimeMillis();
        System.out.println("Simulation completed in " + (endTime - startTime) + " ms");
    }

    public static void main(String[] args) {
        if (args.length == 3) {
            WIDTH = Integer.parseInt(args[0]);
            HEIGHT = Integer.parseInt(args[1]);
            N = Integer.parseInt(args[2]);
        }
        try {
            server master = new server();
            Naming.rebind("//localhost/Master", master);
            System.out.println("Master ready.");
            master.distributeWork();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
