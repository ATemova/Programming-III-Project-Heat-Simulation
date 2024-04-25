import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Random;

public class HeatSimulation extends JFrame {

    private static final int dWidth = 800; //width
    private static final int dHeight = 600; //height
    private static final double EnvironmentTemperature = 0.0;
    private static final double Epsilon = 0.25;
    private static final double Temperature = 100.0;

    private int width;
    private int height;
    private double[][] grid;
    private BufferedImage image;

    //constructor
    public HeatSimulation(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new double[height][width];
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        initializeGrid();
        initializeGUI();
    }

    private void initializeGrid() {
        Random random = new Random(0); // Seeded random number generator
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i == 0 || j == 0 || i == height - 1 || j == width - 1) {
                    grid[i][j] = EnvironmentTemperature;
                } else {
                    grid[i][j] = random.nextDouble() * Temperature;
                }
            }
        }
    }

    private void initializeGUI() {
        setTitle("Combined Heat Simulation");
        setSize(dWidth, dHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void drawImage() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int temperature = (int) Math.round(grid[i][j]);
                Color color = getColorForTemperature(temperature);
                image.setRGB(j, i, color.getRGB()); // swap the indices here
            }
        }
    }

    private Color getColorForTemperature(double temperature) {
        int blue = Math.max(0, 255 - (int) (2.55 * temperature));
        int red = Math.min(255, (int) (2.55 * temperature));
        return new Color(red, 0, blue);
    }

    private void propagateHeat() {
        double[][] newGrid = new double[height][width];
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                newGrid[i][j] = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4;
            }
        }
        grid = newGrid;
    }

    private boolean isStable() {
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                if (Math.abs(grid[i][j] - (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4) > Epsilon) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }

    //main function
    public static void main(String[] args) {
        int width = 800;
        int height = 600;
        HeatSimulation simulation = new HeatSimulation(width, height);
        long startTime = System.currentTimeMillis();

        //timer that calls the simulation update every 100 milliseconds
        Timer timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (simulation.isStable()) {
                    ((Timer)e.getSource()).stop();  //stop the timer when the simulation is stable
                    long endTime = System.currentTimeMillis();
                    System.out.println("Simulation completed in " + (endTime - startTime) + " milliseconds.");
                } else {
                    simulation.propagateHeat();
                    simulation.drawImage();
                    simulation.repaint();  //repaint the panel
                }
            }
        });

        timer.start();  //start the timer
    }
}