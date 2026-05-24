package sampling;

import java.util.LinkedList;
import java.util.List;

/**
 * Generates two-dimensional sample offsets inside a disk.
 */
public class DiskSampler {

    /**
     * List of sample offsets inside the disk.
     */
    private final List<Sample2D> samples = new LinkedList<>();

    /**
     * Number of samples along each logical sampling axis.
     */
    private final int sampleSize;

    /**
     * Disk radius.
     */
    private final double radius;

    /**
     * Getter for the disk samples.
     *
     * @return sample offsets inside the disk
     */
    public List<Sample2D> getSamples() {
        return samples;
    }

    /**
     * Getter for the sample size.
     *
     * @return the number of samples along each logical sampling axis
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     * Getter for the disk radius.
     *
     * @return the disk radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Constructs a disk sampler.
     *
     * @param sampleSize number of samples along each logical sampling axis
     * @param radius     disk radius
     */
    public DiskSampler(int sampleSize, double radius) {
        if (sampleSize <= 0)
            throw new IllegalArgumentException("Sample size must be positive");
        if (radius <= 0)
            throw new IllegalArgumentException("Disk radius must be positive");

        this.sampleSize = sampleSize;
        this.radius = radius;
        initializeSamples();
    }

    /**
     * Initializes uniformly distributed random samples inside the disk.
     */
    private void initializeSamples() {
        samples.clear();

        for (int i = 0; i < sampleSize * sampleSize; i++) {
            double distance = radius * Math.sqrt(Math.random());
            double angle = Math.random() * 2 * Math.PI;
            samples.add(new Sample2D(distance * Math.cos(angle), distance * Math.sin(angle)));
        }
    }
}
