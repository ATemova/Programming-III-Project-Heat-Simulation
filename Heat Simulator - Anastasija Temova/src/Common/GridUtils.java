package Common;
import java.util.Random;

public class GridUtils {
    // Default temperature values
    private static final double BASE_TEMP = 20.0;   // Base temperature of the grid
    private static final double BOUNDARY_TEMP = 0.0; // Boundary temperature of the grid edges

    public static double[][] initializeGrid(int width, int height, int points) {
        double[][] grid = new double[width][height];

        // Initialize the grid with base temperature
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = BASE_TEMP;
            }
        }

        // Set boundary conditions to 0°C
        setBoundaryConditions(grid, width, height);

        // Place random heat sources
        placeHeatSources(grid, width, height, points);

        return grid;
    }

    private static void setBoundaryConditions(double[][] grid, int width, int height) {
        // Set top and bottom rows to boundary temperature
        for (int i = 0; i < width; i++) {
            grid[i][0] = BOUNDARY_TEMP;
            grid[i][height - 1] = BOUNDARY_TEMP;
        }
        // Set left and right columns to boundary temperature
        for (int j = 0; j < height; j++) {
            grid[0][j] = BOUNDARY_TEMP;
            grid[width - 1][j] = BOUNDARY_TEMP;
        }
    }

    private static void placeHeatSources(double[][] grid, int width, int height, int points) {
        Random random = new Random(42); // Seeded random number generator for reproducibility
        for (int i = 0; i < points; i++) {
            // Generate random coordinates for heat sources, avoiding the boundaries
            int x = random.nextInt(width - 2) + 1;
            int y = random.nextInt(height - 2) + 1;
            grid[x][y] = 100.0; // Set heat source temperature
        }
    }

    public static boolean updateGridSequential(double[][] grid, int width, int height) {
        double[][] newGrid = new double[width][height];
        boolean stable = true;

        // Perform the update
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                // Compute the average temperature for the current cell based on its neighbors
                newGrid[i][j] = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4.0;
                // Check if the temperature change exceeds the maximum allowable change
                if (Math.abs(newGrid[i][j] - grid[i][j]) > 0.25) {
                    stable = false; // Mark as unstable if temperature change exceeds threshold
                }
            }
        }

        // Copy newGrid back to grid (excluding boundaries)
        for (int i = 1; i < width - 1; i++) {
            System.arraycopy(newGrid[i], 1, grid[i], 1, height - 2);
        }

        return stable;
    }
}