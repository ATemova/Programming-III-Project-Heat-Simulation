import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class parallel extends JFrame {
    private Random random = new Random();
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double MAX_TEMP_CHANGE = 0.1; // Adjust for more sensitivity
    private double[][] grid = new double[WIDTH][HEIGHT];
    private double[][] newGrid = new double[WIDTH][HEIGHT]; // Buffer for storing new state
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public parallel() {
        super("Heat Diffusion Simulation");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initializeGrid();
        setVisible(true);
        runSimulation();
    }

    private void initializeGrid() {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (i == 0 || j == 0 || i == WIDTH - 1 || j == HEIGHT - 1) {
                    grid[i][j] = 0;
                } else {
                    grid[i][j] = random.nextDouble() < 0.01 ? 100 : 20;
                }
            }
        }
    }

    private void runSimulation() {
        new Thread(() -> {
            boolean stable;
            do {
                stable = updateTemperatures();
                SwingUtilities.invokeLater(this::repaint);
                try {
                    Thread.sleep(100); // Slow down for visibility
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            } while (!stable);
            executor.shutdownNow();
        }).start();
    }

    private boolean updateTemperatures() {
        AtomicBoolean isStable = new AtomicBoolean(true);

        // Prepare tasks for each thread
        for (int i = 1; i < WIDTH - 1; i++) {
            final int row = i;
            executor.submit(() -> {
                for (int j = 1; j < HEIGHT - 1; j++) {
                    newGrid[row][j] = (grid[row - 1][j] + grid[row + 1][j] + grid[row][j - 1] + grid[row][j + 1]) / 4.0;
                    if (Math.abs(newGrid[row][j] - grid[row][j]) > MAX_TEMP_CHANGE) {
                        isStable.set(false);
                    }
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Update the grid for the next iteration
        double[][] temp = grid;
        grid = newGrid;
        newGrid = temp; // Swap grids without reallocating

        return isStable.get();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                g.setColor(getTemperatureColor(grid[i][j]));
                g.fillRect(i, j, 1, 1);
            }
        }
    }

    private Color getTemperatureColor(double temperature) {
        float value = (float) Math.max(0, Math.min(1, temperature / 100));
        return new Color(value, 0, 1 - value);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new parallel());
    }
}
