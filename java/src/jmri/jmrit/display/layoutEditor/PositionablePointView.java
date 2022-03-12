package jmri.jmrit.display.layoutEditor;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;

import javax.annotation.*;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.PositionablePoint.PointType;
import jmri.jmrit.signalling.SignallingGuiTools;
import jmri.util.*;
import jmri.util.swing.JCBHandle;
import jmri.util.swing.JmriColorChooser;

/**
 * MVC View component for the PositionablePoint class.
 *
 * @author Bob Jacobsen Copyright (c) 2020
 *
 * <p>
 * Arrows and bumpers are visual, presentation aspects handled in the View.
 */
public class PositionablePointView extends LayoutTrackView {

    protected NamedBeanHandle<SignalHead> signalEastHeadNamed = null; // signal head for east (south) bound trains
    protected NamedBeanHandle<SignalHead> signalWestHeadNamed = null; // signal head for west (north) bound trains

    private NamedBeanHandle<SignalMast> eastBoundSignalMastNamed = null;
    private NamedBeanHandle<SignalMast> westBoundSignalMastNamed = null;
    /* We use a namedbeanhandle for the sensors, even though we only store the name here,
    this is so that we can keep up with moves and changes of userNames */
    private NamedBeanHandle<Sensor> eastBoundSensorNamed = null;
    private NamedBeanHandle<Sensor> westBoundSensorNamed = null;

    /**
     * constructor method.
     *
     * @param point        the positionable point.
     * @param c            location to display the positionable point
     * @param layoutEditor for access to tools
     */
    public PositionablePointView(@Nonnull PositionablePoint point,
            Point2D c,
            @Nonnull LayoutEditor layoutEditor) {
        super(point, c, layoutEditor);
        this.positionablePoint = point;
    }

    final private PositionablePoint positionablePoint;

    public PositionablePoint getPoint() {
        return positionablePoint;
    }

    // this should only be used for debugging...
    @Override
    public String toString() {
        String result = "PositionalablePoint";
        switch (getType()) {
            case ANCHOR: {
                result = "Anchor";
                break;
            }
            case END_BUMPER: {
                result = "End Bumper";
                break;
            }
            case EDGE_CONNECTOR: {
                result = "Edge Connector";
                break;
            }
            default: {
                result = "Unknown type (" + getType() + ")";
                break;
            }
        }
        return result + " '" + getName() + "'";
    }

    /**
     * Accessor methods
     *
     * @return Type enum for this Positionable Point
     */
    public PointType getType() {
        return positionablePoint.getType();
    }

    public void setType(PointType newType) {
        positionablePoint.setType(newType);

        // (temporary) we keep this echo here until we figure out where arrow info lives
        if (getType() != newType) {
            switch (newType) {
                default:
                case ANCHOR: {
                    setTypeAnchor();
                    break;
                }
                case END_BUMPER: {
                    setTypeEndBumper();
                    break;
                }
                case EDGE_CONNECTOR: {
                    setTypeEdgeConnector();
                    break;
                }
            }
            layoutEditor.repaint();
        }
    }

    private void setTypeAnchor() {
        setIdent(layoutEditor.getFinder().uniqueName("A", 1));

        // type = PointType.ANCHOR;
        positionablePoint.setTypeAnchor();

        if (getConnect1() != null) {
            TrackSegmentView ctv1 = layoutEditor.getTrackSegmentView(getConnect1());
            if (getConnect1().getConnect1() == positionablePoint) {
                ctv1.setArrowEndStart(false);
                ctv1.setBumperEndStart(false);
            }
            if (getConnect1().getConnect2() == positionablePoint) {
                ctv1.setArrowEndStop(false);
                ctv1.setBumperEndStop(false);
            }
        }
        if (getConnect2() != null) {
            TrackSegmentView ctv2 = layoutEditor.getTrackSegmentView(getConnect2());
            if (getConnect2().getConnect1() == positionablePoint) {
                ctv2.setArrowEndStart(false);
                ctv2.setBumperEndStart(false);
            }
            if (getConnect2().getConnect2() == positionablePoint) {
                ctv2.setArrowEndStop(false);
                ctv2.setBumperEndStop(false);
            }
        }
    }

    private void setTypeEndBumper() {
        setIdent(layoutEditor.getFinder().uniqueName("EB", 1));

        // type = PointType.END_BUMPER;
        positionablePoint.setTypeEndBumper();

        if (getConnect1() != null) {
            TrackSegmentView ctv1 = layoutEditor.getTrackSegmentView(getConnect1());
            if (getConnect1().getConnect1() == positionablePoint) {
                ctv1.setArrowEndStart(false);
                ctv1.setBumperEndStart(true);
            }
            if (getConnect1().getConnect2() == positionablePoint) {
                ctv1.setArrowEndStop(false);
                ctv1.setBumperEndStop(true);
            }
        }
    }

    private void setTypeEdgeConnector() {
        setIdent(layoutEditor.getFinder().uniqueName("EC", 1));

        // type = PointType.EDGE_CONNECTOR;
        positionablePoint.setTypeEdgeConnector();

        if (getConnect1() != null) {
            TrackSegmentView ctv1 = layoutEditor.getTrackSegmentView(getConnect1());
            if (getConnect1().getConnect1() == positionablePoint) {
                ctv1.setBumperEndStart(false);
            }
            if (getConnect1().getConnect2() == positionablePoint) {
                ctv1.setBumperEndStop(false);
            }
        }
    }

    public TrackSegment getConnect1() {
        return positionablePoint.getConnect1();
    }

    public TrackSegment getConnect2() {
        return positionablePoint.getConnect2();
    }

    public String getLinkedEditorName() {
        return positionablePoint.getLinkedEditorName();
    }

    public LayoutEditor getLinkedEditor() {
        return positionablePoint.getLinkedEditor();
    }

    public PositionablePoint getLinkedPoint() {
        return positionablePoint.getLinkedPoint();
    }

    public void removeLinkedPoint() {
        positionablePoint.removeLinkedPoint();
    }

    public String getLinkedPointId() {
        return positionablePoint.getLinkedPointId();
    }

    public void setLinkedPoint(PositionablePoint p) {
        positionablePoint.setLinkedPoint(p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scaleCoords(double xFactor, double yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        super.setCoordsCenter(MathUtil.granulize(MathUtil.multiply(getCoordsCenter(), factor), 1.0));
    }

    /**
     * {@inheritDoc}
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
        //can't really rotate a point... so...
        //nothing to see here... move along...
    }

    /**
     * @return the bounds of this positional point
     */
    @Override
    public Rectangle2D getBounds() {
        Point2D c = getCoordsCenter();
        //Note: empty bounds don't draw...
        // so now I'm making them 0.5 bigger in all directions (1 pixel total)
        return new Rectangle2D.Double(c.getX() - 0.5, c.getY() - 0.5, 1.0, 1.0);
    }

    @CheckReturnValue
    protected LayoutEditor getLayoutEditor() {
        return layoutEditor;
    }

    @CheckReturnValue
    @Nonnull
    public String getEastBoundSignal() {
        SignalHead h = getEastBoundSignalHead();
        if (h != null) {
            return h.getDisplayName();
        }
        return "";
    }

    @CheckForNull
    @CheckReturnValue
    public SignalHead getEastBoundSignalHead() {
        if (getType() == PointType.EDGE_CONNECTOR) {
            int dir = getConnect1Dir();
            if (dir == Path.EAST || dir == Path.SOUTH || dir == Path.SOUTH_EAST) {
                if (signalEastHeadNamed != null) {
                    return signalEastHeadNamed.getBean();
                }
                return null;
            } else if (getLinkedPoint() != null) {
                // Do some checks to find where the connection is here.
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.SOUTH || linkDir == Path.EAST || linkDir == Path.SOUTH_EAST) {
                    return getLinkedPoint().getEastBoundSignalHead();
                }
            }
        }

        if (signalEastHeadNamed != null) {
            return signalEastHeadNamed.getBean();
        }
        return null;
    }

    public void setEastBoundSignal(String signalName) {
        if (getType() == PointType.EDGE_CONNECTOR) {
            int dir = getConnect1Dir();
            if (dir == Path.EAST || dir == Path.SOUTH || dir == Path.SOUTH_EAST) {
                setEastBoundSignalName(signalName);
            } else if (getLinkedPoint() != null) {
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.SOUTH || linkDir == Path.EAST || linkDir == Path.SOUTH_EAST) {
                    getLinkedPoint().setEastBoundSignal(signalName);
                } else {
                    setEastBoundSignalName(signalName);
                }
            } else {
                setEastBoundSignalName(signalName);
            }
        } else {
            setEastBoundSignalName(signalName);
        }
    }

    private void setEastBoundSignalName(@CheckForNull String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalEastHeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalEastHeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalEastHeadNamed = null;
        }
    }

    @CheckReturnValue
    @Nonnull
    public String getWestBoundSignal() {
        SignalHead h = getWestBoundSignalHead();
        if (h != null) {
            return h.getDisplayName();
        }
        return "";
    }

    @CheckForNull
    @CheckReturnValue
    public SignalHead getWestBoundSignalHead() {
        if (getType() == PointType.EDGE_CONNECTOR) {
            int dir = getConnect1Dir();
            if (dir == Path.WEST || dir == Path.NORTH || dir == Path.NORTH_WEST) {
                if (signalWestHeadNamed != null) {
                    return signalWestHeadNamed.getBean();
                }
                return null;
            } else if (getLinkedPoint() != null) {
                // Do some checks to find where the connection is here.
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.WEST || linkDir == Path.NORTH || linkDir == Path.NORTH_WEST) {
                    return getLinkedPoint().getWestBoundSignalHead();
                }
            }
        }

        if (signalWestHeadNamed != null) {
            return signalWestHeadNamed.getBean();
        }
        return null;
    }

    public void setWestBoundSignal(String signalName) {
        if (getType() == PointType.EDGE_CONNECTOR) {
            int dir = getConnect1Dir();
            if (dir == Path.WEST || dir == Path.NORTH || dir == Path.NORTH_WEST) {
                setWestBoundSignalName(signalName);
            } else if (getLinkedPoint() != null) {
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.WEST || linkDir == Path.NORTH || linkDir == Path.NORTH_WEST) {
                    getLinkedPoint().setWestBoundSignal(signalName);
                } else {
                    setWestBoundSignalName(signalName);
                }
            } else {
                setWestBoundSignalName(signalName);
            }
        } else {
            setWestBoundSignalName(signalName);
        }
    }

    private void setWestBoundSignalName(@CheckForNull String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalWestHeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalWestHeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalWestHeadNamed = null;
        }
    }

    @CheckReturnValue
    @Nonnull
    public String getEastBoundSensorName() {
        if (eastBoundSensorNamed != null) {
            return eastBoundSensorNamed.getName();
        }
        return "";
    }

    @CheckReturnValue
    public Sensor getEastBoundSensor() {
        if (eastBoundSensorNamed != null) {
            return eastBoundSensorNamed.getBean();
        }
        return null;
    }

    public void setEastBoundSensor(String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            eastBoundSensorNamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            eastBoundSensorNamed = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            eastBoundSensorNamed = null;
        }
    }

    @CheckReturnValue
    @Nonnull
    public String getWestBoundSensorName() {
        if (westBoundSensorNamed != null) {
            return westBoundSensorNamed.getName();
        }
        return "";
    }

    @CheckReturnValue
    public Sensor getWestBoundSensor() {
        if (westBoundSensorNamed != null) {
            return westBoundSensorNamed.getBean();
        }
        return null;
    }

    public void setWestBoundSensor(String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            westBoundSensorNamed = null;
            return;
        }
        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            westBoundSensorNamed = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            westBoundSensorNamed = null;
        }
    }

    @CheckReturnValue
    @Nonnull
    public String getEastBoundSignalMastName() {
        if (getEastBoundSignalMastNamed() != null) {
            return getEastBoundSignalMastNamed().getName();
        }
        return "";
    }

    @CheckReturnValue
    public SignalMast getEastBoundSignalMast() {
        if (getEastBoundSignalMastNamed() != null) {
            return getEastBoundSignalMastNamed().getBean();
        }
        return null;
    }

    @CheckReturnValue
    private NamedBeanHandle<SignalMast> getEastBoundSignalMastNamed() {
        if (getType() == PointType.EDGE_CONNECTOR) {
            int dir = getConnect1Dir();
            if (dir == Path.SOUTH || dir == Path.EAST || dir == Path.SOUTH_EAST) {
                return eastBoundSignalMastNamed;
            } else if (getLinkedPoint() != null) {
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.SOUTH || linkDir == Path.EAST || linkDir == Path.SOUTH_EAST) {
                    return getLinkedPoint().getEastBoundSignalMastNamed();
                }
            }
        }
        return eastBoundSignalMastNamed;
    }

    public void setEastBoundSignalMast(String signalMast) {
        SignalMast mast = null;
        if (signalMast != null && !signalMast.isEmpty()) {
            mast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(signalMast);
            if (mast == null) {
                log.error("{}.setEastBoundSignalMast({}); Unable to find Signal Mast",
                        getName(), signalMast);
                return;
            }
        } else {
            eastBoundSignalMastNamed = null;
            return;
        }
        if (getType() == PointType.EDGE_CONNECTOR) {
            int dir = getConnect1Dir();
            if (dir == Path.EAST || dir == Path.SOUTH || dir == Path.SOUTH_EAST) {
                eastBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
            } else if (getLinkedPoint() != null) {
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.SOUTH || linkDir == Path.EAST || linkDir == Path.SOUTH_EAST) {
                    getLinkedPoint().setEastBoundSignalMast(signalMast);
                } else {
                    eastBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
                }
            } else {
                eastBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
            }
        } else {
            eastBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        }
    }

    @CheckReturnValue
    @Nonnull
    public String getWestBoundSignalMastName() {
        if (getWestBoundSignalMastNamed() != null) {
            return getWestBoundSignalMastNamed().getName();
        }
        return "";
    }

    @CheckReturnValue
    public SignalMast getWestBoundSignalMast() {
        if (getWestBoundSignalMastNamed() != null) {
            return getWestBoundSignalMastNamed().getBean();
        }
        return null;
    }

    @CheckReturnValue
    private NamedBeanHandle<SignalMast> getWestBoundSignalMastNamed() {
        if (getType() == PointType.EDGE_CONNECTOR) {
            int dir = getConnect1Dir();
            if (dir == Path.WEST || dir == Path.NORTH || dir == Path.NORTH_WEST) {
                return westBoundSignalMastNamed;
            } else if (getLinkedPoint() != null) {
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.WEST || linkDir == Path.NORTH || linkDir == Path.NORTH_WEST) {
                    return getLinkedPoint().getWestBoundSignalMastNamed();
                }
            }
        }
        return westBoundSignalMastNamed;
    }

    public void setWestBoundSignalMast(String signalMast) {
        SignalMast mast = null;
        if (signalMast != null && !signalMast.isEmpty()) {
            mast = InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(signalMast);
            if (mast == null) {
                log.error("{}.setWestBoundSignalMast({}); Unable to find Signal Mast",
                        getName(), signalMast);
                return;
            }
        } else {
            westBoundSignalMastNamed = null;
            return;
        }
        if (getType() == PointType.EDGE_CONNECTOR) {
            int dir = getConnect1Dir();
            if (dir == Path.WEST || dir == Path.NORTH || dir == Path.NORTH_WEST) {
                westBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
            } else if (getLinkedPoint() != null) {
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.WEST || linkDir == Path.NORTH || linkDir == Path.NORTH_WEST) {
                    getLinkedPoint().setWestBoundSignalMast(signalMast);
                } else {
                    westBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
                }
            } else {
                westBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
            }
        } else {
            westBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        }
    }

    public void removeBeanReference(jmri.NamedBean nb) {
        if (nb == null) {
            return;
        }
        if (nb instanceof SignalMast) {
            if (nb.equals(getWestBoundSignalMast())) {
                setWestBoundSignalMast(null);
            } else if (nb.equals(getEastBoundSignalMast())) {
                setEastBoundSignalMast(null);
            }
        } else if (nb instanceof Sensor) {
            if (nb.equals(getWestBoundSensor())) {
                setWestBoundSignalMast(null);
            } else if (nb.equals(getEastBoundSensor())) {
                setEastBoundSignalMast(null);
            }
        } else if (nb instanceof jmri.SignalHead) {
            if (nb.equals(getWestBoundSignalHead())) {
                setWestBoundSignal(null);
            }
            if (nb.equals(getEastBoundSignalHead())) {
                setEastBoundSignal(null);
            }

        }
    }

    // initialization instance variables (used when loading a LayoutEditor)
    public String trackSegment1Name = "";
    public String trackSegment2Name = "";

    /**
     * setup a connection to a track
     *
     * @param track the track we want to connect to
     * @return true if successful
     */
    public boolean setTrackConnection(@Nonnull TrackSegment track) {
        return replaceTrackConnection(null, track);
    }

    /**
     * remove a connection to a track
     *
     * @param track the track we want to disconnect from
     * @return true if successful
     */
    public boolean removeTrackConnection(@Nonnull TrackSegment track) {
        return replaceTrackConnection(track, null);
    }

    /**
     * replace old track connection with new track connection
     *
     * @param oldTrack the old track connection
     * @param newTrack the new track connection
     * @return true if successful
     */
    public boolean replaceTrackConnection(@CheckForNull TrackSegment oldTrack, @CheckForNull TrackSegment newTrack) {
        boolean result = false; // assume failure (pessimist!)
        // trying to replace old track with null?
        if (newTrack == null) {
            // (yes) remove old connection
            if (oldTrack != null) {
                result = true;  // assume success (optimist!)
                if (getConnect1() == oldTrack) {
                    positionablePoint.setConnect1(null);        // disconnect getConnect1()
                    reCheckBlockBoundary();
                    removeLinkedPoint();
                    positionablePoint.setConnect1(getConnect2());    // Move getConnect2() to getConnect1()
                    positionablePoint.setConnect2Actual(null);        // disconnect getConnect2()
                } else if (getConnect2() == oldTrack) {
                    positionablePoint.setConnect2Actual(null);
                    reCheckBlockBoundary();
                } else {
                    result = false; // didn't find old connection
                }
            } else {
                result = false; // can't replace null with null
            }
            if (!result) {
                log.error("{}.replaceTrackConnection({}, {}); Attempt to remove non-existant track connection",
                        getName(), (oldTrack == null) ? "null" : oldTrack.getName(), "null");
            }
        } else // already connected to newTrack?
        if ((getConnect1() != newTrack) && (getConnect2() != newTrack)) {
            // (no) find a connection we can connect to
            result = true;  // assume success (optimist!)
            if (getConnect1() == oldTrack) {
                positionablePoint.setConnect1(newTrack);
            } else if ((getType() == PointType.ANCHOR) && (getConnect2() == oldTrack)) {
                positionablePoint.setConnect2Actual(newTrack);
                if (getConnect1().getLayoutBlock() == getConnect2().getLayoutBlock()) {
                    westBoundSignalMastNamed = null;
                    eastBoundSignalMastNamed = null;
                    setWestBoundSensor("");
                    setEastBoundSensor("");
                }
            } else {
                log.error("{}.replaceTrackConnection({}, {}); Attempt to assign more than allowed number of connections",
                        getName(), (oldTrack == null) ? "null" : oldTrack.getName(), newTrack.getName());
                result = false;
            }
        } else {
            log.warn("{}.replaceTrackConnection({}, {}); Already connected",
                    getName(), (oldTrack == null) ? "null" : oldTrack.getName(), newTrack.getName());
            result = false;
        }
        return result;
    }   // replaceTrackConnection

    void removeSML(SignalMast signalMast) {
        if (signalMast == null) {
            return;
        }
        if (jmri.InstanceManager.getDefault(LayoutBlockManager.class
        ).isAdvancedRoutingEnabled() && InstanceManager.getDefault(jmri.SignalMastLogicManager.class
        ).isSignalMastUsed(signalMast)) {
            SignallingGuiTools.removeSignalMastLogic(
                    null, signalMast);
        }
    }

    protected int maxWidth() {
        return 5;
    }

    protected int maxHeight() {
        return 5;
    }
    // cursor location reference for this move (relative to object)
    int xClick = 0;
    int yClick = 0;

    public void mousePressed(MouseEvent e) {
        // remember where we are
        xClick = e.getX();
        yClick = e.getY();
        // if (debug) log.debug("Pressed: "+where(e));
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        // if (debug) log.debug("Release: "+where(e));
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    private JPopupMenu popup = null;

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

        boolean blockBoundary = false;
        boolean addSensorsAndSignalMasksMenuItemsFlag = false;
        JMenuItem jmi = null;
        switch (getType()) {
            case ANCHOR:
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Anchor")) + getName());
                jmi.setEnabled(false);

                LayoutBlock block1 = null;
                if (getConnect1() != null) {
                    block1 = getConnect1().getLayoutBlock();
                }
                LayoutBlock block2 = block1;
                if (getConnect2() != null) {
                    block2 = getConnect2().getLayoutBlock();
                }
                if ((block1 != null) && (block1 == block2)) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameBlock")) + block1.getDisplayName());
                } else if ((block1 != null) && (block2 != null) && (block1 != block2)) {
                    jmi = popup.add(Bundle.getMessage("BlockDivider"));
                    jmi.setEnabled(false);
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", 1)) + block1.getDisplayName());
                    jmi.setEnabled(false);
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", 2)) + block2.getDisplayName());
                    jmi.setEnabled(false);
                    blockBoundary = true;
                }
                jmi.setEnabled(false);
                break;
            case END_BUMPER:
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("EndBumper")) + getName());
                jmi.setEnabled(false);

                LayoutBlock blockEnd = null;
                if (getConnect1() != null) {
                    blockEnd = getConnect1().getLayoutBlock();
                }
                if (blockEnd != null) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BlockID")) + blockEnd.getDisplayName());
                    jmi.setEnabled(false);
                    addSensorsAndSignalMasksMenuItemsFlag = true;
                }
                break;
            case EDGE_CONNECTOR:
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("EdgeConnector")) + getName());
                jmi.setEnabled(false);

                if (getLinkedEditor() != null) {
                    String linkName = getLinkedEditorName() + ":" + getLinkedPointId();
                    jmi = popup.add(Bundle.getMessage("LinkedToX", linkName));
                } else {
                    jmi = popup.add(Bundle.getMessage("EdgeNotLinked"));
                }
                jmi.setEnabled(false);

                block1 = null;
                if (getConnect1() != null) {
                    block1 = getConnect1().getLayoutBlock();
                }
                block2 = block1;
                if (getLinkedPoint() != null) {
                    if (getLinkedPoint().getConnect1() != null) {
                        block2 = getLinkedPoint().getConnect1().getLayoutBlock();
                    }
                }
                if ((block1 != null) && (block1 == block2)) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameBlock")) + block1.getDisplayName());
                } else if ((block1 != null) && (block2 != null) && (block1 != block2)) {
                    jmi = popup.add(Bundle.getMessage("BlockDivider"));
                    jmi.setEnabled(false);
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", 1)) + block1.getDisplayName());
                    jmi.setEnabled(false);
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", 2)) + block2.getDisplayName());
                    jmi.setEnabled(false);
                    blockBoundary = true;
                }
                break;
            default:
                break;
        }

        // if there are any track connections
        if ((getConnect1() != null) || (getConnect2() != null)) {
            JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections")); // there is no pane opening (which is what ... implies)
            if (getConnect1() != null) {
                connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "1") + getConnect1().getName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorFindItems lf = layoutEditor.getFinder();
                        LayoutTrack lt = lf.findObjectByName(getConnect1().getName());
                        // this shouldn't ever be null... however...
                        if (lt != null) {
                            LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                            layoutEditor.setSelectionRect(ltv.getBounds());
                            ltv.showPopup();
                        }
                    }
                });
            }
            if (getConnect2() != null) {
                connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "2") + getConnect2().getName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorFindItems lf = layoutEditor.getFinder();
                        LayoutTrack lt = lf.findObjectByName(getConnect2().getName());
                        // this shouldn't ever be null... however...
                        if (lt != null) {
                            LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                            layoutEditor.setSelectionRect(ltv.getBounds());
                            ltv.showPopup();
                        }
                    }
                });
            }
            popup.add(connectionsMenu);
        }

        if (getConnect1() != null) {
            //
            // decorations menu
            //
            popup.add(new JSeparator(JSeparator.HORIZONTAL));

            JMenu decorationsMenu = new JMenu(Bundle.getMessage("DecorationMenuTitle"));
            decorationsMenu.setToolTipText(Bundle.getMessage("DecorationMenuToolTip"));
            popup.add(decorationsMenu);

            JCheckBoxMenuItem jcbmi;
            TrackSegmentView ctv1 = layoutEditor.getTrackSegmentView(getConnect1());

            if (getType() == PointType.EDGE_CONNECTOR) {
                JMenu arrowsMenu = new JMenu(Bundle.getMessage("ArrowsMenuTitle"));
                decorationsMenu.setToolTipText(Bundle.getMessage("ArrowsMenuToolTip"));
                decorationsMenu.add(arrowsMenu);

                JMenu arrowsCountMenu = new JMenu(Bundle.getMessage("DecorationStyleMenuTitle"));
                arrowsCountMenu.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
                arrowsMenu.add(arrowsCountMenu);

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationNoneMenuItemTitle"));
                arrowsCountMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationNoneMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    if (getConnect1().getConnect1() == positionablePoint) {
                        ctv1.setArrowEndStart(false);
                    }
                    if (getConnect1().getConnect2() == positionablePoint) {
                        ctv1.setArrowEndStop(false);
                    }
                    if (!ctv1.isArrowEndStart() && !ctv1.isArrowEndStop()) {
                        ctv1.setArrowStyle(0);
                    }
                });
                boolean etherEnd = ((getConnect1().getConnect1() == positionablePoint) && ctv1.isArrowEndStart())
                        || ((getConnect1().getConnect2() == positionablePoint) && ctv1.isArrowEndStop());

                jcbmi.setSelected((ctv1.getArrowStyle() == 0) || !etherEnd);

                // configure the arrows
                for (int i = 1; i < NUM_ARROW_TYPES; i++) {
                    jcbmi = loadArrowImageToJCBItem(i, arrowsCountMenu);
                    final int n = i;
                    jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                        if (getConnect1().getConnect1() == positionablePoint) {
                            ctv1.setArrowEndStart(true);
                        }
                        if (getConnect1().getConnect2() == positionablePoint) {
                            ctv1.setArrowEndStop(true);
                        }
                        ctv1.setArrowStyle(n);
                    });
                    jcbmi.setSelected((ctv1.getArrowStyle() == i) && etherEnd);
                }

                JMenu arrowsDirMenu = new JMenu(Bundle.getMessage("ArrowsDirectionMenuTitle"));
                arrowsDirMenu.setToolTipText(Bundle.getMessage("ArrowsDirectionMenuToolTip"));
                arrowsMenu.add(arrowsDirMenu);

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationNoneMenuItemTitle"));
                arrowsDirMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationNoneMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    TrackSegmentView ctv = layoutEditor.getTrackSegmentView(getConnect1());
                    ctv.setArrowDirIn(false);
                    ctv.setArrowDirOut(false);
                });
                jcbmi.setSelected(!ctv1.isArrowDirIn() && !ctv1.isArrowDirOut());

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("ArrowsDirectionInMenuItemTitle"));
                arrowsDirMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("ArrowsDirectionInMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    TrackSegmentView ctv = layoutEditor.getTrackSegmentView(getConnect1());
                    ctv.setArrowDirIn(true);
                    ctv.setArrowDirOut(false);
                });
                jcbmi.setSelected(ctv1.isArrowDirIn() && !ctv1.isArrowDirOut());

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("ArrowsDirectionOutMenuItemTitle"));
                arrowsDirMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("ArrowsDirectionOutMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    TrackSegmentView ctv = layoutEditor.getTrackSegmentView(getConnect1());
                    ctv.setArrowDirOut(true);
                    ctv.setArrowDirIn(false);
                });
                jcbmi.setSelected(!ctv1.isArrowDirIn() && ctv1.isArrowDirOut());

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("ArrowsDirectionBothMenuItemTitle"));
                arrowsDirMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("ArrowsDirectionBothMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    TrackSegmentView ctv = layoutEditor.getTrackSegmentView(getConnect1());
                    ctv.setArrowDirIn(true);
                    ctv.setArrowDirOut(true);
                });
                jcbmi.setSelected(ctv1.isArrowDirIn() && ctv1.isArrowDirOut());

                jmi = arrowsMenu.add(new JMenuItem(Bundle.getMessage("DecorationColorMenuItemTitle")));
                jmi.setToolTipText(Bundle.getMessage("DecorationColorMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    TrackSegmentView ctv = layoutEditor.getTrackSegmentView(getConnect1());
                    Color newColor = JmriColorChooser.showDialog(null, "Choose a color", ctv.getArrowColor());
                    if ((newColor != null) && !newColor.equals(ctv.getArrowColor())) {
                        ctv.setArrowColor(newColor);
                    }
                });
                jmi.setForeground(ctv1.getArrowColor());
                jmi.setBackground(ColorUtil.contrast(ctv1.getArrowColor()));

                jmi = arrowsMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("DecorationLineWidthMenuItemTitle")) + ctv1.getArrowLineWidth()));
                jmi.setToolTipText(Bundle.getMessage("DecorationLineWidthMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    TrackSegmentView ctv = layoutEditor.getTrackSegmentView(getConnect1());
                    //prompt for arrow line width
                    int newValue = QuickPromptUtil.promptForInt(layoutEditor,
                            Bundle.getMessage("DecorationLineWidthMenuItemTitle"),
                            Bundle.getMessage("DecorationLineWidthMenuItemTitle"),
                            ctv.getArrowLineWidth());
                    ctv.setArrowLineWidth(newValue);
                });

                jmi = arrowsMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("DecorationLengthMenuItemTitle")) + ctv1.getArrowLength()));
                jmi.setToolTipText(Bundle.getMessage("DecorationLengthMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    TrackSegmentView ctv = layoutEditor.getTrackSegmentView(getConnect1());
                    //prompt for arrow length
                    int newValue = QuickPromptUtil.promptForInt(layoutEditor,
                            Bundle.getMessage("DecorationLengthMenuItemTitle"),
                            Bundle.getMessage("DecorationLengthMenuItemTitle"),
                            ctv.getArrowLength());
                    ctv.setArrowLength(newValue);
                });

                jmi = arrowsMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("DecorationGapMenuItemTitle")) + ctv1.getArrowGap()));
                jmi.setToolTipText(Bundle.getMessage("DecorationGapMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    TrackSegmentView ctv = layoutEditor.getTrackSegmentView(getConnect1());
                    //prompt for arrow gap
                    int newValue = QuickPromptUtil.promptForInt(layoutEditor,
                            Bundle.getMessage("DecorationGapMenuItemTitle"),
                            Bundle.getMessage("DecorationGapMenuItemTitle"),
                            ctv.getArrowGap());
                    ctv.setArrowGap(newValue);
                });
            } else {

                JMenu endBumperMenu = new JMenu(Bundle.getMessage("EndBumperMenuTitle"));
                decorationsMenu.setToolTipText(Bundle.getMessage("EndBumperMenuToolTip"));
                decorationsMenu.add(endBumperMenu);

                JCheckBoxMenuItem enableCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("EndBumperEnableMenuItemTitle"));
                enableCheckBoxMenuItem.setToolTipText(Bundle.getMessage("EndBumperEnableMenuItemToolTip"));

                endBumperMenu.add(enableCheckBoxMenuItem);
                enableCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> {
                    TrackSegmentView ctv = layoutEditor.getTrackSegmentView(getConnect1());
                    if (getConnect1().getConnect1() == positionablePoint) {
                        ctv.setBumperEndStart(enableCheckBoxMenuItem.isSelected());
                    }
                    if (getConnect1().getConnect2() == positionablePoint) {
                        ctv.setBumperEndStop(enableCheckBoxMenuItem.isSelected());
                    }
                });
                if (getConnect1().getConnect1() == positionablePoint) {
                    enableCheckBoxMenuItem.setSelected(ctv1.isBumperEndStart());
                }
                if (getConnect1().getConnect2() == positionablePoint) {
                    enableCheckBoxMenuItem.setSelected(ctv1.isBumperEndStop());
                }

                jmi = endBumperMenu.add(new JMenuItem(Bundle.getMessage("DecorationColorMenuItemTitle")));
                jmi.setToolTipText(Bundle.getMessage("DecorationColorMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    TrackSegmentView ctv = layoutEditor.getTrackSegmentView(getConnect1());
                    Color newColor = JmriColorChooser.showDialog(null, "Choose a color", ctv.getBumperColor());
                    if ((newColor != null) && !newColor.equals(ctv.getBumperColor())) {
                        ctv.setBumperColor(newColor);
                    }
                });
                jmi.setForeground(ctv1.getBumperColor());
                jmi.setBackground(ColorUtil.contrast(ctv1.getBumperColor()));

                jmi = endBumperMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("DecorationLineWidthMenuItemTitle")) + ctv1.getBumperLineWidth()));
                jmi.setToolTipText(Bundle.getMessage("DecorationLineWidthMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    TrackSegmentView ctv = layoutEditor.getTrackSegmentView(getConnect1());
                    //prompt for width
                    int newValue = QuickPromptUtil.promptForInteger(layoutEditor,
                            Bundle.getMessage("DecorationLineWidthMenuItemTitle"),
                            Bundle.getMessage("DecorationLineWidthMenuItemTitle"),
                            ctv.getBumperLineWidth(), t -> {
                        if (t < 0 || t > TrackSegmentView.MAX_BUMPER_WIDTH) {
                            throw new IllegalArgumentException(
                                    Bundle.getMessage("DecorationLengthMenuItemRange", TrackSegmentView.MAX_BUMPER_WIDTH));
                        }
                        return true;
                    });
                    ctv.setBumperLineWidth(newValue);
                });

                jmi = endBumperMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("DecorationLengthMenuItemTitle")) + ctv1.getBumperLength()));
                jmi.setToolTipText(Bundle.getMessage("DecorationLengthMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    TrackSegmentView ctv = layoutEditor.getTrackSegmentView(getConnect1());
                    //prompt for length
                    int newValue = QuickPromptUtil.promptForInteger(layoutEditor,
                            Bundle.getMessage("DecorationLengthMenuItemTitle"),
                            Bundle.getMessage("DecorationLengthMenuItemTitle"),
                            ctv.getBumperLength(), t -> {
                        if (t < 0 || t > TrackSegmentView.MAX_BUMPER_LENGTH) {
                            throw new IllegalArgumentException(
                                    Bundle.getMessage("DecorationLengthMenuItemRange", TrackSegmentView.MAX_BUMPER_LENGTH));
                        }
                        return true;
                    });
                    ctv.setBumperLength(newValue);
                });
            } // if (getType() == EDGE_CONNECTOR)
        }   // if (getConnect1() != null)

        popup.add(new JSeparator(JSeparator.HORIZONTAL));

        if (getType() == PointType.ANCHOR) {
            if (blockBoundary) {
                jmi = popup.add(new JMenuItem(Bundle.getMessage("CanNotMergeAtBlockBoundary")));
                jmi.setEnabled(false);
            } else if ((getConnect1() != null) && (getConnect2() != null)) {
                jmi = popup.add(new AbstractAction(Bundle.getMessage("MergeAdjacentTracks")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        PositionablePoint pp_this = positionablePoint;
                        // if I'm fully connected...
                        if ((getConnect1() != null) && (getConnect2() != null)) {
                            // who is my connection 2 connected to (that's not me)?
                            LayoutTrack newConnect2 = null;
                            HitPointType newType2 = HitPointType.TRACK;
                            if (getConnect2().getConnect1() == pp_this) {
                                newConnect2 = getConnect2().getConnect2();
                                newType2 = getConnect2().type2;
                            } else if (getConnect2().getConnect2() == pp_this) {
                                newConnect2 = getConnect2().getConnect1();
                                newType2 = getConnect2().type1;
                            } else {
                                //this should never happen however...
                                log.error("Join: wrong getConnect2() error.");
                            }

                            // connect the other connection to my connection 2 to my connection 1
                            if (newConnect2 == null) {
                                // (this should NEVER happen... however...)
                                log.error("Merge: no 'other' connection to getConnect2().");
                            } else {
                                if (newConnect2 instanceof PositionablePoint) {
                                    PositionablePoint pp = (PositionablePoint) newConnect2;
                                    pp.replaceTrackConnection(getConnect2(), getConnect1());
                                } else {
                                    layoutEditor.setLink(newConnect2, newType2, getConnect1(), HitPointType.TRACK);
                                }
                                // connect the track at my getConnect1() to the newConnect2
                                if (getConnect1().getConnect1() == pp_this) {
                                    getConnect1().setNewConnect1(newConnect2, newType2);
                                } else if (getConnect1().getConnect2() == pp_this) {
                                    getConnect1().setNewConnect2(newConnect2, newType2);
                                } else {
                                    // (this should NEVER happen... however...)
                                    log.error("Merge: no connection to connection 1.");
                                }
                            }

                            // remove connection 2 from selection information
                            if (layoutEditor.selectedObject == getConnect2()) {
                                layoutEditor.selectedObject = null;
                            }
                            if (layoutEditor.prevSelectedObject == getConnect2()) {
                                layoutEditor.prevSelectedObject = null;
                            }

                            // remove connection 2 from the layoutEditor's list of layout tracks
                            layoutEditor.removeLayoutTrackAndRedraw(getConnect2());

                            // update affected block
                            LayoutBlock block = getConnect2().getLayoutBlock();
                            if (block != null) {
                                //decrement Block use count
                                block.decrementUse();
                                layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
                                block.updatePaths();
                            }
                            getConnect2().remove();
                            positionablePoint.setConnect2Actual(null);

                            //remove this PositionablePoint from selection information
                            if (layoutEditor.selectedObject == pp_this) {
                                layoutEditor.selectedObject = null;
                            }
                            if (layoutEditor.prevSelectedObject == pp_this) {
                                layoutEditor.prevSelectedObject = null;
                            }
                            clearPossibleSelection();

                            // remove this PositionablePoint and PositionablePointView from the layoutEditor's list of layout tracks
                            layoutEditor.removeLayoutTrackAndRedraw(pp_this);
                            pp_this.remove();
                            dispose();

                            layoutEditor.setDirty();
                            layoutEditor.redrawPanel();
                        } else {
                            // (this should NEVER happen... however...)
                            log.error("Merge: missing connection(s).");
                        }
                    }
                });
            }
        }

        popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
            @Override
            public void actionPerformed(ActionEvent e
            ) {
                if (canRemove() && layoutEditor.removePositionablePoint(positionablePoint)) {
                    // user is serious about removing this point from the panel
                    clearPossibleSelection();
                    remove();
                    dispose();
                }
            }
        });

        JMenu lineType = new JMenu(Bundle.getMessage("ChangeTo"));
        jmi = lineType.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("Anchor")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTypeAnchor();
            }
        }));

        jmi.setSelected(getType() == PointType.ANCHOR);

        // you can't change it to an anchor if it has a 2nd connection
        // TODO: add error dialog if you try?
        if ((getType() == PointType.EDGE_CONNECTOR) && (getConnect2() != null)) {
            jmi.setEnabled(false);
        }

        jmi = lineType.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("EndBumper")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTypeEndBumper();
            }
        }));

        jmi.setSelected(getType() == PointType.END_BUMPER);

        jmi = lineType.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("EdgeConnector")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTypeEdgeConnector();
            }
        }));

        jmi.setSelected(getType() == PointType.EDGE_CONNECTOR);

        popup.add(lineType);

        if (!blockBoundary && getType() == PointType.EDGE_CONNECTOR) {
            popup.add(new JSeparator(JSeparator.HORIZONTAL));
            popup.add(new AbstractAction(Bundle.getMessage("EdgeEditLink")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setLink();
                }
            });
        }

        if (blockBoundary) {
            popup.add(new JSeparator(JSeparator.HORIZONTAL));
            if (getType() == PointType.EDGE_CONNECTOR) {
                popup.add(new AbstractAction(Bundle.getMessage("EdgeEditLink")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setLink();
                    }
                });
                popup.add(new AbstractAction(Bundle.getMessage("SetSignals")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // bring up signals at edge connector tool dialog
                        layoutEditor.getLETools().setSignalsAtBlockBoundaryFromMenu(positionablePoint,
                                getLayoutEditorToolBarPanel().signalIconEditor,
                                getLayoutEditorToolBarPanel().signalFrame);
                    }
                });
            } else {
                AbstractAction ssaa = new AbstractAction(Bundle.getMessage("SetSignals")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // bring up signals at level crossing tool dialog
                        layoutEditor.getLETools().setSignalsAtBlockBoundaryFromMenu(positionablePoint,
                                getLayoutEditorToolBarPanel().signalIconEditor,
                                getLayoutEditorToolBarPanel().signalFrame);
                    }
                };

                JMenu jm = new JMenu(Bundle.getMessage("SignalHeads"));
                if (layoutEditor.getLETools().addBlockBoundarySignalHeadInfoToMenu(positionablePoint, jm)) {
                    jm.add(ssaa);
                    popup.add(jm);
                } else {
                    popup.add(ssaa);
                }
            }
            addSensorsAndSignalMasksMenuItemsFlag = true;
        }
        if (addSensorsAndSignalMasksMenuItemsFlag) {
            popup.add(new AbstractAction(Bundle.getMessage("SetSignalMasts")) {
                @Override
                public void actionPerformed(ActionEvent event) {
                    // bring up signals at block boundary tool dialog
                    layoutEditor.getLETools().setSignalMastsAtBlockBoundaryFromMenu(positionablePoint);
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("SetSensors")) {
                @Override
                public void actionPerformed(ActionEvent event) {
                    // bring up signals at block boundary tool dialog
                    layoutEditor.getLETools().setSensorsAtBlockBoundaryFromMenu(positionablePoint,
                            getLayoutEditorToolBarPanel().sensorIconEditor,
                            getLayoutEditorToolBarPanel().sensorFrame);
                }
            });
        }

        layoutEditor.setShowAlignmentMenu(popup);

        popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());

        return popup;
    }   // showPopup

    /**
     * If an anchor point is selected via a track segment connection, it will be
     * in the track selection list. When the merge or delete finishes, draw can
     * no longer find the object resulting in a Java exception.
     * <p>
     * If the anchor point is in the track selection list, the selection groups
     * are cleared.
     */
    private void clearPossibleSelection() {
        if (layoutEditor.getLayoutTrackSelection().contains(positionablePoint)) {
            layoutEditor.clearSelectionGroups();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemove() {
        List<String> itemList = new ArrayList<>();
        // A has two track segments, EB has one, EC has one plus optional link

        TrackSegment ts1 = getConnect1();
        TrackSegment ts2 = getConnect2();

        if (ts1 != null) {
            itemList.addAll(getSegmentReferences(ts1));
        }
        if (ts2 != null) {
            for (String item : getSegmentReferences(ts2)) {
                // Do not add duplicates
                if (!itemList.contains(item)) {
                    itemList.add(item);
                }
            }
        }

        if (!itemList.isEmpty()) {
            String typeName = "";
            switch (getType()) {
                case ANCHOR:
                    typeName = "Anchor";  // NOI18N
                    break;
                case END_BUMPER:
                    typeName = "EndBumper";  // NOI18N
                    break;
                case EDGE_CONNECTOR:
                    typeName = "EdgeConnector";  // NOI18N
                    break;
                default:
                    typeName = "Unknown type (" + getType() + ")";  // NOI18N
                    break;
            }
            displayRemoveWarningDialog(itemList, typeName);
        }
        return itemList.isEmpty();
    }

    /**
     * Build a list of sensors, signal heads, and signal masts attached to a
     * connection point.
     *
     * @param ts The track segment to be checked.
     * @return a list of bean reference names.
     */
    public ArrayList<String> getSegmentReferences(TrackSegment ts) {
        ArrayList<String> items = new ArrayList<>();

        HitPointType type1 = ts.getType1();
        LayoutTrack conn1 = ts.getConnect1();
        items.addAll(ts.getPointReferences(type1, conn1));

        HitPointType type2 = ts.getType2();
        LayoutTrack conn2 = ts.getConnect2();
        items.addAll(ts.getPointReferences(type2, conn2));

        return items;
    }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove()
     */
    void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
        removeLinkedPoint();
    }

    /**
     * Removes this object from display and persistence
     */
    private void remove() {
        // remove from persistence by flagging inactive
        active = false;
    }

    private boolean active = true;

    /**
     * @return "active" true means that the object is still displayed, and
     *         should be stored.
     */
    protected boolean isActive() {
        return active;
    }

    protected int getConnect1Dir() {
        int result = Path.NONE;

        TrackSegment ts1 = getConnect1();
        if (ts1 != null) {
            Point2D p1;
            if (ts1.getConnect1() == positionablePoint) {
                p1 = layoutEditor.getCoords(ts1.getConnect2(), ts1.getType2());
            } else {
                p1 = layoutEditor.getCoords(ts1.getConnect1(), ts1.getType1());
            }
            result = Path.computeDirection(getCoordsCenter(), p1);
        }
        return result;
    }

    JDialog editLink = null;
    JComboBox<String> linkPointsBox;
    JComboBox<JCBHandle<LayoutEditor>> editorCombo; // Stores with LayoutEditor or "None"

    void setLink() {
        if (getConnect1() == null || getConnect1().getLayoutBlock() == null) {
            log.error("{}.setLink(); Can not set link until we have a connecting track with a block assigned", getName());
            return;
        }
        editLink = new JDialog();
        editLink.setTitle(Bundle.getMessage("EdgeEditLinkFrom", getConnect1().getLayoutBlock().getDisplayName()));

        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());

        JButton done = new JButton(Bundle.getMessage("ButtonDone"));
        done.addActionListener((ActionEvent a) -> updateLink());

        container.add(getLinkPanel(), BorderLayout.NORTH);
        container.add(done, BorderLayout.SOUTH);
        container.revalidate();

        editLink.add(container);

        // make this button the default button (return or enter activates)
        JRootPane rootPane = SwingUtilities.getRootPane(done);
        rootPane.setDefaultButton(done);

        editLink.pack();
        editLink.setModal(false);
        editLink.setVisible(true);
    }

    private ArrayList<PositionablePoint> pointList;

    public JPanel getLinkPanel() {
        editorCombo = new JComboBox<>();
        Set<LayoutEditor> panels = InstanceManager.getDefault(EditorManager.class)
                .getAll(LayoutEditor.class);
        editorCombo.addItem(new JCBHandle<>("None"));
        //if (panels.contains(layoutEditor)) {
        //    panels.remove(layoutEditor);
        //}
        for (LayoutEditor p : panels) {
            JCBHandle<LayoutEditor> h = new JCBHandle<>(p);
            editorCombo.addItem(h);
            if (p == getLinkedEditor()) {
                editorCombo.setSelectedItem(h);
            }
        }

        ActionListener selectPanelListener = (ActionEvent a) -> updatePointBox();

        editorCombo.addActionListener(selectPanelListener);
        JPanel selectorPanel = new JPanel();
        selectorPanel.add(new JLabel(Bundle.getMessage("SelectPanel")));
        selectorPanel.add(editorCombo);
        linkPointsBox = new JComboBox<>();
        updatePointBox();
        selectorPanel.add(new JLabel(Bundle.getMessage("ConnectingTo")));
        selectorPanel.add(linkPointsBox);
        return selectorPanel;
    }

    void updatePointBox() {
        linkPointsBox.removeAllItems();
        pointList = new ArrayList<>();
        if (editorCombo.getSelectedIndex() == 0) {
            linkPointsBox.setEnabled(false);
            return;
        }

        linkPointsBox.setEnabled(true);
        LayoutEditor le = editorCombo.getItemAt(editorCombo.getSelectedIndex()).item();
        for (PositionablePoint p : le.getPositionablePoints()) {
            if (p.getType() == PointType.EDGE_CONNECTOR) {
                if (p.getLinkedPoint() == positionablePoint) {
                    pointList.add(p);
                    linkPointsBox.addItem(p.getName());
                    linkPointsBox.setSelectedItem(p.getName());
                } else if (p.getLinkedPoint() == null) {
                    if (p != positionablePoint) {
                        if (p.getConnect1() != null && p.getConnect1().getLayoutBlock() != null) {
                            if (p.getConnect1().getLayoutBlock() != getConnect1().getLayoutBlock()) {
                                pointList.add(p);
                                linkPointsBox.addItem(p.getName());
                            }
                        }
                    }
                }
            }
        }
        editLink.pack();
    } // updatePointBox

    public void updateLink() {
        if (editorCombo.getSelectedIndex() == 0 || linkPointsBox.getSelectedIndex() == -1) {
            if (getLinkedPoint() != null && getConnect2() != null) {
                String removeremote = null;
                String removelocal = null;
                if (getConnect1Dir() == Path.EAST || getConnect1Dir() == Path.SOUTH) {
                    removeremote = getLinkedPoint().getEastBoundSignal();
                    removelocal = getWestBoundSignal();
                    getLinkedPoint().setEastBoundSignal("");
                } else {
                    removeremote = getLinkedPoint().getWestBoundSignal();
                    removelocal = getEastBoundSignal();
                    getLinkedPoint().setWestBoundSignal("");

                }
                // removelocal and removeremote have been set here.
                if (!removeremote.isEmpty()) {
                    jmri.SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class
                    ).getSignalHead(removeremote);
                    getLinkedEditor().removeSignalHead(sh);
                    jmri.jmrit.blockboss.BlockBossLogic.getStoppedObject(removeremote);

                }
                if (!removelocal.isEmpty()) {
                    jmri.SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class
                    ).getSignalHead(removelocal);
                    layoutEditor.removeSignalHead(sh);
                    jmri.jmrit.blockboss.BlockBossLogic.getStoppedObject(removelocal);
                }
            }
            setLinkedPoint(null);
        } else {
            setLinkedPoint(pointList.get(linkPointsBox.getSelectedIndex()));
        }
        editLink.setVisible(false);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HitPointType findHitPointType(Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        HitPointType result = HitPointType.NONE;  // assume point not on connection
        //note: optimization here: instead of creating rectangles for all the
        // points to check below, we create a rectangle for the test point
        // and test if the points below are in that rectangle instead.
        Rectangle2D r = layoutEditor.layoutEditorControlCircleRectAt(hitPoint);
        Point2D p, minPoint = MathUtil.zeroPoint2D;

        double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        double distance, minDistance = Float.POSITIVE_INFINITY;

        if (!requireUnconnected || (getConnect1() == null)
                || ((getType() == PointType.ANCHOR) && (getConnect2() == null))) {
            // test point control rectangle
            p = getCoordsCenter();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.POS_POINT;
            }
        }
        if ((useRectangles && !r.contains(minPoint))
                || (!useRectangles && (minDistance > circleRadius))) {
            result = HitPointType.NONE;
        }
        return result;
    }   // findHitPointType

    /**
     * return the coordinates for a specified connection type
     *
     * @param connectionType the connection type
     * @return the coordinates for the specified connection type
     */
    @Override
    public Point2D getCoordsForConnectionType(HitPointType connectionType) {
        Point2D result = getCoordsCenter();
        if (connectionType != HitPointType.POS_POINT) {
            log.error("{}.getCoordsForConnectionType({}); Invalid Connection Type",
                    getName(), connectionType); //I18IN
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        LayoutTrack result = null;
        if (connectionType == HitPointType.POS_POINT) {
            result = getConnect1();
            if (null == result) {
                result = getConnect2();
            }
        } else {
            String errString = MessageFormat.format("{0}.getConnection({1}); Invalid Connection Type",
                    getName(), connectionType); //I18IN
            log.error("will throw {}", errString);
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
            String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); unexpected type",
                    getName(), connectionType, (o == null) ? "null" : o.getName(), type); //I18IN
            log.error("will throw {}", errString); //I18IN
            throw new jmri.JmriException(errString);
        }
        if (connectionType != HitPointType.POS_POINT) {
            String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid Connection Type",
                    getName(), connectionType, (o == null) ? "null" : o.getName(), type); //I18IN
            log.error("will throw {}", errString); //I18IN
            throw new jmri.JmriException(errString);
        }
    }

    /**
     * return true if this connection type is disconnected
     *
     * @param connectionType the connection type to test
     * @return true if the connection for this connection type is free
     */
    @Override
    public boolean isDisconnected(HitPointType connectionType) {
        boolean result = false;
        if (connectionType == HitPointType.POS_POINT) {
            result = ((getConnect1() == null) || (getConnect2() == null));
        } else {
            log.error("{}.isDisconnected({}); Invalid Connection Type",
                    getName(), connectionType); //I18IN
        }
        return result;
    }

    /**
     * Draw track decorations.
     * <p>
     * This type of track has none, so this method is empty.
     */
    @Override
    protected void drawDecorations(Graphics2D g2) {
        log.trace("PositionablePointView::drawDecorations");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        //nothing to do here... move along...
        log.trace("PositionablePointView::draw1");
    }   // draw1

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        //nothing to do here... move along...
        log.trace("PositionablePointView::draw2");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        log.trace("PositionablePointView::highlightUnconnected");
        if ((specificType == HitPointType.NONE) || (specificType == HitPointType.POS_POINT)) {
            if ((getConnect1() == null)
                    || ((getType() == PointType.ANCHOR) && (getConnect2() == null))) {
                g2.fill(trackControlCircleAt(getCoordsCenter()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawEditControls(Graphics2D g2) {
        log.trace("PositionablePointView::drawEditControls c1:{} c2:{} {}", getConnect1(), getConnect2(), getType());
        TrackSegment ts1 = getConnect1();
        if (ts1 == null) {
            g2.setColor(Color.red);
        } else {
            TrackSegment ts2 = null;
            if (getType() == PointType.ANCHOR) {
                ts2 = getConnect2();
            } else if (getType() == PointType.EDGE_CONNECTOR) {
                if (getLinkedPoint() != null) {
                    ts2 = getLinkedPoint().getConnect1();
                }
            }
            if ((getType() != PointType.END_BUMPER) && (ts2 == null)) {
                g2.setColor(Color.yellow);
            } else {
                g2.setColor(Color.green);
            }
        }
        log.trace("      at {} in {} draw {}",
                getCoordsCenter(), g2.getColor(),
                layoutEditor.layoutEditorControlRectAt(getCoordsCenter()));

        g2.draw(layoutEditor.layoutEditorControlRectAt(getCoordsCenter()));
    }   // drawEditControls

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        log.trace("PositionablePointView::drawTurnoutControls");
        // PositionablePoints don't have turnout controls...
        // nothing to see here... move along...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reCheckBlockBoundary() {
        if (getType() == PointType.END_BUMPER) {
            return;
        }
        if (getConnect1() == null && getConnect2() == null) {
            //This is no longer a block boundary, therefore will remove signal masts and sensors if present
            if (westBoundSignalMastNamed != null) {
                removeSML(getWestBoundSignalMast());
            }
            if (eastBoundSignalMastNamed != null) {
                removeSML(getEastBoundSignalMast());
            }
            westBoundSignalMastNamed = null;
            eastBoundSignalMastNamed = null;
            setWestBoundSensor("");
            setEastBoundSensor("");
            //TODO: May want to look at a method to remove the assigned mast
            //from the panel and potentially any SignalMast logics generated
        } else if (getConnect1() == null || getConnect2() == null) {
            //could still be in the process of rebuilding the point details
        } else if (getConnect1().getLayoutBlock() == getConnect2().getLayoutBlock()) {
            //We are no longer a block bounardy
            if (westBoundSignalMastNamed != null) {
                removeSML(getWestBoundSignalMast());
            }
            if (eastBoundSignalMastNamed != null) {
                removeSML(getEastBoundSignalMast());
            }
            westBoundSignalMastNamed = null;
            eastBoundSignalMastNamed = null;
            setWestBoundSensor("");
            setEastBoundSensor("");
            //TODO: May want to look at a method to remove the assigned mast
            //from the panel and potentially any SignalMast logics generated
        }
    }   // reCheckBlockBoundary

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        return positionablePoint.getLayoutConnectivity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HitPointType> checkForFreeConnections() {
        List<HitPointType> result = new ArrayList<>();

        if ((getConnect1() == null)
                || ((getType() == PointType.ANCHOR) && (getConnect2() == null))) {
            result.add(HitPointType.POS_POINT);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkForUnAssignedBlocks() {
        // Positionable Points don't have blocks so...
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
        * #2) If this track is not in one of the TrackNameSets for this block
        * #3) add a new set (with this block/track) to
        *     blockNamesToTrackNameSetMap and
        * #4) check all the connections in this
        *     block (by calling the 2nd method below)
        * <p>
        *     Basically, we're maintaining contiguous track sets for each block found
        *     (in blockNamesToTrackNameSetMap)
         */
        //check the 1st connection points block
        TrackSegment ts1 = getConnect1();
        String blk1 = null;
        List<Set<String>> TrackNameSets = null;
        Set<String> TrackNameSet = null;    // assume not found (pessimist!)

        // this should never be null... but just in case...
        if (ts1 != null) {
            blk1 = ts1.getBlockName();
            if (!blk1.isEmpty()) {
                TrackNameSets = blockNamesToTrackNameSetsMap.get(blk1);
                if (TrackNameSets != null) { // (#1)
                    for (Set<String> checkTrackNameSet : TrackNameSets) {
                        if (checkTrackNameSet.contains(getName())) { // (#2)
                            TrackNameSet = checkTrackNameSet;
                            break;
                        }
                    }
                } else {    // (#3)
                    log.debug("*New block (''{}'') trackNameSets", blk1);
                    TrackNameSets = new ArrayList<>();
                    blockNamesToTrackNameSetsMap.put(blk1, TrackNameSets);
                }
                if (TrackNameSet == null) {
                    TrackNameSet = new LinkedHashSet<>();
                    log.debug("*    Add track ''{}'' to trackNameSet for block ''{}''", getName(), blk1);
                    TrackNameSet.add(getName());
                    TrackNameSets.add(TrackNameSet);
                }
                if (getConnect1() != null) { // (#4)
                    getConnect1().collectContiguousTracksNamesInBlockNamed(blk1, TrackNameSet);
                }
            }
        }

        if (getType() == PointType.ANCHOR) {
            //check the 2nd connection points block
            TrackSegment ts2 = getConnect2();
            // this should never be null... but just in case...
            if (ts2 != null) {
                String blk2 = ts2.getBlockName();
                if (!blk2.isEmpty()) {
                    TrackNameSet = null;    // assume not found (pessimist!)
                    TrackNameSets = blockNamesToTrackNameSetsMap.get(blk2);
                    if (TrackNameSets != null) { // (#1)
                        for (Set<String> checkTrackNameSet : TrackNameSets) {
                            if (checkTrackNameSet.contains(getName())) { // (#2)
                                TrackNameSet = checkTrackNameSet;
                                break;
                            }
                        }
                    } else {    // (#3)
                        log.debug("*New block (''{}'') trackNameSets", blk2);
                        TrackNameSets = new ArrayList<>();
                        blockNamesToTrackNameSetsMap.put(blk2, TrackNameSets);
                    }
                    if (TrackNameSet == null) {
                        TrackNameSet = new LinkedHashSet<>();
                        log.debug("*    Add track ''{}'' to TrackNameSet for block ''{}''", getName(), blk2);
                        TrackNameSets.add(TrackNameSet);
                        TrackNameSet.add(getName());
                    }
                    if (getConnect2() != null) { // (#4)
                        getConnect2().collectContiguousTracksNamesInBlockNamed(blk2, TrackNameSet);
                    }
                }
            }
        }
    } // collectContiguousTracksNamesInBlockNamed

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
        if (!TrackNameSet.contains(getName())) {
            TrackSegment ts1 = getConnect1();
            // this should never be null... but just in case...
            if (ts1 != null) {
                String blk1 = ts1.getBlockName();
                // is this the blockName we're looking for?
                if (blk1.equals(blockName)) {
                    // if we are added to the TrackNameSet
                    if (TrackNameSet.add(getName())) {
                        log.debug("*    Add track ''{}''for block ''{}''", getName(), blockName);
                    }
                    // this should never be null... but just in case...
                    if (getConnect1() != null) {
                        getConnect1().collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                    }
                }
            }
            if (getType() == PointType.ANCHOR) {
                TrackSegment ts2 = getConnect2();
                // this should never be null... but just in case...
                if (ts2 != null) {
                    String blk2 = ts2.getBlockName();
                    // is this the blockName we're looking for?
                    if (blk2.equals(blockName)) {
                        // if we are added to the TrackNameSet
                        if (TrackNameSet.add(getName())) {
                            log.debug("*    Add track ''{}''for block ''{}''", getName(), blockName);
                        }
                        // this should never be null... but just in case...
                        if (getConnect2() != null) {
                            // it's time to play... flood your neighbour!
                            getConnect2().collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                        }
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
        // positionable points don't have blocks...
        // nothing to see here, move along...
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionablePointView.class);
}
