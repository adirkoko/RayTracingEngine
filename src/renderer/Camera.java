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
     * Maximum RGB component delta that is treated as visually uniform during adaptive sampling.
     */
    private static final double ADAPTIVE_COLOR_TOLERANCE = 1.0;

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
     * Maximum quadtree subdivision depth for adaptive sampling.
     */
    private int maxAdaptiveDepth = 0;
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
        Point point = constructPixelCenter(nX, nY, j, i);
        return new Ray(position, point.subtract(position));
    }

    /**
     * Calculates the center point of a pixel on the view plane.
     *
     * @param nX number of columns in the view plane
     * @param nY number of rows in the view plane
     * @param j  column index of the pixel
     * @param i  row index of the pixel
     * @return the pixel center point on the view plane
     */
    private Point constructPixelCenter(int nX, int nY, int j, int i) {
        double xOffset = (j - (nX - 1) / 2.0) * (viewPlaneWidth / nX);
        double yOffset = (i - (nY - 1) / 2.0) * (viewPlaneHeight / nY);

        Point pIJ = position.add(toward.scale(viewPlaneDistance));
        if (!isZero(xOffset)) pIJ = pIJ.add(right.scale(xOffset));
        if (!isZero(yOffset)) pIJ = pIJ.add(up.scale(-yOffset));
        return pIJ;
    }

    /**
     * Moves a point on the view plane by local right/up offsets.
     *
     * @param center  starting point on the view plane
     * @param rightDx offset along the camera right vector
     * @param upDy    offset along the camera up vector
     * @return the shifted point
     */
    private Point moveOnViewPlane(Point center, double rightDx, double upDy) {
        Point point = center;
        if (!isZero(rightDx)) point = point.add(right.scale(rightDx));
        if (!isZero(upDy)) point = point.add(up.scale(upDy));
        return point;
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
     * Calculates a pixel color using recursive adaptive super sampling.
     *
     * @param nX number of columns in the view plane
     * @param nY number of rows in the view plane
     * @param j  column index of the pixel
     * @param i  row index of the pixel
     * @return the adaptively sampled pixel color
     */
    private Color adaptiveSuperSampling(int nX, int nY, int j, int i) {
        return adaptiveSuperSampling(
                constructPixelCenter(nX, nY, j, i),
                viewPlaneWidth / nX,
                viewPlaneHeight / nY,
                maxAdaptiveDepth);
    }

    /**
     * Recursively samples a rectangular region on the view plane.
     *
     * @param center center of the sampled region
     * @param width  region width
     * @param height region height
     * @param depth  remaining subdivision depth
     * @return the averaged region color
     */
    private Color adaptiveSuperSampling(Point center, double width, double height, int depth) {
        double halfWidth = width / 2;
        double halfHeight = height / 2;

        Color centerColor = rayTracer.traceRay(new Ray(position, center.subtract(position)));
        Color topLeft = rayTracer.traceRay(new Ray(position, moveOnViewPlane(center, -halfWidth, halfHeight).subtract(position)));
        Color topRight = rayTracer.traceRay(new Ray(position, moveOnViewPlane(center, halfWidth, halfHeight).subtract(position)));
        Color bottomLeft = rayTracer.traceRay(new Ray(position, moveOnViewPlane(center, -halfWidth, -halfHeight).subtract(position)));
        Color bottomRight = rayTracer.traceRay(new Ray(position, moveOnViewPlane(center, halfWidth, -halfHeight).subtract(position)));

        if (depth == 0
                || centerColor.isSimilar(topLeft, ADAPTIVE_COLOR_TOLERANCE)
                && centerColor.isSimilar(topRight, ADAPTIVE_COLOR_TOLERANCE)
                && centerColor.isSimilar(bottomLeft, ADAPTIVE_COLOR_TOLERANCE)
                && centerColor.isSimilar(bottomRight, ADAPTIVE_COLOR_TOLERANCE)) {
            return averageColors(centerColor, topLeft, topRight, bottomLeft, bottomRight);
        }

        double quarterWidth = width / 4;
        double quarterHeight = height / 4;
        return averageColors(
                adaptiveSuperSampling(moveOnViewPlane(center, -quarterWidth, quarterHeight), halfWidth, halfHeight, depth - 1),
                adaptiveSuperSampling(moveOnViewPlane(center, quarterWidth, quarterHeight), halfWidth, halfHeight, depth - 1),
                adaptiveSuperSampling(moveOnViewPlane(center, -quarterWidth, -quarterHeight), halfWidth, halfHeight, depth - 1),
                adaptiveSuperSampling(moveOnViewPlane(center, quarterWidth, -quarterHeight), halfWidth, halfHeight, depth - 1)
        );
    }

    /**
     * Averages several colors.
     *
     * @param colors colors to average
     * @return averaged color
     */
    private Color averageColors(Color... colors) {
        Color sum = Color.BLACK;
        for (Color color : colors) sum = sum.add(color);
        return sum.reduce(colors.length);
    }

    /**
     * Casts rays for a given pixel and writes the resulting color to the image.
     * This method chooses between uniform sampling and recursive adaptive sampling.
     *
     * @param nX Number of columns in the view plane.
     * @param nY Number of rows in the view plane.
     * @param j  Column index of the pixel.
     * @param i  Row index of the pixel.
     */
    private void castRay(int nX, int nY, int j, int i) {
        Color pixelColor;

        // Determine the list of rays to cast
        if (adaptiveSampling) {
            pixelColor = adaptiveSuperSampling(nX, nY, j, i);
        } else {
            List<Ray> rays;
            if (sampleSize <= 1) {
                rays = List.of(constructRay(nX, nY, j, i));
            } else {
                rays = constructJitteredRays(nX, nY, j, i, sampleSize);
            }
            pixelColor = Color.BLACK;
            for (Ray ray : rays) {
                pixelColor = pixelColor.add(rayTracer.traceRay(ray));
            }
            pixelColor = pixelColor.reduce(rays.size());
        }

        // Write the pixel color to the image
        imageWriter.writePixel(j, i, pixelColor);

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
     * This method supports both single-threaded and multi-threaded rendering:
     * - If `threadsCount` is 0, the rendering is done sequentially.
     * - If `threadsCount` is greater than 0, the rendering is performed in parallel using multiple threads.
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
         * Sampling limit setters that are mutually exclusive for one builder.
         */
        private enum SamplingLimit {
            SAMPLE_SIZE,
            SAMPLE_NUM,
            MAX_DEPTH
        }

        /**
         * The Camera instance being built.
         */
        private final Camera camera;

        /**
         * The anti-aliasing boundary parameter selected for this builder.
         */
        private SamplingLimit samplingLimit;

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

            if (camera.adaptiveSampling && samplingLimit == null)
                throw new MissingResourceException(
                        "Missing adaptive sampling limit",
                        Camera.class.getName(),
                        "setMaxDepth, setSampleSize, or setSampleNum");

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
         * Ensures that only one anti-aliasing boundary parameter is configured.
         *
         * @param limit the requested sampling limit type
         */
        private void setSamplingLimit(SamplingLimit limit) {
            if (samplingLimit != null && samplingLimit != limit)
                throw new IllegalStateException("Only one anti-aliasing boundary parameter can be configured");
            samplingLimit = limit;
        }

        /**
         * Converts a uniform sample grid side length to adaptive subdivision depth.
         *
         * @param sampleSize requested sample grid side length
         * @return adaptive recursion depth
         */
        private static int sampleSizeToAdaptiveDepth(int sampleSize) {
            int roundedSize = 1;
            while (roundedSize < sampleSize) roundedSize *= 2;

            int depth = 0;
            while (roundedSize > 1) {
                roundedSize /= 2;
                depth++;
            }
            return depth;
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
            setSamplingLimit(SamplingLimit.SAMPLE_SIZE);
            camera.sampleSize = sampleSize;
            if (camera.adaptiveSampling) camera.maxAdaptiveDepth = sampleSizeToAdaptiveDepth(camera.sampleSize);
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
            setSamplingLimit(SamplingLimit.SAMPLE_NUM);
            camera.sampleSize = (int) Math.ceil(Math.sqrt(sampleNum));
            if (camera.adaptiveSampling) camera.maxAdaptiveDepth = sampleSizeToAdaptiveDepth(camera.sampleSize);
            return this;
        }

        /**
         * Sets whether adaptive sampling is enabled.
         *
         * @param adaptiveSampling true to enable.
         * @return the Builder instance.
         */
        public Builder setAdaptiveSampling(boolean adaptiveSampling) {
            if (!adaptiveSampling && samplingLimit == SamplingLimit.MAX_DEPTH)
                throw new IllegalStateException("setMaxDepth is only valid when adaptive sampling is enabled");
            camera.adaptiveSampling = adaptiveSampling;
            if (adaptiveSampling && (samplingLimit == SamplingLimit.SAMPLE_SIZE || samplingLimit == SamplingLimit.SAMPLE_NUM))
                camera.maxAdaptiveDepth = sampleSizeToAdaptiveDepth(camera.sampleSize);
            if (!adaptiveSampling) camera.maxAdaptiveDepth = 0;
            return this;
        }

        /**
         * Sets the maximum recursive subdivision depth for adaptive sampling.
         *
         * @param maxDepth maximum quadtree subdivision depth
         * @return the Builder instance
         */
        public Builder setMaxDepth(int maxDepth) {
            if (maxDepth < 0)
                throw new IllegalArgumentException("Max depth cannot be negative");
            if (!camera.adaptiveSampling)
                throw new IllegalStateException("setMaxDepth is only valid when adaptive sampling is enabled");
            setSamplingLimit(SamplingLimit.MAX_DEPTH);
            camera.maxAdaptiveDepth = maxDepth;
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
