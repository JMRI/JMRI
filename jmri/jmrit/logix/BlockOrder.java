
package jmri.jmrit.logix;

//import jmri.Path;
import jmri.SignalHead;

/**
 * An BlockOrder is a row in the warrant.  It contains the directives the Engineer
 * must do when in a block
 * <P>
 * 
 *
 * @author	Pete Cressman  Copyright (C) 2009
 */
public class BlockOrder  {

    private OBlock  _block;     // OBlock of these orders
    private String  _pathName;  // path the train is to take in the block
    private String  _entryName; // Name of entry Portal
    private String  _exitName;  // Name of exit Portal

    public BlockOrder(OBlock block) {
        _block = block;
    }

    /**
     * Create BlockOrder.
     *@param block
     *@param path MUST be a path in the blocK
     *@param entry MUST be a name of a Portal to the path
     *@param exit MUST be a name of a Portal to the path
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

    public void setEntryName(String name) { _entryName = name; }
    public String getEntryName() { return _entryName; }


    public void setExitName(String name) { _exitName = name; }
    public String getExitName() { return _exitName; }

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

    public boolean validateOrder() {
        return true;
    }

    /**
    * Set Path. Note that the Path's 'fromPortal' and 'toPortal' have no bearing on 
    * the BlockOrder's entryPortal and exitPortal.
    */
    public void setPathName(String path) {
        _pathName = path;
    }
    public String getPathName() { return _pathName; }

    public OPath getPath() { return _block.getPathByName(_pathName); }

    public void setBlock(OBlock block) { _block = block; }

    public OBlock getBlock() { return _block; }

    public Portal getEntryPortal() {
        if (_entryName==null) { return null; }
        return _block.getPortalByName(_entryName);
    }

    public Portal getExitPortal() {
        if (_exitName==null) { return null; }
        return _block.getPortalByName(_exitName);
    }

    /**
    * Set the signal our train sees entering block
    */
    public void setEntrySignal(int appearance) {
        Portal portal = getEntryPortal();
        if (portal!=null) {
            portal.setSignal(_block, appearance); 
        }
    }

    public int getEntrySignalAppearance() {
        Portal portal = getEntryPortal();
        if (portal!=null) {
            return portal.getSignalAppearance(_block); 
        }
        return SignalHead.DARK;
    }

    /**
    * Set signal an opposing train sees at our train's block entry portal
    * i.e. what another train traveling in the opposite direction would see. 
    */
    public void setOpposingEntrySignal(int appearance) {
        Portal portal = getEntryPortal();
        if (portal!=null) {
            portal.setOpposingSignal(_block, appearance); 
        }
    }

    /**
    * Signal appearance our train sees at exit of block
    */
    public int getExitSignalAppearance() {
        Portal portal = getExitPortal();
        if (portal!=null) {
            return portal.getSignalAppearance(_block); 
        }
        return SignalHead.DARK;
    }

    /**
    * Set signal our train sees exiting block
    */
    public void setExitSignal(int appearance) {
        Portal portal = getExitPortal();
        if (portal!=null) {
            portal.setSignal(_block, appearance); 
        }
    }

    /**
    * Set signal opposing train sees at our train's block exit portal
    */
    public void setOpposingExitSignal(int appearance) {
        Portal portal = getExitPortal();
        if (portal!=null) {
            portal.setOpposingSignal(_block, appearance); 
        }
    }

    /**
    * Set all entry portal signals 'Stop' and exit portals 'approach'
    * set path portal signals 'clear' and opposing path portal signals 'stop'
    */
    public void setPathAndSignalProtection(int delay, int enterAppearance, int exitAppearance) {
        OBlock block = getBlock();
        block.setPath(getPathName(), delay);
        block.setSignalProtection();
        Portal portal = block.getPortalByName(getEntryName());
        if (portal!=null) {
            portal.setOpposingSignal(block, enterAppearance);
            portal.setSignal(block, SignalHead.RED);
        }
        portal = block.getPortalByName(getExitName());
        if (portal!=null) {
            portal.setOpposingSignal(block, SignalHead.RED);
            portal.setSignal(block, exitAppearance);
        }
        if (log.isDebugEnabled()) log.debug("setPathAndSignalProtection for "+this.toString());
    }

    public String toString() {
        return ("BlockOrder: Path "+_pathName+" in Block "+_block.getDisplayName()+ 
        " enters at "+_entryName+" and exits at "+_exitName);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BlockOrder.class.getName());
}
