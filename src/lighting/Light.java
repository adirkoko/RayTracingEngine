package lighting;

import primitives.Color;

/**
 * Abstract class representing a light source.
 */
abstract class Light {
    /**
     * The intensity of the light source.
     */
    protected final Color intensity;

    /**
     * Constructor for Light, initializes the intensity.
     *
     * @param intensity the intensity of the light source
     */
    protected Light(Color intensity) {
        this.intensity = intensity;
    }

    /**
     * Gets the intensity of the light source.
     *
     * @return the intensity of the light source
     */
    public Color getIntensity() {
        return intensity;
    }
}
