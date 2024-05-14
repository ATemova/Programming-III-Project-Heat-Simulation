import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class sequential extends JFrame {
    private Random random = new Random();
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double MAX_TEMP_CHANGE = 100.0;
    private double[][] grid = new double[WIDTH][HEIGHT];
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public sequential() {
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
                stable = true;
                try {
                    stable = step();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                SwingUtilities.invokeLater(this::repaint);
            } while (!stable);
            executor.shutdownNow();
        }).start();
    }

    private boolean step() throws InterruptedException {
        double[][] newGrid = new double[WIDTH][HEIGHT];
        AtomicBoolean stable = new AtomicBoolean(true);

        for (int strip = 0; strip < Runtime.getRuntime().availableProcessors(); strip++) {
            final int from = strip * WIDTH / Runtime.getRuntime().availableProcessors();
            final int to = (strip + 1) * WIDTH / Runtime.getRuntime().availableProcessors();

            executor.submit(() -> {
                for (int i = from; i < to; i++) {
                    for (int j = 1; j < HEIGHT - 1; j++) {
                        newGrid[i][j] = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4;
                        if (Math.abs(newGrid[i][j] - grid[i][j]) > MAX_TEMP_CHANGE) {
                            stable.set(false);
                        }
                    }
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        grid = newGrid;
        return stable.get();
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
        SwingUtilities.invokeLater(() -> new sequential());  // Proper main method to start the application
    }
}
