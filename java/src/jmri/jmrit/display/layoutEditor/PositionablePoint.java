package jmri.jmrit.display.layoutEditor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JButton;
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
import jmri.util.MathUtil;
import jmri.util.swing.JCBHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PositionablePoint is a Point defining a node in the Track that can be dragged
 * around the inside of the enclosing LayoutEditor panel using a right-drag
 * (drag with meta key).
 * <P>
 * Three types of Positionable Point are supported: Anchor - point on track -
 * two track connections End Bumper - end of track point - one track connection
 * Edge Connector - This is used to link track segements between two different
 * panels
 * <P>
 * Note that a PositionablePoint exists for specifying connectivity and drawing
 * position only. The Track Segments connected to a PositionablePoint may belong
 * to the same block or to different blocks. Since each Track Segment may only
 * belong to one block, a PositionablePoint may function as a Block Boundary.
 * <P>
 * Signal names are saved here at a Block Boundary anchor point by the tool Set
 * Signals at Block Boundary. PositionablePoint does nothing with these signal
 * head names; it only serves as a place to store them.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @author Bob Jacobsen Copyright (2) 2014
 */
public class PositionablePoint extends LayoutTrack {

    // Defined text resource
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    // defined constants
    public static final int ANCHOR = 1;
    public static final int END_BUMPER = 2;
    public static final int EDGE_CONNECTOR = 3;

    // operational instance variables (not saved between sessions)
    private PositionablePoint instance = null;

    // persistent instances variables (saved between sessions)
    private int type = 0;
    private TrackSegment connect1 = null;
    private TrackSegment connect2 = null;

    protected NamedBeanHandle<SignalHead> signalEastHeadNamed = null; // signal head for east (south) bound trains
    protected NamedBeanHandle<SignalHead> signalWestHeadNamed = null; // signal head for west (north) bound trains

    private NamedBeanHandle<SignalMast> eastBoundSignalMastNamed = null;
    private NamedBeanHandle<SignalMast> westBoundSignalMastNamed = null;
    /* We use a namedbeanhandle for the the sensors, even though we only store the name here,
     this is so that we can keep up with moves and changes of userNames */
    private NamedBeanHandle<Sensor> eastBoundSensorNamed = null;
    private NamedBeanHandle<Sensor> westBoundSensorNamed = null;

    public PositionablePoint(String id, int t, Point2D p, LayoutEditor myPanel) {
        instance = this;
        layoutEditor = myPanel;
        if ((t == ANCHOR) || (t == END_BUMPER) || (t == EDGE_CONNECTOR)) {
            type = t;
        } else {
            log.error("Illegal type of PositionablePoint - " + t);
            type = ANCHOR;
        }
        ident = id;
        center = p;
    }

    // this should only be used for debugging...
    public String toString() {
        return "PositionablePoint " + ident;
    }

    /**
     * Accessor methods
     */
    public int getType() {
        return type;
    }

    /**
     * get coordinates
     *
     * @return the coordinates
     *
     * @deprecated replaced by
     * @see #getCoordsCenter()
     * @since 4.9.2
     *
     * note: done for Polymorphism so all LayoutTrack sub-classes use same method
     */
    @Deprecated
    public Point2D getCoords() {
        return getCoordsCenter();
    }

    /**
     * set the coordinates
     * @param p the coordinates
     *
     * @deprecated replaced by
     * @see #setCoordsCenter(Point2D)
     * @since 4.9.2
     *
     * note: done for Polymorphism so all LayoutTrack sub-classes use same method
     */
    @Deprecated
    public void setCoords(Point2D p) {
        setCoordsCenter(p);
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
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    public void scaleCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        center = MathUtil.granulize(MathUtil.multiply(center, factor), 1.0);
    }

    /**
     * translate this LayoutTrack's coordinates by the x and y factors
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
    public Rectangle2D getBounds() {
        Point2D c = getCoordsCenter();
        return new Rectangle2D.Double(c.getX(), c.getY(), 0.0, 0.0);
    }

    private PositionablePoint linkedPoint;

    public String getLinkEditorName() {
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
            return linkedPoint.getID();
        }
        return "";
    }

    public void setLinkedPoint(PositionablePoint p) {
        if (p == linkedPoint) {
            return;
        }
        if (linkedPoint != null && linkedPoint != p) {
            PositionablePoint oldLinkedPoint = linkedPoint;
            linkedPoint = null;
            if (oldLinkedPoint.getLinkedPoint() != null) {
                oldLinkedPoint.setLinkedPoint(null);
            }
            if (oldLinkedPoint.getConnect1() != null) {
                TrackSegment ts = oldLinkedPoint.getConnect1();
                oldLinkedPoint.getLayoutEditor().auxTools.setBlockConnectivityChanged();
                ts.updateBlockInfo();
                oldLinkedPoint.getLayoutEditor().repaint();
            }
            if (getConnect1() != null) {
                layoutEditor.auxTools.setBlockConnectivityChanged();
                getConnect1().updateBlockInfo();
                layoutEditor.repaint();
            }
        }
        linkedPoint = p;
        if (p != null) {
            p.setLinkedPoint(this);
            if (getConnect1() != null) {
                layoutEditor.auxTools.setBlockConnectivityChanged();
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
            if (dir == Path.EAST || dir == Path.SOUTH || dir == Path.SOUTH + Path.EAST) {
                if (signalEastHeadNamed != null) {
                    return signalEastHeadNamed.getBean();
                }
                return null;
            } else if (getLinkedPoint() != null) {
                // Do some checks to find where the connection is here.
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.SOUTH || linkDir == Path.EAST || linkDir == Path.SOUTH + Path.EAST) {
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
            if (dir == Path.EAST || dir == Path.SOUTH || dir == Path.SOUTH + Path.EAST) {
                setEastBoundSignalName(signalName);
            } else if (getLinkedPoint() != null) {
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.SOUTH || linkDir == Path.EAST || linkDir == Path.SOUTH + Path.EAST) {
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
            if (dir == Path.WEST || dir == Path.NORTH || dir == Path.NORTH + Path.WEST) {
                if (signalWestHeadNamed != null) {
                    return signalWestHeadNamed.getBean();
                }
                return null;
            } else if (getLinkedPoint() != null) {
                // Do some checks to find where the connection is here.
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.WEST || linkDir == Path.NORTH || linkDir == Path.NORTH + Path.WEST) {
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
            if (dir == Path.WEST || dir == Path.NORTH || dir == Path.NORTH + Path.WEST) {
                setWestBoundSignalName(signalName);
            } else if (getLinkedPoint() != null) {
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.WEST || linkDir == Path.NORTH || linkDir == Path.NORTH + Path.WEST) {
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
            if (dir == Path.SOUTH || dir == Path.EAST || dir == Path.SOUTH + Path.EAST) {
                return eastBoundSignalMastNamed;
            } else if (getLinkedPoint() != null) {
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.SOUTH || linkDir == Path.EAST || linkDir == Path.SOUTH + Path.EAST) {
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
            if (dir == Path.EAST || dir == Path.SOUTH || dir == Path.SOUTH + Path.EAST) {
                eastBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
            } else if (getLinkedPoint() != null) {
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.SOUTH || linkDir == Path.EAST || linkDir == Path.SOUTH + Path.EAST) {
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
            if (dir == Path.WEST || dir == Path.NORTH || dir == Path.NORTH + Path.WEST) {
                return westBoundSignalMastNamed;
            } else if (getLinkedPoint() != null) {
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.WEST || linkDir == Path.NORTH || linkDir == Path.NORTH + Path.WEST) {
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
            if (dir == Path.WEST || dir == Path.NORTH || dir == Path.NORTH + Path.WEST) {
                westBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
            } else if (getLinkedPoint() != null) {
                int linkDir = getLinkedPoint().getConnect1Dir();
                if (linkDir == Path.WEST || linkDir == Path.NORTH || linkDir == Path.NORTH + Path.WEST) {
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
    public void setObjects(LayoutEditor p) {
        if (type == EDGE_CONNECTOR) {
            connect1 = p.getFinder().findTrackSegmentByName(trackSegment1Name);
            if (getConnect2() != null && getLinkedEditor() != null) {
                //now that we have a connection we can fire off a change
                TrackSegment ts = getConnect2();
                getLinkedEditor().auxTools.setBlockConnectivityChanged();
                ts.updateBlockInfo();
            }
        } else {
            connect1 = p.getFinder().findTrackSegmentByName(trackSegment1Name);
            connect2 = p.getFinder().findTrackSegmentByName(trackSegment2Name);
        }
    }

    /**
     * Setup and remove connections to track
     */
    public boolean setTrackConnection(TrackSegment track) {
        if (track == null) {
            return false;
        }
        if ((connect1 != track) && (connect2 != track)) {
            // not connected to this track
            if (connect1 == null) {
                connect1 = track;
            } else if ((type == ANCHOR) && (connect2 == null)) {
                connect2 = track;
                if (connect1.getLayoutBlock() == connect2.getLayoutBlock()) {
                    westBoundSignalMastNamed = null;
                    eastBoundSignalMastNamed = null;
                    setWestBoundSensor("");
                    setEastBoundSensor("");
                }
            } else {
                log.error("Attempt to assign more than allowed number of connections");
                return false;
            }
        }
        return true;
    }

    public void removeTrackConnection(TrackSegment track) {
        if (track == connect1) {
            connect1 = null;
            reCheckBlockBoundary();
            removeLinkedPoint();
        } else if (track == connect2) {
            connect2 = null;
            reCheckBlockBoundary();
        } else {
            log.error("Attempt to remove non-existant track connection");
        }
    }

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
            //May want to look at a method to remove the assigned mast from the panel and potentially any SignalMast logics generated
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
            //May want to look at a method to remove the assigned mast from the panel and potentially any SignalMast logics generated
        }
    }

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
            showPopUp(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        // if (debug) log.debug("Release: "+where(e));
        if (e.isPopupTrigger()) {
            showPopUp(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopUp(e);
        }
    }

    private JPopupMenu popup = null;
    private LayoutEditorTools tools = null;

    /**
     * For editing: only provides remove
     */
    protected void showPopUp(MouseEvent e) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }

        if (tools == null) {
            tools = new LayoutEditorTools(layoutEditor);
        }

        boolean blockBoundary = false;
        boolean endBumper = false;
        JMenuItem jmi = null;
        switch (getType()) {
            case ANCHOR:
                jmi = popup.add(rb.getString("Anchor"));
                jmi.setEnabled(false);
                LayoutBlock block1 = null;
                LayoutBlock block2 = null;
                if (connect1 != null) {
                    block1 = connect1.getLayoutBlock();
                }
                if (connect2 != null) {
                    block2 = connect2.getLayoutBlock();
                }
                if ((block1 != null) && (block1 == block2)) {
                    jmi = popup.add(Bundle.getMessage("BeanNameBlock") + ": " + block1.getID());
                } else if ((block1 != null) && (block2 != null) && (block1 != block2)) {
                    jmi = popup.add(rb.getString("BlockDivider"));
                    jmi.setEnabled(false);
                    jmi = popup.add(" " + Bundle.getMessage("Block_ID", 1) + ": " + block1.getID());
                    jmi.setEnabled(false);
                    jmi = popup.add(" " + Bundle.getMessage("Block_ID", 2) + ": " + block2.getID());
                    blockBoundary = true;
                }
                jmi.setEnabled(false);
                break;
            case END_BUMPER:
                jmi = popup.add(rb.getString("EndBumper"));
                jmi.setEnabled(false);

                LayoutBlock blockEnd = null;
                if (connect1 != null) {
                    blockEnd = connect1.getLayoutBlock();
                }
                if (blockEnd != null) {
                    jmi = popup.add(rb.getString("BlockID") + ": " + blockEnd.getID());
                    jmi.setEnabled(false);
                }
                endBumper = true;
                break;
            case EDGE_CONNECTOR:
                jmi = popup.add(rb.getString("EdgeConnector"));
                jmi.setEnabled(false);

                if (getLinkedEditor() != null) {
                    jmi = popup.add(getLinkEditorName());
                } else {
                    jmi = popup.add(rb.getString("EdgeNotLinked"));
                }
                jmi.setEnabled(false);

                block1 = null;
                block2 = null;
                if (connect1 != null) {
                    block1 = connect1.getLayoutBlock();
                }
                if (getConnect2() != null) {
                    block2 = getConnect2().getLayoutBlock();
                }
                if ((block1 != null) && (block2 != null) && (block1 != block2)) {
                    jmi = popup.add(rb.getString("BlockDivider"));
                    jmi.setEnabled(false);
                    jmi = popup.add(" " + Bundle.getMessage("Block_ID", 1) + ": " + block1.getID());
                    jmi.setEnabled(false);
                    jmi = popup.add(" " + Bundle.getMessage("Block_ID", 2) + ": " + block2.getID());
                    jmi.setEnabled(false);
                }
                blockBoundary = true;
                break;
            default:
                break;
        }
        popup.add(new JSeparator(JSeparator.HORIZONTAL));
        popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (layoutEditor.removePositionablePoint(instance)) {
                    // user is serious about removing this point from the panel
                    remove();
                    dispose();
                }
            }
        });
        if (blockBoundary) {
            if (getType() == EDGE_CONNECTOR) {
                popup.add(new AbstractAction(rb.getString("EdgeEditLink")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setLink();
                    }
                });
                popup.add(new AbstractAction(rb.getString("SetSignals")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tools = new LayoutEditorTools(layoutEditor);
                        // bring up signals at level crossing tool dialog
                        tools.setSignalAtEdgeConnector(instance,
                                layoutEditor.signalIconEditor, layoutEditor.signalFrame);
                    }
                });
                popup.add(new AbstractAction(rb.getString("SetSignalMasts")) {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        // bring up signals at block boundary tool dialog
                        tools.setSignalMastsAtBlockBoundaryFromMenu(instance);
                    }
                });
            } else {
                AbstractAction ssaa = new AbstractAction(rb.getString("SetSignals")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // bring up signals at level crossing tool dialog
                        tools.setSignalsAtBlockBoundaryFromMenu(instance,
                                layoutEditor.signalIconEditor, layoutEditor.signalFrame);
                    }
                };

                JMenu jm = new JMenu(Bundle.getMessage("SignalHeads"));
                if (tools.addBlockBoundarySignalHeadInfoToMenu(instance, jm)) {
                    jm.add(ssaa);
                    popup.add(jm);
                } else {
                    popup.add(ssaa);
                }

                popup.add(new AbstractAction(rb.getString("SetSignalMasts")) {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        // bring up signals at block boundary tool dialog
                        tools.setSignalMastsAtBlockBoundaryFromMenu(instance);
                    }
                });
            }
        }
        if (endBumper) {
            popup.add(new AbstractAction(rb.getString("SetSensors")) {
                @Override
                public void actionPerformed(ActionEvent event) {
                    // bring up signals at block boundary tool dialog
                    tools.setSensorsAtBlockBoundaryFromMenu(instance,
                            layoutEditor.sensorIconEditor, layoutEditor.sensorFrame);
                }
            });
            popup.add(new AbstractAction(rb.getString("SetSignalMasts")) {
                @Override
                public void actionPerformed(ActionEvent event) {
                    // bring up signals at block boundary tool dialog
                    tools.setSignalMastsAtBlockBoundaryFromMenu(instance);
                }
            });
        }
        layoutEditor.setShowAlignmentMenu(popup);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    String where(MouseEvent e) {
        return "" + e.getX() + "," + e.getY();
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
                oldLinkedEditor.auxTools.setBlockConnectivityChanged();
                ts.updateBlockInfo();
            }
            linkedPoint = null;
            //linkedEditor=null;
        }
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
        // remove from persistance by flagging inactive
        active = false;
    }

    boolean active = true;

    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }

    protected int getConnect1Dir() {
        Point2D p1;
        if (getConnect1() == null) {
            return Path.NONE;
        }
        if (getConnect1().getConnect1() == this) {
            p1 = layoutEditor.getCoords(getConnect1().getConnect2(), getConnect1().getType2());
        } else {
            p1 = layoutEditor.getCoords(getConnect1().getConnect1(), getConnect1().getType1());
        }

        double dh = getCoordsCenter().getX() - p1.getX();
        double dv = getCoordsCenter().getY() - p1.getY();
        int dir = Path.NORTH;
        double tanA;
        if (dv != 0.0) {
            tanA = Math.abs(dh) / Math.abs(dv);
        } else {
            tanA = 10.0;
        }
        if (tanA < 0.38268) {
            // track is mostly vertical
            if (dv < 0.0) {
                dir = Path.NORTH;
            } else {
                dir = Path.SOUTH;
            }
        } else if (tanA > 2.4142) {
            // track is mostly horizontal
            if (dh > 0.0) {
                dir = Path.EAST;
            } else {
                dir = Path.WEST;
            }
        } else {
            // track is between horizontal and vertical
            if ((dv > 0.0) && (dh > 0.0)) {
                dir = Path.SOUTH + Path.EAST;
            } else if ((dv > 0.0) && (dh < 0.0)) {
                dir = Path.SOUTH + Path.WEST;
            } else if ((dv < 0.0) && (dh < 0.0)) {
                dir = Path.NORTH + Path.WEST;
            } else {
                dir = Path.NORTH + Path.EAST;
            }
        }
        return dir;
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
        editLink.setTitle("EDIT LINK from " + getConnect1().getLayoutBlock().getDisplayName());

        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());

        JButton done = new JButton(Bundle.getMessage("ButtonDone"));
        done.addActionListener(
                (ActionEvent a) -> {
                    updateLink();
                });

        // make this button the default button (return or enter activates)
        // Note: We have to invoke this later because we don't currently have a root pane
        SwingUtilities.invokeLater(() -> {
            JRootPane rootPane = SwingUtilities.getRootPane(done);
            rootPane.setDefaultButton(done);
        });

        container.add(getLinkPanel(), BorderLayout.NORTH);
        container.add(done, BorderLayout.SOUTH);
        container.revalidate();

        editLink.add(container);

        editLink.pack();
        editLink.setModal(false);
        editLink.setVisible(true);
    }
    ArrayList<PositionablePoint> pointList;

    public JPanel getLinkPanel() {
        editorCombo = new JComboBox<JCBHandle<LayoutEditor>>();
        ArrayList<LayoutEditor> panels = InstanceManager.getDefault(PanelMenu.class).getLayoutEditorPanelList();
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
        selectorPanel.add(new JLabel("Select Panel"));
        selectorPanel.add(editorCombo);
        linkPointsBox = new JComboBox<String>();
        updatePointBox();
        selectorPanel.add(new JLabel("Connecting Block"));
        selectorPanel.add(linkPointsBox);
        return selectorPanel;
    }

    void updatePointBox() {
        linkPointsBox.removeAllItems();
        pointList = new ArrayList<PositionablePoint>();
        if (editorCombo.getSelectedIndex() == 0) {
            linkPointsBox.setEnabled(false);
            return;
        }
        int ourDir = getConnect1Dir();
        linkPointsBox.setEnabled(true);
        for (PositionablePoint p : editorCombo.getItemAt(editorCombo.getSelectedIndex()).item().pointList) {
            if (p.getType() == EDGE_CONNECTOR) {
                if (p.getLinkedPoint() == this) {
                    pointList.add(p);
                    linkPointsBox.addItem(p.getConnect2().getLayoutBlock().getDisplayName());
                    linkPointsBox.setSelectedItem(p.getConnect2().getLayoutBlock().getDisplayName());
                } else if (p.getLinkedPoint() == null) {
                    if (p.getConnect1() != null && p.getConnect1().getLayoutBlock() != null) {
                        if (p.getConnect1().getLayoutBlock() != getConnect1().getLayoutBlock() && ourDir != p.getConnect1Dir()) {
                            pointList.add(p);
                            linkPointsBox.addItem(p.getConnect1().getLayoutBlock().getDisplayName());
                        }
                    }
                }
            }
        }
        editLink.pack();
    }

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
                    jmri.SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(removeremote);
                    getLinkedEditor().removeSignalHead(sh);
                    jmri.jmrit.blockboss.BlockBossLogic.getStoppedObject(removeremote);
                }
                if (!removelocal.isEmpty()) {
                    jmri.SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(removelocal);
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
     * find the hit (location) type for a point
     *
     * @param p                  the point
     * @param useRectangles      whether to use (larger) rectangles or (smaller)
     *                           circles for hit testing
     * @param requireUnconnected whether to only return free connection hit
     *                           types
     * @return the location type for the point (or NONE)
     * @since 7.4.3
     */
    protected int findHitPointType(Point2D p, boolean useRectangles, boolean requireUnconnected) {
        int result = NONE;  // assume point not on connection

        if (!requireUnconnected || (getConnect1() == null)
                || ((getType() == PositionablePoint.ANCHOR) && (getConnect2() == null))) {
            // test point control rectangle
            Rectangle2D r = layoutEditor.trackControlPointRectAt(getCoordsCenter());
            if (r.contains(p)) {
                result = POS_POINT;
            }
        }
        return result;
    }

    /**
     * return the coordinates for a specified connection type
     *
     * @param connectionType the connection type
     * @return the coordinates for the specified connection type
     */
    public Point2D getCoordsForConnectionType(int connectionType) {
        Point2D result = getCoordsCenter();
        if (connectionType != POS_POINT) {
            log.error("Invalid connection type " + connectionType); //I18IN
        }
        return result;
    }

    /**
     * get the object connected to this track for the specified connection type
     *
     * @param connectionType the specified connection type
     * @return the object connected for the specified connection type
     * @throws jmri.JmriException - if the connectionType is invalid
     */
    @Override
    public Object getConnection(int connectionType) throws jmri.JmriException {
        Object result = null;
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
     * set the object connected for the specified connection type
     *
     * @param connectionType the connection type (where it is connected to us)
     * @param o              the object that is being connected
     * @param type           the type of object that we're being connected to
     *                       (Should always be "NONE" or "TRACK")
     * @throws jmri.JmriException - if connectionType or type are invalid
     */
    @Override
    public void setConnection(int connectionType, Object o, int type) throws jmri.JmriException {
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
     * @param connectionType - the connection type to test
     * @return true if the connection for this connection type is free
     */
    public boolean isDisconnected(int connectionType) {
        boolean result = false;
        if (connectionType == POS_POINT) {
            result = ((getConnect1() == null) || (getConnect2() == null));
        } else {
            log.error("Invalid connection type " + connectionType); //I18IN
        }
        return result;
    }

    /**
     * draw this PositionablePoint
     *
     * @param g2 the graphics port to draw to
     */
    public void draw(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (getType()) {
            case PositionablePoint.ANCHOR:
            case PositionablePoint.EDGE_CONNECTOR: {
                if (getConnect1() == null) {
                    g2.setColor(Color.red);
                } else if (getConnect2() == null) {
                    g2.setColor(Color.blue);
                } else {
                    g2.setColor(Color.green);
                }
                break;
            }
            case PositionablePoint.END_BUMPER: {
                if (getConnect1() == null) {
                    g2.setColor(Color.red);
                } else {
                    g2.setColor(Color.green);
                }
                break;
            }
            default: {
                log.error("Illegal type of Positionable Point");
                break;
            }
        }   //switch
        // drawHidden locater rectangle
        Point2D pt = getCoordsCenter();
        g2.draw(layoutEditor.trackControlPointRectAt(pt));
    }

    /*
        return the layout connectivity for this PositionablePoint
    */
    protected ArrayList<LayoutConnectivity> getLayoutConnectivity() {
        ArrayList<LayoutConnectivity> results = new ArrayList<LayoutConnectivity>();
        LayoutConnectivity lc = null;

        LayoutBlock blk1 = null, blk2 = null;
        TrackSegment ts1 = getConnect1(), ts2 = getConnect2();
        Point2D p1, p2;

        if (getType() == PositionablePoint.ANCHOR) {
            if ((ts1 != null) && (ts2 != null)) {
                blk1 = ts1.getLayoutBlock();
                blk2 = ts2.getLayoutBlock();
                if ((blk1 != null) && (blk2 != null) && (blk1 != blk2)) {
                    // this is a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary ('{}'<->'{}') found at {}", blk1, blk2, this);
                    lc = new LayoutConnectivity(blk1, blk2);
                    // determine direction from block 1 to block 2
                    if (ts1.getConnect1() == this) {
                        p1 = layoutEditor.getCoords(ts1.getConnect2(), ts1.getType2());
                    } else {
                        p1 = layoutEditor.getCoords(ts1.getConnect1(), ts1.getType1());
                    }
                    if (ts2.getConnect1() == this) {
                        p2 = layoutEditor.getCoords(ts2.getConnect2(), ts2.getType2());
                    } else {
                        p2 = layoutEditor.getCoords(ts2.getConnect1(), ts2.getType1());
                    }
                    lc.setDirection(LayoutEditorAuxTools.computeDirection(p1, p2));
                    // save Connections
                    lc.setConnections(ts1, ts2, LayoutTrack.TRACK, this);
                    results.add(lc);
                }
            }
        } else if (getType() == PositionablePoint.EDGE_CONNECTOR) {
            if ((ts1 != null) && (ts2 != null)) {
                blk1 = ts1.getLayoutBlock();
                blk2 = ts2.getLayoutBlock();
                if ((blk1 != null) && (blk2 != null) && (blk1 != blk2)) {
                    // this is a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary ('{}'<->'{}') found at {}", blk1, blk2, this);
                    lc = new LayoutConnectivity(blk1, blk2);

                    // determine direction from block 1 to block 2
                    if (ts1.getConnect1() == this) {
                        p1 = layoutEditor.getCoords(ts1.getConnect2(), ts1.getType2());
                    } else {
                        p1 = layoutEditor.getCoords(ts1.getConnect1(), ts1.getType1());
                    }

                    //Need to find a way to compute the direction for this for a split over the panel
                    //In this instance work out the direction of the first track relative to the positionable poin.
                    lc.setDirection(LayoutEditorAuxTools.computeDirection(p1, getCoordsCenter()));
                    // save Connections
                    lc.setConnections(ts1, ts2, LayoutTrack.TRACK, this);
                    results.add(lc);
                }
            }
        }
        return results;
    }   // getLayoutConnectivity()

    private final static Logger log = LoggerFactory.getLogger(PositionablePoint.class.getName());
}
