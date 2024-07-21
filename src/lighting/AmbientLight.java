package lighting;

import primitives.Color;
import primitives.Double3;

/**
 * Represents the ambient light in the scene.
 * This light is constant and not affected by the distance from the light source.
 * Provides constructors for initializing ambient light with intensity and attenuation coefficient.
 *
 * @author Adir and Meir.
 */
public class AmbientLight extends Light {
    /**
     * Constant for no ambient light
     */
    public static final AmbientLight NONE = new AmbientLight(Color.BLACK, Double3.ZERO);

    /**
     * Constructor for AmbientLight.
     *
     * @param iA the intensity of the ambient light
     * @param kA the attenuation coefficient
     */
    public AmbientLight(Color iA, Double3 kA) {
        super(iA.scale(kA));
    }

    /**
     * Constructor for AmbientLight.
     *
     * @param iA the intensity of the ambient light
     * @param kA the attenuation coefficient
     */
    public AmbientLight(Color iA, double kA) {
        super(iA.scale(kA));
    }

}