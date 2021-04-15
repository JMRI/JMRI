package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Math.PI;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.display.layoutEditor.LevelXing.Geometry;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.util.MathUtil;

/**
 * MVC View component for the LevelXing class
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * 
 */
public class LevelXingView extends LayoutTrackView {

    /**
     * Constructor method.
     * @param xing the level crossing.
     * @param layoutEditor for access to tools
     */
    public LevelXingView(@Nonnull LevelXing xing, @Nonnull LayoutEditor layoutEditor) {
        super(xing, layoutEditor);

        this.xing = xing;
        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LevelXingEditor(layoutEditor);
    }
        
    /**
     * constructor method
     * @param xing the level crossing.
     * @param c display location
     * @param layoutEditor for access to tools
     */
    public LevelXingView(@Nonnull LevelXing xing, @Nonnull Point2D c, @Nonnull LayoutEditor layoutEditor) {
        super(xing, c, layoutEditor);

        this.xing = xing;
        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LevelXingEditor(layoutEditor);
    }

    private Point2D dispA = new Point2D.Double(-20.0, 0.0);
    private Point2D dispB = new Point2D.Double(-14.0, 14.0);

    // temporary reference to the Editor that will eventually be part of View
    private final jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LevelXingEditor editor;

    final private LevelXing xing;

    // temporary?
    @Nonnull
    public LevelXing getLevelXing() { return xing; }
    
    // this should only be used for debugging
    @Override
    public String toString() {
        return "LevelXing " + getName();
    }

    /*
    * Accessor methods
     */
    @Nonnull
    public String getBlockNameAC() {
        return xing.getBlockNameAC();
    }

    @Nonnull
    public String getBlockNameBD() {
        return xing.getBlockNameBD();
    }

    public SignalHead getSignalHead(Geometry loc) {
        return xing.getSignalHead(loc);
    }

    public SignalMast getSignalMast(Geometry loc) {
        return xing.getSignalMast(loc);
    }

    public Sensor getSensor(Geometry loc) {
        return xing.getSensor(loc);
    }

    @Nonnull
    public String getSignalAName() {
        return xing.getSignalAName();
    }

    public void setSignalAName(String signalHead) {
        xing.setSignalAName(signalHead);
    }

    @Nonnull
    public String getSignalBName() {
        return xing.getSignalBName();
    }

    public void setSignalBName(String signalHead) {
        xing.setSignalBName(signalHead);
    }

    @Nonnull
    public String getSignalCName() {
        return xing.getSignalCName();
    }

    public void setSignalCName(String signalHead) {
        xing.setSignalCName(signalHead);
    }

    @Nonnull
    public String getSignalDName() {
        return xing.getSignalDName();
    }

    public void setSignalDName(String signalHead) {
        xing.setSignalDName(signalHead);
    }

    public void removeBeanReference(jmri.NamedBean nb) {
        xing.removeBeanReference(nb);
    }

    public String getSignalAMastName() {
        return xing.getSignalAMastName();
    }

    public SignalMast getSignalAMast() {
        return xing.getSignalAMast();
    }

    public void setSignalAMast(String signalMast) {
        xing.setSignalAMast(signalMast);
    }

    public String getSignalBMastName() {
        return xing.getSignalBMastName();
    }

    public SignalMast getSignalBMast() {
        return xing.getSignalBMast();
    }

    public void setSignalBMast(String signalMast) {
        xing.setSignalBMast(signalMast);
    }

    public String getSignalCMastName() {
        return xing.getSignalCMastName();
    }

    public SignalMast getSignalCMast() {
        return xing.getSignalCMast();
    }

    public void setSignalCMast(String signalMast) {
        xing.setSignalCMast(signalMast);
    }

    public String getSignalDMastName() {
        return xing.getSignalDMastName();
    }

    public SignalMast getSignalDMast() {
        return xing.getSignalDMast();
    }

    public void setSignalDMast(String signalMast) {
        xing.setSignalDMast(signalMast);
    }

    public String getSensorAName() {
        return xing.getSensorAName();
    }

    public Sensor getSensorA() {
        return xing.getSensorA();
    }

    public void setSensorAName(String sensorName) {
        xing.setSensorAName(sensorName);
    }

    public String getSensorBName() {
        return xing.getSensorBName();
    }

    public Sensor getSensorB() {
        return xing.getSensorB();
    }

    public void setSensorBName(String sensorName) {
        xing.setSensorBName(sensorName);
    }

    public String getSensorCName() {
        return xing.getSensorCName();
    }

    public Sensor getSensorC() {
        return xing.getSensorC();
    }

    public void setSensorCName(String sensorName) {
        xing.setSensorCName(sensorName);
    }

    public String getSensorDName() {
        return xing.getSensorDName();
    }

    public Sensor getSensorD() {
        return xing.getSensorD();
    }

    public void setSensorDName(String sensorName) {
        xing.setSensorDName(sensorName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        return xing.getConnection(connectionType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(HitPointType connectionType, LayoutTrack o, HitPointType type) throws jmri.JmriException {
        xing.setConnection(connectionType, o, type);
    }

    public LayoutTrack getConnectA() {
        return xing.getConnectA();
    }

    public LayoutTrack getConnectB() {
        return xing.getConnectB();
    }

    public LayoutTrack getConnectC() {
        return xing.getConnectC();
    }

    public LayoutTrack getConnectD() {
        return xing.getConnectD();
    }

    public void setConnectA(LayoutTrack o, HitPointType type) {
        xing.setConnectA(o, type);
    }

    public void setConnectB(LayoutTrack o, HitPointType type) {
        xing.setConnectB(o, type);
    }

    public void setConnectC(LayoutTrack o, HitPointType type) {
        xing.setConnectC(o, type);
    }

    public void setConnectD(LayoutTrack o, HitPointType type) {
        xing.setConnectD(o, type);
    }

    public LayoutBlock getLayoutBlockAC() {
        return xing.getLayoutBlockAC();
    }

    public LayoutBlock getLayoutBlockBD() {
        return xing.getLayoutBlockBD();
    }



    public Point2D getCoordsA() {
        return MathUtil.add(getCoordsCenter(), dispA);
    }

    public Point2D getCoordsB() {
        return MathUtil.add(getCoordsCenter(), dispB);
    }

    public Point2D getCoordsC() {
        return MathUtil.subtract(getCoordsCenter(), dispA);
    }

    public Point2D getCoordsD() {
        return MathUtil.subtract(getCoordsCenter(), dispB);
    }

    /**
     * Get the coordinates for a specified connection type.
     *
     * @param connectionType the connection type
     * @return the coordinates for the specified connection type
     */
    @Override
    public Point2D getCoordsForConnectionType(HitPointType connectionType) {
        Point2D result = getCoordsCenter();
        switch (connectionType) {
            case LEVEL_XING_CENTER:
                break;
            case LEVEL_XING_A:
                result = getCoordsA();
                break;
            case LEVEL_XING_B:
                result = getCoordsB();
                break;
            case LEVEL_XING_C:
                result = getCoordsC();
                break;
            case LEVEL_XING_D:
                result = getCoordsD();
                break;
            default:
                log.error("{}.getCoordsForConnectionType({}); Invalid connection type ",
                        getName(), connectionType); //I18IN
        }
        return result;
    }

    /**
     * @return the bounds of this crossing
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D result;

        Point2D pointA = getCoordsA();
        result = new Rectangle2D.Double(pointA.getX(), pointA.getY(), 0, 0);
        result.add(getCoordsB());
        result.add(getCoordsC());
        result.add(getCoordsD());
        return result;
    }

    /**
     * Add Layout Blocks.
     */
//     @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Null is accepted as a valid value")
//     public void setLayoutBlockAC(LayoutBlock newLayoutBlock) {
//         LayoutBlock blockAC = getLayoutBlockAC();
//         LayoutBlock blockBD = getLayoutBlockBD();
//         if (blockAC != newLayoutBlock) {
//             // block 1 has changed, if old block exists, decrement use
//             if ((blockAC != null) && (blockAC != blockBD)) {
//                 blockAC.decrementUse();
//             }
//             blockAC = newLayoutBlock;
//             if (newLayoutBlock != null) {
//                 namedLayoutBlockAC = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(newLayoutBlock.getUserName(), newLayoutBlock);
//             } else {
//                 namedLayoutBlockAC = null;
//             }
// 
//             // decrement use if block was previously counted
//             if ((blockAC != null) && (blockAC == blockBD)) {
//                 blockAC.decrementUse();
//             }
//         }
//     }

//     @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Null is accepted as a valid value")
//     public void setLayoutBlockBD(LayoutBlock newLayoutBlock) {
//         LayoutBlock blockAC = getLayoutBlockAC();
//         LayoutBlock blockBD = getLayoutBlockBD();
//         if (blockBD != newLayoutBlock) {
//             // block 1 has changed, if old block exists, decrement use
//             if ((blockBD != null) && (blockBD != blockAC)) {
//                 blockBD.decrementUse();
//             }
//             blockBD = newLayoutBlock;
//             if (newLayoutBlock != null) {
//                 namedLayoutBlockBD = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(newLayoutBlock.getUserName(), newLayoutBlock);
//             } else {
//                 namedLayoutBlockBD = null;
//             }
//             // decrement use if block was previously counted
//             if ((blockBD != null) && (blockBD == blockAC)) {
//                 blockBD.decrementUse();
//             }
//         }
// 
//     }
// 
//     public void updateBlockInfo() {
//         LayoutBlock blockAC = getLayoutBlockAC();
//         LayoutBlock blockBD = getLayoutBlockBD();
//         LayoutBlock b1 = null;
//         LayoutBlock b2 = null;
//         if (blockAC != null) {
//             blockAC.updatePaths();
//         }
//         if (connectA != null) {
//             b1 = ((TrackSegment) connectA).getLayoutBlock();
//             if ((b1 != null) && (b1 != blockAC)) {
//                 b1.updatePaths();
//             }
//         }
//         if (connectC != null) {
//             b2 = ((TrackSegment) connectC).getLayoutBlock();
//             if ((b2 != null) && (b2 != blockAC) && (b2 != b1)) {
//                 b2.updatePaths();
//             }
//         }
//         if (blockBD != null) {
//             blockBD.updatePaths();
//         }
//         if (connectB != null) {
//             b1 = ((TrackSegment) connectB).getLayoutBlock();
//             if ((b1 != null) && (b1 != blockBD)) {
//                 b1.updatePaths();
//             }
//         }
//         if (connectD != null) {
//             b2 = ((TrackSegment) connectD).getLayoutBlock();
//             if ((b2 != null) && (b2 != blockBD) && (b2 != b1)) {
//                 b2.updatePaths();
//             }
//         }
//         reCheckBlockBoundary();
//     }
// 
//     void removeSML(SignalMast signalMast) {
//         if (signalMast == null) {
//             return;
//         }
//         if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled() && InstanceManager.getDefault(jmri.SignalMastLogicManager.class).isSignalMastUsed(signalMast)) {
//             SignallingGuiTools.removeSignalMastLogic(null, signalMast);
//         }
//     }

    /**
     * Test if mainline track or not.
     *
     * @return true if either connecting track segment is mainline; Defaults to
     *         not mainline if connecting track segments are missing
     */
    public boolean isMainlineAC() {
        return xing.isMainlineAC();
    }

    public boolean isMainlineBD() {
        return xing.isMainlineBD();
    }

    /*
    * Modify coordinates methods.
     */
    public void setCoordsA(Point2D p) {
        dispA = MathUtil.subtract(p, getCoordsCenter());
    }

    public void setCoordsB(Point2D p) {
        dispB = MathUtil.subtract(p, getCoordsCenter());
    }

    public void setCoordsC(Point2D p) {
        dispA = MathUtil.subtract(getCoordsCenter(), p);
    }

    public void setCoordsD(Point2D p) {
        dispB = MathUtil.subtract(getCoordsCenter(), p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scaleCoords(double xFactor, double yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        super.setCoordsCenter(MathUtil.granulize(MathUtil.multiply(getCoordsCenter(), factor), 1.0));
        dispA = MathUtil.granulize(MathUtil.multiply(dispA, factor), 1.0);
        dispB = MathUtil.granulize(MathUtil.multiply(dispB, factor), 1.0);
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
        // rotate coordinates
        double rotRAD = Math.toRadians(angleDEG);
        double sineRot = Math.sin(rotRAD);
        double cosineRot = Math.cos(rotRAD);

        // rotate displacements around origin {0, 0}
        Point2D center_temp = getCoordsCenter();
        super.setCoordsCenter(MathUtil.zeroPoint2D);
        dispA = rotatePoint(dispA, sineRot, cosineRot);
        dispB = rotatePoint(dispB, sineRot, cosineRot);
        super.setCoordsCenter(center_temp);

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
        Rectangle2D r = trackControlCircleRectAt(hitPoint);
        Point2D p, minPoint = MathUtil.zeroPoint2D;

        double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        double distance, minDistance = POSITIVE_INFINITY;

        //check the center point
        if (!requireUnconnected) {
            p = getCoordsCenter();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.LEVEL_XING_CENTER;
            }
        }

        //check the A connection point
        if (!requireUnconnected || (getConnectA() == null)) {
            p = getCoordsA();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.LEVEL_XING_A;
            }
        }

        //check the B connection point
        if (!requireUnconnected || (getConnectB() == null)) {
            p = getCoordsB();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.LEVEL_XING_B;
            }
        }

        //check the C connection point
        if (!requireUnconnected || (getConnectC() == null)) {
            p = getCoordsC();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.LEVEL_XING_C;
            }
        }

        //check the D connection point
        if (!requireUnconnected || (getConnectD() == null)) {
            p = getCoordsD();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.LEVEL_XING_D;
            }
        }
        if ((useRectangles && !r.contains(minPoint))
                || (!useRectangles && (minDistance > circleRadius))) {
            result = HitPointType.NONE;
        }
        return result;
    }   // findHitPointType

    // initialization instance variables (used when loading a LayoutEditor)
//     public String connectAName = "";
//     public String connectBName = "";
//     public String connectCName = "";
//     public String connectDName = "";
// 
//     public String tLayoutBlockNameAC = "";
//     public String tLayoutBlockNameBD = "";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemove() {
        return xing.canRemove();
    }

//     *
//      * Build a list of sensors, signal heads, and signal masts attached to a
//      * level crossing point.
//      *
//      * @param pointName Specify the point (A-D) or all (All) points.
//      * @return a list of bean reference names.
//      */
//     public ArrayList<String> getBeanReferences(String pointName) {
//         ArrayList<String> references = new ArrayList<>();
//         if (pointName.equals("A") || pointName.equals("All")) {  // NOI18N
//             if (!getSignalAMastName().isEmpty()) {
//                 references.add(getSignalAMastName());
//             }
//             if (!getSensorAName().isEmpty()) {
//                 references.add(getSensorAName());
//             }
//             if (!getSignalAName().isEmpty()) {
//                 references.add(getSignalAName());
//             }
//         }
//         if (pointName.equals("B") || pointName.equals("All")) {  // NOI18N
//             if (!getSignalBMastName().isEmpty()) {
//                 references.add(getSignalBMastName());
//             }
//             if (!getSensorBName().isEmpty()) {
//                 references.add(getSensorBName());
//             }
//             if (!getSignalBName().isEmpty()) {
//                 references.add(getSignalBName());
//             }
//         }
//         if (pointName.equals("C") || pointName.equals("All")) {  // NOI18N
//             if (!getSignalCMastName().isEmpty()) {
//                 references.add(getSignalCMastName());
//             }
//             if (!getSensorCName().isEmpty()) {
//                 references.add(getSensorCName());
//             }
//             if (!getSignalCName().isEmpty()) {
//                 references.add(getSignalCName());
//             }
//         }
//         if (pointName.equals("D") || pointName.equals("All")) {  // NOI18N
//             if (!getSignalDMastName().isEmpty()) {
//                 references.add(getSignalDMastName());
//             }
//             if (!getSensorDName().isEmpty()) {
//                 references.add(getSensorDName());
//             }
//             if (!getSignalDName().isEmpty()) {
//                 references.add(getSignalDName());
//             }
//         }
//         return references;
//     }

    JPopupMenu popup = null;

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected JPopupMenu showPopup(@CheckForNull MouseEvent mouseEvent) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        if (layoutEditor.isEditable()) {
            JMenuItem jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("LevelCrossing")) + getName());
            jmi.setEnabled(false);

            boolean blockACAssigned = false;
            boolean blockBDAssigned = false;
            if (getLayoutBlockAC() == null) {
                jmi = popup.add(Bundle.getMessage("NoBlockX", "AC"));
            } else {
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "AC")) + getLayoutBlockAC().getDisplayName());
                blockACAssigned = true;
            }
            jmi.setEnabled(false);

            if (getLayoutBlockBD() == null) {
                jmi = popup.add(Bundle.getMessage("NoBlockX", "BD"));
            } else {
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "BD")) + getLayoutBlockBD().getDisplayName());
                blockBDAssigned = true;
            }
            jmi.setEnabled(false);

            // if there are any track connections
            if ((getConnectA() != null) || (getConnectB() != null)
                    || (getConnectC() != null) || (getConnectD() != null)) {
                JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections")); // there is no pane opening (which is what ... implies)
                if (getConnectA() != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "A") + getConnectA().getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(getConnectA().getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                                layoutEditor.setSelectionRect(ltv.getBounds());
                                ltv.showPopup();
                            }
                        }
                    });
                }
                if (getConnectB() != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "B") + getConnectB().getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(getConnectB().getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                                layoutEditor.setSelectionRect(ltv.getBounds());
                                ltv.showPopup();
                            }
                        }
                    });
                }
                if (getConnectC() != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "C") + getConnectC().getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(getConnectC().getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                                layoutEditor.setSelectionRect(ltv.getBounds());
                                ltv.showPopup();
                            }
                        }
                    });
                }
                if (getConnectD() != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "D") + getConnectD().getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(getConnectD().getName());
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

            popup.add(new JSeparator(JSeparator.HORIZONTAL));

            JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Hidden"));
            hiddenCheckBoxMenuItem.setSelected(isHidden());
            popup.add(hiddenCheckBoxMenuItem);
            hiddenCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> setHidden(hiddenCheckBoxMenuItem.isSelected()));

            popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editor.editLayoutTrack(LevelXingView.this);
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (canRemove() && layoutEditor.removeLevelXing(xing)) {
                        // Returned true if user did not cancel
                        xing.remove();
                        dispose();
                    }
                }
            });
            if (blockACAssigned && blockBDAssigned) {
                AbstractAction ssaa = new AbstractAction(Bundle.getMessage("SetSignals")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // bring up signals at level crossing tool dialog
                        LayoutEditorToolBarPanel letbp = getLayoutEditorToolBarPanel();
                        layoutEditor.getLETools().
                                setSignalsAtLevelXingFromMenu(xing,
                                        letbp.signalIconEditor,
                                        letbp.signalFrame);
                    }
                };
                JMenu jm = new JMenu(Bundle.getMessage("SignalHeads"));
                if (layoutEditor.getLETools().
                        addLevelXingSignalHeadInfoToMenu(xing, jm)) {
                    jm.add(ssaa);
                    popup.add(jm);
                } else {
                    popup.add(ssaa);
                }
            }

            final String[] boundaryBetween = xing.getBlockBoundaries();
            boolean blockBoundaries = false;
            if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                if (blockACAssigned && !blockBDAssigned) {
                    popup.add(new AbstractAction(Bundle.getMessage("ViewBlockRouting")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlockAC());
                            routeTableAction.actionPerformed(e);
                        }
                    });
                } else if (!blockACAssigned && blockBDAssigned) {
                    popup.add(new AbstractAction(Bundle.getMessage("ViewBlockRouting")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlockBD());
                            routeTableAction.actionPerformed(e);
                        }
                    });
                } else if (blockACAssigned && blockBDAssigned) {
                    JMenu viewRouting = new JMenu(Bundle.getMessage("ViewBlockRouting"));
                    viewRouting.add(new AbstractAction(getLayoutBlockAC().getDisplayName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction(getLayoutBlockAC().getDisplayName(), getLayoutBlockAC());
                            routeTableAction.actionPerformed(e);
                        }
                    });

                    viewRouting.add(new AbstractAction(getLayoutBlockBD().getDisplayName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction(getLayoutBlockBD().getDisplayName(), getLayoutBlockBD());
                            routeTableAction.actionPerformed(e);
                        }
                    });

                    popup.add(viewRouting);
                }
            }

            for (int i = 0; i < 4; i++) {
                if (boundaryBetween[i] != null) {
                    blockBoundaries = true;
                }
            }
            if (blockBoundaries) {
                popup.add(new AbstractAction(Bundle.getMessage("SetSignalMasts")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorToolBarPanel letbp = getLayoutEditorToolBarPanel();
                        layoutEditor.getLETools().
                                setSignalMastsAtLevelXingFromMenu(
                                        xing, boundaryBetween,
                                        letbp.signalFrame);
                    }
                });
                popup.add(new AbstractAction(Bundle.getMessage("SetSensors")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorToolBarPanel letbp = getLayoutEditorToolBarPanel();
                        layoutEditor.getLETools().setSensorsAtLevelXingFromMenu(
                                xing, boundaryBetween,
                                letbp.sensorIconEditor,
                                letbp.sensorFrame);
                    }
                });
            }

            layoutEditor.setShowAlignmentMenu(popup);
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        } else if (!viewAdditionalMenu.isEmpty()) {
            setAdditionalViewPopUpMenu(popup);
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        }
        return popup;
    }   // showPopup

//     public String[] getBlockBoundaries() {
//         final String[] boundaryBetween = new String[4];
// 
//         String blockNameAC = getBlockNameAC();
//         String blockNameBD = getBlockNameBD();
// 
//         LayoutBlock blockAC = getLayoutBlockAC();
//         LayoutBlock blockBD = getLayoutBlockAC();
// 
//         if (!blockNameAC.isEmpty() && (blockAC != null)) {
//             if ((connectA instanceof TrackSegment) && (((TrackSegment) connectA).getLayoutBlock() != blockAC)) {
//                 try {
//                     boundaryBetween[0] = (((TrackSegment) connectA).getLayoutBlock().getDisplayName() + " - " + blockAC.getDisplayName());
//                 } catch (java.lang.NullPointerException e) {
//                     //Can be considered normal if tracksegement hasn't yet been allocated a block
//                     log.debug("TrackSegement at connection A doesn't contain a layout block");
//                 }
//             }
//             if ((connectC instanceof TrackSegment) && (((TrackSegment) connectC).getLayoutBlock() != blockAC)) {
//                 try {
//                     boundaryBetween[2] = (((TrackSegment) connectC).getLayoutBlock().getDisplayName() + " - " + blockAC.getDisplayName());
//                 } catch (java.lang.NullPointerException e) {
//                     //Can be considered normal if tracksegement hasn't yet been allocated a block
//                     log.debug("TrackSegement at connection C doesn't contain a layout block");
//                 }
//             }
//         }
//         if (!blockNameBD.isEmpty() && (blockBD != null)) {
//             if ((connectB instanceof TrackSegment) && (((TrackSegment) connectB).getLayoutBlock() != blockBD)) {
//                 try {
//                     boundaryBetween[1] = (((TrackSegment) connectB).getLayoutBlock().getDisplayName() + " - " + blockBD.getDisplayName());
//                 } catch (java.lang.NullPointerException e) {
//                     //Can be considered normal if tracksegement hasn't yet been allocated a block
//                     log.debug("TrackSegement at connection B doesn't contain a layout block");
//                 }
//             }
//             if ((connectD instanceof TrackSegment) && (((TrackSegment) connectD).getLayoutBlock() != blockBD)) {
//                 try {
//                     boundaryBetween[3] = (((TrackSegment) connectD).getLayoutBlock().getDisplayName() + " - " + blockBD.getDisplayName());
//                 } catch (java.lang.NullPointerException e) {
//                     //Can be considered normal if tracksegement hasn't yet been allocated a block
//                     log.debug("TrackSegement at connection D doesn't contain a layout block");
//                 }
//             }
//         }
//         return boundaryBetween;
//     }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove().
     */
    public void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
    }

    /**
     * Remove this object from display and persistance.
     */
//     public void remove() {
//         // remove from persistance by flagging inactive
//         active = false;
//     }
// 
//     boolean active = true;
// 
//     *
//      * "active" means that the object is still displayed, and should be stored.
//      */
//     public boolean isActive() {
//         return active;
//     }

//     ArrayList<SignalMast> sml = new ArrayList<>();
// 
//     public void addSignalMastLogic(SignalMast sm) {
//         if (sml.contains(sm)) {
//             return;
//         }
//         if (sml.isEmpty()) {
//             sml.add(sm);
//             return;
//         }
//         SignalMastLogic sl = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sm);
//         for (SignalMast signalMast : sml) {
//             SignalMastLogic s = InstanceManager.getDefault(SignalMastLogicManager.class).getSignalMastLogic(signalMast);
//             if (s != null) {
//                 s.setConflictingLogic(sm, xing);
//             }
//             sl.setConflictingLogic(signalMast, xing);
//         }
//         sml.add(sm);
//     }
// 
//     public void removeSignalMastLogic(SignalMast sm) {
//         if (!sml.contains(sm)) {
//             return;
//         }
//         sml.remove(sm);
//         if (sml.isEmpty()) {
//             return;
//         }
//         for (int i = 0; i < sml.size(); i++) {
//             SignalMastLogic s = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sm);
//             if (s != null) {
//                 s.removeConflictingLogic(sm, xing);
//             }
//         }
//     }

    ArrayList<JMenuItem> editAdditionalMenu = new ArrayList<>(0);
    ArrayList<JMenuItem> viewAdditionalMenu = new ArrayList<>(0);

    public void addEditPopUpMenu(JMenuItem menu) {
        if (!editAdditionalMenu.contains(menu)) {
            editAdditionalMenu.add(menu);
        }
    }

    public void addViewPopUpMenu(JMenuItem menu) {
        if (!viewAdditionalMenu.contains(menu)) {
            viewAdditionalMenu.add(menu);
        }
    }

    public void setAdditionalEditPopUpMenu(JPopupMenu popup) {
        if (editAdditionalMenu.isEmpty()) {
            return;
        }
        popup.addSeparator();
        for (JMenuItem mi : editAdditionalMenu) {
            popup.add(mi);
        }
    }

    public void setAdditionalViewPopUpMenu(JPopupMenu popup) {
        if (viewAdditionalMenu.isEmpty()) {
            return;
        }
        popup.addSeparator();
        for (JMenuItem mi : viewAdditionalMenu) {
            popup.add(mi);
        }
    }

    /**
     * Draw track decorations.
     * 
     * This type of track has none, so this method is empty.
     */
    @Override
    protected void drawDecorations(Graphics2D g2) {}

    /**
     * Draw this level crossing.
     *
     * @param g2 the graphics port to draw to
     */
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        if (isMain == isMainlineAC()) {
            if (isBlock) {
                setColorForTrackBlock(g2, getLayoutBlockAC());
            }
            g2.draw(new Line2D.Double(getCoordsA(), getCoordsC()));
        }
        if (isMain == isMainlineBD()) {
            if (isBlock) {
                setColorForTrackBlock(g2, getLayoutBlockBD());
            }
            g2.draw(new Line2D.Double(getCoordsB(), getCoordsD()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        Point2D pA = getCoordsA();
        Point2D pB = getCoordsB();
        Point2D pC = getCoordsC();
        Point2D pD = getCoordsD();
        Point2D pM = getCoordsCenter();

        Point2D vAC = MathUtil.normalize(MathUtil.subtract(pC, pA), railDisplacement);
        double dirAC_DEG = MathUtil.computeAngleDEG(pA, pC);
        Point2D vACo = MathUtil.orthogonal(vAC);
        Point2D pAL = MathUtil.subtract(pA, vACo);
        Point2D pAR = MathUtil.add(pA, vACo);
        Point2D pCL = MathUtil.subtract(pC, vACo);
        Point2D pCR = MathUtil.add(pC, vACo);

        Point2D vBD = MathUtil.normalize(MathUtil.subtract(pD, pB), railDisplacement);
        double dirBD_DEG = MathUtil.computeAngleDEG(pB, pD);
        Point2D vBDo = MathUtil.orthogonal(vBD);
        Point2D pBL = MathUtil.subtract(pB, vBDo);
        Point2D pBR = MathUtil.add(pB, vBDo);
        Point2D pDL = MathUtil.subtract(pD, vBDo);
        Point2D pDR = MathUtil.add(pD, vBDo);

        double deltaDEG = MathUtil.absDiffAngleDEG(dirAC_DEG, dirBD_DEG);
        double deltaRAD = Math.toRadians(deltaDEG);

        double hypotK = railDisplacement / Math.cos((PI - deltaRAD) / 2.0);
        double hypotV = railDisplacement / Math.cos(deltaRAD / 2.0);

        log.debug("dir AC: {}, BD: {}, diff: {}", dirAC_DEG, dirBD_DEG, deltaDEG);

        Point2D vDisK = MathUtil.normalize(MathUtil.add(vAC, vBD), hypotK);
        Point2D vDisV = MathUtil.normalize(MathUtil.orthogonal(vDisK), hypotV);
        Point2D pKL = MathUtil.subtract(pM, vDisK);
        Point2D pKR = MathUtil.add(pM, vDisK);
        Point2D pVL = MathUtil.subtract(pM, vDisV);
        Point2D pVR = MathUtil.add(pM, vDisV);

        if (isMain == isMainlineAC()) {
            // this is the *2.0 vector (rail gap) for the AC diamond parts
            Point2D vAC2 = MathUtil.normalize(vAC, 2.0);
            // KL toward C, VR toward A, VL toward C and KR toward A
            Point2D pKLtC = MathUtil.add(pKL, vAC2);
            Point2D pVRtA = MathUtil.subtract(pVR, vAC2);
            Point2D pVLtC = MathUtil.add(pVL, vAC2);
            Point2D pKRtA = MathUtil.subtract(pKR, vAC2);

            // draw right AC rail: AR====KL == VR====CR
            g2.draw(new Line2D.Double(pAR, pKL));
            g2.draw(new Line2D.Double(pKLtC, pVRtA));
            g2.draw(new Line2D.Double(pVR, pCR));

            // draw left AC rail: AL====VL == KR====CL
            g2.draw(new Line2D.Double(pAL, pVL));
            g2.draw(new Line2D.Double(pVLtC, pKRtA));
            g2.draw(new Line2D.Double(pKR, pCL));
        }
        if (isMain == isMainlineBD()) {
            // this is the *2.0 vector (rail gap) for the BD diamond parts
            Point2D vBD2 = MathUtil.normalize(vBD, 2.0);
            // VR toward D, KR toward B, KL toward D and VL toward B
            Point2D pVRtD = MathUtil.add(pVR, vBD2);
            Point2D pKRtB = MathUtil.subtract(pKR, vBD2);
            Point2D pKLtD = MathUtil.add(pKL, vBD2);
            Point2D pVLtB = MathUtil.subtract(pVL, vBD2);

            // draw right BD rail: BR====VR == KR====DR
            g2.draw(new Line2D.Double(pBR, pVR));
            g2.draw(new Line2D.Double(pVRtD, pKRtB));
            g2.draw(new Line2D.Double(pKR, pDR));

            // draw left BD rail: BL====KL == VL====DL
            g2.draw(new Line2D.Double(pBL, pKL));
            g2.draw(new Line2D.Double(pKLtD, pVLtB));
            g2.draw(new Line2D.Double(pVL, pDL));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.LEVEL_XING_A))
                && (getConnectA() == null)) {
            g2.fill(trackControlCircleAt(getCoordsA()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.LEVEL_XING_B))
                && (getConnectB() == null)) {
            g2.fill(trackControlCircleAt(getCoordsB()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.LEVEL_XING_C))
                && (getConnectC() == null)) {
            g2.fill(trackControlCircleAt(getCoordsC()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.LEVEL_XING_D))
                && (getConnectD() == null)) {
            g2.fill(trackControlCircleAt(getCoordsD()));
        }
    }

    @Override
    protected void drawEditControls(Graphics2D g2) {
        g2.setColor(layoutEditor.getDefaultTrackColorColor());
        g2.draw(trackEditControlCircleAt(getCoordsCenter()));

        if (getConnectA() == null) {
            g2.setColor(Color.magenta);
        } else {
            g2.setColor(Color.blue);
        }
        g2.draw(layoutEditor.layoutEditorControlRectAt(getCoordsA()));

        if (getConnectB() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.layoutEditorControlRectAt(getCoordsB()));

        if (getConnectC() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.layoutEditorControlRectAt(getCoordsC()));

        if (getConnectD() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.layoutEditorControlRectAt(getCoordsD()));
    }

    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        // LevelXings don't have turnout controls...
        // nothing to see here... move along...
    }

    /*
    * {@inheritDoc}
     */
    @Override
    public void reCheckBlockBoundary() {
        // nothing to see here... move along...
    }

    /*
    * {@inheritDoc} temporary
     */
    @Override
    protected ArrayList<LayoutConnectivity> getLayoutConnectivity() {
        // nothing to see here... move along...
        return null;
    }

    /**
     * {@inheritDoc}
     */
     @Override
     public List<HitPointType> checkForFreeConnections() {
        throw new IllegalArgumentException("should have called Object instead of view temporary");
//         List<HitPointType> result = new ArrayList<>();
// 
//         //check the A connection point
//         if (getConnectA() == null) {
//             result.add(HitPointType.LEVEL_XING_A);
//         }
// 
//         //check the B connection point
//         if (getConnectB() == null) {
//             result.add(HitPointType.LEVEL_XING_B);
//         }
// 
//         //check the C connection point
//         if (getConnectC() == null) {
//             result.add(HitPointType.LEVEL_XING_C);
//         }
// 
//         //check the D connection point
//         if (getConnectD() == null) {
//             result.add(HitPointType.LEVEL_XING_D);
//         }
//         return result;
     }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkForUnAssignedBlocks() {
//         return ((getLayoutBlockAC() != null) && (getLayoutBlockBD() != null));
        throw new IllegalArgumentException("should have called Object instead of View temporary");
   }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkForNonContiguousBlocks(
            @Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetsMap) {
        throw new IllegalArgumentException("should have called Object instead of View temporary");

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

        // We're only using a map here because it's convient to
        // use it to pair up blocks and connections
//         Map<LayoutTrack, String> blocksAndTracksMap = new HashMap<>();
//         if ((getLayoutBlockAC() != null) && (connectA != null)) {
//             blocksAndTracksMap.put(connectA, getLayoutBlockAC().getDisplayName());
//         }
//         if ((getLayoutBlockAC() != null) && (connectC != null)) {
//             blocksAndTracksMap.put(connectC, getLayoutBlockAC().getDisplayName());
//         }
//         if ((getLayoutBlockBD() != null) && (connectB != null)) {
//             blocksAndTracksMap.put(connectB, getLayoutBlockBD().getDisplayName());
//         }
//         if ((getLayoutBlockBD() != null) && (connectD != null)) {
//             blocksAndTracksMap.put(connectD, getLayoutBlockBD().getDisplayName());
//         }
// 
//         List<Set<String>> TrackNameSets = null;
//         Set<String> TrackNameSet = null;
//         for (Map.Entry<LayoutTrack, String> entry : blocksAndTracksMap.entrySet()) {
//             LayoutTrack theConnect = entry.getKey();
//             String theBlockName = entry.getValue();
// 
//             TrackNameSet = null;    // assume not found (pessimist!)
//             TrackNameSets = blockNamesToTrackNameSetsMap.get(theBlockName);
//             if (TrackNameSets != null) { // (#1)
//                 for (Set<String> checkTrackNameSet : TrackNameSets) {
//                     if (checkTrackNameSet.contains(getName())) { // (#2)
//                         TrackNameSet = checkTrackNameSet;
//                         break;
//                     }
//                 }
//             } else {    // (#3)
//                 log.debug("*New block ('{}') trackNameSets", theBlockName);
//                 TrackNameSets = new ArrayList<>();
//                 blockNamesToTrackNameSetsMap.put(theBlockName, TrackNameSets);
//             }
//             if (TrackNameSet == null) {
//                 TrackNameSet = new LinkedHashSet<>();
//                 TrackNameSets.add(TrackNameSet);
//             }
//             if (TrackNameSet.add(getName())) {
//                 log.debug("*    Add track ''{}'' to trackNameSet for block ''{}''", getName(), theBlockName);
//             }
//             theConnect.collectContiguousTracksNamesInBlockNamed(theBlockName, TrackNameSet);
//         }
    }   // collectContiguousTracksNamesInBlockNamed

    /**
     * {@inheritDoc}
     */
     @Override
     public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
        throw new IllegalArgumentException("should have called Object instead of View temporary");
//         if (!TrackNameSet.contains(getName())) {
//             // check all the matching blocks in this track and...
//             //  #1) add us to TrackNameSet and...
//             //  #2) flood them
//             //check the AC blockName
//             if (getBlockNameAC().equals(blockName)) {
//                 // if we are added to the TrackNameSet
//                 if (TrackNameSet.add(getName())) {
//                     log.debug("*    Add track ''{}'for block ''{}''", getName(), blockName);
//                 }
//                 // it's time to play... flood your neighbours!
//                 if (connectA != null) {
//                     connectA.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
//                 }
//                 if (connectC != null) {
//                     connectC.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
//                 }
//             }
//             //check the BD blockName
//             if (getBlockNameBD().equals(blockName)) {
//                 // if we are added to the TrackNameSet
//                 if (TrackNameSet.add(getName())) {
//                     log.debug("*    Add track ''{}''for block ''{}''", getName(), blockName);
//                 }
//                 // it's time to play... flood your neighbours!
//                 if (connectB != null) {
//                     connectB.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
//                 }
//                 if (connectD != null) {
//                     connectD.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
//                 }
//             }
//         }
    }

    /**
     * {@inheritDoc}
     */
     @Override
     public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
        throw new IllegalArgumentException("should have called Object instead of View temporary");
//         setLayoutBlockAC(layoutBlock);
//         setLayoutBlockBD(layoutBlock);
     }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LevelXingView.class);
}
