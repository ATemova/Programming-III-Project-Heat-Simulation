package Parallel;
import Common.GridUtils;
import Common.HeatDiffusion;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelHeatDiffusion {

    public static void run(int width, int height, int points) {
        // Initialize the grid with random heat points
        // This method sets up a grid of the given dimensions with the specified number of initial heat sources
        double[][] grid = GridUtils.initializeGrid(width, height, points);

        boolean stable;  // A flag to determine if the grid has reached a stable state
        long startTime = System.currentTimeMillis(); // Record start time for performance measurement

        // Create an executor service with a thread pool
        // The number of threads is based on the number of available processors
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // Run the simulation in a loop until the grid reaches a stable state
        do {
            // Update the grid in parallel
            // This method handles the parallel processing of grid updates
            stable = HeatDiffusion.updateGridParallel(grid, width, height);
        } while (!stable);

        // Shut down the executor service and wait for termination
        executor.shutdown();
        try {
            // Await termination of all tasks; wait for up to 1 minute
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow(); // Force shutdown if tasks are still running after the timeout
            }
        } catch (InterruptedException e) {
            executor.shutdownNow(); // Force shutdown if the current thread is interrupted
        }

        long endTime = System.currentTimeMillis(); // Record end time for performance measurement
        // Output the total time taken for the simulation to complete
        System.out.println("Parallel simulation completed in " + (endTime - startTime) + " ms");
    }
}