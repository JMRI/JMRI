package jmri.jmrit.ctc.topology;

import java.util.*;
import jmri.*;
import jmri.jmrit.ctc.ctcserialdata.CTCSerialData;
import jmri.jmrit.display.layoutEditor.*;

/**
 *
 * IF AND ONLY IF LayoutDesign is available:
 *
 * This object creates a list of objects that describe the path
 * from the passed O.S. section to ALL other adjacent O.S. sections in a
 * specified direction.  It finds all sensors and turnouts (and their alignments).
 *
 * Ultimately, this will provide information to fill in completely
 * (for the direction indicated: left / right traffic) the table
 * _mTRL_TrafficLockingRulesSSVList in FrmTRL_Rules.java
 *
 * Sorry to say that as of 7/16/2020, LayoutBlock routine
 * "getNeighbourAtIndex" does NOT work in complex track situations
 * (one that I know of: Double crossovers), which leads to a
 * failure to "auto-generate" properly Signal Mast Logic for specific
 * signal masts.  I wanted to avoid this.
 *
 * By this time the user SHOULD HAVE been done with all ABS (or APB, etc) rules
 * for signals.  We rely on this to generate our information.
 *
 * Also, NEVER allocate the terminating O.S. section, otherwise the dispatcher
 * cannot clear the terminating O.S. section IN THE SAME DIRECTION as the
 * originating O.S. section!
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019, 2020
 */

public class Topology {
    private final CTCSerialData _mCTCSerialData;
    private final String _mNormal;
    private final String _mReverse;
    private final SignalMastLogicManager _mSignalMastLogicManager = InstanceManager.getDefault(jmri.SignalMastLogicManager.class);
    private final LayoutBlockManager _mLayoutBlockManager = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
    private ArrayList<Block> _mStartingBlocks = new ArrayList<>();

    /**
     * DO NOT USE, only for test suite!  Object will crash if anything referenced in it.
     * This constructor will be replaced by something more useful to the test suite
     * "someday" when the test suite it coded for it.
     */
    public Topology() {
        _mCTCSerialData = null;
        _mNormal = null;
        _mReverse = null;
    }


    /**
     * @param CTCSerialData                     The one and only.
     * @param OSSectionOccupiedExternalSensors  List of sensors to start from.
     * @param normal                            Bundle.getMessage("TLE_Normal")
     * @param reverse                           Bundle.getMessage("TLE_Reverse")
     */
    public Topology(CTCSerialData CTCSerialData, ArrayList<String> OSSectionOccupiedExternalSensors, String normal, String reverse) {
        _mCTCSerialData = CTCSerialData;
        _mNormal = normal;
        _mReverse = reverse;
        for (String OSSectionOccupiedExternalSensor : OSSectionOccupiedExternalSensors) {
            Sensor startingOSSectionSensor = InstanceManager.getDefault(SensorManager.class).getSensor(OSSectionOccupiedExternalSensor);
            if (null != startingOSSectionSensor) { // Available:
                LayoutBlock startingLayoutBlock = _mLayoutBlockManager.getBlockWithSensorAssigned(startingOSSectionSensor);
                if (null != startingLayoutBlock) { // Available:
                    _mStartingBlocks.add(startingLayoutBlock.getBlock());
                }
            }
        }
    }


    /**
     * @return boolean - true if available, else false.
     */
    public boolean isTopologyAvailable() { return !_mStartingBlocks.isEmpty(); }

    /**
     *
     * @param leftTraffic Traffic direction ON THE CTC PANEL that we are generating the rules for.
     * @return TopologyInfo.  All of the possible paths from this O.S. section to all
     * destination(s) in the direction indicated.
     *
     * WE HAVE TO GO in the opposite direction to get the mast for the O.S. section we are in!
     * There can ONLY be one signal in that direction, so upon encountering it, PROCESS ONLY IT!
     * (we could check if there is another, and issue an error, but I believe that the Panel Editor
     * would prevent this!)
     *
     * JMRI: West is left, East is right.
     */
    public ArrayList<TopologyInfo> getTrafficLockingRules(boolean leftTraffic) {
        ArrayList<TopologyInfo> returnValue = new ArrayList<>();
        for (Block startingBlock: _mStartingBlocks) {
            for (Path path : startingBlock.getPaths()) {
                Block neighborBlock = path.getBlock();
                if (inSameDirectionGenerally(getDirectionArrayListFrom(leftTraffic ? Path.EAST : Path.WEST), path.getToBlockDirection())) {
                    SignalMast facingSignalMast = _mLayoutBlockManager.getFacingSignalMast(neighborBlock, startingBlock);
                    if (null != facingSignalMast) { // Safety
                        SignalMastLogic facingSignalMastLogic = _mSignalMastLogicManager.getSignalMastLogic(facingSignalMast);
                        if (null != facingSignalMastLogic) { // Safety
                            processDestinations(returnValue, facingSignalMastLogic);
                        }
                    }
                }
            }
        }
        return returnValue;
    }


    /**
     * Once we've "backed up" to look forward from the starting O.S. section and gotten a facingSignalMastLogic, this
     * routine fills in "topologyInfo" with everything it finds from that point on.  Handles intermediate blocks also
     * (ABS, APB, etc.).  Goes until there is no more.
     *
     * @param topologyInfos             Entry(s) added to this ArrayList as they are encountered.  It is NOT cleared first,
     *                                  as there may be entries from prior calls to this routine in here.
     * @param facingSignalMastLogic     Facing signal mast logic from O.S. section code.
     */
    private void processDestinations(ArrayList<TopologyInfo> topologyInfos, SignalMastLogic facingSignalMastLogic) {
        for (SignalMast destinationSignalMast : facingSignalMastLogic.getDestinationList()) {
            TopologyInfo topologyInfo = new TopologyInfo(_mCTCSerialData, destinationSignalMast.getUserName(), _mNormal, _mReverse);
            createRules(topologyInfo, facingSignalMastLogic, destinationSignalMast);
            for (SignalMast tempSignalMast = destinationSignalMast; isIntermediateSignalMast(tempSignalMast); ) {
                SignalMastLogic tempSignalMastLogic = _mSignalMastLogicManager.getSignalMastLogic(tempSignalMast);
                if (null != tempSignalMastLogic) { // Safety:
 // No safety check needed here, "isIntermediateSignalMast" GUARANTEES that there is EXACTLY one entry in "getDestinationList" list:
                    SignalMast onlyTerminatingSignalMast = tempSignalMastLogic.getDestinationList().get(0);
                    createRules(topologyInfo, tempSignalMastLogic, onlyTerminatingSignalMast);
                    tempSignalMast = onlyTerminatingSignalMast;       // Next iteration, start where we left off.
                } else {
                    break;  // Stop immediately, got an issue.
                }
            }
            if (topologyInfo.nonEmpty()) {
                topologyInfos.add(topologyInfo);
            }
        }
    }


    /**
     * Simple routine to create all of the rules from the past information in "topologyInfo".
     * @param topologyInfo     What to fill in.
     * @param signalMastLogic  From this
     * @param signalMast       And this.
     */
    private void createRules(TopologyInfo topologyInfo, SignalMastLogic signalMastLogic, SignalMast signalMast) {
        topologyInfo.addBlocks(signalMastLogic.getBlocks(signalMast));
        topologyInfo.addBlocks(signalMastLogic.getAutoBlocks(signalMast));
        topologyInfo.addTurnouts(signalMastLogic, signalMast);
    }


    /**
     * Is the past Signal Mast a intermediate signal?  (ABS / APB or some such)?
     * If there are no turnouts, and there is ONLY ONE destination signal, then true, else false.
     *
     * @param signalMast
     * @return true if so, else false.  False also returned if invalid parameter in some way.
     */
//  Plagerization of DefaultSignalMastLogicManager/discoverSignallingDest "intermediateSignal" property code.
    private boolean isIntermediateSignalMast(SignalMast signalMast) {
        if (null != signalMast) { // Safety
            SignalMastLogic signalMastLogic = _mSignalMastLogicManager.getSignalMastLogic(signalMast);
            if (null != signalMastLogic) { // Safety:
                return signalMastLogic.getDestinationList().size() == 1
                        && signalMastLogic.getAutoTurnouts(signalMastLogic.getDestinationList().get(0)).isEmpty()
                        && signalMastLogic.getTurnouts(signalMastLogic.getDestinationList().get(0)).isEmpty();
            }
        }
        return false;   // OOPPSS invalid, can't determine, assume NOT a intermediate signal.
    }


    /**
     *
     * @param direction Direction to generate list from.
     * @return IF passed a valid direction, a 3 element set of "generally in the same direction" directions, else an EMPTY set (NOT null!)
     */
    private ArrayList<Integer> getDirectionArrayListFrom(int direction) {
        switch (direction) {
            case Path.NORTH:
                return new ArrayList<>(Arrays.asList(Path.NORTH_WEST, Path.NORTH, Path.NORTH_EAST));
            case Path.NORTH_EAST:
                return new ArrayList<>(Arrays.asList(Path.NORTH, Path.NORTH_EAST, Path.EAST));
            case Path.EAST:
                return new ArrayList<>(Arrays.asList(Path.NORTH_EAST, Path.EAST, Path.SOUTH_EAST));
            case Path.SOUTH_EAST:
                return new ArrayList<>(Arrays.asList(Path.EAST, Path.SOUTH_EAST, Path.SOUTH));
            case Path.SOUTH:
                return new ArrayList<>(Arrays.asList(Path.SOUTH_EAST, Path.SOUTH, Path.SOUTH_WEST));
            case Path.SOUTH_WEST:
                return new ArrayList<>(Arrays.asList(Path.SOUTH, Path.SOUTH_WEST, Path.WEST));
            case Path.WEST:
                return new ArrayList<>(Arrays.asList(Path.SOUTH_WEST, Path.WEST, Path.NORTH_WEST));
            case Path.NORTH_WEST:
                return new ArrayList<>(Arrays.asList(Path.WEST, Path.NORTH_WEST, Path.NORTH));
            default:
                return new ArrayList<>();    // Huh?
        }
    }


    /**
     *
     * @param possibleDirections The set of possible directions to check.  Caveat Emptor: IF this
     *                           array has no entries, then this routine returns false.
     *
     * @param direction Direction to check.
     * @return True if direction in "possibleDirections", else false.
     */
    private boolean inSameDirectionGenerally(ArrayList<Integer> possibleDirections, int direction) {
        if (possibleDirections.isEmpty()) return false;
        return possibleDirections.contains(direction);
    }
}
