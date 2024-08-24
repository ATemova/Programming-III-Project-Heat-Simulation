package Common;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
public class HeatDiffusion {
    // Threshold to determine stability based on the maximum allowable temperature change
    private static final double STABILITY_THRESHOLD = 0.25;
    public static boolean updateGridParallel(double[][] grid, int width, int height) {
        // Create a new grid to store the updated temperatures
        double[][] newGrid = new double[width][height];
        // AtomicBoolean to track if any temperature changes exceed the stability threshold
        AtomicBoolean isStable = new AtomicBoolean(true);
        // Use parallel streams to process each row of the grid in parallel
        IntStream.range(1, width - 1).parallel().forEach(i -> {
            for (int j = 1; j < height - 1; j++) {
                // Compute the new temperature for the current cell based on its neighboring cells
                double temp = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4.0;
                // Update the newGrid with the computed temperature
                newGrid[i][j] = temp;
                // Check if the temperature change exceeds the stability threshold
                if (Math.abs(temp - grid[i][j]) > STABILITY_THRESHOLD) {
                    // Mark as unstable if the change exceeds the threshold
                    isStable.set(false);
                }
            }
        });
        // Copy the updated values from newGrid back to the original grid (excluding boundaries)
        for (int i = 1; i < width - 1; i++) {
            System.arraycopy(newGrid[i], 1, grid[i], 1, height - 2);
        }
        // Return whether the grid has stabilized
        return isStable.get();
    }
}