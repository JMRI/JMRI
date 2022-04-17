package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.BeanSetting;
import javax.annotation.Nonnull;
import jmri.jmrit.logix.TrainOrder.Cause;


/**
 * A BlockOrder is a row in the route of the warrant. It contains 
 * where the warranted train enters a block, the path it takes and
 * where it exits the block. (The route is a list of BlockOrder.)
 * The Engineer is notified when the train enters the block.
 *
 * @author Pete Cressman Copyright (C) 2009
 */
public class BlockOrder {

    private static final Logger log = LoggerFactory.getLogger(BlockOrder.class);

    private int _index;
    private OBlock _block;     // OBlock of these orders
    private String _pathName;  // path the train is to take in the block
    private String _entryName; // Name of entry Portal
    private String _exitName;  // Name of exit Portal
    private float _tempPathLen; // hold user's input for this session

    public BlockOrder(OBlock block) {
        _block = block;
    }

    /**
     * Create BlockOrder.
     *
     * @param block OBlock of this order
     * @param path  MUST be a path in the blocK
     * @param entry MUST be a name of a Portal to the path
     * @param exit  MUST be a name of a Portal to the path
     */
    public BlockOrder(OBlock block, String path, String entry, String exit) {
        this(block);
        _pathName = path;
        _entryName = entry;
        _exitName = exit;
//         log.debug("ctor1: {}",this);
    }

    // for use by WarrantTableFrame 
    protected BlockOrder(BlockOrder bo) {
        _index = bo._index;
        _block = bo._block;      // shallow copy OK. WarrantTableFrame doesn't write to block
        _pathName = bo._pathName;
        _entryName = bo._entryName;
        _exitName = bo._exitName;
//        log.debug("ctor2: {}",this);
    }

    public void setIndex(int idx) {
        _index = idx;
    }

    public int getIndex() {
        return _index;
    }

    protected void setEntryName(String name) {
        _entryName = name;
    }

    public String getEntryName() {
        return _entryName;
    }

    protected void setExitName(String name) {
        _exitName = name;
    }

    public String getExitName() {
        return _exitName;
    }

    /**
     * Set Path. Note that the Path's 'fromPortal' and 'toPortal' have no
     * bearing on the BlockOrder's entryPortal and exitPortal.
     * @param path  Name of the OPath connecting the entry and exit Portals
     */
    protected void setPathName(String path) {
        _pathName = path;
        _tempPathLen =0.0f;
    }

    public String getPathName() {
        return _pathName;
    }

    protected OPath getPath() {
        return _block.getPathByName(_pathName);
    }

    @Deprecated
    protected String setPath(Warrant warrant) {
        String msg = _block.setPath(getPathName(), warrant);
        if (msg == null) {
            Portal p = getEntryPortal();
            if (p != null) {
                p.setEntryState(_block);
            }
        }
        return msg;
    }
    
    @Nonnull
    protected TrainOrder setPath(Warrant warrant, boolean show) {
        /*
        if (_block.getDisplayName().equals("FarWest")) {
            log.debug("\"{}\" {}", _block.getDisplayName());
        }*/
        if (_pathName == null) {
            log.error("setPath({}) - {}", warrant.getDisplayName(), Bundle.getMessage("NoPaths", _block.getDisplayName()));
            return new TrainOrder(Warrant.Stop, Cause.ERROR, _index, _index, 
                    Bundle.getMessage("NoPaths", _block.getDisplayName()));
        }
        TrainOrder to = null;
        if (show) { // display route, NOT run warrant
            to = findStopCondition(this, warrant);
            if (to != null) {
                if (!to._cause.equals(Cause.OCCUPY) && !to._cause.equals(Cause.WARRANT)) {
                    _block.allocate(warrant);
                    _block.setPath(_pathName, warrant);
                }
                return to;
            }
        }
        if (log.isDebugEnabled()) { 
            log.debug("{}: calls setPath() in block \"{}\" for path \"{}\". _index={}",
                    warrant.getDisplayName(), _block.getDisplayName(), _pathName, _index); 
        }
        String msg = _block.allocate(warrant);
        if (msg != null) {
            return new TrainOrder(Warrant.Stop, Cause.WARRANT, _index, _index, msg);
        }
        if (log.isDebugEnabled()) { 
            log.debug("{}:  block \"{}\" allocated.",
                    warrant.getDisplayName(), _block.getDisplayName()); 
        }

        // Check if next block can be allocated
        BlockOrder bo1 = warrant.getBlockOrderAt(_index + 1);
        if (bo1 != null) {
            OBlock nextBlock = bo1.getBlock();
            to = findStopCondition(bo1, warrant);
            if (to == null) { // Train may enter block of bo1
                nextBlock.allocate(warrant);
                nextBlock.ShowAllocated(warrant, bo1._pathName);
                to = checkForSharedTO(nextBlock, bo1,  warrant);
            }
            // Crossovers typically have both switches controlled by one TO, 
            // yet each switch is in a different block. Setting the path may change
            // a shared TO for another warrant and change its path to
            // short or derail its train entering the block. So we may not allow
            // this warrant to set the path in bo1.  
            // Because the path in bo1 cannot be set, it is not safe to enter
            // the next block. The warrant must hold the train in this block.
            // However, the path in this block may be set
        }
        Portal p = getEntryPortal();
        if (p == null) {
            if (_index > 0) {
                log.error("setPath({}) - block \"{}\" has no Entry Portal!", warrant.getDisplayName(), _block.getDisplayName());
                return new TrainOrder(Warrant.Stop, Cause.ERROR, _index, _index, "No Entry Portal into block "+_block.getDisplayName());
            }
        } else {
            p.setEntryState(_block);
        }
        msg = _block.setPath(_pathName, warrant);
        if (msg != null) {      // should not happen. Block is allocated.
            to = new TrainOrder(Warrant.Stop, Cause.ERROR, _index, _index, msg);
        } else if (log.isDebugEnabled()) { 
            log.debug("{}: set path \"{}\" in block \"{}\"",
                    warrant.getDisplayName(), _pathName, _block.getDisplayName(), _pathName); 
        }

        if (to != null) {
            return to;
        }
        return new TrainOrder(null, Cause.NONE, _index, _index, null);
    }

    /*
     * @param block = block connecting to exit portal of the block of
     * this BlockOrder. "block" is the block of the next BlockOrder,
     * i.e. block of BlockOrder _index + 1
     */
    private TrainOrder checkForSharedTO (OBlock block, BlockOrder bo1, Warrant warrant) {
        Portal exit = bo1.getExitPortal();
        if (exit == null) {
            return null;
        }
        // Check what bo1 connects to at bo2
        BlockOrder bo2 = warrant.getBlockOrderAt(_index + 2);
        if (bo2 != null) {
            Cause cause = enterable(bo2, warrant);
            if (!Cause.NONE.equals(cause)) {
                // block of bo2 cannot be entered, 
                OPath path1 = bo1.getPath();
                if (path1 == null) {
                    log.warn("NULL PATH on block \"{}\" of BlockOrder at index = {}",
                            bo1.getBlock().getDisplayName(), _index + 1); 
                    return new TrainOrder(Warrant.Stop, Cause.ERROR, bo1._index, bo1._index, 
                            Bundle.getMessage("NoPaths", bo1.getBlock().getDisplayName()));
                }
                // See if exit can be set without messing up block of bo2
                String msg =  pathsConnect(path1, exit, bo2, cause);
                if (msg != null) {
                    return new TrainOrder(Warrant.Stop, cause, bo2._index, bo1._index, msg);
                }
           }
        }
        return null;
    }

    protected static Cause enterable(BlockOrder bo, Warrant warrant) {
        OBlock block = bo.getBlock();
        Warrant w = block.getWarrant();
        if (w != null && !warrant.equals(w)) {
            return Cause.WARRANT;
        }
        if (block.isOccupied()) {
            return Cause.OCCUPY;
        }
        if (Warrant.Stop.equals(getPermissibleSpeedAt(bo))) {
            return Cause.SIGNAL;
        }
        return Cause.NONE;
    }

    private TrainOrder findStopCondition(BlockOrder bo, Warrant warrant) {
        OBlock block = bo.getBlock();
        Warrant w = block.getWarrant();
        if (w != null && !warrant.equals(w)) {
           return new TrainOrder(Warrant.Stop, Cause.WARRANT, bo._index, bo._index,
                   Bundle.getMessage("AllocatedToWarrant",
                   w.getDisplayName(), block.getDisplayName(), w.getTrainName()));
        }
        if (block.isOccupied()) {
            String rogue = (String)block.getValue();
            if (rogue == null) {
                rogue = Bundle.getMessage("unknownTrain");
            }
            String train = warrant.getTrainName();
            if (!rogue.equals(train)) {
                return new TrainOrder(Warrant.Stop, Cause.OCCUPY, bo._index, bo._index,
                        Bundle.getMessage("blockInUse", train, block.getDisplayName()));
            }
        }
        String speedType = getPermissibleSpeedAt(bo);
        if (Warrant.Stop.equals(speedType)) {
            return new TrainOrder(speedType, Cause.SIGNAL, bo._index, bo._index,
                    Bundle.getMessage("BlockStopAspect", block.getDisplayName()));
        }
        return null;
    }

    private String pathsConnect(OPath path1, Portal exit, BlockOrder bo2, Cause cause) {
        OPath path2 = bo2._block.getPath();
        if (path2 == null) {
            if (log.isDebugEnabled()) {
                log.debug("No path found at non-enterable block \"{}\"", bo2._block.getDisplayName());
            }
            return null;
        }
        if (exit.equals(path2.getToPortal()) || exit.equals(path2.getFromPortal())) {
            if (log.isDebugEnabled()) {
                log.debug("Path \"{}\" and \"{}\" in block \"{}\" connect at portal \"{}\"",
                        path1.getName(), path2.getName(), bo2._block.getDisplayName(), exit.getName());
            }
            return null;
        }
        for (BeanSetting bs1 : path1.getSettings()) {
            for (BeanSetting bs2 : path2.getSettings()) {
                if (bs1.getBean().equals(bs2.getBean())) {
                    // TO is shared (same bean)
                    if (bs1.equals(bs2)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Path \"{}\" and \"{}\" in block \"{}\" agree on setting of shared turnout \"{}\"",
                                    path1.getName(), path2.getName(), bo2._block.getDisplayName(), 
                                    bs1.getBean().getDisplayName());
                        }
                        return null;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Path \"{}\" and \"{}\" in block \"{}\" have opposed settings of shared turnout \"{}\"",
                                    path1.getName(), path2.getName(), bo2._block.getDisplayName(), 
                                    bs1.getBean().getDisplayName());
                        }
                        return  Bundle.getMessage("SharedTurnoutSet", bs1.getBean().getDisplayName());
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Path \"{}\" and \"{}\" in block \"{}\" of block \"{}\" do not share a turnout or portal",
                    path1.getName(), path2.getName(), bo2._block.getDisplayName());
        }
//        return Bundle.getMessage("PathsDoNotConnect", path1.getName(), path2.getName(), bo2._block.getDisplayName());
        return null;
    }

    static protected String getPermissibleSpeedAt(BlockOrder bo) {
        String speedType = bo.getPermissibleEntranceSpeed();
        if (log.isDebugEnabled()) {
            if (speedType != null) {
                log.debug("getPermissibleSpeedAt(): \"{}\" Signal speed= {}",
                        bo._block.getDisplayName(), speedType);
            }
        } else { //  if signal is configured, ignore block
            speedType = bo._block.getBlockSpeed();
            if (speedType.equals("")) {
                speedType = null;
            }
            if (log.isDebugEnabled()) {
                if (speedType != null) {
                    log.debug("getPermissibleSpeedAt(): \"{}\" Block speed= {}",
                              bo._block.getDisplayName(), speedType);
                }
            }
        }
        return speedType;
    }

    protected void setTempPathLen(float len) {
        _tempPathLen = len;
    }

    protected float getTempPathLen() {
        return _tempPathLen;
    }

    protected void setBlock(OBlock block) {
        _block = block;
    }

    public OBlock getBlock() {
        return _block;
    }

    protected Portal getEntryPortal() {
        if (_entryName == null) {
            return null;
        }
        return _block.getPortalByName(_entryName);
    }

    protected Portal getExitPortal() {
        if (_exitName == null) {
            return null;
        }
        return _block.getPortalByName(_exitName);
    }

    /**
     * Check signals for entrance into next block.
     *
     * @return speed
     */
    protected String getPermissibleEntranceSpeed() {
        Portal portal = _block.getPortalByName(getEntryName());
        if (portal != null) {
            return portal.getPermissibleSpeed(_block, true);
        }
        return null;
    }

    protected float getEntranceSpace() {
        Portal portal = _block.getPortalByName(getEntryName());
        if (portal != null) {
            return portal.getEntranceSpaceForBlock(_block);
        }
        return 0;
    }

    /**
     * Get the signal protecting entry into the block of this blockorder
     * @return signal
     */
    protected jmri.NamedBean getSignal() {
        Portal portal = getEntryPortal();
        if (portal != null) {            
            return portal.getSignalProtectingBlock(_block);
        }
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BlockOrder: Block \"");
        sb.append( _block.getDisplayName());
        sb.append("\" has Path \"");
        sb.append(_pathName);
        sb.append("\" with Portals, entry= \"");
        sb.append(_entryName);
        sb.append("\" and exit= \"");
        sb.append(_exitName);
        sb.append("\"");
        return sb.toString();
    }
}
