package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ResourceBundle;
import javax.annotation.Nullable;
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

    // hit location (& connection) types
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

    protected LayoutEditor layoutEditor = null;

    protected String ident = "";

    // dashed line parameters (unused)
    //protected static int minNumDashes = 3;
    //protected static double maxDashLength = 10;
    protected Point2D center = new Point2D.Double(50.0, 50.0);

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
    public void setCoordsCenter(Point2D p) {
        center = p;
    }

    public static void setDefaultTrackColor(Color color) {
        defaultTrackColor = color;
    }

    protected Color setColorForTrackBlock(Graphics2D g2, @Nullable LayoutBlock b, boolean forceTrack) {
        Color result = defaultTrackColor;
        if (b != null) {
            if (forceTrack) {
                result = b.getBlockTrackColor();
            } else {
                result = b.getBlockColor();
            }
        }
        g2.setColor(result);
        return result;
    }

    // optional prameter forceTrack = false
    protected Color setColorForTrackBlock(Graphics2D g2, @Nullable LayoutBlock b) {
        return setColorForTrackBlock(g2, b, false);
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
     * find the hit (location) type for a point (abstract: should be overridden
     * by ALL subclasses)
     *
     * @param p                  the point
     * @param useRectangles      - whether to use (larger) rectangles or
     *                           (smaller) circles for hit testing
     * @param requireUnconnected - whether to only return hit types for free
     *                           connections
     * @return the location type for the point (or NONE)
     * @since 7.4.3
     */
    protected abstract int findHitPointType(Point2D p, boolean useRectangles, boolean requireUnconnected);

    // optional useRectangles & requireUnconnected parameters default to false
    protected int findHitPointType(Point2D p) {
        return findHitPointType(p, false, false);
    }

    // optional requireUnconnected parameter defaults to false
    protected int findHitPointType(Point2D p, boolean useRectangles) {
        return findHitPointType(p, useRectangles, false);
    }

    // some connection types aren't actually connections
    // they're only used for hit testing (to determine what is at a location)
    protected static boolean isConnectionType(int connectionType) {
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
        if ((connectionType >= BEZIER_CONTROL_POINT_OFFSET_MIN) && (connectionType <= BEZIER_CONTROL_POINT_OFFSET_MAX)) {
            result = false; // these are not
        } else if (connectionType >= TURNTABLE_RAY_OFFSET) {
            result = true;  // these are all connection types
        }
        return result;
    }

    /**
     * return the coordinates for a specified connection type (abstract: should
     * be overridden by ALL subclasses)
     *
     * @param connectionType the connection type
     * @return the coordinates for the specified connection type
     */
    public abstract Point2D getCoordsForConnectionType(int connectionType);

    /**
     * abstract method... subclasses should implement _IF_ they need to recheck
     * their block boundaries
     */
    public abstract void reCheckBlockBoundary();

    /**
     * @return the bounds of this track (abstract: should be overridden by ALL
     *         subclasses)
     */
    public abstract Rectangle2D getBounds();

    protected void showPopUp(MouseEvent e) {

    }

    /**
     * get the object connected to this track for the specified connection type
     *
     * @param connectionType the specified connection type
     * @return the object connected to this slip for the specified connection
     *         type
     * @throws jmri.JmriException - if the connectionType is invalid
     */
    // Note: There are times when subclass instances are stored in variables
    // of this (base) class so when this method is called on them they
    // are dispatched here instead of directly to their subclass implementation.
    // So basicly this is just a subclass dispatcher
    //TODO: Determine if this is 100% necessary
    public Object getConnection(int connectionType) throws jmri.JmriException {
        Object result = null;
        switch (connectionType) {
            case POS_POINT: {
                result = ((PositionablePoint) this).getConnection(connectionType);
                break;
            }
            case TURNOUT_A:
            case TURNOUT_B:
            case TURNOUT_C:
            case TURNOUT_D: {
                result = ((LayoutTurnout) this).getConnection(connectionType);
                break;
            }
            case LEVEL_XING_A:
            case LEVEL_XING_B:
            case LEVEL_XING_C:
            case LEVEL_XING_D: {
                result = ((LevelXing) this).getConnection(connectionType);
                break;
            }

            case TRACK: {
                result = ((TrackSegment) this).getConnection(connectionType);
                break;
            }

            case SLIP_A:
            case SLIP_B:
            case SLIP_C:
            case SLIP_D: {
                result = ((LayoutSlip) this).getConnection(connectionType);
                break;
            }
            default: {
                if (connectionType >= TURNTABLE_RAY_OFFSET) {
                    result = ((LayoutTurntable) this).getConnection(connectionType);
                } else {
                    log.error("Invalid connection type " + connectionType); //I18IN
                    throw new jmri.JmriException("Invalid Point");
                }
                break;
            }
        }
        return result;
    }

    /**
     * set the object connected to this turnout for the specified connection
     * type
     *
     * @param connectionType the connection type (where it is connected to us)
     * @param o              the object that is being connected
     * @param type           the type of object that we're being connected to
     *                       (Should always be "NONE" or "TRACK")
     * @throws jmri.JmriException - if connectionType or type are invalid
     */
    // Note: There are times when subclass instances are stored in variables
    // of this (base) class so when this method is called on them they
    // are dispatched here instead of directly to their subclass implementation.
    // So basicly this is just a subclass dispatcher
    //TODO: Determine if this is 100% necessary
    public void setConnection(int connectionType, Object o, int type) throws jmri.JmriException {
        switch (connectionType) {
            case POS_POINT: {
                ((PositionablePoint) this).setConnection(connectionType, o, type);
                break;
            }
            case TURNOUT_A:
            case TURNOUT_B:
            case TURNOUT_C:
            case TURNOUT_D: {
                ((LayoutTurnout) this).setConnection(connectionType, o, type);
                break;
            }
            case LEVEL_XING_A:
            case LEVEL_XING_B:
            case LEVEL_XING_C:
            case LEVEL_XING_D: {
                ((LevelXing) this).setConnection(connectionType, o, type);
                break;
            }

            case TRACK: {
                ((TrackSegment) this).setConnection(connectionType, o, type);
                break;
            }

            case SLIP_A:
            case SLIP_B:
            case SLIP_C:
            case SLIP_D: {
                ((LayoutSlip) this).setConnection(connectionType, o, type);
                break;
            }
            default: {
                if (connectionType >= TURNTABLE_RAY_OFFSET) {
                    ((LayoutTurntable) this).setConnection(connectionType, o, type);
                } else {
                    log.error("Invalid connection type " + connectionType); //I18IN
                    throw new jmri.JmriException("Invalid Point");
                }
                break;
            }
        }
    }

    /**
     * return true if this connection type is disconnected
     *
     * @param connectionType - the connection type to test
     * @return true if the connection for this connection type is free
     */
    public boolean isDisconnected(int connectionType) {
        boolean result = false;
        if (isConnectionType(connectionType)) {
            try {
                result = (null == getConnection(connectionType));
            } catch (jmri.JmriException e) {
                // this should never happen because isConnectionType() above would have caught an invalid connectionType.
            }
        }
        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutTrack.class.getName());
}
