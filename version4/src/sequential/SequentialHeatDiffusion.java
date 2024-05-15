package sequential;
import common.GridUtils;

public class SequentialHeatDiffusion {
    public static void run(int width, int height, int points) {
        double[][] grid = GridUtils.initializeGrid(width, height, points);
        boolean stable;
        long startTime = System.currentTimeMillis();

        do {
            stable = GridUtils.updateGridSequential(grid, width, height);
        } while (!stable);

        long endTime = System.currentTimeMillis();
        System.out.println("Sequential simulation completed in " + (endTime - startTime) + " ms");
    }
}