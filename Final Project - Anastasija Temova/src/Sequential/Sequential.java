package Sequential;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
public class Sequential extends JPanel {
    // Grid dimensions
    private int width;  // Width of the grid (number of columns)
    private int height;  // Height of the grid (number of rows)
    private int numberOfHeatPoints;  // Number of initial heat sources in the grid
    // Constants for the simulation
    private static final double BOUNDARY_TEMP = 0.0;  // Temperature value at the boundaries of the grid
    private static final double TEMP_THRESHOLD = 0.25;  // Threshold difference to determine equilibrium
    private static final int SCALE_WIDTH = 20;  // Width of the color scale to be displayed in the GUI
    // 2D array to represent the temperature grid (surface)
    private double[][] surface;  // Stores the temperature values of the grid cells
    // Control flags for the simulation
    private volatile boolean running = true;  // Flag to keep track of whether the simulation is running
    private boolean showGUI;  // Flag to determine if the GUI visualization should be displayed
    // Constructor to initialize the grid and start the simulation
    public Sequential(int width, int height, int numberOfHeatPoints, boolean showGUI) {
        this.width = width;  // Set grid width
        this.height = height;  // Set grid height
        this.numberOfHeatPoints = numberOfHeatPoints;  // Set the number of initial heat points
        this.showGUI = showGUI;  // Set the GUI display flag
        // Initialize the grid (surface) with boundary temperatures
        surface = new double[width][height];  // Create a 2D array to store temperature values
        initializeSurface();  // Call method to initialize the grid with heat points and boundary conditions
    }
    // Method to initialize the grid with boundary temperatures and heat sources
    private void initializeSurface() {
        // Set all cells in the grid to the boundary temperature
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                surface[i][j] = BOUNDARY_TEMP;  // Initialize every cell to the boundary temperature
            }
        }
        // Randomly place heat sources on the grid
        Random rand = new Random();  // Create a random number generator
        for (int i = 0; i < numberOfHeatPoints; i++) {
            int heatSourceX = rand.nextInt(width);  // Randomly choose an x-coordinate for the heat source
            int heatSourceY = rand.nextInt(height);  // Randomly choose a y-coordinate for the heat source
            applyHeat(heatSourceX, heatSourceY, 100.0, 5);  // Apply heat around the selected point
        }
    }
    // Method to apply heat to a circular area around a center point
    private void applyHeat(int centerX, int centerY, double temperature, int radius) {
        // Iterate over a square grid surrounding the center point
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                int x = centerX + i;  // Calculate the x-coordinate of the current cell
                int y = centerY + j;  // Calculate the y-coordinate of the current cell
                if (x >= 0 && x < width && y >= 0 && y < height) {  // Ensure the cell is within grid bounds
                    double distance = Math.sqrt(i * i + j * j);  // Calculate the distance from the center point
                    double influence = Math.max(0, (radius - distance) / radius);  // Determine the heat influence based on distance
                    surface[x][y] = Math.max(surface[x][y], temperature * influence);  // Apply the temperature based on influence
                }
            }
        }
    }
    // Method to update the temperature grid and check for equilibrium
    private boolean updateSurface() {
        double[][] newSurface = new double[width][height];  // Create a new grid to store updated temperatures
        boolean inEquilibrium = true;  // Flag to check if the grid has reached equilibrium
        // Iterate over the grid cells (excluding the boundary)
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                // Calculate the average temperature of the four neighboring cells
                newSurface[i][j] = (surface[i - 1][j] + surface[i + 1][j] + surface[i][j - 1] + surface[i][j + 1]) / 4.0;

                // Check if the temperature change is significant (greater than the threshold)
                if (Math.abs(newSurface[i][j] - surface[i][j]) > TEMP_THRESHOLD) {
                    inEquilibrium = false;  // If the change is significant, the grid is not in equilibrium
                }
            }
        }
        // Copy the updated temperatures back to the main surface grid
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                surface[i][j] = newSurface[i][j];  // Update the original grid with the new temperatures
            }
        }
        return inEquilibrium;  // Return whether the grid has reached equilibrium
    }
    // Override the paintComponent method to draw the grid and color scale
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);  // Call superclass method to ensure proper painting behavior
        int scaledWidth = getWidth();  // Get the current width of the JPanel
        int scaledHeight = getHeight();  // Get the current height of the JPanel
        // Create a BufferedImage to represent the grid
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // Iterate over the grid cells (excluding the boundary) to set colors based on temperature
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < height - 1; j++) {
                float value = (float) surface[i][j] / 100.0f;  // Normalize the temperature to a 0-1 range
                Color color = Color.getHSBColor(0.67f * (1 - value), 1.0f, 1.0f);  // Convert temperature to color
                image.setRGB(i, j, color.getRGB());  // Set the pixel color in the BufferedImage
            }
        }
        // Draw the BufferedImage on the JPanel
        g.drawImage(image, 0, 0, scaledWidth - 35, scaledHeight, null);  // Draw the image on the JPanel with appropriate scaling
        // Call method to draw the color scale on the right side of the JPanel
        drawColorScale(g, scaledWidth, scaledHeight);
    }
    // Method to draw the color scale on the right side of the panel
    private void drawColorScale(Graphics g, int scaledWidth, int scaledHeight) {
        // Iterate over the height of the panel to draw the color scale
        for (int i = 0; i < scaledHeight; i++) {
            float value = 1.0f - ((float) i / scaledHeight);  // Calculate the value for the current position on the scale
            Color color = Color.getHSBColor(0.67f * (1 - value), 1.0f, 1.0f);  // Convert value to a color
            g.setColor(color);  // Set the current color for drawing
            g.fillRect(scaledWidth - 30, i, SCALE_WIDTH, 1);  // Draw a line on the scale at the current position
        }
        // Draw labels for the maximum and minimum temperatures
        g.setColor(Color.BLACK);  // Set color for the text
        g.drawString("100°C", scaledWidth - 25, 15);  // Label for 100°C at the top of the scale
        g.drawString("0°C", scaledWidth - 25, scaledHeight - 5);  // Label for 0°C at the bottom of the scale
    }
    // Method to start the simulation loop
    public void startSimulation() {
        long startTime = System.nanoTime();  // Record the start time of the simulation
        boolean equilibrium = false;  // Flag to check if the grid has reached equilibrium
        // Run the simulation loop until equilibrium is reached or the simulation is stopped
        while (!equilibrium && running) {
            equilibrium = updateSurface();  // Update the grid and check for equilibrium
            repaint();  // Repaint the GUI to reflect the updated grid

            // If the GUI is being displayed, slow down the simulation for visualization purposes
            if (showGUI) {
                try {
                    Thread.sleep(200);  // Pause for 200 milliseconds between updates
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();  // Handle interruption during sleep
                }
            }
        }
        long endTime = System.nanoTime();  // Record the end time of the simulation
        double elapsedTime = (endTime - startTime) / 1_000_000.0;  // Calculate the elapsed time in milliseconds
        System.out.println("Simulation completed in " + elapsedTime + " milliseconds.");  // Output the simulation time
    }
    // Method to stop the simulation loop
    public void stopSimulation() {
        running = false;  // Set the running flag to false to stop the simulation
    }
    // Main method to run the simulation
    public static void main(String[] args) {
        // Ensure the correct number of command-line arguments are provided
        if (args.length < 4) {
            System.out.println("Usage: java Sequential <width> <height> <numberOfHeatPoints> <showGUI>");
            return;  // Exit if incorrect arguments are provided
        }
        // Parse command-line arguments to set up the simulation
        int width = Integer.parseInt(args[0]);  // Parse grid width
        int height = Integer.parseInt(args[1]);  // Parse grid height
        int numberOfHeatPoints = Integer.parseInt(args[2]);  // Parse number of heat points
        boolean showGUI = Boolean.parseBoolean(args[3]);  // Parse whether to show the GUI
        // Create an instance of the Sequential class to start the simulation
        Sequential simulation = new Sequential(width, height, numberOfHeatPoints, showGUI);
        // If GUI display is enabled, set up the JFrame to display the simulation
        if (showGUI) {
            JFrame frame = new JFrame("Sequential Heat Diffusion Simulation");  // Create a new JFrame window
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Ensure the application exits when the window is closed
            frame.setSize(800, 800);  // Set the size of the window
            frame.add(simulation);  // Add the simulation JPanel to the frame
            frame.setVisible(true);  // Make the JFrame visible
        }
        // Start the simulation in a new thread to allow the GUI to remain responsive
        new Thread(simulation::startSimulation).start();
    }
}