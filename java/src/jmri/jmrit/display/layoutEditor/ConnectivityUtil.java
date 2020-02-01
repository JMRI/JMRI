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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *
 * @author Dave Duchamp Copyright (c) 2009
 * @author George Warner Copyright (c) 2017-2018
 */
public class ConnectivityUtil {
    // constants
    // operational instance variables
    private LayoutEditor layoutEditor = null;
    private LayoutEditorAuxTools auxTools = null;
    private LayoutBlockManager layoutBlockManager = null;

    // constructor method
    public ConnectivityUtil(LayoutEditor thePanel) {
        layoutEditor = thePanel;
        auxTools = new LayoutEditorAuxTools(layoutEditor);
        layoutBlockManager = InstanceManager.getDefault(LayoutBlockManager.class);
    }

    private TrackSegment trackSegment = null;
    private int prevConnectType = LayoutTrack.NONE;
    private LayoutTrack prevConnectTrack = null;
    private LayoutBlock currLayoutBlock = null;
    private LayoutBlock nextLayoutBlock = null;
    private LayoutBlock prevLayoutBlock = null;

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

        prevLayoutBlock = null;
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
        int cType;
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
                    case LayoutConnectivity.XOVER_BOUNDARY_AB:
                        setting = Turnout.CLOSED;
                        if (((TrackSegment) xt.getConnectA() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectA()).getLayoutBlock())) {
                            // block exits Xover at A
                            trackSegment = (TrackSegment) xt.getConnectA();
                            prevConnectType = LayoutTrack.TURNOUT_A;
                        } else if (((TrackSegment) xt.getConnectB() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectB()).getLayoutBlock())) {
                            // block exits Xover at B
                            trackSegment = (TrackSegment) xt.getConnectB();
                            prevConnectType = LayoutTrack.TURNOUT_B;
                        }
                        break;
                    case LayoutConnectivity.XOVER_BOUNDARY_CD:
                        setting = Turnout.CLOSED;
                        if (((TrackSegment) xt.getConnectC() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectC()).getLayoutBlock())) {
                            // block exits Xover at C
                            trackSegment = (TrackSegment) xt.getConnectC();
                            prevConnectType = LayoutTrack.TURNOUT_C;
                        } else if (((TrackSegment) xt.getConnectD() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectD()).getLayoutBlock())) {
                            // block exits Xover at D
                            trackSegment = (TrackSegment) xt.getConnectD();
                            prevConnectType = LayoutTrack.TURNOUT_D;
                        }
                        break;
                    case LayoutConnectivity.XOVER_BOUNDARY_AC:
                        if (((TrackSegment) xt.getConnectA() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectA()).getLayoutBlock())) {
                            // block exits Xover at A
                            trackSegment = (TrackSegment) xt.getConnectA();
                            prevConnectType = LayoutTrack.TURNOUT_A;
                        } else if (((TrackSegment) xt.getConnectC() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectC()).getLayoutBlock())) {
                            // block exits Xover at C
                            trackSegment = (TrackSegment) xt.getConnectC();
                            prevConnectType = LayoutTrack.TURNOUT_C;
                        }
                        break;
                    case LayoutConnectivity.XOVER_BOUNDARY_BD:
                        if (((TrackSegment) xt.getConnectB() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectB()).getLayoutBlock())) {
                            // block exits Xover at B
                            trackSegment = (TrackSegment) xt.getConnectB();
                            prevConnectType = LayoutTrack.TURNOUT_B;
                        } else if (((TrackSegment) xt.getConnectD() != null) && (currLayoutBlock == ((TrackSegment) xt.getConnectD()).getLayoutBlock())) {
                            // block exits Xover at D
                            trackSegment = (TrackSegment) xt.getConnectD();
                            prevConnectType = LayoutTrack.TURNOUT_D;
                        }
                        break;
                    default:
                        break;
                }
                result.add(new LayoutTrackExpectedState<>(xt, setting));
                notFound = false;
            } else if ((lc.getBlock1() == currLayoutBlock) && (lc.getBlock2() == prevLayoutBlock)) {
                // no turnout or level crossing at the beginning of this block
                trackSegment = lc.getTrackSegment();
                if (lc.getConnectedType() == LayoutTrack.TRACK) {
                    prevConnectType = LayoutTrack.POS_POINT;
                    prevConnectTrack = lc.getAnchor();
                } else {
                    prevConnectType = lc.getConnectedType();
                    prevConnectTrack = lc.getConnectedObject();
                }
                notFound = false;
            } else if ((lc.getBlock2() == currLayoutBlock) && (lc.getBlock1() == prevLayoutBlock)) {
                cType = lc.getConnectedType();
                // check for connection to a track segment
                if (cType == LayoutTrack.TRACK) {
                    trackSegment = (TrackSegment) lc.getConnectedObject();
                    prevConnectType = LayoutTrack.POS_POINT;
                    prevConnectTrack = lc.getAnchor();
                } // check for a level crossing
                else if ((cType >= LayoutTrack.LEVEL_XING_A) && (cType <= LayoutTrack.LEVEL_XING_D)) {
                    // entering this Block at a level crossing, skip over it an initialize the next
                    //      TrackSegment if there is one in this Block
                    setupOpposingTrackSegment((LevelXing) lc.getConnectedObject(), cType);
                } // check for turnout
                else if ((cType >= LayoutTrack.TURNOUT_A) && (cType <= LayoutTrack.TURNOUT_D)) {
                    // add turnout to list
                    result.add(new LayoutTrackExpectedState<>((LayoutTurnout) lc.getConnectedObject(),
                            getTurnoutSetting((LayoutTurnout) lc.getConnectedObject(), cType, suppress)));
                } else if ((cType >= LayoutTrack.SLIP_A) && (cType <= LayoutTrack.SLIP_D)) {
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
                    log.error("Connectivity error when searching turnouts in Block " + currLayoutBlock.getDisplayName());
                    log.warn("Track segment connected to {{}, {}} and {{}, {}} but previous object was {{}, {}}",
                            trackSegment.getConnect1(), connectionTypeToString(trackSegment.getType1()),
                            trackSegment.getConnect2(), connectionTypeToString(trackSegment.getType2()),
                            prevConnectTrack, connectionTypeToString(prevConnectType));
                }
                trackSegment = null;
                break;
            }
            if (cType == LayoutTrack.POS_POINT) {
                // reached anchor point or end bumper
                if (((PositionablePoint) cObject).getType() == PositionablePoint.END_BUMPER) {
                    // end of line
                    trackSegment = null;
                } else if (((PositionablePoint) cObject).getType() == PositionablePoint.ANCHOR || (((PositionablePoint) cObject).getType() == PositionablePoint.EDGE_CONNECTOR)) {
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
            } else if ((cType >= LayoutTrack.LEVEL_XING_A) && (cType <= LayoutTrack.LEVEL_XING_D)) {
                // reached a level crossing, is it within this block?
                if ((cType == LayoutTrack.LEVEL_XING_A) || (cType == LayoutTrack.LEVEL_XING_C)) {
                    if (((LevelXing) cObject).getLayoutBlockAC() != currLayoutBlock) {
                        // outside of block
                        trackSegment = null;
                    } else {
                        // same block
                        setupOpposingTrackSegment((LevelXing) cObject, cType);
                    }
                } else {
                    if (((LevelXing) cObject).getLayoutBlockBD() != currLayoutBlock) {
                        // outside of block
                        trackSegment = null;
                    } else {
                        // same block
                        setupOpposingTrackSegment((LevelXing) cObject, cType);
                    }
                }
            } else if ((cType >= LayoutTrack.TURNOUT_A) && (cType <= LayoutTrack.TURNOUT_D)) {
                // reached a turnout
                LayoutTurnout lt = (LayoutTurnout) cObject;
                int tType = lt.getTurnoutType();
                // is this turnout a crossover turnout at least partly within this block?
                if ((tType == LayoutTurnout.DOUBLE_XOVER) || (tType == LayoutTurnout.RH_XOVER)
                        || (tType == LayoutTurnout.LH_XOVER)) {
                    // reached a crossover turnout
                    switch (cType) {
                        case LayoutTrack.TURNOUT_A:
                            if ((lt.getLayoutBlock()) != currLayoutBlock) {
                                // connection is outside of the current block
                                trackSegment = null;
                            } else if (lt.getLayoutBlockB() == nextLayoutBlock) {
                                // exits Block at B
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = null;
                            } else if ((lt.getLayoutBlockC() == nextLayoutBlock) && (tType != LayoutTurnout.LH_XOVER)) {
                                // exits Block at C, either Double or RH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = null;
                            } else if (lt.getLayoutBlockB() == currLayoutBlock) {
                                // block continues at B
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = (TrackSegment) lt.getConnectB();
                                prevConnectType = LayoutTrack.TURNOUT_B;
                                prevConnectTrack = cObject;
                            } else if ((lt.getLayoutBlockC() == currLayoutBlock) && (tType != LayoutTurnout.LH_XOVER)) {
                                // block continues at C, either Double or RH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = (TrackSegment) lt.getConnectC();
                                prevConnectType = LayoutTrack.TURNOUT_C;
                                prevConnectTrack = cObject;
                            } else if (lt.getLayoutBlock() == currLayoutBlock && currLayoutBlock == nextLayoutBlock) {
                                //we are at our final destination so not an error such
                                trackSegment = null;
                            } else {
                                // no legal outcome found, print error
                                if (!suppress) {
                                    log.warn("Connectivity mismatch at A in turnout " + lt.getTurnoutName());
                                }
                                trackSegment = null;
                            }
                            break;
                        case LayoutTrack.TURNOUT_B:
                            if ((lt.getLayoutBlockB()) != currLayoutBlock) {
                                // connection is outside of the current block
                                trackSegment = null;
                            } else if (lt.getLayoutBlock() == nextLayoutBlock) {
                                // exits Block at A
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = null;
                            } else if ((lt.getLayoutBlockD() == nextLayoutBlock) && (tType != LayoutTurnout.RH_XOVER)) {
                                // exits Block at D, either Double or LH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = null;
                            } else if (lt.getLayoutBlock() == currLayoutBlock) {
                                // block continues at A
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = (TrackSegment) lt.getConnectA();
                                prevConnectType = LayoutTrack.TURNOUT_A;
                                prevConnectTrack = cObject;
                            } else if ((lt.getLayoutBlockD() == currLayoutBlock) && (tType != LayoutTurnout.RH_XOVER)) {
                                // block continues at D, either Double or LH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = (TrackSegment) lt.getConnectD();
                                prevConnectType = LayoutTrack.TURNOUT_D;
                                prevConnectTrack = cObject;
                            } else if (lt.getLayoutBlockB() == currLayoutBlock && currLayoutBlock == nextLayoutBlock) {
                                //we are at our final destination so not an error such
                                trackSegment = null;
                            } else {
                                // no legal outcome found, print error
                                if (!suppress) {
                                    log.warn("Connectivity mismatch at B in turnout " + lt.getTurnoutName());
                                }
                                trackSegment = null;
                            }
                            break;
                        case LayoutTrack.TURNOUT_C:
                            if ((lt.getLayoutBlockC()) != currLayoutBlock) {
                                // connection is outside of the current block
                                trackSegment = null;
                            } else if (lt.getLayoutBlockD() == nextLayoutBlock) {
                                // exits Block at D
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = null;
                            } else if ((lt.getLayoutBlock() == nextLayoutBlock) && (tType != LayoutTurnout.LH_XOVER)) {
                                // exits Block at A, either Double or RH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = null;
                            } else if (lt.getLayoutBlockD() == currLayoutBlock) {
                                // block continues at D
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = (TrackSegment) lt.getConnectD();
                                prevConnectType = LayoutTrack.TURNOUT_D;
                                prevConnectTrack = cObject;
                            } else if ((lt.getLayoutBlock() == currLayoutBlock) && (tType != LayoutTurnout.LH_XOVER)) {
                                // block continues at A, either Double or RH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = (TrackSegment) lt.getConnectA();
                                prevConnectType = LayoutTrack.TURNOUT_A;
                                prevConnectTrack = cObject;
                            } else if (lt.getLayoutBlockC() == currLayoutBlock && currLayoutBlock == nextLayoutBlock) {
                                //we are at our final destination so not an error such
                                trackSegment = null;
                            } else {
                                // no legal outcome found, print error
                                if (!suppress) {
                                    log.warn("Connectivity mismatch at C in turnout " + lt.getTurnoutName());
                                }
                                trackSegment = null;
                            }
                            break;
                        case LayoutTrack.TURNOUT_D:
                            if ((lt.getLayoutBlockD()) != currLayoutBlock) {
                                // connection is outside of the current block
                                trackSegment = null;
                            } else if (lt.getLayoutBlockC() == nextLayoutBlock) {
                                // exits Block at C
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = null;
                            } else if ((lt.getLayoutBlockB() == nextLayoutBlock) && (tType != LayoutTurnout.RH_XOVER)) {
                                // exits Block at B, either Double or LH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = null;
                            } else if (lt.getLayoutBlockC() == currLayoutBlock) {
                                // block continues at C
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.CLOSED));
                                trackSegment = (TrackSegment) lt.getConnectC();
                                prevConnectType = LayoutTrack.TURNOUT_C;
                                prevConnectTrack = cObject;
                            } else if ((lt.getLayoutBlockB() == currLayoutBlock) && (tType != LayoutTurnout.RH_XOVER)) {
                                // block continues at B, either Double or LH
                                result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, Turnout.THROWN));
                                trackSegment = (TrackSegment) lt.getConnectB();
                                prevConnectType = LayoutTrack.TURNOUT_B;
                                prevConnectTrack = cObject;
                            } else if (lt.getLayoutBlockD() == currLayoutBlock && currLayoutBlock == nextLayoutBlock) {
                                //we are at our final destination so not an error such
                                trackSegment = null;
                            } else {
                                // no legal outcome found, print error
                                if (!suppress) {
                                    log.warn("Connectivity mismatch at D in turnout " + lt.getTurnoutName());
                                }
                                trackSegment = null;
                            }
                            break;
                        default:
                            log.warn("Unhandled crossover type: {}", cType);
                            break;
                    }
                } else if ((tType == LayoutTurnout.RH_TURNOUT) || (tType == LayoutTurnout.LH_TURNOUT)
                        || (tType == LayoutTurnout.WYE_TURNOUT)) {
                    // reached RH. LH, or WYE turnout, is it in the current Block?
                    if (lt.getLayoutBlock() != currLayoutBlock) {
                        // turnout is outside of current block
                        trackSegment = null;
                    } else {
                        // turnout is inside current block, add it to the list
                        result.add(new LayoutTrackExpectedState<>((LayoutTurnout) cObject, getTurnoutSetting(lt, cType, suppress)));
                    }
                }
            } else if ((cType >= LayoutTrack.SLIP_A) && (cType <= LayoutTrack.SLIP_D)) {
                // reached a LayoutSlip
                LayoutSlip ls = (LayoutSlip) cObject;
                if (((cType == LayoutTrack.SLIP_A) && (ls.getLayoutBlock() != currLayoutBlock))
                        || ((cType == LayoutTrack.SLIP_B) && (ls.getLayoutBlockB() != currLayoutBlock))
                        || ((cType == LayoutTrack.SLIP_C) && (ls.getLayoutBlockC() != currLayoutBlock))
                        || ((cType == LayoutTrack.SLIP_D) && (ls.getLayoutBlockD() != currLayoutBlock))) {
                    //Slip is outside of the current block
                    trackSegment = null;
                } else {
                    // turnout is inside current block, add it to the list
                    result.add(new LayoutTrackExpectedState<>(ls, getTurnoutSetting(ls, cType, suppress)));
                }
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
    public List<Block> getConnectedBlocks(@Nonnull Block block) {
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
        for (int i = 0; i < cList.size(); i++) {
            LayoutConnectivity lc = cList.get(i);
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
            @Nonnull Block block) {
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
    public List<LevelXing> getLevelCrossingsThisBlock(@Nonnull Block block) {
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
                    log.error("Missing connection or block assignment at Level Crossing in Block " + block.getDisplayName());
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
    public List<LayoutTurnout> getLayoutTurnoutsThisBlock(@Nonnull Block block) {
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
            log.error(txt.toString());
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
            case LayoutTurnout.NO_LINK:
                if ((t.getTurnoutType() == LayoutTurnout.RH_TURNOUT)
                        || (t.getTurnoutType() == LayoutTurnout.LH_TURNOUT)
                        || (t.getTurnoutType() == LayoutTurnout.WYE_TURNOUT)) {
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
                        if (t.getTurnoutType() == LayoutTurnout.SINGLE_SLIP) {
                            return true;
                        }
                        if (t.getTurnoutType() == LayoutTurnout.DOUBLE_SLIP) {
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
            case LayoutTurnout.FIRST_3_WAY:
                return (!t.getSignalA1Name().isEmpty()
                        && !t.getSignalC1Name().isEmpty());
            case LayoutTurnout.SECOND_3_WAY:
            case LayoutTurnout.THROAT_TO_THROAT:
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
            if ((LayoutEditorTools.isAtWestEndOfAnchor(p.getConnect2(), p) && facing)
                    || ((!LayoutEditorTools.isAtWestEndOfAnchor(p.getConnect2(), p)) && (!facing))) {
                return (InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(p.getWestBoundSignal()));
            } else {
                return (InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(p.getEastBoundSignal()));
            }
        } else if (((p.getConnect1()).getLayoutBlock() != lBlock) && ((p.getConnect2()).getLayoutBlock() == lBlock)) {
            if ((LayoutEditorTools.isAtWestEndOfAnchor(p.getConnect1(), p) && facing)
                    || ((!LayoutEditorTools.isAtWestEndOfAnchor(p.getConnect1(), p)) && (!facing))) {
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
            if ((LayoutEditorTools.isAtWestEndOfAnchor(p.getConnect2(), p) && facing)
                    || ((!LayoutEditorTools.isAtWestEndOfAnchor(p.getConnect2(), p)) && (!facing))) {
                return (InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(p.getWestBoundSignalMastName()));
            } else {
                return (InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(p.getEastBoundSignalMastName()));
            }
        } else if (((p.getConnect1()).getLayoutBlock() != lBlock) && ((p.getConnect2()).getLayoutBlock() == lBlock)) {
            if ((LayoutEditorTools.isAtWestEndOfAnchor(p.getConnect1(), p) && facing)
                    || ((!LayoutEditorTools.isAtWestEndOfAnchor(p.getConnect1(), p)) && (!facing))) {
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
                log.error("Panel blocking error at BD of Level Crossing in Block " + block.getDisplayName());
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
     * If the SSL has not been set up yet, the sensor is not added, an error message is output
     *      and 'false' is returned.
     * @param name sensor name
     * @param sh signal head
     * @param where should be one of
     *              DIVERGING if the sensor is being added to the diverging (second) part
     *              of a facing mode SSL,
     *              CONTINUING if the sensor is being added to the continuing (first) part
     *              of a facing mode SSL,
     *              OVERALL if the sensor is being added to the overall sensor list of a
     *              facing mode SSL.
     *              'where' is ignored if not a facing mode SSL
     * @return 'true' if the sensor was already in the signal head SSL or if it has been
     *         added successfully; 'false' and logs an error if not.
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
                bbLogic.retain();
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
                log.error("could not add sensor to SSL for signal head " + sh.getDisplayName()
                        + " because there is no room in the SSL.");
                bbLogic.retain();
                bbLogic.start();
                return false;
            }
        } else if (mode == BlockBossLogic.FACING) {
            switch (where) {
                case DIVERGING:
                    if (((bbLogic.getWatchedSensor2() != null) && (bbLogic.getWatchedSensor2()).equals(name))
                            || ((bbLogic.getWatchedSensor2Alt() != null) && (bbLogic.getWatchedSensor2Alt()).equals(name))) {
                        bbLogic.retain();
                        bbLogic.start();
                        return true;
                    }
                    if (bbLogic.getWatchedSensor2() == null) {
                        bbLogic.setWatchedSensor2(name);
                    } else if (bbLogic.getWatchedSensor2Alt() == null) {
                        bbLogic.setWatchedSensor2Alt(name);
                    } else {
                        log.error("could not add watched sensor to SSL for signal head " + sh.getSystemName()
                                + " because there is no room in the facing SSL diverging part.");
                        bbLogic.retain();
                        bbLogic.start();
                        return false;
                    }
                    break;
                case CONTINUING:
                    if (((bbLogic.getWatchedSensor1() != null) && (bbLogic.getWatchedSensor1()).equals(name))
                            || ((bbLogic.getWatchedSensor1Alt() != null) && (bbLogic.getWatchedSensor1Alt()).equals(name))) {
                        bbLogic.retain();
                        bbLogic.start();
                        return true;
                    }
                    if (bbLogic.getWatchedSensor1() == null) {
                        bbLogic.setWatchedSensor1(name);
                    } else if (bbLogic.getWatchedSensor1Alt() == null) {
                        bbLogic.setWatchedSensor1Alt(name);
                    } else {
                        log.error("could not add watched sensor to SSL for signal head " + sh.getSystemName()
                                + " because there is no room in the facing SSL continuing part.");
                        bbLogic.retain();
                        bbLogic.start();
                        return false;
                    }
                    break;
                default:
                    log.error("could not add watched sensor to SSL for signal head " + sh.getSystemName()
                            + "because 'where' to place the sensor was not correctly designated.");
                    bbLogic.retain();
                    bbLogic.start();
                    return false;
            }
        } else {
            log.error("SSL has not been set up for signal head " + sh.getDisplayName()
                    + ". Could not add sensor - " + name + ".");
            return false;
        }
        bbLogic.retain();
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

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
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
        bbLogic.retain();
        bbLogic.start();
        return true;
    }

    /**
     * Get the next TrackNode following the specified TrackNode.
     *
     * @param cNode      the current node
     * @param cNodeState the possible path to follow (for example, if the
     *                   current node is a turnout entered at its throat, the
     *                   path could be the thrown or closed path)
     * @return the next TrackNode following cNode for the given state or null if
     *         unable to follow the track
     */
    @CheckReturnValue
    @CheckForNull
    public TrackNode getNextNode(@CheckForNull TrackNode cNode, int cNodeState) {
        if (cNode == null) {
            log.error("getNextNode called with a null Track Node");
            return null;
        }
        if (cNode.reachedEndOfTrack()) {
            log.error("getNextNode - attempt to search past endBumper");
            return null;
        }
        return (getTrackNode(cNode.getNode(), cNode.getNodeType(), cNode.getTrackSegment(), cNodeState));
    }

    /**
     * Get the next TrackNode following the specified TrackNode, assuming that
     * TrackNode was reached via the specified TrackSegment.
     * <p>
     * If the specified track node can lead to different paths to the next node,
     * for example, if the specified track node is a turnout entered at its
     * throat, then "cNodeState" must be specified to choose between the
     * possible paths. If cNodeState = 0, the search will follow the
     * 'continuing' track; if cNodeState = 1, the search will follow the
     * 'diverging' track; if cNodeState = 2 (3-way turnouts only), the search
     * will follow the second 'diverging' track.
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
     * @param cNode      the current node
     * @param cNodeType  the type of node
     * @param cTrack     the followed track segment
     * @param cNodeState the possible path to follow (for example, if the
     *                   current node is a turnout entered at its throat, the
     *                   path could be the thrown or closed path)
     * @return the next TrackNode following cNode for the given state if a node
     *         or end_of-track is reachedor null if unable to follow the track
     */
    //TODO: cTrack parameter isn't used in this method; is this a bug?
    //TODO: pType local variable is set but never used; dead-code strip?
    @CheckReturnValue
    @CheckForNull
    public TrackNode getTrackNode(
            @Nonnull LayoutTrack cNode,
            int cNodeType,
            @CheckForNull TrackSegment cTrack,
            int cNodeState) {
        // initialize
        LayoutTrack node = null;
        int nodeType = LayoutTurnout.NONE;
        TrackSegment track = null;
        boolean hitEnd = false;
        @SuppressWarnings("unused")
        int pType = cNodeType;
        LayoutTrack pObject = cNode;
        TrackSegment tTrack = null;
        switch (cNodeType) {
            case LayoutTrack.POS_POINT:
                if (cNode instanceof PositionablePoint) {
                    PositionablePoint p = (PositionablePoint) cNode;
                    if (p.getType() == PositionablePoint.END_BUMPER) {
                        log.warn("Attempt to search beyond end of track");
                        return null;
                    }
                    if (p.getConnect1() == null) {
                        tTrack = p.getConnect2();
                    } else {
                        tTrack = p.getConnect1();
                    }
                } else {
                    log.warn("cNodeType wrong for cNode");
                }
                break;
            case LayoutTrack.TURNOUT_A: {
                if (cNode instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) cNode;
                    if ((lt.getTurnoutType() == LayoutTurnout.RH_TURNOUT)
                            || (lt.getTurnoutType() == LayoutTurnout.LH_TURNOUT)
                            || (lt.getTurnoutType() == LayoutTurnout.WYE_TURNOUT)) {
                        if ((lt.getLinkedTurnoutName() == null)
                                || (lt.getLinkedTurnoutName().isEmpty())) {
                            // Standard turnout - node type A
                            if (lt.getContinuingSense() == Turnout.CLOSED) {
                                switch (cNodeState) {
                                    case 0:
                                        tTrack = (TrackSegment) lt.getConnectB();
                                        pType = LayoutTrack.TURNOUT_B;
                                        break;
                                    case 1:
                                        tTrack = (TrackSegment) lt.getConnectC();
                                        pType = LayoutTrack.TURNOUT_C;
                                        break;
                                    default:
                                        log.error("Bad cNodeState argument when searching track-std. normal");
                                        return null;
                                }
                            } else {
                                switch (cNodeState) {
                                    case 0:
                                        tTrack = (TrackSegment) lt.getConnectC();
                                        pType = LayoutTrack.TURNOUT_C;
                                        break;
                                    case 1:
                                        tTrack = (TrackSegment) lt.getConnectB();
                                        pType = LayoutTrack.TURNOUT_B;
                                        break;
                                    default:
                                        log.error("Bad cNodeState argument when searching track-std reversed");
                                        return null;
                                }
                            }
                        } else {
                            // linked turnout - node type A
                            LayoutTurnout lto = layoutEditor.getFinder().findLayoutTurnoutByName(lt.getLinkedTurnoutName());
                            if (lt.getLinkType() == LayoutTurnout.THROAT_TO_THROAT) {
                                switch (cNodeState) {
                                    case 0:
                                        if (lto.getContinuingSense() == Turnout.CLOSED) {
                                            tTrack = (TrackSegment) lto.getConnectB();
                                            pType = LayoutTrack.TURNOUT_B;
                                        } else {
                                            tTrack = (TrackSegment) lto.getConnectC();
                                            pType = LayoutTrack.TURNOUT_C;
                                        }
                                        break;
                                    case 1:
                                        if (lto.getContinuingSense() == Turnout.CLOSED) {
                                            tTrack = (TrackSegment) lto.getConnectC();
                                            pType = LayoutTrack.TURNOUT_C;
                                        } else {
                                            tTrack = (TrackSegment) lto.getConnectB();
                                            pType = LayoutTrack.TURNOUT_B;
                                        }
                                        break;
                                    default:
                                        log.error("Bad cNodeState argument when searching track - THROAT_TO_THROAT");
                                        return null;
                                }
                                pObject = lto;
                            } else if (lt.getLinkType() == LayoutTurnout.FIRST_3_WAY) {
                                switch (cNodeState) {
                                    case 0:
                                        if (lto.getContinuingSense() == Turnout.CLOSED) {
                                            tTrack = (TrackSegment) lto.getConnectB();
                                            pType = LayoutTrack.TURNOUT_B;
                                        } else {
                                            tTrack = (TrackSegment) lto.getConnectC();
                                            pType = LayoutTrack.TURNOUT_C;
                                        }
                                        pObject = lto;
                                        break;
                                    case 1:
                                        if (lt.getContinuingSense() == Turnout.CLOSED) {
                                            tTrack = (TrackSegment) lt.getConnectC();
                                            pType = LayoutTrack.TURNOUT_C;
                                        } else {
                                            tTrack = (TrackSegment) lt.getConnectB();
                                            pType = LayoutTrack.TURNOUT_B;
                                        }
                                        break;
                                    case 2:
                                        if (lto.getContinuingSense() == Turnout.CLOSED) {
                                            tTrack = (TrackSegment) lto.getConnectC();
                                            pType = LayoutTrack.TURNOUT_C;
                                        } else {
                                            tTrack = (TrackSegment) lto.getConnectB();
                                            pType = LayoutTrack.TURNOUT_B;
                                        }
                                        pObject = lto;
                                        break;
                                    default:
                                        log.error("Bad cNodeState argument when searching track - FIRST_3_WAY");
                                        return null;
                                }
                            }
                        }
                    } else if ((lt.getTurnoutType() == LayoutTurnout.RH_XOVER)
                            || (lt.getTurnoutType() == LayoutTurnout.LH_XOVER)
                            || (lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)) {
                        // crossover turnout - node type A
                        switch (cNodeState) {
                            case 0:
                                tTrack = (TrackSegment) lt.getConnectB();
                                pType = LayoutTrack.TURNOUT_B;
                                break;
                            case 1:
                                if ((cNodeType == LayoutTrack.TURNOUT_A)
                                        && (!(lt.getTurnoutType() == LayoutTurnout.LH_XOVER))) {
                                    tTrack = (TrackSegment) lt.getConnectC();
                                    pType = LayoutTrack.TURNOUT_C;
                                } else {
                                    log.error("Request to follow not allowed switch setting at LH_XOVER or RH_OVER");
                                    return null;
                                }
                                break;
                            default:
                                log.error("Bad cNodeState argument when searching track- XOVER A");
                                return null;
                        }
                    }
                } else {
                    log.error("cNodeType wrong for cNode");
                }
                break;
            }
            case LayoutTrack.TURNOUT_B:
            case LayoutTrack.TURNOUT_C: {
                if (cNode instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) cNode;
                    if ((lt.getTurnoutType() == LayoutTurnout.RH_TURNOUT)
                            || (lt.getTurnoutType() == LayoutTurnout.LH_TURNOUT)
                            || (lt.getTurnoutType() == LayoutTurnout.WYE_TURNOUT)) {
                        if ((lt.getLinkedTurnoutName() == null)
                                || (lt.getLinkedTurnoutName().isEmpty())
                                || (lt.getLinkType() == LayoutTurnout.FIRST_3_WAY)) {
                            tTrack = (TrackSegment) (lt.getConnectA());
                            pType = LayoutTrack.TURNOUT_A;
                        } else {
                            LayoutTurnout lto = layoutEditor.getFinder().findLayoutTurnoutByName(lt.getLinkedTurnoutName());
                            if (lt.getLinkType() == LayoutTurnout.SECOND_3_WAY) {
                                tTrack = (TrackSegment) (lto.getConnectA());
                                pType = LayoutTrack.TURNOUT_A;
                            } else if (lt.getLinkType() == LayoutTurnout.THROAT_TO_THROAT) {
                                switch (cNodeState) {
                                    case 0:
                                        if (lto.getContinuingSense() == Turnout.CLOSED) {
                                            tTrack = (TrackSegment) lto.getConnectB();
                                            pType = LayoutTrack.TURNOUT_B;
                                        } else {
                                            tTrack = (TrackSegment) lto.getConnectC();
                                            pType = LayoutTrack.TURNOUT_C;
                                        }
                                        break;
                                    case 1:
                                        if (lto.getContinuingSense() == Turnout.CLOSED) {
                                            tTrack = (TrackSegment) lto.getConnectC();
                                            pType = LayoutTrack.TURNOUT_C;
                                        } else {
                                            tTrack = (TrackSegment) lto.getConnectB();
                                            pType = LayoutTrack.TURNOUT_B;
                                        }
                                        break;
                                    default:
                                        log.error("Bad cNodeState argument when searching track - THROAT_TO_THROAT - 2");
                                        return null;
                                }
                            }
                            pObject = lto;
                        }
                    } else if ((lt.getTurnoutType() == LayoutTurnout.RH_XOVER)
                            || (lt.getTurnoutType() == LayoutTurnout.LH_XOVER)
                            || (lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)) {
                        switch (cNodeState) {
                            case 0:
                                if (cNodeType == LayoutTrack.TURNOUT_B) {
                                    tTrack = (TrackSegment) lt.getConnectA();
                                    pType = LayoutTrack.TURNOUT_A;
                                } else if (cNodeType == LayoutTrack.TURNOUT_C) {
                                    tTrack = (TrackSegment) lt.getConnectD();
                                    pType = LayoutTrack.TURNOUT_D;
                                }
                                break;
                            case 1:
                                if ((cNodeType == LayoutTrack.TURNOUT_C)
                                        && (!(lt.getTurnoutType() == LayoutTurnout.LH_XOVER))) {
                                    tTrack = (TrackSegment) lt.getConnectA();
                                    pType = LayoutTrack.TURNOUT_A;
                                } else if ((cNodeType == LayoutTrack.TURNOUT_B)
                                        && (!(lt.getTurnoutType() == LayoutTurnout.RH_XOVER))) {
                                    tTrack = (TrackSegment) lt.getConnectD();
                                    pType = LayoutTrack.TURNOUT_D;
                                } else {
                                    log.error("Request to follow not allowed switch setting at LH_XOVER or RH_OVER");
                                    return null;
                                }
                                break;
                            default:
                                log.error("Bad cNodeState argument when searching track - XOVER B or C");
                                return null;
                        }
                    }
                } else {
                    log.error("cNodeType wrong for cNode");
                }
                break;
            }
            case LayoutTrack.TURNOUT_D: {
                if (cNode instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) cNode;
                    if ((lt.getTurnoutType() == LayoutTurnout.RH_XOVER)
                            || (lt.getTurnoutType() == LayoutTurnout.LH_XOVER)
                            || (lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)) {
                        switch (cNodeState) {
                            case 0:
                                tTrack = (TrackSegment) lt.getConnectC();
                                pType = LayoutTrack.TURNOUT_C;
                                break;
                            case 1:
                                if (!(lt.getTurnoutType() == LayoutTurnout.RH_XOVER)) {
                                    tTrack = (TrackSegment) lt.getConnectB();
                                    pType = LayoutTrack.TURNOUT_B;
                                } else {
                                    log.error("Request to follow not allowed switch setting at LH_XOVER or RH_OVER");
                                    return null;
                                }
                                break;
                            default:
                                log.error("Bad cNodeState argument when searching track - XOVER D");
                                return null;
                        }
                    } else {
                        log.error("Bad traak node type - TURNOUT_D, but not a crossover turnout");
                        return null;
                    }
                } else {
                    log.error("cNodeType wrong for cNode");
                }
                break;
            }
            case LayoutTrack.LEVEL_XING_A:
                if (cNode instanceof LevelXing) {
                    tTrack = (TrackSegment) ((LevelXing) cNode).getConnectC();
                    pType = LayoutTrack.LEVEL_XING_C;
                } else {
                    log.error("cNodeType wrong for cNode");
                }
                break;
            case LayoutTrack.LEVEL_XING_B:
                if (cNode instanceof LevelXing) {
                    tTrack = (TrackSegment) ((LevelXing) cNode).getConnectD();
                    pType = LayoutTrack.LEVEL_XING_D;
                } else {
                    log.error("cNodeType wrong for cNode");
                }
                break;
            case LayoutTrack.LEVEL_XING_C:
                if (cNode instanceof LevelXing) {
                    tTrack = (TrackSegment) ((LevelXing) cNode).getConnectA();
                    pType = LayoutTrack.LEVEL_XING_A;
                } else {
                    log.error("cNodeType wrong for cNode");
                }
                break;
            case LayoutTrack.LEVEL_XING_D:
                if (cNode instanceof LevelXing) {
                    tTrack = (TrackSegment) ((LevelXing) cNode).getConnectB();
                    pType = LayoutTrack.LEVEL_XING_B;
                } else {
                    log.error("cNodeType wrong for cNode");
                }
                break;
            case LayoutTrack.SLIP_A: {
                if (cNode instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) cNode;
                    if (cNodeState == 0) {
                        tTrack = (TrackSegment) lt.getConnectC();
                        pType = LayoutTrack.SLIP_C;
                    } else if (cNodeState == 1) {
                        tTrack = (TrackSegment) lt.getConnectD();
                        pType = LayoutTrack.SLIP_D;
                    }
                } else {
                    log.error("cNodeType wrong for cNode");
                }
                break;
            }
            case LayoutTrack.SLIP_B: {
                if (cNode instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) cNode;
                    if (cNodeState == 0) {
                        tTrack = (TrackSegment) lt.getConnectD();
                        pType = LayoutTrack.SLIP_D;
                    } else if (cNodeState == 1 && (lt.getTurnoutType() == LayoutTurnout.DOUBLE_SLIP)) {
                        tTrack = (TrackSegment) lt.getConnectC();
                        pType = LayoutTrack.SLIP_C;
                    } else {
                        log.error("Request to follow not allowed on a single slip");
                        return null;
                    }
                } else {
                    log.error("cNodeType wrong for cNode");
                }
                break;
            }
            case LayoutTrack.SLIP_C: {
                if (cNode instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) cNode;
                    if (cNodeState == 0) {
                        tTrack = (TrackSegment) lt.getConnectA();
                        pType = LayoutTrack.SLIP_A;
                    } else if (cNodeState == 1 && (lt.getTurnoutType() == LayoutTurnout.DOUBLE_SLIP)) {
                        tTrack = (TrackSegment) lt.getConnectB();
                        pType = LayoutTrack.SLIP_B;
                    } else {
                        log.error("Request to follow not allowed on a single slip");
                        return null;
                    }
                } else {
                    log.error("cNodeType wrong for cNode");
                }
                break;
            }
            case LayoutTrack.SLIP_D: {
                if (cNode instanceof LayoutTurnout) {
                    LayoutTurnout lt = (LayoutTurnout) cNode;
                    if (cNodeState == 0) {
                        tTrack = (TrackSegment) lt.getConnectB();
                        pType = LayoutTrack.SLIP_B;
                    } else if (cNodeState == 1) {
                        tTrack = (TrackSegment) lt.getConnectA();
                        pType = LayoutTrack.SLIP_A;
                    }
                } else {
                    log.error("cNodeType wrong for cNode");
                }
                break;
            }
            default:
                log.error("Unable to initiate 'getTrackNode'.  Probably bad input Track Node.");
                return null;
        }

        // follow track to anchor block boundary, turnout, or level crossing
        boolean hasNode = false;
        LayoutTrack tObject;
        int tType;
        if (tTrack == null) {
            log.error("Error tTrack is null!");
            return null;
        }
        while (!hasNode) {
            if (tTrack.getConnect1() == pObject) {
                tObject = tTrack.getConnect2();
                tType = tTrack.getType2();
            } else {
                tObject = tTrack.getConnect1();
                tType = tTrack.getType1();
            }
            if (tObject == null) {
                log.error("Error while following track looking for next node");
                return null;
            }
            if (tType != LayoutTrack.POS_POINT) {
                node = tObject;
                nodeType = tType;
                track = tTrack;
                hasNode = true;
            } else {
                PositionablePoint p = (PositionablePoint) tObject;
                if (p.getType() == PositionablePoint.END_BUMPER) {
                    hitEnd = true;
                    hasNode = true;
                } else {
                    TrackSegment con1 = p.getConnect1();
                    TrackSegment con2 = p.getConnect2();
                    if ((con1 == null) || (con2 == null)) {
                        log.error("Breakin connectivity at Anchor Point when searching for track node");
                        return null;
                    }
                    if (con1.getLayoutBlock() != con2.getLayoutBlock()) {
                        node = tObject;
                        nodeType = LayoutTrack.POS_POINT;
                        track = tTrack;
                        hasNode = true;
                    } else {
                        if (con1 == tTrack) {
                            tTrack = con2;
                        } else {
                            tTrack = con1;
                        }
                        pObject = tObject;
                    }
                }
            }
        }
        return (new TrackNode(node, nodeType, track, hitEnd, cNodeState));
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
            case LayoutTrack.POS_POINT:
                PositionablePoint p = (PositionablePoint) node.getNode();
                block = p.getConnect1().getLayoutBlock().getBlock();
                if (block == node.getTrackSegment().getLayoutBlock().getBlock()) {
                    block = p.getConnect2().getLayoutBlock().getBlock();
                }
                break;
            case LayoutTrack.TURNOUT_A:
                LayoutTurnout lt = (LayoutTurnout) node.getNode();
                Block tBlock = ((TrackSegment) lt.getConnectB()).getLayoutBlock().getBlock();
                if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                        && (tBlock != excludedBlock)) {
                    block = tBlock;
                } else if (lt.getTurnoutType() != LayoutTurnout.LH_XOVER) {
                    tBlock = ((TrackSegment) lt.getConnectC()).getLayoutBlock().getBlock();
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    }
                }
                break;
            case LayoutTrack.TURNOUT_B:
                lt = (LayoutTurnout) node.getNode();
                tBlock = ((TrackSegment) lt.getConnectA()).getLayoutBlock().getBlock();
                if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                        && (tBlock != excludedBlock)) {
                    block = tBlock;
                } else if ((lt.getTurnoutType() == LayoutTurnout.LH_XOVER)
                        || (lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)) {
                    tBlock = ((TrackSegment) lt.getConnectD()).getLayoutBlock().getBlock();
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    }
                }
                break;
            case LayoutTrack.TURNOUT_C:
                lt = (LayoutTurnout) node.getNode();
                if (lt.getTurnoutType() != LayoutTurnout.LH_XOVER) {
                    tBlock = ((TrackSegment) lt.getConnectA()).getLayoutBlock().getBlock();
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    }
                }
                if ((block == null) && ((lt.getTurnoutType() == LayoutTurnout.LH_XOVER)
                        || (lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER))) {
                    tBlock = ((TrackSegment) lt.getConnectD()).getLayoutBlock().getBlock();
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    }
                }
                break;
            case LayoutTrack.TURNOUT_D:
                lt = (LayoutTurnout) node.getNode();
                if ((lt.getTurnoutType() == LayoutTurnout.LH_XOVER)
                        || (lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)) {
                    tBlock = ((TrackSegment) lt.getConnectB()).getLayoutBlock().getBlock();
                    if ((tBlock != node.getTrackSegment().getLayoutBlock().getBlock())
                            && (tBlock != excludedBlock)) {
                        block = tBlock;
                    }
                }
                break;
            case LayoutTrack.LEVEL_XING_A:
                LevelXing x = (LevelXing) node.getNode();
                tBlock = ((TrackSegment) x.getConnectC()).getLayoutBlock().getBlock();
                if (tBlock != node.getTrackSegment().getLayoutBlock().getBlock()) {
                    block = tBlock;
                }
                break;
            case LayoutTrack.LEVEL_XING_B:
                x = (LevelXing) node.getNode();
                tBlock = ((TrackSegment) x.getConnectD()).getLayoutBlock().getBlock();
                if (tBlock != node.getTrackSegment().getLayoutBlock().getBlock()) {
                    block = tBlock;
                }
                break;
            case LayoutTrack.LEVEL_XING_C:
                x = (LevelXing) node.getNode();
                tBlock = ((TrackSegment) x.getConnectA()).getLayoutBlock().getBlock();
                if (tBlock != node.getTrackSegment().getLayoutBlock().getBlock()) {
                    block = tBlock;
                }
                break;
            case LayoutTrack.LEVEL_XING_D:
                x = (LevelXing) node.getNode();
                tBlock = ((TrackSegment) x.getConnectB()).getLayoutBlock().getBlock();
                if (tBlock != node.getTrackSegment().getLayoutBlock().getBlock()) {
                    block = tBlock;
                }
                break;
            case LayoutTrack.SLIP_A:
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
            case LayoutTrack.SLIP_B:
                ls = (LayoutSlip) node.getNode();
                tBlock = ((TrackSegment) ls.getConnectD()).getLayoutBlock().getBlock();
                if (ls.getTurnoutType() == LayoutSlip.DOUBLE_SLIP) {
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
            case LayoutTrack.SLIP_C:
                ls = (LayoutSlip) node.getNode();
                tBlock = ((TrackSegment) ls.getConnectA()).getLayoutBlock().getBlock();
                if (ls.getTurnoutType() == LayoutSlip.DOUBLE_SLIP) {
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
            case LayoutTrack.SLIP_D:
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
            @Nonnull LayoutTurnout layoutTurnout, int cType, boolean suppress) {
        prevConnectTrack = layoutTurnout;
        int setting = Turnout.THROWN;
        int tType = layoutTurnout.getTurnoutType();
        if (layoutTurnout instanceof LayoutSlip) {
            setting = LayoutSlip.UNKNOWN;
            LayoutSlip layoutSlip = (LayoutSlip) layoutTurnout;
            tType = layoutSlip.getTurnoutType();
            LayoutBlock layoutBlockA = ((TrackSegment) layoutSlip.getConnectA()).getLayoutBlock();
            LayoutBlock layoutBlockB = ((TrackSegment) layoutSlip.getConnectB()).getLayoutBlock();
            LayoutBlock layoutBlockC = ((TrackSegment) layoutSlip.getConnectC()).getLayoutBlock();
            LayoutBlock layoutBlockD = ((TrackSegment) layoutSlip.getConnectD()).getLayoutBlock();
            switch (cType) {
                case LayoutTrack.SLIP_A:
                    if (nextLayoutBlock == layoutBlockC) {
                        // exiting block at C
                        prevConnectType = LayoutTrack.SLIP_C;
                        setting = LayoutSlip.STATE_AC;
                        trackSegment = (TrackSegment) layoutSlip.getConnectC();
                    } else if (nextLayoutBlock == layoutBlockD) {
                        // exiting block at D
                        prevConnectType = LayoutTrack.SLIP_D;
                        setting = LayoutSlip.STATE_AD;
                        trackSegment = (TrackSegment) layoutSlip.getConnectD();
                    } else if (currLayoutBlock == layoutBlockC
                            && currLayoutBlock != layoutBlockD) {
                        // block continues at C only
                        trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                        setting = LayoutSlip.STATE_AC;
                        prevConnectType = LayoutTrack.SLIP_C;

                    } else if (currLayoutBlock == layoutBlockD
                            && currLayoutBlock != layoutBlockC) {
                        // block continues at D only
                        setting = LayoutSlip.STATE_AD;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        prevConnectType = LayoutTrack.SLIP_D;
                    } else { // both connecting track segments continue in current block, must search further
                        if ((layoutSlip.getConnectC() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectC(), layoutSlip)) {
                            prevConnectType = LayoutTrack.SLIP_C;
                            setting = LayoutSlip.STATE_AC;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                        } else if ((layoutSlip.getConnectD() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectD(), layoutSlip)) {
                            prevConnectType = LayoutTrack.SLIP_D;
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
                case LayoutTrack.SLIP_B:
                    if (nextLayoutBlock == layoutBlockD) {
                        // exiting block at D
                        prevConnectType = LayoutTrack.SLIP_D;
                        setting = LayoutSlip.STATE_BD;
                        trackSegment = (TrackSegment) layoutSlip.getConnectD();
                    } else if (nextLayoutBlock == layoutBlockC
                            && tType == LayoutSlip.DOUBLE_SLIP) {
                        // exiting block at C
                        prevConnectType = LayoutTrack.SLIP_C;
                        setting = LayoutSlip.STATE_BC;
                        trackSegment = (TrackSegment) layoutSlip.getConnectC();
                    } else {
                        if (tType == LayoutSlip.DOUBLE_SLIP) {
                            if (currLayoutBlock == layoutBlockD
                                    && currLayoutBlock != layoutBlockC) {
                                //Found continuing at D only
                                trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                                setting = LayoutSlip.STATE_BD;
                                prevConnectType = LayoutTrack.SLIP_D;

                            } else if (currLayoutBlock == layoutBlockC
                                    && currLayoutBlock != layoutBlockD) {
                                //Found continuing at C only
                                trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                                setting = LayoutSlip.STATE_BC;
                                prevConnectType = LayoutTrack.SLIP_C;
                            } else { // both connecting track segments continue in current block, must search further
                                if ((layoutSlip.getConnectD() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectD(), layoutSlip)) {
                                    prevConnectType = LayoutTrack.SLIP_D;
                                    setting = LayoutSlip.STATE_BD;
                                    trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                                } else if ((layoutSlip.getConnectC() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectC(), layoutSlip)) {
                                    prevConnectType = LayoutTrack.SLIP_C;
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
                                prevConnectType = LayoutTrack.SLIP_D;
                            } else {
                                trackSegment = null;
                            }
                        }
                    }
                    break;
                case LayoutTrack.SLIP_C:
                    if (nextLayoutBlock == layoutBlockA) {
                        // exiting block at A
                        prevConnectType = LayoutTrack.SLIP_A;
                        setting = LayoutSlip.STATE_AC;
                        trackSegment = (TrackSegment) layoutSlip.getConnectA();
                    } else if (nextLayoutBlock == layoutBlockB
                            && tType == LayoutSlip.DOUBLE_SLIP) {
                        // exiting block at B
                        prevConnectType = LayoutTrack.SLIP_B;
                        setting = LayoutSlip.STATE_BC;
                        trackSegment = (TrackSegment) layoutSlip.getConnectB();
                    } else {
                        if (tType == LayoutSlip.DOUBLE_SLIP) {
                            if (currLayoutBlock == layoutBlockA
                                    && currLayoutBlock != layoutBlockB) {
                                //Found continuing at A only
                                trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                                setting = LayoutSlip.STATE_AC;
                                prevConnectType = LayoutTrack.SLIP_A;
                            } else if (currLayoutBlock == layoutBlockB
                                    && currLayoutBlock != layoutBlockA) {
                                //Found continuing at B only
                                trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                                setting = LayoutSlip.STATE_BC;
                                prevConnectType = LayoutTrack.SLIP_B;
                            } else { // both connecting track segments continue in current block, must search further
                                if ((layoutSlip.getConnectA() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectA(), layoutSlip)) {
                                    prevConnectType = LayoutTrack.SLIP_A;
                                    setting = LayoutSlip.STATE_AC;
                                    trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                                } else if ((layoutSlip.getConnectB() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectB(), layoutSlip)) {
                                    prevConnectType = LayoutTrack.SLIP_B;
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
                                prevConnectType = LayoutTrack.SLIP_A;
                            } else {
                                trackSegment = null;
                            }
                        }
                    }
                    break;
                case LayoutTrack.SLIP_D:
                    if (nextLayoutBlock == layoutBlockB) {
                        // exiting block at B
                        prevConnectType = LayoutTrack.SLIP_B;
                        setting = LayoutSlip.STATE_BD;
                        trackSegment = (TrackSegment) layoutSlip.getConnectB();
                    } else if (nextLayoutBlock == layoutBlockA) {
                        // exiting block at B
                        prevConnectType = LayoutTrack.SLIP_A;
                        setting = LayoutSlip.STATE_AD;
                        trackSegment = (TrackSegment) layoutSlip.getConnectA();
                    } else if (currLayoutBlock == layoutBlockB
                            && currLayoutBlock != layoutBlockA) {
                        //Found continuing at B only
                        trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                        setting = LayoutSlip.STATE_BD;
                        prevConnectType = LayoutTrack.SLIP_B;

                    } else if (currLayoutBlock == layoutBlockA
                            && currLayoutBlock != layoutBlockB) {
                        //Found continuing at A only
                        setting = LayoutSlip.STATE_AD;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                        prevConnectType = LayoutTrack.SLIP_A;
                    } else { // both connecting track segments continue in current block, must search further
                        if ((layoutSlip.getConnectA() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectA(), layoutSlip)) {
                            prevConnectType = LayoutTrack.SLIP_A;
                            setting = LayoutSlip.STATE_AD;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                        } else if ((layoutSlip.getConnectB() != null) && trackSegmentLeadsTo((TrackSegment) layoutSlip.getConnectB(), layoutSlip)) {
                            prevConnectType = LayoutTrack.SLIP_B;
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
                    log.warn("Connectivity not complete at " + layoutSlip.getDisplayName());
                }
                turnoutConnectivity = false;
            }
        } else {
            switch (cType) {
                case LayoutTrack.TURNOUT_A:
                    // check for left-handed crossover
                    if (tType == LayoutTurnout.LH_XOVER) {
                        // entering at a continuing track of a left-handed crossover
                        prevConnectType = LayoutTrack.TURNOUT_B;
                        setting = Turnout.CLOSED;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                    } // entering at a throat, determine exit by checking block of connected track segment
                    else if ((nextLayoutBlock == layoutTurnout.getLayoutBlockB()) || ((layoutTurnout.getConnectB() != null)
                            && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectB()).getLayoutBlock()))) {
                        // exiting block at continuing track
                        prevConnectType = LayoutTrack.TURNOUT_B;
                        setting = Turnout.CLOSED;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                    } else if ((nextLayoutBlock == layoutTurnout.getLayoutBlockC()) || ((layoutTurnout.getConnectC() != null)
                            && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectC()).getLayoutBlock()))) {
                        // exiting block at diverging track
                        prevConnectType = LayoutTrack.TURNOUT_C;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                    } // must stay in block after turnout - check if only one track continues in block
                    else if ((layoutTurnout.getConnectB() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectB()).getLayoutBlock())
                            && (layoutTurnout.getConnectC() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectC()).getLayoutBlock())) {
                        // continuing in block on continuing track only
                        prevConnectType = LayoutTrack.TURNOUT_B;
                        setting = Turnout.CLOSED;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                    } else if ((layoutTurnout.getConnectC() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectC()).getLayoutBlock())
                            && (layoutTurnout.getConnectB() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectB()).getLayoutBlock())) {
                        // continuing in block on diverging track only
                        prevConnectType = LayoutTrack.TURNOUT_C;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                    } else { // both connecting track segments continue in current block, must search further
                        // check if continuing track leads to the next block
                        if ((layoutTurnout.getConnectB() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectB(), layoutTurnout)) {
                            prevConnectType = LayoutTrack.TURNOUT_B;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                        } // check if diverging track leads to the next block
                        else if ((layoutTurnout.getConnectC() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectC(), layoutTurnout)) {
                            prevConnectType = LayoutTrack.TURNOUT_C;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                        } else {
                            if (!suppress) {
                                log.warn("Neither branch at {} leads to next Block {}", layoutTurnout, nextLayoutBlock);
                            }
                            trackSegment = null;
                        }
                    }
                    break;
                case LayoutTrack.TURNOUT_B:
                    if ((tType == LayoutTurnout.LH_XOVER) || (tType == LayoutTurnout.DOUBLE_XOVER)) {
                        // entering at a throat of a double crossover or a left-handed crossover
                        if ((nextLayoutBlock == layoutTurnout.getLayoutBlock()) || ((layoutTurnout.getConnectA() != null)
                                && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectA()).getLayoutBlock()))) {
                            // exiting block at continuing track
                            prevConnectType = LayoutTrack.TURNOUT_A;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                        } else if ((nextLayoutBlock == layoutTurnout.getLayoutBlockD()) || ((layoutTurnout.getConnectD() != null)
                                && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectD()).getLayoutBlock()))) {
                            // exiting block at diverging track
                            prevConnectType = LayoutTrack.TURNOUT_D;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        } // must stay in block after turnout
                        else if (((layoutTurnout.getConnectA() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectA()).getLayoutBlock()))
                                && ((layoutTurnout.getConnectD() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectD()).getLayoutBlock()))) {
                            // continuing in block on continuing track only
                            prevConnectType = LayoutTrack.TURNOUT_A;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                        } else if (((layoutTurnout.getConnectD() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectD()).getLayoutBlock()))
                                && ((layoutTurnout.getConnectA() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectA()).getLayoutBlock()))) {
                            // continuing in block on diverging track only
                            prevConnectType = LayoutTrack.TURNOUT_D;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        } else { // both connecting track segments continue in current block, must search further
                            // check if continuing track leads to the next block
                            if ((layoutTurnout.getConnectA() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectA(), layoutTurnout)) {
                                prevConnectType = LayoutTrack.TURNOUT_A;
                                setting = Turnout.CLOSED;
                                trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                            } // check if diverging track leads to the next block
                            else if ((layoutTurnout.getConnectD() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectD(), layoutTurnout)) {
                                prevConnectType = LayoutTrack.TURNOUT_D;
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
                        prevConnectType = LayoutTrack.TURNOUT_A;
                        setting = Turnout.CLOSED;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                    }
                    break;
                case LayoutTrack.TURNOUT_C:
                    if ((tType == LayoutTurnout.RH_XOVER) || (tType == LayoutTurnout.DOUBLE_XOVER)) {
                        // entering at a throat of a double crossover or a right-handed crossover
                        if ((nextLayoutBlock == layoutTurnout.getLayoutBlockD()) || ((layoutTurnout.getConnectD() != null)
                                && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectD()).getLayoutBlock()))) {
                            // exiting block at continuing track
                            prevConnectType = LayoutTrack.TURNOUT_D;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        } else if ((nextLayoutBlock == layoutTurnout.getLayoutBlock()) || ((layoutTurnout.getConnectA() != null)
                                && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectA()).getLayoutBlock()))) {
                            // exiting block at diverging track
                            prevConnectType = LayoutTrack.TURNOUT_A;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                        } // must stay in block after turnout
                        else if (((layoutTurnout.getConnectD() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectD()).getLayoutBlock()))
                                && ((layoutTurnout.getConnectA() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectA()).getLayoutBlock()))) {
                            // continuing in block on continuing track
                            prevConnectType = LayoutTrack.TURNOUT_D;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        } else if (((layoutTurnout.getConnectA() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectA()).getLayoutBlock()))
                                && ((layoutTurnout.getConnectD() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectD()).getLayoutBlock()))) {
                            // continuing in block on diverging track
                            prevConnectType = LayoutTrack.TURNOUT_A;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                        } else { // both connecting track segments continue in current block, must search further
                            // check if continuing track leads to the next block
                            if ((layoutTurnout.getConnectD() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectD(), layoutTurnout)) {
                                prevConnectType = LayoutTrack.TURNOUT_D;
                                setting = Turnout.CLOSED;
                                trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                            } // check if diverging track leads to the next block
                            else if ((layoutTurnout.getConnectA() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectA(), layoutTurnout)) {
                                prevConnectType = LayoutTrack.TURNOUT_A;
                                trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                            } else {
                                if (!suppress) {
                                    log.warn("Neither branch at {} leads to next Block {}", layoutTurnout, nextLayoutBlock);
                                }
                                trackSegment = null;
                            }
                        }
                    } else if (tType == LayoutTurnout.LH_XOVER) {
                        // entering at continuing track, must exit at throat
                        prevConnectType = LayoutTrack.TURNOUT_D;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectD();
                        setting = Turnout.CLOSED;
                    } else {
                        // entering at diverging track, must exit at throat
                        prevConnectType = LayoutTrack.TURNOUT_A;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                    }
                    break;
                case LayoutTrack.TURNOUT_D:
                    if ((tType == LayoutTurnout.LH_XOVER) || (tType == LayoutTurnout.DOUBLE_XOVER)) {
                        // entering at a throat of a double crossover or a left-handed crossover
                        if ((nextLayoutBlock == layoutTurnout.getLayoutBlockC()) || ((layoutTurnout.getConnectC() != null)
                                && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectC()).getLayoutBlock()))) {
                            // exiting block at continuing track
                            prevConnectType = LayoutTrack.TURNOUT_C;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                        } else if ((nextLayoutBlock == layoutTurnout.getLayoutBlockB()) || ((layoutTurnout.getConnectB() != null)
                                && (nextLayoutBlock == ((TrackSegment) layoutTurnout.getConnectB()).getLayoutBlock()))) {
                            // exiting block at diverging track
                            prevConnectType = LayoutTrack.TURNOUT_B;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                        } // must stay in block after turnout
                        else if (((layoutTurnout.getConnectC() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectC()).getLayoutBlock()))
                                && ((layoutTurnout.getConnectB() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectB()).getLayoutBlock()))) {
                            // continuing in block on continuing track
                            prevConnectType = LayoutTrack.TURNOUT_C;
                            setting = Turnout.CLOSED;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                        } else if (((layoutTurnout.getConnectB() != null) && (currLayoutBlock == ((TrackSegment) layoutTurnout.getConnectB()).getLayoutBlock()))
                                && ((layoutTurnout.getConnectC() != null) && (currLayoutBlock != ((TrackSegment) layoutTurnout.getConnectC()).getLayoutBlock()))) {
                            // continuing in block on diverging track
                            prevConnectType = LayoutTrack.TURNOUT_B;
                            trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                        } else { // both connecting track segments continue in current block, must search further
                            // check if continuing track leads to the next block
                            if ((layoutTurnout.getConnectC() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectC(), layoutTurnout)) {
                                prevConnectType = LayoutTrack.TURNOUT_C;
                                setting = Turnout.CLOSED;
                                trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                            } // check if diverging track leads to the next block
                            else if ((layoutTurnout.getConnectB() != null) && trackSegmentLeadsTo((TrackSegment) layoutTurnout.getConnectB(), layoutTurnout)) {
                                prevConnectType = LayoutTrack.TURNOUT_B;
                                trackSegment = (TrackSegment) layoutTurnout.getConnectB();
                            } else {
                                if (!suppress) {
                                    log.warn("Neither branch at {} leads to next Block {}", layoutTurnout, nextLayoutBlock);
                                }
                                trackSegment = null;
                            }
                        }
                    } else if (tType == LayoutTurnout.RH_XOVER) {
                        // entering at through track of a right-handed crossover, must exit at throat
                        prevConnectType = LayoutTrack.TURNOUT_C;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectC();
                        setting = Turnout.CLOSED;
                    } else {
                        // entering at diverging track of a right-handed crossover, must exit at throat
                        prevConnectType = LayoutTrack.TURNOUT_A;
                        trackSegment = (TrackSegment) layoutTurnout.getConnectA();
                    }
                    break;
                default: {
                    log.warn("getTurnoutSetting() unknown cType: " + cType);
                    break;
                }
            }   // switch (cType)

            if ((trackSegment != null) && (trackSegment.getLayoutBlock() != currLayoutBlock)) {
                // continuing track segment is not in this block
                trackSegment = null;
            } else if (trackSegment == null) {
                if (!suppress) {
                    log.warn("Connectivity not complete at " + layoutTurnout.getTurnoutName());
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

        int conType;
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
                            connectionTypeToString(conType),
                            nextLayoutBlock.getId());
                }

                // follow track according to next destination type
                // this is a positionable point
                if (conType == LayoutTrack.POS_POINT) {
                    // reached anchor point or end bumper
                    if (((PositionablePoint) conLayoutTrack).getType() == PositionablePoint.END_BUMPER) {
                        // end of line without reaching 'nextLayoutBlock'
                        if (log.isDebugEnabled()) {
                            log.info("end of line without reaching {}", nextLayoutBlock.getId());
                        }
                        curTrackSegment = null;
                    } else if (((PositionablePoint) conLayoutTrack).getType() == PositionablePoint.ANCHOR
                            || ((PositionablePoint) conLayoutTrack).getType() == PositionablePoint.EDGE_CONNECTOR) {
                        // proceed to next track segment if within the same Block
                        if (((PositionablePoint) conLayoutTrack).getConnect1() == curTrackSegment) {
                            curTrackSegment = (((PositionablePoint) conLayoutTrack).getConnect2());
                        } else {
                            curTrackSegment = (((PositionablePoint) conLayoutTrack).getConnect1());
                        }
                        curLayoutTrack = conLayoutTrack;
                    }
                } else if ((conType >= LayoutTrack.LEVEL_XING_A) && (conType <= LayoutTrack.LEVEL_XING_D)) {
                    // reached a level crossing
                    if ((conType == LayoutTrack.LEVEL_XING_A) || (conType == LayoutTrack.LEVEL_XING_C)) {
                        if (((LevelXing) conLayoutTrack).getLayoutBlockAC() != currLayoutBlock) {
                            if (((LevelXing) conLayoutTrack).getLayoutBlockAC() == nextLayoutBlock) {
                                return true;
                            } else {
                                curTrackSegment = null;
                            }
                        } else if (conType == LayoutTrack.LEVEL_XING_A) {
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
                        } else if (conType == LayoutTrack.LEVEL_XING_B) {
                            curTrackSegment = (TrackSegment) ((LevelXing) conLayoutTrack).getConnectD();
                        } else {
                            curTrackSegment = (TrackSegment) ((LevelXing) conLayoutTrack).getConnectB();
                        }
                    }
                    curLayoutTrack = conLayoutTrack;
                } else if ((conType >= LayoutTrack.TURNOUT_A) && (conType <= LayoutTrack.TURNOUT_D)) {
                    // reached a turnout
                    LayoutTurnout lt = (LayoutTurnout) conLayoutTrack;
                    int tType = lt.getTurnoutType();

                    // RH, LH or DOUBLE _XOVER
                    if ((tType == LayoutTurnout.DOUBLE_XOVER) || (tType == LayoutTurnout.RH_XOVER)
                            || (tType == LayoutTurnout.LH_XOVER)) {
                        // reached a crossover turnout
                        switch (conType) {
                            case LayoutTrack.TURNOUT_A:
                                if ((lt.getLayoutBlock()) != currLayoutBlock) {
                                    if (lt.getLayoutBlock() == nextLayoutBlock) {
                                        return true;
                                    } else {
                                        curTrackSegment = null;
                                    }
                                } else if ((lt.getLayoutBlockB() == nextLayoutBlock) || ((tType != LayoutTurnout.LH_XOVER)
                                        && (lt.getLayoutBlockC() == nextLayoutBlock))) {
                                    return true;
                                } else if (lt.getLayoutBlockB() == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) lt.getConnectB();
                                    if ((tType != LayoutTurnout.LH_XOVER) && (lt.getLayoutBlockC() == currLayoutBlock)) {
                                        postTrackSegments.add((TrackSegment) lt.getConnectC());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else if ((tType != LayoutTurnout.LH_XOVER) && (lt.getLayoutBlockC() == currLayoutBlock)) {
                                    curTrackSegment = (TrackSegment) lt.getConnectC();
                                } else {
                                    curTrackSegment = null;
                                }
                                break;
                            case LayoutTrack.TURNOUT_B:
                                if ((lt.getLayoutBlockB()) != currLayoutBlock) {
                                    if (lt.getLayoutBlockB() == nextLayoutBlock) {
                                        return true;
                                    } else {
                                        curTrackSegment = null;
                                    }
                                } else if ((lt.getLayoutBlock() == nextLayoutBlock) || ((tType != LayoutTurnout.RH_XOVER)
                                        && (lt.getLayoutBlockD() == nextLayoutBlock))) {
                                    return true;
                                } else if (lt.getLayoutBlock() == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) lt.getConnectA();
                                    if ((tType != LayoutTurnout.RH_XOVER) && (lt.getLayoutBlockD() == currLayoutBlock)) {
                                        postTrackSegments.add((TrackSegment) lt.getConnectD());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else if ((tType != LayoutTurnout.RH_XOVER) && (lt.getLayoutBlockD() == currLayoutBlock)) {
                                    curTrackSegment = (TrackSegment) lt.getConnectD();
                                } else {
                                    curTrackSegment = null;
                                }
                                break;
                            case LayoutTrack.TURNOUT_C:
                                if ((lt.getLayoutBlockC()) != currLayoutBlock) {
                                    if (lt.getLayoutBlockC() == nextLayoutBlock) {
                                        return true;
                                    } else {
                                        curTrackSegment = null;
                                    }
                                } else if ((lt.getLayoutBlockD() == nextLayoutBlock) || ((tType != LayoutTurnout.LH_XOVER)
                                        && (lt.getLayoutBlock() == nextLayoutBlock))) {
                                    return true;
                                } else if (lt.getLayoutBlockD() == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) lt.getConnectD();
                                    if ((tType != LayoutTurnout.LH_XOVER) && (lt.getLayoutBlock() == currLayoutBlock)) {
                                        postTrackSegments.add((TrackSegment) lt.getConnectA());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else if ((tType != LayoutTurnout.LH_XOVER) && (lt.getLayoutBlock() == currLayoutBlock)) {
                                    curTrackSegment = (TrackSegment) lt.getConnectA();
                                } else {
                                    curTrackSegment = null;
                                }
                                break;
                            case LayoutTrack.TURNOUT_D:
                                if ((lt.getLayoutBlockD()) != currLayoutBlock) {
                                    if (lt.getLayoutBlockD() == nextLayoutBlock) {
                                        return true;
                                    } else {
                                        curTrackSegment = null;
                                    }
                                } else if ((lt.getLayoutBlockC() == nextLayoutBlock) || ((tType != LayoutTurnout.RH_XOVER)
                                        && (lt.getLayoutBlockB() == nextLayoutBlock))) {
                                    return true;
                                } else if (lt.getLayoutBlockC() == currLayoutBlock) {
                                    curTrackSegment = (TrackSegment) lt.getConnectC();
                                    if ((tType != LayoutTurnout.RH_XOVER) && (lt.getLayoutBlockB() == currLayoutBlock)) {
                                        postTrackSegments.add((TrackSegment) lt.getConnectB());
                                        postLayoutTracks.add(conLayoutTrack);
                                    }
                                } else if ((tType != LayoutTurnout.RH_XOVER) && (lt.getLayoutBlockB() == currLayoutBlock)) {
                                    curTrackSegment = (TrackSegment) lt.getConnectB();
                                } else {
                                    curTrackSegment = null;
                                }
                                break;
                            default: {
                                log.warn("trackSegmentLeadsTo() unknown conType: " + conType);
                                break;
                            }
                        }   // switch (conType)
                        curLayoutTrack = conLayoutTrack;
                    } else // if RH, LH or DOUBLE _XOVER
                    if ((tType == LayoutTurnout.RH_TURNOUT) || (tType == LayoutTurnout.LH_TURNOUT)
                            || (tType == LayoutTurnout.WYE_TURNOUT)) {
                        // reached RH. LH, or WYE turnout
                        if (lt.getLayoutBlock() != currLayoutBlock) {    // if not in the last block...
                            if (lt.getLayoutBlock() == nextLayoutBlock) {   // if in the next block
                                return true;    //(Yes!) done
                            } else {
                                curTrackSegment = null;   //(nope) dead end
                            }
                        } else {
                            if (conType == LayoutTrack.TURNOUT_A) {
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
                } else if (conType >= LayoutTrack.SLIP_A && conType <= LayoutTrack.SLIP_D) {
                    LayoutSlip ls = (LayoutSlip) conLayoutTrack;
                    int tType = ls.getTurnoutType();

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
                            case LayoutTrack.SLIP_A:
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
                            case LayoutTrack.SLIP_B:
                                if (tType == LayoutSlip.SINGLE_SLIP) {
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
                            case LayoutTrack.SLIP_C:
                                // if this is a single slip...
                                if (tType == LayoutSlip.SINGLE_SLIP) {
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
                            case LayoutTrack.SLIP_D:
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
                                log.warn("trackSegmentLeadsTo() unknown conType: " + conType);
                                break;
                            }
                        }   //switch (conType)
                        curLayoutTrack = conLayoutTrack;
                    }   // if (ls.getLayoutBlock() != currLayoutBlock
                }   //else if (conType >= LayoutTrack.SLIP_A && conType <= LayoutTrack.SLIP_D)
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

    @Nonnull
    private String connectionTypeToString(int conType) {
        String result = "<" + conType + ">";
        String[] con_types = {"NONE", "POS_POINT",
            "TURNOUT_A", "TURNOUT_B", "TURNOUT_C", "TURNOUT_D",
            "LEVEL_XING_A", "LEVEL_XING_B", "LEVEL_XING_C", "LEVEL_XING_D",
            "TRACK", "TURNOUT_CENTER", "LEVEL_XING_CENTER", "TURNTABLE_CENTER",
            "LAYOUT_POS_LABEL", "LAYOUT_POS_JCOMP", "MULTI_SENSOR", "MARKER",
            "TRACK_CIRCLE_CENTRE", "UNUSED_19", "SLIP_CENTER",
            "SLIP_A", "SLIP_B", "SLIP_C", "SLIP_D",
            "SLIP_LEFT", "SLIP_RIGHT"};
        if (conType < con_types.length) {
            result = con_types[conType];
        } else if (LayoutTrack.isBezierHitType(conType)) {
            result = "BEZIER_CONTROL_POINT #" + (conType - LayoutTrack.TURNTABLE_RAY_OFFSET);
        } else if (conType == LayoutTrack.SHAPE_CENTER) {
            result = "SHAPE_CENTER";
        } else if (LayoutShape.isShapePointOffsetHitPointType(conType)) {
            result = "SHAPE_POINT #" + (conType - LayoutTrack.TURNTABLE_RAY_OFFSET);
        } else if (conType >= LayoutTrack.TURNTABLE_RAY_OFFSET) {
            result = "TURNTABLE_RAY #" + (conType - LayoutTrack.TURNTABLE_RAY_OFFSET);
        }
        return result;
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

    private void setupOpposingTrackSegment(@Nonnull LevelXing x, int cType) {
        switch (cType) {
            case LayoutTrack.LEVEL_XING_A:
                trackSegment = (TrackSegment) x.getConnectC();
                prevConnectType = LayoutTrack.LEVEL_XING_C;
                break;
            case LayoutTrack.LEVEL_XING_B:
                trackSegment = (TrackSegment) x.getConnectD();
                prevConnectType = LayoutTrack.LEVEL_XING_D;
                break;
            case LayoutTrack.LEVEL_XING_C:
                trackSegment = (TrackSegment) x.getConnectA();
                prevConnectType = LayoutTrack.LEVEL_XING_A;
                break;
            case LayoutTrack.LEVEL_XING_D:
                trackSegment = (TrackSegment) x.getConnectB();
                prevConnectType = LayoutTrack.LEVEL_XING_B;
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
                .collect(Collectors.toCollection(ArrayList<LayoutTurnout>::new));
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ConnectivityUtil.class);
}
