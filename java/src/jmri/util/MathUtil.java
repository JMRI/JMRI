package jmri.util;

import java.awt.geom.Point2D;
import javax.annotation.CheckReturnValue;

/**
*
* useful math methods
*
* @author geowar Copyright 2017
*/

@CheckReturnValue
public final class MathUtil {

    //return a double between a & b for t:0 ==> a and t:1 ==> b
    public static double lerp(double a, double b, double t) {
        return ((1.0 - t) * a) + (t * b);
    }

    //return a Double between a & b for t:0 ==> a and t:1 ==> b
    public static Double lerp(Double a, Double b, Double t) {
        return ((1.0 - t) * a) + (t * b);
    }

    //return a Point2D between a & b for t:0 ==> a and t:1 ==> b
    public static Point2D lerp(Point2D p1, Point2D p2, double interpolant) {
        return new Point2D.Double(
            lerp(p1.getX(), p2.getX(), interpolant),
            lerp(p1.getY(), p2.getY(), interpolant));
    }

    // return a Point2D at the mid point between p1 & p2
    public static Point2D midpoint(Point2D p1, Point2D p2) {
        return lerp(p1, p2, 0.5);
    }

    // return a Point2D one third of the way from p1 to p2
    public static Point2D third(Point2D p1, Point2D p2) {
        return lerp(p1, p2, 1.0 / 3.0);
    }

    // return a Point2D one forth of the way from p1 to p2
    public static Point2D fourth(Point2D p1, Point2D p2) {
        return lerp(p1, p2, 1.0 / 4.0);
    }

    //
    // Wrap a double between two values (for example +/- 180 or 0-360 degrees)
    // Note: THIS IS NOT A PIN OR TRUNCATE; VALUES WRAP AROUND BETWEEN MIN & MAX
    // (And yes, this works correctly with negative numbers)
    //
    public static double wrap(double inValue, double inMin, double inMax) {
        double valueRange = inMax - inMin;
        return inMin + ((((inValue - inMin) % valueRange) + valueRange) % valueRange);
    }

    // wrap an double between +/-180
    public static double wrapPM180(double inValue) {
        return wrap(inValue, -180.0, +180.0);
    }

    // wrap an double between +/-360
    public static double wrapPM360(double inValue) {
        return wrap(inValue, -360.0, +360.0);
    }

    // wrap an double between 0-360
    public static double wrap360(double inValue) {
        return wrap(inValue, 0.0, +360.0);
    }

    // wrap an angle between 0-360
    public static double normalizeAngle(double a) {
        return wrap360(a);
    }

    // return the absolute difference (0-180) between two angles
    public static double diffAngle(double a, double b) {
        return Math.abs(wrapPM180(a - b));
    }

    // pin a value between min & max
    public static double pin(double inValue, double inMin, double inMax) {
        return Math.min(Math.max(inValue, inMin), inMax);
    }
}
