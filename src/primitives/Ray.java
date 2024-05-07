package primitives;

class Ray {
    private Point head;
    private Vector direction;

    /**
     * Constructor for Ray that accepts a starting point and a direction vector.
     * @param head The start point of the ray
     * @param direction The direction vector of the ray
     */
    public Ray(Point head, Vector direction) {
        this.head = head;
        this.direction = direction.normalize();  // Normalize the direction vector
    }

    public Point getOrigin() {
        return head;
    }

    public Vector getDirection() {
        return direction;
    }
}
