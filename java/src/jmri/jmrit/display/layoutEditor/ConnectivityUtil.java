package jmri.jmrit.display.layoutEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.Block;
import jmri.EntryPoint;
import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.jmrit.blockboss.BlockBossLogic;
import jmri.jmrit.blockboss.BlockBossLogicProvider;

/**
 * ConnectivityUtil provides methods supporting use of layout connectivity
 * available in Layout Editor panels. These tools allow outside classes to
 * inquire into connectivity information contained in a specified Layout Editor
 * panel.
 * <p>
 * Connectivity information is stored in the track diagram of a Layout Editor
 * panel. The "connectivity graph" of the layout consists of nodes
 * (LayoutTurnouts, LevelXings, and PositionablePoints) connected by lines
 * (TrackSegments). These methods extract information from the connection graph
 * and make it available. Each instance of ConnectivityUtil is associated with a
 * specific Layout Editor panel, and is accessed via that LayoutEditor panel's
 * 'getConnectivityUtil' method.
 * <p>
 * The methods in this module do not modify the Layout in any way, or change the
 * state of items on the layout. They only provide information to allow other
 * modules to do so as appropriate. For example, the "getTurnoutList" method
 * provides information about the turnouts in a block, but does not test the
 * state, or change the state, of any turnout.
 * <p>
 * The methods in this module are accessed via direct calls from the inquiring
 * method.
 * <p>
 * A single object of this type, obtained via {@link LayoutEditor#getConnectivityUtil()}
 * is shared across all instances of {@link LayoutBlock}.
 *
 * @author Dave Duchamp Copyright (c) 2009
 * @author George Warner Copyright (c) 2017-2018
 */
final public class ConnectivityUtil {

    // constants
    // operational instance variables
    final private LayoutEditor layoutEditor;
    final private LayoutEditorAuxTools auxTools;
    final private LayoutBlockManager layoutBlockManager;

    private final int TRACKNODE_CONTINUING = 0;
    private final int TRACKNODE_DIVERGING = 1;
    private final int TRACKNODE_DIVERGING_2ND_3WAY = 2;

    private final BlockBossLogicProvider blockBossLogicProvider;

    // constructor method
    public ConnectivityUtil(LayoutEditor thePanel) {
        layoutEditor = thePanel;
        auxTools = layoutEditor.getLEAuxTools();
        layoutBlockManager = InstanceManager.getDefault(LayoutBlockManager.class);
        blockBossLogicProvider = InstanceManager.getDefault(BlockBossLogicProvider.class);
    }

    private TrackSegment trackSegment = null;
    private HitPointType prevConnectType = HitPointType.NONE;
    private LayoutTrack prevConnectTrack = null;
    private LayoutBlock currLayoutBlock = null;
    private LayoutBlock nextLayoutBlock = null;

    /**
     * Provide a list of LayoutTurnouts in the specified Block, in order,
     * beginning at the connection to the specified previous Block and
     * continuing to the specified next Block. Also compiles a companion list of
     * how the turnout should be set for the specified connectivity. The
     * companion list can be accessed by "getTurnoutSettingList" immediately
     * after this method returns.
     *
     * @param currBlock the block to list LayoutTurnouts in
     * @param prevBlock the previous block
     * @param nextBlock the following block
     * @return the list of all turnouts in the block if prevBlock or nextBlock
     *         are null or the list of all turnouts required to transit
     *         currBlock between prevBlock and nextBlock; returns an empty list
     *         if prevBlock and nextBlock are not null and are not connected
     */
    @Nonnull
    public List<LayoutTrackExpectedState<LayoutTurnout>> getTurnoutList(
            @CheckForNull Block currBlock,
            @CheckForNull Block prevBlock,
            @CheckForNull Block nextBlock) {
        return getTurnoutList(currBlock, prevBlock, nextBlock, false);
    }

    /**
     * Provide a list of LayoutTurnouts in the specified Block, in order,
     * beginning at the connection to the specified previous Block and
     * continuing to the specified next Block. Also compiles a companion list of
     * how the turnout should be set for the specified connectivity. The
     * companion list can be accessed by "getTurnoutSettingList" immediately
     * after this method returns.
     *
     * @param currBlock the block to list LayoutTurnouts in
     * @param prevBlock the previous block
     * @param nextBlock the following block
     * @param suppress  true to prevent errors from being logged; false
     *                  otherwise
     * @return the list of all turnouts in the block if prevBlock or nextBlock
     *         are null or the list of all turnouts required to transit
     *         currBlock between prevBlock and nextBlock; returns an empty list
     *         if prevBlock and nextBlock are not null and are not connected
     */
    @Nonnull
    public List<LayoutTrackExpectedState<LayoutTurnout>> getTurnoutList(
            @CheckForNull Block currBlock,
            @CheckForNull Block prevBlock,
            @CheckForNull Block nextBlock,
            boolean suppress) {
        List<LayoutTrackExpectedState<LayoutTurnout>> result = new ArrayList<>();

        // initialize
        currLayoutBlock = null;
        String currUserName = null;
        if (currBlock != null) {
            currUserName = currBlock.getUserName();
            if ((currUserName != null) && !currUserName.isEmpty()) {
                currLayoutBlock = layoutBlockManager.getByUserName(currUserName);
            }
        }

        LayoutBlock prevLayoutBlock = null;
        if (prevBlock != null) {
            String prevUserName = prevBlock.getUserName();
            if ((prevUserName != null) && !prevUserName.isEmpty()) {
                prevLayoutBlock = layoutBlockManager.getByUserName(prevUserName);
            }
        }

        nextLayoutBlock = null;
        if (nextBlock != null) {
            String nextUserName = nextBlock.getUserName();
            if ((nextUserName != null) && !nextUserName.isEmpty()) {
                nextLayoutBlock = layoutBlockManager.getByUserName(nextUserName);
            }
        }

        turnoutConnectivity = true;
        if ((prevLayoutBlock == null) || (nextLayoutBlock == null)) {
            // special search with partial information - not as good, order not assured
            List<LayoutTurnout> allTurnouts = getAllTurnoutsThisBlock(currLayoutBlock);
            for (LayoutTurnout lt : allTurnouts) {
                result.add(new LayoutTrackExpectedState<>(lt,
                        lt.getConnectivityStateForLayoutBlocks(
                                currLayoutBlock, prevLayoutBlock, nextLayoutBlock, true)));
            }
            return result;
        }

        List<LayoutConnectivity> cList = auxTools.getConnectivityList(currLayoutBlock);
        HitPointType cType;
        // initialize the connectivity search, processing a turnout in this block if it is present
        boolean notFound = true;
        for (int i = 0; (i < cList.size()) && notFound; i++) {
            LayoutConnectivity lc = cList.get(i);
            if ((lc.getXover() != null) && (((lc.getBlock1() == currLayoutBlock) && (lc.getBlock2() == prevLayoutBlock))
                    || ((lc.getBlock1() == prevLayoutBlock) && (lc.getBlock2() == currLayoutBlock)))) {
                // have a block boundary in a crossover turnout, add turnout to the List
                LayoutTurnout xt = lc.getXover();
                int setting = Turnout.THROWN;
                // determine setting and setup track segment if there is one
                trackSegment = null;
                prevConnectTrack = xt;
                switch (lc.getXoverBoundaryType()) {
                    case LayoutConnectivity.XOVER_BOUNDARY_AB: {
                        setting = Turnout.CLOSED;
                        if (((TrackSegment) xt.getConnectA() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectA()).getLayoutBlock())) {
                            // block exits Xover at A
                            trackSegment = (TrackSegment) xt.getConnectA();
                            prevConnectType = HitPointType.TURNOUT_A;
                        } else if (((TrackSegment) xt.getConnectB() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectB()).getLayoutBlock())) {
                            // block exits Xover at B
                            trackSegment = (TrackSegment) xt.getConnectB();
                            prevConnectType = HitPointType.TURNOUT_B;
                        }
                        break;
                    }
                    case LayoutConnectivity.XOVER_BOUNDARY_CD: {
                        setting = Turnout.CLOSED;
                        if (((TrackSegment) xt.getConnectC() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectC()).getLayoutBlock())) {
                            // block exits Xover at C
                            trackSegment = (TrackSegment) xt.getConnectC();
                            prevConnectType = HitPointType.TURNOUT_C;
                        } else if (((TrackSegment) xt.getConnectD() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectD()).getLayoutBlock())) {
                            // block exits Xover at D
                            trackSegment = (TrackSegment) xt.getConnectD();
                            prevConnectType = HitPointType.TURNOUT_D;
                        }
                        break;
                    }
                    case LayoutConnectivity.XOVER_BOUNDARY_AC: {
                        if (((TrackSegment) xt.getConnectA() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectA()).getLayoutBlock())) {
                            // block exits Xover at A
                            trackSegment = (TrackSegment) xt.getConnectA();
                            prevConnectType = HitPointType.TURNOUT_A;
                        } else if (((TrackSegment) xt.getConnectC() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectC()).getLayoutBlock())) {
                            // block exits Xover at C
                            trackSegment = (TrackSegment) xt.getConnectC();
                            prevConnectType = HitPointType.TURNOUT_C;
                        }
                        break;
                    }
                    case LayoutConnectivity.XOVER_BOUNDARY_BD: {
                        if (((TrackSegment) xt.getConnectB() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectB()).getLayoutBlock())) {
                            // block exits Xover at B
                            trackSegment = (TrackSegment) xt.getConnectB();
                            prevConnectType = HitPointType.TURNOUT_B;
                        } else if (((TrackSegment) xt.getConnectD() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectD()).getLayoutBlock())) {
                            // block exits Xover at D
                            trackSegment = (TrackSegment) xt.getConnectD();
                            prevConnectType = HitPointType.TURNOUT_D;
                        }
                        break;
                    }
                    default: {
                        log.error("Unhandled crossover boundary type: {}", lc.getXoverBoundaryType());
                        break;
                    }
                }
                result.add(new LayoutTrackExpectedState<>(xt, setting));
                notFound = false;
            } else if ((lc.getBlock1() == currLayoutBlock) && (lc.getBlock2() == prevLayoutBlock)) {
                // no turnout or level crossing at the beginning of this block
                trackSegment = lc.getTrackSegment();
                if (lc.getConnectedType() == HitPointType.TRACK) {
                    prevConnectType = HitPointType.POS_POINT;
                    prevConnectTrack = lc.getAnchor();
                } else {
                    prevConnectType = lc.getConnectedType();
                    prevConnectTrack = lc.getConnectedObject();
                }
                notFound = false;
            } else if ((lc.getBlock2() == currLayoutBlock) && (lc.getBlock1() == prevLayoutBlock)) {
                cType = lc.getConnectedType();
                // check for connection to a track segment
                if (cType == HitPointType.TRACK) {
                    trackSegment = (TrackSegment) lc.getConnectedObject();
                    prevConnectType = HitPointType.POS_POINT;
                    prevConnectTrack = lc.getAnchor();
                } // check for a level crossing
                else if (HitPointType.isLevelXingHitType(cType)) {
                    // entering this Block at a level crossing, skip over it an initialize the next
                    //      TrackSegment if there is one in this Block
                    setupOpposingTrackSegment((LevelXing) lc.getConnectedObject(), cType);
                } // check for turnout
                else if (HitPointType.isTurnoutHitType(cType)) {
                    // add turnout to list
                    result.add(new LayoutTrackExpectedState<>((LayoutTurnout) lc.getConnectedObject(),
                            getTurnoutSetting((LayoutTurnout) lc.getConnectedObject(), cType, suppress)));
                } else if (HitPointType.isSlipHitType(cType)) {
                    result.add(new LayoutTrackExpectedState<>((LayoutTurnout) lc.getConnectedObject(),
                            getTurnoutSetting((LayoutTurnout) lc.getConnectedObject(), cType, suppress)));
                }
                notFound = false;
            }
        }
        if (notFound) {
            if (prevBlock != null) {    // could not initialize the connectivity search
                if (!suppress) {
                    log.warn("Could not find connection between Blocks {} and {}", currUserName, prevBlock.getUserName());
                }
            } else if (!suppress) {
                log.warn("Could not find connection between Blocks {}, prevBock is null!", currUserName);
            }
            return result;
        }
        // search connectivity for turnouts by following TrackSegments to end of Block
        while (trackSegment != null) {
            LayoutTrack cObject;
            // identify next connection
            if ((trackSegment.getConnect1() == prevConnectTrack) && (trackSegment.getType1() == prevConnectType)) {
                cType = trackSegment.getType2();
                cObject = trackSegment.getConnect2();
            } else if ((trackSegment.getConnect2() == prevConnectTrack) && (trackSegment.getType2() == prevConnectType)) {
                cType = trackSegment.getType1();
                cObject = trackSegment.getConnect1();
            } else {
                if (!suppress) {
                    log.error("Connectivity error when searching turnouts in Block {}", currLayoutBlock.getDisplayName());
                    log.warn("Track segment connected to {{}, {}} and {{}, {}} but previous object was {{}, {}}",
                            trackSegment.getConnect1(), trackSegment.getType1().name(),
                            trackSegment.getConnect2(), trackSegment.getType2().name(),
                            prevConnectTrack, prevConnectType);
                }
                trackSegment = null;
                break;
            }
            if (cType == HitPointType.POS_POINT) {
                // reached anchor point or end bumper
                if (((PositionablePoint) cObject).getType() == PositionablePoint.PointType.END_BUMPER) {
                    // end of line
                    trackSegment = null;
                } else if (((PositionablePoint) cObject).getType() == PositionablePoint.PointType.ANCHOR || (((PositionablePoint) cObject).getType() == PositionablePoint.PointType.EDGE_CONNECTOR)) {
                    // proceed to next track segment if within the same Block
                    if (((PositionablePoint) cObject).getConnect1() == trackSegment) {
                        trackSegment = ((PositionablePoint) cObject).getConnect2();
                    } else {
                        trackSegment = ((PositionablePoint) cObject).getConnect1();
                    }
                    if ((trackSegment == null) || (trackSegment.getLayoutBlock() != currLayoutBlock)) {
                        // track segment is not in this block
                        trackSegment = null;
                    } else {
                        prevConnectType = cType;
                        prevConnectTrack = cObject;
                    }
                }
            } else if (HitPointType.isLevelXingHitType(cType)) {
                // reached a level crossing, is it within this block?
                switch (cType) {
                    case LEVEL_XING_A:
                    case LEVEL_XING_C: {
                        if (((LevelXing) cObject).getLayoutBlockAC() != currLayoutBlock) {
                            // outside of block
                            trackSegment = null;
                        } else {
                            // same block
                            setupOpposingTrackSegment((LevelXing) cObject, cType);
                        }
                        break;
                    }
                    case LEVEL_XING_B:
                    case LEVEL_XING_D: {
                        if (((LevelXing) cObject).getLayoutBlockBD() != currLayoutBlock) {
                            // outside of block
                            trackSegment = null;
                        } else {
                            // same block
                            setupOpposingTrackSegment((LevelXing) cObject, cType);
                        }
                        break;
                    }
                    default: {
                        log.warn("Unhandled Level Crossing type: {}", cType);
                        break;
                    }
                }
            } else if (HitPointType.isTurnoutHitType(cType)) {
                // reached a turnout
                LayoutTurnout lt = (LayoutTurnout) cObject;
                LayoutTurnout.TurnoutType tType = lt.getTurnoutType();
                // is this turnout a crossover turnout at least partly within this block?
                if (LayoutTurnout.isTurnoutTypeXover(tType)) {
                    // reached a crossover turnout
                    switch (cType) {
                        case TURNOUT_A:
                            if ((lt.getLayoutBlock()) != currLayoutBlock) {
                                // connection is outside of the current block
                                trackSegment = null;
                            } else if (lt.getLayoutBlockB() == nextLayoutBlock) {
                                // exits Block at B
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = null;
                            } else if ((lt.getLayoutBlockC() == nextLayoutBlock) && (tType != LayoutTurnout.TurnoutType.LH_XOVER)) {
                                // exits Block at C, either Double or RH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = null;
                            } else if (lt.getLayoutBlockB() == currLayoutBlock) {
                                // block continues at B
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = (TrackSegment) lt.getConnectB();
                                prevConnectType = HitPointType.TURNOUT_B;
                                prevConnectTrack = cObject;
                            } else if ((lt.getLayoutBlockC() == currLayoutBlock) && (tType != LayoutTurnout.TurnoutType.LH_XOVER)) {
                                // block continues at C, either Double or RH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = (TrackSegment) lt.getConnectC();
                                prevConnectType = HitPointType.TURNOUT_C;
                                prevConnectTrack = cObject;
                            } else if (lt.getLayoutBlock() == currLayoutBlock && currLayoutBlock == nextLayoutBlock) {
                                //we are at our final destination so not an error such
                                trackSegment = null;
                            } else {
                                // no legal outcome found, print error
                                if (!suppress) {
                                    log.warn("Connectivity mismatch at A in turnout {}", lt.getTurnoutName());
                                }
                                trackSegment = null;
                            }
                            break;
                        case TURNOUT_B:
                            if ((lt.getLayoutBlockB()) != currLayoutBlock) {
                                // connection is outside of the current block
                                trackSegment = null;
                            } else if (lt.getLayoutBlock() == nextLayoutBlock) {
                                // exits Block at A
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = null;
                            } else if ((lt.getLayoutBlockD() == nextLayoutBlock) && (tType != LayoutTurnout.TurnoutType.RH_XOVER)) {
                                // exits Block at D, either Double or LH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = null;
                            } else if (lt.getLayoutBlock() == currLayoutBlock) {
                                // block continues at A
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = (TrackSegment) lt.getConnectA();
                                prevConnectType = HitPointType.TURNOUT_A;
                                prevConnectTrack = cObject;
                            } else if ((lt.getLayoutBlockD() == currLayoutBlock) && (tType != LayoutTurnout.TurnoutType.RH_XOVER)) {
                                // block continues at D, either Double or LH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = (TrackSegment) lt.getConnectD();
                                prevConnectType = HitPointType.TURNOUT_D;
                                prevConnectTrack = cObject;
                            } else if (lt.getLayoutBlockB() == currLayoutBlock && currLayoutBlock == nextLayoutBlock) {
                                //we are at our final destination so not an error such
                                trackSegment = null;
                            } else {
                                // no legal outcome found, print error
                                if (!suppress) {
                                    log.warn("Connectivity mismatch at B in turnout {}", lt.getTurnoutName());
                                }
                                trackSegment = null;
                            }
                            break;
                        case TURNOUT_C:
                            if ((lt.getLayoutBlockC()) != currLayoutBlock) {
                                // connection is outside of the current block
                                trackSegment = null;
                            } else if (lt.getLayoutBlockD() == nextLayoutBlock) {
                                // exits Block at D
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = null;
                            } else if ((lt.getLayoutBlock() == nextLayoutBlock) && (tType != LayoutTurnout.TurnoutType.LH_XOVER)) {
                                // exits Block at A, either Double or RH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = null;
                            } else if (lt.getLayoutBlockD() == currLayoutBlock) {
                                // block continues at D
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = (TrackSegment) lt.getConnectD();
                                prevConnectType = HitPointType.TURNOUT_D;
                                prevConnectTrack = cObject;
                            } else if ((lt.getLayoutBlock() == currLayoutBlock) && (tType != LayoutTurnout.TurnoutType.LH_XOVER)) {
                                // block continues at A, either Double or RH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = (TrackSegment) lt.getConnectA();
                                prevConnectType = HitPointType.TURNOUT_A;
                                prevConnectTrack = cObject;
                            } else if (lt.getLayoutBlockC() == currLayoutBlock && currLayoutBlock == nextLayoutBlock) {
                                //we are at our final destination so not an error such
                                trackSegment = null;
                            } else {
                                // no legal outcome found, print error
                                if (!suppress) {
                                    log.warn("Connectivity mismatch at C in turnout {}", lt.getTurnoutName());
                                }
                                trackSegment = null;
                            }
                            break;
                        case TURNOUT_D:
                            if ((lt.getLayoutBlockD()) != currLayoutBlock) {
                                // connection is outside of the current block
                                trackSegment = null;
                            } else if (lt.getLayoutBlockC() == nextLayoutBlock) {
                                // exits Block at C
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = null;
                            } else if ((lt.getLayoutBlockB() == nextLayoutBlock) && (tType != LayoutTurnout.TurnoutType.RH_XOVER)) {
                                // exits Block at B, either Double or LH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = null;
                            } else if (lt.getLayoutBlockC() == currLayoutBlock) {
                                // block continues at C
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = (TrackSegment) lt.getConnectC();
                                prevConnectType = HitPointType.TURNOUT_C;
                                prevConnectTrack = cObject;
                            } else if ((lt.getLayoutBlockB() == currLayoutBlock) && (tType != LayoutTurnout.TurnoutType.RH_XOVER)) {
                                // block continues at B, either Double or LH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = (TrackSegment) lt.getConnectB();
                                prevConnectType = HitPointType.TURNOUT_B;
                                prevConnectTrack = cObject;
                            } else if (lt.getLayoutBlockD() == currLayoutBlock && currLayoutBlock == nextLayoutBlock) {
                                //we are at our final destination so not an error such
                                trackSegment = null;
                            } else {
                                // no legal outcome found, print error
                                if (!suppress) {
                                    log.warn("Connectivity mismatch at D in turnout {}", lt.getTurnoutName());
                                }
                                trackSegment = null;
                            }
                            break;
                        default: {
                            log.warn("Unhandled crossover type: {}", cType);
                            break;
                        }
                    }
                } else if (LayoutTurnout.isTurnoutTypeTurnout(tType)) {
                    // reached RH. LH, or WYE turnout, is it in the current Block?
                    if (lt.getLayoutBlock() != currLayoutBlock) {
                        // turnout is outside of current block
                        trackSegment = null;
                    } else {
                        // turnout is inside current block, add it to the list
                        result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, getTurnoutSetting(lt, cType, suppress)));
                    }
                }
            } else if (HitPointType.isSlipHitType(cType)) {
                // reached a LayoutSlip
                LayoutSlip ls = (LayoutSlip) cObject;
                if (((cType == HitPointType.SLIP_A) && (ls.getLayoutBlock() != currLayoutBlock))
                        || ((cType == HitPointType.SLIP_B) && (ls.getLayoutBlockB() != currLayoutBlock))
                        || ((cType == HitPointType.SLIP_C) && (ls.getLayoutBlockC() != currLayoutBlock))
                        || ((cType == HitPointType.SLIP_D) && (ls.getLayoutBlockD() != currLayoutBlock))) {
                    //Slip is outside of the current block
                    trackSegment = null;
                } else {
                    // turnout is inside current block, add it to the list
                    result.add(new LayoutTrackExpectedState<>(ls, getTurnoutSetting(ls, cType, suppress)));
                }
            } else if (HitPointType.isTurntableRayHitType(cType)) {
                // Declare arrival at a turntable ray to be the end of the block
                trackSegment = null;
            }
        }
        return result;
    }

    /**
     * Get a list of all Blocks connected to a specified Block.
     *
     * @param block the block to get connections for
     * @return connected blocks or an empty list if none
     */
    @Nonnull
    public List<Block> getConnectedBlocks(@Nonnull Block block
    ) {
        List<Block> result = new ArrayList<>();
        //
        //TODO: Dead-code strip (after 4.9.x)
        // dissusion: lBlock could be used to match against getBlock1 & 2...
        //              instead of matching against block == getBlock()
        //
        //String userName = block.getUserName();
        //LayoutBlock lBlock = null;
        //if ((userName != null) && !userName.isEmpty()) {
        //    lBlock = layoutBlockManager.getByUserName(userName);
        //}
        List<LayoutConnectivity> cList = auxTools.getConnectivityList(currLayoutBlock);
        for (LayoutConnectivity lc : cList) {
            if (lc.getBlock1().getBlock() == block) {
                result.add((lc.getBlock2()).getBlock());
            } else if (lc.getBlock2().getBlock() == block) {
                result.add((lc.getBlock1()).getBlock());
            }
        }
        return result;
    }

    /**
     * Get a list of all anchor point boundaries involving the specified Block.
     *
     * @param block the block to get anchor point boundaries for
     * @return a list of anchor point boundaries
     */
    @Nonnull
    public List<PositionablePoint> getAnchorBoundariesThisBlock(
            @Nonnull Block block
    ) {
        List<PositionablePoint> result = new ArrayList<>();
        String userName = block.getUserName();
        LayoutBlock lBlock = null;
        if ((userName != null) && !userName.isEmpty()) {
            lBlock = layoutBlockManager.getByUserName(userName);
        }
        for (PositionablePoint p : layoutEditor.getPositionablePoints()) {
            if ((p.getConnect2() != null) && (p.getConnect1() != null)) {
                if ((p.getConnect2().getLayoutBlock() != null)
                        && (p.getConnect1().getLayoutBlock() != null)) {
                    if ((((p.getConnect1()).getLayoutBlock() == lBlock)
                            && ((p.getConnect2()).getLayoutBlock() != lBlock))
                            || (((p.getConnect1()).getLayoutBlock() != lBlock)
                            && ((p.getConnect2()).getLayoutBlock() == lBlock))) {
                        result.add(p);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get a list of all LevelXings involving the specified Block. To be listed,
     * a LevelXing must have all its four connections and all blocks must be
     * assigned. If any connection is missing, or if a block assignment is
     * missing, an error message is printed and the level crossing is not added
     * to the list.
     *
     * @param block the block to check
     * @return a list of all complete LevelXings
     */
    @Nonnull
    public List<LevelXing> getLevelCrossingsThisBlock(@Nonnull Block block
    ) {
        List<LevelXing> result = new ArrayList<>();
        String userName = block.getUserName();
        LayoutBlock lBlock = null;
        if ((userName != null) && !userName.isEmpty()) {
            lBlock = layoutBlockManager.getByUserName(userName);
        }
        for (LevelXing x : layoutEditor.getLevelXings()) {
            boolean found = false;
            if ((x.getLayoutBlockAC() == lBlock) || (x.getLayoutBlockBD() == lBlock)) {
                found = true;
            } else if ((x.getConnectA() != null) && (((TrackSegment) x.getConnectA()).getLayoutBlock() == lBlock)) {
                found = true;
            } else if ((x.getConnectB() != null) && (((TrackSegment) x.getConnectB()).getLayoutBlock() == lBlock)) {
                found = true;
            } else if ((x.getConnectC() != null) && (((TrackSegment) x.getConnectC()).getLayoutBlock() == lBlock)) {
                found = true;
            } else if ((x.getConnectD() != null) && (((TrackSegment) x.getConnectD()).getLayoutBlock() == lBlock)) {
                found = true;
            }
            if (found) {
                if ((x.getConnectA() != null) && (((TrackSegment) x.getConnectA()).getLayoutBlock() != null)
                        && (x.getConnectB() != null) && (((TrackSegment) x.getConnectB()).getLayoutBlock() != null)
                        && (x.getConnectC() != null) && (((TrackSegment) x.getConnectC()).getLayoutBlock() != null)
                        && (x.getConnectD() != null) && (((TrackSegment) x.getConnectD()).getLayoutBlock() != null)
                        && (x.getLayoutBlockAC() != null) && (x.getLayoutBlockBD() != null)) {
                    result.add(x);
                } else {
                    log.error("Missing connection or block assignment at Level Crossing in Block {}", block.getDisplayName());
                }
            }
        }
        return result;
    }

    /**
     * Get a list of all layout turnouts involving the specified Block.
     *
     * @param block the Block to get layout turnouts for
     * @return the list of associated layout turnouts or an empty list if none
     */
    @Nonnull
    public List<LayoutTurnout> getLayoutTurnoutsThisBlock(@Nonnull Block block
    ) {
        List<LayoutTurnout> result = new ArrayList<>();
        String userName = block.getUserName();
        LayoutBlock lBlock = null;
        if ((userName != null) && !userName.isEmpty()) {
            lBlock = layoutBlockManager.getByUserName(userName);
        }
        for (LayoutTurnout t : layoutEditor.getLayoutTurnouts()) {
            if ((t.getBlockName().equals(userName)) || (t.getBlockBName().equals(userName))
                    || (t.getBlockCName().equals(userName)) || (t.getBlockDName().equals(userName))) {
                result.add(t);
            } else if ((t.getConnectA() != null) && (((TrackSegment) t.getConnectA()).getLayoutBlock() == lBlock)) {
                result.add(t);
            } else if ((t.getConnectB() != null) && (((TrackSegment) t.getConnectB()).getLayoutBlock() == lBlock)) {
                result.add(t);
            } else if ((t.getConnectC() != null) && (((TrackSegment) t.getConnectC()).getLayoutBlock() == lBlock)) {
                result.add(t);
            } else if ((t.getConnectD() != null) && (((TrackSegment) t.getConnectD()).getLayoutBlock() == lBlock)) {
                result.add(t);
            }
        }
        for (LayoutTurnout ls : layoutEditor.getLayoutTurnouts()) {
            if (ls.getBlockName().equals(userName)) {
                result.add(ls);
            } else if ((ls.getConnectA() != null) && (((TrackSegment) ls.getConnectA()).getLayoutBlock() == lBlock)) {
                result.add(ls);
            } else if ((ls.getConnectB() != null) && (((TrackSegment) ls.getConnectB()).getLayoutBlock() == lBlock)) {
                result.add(ls);
            } else if ((ls.getConnectC() != null) && (((TrackSegment) ls.getConnectC()).getLayoutBlock() == lBlock)) {
                result.add(ls);
            } else if ((ls.getConnectD() != null) && (((TrackSegment) ls.getConnectD()).getLayoutBlock() == lBlock)) {
                result.add(ls);
            }
        }
        if (log.isTraceEnabled()) {
            StringBuilder txt = new StringBuilder("Turnouts for Block ");
            txt.append(block.getUserName()).append(" - ");
            for (int k = 0; k < result.size(); k++) {
                if (k > 0) {
                    txt.append(", ");
                }
                if ((result.get(k)).getTurnout() != null) {
                    txt.append((result.get(k)).getTurnout().getSystemName());
                } else {
                    txt.append("???");
                }
            }
            log.error("Turnouts for Block {}", txt.toString());
        }
        return result;
    }

    /**
     * Check if specified LayoutTurnout has required signals.
     *
     * @param t the LayoutTurnout to check
     * @return true if specified LayoutTurnout has required signal heads; false
     *         otherwise
     */
    public boolean layoutTurnoutHasRequiredSignals(@Nonnull LayoutTurnout t) {
        switch (t.getLinkType()) {
            case NO_LINK:
                if ((t.isTurnoutTypeTurnout())) {
                    return (!t.getSignalA1Name().isEmpty()
                            && !t.getSignalB1Name().isEmpty()
                            && !t.getSignalC1Name().isEmpty());
                } else if (t.isTurnoutTypeSlip()) {
                    if (!t.getSignalA1Name().isEmpty()
                            && !t.getSignalA2Name().isEmpty()
                            && !t.getSignalB1Name().isEmpty()
                            && !t.getSignalC1Name().isEmpty()
                            && !t.getSignalD1Name().isEmpty()
                            && !t.getSignalD2Name().isEmpty()) {
                        if (t.getTurnoutType() == LayoutTurnout.TurnoutType.SINGLE_SLIP) {
                            return true;
                        }
                        if (t.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_SLIP) {
                            if (!t.getSignalB2Name().isEmpty()
                                    && !t.getSignalC2Name().isEmpty()) {
                                return true;
                            }
                        }
                    }
                    return false;
                } else {
                    return !t.getSignalA1Name().isEmpty()
                            && !t.getSignalB1Name().isEmpty()
                            && !t.getSignalC1Name().isEmpty()
                            && !t.getSignalD1Name().isEmpty();
                }
            case FIRST_3_WAY:
                return (!t.getSignalA1Name().isEmpty()
                        && !t.getSignalC1Name().isEmpty());
            case SECOND_3_WAY:
            case THROAT_TO_THROAT:
                return (!t.getSignalB1Name().isEmpty()
                        && !t.getSignalC1Name().isEmpty());
            default:
                break;
        }
        return false;
    }

    /**
     * Get the SignalHead at the Anchor block boundary.
     *
     * @param p      the anchor with the signal head
     * @param block  the adjacent block
     * @param facing true if SignalHead facing towards block should be returned;
     *               false if SignalHead facing away from block should be
     *               returned
     * @return a SignalHead facing away from or towards block depending on value
     *         of facing; may be null
     */
    @CheckReturnValue
    @CheckForNull
    public SignalHead getSignalHeadAtAnchor(@CheckForNull PositionablePoint p,
            @CheckForNull Block block, boolean facing) {
        if ((p == null) || (block == null)) {
            log.error("null arguments in call to getSignalHeadAtAnchor");
            return null;
        }
        String userName = block.getUserName();
        LayoutBlock lBlock = null;
        if ((userName != null) && !userName.isEmpty()) {
            lBlock = layoutBlockManager.getByUserName(userName);
        }
        if (((p.getConnect1()).getLayoutBlock() == lBlock) && ((p.getConnect2()).getLayoutBlock() != lBlock)) {
            if ((LayoutEditorTools.isAtWestEndOfAnchor(layoutEditor, p.getConnect2(), p) && facing)
                    || ((!LayoutEditorTools.isAtWestEndOfAnchor(layoutEditor, p.getConnect2(), p)) && (!facing))) {
                return (InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(p.getWestBoundSignal()));
            } else {
                return (InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(p.getEastBoundSignal()));
            }
        } else if (((p.getConnect1()).getLayoutBlock() != lBlock) && ((p.getConnect2()).getLayoutBlock() == lBlock)) {
            if ((LayoutEditorTools.isAtWestEndOfAnchor(layoutEditor, p.getConnect1(), p) && facing)
                    || ((!LayoutEditorTools.isAtWestEndOfAnchor(layoutEditor, p.getConnect1(), p)) && (!facing))) {
                return (InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(p.getWestBoundSignal()));
            } else {
                return (InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(p.getEastBoundSignal()));
            }
        } else {
            // should never happen
            return null;
        }
    }

    /**
     * Get the SignalMast at the Anchor block boundary.
     *
     * @param p      the anchor with the signal head
     * @param block  the adjacent block
     * @param facing true if SignalMast facing towards block should be returned;
     *               false if SignalMast facing away from block should be
     *               returned
     * @return a SignalMast facing away from or towards block depending on value
     *         of facing; may be null
     */
    @CheckReturnValue
    @CheckForNull
    public SignalMast getSignalMastAtAnchor(@CheckForNull PositionablePoint p,
            @CheckForNull Block block, boolean facing) {
        if ((p == null) || (block == null)) {
            log.error("null arguments in call to getSignalHeadAtAnchor");
            return null;
        }
        String userName = block.getUserName();
        LayoutBlock lBlock = null;
        if ((userName != null) && !userName.isEmpty()) {
            lBlock = layoutBlockManager.getByUserName(userName);
        }
        if (((p.getConnect1()).getLayoutBlock() == lBlock) && ((p.getConnect2()).getLayoutBlock() != lBlock)) {
            if ((LayoutEditorTools.isAtWestEndOfAnchor(layoutEditor, p.getConnect2(), p) && facing)
                    || ((!LayoutEditorTools.isAtWestEndOfAnchor(layoutEditor, p.getConnect2(), p)) && (!facing))) {
                return (InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(p.getWestBoundSignalMastName()));
            } else {
                return (InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(p.getEastBoundSignalMastName()));
            }
        } else if (((p.getConnect1()).getLayoutBlock() != lBlock) && ((p.getConnect2()).getLayoutBlock() == lBlock)) {
            if ((LayoutEditorTools.isAtWestEndOfAnchor(layoutEditor, p.getConnect1(), p) && facing)
                    || ((!LayoutEditorTools.isAtWestEndOfAnchor(layoutEditor, p.getConnect1(), p)) && (!facing))) {
                return (InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(p.getWestBoundSignalMastName()));
            } else {
                return (InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(p.getEastBoundSignalMastName()));
            }
        } else {
            // should never happen
            return null;
        }
    }

    //Signalmasts are only valid or required on the boundary to a block.
    public boolean layoutTurnoutHasSignalMasts(@Nonnull LayoutTurnout t) {
        String[] turnoutBlocks = t.getBlockBoundaries();
        boolean valid = true;
        if (turnoutBlocks[0] != null && (t.getSignalAMastName().isEmpty())) {
            valid = false;
        }
        if (turnoutBlocks[1] != null && (t.getSignalBMastName().isEmpty())) {
            valid = false;
        }
        if (turnoutBlocks[2] != null && (t.getSignalCMastName().isEmpty())) {
            valid = false;
        }
        if (turnoutBlocks[3] != null && (t.getSignalDMastName().isEmpty())) {
            valid = false;
        }
        return valid;
    }

    /**
     * Get the SignalHead at the level crossing.
     *
     * @param x      the level crossing
     * @param block  the adjacent block
     * @param facing true if SignalHead facing towards block should be returned;
     *               false if SignalHead facing away from block should be
     *               returned
     * @return a SignalHead facing away from or towards block depending on value
     *         of facing; may be null
     */
    @CheckReturnValue
    @CheckForNull
    public SignalHead getSignalHeadAtLevelXing(@CheckForNull LevelXing x,
            @CheckForNull Block block, boolean facing) {
        if ((x == null) || (block == null)) {
            log.error("null arguments in call to getSignalHeadAtLevelXing");
            return null;
        }
        String userName = block.getUserName();
        LayoutBlock lBlock = null;
        if ((userName != null) && !userName.isEmpty()) {
            lBlock = layoutBlockManager.getByUserName(userName);
        }
        if ((x.getConnectA() == null) || (x.getConnectB() == null)
                || (x.getConnectC() == null) || (x.getConnectD() == null)) {
            log.error("Missing track around level crossing near Block {}", block.getUserName());
            return null;
        }
        if (((TrackSegment) x.getConnectA()).getLayoutBlock() == lBlock) {
            if (facing) {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(x.getSignalCName());
            } else {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(x.getSignalAName());
            }
        }
        if (((TrackSegment) x.getConnectB()).getLayoutBlock() == lBlock) {
            if (facing) {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(x.getSignalDName());
            } else {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(x.getSignalBName());
            }
        }
        if (((TrackSegment) x.getConnectC()).getLayoutBlock() == lBlock) {
            if (facing) {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(x.getSignalAName());
            } else {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(x.getSignalCName());
            }
        }
        if (((TrackSegment) x.getConnectD()).getLayoutBlock() == lBlock) {
            if (facing) {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(x.getSignalBName());
            } else {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(x.getSignalDName());
            }
        }
        return null;
    }

    /**
     * Check if block is internal to a level crossing.
     *
     * @param x     the level crossing to check
     * @param block the block to check
     * @return true if block is internal to x; false if block is external or
     *         contains a connecting track segment
     */
    public boolean blockInternalToLevelXing(
            @CheckForNull LevelXing x,
            @CheckForNull Block block) {
        if ((x == null) || (block == null)) {
            return false;
        }
        String userName = block.getUserName();
        LayoutBlock lBlock = null;
        if ((userName != null) && !userName.isEmpty()) {
            lBlock = layoutBlockManager.getByUserName(userName);
        }
        if (lBlock == null) {
            return false;
        }
        if ((x.getConnectA() == null) || (x.getConnectB() == null)
                || (x.getConnectC() == null) || (x.getConnectD() == null)) {
            return false;
        }
        if ((x.getLayoutBlockAC() != lBlock) && (x.getLayoutBlockBD() != lBlock)) {
            return false;
        }
        if (((TrackSegment) x.getConnectA()).getLayoutBlock() == lBlock) {
            return false;
        }
        if (((TrackSegment) x.getConnectB()).getLayoutBlock() == lBlock) {
            return false;
        }
        if (((TrackSegment) x.getConnectC()).getLayoutBlock() == lBlock) {
            return false;
        }
        return (((TrackSegment) x.getConnectD()).getLayoutBlock() != lBlock);
    }

    /**
     * Get the direction of the block boundary anchor point p. If
     * {@link EntryPoint#UNKNOWN} is returned, it indicates that p is entirely
     * internal or external to the Section.
     *
     * @param mForwardEntryPoints list of forward entry points
     * @param mReverseEntryPoints list of reverse entry points
     * @param p                   anchor point to match against one of the
     *                            points in the specified lists
     * @return the direction specified in the matching entry point or
     *         {@link EntryPoint#UNKNOWN}
     */
    public int getDirectionFromAnchor(
            @Nonnull List<EntryPoint> mForwardEntryPoints,
            @Nonnull List<EntryPoint> mReverseEntryPoints,
            @Nonnull PositionablePoint p) {
        Block block1 = p.getConnect1().getLayoutBlock().getBlock();
        Block block2 = p.getConnect2().getLayoutBlock().getBlock();
        for (EntryPoint ep : mForwardEntryPoints) {
            if (((ep.getBlock() == block1) && (ep.getFromBlock() == block2))
                    || ((ep.getBlock() == block2) && (ep.getFromBlock() == block1))) {
                return EntryPoint.FORWARD;
            }
        }
        for (EntryPoint ep : mReverseEntryPoints) {
            if (((ep.getBlock() == block1) && (ep.getFromBlock() == block2))
                    || ((ep.getBlock() == block2) && (ep.getFromBlock() == block1))) {
                return EntryPoint.REVERSE;
            }
        }
        return EntryPoint.UNKNOWN;
    }

    /**
     * Check if the AC track of a Level Crossing and its two connecting Track
     * Segments are internal to the specified block.
     * <p>
     * Note: if two connecting track segments are in the block, but the internal
     * connecting track is not, that is an error in the Layout Editor panel. If
     * found, an error message is generated and this method returns false.
     *
     * @param x     the level crossing to check
     * @param block the block to check
     * @return true if the A and C track segments of LevelXing x is in Block
     *         block; false otherwise
     */
    public boolean isInternalLevelXingAC(
            @Nonnull LevelXing x, @Nonnull Block block) {
        String userName = block.getUserName();
        LayoutBlock lBlock = null;
        if ((userName != null) && !userName.isEmpty()) {
            lBlock = layoutBlockManager.getByUserName(userName);
        }
        if ((((TrackSegment) x.getConnectA()).getLayoutBlock() == lBlock)
                && (((TrackSegment) x.getConnectC()).getLayoutBlock() == lBlock)) {
            if (x.getLayoutBlockAC() == lBlock) {
                return true;
            } else {
                log.error("Panel blocking error at AC of Level Crossing in Block {}", block.getUserName());
                return false;
            }
        }
        return false;
    }

    /**
     * Check if the BD track of a Level Crossing and its two connecting Track
     * Segments are internal to the specified block.
     * <p>
     * Note: if two connecting track segments are in the block, but the internal
     * connecting track is not, that is an error in the Layout Editor panel. If
     * found, an error message is generated and this method returns false.
     *
     * @param x     the level crossing to check
     * @param block the block to check
     * @return true if the B and D track segments of LevelXing x is in Block
     *         block; false otherwise
     */
    public boolean isInternalLevelXingBD(
            @Nonnull LevelXing x, @Nonnull Block block) {
        String userName = block.getUserName();
        LayoutBlock lBlock = null;
        if ((userName != null) && !userName.isEmpty()) {
            lBlock = layoutBlockManager.getByUserName(userName);
        }
        if ((((TrackSegment) x.getConnectB()).getLayoutBlock() == lBlock)
                && (((TrackSegment) x.getConnectD()).getLayoutBlock() == lBlock)) {
            if (x.getLayoutBlockBD() == lBlock) {
                return true;
            } else {
                log.error("Panel blocking error at BD of Level Crossing in Block {}", block.getDisplayName());
                return false;
            }
        }
        return false;
    }

    /*
    * Defines where to place sensor in a FACING mode SSL
     */
    public static final int OVERALL = 0x00;
    public static final int CONTINUING = 0x01;
    public static final int DIVERGING = 0x02;

    /**
     * Add the specified sensor ('name') to the SSL for the specified signal
     * head 'name' should be the system name for the sensor.
     * <p>
     * If the SSL has not been set up yet, the sensor is not added, an error
     * message is output and 'false' is returned.
     *
     * @param name  sensor name
     * @param sh    signal head
     * @param where should be one of DIVERGING if the sensor is being added to
     *              the diverging (second) part of a facing mode SSL, CONTINUING
     *              if the sensor is being added to the continuing (first) part
     *              of a facing mode SSL, OVERALL if the sensor is being added
     *              to the overall sensor list of a facing mode SSL. 'where' is
     *              ignored if not a facing mode SSL
     * @return 'true' if the sensor was already in the signal head SSL or if it
     *         has been added successfully; 'false' and logs an error if not.
     */
    public boolean addSensorToSignalHeadLogic(
            @CheckForNull String name,
            @CheckForNull SignalHead sh,
            int where) {
        if (sh == null) {
            log.error("Null signal head on entry to addSensorToSignalHeadLogic");
            return false;
        }
        if ((name == null) || name.isEmpty()) {
            log.error("Null string for sensor name on entry to addSensorToSignalHeadLogic");
            return false;
        }
        BlockBossLogic bbLogic = BlockBossLogic.getStoppedObject(sh.getSystemName());

        int mode = bbLogic.getMode();
        if (((mode == BlockBossLogic.SINGLEBLOCK) || (mode == BlockBossLogic.TRAILINGMAIN)
                || (mode == BlockBossLogic.TRAILINGDIVERGING)) || ((mode == BlockBossLogic.FACING)
                && (where == OVERALL))) {
            if (((bbLogic.getSensor1() != null) && (bbLogic.getSensor1()).equals(name))
                    || ((bbLogic.getSensor2() != null) && (bbLogic.getSensor2()).equals(name))
                    || ((bbLogic.getSensor3() != null) && (bbLogic.getSensor3()).equals(name))
                    || ((bbLogic.getSensor4() != null) && (bbLogic.getSensor4()).equals(name))
                    || ((bbLogic.getSensor5() != null) && (bbLogic.getSensor5()).equals(name))) {
                blockBossLogicProvider.register(bbLogic);
                bbLogic.start();
                return true;
            }
            if (bbLogic.getSensor1() == null) {
                bbLogic.setSensor1(name);
            } else if (bbLogic.getSensor2() == null) {
                bbLogic.setSensor2(name);
            } else if (bbLogic.getSensor3() == null) {
                bbLogic.setSensor3(name);
            } else if (bbLogic.getSensor4() == null) {
                bbLogic.setSensor4(name);
            } else if (bbLogic.getSensor5() == null) {
                bbLogic.setSensor5(name);
            } else {
                log.error("could not add sensor to SSL for signal head {} because there is no room in the SSL.", sh.getDisplayName());
                blockBossLogicProvider.register(bbLogic);
                bbLogic.start();
                return false;
            }
        } else if (mode == BlockBossLogic.FACING) {
            switch (where) {
                case DIVERGING:
                    if (((bbLogic.getWatchedSensor2() != null) && (bbLogic.getWatchedSensor2()).equals(name))
                            || ((bbLogic.getWatchedSensor2Alt() != null) && (bbLogic.getWatchedSensor2Alt()).equals(name))) {
                        blockBossLogicProvider.register(bbLogic);
                        bbLogic.start();
                        return true;
                    }
                    if (bbLogic.getWatchedSensor2() == null) {
                        bbLogic.setWatchedSensor2(name);
                    } else if (bbLogic.getWatchedSensor2Alt() == null) {
                        bbLogic.setWatchedSensor2Alt(name);
                    } else {
                        log.error("could not add watched sensor to SSL for signal head {} because there is no room in the facing SSL diverging part.", sh.getSystemName());
                        blockBossLogicProvider.register(bbLogic);
                        bbLogic.start();
                        return false;
                    }
                    break;
                case CONTINUING:
                    if (((bbLogic.getWatchedSensor1() != null) && (bbLogic.getWatchedSensor1()).equals(name))
                            || ((bbLogic.getWatchedSensor1Alt() != null) && (bbLogic.getWatchedSensor1Alt()).equals(name))) {
                        blockBossLogicProvider.register(bbLogic);
                        bbLogic.start();
                        return true;
                    }
                    if (bbLogic.getWatchedSensor1() == null) {
                        bbLogic.setWatchedSensor1(name);
                    } else if (bbLogic.getWatchedSensor1Alt() == null) {
                        bbLogic.setWatchedSensor1Alt(name);
                    } else {
                        log.error("could not add watched sensor to SSL for signal head {} because there is no room in the facing SSL continuing part.", sh.getSystemName());
                        blockBossLogicProvider.register(bbLogic);
                        bbLogic.start();
                        return false;
                    }
                    break;
                default:
                    log.error("could not add watched sensor to SSL for signal head {}because 'where' to place the sensor was not correctly designated.", sh.getSystemName());
                    blockBossLogicProvider.register(bbLogic);
                    bbLogic.start();
                    return false;
            }
        } else {
            log.error("SSL has not been set up for signal head {}. Could not add sensor - {}.", sh.getDisplayName(), name);
            return false;
        }
        blockBossLogicProvider.register(bbLogic);
        bbLogic.start();
        return true;
    }

    /**
     * Remove the specified sensors from the SSL for the specified signal head
     * if any of the sensors is currently in the SSL.
     *
     * @param names the names of the sensors to remove
     * @param sh    the signal head to remove the sensors from
     * @return true if successful; false otherwise
     */
    public boolean removeSensorsFromSignalHeadLogic(
            @CheckForNull List<String> names, @CheckForNull SignalHead sh) {
        if (sh == null) {
            log.error("Null signal head on entry to removeSensorsFromSignalHeadLogic");
            return false;
        }
        if (names == null) {
            log.error("Null List of sensor names on entry to removeSensorsFromSignalHeadLogic");
            return false;
        }
        BlockBossLogic bbLogic = BlockBossLogic.getStoppedObject(sh.getSystemName());

        for (String name : names) {
            if ((bbLogic.getSensor1() != null) && (bbLogic.getSensor1()).equals(name)) {
                bbLogic.setSensor1(null);
            }
            if ((bbLogic.getSensor2() != null) && (bbLogic.getSensor2()).equals(name)) {
                bbLogic.setSensor2(null);
            }
            if ((bbLogic.getSensor3() != null) && (bbLogic.getSensor3()).equals(name)) {
                bbLogic.setSensor3(null);
            }
            if ((bbLogic.getSensor4() != null) && (bbLogic.getSensor4()).equals(name)) {
                bbLogic.setSensor4(null);
            }
            if ((bbLogic.getSensor5() != null) && (bbLogic.getSensor5()).equals(name)) {
                bbLogic.setSensor5(null);
            }
            if (bbLogic.getMode() == BlockBossLogic.FACING) {
                if ((bbLogic.getWatchedSensor1() != null) && (bbLogic.getWatchedSensor1()).equals(name)) {
                    bbLogic.setWatchedSensor1(null);
                }
                if ((bbLogic.getWatchedSensor1Alt() != null) && (bbLogic.getWatchedSensor1Alt()).equals(name)) {
                    bbLogic.setWatchedSensor1Alt(null);
                }
                if ((bbLogic.getWatchedSensor2() != null) && (bbLogic.getWatchedSensor2()).equals(name)) {
                    bbLogic.setWatchedSensor2(null);
                }
                if ((bbLogic.getWatchedSensor2Alt() != null) && (bbLogic.getWatchedSensor2Alt()).equals(name)) {
                    bbLogic.setWatchedSensor2Alt(null);
                }
            }
        }
        if (bbLogic.getMode() == 0) {
            // this to avoid Unexpected mode ERROR message at startup
            bbLogic.setMode(BlockBossLogic.SINGLEBLOCK);
        }
        blockBossLogicProvider.register(bbLogic);
        bbLogic.start();
        return true;
    }

    /**
     * Get the next TrackNode following the specified TrackNode.
     *
     * @param currentNode     the current node
     * @param currentNodeType the possible path to follow (for example, if the
     *                        current node is a turnout entered at its throat,
     *                        the path could be the thrown or closed path)
     * @return the next TrackNode following currentNode for the given state or
     *         null if unable to follow the track
     */
    @CheckReturnValue
    @CheckForNull
    public TrackNode getNextNode(@CheckForNull TrackNode currentNode, int currentNodeType) {
        if (currentNode == null) {
            log.error("getNextNode called with a null Track Node");
            return null;
        }
        if (currentNode.reachedEndOfTrack()) {
            log.error("getNextNode - attempt to search past endBumper");
            return null;
        }
        return (getTrackNode(currentNode.getNode(), currentNode.getNodeType(), currentNode.getTrackSegment(), currentNodeType));
    }

    /**
     * Get the next TrackNode following the specified TrackNode, assuming that
     * TrackNode was reached via the specified TrackSegment.
     * <p>
     * If the specified track node can lead to different paths to the next node,
     * for example, if the specified track node is a turnout entered at its
     * throat, then "currentNodeType" must be specified to choose between the
     * possible paths. If currentNodeType = 0, the search will follow the
     * 'continuing' track; if currentNodeType = 1, the search will follow the
     * 'diverging' track; if currentNodeType = 2 (3-way turnouts only), the
     * search will follow the second 'diverging' track.
     * <p>
     * In determining which track is the 'continuing' track for RH, LH, and WYE
     * turnouts, this search routine uses the layout turnout's
     * 'continuingState'.
     * <p>
     * When following track, this method skips over anchor points that are not
     * block boundaries.
     * <p>
     * When following track, this method treats a modeled 3-way turnout as a
     * single turnout. It also treats two THROAT_TO_THROAT turnouts as a single
     * turnout, but with each turnout having a continuing sense.
     *
     * @param currentNode         the current node
     * @param currentNodeType     the type of node
     * @param currentTrackSegment the followed track segment
     * @param currentNodeState    the possible path to follow (for example, if
     *                            the current node is a turnout entered at its
     *                            throat, the path could be the thrown or closed
     *                            path)
     * @return the next TrackNode following currentNode for the given state if a
     *         node or end_of-track is reached or null if unable to follow the
     *         track
     */
    //TODO: cTrack parameter isn't used in this method; is this a bug?
    //TODO: prevTrackType local variable is set but never used; dead-code strip?
    @CheckReturnValue
    @CheckForNull
    public TrackNode getTrackNode(
            @Nonnull LayoutTrack currentNode,
            HitPointType currentNodeType,
            @CheckForNull TrackSegment currentTrackSegment,
            int currentNodeState) {
        // initialize
        //LayoutEditor.HitPointType prevTrackType = currentNodeType;
        LayoutTrack prevTrack = currentNode;
        TrackSegment nextTrackSegment = currentTrackSegment;
        switch (currentNodeType) {
            case POS_POINT:
                if (currentNode instanceof PositionablePoint) {
                    PositionablePoint p = (PositionablePoint) currentNode;
                    if (p.getType() == PositionablePoint.PointType.END_BUMPER) {
                        log.warn("Attempt to search beyond end of track");
                        return null;
                    }
                    nextTrackSegment = p.getConnect1();
                    if (nextTrackSegment == null) {
                        nextTrackSegment = p.getConnect2();
                    }
                } else {
                    log.warn("currentNodeType wrong for currentNode");
                }
                break;
            case TURNOUT_A: {
                if (currentNode instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) currentNode;
                    if (lt.isTurnoutTypeTurnout()) {
                        if ((lt.getLinkedTurnoutName() == null)
                                || (lt.getLinkedTurnoutName().isEmpty())) {
                            // Standard turnout - node type A
                            if (lt.getContinuingSense() == Turnout.CLOSED) {
                                switch (currentNodeState) {
                                    case TRACKNODE_CONTINUING:
                                        nextTrackSegment = (TrackSegment) lt.getConnectB();
                                        //prevTrackType = LayoutEditor.HitPointType.TURNOUT_B;
                                        break;
                                    case TRACKNODE_DIVERGING:
                                        nextTrackSegment = (TrackSegment) lt.getConnectC();
                                        //prevTrackType = LayoutEditor.HitPointType.TURNOUT_C;
                                        break;
                                    default:
                                        log.error("Bad currentNodeState when searching track-std. normal");
                                        return null;
                                }
                            } else {
                                switch (currentNodeState) {
                                    case TRACKNODE_CONTINUING:
                                        nextTrackSegment = (TrackSegment) lt.getConnectC();
                                        //prevTrackType = LayoutEditor.HitPointType.TURNOUT_C;
                                        break;
                                    case TRACKNODE_DIVERGING:
                                        nextTrackSegment = (TrackSegment) lt.getConnectB();
                                        //prevTrackType = LayoutEditor.HitPointType.TURNOUT_B;
                                        break;
                                    default:
                                        log.error("Bad currentNodeType argument when searching track-std reversed");
                                        return null;
                                }
                            }
                        } else {
                            // linked turnout - node type A
                            LayoutTurnout lto = layoutEditor.getFinder().findLayoutTurnoutByName(lt.getLinkedTurnoutName());
                            if (lt.getLinkType() == LayoutTurnout.LinkType.THROAT_TO_THROAT) {
                                switch (currentNodeState) {
                                    case TRACKNODE_CONTINUING:
                                        if (lto.getContinuingSense() == Turnout.CLOSED) {
                                            nextTrackSegment = (TrackSegment) lto.getConnectB();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_B;
                                        } else {
                                            nextTrackSegment = (TrackSegment) lto.getConnectC();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_C;
                                        }
                                        break;
                                    case TRACKNODE_DIVERGING:
                                        if (lto.getContinuingSense() == Turnout.CLOSED) {
                                            nextTrackSegment = (TrackSegment) lto.getConnectC();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_C;
                                        } else {
                                            nextTrackSegment = (TrackSegment) lto.getConnectB();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_B;
                                        }
                                        break;
                                    default:
                                        log.error("Bad currentNodeType argument when searching track - THROAT_TO_THROAT");
                                        return null;
                                }
                                prevTrack = lto;
                            } else if (lt.getLinkType() == LayoutTurnout.LinkType.FIRST_3_WAY) {
                                switch (currentNodeState) {
                                    case TRACKNODE_CONTINUING:
                                        if (lto.getContinuingSense() == Turnout.CLOSED) {
                                            nextTrackSegment = (TrackSegment) lto.getConnectB();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_B;
                                        } else {
                                            nextTrackSegment = (TrackSegment) lto.getConnectC();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_C;
                                        }
                                        prevTrack = lto;
                                        break;
                                    case TRACKNODE_DIVERGING:
                                        if (lt.getContinuingSense() == Turnout.CLOSED) {
                                            nextTrackSegment = (TrackSegment) lt.getConnectC();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_C;
                                        } else {
                                            nextTrackSegment = (TrackSegment) lt.getConnectB();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_B;
                                        }
                                        break;
                                    case TRACKNODE_DIVERGING_2ND_3WAY:
                                        if (lto.getContinuingSense() == Turnout.CLOSED) {
                                            nextTrackSegment = (TrackSegment) lto.getConnectC();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_C;
                                        } else {
                                            nextTrackSegment = (TrackSegment) lto.getConnectB();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_B;
                                        }
                                        prevTrack = lto;
                                        break;
                                    default:
                                        log.error("Bad currentNodeType argument when searching track - FIRST_3_WAY");
                                        return null;
                                }
                            }
                        }
                    } else if (lt.isTurnoutTypeXover()) {
                        // crossover turnout - node type A
                        switch (currentNodeState) {
                            case TRACKNODE_CONTINUING:
                                nextTrackSegment = (TrackSegment) lt.getConnectB();
                                //prevTrackType = LayoutEditor.HitPointType.TURNOUT_B;
                                break;
                            case TRACKNODE_DIVERGING:
                                if ((currentNodeType == HitPointType.TURNOUT_A)
                                        && (!(lt.getTurnoutType() == LayoutTurnout.TurnoutType.LH_XOVER))) {
                                    nextTrackSegment = (TrackSegment) lt.getConnectC();
                                    //prevTrackType = LayoutEditor.HitPointType.TURNOUT_C;
                                } else {
                                    log.error("Request to follow not allowed switch setting at LH_XOVER or RH_OVER");
                                    return null;
                                }
                                break;
                            default:
                                log.error("Bad currentNodeType argument when searching track- XOVER A");
                                return null;
                        }
                    }
                } else {
                    log.error("currentNodeType wrong for currentNode");
                }
                break;
            }
            case TURNOUT_B:
            case TURNOUT_C: {
                if (currentNode instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) currentNode;
                    if (lt.isTurnoutTypeTurnout()) {
                        if ((lt.getLinkedTurnoutName() == null)
                                || (lt.getLinkedTurnoutName().isEmpty())
                                || (lt.getLinkType() == LayoutTurnout.LinkType.FIRST_3_WAY)) {
                            nextTrackSegment = (TrackSegment) (lt.getConnectA());
                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_A;
                        } else {
                            LayoutTurnout lto = layoutEditor.getFinder().findLayoutTurnoutByName(lt.getLinkedTurnoutName());
                            if (lt.getLinkType() == LayoutTurnout.LinkType.SECOND_3_WAY) {
                                nextTrackSegment = (TrackSegment) (lto.getConnectA());
                                //prevTrackType = LayoutEditor.HitPointType.TURNOUT_A;
                            } else if (lt.getLinkType() == LayoutTurnout.LinkType.THROAT_TO_THROAT) {
                                switch (currentNodeState) {
                                    case TRACKNODE_CONTINUING:
                                        if (lto.getContinuingSense() == Turnout.CLOSED) {
                                            nextTrackSegment = (TrackSegment) lto.getConnectB();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_B;
                                        } else {
                                            nextTrackSegment = (TrackSegment) lto.getConnectC();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_C;
                                        }
                                        break;
                                    case TRACKNODE_DIVERGING:
                                        if (lto.getContinuingSense() == Turnout.CLOSED) {
                                            nextTrackSegment = (TrackSegment) lto.getConnectC();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_C;
                                        } else {
                                            nextTrackSegment = (TrackSegment) lto.getConnectB();
                                            //prevTrackType = LayoutEditor.HitPointType.TURNOUT_B;
                                        }
                                        break;
                                    default:
                                        log.error("Bad currentNodeType argument when searching track - THROAT_TO_THROAT - 2");
                                        return null;
                                }
                            }
                            prevTrack = lto;
                        }
                    } else if (lt.isTurnoutTypeXover()) {
                        switch (currentNodeState) {
                            case TRACKNODE_CONTINUING:
                                if (currentNodeType == HitPointType.TURNOUT_B) {
                                    nextTrackSegment = (TrackSegment) lt.getConnectA();
                                    //prevTrackType = LayoutEditor.HitPointType.TURNOUT_A;
                                } else if (currentNodeType == HitPointType.TURNOUT_C) {
                                    nextTrackSegment = (TrackSegment) lt.getConnectD();
                                    //prevTrackType = LayoutEditor.HitPointType.TURNOUT_D;
                                }
                                break;
                            case TRACKNODE_DIVERGING:
                                if ((currentNodeType == HitPointType.TURNOUT_C)
                                        && (!(lt.getTurnoutType() == LayoutTurnout.TurnoutType.LH_XOVER))) {
                                    nextTrackSegment = (TrackSegment) lt.getConnectA();
                                    //prevTrackType = LayoutEditor.HitPointType.TURNOUT_A;
                                } else if ((currentNodeType == HitPointType.TURNOUT_B)
                                        && (!(lt.getTurnoutType() == LayoutTurnout.TurnoutType.RH_XOVER))) {
                                    nextTrackSegment = (TrackSegment) lt.getConnectD();
                                    //prevTrackType = LayoutEditor.HitPointType.TURNOUT_D;
                                } else {
                                    log.error("Request to follow not allowed switch setting at LH_XOVER or RH_OVER");
                                    return null;
                                }
                                break;
                            default:
                                log.error("Bad currentNodeType argument when searching track - XOVER B or C");
                                return null;
                        }
                    }
                } else {
                    log.error("currentNodeType wrong for currentNode");
                }
                break;
            }
            case TURNOUT_D: {
                if (currentNode instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) currentNode;
                    if (lt.isTurnoutTypeXover()) {
                        switch (currentNodeState) {
                            case TRACKNODE_CONTINUING:
                                nextTrackSegment = (TrackSegment) lt.getConnectC();
                                //prevTrackType = LayoutEditor.HitPointType.TURNOUT_C;
                                break;
                            case TRACKNODE_DIVERGING:
                                if (!(lt.getTurnoutType() == LayoutTurnout.TurnoutType.RH_XOVER)) {
                                    nextTrackSegment = (TrackSegment) lt.getConnectB();
                                    //prevTrackType = LayoutEditor.HitPointType.TURNOUT_B;
                                } else {
                                    log.error("Request to follow not allowed switch setting at LH_XOVER or RH_OVER");
                                    return null;
                                }
                                break;
                            default:
                                log.error("Bad currentNodeType argument when searching track - XOVER D");
                                return null;
                        }
                    } else {
                        log.error("Bad traak node type - TURNOUT_D, but not a crossover turnout");
                        return null;
                    }
                } else {
                    log.error("currentNodeType wrong for currentNode");
                }
                break;
            }
            case LEVEL_XING_A:
                if (currentNode instanceof LevelXing) {
                    nextTrackSegment = (TrackSegment) ((LevelXing) currentNode).getConnectC();
                    //prevTrackType = LayoutEditor.HitPointType.LEVEL_XING_C;
                } else {
                    log.error("currentNodeType wrong for currentNode");
                }
                break;
            case LEVEL_XING_B:
                if (currentNode instanceof LevelXing) {
                    nextTrackSegment = (TrackSegment) ((LevelXing) currentNode).getConnectD();
                    //prevTrackType = LayoutEditor.HitPointType.LEVEL_XING_D;
                } else {
                    log.error("currentNodeType wrong for currentNode");
                }
                break;
            case LEVEL_XING_C:
                if (currentNode instanceof LevelXing) {
                    nextTrackSegment = (TrackSegment) ((LevelXing) currentNode).getConnectA();
                    //prevTrackType = LayoutEditor.HitPointType.LEVEL_XING_A;
                } else {
                    log.error("currentNodeType wrong for currentNode");
                }
                break;
            case LEVEL_XING_D:
                if (currentNode instanceof LevelXing) {
                    nextTrackSegment = (TrackSegment) ((LevelXing) currentNode).getConnectB();
                    //prevTrackType = LayoutEditor.HitPointType.LEVEL_XING_B;
                } else {
                    log.error("currentNodeType wrong for currentNode");
                }
                break;
            case SLIP_A: {
                if (currentNode instanceof LayoutSlip) {
                    LayoutSlip ls = (LayoutSlip) currentNode;
                    if (currentNodeState == TRACKNODE_CONTINUING) {
                        nextTrackSegment = (TrackSegment) ls.getConnectC();
                        //prevTrackType = LayoutEditor.HitPointType.SLIP_C;
                    } else if (currentNodeState == TRACKNODE_DIVERGING) {
                        nextTrackSegment = (TrackSegment) ls.getConnectD();
                        //prevTrackType = LayoutEditor.HitPointType.SLIP_D;
                    }
                } else {
                    log.error("currentNodeType wrong for currentNode");
                }
                break;
            }
            case SLIP_B: {
                if (currentNode instanceof LayoutSlip) {
                    LayoutSlip ls = (LayoutSlip) currentNode;
                    if (currentNodeState == TRACKNODE_CONTINUING) {
                        nextTrackSegment = (TrackSegment) ls.getConnectD();
                        //prevTrackType = LayoutEditor.HitPointType.SLIP_D;
                    } else if ((currentNodeState == TRACKNODE_DIVERGING)
                            && (ls.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_SLIP)) {
                        nextTrackSegment = (TrackSegment) ls.getConnectC();
                        //prevTrackType = LayoutEditor.HitPointType.SLIP_C;
                    } else {
                        log.error("Request to follow not allowed on a single slip");
                        return null;
                    }
                } else {
                    log.error("currentNodeType wrong for currentNode");
                }
                break;
            }
            case SLIP_C: {
                if (currentNode instanceof LayoutSlip) {
                    LayoutSlip ls = (LayoutSlip) currentNode;
                    if (currentNodeState == TRACKNODE_CONTINUING) {
                        nextTrackSegment = (TrackSegment) ls.getConnectA();
                        //prevTrackType = LayoutEditor.HitPointType.SLIP_A;
                    } else if ((currentNodeState == TRACKNODE_DIVERGING)
                            && (ls.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_SLIP)) {
                        nextTrackSegment = (TrackSegment) ls.getConnectB();
                        //prevTrackType = LayoutEditor.HitPointType.SLIP_B;
                    } else {
                        log.error("Request to follow not allowed on a single slip");
                        return null;
                    }
                } else {
                    log.error("currentNodeType wrong for currentNode");
                }
                break;
            }
            case SLIP_D: {
                if (currentNode instanceof LayoutSlip) {
                    LayoutSlip ls = (LayoutSlip) currentNode;
                    if (currentNodeState == TRACKNODE_CONTINUING) {
                        nextTrackSegment = (TrackSegment) ls.getConnectB();
                        //prevTrackType = LayoutEditor.HitPointType.SLIP_B;
                    } else if (currentNodeState == TRACKNODE_DIVERGING) {
                        nextTrackSegment = (TrackSegment) ls.getConnectA();
                        //prevTrackType = LayoutEditor.HitPointType.SLIP_A;
                    }
                } else {
                    log.error("currentNodeType wrong for currentNode");
                }
                break;
            }
            default:
                log.error("Unable to initiate 'getTrackNode'.  Probably bad input Track Node.");
                return null;
        }

        if (nextTrackSegment == null) {
            log.error("Error nextTrackSegment is null!");
            return null;
        }

        // follow track to next node (anchor block boundary, turnout, or level crossing)
        LayoutTrack node = null;
        HitPointType nodeType = HitPointType.NONE;
        TrackSegment nodeTrackSegment = null;

        boolean hitEnd = false;
        boolean hasNode = false;
        while (!hasNode) {
            LayoutTrack nextLayoutTrack = null;
            HitPointType nextType = HitPointType.NONE;

            if (nextTrackSegment.getConnect1() == prevTrack) {
                nextLayoutTrack = nextTrackSegment.getConnect2();
                nextType = nextTrackSegment.getType2();
            } else if (nextTrackSegment.getConnect2() == prevTrack) {
                nextLayoutTrack = nextTrackSegment.getConnect1();
                nextType = nextTrackSegment.getType1();
            }
            if (nextLayoutTrack == null) {
                log.error("Error while following track {} looking for next node", nextTrackSegment.getName());
                return null;
            }

            if (nextType == HitPointType.POS_POINT) {
                PositionablePoint p = (PositionablePoint) nextLayoutTrack;
                if (p.getType() == PositionablePoint.PointType.END_BUMPER) {
                    hitEnd = true;
                    hasNode = true;
                } else {
                    TrackSegment con1 = p.getConnect1();
                    TrackSegment con2 = p.getConnect2();
                    if ((con1 == null) || (con2 == null)) {
                        log.error("Breakin connectivity at Anchor Point when searching for track node");
                        return null;
                    }
                    if (con1.getLayoutBlock() == con2.getLayoutBlock()) {
                        if (con1 == nextTrackSegment) {
                            nextTrackSegment = con2;
                        } else if (con2 == nextTrackSegment) {
                            nextTrackSegment = con1;
                        } else {
                            log.error("Breakin connectivity at Anchor Point when searching for track node");
                            return null;
                        }
                        prevTrack = nextLayoutTrack;
                    } else {
                        node = nextLayoutTrack;
                        nodeType = nextType;
                        nodeTrackSegment = nextTrackSegment;
                        hasNode = true;
                    }
                }
            } else {
                node = nextLayoutTrack;
                nodeType = nextType;
                nodeTrackSegment = nextTrackSegment;
                hasNode = true;
            }
        }
        return (new TrackNode(node, nodeType, nodeTrackSegment, hitEnd, currentNodeState));
    }

    /**
     * Get an "exit block" for the specified track node if there is one, else
     * returns null. An "exit block" must be different from the block of the
     * track segment in the node. If the node is a PositionablePoint, it is
     * assumed to be a block boundary anchor point.
     *
     * @param node          the node to get the exit block for
     * @param excludedBlock blocks not to be considered as exit blocks
     * @return the exit block for node or null if none exists
     */
    @CheckReturnValue
    @CheckForNull
    public Block getExitBlockForTrackNode(
            @CheckForNull TrackNode node,
            @CheckForNull Block excludedBlock) {
        if ((node == null) || node.reachedEndOfTrack()) {
            return null;
        }
        Block block = null;
        switch (node.getNodeType()) {
            case POS_POINT:
                PositionablePoint p = (PositionablePoint) node.getNode();
                block = p.getConnect1().getLayoutBlock().getBlock();
                if (block == node.getTrackSegment().getLayoutBlock().getBlock()) {
                    block = p.getConnect2().getLayoutBlock().getBlock();
                }
                break;
            case TURNOUT_A:
                LayoutTurnout lt = (LayoutTurnout) node.getNode();
                Block tBlock = ((TrackSegment) lt.getConnectB()).getLayoutBlock().getBlock();
                if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                        && (tBlock != excludedBlock)) {
                    block = tBlock;
                } else if (lt.getTurnoutType() != LayoutTurnout.TurnoutType.LH_XOVER) {
                    tBlock = ((TrackSegment) lt.getConnectC()).getLayoutBlock().getBlock();
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    }
                }
                break;
            case TURNOUT_B:
                lt = (LayoutTurnout) node.getNode();
                tBlock = ((TrackSegment) lt.getConnectA()).getLayoutBlock().getBlock();
                if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                        && (tBlock != excludedBlock)) {
                    block = tBlock;
                } else if ((lt.getTurnoutType() == LayoutTurnout.TurnoutType.LH_XOVER)
                        || (lt.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER)) {
                    tBlock = ((TrackSegment) lt.getConnectD()).getLayoutBlock().getBlock();
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    }
                }
                break;
            case TURNOUT_C:
                lt = (LayoutTurnout) node.getNode();
                if (lt.getTurnoutType() != LayoutTurnout.TurnoutType.LH_XOVER) {
                    tBlock = ((TrackSegment) lt.getConnectA()).getLayoutBlock().getBlock();
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    }
                }
                if ((block == null) && ((lt.getTurnoutType() == LayoutTurnout.TurnoutType.LH_XOVER)
                        || (lt.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER))) {
                    tBlock = ((TrackSegment) lt.getConnectD()).getLayoutBlock().getBlock();
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    }
                }
                break;
            case TURNOUT_D:
                lt = (LayoutTurnout) node.getNode();
                if ((lt.getTurnoutType() == LayoutTurnout.TurnoutType.LH_XOVER)
                        || (lt.getTurnoutType() == LayoutTurnout.TurnoutType.DOUBLE_XOVER)) {
                    tBlock = ((TrackSegment) lt.getConnectB()).getLayoutBlock().getBlock();
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    }
                }
                break;
            case LEVEL_XING_A:
                LevelXing x = (LevelXing) node.getNode();
                tBlock = ((TrackSegment) x.getConnectC()).getLayoutBlock().getBlock();
                if (tBlock != node.getTrackSegment().getLayoutBlock().getBlock()) {
                    block = tBlock;
                }
                break;
            case LEVEL_XING_B:
                x = (LevelXing) node.getNode();
                tBlock = ((TrackSegment) x.getConnectD()).getLayoutBlock().getBlock();
                if (tBlock != node.getTrackSegment().getLayoutBlock().getBlock()) {
                    block = tBlock;
                }
                break;
            case LEVEL_XING_C:
                x = (LevelXing) node.getNode();
                tBlock = ((TrackSegment) x.getConnectA()).getLayoutBlock().getBlock();
                if (tBlock != node.getTrackSegment().getLayoutBlock().getBlock()) {
                    block = tBlock;
                }
                break;
            case LEVEL_XING_D:
                x = (LevelXing) node.getNode();
                tBlock = ((TrackSegment) x.getConnectB()).getLayoutBlock().getBlock();
                if (tBlock != node.getTrackSegment().getLayoutBlock().getBlock()) {
                    block = tBlock;
                }
                break;
            case SLIP_A:
                LayoutSlip ls = (LayoutSlip) node.getNode();
                tBlock = ((TrackSegment) ls.getConnectC()).getLayoutBlock().getBlock();
                if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                        && (tBlock != excludedBlock)) {
                    block = tBlock;
                } else {
                    tBlock = ((TrackSegment) ls.getConnectD()).getLayoutBlock().getBlock();
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    }
                }
                break;
            case SLIP_B:
                ls = (LayoutSlip) node.getNode();
                tBlock = ((TrackSegment) ls.getConnectD()).getLayoutBlock().getBlock();
                if (ls.getTurnoutType() == LayoutSlip.TurnoutType.DOUBLE_SLIP) {
                    //Double slip
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    } else {
                        tBlock = ((TrackSegment) ls.getConnectC()).getLayoutBlock().getBlock();
                        if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                                && (tBlock != excludedBlock)) {
                            block = tBlock;
                        }
                    }
                } else {
                    if (tBlock != node.getTrackSegment().getLayoutBlock().getBlock()) {
                        block = tBlock;
                    }
                }
                break;
            case SLIP_C:
                ls = (LayoutSlip) node.getNode();
                tBlock = ((TrackSegment) ls.getConnectA()).getLayoutBlock().getBlock();
                if (ls.getTurnoutType() == LayoutSlip.TurnoutType.DOUBLE_SLIP) {
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    } else {
                        tBlock = ((TrackSegment) ls.getConnectB()).getLayoutBlock().getBlock();
                        if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                                && (tBlock != excludedBlock)) {
                            block = tBlock;
                        }
                    }
                } else {
                    if (tBlock != node.getTrackSegment().getLayoutBlock().getBlock()) {
                        block = tBlock;
                    }
                }
                break;
            case SLIP_D:
                ls = (LayoutSlip) node.getNode();
                tBlock = ((TrackSegment) ls.getConnectB()).getLayoutBlock().getBlock();
                if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                        && (tBlock != excludedBlock)) {
                    block = tBlock;
                } else {
                    tBlock = ((TrackSegment) ls.getConnectA()).getLayoutBlock().getBlock();
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    }
                }
                break;
            default:
                break;
        }
        return block;
    }

    // support methods
    /**
     * Initialize the setting (as an object), sets the new track segment (if in
     * Block), and sets the prevConnectType.
     */
    private Integer getTurnoutSetting(
            @Nonnull LayoutTurnout layoutTurnout, HitPointType cType, boolean suppress) {
        prevConnectTrack = layoutTurnout;
        int setting = Turnout.THROWN;
        LayoutTurnout.TurnoutType tType = layoutTurnout.getTurnoutType();
        if (layoutTurnout instanceof LayoutSlip) {
            setting = LayoutSlip.UNKNOWN;
            LayoutSlip layoutSlip = (LayoutSlip) layoutTurnout;
            tType = layoutSlip.getTurnoutType();
            LayoutBlock layoutBlockA = ((TrackSegment) layoutSlip.getConnectA()).getLayoutBlock();
            LayoutBlock layoutBlockB = ((TrackSegment) layoutSlip.getConnectB()).getLayoutBlock();
            LayoutBlock layoutBlockC = ((TrackSegment) layoutSlip.getConnectC()).getLayoutBlock();
            LayoutBlock layoutBlockD = ((TrackSegment) layoutSlip.getConnectD()).getLayoutBlock();
            switch (cType) {
                case SLIP_A:
                    if (nextLayoutBlock == layoutBlockC) {
                        // exiting block at C
                        prevConnectType = HitPointType.SLIP_C;
                        setting = LayoutSlip.STATE_AC;
                        trackSegment = (TrackSegment) layoutSlip.getConnectC();
                    } else if (nextLayoutBlock == layoutBlockD) {
                        // exiting block at D
                        prevConnectType = HitPointType.SLIP_D;
                        setting = LayoutSlip.STATE_AD;
                        trackSegment = (TrackSegment) layoutSlip.getConnectD();
                    } else if (currLayoutBlock == layoutBlockC
                            && currLayoutBlock != layoutBlockD) {
                        // block continues at C only
                        trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                        setting = LayoutSlip.STATE_AC;
                        prevConnectType = HitPointType.SLIP_C;

                    } else if (currLayoutBlock == layoutBlockD
                            && currLayoutBlock != layoutBlockC) {
                        // block continues at D only
                        setting = LayoutSlip.STATE_AD;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        prevConnectType = HitPointType.SLIP_D;
                    } else { // both connecting track segments continue in current block, must search further
                        if ((layoutSlip.getConnectC() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectC(), layoutSlip)) {
                            prevConnectType = HitPointType.SLIP_C;
                            setting = LayoutSlip.STATE_AC;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                        } else if ((layoutSlip.getConnectD() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectD(), layoutSlip)) {
                            prevConnectType = HitPointType.SLIP_D;
                            setting = LayoutSlip.STATE_AD;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        } else {
                            if (!suppress) {
                                log.warn("Neither branch at {} leads to next Block {}", layoutTurnout, nextLayoutBlock);
                            }
                            trackSegment = null;
                        }
                    }
                    break;
                case SLIP_B:
                    if (nextLayoutBlock == layoutBlockD) {
                        // exiting block at D
                        prevConnectType = HitPointType.SLIP_D;
                        setting = LayoutSlip.STATE_BD;
                        trackSegment = (TrackSegment) layoutSlip.getConnectD();
                    } else if (nextLayoutBlock == layoutBlockC
                            && tType == LayoutSlip.TurnoutType.DOUBLE_SLIP) {
                        // exiting block at C
                        prevConnectType = HitPointType.SLIP_C;
                        setting = LayoutSlip.STATE_BC;
                        trackSegment = (TrackSegment) layoutSlip.getConnectC();
                    } else {
                        if (tType == LayoutSlip.TurnoutType.DOUBLE_SLIP) {
                            if (currLayoutBlock == layoutBlockD
                                    && currLayoutBlock != layoutBlockC) {
                                //Found continuing at D only
                                trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                                setting = LayoutSlip.STATE_BD;
                                prevConnectType = HitPointType.SLIP_D;

                            } else if (currLayoutBlock == layoutBlockC
                                    && currLayoutBlock != layoutBlockD) {
                                //Found continuing at C only
                                trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                                setting = LayoutSlip.STATE_BC;
                                prevConnectType = HitPointType.SLIP_C;
                            } else { // both connecting track segments continue in current block, must search further
                                if ((layoutSlip.getConnectD() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectD(), layoutSlip)) {
                                    prevConnectType = HitPointType.SLIP_D;
                                    setting = LayoutSlip.STATE_BD;
                                    trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                                } else if ((layoutSlip.getConnectC() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectC(), layoutSlip)) {
                                    prevConnectType = HitPointType.SLIP_C;
                                    setting = LayoutSlip.STATE_BC;
                                    trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                                } else {
                                    if (!suppress) {
                                        log.warn("Neither branch at {} leads to next Block {}", layoutTurnout, nextLayoutBlock);
                                    }
                                    trackSegment = null;
                                }
                            }
                        } else {
                            if (currLayoutBlock == layoutBlockD) {
                                //Found continuing at D only
                                trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                                setting = LayoutSlip.STATE_BD;
                                prevConnectType = HitPointType.SLIP_D;
                            } else {
                                trackSegment = null;
                            }
                        }
                    }
                    break;
                case SLIP_C:
                    if (nextLayoutBlock == layoutBlockA) {
                        // exiting block at A
                        prevConnectType = HitPointType.SLIP_A;
                        setting = LayoutSlip.STATE_AC;
                        trackSegment = (TrackSegment) layoutSlip.getConnectA();
                    } else if (nextLayoutBlock == layoutBlockB
                            && tType == LayoutSlip.TurnoutType.DOUBLE_SLIP) {
                        // exiting block at B
                        prevConnectType = HitPointType.SLIP_B;
                        setting = LayoutSlip.STATE_BC;
                        trackSegment = (TrackSegment) layoutSlip.getConnectB();
                    } else {
                        if (tType == LayoutSlip.TurnoutType.DOUBLE_SLIP) {
                            if (currLayoutBlock == layoutBlockA
                                    && currLayoutBlock != layoutBlockB) {
                                //Found continuing at A only
                                trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                                setting = LayoutSlip.STATE_AC;
                                prevConnectType = HitPointType.SLIP_A;
                            } else if (currLayoutBlock == layoutBlockB
                                    && currLayoutBlock != layoutBlockA) {
                                //Found continuing at B only
                                trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                                setting = LayoutSlip.STATE_BC;
                                prevConnectType = HitPointType.SLIP_B;
                            } else { // both connecting track segments continue in current block, must search further
                                if ((layoutSlip.getConnectA() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectA(), layoutSlip)) {
                                    prevConnectType = HitPointType.SLIP_A;
                                    setting = LayoutSlip.STATE_AC;
                                    trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                                } else if ((layoutSlip.getConnectB() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectB(), layoutSlip)) {
                                    prevConnectType = HitPointType.SLIP_B;
                                    setting = LayoutSlip.STATE_BC;
                                    trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                                } else {
                                    if (!suppress) {
                                        log.warn("Neither branch at {} leads to next Block {}", layoutTurnout, nextLayoutBlock);
                                    }
                                    trackSegment = null;
                                }
                            }
                        } else {
                            if (currLayoutBlock == layoutBlockA) {
                                //Found continuing at A only
                                trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                                setting = LayoutSlip.STATE_AC;
                                prevConnectType = HitPointType.SLIP_A;
                            } else {
                                trackSegment = null;
                            }
                        }
                    }
                    break;
                case SLIP_D:
                    if (nextLayoutBlock == layoutBlockB) {
                        // exiting block at B
                        prevConnectType = HitPointType.SLIP_B;
                        setting = LayoutSlip.STATE_BD;
                        trackSegment = (TrackSegment) layoutSlip.getConnectB();
                    } else if (nextLayoutBlock == layoutBlockA) {
                        // exiting block at B
                        prevConnectType = HitPointType.SLIP_A;
                        setting = LayoutSlip.STATE_AD;
                        trackSegment = (TrackSegment) layoutSlip.getConnectA();
                    } else if (currLayoutBlock == layoutBlockB
                            && currLayoutBlock != layoutBlockA) {
                        //Found continuing at B only
                        trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                        setting = LayoutSlip.STATE_BD;
                        prevConnectType = HitPointType.SLIP_B;

                    } else if (currLayoutBlock == layoutBlockA
                            && currLayoutBlock != layoutBlockB) {
                        //Found continuing at A only
                        setting = LayoutSlip.STATE_AD;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                        prevConnectType = HitPointType.SLIP_A;
                    } else { // both connecting track segments continue in current block, must search further
                        if ((layoutSlip.getConnectA() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectA(), layoutSlip)) {
                            prevConnectType = HitPointType.SLIP_A;
                            setting = LayoutSlip.STATE_AD;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                        } else if ((layoutSlip.getConnectB() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectB(), layoutSlip)) {
                            prevConnectType = HitPointType.SLIP_B;
                            setting = LayoutSlip.STATE_BD;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                        } else {
                            if (!suppress) {
                                log.warn("Neither branch at {} leads to next Block {}", layoutTurnout, nextLayoutBlock);
                            }
                            trackSegment = null;
                        }
                    }
                    break;
                default:
                    break;
            }
            if ((trackSegment != null) && (trackSegment.getLayoutBlock() != currLayoutBlock)) {
                // continuing track segment is not in this block
                trackSegment = null;
            } else if (trackSegment == null) {
                if (!suppress) {
                    log.warn("Connectivity not complete at {}", layoutSlip.getDisplayName());
                }
                turnoutConnectivity = false;
            }
        } else {
            switch (cType) {
                case TURNOUT_A:
                    // check for left-handed crossover
                    if (tType == LayoutTurnout.TurnoutType.LH_XOVER) {
                        // entering at a continuing track of a left-handed crossover
                        prevConnectType = HitPointType.TURNOUT_B;
                        setting = Turnout.CLOSED;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                    } // entering at a throat, determine exit by checking block of connected track segment
                    else if ((nextLayoutBlock == layoutTurnout.getLayoutBlockB()) || ((layoutTurnout.getConnectB() != null)
                            && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectB()).getLayoutBlock()))) {
                        // exiting block at continuing track
                        prevConnectType = HitPointType.TURNOUT_B;
                        setting = Turnout.CLOSED;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                    } else if ((nextLayoutBlock == layoutTurnout.getLayoutBlockC()) || ((layoutTurnout.getConnectC() != null)
                            && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectC()).getLayoutBlock()))) {
                        // exiting block at diverging track
                        prevConnectType = HitPointType.TURNOUT_C;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                    } // must stay in block after turnout - check if only one track continues in block
                    else if ((layoutTurnout.getConnectB() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectB()).getLayoutBlock())
                            && (layoutTurnout.getConnectC() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectC()).getLayoutBlock())) {
                        // continuing in block on continuing track only
                        prevConnectType = HitPointType.TURNOUT_B;
                        setting = Turnout.CLOSED;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                    } else if ((layoutTurnout.getConnectC() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectC()).getLayoutBlock())
                            && (layoutTurnout.getConnectB() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectB()).getLayoutBlock())) {
                        // continuing in block on diverging track only
                        prevConnectType = HitPointType.TURNOUT_C;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                    } else { // both connecting track segments continue in current block, must search further
                        // check if continuing track leads to the next block
                        if ((layoutTurnout.getConnectB() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectB(), layoutTurnout)) {
                            prevConnectType = HitPointType.TURNOUT_B;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                        } // check if diverging track leads to the next block
                        else if ((layoutTurnout.getConnectC() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectC(), layoutTurnout)) {
                            prevConnectType = HitPointType.TURNOUT_C;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                        } else {
                            if (!suppress) {
                                log.warn("Neither branch at {} leads to next Block {}", layoutTurnout, nextLayoutBlock);
                            }
                            trackSegment = null;
                        }
                    }
                    break;
                case TURNOUT_B:
                    if ((tType == LayoutTurnout.TurnoutType.LH_XOVER) || (tType == LayoutTurnout.TurnoutType.DOUBLE_XOVER)) {
                        // entering at a throat of a double crossover or a left-handed crossover
                        if ((nextLayoutBlock == layoutTurnout.getLayoutBlock()) || ((layoutTurnout.getConnectA() != null)
                                && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectA()).getLayoutBlock()))) {
                            // exiting block at continuing track
                            prevConnectType = HitPointType.TURNOUT_A;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                        } else if ((nextLayoutBlock == layoutTurnout.getLayoutBlockD()) || ((layoutTurnout.getConnectD() != null)
                                && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectD()).getLayoutBlock()))) {
                            // exiting block at diverging track
                            prevConnectType = HitPointType.TURNOUT_D;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        } // must stay in block after turnout
                        else if (((layoutTurnout.getConnectA() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectA()).getLayoutBlock()))
                                && ((layoutTurnout.getConnectD() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectD()).getLayoutBlock()))) {
                            // continuing in block on continuing track only
                            prevConnectType = HitPointType.TURNOUT_A;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                        } else if (((layoutTurnout.getConnectD() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectD()).getLayoutBlock()))
                                && ((layoutTurnout.getConnectA() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectA()).getLayoutBlock()))) {
                            // continuing in block on diverging track only
                            prevConnectType = HitPointType.TURNOUT_D;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        } else { // both connecting track segments continue in current block, must search further
                            // check if continuing track leads to the next block
                            if ((layoutTurnout.getConnectA() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectA(), layoutTurnout)) {
                                prevConnectType = HitPointType.TURNOUT_A;
                                setting = Turnout.CLOSED;
                                trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                            } // check if diverging track leads to the next block
                            else if ((layoutTurnout.getConnectD() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectD(), layoutTurnout)) {
                                prevConnectType = HitPointType.TURNOUT_D;
                                trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                            } else {
                                if (!suppress) {
                                    log.warn("Neither branch at {} leads to next Block {}", layoutTurnout, nextLayoutBlock);
                                }
                                trackSegment = null;
                            }
                        }
                    } else {
                        // entering at continuing track, must exit at throat
                        prevConnectType = HitPointType.TURNOUT_A;
                        setting = Turnout.CLOSED;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                    }
                    break;
                case TURNOUT_C:
                    if ((tType == LayoutTurnout.TurnoutType.RH_XOVER) || (tType == LayoutTurnout.TurnoutType.DOUBLE_XOVER)) {
                        // entering at a throat of a double crossover or a right-handed crossover
                        if ((nextLayoutBlock == layoutTurnout.getLayoutBlockD()) || ((layoutTurnout.getConnectD() != null)
                                && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectD()).getLayoutBlock()))) {
                            // exiting block at continuing track
                            prevConnectType = HitPointType.TURNOUT_D;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        } else if ((nextLayoutBlock == layoutTurnout.getLayoutBlock()) || ((layoutTurnout.getConnectA() != null)
                                && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectA()).getLayoutBlock()))) {
                            // exiting block at diverging track
                            prevConnectType = HitPointType.TURNOUT_A;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                        } // must stay in block after turnout
                        else if (((layoutTurnout.getConnectD() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectD()).getLayoutBlock()))
                                && ((layoutTurnout.getConnectA() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectA()).getLayoutBlock()))) {
                            // continuing in block on continuing track
                            prevConnectType = HitPointType.TURNOUT_D;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        } else if (((layoutTurnout.getConnectA() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectA()).getLayoutBlock()))
                                && ((layoutTurnout.getConnectD() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectD()).getLayoutBlock()))) {
                            // continuing in block on diverging track
                            prevConnectType = HitPointType.TURNOUT_A;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                        } else { // both connecting track segments continue in current block, must search further
                            // check if continuing track leads to the next block
                            if ((layoutTurnout.getConnectD() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectD(), layoutTurnout)) {
                                prevConnectType = HitPointType.TURNOUT_D;
                                setting = Turnout.CLOSED;
                                trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                            } // check if diverging track leads to the next block
                            else if ((layoutTurnout.getConnectA() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectA(), layoutTurnout)) {
                                prevConnectType = HitPointType.TURNOUT_A;
                                trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                            } else {
                                if (!suppress) {
                                    log.warn("Neither branch at {} leads to next Block {}", layoutTurnout, nextLayoutBlock);
                                }
                                trackSegment = null;
                            }
                        }
                    } else if (tType == LayoutTurnout.TurnoutType.LH_XOVER) {
                        // entering at continuing track, must exit at throat
                        prevConnectType = HitPointType.TURNOUT_D;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        setting = Turnout.CLOSED;
                    } else {
                        // entering at diverging track, must exit at throat
                        prevConnectType = HitPointType.TURNOUT_A;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                    }
                    break;
                case TURNOUT_D:
                    if ((tType == LayoutTurnout.TurnoutType.LH_XOVER) || (tType == LayoutTurnout.TurnoutType.DOUBLE_XOVER)) {
                        // entering at a throat of a double crossover or a left-handed crossover
                        if ((nextLayoutBlock == layoutTurnout.getLayoutBlockC()) || ((layoutTurnout.getConnectC() != null)
                                && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectC()).getLayoutBlock()))) {
                            // exiting block at continuing track
                            prevConnectType = HitPointType.TURNOUT_C;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                        } else if ((nextLayoutBlock == layoutTurnout.getLayoutBlockB()) || ((layoutTurnout.getConnectB() != null)
                                && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectB()).getLayoutBlock()))) {
                            // exiting block at diverging track
                            prevConnectType = HitPointType.TURNOUT_B;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                        } // must stay in block after turnout
                        else if (((layoutTurnout.getConnectC() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectC()).getLayoutBlock()))
                                && ((layoutTurnout.getConnectB() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectB()).getLayoutBlock()))) {
                            // continuing in block on continuing track
                            prevConnectType = HitPointType.TURNOUT_C;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                        } else if (((layoutTurnout.getConnectB() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectB()).getLayoutBlock()))
                                && ((layoutTurnout.getConnectC() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectC()).getLayoutBlock()))) {
                            // continuing in block on diverging track
                            prevConnectType = HitPointType.TURNOUT_B;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                        } else { // both connecting track segments continue in current block, must search further
                            // check if continuing track leads to the next block
                            if ((layoutTurnout.getConnectC() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectC(), layoutTurnout)) {
                                prevConnectType = HitPointType.TURNOUT_C;
                                setting = Turnout.CLOSED;
                                trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                            } // check if diverging track leads to the next block
                            else if ((layoutTurnout.getConnectB() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectB(), layoutTurnout)) {
                                prevConnectType = HitPointType.TURNOUT_B;
                                trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                            } else {
                                if (!suppress) {
                                    log.warn("Neither branch at {} leads to next Block {}", layoutTurnout, nextLayoutBlock);
                                }
                                trackSegment = null;
                            }
                        }
                    } else if (tType == LayoutTurnout.TurnoutType.RH_XOVER) {
                        // entering at through track of a right-handed crossover, must exit at throat
                        prevConnectType = HitPointType.TURNOUT_C;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                        setting = Turnout.CLOSED;
                    } else {
                        // entering at diverging track of a right-handed crossover, must exit at throat
                        prevConnectType = HitPointType.TURNOUT_A;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                    }
                    break;
                default: {
                    log.warn("getTurnoutSetting() unknown cType: {}", cType);
                    break;
                }
            }   // switch (cType)

            if ((trackSegment != null) && (trackSegment.getLayoutBlock() != currLayoutBlock)) {
                // continuing track segment is not in this block
                trackSegment = null;
            } else if (trackSegment == null) {
                if (!suppress) {
                    log.warn("Connectivity not complete at {}", layoutTurnout.getTurnoutName());
                }
                turnoutConnectivity = false;
            }
            if (layoutTurnout.getContinuingSense() != Turnout.CLOSED) {
                if (setting == Turnout.THROWN) {
                    setting = Turnout.CLOSED;
                } else if (setting == Turnout.CLOSED) {
                    setting = Turnout.THROWN;
                }
            }
        }
        return (setting);
    }

    /**
     * Follow the track from a beginning track segment to its exits from the
     * current LayoutBlock 'currLayoutBlock' until the track connects to the
     * designated Block 'nextLayoutBlock' or all exit points have been tested.
     *
     * @return 'true' if designated Block is connected; 'false' if not
     */
    private boolean trackSegmentLeadsTo(
            @CheckForNull TrackSegment trackSegment, @CheckForNull LayoutTrack layoutTrack) {
        if ((trackSegment == null) || (layoutTrack == null)) {
            log.error("Null argument on entry to trackSegmentLeadsTo");
            return false;
        }
        TrackSegment curTrackSegment = trackSegment;
        LayoutTrack curLayoutTrack = layoutTrack;

        if (log.isDebugEnabled()) {
            log.info("trackSegmentLeadsTo({}, {}): entry", curTrackSegment.getName(), curLayoutTrack.getName());
        }

        // post process track segment and conObj lists
        List<TrackSegment> postTrackSegments = new ArrayList<>();
        List<LayoutTrack> postLayoutTracks = new ArrayList<>();

        HitPointType conType;
        LayoutTrack conLayoutTrack;

        // follow track to all exit points outside this block
        while (curTrackSegment != null) {
            // if the current track segment is in the next block...
            if (curTrackSegment.getLayoutBlock() == nextLayoutBlock) {
                return true;    // ... we're done!
            }

            // if the current track segment is in the current block...
            if (curTrackSegment.getLayoutBlock() == currLayoutBlock) {
                // identify next destination along track
                if (curTrackSegment.getConnect1() == curLayoutTrack) {
                    // entered through 1, leaving through 2
                    conType = curTrackSegment.getType2();
                    conLayoutTrack = curTrackSegment.getConnect2();
                } else if (curTrackSegment.getConnect2() == curLayoutTrack) {
                    // entered through 2, leaving through 1
                    conType = curTrackSegment.getType1();
                    conLayoutTrack = curTrackSegment.getConnect1();
                } else {
                    log.error("Connectivity error when following track {} in Block {}", curTrackSegment.getName(), currLayoutBlock.getUserName());
                    log.warn("{} not connected to {} (connects: {} & {})",
                            curLayoutTrack.getName(),
                            curTrackSegment.getName(),
                            curTrackSegment.getConnect1Name(),
                            curTrackSegment.getConnect2Name());
                    return false;
                }

                if (log.isDebugEnabled()) {
                    log.info("In block {}, going from {} thru {} to {} (conType: {}), nextLayoutBlock: {}",
                            currLayoutBlock.getUserName(),
                            conLayoutTrack.getName(),
                            curTrackSegment.getName(),
                            curLayoutTrack.getName(),
                            conType.name(),
                            nextLayoutBlock.getId());
                }

                // follow track according to next destination type
                // this is a positionable point
                if (conType == HitPointType.POS_POINT) {
                    // reached anchor point or end bumper
                    if (((PositionablePoint) conLayoutTrack).getType() == PositionablePoint.PointType.END_BUMPER) {
                        // end of line without reaching 'nextLayoutBlock'
                        if (log.isDebugEnabled()) {
                            log.info("end of line without reaching {}", nextLayoutBlock.getId());
                        }
                        curTrackSegment = null;
                    } else if (((PositionablePoint) conLayoutTrack).getType() == PositionablePoint.PointType.ANCHOR
                            || ((PositionablePoint) conLayoutTrack).getType() == PositionablePoint.PointType.EDGE_CONNECTOR) {
                        // proceed to next track segment if within the same Block
                        if (((PositionablePoint) conLayoutTrack).getConnect1() == curTrackSegment) {
                            curTrackSegment = (((PositionablePoint) conLayoutTrack).getConnect2());
                        } else {
                            curTrackSegment = (((PositionablePoint) conLayoutTrack).getConnect1());
                        }
                        curLayoutTrack = conLayoutTrack;
                    }
                } else if (HitPointType.isLevelXingHitType(conType)) {
                    // reached a level crossing
                    if ((conType == HitPointType.LEVEL_XING_A) || (conType == HitPointType.LEVEL_XING_C)) {
                        if (((LevelXing) conLayoutTrack).getLayoutBlockAC() != currLayoutBlock) {
                            if (((LevelXing) conLayoutTrack).getLayoutBlockAC() == nextLayoutBlock) {
                                return true;
                            } else {
                                curTrackSegment = null;
                            }
                        } else if (conType == HitPointType.LEVEL_XING_A) {
                            curTrackSegment = (TrackSegment) ((LevelXing) conLayoutTrack).getConnectC();
                        } else {
                            curTrackSegment = (TrackSegment) ((LevelXing) conLayoutTrack).getConnectA();
                        }
                    } else {
                        if (((LevelXing) conLayoutTrack).getLayoutBlockBD() != currLayoutBlock) {
                            if (((LevelXing) conLayoutTrack).getLayoutBlockBD() == nextLayoutBlock) {
                                return true;
                            } else {
                                curTrackSegment = null;
                            }
                        } else if (conType == HitPointType.LEVEL_XING_B) {
                            curTrackSegment = (TrackSegment) ((LevelXing) conLayoutTrack).getConnectD();
                        } else {
                            curTrackSegment = (TrackSegment) ((LevelXing) conLayoutTrack).getConnectB();
                        }
                    }
                    curLayoutTrack = conLayoutTrack;
                } else if (HitPointType.isTurnoutHitType(conType)) {
                    // reached a turnout
                    LayoutTurnout lt = (LayoutTurnout) conLayoutTrack;
                    LayoutTurnout.TurnoutType tType = lt.getTurnoutType();

                    // RH, LH or DOUBLE _XOVER
                    if (lt.isTurnoutTypeXover()) {
                        // reached a crossover turnout
                        switch (conType) {
                            case TURNOUT_A:
                                if ((lt.getLayoutBlock()) != currLayoutBlock) {
                                    if (lt.getLayoutBlock() == nextLayoutBlock) {
                                        return true;
                                    } else {
                                        curTrackSegment = null;
                                    }
                                } else if ((lt.getLayoutBlockB() == nextLayoutBlock) || ((tType != LayoutTurnout.TurnoutType.LH_XOVER)
                                        && (lt.getLayoutBlockC() == nextLayoutBlock))) {
                                    return true;
                                } else if (lt.getLayoutBlockB() == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) lt.getConnectB();
                                    if ((tType != LayoutTurnout.TurnoutType.LH_XOVER) && (lt.getLayoutBlockC() == currLayoutBlock)) {
                                        postTrackSegments.add((TrackSegment) lt.getConnectC());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else if ((tType != LayoutTurnout.TurnoutType.LH_XOVER) && (lt.getLayoutBlockC() == currLayoutBlock)) {
                                    curTrackSegment = (TrackSegment) lt.getConnectC();
                                } else {
                                    curTrackSegment = null;
                                }
                                break;
                            case TURNOUT_B:
                                if ((lt.getLayoutBlockB()) != currLayoutBlock) {
                                    if (lt.getLayoutBlockB() == nextLayoutBlock) {
                                        return true;
                                    } else {
                                        curTrackSegment = null;
                                    }
                                } else if ((lt.getLayoutBlock() == nextLayoutBlock) || ((tType != LayoutTurnout.TurnoutType.RH_XOVER)
                                        && (lt.getLayoutBlockD() == nextLayoutBlock))) {
                                    return true;
                                } else if (lt.getLayoutBlock() == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) lt.getConnectA();
                                    if ((tType != LayoutTurnout.TurnoutType.RH_XOVER) && (lt.getLayoutBlockD() == currLayoutBlock)) {
                                        postTrackSegments.add((TrackSegment) lt.getConnectD());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else if ((tType != LayoutTurnout.TurnoutType.RH_XOVER) && (lt.getLayoutBlockD() == currLayoutBlock)) {
                                    curTrackSegment = (TrackSegment) lt.getConnectD();
                                } else {
                                    curTrackSegment = null;
                                }
                                break;
                            case TURNOUT_C:
                                if ((lt.getLayoutBlockC()) != currLayoutBlock) {
                                    if (lt.getLayoutBlockC() == nextLayoutBlock) {
                                        return true;
                                    } else {
                                        curTrackSegment = null;
                                    }
                                } else if ((lt.getLayoutBlockD() == nextLayoutBlock) || ((tType != LayoutTurnout.TurnoutType.LH_XOVER)
                                        && (lt.getLayoutBlock() == nextLayoutBlock))) {
                                    return true;
                                } else if (lt.getLayoutBlockD() == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) lt.getConnectD();
                                    if ((tType != LayoutTurnout.TurnoutType.LH_XOVER) && (lt.getLayoutBlock() == currLayoutBlock)) {
                                        postTrackSegments.add((TrackSegment) lt.getConnectA());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else if ((tType != LayoutTurnout.TurnoutType.LH_XOVER) && (lt.getLayoutBlock() == currLayoutBlock)) {
                                    curTrackSegment = (TrackSegment) lt.getConnectA();
                                } else {
                                    curTrackSegment = null;
                                }
                                break;
                            case TURNOUT_D:
                                if ((lt.getLayoutBlockD()) != currLayoutBlock) {
                                    if (lt.getLayoutBlockD() == nextLayoutBlock) {
                                        return true;
                                    } else {
                                        curTrackSegment = null;
                                    }
                                } else if ((lt.getLayoutBlockC() == nextLayoutBlock) || ((tType != LayoutTurnout.TurnoutType.RH_XOVER)
                                        && (lt.getLayoutBlockB() == nextLayoutBlock))) {
                                    return true;
                                } else if (lt.getLayoutBlockC() == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) lt.getConnectC();
                                    if ((tType != LayoutTurnout.TurnoutType.RH_XOVER) && (lt.getLayoutBlockB() == currLayoutBlock)) {
                                        postTrackSegments.add((TrackSegment) lt.getConnectB());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else if ((tType != LayoutTurnout.TurnoutType.RH_XOVER) && (lt.getLayoutBlockB() == currLayoutBlock)) {
                                    curTrackSegment = (TrackSegment) lt.getConnectB();
                                } else {
                                    curTrackSegment = null;
                                }
                                break;
                            default: {
                                log.warn("trackSegmentLeadsTo() unknown conType: {}", conType);
                                break;
                            }
                        }   // switch (conType)
                        curLayoutTrack = conLayoutTrack;
                    } else // if RH, LH or DOUBLE _XOVER
                    if (LayoutTurnout.isTurnoutTypeTurnout(tType)) {
                        // reached RH. LH, or WYE turnout
                        if (lt.getLayoutBlock() != currLayoutBlock) {    // if not in the last block...
                            if (lt.getLayoutBlock() == nextLayoutBlock) {   // if in the next block
                                return true;    //(Yes!) done
                            } else {
                                curTrackSegment = null;   //(nope) dead end
                            }
                        } else {
                            if (conType == HitPointType.TURNOUT_A) {
                                // if the connect B or C are in the next block...
                                if ((((TrackSegment) lt.getConnectB()).getLayoutBlock() == nextLayoutBlock)
                                        || (((TrackSegment) lt.getConnectC()).getLayoutBlock() == nextLayoutBlock)) {
                                    return true;    //(yes!) done!
                                } else // if connect B is in this block...
                                if (((TrackSegment) lt.getConnectB()).getLayoutBlock() == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) lt.getConnectB();
                                    //if connect C is in this block
                                    if (((TrackSegment) lt.getConnectC()).getLayoutBlock() == currLayoutBlock) {
                                        // add it to our post processing list
                                        postTrackSegments.add((TrackSegment) lt.getConnectC());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else {
                                    curTrackSegment = (TrackSegment) lt.getConnectC();
                                }
                            } else {
                                curTrackSegment = (TrackSegment) lt.getConnectA();
                            }
                            curLayoutTrack = conLayoutTrack;
                        }
                    }   // if RH, LH or WYE _TURNOUT
                } else if (HitPointType.isSlipHitType(conType)) {
                    LayoutSlip ls = (LayoutSlip) conLayoutTrack;
                    LayoutTurnout.TurnoutType tType = ls.getTurnoutType();

                    if (ls.getLayoutBlock() != currLayoutBlock) {    // if not in the last block
                        if (ls.getLayoutBlock() == nextLayoutBlock) {   // if in the next block
                            return true;    //(yes!) done
                        } else {
                            curTrackSegment = null;   //(nope) dead end
                        }
                    } else {    // still in the last block
                        LayoutBlock layoutBlockA = ((TrackSegment) ls.getConnectA()).getLayoutBlock();
                        LayoutBlock layoutBlockB = ((TrackSegment) ls.getConnectB()).getLayoutBlock();
                        LayoutBlock layoutBlockC = ((TrackSegment) ls.getConnectC()).getLayoutBlock();
                        LayoutBlock layoutBlockD = ((TrackSegment) ls.getConnectD()).getLayoutBlock();
                        switch (conType) {
                            case SLIP_A:
                                if (layoutBlockC == nextLayoutBlock) {
                                    //Leg A-D has next currLayoutBlock
                                    return true;
                                }
                                if (layoutBlockD == nextLayoutBlock) {
                                    //Leg A-C has next currLayoutBlock
                                    return true;
                                }
                                if (layoutBlockC == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) ls.getConnectC();
                                    if (layoutBlockD == currLayoutBlock) {
                                        postTrackSegments.add((TrackSegment) ls.getConnectD());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else {
                                    curTrackSegment = (TrackSegment) ls.getConnectD();
                                }
                                break;
                            case SLIP_B:
                                if (tType == LayoutSlip.TurnoutType.SINGLE_SLIP) {
                                    curTrackSegment = (TrackSegment) ls.getConnectD();
                                    break;
                                }
                                if (layoutBlockC == nextLayoutBlock) {
                                    //Leg B-C has next currLayoutBlock
                                    return true;
                                }
                                if (layoutBlockD == nextLayoutBlock) {
                                    //Leg D-B has next currLayoutBlock
                                    return true;
                                }
                                if (layoutBlockC == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) ls.getConnectC();
                                    if (layoutBlockD == currLayoutBlock) {
                                        postTrackSegments.add((TrackSegment) ls.getConnectD());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else {
                                    curTrackSegment = (TrackSegment) ls.getConnectD();
                                }
                                break;
                            case SLIP_C:
                                // if this is a single slip...
                                if (tType == LayoutSlip.TurnoutType.SINGLE_SLIP) {
                                    curTrackSegment = (TrackSegment) ls.getConnectA();
                                    break;
                                }
                                //if connect A is in the next block
                                if (layoutBlockA == nextLayoutBlock) {
                                    return true;    //(Yes!) Leg A-C has next block
                                }
                                //if connect B is in the next block
                                if (layoutBlockB == nextLayoutBlock) {
                                    return true;    //(Yes!) Leg B-C has next block
                                }

                                //if connect B is in this block...
                                if (layoutBlockB == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) ls.getConnectB();
                                    //if connect A is in this block...
                                    if (layoutBlockA == currLayoutBlock) {
                                        // add it to our post processing list
                                        postTrackSegments.add((TrackSegment) ls.getConnectA());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else { //if connect A is in this block...
                                    if (layoutBlockA == currLayoutBlock) {
                                        curTrackSegment = (TrackSegment) ls.getConnectA();
                                    } else {
                                        log.debug("{} not connected to {} (connections: {} & {})",
                                                currLayoutBlock.getUserName(), ls.getName(),
                                                ls.getConnectA().getName(),
                                                ls.getConnectB().getName());
                                    }
                                }
                                break;
                            case SLIP_D:
                                if (layoutBlockA == nextLayoutBlock) {
                                    //Leg D-A has next currLayoutBlock
                                    return true;
                                }
                                if (layoutBlockB == nextLayoutBlock) {
                                    //Leg D-B has next currLayoutBlock
                                    return true;
                                }
                                if (layoutBlockB == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) ls.getConnectB();
                                    if (layoutBlockA == currLayoutBlock) {
                                        postTrackSegments.add((TrackSegment) ls.getConnectA());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else {
                                    curTrackSegment = (TrackSegment) ls.getConnectA();
                                }
                                break;
                            default: {
                                log.warn("trackSegmentLeadsTo() unknown conType: {}", conType);
                                break;
                            }
                        }   //switch (conType)
                        curLayoutTrack = conLayoutTrack;
                    }   // if (ls.getLayoutBlock() != currLayoutBlock
                }   //else if (LayoutEditor.HitPointType.isSlipHitType(conType))
            } else {
                curTrackSegment = null;
            }

            if (curTrackSegment == null) {
                // reached an end point outside this block that was not 'nextLayoutBlock' - any other paths to follow?
                if (postTrackSegments.size() > 0) {
                    // paths remain, initialize the next one
                    curTrackSegment = postTrackSegments.get(0);
                    curLayoutTrack = postLayoutTracks.get(0);
                    // remove it from the list of unexplored paths
                    postTrackSegments.remove(0);
                    postLayoutTracks.remove(0);
                }
            }
        }   // while (curTS != null)

        // searched all possible paths in this block, 'currLayoutBlock', without finding the desired exit block, 'nextLayoutBlock'
        return false;
    }

    private boolean turnoutConnectivity = true;

    /**
     * Check if the connectivity of the turnouts has been completed in the block
     * after calling getTurnoutList().
     *
     * @return true if turnout connectivity is complete; otherwise false
     */
    public boolean isTurnoutConnectivityComplete() {
        return turnoutConnectivity;
    }

    private void setupOpposingTrackSegment(@Nonnull LevelXing x, HitPointType cType) {
        switch (cType) {
            case LEVEL_XING_A:
                trackSegment = (TrackSegment) x.getConnectC();
                prevConnectType = HitPointType.LEVEL_XING_C;
                break;
            case LEVEL_XING_B:
                trackSegment = (TrackSegment) x.getConnectD();
                prevConnectType = HitPointType.LEVEL_XING_D;
                break;
            case LEVEL_XING_C:
                trackSegment = (TrackSegment) x.getConnectA();
                prevConnectType = HitPointType.LEVEL_XING_A;
                break;
            case LEVEL_XING_D:
                trackSegment = (TrackSegment) x.getConnectB();
                prevConnectType = HitPointType.LEVEL_XING_B;
                break;
            default:
                break;
        }
        if (trackSegment.getLayoutBlock() != currLayoutBlock) {
            // track segment is not in this block
            trackSegment = null;
        } else {
            // track segment is in this block
            prevConnectTrack = x;
        }
    }

    @Nonnull
    public List<LayoutTurnout> getAllTurnoutsThisBlock(
            @Nonnull LayoutBlock currLayoutBlock) {
        return layoutEditor.getLayoutTracks().stream()
                .filter((o) -> (o instanceof LayoutTurnout)) // this includes LayoutSlips
                .map(LayoutTurnout.class::cast)
                .filter((lt) -> ((lt.getLayoutBlock() == currLayoutBlock)
                || (lt.getLayoutBlockB() == currLayoutBlock)
                || (lt.getLayoutBlockC() == currLayoutBlock)
                || (lt.getLayoutBlockD() == currLayoutBlock)))
                .map(LayoutTurnout.class::cast)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectivityUtil.class);
}
