package jmri.jmrit.display.layoutEditor;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TrackSegment is a segment of track on a layout linking two nodes of the
 * layout. A node may be a LayoutTurnout, a LevelXing or a PositionablePoint.
 * <P>
 * PositionablePoints have 1 or 2 connection points. LayoutTurnouts have 3 or 4
 * (crossovers) connection points, designated A, B, C, and D. LevelXing's have 4
 * connection points, designated A, B, C, and D.
 * <P>
 * TrackSegments carry the connectivity information between the three types of
 * nodes. Track Segments serve as the lines in a graph which shows layout
 * connectivity. For the connectivity graph to be valid, all connections between
 * nodes must be via TrackSegments.
 * <P>
 * TrackSegments carry Block information, as do LayoutTurnouts and LevelXings.
 * <P>
 * TrackSegments may be drawn as dashed lines or solid lines. In addition
 * TrackSegments may be hidden when the panel is not in EditMode.
 *
 * @author Dave Duchamp Copyright (c) 2004-2009
 */
public class TrackSegment {

    // Defined text resource
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    // defined constants
    // operational instance variables (not saved between sessions)
    private LayoutBlock block = null;
    private TrackSegment instance = null;
    private LayoutEditor layoutEditor = null;

    // persistent instances variables (saved between sessions)
    private String ident = "";
    private String blockName = "";
    private Object connect1 = null;
    private int type1 = 0;
    private Object connect2 = null;
    private int type2 = 0;
    private boolean dashed = false;
    private boolean mainline = false;
    private boolean hidden = false;
    private boolean arc = false;
    private boolean flip = false;
    private double angle = 0.0D;
    private boolean circle = false;
    private boolean changed = false;

    public TrackSegment(String id, Object c1, int t1, Object c2, int t2, boolean dash,
            boolean main, LayoutEditor myPanel) {
        layoutEditor = myPanel;
        // validate input
        if ((c1 == null) || (c2 == null)) {
            log.error("Invalid object in TrackSegment constructor call - " + id);
        }
        connect1 = c1;
        connect2 = c2;
        if ((t1 < LayoutEditor.POS_POINT)
                || (((t1 > LayoutEditor.LEVEL_XING_D) && (t1 < LayoutEditor.SLIP_A))
                || ((t1 > LayoutEditor.SLIP_D) && (t1 < LayoutEditor.TURNTABLE_RAY_OFFSET)))) {
            log.error("Invalid connect type 1 in TrackSegment constructor - " + id);
        } else {
            type1 = t1;
        }
        if ((t2 < LayoutEditor.POS_POINT)
                || (((t2 > LayoutEditor.LEVEL_XING_D) && (t2 < LayoutEditor.SLIP_A))
                || ((t2 > LayoutEditor.SLIP_D) && (t2 < LayoutEditor.TURNTABLE_RAY_OFFSET)))) {
            log.error("Invalid connect type 2 in TrackSegment constructor - " + id);
        } else {
            type2 = t2;
        }
        instance = this;
        ident = id;
        dashed = dash;
        mainline = main;
        arc = false;
        flip = false;
        angle = 0.0D;
        circle = false;
    }

    // alternate constructor for loading layout editor panels
    public TrackSegment(String id, String c1Name, int t1, String c2Name, int t2, boolean dash,
            boolean main, boolean hide, LayoutEditor myPanel) {
        layoutEditor = myPanel;
        tConnect1Name = c1Name;
        type1 = t1;
        tConnect2Name = c2Name;
        type2 = t2;
        instance = this;
        ident = id;
        dashed = dash;
        mainline = main;
        hidden = hide;
    }

    /**
     * Accessor methods
     */
    public String getID() {
        return ident;
    }

    public String getBlockName() {
        return blockName;
    }

    public int getType1() {
        return type1;
    }

    public int getType2() {
        return type2;
    }

    public Object getConnect1() {
        return connect1;
    }

    public Object getConnect2() {
        return connect2;
    }

    protected void setNewConnect1(Object o, int type) {
        connect1 = o;
        type1 = type;

    }

    protected void setNewConnect2(Object o, int type) {
        connect2 = o;
        type2 = type;
    }

    public boolean getDashed() {
        return dashed;
    }

    public void setDashed(boolean dash) {
        dashed = dash;
    }

    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean hide) {
        hidden = hide;
    }

    public boolean getMainline() {
        return mainline;
    }

    public void setMainline(boolean main) {
        mainline = main;
    }

    public boolean getArc() {
        return arc;
    }

    public void setArc(boolean boo) {
        arc = boo;
        changed = true;
    }

    public boolean getCircle() {
        return circle;
    }

    public void setCircle(boolean boo) {
        circle = boo;
        changed = true;
    }

    public boolean getFlip() {
        return flip;
    }

    public void setFlip(boolean boo) {
        flip = boo;
        changed = true;
    }
    //public int getStartAngle() {return startangle;}
    //public void setStartAngle(int x) {startangle = x;} 

    public double getAngle() {
        return angle;
    }

    public void setAngle(double x) {
// GT 8-OCT-2009 ==== Changed arcs maths : Start
//        if (angle>180) // ???
        if (x > 180.0D) {
            x = 180.0D;
        } else if (x < 0.0D) {
            x = 0.0D;
        }
// GT 8-OCT-2009 ==== Changed arcs maths : End
        angle = x;
        changed = true;
    }

    //This method is used to determine if we need to redraw a curved piece of track
    //It saves having to recalculate the circle details each time.
    public boolean trackNeedsRedraw() {
        return changed;
    }

    public void trackRedrawn() {
        changed = false;
    }

    public LayoutBlock getLayoutBlock() {
        if ((block == null) && (blockName != null) && (blockName != "")) {
            block = layoutEditor.provideLayoutBlock(blockName);
        }
        return block;
    }

    public String getConnect1Name() {
        return getConnectName(connect1, type1);
    }

    public String getConnect2Name() {
        return getConnectName(connect2, type2);
    }

    private String getConnectName(Object o, int type) {
        if (type == LayoutEditor.POS_POINT) {
            return ((PositionablePoint) o).getID();
        }
        if ((type == LayoutEditor.TURNOUT_A) || (type == LayoutEditor.TURNOUT_B)
                || (type == LayoutEditor.TURNOUT_C) || (type == LayoutEditor.TURNOUT_D)) {
            return ((LayoutTurnout) o).getName();
        }
        if ((type == LayoutEditor.LEVEL_XING_A) || (type == LayoutEditor.LEVEL_XING_B)
                || (type == LayoutEditor.LEVEL_XING_C) || (type == LayoutEditor.LEVEL_XING_D)) {
            return ((LevelXing) o).getID();
        }
        if ((type == LayoutEditor.SLIP_A) || (type == LayoutEditor.SLIP_B)
                || (type == LayoutEditor.SLIP_C) || (type == LayoutEditor.SLIP_D)) {
            return ((LayoutSlip) o).getName();
        }
        if (type >= LayoutEditor.TURNTABLE_RAY_OFFSET) {
            return ((LayoutTurntable) o).getID();
        }
        return "";
    }

    // initialization instance variables (used when loading a LayoutEditor)
    public String tBlockName = "";
    public String tConnect1Name = "";
    public String tConnect2Name = "";

    /**
     * Initialization method The above variables are initialized by
     * PositionablePointXml, then the following method is called after the
     * entire LayoutEditor is loaded to set the specific TrackSegment objects.
     */
    public void setObjects(LayoutEditor p) {
        if (tBlockName.length() > 0) {
            block = p.getLayoutBlock(tBlockName);
            if (block != null) {
                blockName = tBlockName;
                block.incrementUse();
            } else {
                log.error("bad blockname '" + tBlockName + "' in tracksegment " + ident);
            }
        }
        connect1 = p.getFinder().findObjectByTypeAndName(type1, tConnect1Name);
        connect2 = p.getFinder().findObjectByTypeAndName(type2, tConnect2Name);
    }

    /**
     * Set Up a Layout Block for a Track Segment
     */
    public void setLayoutBlock(LayoutBlock b) {
        block = b;
        if (b != null) {
            blockName = b.getID();
        }
    }

    public void setLayoutBlockByName(String name) {
        blockName = name;
    }

    protected void updateBlockInfo() {
        if (block != null) {
            block.updatePaths();
        }
        LayoutBlock b1 = getBlock(connect1, type1);
        if ((b1 != null) && (b1 != block)) {
            b1.updatePaths();
        }
        LayoutBlock b2 = getBlock(connect2, type2);
        if ((b2 != null) && (b2 != block) && (b2 != b1)) {
            b2.updatePaths();
        }
        if (getConnect1() instanceof PositionablePoint) {
            ((PositionablePoint) getConnect1()).reCheckBlockBoundary();
        } else if (getConnect1() instanceof LayoutTurnout) {
            ((LayoutTurnout) getConnect1()).reCheckBlockBoundary();
        } else if (getConnect1() instanceof LevelXing) {
            ((LevelXing) getConnect1()).reCheckBlockBoundary();
        } else if (getConnect1() instanceof LayoutSlip) {
            ((LayoutSlip) getConnect1()).reCheckBlockBoundary();
        }

        if (getConnect2() instanceof PositionablePoint) {
            ((PositionablePoint) getConnect2()).reCheckBlockBoundary();
        } else if (getConnect2() instanceof LayoutTurnout) {
            ((LayoutTurnout) getConnect2()).reCheckBlockBoundary();
        } else if (getConnect2() instanceof LevelXing) {
            ((LevelXing) getConnect2()).reCheckBlockBoundary();
        } else if (getConnect2() instanceof LayoutSlip) {
            ((LayoutSlip) getConnect2()).reCheckBlockBoundary();
        }
    }

    private LayoutBlock getBlock(Object connect, int type) {
        if (connect == null) {
            return null;
        }
        if (type == LayoutEditor.POS_POINT) {
            PositionablePoint p = (PositionablePoint) connect;
            if (p.getConnect1() != instance) {
                if (p.getConnect1() != null) {
                    return (p.getConnect1().getLayoutBlock());
                } else {
                    return null;
                }
            } else {
                if (p.getConnect2() != null) {
                    return (p.getConnect2().getLayoutBlock());
                } else {
                    return null;
                }
            }
        } else {
            return (layoutEditor.getAffectedBlock(connect, type));
        }
    }

    JPopupMenu popup = null;

    /**
     * Display popup menu for information and editing
     */
    protected void showPopUp(MouseEvent e) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        if (!dashed) {
            popup.add(rb.getString("Style") + " - " + rb.getString("Solid"));
        } else {
            popup.add(rb.getString("Style") + " - " + rb.getString("Dashed"));
        }
        if (!mainline) {
            popup.add(rb.getString("NotMainline"));
        } else {
            popup.add(rb.getString("Mainline"));
        }
        if (blockName.equals("")) {
            popup.add(rb.getString("NoBlock"));
        } else {
            popup.add(rb.getString("Block") + ": " + getLayoutBlock().getID());
        }
        if (hidden) {
            popup.add(rb.getString("Hidden"));
        } else {
            popup.add(rb.getString("NotHidden"));
        }
        popup.add(new JSeparator(JSeparator.HORIZONTAL));
        popup.add(new AbstractAction(rb.getString("Edit")) {
            /**
             *
             */
            private static final long serialVersionUID = -8434155343805889256L;

            public void actionPerformed(ActionEvent e) {
                editTrackSegment();
            }
        });
        popup.add(new AbstractAction(rb.getString("Remove")) {
            /**
             *
             */
            private static final long serialVersionUID = -5403313571121888412L;

            public void actionPerformed(ActionEvent e) {
                layoutEditor.removeTrackSegment(instance);
                remove();
                dispose();
            }
        });
        JMenu lineType = new JMenu(rb.getString("ChangeTo"));
        lineType.add(new AbstractAction(rb.getString("Line")) {
            /**
             *
             */
            private static final long serialVersionUID = -2124868806018661728L;

            public void actionPerformed(ActionEvent e) {
                changeType(0);
            }
        });
        lineType.add(new AbstractAction(rb.getString("Circle")) {
            /**
             *
             */
            private static final long serialVersionUID = -481775061303245869L;

            public void actionPerformed(ActionEvent e) {
                changeType(1);
            }
        });
        lineType.add(new AbstractAction(rb.getString("Elipse")) {
            /**
             *
             */
            private static final long serialVersionUID = -2192546838539431871L;

            public void actionPerformed(ActionEvent e) {
                changeType(2);
            }
        });
        popup.add(lineType);
        if (getArc()) {
            popup.add(new AbstractAction(rb.getString("FlipAngle")) {
                /**
                 *
                 */
                private static final long serialVersionUID = -5467146687460909227L;

                public void actionPerformed(ActionEvent e) {
                    flipAngle();
                }
            });
            if (hideConstructionLines()) {
                popup.add(new AbstractAction(rb.getString("ShowConstruct")) {
                    /**
                     *
                     */
                    private static final long serialVersionUID = -3681445953384400565L;

                    public void actionPerformed(ActionEvent e) {
                        hideConstructionLines(SHOWCON);
                    }
                });
            } else {
                popup.add(new AbstractAction(rb.getString("HideConstruct")) {
                    /**
                     *
                     */
                    private static final long serialVersionUID = -5218884649785670786L;

                    public void actionPerformed(ActionEvent e) {
                        hideConstructionLines(HIDECON);
                    }
                });
            }
        }
        if ((!blockName.equals("")) && (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled())) {
            popup.add(new AbstractAction(rb.getString("ViewBlockRouting")) {
                /**
                 *
                 */
                private static final long serialVersionUID = -2604285207958381230L;

                public void actionPerformed(ActionEvent e) {
                    AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlock());
                    routeTableAction.actionPerformed(e);
                }
            });
        }
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    void changeType(int choice) {
        switch (choice) {
            case 0:
                setArc(false);
                setAngle(0.0D);
                setCircle(false);
                break;
            case 1:
                setArc(true);
                setAngle(90.0D);
                setCircle(true);
                break;
            case 2:
                setArc(true);
                setAngle(90.0D);
                setCircle(false);
                break;
            default:
                break;
        }
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
    }

    void flipAngle() {
        if (getFlip()) {
            setFlip(false);
        } else {
            setFlip(true);
        }
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
    }

    // variables for Edit Track Segment pane
    private JmriJFrame editTrackSegmentFrame = null;
    private JComboBox<String> dashedBox = new JComboBox<String>();
    private int dashedIndex;
    private int solidIndex;
    private JComboBox<String> mainlineBox = new JComboBox<String>();
    private int mainlineTrackIndex;
    private int sideTrackIndex;
    private JTextField blockNameField = new JTextField(16);
    private JTextField arcField = new JTextField(5);
    private JCheckBox hiddenBox = new JCheckBox(rb.getString("HideTrack"));
    private JButton segmentEditBlock;
    private JButton segmentEditDone;
    private JButton segmentEditCancel;
    private boolean editOpen = false;
    private boolean needsRedraw = false;

    /**
     * Edit a Track Segment
     */
    protected void editTrackSegment() {
        if (editOpen) {
            editTrackSegmentFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (editTrackSegmentFrame == null) {
            editTrackSegmentFrame = new JmriJFrame(rb.getString("EditTrackSegment"), false, true);
            editTrackSegmentFrame.addHelpMenu("package.jmri.jmrit.display.EditTrackSegment", true);
            editTrackSegmentFrame.setLocation(50, 30);
            Container contentPane = editTrackSegmentFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            // add dashed choice 
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            dashedBox.removeAllItems();
            dashedBox.addItem(rb.getString("Solid"));
            solidIndex = 0;
            dashedBox.addItem(rb.getString("Dashed"));
            dashedIndex = 1;
            dashedBox.setToolTipText(rb.getString("DashedToolTip"));
            panel31.add(new JLabel(rb.getString("Style") + " : "));
            panel31.add(dashedBox);
            contentPane.add(panel31);
            // add mainline choice 
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            mainlineBox.removeAllItems();
            mainlineBox.addItem(rb.getString("Mainline"));
            mainlineTrackIndex = 0;
            mainlineBox.addItem(rb.getString("NotMainline"));
            sideTrackIndex = 1;
            mainlineBox.setToolTipText(rb.getString("MainlineToolTip"));
            panel32.add(mainlineBox);
            contentPane.add(panel32);
            // add hidden choice 
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            hiddenBox.setToolTipText(rb.getString("HiddenToolTip"));
            panel33.add(hiddenBox);
            contentPane.add(panel33);
            // setup block name
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel blockNameLabel = new JLabel(rb.getString("BlockID"));
            panel2.add(blockNameLabel);
            panel2.add(blockNameField);
            blockNameField.setToolTipText(rb.getString("EditBlockNameHint"));
            contentPane.add(panel2);
            if ((getArc()) && (getCircle())) {
                JPanel panel20 = new JPanel();
                panel20.setLayout(new FlowLayout());
                JLabel arcLabel = new JLabel("Set Arc Angle");
                panel20.add(arcLabel);
                panel20.add(arcField);
                arcField.setToolTipText("Set Arc Angle");
                contentPane.add(panel20);
                arcField.setText("" + getAngle());
            }
            // set up Edit Block, Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            // Edit Block
            panel5.add(segmentEditBlock = new JButton(rb.getString("EditBlock")));
            segmentEditBlock.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    segmentEditBlockPressed(e);
                }
            });
            segmentEditBlock.setToolTipText(rb.getString("EditBlockHint"));
            panel5.add(segmentEditDone = new JButton(rb.getString("Done")));
            segmentEditDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    segmentEditDonePressed(e);
                }
            });
            segmentEditDone.setToolTipText(rb.getString("DoneHint"));
            // Cancel
            panel5.add(segmentEditCancel = new JButton(rb.getString("Cancel")));
            segmentEditCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    segmentEditCancelPressed(e);
                }
            });
            segmentEditCancel.setToolTipText(rb.getString("CancelHint"));
            contentPane.add(panel5);
        }
        // Set up for Edit
        if (mainline) {
            mainlineBox.setSelectedIndex(mainlineTrackIndex);
        } else {
            mainlineBox.setSelectedIndex(sideTrackIndex);
        }
        if (dashed) {
            dashedBox.setSelectedIndex(dashedIndex);
        } else {
            dashedBox.setSelectedIndex(solidIndex);
        }
        hiddenBox.setSelected(hidden);
        blockNameField.setText(blockName);
        editTrackSegmentFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                segmentEditCancelPressed(null);
            }
        });
        editTrackSegmentFrame.pack();
        editTrackSegmentFrame.setVisible(true);
        editOpen = true;
    }

    void segmentEditBlockPressed(ActionEvent a) {
        // check if a block name has been entered
        if (!blockName.equals(blockNameField.getText().trim())) {
            // block has changed, if old block exists, decrement use
            if (block != null) {
                block.decrementUse();
            }
            // get new block, or null if block has been removed
            blockName = blockNameField.getText().trim();
            try {
                block = layoutEditor.provideLayoutBlock(blockName);
            } catch (IllegalArgumentException ex) {
                blockName = "";
            }
            needsRedraw = true;
            layoutEditor.auxTools.setBlockConnectivityChanged();
            updateBlockInfo();
        }
        // check if a block exists to edit
        if (block == null) {
            JOptionPane.showMessageDialog(editTrackSegmentFrame,
                    rb.getString("Error1"),
                    rb.getString("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        block.editLayoutBlock(editTrackSegmentFrame);
        layoutEditor.setDirty();
        needsRedraw = true;
    }

    void segmentEditDonePressed(ActionEvent a) {
        // set dashed
        boolean oldDashed = dashed;
        if (dashedBox.getSelectedIndex() == dashedIndex) {
            dashed = true;
        } else {
            dashed = false;
        }
        // set mainline
        boolean oldMainline = mainline;
        if (mainlineBox.getSelectedIndex() == mainlineTrackIndex) {
            mainline = true;
        } else {
            mainline = false;
        }
        // set hidden
        boolean oldHidden = hidden;
        hidden = hiddenBox.isSelected();
        if (getArc()) {
            //setAngle(Integer.parseInt(arcField.getText()));
            //needsRedraw = true;
            try {
                double newAngle = Double.parseDouble(arcField.getText());
                setAngle(newAngle);
                needsRedraw = true;
            } catch (NumberFormatException e) {
                arcField.setText("" + getAngle());
            }
        }
        // check if anything changed
        if ((oldDashed != dashed) || (oldMainline != mainline) || (oldHidden != hidden)) {
            needsRedraw = true;
        }
        // check if Block changed
        if (!blockName.equals(blockNameField.getText().trim())) {
            // block has changed, if old block exists, decrement use
            if (block != null) {
                block.decrementUse();
            }
            // get new block, or null if block has been removed
            blockName = blockNameField.getText().trim();
            try {
                block = layoutEditor.provideLayoutBlock(blockName);
            } catch (IllegalArgumentException ex) {
                blockName = "";
            }
            needsRedraw = true;
            layoutEditor.auxTools.setBlockConnectivityChanged();
            updateBlockInfo();
        }
        editOpen = false;
        editTrackSegmentFrame.setVisible(false);
        editTrackSegmentFrame.dispose();
        editTrackSegmentFrame = null;
        if (needsRedraw) {
            layoutEditor.redrawPanel();
        }
        layoutEditor.setDirty();
    }

    void segmentEditCancelPressed(ActionEvent a) {
        editOpen = false;
        editTrackSegmentFrame.setVisible(false);
        editTrackSegmentFrame.dispose();
        editTrackSegmentFrame = null;
        if (needsRedraw) {
            layoutEditor.setDirty();
            layoutEditor.redrawPanel();
        }
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

    public final static int SHOWCON = 0x01;
    public final static int HIDECON = 0x02; //flag set on a segment basis.
    public final static int HIDECONALL = 0x04;  //Used by layout editor for hiding all

    public int showConstructionLine = SHOWCON;

    //Method used by Layout Editor
    protected boolean showConstructionLinesLE() {
        if ((showConstructionLine & HIDECON) == HIDECON || (showConstructionLine & HIDECONALL) == HIDECONALL) {
            return false;
        }
        return true;
    }

    public void hideConstructionLines(int hide) {
        if (hide == HIDECONALL) {
            showConstructionLine = showConstructionLine + HIDECONALL;
        } else if (hide == SHOWCON) {
            if ((showConstructionLine & HIDECONALL) == HIDECONALL) {
                showConstructionLine = (showConstructionLine & (~HIDECONALL));
            } else {
                showConstructionLine = hide;
            }
        } else {
            showConstructionLine = HIDECON;
        }
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
    }

    public boolean hideConstructionLines() {
        if ((showConstructionLine & SHOWCON) == SHOWCON) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * The following are used only as a temporary store after a circle or arc
     * has been calculated. This prevents the need to recalculate the values
     * each time a re-draw is required.
     */
    private Point2D pt1;
    private Point2D pt2;

    public Point2D getTmpPt1() {
        return pt1;
    }

    public Point2D getTmpPt2() {
        return pt2;
    }

    public void setTmpPt1(Point2D Pt1) {
        pt1 = Pt1;
        changed = true;
    }

    public void setTmpPt2(Point2D Pt2) {
        pt2 = Pt2;
        changed = true;
    }

    //private int startadj;
    private double cX;

    public double getCX() {
        return cX;
    }

    public void setCX(double CX) {
        cX = CX;
    }

    private double cY;

    public double getCY() {
        return cY;
    }

    public void setCY(double CY) {
        cY = CY;
    }

    private double cW;

    public double getCW() {
        return cW;
    }

    public void setCW(double CW) {
        cW = CW;
    }

    private double cH;

    public double getCH() {
        return cH;
    }

    public void setCH(double CH) {
        cH = CH;
    }

    private double startadj;

    public double getStartadj() {
        return startadj;
    }

    public void setStartadj(double Startadj) {
        startadj = Startadj;
    }

    private double centreSegX;

    public double getCentreSegX() {
        return centreSegX;
    }

    public void setCentreSegX(double CentreX) {
        centreSegX = CentreX;
    }

    private double centreSegY;

    public double getCentreSegY() {
        return centreSegY;
    }

    public void setCentreSegY(double CentreY) {
        centreSegY = CentreY;
    }

    private double centreX;

    public double getCentreX() {
        return centreX;
    }

    public void setCentreX(double CentreX) {
        centreX = CentreX;
    }

    private double centreY;

    public double getCentreY() {
        return centreY;
    }

    public void setCentreY(double CentreY) {
        centreY = CentreY;
    }

    private double tmpangle;

    public double getTmpAngle() {
        return tmpangle;
    }

    public void setTmpAngle(double TmpAngle) {
        tmpangle = TmpAngle;
    }

    public Point2D getCoordsCenterCircle() {
        return new Point2D.Double(getCentreX(), getCentreY());
    }

    private double chordLength;

    public double getChordLength() {
        return chordLength;
    }

    public void setChordLength(double chord) {
        chordLength = chord;
    }

    /*
     * The recalculation method is used when the user changes the angle dynamically in edit mode
     * by dragging the centre of the cirle
     */
    protected void reCalculateTrackSegmentAngle(double x, double y) {

        double pt2x;
        double pt2y;
        double pt1x;
        double pt1y;

        pt2x = getTmpPt2().getX();
        pt2y = getTmpPt2().getY();
        pt1x = getTmpPt1().getX();
        pt1y = getTmpPt1().getY();
        if (getFlip()) {
            pt1x = getTmpPt2().getX();
            pt1y = getTmpPt2().getY();
            pt2x = getTmpPt1().getX();
            pt2y = getTmpPt1().getY();
        }
        //Point 1 to new point length
        double a;
        double o;
        double la;
        // Compute arc's chord
        a = pt2x - x;
        o = pt2y - y;
        la = java.lang.Math.sqrt(((a * a) + (o * o)));

        double lb;
        a = pt1x - x;
        o = pt1y - y;
        lb = java.lang.Math.sqrt(((a * a) + (o * o)));

        double newangle = Math.toDegrees(Math.acos((-getChordLength() * getChordLength() + la * la + lb * lb) / (2 * la * lb)));
        setAngle(newangle);

    }

    /*
     * Calculates the initally parameters for drawing a circular track segment.
     */
    protected void calculateTrackSegmentAngle(/*Point2D pt1, Point2D pt2*/) {
        Point2D pt1 = layoutEditor.getCoords(getConnect1(), getType1());
        Point2D pt2 = layoutEditor.getCoords(getConnect2(), getType2());
        if (getFlip()) {
            pt1 = layoutEditor.getCoords(getConnect2(), getType2());
            pt2 = layoutEditor.getCoords(getConnect1(), getType1());
        }
        if ((getTmpPt1() != pt1) || (getTmpPt2() != pt2) || trackNeedsRedraw()) {
            setTmpPt1(pt1);
            setTmpPt2(pt2);
            //setTrackStrokeWidth(g2,false);
            double pt2x;
            double pt2y;
            double pt1x;
            double pt1y;
            pt2x = pt2.getX();
            pt2y = pt2.getY();
            pt1x = pt1.getX();
            pt1y = pt1.getY();

            if (getAngle() == 0.0D) {
                setTmpAngle(90.0D);
            } else {
                setTmpAngle(getAngle());
            }
            // Convert angle to radiants in order to speed up maths
            double halfAngle = java.lang.Math.toRadians(getTmpAngle()) / 2.0D;
            double chord;
            double a;
            double o;
            double radius;
            // Compute arc's chord
            a = pt2x - pt1x;
            o = pt2y - pt1y;
            chord = java.lang.Math.sqrt(((a * a) + (o * o)));
            setChordLength(chord);
            // Make sure chord is not null 
            // In such a case (pt1 == pt2), there is no arc to draw
            if (chord > 0.0D) {
                radius = (chord / 2) / (java.lang.Math.sin(halfAngle));
                // Circle
                double startRad = java.lang.Math.atan2(a, o) - halfAngle;
                setStartadj(java.lang.Math.toDegrees(startRad));
                if (getCircle()) {
                    // Circle - Compute center
                    setCentreX(pt2x - java.lang.Math.cos(startRad) * radius);
                    setCentreY(pt2y + java.lang.Math.sin(startRad) * radius);

                    // Circle - Compute rectangle required by Arc2D.Double
                    setCW(radius * 2.0D);
                    setCH(radius * 2.0D);
                    setCX(getCentreX() - (radius));
                    setCY(getCentreY() - (radius));

                    //Compute the vlues for locating the circle
                    setCentreSegX(getCentreX() + radius * java.lang.Math.cos(startRad + halfAngle));
                    setCentreSegY(getCentreY() - java.lang.Math.sin(startRad + halfAngle) * radius);

                } else {
                    // Elipse - Round start angle to the closest multiple of 90
                    setStartadj(java.lang.Math.round(getStartadj() / 90.0D) * 90.0D);
                    // Elipse - Compute rectangle required by Arc2D.Double
                    setCW(java.lang.Math.abs(a) * 2.0D);
                    setCH(java.lang.Math.abs(o) * 2.0D);
                    // Elipse - Adjust rectangle corner, depending on quadrant
                    if (o * a < 0.0D) {
                        a = -a;
                    } else {
                        o = -o;
                    }
                    setCX(java.lang.Math.min(pt1x, pt2x) - java.lang.Math.max(a, 0.0D));
                    setCY(java.lang.Math.min(pt1y, pt2y) - java.lang.Math.max(o, 0.0D));
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrackSegment.class.getName());

}
