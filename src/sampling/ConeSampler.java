package sampling;

import primitives.Vector;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static primitives.Util.isZero;

/**
 * Generates random direction samples inside a cone around a center direction.
 */
public class ConeSampler {

    /**
     * Stable seed for reproducible cone sampling.
     */
    private static final long RANDOM_SEED = 0x434F4E4553414D50L;

    /**
     * Direction samples inside the cone.
     */
    private final List<Vector> samples = new LinkedList<>();

    /**
     * Center direction of the cone.
     */
    private final Vector direction;

    /**
     * Cone blur radius around the center direction.
     */
    private final double radius;

    /**
     * Number of generated direction samples.
     */
    private final int sampleCount;

    /**
     * Getter for the cone direction samples.
     *
     * @return direction samples inside the cone
     */
    public List<Vector> getSamples() {
        return samples;
    }

    /**
     * Getter for the cone center direction.
     *
     * @return normalized center direction
     */
    public Vector getDirection() {
        return direction;
    }

    /**
     * Getter for the cone blur radius.
     *
     * @return cone blur radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Getter for the sample count.
     *
     * @return number of generated samples
     */
    public int getSampleCount() {
        return sampleCount;
    }

    /**
     * Constructs a cone sampler.
     *
     * @param direction   center direction of the cone
     * @param radius      cone blur radius
     * @param sampleCount number of direction samples
     */
    public ConeSampler(Vector direction, double radius, int sampleCount) {
        if (direction == null)
            throw new IllegalArgumentException("Cone direction cannot be null");
        if (radius <= 0)
            throw new IllegalArgumentException("Cone radius must be positive");
        if (sampleCount <= 0)
            throw new IllegalArgumentException("Sample count must be positive");

        this.direction = direction.normalize();
        this.radius = radius;
        this.sampleCount = sampleCount;
        initializeSamples();
    }

    /**
     * Initializes random direction samples around the center direction.
     */
    private void initializeSamples() {
        Vector right = createPerpendicular(direction).normalize();
        Vector up = direction.crossProduct(right).normalize();
        Random random = new Random(RANDOM_SEED);

        for (int i = 0; i < sampleCount; i++) {
            double distance = radius * Math.sqrt(random.nextDouble());
            double angle = random.nextDouble() * 2 * Math.PI;
            samples.add(direction
                    .add(right.scale(distance * Math.cos(angle)))
                    .add(up.scale(distance * Math.sin(angle)))
                    .normalize());
        }
    }

    /**
     * Creates a stable perpendicular vector for the given direction.
     *
     * @param direction center direction
     * @return a vector perpendicular to the direction
     */
    private Vector createPerpendicular(Vector direction) {
        return isZero(direction.dotProduct(new Vector(1, 0, 0)))
                ? new Vector(1, 0, 0)
                : direction.crossProduct(new Vector(0, 1, 0));
    }
}
