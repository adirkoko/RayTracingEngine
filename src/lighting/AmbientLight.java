package lighting;

import primitives.Color;
import primitives.Double3;

/**
 * Represents the ambient light in the scene.
 * This light is constant and not affected by the distance from the light source.
 *
 * @author Adir and Meir.
 */
public class AmbientLight {
    /**
     * Constant for no ambient light
     */
    public static final AmbientLight NONE = new AmbientLight(Color.BLACK, Double3.ZERO);

    /**
     * The intensity of the ambient light
     */
    private final Color intensity;

    /**
     * Constructor for ambient light with intensity and attenuation factor.
     *
     * @param iA the color of the ambient light
     * @param kA the attenuation factor
     */
    public AmbientLight(Color iA, Double3 kA) {
        this.intensity = iA.scale(kA);
    }

    /**
     * Constructor for ambient light with intensity and attenuation factor as scalar.
     *
     * @param iA the color of the ambient light
     * @param kA the attenuation factor as a double
     */
    public AmbientLight(Color iA, double kA) {
        this.intensity = iA.scale(kA);
    }

    /**
     * Gets the intensity of the ambient light.
     *
     * @return the intensity
     */
    public Color getIntensity() {
        return intensity;
    }
}