package jmri.jmrit.logix;


//import jmri.Path;
//import jmri.SignalHead;
/**
 * An BlockOrder is a row in the warrant. It contains the directives the
 * Engineer must do when in a block
 * <P>
 *
 *
 * @author	Pete Cressman Copyright (C) 2009
 */
public class BlockOrder {

    private OBlock _block;     // OBlock of these orders
    private String _pathName;  // path the train is to take in the block
    private String _entryName; // Name of entry Portal
    private String _exitName;  // Name of exit Portal

    public BlockOrder(OBlock block) {
        _block = block;
    }

    /**
     * Create BlockOrder.
     *
     * @param block
     * @param path  MUST be a path in the blocK
     * @param entry MUST be a name of a Portal to the path
     * @param exit  MUST be a name of a Portal to the path
     */
    public BlockOrder(OBlock block, String path, String entry, String exit) {
        this(block);
        _pathName = path;
        _entryName = entry;
        _exitName = exit;
        //if (log.isDebugEnabled()) log.debug("ctor1: "+this.toString());
    }

    // for use by WarrantTableFrame 
    protected BlockOrder(BlockOrder bo) {
        _block = bo._block;      // shallow copy OK. WarrantTableFrame doesn't write to b;ock
        _pathName = bo._pathName;
        _entryName = bo._entryName;
        _exitName = bo._exitName;
        //if (log.isDebugEnabled()) log.debug("ctor2: "+this.toString());
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
    /*
     static String getOppositePortalName(OPath path, String portalName) {
     if (portalName==null) {
     if (path.getFromPortalName() == null) {
     return path.getToPortalName();
     } else if (path.getToPortalName() == null) {
     return path.getFromPortalName();
     }
     } else if (portalName.equals(path.getFromPortalName())) {
     return path.getToPortalName();
     } else if (portalName.equals(path.getToPortalName())) {
     return path.getFromPortalName();
     } else {
     log.error("getOppositePortalName failed. portalName \""+portalName+
     "\" not found in Path \""+path.getName()+"\".");
     }
     return null;
     }
     */

    protected boolean validateOrder() {
        return true;
    }

    /**
     * Set Path. Note that the Path's 'fromPortal' and 'toPortal' have no
     * bearing on the BlockOrder's entryPortal and exitPortal.
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
            return portal.getPermissibleEntranceSpeed(_block);
        }
        // OK if this is first block
        //log.warn("getPermissibleEntranceSpeed, no entry portal! "+this.toString());
        return null;
    }

    protected float getEntranceSpace() {
        Portal portal = _block.getPortalByName(getEntryName());
        if (portal != null) {
            return portal.getEntranceSpaceForBlock(_block);
        }
        return 0;
    }

    protected String getPermissibleExitSpeed() {
        Portal portal = _block.getPortalByName(getEntryName());
        if (portal != null) {
            return portal.getPermissibleExitSpeed(_block);
        }
        // OK if this is first block
//        log.warn("getPermissibleExitSpeed, no entry portal! "+this.toString());
        return null;
    }

    protected jmri.NamedBean getSignal() {
        return _block.getPortalByName(getEntryName()).getSignalProtectingBlock(_block);
    }

    protected String hash() {
        return _block.getDisplayName() + _pathName + _entryName + _exitName;
    }

    public String toString() {
        return ("BlockOrder: Block \"" + _block.getDisplayName() + "\" has Path \"" + _pathName
                + "\" with Portals \"" + _entryName + "\" and \"" + _exitName + "\"");
    }
}
