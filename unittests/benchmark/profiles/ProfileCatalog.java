package benchmark.profiles;

import benchmark.core.RenderProfile;
import geometries.acceleration.AccelerationType;

import java.util.List;

/**
 * Catalog of named benchmark profile sets.
 */
public final class ProfileCatalog {

    /**
     * Thread count used by image-quality comparisons.
     */
    private static final int IMAGE_QUALITY_THREADS = 4;

    /**
     * Acceleration mode kept fixed for image-quality comparisons.
     */
    private static final AccelerationType IMAGE_QUALITY_ACCELERATION = AccelerationType.BVH;

    /**
     * Private constructor to prevent utility class instantiation.
     */
    private ProfileCatalog() {
    }

    /**
     * Profiles for image-quality comparisons.
     * Thread count is fixed because this profile set compares visual output, not CPU scaling.
     *
     * @return render profiles
     */
    public static List<RenderProfile> imageQualityComparisonProfiles() {
        return List.of(
                new RenderProfile("baseline_bvh_t4", IMAGE_QUALITY_THREADS, 1, false, 0, 0,
                        IMAGE_QUALITY_ACCELERATION),
                new RenderProfile("aa3_bvh_t4", IMAGE_QUALITY_THREADS, 3, false, 0, 0,
                        IMAGE_QUALITY_ACCELERATION),
                new RenderProfile("aa5_bvh_t4", IMAGE_QUALITY_THREADS, 5, false, 0, 0,
                        IMAGE_QUALITY_ACCELERATION),
                new RenderProfile("aa3_adaptive_bvh_t4", IMAGE_QUALITY_THREADS, 3, true, 0, 0,
                        IMAGE_QUALITY_ACCELERATION),
                new RenderProfile("aa3_dof_bvh_t4", IMAGE_QUALITY_THREADS, 3, false, 2.0, 150,
                        IMAGE_QUALITY_ACCELERATION));
    }

    /**
     * Baseline acceleration profiles for future render-batch acceleration comparisons.
     *
     * @return render profiles
     */
    public static List<RenderProfile> accelerationBaselineProfiles() {
        return List.of(
                new RenderProfile("baseline_auto_t1", 0, 1, false, 0, 0, AccelerationType.AUTO),
                new RenderProfile("baseline_linear_t1", 0, 1, false, 0, 0, AccelerationType.LINEAR),
                new RenderProfile("baseline_bvh_t1", 0, 1, false, 0, 0, AccelerationType.BVH),
                new RenderProfile("baseline_grid_t1", 0, 1, false, 0, 0, AccelerationType.GRID));
    }
}
