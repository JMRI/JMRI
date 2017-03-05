package jmri.jmrit.display.layoutEditor;

import static jmri.util.MathUtil.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriBeanComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutSlip is two track segment on a layout that cross at an angle.
 * <P>
 * A LayoutSlip has four connection points, designated A, B, C, and D. At the
 * crossing, A-C and B-D are straight segments. A train proceeds through the
 * crossing on either of these segments.
 * <P>
 * For drawing purposes, each LayoutSlip carries a center point and
 * displacements for A and B. The displacements for C = - the displacement for
 * A, and the displacement for D = - the displacement for B. The center point
 * and these displacements may be adjusted by the user when in edit mode.
 * <P>
 * When LayoutSlips are first created, there are no connections. Block
 * information and connections are added when available.
 * <P>
 * SignalHead names are saved here to keep track of where signals are.
 * LayoutSlip only serves as a storage place for SignalHead names. The names
 * are placed here by Set Signals at Level Crossing in Tools menu.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 */
public class LayoutSlip extends LayoutTurnout {

    // Defined text resource
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    // operational instance variables (not saved between sessions)
    final public static int UNKNOWN = Turnout.UNKNOWN;
    final public static int STATE_AC = 0x02;
    final public static int STATE_BD = 0x04;
    final public static int STATE_AD = 0x06;
    final public static int STATE_BC = 0x08;

    public int currentState = UNKNOWN;

    private String turnoutBName = "";
    private NamedBeanHandle<Turnout> namedTurnoutB = null;
    private java.beans.PropertyChangeListener mTurnoutListener = null;

    /**
     * constructor method
     */
    public LayoutSlip(String id, Point2D c, double rot, LayoutEditor myPanel, int type) {
        instance = this;
        layoutEditor = myPanel;
        ident = id;
        center = c;
        dispC = new Point2D.Double(-20.0, 0.0);
        dispB = new Point2D.Double(-14.0, 14.0);
        setTurnoutType(type);
        rotateCoords(rot);
    }

    // this should only be used for debugging…
    public String toString() {
        return "LayoutSlip " + ident;
    }

    public void setTurnoutType(int slipType) {
        setSlipType(slipType);
    }

    public void setSlipType(int slipType) {
        if (type == slipType) {
            return;
        }
        type = slipType;
        if (type == DOUBLE_SLIP) {
            turnoutStates.put(STATE_AC, new TurnoutState(Turnout.CLOSED, Turnout.CLOSED));
            turnoutStates.put(STATE_BD, new TurnoutState(Turnout.THROWN, Turnout.THROWN));
            turnoutStates.put(STATE_AD, new TurnoutState(Turnout.CLOSED, Turnout.THROWN));
            turnoutStates.put(STATE_BC, new TurnoutState(Turnout.THROWN, Turnout.CLOSED));
        } else {
            turnoutStates.put(STATE_AC, new TurnoutState(Turnout.CLOSED, Turnout.THROWN));
            turnoutStates.put(STATE_BD, new TurnoutState(Turnout.THROWN, Turnout.CLOSED));
            turnoutStates.put(STATE_AD, new TurnoutState(Turnout.THROWN, Turnout.THROWN));
            turnoutStates.remove(STATE_BC);
        }
    }

    public int getSlipType() {
        return type;
    }

    public int getSlipState() {
        return currentState;
    }

    public String getTurnoutBName() {
        if (namedTurnoutB != null) {
            return namedTurnoutB.getName();
        }
        return turnoutBName;
    }

    public Turnout getTurnoutB() {
        if (namedTurnoutB == null) {
            // set physical turnout if possible and needed
            setTurnoutB(turnoutBName);
            if (namedTurnoutB == null) {
                return null;
            }
        }
        return namedTurnoutB.getBean();
    }

    public void setTurnoutB(String tName) {
        if (namedTurnoutB != null) {
            deactivateTurnout();
        }
        turnoutBName = tName;
        Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().
                getTurnout(turnoutBName);
        if (turnout != null) {
            namedTurnoutB = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutBName, turnout);
            activateTurnout();
        } else {
            turnoutBName = "";
            namedTurnoutB = null;
        }
    }

    @Override
    public Object getConnection(int location) throws jmri.JmriException {
        switch (location) {
            case SLIP_A:
                return connectA;
            case SLIP_B:
                return connectB;
            case SLIP_C:
                return connectC;
            case SLIP_D:
                return connectD;
        }
        log.error("Invalid Point Type " + location); //I18IN
        throw new jmri.JmriException("Invalid Point");
    }

    @Override
    public void setConnection(int location, Object o, int type) throws jmri.JmriException {
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of connection to layoutturnout - " + type);
            throw new jmri.JmriException("unexpected type of connection to layoutturnout - " + type);
        }
        switch (location) {
            case SLIP_A:
                connectA = o;
                break;
            case SLIP_B:
                connectB = o;
                break;
            case SLIP_C:
                connectC = o;
                break;
            case SLIP_D:
                connectD = o;
                break;
            default:
                log.error("Invalid Point Type " + location); //I18IN
                throw new jmri.JmriException("Invalid Point");
        }
    }

    public String getDisplayName() {
        String name = "Slip :";
        if (getTurnoutName() != null) {
            name += "(" + getTurnoutName();
            if (getTurnoutBName() != null) {
                name += ":" + getTurnoutBName() + ")";
            }
        } else if (getTurnoutBName() != null) {
            name += "(" + getTurnoutBName() + ")";
        }
        return name;
    }

    /**
     * Toggle slip states if clicked on, physical turnout exists, and not disabled
     */
    public void toggleState(int selectedPointType) {
        switch (selectedPointType) {
            case SLIP_LEFT: {
                switch (currentState) {
                    case STATE_AC: {
                        if (type == SINGLE_SLIP) {
                            currentState = STATE_BD;
                        } else {
                            currentState = STATE_BC;
                        }
                        break;
                    }
                    case STATE_BD: {
                        currentState = STATE_AD;
                        break;
                    }
                    case STATE_AD: {
                        currentState = STATE_BD;
                        break;
                    }
                    case STATE_BC:
                    default: {
                        currentState = STATE_AC;
                        break;
                    }
                }
                break;
            }
            case SLIP_RIGHT: {
                switch (currentState) {
                    case STATE_AC: {
                        currentState = STATE_AD;
                        break;
                    }
                    case STATE_BD: {
                        if (type == SINGLE_SLIP) {
                            currentState = STATE_AC;
                        } else {
                            currentState = STATE_BC;
                        }
                        break;
                    }
                    case STATE_AD: {
                        currentState = STATE_AC;
                        break;
                    }
                    case STATE_BC:
                    default: {
                        currentState = STATE_BD;
                        break;
                    }
                }
                break;
            }
            case SLIP_CENTER:   //note: this should "go away"
            //(since SLIP_CENTER should have been replaced by SLIP_LEFT & SLIP_RIGHT everywhere)
            default:
            {
                switch (currentState) {
                    case STATE_AC: {
                        if (singleSlipStraightEqual()) {
                            currentState = STATE_BD;
                        } else {
                            currentState = STATE_BC;
                        }
                        break;
                    }

                    case STATE_BD: {
                        currentState = STATE_AD;
                        break;
                    }

                    case STATE_AD: {
                        currentState = STATE_AC;
                        break;
                    }

                    case STATE_BC:
                    default: {
                        currentState = STATE_BD;
                        break;
                    }
                }
                break;
            }
        }
        setTurnoutState(turnoutStates.get(currentState));
    }

    void setTurnoutState(TurnoutState ts) {
        if (getTurnout() != null) {
            getTurnout().setCommandedState(ts.getTurnoutAState());
        }
        if (getTurnoutB() != null) {
            getTurnoutB().setCommandedState(ts.getTurnoutBState());
        }
    }

    /**
     * Activate/Deactivate turnout to redraw when turnout state changes
     */
    private void activateTurnout() {
        if (namedTurnout != null) {
            namedTurnout.getBean().addPropertyChangeListener(mTurnoutListener
                    = (java.beans.PropertyChangeEvent e) -> {
                        updateState();
                    }, namedTurnout.getName(), "Layout Editor Slip");
        }
        if (namedTurnoutB != null) {
            namedTurnoutB.getBean().addPropertyChangeListener(mTurnoutListener
                    = (java.beans.PropertyChangeEvent e) -> {
                        updateState();
                    }, namedTurnoutB.getName(), "Layout Editor Slip");
        }
    }

    private void deactivateTurnout() {
        if (mTurnoutListener != null) {
            namedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            if (namedTurnoutB != null) {
                namedTurnoutB.getBean().removePropertyChangeListener(mTurnoutListener);
            }
            mTurnoutListener = null;
        }
    }

    @Override
    public Point2D getCoordsCenter() {
        return center;
    }

    @Override
    public Point2D getCoordsA() {
        double x = center.getX() + dispC.getX();
        double y = center.getY() + dispC.getY();
        return new Point2D.Double(x, y);
    }

    @Override
    public Point2D getCoordsB() {
        double x = center.getX() + dispB.getX();
        double y = center.getY() + dispB.getY();
        return new Point2D.Double(x, y);
    }

    @Override
    public Point2D getCoordsC() {
        double x = center.getX() - dispC.getX();
        double y = center.getY() - dispC.getY();
        return new Point2D.Double(x, y);
    }

    @Override
    public Point2D getCoordsD() {
        double x = center.getX() - dispB.getX();
        double y = center.getY() - dispB.getY();
        return new Point2D.Double(x, y);
    }

    private void updateBlockInfo() {
        LayoutBlock b1 = null;
        LayoutBlock b2 = null;
        if (block != null) {
            block.updatePaths();
        }
        if (connectA != null) {
            b1 = ((TrackSegment) connectA).getLayoutBlock();
            if ((b1 != null) && (b1 != block)) {
                b1.updatePaths();
            }
        }
        if (connectC != null) {
            b2 = ((TrackSegment) connectC).getLayoutBlock();
            if ((b2 != null) && (b2 != block) && (b2 != b1)) {
                b2.updatePaths();
            }
        }

        if (connectB != null) {
            b1 = ((TrackSegment) connectB).getLayoutBlock();
            if ((b1 != null) && (b1 != block)) {
                b1.updatePaths();
            }
        }
        if (connectD != null) {
            b2 = ((TrackSegment) connectD).getLayoutBlock();
            if ((b2 != null) && (b2 != block) && (b2 != b1)) {
                b2.updatePaths();
            }
        }
        reCheckBlockBoundary();
    }

    @Override
    public void reCheckBlockBoundary() {
        if (connectA == null && connectB == null && connectC == null && connectD == null) {
            //This is no longer a block boundary, therefore will remove signal masts and sensors if present
            if (signalAMastNamed != null) {
                removeSML(getSignalAMast());
            }
            if (signalBMastNamed != null) {
                removeSML(getSignalBMast());
            }
            if (signalCMastNamed != null) {
                removeSML(getSignalCMast());
            }
            if (signalDMastNamed != null) {
                removeSML(getSignalDMast());
            }
            signalAMastNamed = null;
            signalBMastNamed = null;
            signalCMastNamed = null;
            signalDMastNamed = null;
            sensorANamed = null;
            sensorBNamed = null;
            sensorCNamed = null;
            sensorDNamed = null;
            return;
            //May want to look at a method to remove the assigned mast from the panel and potentially any logics generated
        } else if (connectA == null || connectB == null || connectC == null || connectD == null) {
            //could still be in the process of rebuilding the point details
            return;
        }

        TrackSegment trkA;
        TrackSegment trkB;
        TrackSegment trkC;
        TrackSegment trkD;

        if (connectA instanceof TrackSegment) {
            trkA = (TrackSegment) connectA;
            if (trkA.getLayoutBlock() == block) {
                if (signalAMastNamed != null) {
                    removeSML(getSignalAMast());
                }
                signalAMastNamed = null;
                sensorANamed = null;
            }
        }
        if (connectC instanceof TrackSegment) {
            trkC = (TrackSegment) connectC;
            if (trkC.getLayoutBlock() == block) {
                if (signalCMastNamed != null) {
                    removeSML(getSignalCMast());
                }
                signalCMastNamed = null;
                sensorCNamed = null;
            }
        }
        if (connectB instanceof TrackSegment) {
            trkB = (TrackSegment) connectB;
            if (trkB.getLayoutBlock() == block) {
                if (signalBMastNamed != null) {
                    removeSML(getSignalBMast());
                }
                signalBMastNamed = null;
                sensorBNamed = null;
            }
        }

        if (connectD instanceof TrackSegment) {
            trkD = (TrackSegment) connectC;
            if (trkD.getLayoutBlock() == block) {
                if (signalDMastNamed != null) {
                    removeSML(getSignalDMast());
                }
                signalDMastNamed = null;
                sensorDNamed = null;
            }
        }
    }

    /**
     * Methods to test if mainline track or not Returns true if either
     * connecting track segment is mainline Defaults to not mainline if
     * connecting track segments are missing
     */
    public boolean isMainline() {
        if (((connectA != null) && (((TrackSegment) connectA).getMainline()))
                || ((connectB != null) && (((TrackSegment) connectB).getMainline()))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Modify coordinates methods
     */
    @Override
    public void setCoordsCenter(Point2D p) {
        center = p;
    }

    @Override
    public void setCoordsA(Point2D p) {
        double x = center.getX() - p.getX();
        double y = center.getY() - p.getY();
        dispC = new Point2D.Double(-x, -y);
    }

    @Override
    public void setCoordsB(Point2D p) {
        double x = center.getX() - p.getX();
        double y = center.getY() - p.getY();
        dispB = new Point2D.Double(-x, -y);
    }

    @Override
    public void setCoordsC(Point2D p) {
        double x = center.getX() - p.getX();
        double y = center.getY() - p.getY();
        dispC = new Point2D.Double(x, y);
    }

    @Override
    public void setCoordsD(Point2D p) {
        double x = center.getX() - p.getX();
        double y = center.getY() - p.getY();
        dispB = new Point2D.Double(x, y);
    }

    @Override
    public void scaleCoords(float xFactor, float yFactor) {
        Point2D pt = new Point2D.Double(Math.round(center.getX() * xFactor),
                Math.round(center.getY() * yFactor));
        center = pt;
        pt = new Point2D.Double(Math.round(dispC.getX() * xFactor),
                Math.round(dispC.getY() * yFactor));
        dispC = pt;
        pt = new Point2D.Double(Math.round(dispB.getX() * xFactor),
                Math.round(dispB.getY() * yFactor));
        dispB = pt;
    }

    /**
     * Initialization method The above variables are initialized by
     * PositionablePointXml, then the following method is called after the
     * entire LayoutEditor is loaded to set the specific TrackSegment objects.
     */
    @Override
    public void setObjects(LayoutEditor p) {
        connectA = p.getFinder().findTrackSegmentByName(connectAName);
        connectB = p.getFinder().findTrackSegmentByName(connectBName);
        connectC = p.getFinder().findTrackSegmentByName(connectCName);
        connectD = p.getFinder().findTrackSegmentByName(connectDName);
        if (tBlockName.length() > 0) {
            block = p.getLayoutBlock(tBlockName);
            if (block != null) {
                blockName = tBlockName;
                block.incrementUse();
            } else {
                log.error("bad blocknameac '" + tBlockName + "' in slip " + ident);
            }
        }
    }

    JPopupMenu popup = null;
    LayoutEditorTools tools = null;

    /**
     * Display popup menu for information and editing
     */
    @Override
    protected void showPopUp(MouseEvent e, boolean editable) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        if (editable) {
            popup.add(getName());
            boolean blockAssigned = false;
            if ((blockName == null) || (blockName.equals(""))) {
                popup.add(rb.getString("NoBlock"));
            } else {
                popup.add(rb.getString("BlockID") + ": " + getLayoutBlock().getID());
                blockAssigned = true;
            }

            if (hidden) {
                popup.add(rb.getString("Hidden"));
            } else {
                popup.add(rb.getString("NotHidden"));
            }

            popup.add(new JSeparator(JSeparator.HORIZONTAL));
            popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editLayoutSlip(instance);
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (layoutEditor.removeLayoutSlip(instance)) {
                        // Returned true if user did not cancel
                        remove();
                        dispose();
                    }
                }
            });
            if ((connectA == null) && (connectB == null)
                    && (connectC == null) && (connectD == null)) {
                JMenuItem rotateItem = new JMenuItem(rb.getString("Rotate") + "...");
                popup.add(rotateItem);
                rotateItem.addActionListener(
                    (ActionEvent event) -> {
                        boolean entering = true;
                        boolean error = false;
                        String newAngle = "";
                        while (entering) {
                            // prompt for rotation angle
                            error = false;
                            newAngle = JOptionPane.showInputDialog(layoutEditor,
                                    rb.getString("EnterRotation") + " :");
                            if (newAngle.length() < 1) {
                                return;  // cancelled
                            }
                            double rot = 0.0;
                            try {
                                rot = Double.parseDouble(newAngle);
                            } catch (Exception e1) {
                                JOptionPane.showMessageDialog(layoutEditor, rb.getString("Error3")
                                        + " " + e1, Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                                error = true;
                                newAngle = "";
                            }
                            if (!error) {
                                entering = false;
                                if (rot != 0.0) {
                                    rotateCoords(rot);
                                    layoutEditor.redrawPanel();
                                }
                            }
                        }
                    }
                );
            }
            if (blockAssigned) {
                popup.add(new AbstractAction(rb.getString("SetSignals")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (tools == null) {
                            tools = new LayoutEditorTools(layoutEditor);
                        }
                        tools.setSlipFromMenu((LayoutSlip) instance,
                                layoutEditor.signalIconEditor, layoutEditor.signalFrame);
                    }
                });
            }

            final String[] boundaryBetween = getBlockBoundaries();
            boolean blockBoundaries = false;

            for (int i = 0; i < 4; i++) {
                if (boundaryBetween[i] != null) {
                    blockBoundaries = true;
                }
            }
            if (blockBoundaries) {
                popup.add(new AbstractAction(rb.getString("SetSignalMasts")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (tools == null) {
                            tools = new LayoutEditorTools(layoutEditor);
                        }
                        tools.setSignalMastsAtSlipFromMenu((LayoutSlip) instance, boundaryBetween, layoutEditor.signalFrame);
                    }
                });
                popup.add(new AbstractAction(rb.getString("SetSensors")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (tools == null) {
                            tools = new LayoutEditorTools(layoutEditor);
                        }
                        tools.setSensorsAtSlipFromMenu((LayoutSlip) instance, boundaryBetween, layoutEditor.sensorIconEditor, layoutEditor.sensorFrame);
                    }
                });
            }

            if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                if (blockAssigned) {
                    popup.add(new AbstractAction(rb.getString("ViewBlockRouting")) {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlock());
                            routeTableAction.actionPerformed(event);
                        }
                    });
                }
            }
            setAdditionalEditPopUpMenu(popup);
            layoutEditor.setShowAlignmentMenu(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
        } else if (!viewAdditionalMenu.isEmpty()) {
            setAdditionalViewPopUpMenu(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public String[] getBlockBoundaries() {
        final String[] boundaryBetween = new String[4];

        if ((blockName != null) && (!blockName.equals("")) && (block != null)) {
            if ((connectA instanceof TrackSegment) && (((TrackSegment) connectA).getLayoutBlock() != block)) {
                try {
                    boundaryBetween[0] = (((TrackSegment) connectA).getLayoutBlock().getDisplayName() + " - " + block.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection A doesn't contain a layout block");
                }
            }
            if ((connectC instanceof TrackSegment) && (((TrackSegment) connectC).getLayoutBlock() != block)) {
                try {
                    boundaryBetween[2] = (((TrackSegment) connectC).getLayoutBlock().getDisplayName() + " - " + block.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection C doesn't contain a layout block");
                }
            }
            if ((connectB instanceof TrackSegment) && (((TrackSegment) connectB).getLayoutBlock() != block)) {
                try {
                    boundaryBetween[1] = (((TrackSegment) connectB).getLayoutBlock().getDisplayName() + " - " + block.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection B doesn't contain a layout block");
                }
            }
            if ((connectD instanceof TrackSegment) && (((TrackSegment) connectD).getLayoutBlock() != block)) {
                try {
                    boundaryBetween[3] = (((TrackSegment) connectD).getLayoutBlock().getDisplayName() + " - " + block.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection D doesn't contain a layout block");
                }
            }
        }
        return boundaryBetween;
    }

    // variables for Edit slip Crossing pane
    JButton slipEditDone;
    JButton slipEditCancel;
    JButton turnoutEditBlock;
    boolean editOpen = false;
    private JmriBeanComboBox turnoutAComboBox;
    private JmriBeanComboBox turnoutBComboBox;
    private JCheckBox hiddenBox = new JCheckBox(rb.getString("HideSlip"));

    /**
     * Edit a Slip
     */
    protected void editLayoutSlip(LayoutTurnout o) {
        if (editOpen) {
            editLayoutTurnoutFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (editLayoutTurnoutFrame == null) {
            editLayoutTurnoutFrame = new JmriJFrame(rb.getString("EditSlip"), false, true);
            editLayoutTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.EditLayoutSlip", true);
            editLayoutTurnoutFrame.setLocation(50, 30);
            Container contentPane = editLayoutTurnoutFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel turnoutNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " A " + Bundle.getMessage("Name"));
            turnoutAComboBox = new JmriBeanComboBox(
                InstanceManager.turnoutManagerInstance(), getTurnout(), JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
            panel1.add(turnoutNameLabel);
            panel1.add(turnoutAComboBox);
            contentPane.add(panel1);
            JPanel panel1a = new JPanel();
            panel1a.setLayout(new FlowLayout());
            JLabel turnoutBNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " B " + Bundle.getMessage("Name"));
            turnoutBComboBox = new JmriBeanComboBox(
                InstanceManager.turnoutManagerInstance(), getTurnoutB(), JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
            panel1a.add(turnoutBNameLabel);
            panel1a.add(turnoutBComboBox);
            contentPane.add(panel1a);
            JPanel panel2 = new JPanel();
            panel2.setLayout(new GridLayout(0, 3, 2, 2));

            panel2.add(new Label("   "));
            panel2.add(new Label(Bundle.getMessage("BeanNameTurnout") + " A:"));
            panel2.add(new Label(Bundle.getMessage("BeanNameTurnout") + " B:"));
            for (Entry<Integer, TurnoutState> ts : turnoutStates.entrySet()) {
                SampleStates draw = new SampleStates(ts.getKey());
                draw.repaint();
                draw.setPreferredSize(new Dimension(40, 40));
                panel2.add(draw);

                panel2.add(ts.getValue().getComboA());
                panel2.add(ts.getValue().getComboB());
            }

            testPanel = new TestState();
            testPanel.setSize(40, 40);
            testPanel.setPreferredSize(new Dimension(40, 40));
            panel2.add(testPanel);
            JButton testButton = new JButton("Test");
            testButton.addActionListener((ActionEvent e) -> {
                toggleStateTest();
            });
            panel2.add(testButton);
            contentPane.add(panel2);

            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            hiddenBox.setToolTipText(rb.getString("HiddenToolTip"));
            panel33.add(hiddenBox);
            contentPane.add(panel33);

            // setup block name
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel block1NameLabel = new JLabel(rb.getString("BlockID"));
            panel3.add(block1NameLabel);
            panel3.add(blockNameComboBox);
            if (true) {
                layoutEditor.setupComboBox(blockNameComboBox, false, true);
            } else {
                blockNameComboBox.setEditable(true);
                blockNameComboBox.getEditor().setItem("");
                blockNameComboBox.setSelectedIndex(-1);
            }
            blockNameComboBox.setToolTipText(rb.getString("EditBlockNameHint"));

            contentPane.add(panel3);
            // set up Edit Block buttons
            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
            // Edit Block
            panel4.add(turnoutEditBlock = new JButton(Bundle.getMessage("EditBlock", "")));
            turnoutEditBlock.addActionListener(
                (ActionEvent event) -> {
                    turnoutEditBlockPressed(event);
                }
            );
            turnoutEditBlock.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1

            contentPane.add(panel4);
            // set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(slipEditDone = new JButton(Bundle.getMessage("ButtonDone")));

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(
                () -> {
                    JRootPane rootPane = SwingUtilities.getRootPane(slipEditDone);
                    rootPane.setDefaultButton(slipEditDone);
                }
            );

            slipEditDone.addActionListener(
                (ActionEvent event) -> {
                    slipEditDonePressed(event);
                }
            );
            slipEditDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            // Cancel
            panel5.add(slipEditCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            slipEditCancel.addActionListener(
                (ActionEvent event) -> {
                    slipEditCancelPressed(event);
                }
            );
            slipEditCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            contentPane.add(panel5);
        }

        hiddenBox.setSelected(hidden);

        // Set up for Edit
        blockNameComboBox.getEditor().setItem(blockName);

        editLayoutTurnoutFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                slipEditCancelPressed(null);
            }
        });
        editLayoutTurnoutFrame.pack();
        editLayoutTurnoutFrame.setVisible(true);
        editOpen = true;
        needsBlockUpdate = false;
    }

    void drawSlipState(Graphics2D g2, int state) {
        int ctrX = 20;
        int ctrY = 20;
        Point2D ldispA = new Point2D.Double(-20.0, 0.0);
        Point2D ldispB = new Point2D.Double(-14.0, 14.0);
        g2.setColor(Color.black);

        Point2D A = new Point2D.Double(ctrX + ldispA.getX(), ctrY + ldispA.getY());
        Point2D B = new Point2D.Double(ctrX + ldispB.getX(), ctrY + ldispB.getY());
        Point2D C = new Point2D.Double(ctrX - ldispA.getX(), ctrY - ldispA.getY());
        Point2D D = new Point2D.Double(ctrX - ldispB.getX(), ctrY - ldispB.getY());

        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND));

        g2.draw(new Line2D.Double(A, third(A, C)));
        g2.draw(new Line2D.Double(C, third(C, A)));

        if (state == STATE_AC || state == STATE_BD || state == UNKNOWN) {
            g2.draw(new Line2D.Double(A, third(A, D)));

            g2.draw(new Line2D.Double(D, third(D, A)));

            if (getSlipType() == DOUBLE_SLIP) {
                g2.draw(new Line2D.Double(B, third(B, C)));

                g2.draw(new Line2D.Double(C, third(C, B)));
            }
        } else {
            g2.draw(new Line2D.Double(B, third(B, D)));
            g2.draw(new Line2D.Double(D, third(D, B)));
        }

        if (getSlipType() == DOUBLE_SLIP) {
            if (state == STATE_AC) {
                g2.draw(new Line2D.Double(B, third(B, D)));
                g2.draw(new Line2D.Double(D, third(D, B)));

                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, C));

            } else if (state == STATE_BD) {
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B, D));

            } else if (state == STATE_AD) {
                g2.draw(new Line2D.Double(B, third(B, C)));

                g2.draw(new Line2D.Double(C, third(C, B)));

                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, D));

            } else if (state == STATE_BC) {

                g2.draw(new Line2D.Double(A, third(A, D)));

                g2.draw(new Line2D.Double(D, third(D, A)));
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B, C));
            } else {
                g2.draw(new Line2D.Double(B, third(B, D)));
                g2.draw(new Line2D.Double(D, third(D, B)));
            }
        } else {
            g2.draw(new Line2D.Double(A, third(A, D)));

            g2.draw(new Line2D.Double(D, third(D, A)));
            if (state == STATE_AD) {
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, D));

            } else if (state == STATE_AC) {
                g2.draw(new Line2D.Double(B, third(B, D)));
                g2.draw(new Line2D.Double(D, third(D, B)));

                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, C));

            } else if (state == STATE_BD) {
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B, D));

            } else {
                g2.draw(new Line2D.Double(B, third(B, D)));
                g2.draw(new Line2D.Double(D, third(D, B)));
            }
        }

    }

    class SampleStates extends JPanel {

        // Methods, constructors, fields.
        SampleStates(int state) {
            super();
            this.state = state;
        }
        int state;

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);    // paints background
            Graphics2D g2 = (Graphics2D) g;
            drawSlipState(g2, state);
        }
    }

    int testState = UNKNOWN;

    /**
     * Toggle slip states if clicked on, physical turnout exists, and not
     * disabled
     */
    public void toggleStateTest() {
        int turnAState;
        int turnBState;
        switch (testState) {
            case STATE_AC: {
                if (type == SINGLE_SLIP) {
                    testState = STATE_BD;
                } else {
                    testState = STATE_BD;
                }
                break;
            }

            case STATE_BD: {
                testState = STATE_AD;
                break;
            }

            case STATE_AD: {
                if (type == SINGLE_SLIP) {
                    testState = STATE_AC;
                } else {
                    testState = STATE_BC;
                }
                break;
            }

            case STATE_BC: {
                testState = STATE_AC;
                break;
            }

            default: {
                testState = STATE_BD;
                break;
            }
        }
        turnAState = turnoutStates.get(testState).getTestTurnoutAState();
        turnBState = turnoutStates.get(testState).getTestTurnoutBState();

        ((Turnout) turnoutAComboBox.getSelectedBean()).setCommandedState(turnAState);
        ((Turnout) turnoutBComboBox.getSelectedBean()).setCommandedState(turnBState);
        /*if(getTurnout()!=null)
         getTurnout().setCommandedState(turnAState);
         if(getTurnoutB()!=null)
         getTurnoutB().setCommandedState(turnBState);*/
        if (testPanel != null) {
            testPanel.repaint();
        }
    }

    class TestState extends JPanel {

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            drawSlipState(g2, testState);
        }
    }

    TestState testPanel;

    void slipEditDonePressed(ActionEvent a) {
        String newName = turnoutAComboBox.getEditor().getItem().toString();
        if (-1 != turnoutAComboBox.getSelectedIndex()) {
            newName = turnoutAComboBox.getSelectedDisplayName();
        } else {
            newName = (null != newName) ? newName.trim() : "";
        }
        if (!turnoutName.equals(newName)) {
            if (layoutEditor.validatePhysicalTurnout(newName, editLayoutTurnoutFrame)) {
                setTurnout(newName);
            } else {
                namedTurnout = null;
                turnoutName = "";
            }
            needRedraw = true;
        }
        newName = turnoutBComboBox.getEditor().getItem().toString();
        if (-1 != turnoutBComboBox.getSelectedIndex()) {
            newName = turnoutBComboBox.getSelectedDisplayName();
        } else {
            newName = (null != newName) ? newName.trim() : "";
        }
        if (!turnoutBName.equals(newName)) {
            if (layoutEditor.validatePhysicalTurnout(newName,
                    editLayoutTurnoutFrame)) {
                setTurnoutB(newName);
            } else {
                namedTurnoutB = null;
                turnoutBName = "";
            }
            needRedraw = true;
        }

        newName = blockNameComboBox.getEditor().getItem().toString();
        if (-1 != blockNameComboBox.getSelectedIndex()) {
            newName = blockNameComboBox.getSelectedDisplayName();
        } else {
            newName = (null != newName) ? newName.trim() : "";
        }
        if (!blockName.equals(newName)) {
            // block 1 has changed, if old block exists, decrement use
            if ((block != null)) {
                block.decrementUse();
            }
            // get new block, or null if block has been removed
            blockName = newName;

            try {
                block = layoutEditor.provideLayoutBlock(blockName);
            } catch (IllegalArgumentException ex) {
                blockName = "";
                blockNameComboBox.getEditor().setItem("");
                blockNameComboBox.setSelectedIndex(-1);
            }
            needRedraw = true;
            layoutEditor.auxTools.setBlockConnectivityChanged();
            needsBlockUpdate = true;
        }
        for (TurnoutState ts : turnoutStates.values()) {
            ts.updateStatesFromCombo();
        }

        // set hidden
        boolean oldHidden = hidden;
        hidden = hiddenBox.isSelected();
        if (oldHidden != hidden) {
            needRedraw = true;
        }

        editOpen = false;
        editLayoutTurnoutFrame.setVisible(false);
        editLayoutTurnoutFrame.dispose();
        editLayoutTurnoutFrame = null;
        if (needsBlockUpdate) {
            updateBlockInfo();
        }
        if (needRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }

    void slipEditCancelPressed(ActionEvent a) {
        editOpen = false;
        editLayoutTurnoutFrame.setVisible(false);
        editLayoutTurnoutFrame.dispose();
        editLayoutTurnoutFrame = null;
        if (needsBlockUpdate) {
            updateBlockInfo();
        }
        if (needRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove()
     */
    @Override
    void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
    }

    /**
     * Removes this object from display and persistance
     */
    @Override
    void remove() {

        disableSML(getSignalAMast());
        disableSML(getSignalBMast());
        disableSML(getSignalCMast());
        disableSML(getSignalDMast());
        removeSML(getSignalAMast());
        removeSML(getSignalBMast());
        removeSML(getSignalCMast());
        removeSML(getSignalDMast());
        // remove from persistance by flagging inactive
        active = false;
    }

    void disableSML(SignalMast signalMast) {
        if (signalMast == null) {
            return;
        }
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).disableLayoutEditorUse(signalMast);
    }

    boolean active = true;

    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    @Override
    public boolean isActive() {
        return active;
    }

    public boolean singleSlipStraightEqual() {
        if (type != SINGLE_SLIP) {
            return false;
        }
        return turnoutStates.get(STATE_AC).equals(turnoutStates.get(STATE_BD));
    }

    Hashtable<Integer, TurnoutState> turnoutStates = new Hashtable<Integer, TurnoutState>(4);

    public int getTurnoutState(Turnout turn, int state) {
        if (turn == getTurnout()) {
            return getTurnoutState(state);
        }
        return getTurnoutBState(state);
    }

    public int getTurnoutState(int state) {
        return turnoutStates.get(Integer.valueOf(state)).getTurnoutAState();
    }

    public int getTurnoutBState(int state) {
        return turnoutStates.get(Integer.valueOf(state)).getTurnoutBState();
    }

    public void setTurnoutStates(int state, String turnStateA, String turnStateB) {
        if (!turnoutStates.containsKey(state)) {
            log.error("Trying to set invalid state for slip " + getDisplayName());
            return;
        }
        turnoutStates.get(state).setTurnoutAState(Integer.valueOf(turnStateA));
        turnoutStates.get(state).setTurnoutBState(Integer.valueOf(turnStateB));
    }

    //Internal call to update the state of the slip depending upon the turnout states.
    void updateState() {
        int state_a = getTurnout().getKnownState();
        int state_b = getTurnoutB().getKnownState();
        for (Entry<Integer, TurnoutState> en : turnoutStates.entrySet()) {
            if (en.getValue().getTurnoutAState() == state_a) {
                if (en.getValue().getTurnoutBState() == state_b) {
                    currentState = en.getKey();
                    layoutEditor.redrawPanel();
                    return;
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        if (!isHidden() || layoutEditor.isEditable()) {
            Point2D pointA = getCoordsA();
            Point2D pointB = getCoordsB();
            Point2D pointC = getCoordsC();
            Point2D pointD = getCoordsD();

            Color mainColour;
            Color subColour;
            LayoutBlock b = getLayoutBlock();
            if (b != null) {
                mainColour = b.getBlockColor();
                subColour = b.getBlockTrackColor();
            } else {
                mainColour = defaultTrackColor;
                subColour = defaultTrackColor;
            }

            float w = layoutEditor.setTrackStrokeWidth(g2, isMainline());

            g2.setColor(subColour);

            g2.draw(new Line2D.Double(pointA, third(pointA, pointC)));
            g2.draw(new Line2D.Double(pointC, third(pointC, pointA)));

            g2.draw(new Line2D.Double(pointB, third(pointB, pointD)));
            g2.draw(new Line2D.Double(pointD, third(pointD, pointB)));

            if (getSlipType() == DOUBLE_SLIP) {
                if (getSlipState() == STATE_AC) {
                    g2.draw(new Line2D.Double(pointA, third(pointA, pointD)));
                    g2.draw(new Line2D.Double(pointD, third(pointD, pointA)));
                    g2.draw(new Line2D.Double(pointB, third(pointB, pointC)));
                    g2.draw(new Line2D.Double(pointC, third(pointC, pointB)));

                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(pointA, pointC));

                } else if (getSlipState() == STATE_BD) {
                    g2.draw(new Line2D.Double(pointB, third(pointB, pointC)));
                    g2.draw(new Line2D.Double(pointC, third(pointC, pointB)));
                    g2.draw(new Line2D.Double(pointA, third(pointA, pointD)));
                    g2.draw(new Line2D.Double(pointD, third(pointD, pointA)));

                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(pointB, pointD));

                } else if (getSlipState() == STATE_AD) {
                    g2.draw(new Line2D.Double(pointB, third(pointB, pointC)));
                    g2.draw(new Line2D.Double(pointC, third(pointC, pointB)));

                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(pointA, pointD));
                } else if (getSlipState() == STATE_BC) {
                    g2.draw(new Line2D.Double(pointA, third(pointA, pointD)));
                    g2.draw(new Line2D.Double(pointD, third(pointD, pointA)));

                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(pointB, pointC));
                } else {
                    g2.draw(new Line2D.Double(pointB, third(pointB, pointC)));
                    g2.draw(new Line2D.Double(pointC, third(pointC, pointB)));
                    g2.draw(new Line2D.Double(pointA, third(pointA, pointD)));
                    g2.draw(new Line2D.Double(pointD, third(pointD, pointA)));
                }
            } else {
                g2.draw(new Line2D.Double(pointA, third(pointA, pointD)));
                g2.draw(new Line2D.Double(pointD, third(pointD, pointA)));
                if (getSlipState() == STATE_AD) {
                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(pointA, pointD));
                } else if (getSlipState() == STATE_BD) {
                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(pointB, pointD));
                    if (singleSlipStraightEqual()) {
                        g2.setColor(mainColour);
                        g2.draw(new Line2D.Double(pointA, pointC));
                    }
                } else if (getSlipState() == STATE_AC) {
                    g2.setColor(mainColour);
                    g2.draw(new Line2D.Double(pointA, pointC));
                    if (singleSlipStraightEqual()) {
                        g2.setColor(mainColour);
                        g2.draw(new Line2D.Double(pointB, pointD));
                    }
                } else {
                    g2.draw(new Line2D.Double(pointA, third(pointA, pointD)));
                    g2.draw(new Line2D.Double(pointD, third(pointD, pointA)));
                }
            }
        }   // if (!(getHidden() && !layoutEditor.isEditable()))
    }

    public void drawSlipCircles(Graphics2D g2) {
        Double circleRadius = controlPointSize * layoutEditor.getTurnoutCircleSize();
        Point2D leftCenter = midpoint(getCoordsA(), getCoordsB());
        Double leftFract = circleRadius / center.distance(leftCenter);
        Point2D leftCircleCenter = lerp(center, leftCenter, leftFract);
        g2.draw(layoutEditor.turnoutCircleAt(leftCircleCenter));

        Point2D rightCenter = midpoint(getCoordsC(), getCoordsD());
        Double rightFract = circleRadius / center.distance(rightCenter);
        Point2D rightCircleCenter = lerp(center, rightCenter, rightFract);
        g2.draw(layoutEditor.turnoutCircleAt(rightCircleCenter));
    }

    public void drawSlipRect(Graphics2D g2) {
        // draw east/west turnout (control) circles
        Double circleRadius = controlPointSize * layoutEditor.getTurnoutCircleSize();

        Point2D leftCenter = midpoint(getCoordsA(), getCoordsB());
        Double leftFract = circleRadius / center.distance(leftCenter);
        Point2D leftCircleCenter = lerp(center, leftCenter, leftFract);
        g2.draw(layoutEditor.turnoutCircleAt(leftCircleCenter));

        Point2D rightCenter = midpoint(getCoordsC(), getCoordsD());
        Double rightFract = circleRadius / center.distance(rightCenter);
        Point2D rightCircleCenter = lerp(center, rightCenter, rightFract);
        g2.draw(layoutEditor.turnoutCircleAt(rightCircleCenter));

        Point2D pt = getCoordsA();
        if (getConnectA() == null) {
            g2.setColor(Color.magenta);
        } else {
            g2.setColor(Color.blue);
        }
        g2.draw(layoutEditor.controlPointRectAt(pt));

        pt = getCoordsB();
        if (getConnectB() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.controlPointRectAt(pt));

        pt = getCoordsC();
        if (getConnectC() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.controlPointRectAt(pt));

        pt = getCoordsD();
        if (getConnectD() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.controlPointRectAt(pt));
    }   // public void drawSlipRects(Graphics2D g2)

    static class TurnoutState {

        int turnoutA = Turnout.CLOSED;
        int turnoutB = Turnout.CLOSED;
        JComboBox<String> turnoutABox;
        JComboBox<String> turnoutBBox;

        TurnoutState(int turnoutA, int turnoutB) {
            this.turnoutA = turnoutA;
            this.turnoutB = turnoutB;
        }

        int getTurnoutAState() {
            return turnoutA;
        }

        int getTurnoutBState() {
            return turnoutB;
        }

        void setTurnoutAState(int state) {
            turnoutA = state;
        }

        void setTurnoutBState(int state) {
            turnoutB = state;
        }

        JComboBox<String> getComboA() {
            if (turnoutABox == null) {
                String state[] = new String[]{InstanceManager.turnoutManagerInstance().getClosedText(),
                    InstanceManager.turnoutManagerInstance().getThrownText()};
                turnoutABox = new JComboBox<String>(state);
                if (turnoutA == Turnout.THROWN) {
                    turnoutABox.setSelectedIndex(1);
                }
            }
            return turnoutABox;
        }

        JComboBox<String> getComboB() {
            if (turnoutBBox == null) {
                String state[] = new String[]{InstanceManager.turnoutManagerInstance().getClosedText(),
                    InstanceManager.turnoutManagerInstance().getThrownText()};
                turnoutBBox = new JComboBox<String>(state);
                if (turnoutB == Turnout.THROWN) {
                    turnoutBBox.setSelectedIndex(1);
                }
            }
            return turnoutBBox;
        }

        int getTestTurnoutAState() {
            if (turnoutABox.getSelectedIndex() == 0) {
                return Turnout.CLOSED;
            }
            return Turnout.THROWN;
        }

        int getTestTurnoutBState() {
            if (turnoutBBox.getSelectedIndex() == 0) {
                return Turnout.CLOSED;
            }
            return Turnout.THROWN;
        }

        void updateStatesFromCombo() {
            if (turnoutABox == null || turnoutBBox == null) {
                return;
            }
            if (turnoutABox.getSelectedIndex() == 0) {
                turnoutA = Turnout.CLOSED;
            } else {
                turnoutA = Turnout.THROWN;
            }
            if (turnoutBBox.getSelectedIndex() == 0) {
                turnoutB = Turnout.CLOSED;
            } else {
                turnoutB = Turnout.THROWN;
            }
        }

        boolean equals(TurnoutState ts) {
            if (ts.getTurnoutAState() != this.getTurnoutAState()) {
                return false;
            }
            if (ts.getTurnoutBState() != this.getTurnoutBState()) {
                return false;
            }
            return true;
        }

    }

    private final static Logger log = LoggerFactory.getLogger(LayoutSlip.class.getName());
}
