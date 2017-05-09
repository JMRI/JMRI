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

    /**
     * 
     * @return the point {0, 0}
     */
    public static Point2D zeroPoint2D() {
        return new Point2D.Double(0, 0);
    }

    /**
     * 
     * @param pA the first point
     * @param pB the second point
     * @return the sum of the two points
     */
    public static Point2D add(Point2D pA, Point2D pB) {
        return new Point2D.Double(pA.getX() + pB.getX(), pA.getY() + pB.getY());
    }

    /**
     * 
     * @param pA the first point
     * @param pB the second point
     * @return the difference of the two points
     */
    public static Point2D subtract(Point2D pA, Point2D pB) {
        return new Point2D.Double(pA.getX() - pB.getX(), pA.getY() - pB.getY());
    }

    /**
     * 
     * @param p the  point
     * @param s the scalar
     * @return the point multiplied by the scalar
     */
    public static Point2D multiply(Point2D p, double s) {
        return new Point2D.Double(p.getX() * s, p.getY() * s);
    }

    /**
     * 
     * @param s the scalar
     * @param p the  point
     * @return the point multiplied by the scalar
     */
    // (again just so parameter order doesn't matter…)
    public static Point2D multiply(double s, Point2D p) {
        return new Point2D.Double(p.getX() * s, p.getY() * s);
    }

    /**
     * 
     * @param p the point
     * @param s the scalar
     * @return the point divided by the scalar
     */
    public static Point2D divide(Point2D p, double s) {
        return new Point2D.Double(p.getX() / s, p.getY() / s);
    }

    /**
     * 
     * @param pA the first point
     * @param pB the second point
     * @return the dot product of the two points
     */
    public static double dot(Point2D pA, Point2D pB) {
        return (pA.getX() * pB.getX() + pA.getY() * pB.getY());
    }

    /**
     * 
     * @param p the point (vector)
     * @return the length squared of the point (vector)
     */
    public static double lengthSquared(Point2D p) {
        return dot(p, p);
    }

    /**
     * 
     * @param p the point (vector)
     * @return the length of the point (vector)
     */
    public static double length(Point2D p) {
        return Math.hypot(p.getX(), p.getY());
    }

    /**
     * 
     * @param pA the first point
     * @param pB the second point
     * @return the distance between the two points
     */
    public static double distance(Point2D pA, Point2D pB) {
        return pA.distance(pB);
    }

    /**
     * 
     * @param p the point
     * @return the normalized point
     */
    // normalize a point
    public static Point2D normalize(Point2D p) {
        Point2D result = p;
        double length = length(p);
        if (length >= 0.001) {
            result = divide(p, length);
        }
        return result;
    }

    /**
     * 
     * @param a the first number
     * @param b the second number
     * @param t the fraction (0 <= t <= 1)
     * @return the linear interpolation between a and b for t
     */
    public static double lerp(double a, double b, double t) {
        return ((1.0 - t) * a) + (t * b);
    }

    /**
     * 
     * @param a the first number
     * @param b the second number
     * @param t the fraction (0 <= t <= 1)
     * @return the linear interpolation between a and b for t
     */
    public static Double lerp(Double a, Double b, Double t) {
        return ((1.0 - t) * a) + (t * b);
    }

    /**
     * 
     * @param pA the first point
     * @param pB the second point
     * @param t the fraction (0 <= t <= 1)
     * @return the linear interpolation between a and b for t
     */
    public static Point2D lerp(Point2D pA, Point2D pB, double t) {
        return new Point2D.Double(
            lerp(pA.getX(), pB.getX(), t),
            lerp(pA.getY(), pB.getY(), t));
    }

    /**
     * 
     * @param pA the first point
     * @param pB the second point
     * @return the midpoint between the two points
     */
    public static Point2D midpoint(Point2D pA, Point2D pB) {
        return lerp(pA, pB, 0.5);
    }

    /**
     * 
     * @param pA the first point
     * @param pB the second point
     * @return the point one third of the way from pA to pB
     */
    public static Point2D third(Point2D pA, Point2D pB) {
        return lerp(pA, pB, 1.0 / 3.0);
    }

    /**
     * 
     * @param pA the first point
     * @param pB the second point
     * @return the point one fourth of the way from pA to pB
     */
    public static Point2D fourth(Point2D pA, Point2D pB) {
        return lerp(pA, pB, 1.0 / 4.0);
    }

    /**
     * 
     * Wrap a double between two values (for example +/- 180 or 0-360 degrees)
     * 
     * @param inValue the value
     * @param inMin the lowest value
     * @param inMax the highest value
     * @return the value wrapped between the lowest and highest values
     * Note: THIS IS NOT A PIN OR TRUNCATE; VALUES WRAP AROUND BETWEEN MIN AND MAX
     * (And yes, this works correctly with negative numbers)
     */
    public static double wrap(double inValue, double inMin, double inMax) {
        double valueRange = inMax - inMin;
        return inMin + ((((inValue - inMin) % valueRange) + valueRange) % valueRange);
    }

    /**
     * 
     * wrap a value between +/-180
     * 
     * @param inValue the value
     * @return the value wrapped between -180 and +180
     */
    public static double wrapPM180(double inValue) {
        return wrap(inValue, -180.0, +180.0);
    }

    /**
     * 
     * wrap a value between +/-360
     * 
     * @param inValue the value
     * @return the value wrapped between -360 and +360
     */
    public static double wrapPM360(double inValue) {
        return wrap(inValue, -360.0, +360.0);
    }

    /**
     * 
     * wrap a value between 0 and 360
     * 
     * @param inValue the value
     * @return the value wrapped between -360 and +360
     */
    public static double wrap360(double inValue) {
        return wrap(inValue, 0.0, +360.0);
    }

    /**
     * 
     * wrap an angle between 0 and 360
     * 
     * @param a the angle
     * @return the angle wrapped between 0 and 360
     */
    public static double normalizeAngle(double a) {
        return wrap360(a);
    }

    // 
    /**
     * calculate the absolute difference (0-180) between two angles
     * @param a the first angle
     * @param b the second angle
     * @return the absolute difference between the two angles
     */
    public static double diffAngle(double a, double b) {
        return Math.abs(wrapPM180(a - b));
    }

    /**
     * pin a value between min and max
     * @param inValue the value
     * @param inMin the min
     * @param inMax the max
     * @return the value pinned between the min and max values
     */
    public static double pin(double inValue, double inMin, double inMax) {
        return Math.min(Math.max(inValue, inMin), inMax);
    }
   
    // recursive routine to draw a cubic Bezier…
    // (also returns distance!)
    private static double drawBezier(Graphics2D g2, Point2D p0, Point2D p1, Point2D p2, Point2D p3, int depth) {
        double result = 0;
        
        // calculate flatness to determine if we need to recurse…
        double l01 = distance(p0, p1);
        double l12 = distance(p1, p2);
        double l23 = distance(p2, p3);
        double l03 = distance(p0, p3);
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
