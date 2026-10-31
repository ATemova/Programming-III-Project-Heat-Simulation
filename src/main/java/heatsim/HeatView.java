package heatsim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/** Swing window that shows the plate from blue (0 °C) to red (100 °C). */
public final class HeatView implements Simulation.Listener {

    private static final long FRAME_INTERVAL_NANOS = 33_000_000L;

    private final BufferedImage image;
    private final JPanel canvas;
    private final JLabel status;
    private final JFrame frame;
    private long lastFrame;

    public HeatView(int width, int height, String title) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                synchronized (image) {
                    g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
                }
            }
        };
        double scale = Math.max(1.0, Math.min(800.0 / width, 800.0 / height));
        canvas.setPreferredSize(new Dimension((int) (width * scale), (int) (height * scale)));
        status = new JLabel(" ");
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(status, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void onStep(int iteration, Grid grid, double maxDelta) {
        long now = System.nanoTime();
        if (now - lastFrame < FRAME_INTERVAL_NANOS) {
            return;
        }
        lastFrame = now;
        render(grid, "Iteration " + iteration + "   max change " + String.format("%.4f", maxDelta) + " °C");
    }

    public void finish(Grid grid, Simulation.Result result) {
        render(grid, String.format("Stable after %d iterations (%d ms)", result.iterations(), result.millis()));
    }

    private void render(Grid grid, String text) {
        double[][] t = grid.current();
        synchronized (image) {
            for (int y = 0; y < grid.height(); y++) {
                for (int x = 0; x < grid.width(); x++) {
                    image.setRGB(x, y, color(t[y][x]));
                }
            }
        }
        SwingUtilities.invokeLater(() -> {
            status.setText(text);
            canvas.repaint();
        });
    }

    static int color(double temperature) {
        double v = Math.max(0.0, Math.min(1.0, temperature / Grid.SOURCE_TEMP));
        float hue = (float) ((1.0 - v) * 240.0 / 360.0);
        return Color.HSBtoRGB(hue, 1.0f, 1.0f);
    }
}
