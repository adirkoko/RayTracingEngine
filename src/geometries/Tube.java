package geometries;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents a tube in 3D space, defined by a radius and a central axis ray.
 *
 * @author Adir and Meir.
 */
public class Tube extends RadialGeometry {

    /**
     * The Ray in the center of the tube.
     */
    protected final Ray axisRay;

    /**
     * Constructor for Tube.
     * @param axisRay The central axis of the tube.
     * @param radius The radius of the tube.
     */
    public Tube(Ray axisRay, double radius) {
        super(radius);
        this.axisRay = axisRay;
    }

    @Override
    public Vector getNormal(Point point) {
        return null; // Will be implemented later
    }
}
