package primitives;

public class Ray {
    private Point head;
    private Vector direction;

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

    public Point getOrigin() {
        return head;
    }

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
        return "Ray{head= " + head + "direction= " + direction + "}\n";
    }
}
