package Common;
import java.util.Random;
public class GridUtils {
    // Default temperature values
    private static final double BASE_TEMP = 20.0;   // Base temperature of the grid
    private static final double BOUNDARY_TEMP = 0.0; // Temperature at the grid edges
    public static double[][] initializeGrid(int width, int height, int points) {
        double[][] grid = new double[width][height]; // Create a 2D array for the grid
        // Initialize the grid with the base temperature
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = BASE_TEMP; // Set each cell to the base temperature
            }
        }
        // Set boundary conditions to boundary temperature
        setBoundaryConditions(grid, width, height);
        // Place heat sources randomly in the grid
        placeHeatSources(grid, width, height, points);
        return grid; // Return the initialized grid
    }
    private static void setBoundaryConditions(double[][] grid, int width, int height) {
        // Set the top and bottom rows to the boundary temperature
        for (int i = 0; i < width; i++) {
            grid[i][0] = BOUNDARY_TEMP; // Top boundary
            grid[i][height - 1] = BOUNDARY_TEMP; // Bottom boundary
        }
        // Set the left and right columns to the boundary temperature
        for (int j = 0; j < height; j++) {
            grid[0][j] = BOUNDARY_TEMP; // Left boundary
            grid[width - 1][j] = BOUNDARY_TEMP; // Right boundary
        }
    }
    private static void placeHeatSources(double[][] grid, int width, int height, int points) {
        Random random = new Random(42); // Seeded random number generator for reproducibility
        for (int i = 0; i < points; i++) {
            // Generate random coordinates for heat sources within the grid, avoiding the edges
            int x = random.nextInt(width - 2) + 1;
            int y = random.nextInt(height - 2) + 1;
            grid[x][y] = 100.0; // Set the temperature of the heat source
        }
    }
    public static boolean updateGridSequential(double[][] grid, int width, int height) {
        double[][] newGrid = new double[width][height]; // Create a temporary grid for updated values
        boolean stable = true; // Flag to check if the grid has stabilized
        // Perform the temperature update for each cell (excluding boundaries)
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                // Compute the average temperature of the current cell based on its neighbors
                newGrid[i][j] = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4.0;
                // Check if the temperature change exceeds the maximum allowable change
                if (Math.abs(newGrid[i][j] - grid[i][j]) > 0.25) {
                    stable = false; // Mark as unstable if the change exceeds the threshold
                }
            }
        }
        // Copy the updated values from newGrid back to the original grid (excluding boundaries)
        for (int i = 1; i < width - 1; i++) {
            System.arraycopy(newGrid[i], 1, grid[i], 1, height - 2);
        }
        return stable; // Return whether the grid has stabilized
    }
}