package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;

/**
 * LayoutTileMath
 */
public class LayoutTileMath {

    /**
     * Calculate the end point of a straight segment given a start point,
     * length, and orientation. Uses basic trigonometry to compute the endpoint
     * coordinates.
     *
     * @param startPoint  the starting point coordinates
     * @param length      the length of the straight segment in layout units
     * @param orientation the orientation angle in degrees (0 = east, 90 =
     *                    north, 180 = west, 270 = south)
     * @return the calculated endpoint as a Point2D
     */
    public static Point2D calcStraightEndpoint(Point2D startPoint, double length, double orientation) {
        // Convert angle from degrees to radians
        double angleRadians = Math.toRadians(orientation);

        // Calculate endpoint using basic trigonometry
        double endX = startPoint.getX() + length * Math.cos(angleRadians);
        double endY = startPoint.getY() + length * Math.sin(angleRadians);

        return new Point2D.Double(endX, endY);
    }

    /**
     * Calculate the end point of a curved segment given start point,
     * orientation, radius, arc, and curve face. Uses basic trigonometry to
     * compute the endpoint coordinates along a circular arc.
     *
     * @param startPoint  the starting point coordinates
     * @param orientation the initial orientation angle in degrees (0 = east, 90
     *                    = north, 180 = west, 270 = south)
     * @param curveRadius the radius of the curve in layout units
     * @param curveArc    the arc angle of the curve in degrees (positive
     *                    values)
     * @param curveFace   the direction of the curve: true for right turn, false
     *                    for left turn
     * @return the calculated endpoint as a Point2D
     */
    public static Point2D calcCurveEndpoint(Point2D startPoint, double orientation, double curveRadius,
            double curveArc, boolean curveFace) {
        // Convert angles from degrees to radians
        double orientationRadians = Math.toRadians(orientation);
        double arcRadians = Math.toRadians(curveArc);

        // Determine the direction of the curve (right = clockwise, left = counter-clockwise)
        double curveDirection = curveFace ? -1.0 : 1.0; // right = -1 (clockwise), left = 1 (counter-clockwise)

        // Calculate the center of the circular arc
        // The center is perpendicular to the initial orientation at distance = radius
        double centerAngle = orientationRadians + curveDirection * Math.PI / 2.0;
        double centerX = startPoint.getX() + curveRadius * Math.cos(centerAngle);
        double centerY = startPoint.getY() + curveRadius * Math.sin(centerAngle);

        // Calculate the final angle after traversing the arc
        double finalAngle = centerAngle + Math.PI + curveDirection * arcRadians;

        // Calculate the endpoint relative to the center
        double endX = centerX + curveRadius * Math.cos(finalAngle);
        double endY = centerY + curveRadius * Math.sin(finalAngle);

        return new Point2D.Double(endX, endY);
    }

    /**
     * Calculate the orientation angle of a straight segment from start point to
     * end point. Uses basic trigonometry to compute the angle based on the
     * coordinate difference.
     *
     * @param startPoint the starting point coordinates
     * @param endPoint   the ending point coordinates
     * @return the orientation angle in degrees (0 = east, 90 = north, 180 =
     *         west, 270 = south)
     */
    public static double calcStraightOrientation(Point2D startPoint, Point2D endPoint) {
        // Calculate the difference vector
        double deltaX = endPoint.getX() - startPoint.getX();
        double deltaY = endPoint.getY() - startPoint.getY();

        // Calculate angle using atan2 and convert from radians to degrees
        double angleRadians = Math.atan2(deltaY, deltaX);
        double angleDegrees = Math.toDegrees(angleRadians);

        // Normalize angle to 0-360 range
        if (angleDegrees < 0) {
            angleDegrees += 360.0;
        }

        return angleDegrees;
    }

    /**
     * Calculate the initial orientation angle of a curved segment from start
     * point to end point. Uses geometric analysis of the circular arc to
     * determine the starting orientation.
     *
     * @param startPoint  the starting point coordinates
     * @param endPoint    the ending point coordinates
     * @param curveRadius the radius of the curve in layout units
     * @param curveFace   the direction of the curve: true for right turn, false
     *                    for left turn
     * @return the initial orientation angle in degrees (0 = east, 90 = north,
     *         180 = west, 270 = south)
     */
    public static double calcCurveOrientation(Point2D startPoint, Point2D endPoint, double curveRadius,
            boolean curveFace) {
        // Calculate the chord vector from start to end
        double chordX = endPoint.getX() - startPoint.getX();
        double chordY = endPoint.getY() - startPoint.getY();
        double chordLength = Math.sqrt(chordX * chordX + chordY * chordY);

        // Calculate the chord angle
        double chordAngle = Math.atan2(chordY, chordX);

        // Calculate the arc angle (central angle)
        double halfChord = chordLength / 2.0;
        double arcAngle = 2.0 * Math.asin(halfChord / curveRadius);

        // Determine the initial orientation based on curve direction
        double curveDirection = curveFace ? -1.0 : 1.0; // right = -1, left = 1
        double initialAngle = chordAngle - curveDirection * (Math.PI / 2.0 - arcAngle / 2.0);

        // Convert to degrees and normalize to 0-360 range
        double angleDegrees = Math.toDegrees(initialAngle);
        if (angleDegrees < 0) {
            angleDegrees += 360.0;
        }
        if (angleDegrees >= 360.0) {
            angleDegrees -= 360.0;
        }

        return angleDegrees;
    }

}
