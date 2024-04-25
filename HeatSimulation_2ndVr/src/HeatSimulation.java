//Project number 7
//Heat Simulation

//Anastasija Temova
//Student ID: 89221055

    /*
    This is what I have managed to do until now:
    I will try to improve the "public void paint(Graphics g)" part and see what will happen
    I am not happy with my output because if you try to expand the window,
    you will see that my grid is duplicating in some sort of strange way.
    If you have any suggestions on how I can solve this problem, please give me at least some hint or idea.
    I will really appreciate your opinion.
    */

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeatSimulation extends JFrame {
    private Random random = new Random();  //random generator

    private static int width = 800;  //width
    private static int height = 600;  //height

    /*
    private static final int width = 1024;  //width
    private static final int height = 1024;  //height
     */
    private static final double MAX_TEMP_CHANGE = 100.0;  //maximum temperature change
    private double[][] grid = new double[width][height];  //grid

    //main function
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HeatSimulation simulation = new HeatSimulation();
            simulation.setSize(width, height);
            simulation.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            simulation.setVisible(true);
            simulation.run();
        });
    }

    public void run() {
        initializeGrid();
        boolean stable = false;
        while (!stable) {
            try {
                stable = step();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        repaint();
    }

    private void initializeGrid() {
        //trying to initialize grid with 0°C at the sides and 100°C at random points
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (i == 0 || j == 0 || i == width - 1 || j == height - 1) {
                    grid[i][j] = 0;
                } else {
                    grid[i][j] = random.nextDouble() < 0.01 ? 100 : 0;
                }
            }
        }
    }

    private boolean step() throws InterruptedException {
        double[][] newGrid = new double[width][height];
        AtomicBoolean stable = new AtomicBoolean(true);
        //thread pool
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        //dividing the grid into strips
        for (int strip = 0; strip < Runtime.getRuntime().availableProcessors(); strip++) {
            final int from = strip * width / Runtime.getRuntime().availableProcessors();
            final int to = (strip + 1) * width / Runtime.getRuntime().availableProcessors();

            //each thread computes the new temperatures for a strip of the grid
            executor.submit(() -> {
                for (int i = from; i < to; i++) {
                    for (int j = 1; j < height - 1; j++) {
                        newGrid[i][j] = (grid[i - 1][j] + grid[i + 1][j] + grid[i][j - 1] + grid[i][j + 1]) / 4;
                        if (Math.abs(newGrid[i][j] - grid[i][j]) > MAX_TEMP_CHANGE) {
                            stable.set(false);
                        }
                    }
                }
            });
        }

        //waiting for all threads to finish
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

        //updating the grid only after all threads have finished
        grid = newGrid;
        return stable.get();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //calculating the center of the grid
        int centerX = width / 2;
        int centerY = height / 2;
        //I am trying for each radius to paint a square ring around the center of the grid
        for (int radius = 0; radius <= Math.max(width, height) / 2; radius++) {
            for (int i = Math.max(0, centerX - radius); i <= Math.min(width - 1, centerX + radius); i++) {
                for (int j = Math.max(0, centerY - radius); j <= Math.min(height - 1, centerY + radius); j++) {
                    g.setColor(getTemperatureColor(grid[i][j]));
                    g.fillRect(i, j, 1, 1);
                }
            }
        }
    }

    private Color getTemperatureColor(double temperature) {
        float value = (float) Math.max(0, Math.min(1, temperature / 100));
        return new Color(value, 0, 1 - value);
    }
}