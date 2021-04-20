package jmri.jmrit.display.layoutEditor;

import java.text.MessageFormat;
import java.util.*;

import javax.annotation.*;

import jmri.*;
import jmri.jmrit.signalling.SignallingGuiTools;  // temporary


/**
 * PositionablePoint is a Point defining a node in the Track that can be dragged
 * around the inside of the enclosing LayoutEditor panel using a right-drag
 * (drag with meta key).
 * <p>
 * Three types of Positionable Point are supported: Anchor - point on track -
 * two track connections End Bumper - end of track point - one track connection
 * Edge Connector - This is used to link track segments between two different
 * panels
 * <p>
 * Note that a PositionablePoint exists for specifying connectivity and drawing
 * position only. The Track Segments connected to a PositionablePoint may belong
 * to the same block or to different blocks. Since each Track Segment may only
 * belong to one block, a PositionablePoint may function as a Block Boundary.
 * <p>
 * As an Edge Connector, this is a semi-transparent connection to a remote
 * TrackSeqment via another Edge Connector object.
 * <p>
 * Signal names are saved here at a Block Boundary anchor point by the tool Set
 * Signals at Block Boundary. PositionablePoint does nothing with these signal
 * head names; it only serves as a place to store them.
 * <p>
 * Arrows and bumpers are visual presentation aspects handled in the View.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @author Bob Jacobsen Copyright (2) 2014, 2020
 * @author George Warner Copyright (c) 2017-2019
 */
public class PositionablePoint extends LayoutTrack {

    // defined constants
    public enum PointType {
        NONE,
        ANCHOR,        // 1
        END_BUMPER,    // 2
        EDGE_CONNECTOR // 3
    }

    // operational instance variables (not saved between sessions)
    // persistent instances variables (saved between sessions)
    private PointType type = PointType.NONE;
    private TrackSegment connect1 = null;
    private TrackSegment connect2 = null;

    protected NamedBeanHandle<SignalHead> signalEastHeadNamed = null; // signal head for east (south) bound trains
    protected NamedBeanHandle<SignalHead> signalWestHeadNamed = null; // signal head for west (north) bound trains

    private NamedBeanHandle<SignalMast> eastBoundSignalMastNamed = null;
    private NamedBeanHandle<SignalMast> westBoundSignalMastNamed = null;
    /* We use a namedbeanhandle for the sensors, even though we only store the name here,
    this is so that we can keep up with moves and changes of userNames */
    private NamedBeanHandle<Sensor> eastBoundSensorNamed = null;
    private NamedBeanHandle<Sensor> westBoundSensorNamed = null;

    public PositionablePoint(String id, PointType t, LayoutEditor models) {
        super(id, models);

        if ((t == PointType.ANCHOR) || (t == PointType.END_BUMPER) || (t == PointType.EDGE_CONNECTOR)) {
            type = t;
        } else {
            log.error("Illegal type of PositionablePoint - {}", t);
            type = PointType.ANCHOR;
        }
    }

    // this should only be used for debugging...
    @Override
    public String toString() {
        String result = "PositionalablePoint";
        switch (type) {
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
                result = "Unknown type (" + type + ")";
                break;
            }
        }
        return result + " '" + getName() + "'";
    }

    /**
     * Get the point type.
     * @return point type, i.e. ANCHOR, END_BUMPER, EDGE_CONNECTOR
     */
    public PointType getType() {
        return type;
    }

    public void setType(PointType newType) {
        if (type != newType) {
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

            log.debug("temporary - repaint was removed here, needs to be rescoped");
            // models.repaint();

        }
    }

    void setTypeAnchor() { // temporary: make private once decoupled
        setIdent(models.getFinder().uniqueName("A", 1));
        type = PointType.ANCHOR;
        if (connect1 != null) {
            if (connect1.getConnect1() == PositionablePoint.this) {
                log.info("Elided handling of connect1 in setTypeAnchor");
                //connect1.setArrowEndStart(false);   // temporary - is this being done in the view?
                //connect1.setBumperEndStart(false);   // temporary - is this being done in the view?
            }
            if (connect1.getConnect2() == PositionablePoint.this) {
                log.info("Elided handling of connect1 in setTypeAnchor");
                //connect1.setArrowEndStop(false);   // temporary - is this being done in the view?
                //connect1.setBumperEndStop(false);   // temporary - is this being done in the view?
            }
        }
        if (connect2 != null) {
            if (connect2.getConnect1() == PositionablePoint.this) {
                log.info("Elided handling of connect2 in setTypeAnchor");
                //connect2.setArrowEndStart(false);   // temporary - is this being done in the view?
                //connect2.setBumperEndStart(false);   // temporary - is this being done in the view?
            }
            if (connect2.getConnect2() == PositionablePoint.this) {
                log.info("Elided handling of connect2 in setTypeAnchor");
                //connect2.setArrowEndStop(false);   // temporary - is this being done in the view?
                //connect2.setBumperEndStop(false);   // temporary - is this being done in the view?
            }
        }
    }

    void setTypeEndBumper() { // temporary: make private once decoupled
        setIdent(models.getFinder().uniqueName("EB", 1));
        type = PointType.END_BUMPER;
        if (connect1 != null) {
            if (connect1.getConnect1() == PositionablePoint.this) {
                log.info("Elided handling of connect1 in setTypeEndBumper");
                //connect1.setArrowEndStart(false);   // temporary - is this being done in the view?
                //connect1.setBumperEndStart(true);   // temporary - is this being done in the view?
            }
            if (connect1.getConnect2() == PositionablePoint.this) {
                log.info("Elided handling of connect2 in setTypeEndBumper");
                //connect1.setArrowEndStop(false);   // temporary - is this being done in the view?
                //connect1.setBumperEndStop(true);   // temporary - is this being done in the view?
            }
        }
    }

    void setTypeEdgeConnector() { // temporary: make private once decoupled
        setIdent(models.getFinder().uniqueName("EC", 1));
        type = PointType.EDGE_CONNECTOR;
        if (connect1 != null) {
            if (connect1.getConnect1() == PositionablePoint.this) {
                log.info("Elided handling of connect1 in setTypeEdgeConnector");
                //connect1.setBumperEndStart(false);   // temporary - is this being done in the view?
            }
            if (connect1.getConnect2() == PositionablePoint.this) {
                log.info("Elided handling of connect2 in setTypeEdgeConnector");
                //connect1.setBumperEndStop(false);   // temporary - is this being done in the view?
            }
        }
    }

    /**
     * Provide the destination TrackSegment of the 1st connection.
     * @return destination track segment
     */
    public TrackSegment getConnect1() {
        return connect1;
    }

    public void setConnect1(TrackSegment trk) { connect1 = trk; }

    /**
     * Provide the destination TrackSegment of the 2nd connection.
     * When this is an EDGE CONNECTOR, it looks through the linked point (if any)
     * to the far-end track connection.
     * @return destination track segment
     */
    public TrackSegment getConnect2() {
        if (type == PointType.EDGE_CONNECTOR && getLinkedPoint() != null) {
            return getLinkedPoint().getConnect1();
        }
        return connect2;
    }

    /**
     * Provide the destination TrackSegment of the 2nd connection
     * without doing the look-through present in {@link #getConnect2()}
     * @return destination track segment
     */
    public TrackSegment getConnect2Actual() {
        return connect2;
    }

    public void setConnect2Actual(TrackSegment trk) { connect2 = trk; }


    private PositionablePoint linkedPoint;

    public String getLinkedEditorName() {
        if (getLinkedEditor() != null) {
            return getLinkedEditor().getLayoutName();
        }
        return "";
    }

    public PositionablePoint getLinkedPoint() {
        return linkedPoint;
    }

    public String getLinkedPointId() {
        if (linkedPoint != null) {
            return linkedPoint.getId();
        }
        return "";
    }

    public void setLinkedPoint(PositionablePoint p) {
        if (p == linkedPoint) {
            return;
        }
        if (linkedPoint != null) {
            PositionablePoint oldLinkedPoint = linkedPoint;
            linkedPoint = null;
            if (oldLinkedPoint.getLinkedPoint() != null) {
                oldLinkedPoint.setLinkedPoint(null);
            }
            if (oldLinkedPoint.getConnect1() != null) {
                TrackSegment ts = oldLinkedPoint.getConnect1();
                oldLinkedPoint.getLayoutEditor().getLEAuxTools().setBlockConnectivityChanged();
                ts.updateBlockInfo();

                log.info("temporary - repaint was removed here, needs to be rescoped");
                // oldLinkedPoint.getLayoutEditor().repaint();

            }
            if (getConnect1() != null) {
                models.getLEAuxTools().setBlockConnectivityChanged();
                getConnect1().updateBlockInfo();

                log.info("temporary - repaint was removed here, needs to be rescoped");
                // models.repaint();
            }
        }
        linkedPoint = p;
        if (p != null) {
            p.setLinkedPoint(this);
            if (getConnect1() != null) {
                models.getLEAuxTools().setBlockConnectivityChanged();
                getConnect1().updateBlockInfo();

                log.info("temporary - repaint was removed here, needs to be rescoped");
                // models.repaint();
            }
        }
    }

    @CheckReturnValue
    public LayoutEditor getLinkedEditor() {
        if (getLinkedPoint() != null) {
            return getLinkedPoint().getLayoutEditor();
        }
        return null;
    }

    @CheckReturnValue
    protected LayoutEditor getLayoutEditor() {
        return models;
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
    public NamedBeanHandle<SignalMast> getEastBoundSignalMastNamed() {
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
    public NamedBeanHandle<SignalMast> getWestBoundSignalMastNamed() {
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
     * Initialization method The above variables are initialized by
     * PositionablePointXml, then the following method is called after the
     * entire LayoutEditor is loaded to set the specific TrackSegment objects.
     */
    @Override
    public void setObjects(LayoutEditor p) {
        if (type == PointType.EDGE_CONNECTOR) {
            connect1 = p.getFinder().findTrackSegmentByName(trackSegment1Name);
            if (getConnect2() != null && getLinkedEditor() != null) {
                //now that we have a connection we can fire off a change
                TrackSegment ts = getConnect2();
                getLinkedEditor().getLEAuxTools().setBlockConnectivityChanged();
                ts.updateBlockInfo();
            }
        } else {
            connect1 = p.getFinder().findTrackSegmentByName(trackSegment1Name);
            connect2 = p.getFinder().findTrackSegmentByName(trackSegment2Name);
        }
        log.trace("PositionablePoint:setObjects {}: {} and {} {}", trackSegment1Name, connect1, trackSegment1Name, connect1);
    }

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
                if (connect1 == oldTrack) {
                    connect1 = null;        // disconnect connect1
                    reCheckBlockBoundary();
                    removeLinkedPoint();
                    connect1 = connect2;    // Move connect2 to connect1
                    connect2 = null;        // disconnect connect2
                } else if (connect2 == oldTrack) {
                    connect2 = null;
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
        if ((connect1 != newTrack) && (connect2 != newTrack)) {
            // (no) find a connection we can connect to
            result = true;  // assume success (optimist!)
            if (connect1 == oldTrack) {
                connect1 = newTrack;
            } else if ((type == PointType.ANCHOR) && (connect2 == oldTrack)) {
                connect2 = newTrack;
                if (connect1.getLayoutBlock() == connect2.getLayoutBlock()) {
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
    }

    void removeSML(SignalMast signalMast) {
        if (signalMast == null) {
            return;
        }
        if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled() && InstanceManager.getDefault(jmri.SignalMastLogicManager.class).isSignalMastUsed(signalMast)) {
            SignallingGuiTools.removeSignalMastLogic(null, signalMast);
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
            switch (type) {
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
                    typeName = "Unknown type (" + type + ")";  // NOI18N
                    break;
            }
            models.displayRemoveWarning(this, itemList, typeName);
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

    void removeLinkedPoint() {
        if (type == PointType.EDGE_CONNECTOR && getLinkedPoint() != null) {
            if (getConnect2() != null && getLinkedEditor() != null) {
                //as we have removed the point, need to force the update on the remote end.
                LayoutEditor oldLinkedEditor = getLinkedEditor();
                TrackSegment ts = getConnect2();
                getLinkedPoint().setLinkedPoint(null);

                log.info("temporary - repaint was removed here, needs to be rescoped");
                // oldLinkedEditor.repaint();

                oldLinkedEditor.getLEAuxTools().setBlockConnectivityChanged();
                ts.updateBlockInfo();
            }
            linkedPoint = null;
        }
    }

    /**
     * Removes this object from display and persistence
     */
    public void remove() {  // temporary public instead of private for migration
        // remove from persistence by flagging inactive
        active = false;
    }

    private boolean active = true;

    /**
     * "active" means that the object is still displayed, and should be stored.
     * @return true if active
     */
    protected boolean isActive() {
        return active;
    }

    protected int getConnect1Dir() {
        int result = Path.NONE;

        TrackSegment ts1 = getConnect1();
        if (ts1 != null) {
            if (ts1.getConnect1() == this) {
                result = models.computeDirectionFromCenter(this, ts1.getConnect2(), ts1.getType2());
            } else {
                result = models.computeDirectionFromCenter(this, ts1.getConnect1(), ts1.getType1());
            }
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

    @Override
    public boolean isMainline() {
        boolean result = false; // assume failure (pessimist!)
        if (getConnect1() != null) {
            result = getConnect1().isMainline();
        }
        if (getType() == PointType.ANCHOR) {
            if (getConnect2() != null) {
                result |= getConnect2().isMainline();
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reCheckBlockBoundary() {
        if (type == PointType.END_BUMPER) {
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
     //
     // This uses a getCoords() call, and having that at this level
     // has to be temporary
     //
    @Override
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        List<LayoutConnectivity> results = new ArrayList<>();
        LayoutConnectivity lc = null;
        LayoutBlock blk1 = null, blk2 = null;
        TrackSegment ts1 = getConnect1();

        if (getType() == PointType.ANCHOR) {
            TrackSegment ts2 = getConnect2();
            if ((ts1 != null) && (ts2 != null)) {
                blk1 = ts1.getLayoutBlock();
                blk2 = ts2.getLayoutBlock();
                if ((blk1 != null) && (blk2 != null) && (blk1 != blk2)) {
                    // this is a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary (''{}''<->''{}'') found at {}", blk1, blk2, this);
                    lc = new LayoutConnectivity(blk1, blk2);

                    // determine direction from block 1 to block 2
                    lc.setDirection(
                        models.computeDirection(
                            ts1.getConnect1() == this ? ts1.getConnect2()   : ts1.getConnect1(),
                            ts1.getConnect1() == this ? ts1.getType2()      : ts1.getType1(),

                            ts2.getConnect1() == this ? ts2.getConnect2() : ts2.getConnect1(),
                            ts2.getConnect1() == this ? ts2.getType2() : ts2.getType1()
                        )
                    );

                    // save Connections
                    lc.setConnections(ts1, ts2, HitPointType.TRACK, this);
                    results.add(lc);
                }
            }
        } else if (getType() == PointType.EDGE_CONNECTOR) {
            TrackSegment ts2 = null;
            if (getLinkedPoint() != null) {
                ts2 = getLinkedPoint().getConnect1();
            }
            if ((ts1 != null) && (ts2 != null)) {
                blk1 = ts1.getLayoutBlock();
                blk2 = ts2.getLayoutBlock();
                if ((blk1 != null) && (blk2 != null) && (blk1 != blk2)) {
                    // this is a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary (''{}''<->''{}'') found at {}", blk1, blk2, this);
                    lc = new LayoutConnectivity(blk1, blk2);

                    // determine direction from block 1 to block 2
                    int result;

                    if (ts1.getConnect1() == this) {
                        result = models.computeDirectionToCenter(ts1.getConnect2(), ts1.getType2(), this);
                    } else {
                        result = models.computeDirectionToCenter(ts1.getConnect1(), ts1.getType1(), this);
                    }

                    //Need to find a way to compute the direction for this for a split over the panel
                    //In this instance work out the direction of the first track relative to the positionable poin.
                    lc.setDirection(result);
                    // save Connections
                    lc.setConnections(ts1, ts2, HitPointType.TRACK, this);
                    results.add(lc);
                }
            }
        }
        return results;
    }   // getLayoutConnectivity()

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
                if (connect1 != null) { // (#4)
                    connect1.collectContiguousTracksNamesInBlockNamed(blk1, TrackNameSet);
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
                    if (connect2 != null) { // (#4)
                        connect2.collectContiguousTracksNamesInBlockNamed(blk2, TrackNameSet);
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
                    if (connect1 != null) {
                        connect1.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
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
                        if (connect2 != null) {
                            // it's time to play... flood your neighbour!
                            connect2.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionablePoint.class);

}
