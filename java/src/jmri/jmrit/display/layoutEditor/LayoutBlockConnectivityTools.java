package jmri.jmrit.display.layoutEditor;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import jmri.Block;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.jmrit.display.PanelMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * These are a series of layout block connectivity tools that can be used when
 * the advanced layout block routing has been enabled. These tools can determine
 * if a path from a source to destination bean is valid. If a route between two
 * layout blocks is usable and free.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class LayoutBlockConnectivityTools {

    public LayoutBlockConnectivityTools() {

    }

    /**
     * Constant used in the getLayoutBlocks to represent a path from one Signal
     * Mast to another and that no mast should be in the path.
     */
    public final static int MASTTOMAST = 0x01;

    /**
     * Constant used in the getLayoutBlocks to represent a path from one Signal
     * Head to another and that no head should be in the path.
     */
    public final static int HEADTOHEAD = 0x02;

    /**
     * Constant used in the getLayoutBlocks to represent a path from one Sensor
     * to another and that no sensor should be in the path.
     */
    public final static int SENSORTOSENSOR = 0x04;

    /**
     * Constant used in the getLayoutBlocks to represent a path from either a
     * Signal Mast or Head to another Signal Mast or Head and that no mast of
     * head should be in the path.
     */
    public final static int ANY = 0x08;

    /**
     * Constant used in the getLayoutBlocks to indicate that the the system
     * should not check for signal masts or heads on the path.
     */
    public final static int NONE = 0x00;

    public final static int HOPCOUNT = 0x00;
    public final static int METRIC = 0x01;
    public final static int DISTANCE = 0x02;
    private static int ttlSize = 50;

    /**
     * Determines if a pair of NamedBeans (Signalhead, Signalmast or Sensor)
     * assigned to a block boundary are reachable.
     *
     * @return true if source and destination beans are reachable, or false if
     *         they are not
     * @throws jmri.JmriException if no blocks can be found that related to the
     *                            named beans.
     */
    public boolean checkValidDest(NamedBean sourceBean, NamedBean destBean, int pathMethod) throws jmri.JmriException {
        if (log.isDebugEnabled()) {
            log.debug("check valid des with source/dest bean {} {}", sourceBean.getDisplayName(), destBean.getDisplayName());
        }
        LayoutBlock facingBlock = null;
        LayoutBlock protectingBlock = null;
        LayoutBlock destFacingBlock = null;
        List<LayoutBlock> destProtectBlock = null;
        List<LayoutEditor> layout = InstanceManager.getDefault(PanelMenu.class).getLayoutEditorPanelList();
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        for (int i = 0; i < layout.size(); i++) {
            if (log.isDebugEnabled()) {
                log.debug("Layout name {}", layout.get(i).getLayoutName());
            }
            if (facingBlock == null) {
                facingBlock = lbm.getFacingBlockByNamedBean(sourceBean, layout.get(i));
            }
            if (protectingBlock == null) {
                protectingBlock = lbm.getProtectedBlockByNamedBean(sourceBean, layout.get(i));
            }
            if (destFacingBlock == null) {
                destFacingBlock = lbm.getFacingBlockByNamedBean(destBean, layout.get(i));
            }
            if (destProtectBlock == null) {
                destProtectBlock = lbm.getProtectingBlocksByNamedBean(destBean, layout.get(i));
            }
            if ((destFacingBlock != null) && (facingBlock != null) && (protectingBlock != null)) {
                /*Destination protecting block is allowed to be null, as the destination signalmast
                 could be assigned to an end bumper */
                //A simple to check to see if the remote signal is in the correct direction to ours.
                try {
                    return checkValidDest(facingBlock, protectingBlock, destFacingBlock, destProtectBlock, pathMethod);
                } catch (jmri.JmriException e) {
                    throw e;
                }
            } else {
                log.debug("blocks not found");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("No valid route from {} to {}", sourceBean.getDisplayName(), destBean.getDisplayName());
        }
        throw new jmri.JmriException("Blocks Not Found");
    }

    /**
     * The is used in conjunction with the layout block routing protocol, to
     * discover a clear path from a source layout block through to a destination
     * layout block. By specifying the sourceLayoutBlock and
     * protectingLayoutBlock or sourceLayoutBlock+1, a direction of travel can
     * then be termined, eg east to west, south to north etc.
     * <p>
     * @param sourceBean   - The source bean (SignalHead, SignalMast or Sensor)
     *                     assigned to a block boundary that we are starting
     *                     from.
     * @param destBean     - The destination bean.
     * @param validateOnly - When set false, the system will not use layout
     *                     blocks that are set as either reserved(useExtraColor
     *                     set) or occupied, if it finds any then it will try to
     *                     find an alternative path When set false, no block
     *                     state checking is performed.
     * @param pathMethod   - Performs a check to see if any signal heads/masts
     *                     are in the path, if there are then the system will
     *                     try to find an alternative path. If set to NONE, then
     *                     no checking is performed.
     * @return an List of all the layoutblocks in the path.
     * @throws jmri.JmriException if it can not find a valid path or the routing
     *                            has not been enabled.
     */
    public List<LayoutBlock> getLayoutBlocks(NamedBean sourceBean, NamedBean destBean, boolean validateOnly, int pathMethod) throws jmri.JmriException {
        List<LayoutEditor> layout = InstanceManager.getDefault(PanelMenu.class).getLayoutEditorPanelList();
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        LayoutBlock facingBlock = null;
        LayoutBlock protectingBlock = null;
        LayoutBlock destFacingBlock = null;
        for (int i = 0; i < layout.size(); i++) {
            if (log.isDebugEnabled()) {
                log.debug("Layout name {}", layout.get(i).getLayoutName());
            }
            if (facingBlock == null) {
                facingBlock = lbm.getFacingBlockByNamedBean(sourceBean, layout.get(i));
            }
            if (protectingBlock == null) {
                protectingBlock = lbm.getProtectedBlockByNamedBean(sourceBean, layout.get(i));
            }
            if (destFacingBlock == null) {
                destFacingBlock = lbm.getFacingBlockByNamedBean(destBean, layout.get(i));
            }
            if ((destFacingBlock != null) && (facingBlock != null) && (protectingBlock != null)) {
                try {
                    return getLayoutBlocks(facingBlock, destFacingBlock, protectingBlock, validateOnly, pathMethod);
                } catch (jmri.JmriException e) {
                    throw e;
                }
            } else {
                log.debug("blocks not found");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("No valid route from {} to {}", sourceBean.getDisplayName(), destBean.getDisplayName());
        }
        throw new jmri.JmriException("Blocks Not Found");
    }

    /**
     * Returns a list of NamedBeans (Signalhead, Signalmast or Sensor) that are
     * assinged to block boundaries in a given list
     *
     * @param blocklist The list of block in order that need to be checked.
     * @param panel     (Optional) panel that the blocks need to be checked
     *                  against
     * @param T         (Optional) the class that we want to check against,
     *                  either Sensor, SignalMast or SignalHead, set null will
     *                  return any.
     */
    public List<NamedBean> getBeansInPath(List<LayoutBlock> blocklist, LayoutEditor panel, Class<?> T) {
        List<NamedBean> beansInPath = new ArrayList<>();
        if (blocklist.size() >= 2) {
            LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
            for (int x = 1; x < blocklist.size(); x++) {
                LayoutBlock facingBlock = blocklist.get(x - 1);
                LayoutBlock protectingBlock = blocklist.get(x);
                NamedBean nb = null;
                if (T == null) {
                    nb = lbm.getFacingNamedBean(facingBlock.getBlock(), protectingBlock.getBlock(), panel);
                } else if (T.equals(jmri.SignalMast.class)) {
                    nb = lbm.getFacingSignalMast(facingBlock.getBlock(), protectingBlock.getBlock(), panel);
                } else if (T.equals(jmri.Sensor.class)) {
                    nb = lbm.getFacingSensor(facingBlock.getBlock(), protectingBlock.getBlock(), panel);
                } else if (T.equals(jmri.SignalHead.class)) {
                    nb = lbm.getFacingSignalHead(facingBlock.getBlock(), protectingBlock.getBlock());
                }
                if (nb != null) {
                    beansInPath.add(nb);
                }
            }
        }
        return beansInPath;
    }

    /**
     * Determines if one set of blocks is reachable from another set of blocks
     * based upon the directions of the set of blocks.
     * <p>
     * This is used to help with identifying items such as signalmasts located
     * at positionable points or turnouts are facing in the same direction as
     * other given signalmasts.
     * <p>
     * Given the current block and the next block we can work out the direction
     * of travel. Given the destBlock and the next block on, we can determine
     * the whether the destBlock comes before the destBlock+1.
     *
     * @return true if destBlock comes before destBlock+1 or false if destBlock
     *         comes after destBlock+1
     * @throws jmri.JmriException if any Block is null;
     */
    public boolean checkValidDest(LayoutBlock currentBlock, LayoutBlock nextBlock, LayoutBlock destBlock, LayoutBlock destBlockn1, int pathMethod) throws jmri.JmriException {

        List<LayoutBlock> destList = new ArrayList<>();
        if (destBlockn1 != null) {
            destList.add(destBlockn1);
        }
        try {
            return checkValidDest(currentBlock, nextBlock, destBlock, destList, pathMethod);
        } catch (jmri.JmriException e) {
            throw e;
        }

    }

    public boolean checkValidDest(LayoutBlock currentBlock, LayoutBlock nextBlock, LayoutBlock destBlock, List<LayoutBlock> destBlockn1, int pathMethod) throws jmri.JmriException {
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        if (!lbm.isAdvancedRoutingEnabled()) {
            log.info("Advanced routing has not been enabled therefore we cannot use this function");
            throw new jmri.JmriException("Advanced routing has not been enabled therefore we cannot use this function");
        }

        if (log.isDebugEnabled()) {
            try {
                log.debug("faci " + currentBlock.getDisplayName());
                log.debug("next " + nextBlock.getDisplayName());
                log.debug("dest " + destBlock.getDisplayName());
                for (LayoutBlock dp : destBlockn1) {
                    log.debug("dest + 1 " + dp.getDisplayName());
                }
            } catch (java.lang.NullPointerException e) {

            }
        }
        if ((destBlock != null) && (currentBlock != null) && (nextBlock != null)) {
            if (!currentBlock.isRouteToDestValid(nextBlock.getBlock(), destBlock.getBlock())) {
                log.debug("Route to dest not valid");
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug("dest {}", destBlock.getDisplayName());
                /*if(destBlockn1!=null)
                 log.debug("remote prot " + destBlockn1.getDisplayName());*/
            }
            if (!destBlockn1.isEmpty() && currentBlock == destBlockn1.get(0) && nextBlock == destBlock) {
                log.debug("Our dest protecting block is our current block and our protecting block is the same as our destination block");
                return false;
            }
            //Do a simple test to see if one is reachable from the other.
            int proCount = 0;
            int desCount = 0;
            if (!destBlockn1.isEmpty()) {
                desCount = currentBlock.getBlockHopCount(destBlock.getBlock(), nextBlock.getBlock());
                proCount = currentBlock.getBlockHopCount(destBlockn1.get(0).getBlock(), nextBlock.getBlock());
                if (log.isDebugEnabled()) {
                    log.debug("dest {} protecting {}", desCount, proCount);
                }
            }
            if (proCount > desCount && (proCount - 1) != desCount) {
                /* The block that we are protecting should be one hop greater than the destination count
                 if it is not then the route is not valid.
                 */
                log.debug("Protecting is more than one hop away from destination and therefore not valid.");
                return false;
            }
            if (proCount < desCount) {
                /*Need to do a more advanced check in this case as the destBlockn1
                 could be reached via a different route and therefore have a smaller
                 hop count we need to therefore step through each block until we reach
                 the end.
                 We also need to perform a more advanced check if the destBlockn1
                 is null as this indicates that the destination signal mast is assigned
                 on an end bumper*/
                log.debug("proCount is less than destination");
                List<LayoutBlock> blockList = getLayoutBlocks(currentBlock, destBlock, nextBlock, true, pathMethod); //Was MASTTOMAST
                for (LayoutBlock dp : destBlockn1) {
                    if (blockList.contains(dp) && currentBlock != dp) {
                        log.debug("Signal mast in the wrong direction");
                        return false;
                    }
                }
                /*Work on the basis that if you get the blocks from source to dest
                 then the dest+1 block should not be included*/
                log.debug("Signal mast in the correct direction");
                return true;
            } else if ((proCount == -1) && (desCount == -1)) {
                //The destination block and destBlock+1 are both directly connected
                log.debug("Dest and dest+1 are directly connected");
                return false;
            }
            log.debug("Return true path");
            return true;
        } else if (destBlock == null) {
            throw new jmri.JmriException("Block in Destination Field returns as invalid");
        } else if (currentBlock == null) {
            throw new jmri.JmriException("Block in Facing Field returns as invalid");
        } else if (nextBlock == null) {
            throw new jmri.JmriException("Block in Protecting Field returns as invalid");
        }
        throw new jmri.JmriException("BlockIsNull");
    }

    /**
     * This uses the layout editor to check if the destination location is
     * reachable from the source location
     *
     * @param facing     Layout Block that is considered our first block
     * @param protecting Layout Block that is considered first block +1
     * @param dest       Layout Block that we want to get to
     * @return true if valid, false if not valid.
     */
    public boolean checkValidDest(LayoutBlock facing, LayoutBlock protecting, FacingProtecting dest, int pathMethod) throws JmriException {
        if (facing == null || protecting == null || dest == null) {
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("facing : {} protecting : {} dest {}" + facing.getDisplayName(), protecting.getDisplayName(), dest.getBean().getDisplayName());
        }
        try {
            //In this instance it doesn't matter what the destination protecting block is so we get the first
            /*LayoutBlock destProt = null;
             if(!dest.getProtectingBlocks().isEmpty()){
             destProt = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(dest.getProtectingBlocks().get(0));
             //log.info(dest.getProtectingBlocks());
             }*/
            List<LayoutBlock> destList = new ArrayList<>();
            for (Block b : dest.getProtectingBlocks()) {
                destList.add(InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(b));
            }
            return checkValidDest(facing, protecting, InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(dest.getFacing()), destList, pathMethod);
        } catch (jmri.JmriException e) {
            throw e;
        }
    }

    /**
     * The is used in conjunction with the layout block routing protocol, to
     * discover a clear path from a source layout block through to a destination
     * layout block. By specifying the sourceLayoutBlock and
     * protectingLayoutBlock or sourceLayoutBlock+1, a direction of travel can
     * then be termined, eg east to west, south to north etc.
     * <p>
     * @param sourceLayoutBlock      - The layout block that we are starting
     *                               from, can also be considered as the block
     *                               facing a signal.
     * @param destinationLayoutBlock - The layout block that we want to get to
     * @param protectingLayoutBlock  - The next layout block connected to the
     *                               source block, this can also be considered
     *                               as the block being protected by a signal
     * @param validateOnly           - When set false, the system will not use
     *                               layout blocks that are set as either
     *                               reserved(useExtraColor set) or occupied, if
     *                               it finds any then it will try to find an
     *                               alternative path When set true, no block
     *                               state checking is performed.
     * @param pathMethod             - Performs a check to see if any signal
     *                               heads/masts are in the path, if there are
     *                               then the system will try to find an
     *                               alternative path. If set to NONE, then no
     *                               checking is performed.
     * @return an List of all the layoutblocks in the path.
     * @throws jmri.JmriException if it can not find a valid path or the routing
     *                            has not been enabled.
     */
    public List<LayoutBlock> getLayoutBlocks(LayoutBlock sourceLayoutBlock, LayoutBlock destinationLayoutBlock, LayoutBlock protectingLayoutBlock, boolean validateOnly, int pathMethod) throws jmri.JmriException {
        lastErrorMessage = "Unknown Error Occured";
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        if (!lbm.isAdvancedRoutingEnabled()) {
            log.info("Advanced routing has not been enabled therefore we cannot use this function");
            throw new jmri.JmriException("Advanced routing has not been enabled therefore we cannot use this function");
        }

        int directionOfTravel = sourceLayoutBlock.getNeighbourDirection(protectingLayoutBlock);
        Block currentBlock = sourceLayoutBlock.getBlock();

        Block destBlock = destinationLayoutBlock.getBlock();
        log.debug("Destination Block {} {}", destinationLayoutBlock.getDisplayName(), destBlock);

        Block nextBlock = protectingLayoutBlock.getBlock();
        if (log.isDebugEnabled()) {
            log.debug("s:" + sourceLayoutBlock.getDisplayName() + " p:" + protectingLayoutBlock.getDisplayName() + " d:" + destinationLayoutBlock.getDisplayName());
        }
        List<BlocksTested> blocksInRoute = new ArrayList<>();
        blocksInRoute.add(new BlocksTested(sourceLayoutBlock));

        if (!validateOnly) {
            if (canLBlockBeUsed(protectingLayoutBlock)) {
                blocksInRoute.add(new BlocksTested(protectingLayoutBlock));
            } else {
                lastErrorMessage = "Block we are protecting is already occupied or reserved";
                log.debug(lastErrorMessage);
                throw new jmri.JmriException(lastErrorMessage);
            }
            if (!canLBlockBeUsed(destinationLayoutBlock)) {
                lastErrorMessage = "Destination Block is already occupied or reserved";
                log.debug(lastErrorMessage);
                throw new jmri.JmriException(lastErrorMessage);
            }
        } else {
            blocksInRoute.add(new BlocksTested(protectingLayoutBlock));
        }
        if (destinationLayoutBlock == protectingLayoutBlock) {
            List<LayoutBlock> returnBlocks = new ArrayList<>();
            for (int i = 0; i < blocksInRoute.size(); i++) {
                returnBlocks.add(blocksInRoute.get(i).getBlock());
            }
            return returnBlocks;
        }

        BlocksTested bt = blocksInRoute.get(blocksInRoute.size() - 1);

        int ttl = 1;
        List<Integer> offSet = new ArrayList<>();
        while (ttl < ttlSize) { //value should be higher but low for test!
            log.debug("===== Ttl value = {} ======", ttl);
            log.debug("Looking for next block");
            int nextBlockIndex = findBestHop(currentBlock, nextBlock, destBlock, directionOfTravel, offSet, validateOnly, pathMethod);
            if (nextBlockIndex != -1) {
                bt.addIndex(nextBlockIndex);
                if (log.isDebugEnabled()) {
                    log.debug("block index returned " + nextBlockIndex + " Blocks in route size " + blocksInRoute.size());
                }
                //Sets the old next block to be our current block.
                LayoutBlock currentLBlock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(nextBlock);

                offSet.clear();

                directionOfTravel = currentLBlock.getRouteDirectionAtIndex(nextBlockIndex);

                currentBlock = nextBlock;
                nextBlock = currentLBlock.getRouteNextBlockAtIndex(nextBlockIndex);
                LayoutBlock nextLBlock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(nextBlock);
                if (log.isDebugEnabled()) {
                    log.debug("Blocks in route size {}", blocksInRoute.size());
                    log.debug("{} {}", nextBlock.getDisplayName(), destBlock.getDisplayName());
                }
                if (nextBlock == currentBlock) {
                    nextBlock = currentLBlock.getRouteDestBlockAtIndex(nextBlockIndex);
                    log.debug("the next block to our destination we are looking for is directly connected to this one");
                } else if (protectingLayoutBlock != nextLBlock) {
                    log.debug("Add block {}", nextLBlock.getDisplayName());
                    bt = new BlocksTested(nextLBlock);
                    blocksInRoute.add(bt);
                }
                if (nextBlock == destBlock) {
                    if (!validateOnly && !checkForLevelCrossing(destinationLayoutBlock)) {
                        throw new jmri.JmriException("Destination block is in conflict on a crossover");
                    }
                    List<LayoutBlock> returnBlocks = new ArrayList<>();
                    for (int i = 0; i < blocksInRoute.size(); i++) {
                        returnBlocks.add(blocksInRoute.get(i).getBlock());
                    }
                    returnBlocks.add(destinationLayoutBlock);
                    if (log.isDebugEnabled()) {
                        log.debug("Adding destination Block {}", destinationLayoutBlock.getDisplayName());
                        log.debug("arrived at destination block");
                        log.debug("{} Return as Long", sourceLayoutBlock.getDisplayName());
                        for (int i = 0; i < returnBlocks.size(); i++) {
                            log.debug(returnBlocks.get(i).getDisplayName());
                        }
                        log.debug("Finished List");
                    }
                    return returnBlocks;
                }
            } else {
                //-1 is returned when there are no more valid besthop valids found
                //Block index is -1, so we need to go back a block and find another way.

                //So we have gone back as far as our starting block so we better return.
                int birSize = blocksInRoute.size();
                log.debug("block in route size {}", birSize);
                if (birSize <= 2) {
                    log.debug("drop out here with ttl");
                    ttl = ttlSize + 1;
                } else {
                    if (log.isDebugEnabled()) {
                        for (int t = 0; t < blocksInRoute.size(); t++) {
                            log.debug("index {} block {}", t, blocksInRoute.get(t).getBlock().getDisplayName());
                        }
                        log.debug("To remove last block {}", blocksInRoute.get(birSize - 1).getBlock().getDisplayName());
                    }

                    currentBlock = blocksInRoute.get(birSize - 3).getBlock().getBlock();
                    nextBlock = blocksInRoute.get(birSize - 2).getBlock().getBlock();
                    offSet = blocksInRoute.get(birSize - 2).getTestedIndexes();
                    bt = blocksInRoute.get(birSize - 2);
                    blocksInRoute.remove(birSize - 1);
                    ttl--;
                }
            }
            ttl++;
        }
        if (ttl == ttlSize) {
            lastErrorMessage = "ttlExpired";
        }
        //we exited the loop without either finding the destination or we had error.
        throw new jmri.JmriException(lastErrorMessage);
    }

    static class BlocksTested {

        LayoutBlock block;
        List<Integer> indexNumber = new ArrayList<>();

        BlocksTested(LayoutBlock block) {
            this.block = block;
        }

        void addIndex(int x) {
            indexNumber.add(x);
        }

        int getLastIndex() {
            return indexNumber.get(indexNumber.size() - 1); //get the last one in the list
        }

        List<Integer> getTestedIndexes() {
            return indexNumber;
        }

        LayoutBlock getBlock() {
            return block;
        }
    }

    private boolean canLBlockBeUsed(LayoutBlock lBlock) {
        if (lBlock == null) {
            return true;
        }
        if (lBlock.getUseExtraColor()) {
            return false;
        }
        if (lBlock.getBlock().getPermissiveWorking()) {
            return true;
        }
        if (lBlock.getState() == Block.OCCUPIED) {
            return false;
        }
        return true;
    }

    String lastErrorMessage = "Unknown Error Occured";

    //We need to take into account if the returned block has a signalmast attached.
    int findBestHop(final Block preBlock, final Block currentBlock, Block destBlock, int direction, List<Integer> offSet, boolean validateOnly, int pathMethod) {
        int blockindex = 0;
        Block block;
        LayoutBlock currentLBlock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(currentBlock);
        List<Integer> blkIndexTested = new ArrayList<>(5);
        if (log.isDebugEnabled()) {
            log.debug("In find best hop current " + currentLBlock.getDisplayName() + " previous " + preBlock.getDisplayName());
        }
        while (blockindex != -1) {
            if (currentBlock == preBlock) {
                //Basically looking for the connected block, which there should only be one of!
                log.debug("At get ConnectedBlockRoute");
                blockindex = currentLBlock.getConnectedBlockRouteIndex(destBlock, direction);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Off Set " + offSet);
                }
                blockindex = currentLBlock.getNextBestBlock(preBlock, destBlock, offSet, METRIC);
            }
            if (blockindex != -1) {
                block = currentLBlock.getRouteNextBlockAtIndex(blockindex);
                LayoutBlock lBlock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(block);

                Block blocktoCheck = block;
                if (block == currentBlock) {
                    log.debug("current block matches returned block therefore the next block is directly connected");
                    blocktoCheck = destBlock;
                }

                if ((block == currentBlock) && (currentLBlock.getThroughPathIndex(preBlock, destBlock) == -1)) {
                    lastErrorMessage = "block " + block.getDisplayName() + " is directly attached, however the route to the destination block " + destBlock.getDisplayName() + " can not be directly used";
                    log.debug(lastErrorMessage);
                } else if ((validateOnly) || ((checkForDoubleCrossover(preBlock, currentLBlock, blocktoCheck) && checkForLevelCrossing(currentLBlock)) && canLBlockBeUsed(lBlock))) {
                    if (log.isDebugEnabled()) {
                        log.debug(block.getDisplayName() + " not occupied & not reserved but we need to check if the anchor point between the two contains a signal or not");
                        log.debug(currentBlock.getDisplayName() + " " + block.getDisplayName());
                    }

                    jmri.NamedBean foundBean = null;
                    /* We change the logging level to fatal in the layout block manager as we are testing to make sure that no signalhead/mast exists
                     this would generate an error message that is expected.*/
                    MDC.put("loggingDisabled", LayoutBlockManager.class.getName());
                    switch (pathMethod) {
                        case MASTTOMAST:
                            foundBean = InstanceManager.getDefault(LayoutBlockManager.class).getFacingSignalMast(currentBlock, blocktoCheck);
                            break;
                        case HEADTOHEAD:
                            foundBean = InstanceManager.getDefault(LayoutBlockManager.class).getFacingSignalHead(currentBlock, blocktoCheck);
                            break;
                        case SENSORTOSENSOR:
                            foundBean = InstanceManager.getDefault(LayoutBlockManager.class).getFacingSensor(currentBlock, blocktoCheck, null);
                            break;
                        case NONE:
                            break;
                        default:
                            foundBean = InstanceManager.getDefault(LayoutBlockManager.class).getFacingNamedBean(currentBlock, blocktoCheck, null);
                            break;
                    }
                    MDC.remove("loggingDisabled");
                    if (foundBean == null) {
                        log.debug("No object found so okay to return");
                        return blockindex;
                    } else {
                        lastErrorMessage = "Signal " + foundBean.getDisplayName() + " already exists between blocks " + currentBlock.getDisplayName() + " and " + blocktoCheck.getDisplayName() + " in the same direction on this path";
                        log.debug(lastErrorMessage);
                    }
                } else {
                    lastErrorMessage = "block " + block.getDisplayName() + " found not to be not usable";
                    log.debug(lastErrorMessage);
                }
                if (blkIndexTested.contains(blockindex)) {
                    lastErrorMessage = ("No valid free path found");
                    return -1;
                }
                blkIndexTested.add(blockindex);
                offSet.add(blockindex);
            } else {
                log.debug("At this point the getNextBextBlock() has returned a -1");
            }
        }
        return -1;
    }

    private boolean checkForDoubleCrossover(Block prevBlock, LayoutBlock curBlock, Block nextBlock) {
        LayoutEditor le = curBlock.getMaxConnectedPanel();
        ConnectivityUtil ct = le.getConnectivityUtil();
        List<LayoutTrackExpectedState<LayoutTurnout>> turnoutList = ct.getTurnoutList(curBlock.getBlock(), prevBlock, nextBlock);
        for (int i = 0; i < turnoutList.size(); i++) {
            LayoutTurnout lt = turnoutList.get(i).getObject();
            if (lt.getTurnoutType() == LayoutTurnout.DOUBLE_XOVER) {
                if (turnoutList.get(i).getExpectedState() == jmri.Turnout.THROWN) {
                    jmri.Turnout t = lt.getTurnout();
                    if (t.getKnownState() == jmri.Turnout.THROWN) {
                        if (lt.getLayoutBlock() == curBlock || lt.getLayoutBlockC() == curBlock) {
                            if (!canLBlockBeUsed(lt.getLayoutBlockB()) && !canLBlockBeUsed(lt.getLayoutBlockD())) {
                                return false;
                            }
                        } else if (lt.getLayoutBlockB() == curBlock || lt.getLayoutBlockD() == curBlock) {
                            if (!canLBlockBeUsed(lt.getLayoutBlock()) && !canLBlockBeUsed(lt.getLayoutBlockC())) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean checkForLevelCrossing(LayoutBlock curBlock) {
        LayoutEditor lay = curBlock.getMaxConnectedPanel();
        for (LevelXing lx : lay.getLevelXings()) {
            if (lx.getLayoutBlockAC() == curBlock
                    || lx.getLayoutBlockBD() == curBlock) {
                if ((lx.getLayoutBlockAC() != null)
                        && (lx.getLayoutBlockBD() != null)
                        && (lx.getLayoutBlockAC() != lx.getLayoutBlockBD())) {
                    if (lx.getLayoutBlockAC() == curBlock) {
                        return canLBlockBeUsed(lx.getLayoutBlockBD());
                    } else if (lx.getLayoutBlockBD() == curBlock) {
                        return canLBlockBeUsed(lx.getLayoutBlockAC());
                    }
                }
            }
        }
        return true;
    }

    /**
     * Discovers valid pairs of beans type T assigned to a layout editor. If no
     * bean type is provided, then either SignalMasts or Sensors are discovered
     * If no editor is provided, then all editors are considered
     *
     * @param pathMethod Determine whether or not we should reject pairs if
     *                   there are other beans in the way. Constant values of
     *                   NONE, ANY, MASTTOMAST, HEADTOHEAD
     */
    public Hashtable<NamedBean, List<NamedBean>> discoverValidBeanPairs(LayoutEditor editor, Class<?> T, int pathMethod) {
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        Hashtable<NamedBean, List<NamedBean>> retPairs = new Hashtable<NamedBean, List<NamedBean>>();
        List<FacingProtecting> beanList = generateBlocksWithBeans(editor, T);
        for (FacingProtecting fp : beanList) {
            for (Block block : fp.getProtectingBlocks()) {
                if (log.isDebugEnabled()) {
                    try {
                        log.debug("\nSource " + fp.getBean().getDisplayName());
                        log.debug("facing " + fp.getFacing().getDisplayName());
                        log.debug("protecting " + block.getDisplayName());
                    } catch (java.lang.NullPointerException e) {
                        //Can be considered normal if the signalmast is assigned to an end bumper.
                    }
                }
                LayoutBlock lFacing = lbm.getLayoutBlock(fp.getFacing());
                LayoutBlock lProtecting = lbm.getLayoutBlock(block);
                NamedBean source = fp.getBean();
                try {
                    retPairs.put(source, discoverPairDest(source, lProtecting, lFacing, beanList, pathMethod));
                } catch (JmriException ex) {
                    log.error(ex.toString());
                }
            }
        }
        return retPairs;
    }

    /**
     * Returns a list of valid destination beans reachable from a given source
     * bean.
     *
     * @param source     Either a SignalMast or Sensor
     * @param editor     The layout editor that the source is located on, if
     *                   null, then all editors are considered
     * @param T          The class of the remote destination, if null, then both
     *                   SignalMasts and Sensors are considered
     * @param pathMethod Determine whether or not we should reject pairs if
     *                   there are other beans in the way. Constant values of
     *                   NONE, ANY, MASTTOMAST, HEADTOHEAD
     * @return A list of all reachable NamedBeans
     */
    public List<NamedBean> discoverPairDest(NamedBean source, LayoutEditor editor, Class<?> T, int pathMethod) throws JmriException {
        if (log.isDebugEnabled()) {
            log.debug("discover pairs from source " + source.getDisplayName());
        }
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        LayoutBlock lFacing = lbm.getFacingBlockByNamedBean(source, editor);
        List<LayoutBlock> lProtecting = lbm.getProtectingBlocksByNamedBean(source, editor);
        List<NamedBean> ret = new ArrayList<>();
        List<FacingProtecting> beanList = generateBlocksWithBeans(editor, T);
        try {
            for (LayoutBlock lb : lProtecting) {
                ret.addAll(discoverPairDest(source, lb, lFacing, beanList, pathMethod));
            }
        } catch (JmriException e) {
            throw e;
        }
        return ret;
    }

    List<NamedBean> discoverPairDest(NamedBean source, LayoutBlock lProtecting, LayoutBlock lFacing, List<FacingProtecting> blockList, int pathMethod) throws JmriException {
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        if (!lbm.isAdvancedRoutingEnabled()) {
            throw new JmriException("advanced routing not enabled");
        }
        if (!lbm.routingStablised()) {
            throw new JmriException("routing not stabilised");
        }
        List<NamedBean> validDestBean = new ArrayList<>();
        for (int j = 0; j < blockList.size(); j++) {
            if (blockList.get(j).getBean() != source) {
                NamedBean destObj = blockList.get(j).getBean();
                if (log.isDebugEnabled()) {
                    log.debug("looking for pair " + source.getDisplayName() + " " + destObj.getDisplayName());
                }
                try {
                    if (checkValidDest(lFacing, lProtecting, blockList.get(j), pathMethod)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Valid pair " + source.getDisplayName() + " " + destObj.getDisplayName());
                        }
                        LayoutBlock ldstBlock = lbm.getLayoutBlock(blockList.get(j).getFacing());
                        try {
                            List<LayoutBlock> lblks = getLayoutBlocks(lFacing, ldstBlock, lProtecting, true, pathMethod);
                            if (log.isDebugEnabled()) {
                                log.debug("Adding block " + destObj.getDisplayName() + " to paths, current size " + lblks.size());
                            }
                            validDestBean.add(destObj);
                        } catch (jmri.JmriException e) {  // Considered normal if route not found.
                            log.debug("not a valid route through " + source.getDisplayName() + " - " + destObj.getDisplayName());
                        }
                    }
                } catch (jmri.JmriException ex) {
                    log.debug(ex.toString());
                }
            }
        }
        return validDestBean;
    }

    List<FacingProtecting> generateBlocksWithBeans(LayoutEditor editor, Class<?> T) {
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        List<FacingProtecting> beanList = new ArrayList<>();

        List<String> lblksSysName = lbm.getSystemNameList();
        for (int i = 0; i < lblksSysName.size(); i++) {
            LayoutBlock curLblk = lbm.getLayoutBlock(lblksSysName.get(i));
            Block curBlk = curLblk.getBlock();
            LayoutEditor useEdit = editor;
            if (editor == null) {
                useEdit = curLblk.getMaxConnectedPanel();
            }
            if (curBlk != null) {
                int noNeigh = curLblk.getNumberOfNeighbours();
                for (int x = 0; x < noNeigh; x++) {
                    Block blk = curLblk.getNeighbourAtIndex(x);
                    List<Block> proBlk = new ArrayList<>();
                    NamedBean bean = null;
                    if (T == null) {
                        proBlk.add(blk);
                        bean = lbm.getFacingNamedBean(curBlk, blk, useEdit);
                    } else if (T.equals(SignalMast.class)) {
                        bean = lbm.getFacingSignalMast(curBlk, blk, useEdit);
                        if (bean != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Get list of protecting blocks for " + bean.getDisplayName() + " facing " + curBlk.getDisplayName());
                            }
                            List<LayoutBlock> lProBlk = lbm.getProtectingBlocksByNamedBean(bean, useEdit);
                            for (LayoutBlock lb : lProBlk) {
                                if (lb != null) {
                                    proBlk.add(lb.getBlock());
                                }
                            }
                        }
                    } else if (T.equals(Sensor.class)) {
                        bean = lbm.getFacingSensor(curBlk, blk, useEdit);
                        if (bean != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Get list of protecting blocks for " + bean.getDisplayName());
                            }
                            List<LayoutBlock> lProBlk = lbm.getProtectingBlocksByNamedBean(bean, useEdit);
                            for (LayoutBlock lb : lProBlk) {
                                if (lb != null) {
                                    proBlk.add(lb.getBlock());
                                }
                            }
                        }
                    } else {
                        log.error("Past bean type is unknown " + T);
                    }
                    if (bean != null) {
                        FacingProtecting toadd = new FacingProtecting(curBlk, proBlk, bean);
                        boolean found = false;
                        for (FacingProtecting fp : beanList) {
                            if (fp.equals(toadd)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            beanList.add(toadd);
                        }
                    }
                }
                if (noNeigh == 1) {
                    NamedBean bean = null;
                    if (log.isDebugEnabled()) {
                        log.debug("We have a dead end " + curBlk.getDisplayName());
                    }
                    if (T == null) {
                        bean = lbm.getNamedBeanAtEndBumper(curBlk, useEdit);
                    } else if (T.equals(SignalMast.class)) {
                        bean = lbm.getSignalMastAtEndBumper(curBlk, useEdit);
                    } else if (T.equals(Sensor.class)) {
                        bean = lbm.getSensorAtEndBumper(curBlk, useEdit);
                    } else {
                        log.error("Past bean type is unknown " + T);
                    }
                    if (bean != null) {
                        FacingProtecting toadd = new FacingProtecting(curBlk, null, bean);
                        boolean found = false;
                        for (FacingProtecting fp : beanList) {
                            if (fp.equals(toadd)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            beanList.add(toadd);
                        }
                    }
                }
            }
        }
        return beanList;
    }

    static class FacingProtecting {

        Block facing;
        List<Block> protectingBlocks;
        NamedBean bean;

        FacingProtecting(Block facing, List<Block> protecting, NamedBean bean) {
            this.facing = facing;
            if (protecting == null) {
                this.protectingBlocks = new ArrayList<>(0);
            } else {
                this.protectingBlocks = protecting;
            }
            this.bean = bean;
        }

        Block getFacing() {
            return facing;
        }

        List<Block> getProtectingBlocks() {
            return protectingBlocks;
        }

        NamedBean getBean() {
            return bean;
        }

        @Override
        public boolean equals(Object obj) {

            if (obj == this) {
                return true;
            }
            if (obj == null) {
                return false;
            }

            if (!(getClass() == obj.getClass())) {
                return false;
            } else {
                FacingProtecting tmp = (FacingProtecting) obj;
                if (tmp.getBean() != this.bean) {
                    return false;
                }
                if (tmp.getFacing() != this.facing) {
                    return false;
                }
                if (!tmp.getProtectingBlocks().equals(this.protectingBlocks)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + (this.bean != null ? this.bean.hashCode() : 0);
            hash = 37 * hash + (this.facing != null ? this.facing.hashCode() : 0);
            hash = 37 * hash + (this.protectingBlocks != null ? this.protectingBlocks.hashCode() : 0);
            return hash;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutBlockConnectivityTools.class);

}
