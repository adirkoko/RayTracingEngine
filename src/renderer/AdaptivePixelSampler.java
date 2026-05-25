package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static primitives.Util.isZero;

/**
 * Calculates a pixel color using recursive adaptive super sampling.
 */
class AdaptivePixelSampler {

    /**
     * The camera right vector.
     */
    private final Vector right;

    /**
     * The camera up vector.
     */
    private final Vector up;

    /**
     * Traces a point on the view plane into its rendered color.
     */
    private final Function<Point, Color> viewPlaneTracer;

    /**
     * Maximum RGB component delta treated as visually uniform.
     */
    private final double colorTolerance;

    /**
     * Constructs an adaptive pixel sampler.
     *
     * @param right          camera right vector
     * @param up             camera up vector
     * @param viewPlaneTracer traces a view-plane point into color
     * @param colorTolerance color similarity tolerance
     */
    AdaptivePixelSampler(Vector right, Vector up, Function<Point, Color> viewPlaneTracer, double colorTolerance) {
        this.right = right;
        this.up = up;
        this.viewPlaneTracer = viewPlaneTracer;
        this.colorTolerance = colorTolerance;
    }

    /**
     * Recursively samples a rectangular region on the view plane.
     *
     * @param center center of the sampled region
     * @param width  region width
     * @param height region height
     * @param depth  remaining subdivision depth
     * @return the averaged region color
     */
    Color sample(Point center, double width, double height, int depth) {
        return sample(center, width, height, depth, new HashMap<>());
    }

    /**
     * Recursively samples a rectangular region on the view plane using a render-local color cache.
     *
     * @param center center of the sampled region
     * @param width  region width
     * @param height region height
     * @param depth  remaining subdivision depth
     * @param cache  traced view-plane colors by exact sampled point
     * @return the averaged region color
     */
    private Color sample(Point center, double width, double height, int depth, Map<SamplePoint, Color> cache) {
        double halfWidth = width / 2;
        double halfHeight = height / 2;

        Color centerColor = trace(center, cache);
        Color topLeft = trace(moveOnViewPlane(center, -halfWidth, halfHeight), cache);
        Color topRight = trace(moveOnViewPlane(center, halfWidth, halfHeight), cache);
        Color bottomLeft = trace(moveOnViewPlane(center, -halfWidth, -halfHeight), cache);
        Color bottomRight = trace(moveOnViewPlane(center, halfWidth, -halfHeight), cache);

        if (depth == 0
                || centerColor.isSimilar(topLeft, colorTolerance)
                && centerColor.isSimilar(topRight, colorTolerance)
                && centerColor.isSimilar(bottomLeft, colorTolerance)
                && centerColor.isSimilar(bottomRight, colorTolerance)) {
            return averageColors(centerColor, topLeft, topRight, bottomLeft, bottomRight);
        }

        double quarterWidth = width / 4;
        double quarterHeight = height / 4;
        return averageColors(
                sample(moveOnViewPlane(center, -quarterWidth, quarterHeight), halfWidth, halfHeight, depth - 1, cache),
                sample(moveOnViewPlane(center, quarterWidth, quarterHeight), halfWidth, halfHeight, depth - 1, cache),
                sample(moveOnViewPlane(center, -quarterWidth, -quarterHeight), halfWidth, halfHeight, depth - 1, cache),
                sample(moveOnViewPlane(center, quarterWidth, -quarterHeight), halfWidth, halfHeight, depth - 1, cache)
        );
    }

    /**
     * Traces a ray from the camera position through a point on the view plane.
     *
     * @param point point on the view plane
     * @return traced color
     */
    private Color trace(Point point) {
        return viewPlaneTracer.apply(point);
    }

    /**
     * Traces or reuses a sampled view-plane point.
     *
     * @param point sampled point
     * @param cache traced colors by sampled point
     * @return traced color
     */
    private Color trace(Point point, Map<SamplePoint, Color> cache) {
        return cache.computeIfAbsent(new SamplePoint(point), ignored -> trace(point));
    }

    /**
     * Moves a point on the view plane by local right/up offsets.
     *
     * @param center  starting point on the view plane
     * @param rightDx offset along the camera right vector
     * @param upDy    offset along the camera up vector
     * @return the shifted point
     */
    private Point moveOnViewPlane(Point center, double rightDx, double upDy) {
        Point point = center;
        if (!isZero(rightDx)) point = point.add(right.scale(rightDx));
        if (!isZero(upDy)) point = point.add(up.scale(upDy));
        return point;
    }

    /**
     * Averages several colors.
     *
     * @param colors colors to average
     * @return averaged color
     */
    private Color averageColors(Color... colors) {
        Color sum = Color.BLACK;
        for (Color color : colors) sum = sum.add(color);
        return sum.reduce(colors.length);
    }

    /**
     * Exact sampled view-plane point key for adaptive-sampling cache.
     *
     * @param x point X coordinate
     * @param y point Y coordinate
     * @param z point Z coordinate
     */
    private record SamplePoint(double x, double y, double z) {

        /**
         * Creates a sample key from a point.
         *
         * @param point sampled point
         */
        private SamplePoint(Point point) {
            this(point.getX(), point.getY(), point.getZ());
        }
    }
}
