package parallel;
import common.GridUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelHeatDiffusion {
    public static void run(int width, int height, int points) {
        double[][] grid = GridUtils.initializeGrid(width, height, points);
        boolean stable;
        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        do {
            stable = GridUtils.updateGridParallel(grid, width, height, executor);
        } while (!stable);

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Parallel simulation completed in " + (endTime - startTime) + " ms");
    }
}