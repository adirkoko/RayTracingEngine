package benchmark.core;

import metrics.RenderProgressWriters;
import renderer.Camera;
import renderer.ImageWriter;
import renderer.RenderProgressListener;
import renderer.SimpleRayTracer;
import scene.Scene;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * External benchmark batch runner that drives the rendering engine without becoming part of it.
 */
public final class RenderBatchRunner {

    /**
     * Timestamp format used for unique batch ids.
     */
    private static final DateTimeFormatter BATCH_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /**
     * Runs a complete render batch.
     *
     * @param batch batch definition
     * @return batch result with output locations
     */
    public RenderBatchResult run(RenderBatch batch) {
        String batchId = batch.sceneName() + "__" + BATCH_TIME_FORMAT.format(LocalDateTime.now());
        Path historyDirectory = batch.outputLayout().historyDirectory(batch.suiteName(), batch.sceneName(), batchId);
        Path metricsPath = historyDirectory.resolve("progress.sqlite");
        Path manifestPath = historyDirectory.resolve("manifest.json");
        List<RenderRunResult> runs = new ArrayList<>();

        try {
            Files.createDirectories(historyDirectory);
            writeManifest(batch, batchId, metricsPath, manifestPath, runs, "STARTED");
        } catch (IOException e) {
            throw new IllegalStateException("Could not create render batch history under " + historyDirectory, e);
        }

        RenderProgressListener metrics = RenderProgressListener.resilient(
                RenderProgressWriters.sqlite(metricsPath),
                failure -> System.err.println("Metrics warning: " + failure.getMessage()));

        try {
            for (RenderProfile profile : batch.profiles()) {
                long started = System.nanoTime();
                try {
                    runs.add(runProfile(batch, batchId, metricsPath, metrics, profile, started));
                    writeManifest(batch, batchId, metricsPath, manifestPath, runs, "RUNNING");
                } catch (RuntimeException e) {
                    runs.add(failedRun(batch, batchId, metricsPath, profile, e, elapsedMillis(started)));
                    writeManifest(batch, batchId, metricsPath, manifestPath, runs, "FAILED");
                    throw e;
                }
            }
            writeManifest(batch, batchId, metricsPath, manifestPath, runs, "DONE");
        } catch (RuntimeException e) {
            writeManifest(batch, batchId, metricsPath, manifestPath, runs, "FAILED");
            throw e;
        } finally {
            metrics.close();
        }

        return new RenderBatchResult(batchId, metricsPath, manifestPath, List.copyOf(runs));
    }

    /**
     * Runs one profile inside the batch.
     *
     * @param batch       batch definition
     * @param batchId     unique batch id
     * @param metricsPath SQLite metrics path
     * @param metrics     progress listener shared by the batch
     * @param profile     profile to render
     * @param started     run start timestamp in nanoseconds
     * @return render run result
     */
    private RenderRunResult runProfile(
            RenderBatch batch,
            String batchId,
            Path metricsPath,
            RenderProgressListener metrics,
            RenderProfile profile,
            long started) {
        String renderId = renderId(batchId, profile);
        String imageName = imageName(batch, batchId, profile);

        Scene scene = batch.sceneFactory().createScene(profile.accelerationType());
        Camera.Builder builder = batch.cameraSpec().newBuilder()
                .setImageWriter(new ImageWriter(imageName, batch.imageWidth(), batch.imageHeight()))
                .setRayTracer(new SimpleRayTracer(scene))
                .setProgressListener(metrics)
                .setProgressIntervalPercent(batch.progressIntervalPercent())
                .setRenderIdSupplier(() -> renderId);

        profile.applyTo(builder);
        builder.build().renderImage().writeToImage();
        return new RenderRunResult(
                profile.name(),
                renderId,
                imageName + ".png",
                metricsPath.toString(),
                "DONE",
                scene.geometries.getResolvedAccelerationType(),
                elapsedMillis(started),
                null);
    }

    /**
     * Creates a failed run result.
     *
     * @param batch         batch definition
     * @param batchId       unique batch id
     * @param metricsPath   SQLite metrics path
     * @param profile       failed profile
     * @param failure       render failure
     * @param elapsedMillis elapsed time before failure
     * @return failed run result
     */
    private static RenderRunResult failedRun(
            RenderBatch batch,
            String batchId,
            Path metricsPath,
            RenderProfile profile,
            RuntimeException failure,
            long elapsedMillis) {
        return new RenderRunResult(
                profile.name(),
                renderId(batchId, profile),
                imageName(batch, batchId, profile) + ".png",
                metricsPath.toString(),
                "FAILED",
                null,
                elapsedMillis,
                failure.getMessage());
    }

    /**
     * Builds a stable render id.
     *
     * @param batchId unique batch id
     * @param profile render profile
     * @return render id
     */
    private static String renderId(String batchId, RenderProfile profile) {
        return batchId + "__" + profile.name();
    }

    /**
     * Builds an image name relative to the default image output directory.
     *
     * @param batch   batch definition
     * @param batchId unique batch id
     * @param profile render profile
     * @return image name without extension
     */
    private static String imageName(RenderBatch batch, String batchId, RenderProfile profile) {
        return batch.outputLayout().imageName(batch.suiteName(), batch.sceneName(), batchId, profile);
    }

    /**
     * Calculates elapsed milliseconds since a {@link System#nanoTime()} timestamp.
     *
     * @param started start timestamp in nanoseconds
     * @return elapsed milliseconds
     */
    private static long elapsedMillis(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    /**
     * Writes a JSON manifest that connects profiles, render ids, image names, and metrics.
     *
     * @param batch        batch definition
     * @param batchId      unique batch id
     * @param metricsPath  SQLite metrics path
     * @param manifestPath manifest path
     * @param runs         completed runs so far
     * @param status       batch status
     */
    private static void writeManifest(
            RenderBatch batch,
            String batchId,
            Path metricsPath,
            Path manifestPath,
            List<RenderRunResult> runs,
            String status) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        appendField(json, 1, "batchId", batchId, true);
        appendField(json, 1, "suite", batch.suiteName(), true);
        appendField(json, 1, "scene", batch.sceneName(), true);
        appendField(json, 1, "status", status, true);
        appendField(json, 1, "imageWidth", batch.imageWidth(), true);
        appendField(json, 1, "imageHeight", batch.imageHeight(), true);
        appendField(json, 1, "progressIntervalPercent", batch.progressIntervalPercent(), true);
        appendField(json, 1, "metricsDatabase", metricsPath.toString(), true);
        appendProfiles(json, batch.profiles());
        json.append(",\n");
        appendRuns(json, runs);
        json.append("\n}\n");

        try {
            Files.writeString(manifestPath, json.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write render batch manifest " + manifestPath, e);
        }
    }

    /**
     * Appends the profile array to the manifest.
     *
     * @param json     manifest builder
     * @param profiles profiles
     */
    private static void appendProfiles(StringBuilder json, List<RenderProfile> profiles) {
        indent(json, 1).append("\"profiles\": [");
        for (int i = 0; i < profiles.size(); i++) {
            RenderProfile profile = profiles.get(i);
            json.append(i == 0 ? "\n" : ",\n");
            indent(json, 2).append("{\n");
            appendField(json, 3, "name", profile.name(), true);
            appendField(json, 3, "threadsCount", profile.threadsCount(), true);
            appendField(json, 3, "sampleSize", profile.sampleSize(), true);
            appendField(json, 3, "adaptiveSampling", profile.adaptiveSampling(), true);
            appendField(json, 3, "apertureRadius", profile.apertureRadius(), true);
            appendField(json, 3, "focalDistance", profile.focalDistance(), true);
            appendField(json, 3, "accelerationType", profile.accelerationType().name(), false);
            indent(json, 2).append("}");
        }
        json.append("\n");
        indent(json, 1).append("]");
    }

    /**
     * Appends completed run results to the manifest.
     *
     * @param json manifest builder
     * @param runs run results
     */
    private static void appendRuns(StringBuilder json, List<RenderRunResult> runs) {
        indent(json, 1).append("\"runs\": [");
        for (int i = 0; i < runs.size(); i++) {
            RenderRunResult run = runs.get(i);
            json.append(i == 0 ? "\n" : ",\n");
            indent(json, 2).append("{\n");
            appendField(json, 3, "profile", run.profileName(), true);
            appendField(json, 3, "renderId", run.renderId(), true);
            appendField(json, 3, "image", run.imageName(), true);
            appendField(json, 3, "metricsDatabase", run.metricsPath(), true);
            appendField(json, 3, "status", run.status(), true);
            if (run.resolvedAccelerationType() != null)
                appendField(json, 3, "resolvedAccelerationType", run.resolvedAccelerationType().name(), true);
            appendField(json, 3, "elapsedMillis", run.elapsedMillis(), run.error() != null);
            if (run.error() != null) appendField(json, 3, "error", run.error(), false);
            indent(json, 2).append("}");
        }
        json.append("\n");
        indent(json, 1).append("]");
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
     * @return the manifest builder
     */
    private static StringBuilder indent(StringBuilder json, int level) {
        return json.append("  ".repeat(level));
    }

    /**
     * Escapes a JSON string value.
     *
     * @param value raw value
     * @return escaped value
     */
    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
