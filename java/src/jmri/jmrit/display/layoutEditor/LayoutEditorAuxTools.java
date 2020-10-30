package jmri.jmrit.display.layoutEditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jmri.BeanSetting;
import jmri.Path;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LayoutEditorAuxTools provides tools making use of layout connectivity
 * available in Layout Editor panels. (More tools are in
 * LayoutEditorTools.java.)
 * <p>
 * This module manages block connectivity for its associated LayoutEditor.
 * <p>
 * A single object of this type, obtained via {@link LayoutEditor#getLEAuxTools()}
 * is shared across all instances of {@link ConnectivityUtil}.
 * <p>
 * The tools in this module are accessed via the Tools menu in Layout Editor, or
 * directly from LayoutEditor or LayoutEditor specific modules.
 *
 * @author Dave Duchamp Copyright (c) 2008
 * @author George Warner Copyright (c) 2017-2018
 */
final public class LayoutEditorAuxTools {
    // constants

    // operational instance variables
    final private LayoutModels models;
    final private List<LayoutConnectivity> cList = new ArrayList<>(); // LayoutConnectivity list
    private boolean blockConnectivityChanged = false;  // true if block connectivity may have changed
    private boolean initialized = false;

    // constructor method
    public LayoutEditorAuxTools(LayoutModels theModels) {
        models = theModels;
    }

    // register a change in block connectivity that may require an update of connectivity list
    public void setBlockConnectivityChanged() {
        blockConnectivityChanged = true;
    }

    /**
     * Get Connectivity involving a specific Layout Block.
     * <p>
     * This routine returns an ArrayList of BlockConnectivity objects involving
     * the specified LayoutBlock.
     * @param blk the layout block.
     * @return the layout connectivity list, not null.
     */
    public List<LayoutConnectivity> getConnectivityList(LayoutBlock blk) {
        if (!initialized) {
            initializeBlockConnectivity();
        }
        if (blockConnectivityChanged) {
            updateBlockConnectivity();
        }
        List<LayoutConnectivity> retList = new ArrayList<>();
        for (LayoutConnectivity lc : cList) {
            if ((lc.getBlock1() == blk) || (lc.getBlock2() == blk)) {
                retList.add(lc);
            }
        }
        return (retList);
    }

    /**
     * Initializes the block connectivity (block boundaries) for a Layout Editor
     * panel.
     * <p>
     * This routine sets up the LayoutConnectivity objects needed to show the
     * current connectivity. It gets its information from arrays contained in
     * LayoutEditor.
     * <p>
     * One LayoutConnectivity object is created for each block boundary --
     * connection points where two blocks join. Block boundaries can occur where
     * ever a track segment in one block joins with: 1) a track segment in
     * another block -OR- 2) a connection point in a layout turnout in another
     * block -OR- 3) a connection point in a level crossing in another block.
     * <p>
     * The first block is always a track segment. The direction set in the
     * LayoutConnectivity is the direction of the track segment alone for cases
     * 2) and 3) above. For case 1), two track segments, the direction reflects
     * an "average" over the two track segments. See LayoutConnectivity for the
     * allowed values of direction.
     */
    public void initializeBlockConnectivity() {
        if (initialized) {
            log.error("Call to initialize a connectivity list that has already been initialized");  // NOI18N
            return;
        }
        cList.clear(); 
        List<LayoutConnectivity> lcs = null;

        for (LayoutTrackView ltv : models.getLayoutTrackViews()) {
            if ((ltv instanceof PositionablePointView)    // effectively, skip LevelXing and LayoutTurntable - why?
                    || (ltv instanceof TrackSegmentView)
                    || (ltv instanceof LayoutTurnoutView)) { // <== includes Wye. LayoutSlips, XOvers
                lcs = ltv.getLayoutConnectivity();
                cList.addAll(lcs); // append to list
            }
        }
        initialized = true;
    }   // initializeBlockConnectivity

    /**
     * Updates the block connectivity (block boundaries) for a Layout Editor
     * panel after changes may have been made.
     */
    private void updateBlockConnectivity() {
        int sz = cList.size();
        boolean[] found = new boolean[sz];
        Arrays.fill(found, false);

        List<LayoutConnectivity> lcs = null;

        // Check for block boundaries at positionable points.
        for (PositionablePoint p : models.getPositionablePoints()) {
            lcs = p.getLayoutConnectivity();
            for (LayoutConnectivity lc : lcs) {
                // add to list, if not already present
                checkConnectivity(lc, found);
            }
        }

        // Check for block boundaries at layout turnouts and level crossings
        for (TrackSegment ts : models.getTrackSegments()) {
            lcs = ts.getLayoutConnectivity();
            for (LayoutConnectivity lc : lcs) {
                // add to list, if not already present
                checkConnectivity(lc, found);
            }
        }

        // check for block boundaries internal to crossover turnouts
        for (LayoutTurnout lt : models.getLayoutTurnouts()) {
            lcs = lt.getLayoutConnectivity();
            for (LayoutConnectivity lc : lcs) {
                // add to list, if not already present
                checkConnectivity(lc, found);
            }
        }

        // check for block boundaries internal to slips
        for (LayoutSlip ls : models.getLayoutSlips()) {
            lcs = ls.getLayoutConnectivity();
            for (LayoutConnectivity lc : lcs) {
                // add to list, if not already present
                checkConnectivity(lc, found);
            }
        }

        // delete any LayoutConnectivity objects no longer needed
        for (int i = sz - 1; i >= 0; i--) {
            if (!found[i]) {
                // djd debugging - message to list connectivity being removed
                //    LayoutConnectivity xx = (LayoutConnectivity)cList.get(i);
                //    log.error("  Deleting Layout Connectivity - " + xx.getBlock1().getId() + ", " + xx.getBlock2().getId());
                // end debugging
                cList.remove(i);
            }
        }
        blockConnectivityChanged = false;
    }   // updateBlockConnectivity

    //
    private void checkConnectivity(LayoutConnectivity c, boolean[] found) {
        // initialize input LayoutConnectivity components
        LayoutBlock blk1 = c.getBlock1();
        LayoutBlock blk2 = c.getBlock2();

        int dir = c.getDirection();
        int rDir = c.getReverseDirection();

        TrackSegment track = c.getTrackSegment();
        LayoutTrack connected = c.getConnectedObject();
        HitPointType type = c.getConnectedType();

        LayoutTurnout xOver = c.getXover();
        int xOverType = c.getXoverBoundaryType();

        // loop over connectivity list, looking for this layout connectivity
        for (int i = 0; i < cList.size(); i++) {
            LayoutConnectivity lc = cList.get(i);
            // compare input LayoutConnectivity with LayoutConnectivity from the list
            if (xOver == null) {
                // not a crossover block boundary
                if ((blk1 == lc.getBlock1()) && (blk2 == lc.getBlock2()) && (track == lc.getTrackSegment())
                        && (connected == lc.getConnectedObject()) && (type == lc.getConnectedType())
                        && (dir == lc.getDirection())) {
                    found[i] = true;
                    break;
                }
            } else {
                // boundary is in a crossover turnout
                if ((xOver == lc.getXover()) && (xOverType == lc.getXoverBoundaryType())) {
                    if ((blk1 == lc.getBlock1()) && (blk2 == lc.getBlock2()) && (dir == lc.getDirection())) {
                        found[i] = true;
                        break;
                    } else if ((blk2 == lc.getBlock1()) && (blk1 == lc.getBlock2()) && (rDir == lc.getDirection())) {
                        found[i] = true;
                        break;
                    }
                }
            }
        }

        // Check to see if this connectivity is already in the list
        // This occurs for the first layout editor panel when there
        // are multiple panels connected by edge connectors.
        if (cList.contains(c)) {
            log.debug("checkConnectivity: Duplicate connection: '{}'", c);  // NOI18N
        } else {
            cList.add(c);
        }
    }   // checkConnectivity

    /**
     * Searches for and adds BeanSetting's to a Path as needed.
     * <p>
     * This method starts at the entry point to the LayoutBlock given in the
     * Path at the block boundary specified in the LayoutConnectivity. It
     * follows the track looking for turnout settings that are required for a
     * train entering on this block boundary point to exit the block. If a
     * required turnout setting is found, the turnout and its required state are
     * used to create a BeanSetting, which is added to the Path. Such a setting
     * can occur, for example, if a track enters a right-handed turnout from
     * either the diverging track or the continuing track.
     * <p>
     * If the track branches into two tracks (for example, by entering a
     * right-handed turnout via the throat track), the search is stopped. The
     * search is also stopped when the track reaches a different block (or an
     * undefined block), or reaches an end bumper.
     * @param p path to follow until branch.
     * @param lc layout connectivity.
     * @param layoutBlock the layout block.
     */
    public void addBeanSettings(Path p, LayoutConnectivity lc, LayoutBlock layoutBlock) {
        p.clearSettings();
        LayoutTrack curConnection = null;
        LayoutTrack prevConnection = null;
        HitPointType typeCurConnection = HitPointType.NONE;
        BeanSetting bs = null;
        LayoutTurnout lt = null;
        // process track at block boundary
        if (lc.getBlock1() == layoutBlock) {    // block1 is this LayoutBlock
            curConnection = lc.getTrackSegment();
            if (curConnection != null) {        // connected track in this block is a track segment
                prevConnection = lc.getConnectedObject();
                typeCurConnection = HitPointType.TRACK;
                // is this Track Segment connected to a RH, LH, or WYE turnout at the continuing or diverging track?
                if ((lc.getConnectedType() == HitPointType.TURNOUT_B
                        || lc.getConnectedType() == HitPointType.TURNOUT_C)
                        && ((LayoutTurnout) prevConnection).getTurnoutType() != LayoutTurnout.TurnoutType.NONE
                        && LayoutTurnout.hasEnteringSingleTrack(((LayoutTurnout) prevConnection).getTurnoutType())) {
                    LayoutTurnout ltx = (LayoutTurnout) prevConnection;
                    // Track Segment connected to continuing track of turnout?
                    if (lc.getConnectedType() == HitPointType.TURNOUT_B) {
                        Turnout ltxto = ltx.getTurnout();
                        if ( ltxto != null) {
                            bs = new BeanSetting(ltxto, ltx.getTurnoutName(), ltx.getContinuingSense());
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnout (A): LTO = {}, blk = {}", ltx.getName(), ltx.getLayoutBlock().getDisplayName());  // NOI18N
                        }
                    } else if (lc.getConnectedType() == HitPointType.TURNOUT_C) {
                        // is Track Segment connected to diverging track of turnout?
                        Turnout ltxto = ltx.getTurnout();
                        if (ltxto != null) {
                            if (ltx.getContinuingSense() == Turnout.CLOSED) {
                                bs = new BeanSetting(ltxto, ltx.getTurnoutName(), Turnout.THROWN);
                            } else {
                                bs = new BeanSetting(ltxto, ltx.getTurnoutName(), Turnout.CLOSED);
                            }
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnout (B): LTO = {}, blk = {}", ltx.getName(), ltx.getLayoutBlock().getDisplayName());  // NOI18N
                        }
                    } else {
                        log.warn("Did not decode lc.getConnectedType() of {}", lc.getConnectedType());  // NOI18N
                    }
                } // is this Track Segment connected to the continuing track of a RH_XOVER or LH_XOVER?
                else if (HitPointType.isTurnoutHitType(lc.getConnectedType())
                        && ((((LayoutTurnout) prevConnection).getTurnoutType() == LayoutTurnout.TurnoutType.RH_XOVER)
                        || (((LayoutTurnout) prevConnection).getTurnoutType() == LayoutTurnout.TurnoutType.LH_XOVER))) {
                    LayoutTurnout ltz = (LayoutTurnout) prevConnection;
                    if (((ltz.getTurnoutType() == LayoutTurnout.TurnoutType.RH_XOVER)
                            && ((lc.getConnectedType() == HitPointType.TURNOUT_B)
                            || (lc.getConnectedType() == HitPointType.TURNOUT_D)))
                            || ((ltz.getTurnoutType() == LayoutTurnout.TurnoutType.LH_XOVER)
                            && ((lc.getConnectedType() == HitPointType.TURNOUT_A)
                            || (lc.getConnectedType() == HitPointType.TURNOUT_C)))) {
                            
                        Turnout ltzto = ltz.getTurnout();
                        if (ltzto != null) {
                            bs = new BeanSetting(ltzto, ltz.getTurnoutName(), Turnout.CLOSED);
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnout (C): LTO = {}, blk = {}, TO type = {}, conn type = {}", // NOI18N
                                    ltz.getName(), ltz.getLayoutBlock().getDisplayName(), ltz.getTurnoutType(), lc.getConnectedType());
                        }
                    }
                } // is this track section is connected to a slip?
                else if (HitPointType.isSlipHitType(lc.getConnectedType())) {
                    LayoutSlip lsz = (LayoutSlip) prevConnection;
                    if (lsz.getSlipType() == LayoutSlip.TurnoutType.SINGLE_SLIP) {
                        if (lc.getConnectedType() == HitPointType.SLIP_C) {
                            Turnout lszto = lsz.getTurnout();
                            if (lszto != null) {
                                bs = new BeanSetting(lszto, lsz.getTurnoutName(), lsz.getTurnoutState(LayoutTurnout.STATE_AC));
                                p.addSetting(bs);
                            } else {
                                log.error("No assigned turnout (D): LTO = {}, blk = {}", lsz.getName(), lsz.getLayoutBlock().getDisplayName());  // NOI18N
                            }
                            Turnout lsztob = lsz.getTurnoutB();
                            if (lsztob != null) {
                                bs = new BeanSetting(lsztob, lsz.getTurnoutBName(), lsz.getTurnoutBState(LayoutTurnout.STATE_AC));
                                p.addSetting(bs);
                            } else {
                                log.error("No assigned turnoutB (E): LTO = {}, blk = {}", lsz.getName(), lsz.getLayoutBlock().getDisplayName());  // NOI18N
                            }
                        } else if (lc.getConnectedType() == HitPointType.SLIP_B) {
                            Turnout lszto = lsz.getTurnout();
                            if (lszto != null) {
                                bs = new BeanSetting(lszto, lsz.getTurnoutName(), lsz.getTurnoutState(LayoutTurnout.STATE_BD));
                                p.addSetting(bs);
                            } else {
                                log.error("No assigned turnout (F): LTO = {}, blk = {}", lsz.getName(), lsz.getLayoutBlock().getDisplayName());  // NOI18N
                            }

                            Turnout lsztob = lsz.getTurnoutB();
                            if (lsztob != null) {
                                bs = new BeanSetting(lsztob, lsz.getTurnoutBName(), lsz.getTurnoutBState(LayoutTurnout.STATE_BD));
                                p.addSetting(bs);
                            } else {
                                log.error("No assigned turnoutB (G): LTO = {}, blk = {}", lsz.getName(), lsz.getLayoutBlock().getDisplayName());  // NOI18N
                            }
                        } else if (lc.getConnectedType() == HitPointType.SLIP_A) {
                            log.debug("At connection A of a single slip which could go in two different directions");  // NOI18N
                        } else if (lc.getConnectedType() == HitPointType.SLIP_D) {
                            log.debug("At connection D of a single slip which could go in two different directions");  // NOI18N
                        }
                    } else {
                        //note: I'm adding these logs as a prequel to adding the correct code for double slips
                        if (lc.getConnectedType() == HitPointType.SLIP_A) {
                            log.debug("At connection A of a double slip which could go in two different directions");  // NOI18N
                        } else if (lc.getConnectedType() == HitPointType.SLIP_B) {
                            log.debug("At connection B of a double slip which could go in two different directions");  // NOI18N
                        } else if (lc.getConnectedType() == HitPointType.SLIP_C) {
                            log.debug("At connection C of a double slip which could go in two different directions");  // NOI18N
                        } else if (lc.getConnectedType() == HitPointType.SLIP_D) {
                            log.debug("At connection D of a double slip which could go in two different directions");  // NOI18N
                        } else {    // this should NEVER happen (it should always be SLIP_A, _B, _C or _D.
                            log.info("At a double slip we could go in two different directions");  // NOI18N
                        }
                    }
                }
            } else {
                // block boundary is internal to a crossover turnout
                lt = lc.getXover();
                prevConnection = lt;
                if ((lt != null) && (lt.getTurnout() != null)) {
                    int type = lc.getXoverBoundaryType();
                    // bs is known to be null at this point
                    if (lt.getTurnout() != null) {
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
                            log.warn("failed to decode lc.getXoverBoundaryType() of {} (A)", lc.getXoverBoundaryType());  // NOI18N
                        }
                    }
                    typeCurConnection = HitPointType.TRACK;
                    if (bs != null) {
                        p.addSetting(bs);
                    } else {
                        log.error("No assigned turnout (H): LTO = {}, blk = {}, type = {}", lt.getName(), lt.getLayoutBlock().getDisplayName(), type);  // NOI18N
                    }
                }
            }
        } else if (lc.getXover() != null) {
            // first Block is not in a Track Segment, must be block boundary internal to a crossover turnout
            lt = lc.getXover();
            if ((lt != null) && (lt.getTurnout() != null)) {
                int type = lc.getXoverBoundaryType();
                // bs is known to be null at this point
                if (lt.getTurnout() != null) {
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
                        log.warn("failed to decode lc.getXoverBoundaryType() of {} (B)", lc.getXoverBoundaryType());  // NOI18N
                    }
                }
                typeCurConnection = HitPointType.TRACK;
                if (bs != null) {
                    p.addSetting(bs);
                } else {
                    log.error("No assigned turnout (I): LTO = {}, blk = {}, type = {}", lt.getName(), lt.getLayoutBlock().getDisplayName(), type);  // NOI18N
                }
            }
        } else {
            // block2 is this LayoutBlock, and block1 is in a track segment
            if (lc.getConnectedObject() != null) {
                // connected object in this block is a turnout or levelxing
                curConnection = lc.getConnectedObject();
                prevConnection = lc.getTrackSegment();
                typeCurConnection = lc.getConnectedType();
                if (HitPointType.isTurnoutHitType(typeCurConnection)) {
                    // connected object is a turnout
                    LayoutTurnout.TurnoutType turnoutType = ((LayoutTurnout) curConnection).getTurnoutType();
                    if (LayoutTurnout.hasEnteringDoubleTrack(turnoutType)) {
                        // have crossover turnout
                        if ((turnoutType == LayoutTurnout.TurnoutType.DOUBLE_XOVER)
                                || ((turnoutType == LayoutTurnout.TurnoutType.RH_XOVER) && ((typeCurConnection == HitPointType.TURNOUT_A) || (typeCurConnection == HitPointType.TURNOUT_C)))
                                || ((turnoutType == LayoutTurnout.TurnoutType.LH_XOVER) && ((typeCurConnection == HitPointType.TURNOUT_B) || (typeCurConnection == HitPointType.TURNOUT_D)))) {
                            // entering turnout at a throat, cannot follow path any further
                            curConnection = null;
                        } else {
                            // entering turnout at continuing track
                            if (((LayoutTurnout) curConnection).getTurnout() != null) {
                                bs = new BeanSetting(((LayoutTurnout) curConnection).getTurnout(), ((LayoutTurnout) curConnection).getTurnoutName(), Turnout.CLOSED);
                                p.addSetting(bs);
                            } else {
                                log.error("No assigned turnout (J): LTO = {}, blk = {}", // NOI18N
                                        ((LayoutTurnout) curConnection).getName(), ((LayoutTurnout) curConnection).getLayoutBlock().getDisplayName());
                            }
                            prevConnection = curConnection;
                            if (typeCurConnection == HitPointType.TURNOUT_A) {
                                curConnection = ((LayoutTurnout) curConnection).getConnectB();
                            } else if (typeCurConnection == HitPointType.TURNOUT_B) {
                                curConnection = ((LayoutTurnout) curConnection).getConnectA();
                            } else if (typeCurConnection == HitPointType.TURNOUT_C) {
                                curConnection = ((LayoutTurnout) curConnection).getConnectD();
                            } else { // typeCurConnection == LayoutEditor.HitPointTypes.TURNOUT_D per if statement 3 levels up
                                curConnection = ((LayoutTurnout) curConnection).getConnectC();
                            }
                            typeCurConnection = HitPointType.TRACK;
                        }
                    } // must be RH, LH, or WYE turnout
                    else if (typeCurConnection == HitPointType.TURNOUT_A) {
                        // turnout throat, no bean setting needed and cannot follow Path any further
                        log.debug("At connection A of a turnout which could go in two different directions");  // NOI18N
                        curConnection = null;
                    } else if (typeCurConnection == HitPointType.TURNOUT_B) {
                        // continuing track of turnout
                        if (((LayoutTurnout) curConnection).getTurnout() != null) {
                            if (((LayoutTurnout) curConnection).getContinuingSense() == Turnout.CLOSED) {
                                bs = new BeanSetting(((LayoutTurnout) curConnection).getTurnout(), ((LayoutTurnout) curConnection).getTurnoutName(), Turnout.CLOSED);
                            } else {
                                bs = new BeanSetting(((LayoutTurnout) curConnection).getTurnout(), ((LayoutTurnout) curConnection).getTurnoutName(), Turnout.THROWN);
                            }
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnout (K): LTO = {}, blk = {}", // NOI18N
                                    ((LayoutTurnout) curConnection).getName(), ((LayoutTurnout) curConnection).getLayoutBlock().getDisplayName());
                        }
                        prevConnection = curConnection;
                        curConnection = ((LayoutTurnout) curConnection).getConnectA();
                        typeCurConnection = HitPointType.TRACK;
                    } else if (typeCurConnection == HitPointType.TURNOUT_C) {
                        // diverging track of turnout
                        if (((LayoutTurnout) curConnection).getTurnout() != null) {
                            if (((LayoutTurnout) curConnection).getContinuingSense() == Turnout.CLOSED) {
                                bs = new BeanSetting(((LayoutTurnout) curConnection).getTurnout(), ((LayoutTurnout) curConnection).getTurnoutName(), Turnout.THROWN);
                            } else {
                                bs = new BeanSetting(((LayoutTurnout) curConnection).getTurnout(), ((LayoutTurnout) curConnection).getTurnoutName(), Turnout.CLOSED);
                            }
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnout (L): LTO = {}, blk = {}", // NOI18N
                                    ((LayoutTurnout) curConnection).getName(), ((LayoutTurnout) curConnection).getLayoutBlock().getDisplayName());
                        }
                        prevConnection = curConnection;
                        curConnection = ((LayoutTurnout) curConnection).getConnectA();
                        typeCurConnection = HitPointType.TRACK;
                    }
                } // if level crossing, skip to the connected track segment on opposite side
                else if (typeCurConnection == HitPointType.LEVEL_XING_A) {
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectC();
                    typeCurConnection = HitPointType.TRACK;
                } else if (typeCurConnection == HitPointType.LEVEL_XING_C) {
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectA();
                    typeCurConnection = HitPointType.TRACK;
                } else if (typeCurConnection == HitPointType.LEVEL_XING_B) {
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectD();
                    typeCurConnection = HitPointType.TRACK;
                } else if (typeCurConnection == HitPointType.LEVEL_XING_D) {
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectB();
                    typeCurConnection = HitPointType.TRACK;
                }
            } else {
                // block boundary is internal to a crossover turnout
                lt = lc.getXover();
                prevConnection = lt;
                if ((lt != null) && (lt.getTurnout() != null)) {
                    int type = lc.getXoverBoundaryType();
                    // bs is known to be null at this point
                    if (lt.getTurnout() != null) {
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
                    }
                    typeCurConnection = HitPointType.TRACK;
                    if (bs != null) {
                        p.addSetting(bs);
                    } else {
                        log.error("No assigned turnout (Q): LTO = {}, blk = {}", lt.getName(), lt.getLayoutBlock().getDisplayName());  // NOI18N
                    }
                }
            }
        }
        // follow path through this block - done when reaching another block, or a branching of Path
        while (curConnection != null) {
            if (typeCurConnection == HitPointType.TRACK) {
                TrackSegment curTS = (TrackSegment) curConnection;
                // track segment is current connection
                if (curTS.getLayoutBlock() != layoutBlock) {
                    curConnection = null;
                } else {
                    // skip over to other end of Track Segment
                    if (curTS.getConnect1() == prevConnection) {
                        prevConnection = curConnection;
                        typeCurConnection = curTS.getType2();
                        curConnection = curTS.getConnect2();
                    } else {
                        prevConnection = curConnection;
                        typeCurConnection = curTS.getType1();
                        curConnection = curTS.getConnect1();
                    }
                    // skip further if positionable point (possible anchor point)
                    if (typeCurConnection == HitPointType.POS_POINT) {
                        PositionablePoint pt = (PositionablePoint) curConnection;
                        if (pt.getType() == PositionablePoint.PointType.END_BUMPER) {
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
                                //  reached the end of the defined track
                                curConnection = null;
                            } else {
                                prevConnection = curConnection;
                                curConnection = track;
                                typeCurConnection = HitPointType.TRACK;
                            }
                        }
                    }
                }
            } else if (HitPointType.isTurnoutHitType(typeCurConnection)) {
                lt = (LayoutTurnout) curConnection;
                // test for crossover turnout
                if (lt.hasEnteringSingleTrack()) {
                    // have RH, LH, or WYE turnout

                    if (lt.getLayoutBlock() != layoutBlock) {
                        curConnection = null;
                    } else {
                        // turnout is in current block, test connection point
                        if (typeCurConnection == HitPointType.TURNOUT_A) {
                            // turnout throat, no bean setting needed and cannot follow possible path any further
                            curConnection = null;
                        } else if (typeCurConnection == HitPointType.TURNOUT_B) {
                            // continuing track of turnout, add a bean setting
                            if (lt.getTurnout() != null) {
                                if (lt.getContinuingSense() == Turnout.CLOSED) {
                                    bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                                } else {
                                    bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.THROWN);
                                }
                                p.addSetting(bs);
                            } else {
                                log.error("No assigned turnout (R): LTO = {}, blk = {}", lt.getName(), lt.getLayoutBlock().getDisplayName());  // NOI18N
                            }
                            if (lt.getLayoutBlock() != layoutBlock) {
                                curConnection = null;
                            } else {
                                prevConnection = curConnection;
                                curConnection = lt.getConnectA();
                                typeCurConnection = HitPointType.TRACK;
                            }
                        } else if (typeCurConnection == HitPointType.TURNOUT_C) {
                            // diverging track of turnout
                            if (lt.getTurnout() != null) {
                                if (lt.getContinuingSense() == Turnout.CLOSED) {
                                    bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.THROWN);
                                } else {
                                    bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                                }
                                p.addSetting(bs);
                            } else {
                                log.error("No assigned turnout (S): LTO = {}, blk = {}", lt.getName(), lt.getLayoutBlock().getDisplayName());  // NOI18N
                            }
                            if (lt.getLayoutBlock() != layoutBlock) {
                                curConnection = null;
                            } else {
                                prevConnection = curConnection;
                                curConnection = lt.getConnectA();
                                typeCurConnection = HitPointType.TRACK;
                            }
                        }
                    }
                } else if (lt.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER) {
                    // have a double crossover turnout, cannot follow possible path any further
                    curConnection = null;
                } else if (lt.getTurnoutType() == LayoutTurnout.TurnoutType.RH_XOVER) {
                    // have a right-handed crossover turnout
                    if ((typeCurConnection == HitPointType.TURNOUT_A)
                            || (typeCurConnection == HitPointType.TURNOUT_C)) {
                        // entry is at turnout throat, cannot follow possible path any further
                        curConnection = null;
                    } else if (typeCurConnection == HitPointType.TURNOUT_B) {
                        // entry is at continuing track of turnout
                        if (lt.getLayoutBlockB() != layoutBlock) {
                            // cross-over block different, end of current block
                            break;
                        }
                        if (lt.getTurnout() != null) {
                            bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnout (T): LTO = {}, blk = {}", lt.getName(), lt.getLayoutBlock().getDisplayName());  // NOI18N
                        }
                        if (lt.getLayoutBlock() != layoutBlock) {
                            // left current block
                            curConnection = null;
                        } else {
                            prevConnection = curConnection;
                            curConnection = lt.getConnectA();
                            typeCurConnection = HitPointType.TRACK;
                        }
                    } else { // typeCurConnection == LayoutEditor.HitPointTypes.TURNOUT_D
                        // entry is at continuing track of turnout
                        if (lt.getLayoutBlockD() != layoutBlock) {
                            // cross-over block different, end of current block
                            break;
                        }
                        if (lt.getTurnout() != null) {
                            bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnout (U): LTO = {}, blk = {}", lt.getName(), lt.getLayoutBlock().getDisplayName());  // NOI18N
                        }
                        if (lt.getLayoutBlockC() != layoutBlock) {
                            // left current block
                            curConnection = null;
                        } else {
                            prevConnection = curConnection;
                            curConnection = lt.getConnectC();
                            typeCurConnection = HitPointType.TRACK;
                        }
                    }
                } else if (lt.getTurnoutType() == LayoutTurnout.TurnoutType.LH_XOVER) {
                    // have a left-handed crossover turnout
                    if ((typeCurConnection == HitPointType.TURNOUT_B)
                            || (typeCurConnection == HitPointType.TURNOUT_D)) {
                        // entry is at turnout throat, cannot follow possible path any further
                        curConnection = null;
                    } else if (typeCurConnection == HitPointType.TURNOUT_A) {
                        // entry is at continuing track of turnout
                        if (lt.getLayoutBlock() != layoutBlock) {
                            // cross-over block different, end of current block
                            break;
                        }
                        if (lt.getTurnout() != null) {
                            bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnout (V): LTO = {}, blk = {}", lt.getName(), lt.getLayoutBlock().getDisplayName());  // NOI18N
                        }
                        if (lt.getLayoutBlockB() != layoutBlock) {
                            // left current block
                            curConnection = null;
                        } else {
                            prevConnection = curConnection;
                            curConnection = lt.getConnectB();
                            typeCurConnection = HitPointType.TRACK;
                        }
                    } else { // typeCurConnection == LayoutEditor.HitPointTypes.TURNOUT_C per if statement 2 levels up
                        // entry is at continuing track of turnout
                        if (lt.getLayoutBlockC() != layoutBlock) {
                            // cross-over block different, end of current block
                            break;
                        }
                        if (lt.getTurnout() != null) {
                            bs = new BeanSetting(lt.getTurnout(), lt.getTurnoutName(), Turnout.CLOSED);
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnout (W): LTO = {}, blk = {}", lt.getName(), lt.getLayoutBlock().getDisplayName());  // NOI18N
                        }
                        if (lt.getLayoutBlockD() != layoutBlock) {
                            // left current block
                            curConnection = null;
                        } else {
                            prevConnection = curConnection;
                            curConnection = lt.getConnectD();
                            typeCurConnection = HitPointType.TRACK;
                        }
                    }
                }
            } else if (typeCurConnection == HitPointType.LEVEL_XING_A) {
                // have a level crossing connected at A
                if (((LevelXing) curConnection).getLayoutBlockAC() != layoutBlock) {
                    // moved outside of this block
                    curConnection = null;
                } else {
                    // move to other end of this section of this level crossing track
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectC();
                    typeCurConnection = HitPointType.TRACK;
                }
            } else if (typeCurConnection == HitPointType.LEVEL_XING_B) {
                // have a level crossing connected at B
                if (((LevelXing) curConnection).getLayoutBlockBD() != layoutBlock) {
                    // moved outside of this block
                    curConnection = null;
                } else {
                    // move to other end of this section of this level crossing track
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectD();
                    typeCurConnection = HitPointType.TRACK;
                }
            } else if (typeCurConnection == HitPointType.LEVEL_XING_C) {
                // have a level crossing connected at C
                if (((LevelXing) curConnection).getLayoutBlockAC() != layoutBlock) {
                    // moved outside of this block
                    curConnection = null;
                } else {
                    // move to other end of this section of this level crossing track
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectA();
                    typeCurConnection = HitPointType.TRACK;
                }
            } else if (typeCurConnection == HitPointType.LEVEL_XING_D) {
                // have a level crossing connected at D
                if (((LevelXing) curConnection).getLayoutBlockBD() != layoutBlock) {
                    // moved outside of this block
                    curConnection = null;
                } else {
                    // move to other end of this section of this level crossing track
                    prevConnection = curConnection;
                    curConnection = ((LevelXing) curConnection).getConnectB();
                    typeCurConnection = HitPointType.TRACK;
                }
            } else if (HitPointType.isSlipHitType(typeCurConnection)) {
                LayoutSlip ls = (LayoutSlip) curConnection;
                if (ls.getLayoutBlock() != layoutBlock) {
                    curConnection = null;
                } else if (ls.getSlipType() == LayoutSlip.TurnoutType.SINGLE_SLIP) {
                    if (typeCurConnection == HitPointType.SLIP_C) {
                        if (ls.getTurnout() != null) {
                            bs = new BeanSetting(ls.getTurnout(), ls.getTurnoutName(), ls.getTurnoutState(LayoutTurnout.STATE_AC));
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnout (X): LTO = {}, blk = {}", ls.getName(), ls.getLayoutBlock().getDisplayName());  // NOI18N
                        }
                        if (ls.getTurnoutB() != null) {
                            bs = new BeanSetting(ls.getTurnoutB(), ls.getTurnoutBName(), ls.getTurnoutBState(LayoutTurnout.STATE_AC));
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnoutB (Y): LTO = {}, blk = {}", ls.getName(), ls.getLayoutBlock().getDisplayName());  // NOI18N
                        }
                        prevConnection = curConnection;
                        curConnection = ((LayoutSlip) curConnection).getConnectC();
                        typeCurConnection = HitPointType.TRACK;
                    } else if (typeCurConnection == HitPointType.SLIP_B) {
                        if (ls.getTurnout() != null) {
                            bs = new BeanSetting(ls.getTurnout(), ls.getTurnoutName(), ls.getTurnoutState(LayoutTurnout.STATE_BD));
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnout (Z): LTO = {}, blk = {}", ls.getName(), ls.getLayoutBlock().getDisplayName());  // NOI18N
                        }

                        if (ls.getTurnoutB() != null) {
                            bs = new BeanSetting(ls.getTurnoutB(), ls.getTurnoutBName(), ls.getTurnoutBState(LayoutTurnout.STATE_BD));
                            p.addSetting(bs);
                        } else {
                            log.error("No assigned turnoutB (1): LTO = {}, blk = {}", ls.getName(), ls.getLayoutBlock().getDisplayName());  // NOI18N
                        }
                        prevConnection = curConnection;
                        curConnection = ((LayoutSlip) curConnection).getConnectB();
                        typeCurConnection = HitPointType.TRACK;
                    } else {
                        //Else could be going in the slip direction
                        curConnection = null;
                    }

                } else {
                    //At double slip, can not follow any further
                    curConnection = null;
                }
            } else if (HitPointType.isTurntableRayHitType(typeCurConnection)) {
                if (log.isDebugEnabled()) {
                    log.debug("Layout Block: {}, found track type: {}, to " // NOI18N
                            + "Block: {}, is potentially assigned to turntable ray", // NOI18N
                            layoutBlock.getDisplayName(),
                            typeCurConnection,
                            p.getBlock().getDisplayName()
                    );
                }
                curConnection = null;
            } else {
                // catch when some new type got added
                log.error("Layout Block: {} found unknown track type: {}" // NOI18N
                        + " to Block: {}",
                        layoutBlock.getDisplayName(),
                        typeCurConnection,
                        p.getBlock().getDisplayName()
                );
                break;
            }
        }
    }   // addBeanSettings

    // initialize logging
    private final static Logger log
            = LoggerFactory.getLogger(LayoutEditorAuxTools.class);
}
