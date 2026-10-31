package heatsim;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimulationTest {

    private static final int W = 61;
    private static final int H = 47;
    private static final int POINTS = 12;
    private static final long SEED = 7;

    private static Grid solve(Solver solver) throws Exception {
        try (solver) {
            Grid grid = new Grid(W, H, POINTS, SEED);
            Simulation.Result r = Simulation.run(grid, solver, 0.01, 1_000_000, null);
            assertTrue(r.converged());
            return grid;
        }
    }

    private static void assertSamePlate(Grid expected, Grid actual) {
        for (int y = 0; y < H; y++) {
            assertArrayEquals(expected.current()[y], actual.current()[y], 0.0, "row " + y);
        }
    }

    @Test
    void parallelMatchesSequentialExactly() throws Exception {
        Grid reference = solve(new SequentialSolver());
        for (int threads : new int[] {1, 2, 3, 8, 100}) {
            assertSamePlate(reference, solve(new ParallelSolver(threads)));
        }
    }

    @Test
    void distributedMatchesSequentialExactly() throws Exception {
        Grid reference = solve(new SequentialSolver());
        for (int workers : new int[] {1, 3, 5}) {
            assertSamePlate(reference, solve(DistributedSolver.local(workers)));
        }
    }

    @Test
    void edgesAndSourcesStayFixed() throws Exception {
        Grid grid = solve(new SequentialSolver());
        double[][] t = grid.current();
        boolean[][] fixed = grid.fixed();
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                boolean edge = x == 0 || y == 0 || x == W - 1 || y == H - 1;
                if (edge) {
                    assertEquals(Grid.EDGE_TEMP, t[y][x]);
                } else if (fixed[y][x]) {
                    assertEquals(Grid.SOURCE_TEMP, t[y][x]);
                } else {
                    assertTrue(t[y][x] > Grid.EDGE_TEMP && t[y][x] < Grid.SOURCE_TEMP);
                }
            }
        }
    }

    @Test
    void sameSeedGivesSameSources() {
        Grid a = new Grid(30, 20, 5, 99);
        Grid b = new Grid(30, 20, 5, 99);
        for (int y = 0; y < 20; y++) {
            assertArrayEquals(a.fixed()[y], b.fixed()[y]);
        }
    }

    @Test
    void smallestGridConverges() throws Exception {
        Grid grid = new Grid(3, 3, 0, 1);
        Simulation.Result r = Simulation.run(grid, new SequentialSolver(), 0.25, 10, null);
        assertTrue(r.converged());
        assertEquals(1, r.iterations());
    }

    @Test
    void bandsCoverInteriorRowsWithoutGaps() {
        for (int height = 3; height < 40; height++) {
            for (int parts = 1; parts < 50; parts++) {
                int[][] bands = Kernel.bands(height, parts);
                assertEquals(1, bands[0][0]);
                assertEquals(height - 1, bands[bands.length - 1][1]);
                for (int i = 1; i < bands.length; i++) {
                    assertEquals(bands[i - 1][1], bands[i][0]);
                    assertTrue(bands[i][1] > bands[i][0]);
                }
            }
        }
    }

    @Test
    void rejectsTooSmallGrid() {
        assertThrows(IllegalArgumentException.class, () -> new Grid(2, 10, 1, 1));
    }
}
