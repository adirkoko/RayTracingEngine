package sampling;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Generates jittered two-dimensional sample offsets within a pixel.
 */
public class JitteredSampler {

    /**
     * Stable seed for reproducible jittered sampling.
     */
    private static final long RANDOM_SEED = 0x4A49545445524544L;

    /**
     * List of jittered sample offsets within the pixel.
     */
    private final List<Sample2D> samples = new LinkedList<>();

    /**
     * Number of samples along each pixel axis.
     */
    private final int sampleSize;

    /**
     * Getter for the jittered samples.
     *
     * @return the jittered sample offsets
     */
    public List<Sample2D> getSamples() {
        return samples;
    }

    /**
     * Getter for the sample size.
     *
     * @return the number of samples along each pixel axis
     */
    public int getSampleSize() {
        return sampleSize;
    }

    /**
     * Constructs a jittered sampler for a pixel.
     *
     * @param sampleSize  number of samples along each pixel axis
     * @param pixelWidth  width of the pixel
     * @param pixelHeight height of the pixel
     */
    public JitteredSampler(int sampleSize, double pixelWidth, double pixelHeight) {
        if (sampleSize <= 0)
            throw new IllegalArgumentException("Sample size must be positive");
        this.sampleSize = sampleSize;
        initializeSamples(pixelWidth, pixelHeight);
    }

    /**
     * Initializes a jittered grid of sample offsets within the pixel.
     *
     * @param pixelWidth  width of the pixel
     * @param pixelHeight height of the pixel
     */
    private void initializeSamples(double pixelWidth, double pixelHeight) {
        samples.clear();
        Random random = new Random(RANDOM_SEED);

        double jitterAmount = 1.0 / sampleSize;

        for (int p = 0; p < sampleSize; p++) {
            for (int q = 0; q < sampleSize; q++) {
                samples.add(new Sample2D(
                        (((p + 0.5) * jitterAmount) + (random.nextDouble() - 0.5) * jitterAmount) * pixelWidth - pixelWidth / 2,
                        (((q + 0.5) * jitterAmount) + (random.nextDouble() - 0.5) * jitterAmount) * pixelHeight - pixelHeight / 2));
            }
        }
    }
}
