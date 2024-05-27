package geometries;

import primitives.Point;
import primitives.Vector;

/**
 * Represents a sphere in 3D space.
 *
 * @author Adir and Meir.
 */
public class Sphere extends RadialGeometry {

    /**
     * the center of the Sphere.
     */
    private final Point center;

    /**
     * Constructor for Sphere.
     *
     * @param center The center point of the sphere.
     * @param radius The radius of the sphere.
     */
    public Sphere(Point center, double radius) {
        super(radius);
        this.center = center;
    }

    @Override
    public Vector getNormal(Point point) {
        return null; // Will be implemented later
    }
}
