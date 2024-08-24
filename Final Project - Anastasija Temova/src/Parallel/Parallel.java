package Parallel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JFrame;
import javax.swing.JPanel;
public class Parallel extends JPanel {
    private int width; // Width of the grid
    private int height; // Height of the grid
    private int numberOfHeatPoints; // Number of initial heat points
    private static final double BOUNDARY_TEMP = 0.0; // Boundary temperature for the grid
    private static final double TEMP_THRESHOLD = 0.25; // Threshold to determine if the grid is in equilibrium
    private static final int SCALE_WIDTH = 20; // Width of the color scale for the GUI
    private double[][] surface; // 2D array representing the temperature surface
    private volatile boolean running = true; // Flag to control the simulation loop
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors(); // Number of threads based on available processors
    private ExecutorService executorService; // Executor service for managing parallel tasks
    private boolean showGUI; // Flag to indicate if the GUI should be displayed
    // Constructor to initialize the grid with given dimensions and heat points
    public Parallel(int width, int height, int numberOfHeatPoints) {
        this.width = width;
        this.height = height;
        this.numberOfHeatPoints = numberOfHeatPoints;
        surface = new double[width][height]; // Initialize the surface with the given dimensions
        initializeSurface(); // Initialize the grid with boundary temperature and random heat points
        executorService = Executors.newFixedThreadPool(NUM_THREADS); // Set up the thread pool for parallel execution
    }
    // Method to initialize the grid with boundary temperature and random heat points
    private void initializeSurface() {
        // Set all grid points to the boundary temperature
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                surface[i][j] = BOUNDARY_TEMP;
            }
        }
        // Randomly distribute initial heat points on the grid
        Random rand = new Random(42); // Random number generator with a fixed seed for reproducibility
        for (int i = 0; i < numberOfHeatPoints; i++) {
            int heatSourceX = rand.nextInt(width); // Random x-coordinate for the heat source
            int heatSourceY = rand.nextInt(height); // Random y-coordinate for the heat source
            applyHeat(heatSourceX, heatSourceY, 100.0, 5); // Apply heat with a fixed temperature and radius
        }
    }
    // Method to apply heat at a specific location with a given radius
    private void applyHeat(int centerX, int centerY, double temperature, int radius) {
        // Apply heat within a given radius around the center point
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int x = centerX + i; // Calculate x-coordinate for the current point
                int y = centerY + j; // Calculate y-coordinate for the current point
                if (x >= 0 && x < width && y >= 0 && y < height) { // Check if the point is within the grid bounds
                    double distance = Math.sqrt(i * i + j * j); // Calculate the distance from the heat source
                    double influence = Math.max(0, (radius - distance) / radius); // Calculate the heat influence based on the distance
                    surface[x][y] = Math.max(surface[x][y], temperature * influence); // Apply heat to the grid point
                }
            }
        }
    }
    // Method to update the surface based on heat diffusion and check for equilibrium
    private boolean updateSurface() {
        double[][] newSurface = new double[width][height]; // New grid to store updated values
        boolean[] inEquilibriumArray = { true }; // Shared variable to track equilibrium status
        Future<?>[] futures = new Future[NUM_THREADS]; // Array to hold futures for each thread's task
        int rowsPerThread = (height - 2) / NUM_THREADS; // Number of rows each thread will process
        // Distribute work across multiple threads
        for (int thread = 0; thread < NUM_THREADS; thread++) {
            final int startRow = 1 + thread * rowsPerThread; // Calculate the start row for the current thread
            final int endRow = (thread == NUM_THREADS - 1) ? height - 2 : startRow + rowsPerThread - 1; // Calculate the end row
            // Submit a task to the executor service for parallel processing
            futures[thread] = executorService.submit(() -> {
                boolean localInEquilibrium = true; // Local flag to track equilibrium status for this thread
                // Process assigned rows
                for (int i = startRow; i <= endRow; i++) {
                    for (int j = 1; j < height - 1; j++) {
                        // Update the grid point based on the average temperature of its neighbors
                        newSurface[i][j] = (surface[i - 1][j] + surface[i + 1][j] + surface[i][j - 1] + surface[i][j + 1]) / 4.0;
                        // Check if the temperature change exceeds the threshold
                        if (Math.abs(newSurface[i][j] - surface[i][j]) > TEMP_THRESHOLD) {
                            localInEquilibrium = false; // If any point exceeds the threshold, it's not in equilibrium
                        }
                    }
                }
                // Synchronize the update to the shared equilibrium status
                synchronized (inEquilibriumArray) {
                    if (!localInEquilibrium) {
                        inEquilibriumArray[0] = false; // Update the shared equilibrium status
                    }
                }
            });
        }
        try {
            // Wait for all threads to complete their tasks
            for (Future<?> future : futures) {
                future.get(); // Block until the thread completes
            }
        } catch (Exception e) {
            e.printStackTrace(); // Handle any exceptions that occur during the execution
        }
        // Update the main surface with the new values from the calculation
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                surface[i][j] = newSurface[i][j]; // Copy the updated values back to the main surface array
            }
        }
        return inEquilibriumArray[0]; // Return the equilibrium status
    }
    // Override the paintComponent method to draw the grid on the JPanel
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Call the superclass method to ensure proper rendering
        int scaledWidth = getWidth(); // Get the current width of the JPanel
        int scaledHeight = getHeight(); // Get the current height of the JPanel
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); // Create a new BufferedImage to represent the grid
        // Convert the surface temperature to colors and render the grid
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                float value = (float) surface[i][j] / 100.0f; // Normalize the temperature value to a range of 0 to 1
                Color color = Color.getHSBColor(0.67f * (1 - value), 1.0f, 1.0f); // Convert the normalized value to a color
                image.setRGB(i, j, color.getRGB()); // Set the pixel color in the BufferedImage
            }
        }
        // Draw the image onto the JPanel, scaling it to fit the current size
        g.drawImage(image, 0, 0, scaledWidth - 35, scaledHeight, null);
        drawColorScale(g, scaledWidth, scaledHeight); // Draw the color scale on the right side of the panel
    }
    // Method to draw the color scale on the right side of the panel
    private void drawColorScale(Graphics g, int scaledWidth, int scaledHeight) {
        for (int i = 0; i < scaledHeight; i++) {
            float value = 1.0f - ((float) i / scaledHeight); // Calculate the normalized value for the current position
            Color color = Color.getHSBColor(0.67f * (1 - value), 1.0f, 1.0f); // Convert the normalized value to a color
            g.setColor(color);
            g.fillRect(scaledWidth - 30, i, SCALE_WIDTH, 1); // Draw a line of the color scale
        }
        // Add labels to the color scale
        g.setColor(Color.BLACK);
        g.drawString("100°C", scaledWidth - 25, 15); // Label for the top of the scale (100°C)
        g.drawString("0°C", scaledWidth - 25, scaledHeight - 5); // Label for the bottom of the scale (0°C)
    }
    // Method to start the simulation
    public void startSimulation() {
        long startTime = System.nanoTime(); // Record start time for performance measurement
        boolean equilibrium = false; // Flag to track if the grid is in equilibrium
        while (!equilibrium && running) {
            equilibrium = updateSurface(); // Update the grid and check for equilibrium
            repaint(); // Redraw the JPanel to reflect the new grid state
            if (showGUI) { // If GUI is displayed, slow down the simulation for better visualization
                try {
                    Thread.sleep(10000); // Sleep to slow down the visualization
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Handle interrupted exception
                }
            }
        }
        long endTime = System.nanoTime(); // Record end time for performance measurement
        double elapsedTime = (endTime - startTime) / 1_000_000.0; // Calculate elapsed time in milliseconds
        System.out.println("Simulation completed in " + elapsedTime + " milliseconds."); // Print the elapsed time
    }
    // Method to stop the simulation
    public void stopSimulation() {
        running = false; // Set the running flag to false to stop the simulation
    }
    // Main method to run the simulation
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java Parallel <width> <height> <numberOfHeatPoints> <showGUI>");
            System.exit(1); // Exit if the required arguments are not provided
        }
        int width = Integer.parseInt(args[0]); // Parse the width from the command line arguments
        int height = Integer.parseInt(args[1]); // Parse the height from the command line arguments
        int numberOfHeatPoints = Integer.parseInt(args[2]); // Parse the number of heat points from the command line arguments
        boolean showGUI = Boolean.parseBoolean(args[3]); // Parse the showGUI flag from the command line arguments
        if (showGUI) {
            // Create and show the GUI if requested
            JFrame frame = new JFrame("Heat Transfer"); // Create a JFrame for the simulation
            Parallel heatTransfer = new Parallel(width, height, numberOfHeatPoints); // Create an instance of the Parallel class
            frame.add(heatTransfer); // Add the JPanel to the JFrame
            frame.setSize(800, 600); // Set the default size of the window
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Set the default close operation
            frame.setVisible(true); // Make the window visible
            // Start the simulation in a separate thread to keep the UI responsive
            Thread simulationThread = new Thread(heatTransfer::startSimulation); // Create a thread for the simulation
            simulationThread.start(); // Start the simulation thread
        } else {
            // Run the simulation without a GUI
            Parallel heatTransfer = new Parallel(width, height, numberOfHeatPoints); // Create an instance of the Parallel class
            heatTransfer.startSimulation(); // Start the simulation without GUI
        }
    }
}