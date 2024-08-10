package Distributed;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server extends UnicastRemoteObject {
    private static final double MAX_TEMP_CHANGE = 0.25; // Maximum allowable temperature change for stability
    private double[][] grid; // The temperature grid
    private final ExecutorService executor; // Executor service for parallel computations
    private Random random; // Random number generator for heat source placement

    public Server(int width, int height, int points) throws RemoteException {
        super();
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        random = new Random(42); // Seeded random number generator for reproducibility
        initializeGrid(width, height, points);
    }

    private void initializeGrid(int width, int height, int points) {
        grid = new double[width][height]; // Create the grid with the specified dimensions
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grid[i][j] = 20; // Set a base temperature for all cells
            }
        }
        for (int i = 0; i < points; i++) {
            int x = random.nextInt(width - 2) + 1; // Random x-coordinate for heat source
            int y = random.nextInt(height - 2) + 1; // Random y-coordinate for heat source
            grid[x][y] = 100; // Set the heat source temperature
        }
    }

    public void distributeWork() throws RemoteException {
        boolean stable; // Variable to check if the grid has stabilized
        long startTime = System.currentTimeMillis(); // Record start time for performance measurement

        do {
            stable = true; // Assume the grid is stable initially
            int chunkSize = grid.length / Runtime.getRuntime().availableProcessors(); // Determine chunk size for parallel processing
            AtomicBoolean isStable = new AtomicBoolean(true); // AtomicBoolean to track stability across threads

            // Create a temporary grid for updated temperatures
            double[][] tempGrid = new double[grid.length][grid[0].length];

            // Divide the grid into chunks and process each chunk in parallel
            for (int chunk = 0; chunk < Runtime.getRuntime().availableProcessors(); chunk++) {
                final int from = chunk * chunkSize;
                final int to = (chunk == Runtime.getRuntime().availableProcessors() - 1) ? grid.length : from + chunkSize;

                // Submit tasks to compute chunks of the grid
                executor.submit(() -> {
                    computeChunk(from, to, tempGrid, isStable);
                });
            }

            // Shutdown the executor and await termination
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow(); // Force shutdown if tasks take too long
                }
            } catch (InterruptedException e) {
                executor.shutdownNow(); // Handle interruption during termination
            }

            grid = tempGrid; // Update the grid with new values
        } while (!stable); // Continue until the grid is stable

        long endTime = System.currentTimeMillis(); // Record end time
        System.out.println("Distributed simulation completed in " + (endTime - startTime) + " ms");
    }

    private void computeChunk(int fromRow, int toRow, double[][] tempGrid, AtomicBoolean isStable) {
        // Iterate over the specified chunk of rows
        for (int i = fromRow; i < toRow; i++) {
            for (int j = 1; j < grid[0].length - 1; j++) {
                // Compute the average temperature for the current cell
                tempGrid[i][j] = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4.0;

                // Check if the temperature change exceeds the maximum allowable change
                if (Math.abs(tempGrid[i][j] - grid[i][j]) > MAX_TEMP_CHANGE) {
                    isStable.set(false); // Set the grid as unstable if the change is too large
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Check if the required arguments are provided
            if (args.length == 3) {
                int width = Integer.parseInt(args[0]);  // Parse width from arguments
                int height = Integer.parseInt(args[1]); // Parse height from arguments
                int points = Integer.parseInt(args[2]); // Parse number of heat points from arguments

                // Create and bind the Server object to the RMI registry
                Server server = new Server(width, height, points);
                Naming.rebind("//localhost/Server", server);
                System.out.println("Server ready.");
            } else {
                // Display usage instructions if arguments are missing
                System.out.println("Usage: java distributed.Server <width> <height> <points>");
            }
        } catch (Exception e) {
            // Handle and print any exceptions that occur
            e.printStackTrace();
        }
    }
}