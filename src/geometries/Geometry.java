package geometries;

import primitives.*;

/**
 * Abstract class for geometric bodies.
 * Represents geometric objects that have material properties and emission color.
 * Provides a method for calculating the normal vector at a given point on the geometry.
 *
 * @author Adir and Meir.
 */
public abstract class Geometry extends Intersectable {

    /**
     * The emission color of the geometry.
     */
    protected Color emission = Color.BLACK;

    /**
     * Material properties of the geometry.
     */
    private Material material = new Material();

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
     * Getter for the material properties.
     *
     * @return the material properties
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Setter for the material properties.
     *
     * @param material the material properties
     * @return the Geometry object itself for chaining
     */
    public Geometry setMaterial(Material material) {
        this.material = material;
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