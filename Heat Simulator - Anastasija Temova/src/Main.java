import Distributed.Server;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import Common.GridUtils;
import Common.HeatDiffusion;
import Distributed.Client;

public class Main {
    // Maximum allowable temperature change to determine stability
    // private static final double MAX_TEMP_CHANGE = 0.25;  // This could be used to determine stability, but it is currently unused.

    public static void main(String[] args) {
        // Check if the correct number of arguments are provided
        if (args.length < 4) {
            System.out.println("Usage: java Main <mode> <width> <height> <points>");
            return;
        }

        // Parse the command-line arguments
        String mode = args[0];
        int width = 0;
        int height = 0;
        int points = 0;

        try {
            width = Integer.parseInt(args[1]);
            height = Integer.parseInt(args[2]);
            points = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.out.println("Error: Width, height, and points must be integers.");
            return;
        }

        // Execute the simulation based on the selected mode
        switch (mode.toLowerCase()) {
            case "sequential":
                runSequential(width, height, points);
                break;
            case "parallel":
                runParallel(width, height, points);
                break;
            case "distributed":
                runDistributed(width, height, points);
                break;
            default:
                System.out.println("Unknown mode: " + mode);
                break;
        }
    }

    private static void runSequential(int width, int height, int points) {
        // Initialize the grid with the specified dimensions and number of random heat sources
        double[][] grid = GridUtils.initializeGrid(width, height, points);
        boolean stable = false;

        long startTime = System.currentTimeMillis();  // Start timing the simulation

        // Iterate until the grid becomes stable
        while (!stable) {
            stable = GridUtils.updateGridSequential(grid, width, height);
        }

        long endTime = System.currentTimeMillis();  // End timing the simulation
        System.out.println("Sequential mode completed.");
        System.out.println("Simulation time: " + (endTime - startTime) + " milliseconds");
    }

    private static void runParallel(int width, int height, int points) {
        // Initialize the grid with the specified dimensions and number of random heat sources
        double[][] grid = GridUtils.initializeGrid(width, height, points);
        boolean stable = false;

        long startTime = System.currentTimeMillis();  // Start timing the simulation

        try {
            // Create a thread pool for parallel processing
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            // Iterate until the grid becomes stable
            while (!stable) {
                stable = HeatDiffusion.updateGridParallel(grid, width, height);
            }

            // Shutdown the executor and wait for all tasks to finish
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);  // Wait for up to 1 minute for termination
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();  // End timing the simulation
        System.out.println("Parallel mode completed.");
        System.out.println("Simulation time: " + (endTime - startTime) + " milliseconds");
    }

    private static void runDistributed(int width, int height, int points) {
        try {
            // Create and bind the server (master) for the distributed simulation
            Server server = new Server(width, height, points);
            java.rmi.Naming.rebind("//localhost/Master", server);
            System.out.println("Master ready.");

            // Create and bind the client (worker) for the distributed simulation
            Client client = new Client();
            java.rmi.Naming.rebind("//localhost/Worker", client);
            System.out.println("Worker ready.");

            long startTime = System.currentTimeMillis();  // Start timing the simulation

            // Start the distributed work by delegating tasks to workers
            server.distributeWork();

            long endTime = System.currentTimeMillis();  // End timing the simulation
            System.out.println("Distributed mode completed.");
            System.out.println("Simulation time: " + (endTime - startTime) + " milliseconds");
        } catch (Exception e) {
            System.err.println("Distributed mode error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}