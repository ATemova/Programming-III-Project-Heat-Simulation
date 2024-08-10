package Sequential;
import Common.GridUtils;

public class SequentialHeatDiffusion {
    public static void run(int width, int height, int points) {
        // Initialize the grid with specified dimensions and heat points
        // The grid is a 2D array where each cell represents the temperature at that point
        double[][] grid = GridUtils.initializeGrid(width, height, points);
        boolean stable; // Flag to check if the grid has reached a stable state

        // Record the start time of the simulation
        long startTime = System.currentTimeMillis();

        // Update the grid until it reaches a stable state
        do {
            // Update the grid in a sequential manner and check if it has stabilized
            stable = GridUtils.updateGridSequential(grid, width, height);
        } while (!stable);

        // Record the end time of the simulation
        long endTime = System.currentTimeMillis();

        // Output the total time taken for the simulation
        System.out.println("Sequential simulation completed in " + (endTime - startTime) + " ms");
    }
}