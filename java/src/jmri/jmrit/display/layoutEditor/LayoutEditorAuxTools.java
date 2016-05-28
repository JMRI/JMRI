package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.ResourceBundle;
import jmri.BeanSetting;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LayoutEditorAuxTools provides tools making use of layout connectivity
 * available in Layout Editor panels. (More tools are in
 * LayoutEditorTools.java.)
 * <P>
 * This module manages block connectivity for its associated LayoutEditor.
 * <P>
 * The tools in this module are accessed via the Tools menu in Layout Editor, or
 * directly from LayoutEditor or LayoutEditor specific modules.
 * <P>
 * @author Dave Duchamp Copyright (c) 2008
 */
public class LayoutEditorAuxTools {

    // Defined text resource
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    // constants
    // operational instance variables 
    private LayoutEditor layoutEditor = null;
    private ArrayList<LayoutConnectivity> cList = new ArrayList<LayoutConnectivity>(); //LayoutConnectivity list
    private boolean blockConnectivityChanged = false;  // true if block connectivity may have changed
    private boolean initialized = false;

    // constructor method
    public LayoutEditorAuxTools(LayoutEditor thePanel) {
        layoutEditor = thePanel;
    }

    // register a change in block connectivity that may require an update of connectivity list
    public void setBlockConnectivityChanged() {
        blockConnectivityChanged = true;
    }

    /**
     * Get Connectivity involving a specific Layout Block
     * <P>
     * This routine returns an ArrayList of BlockConnectivity objects involving
     * the specified LayoutBlock.
     */
    public ArrayList<LayoutConnectivity> getConnectivityList(LayoutBlock blk) {
        if (!initialized) {
            initializeBlockConnectivity();
        }
        if (blockConnectivityChanged) {
            updateBlockConnectivity();
        }
        ArrayList<LayoutConnectivity> retList = new ArrayList<LayoutConnectivity>();
        for (int i = 0; i < cList.size(); i++) {
            LayoutConnectivity lc = cList.get(i);
            if ((lc.getBlock1() == blk) || (lc.getBlock2() == blk)) {
                retList.add(lc);
            }
        }
        return (retList);
    }

    /**
     * Initializes the block connectivity (block boundaries) for a Layout Editor
     * panel.
     * <P>
     * This routine sets up the LayoutConnectivity objects needed to show the
     * current connectivity. It gets its information from arrays contained in
     * LayoutEditor.
     * <P>
     * One LayoutConnectivity object is created for each block boundary --
     * connection points where two blocks join. Block boundaries can occur
     * whereever a track segment in one block joins with: 1) a track segment in
     * another block -OR- 2) a connection point in a layout turnout in another
     * block -OR- 3) a connection point in a level crossing in another block.
     * <P>
     * The first block is always a track segment. The direction set in the
     * LayoutConnectivity is the direction of the track segment alone for cases
     * 2) and 3) above. For case 1), two track segments, the direction reflects
     * an "average" over the two track segments. See LayoutConnectivity for the
     * allowed values of direction.
     *
     */
    public void initializeBlockConnectivity() {
        if (initialized) {
            log.error("Call to initialize a connectivity list that has already been initialized");
            return;
        }
        cList = new ArrayList<LayoutConnectivity>();
        LayoutBlock blk1 = null;
        LayoutBlock blk2 = null;
        LayoutConnectivity c = null;
        Point2D p1;
        Point2D p2;
        // Check for block boundaries at positionable points.
        if (layoutEditor.pointList.size() > 0) {
            PositionablePoint p = null;
            TrackSegment ts1 = null;
            TrackSegment ts2 = null;
            for (int i = 0; i < layoutEditor.pointList.size(); i++) {
                p = layoutEditor.pointList.get(i);
                if (p.getType() == PositionablePoint.ANCHOR) {
                    // within PositionablePoints, only ANCHOR points can be block boundaries
                    ts1 = p.getConnect1();
                    ts2 = p.getConnect2();
                    if ((ts1 != null) && (ts2 != null)) {
                        blk1 = ts1.getLayoutBlock();
                        blk2 = ts2.getLayoutBlock();
                        if ((blk1 != null) && (blk2 != null) && (blk1 != blk2)) {
                            // this is a block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(blk1, blk2);
                            // determine direction from block 1 to block 2
                            if (ts1.getConnect1() == p) {
                                p1 = layoutEditor.getCoords(
                                        ts1.getConnect2(), ts1.getType2());
                            } else {
                                p1 = layoutEditor.getCoords(ts1.getConnect1(), ts1.getType1());
                            }
                            if (ts2.getConnect1() == p) {
                                p2 = layoutEditor.getCoords(
                                        ts2.getConnect2(), ts2.getType2());
                            } else {
                                p2 = layoutEditor.getCoords(ts2.getConnect1(), ts2.getType1());
                            }
                            c.setDirection(computeDirection(p1, p2));
                            // save Connections
                            c.setConnections(ts1, ts2, LayoutEditor.TRACK, p);
                            // add to list
                            cList.add(c);
                        }
                    }
                }
                if (p.getType() == PositionablePoint.EDGE_CONNECTOR) {

                    //Need to find a way to compute the direction for this for a split over the panel
                    ts1 = p.getConnect1();
                    ts2 = p.getConnect2();
                    if ((ts1 != null) && (ts2 != null)) {
                        blk1 = ts1.getLayoutBlock();
                        blk2 = ts2.getLayoutBlock();
                        if ((blk1 != null) && (blk2 != null) && (blk1 != blk2)) {
                            // this is a block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(blk1, blk2);

                            // determine direction from block 1 to block 2
                            if (ts1.getConnect1() == p) {
                                p1 = layoutEditor.getCoords(
                                        ts1.getConnect2(), ts1.getType2());
                            } else {
                                p1 = layoutEditor.getCoords(ts1.getConnect1(), ts1.getType1());
                            }
                            //In this instance work out the direction of the first track relative to the positionable poin.

                            c.setDirection(computeDirection(p1, p.getCoords()));
                            // save Connections
                            c.setConnections(ts1, ts2, LayoutEditor.TRACK, p);
                            // add to list
                            cList.add(c);
                        }
                    }
                }
            }
        }
        // Check for block boundaries at layout turnouts and level crossings
        if (layoutEditor.trackList.size() > 0) {
            LayoutTurnout lt = null;
            int type = 0;
            LevelXing lx = null;
            LayoutSlip ls = null;
            TrackSegment ts = null;
            for (int i = 0; i < layoutEditor.trackList.size(); i++) {
                ts = layoutEditor.trackList.get(i);
                // ensure that block is assigned
                blk1 = ts.getLayoutBlock();
                if (blk1 != null) {
                    // check first connection for turnout or level crossing
                    if ((ts.getType1() >= LayoutEditor.TURNOUT_A)
                            && (ts.getType1() <= LayoutEditor.LEVEL_XING_D)) {
                        // have connection to turnout or level crossing							
                        if (ts.getType1() <= LayoutEditor.TURNOUT_D) {
                            // have connection to a turnout, is block different
                            lt = (LayoutTurnout) ts.getConnect1();
                            type = ts.getType1();
                            blk2 = lt.getLayoutBlock();
                            if (lt.getTurnoutType() > LayoutTurnout.WYE_TURNOUT) {
                                // not RH, LH, or WYE turnout - other blocks possible
                                if ((type == LayoutEditor.TURNOUT_B) && (lt.getLayoutBlockB() != null)) {
                                    blk2 = lt.getLayoutBlockB();
                                }
                                if ((type == LayoutEditor.TURNOUT_C) && (lt.getLayoutBlockC() != null)) {
                                    blk2 = lt.getLayoutBlockC();
                                }
                                if ((type == LayoutEditor.TURNOUT_D) && (lt.getLayoutBlockD() != null)) {
                                    blk2 = lt.getLayoutBlockD();
                                }
                            }
                            if ((blk2 != null) && (blk1 != blk2)) {
                                // have a block boundary, create a LayoutConnectivity
                                c = new LayoutConnectivity(blk1, blk2);
                                c.setConnections(ts, lt, type, null);
                                c.setDirection(computeDirection(layoutEditor.getCoords(ts.getConnect2(),
                                        ts.getType2()), layoutEditor.getCoords(ts.getConnect1(), type)));
                                // add to list
                                cList.add(c);
                            }
                        } else {
                            // have connection to a level crossing
                            lx = (LevelXing) ts.getConnect1();
                            type = ts.getType1();
                            if ((type == LayoutEditor.LEVEL_XING_A) || (type == LayoutEditor.LEVEL_XING_C)) {
                                blk2 = lx.getLayoutBlockAC();
                            } else {
                                blk2 = lx.getLayoutBlockBD();
                            }
                            if ((blk2 != null) && (blk1 != blk2)) {
                                // have a block boundary, create a LayoutConnectivity
                                c = new LayoutConnectivity(blk1, blk2);
                                c.setConnections(ts, lx, type, null);
                                c.setDirection(computeDirection(layoutEditor.getCoords(ts.getConnect2(),
                                        ts.getType2()), layoutEditor.getCoords(ts.getConnect1(), type)));
                                // add to list
                                cList.add(c);
                            }
                        }
                    } else if ((ts.getType1() >= LayoutEditor.SLIP_A) && (ts.getType1() <= LayoutEditor.SLIP_D)) {
                        // have connection to a slip crossing
                        ls = (LayoutSlip) ts.getConnect1();
                        type = ts.getType1();
                        blk2 = ls.getLayoutBlock();
                        if ((blk2 != null) && (blk1 != blk2)) {
                            // have a block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(blk1, blk2);
                            c.setConnections(ts, ls, type, null);
                            c.setDirection(computeDirection(layoutEditor.getCoords(ts.getConnect2(),
                                    ts.getType2()), layoutEditor.getCoords(ts.getConnect1(), type)));
                            // add to list
                            cList.add(c);
                        }
                    }
                    // check second connection for turnout or level crossing
                    if ((ts.getType2() >= LayoutEditor.TURNOUT_A)
                            && (ts.getType2() <= LayoutEditor.LEVEL_XING_D)) {
                        // have connection to turnout or level crossing							
                        if (ts.getType2() <= LayoutEditor.TURNOUT_D) {
                            // have connection to a turnout
                            lt = (LayoutTurnout) ts.getConnect2();
                            type = ts.getType2();
                            blk2 = lt.getLayoutBlock();
                            if (lt.getTurnoutType() > LayoutTurnout.WYE_TURNOUT) {
                                // not RH, LH, or WYE turnout - other blocks possible
                                if ((type == LayoutEditor.TURNOUT_B) && (lt.getLayoutBlockB() != null)) {
                                    blk2 = lt.getLayoutBlockB();
                                }
                                if ((type == LayoutEditor.TURNOUT_C) && (lt.getLayoutBlockC() != null)) {
                                    blk2 = lt.getLayoutBlockC();
                                }
                                if ((type == LayoutEditor.TURNOUT_D) && (lt.getLayoutBlockD() != null)) {
                                    blk2 = lt.getLayoutBlockD();
                                }
                            }
                            if ((blk2 != null) && (blk1 != blk2)) {
                                // have a block boundary, create a LayoutConnectivity
                                c = new LayoutConnectivity(blk1, blk2);
                                c.setConnections(ts, lt, type, null);
                                c.setDirection(computeDirection(layoutEditor.getCoords(ts.getConnect1(),
                                        ts.getType1()), layoutEditor.getCoords(ts.getConnect2(), type)));
                                // add to list
                                cList.add(c);
                            }
                        } else {
                            // have connection to a level crossing
                            lx = (LevelXing) ts.getConnect2();
                            type = ts.getType2();
                            if ((type == LayoutEditor.LEVEL_XING_A) || (type == LayoutEditor.LEVEL_XING_C)) {
                                blk2 = lx.getLayoutBlockAC();
                            } else {
                                blk2 = lx.getLayoutBlockBD();
                            }
                            if ((blk2 != null) && (blk1 != blk2)) {
                                // have a block boundary, create a LayoutConnectivity
                                c = new LayoutConnectivity(blk1, blk2);
                                c.setConnections(ts, lx, type, null);
                                c.setDirection(computeDirection(layoutEditor.getCoords(ts.getConnect1(),
                                        ts.getType1()), layoutEditor.getCoords(ts.getConnect2(), type)));
                                // add to list
                                cList.add(c);
                            }
                        }
                    } else if ((ts.getType2() >= LayoutEditor.SLIP_A) && (ts.getType2() <= LayoutEditor.SLIP_D)) {
                        // have connection to a slip crossing
                        ls = (LayoutSlip) ts.getConnect2();
                        type = ts.getType2();
                        blk2 = ls.getLayoutBlock();
                        if ((blk2 != null) && (blk1 != blk2)) {
                            // have a block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(blk1, blk2);
                            c.setConnections(ts, ls, type, null);
                            c.setDirection(computeDirection(layoutEditor.getCoords(ts.getConnect1(),
                                    ts.getType1()), layoutEditor.getCoords(ts.getConnect2(), type)));
                            // add to list
                            cList.add(c);
                        }
                    } // this is routinely reached in normal operation with 
                }
            }
        }
        // check for block boundaries internal to crossover turnouts
        if (layoutEditor.turnoutList.size() > 0) {
            LayoutTurnout lt = null;
            for (int i = 0; i < layoutEditor.turnoutList.size(); i++) {
                lt = layoutEditor.turnoutList.get(i);
                // check for layout turnout
                if ((lt.getTurnoutType() >= LayoutTurnout.DOUBLE_XOVER)
                        && (lt.getLayoutBlock() != null)) {
                    // have a crossover turnout with at least one block, check for multiple blocks
                    if ((lt.getLayoutBlockB() != lt.getLayoutBlock()) || (lt.getLayoutBlockC() != lt.getLayoutBlock())
                            || (lt.getLayoutBlockD() != lt.getLayoutBlock())) {
                        // have multiple blocks and therefore internal block boundaries
                        if (lt.getLayoutBlock() != lt.getLayoutBlockB()) {
                            // have a AB block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(lt.getLayoutBlock(), lt.getLayoutBlockB());
                            c.setXoverBoundary(lt, LayoutConnectivity.XOVER_BOUNDARY_AB);
                            c.setDirection(computeDirection(lt.getCoordsA(), lt.getCoordsB()));
                            cList.add(c);
                        }
                        if ((lt.getTurnoutType() != LayoutTurnout.LH_XOVER)
                                && (lt.getLayoutBlock() != lt.getLayoutBlockC())) {
                            // have a AC block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(lt.getLayoutBlock(), lt.getLayoutBlockC());
                            c.setXoverBoundary(lt, LayoutConnectivity.XOVER_BOUNDARY_AC);
                            c.setDirection(computeDirection(lt.getCoordsA(), lt.getCoordsC()));
                            cList.add(c);
                        }
                        if (lt.getLayoutBlockC() != lt.getLayoutBlockD()) {
                            // have a CD block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(lt.getLayoutBlockC(), lt.getLayoutBlockD());
                            c.setXoverBoundary(lt, LayoutConnectivity.XOVER_BOUNDARY_CD);
                            c.setDirection(computeDirection(lt.getCoordsC(), lt.getCoordsD()));
                            cList.add(c);
                        }
                        if ((lt.getTurnoutType() != LayoutTurnout.RH_XOVER)
                                && (lt.getLayoutBlockB() != lt.getLayoutBlockD())) {
                            // have a BD block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(lt.getLayoutBlockB(), lt.getLayoutBlockD());
                            c.setXoverBoundary(lt, LayoutConnectivity.XOVER_BOUNDARY_BD);
                            c.setDirection(computeDirection(lt.getCoordsB(), lt.getCoordsD()));
                            cList.add(c);
                        }
                    }
                }
            }
        }
        initialized = true;
    }

    /**
     * Updates the block connectivity (block boundaries) for a Layout Editor
     * panel after changes may have been made.
     */
    private void updateBlockConnectivity() {
        int sz = cList.size();
        boolean[] found = new boolean[sz];
        for (int i = 0; i < sz; i++) {
            found[i] = false;
        }
        LayoutBlock blk1 = null;
        LayoutBlock blk2 = null;
        LayoutConnectivity c = null;
        Point2D p1;
        Point2D p2;
        // Check for block boundaries at positionable points.
        if (layoutEditor.pointList.size() > 0) {
            PositionablePoint p = null;
            TrackSegment ts1 = null;
            TrackSegment ts2 = null;
            for (int i = 0; i < layoutEditor.pointList.size(); i++) {
                p = layoutEditor.pointList.get(i);
                if (p.getType() == PositionablePoint.ANCHOR) {
                    // within PositionablePoints, only ANCHOR points can be block boundaries
                    ts1 = p.getConnect1();
                    ts2 = p.getConnect2();
                    if ((ts1 != null) && (ts2 != null)) {
                        blk1 = ts1.getLayoutBlock();
                        blk2 = ts2.getLayoutBlock();
                        if ((blk1 != null) && (blk2 != null) && (blk1 != blk2)) {
                            // this is a block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(blk1, blk2);
                            // determine direction from block 1 to block 2
                            if (ts1.getConnect1() == p) {
                                p1 = layoutEditor.getCoords(
                                        ts1.getConnect2(), ts1.getType2());
                            } else {
                                p1 = layoutEditor.getCoords(ts1.getConnect1(), ts1.getType1());
                            }
                            if (ts2.getConnect1() == p) {
                                p2 = layoutEditor.getCoords(
                                        ts2.getConnect2(), ts2.getType2());
                            } else {
                                p2 = layoutEditor.getCoords(ts2.getConnect1(), ts2.getType1());
                            }
                            c.setDirection(computeDirection(p1, p2));
                            // save Connections
                            c.setConnections(ts1, ts2, LayoutEditor.TRACK, p);
                            // add to list, if not already present
                            checkConnectivity(c, found);
                        }
                    }
                } else if (p.getType() == PositionablePoint.EDGE_CONNECTOR) {

                    //Need to find a way to compute the direction for this for a split over the panel
                    ts1 = p.getConnect1();
                    ts2 = p.getConnect2();
                    if ((ts1 != null) && (ts2 != null)) {
                        blk1 = ts1.getLayoutBlock();
                        blk2 = ts2.getLayoutBlock();
                        if ((blk1 != null) && (blk2 != null) && (blk1 != blk2)) {
                            // this is a block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(blk1, blk2);

                            // determine direction from block 1 to block 2
                            if (ts1.getConnect1() == p) {
                                p1 = layoutEditor.getCoords(
                                        ts1.getConnect2(), ts1.getType2());
                            } else {
                                p1 = layoutEditor.getCoords(ts1.getConnect1(), ts1.getType1());
                            }
                            //In this instance work out the direction of the first track relative to the positionable poin.

                            c.setDirection(computeDirection(p1, p.getCoords()));
                            // save Connections
                            c.setConnections(ts1, ts2, LayoutEditor.TRACK, p);
                            // add to list, if not already present
                            checkConnectivity(c, found);
                        }
                    }
                }
            }
        }
        // Check for block boundaries at layout turnouts and level crossings
        if (layoutEditor.trackList.size() > 0) {
            LayoutTurnout lt = null;
            int type = 0;
            LevelXing lx = null;
            LayoutSlip ls = null;
            TrackSegment ts = null;
            for (int i = 0; i < layoutEditor.trackList.size(); i++) {
                ts = layoutEditor.trackList.get(i);
                // ensure that block is assigned
                blk1 = ts.getLayoutBlock();
                if (blk1 != null) {
                    // check first connection for turnout or level crossing
                    if ((ts.getType1() >= LayoutEditor.TURNOUT_A)
                            && (ts.getType1() <= LayoutEditor.LEVEL_XING_D)) {
                        // have connection to turnout or level crossing							
                        if (ts.getType1() <= LayoutEditor.TURNOUT_D) {
                            // have connection to a turnout, is block different
                            lt = (LayoutTurnout) ts.getConnect1();
                            type = ts.getType1();
                            blk2 = lt.getLayoutBlock();
                            if (lt.getTurnoutType() > LayoutTurnout.WYE_TURNOUT) {
                                // not RH, LH, or WYE turnout - other blocks possible
                                if ((type == LayoutEditor.TURNOUT_B) && (lt.getLayoutBlockB() != null)) {
                                    blk2 = lt.getLayoutBlockB();
                                }
                                if ((type == LayoutEditor.TURNOUT_C) && (lt.getLayoutBlockC() != null)) {
                                    blk2 = lt.getLayoutBlockC();
                                }
                                if ((type == LayoutEditor.TURNOUT_D) && (lt.getLayoutBlockD() != null)) {
                                    blk2 = lt.getLayoutBlockD();
                                }
                            }
                            if ((blk2 != null) && (blk1 != blk2)) {
                                // have a block boundary, create a LayoutConnectivity
                                c = new LayoutConnectivity(blk1, blk2);
                                c.setConnections(ts, lt, type, null);
                                c.setDirection(computeDirection(layoutEditor.getCoords(ts.getConnect2(),
                                        ts.getType2()), layoutEditor.getCoords(ts.getConnect1(), type)));
                                // add to list
                                checkConnectivity(c, found);
                            }
                        } else {
                            // have connection to a level crossing
                            lx = (LevelXing) ts.getConnect1();
                            type = ts.getType1();
                            if ((type == LayoutEditor.LEVEL_XING_A) || (type == LayoutEditor.LEVEL_XING_C)) {
                                blk2 = lx.getLayoutBlockAC();
                            } else {
                                blk2 = lx.getLayoutBlockBD();
                            }
                            if ((blk2 != null) && (blk1 != blk2)) {
                                // have a block boundary, create a LayoutConnectivity
                                c = new LayoutConnectivity(blk1, blk2);
                                c.setConnections(ts, lx, type, null);
                                c.setDirection(computeDirection(layoutEditor.getCoords(ts.getConnect2(),
                                        ts.getType2()), layoutEditor.getCoords(ts.getConnect1(), type)));
                                // add to list
                                checkConnectivity(c, found);
                            }
                        }
                    } else if ((ts.getType1() >= LayoutEditor.SLIP_A) && (ts.getType1() <= LayoutEditor.SLIP_D)) {
                        // have connection to a slip crossing
                        ls = (LayoutSlip) ts.getConnect1();
                        type = ts.getType1();
                        blk2 = ls.getLayoutBlock();
                        if ((blk2 != null) && (blk1 != blk2)) {
                            // have a block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(blk1, blk2);
                            c.setConnections(ts, ls, type, null);
                            c.setDirection(computeDirection(layoutEditor.getCoords(ts.getConnect2(),
                                    ts.getType2()), layoutEditor.getCoords(ts.getConnect1(), type)));
                            // add to list
                            checkConnectivity(c, found);
                        }
                    }
                    // check second connection for turnout or level crossing
                    if ((ts.getType2() >= LayoutEditor.TURNOUT_A)
                            && (ts.getType2() <= LayoutEditor.LEVEL_XING_D)) {
                        // have connection to turnout or level crossing							
                        if (ts.getType2() <= LayoutEditor.TURNOUT_D) {
                            // have connection to a turnout
                            lt = (LayoutTurnout) ts.getConnect2();
                            type = ts.getType2();
                            blk2 = lt.getLayoutBlock();
                            if (lt.getTurnoutType() > LayoutTurnout.WYE_TURNOUT) {
                                // not RH, LH, or WYE turnout - other blocks possible
                                if ((type == LayoutEditor.TURNOUT_B) && (lt.getLayoutBlockB() != null)) {
                                    blk2 = lt.getLayoutBlockB();
                                }
                                if ((type == LayoutEditor.TURNOUT_C) && (lt.getLayoutBlockC() != null)) {
                                    blk2 = lt.getLayoutBlockC();
                                }
                                if ((type == LayoutEditor.TURNOUT_D) && (lt.getLayoutBlockD() != null)) {
                                    blk2 = lt.getLayoutBlockD();
                                }
                            }
                            if ((blk2 != null) && (blk1 != blk2)) {
                                // have a block boundary, create a LayoutConnectivity
                                c = new LayoutConnectivity(blk1, blk2);
                                c.setConnections(ts, lt, type, null);
                                c.setDirection(computeDirection(layoutEditor.getCoords(ts.getConnect1(),
                                        ts.getType1()), layoutEditor.getCoords(ts.getConnect2(), type)));
                                // add to list
                                checkConnectivity(c, found);
                            }
                        } else {
                            // have connection to a level crossing
                            lx = (LevelXing) ts.getConnect2();
                            type = ts.getType2();
                            if ((type == LayoutEditor.LEVEL_XING_A) || (type == LayoutEditor.LEVEL_XING_C)) {
                                blk2 = lx.getLayoutBlockAC();
                            } else {
                                blk2 = lx.getLayoutBlockBD();
                            }
                            if ((blk2 != null) && (blk1 != blk2)) {
                                // have a block boundary, create a LayoutConnectivity
                                c = new LayoutConnectivity(blk1, blk2);
                                c.setConnections(ts, lx, type, null);
                                c.setDirection(computeDirection(layoutEditor.getCoords(ts.getConnect1(),
                                        ts.getType1()), layoutEditor.getCoords(ts.getConnect2(), type)));
                                // add to list
                                checkConnectivity(c, found);
                            }
                        }
                    } else if ((ts.getType2() >= LayoutEditor.SLIP_A) && (ts.getType2() <= LayoutEditor.SLIP_D)) {
                        // have connection to a slip crossing
                        ls = (LayoutSlip) ts.getConnect2();
                        type = ts.getType2();
                        blk2 = ls.getLayoutBlock();
                        if ((blk2 != null) && (blk1 != blk2)) {
                            // have a block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(blk1, blk2);
                            c.setConnections(ts, ls, type, null);
                            c.setDirection(computeDirection(layoutEditor.getCoords(ts.getConnect1(),
                                    ts.getType1()), layoutEditor.getCoords(ts.getConnect2(), type)));
                            // add to list
                            checkConnectivity(c, found);
                        }
                    } else {
                        log.warn("Did not decode ts.getType2() of {} (A)", ts.getType2());
                    }
                }
            }
        }
        // check for block boundaries internal to crossover turnouts
        if (layoutEditor.turnoutList.size() > 0) {
            LayoutTurnout lt = null;
            for (int i = 0; i < layoutEditor.turnoutList.size(); i++) {
                lt = layoutEditor.turnoutList.get(i);
                // check for layout turnout
                if ((lt.getTurnoutType() >= LayoutTurnout.DOUBLE_XOVER)
                        && (lt.getLayoutBlock() != null)) {
                    // have a crossover turnout with at least one block, check for multiple blocks
                    if ((lt.getLayoutBlockB() != null) || (lt.getLayoutBlockC() != null)
                            || (lt.getLayoutBlockD() != null)) {
                        // have multiple blocks and therefore internal block boundaries
                        if ((lt.getLayoutBlockB() != null) && (lt.getLayoutBlock() != lt.getLayoutBlockB())) {
                            // have a AB block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(lt.getLayoutBlock(), lt.getLayoutBlockB());
                            c.setXoverBoundary(lt, LayoutConnectivity.XOVER_BOUNDARY_AB);
                            c.setDirection(computeDirection(lt.getCoordsA(), lt.getCoordsB()));
                            checkConnectivity(c, found);
                        }
                        if ((lt.getTurnoutType() != LayoutTurnout.LH_XOVER) && (lt.getLayoutBlockC() != null)
                                && (lt.getLayoutBlock() != lt.getLayoutBlockC())) {
                            // have a AC block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(lt.getLayoutBlock(), lt.getLayoutBlockC());
                            c.setXoverBoundary(lt, LayoutConnectivity.XOVER_BOUNDARY_AC);
                            c.setDirection(computeDirection(lt.getCoordsA(), lt.getCoordsC()));
                            checkConnectivity(c, found);
                        }
                        if ((lt.getLayoutBlockC() != null) && (lt.getLayoutBlockD() != null)
                                && (lt.getLayoutBlockC() != lt.getLayoutBlockD())) {
                            // have a CD block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(lt.getLayoutBlockC(), lt.getLayoutBlockD());
                            c.setXoverBoundary(lt, LayoutConnectivity.XOVER_BOUNDARY_CD);
                            c.setDirection(computeDirection(lt.getCoordsC(), lt.getCoordsD()));
                            checkConnectivity(c, found);
                        }
                        if ((lt.getTurnoutType() != LayoutTurnout.RH_XOVER) && (lt.getLayoutBlockB() != null)
                                && (lt.getLayoutBlockD() != null) && (lt.getLayoutBlockB() != lt.getLayoutBlockD())) {
                            // have a BD block boundary, create a LayoutConnectivity
                            c = new LayoutConnectivity(lt.getLayoutBlockB(), lt.getLayoutBlockD());
                            c.setXoverBoundary(lt, LayoutConnectivity.XOVER_BOUNDARY_BD);
                            c.setDirection(computeDirection(lt.getCoordsB(), lt.getCoordsD()));
                            checkConnectivity(c, found);
                        }
                    }
                }
            }
        }
        // delete any LayoutConnectivity objects no longer needed
        for (int i = sz - 1; i >= 0; i--) {
            if (!found[i]) {
// djd debugging - message to list connectivity being removed
//				LayoutConnectivity xx = (LayoutConnectivity)cList.get(i);
//				log.error("  Deleting Layout Connectivity - "+xx.getBlock1().getID()+", "+
//													xx.getBlock2().getID());
// end debugging
                cList.remove(i);
            }
        }
        blockConnectivityChanged = false;
    }

    // 
    private void checkConnectivity(LayoutConnectivity c, boolean[] found) {
        // initialize input LayoutConnectivity components
        LayoutBlock blk1 = c.getBlock1();
        LayoutBlock blk2 = c.getBlock2();
        int dir = c.getDirection();
        int rDir = c.getReverseDirection();
        TrackSegment track = c.getTrackSegment();
        Object connected = c.getConnectedObject();
        int type = c.getConnectedType();
        LayoutTurnout xOver = c.getXover();
        int xOverType = c.getXoverBoundaryType();
        // loop over connectivity list, looking for this layout connectivity
        boolean looking = true;
        for (int i = 0; (i < cList.size()) && looking; i++) {
            LayoutConnectivity lc = cList.get(i);
            // compare input LayoutConnectivity with LayoutConnectivity from the list
            if (xOver == null) {
                // not a crossover block boundary
                if ((blk1 == lc.getBlock1()) && (blk2 == lc.getBlock2()) && (track == lc.getTrackSegment())
                        && (connected == lc.getConnectedObject()) && (type == lc.getConnectedType())
                        && (dir == lc.getDirection())) {
                    looking = false;
                    found[i] = true;
                }
            } else {
                // boundary is in a crossover turnout
                if ((xOver == lc.getXover()) && (xOverType == lc.getXoverBoundaryType())) {
                    if ((blk1 == lc.getBlock1()) && (blk2 == lc.getBlock2()) && (dir == lc.getDirection())) {
                        looking = false;
                        found[i] = true;
                    } else if ((blk2 == lc.getBlock1()) && (blk1 == lc.getBlock2()) && (rDir == lc.getDirection())) {
                        looking = false;
                        found[i] = true;
                    }
                }
            }
        }
        // if not found in list, add it
        if (looking) {
            cList.add(c);
        }
    }

    // compute direction of vector from p1 to p2
    private int computeDirection(Point2D p1, Point2D p2) {
        double dh = p2.getX() - p1.getX();
        double dv = p2.getY() - p1.getY();
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

    /**
     * Searches for and adds BeanSetting's to a Path as needed.
     * <P>
     * This method starts at the entry point to the LayoutBlock given in the
     * Path at the block boundary specified in the LayoutConnectivity. It
     * follows the track looking for turnout settings that are required for a
     * train entering on this block boundary point to exit the block. If a
     * required turnout setting is found, the turnout and its required state are
     * used to create a BeanSetting, which is added to the Path. Such a setting
     * can occur, for example, if a track enters a right-handed turnout from
     * either the diverging track or the continuing track.
     * <P>
     * If the track branches into two tracks (for example, by entering a
     * right-handed turnout via the throat track), the search is stopped. The
     * search is also stopped when the track reaches a different block (or an
     * undefined block), or reaches an end bumper.
     */
    public void addBeanSettings(Path p, LayoutConnectivity lc, LayoutBlock layoutBlock) {
        p.clearSettings();
        Object curConnection = null;
        Object prevConnection = null;
        int typeCurConnection = 0;
        BeanSetting bs = null;
        LayoutTurnout lt = null;
        // process object at block boundary
        if (lc.getBlock1() == layoutBlock) {
            // block1 is this LayoutBlock
            if (lc.getTrackSegment() != null) {
                // connected object in this block is a track segment
                curConnection = lc.getTrackSegment();
                prevConnection = lc.getConnectedObject();
                typeCurConnection = LayoutEditor.TRACK;
                // is this Track Segment connected to a RH, LH, or WYE turnout at the continuing or diverging track
                if (((lc.getConnectedType() == LayoutEditor.TURNOUT_B)
                        || (lc.getConnectedType() == LayoutEditor.TURNOUT_C))
                        && ((((LayoutTurnout) prevConnection).getTurnoutType() >= LayoutTurnout.RH_TURNOUT)
                        && (((LayoutTurnout) prevConnection).getTurnoutType() <= LayoutTurnout.WYE_TURNOUT))) {
                    LayoutTurnout ltx = (LayoutTurnout) prevConnection;
                    if (lc.getConnectedType() == LayoutEditor.TURNOUT_B) {
                        // Track Segment connected to continuing track of turnout
                        if (ltx.getContinuingSense() == Turnout.CLOSED) {
                            bs = new BeanSetting(ltx.getTurnout(), ltx.getTurnoutName(), Turnout.CLOSED);
                        } else {
                            bs = new BeanSetting(ltx.getTurnout(), ltx.getTurnoutName(), Turnout.THROWN);
                        }
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (A): " + ltx.getName() + " " + ltx.getLayoutBlock().getDisplayName()+" ltx.getContinuingSense(): "+ltx.getContinuingSense());
                        }

                    } else if (lc.getConnectedType() == LayoutEditor.TURNOUT_C) {
                        // Track Segment connected to diverging track of turnout
                        if (ltx.getContinuingSense() == Turnout.CLOSED) {
                            bs = new BeanSetting(ltx.getTurnout(), ltx.getTurnoutName(), Turnout.THROWN);
                        } else {
                            bs = new BeanSetting(ltx.getTurnout(), ltx.getTurnoutName(), Turnout.CLOSED);
                        }
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (B): " + ltx.getName() + " " + ltx.getLayoutBlock().getDisplayName()+ " ltx.getContinuingSense(): "+ltx.getContinuingSense());
                        }
                    } else {
                        log.warn("Did not decode lc.getConnectedType() of {}", lc.getConnectedType());
                    }
                } // is this Track Segment connected to the continuing track of a RH_XOVER or LH_XOVER
                else if (((lc.getConnectedType() >= LayoutEditor.TURNOUT_A)
                        && (lc.getConnectedType() <= LayoutEditor.TURNOUT_D))
                        && ((((LayoutTurnout) prevConnection).getTurnoutType() == LayoutTurnout.RH_XOVER)
                        || (((LayoutTurnout) prevConnection).getTurnoutType() == LayoutTurnout.LH_XOVER))) {
                    LayoutTurnout ltz = (LayoutTurnout) prevConnection;
                    if (((ltz.getTurnoutType() == LayoutTurnout.RH_XOVER)
                            && ((lc.getConnectedType() == LayoutEditor.TURNOUT_B)
                            || (lc.getConnectedType() == LayoutEditor.TURNOUT_D)))
                            || ((ltz.getTurnoutType() == LayoutTurnout.LH_XOVER)
                            && ((lc.getConnectedType() == LayoutEditor.TURNOUT_A)
                            || (lc.getConnectedType() == LayoutEditor.TURNOUT_C)))) {
                        bs = new BeanSetting(ltz.getTurnout(), ltz.getTurnoutName(), Turnout.CLOSED);
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (C): " + ltz.getName() + " " + ltz.getLayoutBlock().getDisplayName()+" ltz.getTurnoutType(): "+ltz.getTurnoutType()+" lc.getConnectedType(): "+lc.getConnectedType());
                        }
                    }
                } //This track section is connected to a slip
                else if (lc.getConnectedType() >= LayoutEditor.SLIP_A
                        && lc.getConnectedType() <= LayoutEditor.SLIP_D) {

                    LayoutSlip lsz = (LayoutSlip) prevConnection;
                    if (lsz.getSlipType() == LayoutSlip.SINGLE_SLIP) {
                        if (lc.getConnectedType() == LayoutEditor.SLIP_C) {
                            bs = new BeanSetting(lsz.getTurnout(), lsz.getTurnoutName(), lsz.getTurnoutState(LayoutSlip.STATE_AC));
                            if (bs.getBean() != null) {
                                p.addSetting(bs);
                            } else {
                                InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                                log.error("BadBeanError (D): " + lsz.getName() + " " + lsz.getLayoutBlock().getDisplayName());
                            }
                            bs = new BeanSetting(lsz.getTurnoutB(), lsz.getTurnoutBName(), lsz.getTurnoutBState(LayoutSlip.STATE_AC));
                            if (bs.getBean() != null) {
                                p.addSetting(bs);
                            } else {
                                InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                                log.error("BadBeanError (E): " + lsz.getName() + " " + lsz.getLayoutBlock().getDisplayName());
                            }
                        } else if (lc.getConnectedType() == LayoutEditor.SLIP_B) {
                            bs = new BeanSetting(lsz.getTurnout(), lsz.getTurnoutName(), lsz.getTurnoutState(LayoutSlip.STATE_BD));
                            if (bs.getBean() != null) {
                                p.addSetting(bs);
                            } else {
                                InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                                log.error("BadBeanError (F): " + lsz.getName() + " " + lsz.getLayoutBlock().getDisplayName()+" "+lsz.getTurnout()+" "+lsz.getTurnoutName()+" "+lsz.getTurnoutState(LayoutSlip.STATE_BD));
                            }

                            bs = new BeanSetting(lsz.getTurnoutB(), lsz.getTurnoutBName(), lsz.getTurnoutBState(LayoutSlip.STATE_BD));
                            if (bs.getBean() != null) {
                                p.addSetting(bs);
                            } else {
                                InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                                log.error("BadBeanError (G): " + lsz.getName() + " " + lsz.getLayoutBlock().getDisplayName()+" "+lsz.getTurnoutB()+" "+lsz.getTurnoutBName()+" "+lsz.getTurnoutBState(LayoutSlip.STATE_BD));
                            }
                        } else if (lc.getConnectedType() == LayoutEditor.SLIP_A) {
                            //As connection A of a single slip which could go in two different directions
                        } else if (lc.getConnectedType() == LayoutEditor.SLIP_D) {
                            //As connection D of a single slip which could go in two different directions
                        }
                    } else {
                        //At a double slip we could go in two different directions
                        log.warn("possible incomplete code: At a double slip we could go in two different directions");
                    }
                }
            } else {
                // block boundary is internal to a crossover turnout
                lt = lc.getXover();
                prevConnection = lt;
                if ((lt != null) && (lt.getTurnout() != null)) {
                    int type = lc.getXoverBoundaryType();
                    if (type == LayoutConnectivity.XOVER_BOUNDARY_AB) {
                        bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                        curConnection = lt.getConnectA();
                    } else if (type == LayoutConnectivity.XOVER_BOUNDARY_CD) {
                        bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                        curConnection = lt.getConnectC();
                    } else if (type == LayoutConnectivity.XOVER_BOUNDARY_AC) {
                        bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.THROWN);
                        curConnection = lt.getConnectA();
                    } else if (type == LayoutConnectivity.XOVER_BOUNDARY_BD) {
                        bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.THROWN);
                        curConnection = lt.getConnectB();
                    } else {
                        log.warn("failed to decode lc.getXoverBoundaryType() of {} (A)", lc.getXoverBoundaryType());
                    }
                    typeCurConnection = LayoutEditor.TRACK;
                    if ((bs != null) && (bs.getBean() != null)) {
                        p.addSetting(bs);
                    } else {
                        InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                        log.error("BadBeanError (H): " + lt.getName() + " " + lt.getLayoutBlock().getDisplayName()+" "+type);
                    }
                }
            }
        } else if (lc.getXover() != null) {
            // first Block is not in a Track Segment, must be block boundary internal to a crossover turnout
            lt = lc.getXover();
            if ((lt != null) && (lt.getTurnout() != null)) {
                int type = lc.getXoverBoundaryType();
                if (type == LayoutConnectivity.XOVER_BOUNDARY_AB) {
                    bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                    curConnection = lt.getConnectB();
                } else if (type == LayoutConnectivity.XOVER_BOUNDARY_CD) {
                    bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                    curConnection = lt.getConnectD();
                } else if (type == LayoutConnectivity.XOVER_BOUNDARY_AC) {
                    bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.THROWN);
                    curConnection = lt.getConnectC();
                } else if (type == LayoutConnectivity.XOVER_BOUNDARY_BD) {
                    bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.THROWN);
                    curConnection = lt.getConnectD();
                } else {
                    log.warn("failed to decode lc.getXoverBoundaryType() of {} (B)", lc.getXoverBoundaryType());
                }
                typeCurConnection = LayoutEditor.TRACK;
                if ((bs != null) && (bs.getBean() != null)) {
                    p.addSetting(bs);
                } else {
                    InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                    log.error("BadBeanError (I): " + lt.getName() + " " + lt.getLayoutBlock().getDisplayName()+" "+type);
                }
            }
        } else {
            // block2 is this LayoutBlock, and block1 is in a track segment
            if (lc.getConnectedObject() != null) {
                // connected object in this block is a turnout or levelxing
                curConnection = lc.getConnectedObject();
                prevConnection = lc.getTrackSegment();
                typeCurConnection = lc.getConnectedType();
                if ((typeCurConnection >= LayoutEditor.TURNOUT_A) && (typeCurConnection <= LayoutEditor.TURNOUT_D)) {
                    // connected object is a turnout
                    int turnoutType = ((LayoutTurnout) curConnection).getTurnoutType();
                    if (turnoutType > LayoutTurnout.WYE_TURNOUT) {
                        // have crossover turnout
                        if ((turnoutType == LayoutTurnout.DOUBLE_XOVER)
                                || ((turnoutType == LayoutTurnout.RH_XOVER) && ((typeCurConnection == LayoutEditor.TURNOUT_A) || (typeCurConnection == LayoutEditor.TURNOUT_C)))
                                || ((turnoutType == LayoutTurnout.LH_XOVER) && ((typeCurConnection == LayoutEditor.TURNOUT_B) || (typeCurConnection == LayoutEditor.TURNOUT_D)))) {
                            // entering turnout at a throat, cannot follow path any further
                            curConnection = null;
                        } else {
                            // entering turnout at continuing track
                            bs = new BeanSetting(((LayoutTurnout) curConnection).getTurnout(), ((LayoutTurnout) curConnection).getTurnoutName(), Turnout.CLOSED);
                            if (bs.getBean() != null) {
                                p.addSetting(bs);
                            } else {
                                InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                                log.error("BadBeanError (J): " + ((LayoutTurnout) curConnection).getName() + " " + ((LayoutTurnout) curConnection).getLayoutBlock().getDisplayName());
                            }
                            prevConnection = curConnection;
                            if (typeCurConnection == LayoutEditor.TURNOUT_A) {
                                curConnection = ((LayoutTurnout) curConnection).getConnectB();
                            } else if (typeCurConnection == LayoutEditor.TURNOUT_B) {
                                curConnection = ((LayoutTurnout) curConnection).getConnectA();
                            } else if (typeCurConnection == LayoutEditor.TURNOUT_C) {
                                curConnection = ((LayoutTurnout) curConnection).getConnectD();
                            } else { // typeCurConnection == LayoutEditor.TURNOUT_D per if statement 3 levels up
                                curConnection = ((LayoutTurnout) curConnection).getConnectC();
                            }
                            typeCurConnection = LayoutEditor.TRACK;
                        }
                    } // must be RH, LH, or WYE turnout
                    else if (typeCurConnection == LayoutEditor.TURNOUT_A) {
                        // turnout throat, no bean setting needed and cannot follow Path any further
                        curConnection = null;
                    } else if (typeCurConnection == LayoutEditor.TURNOUT_B) {
                        // continuing track of turnout
                        if (((LayoutTurnout) curConnection).getContinuingSense() == Turnout.CLOSED) {
                            bs = new BeanSetting(((LayoutTurnout) curConnection).getTurnout(), ((LayoutTurnout) curConnection).getTurnoutName(), Turnout.CLOSED);
                        } else {
                            bs = new BeanSetting(((LayoutTurnout) curConnection).getTurnout(), ((LayoutTurnout) curConnection).getTurnoutName(), Turnout.THROWN);
                        }
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (K): " + ((LayoutTurnout) curConnection).getName() + " " + ((LayoutTurnout) curConnection).getLayoutBlock().getDisplayName());
                        }
                        prevConnection = curConnection;
                        curConnection = ((LayoutTurnout) curConnection).getConnectA();
                        typeCurConnection = LayoutEditor.TRACK;
                    } else if (typeCurConnection == LayoutEditor.TURNOUT_C) {
                        // diverging track of turnout
                        if (((LayoutTurnout) curConnection).getContinuingSense() == Turnout.CLOSED) {
                            bs = new BeanSetting(((LayoutTurnout) curConnection).getTurnout(), ((LayoutTurnout) curConnection).getTurnoutName(), Turnout.THROWN);
                        } else {
                            bs = new BeanSetting(((LayoutTurnout) curConnection).getTurnout(), ((LayoutTurnout) curConnection).getTurnoutName(), Turnout.CLOSED);
                        }
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (L): " + ((LayoutTurnout) curConnection).getName() + " " + ((LayoutTurnout) curConnection).getLayoutBlock().getDisplayName());
                        }
                        prevConnection = curConnection;
                        curConnection = ((LayoutTurnout) curConnection).getConnectA();
                        typeCurConnection = LayoutEditor.TRACK;
                    }
                } // if level crossing, skip to the connected track segment on opposite side
                else if (typeCurConnection == LayoutEditor.LEVEL_XING_A) {
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectC();
                    typeCurConnection = LayoutEditor.TRACK;
                } else if (typeCurConnection == LayoutEditor.LEVEL_XING_C) {
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectA();
                    typeCurConnection = LayoutEditor.TRACK;
                } else if (typeCurConnection == LayoutEditor.LEVEL_XING_B) {
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectD();
                    typeCurConnection = LayoutEditor.TRACK;
                } else if (typeCurConnection == LayoutEditor.LEVEL_XING_D) {
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectB();
                    typeCurConnection = LayoutEditor.TRACK;
                } else if (typeCurConnection == LayoutEditor.SLIP_D) {
                    LayoutSlip lsz = (LayoutSlip) curConnection;
                    curConnection = null;
                }
            } else {
                // block boundary is internal to a crossover turnout
                lt = lc.getXover();
                prevConnection = lt;
                if ((lt != null) && (lt.getTurnout() != null)) {
                    int type = lc.getXoverBoundaryType();
                    if (type == LayoutConnectivity.XOVER_BOUNDARY_AB) {
                        bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                        curConnection = lt.getConnectB();
                    } else if (type == LayoutConnectivity.XOVER_BOUNDARY_CD) {
                        bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                        curConnection = lt.getConnectD();
                    } else if (type == LayoutConnectivity.XOVER_BOUNDARY_AC) {
                        bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.THROWN);
                        curConnection = lt.getConnectC();
                    } else if (type == LayoutConnectivity.XOVER_BOUNDARY_BD) {
                        bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.THROWN);
                        curConnection = lt.getConnectD();
                    }
                    typeCurConnection = LayoutEditor.TRACK;
                    if ((bs != null) && (bs.getBean() != null)) {
                        p.addSetting(bs);
                    } else {
                        InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                        log.error("BadBeanError (Q): " + lt.getName() + " " + lt.getLayoutBlock().getDisplayName());
                    }
                }
            }
        }
        // follow path through this block - done when reaching another block, or a branching of Path
        while (curConnection != null) {
            if (typeCurConnection == LayoutEditor.TRACK) {
                // track segment is current connection
                if (((TrackSegment) curConnection).getLayoutBlock() != layoutBlock) {
                    curConnection = null;
                } else {
                    // skip over to other end of Track Segment
                    if (((TrackSegment) curConnection).getConnect1() == prevConnection) {
                        prevConnection = curConnection;
                        typeCurConnection = ((TrackSegment) curConnection).getType2();
                        curConnection = ((TrackSegment) curConnection).getConnect2();
                    } else {
                        prevConnection = curConnection;
                        typeCurConnection = ((TrackSegment) curConnection).getType1();
                        curConnection = ((TrackSegment) curConnection).getConnect1();
                    }
                    // skip further if positionable point (possible anchor point)
                    if (typeCurConnection == LayoutEditor.POS_POINT) {
                        PositionablePoint pt = (PositionablePoint) curConnection;
                        if (pt.getType() == PositionablePoint.END_BUMPER) {
                            // reached end of track
                            curConnection = null;
                        } else {
                            // at an anchor point, find track segment on other side
                            TrackSegment track = null;
                            if (pt.getConnect1() == prevConnection) {
                                track = pt.getConnect2();
                            } else {
                                track = pt.getConnect1();
                            }
                            // check for block boundary
                            if ((track == null) || (track.getLayoutBlock() != layoutBlock)) {
                                // moved outside of block - anchor point was a block boundary -OR-
                                //		reached the end of the defined track
                                curConnection = null;
                            } else {
                                prevConnection = curConnection;
                                curConnection = track;
                                typeCurConnection = LayoutEditor.TRACK;
                            }
                        }
                    }
                }
            } else if ((typeCurConnection >= LayoutEditor.TURNOUT_A)
                    && (typeCurConnection <= LayoutEditor.TURNOUT_D)) {
                lt = (LayoutTurnout) curConnection;
                // test for crossover turnout
                if (lt.getTurnoutType() <= LayoutTurnout.WYE_TURNOUT) {
                    // have RH, LH, or WYE turnout

                    if (lt.getLayoutBlock() != layoutBlock) {
                        curConnection = null;
                    } else {
                        // turnout is in current block, test connection point
                        if (typeCurConnection == LayoutEditor.TURNOUT_A) {
                            // turnout throat, no bean setting needed and cannot follow possible path any further
                            curConnection = null;
                        } else if (typeCurConnection == LayoutEditor.TURNOUT_B) {
                            // continuing track of turnout, add a bean setting
                            if (lt.getContinuingSense() == Turnout.CLOSED) {
                                bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                            } else {
                                bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.THROWN);
                            }
                            if (bs.getBean() != null) {
                                p.addSetting(bs);
                            } else {
                                InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                                log.error("BadBeanError (R): " + lt.getName() + " " + lt.getLayoutBlock().getDisplayName());
                            }
                            if (lt.getLayoutBlock() != layoutBlock) {
                                curConnection = null;
                            } else {
                                prevConnection = curConnection;
                                curConnection = lt.getConnectA();
                                typeCurConnection = LayoutEditor.TRACK;
                            }
                        } else if (typeCurConnection == LayoutEditor.TURNOUT_C) {
                            // diverging track of turnout
                            if (lt.getContinuingSense() == Turnout.CLOSED) {
                                bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.THROWN);
                            } else {
                                bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                            }
                            if (bs.getBean() != null) {
                                p.addSetting(bs);
                            } else {
                                InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                                log.error("BadBeanError (S) LayoutTurnout " + lt.getName() + " has Turnout: " + lt.getTurnout() + " turnoutName: " + lt.getTurnoutName());
                            }
                            if (lt.getLayoutBlock() != layoutBlock) {
                                curConnection = null;
                            } else {
                                prevConnection = curConnection;
                                curConnection = lt.getConnectA();
                                typeCurConnection = LayoutEditor.TRACK;
                            }
                        }
                    }
                } else if (lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER) {
                    // have a double crossover turnout, cannot follow possible path any further
                    curConnection = null;
                } else if (lt.getTurnoutType() == LayoutTurnout.RH_XOVER) {
                    // have a right-handed crossover turnout
                    if ((typeCurConnection == LayoutEditor.TURNOUT_A)
                            || (typeCurConnection == LayoutEditor.TURNOUT_C)) {
                        // entry is at turnout throat, cannot follow possible path any further
                        curConnection = null;
                    } else if (typeCurConnection == LayoutEditor.TURNOUT_B) {
                        // entry is at continuing track of turnout 
                        bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (T) LayoutTurnout " + lt.getName() + " has Turnout: " + lt.getTurnout() + " turnoutName: " + lt.getTurnoutName());
                        }
                        if (lt.getLayoutBlock() != layoutBlock) {
                            // left current block 
                            curConnection = null;
                        } else {
                            prevConnection = curConnection;
                            curConnection = lt.getConnectA();
                            typeCurConnection = LayoutEditor.TRACK;
                        }
                    } else if (typeCurConnection == LayoutEditor.TURNOUT_D) {
                        // entry is at continuing track of turnout 
                        bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (U) LayoutTurnout " + lt.getName() + " has Turnout: " + lt.getTurnout() + " turnoutName: " + lt.getTurnoutName());
                        }
                        if (lt.getLayoutBlockC() != layoutBlock) {
                            // left current block 
                            curConnection = null;
                        } else {
                            prevConnection = curConnection;
                            curConnection = lt.getConnectC();
                            typeCurConnection = LayoutEditor.TRACK;
                        }
                    }
                } else if (lt.getTurnoutType() == LayoutTurnout.LH_XOVER) {
                    // have a left-handed crossover turnout
                    if ((typeCurConnection == LayoutEditor.TURNOUT_B)
                            || (typeCurConnection == LayoutEditor.TURNOUT_D)) {
                        // entry is at turnout throat, cannot follow possible path any further
                        curConnection = null;
                    } else if (typeCurConnection == LayoutEditor.TURNOUT_A) {
                        // entry is at continuing track of turnout 
                        bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (V) LayoutTurnout " + lt.getName() + " has Turnout: " + lt.getTurnout() + " turnoutName: " + lt.getTurnoutName());
                        }
                        if (lt.getLayoutBlockB() != layoutBlock) {
                            // left current block 
                            curConnection = null;
                        } else {
                            prevConnection = curConnection;
                            curConnection = lt.getConnectB();
                            typeCurConnection = LayoutEditor.TRACK;
                        }
                    } else { // typeCurConnection == LayoutEditor.TURNOUT_C per if statement 2 levels up
                        // entry is at continuing track of turnout 
                        bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (W) LayoutTurnout " + lt.getName() + " has Turnout: " + lt.getTurnout() + " turnoutName: " + lt.getTurnoutName());
                        }
                        if (lt.getLayoutBlockD() != layoutBlock) {
                            // left current block
                            curConnection = null;
                        } else {
                            prevConnection = curConnection;
                            curConnection = lt.getConnectD();
                            typeCurConnection = LayoutEditor.TRACK;
                        }
                    }
                }
            } else if (typeCurConnection == LayoutEditor.LEVEL_XING_A) {
                // have a level crossing connected at A
                if (((LevelXing) curConnection).getLayoutBlockAC() != layoutBlock) {
                    // moved outside of this block
                    curConnection = null;
                } else {
                    // move to other end of this section of this level crossing track
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectC();
                    typeCurConnection = LayoutEditor.TRACK;
                }
            } else if (typeCurConnection == LayoutEditor.LEVEL_XING_B) {
                // have a level crossing connected at B
                if (((LevelXing) curConnection).getLayoutBlockBD() != layoutBlock) {
                    // moved outside of this block
                    curConnection = null;
                } else {
                    // move to other end of this section of this level crossing track
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectD();
                    typeCurConnection = LayoutEditor.TRACK;
                }
            } else if (typeCurConnection == LayoutEditor.LEVEL_XING_C) {
                // have a level crossing connected at C
                if (((LevelXing) curConnection).getLayoutBlockAC() != layoutBlock) {
                    // moved outside of this block
                    curConnection = null;
                } else {
                    // move to other end of this section of this level crossing track
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectA();
                    typeCurConnection = LayoutEditor.TRACK;
                }
            } else if (typeCurConnection == LayoutEditor.LEVEL_XING_D) {
                // have a level crossing connected at D
                if (((LevelXing) curConnection).getLayoutBlockBD() != layoutBlock) {
                    // moved outside of this block
                    curConnection = null;
                } else {
                    // move to other end of this section of this level crossing track
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectB();
                    typeCurConnection = LayoutEditor.TRACK;
                }
            } else if (typeCurConnection >= LayoutEditor.SLIP_A && typeCurConnection <= LayoutEditor.SLIP_D) {
                LayoutSlip ls = (LayoutSlip) curConnection;
                if (ls.getLayoutBlock() != layoutBlock) {
                    curConnection = null;
                } else if (ls.getSlipType() == LayoutSlip.SINGLE_SLIP) {
                    if (typeCurConnection == LayoutEditor.SLIP_C) {
                        bs = new BeanSetting(ls.getTurnout(), ls.getTurnoutName(), ls.getTurnoutState(LayoutSlip.STATE_AC));
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (X): " + ls.getName() + " " + ls.getLayoutBlock().getDisplayName());
                        }
                        bs = new BeanSetting(ls.getTurnoutB(), ls.getTurnoutBName(), ls.getTurnoutBState(LayoutSlip.STATE_AC));
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (Y): " + ls.getName() + " " + ls.getLayoutBlock().getDisplayName());
                        }
                        prevConnection = curConnection;
                        curConnection = ((LayoutSlip) curConnection).getConnectC();
                        typeCurConnection = LayoutEditor.TRACK;
                    } else if (typeCurConnection == LayoutEditor.SLIP_B) {
                        bs = new BeanSetting(ls.getTurnout(), ls.getTurnoutName(), ls.getTurnoutState(LayoutSlip.STATE_BD));
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (Z): " + ls.getName() + " " + ls.getLayoutBlock().getDisplayName());
                        }

                        bs = new BeanSetting(ls.getTurnoutB(), ls.getTurnoutBName(), ls.getTurnoutBState(LayoutSlip.STATE_BD));
                        if (bs.getBean() != null) {
                            p.addSetting(bs);
                        } else {
                            InstanceManager.getDefault(LayoutBlockManager.class).addBadBeanError();
                            log.error("BadBeanError (1): " + ls.getName() + " " + ls.getLayoutBlock().getDisplayName());
                        }
                        prevConnection = curConnection;
                        curConnection = ((LayoutSlip) curConnection).getConnectB();
                        typeCurConnection = LayoutEditor.TRACK;
                    } else {
                        //Else could be going in the slip direction
                        curConnection = null;
                    }

                } else {
                    //At double slip, can not follow any further
                    curConnection = null;
                }
            } else if (typeCurConnection >= 50) {
                if (log.isDebugEnabled()) {
                    log.debug("Layout Block: " + layoutBlock.getDisplayName() + " found track type: " + typeCurConnection + " to Block: " + p.getBlock().getDisplayName() + " Is potentially assigned to turntable ray");
                }
                curConnection = null;
            } else {
                // catch when some new type got added
                log.error("Layout Block: " + layoutBlock.getDisplayName() + " found unknown track type: " + typeCurConnection + " to Block: " + p.getBlock().getDisplayName());
                break;
            }
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LayoutEditorAuxTools.class.getName());
}
