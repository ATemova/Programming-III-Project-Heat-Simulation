package distributed;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server extends UnicastRemoteObject implements ServerInterface {
    private static final double MAX_TEMP_CHANGE = 0.25;
    private double[][] grid;
    private double[][] newGrid;
    private final ExecutorService executor;
    private Random random;

    public Server(int width, int height, int points) throws RemoteException {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        random = new Random(42); // Seeded random number generator
        initializeGrid(width, height, points);
    }

    private void initializeGrid(int width, int height, int points) {
        grid = new double[width][height];
        newGrid = new double[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = 20;
            }
        }
        for (int i = 0; i < points; i++) {
            int x = random.nextInt(width - 2) + 1;
            int y = random.nextInt(height - 2) + 1;
            grid[x][y] = 100;
        }
    }

    @Override
    public void distributeWork() throws RemoteException {
        boolean stable;
        long startTime = System.currentTimeMillis();
        do {
            stable = true;
            int chunkSize = grid.length / Runtime.getRuntime().availableProcessors();
            AtomicBoolean isStable = new AtomicBoolean(true);

            for (int chunk = 0; chunk < Runtime.getRuntime().availableProcessors(); chunk++) {
                final int from = chunk * chunkSize;
                final int to = (chunk == Runtime.getRuntime().availableProcessors() - 1) ? grid.length : from + chunkSize;

                executor.submit(() -> {
                    try {
                        ClientInterface client = (ClientInterface) Naming.lookup("//localhost/Worker");
                        double[][] result = client.computeChunk(grid, from, to);
                        synchronized (newGrid) {
                            for (int i = from; i < to; i++) {
                                for (int j = 1; j < grid[0].length - 1; j++) {
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
        System.out.println("Distributed simulation completed in " + (endTime - startTime) + " ms");
    }
}
