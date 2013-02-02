package jmri.jmrit.display.layoutEditor;

import org.apache.log4j.Logger;
import jmri.Sensor;
import jmri.Block;
import jmri.SignalMast;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.JmriException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
/**
* These are a series of layout block connectivity tools that can be used
* when the advanced layout block routing has been enabled.
* These tools can determine if a path from a source to destination bean is valid.
* If a route between two layout blocks is usable and free.
*
* @author Kevin Dickerson  Copyright (C) 2011
* @version			$Revision: 19923 $
*/

public class LayoutBlockConnectivityTools{

    public LayoutBlockConnectivityTools(){
    
    }

    /**
     * Constant used in the getLayoutBlocks to represent a path from one Signal Mast
     * to another and that no mast should be in the path.
     */
    public final static int MASTTOMAST = 0x01;
    
    /**
     * Constant used in the getLayoutBlocks to represent a path from one Signal Head
     * to another and that no head should be in the path.
     */
    public final static int HEADTOHEAD = 0x02;
    
    /**
     * Constant used in the getLayoutBlocks to represent a path from either 
     * a Signal Mast or Head to another Signal Mast or Head and that no mast of
     * head should be in the path.
     */
    public final static int ANY = 0x04;
    
    /**
     * Constant used in the getLayoutBlocks to indicate that the the system should
     * not check for signal masts or heads on the path.
     */
    public final static int NONE = 0x00;

        
    public final static int HOPCOUNT = 0x00;
    public final static int METRIC = 0x01;
    public final static int DISTANCE = 0x02;
    private static int ttlSize = 50;

    /**
     * Determines if a pair of NamedBeans (Signalhead, Signalmast or Sensor) assigned to a
     * block boundary are reachable.
     *
     * @return true if source and destination beans are reachable, or false if they are not
     * @throws Jmri.Exception if no blocks can be found that related to the named beans.
     */
    public boolean checkValidDest(NamedBean sourceBean, NamedBean destBean) throws jmri.JmriException{
        LayoutBlock facingBlock = null;
        LayoutBlock protectingBlock = null;
        LayoutBlock destFacingBlock = null;
        LayoutBlock destProtectBlock = null;
        ArrayList<LayoutEditor> layout = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
        LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        for(int i = 0; i<layout.size(); i++){
            if(log.isDebugEnabled())
                log.debug("Layout name " + layout.get(i).getLayoutName());
            if (facingBlock==null){
                facingBlock = lbm.getFacingBlockByNamedBean(sourceBean, layout.get(i));
            }
            if (protectingBlock==null){
                protectingBlock = lbm.getProtectedBlockByNamedBean(sourceBean, layout.get(i));
            }
            if(destFacingBlock==null){
                destFacingBlock = lbm.getFacingBlockByNamedBean(destBean, layout.get(i));
            }
            if(destProtectBlock==null){
                destProtectBlock = lbm.getProtectedBlockByNamedBean(destBean, layout.get(i));
            }
            if((destFacingBlock!=null) && (facingBlock!=null) && (protectingBlock!=null)){
                /*Destination protecting block is allowed to be null, as the destination signalmast
                could be assigned to an end bumper */
                //A simple to check to see if the remote signal is in the correct direction to ours.
                try{
                    return checkValidDest(facingBlock, protectingBlock, destFacingBlock, destProtectBlock);
                } catch (jmri.JmriException e){
                    throw e;
                }
            } else {
                log.debug("blocks not found");
            }
        }
        if(log.isDebugEnabled())
            log.debug("No valid route from " + sourceBean.getDisplayName() + " to " + destBean.getDisplayName());
        throw new jmri.JmriException("Blocks Not Found");
    }
    
   /**
    * The is used in conjunction with the layout block routing protocol, to discover
    * a clear path from a source layout block through to a destination layout block.
    * By specifying the sourceLayoutBlock and protectingLayoutBlock or sourceLayoutBlock+1, 
    * a direction of travel can then be termined, eg east to west, south to north etc.
    * <p>
    * @param sourceBean - The source bean (SignalHead, SignalMast or Sensor) assigned to a 
    *                      block boundary that we are starting from.
    * @param destBean - The destination bean.
    * @param validateOnly - When set false, the system will not use layout blocks
    *                       that are set as either reserved(useExtraColor set) or occupied, if it 
    *                       finds any then it will try to find an alternative path
    *                       When set false, no block state checking is performed.
    * @param pathMethod - Performs a check to see if any signal heads/masts are 
    *                     in the path, if there are then the system will try to find
    *                     an alternative path.  If set to NONE, then no checking is performed.
    * @return an ArrayList of all the layoutblocks in the path.
    * @throws jmri.JmriException if it can not find a valid path or the routing 
    *                            has not been enabled.
    */
    public ArrayList<LayoutBlock> getLayoutBlocks(NamedBean sourceBean, NamedBean destBean, boolean validateOnly, int pathMethod) throws jmri.JmriException{
        ArrayList<LayoutEditor> layout = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
        LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        LayoutBlock facingBlock = null;
        LayoutBlock protectingBlock = null;
        LayoutBlock destFacingBlock = null;
        for(int i = 0; i<layout.size(); i++){
            if(log.isDebugEnabled())
                log.debug("Layout name " + layout.get(i).getLayoutName());
            if (facingBlock==null){
                facingBlock = lbm.getFacingBlockByNamedBean(sourceBean, layout.get(i));
            }
            if (protectingBlock==null){
                protectingBlock = lbm.getProtectedBlockByNamedBean(sourceBean, layout.get(i));
            }
            if(destFacingBlock==null){
                destFacingBlock = lbm.getFacingBlockByNamedBean(destBean, layout.get(i));
            }
            if((destFacingBlock!=null) && (facingBlock!=null) && (protectingBlock!=null)){
                try{
                    return getLayoutBlocks(facingBlock, destFacingBlock, protectingBlock, validateOnly, pathMethod);
                } catch (jmri.JmriException e){
                    throw e;
                }
            } else {
                log.debug("blocks not found");
            }
        }
        if(log.isDebugEnabled())
            log.debug("No valid route from " + sourceBean.getDisplayName() + " to " + destBean.getDisplayName());
        throw new jmri.JmriException("Blocks Not Found");
    }
    
    /**
    * Returns a list of NamedBeans (Signalhead, Signalmast or Sensor) that are assinged to block boundaries
    * in a given list
    * @param blocklist The list of block in order that need to be checked.
    * @param panel (Optional) panel that the blocks need to be checked against
    * @param T (Optional) the class that we want to check against, either Sensor, SignalMast or SignalHead, set null will return any.
    */
    public List<NamedBean> getBeansInPath(List<LayoutBlock> blocklist, LayoutEditor panel, Class<?> T){
        ArrayList<NamedBean> beansInPath = new ArrayList<NamedBean>();
        if(blocklist.size()>=2){
            LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
            for(int x = 1; x<blocklist.size(); x++){
                LayoutBlock facingBlock = blocklist.get(x-1);
                LayoutBlock protectingBlock = blocklist.get(x);
                NamedBean nb = null;
                if(T==null){
                    nb = lbm.getFacingNamedBean(facingBlock.getBlock(), protectingBlock.getBlock(), panel);
                } else if(T.equals(jmri.SignalMast.class)){
                    nb = lbm.getFacingSignalMast(facingBlock.getBlock(), protectingBlock.getBlock(), panel);
                } else if (T.equals(jmri.Sensor.class)){
                    nb = lbm.getFacingSensor(facingBlock.getBlock(), protectingBlock.getBlock(), panel);
                } else if (T.equals(jmri.SignalHead.class)){
                    nb = lbm.getFacingSignalHead(facingBlock.getBlock(), protectingBlock.getBlock());
                }
                if(nb!=null)
                    beansInPath.add(nb);
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
     * of travel.
     * Given the destBlock and the next block on, we can determine the whether 
     * the destBlock comes before the destBlock+1.
     * @return true if destBlock comes before destBlock+1 or 
     * false if destBlock comes after destBlock+1
     * @throws Jmri.Exception if any Block is null;
     */
    public boolean checkValidDest(LayoutBlock currentBlock, LayoutBlock nextBlock, LayoutBlock destBlock, LayoutBlock destBlockn1) throws jmri.JmriException {
        LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        if (!lbm.isAdvancedRoutingEnabled()){
            log.info("Advanced routing has not been enabled therefore we cannot use this function");
            throw new jmri.JmriException("Advanced routing has not been enabled therefore we cannot use this function");
        }
        if(log.isDebugEnabled()){
            try {
                log.debug("faci " + currentBlock.getDisplayName());
                log.debug("next " + nextBlock.getDisplayName());
                log.debug("dest " + destBlock.getDisplayName());
                log.debug("dest + 1 " + destBlockn1.getDisplayName());
            } catch (java.lang.NullPointerException e){

            }
        }
        if((destBlock!=null) && (currentBlock!=null) && (nextBlock!=null)){
            if(!currentBlock.isRouteToDestValid(nextBlock.getBlock(), destBlock.getBlock())){
                log.debug("Route to dest not valid");
                return false;
            }
            if(log.isDebugEnabled()){
                log.debug("dest " + destBlock.getDisplayName());
                if(destBlockn1!=null)
                    log.debug("remote prot " + destBlockn1.getDisplayName());
            }
            //Do a simple test to see if one is reachable from the other.
            int proCount = 0;
            int desCount = 0;
            if(destBlockn1!=null){
                desCount = currentBlock.getBlockHopCount(destBlock.getBlock(), nextBlock.getBlock());
                proCount = currentBlock.getBlockHopCount(destBlockn1.getBlock(), nextBlock.getBlock());
                log.debug("dest " + desCount + " protecting " + proCount);
            }
            if(proCount<desCount){
                /*Need to do a more advanced check in this case as the destBlockn1
                could be reached via a different route and therefore have a smaller 
                hop count we need to therefore step through each block until we reach
                the end.
                We also need to perform a more advanced check if the destBlockn1 
                is null as this indicates that the destination signal mast is assigned
                on an end bumper*/
                ArrayList<LayoutBlock> blockList = getLayoutBlocks(currentBlock, destBlock, nextBlock, true, NONE); //Was MASTTOMAST
                if(blockList.contains(destBlockn1)){
                    log.debug("Signal mast in the wrong direction");
                    return false;
                }
                /*Work on the basis that if you get the blocks from source to dest
                then the dest+1 block should not be included*/
                log.debug("Signal mast in the correct direction");
                return true;
            } else if ((proCount==-1) && (desCount==-1)) {
                //The destination block and destBlock+1 are both directly connected
                return false;
            }
            return true;
        } else if (destBlock==null){
            throw new jmri.JmriException("Block in Destination Field returns as invalid");
        } else if (currentBlock==null){
            throw new jmri.JmriException("Block in Facing Field returns as invalid");
        }
        else if (nextBlock==null){
            throw new jmri.JmriException("Block in Protecting Field returns as invalid");
        }
        throw new jmri.JmriException("BlockIsNull");
    }
    
    /**
    * This uses the layout editor to check if the destination location is 
    * reachable from the source location
    *
    * @param facing Layout Block that is considered our first block
    * @param protecting Layout Block that is considered first block +1
    * @param dest Layout Block that we want to get to
    * @return true if valid, false if not valid.
    */
    
    public boolean checkValidDest(LayoutBlock facing, LayoutBlock protecting, FacingProtecting dest) throws JmriException{
        if(facing==null || protecting==null || dest == null){
            return false;
        }
        try{
            return checkValidDest(facing, protecting, InstanceManager.layoutBlockManagerInstance().getLayoutBlock(dest.getFacing()), InstanceManager.layoutBlockManagerInstance().getLayoutBlock(dest.getProtecting()));
        } catch (jmri.JmriException e){
            throw e;
        }
    }

   /**
    * The is used in conjunction with the layout block routing protocol, to discover
    * a clear path from a source layout block through to a destination layout block.
    * By specifying the sourceLayoutBlock and protectingLayoutBlock or sourceLayoutBlock+1, 
    * a direction of travel can then be termined, eg east to west, south to north etc.
    * <p>
    * @param sourceLayoutBlock - The layout block that we are starting from, 
    *                    can also be considered as the block facing a signal.
    * @param destinationLayoutBlock - The layout block that we want to get to
    * @param protectingLayoutBlock - The next layout block connected to the source 
    *                 block, this can also be considered as the block being protected by a signal
    * @param validateOnly - When set false, the system will not use layout blocks
    *                       that are set as either reserved(useExtraColor set) or occupied, if it 
    *                       finds any then it will try to find an alternative path
    *                       When set false, no block state checking is performed.
    * @param pathMethod - Performs a check to see if any signal heads/masts are 
    *                     in the path, if there are then the system will try to find
    *                     an alternative path.  If set to NONE, then no checking is performed.
    * @return an ArrayList of all the layoutblocks in the path.
    * @throws jmri.JmriException if it can not find a valid path or the routing 
    *                            has not been enabled.
    */
    public ArrayList<LayoutBlock> getLayoutBlocks(LayoutBlock sourceLayoutBlock, LayoutBlock destinationLayoutBlock, LayoutBlock protectingLayoutBlock, boolean validateOnly, int pathMethod) throws jmri.JmriException{
        lastErrorMessage= "Unknown Error Occured";
        LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        if (!lbm.isAdvancedRoutingEnabled()){
            log.info("Advanced routing has not been enabled therefore we cannot use this function");
            throw new jmri.JmriException("Advanced routing has not been enabled therefore we cannot use this function");
        }
        
        int directionOfTravel = sourceLayoutBlock.getNeighbourDirection(protectingLayoutBlock);
        Block currentBlock = sourceLayoutBlock.getBlock();

        Block destBlock = destinationLayoutBlock.getBlock();
        if(log.isDebugEnabled()) log.debug("Destination Block " + destinationLayoutBlock.getDisplayName() + " " + destBlock);
        Block nextBlock = protectingLayoutBlock.getBlock();
        if(log.isDebugEnabled()){
            log.debug("s:" + sourceLayoutBlock.getDisplayName() + " p:" + protectingLayoutBlock.getDisplayName() + " d:" + destinationLayoutBlock.getDisplayName());
        }
        ArrayList<BlocksTested> blocksInRoute = new ArrayList<BlocksTested>();
        blocksInRoute.add(new BlocksTested(sourceLayoutBlock));
        
        if(!validateOnly){
            if (canLBlockBeUsed(protectingLayoutBlock)){
                blocksInRoute.add(new BlocksTested(protectingLayoutBlock));
            } else {
                lastErrorMessage = "Block we are protecting is already occupied or reserved";
                log.debug(lastErrorMessage);
                throw new jmri.JmriException(lastErrorMessage);
            }
            if (!canLBlockBeUsed(destinationLayoutBlock)){
                lastErrorMessage = "Destination Block is already occupied or reserved";
                log.debug(lastErrorMessage);
                throw new jmri.JmriException(lastErrorMessage);
            }
        } else {
            blocksInRoute.add(new BlocksTested(protectingLayoutBlock));
        }
        if (destinationLayoutBlock==protectingLayoutBlock){
            ArrayList<LayoutBlock> returnBlocks = new ArrayList<LayoutBlock>();
            for (int i =0; i<blocksInRoute.size(); i++){
                returnBlocks.add(blocksInRoute.get(i).getBlock());
            }
            return returnBlocks;
        }
        LayoutBlock currentLBlock = protectingLayoutBlock;

        BlocksTested bt = blocksInRoute.get(blocksInRoute.size()-1);

        int ttl=1;
        List<Integer> offSet= new ArrayList<Integer>();
        while (ttl <ttlSize){ //value should be higher but low for test!
            log.debug("===== Ttl value = " + ttl + " ======\nLooking for next block");
            int nextBlockIndex = findBestHop(currentBlock, nextBlock, destBlock, directionOfTravel, offSet, validateOnly, pathMethod);
            if (nextBlockIndex!=-1){
                bt.addIndex(nextBlockIndex);
                if(log.isDebugEnabled()) log.debug("block index returned " + nextBlockIndex + " Blocks in route size " + blocksInRoute.size());
                //Sets the old next block to be our current block.
                currentLBlock = InstanceManager.layoutBlockManagerInstance().getLayoutBlock(nextBlock);

                offSet = new ArrayList<Integer>();

                directionOfTravel = currentLBlock.getRouteDirectionAtIndex(nextBlockIndex);

                currentBlock = nextBlock;
                nextBlock = currentLBlock.getRouteNextBlockAtIndex(nextBlockIndex);
                LayoutBlock nextLBlock = InstanceManager.layoutBlockManagerInstance().getLayoutBlock(nextBlock);
                if(log.isDebugEnabled()){
                    log.debug("Blocks in route size " + blocksInRoute.size());
                    log.debug(nextBlock.getDisplayName() + " " + destBlock.getDisplayName());
                }
                if (nextBlock==currentBlock){
                    nextBlock = currentLBlock.getRouteDestBlockAtIndex(nextBlockIndex);
                    log.debug("the next block to our destination we are looking for is directly connected to this one");
                } else if(protectingLayoutBlock!=nextLBlock){
                    log.debug("Add block " + nextLBlock.getDisplayName());
                    bt = new BlocksTested(nextLBlock);
                    blocksInRoute.add(bt);
                }
                if (nextBlock==destBlock){
                    ArrayList<LayoutBlock> returnBlocks = new ArrayList<LayoutBlock>();
                    for (int i =0; i<blocksInRoute.size(); i++){
                        returnBlocks.add(blocksInRoute.get(i).getBlock());
                    }
                    returnBlocks.add(destinationLayoutBlock);
                    if(log.isDebugEnabled()){
                        log.debug("Adding destination Block " + destinationLayoutBlock.getDisplayName());
                        log.debug("arrived at destination block");
                        log.debug(sourceLayoutBlock.getDisplayName() + " Return as Long");
                        for (int i =0; i<returnBlocks.size(); i++){
                            log.debug(returnBlocks.get(i).getDisplayName());
                        }
                        log.debug("Finished List");
                    }
                    return returnBlocks;
                }
            } 
            else {
            //-1 is returned when there are no more valid besthop valids found
                //Block index is -1, so we need to go back a block and find another way.

                //So we have gone back as far as our starting block so we better return.
                int birSize = blocksInRoute.size();
                log.debug("block in route size " + birSize);
                if (birSize<=2) {
                    log.debug("drop out here with ttl");
                    ttl=ttlSize+1;
                }
                else {
                    if(log.isDebugEnabled()){
                        for (int t = 0; t<blocksInRoute.size(); t++){
                            log.debug("index " + t + " block " + blocksInRoute.get(t).getBlock().getDisplayName());
                        }
                        log.debug("To remove last block " + blocksInRoute.get(birSize-1).getBlock().getDisplayName());
                    }

                    currentBlock = blocksInRoute.get(birSize-3).getBlock().getBlock();
                    nextBlock = blocksInRoute.get(birSize-2).getBlock().getBlock();
                    offSet = blocksInRoute.get(birSize-2).getTestedIndexes();
                    bt = blocksInRoute.get(birSize-2);
                    blocksInRoute.remove(birSize-1);
                    ttl--;
                }
            }
            ttl++;
        }
        if(ttl==ttlSize){
            lastErrorMessage = "ttlExpired";
        }
        //we exited the loop without either finding the destination or we had error.
        throw new jmri.JmriException(lastErrorMessage);
    }
    
    static class BlocksTested {
        
        LayoutBlock block;
        ArrayList<Integer> indexNumber = new ArrayList<Integer>();
        
        BlocksTested(LayoutBlock block){
            this.block=block;
        }
        
        void addIndex (int x){
            indexNumber.add(x);
        }
        
        int getLastIndex(){
            return indexNumber.get(indexNumber.size()-1); //get the last one in the list
        }
        
        List<Integer> getTestedIndexes(){
            return indexNumber;
        }
        
        LayoutBlock getBlock(){
            return block;
        }
    }

    private boolean canLBlockBeUsed(LayoutBlock lBlock){
        if (lBlock.getBlock().getPermissiveWorking())
            return true;
        if (lBlock.getState()==Block.OCCUPIED)
            return false;
        if (lBlock.getUseExtraColor())
            return false;
        return true;
    }
    
    String lastErrorMessage = "Unknown Error Occured";
    //We need to take into account if the returned block has a signalmast attached.
    int findBestHop(final Block preBlock, final Block currentBlock, Block destBlock, int direction, List<Integer> offSet, boolean validateOnly, int pathMethod){
        org.apache.log4j.Logger lBlockManLog = Logger.getLogger(InstanceManager.layoutBlockManagerInstance().getClass().getName());
        org.apache.log4j.Level currentLevel = lBlockManLog.getLevel();
        int blockindex = 0;
        Block block;
        LayoutBlock currentLBlock = InstanceManager.layoutBlockManagerInstance().getLayoutBlock(currentBlock);
        ArrayList<Integer> blkIndexTested = new ArrayList<Integer>(5);
        if(log.isDebugEnabled())
            log.debug("In find best hop current " + currentLBlock.getDisplayName() + " previous " + preBlock.getDisplayName());
        while(blockindex!=-1){
            if (currentBlock==preBlock){
                //Basically looking for the connected block, which there should only be one of!
                blockindex = currentLBlock.getConnectedBlockRouteIndex(destBlock, direction);
            } else {
                blockindex = currentLBlock.getNextBestBlock(preBlock, destBlock, offSet, METRIC);
            }
            if (blockindex!=-1){
                block = currentLBlock.getRouteNextBlockAtIndex(blockindex);
                LayoutBlock lBlock = InstanceManager.layoutBlockManagerInstance().getLayoutBlock(block);
                if ((block == currentBlock) && (currentLBlock.getThroughPathIndex(preBlock, destBlock)==-1)){
                    lastErrorMessage="block " + block.getDisplayName() + " is directly attached, however the route to the destination block " + destBlock.getDisplayName() + " can not be directly used";
                    log.debug(lastErrorMessage);
                }
                else if ((validateOnly) || (canLBlockBeUsed(lBlock))){
                    if(log.isDebugEnabled()){
                        log.debug(block.getDisplayName() + " not occupied & not reserved but we need to check if the anchor point between the two contains a signal or not");
                        log.debug(currentBlock.getDisplayName() + " " + block.getDisplayName());
                    }
                    Block blocktoCheck = block;
                    if (block == currentBlock){
                        log.debug("current block matches returned block therefore the next block is directly connected");
                        blocktoCheck=destBlock;
                    }
                    jmri.NamedBean signal = null;
                    /* We change the logging level to fatal in the layout block manager as we are testing to make sure that no signalhead/mast exists
                       this would generate an error message that is expected.*/
                    lBlockManLog.setLevel(org.apache.log4j.Level.FATAL);
                    switch(pathMethod){
                        case MASTTOMAST : signal = InstanceManager.layoutBlockManagerInstance().getFacingSignalMast(currentBlock, blocktoCheck); break;
                        case HEADTOHEAD : signal = InstanceManager.layoutBlockManagerInstance().getFacingSignalHead(currentBlock, blocktoCheck); break;
                        default : signal = (jmri.NamedBean) InstanceManager.layoutBlockManagerInstance().getFacingSignalObject(currentBlock, blocktoCheck); break;
                    }
                    lBlockManLog.setLevel(currentLevel);
                    if (signal==null){
                        log.debug("No object found so okay to return");
                        return blockindex;
                    } else {
                        lastErrorMessage ="Signal " + signal.getDisplayName() + " already exists between blocks " + currentBlock.getDisplayName() + " and " + blocktoCheck.getDisplayName() + " in the same direction on this path";
                        log.debug(lastErrorMessage);
                    }
                } else {
                    lastErrorMessage="block " + block.getDisplayName() + " found not to be not usable";
                    log.debug(lastErrorMessage);
                }
                if(blkIndexTested.contains(blockindex)){
                    lastErrorMessage=("No valid free path found");
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
    
    /**
    *   Discovers valid pairs of beans type T assigned to a layout editor.
    *   If no bean type is provided, then either SignalMasts or Sensors are discovered
    *   If no editor is provided, then all editors are considered
    */
    public Hashtable<NamedBean, ArrayList<NamedBean>> discoverValidBeanPairs(LayoutEditor editor, Class<?> T){
        LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        Hashtable<NamedBean, ArrayList<NamedBean>> retPairs = new Hashtable<NamedBean, ArrayList<NamedBean>>();
        ArrayList<FacingProtecting> beanList = generateBlocksWithBeans(editor, T);
        for(int i = 0; i<beanList.size(); i++){
            if(log.isDebugEnabled())
                try{
                    log.debug("\nSource " + beanList.get(i).getBean().getDisplayName());
                    log.debug("facing " + beanList.get(i).getFacing().getDisplayName());
                    log.debug("protecting " + beanList.get(i).getProtecting().getDisplayName());
                } catch (java.lang.NullPointerException e){
                    //Can be considered normal if the signalmast is assigned to an end bumper.
                }
            Block facing = beanList.get(i).getFacing();
            LayoutBlock lFacing = lbm.getLayoutBlock(facing);
            Block protecting = beanList.get(i).getProtecting();
            LayoutBlock lProtecting = lbm.getLayoutBlock(protecting);
            NamedBean source = beanList.get(i).getBean();
            try {
                retPairs.put(source, discoverPairDest(source, lProtecting, lFacing, beanList));
            } catch (JmriException ex){
                log.error(ex.toString());
            }
        }
        return retPairs;
    }
    
   /**
    *   Returns a list of valid destination beans reachable from a given source bean.
    * @param source Either a SignalMast or Sensor
    * @param editor The layout editor that the source is located on, if null, then all editors are considered
    * @param T The class of the remote destination, if null, then both SignalMasts and Sensors are considered
    * @return A list of all reachable NamedBeans
    */
    public List<NamedBean> discoverPairDest(NamedBean source, LayoutEditor editor, Class<?> T) throws JmriException{
        LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        LayoutBlock lFacing = lbm.getFacingBlockByNamedBean(source, editor);
        LayoutBlock lProtecting = lbm.getProtectedBlockByNamedBean(source, editor);
        try {
            return discoverPairDest(source, lProtecting, lFacing, generateBlocksWithBeans(editor, T));
        } catch (JmriException e){
            throw e;
        }
    }
    
    ArrayList<NamedBean> discoverPairDest(NamedBean source, LayoutBlock lProtecting, LayoutBlock lFacing, ArrayList<FacingProtecting> blockList) throws JmriException{
        LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        if(!lbm.isAdvancedRoutingEnabled()){
            throw new JmriException("advanced routing not enabled");
        }
        if(!lbm.routingStablised()){
            throw new JmriException("routing not stablised");
        }
        ArrayList<NamedBean> validDestBean = new ArrayList<NamedBean>();
        for (int j = 0; j<blockList.size(); j++){
            if (blockList.get(j).getBean()!=source){
 //               boolean alreadyExist = false;
                NamedBean destObj = blockList.get(j).getBean();
                if(log.isDebugEnabled())
                    log.debug("looking for pair " + source.getDisplayName() + " " + destObj.getDisplayName());
                try {
                    if(checkValidDest(lFacing, lProtecting, blockList.get(j))){
                        if(log.isDebugEnabled())
                            log.debug("Valid pair " + source.getDisplayName() + " " + destObj.getDisplayName());
                        LayoutBlock ldstBlock = lbm.getLayoutBlock(blockList.get(j).getFacing());
                        try {
                            ArrayList<LayoutBlock> lblks = getLayoutBlocks(lFacing, ldstBlock, lProtecting, true, MASTTOMAST);
                            if(log.isDebugEnabled())
                                log.debug("Adding block " + destObj.getDisplayName() + " to paths, current size " + lblks.size());
                            validDestBean.add(destObj);
                        } catch (jmri.JmriException e){  // Considered normal if route not found.
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


    ArrayList<FacingProtecting> generateBlocksWithBeans(LayoutEditor editor, Class<?> T){
        LayoutBlockManager lbm = InstanceManager.layoutBlockManagerInstance();
        ArrayList<FacingProtecting> beanList = new ArrayList<FacingProtecting>();
    
        List<String> lblksSysName = lbm.getSystemNameList();
        for(int i = 0; i<lblksSysName.size(); i++){
            LayoutBlock curLblk = lbm.getLayoutBlock(lblksSysName.get(i));
            Block curBlk = curLblk.getBlock();
            if(curBlk!=null){
                int noNeigh = curLblk.getNumberOfNeighbours();
                for(int x = 0; x<noNeigh; x++){
                    Block blk = curLblk.getNeighbourAtIndex(x);
                    
                    if(T==null) {
                        NamedBean sourceBean = lbm.getFacingNamedBean(curBlk, blk, editor);
                        if(sourceBean!=null){
                            FacingProtecting toadd = new FacingProtecting(curBlk, blk, sourceBean);
                            if(!beanList.contains(toadd)){
                                beanList.add(toadd);
                            }
                        }
                    } else if (T.equals(SignalMast.class)){
                        NamedBean sourceBean = lbm.getFacingSignalMast(curBlk, blk, editor);
                        if(sourceBean!=null){
                            FacingProtecting toadd = new FacingProtecting(curBlk, blk, sourceBean);
                            if(!beanList.contains(toadd)){
                                beanList.add(toadd);
                            }
                        }
                    } else if (T.equals(Sensor.class)){
                        NamedBean sourceBean = lbm.getFacingSensor(curBlk, blk, editor);
                        if(sourceBean!=null){
                            FacingProtecting toadd = new FacingProtecting(curBlk, blk, sourceBean);
                            if(!beanList.contains(toadd)){
                                beanList.add(toadd);
                            }
                        }
                    }  else {
                        log.error("Past bean type is unknown " + T);
                    }
                }
                if (noNeigh==1){
                    if(log.isDebugEnabled())
                        log.debug("We have a dead end " + curBlk.getDisplayName());
                    if(T==null){
                        NamedBean destBean = lbm.getNamedBeanAtEndBumper(curBlk, editor);
                        if(destBean!=null){
                            FacingProtecting toadd = new FacingProtecting(curBlk, null, destBean);
                            if(!beanList.contains(toadd)){
                                beanList.add(toadd);
                            }
                            if(log.isDebugEnabled())
                                log.debug("We have found dest bean " + destBean.getDisplayName());
                        }
                    } else if (T.equals(SignalMast.class)){
                        NamedBean destBean = lbm.getSignalMastAtEndBumper(curBlk, editor);
                        if(destBean!=null){
                            FacingProtecting toadd = new FacingProtecting(curBlk, null, destBean);
                            if(!beanList.contains(toadd)){
                                beanList.add(toadd);
                            }
                            if(log.isDebugEnabled())
                                log.debug("We have found dest bean " + destBean.getDisplayName());
                        }
                    } else if (T.equals(Sensor.class)){
                        NamedBean sourceBean = lbm.getSensorAtEndBumper(curBlk, editor);
                        if(sourceBean!=null){
                            FacingProtecting toadd = new FacingProtecting(curBlk, null, sourceBean);
                            if(!beanList.contains(toadd)){
                                beanList.add(toadd);
                            }
                        }
                    } else {
                        log.error("Past bean type is unknown " + T);
                    }
                }
            }
        }
        return beanList;
    }
    
    static class FacingProtecting{
        Block facing;
        Block protecting;
        NamedBean bean;
        
        FacingProtecting(Block facing, Block protecting, NamedBean bean){
            this.facing = facing;
            this.protecting = protecting;
            this.bean = bean;
        }
        
        Block getFacing() { return facing; }
        
        Block getProtecting() { return protecting; }
        
        NamedBean getBean() { return bean; }
        
        @Override
        public boolean equals(Object obj){
            if(obj ==this)
                return true;
            if(obj ==null){
                return false;
            }
                
            if(!(getClass()==obj.getClass())){
                return false;
            }
            else{
                FacingProtecting tmp = (FacingProtecting)obj;
                if(tmp.getFacing()!=this.facing){
                    return false;
                }
                if(tmp.getProtecting()!=this.protecting){
                    return false;
                }
                if(tmp.getBean()!=this.bean){
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
            hash = 37 * hash + (this.protecting != null ? this.protecting.hashCode() : 0);
            return hash;
        }
    }
    
    static Logger log = Logger.getLogger(LayoutBlockConnectivityTools.class.getName());

}
