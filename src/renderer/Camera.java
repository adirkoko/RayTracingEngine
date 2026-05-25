package renderer;

import primitives.*;
import sampling.DiskSampler;
import sampling.JitteredSampler;
import sampling.Sample2D;

import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.UUID;
import java.util.function.Supplier;

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
    private JitteredSampler jitteredSampler;

    /**
     * Disk sampler for lens aperture sampling.
     */
    private DiskSampler diskSampler;

    /**
     * Radius of the camera aperture. A zero radius keeps pinhole-camera behavior.
     */
    private double apertureRadius = 0;

    /**
     * Number of aperture samples along each logical lens axis.
     * A zero value inherits the pixel sample size to keep existing behavior.
     */
    private int apertureSampleSize = 0;

    /**
     * Distance from the camera to the focal plane along the forward axis.
     */
    private double focalDistance = 0;

    /**
     * Adaptive sampler for pixel color refinement.
     */
    private AdaptivePixelSampler adaptivePixelSampler;

    /**
     * Listener for render progress events.
     */
    private RenderProgressListener progressListener = RenderProgressListener.CONSOLE;

    /**
     * Progress report interval in percent.
     */
    private double progressIntervalPercent = 0.1;

    /**
     * Current render run identifier.
     */
    private String renderId;

    /**
     * Supplies a new render identifier for each render run.
     */
    private Supplier<String> renderIdSupplier = () -> UUID.randomUUID().toString();

    /**
     * Current render run start timestamp.
     */
    private long renderStartedMillis;

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

        // Initialize jittered sampler if it is null or sample size has changed
        if (jitteredSampler == null || jitteredSampler.getSampleSize() != sampleSize)
            jitteredSampler = new JitteredSampler(sampleSize, pixelWidth, pixelHeight);

        return constructRaysFromSamples(pIJ, jitteredSampler.getSamples());
    }

    /**
     * Constructs rays through a pixel from sample offsets.
     *
     * @param pixelCenter pixel center on the view plane
     * @param samples     sample offsets
     * @return list of rays through the samples
     */
    private List<Ray> constructRaysFromSamples(Point pixelCenter, List<Sample2D> samples) {
        List<Ray> rays = new LinkedList<>();
        List<Sample2D> lensSamples = isDepthOfFieldEnabled() ? getLensSamples() : List.of();
        int lensIndex = 0;

        for (Sample2D sample : samples) {
            Point samplePoint = pixelCenter;
            if (!isZero(sample.x())) samplePoint = samplePoint.add(right.scale(sample.x()));
            if (!isZero(sample.y())) samplePoint = samplePoint.add(up.scale(sample.y()));
            rays.add(constructRayThroughViewPlanePoint(
                    samplePoint,
                    lensSamples.isEmpty() ? null : lensSamples.get(lensIndex++ % lensSamples.size())));
        }

        return rays;
    }

    /**
     * Constructs rays through a single view-plane point using the configured lens model.
     *
     * @param point point on the view plane
     * @return rays through the point
     */
    private List<Ray> constructRaysThroughViewPlanePoint(Point point) {
        if (!isDepthOfFieldEnabled()) return List.of(constructRayThroughViewPlanePoint(point, null));

        List<Ray> rays = new LinkedList<>();
        for (Sample2D lensSample : getLensSamples())
            rays.add(constructRayThroughViewPlanePoint(point, lensSample));
        return rays;
    }

    /**
     * Constructs one ray through a view-plane point, either from the camera position or through the sampled aperture.
     *
     * @param point      point on the view plane
     * @param lensSample aperture offset, or null for pinhole behavior
     * @return the constructed ray
     */
    private Ray constructRayThroughViewPlanePoint(Point point, Sample2D lensSample) {
        if (!isDepthOfFieldEnabled() || lensSample == null)
            return new Ray(position, point.subtract(position));

        Point lensPoint = moveByCameraAxes(position, lensSample.x(), lensSample.y());
        return new Ray(lensPoint, constructFocalPoint(point).subtract(lensPoint));
    }

    /**
     * Calculates the point where a pinhole ray hits the focal plane.
     *
     * @param viewPlanePoint point on the view plane
     * @return matching point on the focal plane
     */
    private Point constructFocalPoint(Point viewPlanePoint) {
        Vector direction = viewPlanePoint.subtract(position).normalize();
        return position.add(direction.scale(focalDistance / alignZero(direction.dotProduct(toward))));
    }

    /**
     * Moves a point by camera-local right/up offsets.
     *
     * @param point   starting point
     * @param rightDx offset along the camera right vector
     * @param upDy    offset along the camera up vector
     * @return moved point
     */
    private Point moveByCameraAxes(Point point, double rightDx, double upDy) {
        Point movedPoint = point;
        if (!isZero(rightDx)) movedPoint = movedPoint.add(right.scale(rightDx));
        if (!isZero(upDy)) movedPoint = movedPoint.add(up.scale(upDy));
        return movedPoint;
    }

    /**
     * Gets cached aperture samples for the current camera configuration.
     *
     * @return aperture samples
     */
    private List<Sample2D> getLensSamples() {
        int lensSampleSize = lensSampleSize();
        if (diskSampler == null
                || diskSampler.getSampleSize() != lensSampleSize
                || !isZero(diskSampler.getRadius() - apertureRadius))
            diskSampler = new DiskSampler(lensSampleSize, apertureRadius);
        return diskSampler.getSamples();
    }

    /**
     * Gets the effective aperture sample size.
     *
     * @return configured aperture sample size, or pixel sample size for backward compatibility
     */
    private int lensSampleSize() {
        return apertureSampleSize > 0 ? apertureSampleSize : sampleSize;
    }

    /**
     * Checks whether depth of field is enabled.
     *
     * @return true when the aperture has positive radius
     */
    private boolean isDepthOfFieldEnabled() {
        return apertureRadius > 0;
    }

    /**
     * Traces a view-plane point through the configured camera lens.
     *
     * @param point point on the view plane
     * @return averaged color for the point
     */
    private Color traceViewPlanePoint(Point point) {
        List<Ray> rays = constructRaysThroughViewPlanePoint(point);
        Color color = Color.BLACK;
        for (Ray ray : rays) color = color.add(rayTracer.traceRay(ray));
        return color.reduce(rays.size());
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
            pixelColor = adaptivePixelSampler.sample(
                    constructPixelCenter(nX, nY, j, i),
                    viewPlaneWidth / nX,
                    viewPlaneHeight / nY,
                    maxAdaptiveDepth);
        } else {
            List<Ray> rays;
            if (sampleSize <= 1 && !isDepthOfFieldEnabled()) {
                rays = List.of(constructRay(nX, nY, j, i));
            } else if (sampleSize <= 1) {
                rays = constructRaysThroughViewPlanePoint(constructPixelCenter(nX, nY, j, i));
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
        ensureRenderRun();
        long stageStartedMillis = System.currentTimeMillis();
        reportProgress(RenderStage.WRITE_IMAGE, 0, 1, stageStartedMillis);
        try {
            imageWriter.writeToImage();
        } catch (RuntimeException e) {
            reportFailureProgress(e, stageStartedMillis);
            throw e;
        }
        reportProgress(RenderStage.WRITE_IMAGE, 1, 1, stageStartedMillis);
        reportTerminalProgress(RenderStage.DONE, 1, 1, renderStartedMillis);
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
     * The progress of the rendering is tracked using the `PixelManager`, which emits render progress events.
     *
     * @return The current `Camera` instance.
     */
    public Camera renderImage() {
        final int nX = imageWriter.getNx();
        final int nY = imageWriter.getNy();

        beginRenderRun();

        // Initialize pixel manager with progress reporting interval
        pixelManager = new PixelManager(
                nY,
                nX,
                progressIntervalPercent,
                renderId,
                renderStartedMillis,
                System.currentTimeMillis(),
                progressListener);
        adaptivePixelSampler = new AdaptivePixelSampler(right, up, this::traceViewPlanePoint, ADAPTIVE_COLOR_TOLERANCE);

        try {
            if (threadsCount == 0) {  // No multi-threading
                for (int i = 0; i < nY; i++) {
                    for (int j = 0; j < nX; j++) {
                        castRay(nX, nY, j, i);
                    }
                }
            } else {
                var threads = new LinkedList<Thread>();
                int threadsToStart = threadsCount;
                while (threadsToStart-- > 0) {
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
                    Thread.currentThread().interrupt();
                }
            }
            pixelManager.finish();
        } catch (RuntimeException e) {
            reportFailureProgress(e, renderStartedMillis);
            throw e;
        }

        return this;
    }

    /**
     * Starts a new render run.
     */
    private void beginRenderRun() {
        renderId = renderIdSupplier.get();
        if (renderId == null || renderId.isBlank())
            throw new IllegalStateException("Render id cannot be null or blank");
        renderStartedMillis = System.currentTimeMillis();
    }

    /**
     * Ensures a render run exists before reporting a stage.
     */
    private void ensureRenderRun() {
        if (renderId == null) beginRenderRun();
    }

    /**
     * Reports progress for a render lifecycle stage.
     *
     * @param stage              render stage
     * @param completedWork      completed stage work
     * @param totalWork          total stage work
     * @param stageStartedMillis stage start timestamp
     */
    private void reportProgress(RenderStage stage, long completedWork, long totalWork, long stageStartedMillis) {
        long now = System.currentTimeMillis();
        progressListener.onProgress(new RenderProgress(
                renderId,
                stage,
                completedWork,
                totalWork,
                totalWork == 0 ? 100 : completedWork * 100.0 / totalWork,
                now - renderStartedMillis,
                now - stageStartedMillis,
                now));
    }

    /**
     * Reports a terminal progress event and releases listener resources.
     *
     * @param stage              terminal render stage
     * @param completedWork      completed stage work
     * @param totalWork          total stage work
     * @param stageStartedMillis stage start timestamp
     */
    private void reportTerminalProgress(RenderStage stage, long completedWork, long totalWork, long stageStartedMillis) {
        try {
            reportProgress(stage, completedWork, totalWork, stageStartedMillis);
        } finally {
            progressListener.close();
        }
    }

    /**
     * Reports failed render progress without hiding the original render failure.
     *
     * @param failure            original render failure
     * @param stageStartedMillis stage start timestamp
     */
    private void reportFailureProgress(RuntimeException failure, long stageStartedMillis) {
        try {
            reportTerminalProgress(RenderStage.FAILED, 0, 1, stageStartedMillis);
        } catch (RuntimeException progressFailure) {
            failure.addSuppressed(progressFailure);
        }
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

            if (camera.apertureRadius > 0 && camera.focalDistance == 0)
                throw new MissingResourceException(
                        "Missing depth of field focal distance",
                        Camera.class.getName(),
                        "setFocalDistance");

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
         * Sets a render progress listener.
         *
         * @param progressListener listener that receives render progress events
         * @return the Builder instance
         */
        public Builder setProgressListener(RenderProgressListener progressListener) {
            if (progressListener == null)
                throw new IllegalArgumentException("Progress listener cannot be null");
            camera.progressListener = progressListener;
            return this;
        }

        /**
         * Sets the supplier used to create render identifiers.
         *
         * @param renderIdSupplier supplier that returns a non-blank render id for each render run
         * @return the Builder instance
         */
        public Builder setRenderIdSupplier(Supplier<String> renderIdSupplier) {
            if (renderIdSupplier == null)
                throw new IllegalArgumentException("Render id supplier cannot be null");
            camera.renderIdSupplier = renderIdSupplier;
            return this;
        }

        /**
         * Sets the progress report interval.
         *
         * @param progressIntervalPercent interval in percent; zero reports only stage start and finish
         * @return the Builder instance
         */
        public Builder setProgressIntervalPercent(double progressIntervalPercent) {
            if (progressIntervalPercent < 0)
                throw new IllegalArgumentException("Progress interval cannot be negative");
            camera.progressIntervalPercent = progressIntervalPercent;
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
         * Sets the radius of the camera aperture for depth of field.
         *
         * @param apertureRadius aperture radius; zero keeps pinhole behavior
         * @return the Builder instance
         */
        public Builder setApertureRadius(double apertureRadius) {
            if (apertureRadius < 0)
                throw new IllegalArgumentException("Aperture radius cannot be negative");
            camera.apertureRadius = apertureRadius;
            return this;
        }

        /**
         * Sets the number of aperture samples along each lens axis.
         * If this is not set, depth of field inherits the pixel sample size to preserve existing behavior.
         *
         * @param apertureSampleSize number of samples along each lens axis
         * @return the Builder instance
         */
        public Builder setApertureSampleSize(int apertureSampleSize) {
            if (apertureSampleSize <= 0)
                throw new IllegalArgumentException("Aperture sample size must be positive");
            camera.apertureSampleSize = apertureSampleSize;
            return this;
        }

        /**
         * Sets the distance from the camera to the focal plane.
         *
         * @param focalDistance focal plane distance along the camera forward axis
         * @return the Builder instance
         */
        public Builder setFocalDistance(double focalDistance) {
            if (focalDistance <= 0)
                throw new IllegalArgumentException("Focal distance must be positive");
            camera.focalDistance = focalDistance;
            return this;
        }

        /**
         * Sets the number of threads for rendering.
         *
         * @param threadsCount The number of threads to use.
         * @return The current Builder instance.
         */
        public Builder setThreadsCount(int threadsCount) {
            if (threadsCount < 0)
                throw new IllegalArgumentException("Thread count cannot be negative");
            camera.threadsCount = threadsCount;
            return this;
        }

    }
}
