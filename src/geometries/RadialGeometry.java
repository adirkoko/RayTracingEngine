package geometries;

import primitives.Point;
import primitives.Vector;

/**
 * Abstract class representing radial geometries.
 * Radial geometries are defined by a radius.
 */
public abstract class RadialGeometry implements Geometry {
    protected final double radius;

    /**
     * Constructor for radial geometries. Initializes the geometry with the given radius.
     *
     * @param radius The radius of the radial geometry.
     */
    public RadialGeometry(double radius) {
        this.radius = radius;
    }
}