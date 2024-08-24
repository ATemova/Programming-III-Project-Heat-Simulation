// Import necessary classes for distributed computing and multithreading
import Distributed.Server;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import Common.GridUtils;
import Common.HeatDiffusion;
import Distributed.Client;
public class Main {
    // The entry point of the application
    public static void main(String[] args) {
        // Ensure that the correct number of command-line arguments are provided
        if (args.length < 4) {
            System.out.println("Usage: java Main <mode> <width> <height> <points>");
            return;
        }
        // Extract and parse command-line arguments
        String mode = args[0];  // Mode of operation: sequential, parallel, or distributed
        int width = 0;          // Width of the grid
        int height = 0;         // Height of the grid
        int points = 0;         // Number of random heat sources
        // Try to parse the width, height, and points arguments as integers
        try {
            width = Integer.parseInt(args[1]);
            height = Integer.parseInt(args[2]);
            points = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            // If the arguments are not valid integers, print an error message and exit
            System.out.println("Error: Width, height, and points must be integers.");
            return;
        }
        // Select and execute the appropriate simulation mode based on the user input
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
                // If an unknown mode is provided, notify the user
                System.out.println("Unknown mode: " + mode);
                break;
        }
    }
    // Method to run the simulation in sequential mode
    private static void runSequential(int width, int height, int points) {
        // Initialize the grid with the given dimensions and random heat sources
        double[][] grid = GridUtils.initializeGrid(width, height, points);
        boolean stable = false;  // Flag to check if the grid has reached stability
        long startTime = System.currentTimeMillis();  // Start the timer for the simulation
        // Continuously update the grid until it stabilizes
        while (!stable) {
            stable = GridUtils.updateGridSequential(grid, width, height);
        }
        long endTime = System.currentTimeMillis();  // End the timer for the simulation
        System.out.println("Sequential mode completed.");
        System.out.println("Simulation time: " + (endTime - startTime) + " milliseconds");
    }
    // Method to run the simulation in parallel mode
    private static void runParallel(int width, int height, int points) {
        // Initialize the grid with the given dimensions and random heat sources
        double[][] grid = GridUtils.initializeGrid(width, height, points);
        boolean stable = false;  // Flag to check if the grid has reached stability
        long startTime = System.currentTimeMillis();  // Start the timer for the simulation
        try {
            // Create a thread pool to handle parallel processing
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            // Continuously update the grid until it stabilizes
            while (!stable) {
                stable = HeatDiffusion.updateGridParallel(grid, width, height);
            }
            // Shut down the executor service and wait for all tasks to complete
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);  // Wait for a maximum of 1 minute
        } catch (InterruptedException e) {
            // Handle any interruption during the execution
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();  // End the timer for the simulation
        System.out.println("Parallel mode completed.");
        System.out.println("Simulation time: " + (endTime - startTime) + " milliseconds");
    }
    // Method to run the simulation in distributed mode
    private static void runDistributed(int width, int height, int points) {
        try {
            // Initialize and bind the server (master) for the distributed simulation
            Server server = new Server(width, height, points);
            java.rmi.Naming.rebind("//localhost/Master", server);
            System.out.println("Master ready.");
            // Initialize and bind the client (worker) for the distributed simulation
            Client client = new Client();
            java.rmi.Naming.rebind("//localhost/Worker", client);
            System.out.println("Worker ready.");
            long startTime = System.currentTimeMillis();  // Start the timer for the simulation
            // Start the distributed computation by delegating tasks to worker nodes
            server.distributeWork();
            long endTime = System.currentTimeMillis();  // End the timer for the simulation
            System.out.println("Distributed mode completed.");
            System.out.println("Simulation time: " + (endTime - startTime) + " milliseconds");
        } catch (Exception e) {
            // Handle any exceptions that occur during the distributed simulation
            System.err.println("Distributed mode error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}