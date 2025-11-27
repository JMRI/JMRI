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
}
