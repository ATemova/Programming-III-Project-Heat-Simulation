package Parallel;
import Common.GridUtils;
import Common.HeatDiffusion;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
public class ParallelHeatDiffusion {
    // Main method to run the heat diffusion simulation in parallel
    public static void run(int width, int height, int points) {
        // Initialize the grid with random heat points
        // This method sets up a grid of the given dimensions with the specified number of initial heat sources
        double[][] grid = GridUtils.initializeGrid(width, height, points);
        boolean stable;  // A flag to determine if the grid has reached a stable state
        long startTime = System.currentTimeMillis(); // Record the start time for performance measurement
        // Create an executor service with a thread pool
        // The number of threads is set to the number of available processors to utilize the full potential of the CPU
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        // Run the simulation in a loop until the grid reaches a stable state
        do {
            // Update the grid in parallel
            // This method handles the parallel processing of grid updates, allowing the grid to be updated concurrently across multiple threads
            stable = HeatDiffusion.updateGridParallel(grid, width, height);
        } while (!stable);  // Continue looping until the grid stabilizes
        // Shut down the executor service and wait for termination
        executor.shutdown();  // Initiate an orderly shutdown of the executor service
        try {
            // Await termination of all tasks; wait for up to 1 minute
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();  // Force shutdown if tasks are still running after the timeout
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();  // Force shutdown if the current thread is interrupted
        }
        long endTime = System.currentTimeMillis();  // Record the end time for performance measurement
        // Output the total time taken for the simulation to complete
        System.out.println("Parallel simulation completed in " + (endTime - startTime) + " ms");
    }
}