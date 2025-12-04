package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LayoutTileMath class.
 *
 * @author JMRI
 */
@Tag("tracktiles")
public class LayoutTileMathTest {

    private static final double TOLERANCE = 0.001; // 1mm tolerance for floating point comparisons

    /**
     * Test that 12 consecutive 30-degree curve segments with the same curveFace
     * and a 360mm radius form a complete circle and return to the original
     * starting point. This validates the geometric accuracy of the
     * calcCurveEndpoint method.
     */
    @Test
    public void testTwelveCurvesFormCompleteCircle() {
        // Test parameters: 12 curves of 30 degrees each = 360 degrees total
        double radius = 360.0; // 360mm radius
        double arcAngle = 30.0; // 30 degrees per segment
        boolean curveFace = true; // right curves

        // Starting point and orientation
        Point2D startPoint = new Point2D.Double(0.0, 0.0);
        double orientation = 0.0; // start facing east

        Point2D currentPoint = startPoint;
        double currentOrientation = orientation;

        // Execute 12 consecutive curve calculations
        for (int i = 0; i < 12; i++) {
            // Calculate the endpoint of this curve segment
            Point2D nextPoint = LayoutTileMath.calcCurveEndpoint(
                    currentPoint, currentOrientation, radius, arcAngle, curveFace);

            // Update orientation for next segment
            // For right curves, orientation decreases by the arc angle
            currentOrientation -= arcAngle;
            if (currentOrientation < 0) {
                currentOrientation += 360.0;
            }

            // Move to the next point
            currentPoint = nextPoint;
        }

        // After 12 segments of 30 degrees each (360 degrees total),
        // we should be back at the original starting point
        assertEquals(startPoint.getX(), currentPoint.getX(), TOLERANCE,
                "X coordinate should return to start after complete circle");
        assertEquals(startPoint.getY(), currentPoint.getY(), TOLERANCE,
                "Y coordinate should return to start after complete circle");

        // Orientation should also return to original (0 degrees)
        assertEquals(0.0, currentOrientation, TOLERANCE,
                "Orientation should return to start after complete circle");
    }

    /**
     * Test that 12 consecutive 30-degree left curve segments with a 360mm
     * radius also form a complete circle and return to the original starting
     * point.
     */
    @Test
    public void testTwelveLeftCurvesFormCompleteCircle() {
        // Test parameters: 12 left curves of 30 degrees each = 360 degrees total
        double radius = 360.0; // 360mm radius
        double arcAngle = 30.0; // 30 degrees per segment
        boolean curveFace = false; // left curves

        // Starting point and orientation
        Point2D startPoint = new Point2D.Double(0.0, 0.0);
        double orientation = 0.0; // start facing east

        Point2D currentPoint = startPoint;
        double currentOrientation = orientation;

        // Execute 12 consecutive curve calculations
        for (int i = 0; i < 12; i++) {
            // Calculate the endpoint of this curve segment
            Point2D nextPoint = LayoutTileMath.calcCurveEndpoint(
                    currentPoint, currentOrientation, radius, arcAngle, curveFace);

            // Update orientation for next segment
            // For left curves, orientation increases by the arc angle
            currentOrientation += arcAngle;
            if (currentOrientation >= 360.0) {
                currentOrientation -= 360.0;
            }

            // Move to the next point
            currentPoint = nextPoint;
        }

        // After 12 segments of 30 degrees each (360 degrees total),
        // we should be back at the original starting point
        assertEquals(startPoint.getX(), currentPoint.getX(), TOLERANCE,
                "X coordinate should return to start after complete circle (left curves)");
        assertEquals(startPoint.getY(), currentPoint.getY(), TOLERANCE,
                "Y coordinate should return to start after complete circle (left curves)");

        // Orientation should also return to original (0 degrees)
        assertEquals(0.0, currentOrientation, TOLERANCE,
                "Orientation should return to start after complete circle (left curves)");
    }

    /**
     * Test basic calcStraightEndpoint functionality.
     */
    @Test
    public void testCalcStraightEndpoint() {
        Point2D start = new Point2D.Double(0.0, 0.0);
        double length = 100.0;

        // Test east direction (0 degrees)
        Point2D end = LayoutTileMath.calcStraightEndpoint(start, length, 0.0);
        assertEquals(100.0, end.getX(), TOLERANCE, "East direction X coordinate");
        assertEquals(0.0, end.getY(), TOLERANCE, "East direction Y coordinate");

        // Test north direction (90 degrees)
        end = LayoutTileMath.calcStraightEndpoint(start, length, 90.0);
        assertEquals(0.0, end.getX(), TOLERANCE, "North direction X coordinate");
        assertEquals(100.0, end.getY(), TOLERANCE, "North direction Y coordinate");
    }

    /**
     * Test basic calcStraightOrientation functionality.
     */
    @Test
    public void testCalcStraightOrientation() {
        Point2D start = new Point2D.Double(0.0, 0.0);

        // Test east direction
        Point2D end = new Point2D.Double(100.0, 0.0);
        double orientation = LayoutTileMath.calcStraightOrientation(start, end);
        assertEquals(0.0, orientation, TOLERANCE, "East orientation");

        // Test north direction
        end = new Point2D.Double(0.0, 100.0);
        orientation = LayoutTileMath.calcStraightOrientation(start, end);
        assertEquals(90.0, orientation, TOLERANCE, "North orientation");
    }

    /**
     * Test that calcCurveOrientation and calcCurveEndpoint are consistent.
     * Given start and end points, calcCurveOrientation should return an orientation
     * that when used with calcCurveEndpoint produces the correct endpoint.
     */
    @Test
    public void testCurveOrientationAndEndpointConsistency() {
        Point2D start = new Point2D.Double(0.0, 0.0);
        Point2D expectedEnd = new Point2D.Double(10.0, 10.0);
        double radius = 360.0;
        boolean curveFace = true;

        // Calculate what orientation should be used to connect start to expectedEnd
        double orientation = LayoutTileMath.calcCurveOrientation(start, expectedEnd, radius, curveFace);

        // Calculate the arc angle from the chord length
        double chordLength = Math.sqrt(
                Math.pow(expectedEnd.getX() - start.getX(), 2) +
                        Math.pow(expectedEnd.getY() - start.getY(), 2));
        double arcAngle = Math.toDegrees(2.0 * Math.asin(chordLength / (2.0 * radius)));

        // Use calcCurveEndpoint to see if we get back to expectedEnd
        Point2D calculatedEnd = LayoutTileMath.calcCurveEndpoint(start, orientation, radius, arcAngle, curveFace);

        assertEquals(expectedEnd.getX(), calculatedEnd.getX(), TOLERANCE,
                "Calculated endpoint X should match expected");
        assertEquals(expectedEnd.getY(), calculatedEnd.getY(), TOLERANCE,
                "Calculated endpoint Y should match expected");
    }

    /**
     * Test bidirectional consistency: create a curve segment, then prove we can
     * work backwards from either end to reconstruct the same circle geometry.
     * This demonstrates that LayoutTileMath works correctly when constructing
     * circles from either edge of the same initial circle segment.
     */
    @Test
    public void testBidirectionalCurveConsistency() {
        // Create an initial curve segment
        Point2D pointA = new Point2D.Double(0.0, 0.0);
        double initialOrientation = 45.0; // northeast direction
        double radius = 360.0;
        double arcAngle = 30.0; // 30-degree arc
        boolean curveFace = true; // right turn

        // Calculate point B using calcCurveEndpoint
        Point2D pointB = LayoutTileMath.calcCurveEndpoint(pointA, initialOrientation, radius, arcAngle, curveFace);

        // Test 1: Calculate orientation from A to B, should match our initial orientation
        double calculatedOrientationAB = LayoutTileMath.calcCurveOrientation(pointA, pointB, radius, curveFace);
        assertEquals(initialOrientation, calculatedOrientationAB, TOLERANCE,
                "Calculated orientation A->B should match initial orientation");

        // Test 2: Calculate orientation from B to A (reverse direction)
        // For the reverse direction, we need to flip the curve face
        double calculatedOrientationBA = LayoutTileMath.calcCurveOrientation(pointB, pointA, radius, !curveFace);
        
        // The reverse orientation should be the final orientation of the original curve + 180°
        double expectedReverseOrientation = (initialOrientation + (curveFace ? -arcAngle : arcAngle) + 180.0) % 360.0;
        if (expectedReverseOrientation < 0) expectedReverseOrientation += 360.0;
        
        assertEquals(expectedReverseOrientation, calculatedOrientationBA, TOLERANCE,
                "Calculated orientation B->A should match expected reverse orientation");

        // Test 3: Verify we can reconstruct point A from point B
        Point2D reconstructedA = LayoutTileMath.calcCurveEndpoint(pointB, calculatedOrientationBA, radius, arcAngle, !curveFace);
        assertEquals(pointA.getX(), reconstructedA.getX(), TOLERANCE,
                "Reconstructed point A X should match original");
        assertEquals(pointA.getY(), reconstructedA.getY(), TOLERANCE,
                "Reconstructed point A Y should match original");

        // Test 4: Create a second curve segment from point B and verify circle continuity
        Point2D pointC = LayoutTileMath.calcCurveEndpoint(pointB, 
                initialOrientation + (curveFace ? -arcAngle : arcAngle), radius, arcAngle, curveFace);
        
        // Calculate orientation from B to C
        double orientationBC = LayoutTileMath.calcCurveOrientation(pointB, pointC, radius, curveFace);
        double expectedOrientationBC = initialOrientation + (curveFace ? -arcAngle : arcAngle);
        if (expectedOrientationBC < 0) expectedOrientationBC += 360.0;
        if (expectedOrientationBC >= 360.0) expectedOrientationBC -= 360.0;
        
        assertEquals(expectedOrientationBC, orientationBC, TOLERANCE,
                "Orientation B->C should continue the same circle");
    }
}
