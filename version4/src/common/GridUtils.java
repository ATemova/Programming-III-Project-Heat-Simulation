package common;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GridUtils {
    private static final double MAX_TEMP_CHANGE = 0.25;
    private static Random random = new Random(42);

    public static double[][] initializeGrid(int width, int height, int points) {
        double[][] grid = new double[width][height];
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
        return grid;
    }

    public static boolean updateGridSequential(double[][] grid, int width, int height) {
        double[][] newGrid = new double[width][height];
        boolean stable = true;

        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                newGrid[i][j] = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4.0;
                if (Math.abs(newGrid[i][j] - grid[i][j]) > MAX_TEMP_CHANGE) {
                    stable = false;
                }
            }
        }

        for (int i = 1; i < width - 1; i++) {
            System.arraycopy(newGrid[i], 1, grid[i], 1, height - 2);
        }

        return stable;
    }

    public static boolean updateGridParallel(double[][] grid, int width, int height, ExecutorService executor) {
        double[][] newGrid = new double[width][height];
        AtomicBoolean isStable = new AtomicBoolean(true);

        for (int chunk = 0; chunk < Runtime.getRuntime().availableProcessors(); chunk++) {
            final int from = chunk * width / Runtime.getRuntime().availableProcessors();
            final int to = (chunk == Runtime.getRuntime().availableProcessors() - 1) ? width : from + width / Runtime.getRuntime().availableProcessors();

            executor.submit(() -> {
                for (int i = from; i < to; i++) {
                    for (int j = 1; j < height - 1; j++) {
                        newGrid[i][j] = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4.0;
                        if (Math.abs(newGrid[i][j] - grid[i][j]) > MAX_TEMP_CHANGE) {
                            isStable.set(false);
                        }
                    }
                }
            });
        }

        try {
            executor.shutdown();
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        for (int i = 1; i < width - 1; i++) {
            System.arraycopy(newGrid[i], 1, grid[i], 1, height - 2);
        }

        return isStable.get();
    }
}
