package renderer;

import geometries.acceleration.AccelerationType;
import primitives.Point;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Writes opt-in JSON summaries for completed or failed render runs.
 * This class keeps JSON formatting details out of {@link Camera}; the camera
 * only collects the render snapshot and decides whether a manifest is requested.
 */
final class RenderManifestWriter {

    /**
     * Don't let anyone instantiate this utility class.
     */
    private RenderManifestWriter() {
    }

    /**
     * Writes a render manifest snapshot to disk.
     *
     * @param path     manifest output path
     * @param snapshot render-run data snapshot
     */
    static void write(Path path, Snapshot snapshot) {
        try {
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.writeString(path, manifest(snapshot), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write render manifest " + path, e);
        }
    }

    /**
     * Immutable render-run data used for manifest serialization.
     *
     * @param renderId                    render identifier
     * @param status                      terminal render status
     * @param error                       error message, or null on success
     * @param imagePath                   PNG output path
     * @param imageWidth                  image width in pixels
     * @param imageHeight                 image height in pixels
     * @param startedMillis               render start timestamp
     * @param finishedMillis              manifest creation timestamp
     * @param renderPixelsElapsedMillis   pixel-rendering elapsed time
     * @param writeImageElapsedMillis     image-writing elapsed time
     * @param location                    camera location
     * @param toward                      camera forward direction
     * @param up                          camera upward direction
     * @param viewPlaneDistance           view-plane distance
     * @param viewPlaneWidth              view-plane width
     * @param viewPlaneHeight             view-plane height
     * @param threadsCount                configured render thread count
     * @param sampleSize                  anti-aliasing sample grid size
     * @param adaptiveSampling            whether adaptive sampling is enabled
     * @param maxAdaptiveDepth            adaptive sampling recursion depth
     * @param apertureRadius              depth-of-field aperture radius
     * @param focalDistance               depth-of-field focal distance
     * @param apertureSampleSize          configured aperture sample grid size
     * @param effectiveApertureSampleSize effective aperture sample grid size
     * @param progressIntervalPercent     progress reporting interval
     * @param requestedAccelerationType   requested acceleration mode
     * @param resolvedAccelerationType    concrete acceleration mode selected for this scene
     * @param sceneName                   scene name
     * @param geometryCount               direct geometry count
     * @param lightCount                  light source count
     */
    record Snapshot(
            String renderId,
            RenderStage status,
            String error,
            String imagePath,
            int imageWidth,
            int imageHeight,
            long startedMillis,
            long finishedMillis,
            long renderPixelsElapsedMillis,
            long writeImageElapsedMillis,
            Point location,
            Point toward,
            Point up,
            double viewPlaneDistance,
            double viewPlaneWidth,
            double viewPlaneHeight,
            int threadsCount,
            int sampleSize,
            boolean adaptiveSampling,
            int maxAdaptiveDepth,
            double apertureRadius,
            double focalDistance,
            int apertureSampleSize,
            int effectiveApertureSampleSize,
            double progressIntervalPercent,
            AccelerationType requestedAccelerationType,
            AccelerationType resolvedAccelerationType,
            String sceneName,
            int geometryCount,
            int lightCount) {
    }

    /**
     * Builds a JSON render manifest.
     *
     * @param snapshot render-run data snapshot
     * @return JSON manifest
     */
    private static String manifest(Snapshot snapshot) {
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        appendField(json, 1, "renderId", snapshot.renderId(), true);
        appendField(json, 1, "status", snapshot.status().name(), true);
        if (snapshot.error() != null) appendField(json, 1, "error", snapshot.error(), true);

        appendImage(json, snapshot);
        json.append(",\n");
        appendTiming(json, snapshot);
        json.append(",\n");
        appendCamera(json, snapshot);
        json.append(",\n");
        appendRenderSettings(json, snapshot);
        json.append(",\n");
        appendAcceleration(json, snapshot);
        json.append(",\n");
        appendSceneSummary(json, snapshot);
        json.append("\n}\n");

        return json.toString();
    }

    /**
     * Appends image output data to a JSON manifest.
     *
     * @param json     manifest builder
     * @param snapshot render-run data snapshot
     */
    private static void appendImage(StringBuilder json, Snapshot snapshot) {
        indent(json, 1).append("\"image\": {\n");
        appendField(json, 2, "path", snapshot.imagePath(), true);
        appendField(json, 2, "width", snapshot.imageWidth(), true);
        appendField(json, 2, "height", snapshot.imageHeight(), true);
        appendField(json, 2, "format", "png", false);
        indent(json, 1).append("}");
    }

    /**
     * Appends timing data to a JSON manifest.
     *
     * @param json     manifest builder
     * @param snapshot render-run data snapshot
     */
    private static void appendTiming(StringBuilder json, Snapshot snapshot) {
        indent(json, 1).append("\"timing\": {\n");
        appendField(json, 2, "startedAt", Instant.ofEpochMilli(snapshot.startedMillis()).toString(), true);
        appendField(json, 2, "finishedAt", Instant.ofEpochMilli(snapshot.finishedMillis()).toString(), true);
        appendField(json, 2, "elapsedMillis", snapshot.finishedMillis() - snapshot.startedMillis(), true);
        appendField(json, 2, "renderPixelsMillis", snapshot.renderPixelsElapsedMillis(), true);
        appendField(json, 2, "writeImageMillis", snapshot.writeImageElapsedMillis(), false);
        indent(json, 1).append("}");
    }

    /**
     * Appends camera geometry to a JSON manifest.
     *
     * @param json     manifest builder
     * @param snapshot render-run data snapshot
     */
    private static void appendCamera(StringBuilder json, Snapshot snapshot) {
        indent(json, 1).append("\"camera\": {\n");
        appendPoint(json, 2, "location", snapshot.location(), true);
        appendPoint(json, 2, "toward", snapshot.toward(), true);
        appendPoint(json, 2, "up", snapshot.up(), true);
        appendField(json, 2, "viewPlaneDistance", snapshot.viewPlaneDistance(), true);
        appendField(json, 2, "viewPlaneWidth", snapshot.viewPlaneWidth(), true);
        appendField(json, 2, "viewPlaneHeight", snapshot.viewPlaneHeight(), false);
        indent(json, 1).append("}");
    }

    /**
     * Appends render settings to a JSON manifest.
     *
     * @param json     manifest builder
     * @param snapshot render-run data snapshot
     */
    private static void appendRenderSettings(StringBuilder json, Snapshot snapshot) {
        indent(json, 1).append("\"renderSettings\": {\n");
        appendField(json, 2, "threadsCount", snapshot.threadsCount(), true);
        appendField(json, 2, "sampleSize", snapshot.sampleSize(), true);
        appendField(json, 2, "adaptiveSampling", snapshot.adaptiveSampling(), true);
        appendField(json, 2, "maxAdaptiveDepth", snapshot.maxAdaptiveDepth(), true);
        appendField(json, 2, "apertureRadius", snapshot.apertureRadius(), true);
        appendField(json, 2, "focalDistance", snapshot.focalDistance(), true);
        appendField(json, 2, "apertureSampleSize", snapshot.apertureSampleSize(), true);
        appendField(json, 2, "effectiveApertureSampleSize", snapshot.effectiveApertureSampleSize(), true);
        appendField(json, 2, "progressIntervalPercent", snapshot.progressIntervalPercent(), false);
        indent(json, 1).append("}");
    }

    /**
     * Appends acceleration data to a JSON manifest.
     *
     * @param json     manifest builder
     * @param snapshot render-run data snapshot
     */
    private static void appendAcceleration(StringBuilder json, Snapshot snapshot) {
        indent(json, 1).append("\"acceleration\": {\n");
        appendField(json, 2, "requested", snapshot.requestedAccelerationType().name(), true);
        appendField(json, 2, "resolved", snapshot.resolvedAccelerationType().name(), false);
        indent(json, 1).append("}");
    }

    /**
     * Appends scene summary data to a JSON manifest.
     *
     * @param json     manifest builder
     * @param snapshot render-run data snapshot
     */
    private static void appendSceneSummary(StringBuilder json, Snapshot snapshot) {
        indent(json, 1).append("\"sceneSummary\": {\n");
        appendField(json, 2, "name", snapshot.sceneName(), true);
        appendField(json, 2, "geometryCount", snapshot.geometryCount(), true);
        appendField(json, 2, "lightCount", snapshot.lightCount(), false);
        indent(json, 1).append("}");
    }

    /**
     * Appends a point/vector object field.
     *
     * @param json        manifest builder
     * @param level       indentation level
     * @param name        field name
     * @param point       point or vector
     * @param appendComma true to append a trailing comma
     */
    private static void appendPoint(StringBuilder json, int level, String name, Point point, boolean appendComma) {
        indent(json, level)
                .append('"').append(escape(name)).append("\": {")
                .append("\"x\": ").append(point.getX()).append(", ")
                .append("\"y\": ").append(point.getY()).append(", ")
                .append("\"z\": ").append(point.getZ()).append('}');
        if (appendComma) json.append(',');
        json.append('\n');
    }

    /**
     * Appends a string JSON field.
     *
     * @param json        manifest builder
     * @param level       indentation level
     * @param name        field name
     * @param value       field value
     * @param appendComma true to append a trailing comma
     */
    private static void appendField(StringBuilder json, int level, String name, String value, boolean appendComma) {
        indent(json, level)
                .append('"').append(escape(name)).append("\": \"")
                .append(escape(value)).append('"');
        if (appendComma) json.append(',');
        json.append('\n');
    }

    /**
     * Appends a numeric JSON field.
     *
     * @param json        manifest builder
     * @param level       indentation level
     * @param name        field name
     * @param value       field value
     * @param appendComma true to append a trailing comma
     */
    private static void appendField(StringBuilder json, int level, String name, Number value, boolean appendComma) {
        indent(json, level).append('"').append(escape(name)).append("\": ").append(value);
        if (appendComma) json.append(',');
        json.append('\n');
    }

    /**
     * Appends a boolean JSON field.
     *
     * @param json        manifest builder
     * @param level       indentation level
     * @param name        field name
     * @param value       field value
     * @param appendComma true to append a trailing comma
     */
    private static void appendField(StringBuilder json, int level, String name, boolean value, boolean appendComma) {
        indent(json, level).append('"').append(escape(name)).append("\": ").append(value);
        if (appendComma) json.append(',');
        json.append('\n');
    }

    /**
     * Appends indentation spaces.
     *
     * @param json  manifest builder
     * @param level indentation level
     * @return manifest builder
     */
    private static StringBuilder indent(StringBuilder json, int level) {
        return json.append("  ".repeat(level));
    }

    /**
     * Escapes a JSON string value.
     *
     * @param value raw value
     * @return escaped JSON value
     */
    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
