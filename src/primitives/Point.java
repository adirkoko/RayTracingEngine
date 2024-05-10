package primitives;

import static primitives.Util.isZero;

public class Point {

    protected Double3 xyz;
    public static final Point ZERO = new Point(0, 0, 0);

    /**
     * Constructs a new Point given three double values.
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @param z the z-coordinate of the point
     */
    public Point(double x, double y, double z) {
        xyz = new Double3(x, y, z);
    }

    /**
     * Constructs a new Point from a Double3 object.
     *
     * @param xyz of Double3 object representing the coordinates
     */
    Point(Double3 xyz) {
        this.xyz = xyz;
    }

    /**
     * Subtracts another point from this point and returns a vector.
     *
     * @param other The point to subtract
     * @return Vector from the other point to this point
     */
    public Vector subtract(Point other) {
        return new Vector(this.xyz.subtract(other.xyz));
    }

    /**
     * Adds a vector to this point and returns a new point.
     *
     * @param other The vector to add
     * @return A new point after addition
     */
    public Point add(Vector other) {
        return new Point(this.xyz.add(other.xyz));
    }

    /**
     * Calculates the squared distance between this point and another point.
     *
     * @param other The point to calculate the distance to
     * @return The squared distance between this point and the other point
     */
    public double distanceSquared(Point other) {
        double dx = this.xyz.d1 - other.xyz.d1;
        double dy = this.xyz.d2 - other.xyz.d2;
        double dz = this.xyz.d3 - other.xyz.d3;
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Calculates the distance between this point and another point.
     *
     * @param other The point to calculate the distance to
     * @return The distance between this point and the other point
     */
    public double distance(Point other) {
        return Math.sqrt(distanceSquared(other));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return (obj instanceof Point other)
                && this.xyz.equals(other.xyz);
    }

    @Override
    public String toString() {
        return xyz.toString();
    }
}
