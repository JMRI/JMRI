package jmri.util;

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *
 * useful math methods
 *
 * @author geowar Copyright 2017
 */
public final class MathUtil {

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
     * Convert Point to Point2D
     *
     * @param p the Point
     * @return the Point2D
     */
    @CheckReturnValue
    public static Point2D pointToPoint2D(@Nonnull Point p) {
        return new Point2D.Double(p.x, p.y);
    }

    /**
     * Convert Point2D to Point
     *
     * @param p the Point
     * @return the Point2D
     */
    @CheckReturnValue
    public static Point point2DToPoint(@Nonnull Point2D p) {
        return new Point((int) p.getX(), (int) p.getY());
    }

    /**
     * @param p the point
     * @return true if p1 is equal to zeroPoint2D
     */
    public static boolean isEqualToZeroPoint2D(@Nonnull Point2D p) {
        return p.equals(zeroPoint2D);
    }

    /**
     * return the minimum coordinates of two points
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
     * return the maximum coordinates of two points
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
     * return the coordinates of a point pinned between two other points
     *
     * @param pA the first point
     * @param pB the second point
     * @param pC the third point
     * @return the coordinated of pA pined between pB and pC
     */
    @CheckReturnValue
    public static Point2D pin(@Nonnull Point2D pA, @Nonnull Point2D pB, @Nonnull Point2D pC) {
        return min(max(pA, min(pB, pC)), max(pB, pC));
    }

    /**
     * add two points
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
     * subtract two points
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
     * multiply a point times a scalar
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
     * multiply a point times two scalar
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
     * multiply a scalar times a point
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
     * multiply a point times a point
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
     * divide a point by a scalar
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
     * divide a point by two scalars
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
     * offset a point by two scalars
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
     * rotate x and y coordinates (by radians)
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
     * rotate x and y coordinates (by degrees)
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
     * rotate a point (by radians)
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
     * rotate a point (by degrees)
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
     * rotate a point around another point (by radians)
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
     * rotate a point around another point (by degrees)
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
     * dot product of two points (vectors)
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
     * calculate the length squared of a point (vector)
     *
     * @param p the point (vector)
     * @return the length squared of the point (vector)
     */
    @CheckReturnValue
    public static double lengthSquared(@Nonnull Point2D p) {
        return dot(p, p);
    }

    /**
     * calculate the length of a point (vector)
     *
     * @param p the point (vector)
     * @return the length of the point (vector)
     */
    @CheckReturnValue
    public static double length(@Nonnull Point2D p) {
        return Math.hypot(p.getX(), p.getY());
    }

    /**
     * calculate the distance between two points
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
     * normalize a point
     *
     * @param p the point
     * @return the normalized point
     */
    @CheckReturnValue
    public static Point2D normalize(@Nonnull Point2D p) {
        Point2D result = p;
        double length = length(p);
        if (length >= 0.001) {
            result = divide(p, length);
        }
        return result;
    }

    /**
     * compute the angle (direction in radians) from point 1 to point 2
     *
     * @param p1 the first Point2D
     * @param p2 the second Point2D
     * @return the angle in radians
     */
    @CheckReturnValue
    public static double computeAngleRAD(@Nonnull Point2D p1, @Nonnull Point2D p2) {
        Point2D delta = subtract(p1, p2);
        return Math.atan2(delta.getX(), delta.getY());
    }

    /**
     * compute the angle (direction in degrees) from point 1 to point 2
     *
     * @param p1 the first Point2D
     * @param p2 the second Point2D
     * @return the angle in degrees
     */
    @CheckReturnValue
    public static double computeAngleDEG(@Nonnull Point2D p1, @Nonnull Point2D p2) {
        return Math.toDegrees(computeAngleRAD(p1, p2));
    }

    /**
     * calculate the linear interpolation between two integers
     *
     * @param a the first number
     * @param b the second number
     * @param t the fraction (between 0 and 1)
     * @return the linear interpolation between a and b for t
     */
    @CheckReturnValue
    public static int lerp(int a, int b, double t) {
        return (int) lerp((double) a, (double) b, t);
    }

    /**
     * calculate the linear interpolation between two doubles
     *
     * @param a the first number
     * @param b the second number
     * @param t the fraction (between 0 and 1)
     * @return the linear interpolation between a and b for t
     */
    @CheckReturnValue
    public static double lerp(double a, double b, double t) {
        return ((1.0 - t) * a) + (t * b);
    }

    /**
     * calculate the linear interpolation between two Doubles
     *
     * @param a the first number
     * @param b the second number
     * @param t the fraction (between 0 and 1)
     * @return the linear interpolation between a and b for t
     */
    @CheckReturnValue
    public static Double lerp(@Nonnull Double a, @Nonnull Double b, @Nonnull Double t) {
        return ((1.0 - t) * a) + (t * b);
    }

    /**
     * calculate the linear interpolation between two points
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
     * round value to granular increment
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
     * round point to horzontal and vertical granular increments
     *
     * @param p  the point to granulize
     * @param gH the horzontal granularity
     * @param gV the vertical granularity
     * @return the point granulized to the granularity
     */
    @CheckReturnValue
    public static Point2D granulize(@Nonnull Point2D p, double gH, double gV) {
        return new Point2D.Double(granulize(p.getX(), gH), granulize(p.getY(), gV));
    }

    /**
     * round point to granulur increment
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
     * calculate the midpoint between two points
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
     * calculate the point 1/3 of the way between two points
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
     * calculate the point 2/3 of the way between two points
     *
     * @param pA the first point
     * @param pB the second point
     * @return the point two thirds of the way from pA to pB
     */
    @CheckReturnValue
    public static Point2D twoThirdPoint(@Nonnull Point2D pA, @Nonnull Point2D pB) {
        return lerp(pA, pB, 1.0 / 3.0);
    }

    /**
     * calculate the point 1/4 of the way between two points
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
     * calculate the point 3/4 of the way between two points
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
     * Wrap an int between two values (for example +/- 180 or 0-360 degrees)
     *
     * @param inValue the value
     * @param inMin   the lowest value
     * @param inMax   the highest value
     * @return the value wrapped between the lowest and highest values Note:
     *         THIS IS NOT A PIN OR TRUNCATE; VALUES WRAP AROUND BETWEEN MIN AND
     *         MAX (And yes, this works correctly with negative numbers)
     */
    @CheckReturnValue
    public static int wrap(int inValue, int inMin, int inMax) {
        int valueRange = inMax - inMin;
        return inMin + ((((inValue - inMin) % valueRange) + valueRange) % valueRange);
    }

    /**
     * Wrap a double between two values (for example +/- 180 or 0-360 degrees)
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
     * wrap a value between +/-180
     *
     * @param inValue the value
     * @return the value wrapped between -180 and +180
     */
    @CheckReturnValue
    public static double wrapPM180(double inValue) {
        return wrap(inValue, -180.0, +180.0);
    }

    /**
     * wrap a value between +/-360
     *
     * @param inValue the value
     * @return the value wrapped between -360 and +360
     */
    @CheckReturnValue
    public static double wrapPM360(double inValue) {
        return wrap(inValue, -360.0, +360.0);
    }

    /**
     * wrap a value between 0 and 360
     *
     * @param inValue the value
     * @return the value wrapped between -360 and +360
     */
    @CheckReturnValue
    public static double wrap360(double inValue) {
        return wrap(inValue, 0.0, +360.0);
    }

    /**
     * wrap an angle between 0 and 360
     *
     * @param a the angle
     * @return the angle wrapped between 0 and 360
     */
    @CheckReturnValue
    public static double normalizeAngleDEG(double a) {
        return wrap360(a);
    }

    /**
     * calculate the relative difference (+/-180) between two angles
     *
     * @param a the first angle
     * @param b the second angle
     * @return the relative difference between the two angles
     */
    @CheckReturnValue
    public static double diffAngleDEG(double a, double b) {
        return wrapPM180(a - b);
    }

    /**
     * calculate the absolute difference (0-180) between two angles
     *
     * @param a the first angle
     * @param b the second angle
     * @return the absolute difference between the two angles
     */
    @CheckReturnValue
    public static double absDiffAngleDEG(double a, double b) {
        return Math.abs(diffAngleDEG(a, b));
    }

    /**
     * pin a value between min and max
     *
     * @param inValue the value
     * @param inMin   the min
     * @param inMax   the max
     * @return the value pinned between the min and max values
     */
    @CheckReturnValue
    public static int pin(int inValue, int inMin, int inMax) {
        return Math.min(Math.max(inValue, inMin), inMax);
    }

    /**
     * pin a value between min and max
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
     * Convert Rectangle to Rectangle2D
     *
     * @param r the Rectangle
     * @return the Rectangle2D
     * @deprecated since 4.9.4; use
     * {@link #rectangleToRectangle2D(java.awt.Rectangle)} instead
     */
    @Deprecated
    @CheckReturnValue
    public static Rectangle2D rectangle2DForRectangle(@Nonnull Rectangle r) {
        return rectangleToRectangle2D(r);
    }

    /**
     * Convert Rectangle to Rectangle2D
     *
     * @param r the Rectangle
     * @return the Rectangle2D
     */
    @CheckReturnValue
    public static Rectangle2D rectangleToRectangle2D(@Nonnull Rectangle r) {
        return new Rectangle2D.Double(r.x, r.y, r.width, r.height);
    }

    /**
     * Convert Rectangle2D to Rectangle
     *
     * @param r the Rectangle
     * @return the Rectangle2D
     * @deprecated since 4.9.4; use
     * {@link #rectangle2DToRectangle(java.awt.geom.Rectangle2D)} instead
     */
    @Deprecated
    @CheckReturnValue
    public static Rectangle rectangleForRectangle2D(@Nonnull Rectangle2D r) {
        return rectangle2DToRectangle(r);
    }

    /**
     * Convert Rectangle2D to Rectangle
     *
     * @param r the Rectangle
     * @return the Rectangle2D
     */
    @CheckReturnValue
    public static Rectangle rectangle2DToRectangle(@Nonnull Rectangle2D r) {
        return new Rectangle((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
    }

    /**
     * returns the origin (top left) of the rectangle
     *
     * @param r the rectangle
     * @return the origin of the rectangle
     */
    @CheckReturnValue
    public static Point2D origin(@Nonnull Rectangle2D r) {
        return new Point2D.Double(r.getX(), r.getY());
    }

    /**
     * returns the size of the rectangle
     *
     * @param r the rectangle
     * @return the size of the rectangle
     */
    @CheckReturnValue
    public static Point2D size(@Nonnull Rectangle2D r) {
        return new Point2D.Double(r.getWidth(), r.getHeight());
    }

    /**
     * calculate the center of the rectangle
     *
     * @param r the rectangle
     * @return the center of the rectangle
     */
    @CheckReturnValue
    public static Point2D center(@Nonnull Rectangle2D r) {
        return new Point2D.Double(r.getCenterX(), r.getCenterY());
    }

    /**
     * calculate the midpoint of the rectangle
     *
     * @param r the rectangle
     * @return the midpoint of the rectangle
     */
    @CheckReturnValue
    public static Point2D midPoint(@Nonnull Rectangle2D r) {
        return center(r);
    }

    /**
     * offset a rectangle
     *
     * @param r the rectangle
     * @param x the horzontial offset
     * @param y the vertical offset
     * @return the offset rectangle
     */
    @CheckReturnValue
    public static Rectangle2D offset(@Nonnull Rectangle2D r, double x, double y) {
        return new Rectangle2D.Double(r.getX() + x, r.getY() + y, r.getWidth(), r.getHeight());
    }

    /**
     * offset a rectangle
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
     * inset a rectangle
     *
     * @param r the rectangle
     * @param i the inset (positive make it smaller, negative, bigger)
     * @return the inset rectangle
     */
    @CheckReturnValue
    public static Rectangle2D inset(@Nonnull Rectangle2D r, double i) {
        return new Rectangle2D.Double(r.getX() + i, r.getY() + i, r.getWidth() - (2 * i), r.getHeight() - (2 * i));
    }

    /**
     * inset a rectangle
     *
     * @param r the rectangle
     * @param h the horzontial inset (positive make it smaller, negative, bigger)
     * @param v the vertical inset (positive make it smaller, negative, bigger)
     * @return the inset rectangle
     */
    @CheckReturnValue
    public static Rectangle2D inset(@Nonnull Rectangle2D r, double h, double v) {
        return new Rectangle2D.Double(r.getX() + h, r.getY() + v, r.getWidth() - (2 * h), r.getHeight() - (2 * v));
    }

    /**
     * scale a rectangle
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
     * center rectangle on point
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
     * center rectangle on rectangle
     *
     * @param r1 the first rectangle
     * @param r2 the second rectangle
     * @return the first rectangle centered on the second
     */
    @CheckReturnValue
    public static Rectangle2D centerRectangleOnRectangle(@Nonnull Rectangle2D r1, @Nonnull Rectangle2D r2) {
        return offset(r1, subtract(center(r2), center(r1)));
    }

    // recursive routine to draw a cubic Bezier...
    // (also returns distance!)
    private static double drawBezier(Graphics2D g2,
            @Nonnull Point2D p0, @Nonnull Point2D p1, @Nonnull Point2D p2,
            @Nonnull Point2D p3, int depth) {
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
            g2.draw(new Line2D.Double(p0, p3));
            result = l03;
        } else {
            // first order midpoints
            Point2D q0 = midPoint(p0, p1);
            Point2D q1 = midPoint(p1, p2);
            Point2D q2 = midPoint(p2, p3);

            // second order midpoints
            Point2D r0 = midPoint(q0, q1);
            Point2D r1 = midPoint(q1, q2);

            // oneThirdPoint order midPoint
            Point2D s = midPoint(r0, r1);

            // draw left side Bezier
            result = drawBezier(g2, p0, q0, r0, s, depth + 1);
            // draw right side Bezier
            result += drawBezier(g2, s, r1, q2, p3, depth + 1);
        }
        return result;
    }

    /**
     * Draw a cubic Bezier curve
     *
     * @param g2 the Graphics2D to draw to
     * @param p0 origin control point
     * @param p1 first control point
     * @param p2 second control point
     * @param p3 terminating control point
     * @return the length of the Bezier curve
     */
    public static double drawBezier(Graphics2D g2, @Nonnull Point2D p0, @Nonnull Point2D p1, @Nonnull Point2D p2, @Nonnull Point2D p3) {
        return drawBezier(g2, p0, p1, p2, p3, 0);
    }

    // recursive routine to draw a Bezier curve...
    // (also returns distance!)
    private static double drawBezier(Graphics2D g2, @Nonnull Point2D points[], int depth) {
        int len = points.length, idx, jdx;
        double result;

        // calculate flatness to determine if we need to recurse...
        double outer_distance = 0;
        for (idx = 1; idx < len; idx++) {
            outer_distance += MathUtil.distance(points[idx - 1], points[idx]);
        }
        double inner_distance = MathUtil.distance(points[0], points[len - 1]);
        double flatness = outer_distance / inner_distance;

        // depth prevents stack overflow
        // (I picked 12 because 2^12 = 2048 is larger than most monitors ;-)
        // the flatness comparison value is somewhat arbitrary.
        // (I just kept moving it closer to 1 until I got good results. ;-)
        if ((depth > 12) || (flatness <= 1.001)) {
            g2.draw(new Line2D.Double(points[0], points[len - 1]));
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
            result = drawBezier(g2, leftPoints, depth + 1);

            // collect right points
            Point2D[] rightPoints = new Point2D[len];
            for (idx = 0; idx < len - 1; idx++) {
                rightPoints[idx] = nthOrderPoints[len - 2 - idx][idx];
            }
            rightPoints[idx] = points[len - 1];

            // draw right side Bezier
            result += drawBezier(g2, rightPoints, depth + 1);
        }
        return result;
    }

    /**
     * Draw a Bezier curve
     *
     * @param g2  the Graphics2D to draw to
     * @param p[] control points
     * @return the length of the Bezier curve
     */
    public static double drawBezier(Graphics2D g2, @Nonnull Point2D p[]) {
        if (p.length == 4) {    // draw cubic bezier?
            return drawBezier(g2, p[0], p[1], p[2], p[3], 0);
        } else {    // (nope)
            return drawBezier(g2, p, 0);
        }
    }
}
