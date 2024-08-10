package Common;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class HeatDiffusion {

    private static final double STABILITY_THRESHOLD = 0.25; // Maximum allowable temperature change for stability

    public static boolean updateGridParallel(double[][] grid, int width, int height) {
        // Create a new grid to store the updated temperatures
        double[][] newGrid = new double[width][height];
        // AtomicBoolean to track if the grid is stable
        AtomicBoolean isStable = new AtomicBoolean(true);

        // Use parallel streams to process each row of the grid in parallel
        IntStream.range(1, width - 1).parallel().forEach(i -> {
            for (int j = 1; j < height - 1; j++) {
                // Calculate the new temperature for the current cell based on its neighbors
                double temp = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4.0;
                // Update the newGrid with the computed temperature
                newGrid[i][j] = temp;

                // Check if the change in temperature exceeds the stability threshold
                if (Math.abs(temp - grid[i][j]) > STABILITY_THRESHOLD) {
                    isStable.set(false); // Mark as unstable if the change exceeds the threshold
                }
            }
        });

        // Copy newGrid back to grid, excluding boundaries
        for (int i = 1; i < width - 1; i++) {
            System.arraycopy(newGrid[i], 1, grid[i], 1, height - 2);
        }

        return isStable.get(); // Return whether the grid is stable
    }
}