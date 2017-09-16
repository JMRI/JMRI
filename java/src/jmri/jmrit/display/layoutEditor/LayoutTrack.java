package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all layout track objects (PositionablePoint,
 * TrackSegment, LayoutTurnout, LayoutSlip, LevelXing and LayoutTurntable)
 *
 * @author Dave Duchamp Copyright (C) 2009
 * @author George Warner Copyright (c) 2017
 */
public abstract class LayoutTrack {

    // Defined text resource
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    // hit point types
    public static final int NONE = 0;
    public static final int POS_POINT = 1;
    public static final int TURNOUT_A = 2;  // throat for RH, LH, and WYE turnouts
    public static final int TURNOUT_B = 3;  // continuing route for RH and LH turnouts
    public static final int TURNOUT_C = 4;  // diverging route for RH and LH turnouts
    public static final int TURNOUT_D = 5;  // 4th route for crossovers;
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
    public static final int SLIP_A = 21;
    public static final int SLIP_B = 22;
    public static final int SLIP_C = 23;
    public static final int SLIP_D = 24;
    public static final int SLIP_LEFT = 25;
    public static final int SLIP_RIGHT = 26;
    public static final int BEZIER_CONTROL_POINT_OFFSET_MIN = 30; // offset for TrackSegment Bezier control points (minimum)
    public static final int BEZIER_CONTROL_POINT_OFFSET_MAX = 38; // offset for TrackSegment Bezier control points (maximum)
    public static final int TURNTABLE_RAY_OFFSET = 50; // offset for turntable connection points

    // operational instance variables (not saved between sessions)
    protected LayoutEditor layoutEditor = null;
    protected String ident = "";
    protected Point2D center = new Point2D.Double(50.0, 50.0);

    // dashed line parameters (unused)
    //protected static int minNumDashes = 3;
    //protected static double maxDashLength = 10;
    protected boolean hidden = false;

    protected static Color defaultTrackColor = Color.black;

    protected static final double controlPointSize = 3.0;   // LayoutEditor.SIZE;
    protected static final double controlPointSize2 = 2.0 * controlPointSize; // LayoutEditor.SIZE2;

    /**
     * constructor method
     */
    public LayoutTrack(@Nonnull String ident, @Nonnull Point2D c, @Nonnull LayoutEditor layoutEditor) {
        this.ident = ident;
        this.center = c;
        this.layoutEditor = layoutEditor;
    }

    /**
     * accessor methods
     */
    public String getId() {
        return ident;
    }

    public String getName() {
        return ident;
    }

    /**
     * get center coordinates
     *
     * @return the center coordinates
     */
    public Point2D getCoordsCenter() {
        return center;
    }

    /**
     * set center coordinates
     *
     * @param p the coordinates to set
     */
    public void setCoordsCenter(@Nullable Point2D p) {
        center = p;
    }

    public static void setDefaultTrackColor(@Nullable Color color) {
        defaultTrackColor = color;
    }

    protected Color setColorForTrackBlock(Graphics2D g2, @Nullable LayoutBlock lb, boolean forceBlockTrackColor) {
        Color result = defaultTrackColor;
        if (lb != null) {
            if (forceBlockTrackColor) {
                result = lb.getBlockTrackColor();
            } else {
                result = lb.getBlockColor();
            }
        }
        g2.setColor(result);
        return result;
    }

    // optional prameter forceTrack = false
    protected Color setColorForTrackBlock(Graphics2D g2, @Nullable LayoutBlock lb) {
        return setColorForTrackBlock(g2, lb, false);
    }

    /**
     * one draw routine to rule them all...
     * @param g2 the graphics context
     */
    protected abstract void draw(Graphics2D g2);

    /**
     * draw the edit controls
     * @param g2 the graphics context
     */
    protected abstract void drawEditControls(Graphics2D g2);

    /**
     * draw the turnout controls
     * @param g2 the graphics context
     */
    protected abstract void drawTurnoutControls(Graphics2D g2);
    
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
        if (hidden != hide) {
            hidden = hide;
            if (layoutEditor != null) {
                layoutEditor.redrawPanel();
            }
        }
    }

    /*
     * non-accessor methods
     */

    /**
     * Initialization method for LayoutTrack sub-classes.
     * The following method is called for each instance after the entire
     * LayoutEditor is loaded to set the specific objects for that instance
     * 
     * @param le the layout editor
     */
    public abstract void setObjects(@Nonnull LayoutEditor le);

    /**
     * scale this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    public abstract void scaleCoords(float xFactor, float yFactor);

    /**
     * translate this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
    public abstract void translateCoords(float xFactor, float yFactor);

    protected Point2D rotatePoint(@Nonnull Point2D p, double sineRot, double cosineRot) {
        double cX = center.getX();
        double cY = center.getY();
        double deltaX = p.getX() - cX;
        double deltaY = p.getY() - cY;
        double x = cX + cosineRot * deltaX - sineRot * deltaY;
        double y = cY + sineRot * deltaX + cosineRot * deltaY;
        return new Point2D.Double(x, y);
    }

    /**
     * find the hit (location) type for a point
     *
     * @param hitPoint           - the point
     * @param useRectangles      - whether to use (larger) rectangles or
     *                             (smaller) circles for hit testing
     * @param requireUnconnected - whether to only return hit types for free
     *                             connections
     * @return the location type for the point (or NONE)
     * @since 7.4.3
     */
    protected abstract int findHitPointType(@Nonnull Point2D hitPoint, boolean useRectangles, boolean requireUnconnected);

    // optional useRectangles & requireUnconnected parameters default to false
    protected int findHitPointType(@Nonnull Point2D p) {
        return findHitPointType(p, false, false);
    }

    // optional requireUnconnected parameter defaults to false
    protected int findHitPointType(@Nonnull Point2D p, boolean useRectangles) {
        return findHitPointType(p, useRectangles, false);
    }

    /**
     * @param hitType the hit point type
     * @return true if this int is for a connection to a LayoutTrack
     */
    protected static boolean isConnectionHitType(int hitType) {
        boolean result = false; // assume failure (pessimist!)
        switch (hitType) {
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
            default:
                result = false; // these are not
                break;
        }
        if ((hitType >= BEZIER_CONTROL_POINT_OFFSET_MIN) && (hitType <= BEZIER_CONTROL_POINT_OFFSET_MAX)) {
            result = false; // these are not
        } else if (hitType >= TURNTABLE_RAY_OFFSET) {
            result = true;  // these are all connection types
        }
        return result;
    }   // isConnectionHitType

    /**
     * @param hitType the hit point type
     * @return true if this int is for a layout control
     */
    protected static boolean isControlHitType(int hitType) {
        boolean result = false; // assume failure (pessimist!)
        switch (hitType) {
            case TURNOUT_CENTER:
            case SLIP_CENTER:
            case SLIP_LEFT:
            case SLIP_RIGHT:
                result = true;  // these are all control types
                break;
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
            case NONE:
            case LEVEL_XING_CENTER:
            case TURNTABLE_CENTER:
            case LAYOUT_POS_LABEL:
            case LAYOUT_POS_JCOMP:
            case MULTI_SENSOR:
            case MARKER:
            case TRACK_CIRCLE_CENTRE:
            default:
                result = false; // these are not
                break;
        }
        if ((hitType >= BEZIER_CONTROL_POINT_OFFSET_MIN) && (hitType <= BEZIER_CONTROL_POINT_OFFSET_MAX)) {
            result = false; // these are not control types
        } else if (hitType >= TURNTABLE_RAY_OFFSET) {
            result = true;  // these are all control types
        }
        return result;
    }   // isControlHitType

    /**
     * @param hitType the hit point type
     * @return true if this int is for a popup menu
     */
    protected static boolean isPopupHitType(int hitType) {
        boolean result = false; // assume failure (pessimist!)
        switch (hitType) {
            case LEVEL_XING_CENTER:
            case POS_POINT:
            case SLIP_CENTER:
            case SLIP_LEFT:
            case SLIP_RIGHT:
            case TRACK:
            case TRACK_CIRCLE_CENTRE:
            case TURNOUT_CENTER:
            case TURNTABLE_CENTER:
                result = true;  // these are all popup hit types
                break;
            case LAYOUT_POS_JCOMP:
            case LAYOUT_POS_LABEL:
            case LEVEL_XING_A:
            case LEVEL_XING_B:
            case LEVEL_XING_C:
            case LEVEL_XING_D:
            case MARKER:
            case MULTI_SENSOR:
            case NONE:
            case SLIP_A:
            case SLIP_B:
            case SLIP_C:
            case SLIP_D:
            case TURNOUT_A:
            case TURNOUT_B:
            case TURNOUT_C:
            case TURNOUT_D:
            default:
                result = false; // these are not
                break;
        }
        if ((hitType >= BEZIER_CONTROL_POINT_OFFSET_MIN) && (hitType <= BEZIER_CONTROL_POINT_OFFSET_MAX)) {
            result = true; // these are all popup hit types
        } else if (hitType >= TURNTABLE_RAY_OFFSET) {
            result = true;  // these are all popup hit types
        }
        return result;
    }   // isPopupHitType

    /**
     * return the coordinates for a specified connection type (abstract: should
     * be overridden by ALL subclasses)
     *
     * @param connectionType the connection type
     * @return the coordinates for the specified connection type
     */
    public abstract Point2D getCoordsForConnectionType(int connectionType);

    /**
     * @return the bounds of this track 
     */
    public abstract Rectangle2D getBounds();

    protected abstract void showPopup(MouseEvent e);

    /**
     * get the LayoutTrack connected at the specified connection type
     *
     * @param connectionType where on us to get the connection
     * @return the LayoutTrack connected at the specified connection type
     * @throws jmri.JmriException - if the connectionType is invalid
     */
    public abstract LayoutTrack getConnection(int connectionType) throws jmri.JmriException;

    /**
     * set the LayoutTrack connected at the specified connection type
     *
     * @param connectionType where on us to set the connection
     * @param o              the LayoutTrack that is to be connected
     * @param type           where on the LayoutTrack we are connected
     * @throws jmri.JmriException - if connectionType or type are invalid
     */
    public abstract void setConnection(int connectionType, LayoutTrack o, int type) throws jmri.JmriException;

    /**
     * abstract method... subclasses should implement _IF_ they need to recheck
     * their block boundaries
     */
    protected abstract void reCheckBlockBoundary();

    /**
     * get the layout connectivity for this track
     * @return the list of Layout Connectivity objects
     */
    protected abstract List<LayoutConnectivity> getLayoutConnectivity();

    /**
     * return true if this connection type is disconnected
     *
     * @param connectionType - the connection type to test
     * @return true if the connection for this connection type is free
     */
    public boolean isDisconnected(int connectionType) {
        boolean result = false;
        if (isConnectionHitType(connectionType)) {
            try {
                result = (null == getConnection(connectionType));
            } catch (jmri.JmriException e) {
                // this should never happen because isConnectionType() above would have caught an invalid connectionType.
            }
        }
        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutTrack.class);
}
