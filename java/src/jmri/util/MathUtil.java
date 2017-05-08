package jmri.util;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
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

    //return the sum of two points
    public static Point2D add(Point2D a, Point2D b) {
        return new Point2D.Double(a.getX() + b.getX(), a.getY() + b.getY());
    }

    //return the difference of two points
    public static Point2D subtract(Point2D a, Point2D b) {
        return new Point2D.Double(a.getX() - b.getX(), a.getY() - b.getY());
    }

    // multiply a point by a scalar
    public static Point2D multiply(Point2D p, double s) {
        return new Point2D.Double(p.getX() * s, p.getY() * s);
    }

    // multiply a point by a scalar
    // (again just so parameter order doesn't matter…)
    public static Point2D multiply(double s, Point2D p) {
        return new Point2D.Double(p.getX() * s, p.getY() * s);
    }

    // divide a point by a scalar
    public static Point2D divide(Point2D p, double s) {
        return new Point2D.Double(p.getX() / s, p.getY() / s);
    }

    // return the dot product of two points
    public static double dot(Point2D pA, Point2D pB) {
        return (pA.getX() * pB.getX() + pA.getY() * pB.getY());
    }

    // return the length of a vector
    public static double lengthSquared(Point2D pA) {
        return dot(pA, pA);
    }

    // return the distance squared between two points
    public static double lengthSquared(Point2D pA, Point2D pB) {
        return dot(pA, pB);
    }

    // return the length of a vector
    public static double length(Point2D pA) {
        return Math.hypot(pA.getX(), pA.getY());
    }

    // return the distance between two points
    public static double length(Point2D pA, Point2D pB) {
        Point2D delta = subtract(pA, pB);
        return Math.hypot(delta.getX(), delta.getY());
    }

    // normalize a point
    public static Point2D normalize(Point2D pA) {
        Point2D result = pA;
        double length = length(pA);
        if (length >= 0.001) {
            result = divide(pA, length);
        }
        return result;
    }

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

    
    // recursive routine to draw a cubic Bezier…
    // (also returns length!)
    private static double drawBezier(Graphics2D g2, Point2D p0, Point2D p1, Point2D p2, Point2D p3, int depth) {
        double result = 0;
        
        // calculate flatness to determine if we need to recurse…
        double l01 = length(p0, p1);
        double l12 = length(p1, p2);
        double l23 = length(p2, p3);
        double l03 = length(p0, p3);
        double flatness = (l01 + l12 + l23) / l03;
        
        // depth prevents stack overflow… 
        // (I picked 12 because 2^12 = 2048… is larger than most monitors ;-)
        // the flatness comparison value is somewhat arbitrary.
        // (I just kept moving it closer to 1 until I got good results. ;-)
        if ((depth > 12) || (flatness <= 1.001)) {
            g2.draw(new Line2D.Double(p0, p3));
            result = l03;
        } else {
            // first order midpoints
            Point2D q0 = midpoint(p0, p1);
            Point2D q1 = midpoint(p1, p2);
            Point2D q2 = midpoint(p2, p3);
            
            // second order midpoints
            Point2D r0 = midpoint(q0, q1);
            Point2D r1 = midpoint(q1, q2);

            // third order midpoint
            Point2D s = midpoint(r0, r1);
            
            // draw left side Bezier
            result = drawBezier(g2, p0, q0, r0, s, depth + 1);
            // draw right side Bezier
            result += drawBezier(g2, s, r1, q2, p3, depth + 1);
        }
        return result;
    }

    /**
     * Draw a cubic Bezier curve
     * @param g2 the Graphics2D to draw to
     * @param p0-p3 the Control points
     * @return  the length of the Bezier curve
     */
    
    public static double drawBezier(Graphics2D g2, Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
        return drawBezier(g2, p0, p1, p2, p3, 0);
    }
}
