package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;

/**
 * Utility class for calculating track tile geometry, particularly orientations
 * at connection points for curved and straight track segments.
 *
 * @author Ralf Lang Copyright (c) 2025
 */
public class LayoutTileGeometry {

    /**
     * Calculate the tangent orientation angle at a point on a curved track segment.
     * Returns the outgoing tangent direction for continuing track from this point.
     * 
     * @param trackSegmentView the curved track segment view
     * @param point the point at which to calculate the tangent
     * @param layoutEditor the layout editor to determine which endpoint we're at
     * @return the tangent angle in degrees (0-360), or -1 if calculation fails
     */
    public static double calculateCurveTangentOrientation(
            @Nonnull TrackSegmentView trackSegmentView, 
            @Nonnull Point2D point,
            @Nonnull LayoutEditor layoutEditor) {
        
        if (!trackSegmentView.isCircle()) {
            return -1.0; // Not a curve
        }
        
        Point2D center = trackSegmentView.getCentre();
        if (center == null) {
            return -1.0;
        }
        
        TrackSegment trackSegment = trackSegmentView.getTrackSegment();
        Point2D connect1Point = layoutEditor.getCoords(trackSegment.getConnect1(), trackSegment.getType1());
        Point2D connect2Point = layoutEditor.getCoords(trackSegment.getConnect2(), trackSegment.getType2());
        
        if (connect1Point == null || connect2Point == null) {
            return -1.0;
        }
        
        // Determine which endpoint we're at
        double dist1 = point.distance(connect1Point);
        double dist2 = point.distance(connect2Point);
        boolean atConnect1 = (dist1 < dist2);
        
        // Calculate radius angle from point to center
        double radiusAngle = Math.toDegrees(Math.atan2(
            center.getY() - point.getY(),
            center.getX() - point.getX()));
        
        // The tangent is perpendicular to the radius
        double arc = trackSegmentView.getAngle();
        boolean isFlip = trackSegmentView.isFlip();
        boolean counterClockwise = (arc > 0) != isFlip;
        
        double tangent;
        if (counterClockwise) {
            // CCW motion: tangent is 90° behind the radius
            tangent = radiusAngle - 90.0;
        } else {
            // CW motion: tangent is 90° ahead of the radius
            tangent = radiusAngle + 90.0;
        }
        
        // The tangent calculated above is for forward motion (connect1 → connect2)
        // If we're at connect1, we want the backward tangent (away from connect2)
        if (atConnect1) {
            tangent += 180.0;
        }
        
        // Normalize to 0-360
        tangent = (tangent % 360 + 360) % 360;
        
        return tangent;
    }
    
    /**
     * Calculate the orientation angle at a point on a straight track segment.
     * This is the angle pointing away from the existing track (for continuing the track).
     * 
     * @param trackSegmentView the track segment view
     * @param point the point at which to calculate orientation (should be one of the endpoints)
     * @param layoutEditor the layout editor to get coordinates
     * @return the orientation angle in degrees (0-360), or -1 if calculation fails
     */
    public static double calculateStraightOrientation(
            @Nonnull TrackSegmentView trackSegmentView,
            @Nonnull Point2D point,
            @Nonnull LayoutEditor layoutEditor) {
        
        TrackSegment trackSegment = trackSegmentView.getTrackSegment();
        Point2D connect1Point = layoutEditor.getCoords(trackSegment.getConnect1(), trackSegment.getType1());
        Point2D connect2Point = layoutEditor.getCoords(trackSegment.getConnect2(), trackSegment.getType2());
        
        if (connect1Point == null || connect2Point == null) {
            return -1.0;
        }
        
        // Determine which endpoint we're at
        double dist1 = point.distance(connect1Point);
        double dist2 = point.distance(connect2Point);
        
        double angle;
        if (dist1 < dist2) {
            // We're at connect1, so angle points away from connect2 (to continue the track)
            angle = Math.toDegrees(Math.atan2(
                connect1Point.getY() - connect2Point.getY(),
                connect1Point.getX() - connect2Point.getX()));
        } else {
            // We're at connect2, so angle points away from connect1 (to continue the track)
            angle = Math.toDegrees(Math.atan2(
                connect2Point.getY() - connect1Point.getY(),
                connect2Point.getX() - connect1Point.getX()));
        }
        
        // Normalize to 0-360
        angle = (angle % 360 + 360) % 360;
        
        return angle;
    }
    
    /**
     * Calculate the orientation angle at a point on any track segment (curved or straight).
     * 
     * @param trackSegmentView the track segment view
     * @param point the point at which to calculate orientation
     * @param layoutEditor the layout editor to get coordinates (used for straight segments)
     * @return the orientation angle in degrees (0-360), or -1 if calculation fails
     */
    public static double calculateOrientation(
            @Nonnull TrackSegmentView trackSegmentView,
            @Nonnull Point2D point,
            @Nonnull LayoutEditor layoutEditor) {
        
        if (trackSegmentView.isCircle()) {
            return calculateCurveTangentOrientation(trackSegmentView, point, layoutEditor);
        } else {
            return calculateStraightOrientation(trackSegmentView, point, layoutEditor);
        }
    }
    
    /**
     * Calculate the default path length for a track tile in millimeters.
     * For straight tiles, this is the tile length.
     * For curved tiles, this is the arc length: L = 2 * pi * radius * (arc/360).
     * 
     * @param tile the track tile
     * @return the default path length in millimeters, or 0.0 if not available
     */
    public static double calculatePathLength(@CheckForNull jmri.tracktiles.TrackTile tile) {
        // Check if tile information is available
        if (tile == null || tile instanceof jmri.tracktiles.NotATile || tile instanceof jmri.tracktiles.UnknownTile) {
            return 0.0;
        }
        
        String jmriType = tile.getJmriType();
        
        if ("straight".equals(jmriType)) {
            // For straight tiles, return the length
            return tile.getLength();
        } else if ("curved".equals(jmriType)) {
            // For curved tiles, calculate arc length
            return calculateArcLength(tile.getRadius(), tile.getArc());
        }
        
        // For other types (turnouts, crossings), use the straight length if available
        double length = tile.getLength();
        return (length > 0) ? length : 0.0;
    }
    
    /**
     * Calculate the arc length for a curved segment.
     * Formula: L = 2 * pi * radius * (arc/360)
     * 
     * @param radius the radius in millimeters
     * @param arcDegrees the arc angle in degrees
     * @return the arc length in millimeters
     */
    public static double calculateArcLength(double radius, double arcDegrees) {
        return 2.0 * Math.PI * radius * (arcDegrees / 360.0);
    }
    
    /**
     * Calculate the endpoint for a straight track segment.
     * 
     * @param start the starting point
     * @param angle the angle in degrees
     * @param lengthMM the length in millimeters
     * @param layoutUnitsPerMM the conversion factor from mm to layout units
     * @return the calculated endpoint
     */
    public static Point2D calculateStraightEndpoint(
            @Nonnull Point2D start, 
            double angle, 
            double lengthMM, 
            double layoutUnitsPerMM) {
        
        double lengthLayoutUnits = lengthMM * layoutUnitsPerMM;
        
        // Convert angle to radians and calculate offset
        double radians = Math.toRadians(angle);
        double dx = lengthLayoutUnits * Math.cos(radians);
        double dy = lengthLayoutUnits * Math.sin(radians);
        
        return new Point2D.Double(start.getX() + dx, start.getY() + dy);
    }
    
    /**
     * Calculate the endpoint for a curved track segment rotating around a center point.
     * 
     * @param center the center point of the curve
     * @param start the starting point on the curve
     * @param arcDegrees the arc angle in degrees (magnitude)
     * @param curveLeft true for left curve (CCW rotation), false for right curve (CW rotation)
     * @return the calculated endpoint
     */
    public static Point2D calculateCurvedEndpoint(
            @Nonnull Point2D center, 
            @Nonnull Point2D start, 
            double arcDegrees, 
            boolean curveLeft) {
        
        // Calculate radius from center to start
        double dx = start.getX() - center.getX();
        double dy = start.getY() - center.getY();
        double radius = Math.sqrt(dx * dx + dy * dy);
        
        // Current angle from center to start
        double currentAngle = Math.toDegrees(Math.atan2(dy, dx));
        
        // Calculate new angle by rotating around center
        // Left curve: counter-clockwise (add arc)
        // Right curve: clockwise (subtract arc)
        double newAngle;
        if (curveLeft) {
            newAngle = currentAngle + Math.abs(arcDegrees);
        } else {
            newAngle = currentAngle - Math.abs(arcDegrees);
        }
        
        // Calculate endpoint at new angle on same circle
        double newRadians = Math.toRadians(newAngle);
        double endX = center.getX() + radius * Math.cos(newRadians);
        double endY = center.getY() + radius * Math.sin(newRadians);
        
        return new Point2D.Double(endX, endY);
    }
    
    /**
     * Convert millimeters to layout units.
     * Layout coordinates are zoom-independent.
     * 
     * @param mm the measurement in millimeters
     * @param layoutUnitsPerMM the conversion factor from mm to layout units
     * @return the equivalent measurement in layout units
     */
    public static double convertMMToLayoutUnits(double mm, double layoutUnitsPerMM) {
        return mm * layoutUnitsPerMM;
    }
}
