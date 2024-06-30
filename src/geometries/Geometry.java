package geometries;

import primitives.*;

/**
 * Abstract class for geometric bodies.
 *
 * @author Adir and Meir.
 */
public abstract class Geometry extends Intersectable {

    /**
     * The emission color of the geometry.
     */
    protected Color emission = Color.BLACK;

    /**
     * Get the emission color of the geometry.
     *
     * @return the emission color
     */
    public Color getEmission() {
        return emission;
    }

    /**
     * Set the emission color of the geometry.
     *
     * @param emission the emission color
     * @return the geometry itself
     */
    public Geometry setEmission(Color emission) {
        this.emission = emission;
        return this;
    }

    /**
     * Calculates the normal vector to the geometry at the specified point.
     *
     * @param point The point on the geometry where the normal is to be calculated.
     * @return The normal vector at the given point.
     */
    public abstract Vector getNormal(Point point);
}