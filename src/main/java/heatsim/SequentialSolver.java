package heatsim;

/** Single-threaded reference implementation. */
public final class SequentialSolver implements Solver {

    @Override
    public String name() {
        return "sequential";
    }

    @Override
    public double step(Grid grid) {
        return Kernel.relax(grid.current(), grid.fixed(), grid.next(), 1, grid.height() - 1);
    }
}
