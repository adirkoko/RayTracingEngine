package renderer;

import primitives.*;

import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;

import static primitives.Util.*;

/**
 * Represents a camera in a 3D space with the ability to construct rays.
 * This class uses the Builder Design Pattern.
 * Provides methods for setting the camera's position, direction, and view plane properties.
 * Can construct rays through view plane pixels and render images.
 *
 * @author Adir and Meir
 */
public class Camera implements Cloneable {

    /**
     * The number of threads to use for multi-threaded rendering.
     * A value of 0 indicates single-threaded rendering.
     */

    private int threadsCount = 0;

    /**
     * Manages pixel allocation and progress tracking for multi-threaded rendering.
     */
    private PixelManager pixelManager;

    /**
     * Indicates if adaptive sampling is enabled.
     */
    private boolean adaptiveSampling = false;
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
     * The number of samples per pixel for anti-aliasing.
     * Default value for no anti-aliasing.
     */
    private int sampleSize = 1;

    /**
     * Jittered grid for anti-aliasing.
     */
    private JitteredGrid jitteredGrid;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Camera() {
    }

    /**
     * Getter for the position of the camera
     *
     * @return the position of the camera
     */
    @SuppressWarnings("unused")
    public Point getPosition() {
        return position;
    }

    /**
     * Getter for the right direction vector of the camera
     *
     * @return the right direction vector of the camera
     */
    @SuppressWarnings("unused")
    public Vector getRight() {
        return right;
    }

    /**
     * Getter for the upward direction vector of the camera
     *
     * @return the upward direction vector of the camera
     */
    @SuppressWarnings("unused")
    public Vector getUp() {
        return up;
    }

    /**
     * Getter for the direction vector pointing towards the view plane
     *
     * @return the direction vector pointing towards the view plane
     */
    @SuppressWarnings("unused")
    public Vector getToward() {
        return toward;
    }

    /**
     * Getter for the height of the view plane
     *
     * @return the height of the view plane
     */
    @SuppressWarnings("unused")
    public double getViewPlaneHeight() {
        return viewPlaneHeight;
    }

    /**
     * Getter for the width of the view plane
     *
     * @return the width of the view plane
     */
    @SuppressWarnings("unused")
    public double getViewPlaneWidth() {
        return viewPlaneWidth;
    }

    /**
     * Getter for the distance from the camera to the view plane
     *
     * @return the distance from the camera to the view plane
     */
    @SuppressWarnings("unused")
    public double getViewPlaneDistance() {
        return viewPlaneDistance;
    }

    /**
     * The image writer for the camera.
     */
    private ImageWriter imageWriter;

    /**
     * The ray tracer for the camera.
     */
    private RayTracerBase rayTracer;


    /**
     * Static method to get the builder for the Camera class.
     *
     * @return a new instance of the Builder class
     */
    public static Builder getBuilder() {
        return new Builder();
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
        // Calculate the offsets for the pixel in the view plane
        double xOffset = (j - (nX - 1) / 2.0) * (viewPlaneWidth / nX);
        double yOffset = (i - (nY - 1) / 2.0) * (viewPlaneHeight / nY);

        // Start with the center of the view plane
        Point pIJ = position.add(toward.scale(viewPlaneDistance));

        // Adjust the point based on the offsets
        if (!isZero(xOffset)) pIJ = pIJ.add(right.scale(xOffset));
        if (!isZero(yOffset)) pIJ = pIJ.add(up.scale(-yOffset));

        // Return the ray from the camera position through the adjusted point
        return new Ray(position, pIJ.subtract(position));
    }


    /**
     * Constructs jittered rays through the pixel for anti-aliasing.
     *
     * @param nX         number of columns in the view plane
     * @param nY         number of rows in the view plane
     * @param j          column index of the pixel
     * @param i          row index of the pixel
     * @param sampleSize number of samples per pixel
     * @return a list of jittered rays
     */
    private List<Ray> constructJitteredRays(int nX, int nY, int j, int i, int sampleSize) {
        List<Ray> rays = new LinkedList<>();

        // Calculate the pixel size
        double pixelWidth = viewPlaneWidth / nX;
        double pixelHeight = viewPlaneHeight / nY;

        // Calculate the offsets for the pixel in the view plane
        double xOffset = (j - (nX - 1) / 2.0) * pixelWidth;
        double yOffset = (i - (nY - 1) / 2.0) * pixelHeight;

        // Start with the center of the view plane
        Point pIJ = position.add(toward.scale(viewPlaneDistance));

        // Adjust the point based on the offsets
        if (!isZero(xOffset)) pIJ = pIJ.add(right.scale(xOffset));
        if (!isZero(yOffset)) pIJ = pIJ.add(up.scale(-yOffset));

        // Initialize jittered grid if it is null or sample size has changed
        if (jitteredGrid == null || jitteredGrid.getSampleSize() != sampleSize)
            jitteredGrid = new JitteredGrid(sampleSize, pixelWidth, pixelHeight);

        // Generate rays through jittered points using the function in Ray class
        return Ray.generateJitteredRays(position, pIJ, right, up, jitteredGrid.getJitteredPoints());
    }

    /**
     * Constructs a list of rays for adaptive super sampling by checking color similarity.
     * This method generates a list of rays using jittered sampling, traces them to get their colors,
     * and checks if all obtained colors are similar. If the colors are similar, it returns the generated rays;
     * otherwise, it generates rays with the specified sample size.
     *
     * @param nX         Number of columns in the view plane.
     * @param nY         Number of rows in the view plane.
     * @param j          Column index of the pixel.
     * @param i          Row index of the pixel.
     * @param sampleSize Number of samples per pixel.
     * @return A list of rays for the pixel based on adaptive sampling.
     */
    private List<Ray> adaptiveSuperSampling(int nX, int nY, int j, int i, int sampleSize) {
        // Generate rays using jittered sampling
        List<Ray> rays = constructJitteredRays(nX, nY, j, i, 2);
        List<Color> colors = new LinkedList<>();

        // Trace rays and collect colors
        for (Ray ray : rays) colors.add(rayTracer.traceRay(ray));

        // Check if all colors are similar
        boolean colorsEqual = true;
        for (Color color : colors) {
            if (!color.equals(colors.getFirst())) {
                colorsEqual = false;
                break;
            }
        }

        // Return rays based on color similarity
        if (colorsEqual) return rays;
        else return constructJitteredRays(nX, nY, j, i, sampleSize);

    }

    /**
     * Casts rays for a given pixel and writes the resulting color to the image.
     * This method determines the list of rays to cast based on whether adaptive sampling is enabled.
     * It traces each ray to obtain its color and computes the average color for the pixel.
     *
     * @param nX Number of columns in the view plane.
     * @param nY Number of rows in the view plane.
     * @param j  Column index of the pixel.
     * @param i  Row index of the pixel.
     */
    private void castRay(int nX, int nY, int j, int i) {
        List<Ray> rays;

        // Determine the list of rays to cast
        if (adaptiveSampling) {
            rays = adaptiveSuperSampling(nX, nY, j, i, sampleSize);
        } else {
            if (sampleSize <= 1) {
                rays = List.of(constructRay(nX, nY, j, i));
            } else {
                rays = constructJitteredRays(nX, nY, j, i, sampleSize);
            }
        }

        // Compute the average color for the pixel
        Color pixelColor = Color.BLACK;
        for (Ray ray : rays) {
            pixelColor = pixelColor.add(rayTracer.traceRay(ray));
        }

        // Write the pixel color to the image
        imageWriter.writePixel(j, i, pixelColor.reduce(rays.size()));

        // Notify PixelManager that this pixel is done
        pixelManager.pixelDone();
    }


    /**
     * Writes the image to a file using the image writer.
     *
     * @return the current Camera instance
     */
    public Camera writeToImage() {
        imageWriter.writeToImage();
        return this;
    }


    /**
     * Prints a grid on the image with the specified interval and color.
     *
     * @param interval the interval between grid lines.
     * @param color    the color of the grid lines.
     * @return the current Camera instance
     */
    public Camera printGrid(int interval, Color color) {
        int nX = imageWriter.getNx();
        int nY = imageWriter.getNy();

        // Draw vertical lines
        for (int i = 0; i < nY; i += interval) {
            for (int j = 0; j < nX; j++) {
                imageWriter.writePixel(j, i, color);
            }
        }

        // Draw horizontal lines
        for (int j = 0; j < nX; j += interval) {
            for (int i = 0; i < nY; i++) {
                imageWriter.writePixel(j, i, color);
            }
        }

        return this;
    }

    /**
     * Renders the image by casting rays through all the pixels in the view plane and calculating their color.
     * <p>
     * This method supports both single-threaded and multi-threaded rendering:
     * - If `threadsCount` is 0, the rendering is done sequentially.
     * - If `threadsCount` is greater than 0, the rendering is performed in parallel using multiple threads.
     * <p>
     * The progress of the rendering is tracked using the `PixelManager`, which can print the progress percentage to the console.
     *
     * @return The current `Camera` instance.
     */
    public Camera renderImage() {
        final int nX = imageWriter.getNx();
        final int nY = imageWriter.getNy();

        // Initialize pixel manager with progress print interval
        pixelManager = new PixelManager(nY, nX, 0.1);

        if (threadsCount == 0) {  // No multi-threading
            for (int i = 0; i < nY; i++) {
                for (int j = 0; j < nX; j++) {
                    castRay(nX, nY, j, i);
                }
            }
        } else {
            var threads = new LinkedList<Thread>();
            while (threadsCount-- > 0) {
                threads.add(new Thread(() -> {
                    PixelManager.Pixel pixel;
                    while ((pixel = pixelManager.nextPixel()) != null) {
                        castRay(nX, nY, pixel.col(), pixel.row());
                    }
                }));
            }
            for (var thread : threads) thread.start();
            try {
                for (var thread : threads) thread.join();
            } catch (InterruptedException ignore) {
            }
        }

        return this;
    }


    /**
     * Builder class for constructing a Camera object.
     * Provides methods for setting camera properties and building the Camera object.
     * Implements the Builder Design Pattern.
     *
     * @author Adir and Meir
     */
    public static class Builder {

        /**
         * The Camera instance being built.
         */
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
            if (position == null)
                throw new IllegalArgumentException("Position cannot be null");
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
            if (toward == null || up == null || !isZero(toward.dotProduct(up)))
                throw new IllegalArgumentException("Invalid direction vectors");

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
            if (width <= 0 || height <= 0)
                throw new IllegalArgumentException("View plane dimensions must be positive");
            camera.viewPlaneWidth = width;
            camera.viewPlaneHeight = height;
            return this;
        }

        /**
         * Sets the distance from the camera to the view plane.
         *
         * @param distance the distance to set.
         * @return the Builder instance.
         */
        public Builder setVpDistance(double distance) {
            if (distance <= 0)
                throw new IllegalArgumentException("View plane distance must be positive");

            camera.viewPlaneDistance = distance;
            return this;
        }

        /**
         * Builds and returns the Camera object.
         *
         * @return the constructed Camera object.
         * @throws MissingResourceException if any required field is missing.
         */
        public Camera build() {
            if (camera.position == null || camera.toward == null || camera.up == null ||
                    camera.viewPlaneHeight == 0 || camera.viewPlaneWidth == 0 || camera.viewPlaneDistance == 0 ||
                    camera.imageWriter == null || camera.rayTracer == null)
                throw new MissingResourceException("Missing rendering data", Camera.class.getName(), "Camera fields");

            // Calculate the right vector if it's not already calculated
            if (camera.right == null)
                camera.right = camera.toward.crossProduct(camera.up).normalize();

            try {
                return (Camera) camera.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError("Failed to clone the camera object", e);
            }
        }

        /**
         * Sets the image writer for the camera.
         *
         * @param imageWriter the image writer to set.
         * @return the Builder instance.
         */
        public Builder setImageWriter(ImageWriter imageWriter) {
            if (imageWriter == null)
                throw new IllegalArgumentException("ImageWriter cannot be null");

            camera.imageWriter = imageWriter;
            return this;
        }

        /**
         * Sets the ray tracer for the camera.
         *
         * @param rayTracer the ray tracer to set.
         * @return the Builder instance.
         */
        public Builder setRayTracer(RayTracerBase rayTracer) {
            if (rayTracer == null) throw new IllegalArgumentException("RayTracer cannot be null");
            camera.rayTracer = rayTracer;
            return this;
        }


        /**
         * Sets the sample size per pixel for anti-aliasing (N * N).
         *
         * @param sampleSize the sample size in a pixel.
         * @return the Builder instance.
         */
        public Builder setSampleSize(int sampleSize) {
            if (sampleSize <= 0)
                throw new IllegalArgumentException("Sample size must be positive");
            camera.sampleSize = sampleSize;
            return this;
        }

        /**
         * Sets the number of samples per pixel for anti-aliasing.
         * Adjusts the sample size to the nearest valid square root value.
         *
         * @param sampleNum the number of samples per pixel.
         * @return the Builder instance.
         */
        public Builder setSampleNum(int sampleNum) {
            if (sampleNum <= 0)
                throw new IllegalArgumentException("Sample number must be positive");
            camera.sampleSize = (int) Math.round(Math.sqrt(sampleNum));
            return this;
        }

        /**
         * Sets whether adaptive sampling is enabled.
         *
         * @param adaptiveSampling true to enable.
         * @return the Builder instance.
         */
        public Builder setAdaptiveSampling(boolean adaptiveSampling) {
            camera.adaptiveSampling = adaptiveSampling;
            return this;
        }

        /**
         * Sets the number of threads for rendering.
         *
         * @param threadsCount The number of threads to use.
         * @return The current Builder instance.
         */
        public Builder setThreadsCount(int threadsCount) {
            camera.threadsCount = threadsCount;
            return this;
        }

    }
}