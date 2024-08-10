package Sequential;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Sequential extends JPanel {
    // Grid dimensions
    private int width;
    private int height;
    private int numberOfHeatPoints;

    // Constant values for simulation
    private static final double BOUNDARY_TEMP = 0.0; // Temperature at the boundary of the grid
    private static final double TEMP_THRESHOLD = 0.25; // Threshold for determining equilibrium
    private static final int SCALE_WIDTH = 20; // Width of the color scale in the GUI

    // Grid representing the surface temperature
    private double[][] surface;

    // Control flags for the simulation
    private volatile boolean running = true; // Indicates if the simulation should keep running
    private boolean showGUI; // Indicates if the GUI should be displayed

    public Sequential(int width, int height, int numberOfHeatPoints, boolean showGUI) {
        this.width = width;
        this.height = height;
        this.numberOfHeatPoints = numberOfHeatPoints;
        this.showGUI = showGUI; // Initialize flag for GUI display
        surface = new double[width][height]; // Initialize the surface grid
        initializeSurface(); // Initialize the grid with boundary temperatures and random heat sources
    }

    private void initializeSurface() {
        // Set all grid cells to the boundary temperature
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                surface[i][j] = BOUNDARY_TEMP;
            }
        }

        // Randomly place heat sources on the grid
        Random rand = new Random();
        for (int i = 0; i < numberOfHeatPoints; i++) {
            int heatSourceX = rand.nextInt(width);
            int heatSourceY = rand.nextInt(height);
            applyHeat(heatSourceX, heatSourceY, 100.0, 5); // Apply heat to a small area around the heat source
        }
    }

    private void applyHeat(int centerX, int centerY, double temperature, int radius) {
        // Apply heat to a circular area around the center point
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int x = centerX + i;
                int y = centerY + j;
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    double distance = Math.sqrt(i * i + j * j);
                    double influence = Math.max(0, (radius - distance) / radius);
                    surface[x][y] = Math.max(surface[x][y], temperature * influence);
                }
            }
        }
    }

    private boolean updateSurface() {
        double[][] newSurface = new double[width][height]; // Create a new surface to store updated temperatures
        boolean inEquilibrium = true; // Flag to track if the grid has reached equilibrium

        // Calculate the new temperature for each grid cell
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                newSurface[i][j] = (surface[i - 1][j] + surface[i + 1][j] + surface[i][j - 1] + surface[i][j + 1]) / 4.0;
                if (Math.abs(newSurface[i][j] - surface[i][j]) > TEMP_THRESHOLD) {
                    inEquilibrium = false; // If the temperature difference is above the threshold, not in equilibrium
                }
            }
        }

        // Copy new surface values back to the main surface
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                surface[i][j] = newSurface[i][j];
            }
        }

        return inEquilibrium; // Return whether the grid is in equilibrium
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int scaledWidth = getWidth(); // Get the current width of the component
        int scaledHeight = getHeight(); // Get the current height of the component
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Create a color image based on surface temperatures
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                float value = (float) surface[i][j] / 100.0f;
                Color color = Color.getHSBColor(0.67f * (1 - value), 1.0f, 1.0f);
                image.setRGB(i, j, color.getRGB());
            }
        }
        g.drawImage(image, 0, 0, scaledWidth - 35, scaledHeight, null); // Draw the image on the JPanel
        drawColorScale(g, scaledWidth, scaledHeight); // Draw the color scale on the side
    }

    private void drawColorScale(Graphics g, int scaledWidth, int scaledHeight) {
        // Draw the color scale on the right side of the panel
        for (int i = 0; i < scaledHeight; i++) {
            float value = 1.0f - ((float) i / scaledHeight);
            Color color = Color.getHSBColor(0.67f * (1 - value), 1.0f, 1.0f);
            g.setColor(color);
            g.fillRect(scaledWidth - 30, i, SCALE_WIDTH, 1);
        }
        // Add labels for the temperature values
        g.setColor(Color.BLACK);
        g.drawString("100°C", scaledWidth - 25, 15);
        g.drawString("0°C", scaledWidth - 25, scaledHeight - 5);
    }

    public void startSimulation() {
        long startTime = System.nanoTime(); // Record the start time of the simulation
        boolean equilibrium = false; // Flag to check if the grid has reached equilibrium

        // Run the simulation until equilibrium is reached
        while (!equilibrium && running) {
            equilibrium = updateSurface(); // Update the grid and check for equilibrium
            repaint(); // Repaint the GUI to reflect the updated grid

            // Slow down the simulation for visualization if GUI is shown
            if (showGUI) {
                try {
                    Thread.sleep(200); // Pause for 200 milliseconds between updates
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Handle interruption during sleep
                }
            }
        }

        long endTime = System.nanoTime(); // Record the end time of the simulation
        double elapsedTime = (endTime - startTime) / 1_000_000.0; // Calculate elapsed time in milliseconds
        System.out.println("Simulation completed in " + elapsedTime + " milliseconds."); // Print the simulation time
    }

    public void stopSimulation() {
        running = false; // Set the running flag to false to stop the simulation
    }

    public static void main(String[] args) {
        // Check for correct number of command-line arguments
        if (args.length < 4) {
            System.out.println("Usage: java Sequential <width> <height> <numberOfHeatPoints> <showGUI>");
            return;
        }

        // Parse command-line arguments
        int width = Integer.parseInt(args[0]);
        int height = Integer.parseInt(args[1]);
        int numberOfHeatPoints = Integer.parseInt(args[2]);
        boolean showGUI = Boolean.parseBoolean(args[3]);

        Sequential simulation = new Sequential(width, height, numberOfHeatPoints, showGUI); // Create a simulation instance

        if (showGUI) {
            JFrame frame = new JFrame("Sequential Heat Diffusion Simulation"); // Create a JFrame for the GUI
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 800); // Set the window size
            frame.add(simulation); // Add the simulation panel to the frame
            frame.setVisible(true); // Make the frame visible
        }

        new Thread(simulation::startSimulation).start(); // Start the simulation in a new thread
    }
}