package primitives;

/**
 * Represents a ray in three-dimensional space defined by a starting point (head) and a direction vector.
 * The direction vector is always normalized.
 *
 * @author Adir and Meir.
 */
public class Ray {

    private final Point head;
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
}
