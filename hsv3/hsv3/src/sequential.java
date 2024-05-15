import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class sequential extends JFrame {
    private static int WIDTH = 800;
    private static int HEIGHT = 600;
    private static int N = 10;
    private static final double MAX_TEMP_CHANGE = 0.25;
    private double[][] grid;
    private double[][] newGrid;
    private boolean showGUI;
    private Random random;

    public sequential(boolean showGUI) {
        super("Heat Diffusion Simulation - Sequential");
        this.showGUI = showGUI;
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        if (showGUI) {
            setVisible(true);
        }
        random = new Random(42); // Seeded random number generator
        initializeGrid();
        runSimulation();
    }

    private void initializeGrid() {
        grid = new double[WIDTH][HEIGHT];
        newGrid = new double[WIDTH][HEIGHT];

        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                grid[i][j] = 20;
            }
        }
        for (int i = 0; i < N; i++) {
            int x = random.nextInt(WIDTH - 2) + 1;
            int y = random.nextInt(HEIGHT - 2) + 1;
            grid[x][y] = 100;
        }
    }

    private void runSimulation() {
        boolean stable;
        long startTime = System.currentTimeMillis();
        do {
            stable = step();
            if (showGUI) {
                SwingUtilities.invokeLater(this::repaint);
            }
        } while (!stable);
        long endTime = System.currentTimeMillis();
        System.out.println("Simulation completed in " + (endTime - startTime) + " ms");
    }

    private boolean step() {
        boolean stable = true;
        for (int i = 1; i < WIDTH - 1; i++) {
            for (int j = 1; j < HEIGHT - 1; j++) {
                newGrid[i][j] = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4.0;
                if (Math.abs(newGrid[i][j] - grid[i][j]) > MAX_TEMP_CHANGE) {
                    stable = false;
                }
            }
        }
        double[][] temp = grid;
        grid = newGrid;
        newGrid = temp;
        return stable;
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
        if (args.length == 4) {
            WIDTH = Integer.parseInt(args[0]);
            HEIGHT = Integer.parseInt(args[1]);
            N = Integer.parseInt(args[2]);
        }
        boolean showGUI = Boolean.parseBoolean(args[3]);
        SwingUtilities.invokeLater(() -> new sequential(showGUI));
    }
}