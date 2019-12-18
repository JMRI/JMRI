package jmri.jmrit.display.layoutEditor;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.swing.JOptionPane;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.SignalSystem;
import jmri.Turnout;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.AbstractManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a Manager to handle LayoutBlocks. Note: the same
 * LayoutBlocks may appear in multiple LayoutEditor panels.
 * <p>
 * This manager does not enforce any particular system naming convention.
 * <p>
 * LayoutBlocks are usually addressed by userName. The systemName is hidden from
 * the user for the most part.
 *
 * @author Dave Duchamp Copyright (C) 2007
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutBlockManager extends AbstractManager<LayoutBlock> implements jmri.InstanceManagerAutoDefault {

    public LayoutBlockManager() {
        super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
        InstanceManager.memoryManagerInstance().addVetoableChangeListener(this);
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.LAYOUTBLOCKS;
    }

    @Override
    public char typeLetter() {
        return 'B';
    }
    private int blkNum = 1;

    /**
     * Create a new LayoutBlock if the LayoutBlock does not exist.
     * <p>
     * Note that since the userName is used to address LayoutBlocks, the user name
     * must be present. If the user name is not present, the new LayoutBlock is
     * not created, and null is returned.
     *
     * @return null if a LayoutBlock with the same systemName or userName
     *         already exists, or if there is trouble creating a new LayoutBlock
     */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock createNewLayoutBlock(
            @CheckForNull String systemName,
            String userName) {
        // Check that LayoutBlock does not already exist
        LayoutBlock result = null;

        if ((userName == null) || userName.isEmpty()) {
            log.error("Attempt to create a LayoutBlock with no user name");

            return null;
        }
        result = getByUserName(userName);

        if (result != null) {
            return null;
        }

        // here if not found under user name
        String sName = "";

        if (systemName == null) {
            //create a new unique system name
            boolean found = true;

            while (found) {
                sName = "ILB" + blkNum;
                blkNum++;
                result = getBySystemName(sName);

                if (result == null) {
                    found = false;
                }
            }
        } else {
            // try the supplied system name
            result = getBySystemName((systemName));

            if (result != null) {
                return null;
            }
            sName = systemName;
        }

        // LayoutBlock does not exist, create a new LayoutBlock
        result = new LayoutBlock(sName, userName);

        //save in the maps
        register(result);

        return result;
    }

    @CheckReturnValue
    @CheckForNull
    public LayoutBlock createNewLayoutBlock() {
        boolean found = true;

        while (found) {
            String sName = "ILB" + blkNum;
            LayoutBlock block = getBySystemName(sName);

            if (block == null) {
                found = false;
                String uName = "AUTOBLK:" + blkNum;
                block = new LayoutBlock(sName, uName);
                register(block);

                return block;
            }
            blkNum++;
        }
        return null;
    }

    /**
     * Remove an existing LayoutBlock.
     */
    public void deleteLayoutBlock(LayoutBlock block) {
        deregister(block);
    }

    /**
     * Get an existing LayoutBlock. First looks up assuming that name
     * is a User Name. If this fails, looks up assuming that name is a System
     * Name.
     * @return LayoutBlock, or null if not found by either user name or system name
     */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getLayoutBlock(@Nonnull String name) {
        LayoutBlock block = getByUserName(name);

        if (block != null) {
            return block;
        }
        return getBySystemName(name);
    }

    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getLayoutBlock(@CheckForNull Block block) {
        for (LayoutBlock lb : getNamedBeanSet()) {
             if (lb.getBlock() == block) {
                return lb;
            }
        }
        return null;
    }

    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getBySystemName(@Nonnull String key) {
        return _tsys.get(key);
    }

    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getByUserName(@Nonnull String key) {
        return _tuser.get(key);
    }

    /**
     * Find a LayoutBlock with a specified Sensor assigned as its
     * occupancy sensor.
     *
     * @return the block or null if no existing LayoutBlock has the Sensor
     *         assigned
     */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getBlockWithSensorAssigned(@CheckForNull Sensor s) {
        for (LayoutBlock block : getNamedBeanSet()) {
            if (block.getOccupancySensor() == s) {
                return block;
            }
        }
        return null;
    }

    /**
     * Find a LayoutBlock with a specified Memory assigned as its
     * value display.
     *
     * @return the block or null if no existing LayoutBlock has the memory
     *         assigned.
     */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getBlockWithMemoryAssigned(Memory m) {
        for (LayoutBlock block : getNamedBeanSet()) {
            if (block.getMemory() == m) {
                return block;
            }
        }
        return null;
    }

    /**
     * Initialize/check the Paths of all Blocks associated with LayoutBlocks.
     * <p>
     * This routine should be called when loading panels, after all Layout
     * Editor panels have been loaded.
     */
    public void initializeLayoutBlockPaths() {
        log.debug("start initializeLayoutBlockPaths");

        // cycle through all LayoutBlocks, completing initialization of associated jmri.Blocks
        for (LayoutBlock b : getNamedBeanSet()) {
                log.debug("Calling block '{}({})'.initializeLayoutBlock()", b.getSystemName(), b.getDisplayName());
                b.initializeLayoutBlock();
        }

        //cycle through all LayoutBlocks, updating Paths of associated jmri.Blocks
        badBeanErrors = 0; // perhaps incremented via addBadBeanError(), but that's never called?
        for (LayoutBlock b : getNamedBeanSet()) {
                log.debug("Calling block '{}({})'.updatePaths()", b.getSystemName(), b.getDisplayName());

                b.updatePaths();

                if (b.getBlock().getValue() != null) {
                    b.getBlock().setValue(null);
                }
        }

        if (badBeanErrors > 0) { // perhaps incremented via addBadBeanError(), but that's never called?
            JOptionPane.showMessageDialog(null, "" + badBeanErrors + " " + Bundle.getMessage("Warn2"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
        }
        try {
            new BlockValueFile().readBlockValues();
        } catch (org.jdom2.JDOMException jde) {
            log.error("JDOM Exception when retreiving block values {}", jde);
        } catch (java.io.IOException ioe) {
            log.error("I/O Exception when retreiving block values {}", ioe);
        }

        //special tests for getFacingSignalHead method - comment out next three lines unless using LayoutEditorTests
        //LayoutEditorTests layoutEditorTests = new LayoutEditorTests();
        //layoutEditorTests.runClinicTests();
        //layoutEditorTests.runTestPanel3Tests();

        initialized = true;
        log.debug("start initializeLayoutBlockRouting");
        initializeLayoutBlockRouting();
        log.debug("end initializeLayoutBlockRouting and initializeLayoutBlockPaths");
    }

    private boolean initialized = false;

    // Is this ever called?
    public void addBadBeanError() {
        badBeanErrors++;
    }
    private int badBeanErrors = 0;

    /**
     * Get the Signal Head facing into a specified Block from a
     * specified protected Block.
     * <p>
     * This method is primarily designed for use with scripts to get information
     * initially residing in a Layout Editor panel. If either of the input
     * Blocks is null, or if the two blocks do not join at a block boundary, or
     * if either of the input Blocks are not Layout Editor panel blocks, an
     * error message is logged, and "null" is returned. If the signal at the
     * block boundary has two heads--is located at the facing point of a
     * turnout-- the Signal Head that applies for the current setting of turnout
     * (THROWN or CLOSED) is returned. If the turnout state is UNKNOWN or
     * INCONSISTENT, an error message is logged, and "null" is returned. If the
     * signal at the block boundary has three heads--the facing point of a 3-way
     * turnout--the Signal Head that applies for the current settings of the two
     * turnouts of the 3-way turnout is returned. If the turnout state of either
     * turnout is UNKNOWN or INCONSISTENT, an error is logged and "null" is
     * returned. "null" is returned if the block boundary is between the two
     * turnouts of a THROAT_TO_THROAT turnout or a 3-way turnout. "null" is
     * returned for block boundaries exiting a THROAT_TO_THROAT turnout block,
     * since there are no signals that apply there.
     */
    @CheckReturnValue
    @CheckForNull
    public SignalHead getFacingSignalHead(
            @CheckForNull Block facingBlock,
            @CheckForNull Block protectedBlock) {
        //check input
        if ((facingBlock == null) || (protectedBlock == null)) {
            log.error("null block in call to getFacingSignalHead");
            return null;
        }

        //non-null - check if input corresponds to Blocks in a Layout Editor panel.
        String facingBlockName = facingBlock.getUserName();
        if ((facingBlockName == null) || facingBlockName.isEmpty()) {
            log.error("facingBlockName has no user name");
            return null;
        }

        String protectedBlockName = protectedBlock.getUserName();
        if ((protectedBlockName == null) || protectedBlockName.isEmpty()) {
            log.error("protectedBlockName has no user name");
            return null;
        }

        LayoutBlock fLayoutBlock = getByUserName(facingBlockName);
        LayoutBlock pLayoutBlock = getByUserName(protectedBlockName);
        if ((fLayoutBlock == null) || (pLayoutBlock == null)) {
            if (fLayoutBlock == null) {
                log.error("Block {} is not on a Layout Editor panel.", facingBlock.getDisplayName());
            }

            if (pLayoutBlock == null) {
                log.error("Block {} is not on a Layout Editor panel.", protectedBlock.getDisplayName());
            }
            return null;
        }

        //input has corresponding LayoutBlocks - does it correspond to a block boundary?
        LayoutEditor panel = fLayoutBlock.getMaxConnectedPanel();
        List<LayoutConnectivity> c = panel.getLEAuxTools().getConnectivityList(fLayoutBlock);
        LayoutConnectivity lc = null;
        int i = 0;
        boolean facingIsBlock1 = true;

        while ((i < c.size()) && (lc == null)) {
            LayoutConnectivity tlc = c.get(i);

            if ((tlc.getBlock1() == fLayoutBlock) && (tlc.getBlock2() == pLayoutBlock)) {
                lc = tlc;
            } else if ((tlc.getBlock1() == pLayoutBlock) && (tlc.getBlock2() == fLayoutBlock)) {
                lc = tlc;
                facingIsBlock1 = false;
            }
            i++;
        }

        if (lc == null) {
            log.error("Block {} ({}) is not connected to Block {}", facingBlock.getDisplayName(),
                    facingBlock.getDisplayName(), protectedBlock.getDisplayName());
            return null;
        }

        //blocks are connected, get connection item types
        LayoutTurnout lt = null;
        TrackSegment tr = lc.getTrackSegment();
        int cType = 0;

        if (tr == null) {
            // this is an internal crossover block boundary
            lt = lc.getXover();
            cType = lc.getXoverBoundaryType();

            switch (cType) {
                case LayoutConnectivity.XOVER_BOUNDARY_AB: {
                    if (facingIsBlock1) {
                        return lt.getSignalHead(LayoutTurnout.POINTA1);
                    } else {
                        return lt.getSignalHead(LayoutTurnout.POINTB1);
                    }
                }

                case LayoutConnectivity.XOVER_BOUNDARY_CD: {
                    if (facingIsBlock1) {
                        return lt.getSignalHead(LayoutTurnout.POINTC1);
                    } else {
                        return lt.getSignalHead(LayoutTurnout.POINTD1);
                    }
                }

                case LayoutConnectivity.XOVER_BOUNDARY_AC: {
                    if (facingIsBlock1) {
                        if (lt.getSignalHead(LayoutTurnout.POINTA2) == null) {	//there is no signal head for diverging (crossed
                            //over)
                            return lt.getSignalHead(LayoutTurnout.POINTA1);
                        } else {	//there is a diverging (crossed over) signal head, return it
                            return lt.getSignalHead(LayoutTurnout.POINTA2);
                        }
                    } else {
                        if (lt.getSignalHead(LayoutTurnout.POINTC2) == null) {
                            return lt.getSignalHead(LayoutTurnout.POINTC1);
                        } else {
                            return lt.getSignalHead(LayoutTurnout.POINTC2);
                        }
                    }
                }

                case LayoutConnectivity.XOVER_BOUNDARY_BD: {
                    if (facingIsBlock1) {
                        if (lt.getSignalHead(LayoutTurnout.POINTB2) == null) {	//there is no signal head for diverging (crossed
                            //over)
                            return lt.getSignalHead(LayoutTurnout.POINTB1);
                        } else {	//there is a diverging (crossed over) signal head, return it
                            return lt.getSignalHead(LayoutTurnout.POINTB2);
                        }
                    } else {
                        if (lt.getSignalHead(LayoutTurnout.POINTD2) == null) {
                            return lt.getSignalHead(LayoutTurnout.POINTD1);
                        } else {
                            return lt.getSignalHead(LayoutTurnout.POINTD2);
                        }
                    }
                }

                default: {
                    log.error("Unhandled crossover connection type: {}", cType);
                    break;
                }
            }	//switch

            //should never reach here, but ...
            log.error("crossover turnout block boundary not found in getFacingSignal");

            return null;
        }

        //not internal crossover block boundary
        LayoutTrack connected = lc.getConnectedObject();
        cType = lc.getConnectedType();

        if (connected == null) {
            log.error("No connectivity object found between Blocks {}, {} {}", facingBlock.getDisplayName(),
                    protectedBlock.getDisplayName(), cType);

            return null;
        }

        if (cType == LayoutTrack.TRACK) {
            // block boundary is at an Anchor Point
            //    LayoutEditorTools tools = panel.getLETools(); //TODO: Dead-code strip this
            PositionablePoint p = panel.getFinder().findPositionablePointAtTrackSegments(tr, (TrackSegment) connected);
            boolean block1IsWestEnd = LayoutEditorTools.isAtWestEndOfAnchor(tr, p);

            if ((block1IsWestEnd && facingIsBlock1) || (!block1IsWestEnd && !facingIsBlock1)) {
                //block1 is on the west (north) end of the block boundary
                return p.getEastBoundSignalHead();
            } else {
                return p.getWestBoundSignalHead();
            }
        }

        if (cType == LayoutTrack.TURNOUT_A) {
            // block boundary is at the facing point of a turnout or A connection of a crossover turnout
            lt = (LayoutTurnout) connected;

            if (lt.getLinkType() == LayoutTurnout.NO_LINK) {
                //standard turnout or A connection of a crossover turnout
                if (facingIsBlock1) {
                    if (lt.getSignalHead(LayoutTurnout.POINTA2) == null) {	//there is no signal head for diverging
                        return lt.getSignalHead(LayoutTurnout.POINTA1);
                    } else {
                        //check if track segments at B or C are in protected block (block 2)
                        if (((TrackSegment) (lt.getConnectB())).getBlockName().equals(protectedBlock.getUserName())) {
                            //track segment connected at B matches block 2, check C
                            if (!(((TrackSegment) lt.getConnectC()).getBlockName().equals(protectedBlock.getUserName()))) {
                                //track segment connected at C is not in block2, return continuing signal head at A
                                if (lt.getContinuingSense() == Turnout.CLOSED) {
                                    return lt.getSignalHead(LayoutTurnout.POINTA1);
                                } else {
                                    return lt.getSignalHead(LayoutTurnout.POINTA2);
                                }
                            } else {
                                //B and C both in block2, check turnout position to decide which signal head to return
                                int state = lt.getTurnout().getKnownState();

                                if (((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.CLOSED))
                                        || ((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.THROWN))) {	//continuing
                                    return lt.getSignalHead(LayoutTurnout.POINTA1);
                                } else if (((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.CLOSED))
                                        || ((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.THROWN))) {	//diverging
                                    return lt.getSignalHead(LayoutTurnout.POINTA2);
                                } else {
                                    //turnout state is UNKNOWN or INCONSISTENT
                                    log.error("Cannot choose signal head because turnout {} is in an UNKNOWN or INCONSISTENT state.",
                                            lt.getTurnout().getDisplayName());

                                    return null;
                                }
                            }
                        }

                        //track segment connected at B is not in block 2
                        if ((((TrackSegment) lt.getConnectC()).getBlockName().equals(protectedBlock.getUserName()))) {
                            //track segment connected at C is in block 2, return diverging signal head
                            if (lt.getContinuingSense() == Turnout.CLOSED) {
                                return lt.getSignalHead(LayoutTurnout.POINTA2);
                            } else {
                                return lt.getSignalHead(LayoutTurnout.POINTA1);
                            }
                        } else {
                            //neither track segment is in block 2 - should never get here unless layout turnout is
                            //the only item in block 2
                            if (!(lt.getBlockName().equals(protectedBlock.getUserName()))) {
                                log.error("neither signal at A protects block {}, and turnout is not in block either",
                                        protectedBlock.getDisplayName());
                            }
                            return null;
                        }
                    }
                } else {
                    //check if track segments at B or C are in facing block (block 1)
                    if (((TrackSegment) (lt.getConnectB())).getBlockName().equals(facingBlock.getUserName())) {
                        //track segment connected at B matches block 1, check C
                        if (!(((TrackSegment) lt.getConnectC()).getBlockName().equals(facingBlock.getDisplayName()))) {
                            //track segment connected at C is not in block 2, return signal head at continuing end
                            return lt.getSignalHead(LayoutTurnout.POINTB1);
                        } else {
                            //B and C both in block 1, check turnout position to decide which signal head to return
                            int state = lt.getTurnout().getKnownState();

                            if (((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.CLOSED))
                                    || ((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.THROWN))) {	//continuing
                                return lt.getSignalHead(LayoutTurnout.POINTB1);
                            } else if (((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.CLOSED))
                                    || ((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.THROWN))) {
                                //diverging, check for second head
                                if (lt.getSignalHead(LayoutTurnout.POINTC2) == null) {
                                    return lt.getSignalHead(LayoutTurnout.POINTC1);
                                } else {
                                    return lt.getSignalHead(LayoutTurnout.POINTC2);
                                }
                            } else {
                                //turnout state is UNKNOWN or INCONSISTENT
                                log.error("Cannot choose signal head because turnout {} is in an UNKNOWN or INCONSISTENT state.",
                                        lt.getTurnout().getDisplayName());

                                return null;
                            }
                        }
                    }

                    //track segment connected at B is not in block 1
                    if (((TrackSegment) lt.getConnectC()).getBlockName().equals(facingBlock.getUserName())) {
                        //track segment connected at C is in block 1, return diverging signal head, check for second head
                        if (lt.getSignalHead(LayoutTurnout.POINTC2) == null) {
                            return lt.getSignalHead(LayoutTurnout.POINTC1);
                        } else {
                            return lt.getSignalHead(LayoutTurnout.POINTC2);
                        }
                    } else {
                        //neither track segment is in block 1 - should never get here unless layout turnout is
                        //the only item in block 1
                        if (!(lt.getBlockName().equals(facingBlock.getUserName()))) {
                            log.error("no signal faces block {}, and turnout is not in block either",
                                    facingBlock.getDisplayName());
                        }
                        return null;
                    }
                }
            } else if (lt.getLinkType() == LayoutTurnout.THROAT_TO_THROAT) {
                //There are no signals at the throat of a THROAT_TO_THROAT

                //There should not be a block boundary here
                return null;
            } else if (lt.getLinkType() == LayoutTurnout.FIRST_3_WAY) {
                //3-way turnout is in its own block - block boundary is at the throat of the 3-way turnout
                if (!facingIsBlock1) {
                    //facing block is within the three-way turnout's block - no signals for exit of the block
                    return null;
                } else {
                    //select throat signal according to state of the 3-way turnout
                    int state = lt.getTurnout().getKnownState();

                    if (state == Turnout.THROWN) {
                        if (lt.getSignalHead(LayoutTurnout.POINTA2) == null) {
                            return lt.getSignalHead(LayoutTurnout.POINTA1);
                        } else {
                            return lt.getSignalHead(LayoutTurnout.POINTA2);
                        }
                    } else if (state == Turnout.CLOSED) {
                        LayoutTurnout tLinked = panel.getFinder().findLayoutTurnoutByTurnoutName(lt.getLinkedTurnoutName());
                        state = tLinked.getTurnout().getKnownState();

                        if (state == Turnout.CLOSED) {
                            if (tLinked.getContinuingSense() == Turnout.CLOSED) {
                                return lt.getSignalHead(LayoutTurnout.POINTA1);
                            } else if (lt.getSignalHead(LayoutTurnout.POINTA3) == null) {
                                return lt.getSignalHead(LayoutTurnout.POINTA1);
                            } else {
                                return lt.getSignalHead(LayoutTurnout.POINTA3);
                            }
                        } else if (state == Turnout.THROWN) {
                            if (tLinked.getContinuingSense() == Turnout.THROWN) {
                                return lt.getSignalHead(LayoutTurnout.POINTA1);
                            } else if (lt.getSignalHead(LayoutTurnout.POINTA3) == null) {
                                return lt.getSignalHead(LayoutTurnout.POINTA1);
                            } else {
                                return lt.getSignalHead(LayoutTurnout.POINTA3);
                            }
                        } else {
                            //should never get here - linked turnout state is UNKNOWN or INCONSISTENT
                            log.error("Cannot choose 3-way signal head to return because turnout {} is in an UNKNOWN or INCONSISTENT state.",
                                    tLinked.getTurnout().getSystemName());
                            return null;
                        }
                    } else {
                        //should never get here - linked turnout state is UNKNOWN or INCONSISTENT
                        log.error("Cannot choose 3-way signal head to return because turnout {} is in an UNKNOWN or INCONSISTENT state.",
                                lt.getTurnout().getSystemName());
                        return null;
                    }
                }
            } else if (lt.getLinkType() == LayoutTurnout.SECOND_3_WAY) {
                //There are no signals at the throat of the SECOND_3_WAY turnout of a 3-way turnout

                //There should not be a block boundary here
                return null;
            }
        }

        if (cType == LayoutTrack.TURNOUT_B) {
            //block boundary is at the continuing track of a turnout or B connection of a crossover turnout
            lt = (LayoutTurnout) connected;

            //check for double crossover or LH crossover
            if (((lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                    || (lt.getTurnoutType() == LayoutTurnout.LH_XOVER))) {
                if (facingIsBlock1) {
                    if (lt.getSignalHead(LayoutTurnout.POINTB2) == null) {	//there is only one signal at B, return it
                        return lt.getSignalHead(LayoutTurnout.POINTB1);
                    }

                    //check if track segments at A or D are in protected block (block 2)
                    if (((TrackSegment) (lt.getConnectA())).getBlockName().equals(protectedBlock.getUserName())) {
                        //track segment connected at A matches block 2, check D
                        if (!(((TrackSegment) lt.getConnectD()).getBlockName().equals(protectedBlock.getUserName()))) {
                            //track segment connected at D is not in block2, return continuing signal head at B
                            return lt.getSignalHead(LayoutTurnout.POINTB1);
                        } else {
                            //A and D both in block 2, check turnout position to decide which signal head to return
                            int state = lt.getTurnout().getKnownState();

                            if (((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.CLOSED))
                                    || ((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.THROWN))) {	//continuing
                                return lt.getSignalHead(LayoutTurnout.POINTB1);
                            } else if (((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.CLOSED))
                                    || ((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.THROWN))) {	//diverging
                                //(crossed

                                //over)
                                return lt.getSignalHead(LayoutTurnout.POINTB2);
                            } else {
                                //turnout state is UNKNOWN or INCONSISTENT
                                log.error("Cannot choose signal head because turnout {} is in an UNKNOWN or INCONSISTENT state.",
                                        lt.getTurnout().getDisplayName());
                                return null;
                            }
                        }
                    }

                    //track segment connected at A is not in block 2
                    if ((((TrackSegment) lt.getConnectD()).getBlockName().equals(protectedBlock.getUserName()))) {	//track segment
                        //connected at D
                        //is in block 2,
                        //return
                        //diverging

                        //signal head
                        return lt.getSignalHead(LayoutTurnout.POINTB2);
                    } else {
                        //neither track segment is in block 2 - should never get here unless layout turnout is
                        //only item in block 2
                        if (!(lt.getBlockName().equals(protectedBlock.getUserName()))) {
                            log.error("neither signal at B protects block {}, and turnout is not in block either",
                                    protectedBlock.getDisplayName());
                        }
                        return null;
                    }
                } else {
                    //check if track segments at A or D are in facing block (block 1)
                    if (((TrackSegment) (lt.getConnectA())).getBlockName().equals(facingBlock.getUserName())) {
                        //track segment connected at A matches block 1, check D
                        if (!(((TrackSegment) lt.getConnectD()).getBlockName().equals(facingBlock.getUserName()))) {
                            //track segment connected at D is not in block 2, return signal head at continuing end
                            return lt.getSignalHead(LayoutTurnout.POINTA1);
                        } else {
                            //A and D both in block 1, check turnout position to decide which signal head to return
                            int state = lt.getTurnout().getKnownState();

                            if (((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.CLOSED))
                                    || ((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.THROWN))) {	//continuing
                                return lt.getSignalHead(LayoutTurnout.POINTA1);
                            } else if (((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.CLOSED))
                                    || ((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.THROWN))) {
                                //diverging, check for second head
                                if (lt.getSignalHead(LayoutTurnout.POINTD2) == null) {
                                    return lt.getSignalHead(LayoutTurnout.POINTD1);
                                } else {
                                    return lt.getSignalHead(LayoutTurnout.POINTD2);
                                }
                            } else {
                                //turnout state is UNKNOWN or INCONSISTENT
                                log.error("Cannot choose signal head because turnout {} is in an UNKNOWN or INCONSISTENT state.",
                                        lt.getTurnout().getDisplayName());
                                return null;
                            }
                        }
                    }

                    //track segment connected at A is not in block 1
                    if (((TrackSegment) lt.getConnectD()).getBlockName().equals(facingBlock.getUserName())) {
                        //track segment connected at D is in block 1, return diverging signal head, check for second head
                        if (lt.getSignalHead(LayoutTurnout.POINTD2) == null) {
                            return lt.getSignalHead(LayoutTurnout.POINTD1);
                        } else {
                            return lt.getSignalHead(LayoutTurnout.POINTD2);
                        }
                    } else {
                        //neither track segment is in block 1 - should never get here unless layout turnout is
                        //the only item in block 1
                        if (!(lt.getBlockName().equals(facingBlock.getUserName()))) {
                            log.error("no signal faces block {}, and turnout is not in block either",
                                    facingBlock.getDisplayName());
                        }
                        return null;
                    }
                }
            }

            //not double crossover or LH crossover
            if ((lt.getLinkType() == LayoutTurnout.NO_LINK) && (lt.getContinuingSense() == Turnout.CLOSED)) {
                if (facingIsBlock1) {
                    return lt.getSignalHead(LayoutTurnout.POINTB1);
                } else {
                    return lt.getSignalHead(LayoutTurnout.POINTA1);
                }
            } else if (lt.getLinkType() == LayoutTurnout.NO_LINK) {
                if (facingIsBlock1) {
                    return lt.getSignalHead(LayoutTurnout.POINTC1);
                } else {
                    if (lt.getSignalHead(LayoutTurnout.POINTA2) == null) {
                        return lt.getSignalHead(LayoutTurnout.POINTA1);
                    } else {
                        return lt.getSignalHead(LayoutTurnout.POINTA2);
                    }
                }
            } else if (lt.getLinkType() == LayoutTurnout.THROAT_TO_THROAT) {
                if (!facingIsBlock1) {
                    //There are no signals at the throat of a THROAT_TO_THROAT
                    return null;
                }

                //facing block is outside of the THROAT_TO_THROAT
                if ((lt.getContinuingSense() == Turnout.CLOSED) && (lt.getSignalHead(LayoutTurnout.POINTB2) == null)) {
                    //there is only one signal head here - return it
                    return lt.getSignalHead(LayoutTurnout.POINTB1);
                } else if ((lt.getContinuingSense() == Turnout.THROWN) && (lt.getSignalHead(LayoutTurnout.POINTC2) == null)) {
                    //there is only one signal head here - return it
                    return lt.getSignalHead(LayoutTurnout.POINTC1);
                }

                //There are two signals here get linked turnout and decide which to return from linked turnout state
                LayoutTurnout tLinked = panel.getFinder().findLayoutTurnoutByTurnoutName(lt.getLinkedTurnoutName());
                int state = tLinked.getTurnout().getKnownState();

                if (state == Turnout.CLOSED) {
                    if (lt.getContinuingSense() == Turnout.CLOSED) {
                        return lt.getSignalHead(LayoutTurnout.POINTB1);
                    } else {
                        return lt.getSignalHead(LayoutTurnout.POINTC1);
                    }
                } else if (state == Turnout.THROWN) {
                    if (lt.getContinuingSense() == Turnout.CLOSED) {
                        return lt.getSignalHead(LayoutTurnout.POINTB2);
                    } else {
                        return lt.getSignalHead(LayoutTurnout.POINTC2);
                    }
                } else {	//should never get here - linked turnout state is UNKNOWN or INCONSISTENT
                    log.error("Cannot choose signal head to return because turnout {} is in an UNKNOWN or INCONSISTENT state.",
                            tLinked.getTurnout().getDisplayName());
                }
                return null;
            } else if (lt.getLinkType() == LayoutTurnout.FIRST_3_WAY) {
                //there is no signal at the FIRST_3_WAY turnout continuing track of a 3-way turnout
                //there should not be a block boundary here
                return null;
            } else if (lt.getLinkType() == LayoutTurnout.SECOND_3_WAY) {
                if (facingIsBlock1) {
                    if (lt.getContinuingSense() == Turnout.CLOSED) {
                        return lt.getSignalHead(LayoutTurnout.POINTB1);
                    } else {
                        return lt.getSignalHead(LayoutTurnout.POINTC1);
                    }
                } else {
                    //signal is at the linked turnout - the throat of the 3-way turnout
                    LayoutTurnout tLinked = panel.getFinder().findLayoutTurnoutByTurnoutName(lt.getLinkedTurnoutName());

                    if (lt.getContinuingSense() == Turnout.CLOSED) {
                        return tLinked.getSignalHead(LayoutTurnout.POINTA1);
                    } else {
                        if (tLinked.getSignalHead(LayoutTurnout.POINTA3) == null) {
                            return tLinked.getSignalHead(LayoutTurnout.POINTA1);
                        } else {
                            return tLinked.getSignalHead(LayoutTurnout.POINTA3);
                        }
                    }
                }
            }
        }

        if (cType == LayoutTrack.TURNOUT_C) {
            //block boundary is at the diverging track of a turnout or C connection of a crossover turnout
            lt = (LayoutTurnout) connected;

            //check for double crossover or RH crossover
            if ((lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER)
                    || (lt.getTurnoutType() == LayoutTurnout.RH_XOVER)) {
                if (facingIsBlock1) {
                    if (lt.getSignalHead(LayoutTurnout.POINTC2) == null) {	//there is only one head at C, return it
                        return lt.getSignalHead(LayoutTurnout.POINTC1);
                    }

                    //check if track segments at A or D are in protected block (block 2)
                    if (((TrackSegment) (lt.getConnectA())).getBlockName().equals(protectedBlock.getUserName())) {
                        //track segment connected at A matches block 2, check D
                        if (!(((TrackSegment) lt.getConnectD()).getBlockName().equals(protectedBlock.getUserName()))) {
                            //track segment connected at D is not in block2, return diverging signal head at C
                            return lt.getSignalHead(LayoutTurnout.POINTC2);
                        } else {
                            //A and D both in block 2, check turnout position to decide which signal head to return
                            int state = lt.getTurnout().getKnownState();

                            if (((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.CLOSED))
                                    || ((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.THROWN))) {	//continuing
                                return lt.getSignalHead(LayoutTurnout.POINTC1);
                            } else if (((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.CLOSED))
                                    || ((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.THROWN))) {	//diverging
                                //(crossed

                                //over)
                                return lt.getSignalHead(LayoutTurnout.POINTC2);
                            } else {
                                //turnout state is UNKNOWN or INCONSISTENT
                                log.error("Cannot choose signal head because turnout {} is in an UNKNOWN or INCONSISTENT state.",
                                        lt.getTurnout().getDisplayName());
                                return null;
                            }
                        }
                    }

                    //track segment connected at A is not in block 2
                    if ((((TrackSegment) lt.getConnectD()).getBlockName().equals(protectedBlock.getUserName()))) {
                        //track segment connected at D is in block 2, return continuing signal head
                        return lt.getSignalHead(LayoutTurnout.POINTC1);
                    } else {
                        //neither track segment is in block 2 - should never get here unless layout turnout is
                        //only item in block 2
                        if (!(lt.getBlockName().equals(protectedBlock.getUserName()))) {
                            log.error("neither signal at C protects block {}, and turnout is not in block either",
                                    protectedBlock.getDisplayName());
                        }
                        return null;
                    }
                } else {
                    //check if track segments at D or A are in facing block (block 1)
                    if (((TrackSegment) (lt.getConnectD())).getBlockName().equals(facingBlock.getUserName())) {
                        //track segment connected at D matches block 1, check A
                        if (!(((TrackSegment) lt.getConnectA()).getBlockName().equals(facingBlock.getUserName()))) {
                            //track segment connected at A is not in block 2, return signal head at continuing end
                            return lt.getSignalHead(LayoutTurnout.POINTD1);
                        } else {
                            //A and D both in block 1, check turnout position to decide which signal head to return
                            int state = lt.getTurnout().getKnownState();

                            if (((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.CLOSED))
                                    || ((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.THROWN))) {	//continuing
                                return lt.getSignalHead(LayoutTurnout.POINTD1);
                            } else if (((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.CLOSED))
                                    || ((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.THROWN))) {
                                //diverging, check for second head
                                if (lt.getSignalHead(LayoutTurnout.POINTA2) == null) {
                                    return lt.getSignalHead(LayoutTurnout.POINTA1);
                                } else {
                                    return lt.getSignalHead(LayoutTurnout.POINTA2);
                                }
                            } else {
                                //turnout state is UNKNOWN or INCONSISTENT
                                log.error("Cannot choose signal head because turnout {} is in an UNKNOWN or INCONSISTENT state.",
                                        lt.getTurnout().getDisplayName());
                                return null;
                            }
                        }
                    }

                    //track segment connected at D is not in block 1
                    if (((TrackSegment) lt.getConnectA()).getBlockName().equals(facingBlock.getUserName())) {
                        //track segment connected at A is in block 1, return diverging signal head, check for second head
                        if (lt.getSignalHead(LayoutTurnout.POINTA2) == null) {
                            return lt.getSignalHead(LayoutTurnout.POINTA1);
                        } else {
                            return lt.getSignalHead(LayoutTurnout.POINTA2);
                        }
                    } else {
                        //neither track segment is in block 1 - should never get here unless layout turnout is
                        //the only item in block 1
                        if (!(lt.getBlockName().equals(facingBlock.getUserName()))) {
                            log.error("no signal faces block {}, and turnout is not in block either",
                                    facingBlock.getDisplayName());
                        }
                        return null;
                    }
                }
            }

            //not double crossover or RH crossover
            if ((lt.getLinkType() == LayoutTurnout.NO_LINK) && (lt.getContinuingSense() == Turnout.CLOSED)) {
                if (facingIsBlock1) {
                    return lt.getSignalHead(LayoutTurnout.POINTC1);
                } else if (lt.getTurnoutType() == LayoutTurnout.LH_XOVER) {	//LH turnout - this is continuing track for D connection
                    return lt.getSignalHead(LayoutTurnout.POINTD1);
                } else {
                    //RH, LH or WYE turnout, this is diverging track for A connection
                    if (lt.getSignalHead(LayoutTurnout.POINTA2) == null) {	//there is no signal head at the throat for diverging
                        return lt.getSignalHead(LayoutTurnout.POINTA1);
                    } else {	//there is a diverging head at the throat, return it
                        return lt.getSignalHead(LayoutTurnout.POINTA2);
                    }
                }
            } else if (lt.getLinkType() == LayoutTurnout.NO_LINK) {
                if (facingIsBlock1) {
                    return lt.getSignalHead(LayoutTurnout.POINTB1);
                } else {
                    return lt.getSignalHead(LayoutTurnout.POINTA1);
                }
            } else if (lt.getLinkType() == LayoutTurnout.THROAT_TO_THROAT) {
                if (!facingIsBlock1) {
                    //There are no signals at the throat of a THROAT_TO_THROAT
                    return null;
                }

                //facing block is outside of the THROAT_TO_THROAT
                if ((lt.getContinuingSense() == Turnout.CLOSED) && (lt.getSignalHead(LayoutTurnout.POINTC2) == null)) {
                    //there is only one signal head here - return it
                    return lt.getSignalHead(LayoutTurnout.POINTC1);
                } else if ((lt.getContinuingSense() == Turnout.THROWN) && (lt.getSignalHead(LayoutTurnout.POINTB2) == null)) {
                    //there is only one signal head here - return it
                    return lt.getSignalHead(LayoutTurnout.POINTB1);
                }

                //There are two signals here get linked turnout and decide which to return from linked turnout state
                LayoutTurnout tLinked = panel.getFinder().findLayoutTurnoutByTurnoutName(lt.getLinkedTurnoutName());
                int state = tLinked.getTurnout().getKnownState();

                if (state == Turnout.CLOSED) {
                    if (lt.getContinuingSense() == Turnout.CLOSED) {
                        return lt.getSignalHead(LayoutTurnout.POINTC1);
                    } else {
                        return lt.getSignalHead(LayoutTurnout.POINTB1);
                    }
                } else if (state == Turnout.THROWN) {
                    if (lt.getContinuingSense() == Turnout.CLOSED) {
                        return lt.getSignalHead(LayoutTurnout.POINTC2);
                    } else {
                        return lt.getSignalHead(LayoutTurnout.POINTB2);
                    }
                } else {
                    //should never get here - linked turnout state is UNKNOWN or INCONSISTENT
                    log.error("Cannot choose signal head to return because turnout {} is in an UNKNOWN or INCONSISTENT state.",
                            tLinked.getTurnout().getDisplayName());
                    return null;
                }
            } else if (lt.getLinkType() == LayoutTurnout.FIRST_3_WAY) {
                if (facingIsBlock1) {
                    return lt.getSignalHead(LayoutTurnout.POINTC1);
                } else {
                    if (lt.getSignalHead(LayoutTurnout.POINTA2) == null) {
                        return lt.getSignalHead(LayoutTurnout.POINTA1);
                    } else {
                        return lt.getSignalHead(LayoutTurnout.POINTA2);
                    }
                }
            } else if (lt.getLinkType() == LayoutTurnout.SECOND_3_WAY) {
                if (facingIsBlock1) {
                    if (lt.getContinuingSense() == Turnout.CLOSED) {
                        return lt.getSignalHead(LayoutTurnout.POINTC1);
                    } else {
                        return lt.getSignalHead(LayoutTurnout.POINTB1);
                    }
                } else {
                    //signal is at the linked turnout - the throat of the 3-way turnout
                    LayoutTurnout tLinked = panel.getFinder().findLayoutTurnoutByTurnoutName(lt.getLinkedTurnoutName());

                    if (lt.getContinuingSense() == Turnout.CLOSED) {
                        if (tLinked.getSignalHead(LayoutTurnout.POINTA3) == null) {
                            return tLinked.getSignalHead(LayoutTurnout.POINTA1);
                        } else {
                            return tLinked.getSignalHead(LayoutTurnout.POINTA3);
                        }
                    } else {
                        if (tLinked.getSignalHead(LayoutTurnout.POINTA2) == null) {
                            return tLinked.getSignalHead(LayoutTurnout.POINTA1);
                        } else {
                            return tLinked.getSignalHead(LayoutTurnout.POINTA2);
                        }
                    }
                }
            }
        }

        if (cType == LayoutTrack.TURNOUT_D) {
            //block boundary is at D connectin of a crossover turnout
            lt = (LayoutTurnout) connected;

            if (lt.getTurnoutType() == LayoutTurnout.RH_XOVER) {
                //no diverging route possible, this is continuing track for C connection
                if (facingIsBlock1) {
                    return lt.getSignalHead(LayoutTurnout.POINTD1);
                } else {
                    return lt.getSignalHead(LayoutTurnout.POINTC1);
                }
            }

            if (facingIsBlock1) {
                if (lt.getSignalHead(LayoutTurnout.POINTD2) == null) {	//there is no signal head for diverging
                    return lt.getSignalHead(LayoutTurnout.POINTD1);
                } else {
                    //check if track segments at C or B are in protected block (block 2)
                    if (((TrackSegment) (lt.getConnectC())).getBlockName().equals(protectedBlock.getUserName())) {
                        //track segment connected at C matches block 2, check B
                        if (!(((TrackSegment) lt.getConnectB()).getBlockName().equals(protectedBlock.getUserName()))) {
                            //track segment connected at B is not in block2, return continuing signal head at D
                            return lt.getSignalHead(LayoutTurnout.POINTD1);
                        } else {
                            //C and B both in block2, check turnout position to decide which signal head to return
                            int state = lt.getTurnout().getKnownState();

                            if (((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.CLOSED))
                                    || ((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.THROWN))) {	//continuing
                                return lt.getSignalHead(LayoutTurnout.POINTD1);
                            } else if (((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.CLOSED))
                                    || ((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.THROWN))) {	//diverging
                                return lt.getSignalHead(LayoutTurnout.POINTD2);
                            } else {
                                //turnout state is UNKNOWN or INCONSISTENT
                                log.error("Cannot choose signal head because turnout {} is in an UNKNOWN or INCONSISTENT state.",
                                        lt.getTurnout().getDisplayName());
                                return null;
                            }
                        }
                    }

                    //track segment connected at C is not in block 2
                    if ((((TrackSegment) lt.getConnectB()).getBlockName().equals(protectedBlock.getUserName()))) {
                        //track segment connected at B is in block 2, return diverging signal head
                        return lt.getSignalHead(LayoutTurnout.POINTD2);
                    } else {
                        //neither track segment is in block 2 - should never get here unless layout turnout is
                        //the only item in block 2
                        if (!(lt.getBlockName().equals(protectedBlock.getUserName()))) {
                            log.error("neither signal at D protects block {}, and turnout is not in block either",
                                    protectedBlock.getDisplayName());
                        }
                        return null;
                    }
                }
            } else {
                //check if track segments at C or B are in facing block (block 1)
                if (((TrackSegment) (lt.getConnectC())).getBlockName().equals(facingBlock.getUserName())) {
                    //track segment connected at C matches block 1, check B
                    if (!(((TrackSegment) lt.getConnectB()).getBlockName().equals(facingBlock.getUserName()))) {
                        //track segment connected at B is not in block 2, return signal head at continuing end
                        return lt.getSignalHead(LayoutTurnout.POINTC1);
                    } else {
                        //C and B both in block 1, check turnout position to decide which signal head to return
                        int state = lt.getTurnout().getKnownState();

                        if (((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.CLOSED))
                                || ((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.THROWN))) {	//continuing
                            return lt.getSignalHead(LayoutTurnout.POINTC1);
                        } else if (((state == Turnout.THROWN) && (lt.getContinuingSense() == Turnout.CLOSED))
                                || ((state == Turnout.CLOSED) && (lt.getContinuingSense() == Turnout.THROWN))) {
                            //diverging, check for second head
                            if (lt.getSignalHead(LayoutTurnout.POINTB2) == null) {
                                return lt.getSignalHead(LayoutTurnout.POINTB1);
                            } else {
                                return lt.getSignalHead(LayoutTurnout.POINTB2);
                            }
                        } else {
                            //turnout state is UNKNOWN or INCONSISTENT
                            log.error("Cannot choose signal head because turnout {} is in an UNKNOWN or INCONSISTENT state.",
                                    lt.getTurnout().getDisplayName());
                            return null;
                        }
                    }
                }

                //track segment connected at C is not in block 1
                if (((TrackSegment) lt.getConnectB()).getBlockName().equals(facingBlock.getUserName())) {
                    //track segment connected at B is in block 1, return diverging signal head, check for second head
                    if (lt.getSignalHead(LayoutTurnout.POINTB2) == null) {
                        return lt.getSignalHead(LayoutTurnout.POINTB1);
                    } else {
                        return lt.getSignalHead(LayoutTurnout.POINTB2);
                    }
                } else {
                    //neither track segment is in block 1 - should never get here unless layout turnout is
                    //the only item in block 1
                    if (!(lt.getBlockName().equals(facingBlock.getUserName()))) {
                        log.error("no signal faces block {}, and turnout is not in block either",
                                facingBlock.getDisplayName());
                    }
                    return null;
                }
            }
        }

        if ((cType >= LayoutTrack.SLIP_A) && (cType <= LayoutTrack.SLIP_D)) {
            if (!facingIsBlock1) {
                return null;
            }

            LayoutSlip ls = (LayoutSlip) connected;

            switch (cType) {
                case LayoutTrack.SLIP_A: {
                    if (ls.getSlipState() == LayoutSlip.STATE_AD) {
                        return ls.getSignalHead(LayoutTurnout.POINTA2);
                    } else {
                        return ls.getSignalHead(LayoutTurnout.POINTA1);
                    }
                }

                case LayoutTrack.SLIP_B: {
                    if (ls.getTurnoutType() == LayoutSlip.DOUBLE_SLIP) {
                        if (ls.getSlipState() == LayoutSlip.STATE_BC) {
                            return ls.getSignalHead(LayoutTurnout.POINTB2);
                        } else {
                            return ls.getSignalHead(LayoutTurnout.POINTB1);
                        }
                    } else {
                        return ls.getSignalHead(LayoutTurnout.POINTB1);
                    }
                }

                case LayoutTrack.SLIP_C: {
                    if (ls.getTurnoutType() == LayoutSlip.DOUBLE_SLIP) {
                        if (ls.getSlipState() == LayoutSlip.STATE_BC) {
                            return ls.getSignalHead(LayoutTurnout.POINTC2);
                        } else {
                            return ls.getSignalHead(LayoutTurnout.POINTC1);
                        }
                    } else {
                        return ls.getSignalHead(LayoutTurnout.POINTC1);
                    }
                }

                case LayoutTrack.SLIP_D: {
                    if (ls.getSlipState() == LayoutSlip.STATE_AD) {
                        return ls.getSignalHead(LayoutTurnout.POINTD2);
                    } else {
                        return ls.getSignalHead(LayoutTurnout.POINTD1);
                    }
                }

                default: {
                    break;
                }
            }	//switch
        }

        //block boundary must be at a level crossing
        if ((cType < LayoutTrack.LEVEL_XING_A) || (cType > LayoutTrack.LEVEL_XING_D)) {
            log.error("{} {} Block Boundary not identified correctly - Blocks {}, {}",
                    cType, connected, facingBlock.getDisplayName(), protectedBlock.getDisplayName());

            return null;
        }
        LevelXing xing = (LevelXing) connected;

        switch (cType) {
            case LayoutTrack.LEVEL_XING_A: {
                //block boundary is at the A connection of a level crossing
                if (facingIsBlock1) {
                    return xing.getSignalHead(LevelXing.POINTA);
                } else {
                    return xing.getSignalHead(LevelXing.POINTC);
                }
            }

            case LayoutTrack.LEVEL_XING_B: {
                //block boundary is at the B connection of a level crossing
                if (facingIsBlock1) {
                    return xing.getSignalHead(LevelXing.POINTB);
                } else {
                    return xing.getSignalHead(LevelXing.POINTD);
                }
            }

            case LayoutTrack.LEVEL_XING_C: {
                //block boundary is at the C connection of a level crossing
                if (facingIsBlock1) {
                    return xing.getSignalHead(LevelXing.POINTC);
                } else {
                    return xing.getSignalHead(LevelXing.POINTA);
                }
            }

            case LayoutTrack.LEVEL_XING_D: {
                //block boundary is at the D connection of a level crossing
                if (facingIsBlock1) {
                    return xing.getSignalHead(LevelXing.POINTD);
                } else {
                    return xing.getSignalHead(LevelXing.POINTB);
                }
            }

            default: {
                break;
            }
        }
        return null;
    }

    /**
     * Get the named bean of either a Sensor or signalmast facing
     * into a specified Block from a specified protected Block.
     *
     * @return The assigned sensor or signal mast as a named bean
     */
    @CheckReturnValue
    @CheckForNull
    public NamedBean getNamedBeanAtEndBumper(
            @CheckForNull Block facingBlock,
            @CheckForNull LayoutEditor panel) {
        NamedBean bean = getSignalMastAtEndBumper(facingBlock, panel);

        if (bean != null) {
            return bean;
        } else {
            return getSensorAtEndBumper(facingBlock, panel);
        }
    }

    /**
     * Get a Signal Mast that is assigned to a block which has an
     * end bumper at one end.
     */
    @CheckReturnValue
    @CheckForNull
    public SignalMast getSignalMastAtEndBumper(
            @CheckForNull Block facingBlock,
            @CheckForNull LayoutEditor panel) {
        if (facingBlock == null) {
            log.error("null block in call to getFacingSignalMast");
            return null;
        }
        String facingBlockName = facingBlock.getUserName();
        if ((facingBlockName == null) || facingBlockName.isEmpty()) {
            log.error("facing block has no user name");
            return null;
        }

        LayoutBlock fLayoutBlock = getByUserName(facingBlockName);
        if (fLayoutBlock == null) {
            log.error("Block {} is not on a Layout Editor panel.", facingBlock.getDisplayName());

            return null;
        }

        if (panel == null) {
            panel = fLayoutBlock.getMaxConnectedPanel();
        }

        for (TrackSegment t : panel.getTrackSegments()) {
            if (t.getLayoutBlock() == fLayoutBlock) {
                PositionablePoint p = null;

                if (t.getType1() == LayoutTrack.POS_POINT) {
                    p = (PositionablePoint) t.getConnect1();

                    if (p.getType() == PositionablePoint.END_BUMPER) {
                        if (p.getEastBoundSignalMast() != null) {
                            return p.getEastBoundSignalMast();
                        }

                        if (p.getWestBoundSignalMast() != null) {
                            return p.getWestBoundSignalMast();
                        }
                    }
                }

                if (t.getType2() == LayoutTrack.POS_POINT) {
                    p = (PositionablePoint) t.getConnect2();

                    if (p.getType() == PositionablePoint.END_BUMPER) {
                        if (p.getEastBoundSignalMast() != null) {
                            return p.getEastBoundSignalMast();
                        }

                        if (p.getWestBoundSignalMast() != null) {
                            return p.getWestBoundSignalMast();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get a Sensor facing into a specific Block. This is used for
     * Blocks that have an end bumper at one end.
     */
    @CheckReturnValue
    @CheckForNull
    public Sensor getSensorAtEndBumper(
            @CheckForNull Block facingBlock,
            @CheckForNull LayoutEditor panel) {
        if (facingBlock == null) {
            log.error("null block in call to getFacingSensor");
            return null;
        }

        String facingBlockName = facingBlock.getUserName();
        if ((facingBlockName == null) || (facingBlockName.isEmpty())) {
            log.error("Block {} has no user name.", facingBlock.getDisplayName());
            return null;
        }
        LayoutBlock fLayoutBlock = getByUserName(facingBlockName);
        if (fLayoutBlock == null) {
            log.error("Block {} is not on a Layout Editor panel.", facingBlock.getDisplayName());

            return null;
        }

        if (panel == null) {
            panel = fLayoutBlock.getMaxConnectedPanel();
        }

        for (TrackSegment t : panel.getTrackSegments()) {
            if (t.getLayoutBlock() == fLayoutBlock) {
                PositionablePoint p = null;

                if (t.getType1() == LayoutTrack.POS_POINT) {
                    p = (PositionablePoint) t.getConnect1();

                    if (p.getType() == PositionablePoint.END_BUMPER) {
                        if (p.getEastBoundSensor() != null) {
                            return p.getEastBoundSensor();
                        }

                        if (p.getWestBoundSensor() != null) {
                            return p.getWestBoundSensor();
                        }
                    }
                }

                if (t.getType2() == LayoutTrack.POS_POINT) {
                    p = (PositionablePoint) t.getConnect2();

                    if (p.getType() == PositionablePoint.END_BUMPER) {
                        if (p.getEastBoundSensor() != null) {
                            return p.getEastBoundSensor();
                        }

                        if (p.getWestBoundSensor() != null) {
                            return p.getWestBoundSensor();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the named bean of either a Sensor or signalmast facing
     * into a specified Block from a specified protected Block.
     *
     * @return The assigned sensor or signal mast as a named bean
     */
    @CheckReturnValue
    @CheckForNull
    public NamedBean getFacingNamedBean(@CheckForNull Block facingBlock,
            @CheckForNull Block protectedBlock,
            @CheckForNull LayoutEditor panel) {
        NamedBean bean = getFacingBean(facingBlock, protectedBlock, panel, SignalMast.class);

        if (bean != null) {
            return bean;
        }
        bean = getFacingBean(facingBlock, protectedBlock, panel, Sensor.class);

        if (bean != null) {
            return bean;
        }
        return getFacingSignalHead(facingBlock, protectedBlock);
    }

    @CheckReturnValue
    @CheckForNull
    public SignalMast getFacingSignalMast(
            @Nonnull Block facingBlock,
            @CheckForNull Block protectedBlock) {
        return getFacingSignalMast(facingBlock, protectedBlock, null);
    }

    /**
     * Get the Signal Mast facing into a specified Block from a
     * specified protected Block.
     *
     * @return The assigned signalMast.
     */
    @CheckReturnValue
    @CheckForNull
    public SignalMast getFacingSignalMast(
            @Nonnull Block facingBlock,
            @CheckForNull Block protectedBlock,
            @CheckForNull LayoutEditor panel) {
        log.debug("calling getFacingMast on block '{}'", facingBlock.getDisplayName());
        return (SignalMast) getFacingBean(facingBlock, protectedBlock, panel, SignalMast.class);
    }

    /**
     * Get the Sensor facing into a specified Block from a
     * specified protected Block.
     *
     * @return The assigned sensor
     */
    @CheckReturnValue
    @CheckForNull
    public Sensor getFacingSensor(@CheckForNull Block facingBlock,
            @CheckForNull Block protectedBlock,
            @CheckForNull LayoutEditor panel) {
        return (Sensor) getFacingBean(facingBlock, protectedBlock, panel, Sensor.class);
    }

    /**
     * Get a facing bean into a specified Block from a specified
     * protected Block.
     *
     * @param panel the layout editor panel the block is assigned, if null then
     *              the maximum connected panel of the facing block is used
     * @param T     The class of the item that we are looking for, either
     *              SignalMast or Sensor
     * @return The assigned sensor.
     */
    @CheckReturnValue
    @CheckForNull
    public NamedBean getFacingBean(@CheckForNull Block facingBlock,
            @CheckForNull Block protectedBlock,
            @CheckForNull LayoutEditor panel, Class< ?> T) {
        //check input
        if ((facingBlock == null) || (protectedBlock == null)) {
            log.error("null block in call to getFacingSignalMast");
            return null;
        }

        if (!T.equals(SignalMast.class) && !T.equals(Sensor.class)) {
            log.error("Incorrect class type called, must be either SignalMast or Sensor");

            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("find signal mast between facing {} ({}) - protected {} ({})",
                    facingBlock.getDisplayName(), facingBlock.getDisplayName(),
                    protectedBlock.getDisplayName(), protectedBlock.getDisplayName());
        }

        //non-null - check if input corresponds to Blocks in a Layout Editor panel.
        String facingBlockName = facingBlock.getUserName();
        if ((facingBlockName == null) || facingBlockName.isEmpty()) {
            log.error("facing block has no user name");
            return null;
        }
        LayoutBlock fLayoutBlock = getByUserName(facingBlockName);
        String protectedBlockName = protectedBlock.getUserName();
        LayoutBlock pLayoutBlock = (protectedBlockName == null) ? null : getByUserName(protectedBlockName);
        if ((fLayoutBlock == null) || (pLayoutBlock == null)) {
            if (fLayoutBlock == null) {
                log.error("Block {} is not on a Layout Editor panel.", facingBlock.getDisplayName());
            }

            if (pLayoutBlock == null) {
                log.error("Block {} is not on a Layout Editor panel.", protectedBlock.getDisplayName());
            }
            return null;
        }

        //input has corresponding LayoutBlocks - does it correspond to a block boundary?
        if (panel == null) {
            panel = fLayoutBlock.getMaxConnectedPanel();
        }
        List<LayoutConnectivity> c = panel.getLEAuxTools().getConnectivityList(fLayoutBlock);
        LayoutConnectivity lc = null;
        int i = 0;
        boolean facingIsBlock1 = true;

        while ((i < c.size()) && (lc == null)) {
            LayoutConnectivity tlc = c.get(i);

            if ((tlc.getBlock1() == fLayoutBlock) && (tlc.getBlock2() == pLayoutBlock)) {
                lc = tlc;
            } else if ((tlc.getBlock1() == pLayoutBlock) && (tlc.getBlock2() == fLayoutBlock)) {
                lc = tlc;
                facingIsBlock1 = false;
            }
            i++;
        }

        if (lc == null) {
            PositionablePoint p = panel.getFinder().findPositionableLinkPoint(fLayoutBlock);

            if (p == null) {
                p = panel.getFinder().findPositionableLinkPoint(pLayoutBlock);
            }

            if ((p != null) && (p.getLinkedEditor() != null)) {
                return getFacingBean(facingBlock, protectedBlock, p.getLinkedEditor(), T);
            }
            log.debug("Block {} is not connected to Block {} on panel {}", facingBlock.getDisplayName(),
                    protectedBlock.getDisplayName(), panel.getLayoutName());

            return null;
        }
        LayoutTurnout lt = null;
        LayoutTrack connected = lc.getConnectedObject();

        TrackSegment tr = lc.getTrackSegment();
        int cType = lc.getConnectedType();

        if (connected == null) {
            if (lc.getXover() != null) {
                if (lc.getXoverBoundaryType() == LayoutConnectivity.XOVER_BOUNDARY_AB) {
                    if (fLayoutBlock == lc.getXover().getLayoutBlock()) {
                        cType = LayoutTrack.TURNOUT_A;
                    } else {
                        cType = LayoutTrack.TURNOUT_B;
                    }
                    connected = lc.getXover();
                } else if (lc.getXoverBoundaryType() == LayoutConnectivity.XOVER_BOUNDARY_CD) {
                    if (fLayoutBlock == lc.getXover().getLayoutBlockC()) {
                        cType = LayoutTrack.TURNOUT_C;
                    } else {
                        cType = LayoutTrack.TURNOUT_D;
                    }
                    connected = lc.getXover();
                } else if (lc.getXoverBoundaryType() == LayoutConnectivity.XOVER_BOUNDARY_AC) {
                    if (fLayoutBlock == lc.getXover().getLayoutBlock()) {
                        cType = LayoutTrack.TURNOUT_A;
                    } else {
                        cType = LayoutTrack.TURNOUT_C;
                    }
                    connected = lc.getXover();
                } else if (lc.getXoverBoundaryType() == LayoutConnectivity.XOVER_BOUNDARY_BD) {
                    if (fLayoutBlock == lc.getXover().getLayoutBlockB()) {
                        cType = LayoutTrack.TURNOUT_B;
                    } else {
                        cType = LayoutTrack.TURNOUT_D;
                    }
                    connected = lc.getXover();
                }
            }
        }

        if (connected == null) {
            log.error("No connectivity object found between Blocks {}, {} {}", facingBlock.getDisplayName(),
                    protectedBlock.getDisplayName(), cType);

            return null;
        }

        if (cType == LayoutTrack.TRACK) {
            //block boundary is at an Anchor Point
            PositionablePoint p = panel.getFinder().findPositionablePointAtTrackSegments(tr, (TrackSegment) connected);

            boolean block1IsWestEnd = LayoutEditorTools.isAtWestEndOfAnchor(tr, p);
            log.debug("Track is west end? {}", block1IsWestEnd);
            if ((block1IsWestEnd && facingIsBlock1) || (!block1IsWestEnd && !facingIsBlock1)) {
                //block1 is on the west (north) end of the block boundary
                if (T.equals(SignalMast.class)) {
                    return p.getEastBoundSignalMast();
                } else if (T.equals(Sensor.class)) {
                    return p.getEastBoundSensor();
                }
            } else {
                if (T.equals(SignalMast.class)) {
                    return p.getWestBoundSignalMast();
                } else if (T.equals(Sensor.class)) {
                    return p.getWestBoundSensor();
                }
            }
        }

        if (cType == LayoutTrack.TURNOUT_A) {
            lt = (LayoutTurnout) connected;

            if ((lt.getLinkType() == LayoutTurnout.NO_LINK) || (lt.getLinkType() == LayoutTurnout.FIRST_3_WAY)) {
                if ((T.equals(SignalMast.class) && (lt.getSignalAMast() != null))
                        || (T.equals(Sensor.class) && (lt.getSensorA() != null))) {
                    if (tr == null) {
                        if (lt.getConnectA() instanceof TrackSegment) {
                            TrackSegment t = (TrackSegment) lt.getConnectA();

                            if ((t.getLayoutBlock() != null) && (t.getLayoutBlock() == lt.getLayoutBlock())) {
                                if (T.equals(SignalMast.class)) {
                                    return lt.getSignalAMast();
                                } else if (T.equals(Sensor.class)) {
                                    return lt.getSensorA();
                                }
                            }
                        }
                    } else if (tr.getLayoutBlock().getBlock() == facingBlock) {
                        if (T.equals(SignalMast.class)) {
                            return lt.getSignalAMast();
                        } else if (T.equals(Sensor.class)) {
                            return lt.getSensorA();
                        }
                    }
                }
            }
            return null;
        }

        if (cType == LayoutTrack.TURNOUT_B) {
            lt = (LayoutTurnout) connected;

            if ((T.equals(SignalMast.class) && (lt.getSignalBMast() != null))
                    || (T.equals(Sensor.class) && (lt.getSensorB() != null))) {
                if (tr == null) {
                    if (lt.getConnectB() instanceof TrackSegment) {
                        TrackSegment t = (TrackSegment) lt.getConnectB();

                        if ((t.getLayoutBlock() != null) && (t.getLayoutBlock() == lt.getLayoutBlockB())) {
                            if (T.equals(SignalMast.class)) {
                                return lt.getSignalBMast();
                            } else if (T.equals(Sensor.class)) {
                                return lt.getSensorB();
                            }
                        }
                    }
                } else if (tr.getLayoutBlock().getBlock() == facingBlock) {
                    if (T.equals(SignalMast.class)) {
                        return lt.getSignalBMast();
                    } else if (T.equals(Sensor.class)) {
                        return lt.getSensorB();
                    }
                }
            }
            return null;
        }

        if (cType == LayoutTrack.TURNOUT_C) {
            lt = (LayoutTurnout) connected;

            if ((T.equals(SignalMast.class) && (lt.getSignalCMast() != null))
                    || (T.equals(Sensor.class) && (lt.getSensorC() != null))) {
                if (tr == null) {
                    if (lt.getConnectC() instanceof TrackSegment) {
                        TrackSegment t = (TrackSegment) lt.getConnectC();

                        if ((t.getLayoutBlock() != null) && (t.getLayoutBlock() == lt.getLayoutBlockC())) {
                            if (T.equals(SignalMast.class)) {
                                return lt.getSignalCMast();
                            } else if (T.equals(Sensor.class)) {
                                return lt.getSensorC();
                            }
                        }
                    }
                } else if (tr.getLayoutBlock().getBlock() == facingBlock) {
                    if (T.equals(SignalMast.class)) {
                        return lt.getSignalCMast();
                    } else if (T.equals(Sensor.class)) {
                        return lt.getSensorC();
                    }
                }
            }
            return null;
        }

        if (cType == LayoutTrack.TURNOUT_D) {
            lt = (LayoutTurnout) connected;

            if ((T.equals(SignalMast.class) && (lt.getSignalDMast() != null))
                    || (T.equals(Sensor.class) && (lt.getSensorD() != null))) {
                if (tr == null) {
                    if (lt.getConnectD() instanceof TrackSegment) {
                        TrackSegment t = (TrackSegment) lt.getConnectD();

                        if ((t.getLayoutBlock() != null) && (t.getLayoutBlock() == lt.getLayoutBlockD())) {
                            if (T.equals(SignalMast.class)) {
                                return lt.getSignalDMast();
                            } else if (T.equals(Sensor.class)) {
                                return lt.getSensorD();
                            }
                        }
                    }
                } else if (tr.getLayoutBlock().getBlock() == facingBlock) {
                    if (T.equals(SignalMast.class)) {
                        return lt.getSignalDMast();
                    } else if (T.equals(Sensor.class)) {
                        return lt.getSensorD();
                    }
                }
            }
            return null;
        }

        if ((tr == null) || (tr.getLayoutBlock().getBlock() != facingBlock)) {
            return null;
        }

        if ((cType >= LayoutTrack.SLIP_A) && (cType <= LayoutTrack.SLIP_D)) {
            LayoutSlip ls = (LayoutSlip) connected;

            if (cType == LayoutTrack.SLIP_A) {
                if (T.equals(SignalMast.class)) {
                    return ls.getSignalAMast();
                } else if (T.equals(Sensor.class)) {
                    return ls.getSensorA();
                }
            }

            if (cType == LayoutTrack.SLIP_B) {
                if (T.equals(SignalMast.class)) {
                    return ls.getSignalBMast();
                } else if (T.equals(Sensor.class)) {
                    return ls.getSensorB();
                }
            }

            if (cType == LayoutTrack.SLIP_C) {
                if (T.equals(SignalMast.class)) {
                    return ls.getSignalCMast();
                } else if (T.equals(Sensor.class)) {
                    return ls.getSensorC();
                }
            }

            if (cType == LayoutTrack.SLIP_D) {
                if (T.equals(SignalMast.class)) {
                    return ls.getSignalDMast();
                } else if (T.equals(Sensor.class)) {
                    return ls.getSensorD();
                }
            }
        }

        if ((cType < LayoutTrack.LEVEL_XING_A) || (cType > LayoutTrack.LEVEL_XING_D)) {
            log.error("Block Boundary not identified correctly - Blocks {}, {}", facingBlock.getDisplayName(),
                    protectedBlock.getDisplayName());

            return null;
        }

        /* We don't allow signal masts on the block outward facing from the level
		   xing, nor do we consider the signal mast, that is protecting the in block on the xing */
        LevelXing xing = (LevelXing) connected;

        if (cType == LayoutTrack.LEVEL_XING_A) {
            //block boundary is at the A connection of a level crossing
            if (T.equals(SignalMast.class)) {
                return xing.getSignalAMast();
            } else if (T.equals(Sensor.class)) {
                return xing.getSensorA();
            }
        }

        if (cType == LayoutTrack.LEVEL_XING_B) {
            //block boundary is at the B connection of a level crossing
            if (T.equals(SignalMast.class)) {
                return xing.getSignalBMast();
            } else if (T.equals(Sensor.class)) {
                return xing.getSensorB();
            }
        }

        if (cType == LayoutTrack.LEVEL_XING_C) {
            //block boundary is at the C connection of a level crossing
            if (T.equals(SignalMast.class)) {
                return xing.getSignalCMast();
            } else if (T.equals(Sensor.class)) {
                return xing.getSensorC();
            }
        }

        if (cType == LayoutTrack.LEVEL_XING_D) {
            if (T.equals(SignalMast.class)) {
                return xing.getSignalDMast();
            } else if (T.equals(Sensor.class)) {
                return xing.getSensorD();
            }
        }
        return null;
    }	//getFacingBean

    /**
     * In the first instance get a Signal Mast or if none exists a Signal
     * Head for a given facing block and protected block combination. See
     * #getFacingSignalMast() and #getFacingSignalHead() as to how they deal with what
     * each returns.
     *
     * @return either a signalMast or signalHead
     */
    @CheckReturnValue
    @CheckForNull
    public Object getFacingSignalObject(
            @Nonnull Block facingBlock,
            @CheckForNull Block protectedBlock) {
        Object sig = getFacingSignalMast(facingBlock, protectedBlock, null);

        if (sig != null) {
            return sig;
        }
        sig = getFacingSignalHead(facingBlock, protectedBlock);
        return sig;
    }

    /**
     * Get the block that a given bean object (Sensor, SignalMast
     * or SignalHead) is protecting.
     *
     * @param nb    NamedBean
     * @param panel  panel that this bean is on
     * @return The block that the bean object is facing
     */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getProtectedBlockByNamedBean(
            @CheckForNull NamedBean nb,
            @CheckForNull LayoutEditor panel) {
        if (nb instanceof SignalHead) {
            return getProtectedBlock((SignalHead) nb, panel);
        }
        List<LayoutBlock> proBlocks = getProtectingBlocksByBean(nb, panel);

        if (proBlocks.isEmpty()) {
            return null;
        }
        return proBlocks.get(0);
    }	//getProtectedBlockByNamedBean

    @CheckReturnValue
    @Nonnull
    public List<LayoutBlock> getProtectingBlocksByNamedBean(
            @CheckForNull NamedBean nb,
            @CheckForNull LayoutEditor panel) {
        ArrayList<LayoutBlock> ret = new ArrayList<>();

        if (nb instanceof SignalHead) {
            ret.add(getProtectedBlock((SignalHead) nb, panel));
            return ret;
        }
        return getProtectingBlocksByBean(nb, panel);
    }

    /**
     * If the panel variable is null, search all LE panels.
     * This was added to support multi panel entry/exit.
     *
     * @param bean  The sensor, mast or head to be located.
     * @param panel The panel to search. If null, search all LE panels.
     * @return a list of protected layout blocks.
     */
    @Nonnull
    private List<LayoutBlock> getProtectingBlocksByBean(
            @CheckForNull NamedBean bean,
            @CheckForNull LayoutEditor panel) {
        if (panel == null) {
            List<LayoutEditor> panels = InstanceManager.getDefault(jmri.jmrit.display.PanelMenu.class)
                    .getLayoutEditorPanelList();
            List<LayoutBlock> protectingBlocks = new ArrayList<>();
            for (LayoutEditor p : panels) {
                protectingBlocks = getProtectingBlocksByBeanByPanel(bean, p);
                if (!protectingBlocks.isEmpty()) {
                    break;
                }
            }
            return protectingBlocks;
        } else {
            return getProtectingBlocksByBeanByPanel(bean, panel);
        }
    }

    @Nonnull
    private List<LayoutBlock> getProtectingBlocksByBeanByPanel(
            @CheckForNull NamedBean bean,
            @CheckForNull LayoutEditor panel) {
        List<LayoutBlock> protectingBlocks = new ArrayList<>();

        if (!(bean instanceof SignalMast) && !(bean instanceof Sensor)) {
            log.error("Incorrect class type called, must be either SignalMast or Sensor");

            return protectingBlocks;
        }

        PositionablePoint pp = panel.getFinder().findPositionablePointByEastBoundBean(bean);
        TrackSegment tr = null;
        boolean east = true;

        if (pp == null) {
            pp = panel.getFinder().findPositionablePointByWestBoundBean(bean);
            east = false;
        }

        if (pp != null) {
            //   LayoutEditorTools tools = panel.getLETools(); //TODO: Dead-code strip this

            if (east) {
                if (LayoutEditorTools.isAtWestEndOfAnchor(pp.getConnect1(), pp)) {
                    tr = pp.getConnect2();
                } else {
                    tr = pp.getConnect1();
                }
            } else {
                if (LayoutEditorTools.isAtWestEndOfAnchor(pp.getConnect1(), pp)) {
                    tr = pp.getConnect1();
                } else {
                    tr = pp.getConnect2();
                }
            }

            if (tr != null) {
                protectingBlocks.add(tr.getLayoutBlock());

                return protectingBlocks;
            }
        }

        LevelXing l = panel.getFinder().findLevelXingByBean(bean);

        if (l != null) {
            if (bean instanceof SignalMast) {
                if (l.getSignalAMast() == bean) {
                    protectingBlocks.add(l.getLayoutBlockAC());
                } else if (l.getSignalBMast() == bean) {
                    protectingBlocks.add(l.getLayoutBlockBD());
                } else if (l.getSignalCMast() == bean) {
                    protectingBlocks.add(l.getLayoutBlockAC());
                } else {
                    protectingBlocks.add(l.getLayoutBlockBD());
                }
            } else if (bean instanceof Sensor) {
                if (l.getSensorA() == bean) {
                    protectingBlocks.add(l.getLayoutBlockAC());
                } else if (l.getSensorB() == bean) {
                    protectingBlocks.add(l.getLayoutBlockBD());
                } else if (l.getSensorC() == bean) {
                    protectingBlocks.add(l.getLayoutBlockAC());
                } else {
                    protectingBlocks.add(l.getLayoutBlockBD());
                }
            }
            return protectingBlocks;
        }

        LayoutSlip ls = panel.getFinder().findLayoutSlipByBean(bean);

        if (ls != null) {
            protectingBlocks.add(ls.getLayoutBlock());

            return protectingBlocks;
        }

        LayoutTurnout t = panel.getFinder().findLayoutTurnoutByBean(bean);

        if (t != null) {
            return t.getProtectedBlocks(bean);
        }
        return protectingBlocks;
    }	//getProtectingBlocksByBean

    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getProtectedBlockByMast(
            @CheckForNull SignalMast signalMast,
            @CheckForNull LayoutEditor panel) {
        List<LayoutBlock> proBlocks = getProtectingBlocksByBean(signalMast, panel);

        if (proBlocks.isEmpty()) {
            return null;
        }
        return proBlocks.get(0);
    }

    /**
     * Get the LayoutBlock that a given sensor is protecting.
     */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getProtectedBlockBySensor(
            @Nonnull String sensorName,
            @CheckForNull LayoutEditor panel) {
        Sensor sensor = InstanceManager.sensorManagerInstance().getSensor(sensorName);

        return getProtectedBlockBySensor(sensor, panel);
    }

    @Nonnull
    public List<LayoutBlock> getProtectingBlocksBySensor(
            @CheckForNull Sensor sensor, @CheckForNull LayoutEditor panel) {
        return getProtectingBlocksByBean(sensor, panel);
    }

    @Nonnull
    public List<LayoutBlock> getProtectingBlocksBySensorOld(
            @CheckForNull Sensor sensor, @Nonnull LayoutEditor panel) {
        List<LayoutBlock> result = new ArrayList<>();
        PositionablePoint pp = panel.getFinder().findPositionablePointByEastBoundBean(sensor);
        TrackSegment tr;
        boolean east = true;

        if (pp == null) {
            pp = panel.getFinder().findPositionablePointByWestBoundBean(sensor);
            east = false;
        }

        if (pp != null) {
            //            LayoutEditorTools tools = panel.getLETools(); //TODO: Dead-code strip this

            if (east) {
                if (LayoutEditorTools.isAtWestEndOfAnchor(pp.getConnect1(), pp)) {
                    tr = pp.getConnect2();
                } else {
                    tr = pp.getConnect1();
                }
            } else {
                if (LayoutEditorTools.isAtWestEndOfAnchor(pp.getConnect1(), pp)) {
                    tr = pp.getConnect1();
                } else {
                    tr = pp.getConnect2();
                }
            }

            if (tr != null) {
                result.add(tr.getLayoutBlock());

                return result;
            }
        }

        LevelXing l = panel.getFinder().findLevelXingByBean(sensor);

        if (l != null) {
            if (l.getSensorA() == sensor) {
                result.add(l.getLayoutBlockAC());
            } else if (l.getSensorB() == sensor) {
                result.add(l.getLayoutBlockBD());
            } else if (l.getSensorC() == sensor) {
                result.add(l.getLayoutBlockAC());
            } else {
                result.add(l.getLayoutBlockBD());
            }
            return result;
        }
        LayoutSlip ls = panel.getFinder().findLayoutSlipByBean(sensor);

        if (ls != null) {
            result.add(ls.getLayoutBlock());

            return result;
        }
        LayoutTurnout t = panel.getFinder().findLayoutTurnoutByBean(sensor);

        if (t != null) {
            return t.getProtectedBlocks(sensor);
        }
        return result;
    }	//getProtectingBlocksBySensorOld

    /**
     * Get the LayoutBlock that a given sensor is protecting.
     */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getProtectedBlockBySensor(
            @CheckForNull Sensor sensor, @CheckForNull LayoutEditor panel) {
        List<LayoutBlock> proBlocks = getProtectingBlocksByBean(sensor, panel);

        if (proBlocks.isEmpty()) {
            return null;
        }
        return proBlocks.get(0);
    }

    /**
     * Get the block facing a given bean object (Sensor, SignalMast
     * or SignalHead).
     *
     * @param nb    NamedBean
     * @param panel  panel that this bean is on
     * @return The block that the bean object is facing
     */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getFacingBlockByNamedBean(
            @Nonnull NamedBean nb, @CheckForNull LayoutEditor panel) {
        if (nb instanceof SignalHead) {
            return getFacingBlock((SignalHead) nb, panel);
        }
        return getFacingBlockByBean(nb, panel);
    }

    /**
     * Get the LayoutBlock that a given sensor is facing.
     */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getFacingBlockBySensor(@Nonnull String sensorName,
            @CheckForNull LayoutEditor panel) {
        Sensor sensor = InstanceManager.sensorManagerInstance().getSensor(sensorName);
        return (sensor == null) ? null : getFacingBlockBySensor(sensor, panel);
    }

    /**
     * Get the LayoutBlock that a given signal is facing.
     */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getFacingBlockByMast(
            @Nonnull SignalMast signalMast,
            @Nonnull LayoutEditor panel) {
        return getFacingBlockByBean(signalMast, panel);
    }

    /**
     * If the panel variable is null, search all LE panels.
     * This was added to support multi panel entry/exit.
     * @param bean  The sensor, mast or head to be located.
     * @param panel The panel to search. Search all LE panels if null.
     * @return the facing layout block.
     */
    @CheckReturnValue
    @CheckForNull
    private LayoutBlock getFacingBlockByBean(
            @Nonnull NamedBean bean,
            LayoutEditor panel) {
        if (panel == null) {
            List<LayoutEditor> panels = InstanceManager.getDefault(jmri.jmrit.display.PanelMenu.class).
                    getLayoutEditorPanelList();
            LayoutBlock returnBlock = null;
            for (LayoutEditor p : panels) {
                returnBlock = getFacingBlockByBeanByPanel(bean, p);
                if (returnBlock != null) {
                    break;
                }
            }
            return returnBlock;
        } else {
            return getFacingBlockByBeanByPanel(bean, panel);
        }
    }

    @CheckReturnValue
    @CheckForNull
    private LayoutBlock getFacingBlockByBeanByPanel(
            @Nonnull NamedBean bean,
            @Nonnull LayoutEditor panel) {
        PositionablePoint pp = panel.getFinder().findPositionablePointByEastBoundBean(bean);
        TrackSegment tr = null;
        boolean east = true;

        //Don't think that the logic for this is the right way round
        if (pp == null) {
            pp = panel.getFinder().findPositionablePointByWestBoundBean(bean);
            east = false;
        }

        if (pp != null) {
            // LayoutEditorTools tools = panel.getLETools(); //TODO: Dead-code strip this

            if (east) {
                if (LayoutEditorTools.isAtWestEndOfAnchor(pp.getConnect1(), pp)) {
                    tr = pp.getConnect1();
                } else {
                    tr = pp.getConnect2();
                }
            } else {
                if (LayoutEditorTools.isAtWestEndOfAnchor(pp.getConnect1(), pp)) {
                    tr = pp.getConnect2();
                } else {
                    tr = pp.getConnect1();
                }
            }

            if (tr != null) {
                log.debug("found facing block by positionable point");

                return tr.getLayoutBlock();
            }
        }
        LayoutTurnout t = panel.getFinder().findLayoutTurnoutByBean(bean);

        if (t != null) {
            log.debug("found signalmast at turnout {}", t.getTurnout().getDisplayName());
            Object connect = null;

            if (bean instanceof SignalMast) {
                if (t.getSignalAMast() == bean) {
                    connect = t.getConnectA();
                } else if (t.getSignalBMast() == bean) {
                    connect = t.getConnectB();
                } else if (t.getSignalCMast() == bean) {
                    connect = t.getConnectC();
                } else {
                    connect = t.getConnectD();
                }
            } else if (bean instanceof Sensor) {
                if (t.getSensorA() == bean) {
                    connect = t.getConnectA();
                } else if (t.getSensorB() == bean) {
                    connect = t.getConnectB();
                } else if (t.getSensorC() == bean) {
                    connect = t.getConnectC();
                } else {
                    connect = t.getConnectD();
                }
            }

            if (connect instanceof TrackSegment) {
                tr = (TrackSegment) connect;
                log.debug("return block {}", tr.getLayoutBlock().getDisplayName());

                return tr.getLayoutBlock();
            }
        }

        LevelXing l = panel.getFinder().findLevelXingByBean(bean);

        if (l != null) {
            Object connect = null;

            if (bean instanceof SignalMast) {
                if (l.getSignalAMast() == bean) {
                    connect = l.getConnectA();
                } else if (l.getSignalBMast() == bean) {
                    connect = l.getConnectB();
                } else if (l.getSignalCMast() == bean) {
                    connect = l.getConnectC();
                } else {
                    connect = l.getConnectD();
                }
            } else if (bean instanceof Sensor) {
                if (l.getSensorA() == bean) {
                    connect = l.getConnectA();
                } else if (l.getSensorB() == bean) {
                    connect = l.getConnectB();
                } else if (l.getSensorC() == bean) {
                    connect = l.getConnectC();
                } else {
                    connect = l.getConnectD();
                }
            }

            if (connect instanceof TrackSegment) {
                tr = (TrackSegment) connect;
                log.debug("return block {}", tr.getLayoutBlock().getDisplayName());

                return tr.getLayoutBlock();
            }
        }

        LayoutSlip ls = panel.getFinder().findLayoutSlipByBean(bean);

        if (ls != null) {
            Object connect = null;

            if (bean instanceof SignalMast) {
                if (ls.getSignalAMast() == bean) {
                    connect = ls.getConnectA();
                } else if (ls.getSignalBMast() == bean) {
                    connect = ls.getConnectB();
                } else if (ls.getSignalCMast() == bean) {
                    connect = ls.getConnectC();
                } else {
                    connect = ls.getConnectD();
                }
            } else if (bean instanceof Sensor) {
                if (ls.getSensorA() == bean) {
                    connect = ls.getConnectA();
                } else if (ls.getSensorB() == bean) {
                    connect = ls.getConnectB();
                } else if (ls.getSensorC() == bean) {
                    connect = ls.getConnectC();
                } else {
                    connect = ls.getConnectD();
                }
            }

            if (connect instanceof TrackSegment) {
                tr = (TrackSegment) connect;
                log.debug("return block {}", tr.getLayoutBlock().getDisplayName());

                return tr.getLayoutBlock();
            }
        }
        return null;
    }	//getFacingBlockByBean

    /**
     * Get the LayoutBlock that a given sensor is facing.
     */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getFacingBlockBySensor(
            @Nonnull Sensor sensor,
            @Nonnull LayoutEditor panel) {
        return getFacingBlockByBean(sensor, panel);
    }

    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getProtectedBlock(
            @Nonnull SignalHead signalHead, @CheckForNull LayoutEditor panel) {
        String userName = signalHead.getUserName();
        LayoutBlock protect = (userName == null) ? null : getProtectedBlock(userName, panel);

        if (protect == null) {
            protect = getProtectedBlock(signalHead.getSystemName(), panel);
        }
        return protect;
    }

    /**
     * Get the LayoutBlock that a given signal is protecting.
     */
    /* @TODO This needs to be expanded to cover turnouts and level crossings. */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getProtectedBlock(
            @Nonnull String signalName, @Nonnull LayoutEditor panel) {
        PositionablePoint pp = panel.getFinder().findPositionablePointByEastBoundSignal(signalName);
        TrackSegment tr;

        if (pp == null) {
            pp = panel.getFinder().findPositionablePointByWestBoundSignal(signalName);

            if (pp == null) {
                return null;
            }
            tr = pp.getConnect1();
        } else {
            tr = pp.getConnect2();
        }

        //tr = pp.getConnect2();
        if (tr == null) {
            return null;
        }
        return tr.getLayoutBlock();
    }

    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getFacingBlock(
            @Nonnull SignalHead signalHead, @CheckForNull LayoutEditor panel) {
        String userName = signalHead.getUserName();
        LayoutBlock facing = (userName == null) ? null : getFacingBlock(userName, panel);
        if (facing == null) {
            facing = getFacingBlock(signalHead.getSystemName(), panel);
        }
        return facing;
    }

    /**
     * Get the LayoutBlock that a given signal is facing.
     */
    /* @TODO This needs to be expanded to cover turnouts and level crossings. */
    @CheckReturnValue
    @CheckForNull
    public LayoutBlock getFacingBlock(
            @Nonnull String signalName, @Nonnull LayoutEditor panel) {
        PositionablePoint pp = panel.getFinder().findPositionablePointByWestBoundSignal(signalName);
        TrackSegment tr;

        if (pp == null) {
            pp = panel.getFinder().findPositionablePointByWestBoundSignal(signalName);

            if (pp == null) {
                return null;
            }
            tr = pp.getConnect1();
        } else {
            tr = pp.getConnect2();
        }

        if (tr == null) {
            return null;
        }
        return tr.getLayoutBlock();
    }

    private boolean warnConnectivity = true;

    /**
     * Controls switching off incompatible block connectivity messages.
     * <p>
     * Warnings are always on when program starts up. Once stopped by the user,
     * these messages may not be switched on again until program restarts.
     */
    public boolean warn() {
        return warnConnectivity;
    }

    public void turnOffWarning() {
        warnConnectivity = false;
    }

    protected boolean enableAdvancedRouting = false;

    /**
     * @return true if advanced layout block routing has been enabled
     */
    public boolean isAdvancedRoutingEnabled() {
        return enableAdvancedRouting;
    }

    /**
     * Enable the advanced layout block routing protocol
     * <p>
     * The block routing protocol enables each layout block to build up a list
     * of all reachable blocks, along with how far away they are, which
     * direction they are in and which of the connected blocks they are
     * reachable from.
     */
    private long firstRoutingChange;

    public void enableAdvancedRouting(boolean boo) {
        if (boo == enableAdvancedRouting) {
            return;
        }
        enableAdvancedRouting = boo;

        if (boo && initialized) {
            initializeLayoutBlockRouting();
        }
        firePropertyChange("advancedRoutingEnabled", !enableAdvancedRouting, enableAdvancedRouting);
    }

    private void initializeLayoutBlockRouting() {
        if (!enableAdvancedRouting || !initialized) {
            log.debug("initializeLayoutBlockRouting immediate return due to {} {}", enableAdvancedRouting, initialized);

            return;
        }
        firstRoutingChange = System.nanoTime();

        //cycle through all LayoutBlocks, completing initialization of the layout block routing
        java.util.Enumeration<LayoutBlock> en = _tsys.elements();

        while (en.hasMoreElements()) {
            en.nextElement().initializeLayoutBlockRouting();
        }
    }

    @Nonnull
    public LayoutBlockConnectivityTools getLayoutBlockConnectivityTools() {
        return lbct;
    }

    LayoutBlockConnectivityTools lbct = new LayoutBlockConnectivityTools();

    private long lastRoutingChange;

    void setLastRoutingChange() {
        log.debug("setLastRoutingChange");
        lastRoutingChange = System.nanoTime();
        stabilised = false;
        setRoutingStabilised();
    }

    boolean checking = false;
    boolean stabilised = false;

    private void setRoutingStabilised() {
        if (checking) {
            return;
        }
        log.debug("routing table change has been initiated");
        checking = true;

        if (namedStabilisedIndicator != null) {
            try {
                namedStabilisedIndicator.getBean().setState(Sensor.INACTIVE);
            } catch (jmri.JmriException ex) {
                log.debug("Error setting stability indicator sensor");
            }
        }
        Runnable r = () -> {
            try {
                firePropertyChange("topology", true, false);
                long oldvalue = lastRoutingChange;

                while (!stabilised) {
                    Thread.sleep(2000L);	//two seconds

                    if (oldvalue == lastRoutingChange) {
                        log.debug("routing table has now been stable for 2 seconds");
                        checking = false;
                        stabilised = true;
                        jmri.util.ThreadingUtil.runOnLayoutEventually(() -> {
                            firePropertyChange("topology", false, true);
                        });

                        if (namedStabilisedIndicator != null) {
                            jmri.util.ThreadingUtil.runOnLayoutEventually(() -> {
                                log.debug("Setting StabilisedIndicator Sensor {} ACTIVE",
                                        namedStabilisedIndicator.getBean().getDisplayName());
                                try {
                                    namedStabilisedIndicator.getBean().setState(Sensor.ACTIVE);
                                } catch (jmri.JmriException ex) {
                                    log.debug("Error setting stability indicator sensor");
                                }
                            });
                        } else {
                            log.debug("Stable, no sensor to set");
                        }
                    } else {
                        long seconds = (long) ((lastRoutingChange - firstRoutingChange) / 1e9);
                        log.debug("routing table not stable after {} in {}",
                                String.format("%d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60),
                                Thread.currentThread().getName());
                    }
                    oldvalue = lastRoutingChange;
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                checking = false;

        //} catch (jmri.JmriException ex) {
        //log.debug("Error setting stability indicator sensor");
            }
        };
        thr = new Thread(r, "Routing stabilising timer");
        thr.start();
    }	//setRoutingStabilised

    private Thread thr = null;

    private NamedBeanHandle<Sensor> namedStabilisedIndicator;

    /**
     * Assign a sensor to the routing protocol, that changes state dependant
     * upon if the routing protocol has stabilised or is under going a change.
     */
    public void setStabilisedSensor(@Nonnull String pName) throws jmri.JmriException {
        if (InstanceManager.getNullableDefault(jmri.SensorManager.class) != null) {
            try {
                Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(pName);
                namedStabilisedIndicator = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(
                        pName,
                        sensor);
                try {
                    if (stabilised) {
                        sensor.setState(Sensor.ACTIVE);
                    } else {
                        sensor.setState(Sensor.INACTIVE);
                    }
                } catch (jmri.JmriException ex) {
                    log.error("Error setting stablilty indicator sensor");
                }
            } catch (IllegalArgumentException ex) {
                log.error("Sensor '{}' not available", pName);
                throw new jmri.JmriException("Sensor '" + pName + "' not available");
            }
        } else {
            log.error("No SensorManager for this protocol");
            throw new jmri.JmriException("No Sensor Manager Found");
        }
    }

    /**
     * Get the sensor used to indicate if the routing protocol has stabilised
     * or not.
     */
    public Sensor getStabilisedSensor() {
        if (namedStabilisedIndicator == null) {
            return null;
        }
        return namedStabilisedIndicator.getBean();
    }

    /**
     * Get the sensor used for the stability indication
     */
    @CheckReturnValue
    @CheckForNull
    public NamedBeanHandle<Sensor> getNamedStabilisedSensor() {
        return namedStabilisedIndicator;
    }

    /**
     * @return true if the layout block routing protocol has stabilised
     */
    public boolean routingStablised() {
        return stabilised;
    }

    /**
     * @return the time when the last routing change was made, recorded as
     *         System.nanoTime()
     */
    public long getLastRoutingChange() {
        return lastRoutingChange;
    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameLayoutBlocks" : "BeanNameLayoutBlock");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<LayoutBlock> getNamedBeanClass() {
        return LayoutBlock.class;
    }

    /**
     * Get a list of layout blocks which this roster entry appears to be
     * occupying. A layout block is assumed to contain this roster entry if the
     * value of the underlying block is the RosterEntry itself, or a string with
     * the entry's id or dcc address.
     *
     * @param re the roster entry
     * @return list of layout block user names
     */
    @Nonnull
    public List<LayoutBlock> getLayoutBlocksOccupiedByRosterEntry(
            @Nonnull RosterEntry re) {
        List<LayoutBlock> result = new ArrayList<>();

        BlockManager bm = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
        List<Block> blockList = bm.getBlocksOccupiedByRosterEntry(re);
        for (Block block : blockList) {
            String uname = block.getUserName();
            if (uname != null) {
                LayoutBlock lb = getByUserName(uname);
                if (lb != null) {
                    result.add(lb);
                }
            }
        }
        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutBlockManager.class);

}
