package jmri.jmrit.display.layoutEditor;

import static java.lang.Float.POSITIVE_INFINITY;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import jmri.NamedBeanHandle;
import jmri.Turnout;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutTurntable is a representation used by LayoutEditor to display a
 * turntable.
 * <p>
 * A LayoutTurntable has a variable number of connection points, called
 * RayTracks, each radiating from the center of the turntable. Each of these
 * points should be connected to a TrackSegment.
 * <p>
 * Each radiating segment (RayTrack) gets its Block information from its
 * connected track segment.
 * <p>
 * Each radiating segment (RayTrack) has a unique connection index. The
 * connection index is set when the RayTrack is created, and cannot be changed.
 * This connection index is used to maintain the identity of the radiating
 * segment to its connected Track Segment as ray tracks are added and deleted by
 * the user.
 * <p>
 * The radius of the turntable circle is variable by the user.
 * <p>
 * Each radiating segment (RayTrack) connecting point is a fixed distance from
 * the center of the turntable. The user may vary the angle of the radiating
 * segment. Angles are measured from the vertical (12 o'clock) position in a
 * clockwise manner. For example, 30 degrees is 1 o'clock, 60 degrees is 2
 * o'clock, 90 degrees is 3 o'clock, etc.
 * <p>
 * Each radiating segment is drawn from its connection point to the turntable
 * circle in the direction of the turntable center.
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutTurntable extends LayoutTrack {

    // defined constants
    // operational instance variables (not saved between sessions)
    // persistent instance variables (saved between sessions)
    private boolean turnoutControlled = false;
    private double radius = 25.0;
    private ArrayList<RayTrack> rayList = new ArrayList<>(); // list of Ray Track objects.
    private int lastKnownIndex = -1;

    /**
     * Constructor method
     */
    public LayoutTurntable(@Nonnull String id, @Nonnull Point2D c, @Nonnull LayoutEditor layoutEditor) {
        super(id, c, layoutEditor);
        radius = 25.0;
    }

    //
    /**
     * Get a string that represents this object (this should only be used for
     * debugging)
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "LayoutTurntable " + getName();
    }

    //
    // Accessor methods
    //

    /**
     * Get the radius for this turntable.
     *
     * @return the radius for this turntable
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Set the radius for this turntable.
     *
     * @param r the radius for this turntable
     */
    public void setRadius(double r) {
        radius = r;
    }

    /**
     * @return the bounds of this turntable
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D result;

        result = new Rectangle2D.Double(center.getX(), center.getY(), 0, 0);
        for (int k = 0; k < getNumberRays(); k++) {
            result.add(getRayCoordsOrdered(k));
        }
        return result;
    }

    /**
     * Add a ray at the specified angle.
     *
     * @param angle the angle
     * @return the RayTrack
     */
    protected RayTrack addRay(double angle) {
        RayTrack rt = new RayTrack(angle, getNewIndex());
        rayList.add(rt);
        return rt;
    }

    private int getNewIndex() {
        int index = -1;
        if (rayList.size() == 0) {
            return 0;
        }

        boolean found = true;
        while (found) {
            index++;
            found = false; // assume failure (pessimist!)
            for (RayTrack rt : rayList) {
                if (index == rt.getConnectionIndex()) {
                    found = true;
                }
            }
        }
        return index;
    }

    // the following method is only for use in loading layout turntables
    public void addRayTrack(double angle, int index, String name) {
        RayTrack rt = new RayTrack(angle, index);
        //if (ray!=null) {
        rayList.add(rt);
        rt.connectName = name;
        //}
    }

    /**
     * Get the connection for the ray with this index.
     *
     * @param index the index
     * @return the connection for the ray with this index
     */
    public TrackSegment getRayConnectIndexed(int index) {
        TrackSegment result = null;
        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                result = rt.getConnect();
                break;
            }
        }
        return result;
    }

    /**
     * Get the connection for the ray at the index in the rayList.
     *
     * @param i the index in the rayList
     * @return the connection for the ray at that index in the rayList
     */
    public TrackSegment getRayConnectOrdered(int i) {
        TrackSegment result = null;

        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            if (rt != null) {
                result = rt.getConnect();
            }
        }
        return result;
    }

    /**
     * Set the connection for the ray at the index in the rayList.
     *
     * @param ts    the connection
     * @param index the index in the rayList
     */
    public void setRayConnect(TrackSegment ts, int index) {
        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                rt.setConnect(ts);
                break;
            }
        }
    }

    // should only be used by xml save code
    protected ArrayList<RayTrack> getRayList() {
        return rayList;
    }

    /**
     * Get the number of rays on turntable.
     *
     * @return the number of rays
     */
    public int getNumberRays() {
        return rayList.size();
    }

    /**
     * Get the index for the ray at this position in the rayList.
     *
     * @param i the position in the rayList
     * @return the index
     */
    public int getRayIndex(int i) {
        int result = 0;
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            result = rt.getConnectionIndex();
        }
        return result;
    }

    /**
     * Get the angle for the ray at this position in the rayList.
     *
     * @param i the position in the rayList
     * @return the angle
     */
    public double getRayAngle(int i) {
        double result = 0.0;
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            result = rt.getAngle();
        }
        return result;
    }

    /**
     * Set the turnout and state for the ray with this index.
     *
     * @param index       the index
     * @param turnoutName the turnout name
     * @param state       the state
     */
    public void setRayTurnout(int index, String turnoutName, int state) {
        boolean found = false; // assume failure (pessimist!)
        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                rt.setTurnout(turnoutName, state);
                found = true;
                break;
            }
        }
        if (!found) {
            log.error("Attempt to add Turnout control to a non-existant ray track");
        }
    }

    /**
     * Get the name of the turnout for the ray at this index.
     *
     * @param i the index
     * @return name of the turnout for the ray at this index
     */
    public String getRayTurnoutName(int i) {
        String result = null;
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            result = rt.getTurnoutName();
        }
        return result;
    }

    /**
     * Get the turnout for the ray at this index.
     *
     * @param i the index
     * @return the turnout for the ray at this index
     */
    public Turnout getRayTurnout(int i) {
        Turnout result = null;
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            result = rt.getTurnout();
        }
        return result;
    }

    /**
     * Get the state of the turnout for the ray at this index.
     *
     * @param i the index
     * @return state of the turnout for the ray at this index
     */
    public int getRayTurnoutState(int i) {
        int result = 0;
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            result = rt.getTurnoutState();
        }
        return result;
    }

    /**
     * Get if the ray at this index is disabled.
     *
     * @param i the index
     * @return true if disabled
     */
    public boolean isRayDisabled(int i) {
        boolean result = false;    // assume not disabled
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            result = rt.isDisabled();
        }
        return result;
    }

    /**
     * Set the disabled state of the ray at this index.
     *
     * @param i   the index
     * @param boo the state
     */
    public void setRayDisabled(int i, boolean boo) {
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            rt.setDisabled(boo);
        }
    }

    /**
     * Get the disabled when occupied state of the ray at this index.
     *
     * @param i the index
     * @return the state
     */
    public boolean isRayDisabledWhenOccupied(int i) {
        boolean result = false;    // assume not disabled when occupied
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            result = rt.isDisabledWhenOccupied();
        }
        return result;
    }

    /**
     * Set the disabled when occupied state of the ray at this index.
     *
     * @param i   the index
     * @param boo the state
     */
    public void setRayDisabledWhenOccupied(int i, boolean boo) {
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            rt.setDisabledWhenOccupied(boo);
        }
    }

    /**
     * Get the coordinates for the ray with this index.
     *
     * @param index the index
     * @return the coordinates
     */
    public Point2D getRayCoordsIndexed(int index) {
        Point2D result = MathUtil.zeroPoint2D;
        double rayRadius = radius + LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                double angle = Math.toRadians(rt.getAngle());
                // calculate coordinates
                result = new Point2D.Double(
                        (center.getX() + (rayRadius * Math.sin(angle))),
                        (center.getY() - (rayRadius * Math.cos(angle))));
                break;
            }
        }
        return result;
    }

    /**
     * Get the coordinates for the ray at this index.
     *
     * @param i the index; zero point returned if this is out of range
     * @return the coordinates
     */
    public Point2D getRayCoordsOrdered(int i) {
        Point2D result = MathUtil.zeroPoint2D;
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            if (rt != null) {
                double angle = Math.toRadians(rt.getAngle());
                double rayRadius = radius + LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
                // calculate coordinates
                result = new Point2D.Double(
                        (center.getX() + (rayRadius * Math.sin(angle))),
                        (center.getY() - (rayRadius * Math.cos(angle))));
            }
        }
        return result;
    }

    /**
     * Set the coordinates for the ray at this index.
     *
     * @param x     the x coordinates
     * @param y     the y coordinates
     * @param index the index
     */
    public void setRayCoordsIndexed(double x, double y, int index) {
        boolean found = false; // assume failure (pessimist!)
        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                // convert these coordinates to an angle
                double angle = Math.atan2(x - center.getX(), y - center.getY());
                angle = MathUtil.wrapPM360(180.0 - Math.toDegrees(angle));
                rt.setAngle(angle);
                found = true;
                break;
            }
        }
        if (!found) {
            log.error("Attempt to move a non-existant ray track");
        }
    }

    /**
     * Get the coordinates for a specified connection type.
     *
     * @param locationType the connection type
     * @return the coordinates
     */
    @Override
    public Point2D getCoordsForConnectionType(int locationType) {
        Point2D result = getCoordsCenter();
        if (TURNTABLE_CENTER == locationType) {
            // nothing to see here, move along...
            // (results are already correct)
        } else if (locationType >= TURNTABLE_RAY_OFFSET) {
            result = getRayCoordsIndexed(locationType - TURNTABLE_RAY_OFFSET);
        } else {
            log.error("Invalid connection type " + locationType); // NOI18N
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(int connectionType) throws jmri.JmriException {
        LayoutTrack result = null;
        if (connectionType >= TURNTABLE_RAY_OFFSET) {
            result = getRayConnectIndexed(connectionType - TURNTABLE_RAY_OFFSET);
        } else {
            log.error("Invalid Turntable connection type " + connectionType); // NOI18N
            throw new jmri.JmriException("Invalid Point");
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(int connectionType, LayoutTrack o, int type) throws jmri.JmriException {
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of connection to LevelXing - " + type);
            throw new jmri.JmriException("unexpected type of connection to LevelXing - " + type);
        }
        if (connectionType >= TURNTABLE_RAY_OFFSET) {
            if ((o == null) || (o instanceof TrackSegment)) {
                setRayConnect((TrackSegment) o, connectionType - TURNTABLE_RAY_OFFSET);
            } else {
                String msg = "Invalid object type " + o.getClass().getName(); // NOI18N
                log.error(msg);
                throw new jmri.JmriException(msg);
            }
        } else {
            String msg = "Invalid Connection Type " + connectionType; // NOI18N
            log.error(msg);
            throw new jmri.JmriException(msg);
        }
    }

    /**
     * Test if ray with this index is a mainline track or not.
     * <p>
     * Defaults to false (not mainline) if connecting track segment is missing.
     *
     * @param index the index
     * @return true if connecting track segment is mainline
     */
    public boolean isMainlineIndexed(int index) {
        boolean result = false; // assume failure (pessimist!)

        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                TrackSegment ts = rt.getConnect();
                if (ts != null) {
                    result = ts.isMainline();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Test if ray at this index is a mainline track or not.
     * <p>
     * Defaults to false (not mainline) if connecting track segment is missing.
     *
     * @param i the index
     * @return true if connecting track segment is mainline
     */
    public boolean isMainlineOrdered(int i) {
        boolean result = false; // assume failure (pessimist!)
        if (i < rayList.size()) {
            RayTrack rt = rayList.get(i);
            if (rt != null) {
                TrackSegment ts = rt.getConnect();
                if (ts != null) {
                    result = ts.isMainline();
                }
            }
        }
        return result;
    }

    @Override
    public boolean isMainline() {
        return false;
    }

    //
    // Modify coordinates methods
    //

    /**
     * Scale this LayoutTrack's coordinates by the x and y factors.
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    @Override
    public void scaleCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        center = MathUtil.granulize(MathUtil.multiply(center, factor), 1.0);
    }

    /**
     * Translate (2D move) this LayoutTrack's coordinates by the x and y factors.
     *
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
    @Override
    public void translateCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        center = MathUtil.add(center, factor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int findHitPointType(Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        int result = NONE;  // assume point not on connection
        //note: optimization here: instead of creating rectangles for all the
        // points to check below, we create a rectangle for the test point
        // and test if the points below are in that rectangle instead.
        Rectangle2D r = layoutEditor.trackControlCircleRectAt(hitPoint);
        Point2D p, minPoint = MathUtil.zeroPoint2D;

        double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        double distance, minDistance = POSITIVE_INFINITY;
        if (!requireUnconnected) {
            //check the center point
            p = getCoordsCenter();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = TURNTABLE_CENTER;
            }
        }

        for (int k = 0; k < getNumberRays(); k++) {
            if (!requireUnconnected || (getRayConnectOrdered(k) == null)) {
                p = getRayCoordsOrdered(k);
                distance = MathUtil.distance(p, hitPoint);
                if (distance < minDistance) {
                    minDistance = distance;
                    minPoint = p;
                    result = TURNTABLE_RAY_OFFSET + getRayIndex(k);
                }
            }
        }
        if ((useRectangles && !r.contains(minPoint))
                || (!useRectangles && (minDistance > circleRadius))) {
            result = NONE;
        }
        return result;
    }

    /**
     * Initialization method The name of each track segment connected to a ray
     * track is initialized by by LayoutTurntableXml, then the following method
     * is called after the entire LayoutEditor is loaded to set the specific
     * TrackSegment objects.
     *
     * @param p the layout editor
     */
    @Override
    public void setObjects(LayoutEditor p) {
        for (RayTrack rt : rayList) {
            rt.setConnect(p.getFinder().findTrackSegmentByName(rt.connectName));
        }
    }

    /**
     * Is this turntable turnout controlled?
     *
     * @return true if so
     */
    public boolean isTurnoutControlled() {
        return turnoutControlled;
    }

    /**
     * Set if this turntable is turnout controlled.
     *
     * @param boo set true if so
     */
    public void setTurnoutControlled(boolean boo) {
        turnoutControlled = boo;
    }

    JPopupMenu popup = null;

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected JPopupMenu showPopup(@Nonnull MouseEvent mouseEvent) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }

        JMenuItem jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Turntable")) + getName());
        jmi.setEnabled(false);

        popup.add(new JSeparator(JSeparator.HORIZONTAL));

        popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                layoutEditor.getLayoutTrackEditors().editLayoutTurntable(LayoutTurntable.this);
            }
        });
        popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (layoutEditor.removeTurntable(LayoutTurntable.this)) {
                    // Returned true if user did not cancel
                    remove();
                    dispose();
                }
            }
        });
        layoutEditor.setShowAlignmentMenu(popup);
        popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        return popup;
    }

    private JPopupMenu rayPopup = null;

    protected void showRayPopUp(MouseEvent e, int index) {
        if (rayPopup != null) {
            rayPopup.removeAll();
        } else {
            rayPopup = new JPopupMenu();
        }

        for (RayTrack rt : rayList) {
            if (rt.getConnectionIndex() == index) {
                JMenuItem jmi = rayPopup.add("Turntable Ray " + index);
                jmi.setEnabled(false);

                rayPopup.add(new AbstractAction(
                        Bundle.getMessage("MakeLabel",
                                Bundle.getMessage("Connected"))
                        + rt.getConnect().getName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorFindItems lf = layoutEditor.getFinder();
                        LayoutTrack lt = lf.findObjectByName(rt.getConnect().getName());
                        // this shouldn't ever be null... however...
                        if (lt != null) {
                            layoutEditor.setSelectionRect(lt.getBounds());
                            lt.showPopup();
                        }
                    }
                });

                if (rt.getTurnout() != null) {
                    String info = rt.getTurnout().getDisplayName();
                    String stateString = getTurnoutStateString(rt.getTurnoutState());
                    if (!stateString.isEmpty()) {
                        info += " (" + stateString + ")";
                    }
                    jmi = rayPopup.add(info);
                    jmi.setEnabled(false);

                    rayPopup.add(new JSeparator(JSeparator.HORIZONTAL));

                    JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(Bundle.getMessage("Disabled"));
                    cbmi.setSelected(rt.isDisabled());
                    rayPopup.add(cbmi);
                    cbmi.addActionListener((java.awt.event.ActionEvent e2) -> {
                        JCheckBoxMenuItem o = (JCheckBoxMenuItem) e2.getSource();
                        rt.setDisabled(o.isSelected());
                    });

                    cbmi = new JCheckBoxMenuItem(Bundle.getMessage("DisabledWhenOccupied"));
                    cbmi.setSelected(rt.isDisabledWhenOccupied());
                    rayPopup.add(cbmi);
                    cbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                        JCheckBoxMenuItem o = (JCheckBoxMenuItem) e3.getSource();
                        rt.setDisabledWhenOccupied(o.isSelected());
                    });
                }
                rayPopup.show(e.getComponent(), e.getX(), e.getY());
                break;
            }
        }
    }

    /**
     * Set turntable position to the ray with this index.
     *
     * @param index the index
     */
    public void setPosition(int index) {
        if (isTurnoutControlled()) {
            boolean found = false; // assume failure (pessimist!)
            for (RayTrack rt : rayList) {
                if (rt.getConnectionIndex() == index) {
                    lastKnownIndex = index;
                    rt.setPosition();
                    layoutEditor.redrawPanel();
                    layoutEditor.setDirty();
                    found = true;
                    break;
                }
            }
            if (!found) {
                log.error("Attempt to set the position on a non-existant ray track");
            }
        }
    }

    /**
     * Get the turntable position.
     *
     * @return the turntable position
     */
    public int getPosition() {
        return lastKnownIndex;
    }

    /**
     * Delete this ray track.
     *
     * @param rayTrack the ray track
     */
    protected void deleteRay(RayTrack rayTrack) {
        TrackSegment t = null;
        if (rayTrack == null) {
            log.error("rayTrack is null!");
        } else {
            t = rayTrack.getConnect();
            getRayList().remove(rayTrack.getConnectionIndex());
            rayTrack.dispose();
        }
        if (t != null) {
            layoutEditor.removeTrackSegment(t);
        }

        // update the panel
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
    }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove().
     */
    void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
        for (RayTrack rt : rayList) {
            rt.dispose();
        }
    }

    /**
     * Remove this object from display and persistance.
     */
    void remove() {
        // remove from persistance by flagging inactive
        active = false;
    }

    private boolean active = true;

    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    protected class RayTrack {

        /**
         * constructor for RayTracks
         *
         * @param angle its angle
         * @param index its index
         */
        public RayTrack(double angle, int index) {
            rayAngle = MathUtil.wrapPM360(angle);
            connect = null;
            connectionIndex = index;

            disabled = false;
            disableWhenOccupied = false;
        }

        // persistant instance variables
        private double rayAngle = 0.0;
        private TrackSegment connect = null;
        private int connectionIndex = -1;

        private boolean disabled = false;
        private boolean disableWhenOccupied = false;

        //
        // Accessor routines
        //

        /**
         * Set ray track disabled.
         *
         * @param boo set true to disable
         */
        public void setDisabled(boolean boo) {
            if (disabled != boo) {
                disabled = boo;
                if (layoutEditor != null) {
                    layoutEditor.redrawPanel();
                }
            }
        }

        /**
         * Is this ray track disabled?
         *
         * @return true if so
         */
        public boolean isDisabled() {
            return disabled;
        }

        /**
         * Set ray track disabled if occupied.
         *
         * @param boo set true to disable if occupied
         */
        public void setDisabledWhenOccupied(boolean boo) {
            if (disableWhenOccupied != boo) {
                disableWhenOccupied = boo;
                if (layoutEditor != null) {
                    layoutEditor.redrawPanel();
                }
            }
        }

        /**
         * Is ray track disabled if occupied?
         *
         * @return true if so
         */
        public boolean isDisabledWhenOccupied() {
            return disableWhenOccupied;
        }

        /**
         * get the track segment connected to this ray
         *
         * @return the track segment connected to this ray
         */
        public TrackSegment getConnect() {
            return connect;
        }

        /**
         * set the track segment connected to this ray
         *
         * @param ts the track segment to connect to this ray
         */
        public void setConnect(TrackSegment ts) {
            connect = ts;
        }

        /**
         * get the angle for this ray
         *
         * @return the angle for this ray
         */
        public double getAngle() {
            return rayAngle;
        }

        /**
         * set the angle for this ray
         *
         * @param an the angle for this ray
         */
        public void setAngle(double an) {
            rayAngle = MathUtil.wrapPM360(an);
        }

        /**
         * get the connection index for this ray
         *
         * @return the connection index for this ray
         */
        public int getConnectionIndex() {
            return connectionIndex;
        }

        /**
         * is this ray occupied?
         *
         * @return true if occupied
         */
        private boolean isOccupied() {
            boolean result = false; // assume not
            if (connect != null) {  // does it have a connection? (yes)
                LayoutBlock lb = connect.getLayoutBlock();
                if (lb != null) {   // does the connection have a block? (yes)
                    // is the block occupied?
                    result = (lb.getOccupancy() == LayoutBlock.OCCUPIED);
                }
            }
            return result;
        }

        // initialization instance variable (used when loading a LayoutEditor)
        public String connectName = "";

        private NamedBeanHandle<Turnout> namedTurnout;
        //Turnout t;
        private int turnoutState;
        private PropertyChangeListener mTurnoutListener;

        /**
         * Set the turnout and state for this ray track.
         *
         * @param turnoutName the turnout name
         * @param state       its state
         */
        public void setTurnout(String turnoutName, int state) {
            Turnout turnout = null;
            if (mTurnoutListener == null) {
                mTurnoutListener = (PropertyChangeEvent e) -> {
                    if (getTurnout().getKnownState() == turnoutState) {
                        lastKnownIndex = connectionIndex;
                        layoutEditor.redrawPanel();
                        layoutEditor.setDirty();
                    }
                };
            }
            if (turnoutName != null) {
                turnout = jmri.InstanceManager.turnoutManagerInstance().
                        getTurnout(turnoutName);
            }
            if (namedTurnout != null && namedTurnout.getBean() != turnout) {
                namedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            }
            if (turnout != null && (namedTurnout == null || namedTurnout.getBean() != turnout)) {
                namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutName, turnout);
                turnout.addPropertyChangeListener(mTurnoutListener, turnoutName, "Layout Editor Turntable");
            }
            if (turnout == null) {
                namedTurnout = null;
            }

            if (this.turnoutState != state) {
                this.turnoutState = state;
            }
        }

        /**
         * Set the position for this ray track.
         */
        public void setPosition() {
            if (namedTurnout != null) {
                if (disableWhenOccupied && isOccupied()) {
                    log.debug("Can not setPosition of turntable ray when it is occupied");
                } else {
                    getTurnout().setCommandedState(turnoutState);
                }
            }
        }

        /**
         * Get the turnout for this ray track.
         *
         * @return the turnout
         */
        public Turnout getTurnout() {
            if (namedTurnout == null) {
                return null;
            }
            return namedTurnout.getBean();
        }

        /**
         * Get the turnout name for the ray track.
         *
         * @return the turnout name
         */
        public String getTurnoutName() {
            if (namedTurnout == null) {
                return null;
            }
            return namedTurnout.getName();
        }

        /**
         * Get the state for the turnout for this ray track.
         *
         * @return the state
         */
        public int getTurnoutState() {
            return turnoutState;
        }

        /**
         * Dispose of this ray track.
         */
        void dispose() {
            if (getTurnout() != null) {
                getTurnout().removePropertyChangeListener(mTurnoutListener);
            }
            if (lastKnownIndex == connectionIndex) {
                lastKnownIndex = -1;
            }
        }
    }   // class RayTrack

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        float trackWidth = 2.F;
        float halfTrackWidth = trackWidth / 2.f;
        double radius = getRadius(), diameter = radius + radius;

        if (isBlock && isMain) {
            Stroke stroke = g2.getStroke();
            Color color = g2.getColor();
            // draw turntable circle - default track color, side track width
            g2.setStroke(new BasicStroke(trackWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.setColor(layoutEditor.getDefaultTrackColorColor());
            g2.draw(new Ellipse2D.Double(center.getX() - radius, center.getY() - radius, diameter, diameter));
            g2.setStroke(stroke);
            g2.setColor(color);
        }

        // draw ray tracks
        for (int j = 0; j < getNumberRays(); j++) {
            boolean main = false;
            TrackSegment ts = getRayConnectOrdered(j);
            if (ts != null) {
                main = ts.isMainline();
            }
            if (isBlock) {
                if (ts == null) {
                    g2.setColor(layoutEditor.getDefaultTrackColorColor());
                } else {
                    setColorForTrackBlock(g2, ts.getLayoutBlock());
                }
            }
            if (main == isMain) {
                Point2D pt2 = getRayCoordsOrdered(j);
                Point2D delta = MathUtil.normalize(MathUtil.subtract(pt2, center), radius);
                Point2D pt1 = MathUtil.add(center, delta);
                g2.draw(new Line2D.Double(pt1, pt2));
                if (isTurnoutControlled() && (getPosition() == j)) {
                    delta = MathUtil.normalize(delta, radius - halfTrackWidth);
                    pt1 = MathUtil.subtract(center, delta);
                    g2.draw(new Line2D.Double(pt1, pt2));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        float trackWidth = 2.F;
        float halfTrackWidth = trackWidth / 2.f;

        // draw ray tracks
        for (int j = 0; j < getNumberRays(); j++) {
            boolean main = false;
            TrackSegment ts = getRayConnectOrdered(j);
            if (ts != null) {
                main = ts.isMainline();
            }
            if (main == isMain) {
                Point2D pt2 = getRayCoordsOrdered(j);
                Point2D vDelta = MathUtil.normalize(MathUtil.subtract(pt2, center), radius);
                Point2D vDeltaO = MathUtil.normalize(MathUtil.orthogonal(vDelta), railDisplacement);
                Point2D pt1 = MathUtil.add(center, vDelta);
                Point2D pt1L = MathUtil.subtract(pt1, vDeltaO);
                Point2D pt1R = MathUtil.add(pt1, vDeltaO);
                Point2D pt2L = MathUtil.subtract(pt2, vDeltaO);
                Point2D pt2R = MathUtil.add(pt2, vDeltaO);
                g2.draw(new Line2D.Double(pt1L, pt2L));
                g2.draw(new Line2D.Double(pt1R, pt2R));
                if (isTurnoutControlled() && (getPosition() == j)) {
                    vDelta = MathUtil.normalize(vDelta, radius - halfTrackWidth);
                    pt1 = MathUtil.subtract(center, vDelta);
                    pt1L = MathUtil.subtract(pt1, vDeltaO);
                    pt1R = MathUtil.add(pt1, vDeltaO);
                    g2.draw(new Line2D.Double(pt1L, pt2L));
                    g2.draw(new Line2D.Double(pt1R, pt2R));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, int specificType) {
        for (int j = 0; j < getNumberRays(); j++) {
            if ((specificType == NONE) || (specificType == (TURNTABLE_RAY_OFFSET + j))) {
                if (getRayConnectOrdered(j) == null) {
                    Point2D pt = getRayCoordsOrdered(j);
                    g2.fill(layoutEditor.trackControlCircleAt(pt));
                }
            }
        }
    }

    /**
     * Draw this turntable's controls.
     *
     * @param g2 the graphics port to draw to
     */
    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        if (isTurnoutControlled()) {
            // draw control circles at all but current position ray tracks
            for (int j = 0; j < getNumberRays(); j++) {
                if (getPosition() != j) {
                    RayTrack rt = rayList.get(j);
                    if (!rt.isDisabled() && !(rt.isDisabledWhenOccupied() && rt.isOccupied())) {
                        Point2D pt = getRayCoordsOrdered(j);
                        g2.draw(layoutEditor.trackControlCircleAt(pt));
                    }
                }
            }
        }
    }

    /**
     * Draw this turntable's edit controls.
     *
     * @param g2 the graphics port to draw to
     */
    @Override
    protected void drawEditControls(Graphics2D g2) {
        Point2D pt = getCoordsCenter();
        g2.setColor(layoutEditor.getDefaultTrackColorColor());
        g2.draw(layoutEditor.trackControlCircleAt(pt));

        for (int j = 0; j < getNumberRays(); j++) {
            pt = getRayCoordsOrdered(j);

            if (getRayConnectOrdered(j) == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
            g2.draw(layoutEditor.trackEditControlRectAt(pt));
        }
    }

    /*
     * {@inheritDoc}
     */
    @Override
    protected void reCheckBlockBoundary() {
        // nothing to see here... move along...
    }

    /*
     * {@inheritDoc}
     */
    @Override
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        // nothing to see here... move along...
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> checkForFreeConnections() {
        List<Integer> result = new ArrayList<>();

        for (int k = 0; k < getNumberRays(); k++) {
            if (getRayConnectOrdered(k) == null) {
                result.add(Integer.valueOf(TURNTABLE_RAY_OFFSET + getRayIndex(k)));
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkForUnAssignedBlocks() {
        // Layout turnouts get their block information from the
        // track segments attached to their rays so...
        // nothing to see here... move along...
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkForNonContiguousBlocks(
            @Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetsMap) {
        /*
         * For each (non-null) blocks of this track do:
         * #1) If it's got an entry in the blockNamesToTrackNameSetMap then
         * #2) If this track is already in the TrackNameSet for this block
         *     then return (done!)
         * #3) else add a new set (with this block/track) to
         *     blockNamesToTrackNameSetMap and check all the connections in this
         *     block (by calling the 2nd method below)
         * <p>
         *     Basically, we're maintaining contiguous track sets for each block found
         *     (in blockNamesToTrackNameSetMap)
         */

        // We're using a map here because it is convient to
        // use it to pair up blocks and connections
        Map<LayoutTrack, String> blocksAndTracksMap = new HashMap<>();
        for (int k = 0; k < getNumberRays(); k++) {
            TrackSegment ts = getRayConnectOrdered(k);
            if (ts != null) {
                String blockName = ts.getBlockName();
                blocksAndTracksMap.put(ts, blockName);
            }
        }

        List<Set<String>> TrackNameSets = null;
        Set<String> TrackNameSet = null;
        for (Map.Entry<LayoutTrack, String> entry : blocksAndTracksMap.entrySet()) {
            LayoutTrack theConnect = entry.getKey();
            String theBlockName = entry.getValue();

            TrackNameSet = null;    // assume not found (pessimist!)
            TrackNameSets = blockNamesToTrackNameSetsMap.get(theBlockName);
            if (TrackNameSets != null) { // (#1)
                for (Set<String> checkTrackNameSet : TrackNameSets) {
                    if (checkTrackNameSet.contains(getName())) { // (#2)
                        TrackNameSet = checkTrackNameSet;
                        break;
                    }
                }
            } else {    // (#3)
                log.debug("*New block ('{}') trackNameSets", theBlockName);
                TrackNameSets = new ArrayList<>();
                blockNamesToTrackNameSetsMap.put(theBlockName, TrackNameSets);
            }
            if (TrackNameSet == null) {
                TrackNameSet = new LinkedHashSet<>();
                TrackNameSets.add(TrackNameSet);
            }
            if (TrackNameSet.add(getName())) {
                log.debug("*    Add track '{}' to trackNameSet for block '{}'", getName(), theBlockName);
            }
            theConnect.collectContiguousTracksNamesInBlockNamed(theBlockName, TrackNameSet);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
        if (!TrackNameSet.contains(getName())) {
            // for all the rays with matching blocks in this turnout
            //  #1) if its track segment's block is in this block
            //  #2)     add turntable to TrackNameSet (if not already there)
            //  #3)     if the track segment isn't in the TrackNameSet
            //  #4)         flood it
            for (int k = 0; k < getNumberRays(); k++) {
                TrackSegment ts = getRayConnectOrdered(k);
                if (ts != null) {
                    String blk = ts.getBlockName();
                    if ((!blk.isEmpty()) && (blk.equals(blockName))) { // (#1)
                        // if we are added to the TrackNameSet
                        if (TrackNameSet.add(getName())) {
                            log.debug("*    Add track '{}' for block '{}'", getName(), blockName);
                        }
                        // it's time to play... flood your neighbours!
                        ts.collectContiguousTracksNamesInBlockNamed(blockName,
                                TrackNameSet); // (#4)
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
        // turntables don't have blocks...
        // nothing to see here, move along...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemove() {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutTurntable.class);

}
