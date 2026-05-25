package primitives;

/**
 * Class representing the material properties of a geometry.
 * Provides coefficients for transparency, reflection, diffuse, and specular properties.
 * Also includes shininess coefficient for specular highlights and sampling parameters for advanced global effects.
 *
 * @author Adir and Meir
 */
public class Material {

    /**
     * Maximum number of samples allowed for blurred global effects.
     */
    public static final int MAX_GLOBAL_SAMPLES = 16;

    /**
     * Transparency attenuation coefficient.
     */
    public Double3 kT = Double3.ZERO;

    /**
     * Reflection attenuation coefficient.
     */
    public Double3 kR = Double3.ZERO;

    /**
     * Diffuse coefficient.
     */
    public Double3 kD = Double3.ZERO;

    /**
     * Specular coefficient.
     */
    public Double3 kS = Double3.ZERO;

    /**
     * Shininess coefficient.
     */
    public int nShininess = 0;

    /**
     * Reflection blur radius for future glossy reflection sampling.
     * A value of 0 keeps perfect mirror reflection behavior.
     */
    public double reflectionBlur = 0;

    /**
     * Transparency blur radius for future diffused glass sampling.
     * A value of 0 keeps perfect transparency/refraction behavior.
     */
    public double transparencyBlur = 0;

    /**
     * Number of samples for future blurred global effects.
     * A value of 1 keeps the current single-ray global effect behavior.
     */
    public int globalSamples = 1;

    /**
     * Sets the diffuse coefficient.
     *
     * @param kD the diffuse coefficient
     * @return the Material object itself for chaining
     */
    public Material setKd(Double3 kD) {
        validateCoefficient(kD, "Diffuse coefficient");
        this.kD = kD;
        return this;
    }

    /**
     * Sets the diffuse coefficient.
     *
     * @param kD the diffuse coefficient
     * @return the Material object itself for chaining
     */
    public Material setKd(double kD) {
        validateCoefficient(kD, "Diffuse coefficient");
        this.kD = new Double3(kD);
        return this;
    }

    /**
     * Sets the specular coefficient.
     *
     * @param kS the specular coefficient
     * @return the Material object itself for chaining
     */
    public Material setKs(Double3 kS) {
        validateCoefficient(kS, "Specular coefficient");
        this.kS = kS;
        return this;
    }

    /**
     * Sets the specular coefficient.
     *
     * @param kS the specular coefficient
     * @return the Material object itself for chaining
     */
    public Material setKs(double kS) {
        validateCoefficient(kS, "Specular coefficient");
        this.kS = new Double3(kS);
        return this;
    }

    /**
     * Sets the shininess coefficient.
     *
     * @param nShininess the shininess coefficient
     * @return the Material object itself for chaining
     */
    public Material setShininess(int nShininess) {
        this.nShininess = nShininess;
        return this;
    }

    /**
     * Sets the reflection blur radius for future glossy reflection sampling.
     *
     * @param reflectionBlur reflection blur radius
     * @return the Material object itself for chaining
     */
    public Material setReflectionBlur(double reflectionBlur) {
        if (reflectionBlur < 0)
            throw new IllegalArgumentException("Reflection blur cannot be negative");
        this.reflectionBlur = reflectionBlur;
        return this;
    }

    /**
     * Sets the transparency blur radius for future diffused glass sampling.
     *
     * @param transparencyBlur transparency blur radius
     * @return the Material object itself for chaining
     */
    public Material setTransparencyBlur(double transparencyBlur) {
        if (transparencyBlur < 0)
            throw new IllegalArgumentException("Transparency blur cannot be negative");
        this.transparencyBlur = transparencyBlur;
        return this;
    }

    /**
     * Sets the number of samples for future blurred global effects.
     *
     * @param globalSamples number of global-effect samples
     * @return the Material object itself for chaining
     */
    public Material setGlobalSamples(int globalSamples) {
        if (globalSamples <= 0)
            throw new IllegalArgumentException("Global samples must be positive");
        if (globalSamples > MAX_GLOBAL_SAMPLES)
            throw new IllegalArgumentException("Global samples cannot exceed " + MAX_GLOBAL_SAMPLES);
        this.globalSamples = globalSamples;
        return this;
    }

    /**
     * Sets the transparency attenuation coefficient.
     *
     * @param kT the transparency attenuation coefficient
     * @return the current material
     */
    public Material setKt(Double3 kT) {
        validateCoefficient(kT, "Transparency coefficient");
        this.kT = kT;
        return this;
    }

    /**
     * Sets the transparency attenuation coefficient.
     *
     * @param kT the transparency attenuation coefficient
     * @return the current material
     */
    public Material setKt(double kT) {
        validateCoefficient(kT, "Transparency coefficient");
        this.kT = new Double3(kT);
        return this;
    }

    /**
     * Sets the reflection attenuation coefficient.
     *
     * @param kR the reflection attenuation coefficient
     * @return the current material
     */
    public Material setKr(Double3 kR) {
        validateCoefficient(kR, "Reflection coefficient");
        this.kR = kR;
        return this;
    }

    /**
     * Sets the reflection attenuation coefficient.
     *
     * @param kR the reflection attenuation coefficient
     * @return the current material
     */
    public Material setKr(double kR) {
        validateCoefficient(kR, "Reflection coefficient");
        this.kR = new Double3(kR);
        return this;
    }

    /**
     * Validates scalar material coefficients.
     *
     * @param coefficient coefficient value
     * @param name        coefficient name for error messages
     */
    private static void validateCoefficient(double coefficient, String name) {
        if (coefficient < 0)
            throw new IllegalArgumentException(name + " cannot be negative");
    }

    /**
     * Validates per-channel material coefficients.
     *
     * @param coefficient coefficient value
     * @param name        coefficient name for error messages
     */
    private static void validateCoefficient(Double3 coefficient, String name) {
        if (coefficient == null)
            throw new IllegalArgumentException(name + " cannot be null");
        if (coefficient.d1 < 0 || coefficient.d2 < 0 || coefficient.d3 < 0)
            throw new IllegalArgumentException(name + " cannot contain negative components");
    }

}
