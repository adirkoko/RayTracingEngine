package benchmark.scenes;

import lighting.LightSample;
import lighting.LightSource;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Benchmark-only rectangular area light that exposes deterministic light samples.
 */
final class SampledAreaLight implements LightSource {

    /**
     * Light intensity before distance attenuation.
     */
    private final Color intensity;

    /**
     * Deterministic sample positions across the light surface.
     */
    private final List<Point> samples;

    /**
     * Linear attenuation factor.
     */
    private final double kL;

    /**
     * Quadratic attenuation factor.
     */
    private final double kQ;

    /**
     * Creates a sampled rectangular area light.
     *
     * @param intensity  light intensity
     * @param center     area center
     * @param right      area right direction
     * @param up         area up direction
     * @param width      area width
     * @param height     area height
     * @param sampleSize number of samples along each side
     * @param kL         linear attenuation factor
     * @param kQ         quadratic attenuation factor
     */
    SampledAreaLight(
            Color intensity,
            Point center,
            Vector right,
            Vector up,
            double width,
            double height,
            int sampleSize,
            double kL,
            double kQ) {
        if (sampleSize <= 0)
            throw new IllegalArgumentException("Area light sample size must be positive");
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException("Area light dimensions must be positive");
        this.intensity = intensity;
        this.samples = createSamples(center, right.normalize(), up.normalize(), width, height, sampleSize);
        this.kL = kL;
        this.kQ = kQ;
    }

    @Override
    public Color getIntensity(Point point) {
        double distance = getDistance(point);
        return attenuatedIntensity(distance);
    }

    @Override
    public Vector getL(Point point) {
        return point.subtract(samples.getFirst()).normalize();
    }

    @Override
    public double getDistance(Point point) {
        return samples.getFirst().distance(point);
    }

    @Override
    public List<LightSample> getSamples(Point point) {
        List<LightSample> lightSamples = new ArrayList<>();
        for (Point sample : samples) {
            double distance = sample.distance(point);
            lightSamples.add(new LightSample(
                    attenuatedIntensity(distance),
                    point.subtract(sample).normalize(),
                    distance));
        }
        return lightSamples;
    }

    /**
     * Calculates distance attenuation.
     *
     * @param distance distance from light sample
     * @return attenuated intensity
     */
    private Color attenuatedIntensity(double distance) {
        return intensity.scale(1 / (1 + kL * distance + kQ * distance * distance));
    }

    /**
     * Creates deterministic sample positions over a rectangular area.
     *
     * @param center     area center
     * @param right      normalized right axis
     * @param up         normalized up axis
     * @param width      area width
     * @param height     area height
     * @param sampleSize sample count per side
     * @return sample positions
     */
    private static List<Point> createSamples(
            Point center,
            Vector right,
            Vector up,
            double width,
            double height,
            int sampleSize) {
        List<Point> samples = new ArrayList<>();
        double cellWidth = width / sampleSize;
        double cellHeight = height / sampleSize;

        for (int y = 0; y < sampleSize; y++) {
            for (int x = 0; x < sampleSize; x++) {
                double rightOffset = (x - (sampleSize - 1) / 2.0) * cellWidth;
                double upOffset = (y - (sampleSize - 1) / 2.0) * cellHeight;
                samples.add(offset(center, right, rightOffset, up, upOffset));
            }
        }

        return samples;
    }

    /**
     * Applies two optional vector offsets without constructing zero vectors.
     *
     * @param point       base point
     * @param right       first axis
     * @param rightOffset first offset
     * @param up          second axis
     * @param upOffset    second offset
     * @return shifted point
     */
    private static Point offset(Point point, Vector right, double rightOffset, Vector up, double upOffset) {
        if (rightOffset != 0) point = point.add(right.scale(rightOffset));
        if (upOffset != 0) point = point.add(up.scale(upOffset));
        return point;
    }
}
