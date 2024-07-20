package primitives;


/**
 * Represents a vector in three-dimensional space, extending the functionality of a point with additional vector operations.
 * Provides methods for vector addition, scaling, dot product, cross product, length calculations, and normalization.
 * Crucial for various 3D applications.
 *
 * @author Adir and Meir.
 */
public class Vector extends Point {

    /**
     * Public constructor to initialize vector with three doubles.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     * @param z Z-coordinate
     */
    public Vector(double x, double y, double z) {
        super(x, y, z);
        if (xyz.equals(Double3.ZERO)) {
            throw new IllegalArgumentException("Vector cannot be zero vector");
        }
    }

    /**
     * Package-private constructor to initialize vector from a Double3 object.
     *
     * @param xyz Coordinates object
     */
    public Vector(Double3 xyz) {
        super(xyz);
        if (xyz.equals(Double3.ZERO)) {
            throw new IllegalArgumentException("Vector cannot be zero vector");
        }
    }

    /**
     * Adds another vector to this vector and returns a new vector.
     *
     * @param other The vector to add
     * @return A new vector after addition
     */
    @Override
    public Vector add(Vector other) {
        return new Vector(this.xyz.add(other.xyz));
    }

    /**
     * Scales this vector by a scalar and returns a new vector.
     *
     * @param scalar The scalar to multiply
     * @return A new vector after scaling
     */
    public Vector scale(double scalar) {
        return new Vector(this.xyz.scale(scalar));
    }

    /**
     * Calculates the dot product of this vector with another vector.
     *
     * @param other The other vector
     * @return The dot product
     */
    public double dotProduct(Vector other) {
        return this.xyz.d1 * other.xyz.d1 + this.xyz.d2 * other.xyz.d2 + this.xyz.d3 * other.xyz.d3;
    }

    /**
     * Computes the cross product of this vector with another vector.
     *
     * @param other The vector to cross with
     * @return A new vector perpendicular to both this and rhs vectors
     */
    public Vector crossProduct(Vector other) {
        double x = this.xyz.d2 * other.xyz.d3 - this.xyz.d3 * other.xyz.d2;
        double y = this.xyz.d3 * other.xyz.d1 - this.xyz.d1 * other.xyz.d3;
        double z = this.xyz.d1 * other.xyz.d2 - this.xyz.d2 * other.xyz.d1;
        return new Vector(x, y, z);
    }

    /**
     * Calculates the squared length of this vector.
     *
     * @return The squared length of the vector
     */
    public double lengthSquared() {
        return this.xyz.d1 * this.xyz.d1 + this.xyz.d2 * this.xyz.d2 + this.xyz.d3 * this.xyz.d3;
    }

    /**
     * Calculates the length of this vector.
     *
     * @return The length of the vector
     */
    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * Normalizes this vector to a unit vector.
     *
     * @return A new vector that is a normalized version of this vector
     */
    public Vector normalize() {
        double len = length();
        return new Vector(this.xyz.d1 / len, this.xyz.d2 / len, this.xyz.d3 / len);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return (obj instanceof Vector other)
                && this.xyz.equals(other.xyz);
    }

    @Override
    public String toString() {
        return "v" + super.toString();
    }
}
