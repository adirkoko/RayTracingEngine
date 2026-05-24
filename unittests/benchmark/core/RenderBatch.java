package benchmark.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Immutable render batch definition.
 *
 * @param suiteName               benchmark suite identifier
 * @param sceneName               scene identifier
 * @param imageWidth              output image width
 * @param imageHeight             output image height
 * @param progressIntervalPercent progress event interval
 * @param outputLayout            image and history output layout
 * @param cameraSpec              camera configuration shared by all profiles
 * @param sceneFactory            factory creating a fresh scene per profile
 * @param profiles                profiles to render
 */
public record RenderBatch(
        String suiteName,
        String sceneName,
        int imageWidth,
        int imageHeight,
        double progressIntervalPercent,
        BenchmarkOutputLayout outputLayout,
        RenderCameraSpec cameraSpec,
        BenchmarkSceneFactory sceneFactory,
        List<RenderProfile> profiles) {

    /**
     * Validates batch data.
     */
    public RenderBatch {
        if (suiteName == null || suiteName.isBlank())
            throw new IllegalArgumentException("Suite name cannot be null or blank");
        if (sceneName == null || sceneName.isBlank())
            throw new IllegalArgumentException("Scene name cannot be null or blank");
        if (imageWidth <= 0 || imageHeight <= 0)
            throw new IllegalArgumentException("Image dimensions must be positive");
        if (progressIntervalPercent < 0)
            throw new IllegalArgumentException("Progress interval cannot be negative");
        if (outputLayout == null)
            throw new IllegalArgumentException("Output layout cannot be null");
        if (cameraSpec == null)
            throw new IllegalArgumentException("Camera spec cannot be null");
        if (sceneFactory == null)
            throw new IllegalArgumentException("Scene factory cannot be null");
        if (profiles == null || profiles.isEmpty())
            throw new IllegalArgumentException("Render batch must contain at least one profile");

        profiles = List.copyOf(profiles);
        Set<String> names = new HashSet<>();
        for (RenderProfile profile : profiles)
            if (!names.add(profile.name()))
                throw new IllegalArgumentException("Duplicate render profile name " + profile.name());
    }
}
