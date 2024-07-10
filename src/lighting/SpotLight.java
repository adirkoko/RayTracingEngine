package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static primitives.Util.alignZero;

/**
 * Class representing a spotlight source.
 *
 * @author Adir and Meir.
 */
public class SpotLight extends PointLight {

    /**
     * Determines the focus of the light beam
     * Higher values mean a narrower beam.
     */
    private double narrowBeam = 1;

    /**
     * The direction of the spotlight.
     */
    private final Vector direction;

    /**
     * Constructor for SpotLight.
     *
     * @param intensity the intensity of the light source
     * @param position  the position of the light source
     * @param direction the direction of the light source
     */
    public SpotLight(Color intensity, Point position, Vector direction) {
        super(intensity, position);
        this.direction = direction.normalize();
    }

    @Override
    public Color getIntensity(Point p) {
        double projection = alignZero(direction.dotProduct(getL(p)));
        return projection <= 0 ? Color.BLACK : super.getIntensity(p).scale((narrowBeam > 1) ? Math.pow(projection, narrowBeam) : projection);
    }

    /**
     * Sets the constant attenuation factor and returns the SpotLight itself (chained).
     *
     * @param kC the constant attenuation factor
     * @return the SpotLight itself
     */
    public SpotLight setKc(double kC) {
        super.setKc(kC);
        return this;
    }

    /**
     * Sets the linear attenuation factor and returns the SpotLight itself (chained).
     *
     * @param kL the linear attenuation factor
     * @return the SpotLight itself
     */
    public SpotLight setKl(double kL) {
        super.setKl(kL);
        return this;
    }

    /**
     * Sets the quadratic attenuation factor and returns the SpotLight itself (chained).
     *
     * @param kQ the quadratic attenuation factor
     * @return the SpotLight itself
     */
    public SpotLight setKq(double kQ) {
        super.setKq(kQ);
        return this;
    }

    /**
     * Sets the narrow beam factor.
     *
     * @param narrowBeam the narrow beam factor
     * @return the SpotLight object itself for chaining
     */
    public SpotLight setNarrowBeam(double narrowBeam) {
        this.narrowBeam = narrowBeam;
        return this;
    }


}
