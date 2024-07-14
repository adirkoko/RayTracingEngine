package primitives;

/**
 * Class representing the material properties of a geometry.
 */
public class Material {

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
     * Sets the diffuse coefficient.
     *
     * @param kD the diffuse coefficient
     * @return the Material object itself for chaining
     */
    public Material setKd(Double3 kD) {
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
     * Sets the transparency attenuation coefficient.
     *
     * @param kT the transparency attenuation coefficient
     * @return the current material
     */
    public Material setKt(Double3 kT) {
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
        this.kR = new Double3(kR);
        return this;
    }

}
