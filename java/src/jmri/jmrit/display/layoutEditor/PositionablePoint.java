package jmri.jmrit.display.layoutEditor;

import static java.lang.Float.POSITIVE_INFINITY;
import static jmri.jmrit.display.layoutEditor.LayoutTrack.NONE;
import static jmri.jmrit.display.layoutEditor.LayoutTrack.POS_POINT;
import static jmri.jmrit.display.layoutEditor.LayoutTrack.TRACK;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Path;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.signalling.SignallingGuiTools;
import jmri.util.ColorUtil;
import jmri.util.FileUtil;
import jmri.util.MathUtil;
import jmri.util.QuickPromptUtil;
import jmri.util.swing.JCBHandle;
import jmri.util.swing.JmriColorChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Signal names are saved here at a Block Boundary anchor point by the tool Set
 * Signals at Block Boundary. PositionablePoint does nothing with these signal
 * head names; it only serves as a place to store them.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @author Bob Jacobsen Copyright (2) 2014
 * @author George Warner Copyright (c) 2017-2019
 */
public class PositionablePoint extends LayoutTrack {

    // defined constants
    public static final int ANCHOR = 1;
    public static final int END_BUMPER = 2;
    public static final int EDGE_CONNECTOR = 3;

    // operational instance variables (not saved between sessions)
    // persistent instances variables (saved between sessions)
    private int type = 0;
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

    public PositionablePoint(String id, int t, Point2D c, LayoutEditor layoutEditor) {
        super(id, c, layoutEditor);

        if ((t == ANCHOR) || (t == END_BUMPER) || (t == EDGE_CONNECTOR)) {
            type = t;
        } else {
            log.error("Illegal type of PositionablePoint - " + t);
            type = ANCHOR;
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
     * Accessor methods
     */
    public int getType() {
        return type;
    }

    public void setType(int newType) {
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
            layoutEditor.repaint();
        }
    }

    private void setTypeAnchor() {
        ident = layoutEditor.getFinder().uniqueName("A", 1);
        type = ANCHOR;
        if (connect1 != null) {
            if (connect1.getConnect1() == PositionablePoint.this) {
                connect1.setArrowEndStart(false);
                connect1.setBumperEndStart(false);
            }
            if (connect1.getConnect2() == PositionablePoint.this) {
                connect1.setArrowEndStop(false);
                connect1.setBumperEndStop(false);
            }
        }
        if (connect2 != null) {
            if (connect2.getConnect1() == PositionablePoint.this) {
                connect2.setArrowEndStart(false);
                connect2.setBumperEndStart(false);
            }
            if (connect2.getConnect2() == PositionablePoint.this) {
                connect2.setArrowEndStop(false);
                connect2.setBumperEndStop(false);
            }
        }
    }

    private void setTypeEndBumper() {
        ident = layoutEditor.getFinder().uniqueName("EB", 1);
        type = END_BUMPER;
        if (connect1 != null) {
            if (connect1.getConnect1() == PositionablePoint.this) {
                connect1.setArrowEndStart(false);
                connect1.setBumperEndStart(true);
            }
            if (connect1.getConnect2() == PositionablePoint.this) {
                connect1.setArrowEndStop(false);
                connect1.setBumperEndStop(true);
            }
        }
    }

    private void setTypeEdgeConnector() {
        ident = layoutEditor.getFinder().uniqueName("EC", 1);
        type = EDGE_CONNECTOR;
        if (connect1 != null) {
            if (connect1.getConnect1() == PositionablePoint.this) {
                connect1.setBumperEndStart(false);
            }
            if (connect1.getConnect2() == PositionablePoint.this) {
                connect1.setBumperEndStop(false);
            }
        }
    }

    public TrackSegment getConnect1() {
        return connect1;
    }

    public TrackSegment getConnect2() {
        if (type == EDGE_CONNECTOR && getLinkedPoint() != null) {
            return getLinkedPoint().getConnect1();
        }
        return connect2;
    }

    /**
     * scale this LayoutTrack's coordinates by the x and y factors
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
     * translate this LayoutTrack's coordinates by the x and y factors
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
     * @return the bounds of this positional point
     */
    @Override
    public Rectangle2D getBounds() {
        Point2D c = getCoordsCenter();
        //Note: empty bounds don't draw...
        // so now I'm making them 0.5 bigger in all directions (1 pixel total)
        return new Rectangle2D.Double(c.getX() - 0.5, c.getY() - 0.5, 1.0, 1.0);
    }

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
                oldLinkedPoint.getLayoutEditor().repaint();
            }
            if (getConnect1() != null) {
                layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
                getConnect1().updateBlockInfo();
                layoutEditor.repaint();
            }
        }
        linkedPoint = p;
        if (p != null) {
            p.setLinkedPoint(this);
            if (getConnect1() != null) {
                layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
                getConnect1().updateBlockInfo();
                layoutEditor.repaint();
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
        if (getType() == EDGE_CONNECTOR) {
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
        if (getType() == EDGE_CONNECTOR) {
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
        if (getType() == EDGE_CONNECTOR) {
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
        if (getType() == EDGE_CONNECTOR) {
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
        if (getType() == EDGE_CONNECTOR) {
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
                log.error("Unable to find Signal Mast " + signalMast);
                return;
            }
        } else {
            eastBoundSignalMastNamed = null;
            return;
        }
        if (getType() == EDGE_CONNECTOR) {
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
        if (getType() == EDGE_CONNECTOR) {
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
                log.error("Unable to find Signal Mast " + signalMast);
                return;
            }
        } else {
            westBoundSignalMastNamed = null;
            return;
        }
        if (getType() == EDGE_CONNECTOR) {
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
        if (type == EDGE_CONNECTOR) {
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
                log.error("Attempt to remove non-existant track connection: {}", oldTrack);
            }
        } else // already connected to newTrack?
        if ((connect1 != newTrack) && (connect2 != newTrack)) {
            // (no) find a connection we can connect to
            result = true;  // assume success (optimist!)
            if (connect1 == oldTrack) {
                connect1 = newTrack;
            } else if ((type == ANCHOR) && (connect2 == oldTrack)) {
                connect2 = newTrack;
                if (connect1.getLayoutBlock() == connect2.getLayoutBlock()) {
                    westBoundSignalMastNamed = null;
                    eastBoundSignalMastNamed = null;
                    setWestBoundSensor("");
                    setEastBoundSensor("");
                }
            } else {
                log.error("Attempt to assign more than allowed number of connections");
                result = false;
            }
        } else {
            log.error("Already connected to {}", newTrack.getName());
            result = false;
        }
        return result;
    }   // replaceTrackConnection

    void removeSML(SignalMast signalMast) {
        if (signalMast == null) {
            return;
        }
        if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled() && InstanceManager.getDefault(jmri.SignalMastLogicManager.class).isSignalMastUsed(signalMast)) {
            SignallingGuiTools.removeSignalMastLogic(null, signalMast);
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
                if (connect1 != null) {
                    block1 = connect1.getLayoutBlock();
                }
                LayoutBlock block2 = block1;
                if (connect2 != null) {
                    block2 = connect2.getLayoutBlock();
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
                if (connect1 != null) {
                    blockEnd = connect1.getLayoutBlock();
                }
                if (blockEnd != null) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BlockID")) + blockEnd.getDisplayName());
                    jmi.setEnabled(false);
                }
                addSensorsAndSignalMasksMenuItemsFlag = true;
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
        if ((connect1 != null) || (connect2 != null)) {
            JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections")); // there is no pane opening (which is what ... implies)
            if (connect1 != null) {
                connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "1") + connect1.getName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorFindItems lf = layoutEditor.getFinder();
                        LayoutTrack lt = lf.findObjectByName(connect1.getName());
                        // this shouldn't ever be null... however...
                        if (lt != null) {
                            layoutEditor.setSelectionRect(lt.getBounds());
                            lt.showPopup();
                        }
                    }
                });
            }
            if (connect2 != null) {
                connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "2") + connect2.getName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorFindItems lf = layoutEditor.getFinder();
                        LayoutTrack lt = lf.findObjectByName(connect2.getName());
                        // this shouldn't ever be null... however...
                        if (lt != null) {
                            layoutEditor.setSelectionRect(lt.getBounds());
                            lt.showPopup();
                        }
                    }
                });
            }
            popup.add(connectionsMenu);
        }

        if (connect1 != null && (type == EDGE_CONNECTOR || type == END_BUMPER)) {
            //
            // decorations menu
            //
            popup.add(new JSeparator(JSeparator.HORIZONTAL));

            JMenu decorationsMenu = new JMenu(Bundle.getMessage("DecorationMenuTitle"));
            decorationsMenu.setToolTipText(Bundle.getMessage("DecorationMenuToolTip"));
            popup.add(decorationsMenu);

            JCheckBoxMenuItem jcbmi;
            if (type == EDGE_CONNECTOR) {
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
                    if (connect1.getConnect1() == this) {
                        connect1.setArrowEndStart(false);
                    }
                    if (connect1.getConnect2() == this) {
                        connect1.setArrowEndStop(false);
                    }
                    if (!connect1.isArrowEndStart() && !connect1.isArrowEndStop()) {
                        connect1.setArrowStyle(0);
                    }
                });
                boolean etherEnd = ((connect1.getConnect1() == this) && connect1.isArrowEndStart())
                        || ((connect1.getConnect2() == this) && connect1.isArrowEndStop());

                jcbmi.setSelected((connect1.getArrowStyle() == 0) || !etherEnd);

                ImageIcon imageIcon = new ImageIcon(FileUtil.findURL("program:resources/icons/decorations/ArrowStyle1.png"));
                jcbmi = new JCheckBoxMenuItem(imageIcon);
                arrowsCountMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    if (connect1.getConnect1() == this) {
                        connect1.setArrowEndStart(true);
                    }
                    if (connect1.getConnect2() == this) {
                        connect1.setArrowEndStop(true);
                    }
                    connect1.setArrowStyle(1);
                });
                jcbmi.setSelected((connect1.getArrowStyle() == 1) && etherEnd);

                imageIcon = new ImageIcon(FileUtil.findURL("program:resources/icons/decorations/ArrowStyle2.png"));
                jcbmi = new JCheckBoxMenuItem(imageIcon);
                arrowsCountMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    if (connect1.getConnect1() == this) {
                        connect1.setArrowEndStart(true);
                    }
                    if (connect1.getConnect2() == this) {
                        connect1.setArrowEndStop(true);
                    }
                    connect1.setArrowStyle(2);
                });
                jcbmi.setSelected((connect1.getArrowStyle() == 2) && etherEnd);

                imageIcon = new ImageIcon(FileUtil.findURL("program:resources/icons/decorations/ArrowStyle3.png"));
                jcbmi = new JCheckBoxMenuItem(imageIcon);
                arrowsCountMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    if (connect1.getConnect1() == this) {
                        connect1.setArrowEndStart(true);
                    }
                    if (connect1.getConnect2() == this) {
                        connect1.setArrowEndStop(true);
                    }
                    connect1.setArrowStyle(3);
                });
                jcbmi.setSelected((connect1.getArrowStyle() == 3) && etherEnd);

                imageIcon = new ImageIcon(FileUtil.findURL("program:resources/icons/decorations/ArrowStyle4.png"));
                jcbmi = new JCheckBoxMenuItem(imageIcon);
                arrowsCountMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    if (connect1.getConnect1() == this) {
                        connect1.setArrowEndStart(true);
                    }
                    if (connect1.getConnect2() == this) {
                        connect1.setArrowEndStop(true);
                    }
                    connect1.setArrowStyle(4);
                });
                jcbmi.setSelected((connect1.getArrowStyle() == 4) && etherEnd);

                imageIcon = new ImageIcon(FileUtil.findURL("program:resources/icons/decorations/ArrowStyle5.png"));
                jcbmi = new JCheckBoxMenuItem(imageIcon);
                arrowsCountMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    if (connect1.getConnect1() == this) {
                        connect1.setArrowEndStart(true);
                    }
                    if (connect1.getConnect2() == this) {
                        connect1.setArrowEndStop(true);
                    }
                    connect1.setArrowStyle(5);
                });
                jcbmi.setSelected((connect1.getArrowStyle() == 5) && etherEnd);

                JMenu arrowsDirMenu = new JMenu(Bundle.getMessage("ArrowsDirectionMenuTitle"));
                arrowsDirMenu.setToolTipText(Bundle.getMessage("ArrowsDirectionMenuToolTip"));
                arrowsMenu.add(arrowsDirMenu);

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationNoneMenuItemTitle"));
                arrowsDirMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationNoneMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    connect1.setArrowDirIn(false);
                    connect1.setArrowDirOut(false);
                });
                jcbmi.setSelected(!connect1.isArrowDirIn() && !connect1.isArrowDirOut());

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("ArrowsDirectionInMenuItemTitle"));
                arrowsDirMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("ArrowsDirectionInMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    connect1.setArrowDirIn(true);
                    connect1.setArrowDirOut(false);
                });
                jcbmi.setSelected(connect1.isArrowDirIn() && !connect1.isArrowDirOut());

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("ArrowsDirectionOutMenuItemTitle"));
                arrowsDirMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("ArrowsDirectionOutMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    connect1.setArrowDirOut(true);
                    connect1.setArrowDirIn(false);
                });
                jcbmi.setSelected(!connect1.isArrowDirIn() && connect1.isArrowDirOut());

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("ArrowsDirectionBothMenuItemTitle"));
                arrowsDirMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("ArrowsDirectionBothMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    connect1.setArrowDirIn(true);
                    connect1.setArrowDirOut(true);
                });
                jcbmi.setSelected(connect1.isArrowDirIn() && connect1.isArrowDirOut());

                jmi = arrowsMenu.add(new JMenuItem(Bundle.getMessage("DecorationColorMenuItemTitle")));
                jmi.setToolTipText(Bundle.getMessage("DecorationColorMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    Color newColor = JmriColorChooser.showDialog(null, "Choose a color", connect1.getArrowColor());
                    if ((newColor != null) && !newColor.equals(connect1.getArrowColor())) {
                        connect1.setArrowColor(newColor);
                    }
                });
                jmi.setForeground(connect1.getArrowColor());
                jmi.setBackground(ColorUtil.contrast(connect1.getArrowColor()));

                jmi = arrowsMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("DecorationLineWidthMenuItemTitle")) + connect1.getArrowLineWidth()));
                jmi.setToolTipText(Bundle.getMessage("DecorationLineWidthMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    //prompt for arrow line width
                    int newValue = QuickPromptUtil.promptForInt(layoutEditor,
                            Bundle.getMessage("DecorationLineWidthMenuItemTitle"),
                            Bundle.getMessage("DecorationLineWidthMenuItemTitle"),
                            connect1.getArrowLineWidth());
                    connect1.setArrowLineWidth(newValue);
                });

                jmi = arrowsMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("DecorationLengthMenuItemTitle")) + connect1.getArrowLength()));
                jmi.setToolTipText(Bundle.getMessage("DecorationLengthMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    //prompt for arrow length
                    int newValue = QuickPromptUtil.promptForInt(layoutEditor,
                            Bundle.getMessage("DecorationLengthMenuItemTitle"),
                            Bundle.getMessage("DecorationLengthMenuItemTitle"),
                            connect1.getArrowLength());
                    connect1.setArrowLength(newValue);
                });

                jmi = arrowsMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("DecorationGapMenuItemTitle")) + connect1.getArrowGap()));
                jmi.setToolTipText(Bundle.getMessage("DecorationGapMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    //prompt for arrow gap
                    int newValue = QuickPromptUtil.promptForInt(layoutEditor,
                            Bundle.getMessage("DecorationGapMenuItemTitle"),
                            Bundle.getMessage("DecorationGapMenuItemTitle"),
                            connect1.getArrowGap());
                    connect1.setArrowGap(newValue);
                });
            } // if (type == EDGE_CONNECTOR)

            if (type == END_BUMPER) {
                JMenu endBumperMenu = new JMenu(Bundle.getMessage("EndBumperMenuTitle"));
                decorationsMenu.setToolTipText(Bundle.getMessage("EndBumperMenuToolTip"));
                decorationsMenu.add(endBumperMenu);

                JCheckBoxMenuItem enableCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("EndBumperEnableMenuItemTitle"));
                enableCheckBoxMenuItem.setToolTipText(Bundle.getMessage("EndBumperEnableMenuItemToolTip"));

                endBumperMenu.add(enableCheckBoxMenuItem);
                enableCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> {
                    if (connect1.getConnect1() == this) {
                        connect1.setBumperEndStart(enableCheckBoxMenuItem.isSelected());
                    }
                    if (connect1.getConnect2() == this) {
                        connect1.setBumperEndStop(enableCheckBoxMenuItem.isSelected());
                    }
                });
                if (connect1.getConnect1() == this) {
                    enableCheckBoxMenuItem.setSelected(connect1.isBumperEndStart());
                }
                if (connect1.getConnect2() == this) {
                    enableCheckBoxMenuItem.setSelected(connect1.isBumperEndStop());
                }

                jmi = endBumperMenu.add(new JMenuItem(Bundle.getMessage("DecorationColorMenuItemTitle")));
                jmi.setToolTipText(Bundle.getMessage("DecorationColorMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    Color newColor = JmriColorChooser.showDialog(null, "Choose a color", connect1.getBumperColor());
                    if ((newColor != null) && !newColor.equals(connect1.getBumperColor())) {
                        connect1.setBumperColor(newColor);
                    }
                });
                jmi.setForeground(connect1.getBumperColor());
                jmi.setBackground(ColorUtil.contrast(connect1.getBumperColor()));

                jmi = endBumperMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("DecorationLineWidthMenuItemTitle")) + connect1.getBumperLineWidth()));
                jmi.setToolTipText(Bundle.getMessage("DecorationLineWidthMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    //prompt for width
                    int newValue = QuickPromptUtil.promptForInteger(layoutEditor,
                            Bundle.getMessage("DecorationLineWidthMenuItemTitle"),
                            Bundle.getMessage("DecorationLineWidthMenuItemTitle"),
                            connect1.getBumperLineWidth(), new Predicate<Integer>() {
                        @Override
                        public boolean test(Integer t) {
                            if (t < 0 || t > TrackSegment.MAX_BUMPER_WIDTH) {
                                throw new IllegalArgumentException(
                                        Bundle.getMessage("DecorationLengthMenuItemRange", TrackSegment.MAX_BUMPER_WIDTH));
                            }
                            return true;
                        }
                    });
                    connect1.setBumperLineWidth(newValue);
                });

                jmi = endBumperMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                        Bundle.getMessage("DecorationLengthMenuItemTitle")) + connect1.getBumperLength()));
                jmi.setToolTipText(Bundle.getMessage("DecorationLengthMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    //prompt for length
                    int newValue = QuickPromptUtil.promptForInteger(layoutEditor,
                            Bundle.getMessage("DecorationLengthMenuItemTitle"),
                            Bundle.getMessage("DecorationLengthMenuItemTitle"),
                            connect1.getBumperLength(), new Predicate<Integer>() {
                        @Override
                        public boolean test(Integer t) {
                            if (t < 0 || t > TrackSegment.MAX_BUMPER_LENGTH) {
                                throw new IllegalArgumentException(
                                        Bundle.getMessage("DecorationLengthMenuItemRange", TrackSegment.MAX_BUMPER_LENGTH));
                            }
                            return true;
                        }
                    });
                    connect1.setBumperLength(newValue);
                });
            }
        }   // if ((type == EDGE_CONNECTOR) || (type == END_BUMPER))

        popup.add(new JSeparator(JSeparator.HORIZONTAL));

        if (getType() == ANCHOR) {
            if (blockBoundary) {
                jmi = popup.add(new JMenuItem(Bundle.getMessage("CanNotMergeAtBlockBoundary")));
                jmi.setEnabled(false);
            } else if ((connect1 != null) && (connect2 != null)) {
                jmi = popup.add(new AbstractAction(Bundle.getMessage("MergeAdjacentTracks")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        PositionablePoint pp_this = PositionablePoint.this;
                        // if I'm fully connected...
                        if ((connect1 != null) && (connect2 != null)) {
                            // who is my connect2 connected to (that's not me)?
                            LayoutTrack newConnect2 = null;
                            int newType2 = TRACK;
                            if (connect2.connect1 == pp_this) {
                                newConnect2 = connect2.connect2;
                                newType2 = connect2.type2;
                            } else if (connect2.connect2 == pp_this) {
                                newConnect2 = connect2.connect1;
                                newType2 = connect2.type1;
                            } else {
                                //this should never happen however...
                                log.error("Join: wrong connect2 error.");
                            }

                            // connect the other connection to my connect2 to my connect1
                            if (newConnect2 == null) {
                                // (this should NEVER happen... however...)
                                log.error("Merge: no 'other' connection to connect2.");
                            } else {
                                if (newConnect2 instanceof PositionablePoint) {
                                    PositionablePoint pp = (PositionablePoint) newConnect2;
                                    pp.replaceTrackConnection(connect2, connect1);
                                } else {
                                    layoutEditor.setLink(newConnect2, newType2, connect1, TRACK);
                                }
                                // connect the track at my connect1 to the newConnect2
                                if (connect1.getConnect1() == pp_this) {
                                    connect1.setNewConnect1(newConnect2, newType2);
                                } else if (connect1.getConnect2() == pp_this) {
                                    connect1.setNewConnect2(newConnect2, newType2);
                                } else {
                                    // (this should NEVER happen... however...)
                                    log.error("Merge: no connection to connect1.");
                                }
                            }

                            // remove connect2 from selection information
                            if (layoutEditor.selectedObject == connect2) {
                                layoutEditor.selectedObject = null;
                            }
                            if (layoutEditor.prevSelectedObject == connect2) {
                                layoutEditor.prevSelectedObject = null;
                            }

                            // remove connect2 from the layoutEditor's list of layout tracks
                            if (layoutEditor.getLayoutTracks().contains(connect2)) {
                                layoutEditor.getLayoutTracks().remove(connect2);
                                layoutEditor.setDirty();
                                layoutEditor.redrawPanel();
                            }

                            //update affected block
                            LayoutBlock block = connect2.getLayoutBlock();
                            if (block != null) {
                                //decrement Block use count
                                block.decrementUse();
                                layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
                                block.updatePaths();
                            }
                            connect2.remove();
                            connect2.dispose();
                            connect2 = null;

                            //remove pp_this from selection information
                            if (layoutEditor.selectedObject == pp_this) {
                                layoutEditor.selectedObject = null;
                            }
                            if (layoutEditor.prevSelectedObject == pp_this) {
                                layoutEditor.prevSelectedObject = null;
                            }

                            // remove pp_this from the layoutEditor's list of layout tracks
                            if (layoutEditor.getLayoutTracks().contains(pp_this)) {
                                layoutEditor.getLayoutTracks().remove(pp_this);
                                layoutEditor.setDirty();
                                layoutEditor.redrawPanel();
                            }
                            pp_this.remove();
                            pp_this.dispose();

                            layoutEditor.setDirty();
                            layoutEditor.redrawPanel();
                        } else {
                            // (this should NEVER happen... however...)
                            log.error("Merge: missing connection(s).");
                        }   // if ((connect1 != null) && (connect2 != null))
                    }   // actionPerformed
                }); // jmi = popup.add(new AbstractAction(...) {
            }   // if (blockBoundary) {} else if ((connect1 != null) && (connect2 != null))
        }   // if (getType() == ANCHOR)

        popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
            @Override
            public void actionPerformed(ActionEvent e
            ) {
                if (canRemove() && layoutEditor.removePositionablePoint(PositionablePoint.this)) {
                    // user is serious about removing this point from the panel
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

        jmi.setSelected(getType() == ANCHOR);

        // you can't change it to an anchor if it has a 2nd connection
        // TODO: add error dialog if you try?
        if ((getType() == EDGE_CONNECTOR) && (getConnect2() != null)) {
            jmi.setEnabled(false);
        }

        jmi = lineType.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("EndBumper")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTypeEndBumper();
            }
        }));

        jmi.setSelected(getType() == END_BUMPER);

        jmi = lineType.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("EdgeConnector")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTypeEdgeConnector();
            }
        }));

        jmi.setSelected(getType() == EDGE_CONNECTOR);

        popup.add(lineType);

        if (!blockBoundary && getType() == EDGE_CONNECTOR) {
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
            if (getType() == EDGE_CONNECTOR) {
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
                        layoutEditor.getLETools().setSignalsAtBlockBoundaryFromMenu(PositionablePoint.this,
                                layoutEditor.signalIconEditor, layoutEditor.signalFrame);
                    }
                });
            } else {
                AbstractAction ssaa = new AbstractAction(Bundle.getMessage("SetSignals")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // bring up signals at level crossing tool dialog
                        layoutEditor.getLETools().setSignalsAtBlockBoundaryFromMenu(PositionablePoint.this,
                                layoutEditor.signalIconEditor, layoutEditor.signalFrame);
                    }
                };

                JMenu jm = new JMenu(Bundle.getMessage("SignalHeads"));
                if (layoutEditor.getLETools().addBlockBoundarySignalHeadInfoToMenu(PositionablePoint.this, jm)) {
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
                    layoutEditor.getLETools().setSignalMastsAtBlockBoundaryFromMenu(PositionablePoint.this);
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("SetSensors")) {
                @Override
                public void actionPerformed(ActionEvent event) {
                    // bring up signals at block boundary tool dialog
                    layoutEditor.getLETools().setSensorsAtBlockBoundaryFromMenu(PositionablePoint.this,
                            layoutEditor.sensorIconEditor, layoutEditor.sensorFrame);
                }
            });
        }

        layoutEditor.setShowAlignmentMenu(popup);

        popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());

        return popup;
    }   // showPopup

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

        int type1 = ts.getType1();
        LayoutTrack conn1 = ts.getConnect1();
        items.addAll(ts.getPointReferences(type1, conn1));

        int type2 = ts.getType2();
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

    void removeLinkedPoint() {
        if (type == EDGE_CONNECTOR && getLinkedPoint() != null) {
            if (getConnect2() != null && getLinkedEditor() != null) {
                //as we have removed the point, need to force the update on the remote end.
                LayoutEditor oldLinkedEditor = getLinkedEditor();
                TrackSegment ts = getConnect2();
                getLinkedPoint().setLinkedPoint(null);
                oldLinkedEditor.repaint();
                oldLinkedEditor.getLEAuxTools().setBlockConnectivityChanged();
                ts.updateBlockInfo();
            }
            linkedPoint = null;
        }
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
     * "active" means that the object is still displayed, and should be stored.
     */
    protected boolean isActive() {
        return active;
    }

    protected int getConnect1Dir() {
        int result = Path.NONE;

        TrackSegment ts1 = getConnect1();
        if (ts1 != null) {
            Point2D p1;
            if (ts1.getConnect1() == this) {
                p1 = LayoutEditor.getCoords(ts1.getConnect2(), ts1.getType2());
            } else {
                p1 = LayoutEditor.getCoords(ts1.getConnect1(), ts1.getType1());
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
            log.error("Can not set link until we have a connecting track with a block assigned");
            return;
        }
        editLink = new JDialog();
        editLink.setTitle(Bundle.getMessage("EdgeEditLinkFrom", getConnect1().getLayoutBlock().getDisplayName()));

        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());

        JButton done = new JButton(Bundle.getMessage("ButtonDone"));
        done.addActionListener((ActionEvent a) -> {
            updateLink();
        });

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
        editorCombo = new JComboBox<JCBHandle<LayoutEditor>>();
        ArrayList<LayoutEditor> panels
                = InstanceManager.getDefault(PanelMenu.class).getLayoutEditorPanelList();
        editorCombo.addItem(new JCBHandle<LayoutEditor>("None"));
        if (panels.contains(layoutEditor)) {
            panels.remove(layoutEditor);
        }
        for (LayoutEditor p : panels) {
            JCBHandle<LayoutEditor> h = new JCBHandle<LayoutEditor>(p);
            editorCombo.addItem(h);
            if (p == getLinkedEditor()) {
                editorCombo.setSelectedItem(h);
            }
        }

        ActionListener selectPanelListener = (ActionEvent a) -> {
            updatePointBox();
        };

        editorCombo.addActionListener(selectPanelListener);
        JPanel selectorPanel = new JPanel();
        selectorPanel.add(new JLabel(Bundle.getMessage("SelectPanel")));
        selectorPanel.add(editorCombo);
        linkPointsBox = new JComboBox<String>();
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
            if (p.getType() == EDGE_CONNECTOR) {
                if (p.getLinkedPoint() == this) {
                    pointList.add(p);
                    linkPointsBox.addItem(p.getName());
                    linkPointsBox.setSelectedItem(p.getName());
                } else if (p.getLinkedPoint() == null) {
                    if (p.getConnect1() != null && p.getConnect1().getLayoutBlock() != null) {
                        if (p.getConnect1().getLayoutBlock() != getConnect1().getLayoutBlock()) {
                            pointList.add(p);
                            linkPointsBox.addItem(p.getConnect1().getLayoutBlock().getDisplayName());
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
    protected int findHitPointType(Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        int result = NONE;  // assume point not on connection
        //note: optimization here: instead of creating rectangles for all the
        // points to check below, we create a rectangle for the test point
        // and test if the points below are in that rectangle instead.
        Rectangle2D r = layoutEditor.trackControlCircleRectAt(hitPoint);
        Point2D p, minPoint = MathUtil.zeroPoint2D;

        double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        double distance, minDistance = POSITIVE_INFINITY;

        if (!requireUnconnected || (getConnect1() == null)
                || ((getType() == ANCHOR) && (getConnect2() == null))) {
            // test point control rectangle
            p = getCoordsCenter();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = POS_POINT;
            }
        }
        if ((useRectangles && !r.contains(minPoint))
                || (!useRectangles && (minDistance > circleRadius))) {
            result = NONE;
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
    public Point2D getCoordsForConnectionType(int connectionType) {
        Point2D result = getCoordsCenter();
        if (connectionType != POS_POINT) {
            log.error("Invalid connection type " + connectionType); //I18IN
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(int connectionType) throws jmri.JmriException {
        LayoutTrack result = null;
        if (connectionType == POS_POINT) {
            result = getConnect1();
            if (null == result) {
                result = getConnect2();
            }
        } else {
            log.error("Invalid connection type " + connectionType); //I18IN
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
            log.error("unexpected type of connection to positionable point - " + type);
            throw new jmri.JmriException("unexpected type of connection to positionable point - " + type);
        }
        if (connectionType != POS_POINT) {
            log.error("Invalid Connection Type " + connectionType); //I18IN
            throw new jmri.JmriException("Invalid Connection Type " + connectionType);
        }
    }

    /**
     * return true if this connection type is disconnected
     *
     * @param connectionType the connection type to test
     * @return true if the connection for this connection type is free
     */
    @Override
    public boolean isDisconnected(int connectionType) {
        boolean result = false;
        if (connectionType == POS_POINT) {
            result = ((getConnect1() == null) || (getConnect2() == null));
        } else {
            log.error("Invalid connection type " + connectionType); //I18IN
        }
        return result;
    }

    @Override
    public boolean isMainline() {
        boolean result = false; // assume failure (pessimist!)
        if (getConnect1() != null) {
            result = getConnect1().isMainline();
        }
        if (getType() == ANCHOR) {
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
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        //nothing to do here... move along...
    }   // draw1

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        //nothing to do here... move along...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, int specificType) {
        if ((specificType == NONE) || (specificType == POS_POINT)) {
            if ((getConnect1() == null)
                    || ((getType() == ANCHOR) && (getConnect2() == null))) {
                g2.fill(layoutEditor.trackControlCircleAt(getCoordsCenter()));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawEditControls(Graphics2D g2) {
        TrackSegment ts1 = getConnect1();
        if (ts1 == null) {
            g2.setColor(Color.red);
        } else {
            TrackSegment ts2 = null;
            if (getType() == ANCHOR) {
                ts2 = getConnect2();
            } else if (getType() == EDGE_CONNECTOR) {
                if (getLinkedPoint() != null) {
                    ts2 = getLinkedPoint().getConnect1();
                }
            }
            if ((getType() != END_BUMPER) && (ts2 == null)) {
                g2.setColor(Color.yellow);
            } else {
                g2.setColor(Color.green);
            }
        }
        g2.draw(layoutEditor.trackEditControlRectAt(getCoordsCenter()));
    }   // drawEditControls

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        // PositionablePoints don't have turnout controls...
        // nothing to see here... move along...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reCheckBlockBoundary() {
        if (type == END_BUMPER) {
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
            return;
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
        List<LayoutConnectivity> results = new ArrayList<>();
        LayoutConnectivity lc = null;
        LayoutBlock blk1 = null, blk2 = null;
        TrackSegment ts1 = getConnect1();
        Point2D p1, p2;

        if (getType() == ANCHOR) {
            TrackSegment ts2 = getConnect2();
            if ((ts1 != null) && (ts2 != null)) {
                blk1 = ts1.getLayoutBlock();
                blk2 = ts2.getLayoutBlock();
                if ((blk1 != null) && (blk2 != null) && (blk1 != blk2)) {
                    // this is a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary ('{}'<->'{}') found at {}", blk1, blk2, this);
                    lc = new LayoutConnectivity(blk1, blk2);
                    // determine direction from block 1 to block 2
                    if (ts1.getConnect1() == this) {
                        p1 = LayoutEditor.getCoords(ts1.getConnect2(), ts1.getType2());
                    } else {
                        p1 = LayoutEditor.getCoords(ts1.getConnect1(), ts1.getType1());
                    }
                    if (ts2.getConnect1() == this) {
                        p2 = LayoutEditor.getCoords(ts2.getConnect2(), ts2.getType2());
                    } else {
                        p2 = LayoutEditor.getCoords(ts2.getConnect1(), ts2.getType1());
                    }
                    lc.setDirection(Path.computeDirection(p1, p2));
                    // save Connections
                    lc.setConnections(ts1, ts2, TRACK, this);
                    results.add(lc);
                }
            }
        } else if (getType() == EDGE_CONNECTOR) {
            TrackSegment ts2 = null;
            if (getLinkedPoint() != null) {
                ts2 = getLinkedPoint().getConnect1();
            }
            if ((ts1 != null) && (ts2 != null)) {
                blk1 = ts1.getLayoutBlock();
                blk2 = ts2.getLayoutBlock();
                if ((blk1 != null) && (blk2 != null) && (blk1 != blk2)) {
                    // this is a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary ('{}'<->'{}') found at {}", blk1, blk2, this);
                    lc = new LayoutConnectivity(blk1, blk2);

                    // determine direction from block 1 to block 2
                    if (ts1.getConnect1() == this) {
                        p1 = LayoutEditor.getCoords(ts1.getConnect2(), ts1.getType2());
                    } else {
                        p1 = LayoutEditor.getCoords(ts1.getConnect1(), ts1.getType1());
                    }

                    //Need to find a way to compute the direction for this for a split over the panel
                    //In this instance work out the direction of the first track relative to the positionable poin.
                    lc.setDirection(Path.computeDirection(p1, getCoordsCenter()));
                    // save Connections
                    lc.setConnections(ts1, ts2, TRACK, this);
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
    public List<Integer> checkForFreeConnections() {
        List<Integer> result = new ArrayList<>();

        if ((getConnect1() == null)
                || ((getType() == ANCHOR) && (getConnect2() == null))) {
            result.add(Integer.valueOf(POS_POINT));
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
                    log.debug("*New block ('{}') trackNameSets", blk1);
                    TrackNameSets = new ArrayList<>();
                    blockNamesToTrackNameSetsMap.put(blk1, TrackNameSets);
                }
                if (TrackNameSet == null) {
                    TrackNameSet = new LinkedHashSet<>();
                    log.debug("*    Add track '{}' to trackNameSet for block '{}'", getName(), blk1);
                    TrackNameSet.add(getName());
                    TrackNameSets.add(TrackNameSet);
                }
                if (connect1 != null) { // (#4)
                    connect1.collectContiguousTracksNamesInBlockNamed(blk1, TrackNameSet);
                }
            }
        }

        if (getType() == ANCHOR) {
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
                        log.debug("*New block ('{}') trackNameSets", blk2);
                        TrackNameSets = new ArrayList<>();
                        blockNamesToTrackNameSetsMap.put(blk2, TrackNameSets);
                    }
                    if (TrackNameSet == null) {
                        TrackNameSet = new LinkedHashSet<>();
                        log.debug("*    Add track '{}' to TrackNameSet for block '{}'", getName(), blk2);
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
                        log.debug("*    Add track '{}'for block '{}'", getName(), blockName);
                    }
                    // this should never be null... but just in case...
                    if (connect1 != null) {
                        connect1.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                    }
                }
            }
            if (getType() == ANCHOR) {
                TrackSegment ts2 = getConnect2();
                // this should never be null... but just in case...
                if (ts2 != null) {
                    String blk2 = ts2.getBlockName();
                    // is this the blockName we're looking for?
                    if (blk2.equals(blockName)) {
                        // if we are added to the TrackNameSet
                        if (TrackNameSet.add(getName())) {
                            log.debug("*    Add track '{}'for block '{}'", getName(), blockName);
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

    private final static Logger log = LoggerFactory.getLogger(PositionablePoint.class);

}
