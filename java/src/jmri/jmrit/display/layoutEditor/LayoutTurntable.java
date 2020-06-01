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
import java.beans.*;
import java.text.MessageFormat;
import java.util.*;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import jmri.*;
import jmri.util.MathUtil;
import org.slf4j.*;

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
    private NamedBeanHandle<LayoutBlock> namedLayoutBlock = null;

    private boolean turnoutControlled = false;
    private double radius = 25.0;
    private int lastKnownIndex = -1;
    private final jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutTurntableEditor editor;
    // persistent instance variables (saved between sessions)
    private final List<RayTrack> rayTrackList = new ArrayList<>(); // list of Ray Track objects

    /**
     * Constructor method
     *
     * @param id           the name for the turntable
     * @param c            where to put it
     * @param layoutEditor what layout editor panel to put it in
     */
    public LayoutTurntable(@Nonnull String id, @Nonnull Point2D c, @Nonnull LayoutEditor layoutEditor) {
        super(id, c, layoutEditor);
        radius = 25.0;
        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutTurntableEditor(layoutEditor);
    }

    //
    /**
     * Get a string that represents this object. This should only be used for
     * debugging.
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
     * @return the layout block name
     */
    @Nonnull
    public String getBlockName() {
        String result = null;
        if (namedLayoutBlock != null) {
            result = namedLayoutBlock.getName();
        }
        return ((result == null) ? "" : result);
    }

    /**
     * @return the layout block
     */
    @CheckForNull
    public LayoutBlock getLayoutBlock() {
        return (namedLayoutBlock != null) ? namedLayoutBlock.getBean() : null;
    }

    /**
     * Set up a LayoutBlock for this LayoutTurntable.
     *
     * @param newLayoutBlock the LayoutBlock to set
     */
    public void setLayoutBlock(@CheckForNull LayoutBlock newLayoutBlock) {
        LayoutBlock layoutBlock = getLayoutBlock();
        if (layoutBlock != newLayoutBlock) {
            /// block has changed, if old block exists, decrement use
            if (layoutBlock != null) {
                layoutBlock.decrementUse();
            }
            if (newLayoutBlock != null) {
                String newName = newLayoutBlock.getUserName();
                if ((newName != null) && !newName.isEmpty()) {
                    namedLayoutBlock = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(newName, newLayoutBlock);
                } else {
                    namedLayoutBlock = null;
                }
            } else {
                namedLayoutBlock = null;
            }
        }
    }

    /**
     * Set up a LayoutBlock for this LayoutTurntable.
     *
     * @param name the name of the new LayoutBlock
     */
    public void setLayoutBlockByName(@CheckForNull String name) {
        if ((name != null) && !name.isEmpty()) {
            setLayoutBlock(layoutEditor.provideLayoutBlock(name));
        }
    }

    /*
     * non-accessor methods
     */
    /**
     * @return the bounds of this turntable.
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D result;

        result = new Rectangle2D.Double(getCoordsCenter().getX(), getCoordsCenter().getY(), 0, 0);
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
    public RayTrack addRay(double angle) {
        RayTrack rt = new RayTrack(angle, getNewIndex());
        rayTrackList.add(rt);
        return rt;
    }

    private int getNewIndex() {
        int index = -1;
        if (rayTrackList.isEmpty()) {
            return 0;
        }

        boolean found = true;
        while (found) {
            index++;
            found = false; // assume failure (pessimist!)
            for (RayTrack rt : rayTrackList) {
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
        /// if (ray!=null) {
        rayTrackList.add(rt);
        rt.connectName = name;
        //}
    }

    /**
     * Get the connection for the ray with this index.
     *
     * @param index the index
     * @return the connection for the ray with this value of getConnectionIndex
     */
    public TrackSegment getRayConnectIndexed(int index) {
        TrackSegment result = null;
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                result = rt.getConnect();
                break;
            }
        }
        return result;
    }

    /**
     * Get the connection for the ray at the index in the rayTrackList.
     *
     * @param i the index in the rayTrackList
     * @return the connection for the ray at that index in the rayTrackList or null
     */
    public TrackSegment getRayConnectOrdered(int i) {
        TrackSegment result = null;

        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            if (rt != null) {
                result = rt.getConnect();
            }
        }
        return result;
    }

    /**
     * Set the connection for the ray at the index in the rayTrackList.
     *
     * @param ts    the connection
     * @param index the index in the rayTrackList
     */
    public void setRayConnect(TrackSegment ts, int index) {
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                rt.setConnect(ts);
                break;
            }
        }
    }

    // should only be used by xml save code
    public List<RayTrack> getRayTrackList() {
        return rayTrackList;
    }

    /**
     * Get the number of rays on turntable.
     *
     * @return the number of rays
     */
    public int getNumberRays() {
        return rayTrackList.size();
    }

    /**
     * Get the index for the ray at this position in the rayTrackList.
     *
     * @param i the position in the rayTrackList
     * @return the index
     */
    public int getRayIndex(int i) {
        int result = 0;
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            result = rt.getConnectionIndex();
        }
        return result;
    }

    /**
     * Get the angle for the ray at this position in the rayTrackList.
     *
     * @param i the position in the rayTrackList
     * @return the angle
     */
    public double getRayAngle(int i) {
        double result = 0.0;
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
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
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                rt.setTurnout(turnoutName, state);
                found = true;
                break;
            }
        }
        if (!found) {
            log.error("{}.setRayTurnout({}, {}, {}); Attempt to add Turnout control to a non-existant ray track",
                    getName(), index, turnoutName, state);
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
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
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
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
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
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
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
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
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
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
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
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
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
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
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
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                double angle = Math.toRadians(rt.getAngle());
                // calculate coordinates
                result = new Point2D.Double(
                        (getCoordsCenter().getX() + (rayRadius * Math.sin(angle))),
                        (getCoordsCenter().getY() - (rayRadius * Math.cos(angle))));
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
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
            if (rt != null) {
                double angle = Math.toRadians(rt.getAngle());
                double rayRadius = radius + LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
                // calculate coordinates
                result = new Point2D.Double(
                        (getCoordsCenter().getX() + (rayRadius * Math.sin(angle))),
                        (getCoordsCenter().getY() - (rayRadius * Math.cos(angle))));
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
        for (RayTrack rt : rayTrackList) {
            if (rt.getConnectionIndex() == index) {
                // convert these coordinates to an angle
                double angle = Math.atan2(x - getCoordsCenter().getX(), y - getCoordsCenter().getY());
                angle = MathUtil.wrapPM360(180.0 - Math.toDegrees(angle));
                rt.setAngle(angle);
                found = true;
                break;
            }
        }
        if (!found) {
            log.error("{}.setRayCoordsIndexed({}, {}, {}); Attempt to move a non-existant ray track",
                    getName(), x, y, index);
        }
    }

    /**
     * Set the coordinates for the ray at this index.
     *
     * @param point the new coordinates
     * @param index the index
     */
    public void setRayCoordsIndexed(Point2D point, int index) {
        setRayCoordsIndexed(point.getX(), point.getY(), index);
    }

    /**
     * Get the coordinates for a specified connection type.
     *
     * @param connectionType the connection type
     * @return the coordinates
     */
    @Override
    public Point2D getCoordsForConnectionType(HitPointType connectionType) {
        Point2D result = getCoordsCenter();
        if (HitPointType.TURNTABLE_CENTER == connectionType) {
            // nothing to see here, move along...
            // (results are already correct)
        } else if (HitPointType.isTurntableRayHitType(connectionType)) {
            result = getRayCoordsIndexed(connectionType.turntableTrackIndex());
        } else {
            log.error("{}.getCoordsForConnectionType({}); Invalid connection type",
                    getName(), connectionType); // NOI18N
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        LayoutTrack result = null;
        if (HitPointType.isTurntableRayHitType(connectionType)) {
            result = getRayConnectIndexed(connectionType.turntableTrackIndex());
        } else {
            String errString = MessageFormat.format("{0}.getCoordsForConnectionType({1}); Invalid connection type",
                    getName(), connectionType); // NOI18N
            log.error("will throw {}", errString); // NOI18N
            throw new jmri.JmriException(errString);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(HitPointType connectionType, LayoutTrack o, HitPointType type) throws jmri.JmriException {
        if ((type != HitPointType.TRACK) && (type != HitPointType.NONE)) {
            String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid type",
                    getName(), connectionType, (o == null) ? "null" : o.getName(), type); // NOI18N
            log.error("will throw {}", errString); // NOI18N
            throw new jmri.JmriException(errString);
        }
        if (HitPointType.isTurntableRayHitType(connectionType)) {
            if ((o == null) || (o instanceof TrackSegment)) {
                setRayConnect((TrackSegment) o, connectionType.turntableTrackIndex());
            } else {
                String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid object: {4}",
                        getName(), connectionType, o.getName(),
                        type, o.getClass().getName()); // NOI18N
                log.error("will throw {}", errString); // NOI18N
                throw new jmri.JmriException(errString);
            }
        } else {
            String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid connection type",
                    getName(), connectionType, (o == null) ? "null" : o.getName(), type); // NOI18N
            log.error("will throw {}", errString); // NOI18N
            throw new jmri.JmriException(errString);
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

        for (RayTrack rt : rayTrackList) {
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
     * Defaults to false (not mainline) if connecting track segment is missing
     *
     * @param i the index
     * @return true if connecting track segment is mainline
     */
    public boolean isMainlineOrdered(int i) {
        boolean result = false; // assume failure (pessimist!)
        if (i < rayTrackList.size()) {
            RayTrack rt = rayTrackList.get(i);
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
    public void scaleCoords(double xFactor, double yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        super.setCoordsCenter(MathUtil.granulize(MathUtil.multiply(getCoordsCenter(), factor), 1.0));
        radius *= Math.hypot(xFactor, yFactor);
    }

    /**
     * Translate (2D move) this LayoutTrack's coordinates by the x and y
     * factors.
     *
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
    @Override
    public void translateCoords(double xFactor, double yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        super.setCoordsCenter(MathUtil.add(getCoordsCenter(), factor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rotateCoords(double angleDEG) {
        // rotate all rayTracks
        rayTrackList.forEach((rayTrack) -> {
            rayTrack.setAngle(rayTrack.getAngle() + angleDEG);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HitPointType findHitPointType(Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        HitPointType result = HitPointType.NONE;  // assume point not on connection
        // note: optimization here: instead of creating rectangles for all the
        // points to check below, we create a rectangle for the test point
        // and test if the points below are in that rectangle instead.
        Rectangle2D r = layoutEditor.layoutEditorControlCircleRectAt(hitPoint);
        Point2D p, minPoint = MathUtil.zeroPoint2D;

        double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        double distance, minDistance = POSITIVE_INFINITY;
        if (!requireUnconnected) {
            // check the center point
            p = getCoordsCenter();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.TURNTABLE_CENTER;
            }
        }

        for (int k = 0; k < getNumberRays(); k++) {
            if (!requireUnconnected || (getRayConnectOrdered(k) == null)) {
                p = getRayCoordsOrdered(k);
                distance = MathUtil.distance(p, hitPoint);
                if (distance < minDistance) {
                    minDistance = distance;
                    minPoint = p;
                    result = HitPointType.turntableTrackIndexedValue(k);
                }
            }
        }
        if ((useRectangles && !r.contains(minPoint))
                || (!useRectangles && (minDistance > circleRadius))) {
            result = HitPointType.NONE;
        }
        return result;
    }

    public String tLayoutBlockName = "";

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
        if (tLayoutBlockName != null && !tLayoutBlockName.isEmpty()) {
            setLayoutBlockByName(tLayoutBlockName);
        }
        tLayoutBlockName = null; /// release this memory

        rayTrackList.forEach((rt) -> {
            rt.setConnect(p.getFinder().findTrackSegmentByName(rt.connectName));
        });
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

    private JPopupMenu popupMenu = null;

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected JPopupMenu showPopup(@Nonnull MouseEvent mouseEvent) {
        if (popupMenu != null) {
            popupMenu.removeAll();
        } else {
            popupMenu = new JPopupMenu();
        }

        JMenuItem jmi = popupMenu.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Turntable")) + getName());
        jmi.setEnabled(false);

        LayoutBlock lb = getLayoutBlock();
        if (lb == null) {
            jmi = popupMenu.add(Bundle.getMessage("NoBlock"));
        } else {
            String displayName = lb.getDisplayName();
            jmi = popupMenu.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameBlock")) + displayName);
        }
        jmi.setEnabled(false);

        /// if there are any track connections
        if (!rayTrackList.isEmpty()) {
            JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections"));
            rayTrackList.forEach((rt) -> {
                TrackSegment ts = rt.getConnect();
                if (ts != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "" + rt.getConnectionIndex()) + ts.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            layoutEditor.setSelectionRect(ts.getBounds());
                            ts.showPopup();
                        }
                    });
                }
            });
            popupMenu.add(connectionsMenu);
        }

        popupMenu.add(new JSeparator(JSeparator.HORIZONTAL));

        popupMenu.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.editLayoutTrack(LayoutTurntable.this);
            }
        });
        popupMenu.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (layoutEditor.removeTurntable(LayoutTurntable.this)) {
                    // Returned true if user did not cancel
                    remove();
                    dispose();
                }
            }
        });
        layoutEditor.setShowAlignmentMenu(popupMenu);
        popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        return popupMenu;
    }

    private JPopupMenu rayPopup = null;

    protected void showRayPopUp(MouseEvent e, int index) {
        if (rayPopup != null) {
            rayPopup.removeAll();
        } else {
            rayPopup = new JPopupMenu();
        }

        for (RayTrack rt : rayTrackList) {
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
            for (RayTrack rt : rayTrackList) {
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
                log.error("{}.setPosition({}); Attempt to set the position on a non-existant ray track",
                        getName(), index);
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
    public void deleteRay(RayTrack rayTrack) {
        TrackSegment t = null;
        if (rayTrackList == null) {
            log.error("{}.deleteRay(null); rayTrack is null", getName());
        } else {
            t = rayTrack.getConnect();
            getRayTrackList().remove(rayTrack.getConnectionIndex());
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
    public void dispose() {
        if (popupMenu != null) {
            popupMenu.removeAll();
        }
        popupMenu = null;
        rayTrackList.forEach((rt) -> {
            rt.dispose();
        });
    }

    /**
     * Remove this object from display and persistance.
     */
    public void remove() {
        // remove from persistance by flagging inactive
        active = false;
    }

    private boolean active = true;

    /**
     * Get if turntable is active.
     * "active" means that the object is still displayed, and should be stored.
     * @return true if active, else false.
     */
    public boolean isActive() {
        return active;
    }

    public class RayTrack {

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
         * Set the track segment connected to this ray.
         *
         * @param ts the track segment to connect to this ray
         */
        public void setConnect(TrackSegment ts) {
            connect = ts;
        }

        /**
         * Get the angle for this ray.
         *
         * @return the angle for this ray
         */
        public double getAngle() {
            return rayAngle;
        }

        /**
         * Set the angle for this ray.
         *
         * @param an the angle for this ray
         */
        public void setAngle(double an) {
            rayAngle = MathUtil.wrapPM360(an);
        }

        /**
         * Get the connection index for this ray.
         *
         * @return the connection index for this ray
         */
        public int getConnectionIndex() {
            return connectionIndex;
        }

        /**
         * Is this ray occupied?
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
        // Turnout t;
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
                turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
            }
            if (namedTurnout != null && namedTurnout.getBean() != turnout) {
                namedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            }
            if (turnout != null && (namedTurnout == null || namedTurnout.getBean() != turnout)) {
                if ((turnoutName != null) && !turnoutName.isEmpty()) {
                    namedTurnout = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutName, turnout);
                    turnout.addPropertyChangeListener(mTurnoutListener, turnoutName, "Layout Editor Turntable");
                }
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
     * Draw track decorations.
     * 
     * This type of track has none, so this method is empty.
     */
    @Override
    protected void drawDecorations(Graphics2D g2) {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        float trackWidth = 2.F;
        float halfTrackWidth = trackWidth / 2.f;
        double radius = getRadius(), diameter = 2.f * radius;

        if (isBlock && isMain) {
            double radius2 = Math.max(radius / 4.f, trackWidth * 2);
            double diameter2 = radius2 * 2.f;
            Stroke stroke = g2.getStroke();
            Color color = g2.getColor();
            // draw turntable circle - default track color, side track width
            g2.setStroke(new BasicStroke(trackWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.setColor(layoutEditor.getDefaultTrackColorColor());
            g2.draw(new Ellipse2D.Double(getCoordsCenter().getX() - radius, getCoordsCenter().getY() - radius, diameter, diameter));
            g2.draw(new Ellipse2D.Double(getCoordsCenter().getX() - radius2, getCoordsCenter().getY() - radius2, diameter2, diameter2));
            g2.setStroke(stroke);
            g2.setColor(color);
        }

        // draw ray tracks
        for (int j = 0; j < getNumberRays(); j++) {
            boolean main = false;
            Color color = null;
            TrackSegment ts = getRayConnectOrdered(j);
            if (ts != null) {
                main = ts.isMainline();
            }

            if (isBlock) {
                if (ts == null) {
                    g2.setColor(layoutEditor.getDefaultTrackColorColor());
                } else {
                    LayoutBlock lb = ts.getLayoutBlock();
                    if (lb != null) {
                        color = g2.getColor();
                        setColorForTrackBlock(g2, lb);
                    }
                }
            }

            Point2D pt2 = getRayCoordsOrdered(j);
            Point2D delta = MathUtil.normalize(MathUtil.subtract(pt2, getCoordsCenter()), radius);
            Point2D pt1 = MathUtil.add(getCoordsCenter(), delta);
            if (main == isMain) {
                g2.draw(new Line2D.Double(pt1, pt2));
            }
            if (isMain && isTurnoutControlled() && (getPosition() == j)) {
                if (isBlock) {
                    LayoutBlock lb = getLayoutBlock();
                    if (lb != null) {
                        color = (color == null) ? g2.getColor() : color;
                        setColorForTrackBlock(g2, lb);
                    } else {
                        g2.setColor(layoutEditor.getDefaultTrackColorColor());
                    }
                }
                delta = MathUtil.normalize(delta, radius - halfTrackWidth);
                pt1 = MathUtil.subtract(getCoordsCenter(), delta);
                g2.draw(new Line2D.Double(pt1, pt2));
            }
            if (color != null) {
                g2.setColor(color); /// restore previous color
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
//            Color c = null;
            TrackSegment ts = getRayConnectOrdered(j);
            if (ts != null) {
                main = ts.isMainline();
//                LayoutBlock lb = ts.getLayoutBlock();
//                if (lb != null) {
//                    c = g2.getColor();
//                    setColorForTrackBlock(g2, lb);
//                }
            }
            Point2D pt2 = getRayCoordsOrdered(j);
            Point2D vDelta = MathUtil.normalize(MathUtil.subtract(pt2, getCoordsCenter()), radius);
            Point2D vDeltaO = MathUtil.normalize(MathUtil.orthogonal(vDelta), railDisplacement);
            Point2D pt1 = MathUtil.add(getCoordsCenter(), vDelta);
            Point2D pt1L = MathUtil.subtract(pt1, vDeltaO);
            Point2D pt1R = MathUtil.add(pt1, vDeltaO);
            Point2D pt2L = MathUtil.subtract(pt2, vDeltaO);
            Point2D pt2R = MathUtil.add(pt2, vDeltaO);
            if (main == isMain) {
                g2.draw(new Line2D.Double(pt1L, pt2L));
                g2.draw(new Line2D.Double(pt1R, pt2R));
            }
            if (isMain && isTurnoutControlled() && (getPosition() == j)) {
//                LayoutBlock lb = getLayoutBlock();
//                if (lb != null) {
//                    c = g2.getColor();
//                    setColorForTrackBlock(g2, lb);
//                } else {
//                    g2.setColor(layoutEditor.getDefaultTrackColorColor());
//                }
                vDelta = MathUtil.normalize(vDelta, radius - halfTrackWidth);
                pt1 = MathUtil.subtract(getCoordsCenter(), vDelta);
                pt1L = MathUtil.subtract(pt1, vDeltaO);
                pt1R = MathUtil.add(pt1, vDeltaO);
                g2.draw(new Line2D.Double(pt1L, pt2L));
                g2.draw(new Line2D.Double(pt1R, pt2R));
            }
//            if (c != null) {
//                g2.setColor(c); /// restore previous color
//            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        for (int j = 0; j < getNumberRays(); j++) {
            if (  (specificType == HitPointType.NONE)
                    || (specificType == (HitPointType.turntableTrackIndexedValue(j)))
                ) 
            {
                if (getRayConnectOrdered(j) == null) {
                    Point2D pt = getRayCoordsOrdered(j);
                    g2.fill(trackControlCircleAt(pt));
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
                    RayTrack rt = rayTrackList.get(j);
                    if (!rt.isDisabled() && !(rt.isDisabledWhenOccupied() && rt.isOccupied())) {
                        Point2D pt = getRayCoordsOrdered(j);
                        g2.draw(trackControlCircleAt(pt));
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
        g2.draw(trackControlCircleAt(pt));

        for (int j = 0; j < getNumberRays(); j++) {
            pt = getRayCoordsOrdered(j);

            if (getRayConnectOrdered(j) == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
            g2.draw(layoutEditor.layoutEditorControlRectAt(pt));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reCheckBlockBoundary() {
        // nothing to see here... move along...
    }

    /**
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
    public List<HitPointType> checkForFreeConnections() {
        List<HitPointType> result = new ArrayList<>();

        for (int k = 0; k < getNumberRays(); k++) {
            if (getRayConnectOrdered(k) == null) {
                result.add(HitPointType.turntableTrackIndexedValue(k));
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
        * #3) else add a new set (with this block// track) to
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

        List<Set<String>> TrackNameSets;
        Set<String> TrackNameSet;
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
                log.debug("*New block (''{}'') trackNameSets", theBlockName);
                TrackNameSets = new ArrayList<>();
                blockNamesToTrackNameSetsMap.put(theBlockName, TrackNameSets);
            }
            if (TrackNameSet == null) {
                TrackNameSet = new LinkedHashSet<>();
                TrackNameSets.add(TrackNameSet);
            }
            if (TrackNameSet.add(getName())) {
                log.debug("*    Add track ''{}'' to trackNameSet for block ''{}''", getName(), theBlockName);
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
                            log.debug("*    Add track ''{}'' for block ''{}''", getName(), blockName);
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurntable.class);

}
