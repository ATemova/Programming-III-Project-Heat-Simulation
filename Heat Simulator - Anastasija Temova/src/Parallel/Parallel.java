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

    public Parallel(int width, int height, int numberOfHeatPoints) {
        this.width = width;
        this.height = height;
        this.numberOfHeatPoints = numberOfHeatPoints;
        surface = new double[width][height]; // Initialize the surface with the given dimensions
        initializeSurface(); // Initialize the grid with boundary temperature and random heat points
        executorService = Executors.newFixedThreadPool(NUM_THREADS); // Set up the thread pool
    }

    private void initializeSurface() {
        // Set all grid points to the boundary temperature
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                surface[i][j] = BOUNDARY_TEMP;
            }
        }

        // Randomly distribute initial heat points on the grid
        Random rand = new Random(42);
        for (int i = 0; i < numberOfHeatPoints; i++) {
            int heatSourceX = rand.nextInt(width);
            int heatSourceY = rand.nextInt(height);
            applyHeat(heatSourceX, heatSourceY, 100.0, 5); // Apply heat with a fixed temperature and radius
        }
    }

    private void applyHeat(int centerX, int centerY, double temperature, int radius) {
        // Apply heat within a given radius around the center point
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int x = centerX + i;
                int y = centerY + j;
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    double distance = Math.sqrt(i * i + j * j);
                    double influence = Math.max(0, (radius - distance) / radius);
                    surface[x][y] = Math.max(surface[x][y], temperature * influence); // Calculate heat influence
                }
            }
        }
    }

    private boolean updateSurface() {
        double[][] newSurface = new double[width][height]; // New grid to store updated values
        boolean[] inEquilibriumArray = { true }; // Shared variable to track equilibrium status

        Future<?>[] futures = new Future[NUM_THREADS];
        int rowsPerThread = (height - 2) / NUM_THREADS; // Number of rows each thread will process

        // Distribute work across multiple threads
        for (int thread = 0; thread < NUM_THREADS; thread++) {
            final int startRow = 1 + thread * rowsPerThread;
            final int endRow = (thread == NUM_THREADS - 1) ? height - 2 : startRow + rowsPerThread - 1;

            futures[thread] = executorService.submit(() -> {
                boolean localInEquilibrium = true;

                // Process assigned rows
                for (int i = startRow; i <= endRow; i++) {
                    for (int j = 1; j < height - 1; j++) {
                        newSurface[i][j] = (surface[i - 1][j] + surface[i + 1][j] + surface[i][j - 1] + surface[i][j + 1]) / 4.0;
                        if (Math.abs(newSurface[i][j] - surface[i][j]) > TEMP_THRESHOLD) {
                            localInEquilibrium = false; // If any point exceeds the threshold, it's not in equilibrium
                        }
                    }
                }

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
                future.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Update the main surface with the new values
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                surface[i][j] = newSurface[i][j];
            }
        }

        return inEquilibriumArray[0]; // Return the equilibrium status
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int scaledWidth = getWidth();
        int scaledHeight = getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Convert the surface temperature to colors and render the grid
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                float value = (float) surface[i][j] / 100.0f;
                Color color = Color.getHSBColor(0.67f * (1 - value), 1.0f, 1.0f);
                image.setRGB(i, j, color.getRGB());
            }
        }

        // Draw the image onto the JPanel
        g.drawImage(image, 0, 0, scaledWidth - 35, scaledHeight, null);
        drawColorScale(g, scaledWidth, scaledHeight); // Draw the color scale on the right side of the panel
    }

    private void drawColorScale(Graphics g, int scaledWidth, int scaledHeight) {
        for (int i = 0; i < scaledHeight; i++) {
            float value = 1.0f - ((float) i / scaledHeight);
            Color color = Color.getHSBColor(0.67f * (1 - value), 1.0f, 1.0f);
            g.setColor(color);
            g.fillRect(scaledWidth - 30, i, SCALE_WIDTH, 1); // Draw a line of the color scale
        }

        // Add labels to the color scale
        g.setColor(Color.BLACK);
        g.drawString("100°C", scaledWidth - 25, 15);
        g.drawString("0°C", scaledWidth - 25, scaledHeight - 5);
    }

    public void startSimulation() {
        long startTime = System.nanoTime(); // Record start time
        boolean equilibrium = false;

        while (!equilibrium && running) {
            equilibrium = updateSurface(); // Update the grid and check for equilibrium
            repaint(); // Redraw the JPanel to reflect the new grid state

            if (showGUI) {
                try {
                    Thread.sleep(10000); // Slow down the simulation for better visualization
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        long endTime = System.nanoTime(); // Record end time
        double elapsedTime = (endTime - startTime) / 1_000_000.0;
        System.out.println("Simulation completed in " + elapsedTime + " milliseconds.");
    }

    public void stopSimulation() {
        running = false; // Set the running flag to false to stop the simulation
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java Parallel <width> <height> <numberOfHeatPoints> <showGUI>");
            System.exit(1);
        }

        int width = Integer.parseInt(args[0]);
        int height = Integer.parseInt(args[1]);
        int numberOfHeatPoints = Integer.parseInt(args[2]);
        boolean showGUI = Boolean.parseBoolean(args[3]);

        if (showGUI) {
            // Create and show the GUI if requested
            JFrame frame = new JFrame("Heat Transfer");
            Parallel heatTransfer = new Parallel(width, height, numberOfHeatPoints);
            frame.add(heatTransfer);
            frame.setSize(800, 600); // Set the default size of the window
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            // Start the simulation in a separate thread to keep the UI responsive
            Thread simulationThread = new Thread(heatTransfer::startSimulation);
            simulationThread.start();
        } else {
            // Run the simulation without a GUI
            Parallel heatTransfer = new Parallel(width, height, numberOfHeatPoints);
            heatTransfer.startSimulation();
        }
    }
}