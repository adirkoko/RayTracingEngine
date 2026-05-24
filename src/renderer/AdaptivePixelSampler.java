package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

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
        double halfWidth = width / 2;
        double halfHeight = height / 2;

        Color centerColor = trace(center);
        Color topLeft = trace(moveOnViewPlane(center, -halfWidth, halfHeight));
        Color topRight = trace(moveOnViewPlane(center, halfWidth, halfHeight));
        Color bottomLeft = trace(moveOnViewPlane(center, -halfWidth, -halfHeight));
        Color bottomRight = trace(moveOnViewPlane(center, halfWidth, -halfHeight));

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
                sample(moveOnViewPlane(center, -quarterWidth, quarterHeight), halfWidth, halfHeight, depth - 1),
                sample(moveOnViewPlane(center, quarterWidth, quarterHeight), halfWidth, halfHeight, depth - 1),
                sample(moveOnViewPlane(center, -quarterWidth, -quarterHeight), halfWidth, halfHeight, depth - 1),
                sample(moveOnViewPlane(center, quarterWidth, -quarterHeight), halfWidth, halfHeight, depth - 1)
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
}
