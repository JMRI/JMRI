package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all layout track objects (LayoutTurnout, LayoutSlip,
 * LayoutTurntable, LevelXing, TrackSegment &amp; PositionablePoint)
 *
 * @author George Warner Copyright (c) 2017
 */
public abstract class LayoutTrack {

    // Defined text resource
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    // connection types
    public static final int NONE = 0;
    public static final int POS_POINT = 1;
    public static final int TURNOUT_A = 2;  // throat for RH, LH, and WYE turnouts
    public static final int TURNOUT_B = 3;  // continuing route for RH or LH turnouts
    public static final int TURNOUT_C = 4;  // diverging route for RH or LH turnouts
    public static final int TURNOUT_D = 5;  // double-crossover or single crossover only
    public static final int LEVEL_XING_A = 6;
    public static final int LEVEL_XING_B = 7;
    public static final int LEVEL_XING_C = 8;
    public static final int LEVEL_XING_D = 9;
    public static final int TRACK = 10;
    public static final int TURNOUT_CENTER = 11; // non-connection points should be last
    public static final int LEVEL_XING_CENTER = 12;
    public static final int TURNTABLE_CENTER = 13;
    public static final int LAYOUT_POS_LABEL = 14;
    public static final int LAYOUT_POS_JCOMP = 15;
    public static final int MULTI_SENSOR = 16;
    public static final int MARKER = 17;
    public static final int TRACK_CIRCLE_CENTRE = 18;
    public static final int SLIP_CENTER = 20;   //should be @Deprecated (use SLIP_LEFT & SLIP_RIGHT instead)
    public static final int SLIP_A = 21; // offset for slip connection points
    public static final int SLIP_B = 22; // offset for slip connection points
    public static final int SLIP_C = 23; // offset for slip connection points
    public static final int SLIP_D = 24; // offset for slip connection points
    public static final int SLIP_LEFT = 25;
    public static final int SLIP_RIGHT = 26;
    public static final int FLEX_CENTER = 27;
    public static final int FLEX_A = 28;
    public static final int FLEX_B = 29;
    public static final int BEZIER_CONTROL_POINT_OFFSET_MIN = 30; // offset for TrackSegment Bezier control points (minimum)
    public static final int BEZIER_CONTROL_POINT_OFFSET_MAX = 38; // offset for TrackSegment Bezier control points (maximum)
    //NOTE: if(/when) you need another control/hit point type leave at least four (if not eight) unused here (for more Bezier control points)
    public static final int TURNTABLE_RAY_OFFSET = 50; // offset for turntable connection points

    protected String ident = "";

    // dashed line parameters
    //private static int minNumDashes = 3;
    //private static double maxDashLength = 10;
    public Point2D center = new Point2D.Double(50.0, 50.0);

    protected boolean hidden = false;

    protected static Color defaultTrackColor = Color.black;

    protected static final double controlPointSize = 3.0;   // LayoutEditor.SIZE;
    protected static final double controlPointSize2 = 2.0 * controlPointSize; // LayoutEditor.SIZE2;

    /**
     * constructor method
     */
    public LayoutTrack() {
    }

    /**
     * accessor methods
     */
    public String getID() {
        return ident;
    }

    public String getName() {
        return ident;
    }

    public static void setDefaultTrackColor(Color color) {
        defaultTrackColor = color;
    }

    /**
     * Get the hidden state of the track element.
     *
     * @return true if hidden; false otherwise
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Get the hidden state of the track element.
     *
     * @return true if hidden; false otherwise
     * @deprecated since 4.7.2; use {@link #isHidden()} instead
     */
    @Deprecated // Java standard pattern for boolean getters is "isHidden()"
    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean hide) {
        hidden = hide;
    }

    /*
     * non-accessor methods
     */

    protected Point2D rotatePoint(Point2D p, double sineRot, double cosineRot) {
        double cX = center.getX();
        double cY = center.getY();
        double deltaX = p.getX() - cX;
        double deltaY = p.getY() - cY;
        double x = cX + cosineRot * deltaX - sineRot * deltaY;
        double y = cY + sineRot * deltaX + cosineRot * deltaY;
        return new Point2D.Double(x, y);
    }

    /**
     * return the connection type for a point
     * (abstract; should be overridden by sub-classes)
     *
     * @since 7.4.?
     */
    protected int hitTestPoint(Point2D p, boolean useRectangles, boolean requireUnconnected) {
        return NONE;
    }

    // optional useRectangles & requireUnconnected parameters default to false
    public int hitTestPoint(Point2D p) {
        return hitTestPoint(p, false, false);
    }

    // optional requireUnconnected parameter defaults to false
    public int hitTestPoint(Point2D p, boolean useRectangles) {
        return hitTestPoint(p, useRectangles, false);
    }

    // some connection types aren't actually connections
    // they're only used for hit testing (to determine what was clicked)
    public boolean isConnectionType(int connectionType) {
        boolean result = false; // assume failure (pessimist!)
        switch (connectionType) {
            case POS_POINT:
            case TURNOUT_A:
            case TURNOUT_B:
            case TURNOUT_C:
            case TURNOUT_D:
            case LEVEL_XING_A:
            case LEVEL_XING_B:
            case LEVEL_XING_C:
            case LEVEL_XING_D:
            case TRACK:
            case SLIP_A:
            case SLIP_B:
            case SLIP_C:
            case SLIP_D:
            case FLEX_A:
            case FLEX_B:
                result = true;  // these are all connection types
                break;
            case NONE:
            case TURNOUT_CENTER:
            case LEVEL_XING_CENTER:
            case TURNTABLE_CENTER:
            case LAYOUT_POS_LABEL:
            case LAYOUT_POS_JCOMP:
            case MULTI_SENSOR:
            case MARKER:
            case TRACK_CIRCLE_CENTRE:
            case SLIP_CENTER:
            case SLIP_LEFT:
            case SLIP_RIGHT:
            case FLEX_CENTER:
            default:
                result = false; // these are all hit types
                break;
        }
        if (TURNTABLE_RAY_OFFSET <= connectionType) {
            result = true;  // these are all connection types
        } else if (BEZIER_CONTROL_POINT_OFFSET_MIN <= connectionType) {
            result = false; // these are all hit types
        }
        return result;
    }

    public void reCheckBlockBoundary() {
        log.error("virtual method: override in sub-classes; don't call [super ...].");
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutTrack.class.getName());
}
