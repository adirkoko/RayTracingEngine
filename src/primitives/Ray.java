package primitives;

/**
 * Represents a ray in three-dimensional space defined by a starting point (head) and a direction vector.
 * The direction vector is always normalized.
 *
 * @author Adir and Meir.
 */
public class Ray {

    /**
     * The starting point of the Ray.
     */
    private final Point head;

    /**
     * The direction of the Ray.
     */
    private final Vector direction;

    /**
     * Constructor for Ray that accepts a starting point and a direction vector.
     *
     * @param head      The start point of the ray
     * @param direction The direction vector of the ray
     */
    public Ray(Point head, Vector direction) {
        this.head = head;
        this.direction = direction.normalize();
    }

    /**
     * Returns the starting point of the ray.
     *
     * @return starting point of ray.
     */
    public Point getHead() {
        return head;
    }

    /**
     * Returns the directional vector of the ray.
     *
     * @return directional vector of ray.
     */
    public Vector getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return (obj instanceof Ray other)
                && this.head.equals(other.head)
                && this.direction.equals(other.direction);
    }

    @Override
    public String toString() {
        return head + "->" + direction;
    }

    /**
     * Calculates a point on the ray at a given distance from the ray's origin.
     *
     * @param t the distance from the origin along the direction vector
     * @return the point at the distance t from the origin
     */
    public Point getPoint(double t) {
        return head.add(direction.scale(t));
    }

}
