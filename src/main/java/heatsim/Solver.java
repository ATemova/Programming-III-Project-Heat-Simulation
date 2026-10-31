package heatsim;

/** Performs one relaxation step over the whole grid. */
public interface Solver extends AutoCloseable {

    String name();

    /**
     * Computes the next state into the grid's back buffer.
     *
     * @return the largest absolute temperature change of this step
     */
    double step(Grid grid) throws Exception;

    @Override
    default void close() {
    }
}
