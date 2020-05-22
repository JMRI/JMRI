package jmri.jmrit.display.layoutEditor;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.event.*;
import java.util.*;
import java.util.function.*;
import javax.annotation.*;
import jmri.*;
import jmri.util.*;

// temporary - these need to go
import jmri.util.swing.JmriColorChooser;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;

/**
 * TrackSegment is a segment of track on a layout linking two nodes of the
 * layout. A node may be a LayoutTurnout, a LevelXing or a PositionablePoint.
 * <p>
 * PositionablePoints have 1 or 2 connection points. LayoutTurnouts have 3 or 4
 * (crossovers) connection points, designated A, B, C, and D. LevelXing's have 4
 * connection points, designated A, B, C, and D.
 * <p>
 * TrackSegments carry the connectivity information between the three types of
 * nodes. Track Segments serve as the lines in a graph which shows layout
 * connectivity. For the connectivity graph to be valid, all connections between
 * nodes must be via TrackSegments.
 * <p>
 * TrackSegments carry Block information, as do LayoutTurnouts and LevelXings.
 *
 * @author Dave Duchamp Copyright (p) 2004-2009
 * @author George Warner Copyright (c) 2017-2019
 */
public class TrackSegment extends LayoutTrack {

    public TrackSegment(@Nonnull String id,
            @CheckForNull LayoutTrack c1, HitPointType t1,
            @CheckForNull LayoutTrack c2, HitPointType t2,
            boolean main,
            @Nonnull LayoutEditor layoutEditor) {
        super(id, layoutEditor);

        // validate input
        if ((c1 == null) || (c2 == null)) {
            log.error("Invalid object in TrackSegment constructor call - {}", id);
        }

        if (HitPointType.isConnectionHitType(t1)) {
            connect1 = c1;
            type1 = t1;
        } else {
            log.error("Invalid connect type 1 ('{}') in TrackSegment constructor - {}", t1, id);
        }
        if (HitPointType.isConnectionHitType(t2)) {
            connect2 = c2;
            type2 = t2;
        } else {
            log.error("Invalid connect type 2 ('{}') in TrackSegment constructor - {}", t2, id);
        }

        mainline = main;
        
        setupDefaultBumperSizes(layoutEditor);

        // editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.TrackSegmentEditor(layoutEditor);
    }

    // alternate constructor for loading layout editor panels
    public TrackSegment(@Nonnull String id,
            @CheckForNull String c1Name, HitPointType t1,
            @CheckForNull String c2Name, HitPointType t2,
            boolean main,
            @Nonnull LayoutEditor layoutEditor) {
        super(id, layoutEditor);

        tConnect1Name = c1Name;
        type1 = t1;
        tConnect2Name = c2Name;
        type2 = t2;

        mainline = main;
        
        setupDefaultBumperSizes(layoutEditor);
        
        // editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.TrackSegmentEditor(layoutEditor);
    }


    // defined constants
    // operational instance variables (not saved between sessions)
    private NamedBeanHandle<LayoutBlock> namedLayoutBlock = null;

    // persistent instances variables (saved between sessions)
    protected LayoutTrack connect1 = null;
    protected HitPointType type1 = HitPointType.NONE;
    protected LayoutTrack connect2 = null;
    protected HitPointType type2 = HitPointType.NONE;
    private boolean mainline = false;

    // eventually drop

//     private boolean dashed = false;
//     private boolean arc = false;
//     private boolean flip = false;
//     private double angle = 0.0D;
//     private boolean circle = false;
//     private boolean changed = false;
//     private boolean bezier = false;



    // for Bezier
    // private final ArrayList<Point2D> bezierControlPoints = new ArrayList<>(); // list of control point displacements

    // temporary reference to the Editor that will eventually be part of View
    // private final jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.TrackSegmentEditor editor;

    /**
     * Get debugging string for the TrackSegment.
     *
     * @return text showing id and connections of this segment
     */
    @Override
    public String toString() {
        return "TrackSegment " + getName()
                + " c1:{" + getConnect1Name() + " (" + type1 + ")},"
                + " c2:{" + getConnect2Name() + " (" + type2 + ")}";

    }

    /*
    * Accessor methods
     */
    @Nonnull
    public String getBlockName() {
        String result = null;
        if (namedLayoutBlock != null) {
            result = namedLayoutBlock.getName();
        }
        return ((result == null) ? "" : result);
    }

    public HitPointType getType1() {
        return type1;
    }

    public HitPointType getType2() {
        return type2;
    }

    public LayoutTrack getConnect1() {
        return connect1;
    }

    public LayoutTrack getConnect2() {
        return connect2;
    }

    /**
     * set a new connection 1
     *
     * @param connectTrack   the track we want to connect to
     * @param connectionType where on that track we want to be connected
     */
    protected void setNewConnect1(@CheckForNull LayoutTrack connectTrack, HitPointType connectionType) {
        connect1 = connectTrack;
        type1 = connectionType;
    }

    /**
     * set a new connection 2
     *
     * @param connectTrack   the track we want to connect to
     * @param connectionType where on that track we want to be connected
     */
    protected void setNewConnect2(@CheckForNull LayoutTrack connectTrack, HitPointType connectionType) {
        connect2 = connectTrack;
        type2 = connectionType;
    }

    /**
     * Replace old track connection with new track connection.
     *
     * @param oldTrack the old track connection.
     * @param newTrack the new track connection.
     * @param newType the hit point type.
     * @return true if successful.
     */
    public boolean replaceTrackConnection(@CheckForNull LayoutTrack oldTrack, @CheckForNull LayoutTrack newTrack, HitPointType newType) {
        boolean result = false; // assume failure (pessimist!)
        // trying to replace old track with null?
        if (newTrack == null) {
            result = true;  // assume success (optimist!)
            //(yes) remove old connection
            if (oldTrack != null) {
                if (connect1 == oldTrack) {
                    connect1 = null;
                    type1 = HitPointType.NONE;
                } else if (connect2 == oldTrack) {
                    connect2 = null;
                    type2 = HitPointType.NONE;
                } else {
                    log.error("{}.replaceTrackConnection({}, null, {}); Attempt to remove invalid track connection",
                            getName(), oldTrack.getName(), newType);
                    result = false;
                }
            } else {
                log.warn("{}.replaceTrackConnection(null, null, {}); Can't replace null track connection with null",
                        getName(), newType);
                result = false;
            }
        } else // already connected to newTrack?
        if ((connect1 != newTrack) && (connect2 != newTrack)) {
            //(no) find a connection we can connect to
            result = true;  // assume success (optimist!)
            if (connect1 == oldTrack) {
                connect1 = newTrack;
                type1 = newType;
            } else if (connect2 == oldTrack) {
                connect2 = newTrack;
                type2 = newType;
            } else {
                log.error("{}.replaceTrackConnection({}, {}, {}); Attempt to replace invalid track connection",
                        getName(), (oldTrack == null) ? "null" : oldTrack.getName(), newTrack.getName(), newType);
                result = false;
            }
        }
        return result;
    }

    /**
     * @return true if track segment should be drawn dashed
     */
//     public boolean isDashed() {
//         return dashed;
//     }

//     public void setDashed(boolean dash) {
//         if (dashed != dash) {
//             dashed = dash;
//             layoutEditor.redrawPanel();
//             layoutEditor.setDirty();
//         }
//     }

    /**
     * @return true if track segment is a main line
     */
    @Override
    public boolean isMainline() {
        return mainline;
    }

    public void setMainline(boolean main) {
        if (mainline != main) {
            mainline = main;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }

    /**
     * @return true if track segment is an arc
     */
    public boolean isArc() {
        log.error("isArc should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        return layoutEditor.getTrackSegmentView(this).isArc();
    }
// 
//     public void setArc(boolean boo) {
//         if (arc != boo) {
//             arc = boo;
//             if (arc) {
//                 circle = false;
//                 bezier = false;
//                 hideConstructionLines(SHOWCON);
//             }
//             changed = true;
//         }
//     }

    /**
     * @return true if track segment is circle
     */
    public boolean isCircle() {
        log.error("isCircle should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        return layoutEditor.getTrackSegmentView(this).isCircle();
    }

    public void setCircle(boolean boo) {
        log.error("setCircle should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        layoutEditor.getTrackSegmentView(this).setCircle(boo);
    }

    /**
     * @return true if track segment circle or arc should be drawn flipped
     */
//     public boolean isFlip() {
//         return flip;
//     }
// 
//     public void setFlip(boolean boo) {
//         if (flip != boo) {
//             flip = boo;
//             changed = true;
//             hideConstructionLines(SHOWCON);
//             layoutEditor.redrawPanel();
//             layoutEditor.setDirty();
//         }
//     }

    /**
     * @return true if track segment is a bezier curve
     */
    public boolean isBezier() {
        log.error("isBezier should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        return layoutEditor.getTrackSegmentView(this).isBezier();
    }

//     public void setBezier(boolean boo) {
//         if (bezier != boo) {
//             bezier = boo;
//             if (bezier) {
//                 arc = false;
//                 circle = false;
//                 hideConstructionLines(SHOWCON);
//             }
//             changed = true;
//         }
//     }
// 
//     public double getAngle() {
//         return angle;
//     }
// 
//     public void setAngle(double x) {
//         angle = MathUtil.pin(x, 0.0D, 180.0D);
//         changed = true;
//     }

    /**
     * Get the direction from end point 1 to 2
     * <p>
     * Note: Goes CW from east (0) to south (PI/2) to west (PI) to north
     * (PI*3/2), etc.
     *
     * @return the direction (in radians)
     */
    public double getDirectionRAD() {
        log.error("getDirectionRAD should have called View instead of TrackSegment (temporary)",
                jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback")));
        return layoutEditor.getTrackSegmentView(this).getDirectionRAD();
    }

    /**
     * Get the direction from end point 1 to 2
     * <p>
     * Note: Goes CW from east (0) to south (90) to west (180) to north (270),
     * etc.
     *
     * @return the direction (in degrees)
     */
    public double getDirectionDEG() {
        log.error("getDirectionDEG should have called View instead of TrackSegment (temporary)",
                jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback")));
        return layoutEditor.getTrackSegmentView(this).getDirectionDEG();
    }

    /**
     * Determine if we need to redraw a curved piece of track. Saves having to
     * recalculate the circle details each time.
     * @return true if needs redraw, else false.
     */
//     public boolean trackNeedsRedraw() {
//         return changed;
//     }
// 
//     public void trackRedrawn() {
//         changed = false;
//     }

    public LayoutBlock getLayoutBlock() {
        return (namedLayoutBlock != null) ? namedLayoutBlock.getBean() : null;
    }

    public String getConnect1Name() {
        return getConnectName(connect1, type1);
    }

    public String getConnect2Name() {
        return getConnectName(connect2, type2);
    }

    private String getConnectName(@CheckForNull LayoutTrack layoutTrack, HitPointType type) {
        return (layoutTrack == null) ? null : layoutTrack.getName();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns null because {@link #getConnect1} and
     * {@link #getConnect2} should be used instead.
     */
    // only implemented here to suppress "does not override abstract method " error in compiler
    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        // nothing to see here, move along
        throw new jmri.JmriException("Use getConnect1() or getConnect2() instead.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing because {@link #setNewConnect1} and
     * {@link #setNewConnect2} should be used instead.
     */
    // only implemented here to suppress "does not override abstract method " error in compiler
    @Override
    public void setConnection(HitPointType connectionType, @CheckForNull LayoutTrack o, HitPointType type) throws jmri.JmriException {
        // nothing to see here, move along
        throw new jmri.JmriException("Use setConnect1() or setConnect2() instead.");
    }

    public void setConnect1(@CheckForNull LayoutTrack o, HitPointType type) {
        type1 = type;
        connect1 = o;
    }
    
    public void setConnect2(@CheckForNull LayoutTrack o, HitPointType type) {
        type2 = type;
        connect2 = o;
    }
    
    public int getNumberOfBezierControlPoints() {
        log.error("getNumberOfBezierControlPoints should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        return layoutEditor.getTrackSegmentView(this).getNumberOfBezierControlPoints();
    }

    public Point2D getBezierControlPoint(int index) {
        log.error("getBezierControlPoint should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        return layoutEditor.getTrackSegmentView(this).getBezierControlPoint(index);
    }

    public void setBezierControlPoint(@CheckForNull Point2D p, int index) {
        log.error("setBezierControlPoint should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        layoutEditor.getTrackSegmentView(this).setBezierControlPoint(p, index);
    }

    public ArrayList<Point2D> getBezierControlPoints() {
        log.error("getBezierControlPoints should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        return layoutEditor.getTrackSegmentView(this).getBezierControlPoints();
    }

    /**
     * Set up a LayoutBlock for this Track Segment.
     *
     * @param newLayoutBlock the LayoutBlock to set
     */
    public void setLayoutBlock(@CheckForNull LayoutBlock newLayoutBlock) {
        LayoutBlock layoutBlock = getLayoutBlock();
        if (layoutBlock != newLayoutBlock) {
            //block has changed, if old block exists, decrement use
            if (layoutBlock != null) {
                layoutBlock.decrementUse();
            }
            namedLayoutBlock = null;
            if (newLayoutBlock != null) {
                String newName = newLayoutBlock.getUserName();
                if ((newName != null) && !newName.isEmpty()) {
                    namedLayoutBlock = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(newName, newLayoutBlock);
                }
            }
        }
    }

    /**
     * Set up a LayoutBlock for this Track Segment.
     *
     * @param name the name of the new LayoutBlock
     */
    public void setLayoutBlockByName(@CheckForNull String name) {
        if ((name != null) && !name.isEmpty()) {
            LayoutBlock b = layoutEditor.provideLayoutBlock(name);
            if (b != null) {
                namedLayoutBlock = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(name, b);
            } else {
                namedLayoutBlock = null;
            }
        } else {
            namedLayoutBlock = null;
        }
    }

    // initialization instance variables (used when loading a LayoutEditor)
    public String tConnect1Name = "";
    public String tConnect2Name = "";

    public String tLayoutBlockName = "";

    /**
     * Initialization method. The above variables are initialized by
     * PositionablePointXml, then the following method is called after the
     * entire LayoutEditor is loaded to set the specific TrackSegment objects.
     */
    @SuppressWarnings("deprecation")
    // NOTE: findObjectByTypeAndName is @Deprecated;
    // we're using it here for backwards compatibility until it can be removed
    @Override
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Null check performed before using return value")
    public void setObjects(LayoutEditor p) {

        LayoutBlock lb;
        if (!tLayoutBlockName.isEmpty()) {
            lb = p.provideLayoutBlock(tLayoutBlockName);
            if (lb != null) {
                namedLayoutBlock = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(lb.getUserName(), lb);
                lb.incrementUse();
            } else {
                log.error("{}.setObjects(...); bad blockname '{}' in tracksegment {}",
                        getName(), tLayoutBlockName, getName());
                namedLayoutBlock = null;
            }
            tLayoutBlockName = null; //release this memory
        }

        connect1 = p.getFinder().findObjectByName(tConnect1Name);
        connect2 = p.getFinder().findObjectByName(tConnect2Name);
    }

    public void updateBlockInfo() {
        LayoutBlock layoutBlock = getLayoutBlock();
        if (layoutBlock != null) {
            layoutBlock.updatePaths();
        }
        LayoutBlock b1 = getBlock(connect1, type1);
        if ((b1 != null) && (b1 != layoutBlock)) {
            b1.updatePaths();
        }
        LayoutBlock b2 = getBlock(connect2, type2);
        if ((b2 != null) && (b2 != layoutBlock) && (b2 != b1)) {
            b2.updatePaths();
        }

        getConnect1().reCheckBlockBoundary();
        getConnect2().reCheckBlockBoundary();
    }

    private LayoutBlock getBlock(LayoutTrack connect, HitPointType type) {
        LayoutBlock result = null;
        if (connect != null) {
            if (type == HitPointType.POS_POINT) {
                PositionablePoint p = (PositionablePoint) connect;
                if (p.getConnect1() != this) {
                    if (p.getConnect1() != null) {
                        result = p.getConnect1().getLayoutBlock();
                    }
                } else {
                    if (p.getConnect2() != null) {
                        result = p.getConnect2().getLayoutBlock();
                    }
                }
            } else {
                result = layoutEditor.getAffectedBlock(connect, type);
            }
        }
        return result;
    }

//     private JPopupMenu popupMenu = null;
//     private final JCheckBoxMenuItem mainlineCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("MainlineCheckBoxMenuItemTitle"));
//     private final JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("HiddenCheckBoxMenuItemTitle"));
//     private final JCheckBoxMenuItem dashedCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("DashedCheckBoxMenuItemTitle"));
//     private final JCheckBoxMenuItem flippedCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("FlippedCheckBoxMenuItemTitle"));

    /**
     * Maximum length of the bumper decoration. (temporary:  why here instead of View?)
     */
    public static final int MAX_BUMPER_LENGTH = 40;
    public static final int MAX_BUMPER_WIDTH = 10;

    private static final int MAX_ARROW_LINE_WIDTH = 5;
    private static final int MAX_ARROW_LENGTH = 60;
    private static final int MAX_ARROW_GAP = 40;
    private static final int MAX_BRIDGE_LINE_WIDTH = 5;
    private static final int MAX_BRIDGE_APPROACH_WIDTH = 100;
    private static final int MAX_BRIDGE_DECK_WIDTH = 80;
    private static final int MAX_BUMPER_LINE_WIDTH = 9;
    private static final int MAX_TUNNEL_FLOOR_WIDTH = 40;
    private static final int MAX_TUNNEL_LINE_WIDTH = 9;
    private static final int MAX_TUNNEL_ENTRANCE_WIDTH = 80;
    
    /**
     * Helper method, which adds "Set value" item to the menu. The value can be
     * optionally range-checked. Item will be appended at the end of the menu.
     *
     * @param menu       the target menu.
     * @param titleKey   bundle key for the menu title/dialog title
     * @param toolTipKey bundle key for the menu item tooltip
     * @param val        value getter
     * @param set        value setter
     * @param predicate  checking predicate, possibly null.
     */
//     private void addNumericMenuItem(@Nonnull JMenu menu,
//             @Nonnull String titleKey, @Nonnull String toolTipKey,
//             @Nonnull Supplier<Integer> val,
//             @Nonnull Consumer<Integer> set,
//             @CheckForNull Predicate<Integer> predicate) {
//         int oldVal = val.get();
//         JMenuItem jmi = menu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
//                 Bundle.getMessage(titleKey)) + oldVal));
//         jmi.setToolTipText(Bundle.getMessage(toolTipKey));
//         jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
//             // prompt for lineWidth
//             int newValue = QuickPromptUtil.promptForInteger(layoutEditor,
//                     Bundle.getMessage(titleKey),
//                     Bundle.getMessage(titleKey),
//                     // getting again, maybe something changed from the menu construction ?
//                     val.get(), predicate);
//             set.accept(newValue);
//             layoutEditor.repaint();
//         });
//     }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemove() {
        List<String> itemList = new ArrayList<>();

        HitPointType type1 = getType1();
        LayoutTrack conn1 = getConnect1();
        itemList.addAll(getPointReferences(type1, conn1));

        HitPointType type2 = getType2();
        LayoutTrack conn2 = getConnect2();
        itemList.addAll(getPointReferences(type2, conn2));

        if (!itemList.isEmpty()) {
            displayRemoveWarningDialog(itemList, "TrackSegment");  // NOI18N
        }
        return itemList.isEmpty();
    }

    public ArrayList<String> getPointReferences(HitPointType type, LayoutTrack conn) {
        ArrayList<String> result = new ArrayList<>();

        if (type == HitPointType.POS_POINT && conn instanceof PositionablePoint) {
            PositionablePoint pt = (PositionablePoint) conn;
            if (!pt.getEastBoundSignal().isEmpty()) {
                result.add(pt.getEastBoundSignal());
            }
            if (!pt.getWestBoundSignal().isEmpty()) {
                result.add(pt.getWestBoundSignal());
            }
            if (!pt.getEastBoundSignalMastName().isEmpty()) {
                result.add(pt.getEastBoundSignalMastName());
            }
            if (!pt.getWestBoundSignalMastName().isEmpty()) {
                result.add(pt.getWestBoundSignalMastName());
            }
            if (!pt.getEastBoundSensorName().isEmpty()) {
                result.add(pt.getEastBoundSensorName());
            }
            if (!pt.getWestBoundSensorName().isEmpty()) {
                result.add(pt.getWestBoundSensorName());
            }
            if (pt.getType() == PositionablePoint.PointType.EDGE_CONNECTOR && pt.getLinkedPoint() != null) {
                result.add(Bundle.getMessage("DeleteECisActive"));   // NOI18N
            }
        }

        if (HitPointType.isTurnoutHitType(type) && conn instanceof LayoutTurnout) {
            LayoutTurnout lt = (LayoutTurnout) conn;
            switch (type) {
                case TURNOUT_A: {
                    result = lt.getBeanReferences("A");  // NOI18N
                    break;
                }
                case TURNOUT_B: {
                    result = lt.getBeanReferences("B");  // NOI18N
                    break;
                }
                case TURNOUT_C: {
                    result = lt.getBeanReferences("C");  // NOI18N
                    break;
                }
                case TURNOUT_D: {
                    result = lt.getBeanReferences("D");  // NOI18N
                    break;
                }
                default: {
                    log.error("Unexpected HitPointType: {}", type);
                }
            }
        }

        if (HitPointType.isLevelXingHitType(type) && conn instanceof LevelXing) {
            LevelXing lx = (LevelXing) conn;
            switch (type) {
                case LEVEL_XING_A: {
                    result = lx.getBeanReferences("A");  // NOI18N
                    break;
                }
                case LEVEL_XING_B: {
                    result = lx.getBeanReferences("B");  // NOI18N
                    break;
                }
                case LEVEL_XING_C: {
                    result = lx.getBeanReferences("C");  // NOI18N
                    break;
                }
                case LEVEL_XING_D: {
                    result = lx.getBeanReferences("D");  // NOI18N
                    break;
                }
                default: {
                    log.error("Unexpected HitPointType: {}", type);
                }
            }
        }

        if (HitPointType.isSlipHitType(type) && conn instanceof LayoutSlip) {
            LayoutSlip ls = (LayoutSlip) conn;
            switch (type) {
                case SLIP_A: {
                    result = ls.getBeanReferences("A");  // NOI18N
                    break;
                }
                case SLIP_B: {
                    result = ls.getBeanReferences("B");  // NOI18N
                    break;
                }
                case SLIP_C: {
                    result = ls.getBeanReferences("C");  // NOI18N
                    break;
                }
                case SLIP_D: {
                    result = ls.getBeanReferences("D");  // NOI18N
                    break;
                }
                default: {
                    log.error("Unexpected HitPointType: {}", type);
                }
            }
        }

        return result;
    }

    /**
     * split track segment into two track segments with an anchor between
     */
    public void splitTrackSegment() {
        throw new IllegalArgumentException("splitTrackSegment should be called in View");
    }

    /**
     * Display popup menu for information and editing.
     * @param e mouse event, for co-ordinates of popup.
     * @param hitPointType the hit point type.
     */
    protected void showBezierPopUp(MouseEvent e, HitPointType hitPointType) {
        log.error("showBezierPopUp should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        layoutEditor.getTrackSegmentView(this).showBezierPopUp(e, hitPointType);
    }

    /**
     * Clean up when this object is no longer needed.
     * <p>
     * Should not be called while the object is still displayed.
     *
     * @see #remove()
     */
    public void dispose() {
        log.error("dispose should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        layoutEditor.getTrackSegmentView(this).dispose();
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
     * Get state. "active" means that the object is still displayed, and should
     * be stored.
     * @return true if still displayed, else false.
     */
    public boolean isActive() {
        return active;
    }

    public static final int SHOWCON = 0x01;
    public static final int HIDECON = 0x02;     // flag set on a segment basis.
    public static final int HIDECONALL = 0x04;  // Used by layout editor for hiding all

    public int showConstructionLine = SHOWCON;

    /**
     * @return true if HIDECON is not set and HIDECONALL is not set
     */
    public boolean isShowConstructionLines() {
        return (((showConstructionLine & HIDECON) != HIDECON)
                && ((showConstructionLine & HIDECONALL) != HIDECONALL));
    }

    /**
     * Method used by LayoutEditor.
     * <p>
     * If the argument is 
     * <ul>
     * <li>HIDECONALL then set HIDECONALL
     * <li>SHOWCON reset HIDECONALL is set, other wise set SHOWCON
     * <li>HIDECON or otherwise set HIDECON
     * </ul>
     * Then always redraw the LayoutEditor panel and set it dirty.
     * @param hide HIDECONALL, SHOWCON, HIDECON.
     */
    public void hideConstructionLines(int hide) {
        if (hide == HIDECONALL) {
            showConstructionLine |= HIDECONALL;
        } else if (hide == SHOWCON) {
            if ((showConstructionLine & HIDECONALL) == HIDECONALL) {
                showConstructionLine &= ~HIDECONALL;
            } else {
                showConstructionLine = hide;
            }
        } else {
            showConstructionLine = HIDECON;
        }
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
    }

    /**
     * @return true if SHOWCON is not set
     */
    public boolean hideConstructionLines() {
        return ((showConstructionLine & SHOWCON) != SHOWCON);
    }

    /**
     * The following are used only as a local store after a circle or arc
     * has been calculated. This prevents the need to recalculate the values
     * each time a re-draw is required.
     */
//     private Point2D pt1;
//     private Point2D pt2;
// 
//     public Point2D getTmpPt1() {
//         return pt1;
//     }
// 
//     public Point2D getTmpPt2() {
//         return pt2;
//     }
// 
//     public void setTmpPt1(Point2D Pt1) {
//         pt1 = Pt1;
//         changed = true;
//     }
// 
//     public void setTmpPt2(Point2D Pt2) {
//         pt2 = Pt2;
//         changed = true;
//     }

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

    private double startAdj;

    public double getStartAdj() {
        return startAdj;
    }

    public void setStartAdj(double startAdj) {
        this.startAdj = startAdj;
    }

    // this is the center of the track segment (it is "on" the track segment)
    public double getCentreSegX() {
        log.error("getCentreSegX should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        return layoutEditor.getTrackSegmentView(this).getCentreSegX();
    }
// 
//     public void setCentreSegX(double x) {
//         super.setCoordsCenter(new Point2D.Double(x, getCentreSeg().getY()));
//     }
// 
    public double getCentreSegY() {
        log.error("getCentreSegY should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        return layoutEditor.getTrackSegmentView(this).getCentreSegY();
    }
// 
//     public void setCentreSegY(double y) {
//         super.setCoordsCenter(new Point2D.Double(getCentreSeg().getX(), y));
//     }

    /**
     * @return the location of the middle of the segment (on the segment)
     */
    public Point2D getCentreSeg() {
        log.error("getCentreSeg should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        return layoutEditor.getTrackSegmentView(this).getCentreSeg();
    }
// 
//     public void setCentreSeg(Point2D p) {
//         super.setCoordsCenter(p);
//     }

    // this is the center of the track segment when configured as a circle
    private double centreX;

    public double getCentreX() {
        return centreX;
    }

    public void setCentreX(double x) {
        centreX = x;
    }

    private double centreY;

    public double getCentreY() {
        return centreY;
    }

    public void setCentreY(double y) {
        centreY = y;
    }

    public Point2D getCentre() {
        return new Point2D.Double(centreX, centreY);
    }

    private double tmpangle;

    public double getTmpAngle() {
        return tmpangle;
    }

    public void setTmpAngle(double a) {
        tmpangle = a;
    }

    /**
     * get center coordinates
     *
     * @return the center coordinates
     */
    public Point2D getCoordsCenterCircle() {
        return getCentre();
    }

    /**
     * set center coordinates
     *
     * @param p the coordinates to set
     */
//     public void setCoordsCenterCircle(Point2D p) {
//         centreX = p.getX();
//         centreY = p.getY();
//     }

    private double chordLength;

    public double getChordLength() {
        return chordLength;
    }

    public void setChordLength(double chord) {
        chordLength = chord;
    }

    /**
     * Called when the user changes the angle dynamically in edit mode
     * by dragging the centre of the circle.
     * @param x new width.
     * @param y new height.
     */
    protected void reCalculateTrackSegmentAngle(double x, double y) {
        log.error("reCalculateTrackSegmentAngle should have called View instead of TrackSegment (temporary)"
                , jmri.util.Log4JUtil.shortenStacktrace(new Exception("temporary traceback"))
            );
        layoutEditor.getTrackSegmentView(this).reCalculateTrackSegmentAngle(x, y);
    }

    /**
     * Calculate the initial parameters for drawing a circular track segment.
     */
//     protected void calculateTrackSegmentAngle() {
//         Point2D pt1, pt2;
//         if (isFlip()) {
//             pt1 = layoutEditor.getCoords(getConnect2(), getType2());
//             pt2 = layoutEditor.getCoords(getConnect1(), getType1());
//         } else {
//             pt1 = layoutEditor.getCoords(getConnect1(), getType1());
//             pt2 = layoutEditor.getCoords(getConnect2(), getType2());
//         }
//         if ((getTmpPt1() != pt1) || (getTmpPt2() != pt2) || trackNeedsRedraw()) {
//             setTmpPt1(pt1);
//             setTmpPt2(pt2);
// 
//             double pt1x = pt1.getX();
//             double pt1y = pt1.getY();
//             double pt2x = pt2.getX();
//             double pt2y = pt2.getY();
// 
//             if (getAngle() == 0.0D) {
//                 setTmpAngle(90.0D);
//             } else {
//                 setTmpAngle(getAngle());
//             }
//             // Convert angle to radiants in order to speed up math
//             double halfAngleRAD = Math.toRadians(getTmpAngle()) / 2.D;
// 
//             // Compute arc's chord
//             double a = pt2x - pt1x;
//             double o = pt2y - pt1y;
//             double chord = Math.hypot(a, o);
//             setChordLength(chord);
// 
//             // Make sure chord is not null
//             // In such a case (ep1 == ep2), there is no arc to draw
//             if (chord > 0.D) {
//                 double radius = (chord / 2.D) / Math.sin(halfAngleRAD);
//                 // Circle
//                 double startRad = Math.atan2(a, o) - halfAngleRAD;
//                 setStartAdj(Math.toDegrees(startRad));
//                 if (isCircle()) {
//                     // Circle - Compute center
//                     setCentreX(pt2x - Math.cos(startRad) * radius);
//                     setCentreY(pt2y + Math.sin(startRad) * radius);
// 
//                     // Circle - Compute rectangle required by Arc2D.Double
//                     setCW(radius * 2.0D);
//                     setCH(radius * 2.0D);
//                     setCX(getCentreX() - radius);
//                     setCY(getCentreY() - radius);
// 
//                     // Compute where to locate the control circle on the circle segment
//                     Point2D offset = new Point2D.Double(
//                             +radius * Math.cos(startRad + halfAngleRAD),
//                             -radius * Math.sin(startRad + halfAngleRAD));
//                     setCentreSeg(MathUtil.add(getCentre(), offset));
//                 } else {
//                     // Ellipse - Round start angle to the closest multiple of 90
//                     setStartAdj(Math.round(getStartAdj() / 90.0D) * 90.0D);
//                     // Ellipse - Compute rectangle required by Arc2D.Double
//                     setCW(Math.abs(a) * 2.0D);
//                     setCH(Math.abs(o) * 2.0D);
//                     // Ellipse - Adjust rectangle corner, depending on quadrant
//                     if (o * a < 0.0D) {
//                         a = -a;
//                     } else {
//                         o = -o;
//                     }
//                     setCX(Math.min(pt1x, pt2x) - Math.max(a, 0.0D));
//                     setCY(Math.min(pt1y, pt2y) - Math.max(o, 0.0D));
//                 }
//             }
//         }
//     }   // calculateTrackSegmentAngle


    /**
     * temporary fill of abstract from above
     */
    @Override
    public void reCheckBlockBoundary() {
        log.info("reCheckBlockBoundary is temporary, but was invoked", new Exception("traceback"));
    }


    /** 
     * Arrow decoration accessor.
     * The 0 (none) and 1 through 5 arrow decorations are keyed to 
     * files like program:resources/icons/decorations/ArrowStyle1.png
     * et al.
     * @return arrow style, 0 is none.
     */
   public int getArrowStyle() {
       return arrowStyle;
   }

    /** 
     * Set the arrow decoration.
     * The 0 (none) and 1 through 5 arrow decorations are keyed to 
     * files like program:resources/icons/decorations/ArrowStyle1.png
     * et al.
     * @param newVal the arrow style index, 0 is none.
     */
    public void setArrowStyle(int newVal) {
        log.trace("TrackSegment:setArrowStyle {} {} {}", newVal, arrowEndStart, arrowEndStop);
        if (arrowStyle != newVal) {
            if (newVal > 0) {
                if (!arrowEndStart && !arrowEndStop) {
                    arrowEndStart = true;
                    arrowEndStop = true;
                }
                if (!arrowDirIn && !arrowDirOut) {
                    arrowDirOut = true;
                }
            } else {
                newVal = 0; // only positive styles allowed!
            }
            arrowStyle = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int arrowStyle = 0;

    public boolean isArrowEndStart() {
        return arrowEndStart;
    }

    public void setArrowEndStart(boolean newVal) {
        if (arrowEndStart != newVal) {
            arrowEndStart = newVal;
            if (!arrowEndStart && !arrowEndStop) {
                arrowStyle = 0;
            } else if (arrowStyle == 0) {
                arrowStyle = 1;
            }
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean arrowEndStart = false;

    public boolean isArrowEndStop() {
        return arrowEndStop;
    }

    public void setArrowEndStop(boolean newVal) {
        if (arrowEndStop != newVal) {
            arrowEndStop = newVal;
            if (!arrowEndStart && !arrowEndStop) {
                arrowStyle = 0;
            } else if (arrowStyle == 0) {
                arrowStyle = 1;
            }
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean arrowEndStop = false;

    public boolean isArrowDirIn() {
        return arrowDirIn;
    }

    public void setArrowDirIn(boolean newVal) {
        if (arrowDirIn != newVal) {
            arrowDirIn = newVal;
            if (!arrowDirIn && !arrowDirOut) {
                arrowStyle = 0;
            } else if (arrowStyle == 0) {
                arrowStyle = 1;
            }
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean arrowDirIn = false;

    public boolean isArrowDirOut() {
        return arrowDirOut;
    }

    public void setArrowDirOut(boolean newVal) {
        if (arrowDirOut != newVal) {
            arrowDirOut = newVal;
            if (!arrowDirIn && !arrowDirOut) {
                arrowStyle = 0;
            } else if (arrowStyle == 0) {
                arrowStyle = 1;
            }
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean arrowDirOut = false;

    public Color getArrowColor() {
        return arrowColor;
    }

    public void setArrowColor(Color newVal) {
        if (arrowColor != newVal) {
            arrowColor = newVal;
            JmriColorChooser.addRecentColor(newVal);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private Color arrowColor = Color.BLACK;

    public int getArrowLineWidth() {
        return arrowLineWidth;
    }

    public void setArrowLineWidth(int newVal) {
        if (arrowLineWidth != newVal) {
            arrowLineWidth = MathUtil.pin(newVal, 1, MAX_ARROW_LINE_WIDTH);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int arrowLineWidth = 4;

    public int getArrowLength() {
        return arrowLength;
    }

    public void setArrowLength(int newVal) {
        if (arrowLength != newVal) {
            arrowLength = MathUtil.pin(newVal, 2, MAX_ARROW_LENGTH);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int arrowLength = 4;

    public int getArrowGap() {
        return arrowGap;
    }

    public void setArrowGap(int newVal) {
        if (arrowGap != newVal) {
            arrowGap = MathUtil.pin(newVal, 0, MAX_ARROW_GAP);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int arrowGap = 1;

    //
    // bridge decoration accessors
    //
    public boolean isBridgeSideRight() {
        return bridgeSideRight;
    }

    public void setBridgeSideRight(boolean newVal) {
        if (bridgeSideRight != newVal) {
            bridgeSideRight = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bridgeSideRight = false;

    public boolean isBridgeSideLeft() {
        return bridgeSideLeft;
    }

    public void setBridgeSideLeft(boolean newVal) {
        if (bridgeSideLeft != newVal) {
            bridgeSideLeft = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bridgeSideLeft = false;

    public boolean isBridgeHasEntry() {
        return bridgeHasEntry;
    }

    public void setBridgeHasEntry(boolean newVal) {
        if (bridgeHasEntry != newVal) {
            bridgeHasEntry = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bridgeHasEntry = false;

    public boolean isBridgeHasExit() {
        return bridgeHasExit;
    }

    public void setBridgeHasExit(boolean newVal) {
        if (bridgeHasExit != newVal) {
            bridgeHasExit = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bridgeHasExit = false;

    public Color getBridgeColor() {
        return bridgeColor;
    }

    public void setBridgeColor(Color newVal) {
        if (bridgeColor != newVal) {
            bridgeColor = newVal;
            JmriColorChooser.addRecentColor(newVal);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private Color bridgeColor = Color.BLACK;

    public int getBridgeDeckWidth() {
        return bridgeDeckWidth;
    }

    public void setBridgeDeckWidth(int newVal) {
        if (bridgeDeckWidth != newVal) {
            bridgeDeckWidth = Math.max(6, newVal);   // don't let value be less than 6
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int bridgeDeckWidth = 10;

    public int getBridgeLineWidth() {
        return bridgeLineWidth;
    }

    public void setBridgeLineWidth(int newVal) {
        if (bridgeLineWidth != newVal) {
            bridgeLineWidth = Math.max(1, newVal);   // don't let value be less than 1
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int bridgeLineWidth = 1;

    public int getBridgeApproachWidth() {
        return bridgeApproachWidth;
    }

    public void setBridgeApproachWidth(int newVal) {
        if (bridgeApproachWidth != newVal) {
            bridgeApproachWidth = Math.max(8, newVal);   // don't let value be less than 8
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int bridgeApproachWidth = 4;

    //
    // bumper decoration accessors
    //
    public boolean isBumperEndStart() {
        return bumperEndStart;
    }

    public void setBumperEndStart(boolean newVal) {
        if (bumperEndStart != newVal) {
            bumperEndStart = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bumperEndStart = false;

    public boolean isBumperEndStop() {
        return bumperEndStop;
    }

    public void setBumperEndStop(boolean newVal) {
        if (bumperEndStop != newVal) {
            bumperEndStop = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bumperEndStop = false;

    public Color getBumperColor() {
        return bumperColor;
    }

    public void setBumperColor(Color newVal) {
        if (bumperColor != newVal) {
            bumperColor = newVal;
            JmriColorChooser.addRecentColor(newVal);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private Color bumperColor = Color.BLACK;

    public int getBumperLineWidth() {
        return bumperLineWidth;
    }

    public void setBumperLineWidth(int newVal) {
        if (bumperLineWidth != newVal) {
            bumperLineWidth = MathUtil.pin(newVal, 1, MAX_BUMPER_LINE_WIDTH);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }

    private int bumperLineWidth = 3;

    public int getBumperLength() {
        return bumperLength;
    }

    public void setBumperLength(int newVal) {
        if (bumperLength != newVal) {
            bumperLength = Math.max(8, newVal);   // don't let value be less than 8
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int bumperLength = 20;

    public boolean isBumperFlipped() {
        return bumperFlipped;
    }

    public void setBumperFlipped(boolean newVal) {
        if (bumperFlipped != newVal) {
            bumperFlipped = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bumperFlipped = false;

    private void setupDefaultBumperSizes(LayoutEditor layoutEditor) {
        LayoutTrackDrawingOptions ltdo = layoutEditor.getLayoutTrackDrawingOptions();

        // use these as default sizes for end bumpers
        int tieLength = ltdo.getSideTieLength();
        int tieWidth = ltdo.getSideTieWidth();
        int railWidth = ltdo.getSideRailWidth();
        int railGap = ltdo.getSideRailGap();
        if (mainline) {
            tieLength = ltdo.getMainTieLength();
            tieWidth = ltdo.getMainTieWidth();
            railWidth = ltdo.getMainRailWidth();
            railGap = ltdo.getMainRailGap();
        }
        bumperLineWidth = Math.max(railWidth, ltdo.getMainBlockLineWidth()) * 2;
        bumperLength = railGap + (2 * railWidth);
        if ((tieLength > 0) && (tieWidth > 0)) {
            bumperLineWidth = tieWidth;
            bumperLength = tieLength * 3 / 2;
        }
        bumperLineWidth = Math.max(1, bumperLineWidth);
        bumperLength = Math.max(10, bumperLength);
    }

    //
    // tunnel decoration accessors
    //
    public boolean isTunnelSideRight() {
        return tunnelSideRight;
    }

    public void setTunnelSideRight(boolean newVal) {
        if (tunnelSideRight != newVal) {
            tunnelSideRight = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean tunnelSideRight = false;

    public boolean isTunnelSideLeft() {
        return tunnelSideLeft;
    }

    public void setTunnelSideLeft(boolean newVal) {
        if (tunnelSideLeft != newVal) {
            tunnelSideLeft = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean tunnelSideLeft = false;

    public boolean isTunnelHasEntry() {
        return tunnelHasEntry;
    }

    public void setTunnelHasEntry(boolean newVal) {
        if (tunnelHasEntry != newVal) {
            tunnelHasEntry = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean tunnelHasEntry = false;

    public boolean isTunnelHasExit() {
        return tunnelHasExit;
    }

    public void setTunnelHasExit(boolean newVal) {
        if (tunnelHasExit != newVal) {
            tunnelHasExit = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean tunnelHasExit = false;

    public Color getTunnelColor() {
        return tunnelColor;
    }

    public void setTunnelColor(Color newVal) {
        if (tunnelColor != newVal) {
            tunnelColor = newVal;
            JmriColorChooser.addRecentColor(newVal);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private Color tunnelColor = Color.BLACK;

    public int getTunnelFloorWidth() {
        return tunnelFloorWidth;
    }

    public void setTunnelFloorWidth(int newVal) {
        if (tunnelFloorWidth != newVal) {
            tunnelFloorWidth = Math.max(4, newVal);   // don't let value be less than 4
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int tunnelFloorWidth = 10;

    public int getTunnelLineWidth() {
        return tunnelLineWidth;
    }

    public void setTunnelLineWidth(int newVal) {
        if (tunnelLineWidth != newVal) {
            tunnelLineWidth = Math.max(1, newVal);   // don't let value be less than 1
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int tunnelLineWidth = 1;

    public int getTunnelEntranceWidth() {
        return tunnelEntranceWidth;
    }

    public void setTunnelEntranceWidth(int newVal) {
        if (tunnelEntranceWidth != newVal) {
            tunnelEntranceWidth = Math.max(1, newVal);   // don't let value be less than 1
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int tunnelEntranceWidth = 16;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        List<LayoutConnectivity> results = new ArrayList<>();

        LayoutConnectivity lc = null;
        LayoutBlock lb1 = getLayoutBlock(), lb2 = null;
        // ensure that block is assigned
        if (lb1 != null) {
            // check first connection for turnout
            if (HitPointType.isTurnoutHitType(type1)) {
                // have connection to a turnout, is block different
                LayoutTurnout lt = (LayoutTurnout) getConnect1();
                lb2 = lt.getLayoutBlock();
                if (lt.hasEnteringDoubleTrack()) {
                    // not RH, LH, or WYE turnout - other blocks possible
                    if ((type1 == HitPointType.TURNOUT_B) && (lt.getLayoutBlockB() != null)) {
                        lb2 = lt.getLayoutBlockB();
                    }
                    if ((type1 == HitPointType.TURNOUT_C) && (lt.getLayoutBlockC() != null)) {
                        lb2 = lt.getLayoutBlockC();
                    }
                    if ((type1 == HitPointType.TURNOUT_D) && (lt.getLayoutBlockD() != null)) {
                        lb2 = lt.getLayoutBlockD();
                    }
                }
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  (''{}''<->''{}'') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, lt, type1, null);
                    lc.setDirection(Path.computeDirection(
                            layoutEditor.getCoords(getConnect2(), type2),
                            layoutEditor.getCoords(getConnect1(), type1)));
                    results.add(lc);
                }
            } else if (HitPointType.isLevelXingHitType(type1)) {
                // have connection to a level crossing
                LevelXing lx = (LevelXing) getConnect1();
                if ((type1 == HitPointType.LEVEL_XING_A) || (type1 == HitPointType.LEVEL_XING_C)) {
                    lb2 = lx.getLayoutBlockAC();
                } else {
                    lb2 = lx.getLayoutBlockBD();
                }
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  (''{}''<->''{}'') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, lx, type1, null);
                    lc.setDirection(Path.computeDirection(
                            layoutEditor.getCoords(getConnect2(), type2),
                            layoutEditor.getCoords(getConnect1(), type1)));
                    results.add(lc);
                }
            } else if (HitPointType.isSlipHitType(type1)) {
                // have connection to a slip crossing
                LayoutSlip ls = (LayoutSlip) getConnect1();
                lb2 = ls.getLayoutBlock();
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  (''{}''<->''{}'') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, ls, type1, null);
                    lc.setDirection(Path.computeDirection(layoutEditor.getCoords(getConnect2(),
                            type2), layoutEditor.getCoords(getConnect1(), type1)));
                    results.add(lc);
                }
            }
            // check second connection for turnout
            if (HitPointType.isTurnoutHitType(type2)) {
                // have connection to a turnout
                LayoutTurnout lt = (LayoutTurnout) getConnect2();
                lb2 = lt.getLayoutBlock();
                if (lt.hasEnteringDoubleTrack()) {
                    // not RH, LH, or WYE turnout - other blocks possible
                    if ((type2 == HitPointType.TURNOUT_B) && (lt.getLayoutBlockB() != null)) {
                        lb2 = lt.getLayoutBlockB();
                    }
                    if ((type2 == HitPointType.TURNOUT_C) && (lt.getLayoutBlockC() != null)) {
                        lb2 = lt.getLayoutBlockC();
                    }
                    if ((type2 == HitPointType.TURNOUT_D) && (lt.getLayoutBlockD() != null)) {
                        lb2 = lt.getLayoutBlockD();
                    }
                }
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  (''{}''<->''{}'') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, lt, type2, null);
                    lc.setDirection(Path.computeDirection(
                            layoutEditor.getCoords(getConnect1(), type1),
                            layoutEditor.getCoords(getConnect2(), type2)));
                    results.add(lc);
                }
            } else if (HitPointType.isLevelXingHitType(type2)) {
                // have connection to a level crossing
                LevelXing lx = (LevelXing) getConnect2();
                if ((type2 == HitPointType.LEVEL_XING_A) || (type2 == HitPointType.LEVEL_XING_C)) {
                    lb2 = lx.getLayoutBlockAC();
                } else {
                    lb2 = lx.getLayoutBlockBD();
                }
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  (''{}''<->''{}'') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, lx, type2, null);
                    lc.setDirection(Path.computeDirection(
                            layoutEditor.getCoords(getConnect1(), type1),
                            layoutEditor.getCoords(getConnect2(), type2)));
                    results.add(lc);
                }
            } else if (HitPointType.isSlipHitType(type2)) {
                // have connection to a slip crossing
                LayoutSlip ls = (LayoutSlip) getConnect2();
                lb2 = ls.getLayoutBlock();
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  (''{}''<->''{}'') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, ls, type2, null);
                    lc.setDirection(Path.computeDirection(
                            layoutEditor.getCoords(getConnect1(), type1),
                            layoutEditor.getCoords(getConnect2(), type2)));
                    results.add(lc);
                }
            }
        }   // if (lb1 != null)
        return results;
    }   // getLayoutConnectivity()

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HitPointType> checkForFreeConnections() {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkForUnAssignedBlocks() {
        return (getLayoutBlock() != null);
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
        *     blockNamesToTrackNameSetMap and
        * #4) collect all the connections in this block
        * <p>
        *     Basically, we're maintaining contiguous track sets for each block found
        *     (in blockNamesToTrackNameSetMap)
         */
        List<Set<String>> TrackNameSets = null;
        Set<String> TrackNameSet = null;    // assume not found (pessimist!)
        String blockName = getBlockName();
        if (!blockName.isEmpty()) {
            TrackNameSets = blockNamesToTrackNameSetsMap.get(blockName);
            if (TrackNameSets != null) { //(#1)
                for (Set<String> checkTrackNameSet : TrackNameSets) {
                    if (checkTrackNameSet.contains(getName())) { //(#2)
                        TrackNameSet = checkTrackNameSet;
                        break;
                    }
                }
            } else {    //(#3)
                log.debug("*New block (''{}'') trackNameSets", blockName);
                TrackNameSets = new ArrayList<>();
                blockNamesToTrackNameSetsMap.put(blockName, TrackNameSets);
            }
            if (TrackNameSet == null) {
                TrackNameSet = new LinkedHashSet<>();
                TrackNameSets.add(TrackNameSet);
            }
            if (TrackNameSet.add(getName())) {
                log.debug("*    Add track ''{}'' to TrackNameSets for block ''{}''", getName(), blockName);
            }
            //(#4)
            if (connect1 != null) {
                connect1.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
            }
            if (connect2 != null) { //(#4)
                connect2.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
        if (!TrackNameSet.contains(getName())) {
            // is this the blockName we're looking for?
            if (getBlockName().equals(blockName)) {
                // if we are added to the TrackNameSet
                if (TrackNameSet.add(getName())) {
                    log.debug("*    Add track ''{}''for block ''{}''", getName(), blockName);
                }
                // these should never be null... but just in case...
                // it's time to play... flood your neighbours!
                if (connect1 != null) {
                    connect1.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
                if (connect2 != null) {
                    connect2.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
        setLayoutBlock(layoutBlock);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackSegment.class);
}
