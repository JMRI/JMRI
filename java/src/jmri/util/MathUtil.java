package jmri.util;

import java.awt.*;
import java.awt.geom.*;
import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Math.PI;
import java.util.*;
import java.util.List;

import javax.annotation.*;

/**
 * Useful math methods.
 *
 * @author geowar Copyright 2017
 */
public final class MathUtil {

    public static final Point2D zeroPoint2D = zeroPoint2D();
    public static final Point2D infinityPoint2D = infinityPoint2D();
    public static final Rectangle2D zeroRectangle2D = zeroRectangle2D();
    public static final Rectangle2D zeroToInfinityRectangle2D = zeroToInfinityRectangle2D();
    public static final Rectangle2D infinityRectangle2D = infinityRectangle2D();

    /**
     * @return the point {0, 0}
     */
    @CheckReturnValue
    public static Point2D zeroPoint2D() {
        return new Point2D.Double(0, 0);
    }

    /**
     * @return the point {POSITIVE_INFINITY, POSITIVE_INFINITY}
     */
    @CheckReturnValue
    public static Point2D infinityPoint2D() {
        return new Point2D.Double(POSITIVE_INFINITY, POSITIVE_INFINITY);
    }

    /**
     * @param a the first number
     * @param b the second number
     * @return the greatest common divisor of a and b
     */
    public static int gcd(int a, int b) {
        int result = b;
        if (a != 0) {
            result = gcd(b % a, a);
        }
        return result;
    }

    /**
     * Convert Point to Point2D.
     *
     * @param p the Point
     * @return the Point2D
     */
    @CheckReturnValue
    public static Point2D pointToPoint2D(@Nonnull Point p) {
        return new Point2D.Double(p.x, p.y);
    }

    /**
     * Convert Point2D to Point.
     *
     * @param p the Point
     * @return the Point2D
     */
    @CheckReturnValue
    public static Point point2DToPoint(@Nonnull Point2D p) {
        return new Point((int) p.getX(), (int) p.getY());
    }

    /**
     * @param a the first float
     * @param b the second float
     * @return true if a is equal to b
     */
    public static boolean equals(float a, float b) {
        return (Float.floatToIntBits(a) == Float.floatToIntBits(b));
    }

    /**
     * @param a the first double
     * @param b the second double
     * @return true if a is equal to b
     */
    public static boolean equals(double a, double b) {
        return (Double.doubleToLongBits(a) == Double.doubleToLongBits(b));
    }

    /**
     * @param a the first Rectangle2D
     * @param b the second Rectangle2D
     * @return true if a is equal to b
     */
    public static boolean equals(Rectangle2D a, Rectangle2D b) {
        return (equals(a.getMinX(), b.getMinX())
                && equals(a.getMinY(), b.getMinY())
                && equals(a.getWidth(), b.getWidth())
                && equals(a.getHeight(), b.getHeight()));
    }

    /**
     * @param a the first Point2D
     * @param b the second Point2D
     * @return true if a is equal to b
     */
    public static boolean equals(Point2D a, Point2D b) {
        return (equals(a.getX(), b.getX()) && equals(a.getY(), b.getY()));
    }

    /**
     * @param p the point
     * @return true if p1 is equal to zeroPoint2D
     */
    public static boolean isEqualToZeroPoint2D(@Nonnull Point2D p) {
        return p.equals(zeroPoint2D);
    }

    /**
     * Get the minimum coordinates of two points.
     *
     * @param pA the first point
     * @param pB the second point
     * @return the minimum coordinates
     */
    @CheckReturnValue
    public static Point2D min(@Nonnull Point2D pA, @Nonnull Point2D pB) {
        return new Point2D.Double(Math.min(pA.getX(), pB.getX()), Math.min(pA.getY(), pB.getY()));
    }

    /**
     * Get the maximum coordinates of two points.
     *
     * @param pA the first point
     * @param pB the second point
     * @return the maximum coordinates
     */
    @CheckReturnValue
    public static Point2D max(@Nonnull Point2D pA, @Nonnull Point2D pB) {
        return new Point2D.Double(Math.max(pA.getX(), pB.getX()), Math.max(pA.getY(), pB.getY()));
    }

    /**
     * Get the coordinates of a point pinned between two other points.
     *
     * @param pA the first point
     * @param pB the second point
     * @param pC the third point
     * @return the coordinates of pA pined between pB and pC
     */
    @CheckReturnValue
    public static Point2D pin(@Nonnull Point2D pA, @Nonnull Point2D pB, @Nonnull Point2D pC) {
        return min(max(pA, min(pB, pC)), max(pB, pC));
    }

    /**
     * Get the coordinates of a point pinned in a rectangle
     *
     * @param pA the point
     * @param pR the rectangle
     * @return the coordinates of point pA pined in rectangle pR
     */
    @CheckReturnValue
    public static Point2D pin(@Nonnull Point2D pA, @Nonnull Rectangle2D pR) {
        return min(max(pA, getOrigin(pR)), offset(getOrigin(pR), pR.getWidth(), pR.getHeight()));
    }

    /**
     * Add two points.
     *
     * @param pA the first point
     * @param pB the second point
     * @return the sum of the two points
     */
    @CheckReturnValue
    public static Point2D add(@Nonnull Point2D pA, @Nonnull Point2D pB) {
        return new Point2D.Double(pA.getX() + pB.getX(), pA.getY() + pB.getY());
    }

    /**
     * Subtract two points.
     *
     * @param pA the first point
     * @param pB the second point
     * @return the difference of the two points
     */
    @CheckReturnValue
    public static Point2D subtract(@Nonnull Point2D pA, @Nonnull Point2D pB) {
        return new Point2D.Double(pA.getX() - pB.getX(), pA.getY() - pB.getY());
    }

    /**
     * Multiply a point times a scalar.
     *
     * @param p the point
     * @param s the scalar
     * @return the point multiplied by the scalar
     */
    @CheckReturnValue
    public static Point2D multiply(@Nonnull Point2D p, double s) {
        return new Point2D.Double(p.getX() * s, p.getY() * s);
    }

    /**
     * Multiply a point times two scalar.
     *
     * @param p the point
     * @param x the X scalar
     * @param y the Y scalar
     * @return the point multiplied by the two scalars
     */
    @CheckReturnValue
    public static Point2D multiply(@Nonnull Point2D p, double x, double y) {
        return new Point2D.Double(p.getX() * x, p.getY() * y);
    }

    /**
     * Multiply a scalar times a point.
     *
     * @param s the scalar
     * @param p the point
     * @return the point multiplied by the scalar
     */
    // (again just so parameter order doesn't matter...)
    public static Point2D multiply(double s, @Nonnull Point2D p) {
        return new Point2D.Double(p.getX() * s, p.getY() * s);
    }

    /**
     * Multiply a point times a point.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the first point multiplied by the second
     */
    @CheckReturnValue
    public static Point2D multiply(@Nonnull Point2D p1, @Nonnull Point2D p2) {
        return multiply(p1, p2.getX(), p2.getY());
    }

    /**
     * Divide a point by a scalar.
     *
     * @param p the point
     * @param s the scalar
     * @return the point divided by the scalar
     */
    @CheckReturnValue
    public static Point2D divide(@Nonnull Point2D p, double s) {
        return new Point2D.Double(p.getX() / s, p.getY() / s);
    }

    /**
     * Divide a point by two scalars.
     *
     * @param p the point
     * @param x the X scalar
     * @param y the Y scalar
     * @return the point divided by the scalar
     */
    @CheckReturnValue
    public static Point2D divide(@Nonnull Point2D p, double x, double y) {
        return new Point2D.Double(p.getX() / x, p.getY() / y);
    }

    /**
     * Offset a point by two scalars.
     *
     * @param p the point
     * @param x the x scalar
     * @param y the y scalar
     * @return the point offset by the scalars
     */
    @CheckReturnValue
    public static Point2D offset(@Nonnull Point2D p, double x, double y) {
        return new Point2D.Double(p.getX() + x, p.getY() + y);
    }

    /**
     * Rotate x and y coordinates (by radians).
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param a the angle (in radians)
     * @return the point rotated by the angle
     */
    @CheckReturnValue
    public static Point2D rotateRAD(double x, double y, double a) {
        double cosA = Math.cos(a), sinA = Math.sin(a);
        return new Point2D.Double(cosA * x - sinA * y, sinA * x + cosA * y);
    }

    /**
     * Rotate x and y coordinates (by degrees).
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param a the angle (in radians)
     * @return the point rotated by the angle
     */
    @CheckReturnValue
    public static Point2D rotateDEG(double x, double y, double a) {
        return rotateRAD(x, y, Math.toRadians(a));
    }

    /**
     * Rotate a point (by radians).
     *
     * @param p the point
     * @param a the angle (in radians)
     * @return the point rotated by the angle
     */
    @CheckReturnValue
    public static Point2D rotateRAD(@Nonnull Point2D p, double a) {
        return rotateRAD(p.getX(), p.getY(), a);
    }

    /**
     * Rotate a point (by degrees).
     *
     * @param p the point
     * @param a the angle (in radians)
     * @return the point rotated by the angle
     */
    @CheckReturnValue
    public static Point2D rotateDEG(@Nonnull Point2D p, double a) {
        return rotateRAD(p, Math.toRadians(a));
    }

    /**
     * Rotate a point around another point (by radians).
     *
     * @param p    the point being rotated
     * @param c    the point its being rotated around
     * @param aRAD the angle (in radians)
     * @return the point rotated by the angle
     */
    @CheckReturnValue
    public static Point2D rotateRAD(
            @Nonnull Point2D p, @Nonnull Point2D c, double aRAD) {
        return add(c, rotateRAD(subtract(p, c), aRAD));
    }

    /**
     * Rotate a point around another point (by degrees).
     *
     * @param p    the point being rotated
     * @param c    the point its being rotated around
     * @param aDEG the angle (in radians)
     * @return the point rotated by the angle
     */
    @CheckReturnValue
    public static Point2D rotateDEG(
            @Nonnull Point2D p, @Nonnull Point2D c, double aDEG) {
        return rotateRAD(p, c, Math.toRadians(aDEG));
    }

    /**
     * @param p the point
     * @return the point orthogonal to this one (relative to {0, 0})
     */
    public static Point2D orthogonal(@Nonnull Point2D p) {
        return new Point2D.Double(-p.getY(), p.getX());
    }

    /**
     * Create a vector given a direction and a magnitude.
     *
     * @param dirDEG    the direction (in degrees)
     * @param magnitude the magnitude
     * @return the vector with the specified direction and magnitude
     */
    @CheckReturnValue
    public static Point2D vectorDEG(double dirDEG, double magnitude) {
        Point2D result = new Point2D.Double(magnitude, 0.0);
        return rotateDEG(result, dirDEG);
    }

    /**
     * Create a vector given a direction and a magnitude.
     *
     * @param dirRAD    the direction (in radians)
     * @param magnitude the magnitude
     * @return the vector with the specified direction and magnitude
     */
    @CheckReturnValue
    public static Point2D vectorRAD(double dirRAD, double magnitude) {
        Point2D result = new Point2D.Double(magnitude, 0.0);
        return rotateRAD(result, dirRAD);
    }

    /**
     * Dot product of two points (vectors).
     *
     * @param pA the first point
     * @param pB the second point
     * @return the dot product of the two points note: Arccos(x) (inverse
     *         cosine) of dot product is the angle between the vectors
     */
    @CheckReturnValue
    public static double dot(@Nonnull Point2D pA, @Nonnull Point2D pB) {
        return (pA.getX() * pB.getX() + pA.getY() * pB.getY());
    }

    /**
     * Calculate the length squared of a point (vector).
     *
     * @param p the point (vector)
     * @return the length squared of the point (vector)
     */
    @CheckReturnValue
    public static double lengthSquared(@Nonnull Point2D p) {
        return dot(p, p);
    }

    /**
     * Calculate the length of a point (vector).
     *
     * @param p the point (vector)
     * @return the length of the point (vector)
     */
    @CheckReturnValue
    public static double length(@Nonnull Point2D p) {
        return Math.hypot(p.getX(), p.getY());
    }

    /**
     * Calculate the distance between two points.
     *
     * @param pA the first point
     * @param pB the second point
     * @return the distance between the two points
     */
    @CheckReturnValue
    public static double distance(@Nonnull Point2D pA, @Nonnull Point2D pB) {
        return pA.distance(pB);
    }

    /**
     * Normalize a point (vector) to a length.
     *
     * @param p      the point (vector)
     * @param length the length to normalize to
     * @return the normalized point (vector)
     */
    @CheckReturnValue
    public static Point2D normalize(@Nonnull Point2D p, double length) {
        return multiply(normalize(p), length);
    }

    /**
     * Normalize a point (vector).
     *
     * @param p the point (vector)
     * @return the normalized point (vector)
     */
    @CheckReturnValue
    public static Point2D normalize(@Nonnull Point2D p) {
        Point2D result = p;
        double length = length(p);
        if (length > 0.0) {
            result = divide(p, length);
        }
        return result;
    }

    /**
     * Compute the angle (direction in radians) for a vector.
     *
     * @param p the vector (point relative to zeroPoint2D)
     * @return the angle in radians
     */
    @CheckReturnValue
    public static double computeAngleRAD(@Nonnull Point2D p) {
        return Math.atan2(p.getX(), p.getY());
    }

    /**
     * Compute the angle (direction in degrees) for a vector.
     *
     * @param p the vector (point relative to zeroPoint2D)
     * @return the angle in degrees
     */
    @CheckReturnValue
    public static double computeAngleDEG(@Nonnull Point2D p) {
        return Math.toDegrees(computeAngleRAD(p));
    }

    /**
     * Compute the angle (direction in radians) from point 1 to point 2.
     * <p>
     * Note: Goes CCW from south to east to north to west, etc. For JMRI
     * subtract from PI/2 to get east, south, west, north
     *
     * @param p1 the first Point2D
     * @param p2 the second Point2D
     * @return the angle in radians
     */
    @CheckReturnValue
    public static double computeAngleRAD(@Nonnull Point2D p1, @Nonnull Point2D p2) {
        return computeAngleRAD(subtract(p1, p2));
    }

    /**
     * Compute the angle (direction in degrees) from point 1 to point 2.
     * <p>
     * Note: Goes CCW from south to east to north to west, etc. For JMRI
     * subtract from 90.0 to get east, south, west, north
     *
     * @param p1 the first Point2D
     * @param p2 the second Point2D
     * @return the angle in degrees
     */
    @CheckReturnValue
    public static double computeAngleDEG(@Nonnull Point2D p1, @Nonnull Point2D p2) {
        return Math.toDegrees(computeAngleRAD(subtract(p1, p2)));
    }

    /**
     * Calculate the linear interpolation between two integers.
     *
     * @param a the first number
     * @param b the second number
     * @param t the fraction (between 0 and 1)
     * @return the linear interpolation between a and b for t
     */
    public static int lerp(int a, int b, double t) {
        return (int) lerp((double) a, (double) b, t);
    }

    /**
     * Calculate the linear interpolation between two doubles.
     *
     * @param a the first number
     * @param b the second number
     * @param t the fraction (between 0 and 1)
     * @return the linear interpolation between a and b for t
     */
    @CheckReturnValue
    public static double lerp(double a, double b, double t) {
        return ((1.D - t) * a) + (t * b);
    }

    /**
     * Calculate the linear interpolation between two Doubles.
     *
     * @param a the first number
     * @param b the second number
     * @param t the fraction (between 0 and 1)
     * @return the linear interpolation between a and b for t
     */
    @CheckReturnValue
    public static Double lerp(@Nonnull Double a, @Nonnull Double b, @Nonnull Double t) {
        return ((1.D - t) * a) + (t * b);
    }

    /**
     * Calculate the linear interpolation between two points.
     *
     * @param pA the first point
     * @param pB the second point
     * @param t  the fraction (between 0 and 1)
     * @return the linear interpolation between a and b for t
     */
    @CheckReturnValue
    public static Point2D lerp(@Nonnull Point2D pA, @Nonnull Point2D pB, double t) {
        return new Point2D.Double(
                lerp(pA.getX(), pB.getX(), t),
                lerp(pA.getY(), pB.getY(), t));
    }

    /**
     * Round value to granular increment.
     *
     * @param v the value to granulize
     * @param g the granularity
     * @return the value granulized to the granularity
     */
    @CheckReturnValue
    public static double granulize(double v, double g) {
        return Math.round(v / g) * g;
    }

    /**
     * Round point to horizontal and vertical granular increments.
     *
     * @param p  the point to granulize
     * @param gH the horizontal granularity
     * @param gV the vertical granularity
     * @return the point granulized to the granularity
     */
    @CheckReturnValue
    public static Point2D granulize(@Nonnull Point2D p, double gH, double gV) {
        return new Point2D.Double(granulize(p.getX(), gH), granulize(p.getY(), gV));
    }

    /**
     * Round point to granular increment.
     *
     * @param p the point to granulize
     * @param g the granularity
     * @return the point granulized to the granularity
     */
    @CheckReturnValue
    public static Point2D granulize(@Nonnull Point2D p, double g) {
        return granulize(p, g, g);
    }

    /**
     * Round Rectangle2D to granular increment.
     *
     * @param r the rectangle to granulize
     * @param g the granularity
     * @return the rectangle granulized to the granularity
     */
    @CheckReturnValue
    public static Rectangle2D granulize(@Nonnull Rectangle2D r, double g) {
        return new Rectangle2D.Double(
                granulize(r.getMinX(), g),
                granulize(r.getMinY(), g),
                granulize(r.getWidth(), g),
                granulize(r.getHeight(), g));
    }

    /**
     * Calculate the midpoint between two points.
     *
     * @param pA the first point
     * @param pB the second point
     * @return the midpoint between the two points
     */
    @CheckReturnValue
    public static Point2D midPoint(@Nonnull Point2D pA, @Nonnull Point2D pB) {
        return lerp(pA, pB, 0.5);
    }

    /**
     * Calculate the point 1/3 of the way between two points.
     *
     * @param pA the first point
     * @param pB the second point
     * @return the point one third of the way from pA to pB
     */
    @CheckReturnValue
    public static Point2D oneThirdPoint(@Nonnull Point2D pA, @Nonnull Point2D pB) {
        return lerp(pA, pB, 1.0 / 3.0);
    }

    /**
     * Calculate the point 2/3 of the way between two points.
     *
     * @param pA the first point
     * @param pB the second point
     * @return the point two thirds of the way from pA to pB
     */
    @CheckReturnValue
    public static Point2D twoThirdsPoint(@Nonnull Point2D pA, @Nonnull Point2D pB) {
        return lerp(pA, pB, 2.0 / 3.0);
    }

    /**
     * Calculate the point 1/4 of the way between two points.
     *
     * @param pA the first point
     * @param pB the second point
     * @return the point one fourth of the way from pA to pB
     */
    @CheckReturnValue
    public static Point2D oneFourthPoint(@Nonnull Point2D pA, @Nonnull Point2D pB) {
        return lerp(pA, pB, 1.0 / 4.0);
    }

    /**
     * Calculate the point 3/4 of the way between two points.
     *
     * @param pA the first point
     * @param pB the second point
     * @return the point three fourths of the way from pA to pB
     */
    @CheckReturnValue
    public static Point2D threeFourthsPoint(@Nonnull Point2D pA, @Nonnull Point2D pB) {
        return lerp(pA, pB, 3.0 / 4.0);
    }

    /**
     * Wrap an int between two values (for example +/- 180 or 0-360 degrees).
     *
     * @param inValue the value
     * @param inMin   the lowest value
     * @param inMax   the highest value
     * @return the value wrapped between the lowest and highest values Note:
     *         THIS IS NOT A PIN OR TRUNCATE; VALUES WRAP AROUND BETWEEN MIN AND
     *         MAX (And yes, this works correctly with negative numbers)
     */
    public static int wrap(int inValue, int inMin, int inMax) {
        int valueRange = inMax - inMin;
        return inMin + ((((inValue - inMin) % valueRange) + valueRange) % valueRange);
    }

    /**
     * Wrap a double between two values (for example +/- 180 or 0-360 degrees).
     *
     * @param inValue the value
     * @param inMin   the lowest value
     * @param inMax   the highest value
     * @return the value wrapped between the lowest and highest values Note:
     *         THIS IS NOT A PIN OR TRUNCATE; VALUES WRAP AROUND BETWEEN MIN AND
     *         MAX (And yes, this works correctly with negative numbers)
     */
    @CheckReturnValue
    public static double wrap(double inValue, double inMin, double inMax) {
        double valueRange = inMax - inMin;
        return inMin + ((((inValue - inMin) % valueRange) + valueRange) % valueRange);
    }

    /**
     * Wrap a value between +/-180.
     *
     * @param inValue the value
     * @return the value wrapped between -180 and +180
     */
    @CheckReturnValue
    public static double wrapPM180(double inValue) {
        return wrap(inValue, -180.0, +180.0);
    }

    /**
     * Wrap a value between +/-360.
     *
     * @param inValue the value
     * @return the value wrapped between -360 and +360
     */
    @CheckReturnValue
    public static double wrapPM360(double inValue) {
        return wrap(inValue, -360.0, +360.0);
    }

    /**
     * Wrap a value between 0 and 360.
     *
     * @param inValue the value
     * @return the value wrapped between -360 and +360
     */
    @CheckReturnValue
    public static double wrap360(double inValue) {
        return wrap(inValue, 0.0, +360.0);
    }

    /**
     * Wrap an angle between 0 and 360.
     *
     * @param a the angle
     * @return the angle wrapped between 0 and 360
     */
    @CheckReturnValue
    public static double normalizeAngleDEG(double a) {
        return wrap360(a);
    }

    /**
     * Calculate the relative difference (+/-180) between two angles.
     *
     * @param a the first angle
     * @param b the second angle
     * @return the relative difference between the two angles (in degrees)
     */
    @CheckReturnValue
    public static double diffAngleDEG(double a, double b) {
        return wrapPM180(a - b);
    }

    /**
     * Calculate the absolute difference (0-180) between two angles.
     *
     * @param a the first angle
     * @param b the second angle
     * @return the absolute difference between the two angles (in degrees)
     */
    @CheckReturnValue
    public static double absDiffAngleDEG(double a, double b) {
        return Math.abs(diffAngleDEG(a, b));
    }

    /**
     * Calculate the relative difference (+/-PI) between two angles.
     *
     * @param a the first angle
     * @param b the second angle
     * @return the relative difference between the two angles (in radians)
     */
    @CheckReturnValue
    public static double diffAngleRAD(double a, double b) {
        return wrap(a - b, -PI, +PI);

    }

    /**
     * Calculate the absolute difference (0-PI) between two angles.
     *
     * @param a the first angle
     * @param b the second angle
     * @return the absolute difference between the two angles (in radians)
     */
    @CheckReturnValue
    public static double absDiffAngleRAD(double a, double b) {
        return Math.abs(diffAngleRAD(a, b));
    }

    /**
     * Pin a value between min and max.
     *
     * @param inValue the value
     * @param inMin   the min
     * @param inMax   the max
     * @return the value pinned between the min and max values
     */
    public static int pin(int inValue, int inMin, int inMax) {
        return Math.min(Math.max(inValue, inMin), inMax);
    }

    /**
     * Pin a value between min and max.
     *
     * @param inValue the value
     * @param inMin   the min
     * @param inMax   the max
     * @return the value pinned between the min and max values
     */
    @CheckReturnValue
    public static double pin(double inValue, double inMin, double inMax) {
        return Math.min(Math.max(inValue, inMin), inMax);
    }

    /**
     * @return a new rectangle {0.0, 0.0, 0.0, 0.0}
     */
    @CheckReturnValue
    public static Rectangle2D zeroRectangle2D() {
        return new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
    }

    /**
     * @return a new rectangle {0.0, 0.0, POSITIVE_INFINITY, POSITIVE_INFINITY}
     */
    @CheckReturnValue
    public static Rectangle2D zeroToInfinityRectangle2D() {
        return new Rectangle2D.Double(0.0, 0.0, POSITIVE_INFINITY, POSITIVE_INFINITY);
    }

    /**
     * @return a new rectangle {NEGATIVE_INFINITY, NEGATIVE_INFINITY,
     *         POSITIVE_INFINITY, POSITIVE_INFINITY}
     */
    @CheckReturnValue
    public static Rectangle2D infinityRectangle2D() {
        return new Rectangle2D.Double(NEGATIVE_INFINITY, NEGATIVE_INFINITY, POSITIVE_INFINITY, POSITIVE_INFINITY);
    }

    /**
     * rectangle2DToString return a string to represent a rectangle
     *
     * @param r the rectangle2D
     * @return the string
     */
    @Nonnull
    public static String rectangle2DToString(@Nonnull Rectangle2D r) {
        return String.format("{%.2f, %.2f, %.2f, %.2f}",
                r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
    }

    /**
     * Convert Rectangle to Rectangle2D.
     *
     * @param r the Rectangle
     * @return the Rectangle2D
     */
    @CheckReturnValue
    public static Rectangle2D rectangleToRectangle2D(@Nonnull Rectangle r) {
        return new Rectangle2D.Double(r.x, r.y, r.width, r.height);
    }

    /**
     * Convert Rectangle2D to Rectangle.
     *
     * @param r the Rectangle
     * @return the Rectangle2D
     */
    @CheckReturnValue
    public static Rectangle rectangle2DToRectangle(@Nonnull Rectangle2D r) {
        return new Rectangle((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
    }

    /**
     * Get the origin (top left) of the rectangle.
     *
     * @param r the rectangle
     * @return the origin of the rectangle
     */
    @CheckReturnValue
    public static Point2D getOrigin(@Nonnull Rectangle2D r) {
        return new Point2D.Double(r.getX(), r.getY());
    }

    /**
     * Set the origin (top left) of the rectangle.
     *
     * @param r      the rectangle
     * @param origin the origin
     * @return a new rectangle with the new origin
     */
    @CheckReturnValue
    public static Rectangle2D setOrigin(@Nonnull Rectangle2D r, @Nonnull Point2D origin) {
        return new Rectangle2D.Double(origin.getX(), origin.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * dimensionToString return a string to represent a Dimension
     *
     * @param d the Dimension
     * @return the string
     */
    @Nonnull
    public static String dimensionToString(@Nonnull Dimension d) {
        return String.format("{%.2f, %.2f}", d.getWidth(), d.getHeight());
    }

    /**
     * Get the size of a rectangle.
     *
     * @param r the rectangle
     * @return the size of the rectangle
     */
    @CheckReturnValue
    public static Dimension getSize(@Nonnull Rectangle2D r) {
        return new Dimension((int) r.getWidth(), (int) r.getHeight());
    }

    /**
     * Set the size of a rectangle
     *
     * @param r the rectangle
     * @param d the new size (as dimension)
     * @return a new rectangle with the new size
     */
    @CheckReturnValue
    public static Rectangle2D setSize(@Nonnull Rectangle2D r, @Nonnull Dimension d) {
        return new Rectangle2D.Double(r.getMinX(), r.getMinY(), d.getWidth(), d.getHeight());
    }

    /**
     * Set the size of a rectangle
     *
     * @param r the rectangle
     * @param s the new size (as Point2D)
     * @return a new rectangle with the new size
     */
    @CheckReturnValue
    public static Rectangle2D setSize(@Nonnull Rectangle2D r, @Nonnull Point2D s) {
        return new Rectangle2D.Double(r.getMinX(), r.getMinY(), s.getX(), s.getY());
    }

    /**
     * Calculate the center of the rectangle.
     *
     * @param r the rectangle
     * @return the center of the rectangle
     */
    @CheckReturnValue
    public static Point2D center(@Nonnull Rectangle2D r) {
        return new Point2D.Double(r.getCenterX(), r.getCenterY());
    }

    /**
     * Calculate the midpoint of the rectangle.
     *
     * @param r the rectangle
     * @return the midpoint of the rectangle
     */
    @CheckReturnValue
    public static Point2D midPoint(@Nonnull Rectangle2D r) {
        return center(r);
    }

    /**
     * Offset a rectangle by distinct x,y values.
     *
     * @param r the rectangle
     * @param x the horizontal offset
     * @param y the vertical offset
     * @return the offset rectangle
     */
    @CheckReturnValue
    public static Rectangle2D offset(@Nonnull Rectangle2D r, double x, double y) {
        return new Rectangle2D.Double(r.getX() + x, r.getY() + y, r.getWidth(), r.getHeight());
    }

    /**
     * Offset a rectangle by a single value.
     *
     * @param r the rectangle
     * @param o the offset
     * @return the offset rectangle
     */
    @CheckReturnValue
    public static Rectangle2D offset(@Nonnull Rectangle2D r, @Nonnull Point2D o) {
        return offset(r, o.getX(), o.getY());
    }

    /**
     * Inset a rectangle by a single value.
     *
     * @param r the rectangle
     * @param i the inset (positive make it smaller, negative, bigger)
     * @return the inset rectangle
     */
    @CheckReturnValue
    public static Rectangle2D inset(@Nonnull Rectangle2D r, double i) {
        return inset(r, i, i);
    }

    /**
     * Inset a rectangle by distinct x,y values.
     *
     * @param r the rectangle
     * @param h the horzontial inset (positive make it smaller, negative,
     *          bigger)
     * @param v the vertical inset (positive make it smaller, negative, bigger)
     * @return the inset rectangle
     */
    @CheckReturnValue
    public static Rectangle2D inset(@Nonnull Rectangle2D r, double h, double v) {
        return new Rectangle2D.Double(r.getX() + h, r.getY() + v, r.getWidth() - (2 * h), r.getHeight() - (2 * v));
    }

    /**
     * Scale a rectangle.
     *
     * @param r the rectangle
     * @param s the scale
     * @return the scaled rectangle
     */
    //TODO: add test case
    @CheckReturnValue
    public static Rectangle2D scale(@Nonnull Rectangle2D r, double s) {
        return new Rectangle2D.Double(r.getX() * s, r.getY() * s, r.getWidth() * s, r.getHeight() * s);
    }

    /**
     * Center rectangle on point.
     *
     * @param r the rectangle
     * @param p the point
     * @return the Point2D
     */
    @CheckReturnValue
    public static Rectangle2D centerRectangleOnPoint(@Nonnull Rectangle2D r, @Nonnull Point2D p) {
        Rectangle2D result = r.getBounds2D();
        result = offset(r, subtract(p, center(result)));
        return result;
    }

    /**
     * Center rectangle on rectangle.
     *
     * @param r1 the first rectangle
     * @param r2 the second rectangle
     * @return the first rectangle centered on the second
     */
    @CheckReturnValue
    public static Rectangle2D centerRectangleOnRectangle(@Nonnull Rectangle2D r1, @Nonnull Rectangle2D r2) {
        return offset(r1, subtract(center(r2), center(r1)));
    }

    /**
     * Get rectangle at point.
     *
     * @param p      the point
     * @param width  the width
     * @param height the height
     * @return the rectangle
     */
    @CheckReturnValue
    public static Rectangle2D rectangleAtPoint(@Nonnull Point2D p, Double width, Double height) {
        return new Rectangle2D.Double(p.getX(), p.getY(), width, height);
    }

    /**
     * reverse an array of Point2D's
     *
     * @param points the array
     * @return the reversed array
     */
    public static Point2D[] reverse(Point2D[] points) {
        Point2D[] results = new Point2D[points.length];

        List<Point2D> itemList = Arrays.asList(points);
        Collections.reverse(itemList);
        results = itemList.toArray(results);
        return results;
    }

    // recursive routine to plot a cubic Bezier...
    // (also returns distance!)
    private static double plotBezier(
            GeneralPath path,
            @Nonnull Point2D p0,
            @Nonnull Point2D p1,
            @Nonnull Point2D p2,
            @Nonnull Point2D p3,
            int depth,
            double displacement) {
        double result;

        // calculate flatness to determine if we need to recurse...
        double l01 = distance(p0, p1);
        double l12 = distance(p1, p2);
        double l23 = distance(p2, p3);
        double l03 = distance(p0, p3);
        double flatness = (l01 + l12 + l23) / l03;

        // depth prevents stack overflow
        // (I picked 12 because 2^12 = 2048 is larger than most monitors ;-)
        // the flatness comparison value is somewhat arbitrary.
        // (I just kept moving it closer to 1 until I got good results. ;-)
        if ((depth > 12) || (flatness <= 1.001)) {
            Point2D vO = normalize(orthogonal(subtract(p3, p0)), displacement);
            if (path.getCurrentPoint() == null) {   // if this is the 1st point
                Point2D p0P = add(p0, vO);
                path.moveTo(p0P.getX(), p0P.getY());
            }
            Point2D p3P = add(p3, vO);
            path.lineTo(p3P.getX(), p3P.getY());
            result = l03;
        } else {
            // first order midpoints
            Point2D q0 = midPoint(p0, p1);
            Point2D q1 = midPoint(p1, p2);
            Point2D q2 = midPoint(p2, p3);

            // second order midpoints
            Point2D r0 = midPoint(q0, q1);
            Point2D r1 = midPoint(q1, q2);

            // third order midPoint
            Point2D s = midPoint(r0, r1);

            // draw left side Bezier
            result = plotBezier(path, p0, q0, r0, s, depth + 1, displacement);
            // draw right side Bezier
            result += plotBezier(path, s, r1, q2, p3, depth + 1, displacement);
        }
        return result;
    }

    /**
     * Draw a cubic Bezier curve.
     *
     * @param g2 the Graphics2D context to draw to
     * @param p0 origin control point
     * @param p1 first control point
     * @param p2 second control point
     * @param p3 terminating control point
     *
     * @return the length of the Bezier curve
     */
    public static double drawBezier(
            @Nullable Graphics2D g2,
            @Nonnull Point2D p0,
            @Nonnull Point2D p1,
            @Nonnull Point2D p2,
            @Nonnull Point2D p3) {
        GeneralPath path = new GeneralPath();
        double result = plotBezier(path, p0, p1, p2, p3, 0, 0.0);
        if (g2 != null) {
            g2.draw(path);
        }
        return result;
    }

    // recursive routine to plot a Bezier curve...
    // (also returns distance!)
    private static double plotBezier(
            GeneralPath path,
            @Nonnull Point2D points[],
            int depth,
            double displacement) {
        int len = points.length, idx, jdx;
        double result;

        // calculate flatness to determine if we need to recurse...
        double outer_distance = 0;
        for (idx = 1; idx < len; idx++) {
            outer_distance += distance(points[idx - 1], points[idx]);
        }
        double inner_distance = distance(points[0], points[len - 1]);
        double flatness = outer_distance / inner_distance;

        // depth prevents stack overflow
        // (I picked 12 because 2^12 = 2048 is larger than most monitors ;-)
        // the flatness comparison value is somewhat arbitrary.
        // (I just kept moving it closer to 1 until I got good results. ;-)
        if ((depth > 12) || (flatness <= 1.001)) {
            Point2D p0 = points[0], pN = points[len - 1];
            Point2D vO = normalize(orthogonal(subtract(pN, p0)), displacement);
            if (path.getCurrentPoint() == null) {   // if this is the 1st point
                Point2D p0P = add(p0, vO);
                path.moveTo(p0P.getX(), p0P.getY());
            }
            Point2D pNP = add(pN, vO);
            path.lineTo(pNP.getX(), pNP.getY());
            result = inner_distance;
        } else {
            // calculate (len - 1) order of points
            // (zero'th order are the input points)
            Point2D[][] nthOrderPoints = new Point2D[len - 1][];
            for (idx = 0; idx < len - 1; idx++) {
                nthOrderPoints[idx] = new Point2D[len - 1 - idx];
                for (jdx = 0; jdx < len - 1 - idx; jdx++) {
                    if (idx == 0) {
                        nthOrderPoints[idx][jdx] = midPoint(points[jdx], points[jdx + 1]);
                    } else {
                        nthOrderPoints[idx][jdx] = midPoint(nthOrderPoints[idx - 1][jdx], nthOrderPoints[idx - 1][jdx + 1]);
                    }
                }
            }

            // collect left points
            Point2D[] leftPoints = new Point2D[len];
            leftPoints[0] = points[0];
            for (idx = 0; idx < len - 1; idx++) {
                leftPoints[idx + 1] = nthOrderPoints[idx][0];
            }
            // draw left side Bezier
            result = plotBezier(path, leftPoints, depth + 1, displacement);

            // collect right points
            Point2D[] rightPoints = new Point2D[len];
            for (idx = 0; idx < len - 1; idx++) {
                rightPoints[idx] = nthOrderPoints[len - 2 - idx][idx];
            }
            rightPoints[idx] = points[len - 1];

            // draw right side Bezier
            result += plotBezier(path, rightPoints, depth + 1, displacement);
        }
        return result;
    }

    /*
     * Plot a Bezier curve.
     *
     * @param g2 the Graphics2D context to draw to
     * @param p  the control points
     * @param displacement right/left to draw a line parallel to the Bezier
     * @param fillFlag     false to draw / true to fill
     * @return the length of the Bezier curve
     */
    private static double plotBezier(
            Graphics2D g2,
            @Nonnull Point2D p[],
            double displacement,
            boolean fillFlag) {
        double result;
        GeneralPath path = new GeneralPath();
        if (p.length == 4) {    // draw cubic bezier?
            result = plotBezier(path, p[0], p[1], p[2], p[3], 0, displacement);
        } else {    // (nope)
            result = plotBezier(path, p, 0, displacement);
        }
        if (fillFlag) {
            g2.fill(path);
        } else {
            g2.draw(path);
        }
        return result;
    }

    /**
     * Get the path for a Bezier curve.
     *
     * @param p            control points
     * @param displacement right/left to draw a line parallel to the Bezier
     * @return the length of the Bezier curve
     */
    public static GeneralPath getBezierPath(
            @Nonnull Point2D p[],
            double displacement) {
        GeneralPath result = new GeneralPath();
        if (p.length == 4) {    // draw cubic bezier?
            plotBezier(result, p[0], p[1], p[2], p[3], 0, displacement);
        } else {    // (nope)
            plotBezier(result, p, 0, displacement);
        }
        return result;
    }

    /**
     * Get the path for a Bezier curve.
     *
     * @param p control points
     * @return the length of the Bezier curve
     */
    public static GeneralPath getBezierPath(@Nonnull Point2D p[]) {
        return getBezierPath(p, 0);
    }

    /**
     * Draw a Bezier curve
     *
     * @param g2           the Graphics2D context to draw to
     * @param p            the control points
     * @param displacement right/left to draw a line parallel to the Bezier
     * @return the length of the Bezier curve
     */
    public static double drawBezier(
            @Nullable Graphics2D g2,
            @Nonnull Point2D p[],
            double displacement) {
        return plotBezier(g2, p, displacement, false);
    }

    /**
     * Fill a Bezier curve.
     *
     * @param g2           the Graphics2D context to draw to
     * @param p            the control points
     * @param displacement right/left to draw a line parallel to the Bezier
     * @return the length of the Bezier curve
     */
    public static double fillBezier(
            Graphics2D g2,
            @Nonnull Point2D p[],
            double displacement) {
        return plotBezier(g2, p, displacement, true);
    }

    /**
     * Draw a Bezier curve.
     *
     * @param g2 the Graphics2D context to draw to
     * @param p  the control points
     * @return the length of the Bezier curve
     */
    public static double drawBezier(@Nullable Graphics2D g2, @Nonnull Point2D p[]) {
        return drawBezier(g2, p, 0.0);
    }

    /**
     * Fill a Bezier curve.
     *
     * @param g2 the Graphics2D context to draw to
     * @param p  the control points
     * @return the length of the Bezier curve
     */
    public static double fillBezier(Graphics2D g2, @Nonnull Point2D p[]) {
        return plotBezier(g2, p, 0.0, true);
    }

    /**
     * computer the bounds of a Bezier curve.
     *
     * @param p the control points
     * @return the bounds of the Bezier curve
     */
    public static Rectangle2D getBezierBounds(@Nonnull Point2D p[]) {
        return getBezierPath(p).getBounds2D();
    }

    /**
     * Find intersection of two lines.
     *
     * @param p1 the first point on the first line
     * @param p2 the second point on the first line
     * @param p3 the first point on the second line
     * @param p4 the second point on the second line
     * @return the intersection point of the two lines or null if one doesn't
     *         exist
     */
    @CheckReturnValue
    public static Point2D intersect(
            @Nonnull Point2D p1,
            @Nonnull Point2D p2,
            @Nonnull Point2D p3,
            @Nonnull Point2D p4) {
        Point2D result = null;  // assume failure (pessimist!)

        Point2D delta31 = MathUtil.subtract(p3, p1);    //p
        Point2D delta21 = MathUtil.subtract(p2, p1);    //q
        Point2D delta43 = MathUtil.subtract(p4, p3);    //r

        double det = delta21.getX() * delta43.getY() - delta21.getY() * delta43.getX();
        if (!MathUtil.equals(det, 0.0)) {
            double t = (delta21.getY() * delta31.getX() - delta21.getX() * delta31.getY()) / det;
            result = lerp(p1, p2, t);
        }
        return result;
    }

    /**
     * get (signed) distance p3 is from line segment defined by p1 and p2
     *
     * @param p1 the first point on the line segment
     * @param p2 the second point on the line segment
     * @param p3 the point whose distance from the line segment you wish to
     *           calculate
     * @return the distance (note: plus/minus determines the (left/right) side
     *         of the line)
     */
    public static double distance(
            @Nonnull Point2D p1,
            @Nonnull Point2D p2,
            @Nonnull Point2D p3) {
        double p1X = p1.getX(), p1Y = p1.getY();
        double p2X = p2.getX(), p2Y = p2.getY();
        double p3X = p3.getX(), p3Y = p3.getY();

        double a = p1Y - p2Y;
        double b = p2X - p1X;
        double c = (p1X * p2Y) - (p2X * p1Y);

        return (a * p3X + b * p3Y + c) / Math.sqrt(a * a + b * b);
    }

    /*==========*\
    |* polygons *|
    \*==========*/

    /**
     * return average point in points
     *
     * @param points to average
     * @return the average point
     */
    public static Point2D midPoint(List<Point2D> points) {
        Point2D result = zeroPoint2D();
        for (Point2D point : points) {
            result = add(result, point);
        }
        result = divide(result, points.size());
        return result;
    }

    /**
     * @param pointT the point
     * @param points the polygon
     * @return true if pointT is in the polygon made up of the points
     */
    public static boolean isPointInPolygon(Point2D pointT, List<Point2D> points) {
        boolean result = false;

        Double pointT_x = pointT.getX(), pointT_y = pointT.getY();

        int n = points.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            Point2D pointI = points.get(i), pointJ = points.get(j);
            Double pointI_x = pointI.getX(), pointI_y = pointI.getY();
            Double pointJ_x = pointJ.getX(), pointJ_y = pointJ.getY();

            if ((pointI_y > pointT_y) != (pointJ_y > pointT_y)
                    && (pointT_x < (pointJ_x - pointI_x) * (pointT_y - pointI_y) / (pointJ_y - pointI_y) + pointI_x)) {
                result = !result;
            }
        }
        return result;
    }

    /**
     * compute convex hull (outline of polygon)
     *
     * @param points of the polygon
     * @return points of the convex hull
     */
    public static List<Point2D> convexHull(List<Point2D> points) {
        if (points.isEmpty()) {
            return points;
        }

        points.sort((p1, p2) -> (int) Math.signum(p1.getX() - p2.getX()));

        List<Point2D> results = new ArrayList<>();

        // lower hull
        for (Point2D pt : points) {
            while (results.size() > 1) {
                int n = results.size();
                if (isCounterClockWise(results.get(n - 2), results.get(n - 1), pt)) {
                    break;
                } else {
                    results.remove(n - 1);
                }
            }
            results.add(pt);
        }

        // upper hull
        int t = results.size(); //terminate while loop when results are this size
        for (int i = points.size() - 1; i >= 0; i--) {
            Point2D pt = points.get(i);

            while (results.size() > t) {
                int n = results.size();
                if (isCounterClockWise(results.get(n - 2), results.get(n - 1), pt)) {
                    break;
                } else {
                    results.remove(n - 1);
                }
            }
            results.add(pt);
        }

        results.remove(results.size() - 1);
        return results;
    }

    /**
     * isCounterClockWise
     *
     * @param a the first point
     * @param b the second point
     * @param c the third point
     * @return true if the three points make a counter-clockwise turn
     */
    public static boolean isCounterClockWise(Point2D a, Point2D b, Point2D c) {
        return ((b.getX() - a.getX()) * (c.getY() - a.getY())) > ((b.getY() - a.getY()) * (c.getX() - a.getX()));
    }

    // private transient final static Logger log = LoggerFactory.getLogger(MathUtil.class);
}
