package heatsim;

/** The Jacobi relaxation step shared by every solver. */
public final class Kernel {

    private Kernel() {
    }

    /**
     * Writes the next temperature of rows {@code [fromRow, toRow)} into {@code dst}.
     * Each free cell becomes the average of its four neighbours; fixed cells are copied.
     *
     * @return the largest absolute temperature change in the band
     */
    public static double relax(double[][] src, boolean[][] fixed, double[][] dst, int fromRow, int toRow) {
        double maxDelta = 0.0;
        int width = src[0].length;
        for (int y = fromRow; y < toRow; y++) {
            double[] up = src[y - 1];
            double[] row = src[y];
            double[] down = src[y + 1];
            boolean[] fixedRow = fixed[y];
            double[] out = dst[y];
            out[0] = row[0];
            out[width - 1] = row[width - 1];
            for (int x = 1; x < width - 1; x++) {
                if (fixedRow[x]) {
                    out[x] = row[x];
                    continue;
                }
                double value = (up[x] + down[x] + row[x - 1] + row[x + 1]) * 0.25;
                double delta = Math.abs(value - row[x]);
                if (delta > maxDelta) {
                    maxDelta = delta;
                }
                out[x] = value;
            }
        }
        return maxDelta;
    }

    /** Splits the interior rows {@code [1, height-1)} into {@code parts} contiguous bands. */
    public static int[][] bands(int height, int parts) {
        int rows = height - 2;
        int count = Math.max(1, Math.min(parts, rows));
        int[][] bands = new int[count][2];
        int start = 1;
        for (int i = 0; i < count; i++) {
            int size = rows / count + (i < rows % count ? 1 : 0);
            bands[i][0] = start;
            bands[i][1] = start + size;
            start += size;
        }
        return bands;
    }
}
