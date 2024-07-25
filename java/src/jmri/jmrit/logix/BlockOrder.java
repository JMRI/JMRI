package jmri.jmrit.logix;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.BeanSetting;
import jmri.jmrit.logix.TrainOrder.Cause;

/**
 * A BlockOrder is a row in the route of the warrant.
 * It contains where the warranted train enters a block, the path it takes and
 * where it exits the block.
 * (The route is a list of BlockOrder.)
 * The Engineer is notified when the train enters the block.
 *
 * @author Pete Cressman Copyright (C) 2009
 */
public class BlockOrder {

    private int _index;
    private OBlock _block;     // OBlock of these orders
    private String _pathName;  // path the train is to take in the block
    private String _entryName; // Name of entry Portal
    private String _exitName;  // Name of exit Portal
    private float _pathLength; // path length in millimeters

    public BlockOrder(@Nonnull OBlock block) {
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
    public BlockOrder(@Nonnull OBlock block, String path, String entry, String exit) {
        this(block);
        _pathName = path;
        _entryName = entry;
        _exitName = exit;
    }

    // for use by WarrantTableFrame 
    protected BlockOrder(@Nonnull BlockOrder bo) {
        _index = bo.getIndex();
        _block = bo.getBlock();
        _pathName = bo.getPathName();
        _entryName = bo.getEntryName();
        _exitName = bo.getExitName();
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
    }

    public String getPathName() {
        return _pathName;
    }

    protected OPath getPath() {
        return _block.getPathByName(_pathName);
    }

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
    protected TrainOrder allocatePaths(@Nonnull Warrant warrant, boolean allocate) {
        if (_pathName == null) {
            log.error("setPaths({}) - {}", warrant.getDisplayName(),
                Bundle.getMessage("NoPaths", _block.getDisplayName()));
            return new TrainOrder(Warrant.Stop, Cause.ERROR, _index, _index, 
                    Bundle.getMessage("NoPaths", _block.getDisplayName()));
        }
        log.debug("{}: calls allocatePaths() in block \"{}\" for path \"{}\". _index={}",
                warrant.getDisplayName(), _block.getDisplayName(), _pathName, _index);
        TrainOrder to = findStopCondition(this, warrant);
        if (to != null && Warrant.Stop.equals(to._speedType)) {
            return to;
        }
        String msg = _block.allocate(warrant);
        if (msg != null) {  // unnecessary, findStopCondition() already has checked
            return new TrainOrder(Warrant.Stop, Cause.ERROR, _index, _index, msg);
        }

        // Check if next block can be allocated
        BlockOrder bo1 = warrant.getBlockOrderAt(_index + 1);
        if (bo1 != null) {
            OBlock nextBlock = bo1.getBlock();
            TrainOrder to1 = findStopCondition(bo1, warrant);
            if (to1 == null || !Warrant.Stop.equals(to1._speedType)) { // Train may enter block of bo1
                if (allocate) {
                    nextBlock.allocate(warrant);
                    nextBlock.showAllocated(warrant, bo1.getPathName());
                }
            } else {
                // See if path to exit can be set without messing up block of bo1
                OPath path1 = getPath();
                Portal exit = getExitPortal();
                msg =  pathsConnect(path1, exit, bo1.getBlock());
            }
            if (msg != null) {
                // cannot set path
                return new TrainOrder(Warrant.Stop, ( to1 != null ? to1._cause : Cause.WARRANT), 
                    bo1.getIndex(), _index, msg);
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
        if (allocate) {
            msg = setPath(warrant);
            if (msg != null) {  // unnecessary, already been checked
                return new TrainOrder(Warrant.Stop, Cause.ERROR, _index, _index, msg);
            }
        }
        if (to != null) {
            return to;
        }
        return new TrainOrder(null, Cause.NONE, _index, _index, null);
    }

    @CheckForNull
    private TrainOrder findStopCondition(@Nonnull BlockOrder bo, @Nonnull Warrant warrant) {
        OBlock block = bo.getBlock();
        Warrant w = block.getWarrant();
        if (w != null && !warrant.equals(w)) {
           return new TrainOrder(Warrant.Stop, Cause.WARRANT, bo.getIndex(), bo.getIndex(),
                   Bundle.getMessage("AllocatedToWarrant",
                   w.getDisplayName(), block.getDisplayName(), w.getTrainName()));
        }
        if (block.isOccupied()) {
            String rogue = (String)block.getValue();
            if (rogue == null) {
                rogue = Bundle.getMessage("unknownTrain");
            }
            if (!rogue.equals(warrant.getTrainName())) {
                return new TrainOrder(Warrant.Stop, Cause.OCCUPY, bo.getIndex(), bo.getIndex(),
                        Bundle.getMessage("blockInUse", rogue, block.getDisplayName()));
            }
        }
        String speedType = getPermissibleSpeedAt(bo);
        if (speedType != null) {
            String msg;
            if (Warrant.Stop.equals(speedType)) {
                msg = Bundle.getMessage("BlockStopAspect", block.getDisplayName(), speedType);
            } else {
                msg = Bundle.getMessage("BlockSpeedAspect", block.getDisplayName(), speedType);
            }
            return new TrainOrder(speedType, Cause.SIGNAL, bo.getIndex(), bo.getIndex(), msg);
        }
        return null;
    }

    @CheckForNull
    protected String pathsConnect(@Nonnull OPath path1, @CheckForNull Portal exit, @CheckForNull OBlock block) {
        if (exit == null || block == null) {
            return null;
        }
        
        OPath path2 = block.getPath();
        if (path2 == null) {
            return null;
        }
        for (BeanSetting bs1 : path1.getSettings()) {
            for (BeanSetting bs2 : path2.getSettings()) {
                if (bs1.getBean().equals(bs2.getBean())) {
                    // TO is shared (same bean)
                    if (log.isDebugEnabled()) {
                        if (bs1.equals(bs2)) {
                            log.debug("Path \"{}\" in block \"{}\" and \"{}\" in block \"{}\" agree on setting of shared turnout \"{}\"",
                                    path1.getName(), _block.getDisplayName(), path2.getName(), block.getDisplayName(), 
                                    bs1.getBean().getDisplayName());
                        } else {
                            log.debug("Path \"{}\" in block \"{}\" and \"{}\" in block \"{}\" have opposed settings of shared turnout \"{}\"",
                                    path1.getName(), _block.getDisplayName(), path2.getName(), block.getDisplayName(), 
                                    bs1.getBean().getDisplayName());
                        }
                    }
                    return  Bundle.getMessage("SharedTurnout", bs1.getBean().getDisplayName(), _block.getDisplayName(), block.getDisplayName());
                }
            }
        }
        return null;
    }

    protected static String getPermissibleSpeedAt(BlockOrder bo) {
        String speedType = bo.getPermissibleEntranceSpeed();
        if (speedType != null) {
            log.debug("getPermissibleSpeedAt(): \"{}\" Signal speed= {}",
                bo.getBlock().getDisplayName(), speedType);
        } else { //  if signal is configured, ignore block
            speedType = bo.getBlock().getBlockSpeed();
            if (speedType.equals("")) {
                speedType = null;
            }
            if (speedType != null) {
                log.debug("getPermissibleSpeedAt(): \"{}\" Block speed= {}",
                    bo.getBlock().getDisplayName(), speedType);
            }
        }
        return speedType;
    }

    protected void setPathLength(float len) {
        _pathLength = len;
    }

    protected float getPathLength() {
        if (_pathLength <= 0) {
            OPath p  = getPath();
            if (p != null) {
                _pathLength = p.getLengthMm();
            } else {
                _pathLength = 0;
            }
        }
        return _pathLength;
    }

    protected void setBlock(@Nonnull OBlock block) {
        _block = block;
    }

    @Nonnull
    public OBlock getBlock() {
        return _block;
    }

    @CheckForNull
    protected Portal getEntryPortal() {
        if (_entryName == null) {
            return null;
        }
        return _block.getPortalByName(_entryName);
    }

    @CheckForNull
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
     * Get the signal protecting entry into the block of this BlockOrder.
     * @return signal
     */
    @CheckForNull
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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BlockOrder.class);

}
