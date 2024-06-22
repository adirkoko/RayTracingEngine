package renderer;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.MissingResourceException;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * represents a camera in a 3D space with the ability to construct rays.
 * This class uses the Builder Design Pattern.
 *
 * @author Adir and Meir.
 */
public class Camera implements Cloneable {
    /**
     * The position of the camera in 3D space.
     */
    private Point position;

    /**
     * The right direction vector of the camera.
     */
    private Vector right;

    /**
     * The upward direction vector of the camera.
     */
    private Vector up;

    /**
     * The direction vector pointing towards the view plane.
     */
    private Vector toward;

    /**
     * The height of the view plane.
     */
    private double viewPlaneHeight = 0;

    /**
     * The width of the view plane.
     */
    private double viewPlaneWidth = 0;

    /**
     * The distance from the camera to the view plane.
     */
    private double viewPlaneDistance = 0;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Camera() {
    }

    /**
     * @return the position of the camera
     */
    public Point getPosition() {
        return position;
    }

    /**
     * @return the right direction vector of the camera
     */
    public Vector getRight() {
        return right;
    }

    /**
     * @return the upward direction vector of the camera
     */
    public Vector getUp() {
        return up;
    }

    /**
     * @return the direction vector pointing towards the view plane
     */
    public Vector getToward() {
        return toward;
    }

    /**
     * @return the height of the view plane
     */
    public double getViewPlaneHeight() {
        return viewPlaneHeight;
    }

    /**
     * @return the width of the view plane
     */
    public double getViewPlaneWidth() {
        return viewPlaneWidth;
    }

    /**
     * @return the distance from the camera to the view plane
     */
    public double getViewPlaneDistance() {
        return viewPlaneDistance;
    }


    /**
     * Static method to get the builder for the Camera class.
     *
     * @return a new instance of the Builder class
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Constructs a ray from the camera through a pixel in the view plane
     *
     * @param nX number of columns (width) in the view plane
     * @param nY number of rows (height) in the view plane
     * @param j  column index of the pixel
     * @param i  row index of the pixel
     * @return the constructed Ray
     */
    public Ray constructRay(int nX, int nY, int j, int i) {
        // Find the center of the view plane
        Point center = position.add(toward.scale(viewPlaneDistance));

        // Calculate the pixel size
        double pixelWidth = viewPlaneWidth / nX;
        double pixelHeight = viewPlaneHeight / nY;

        // Calculate the offset of the pixel from the center
        double xOffset = (j - (nX - 1) / 2.0) * pixelWidth;
        double yOffset = (i - (nY - 1) / 2.0) * pixelHeight;

        // Calculate the point on the view plane
        Point pIJ = center;
        if (!isZero(xOffset)) {
            pIJ = pIJ.add(right.scale(xOffset));
        }
        if (!isZero(yOffset)) {
            // Minus because up is positive direction
            pIJ = pIJ.add(up.scale(-yOffset));
        }

        // Return the ray from the camera position through the pixel
        return new Ray(position, pIJ.subtract(position));
    }

    /**
     * Builder class for constructing a Camera object.
     *
     * @author Adir and Meir.
     */
    public static class Builder {

        private final Camera camera;

        /**
         * Constructor for the Builder class, initializes a new Camera.
         */
        public Builder() {
            this.camera = new Camera();
        }

        /**
         * Sets the camera position.
         *
         * @param position the position to set
         * @return the Builder instance
         */
        public Builder setLocation(Point position) {
            if (position == null) {
                throw new IllegalArgumentException("Position cannot be null");
            }
            camera.position = position;
            return this;
        }

        /**
         * Sets the camera direction vectors.
         *
         * @param toward the forward direction vector
         * @param up     the upward direction vector
         * @return the Builder instance
         */
        public Builder setDirection(Vector toward, Vector up) {
            if (toward == null || up == null || alignZero(toward.dotProduct(up)) != 0) {
                throw new IllegalArgumentException("Invalid direction vectors");
            }
            camera.toward = toward.normalize();
            camera.up = up.normalize();
            camera.right = camera.toward.crossProduct(camera.up);
            return this;
        }

        /**
         * Sets the view plane size.
         *
         * @param width  the width of the view plane
         * @param height the height of the view plane
         * @return the Builder instance
         */
        public Builder setVpSize(double width, double height) {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("View plane dimensions must be positive");
            }
            camera.viewPlaneWidth = width;
            camera.viewPlaneHeight = height;
            return this;
        }

        /**
         * Sets the distance from the camera to the view plane.
         *
         * @param distance the distance to set
         * @return the Builder instance
         */
        public Builder setVpDistance(double distance) {
            if (distance <= 0) {
                throw new IllegalArgumentException("View plane distance must be positive");
            }
            camera.viewPlaneDistance = distance;
            return this;
        }

        /**
         * Builds and returns the Camera object.
         *
         * @return the constructed Camera object
         */
        public Camera build() {
            if (camera.position == null || camera.toward == null || camera.up == null || camera.viewPlaneHeight == 0 || camera.viewPlaneWidth == 0 || camera.viewPlaneDistance == 0) {
                throw new MissingResourceException("Missing rendering data", Camera.class.getName(), "Camera fields");
            }
            // Calculate the right vector if it's not already calculated
            if (camera.right == null) {
                camera.right = camera.toward.crossProduct(camera.up).normalize();
            }
            try {
                return (Camera) camera.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("Failed to clone the camera object", e);
            }
        }


    }
}
