package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Class representing a point light source.
 */
public class PointLight extends Light implements LightSource {

    /**
     * The position of the light source.
     */
    private final Point position;

    /**
     * Constant attenuation factor.
     */
    private double kC = 1;

    /**
     * Linear attenuation factor.
     */
    private double kL = 0;

    /**
     * Quadratic attenuation factor.
     */
    private double kQ = 0;

    /**
     * Constructor for PointLight.
     *
     * @param intensity the intensity of the light source
     * @param position  the position of the light source
     */
    public PointLight(Color intensity, Point position) {
        super(intensity);
        this.position = position;
    }

    /**
     * Sets the constant attenuation factor.
     *
     * @param kC the constant attenuation factor
     * @return the PointLight object itself for chaining
     */
    public PointLight setKc(double kC) {
        this.kC = kC;
        return this;
    }

    /**
     * Sets the linear attenuation factor.
     *
     * @param kL the linear attenuation factor
     * @return the PointLight object itself for chaining
     */
    public PointLight setKl(double kL) {
        this.kL = kL;
        return this;
    }

    /**
     * Sets the quadratic attenuation factor.
     *
     * @param kQ the quadratic attenuation factor
     * @return the PointLight object itself for chaining
     */
    public PointLight setKq(double kQ) {
        this.kQ = kQ;
        return this;
    }

    @Override
    public Color getIntensity(Point p) {
        double distance = position.distance(p);
        return intensity.scale(1 / (kC + kL * distance + kQ * distance * distance));
    }

    @Override
    public Vector getL(Point p) {
        return p.subtract(position).normalize();
    }
}
