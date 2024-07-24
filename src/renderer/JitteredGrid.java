package renderer;

import primitives.Point;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a jittered grid for generating anti-aliasing rays.
 * Generates and holds jittered points within a pixel.
 *
 * @author Adir and Meir
 */
public class JitteredGrid {

    /**
     * List of jittered points within the pixel.
     */
    private final List<Point> jitteredPoints = new LinkedList<>();

    /**
     * Number of samples per pixel for anti-aliasing.
     */
    private final int sampleSize;

    /**
     * Constructs a JitteredGrid with the specified sample size and pixel dimensions.
     *
     * @param sampleSize  number of samples per pixel.
     * @param pixelWidth  width of the pixel.
     * @param pixelHeight height of the pixel.
     */
    public JitteredGrid(int sampleSize, double pixelWidth, double pixelHeight) {
        this.sampleSize = sampleSize;
        initializeJitteredPoints(pixelWidth, pixelHeight); // Initialize jittered points within the pixel
    }

    /**
     * Initializes the jittered points within the pixel.
     * Creates a grid of points within the pixel, with random jittering for each point.
     *
     * @param pixelWidth  width of the pixel.
     * @param pixelHeight height of the pixel.
     */
    private void initializeJitteredPoints(double pixelWidth, double pixelHeight) {
        jitteredPoints.clear(); // Clear existing points
        for (int p = 0; p < sampleSize; p++) {
            for (int q = 0; q < sampleSize; q++) {
                // Add jittered point to the list
                jitteredPoints.add(new Point(
                        (Math.random() - 0.5 + (double) p / sampleSize) * pixelWidth,
                        (Math.random() - 0.5 + (double) q / sampleSize) * pixelHeight,
                        0));
            }
        }
    }

    /**
     * Getter for the Jittered points.
     *
     * @return the list of jittered points within the pixel.
     */
    public List<Point> getJitteredPoints() {
        return jitteredPoints;
    }

    /**
     * Getter for the sample size.
     *
     * @return the number of samples per pixel.
     */
    public int getSampleSize() {
        return sampleSize;
    }
}
