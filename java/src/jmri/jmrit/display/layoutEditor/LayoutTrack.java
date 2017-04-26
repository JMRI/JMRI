package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all layout track objects (LayoutTurnout, LayoutSlip,
 * LayoutTurntable, LevelXing, TrackSegment)
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
    public static final int SLIP_CENTER = 20; //
    public static final int SLIP_A = 21; // offset for slip connection points
    public static final int SLIP_B = 22; // offset for slip connection points
    public static final int SLIP_C = 23; // offset for slip connection points
    public static final int SLIP_D = 24; // offset for slip connection points
    public static final int SLIP_LEFT = 25;
    public static final int SLIP_RIGHT = 26;
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

    public void reCheckBlockBoundary() {
        log.error("virtual method: override in sub-classes; don't call [super ...].");
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutTrack.class.getName());
}
