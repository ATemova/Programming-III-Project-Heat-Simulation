package heatsim;

import java.util.Random;

/**
 * Temperature field of a rectangular plate, stored row-major as {@code temp[y][x]}.
 * Edges are held at {@link #EDGE_TEMP} and heat sources at {@link #SOURCE_TEMP};
 * both are fixed cells that the solvers never change.
 */
public final class Grid {

    public static final double EDGE_TEMP = 0.0;
    public static final double SOURCE_TEMP = 100.0;

    private final int width;
    private final int height;
    private final boolean[][] fixed;
    private double[][] current;
    private double[][] next;

    public Grid(int width, int height, int sources, long seed) {
        if (width < 3 || height < 3) {
            throw new IllegalArgumentException("Grid must be at least 3x3");
        }
        if (sources < 0) {
            throw new IllegalArgumentException("Number of heat sources must not be negative");
        }
        this.width = width;
        this.height = height;
        this.fixed = new boolean[height][width];
        this.current = new double[height][width];
        this.next = new double[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    fixed[y][x] = true;
                    current[y][x] = EDGE_TEMP;
                }
            }
        }

        Random random = new Random(seed);
        for (int i = 0; i < sources; i++) {
            int x = 1 + random.nextInt(width - 2);
            int y = 1 + random.nextInt(height - 2);
            fixed[y][x] = true;
            current[y][x] = SOURCE_TEMP;
        }

        for (int y = 0; y < height; y++) {
            System.arraycopy(current[y], 0, next[y], 0, width);
        }
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public double[][] current() {
        return current;
    }

    double[][] next() {
        return next;
    }

    public boolean[][] fixed() {
        return fixed;
    }

    void swap() {
        double[][] tmp = current;
        current = next;
        next = tmp;
    }

    public double[][] snapshot() {
        double[][] copy = new double[height][];
        for (int y = 0; y < height; y++) {
            copy[y] = current[y].clone();
        }
        return copy;
    }
}
