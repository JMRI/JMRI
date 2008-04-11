// jmri.jmrit.display.LayoutBlockManager.java

package jmri.jmrit.display;

import jmri.AbstractManager;
import jmri.Sensor;

/**
 * Implementation of a Manager to handle LayoutBlocks
 * Note: that the same LayoutBlocks may appear in multiple LayoutEditor panels.
 * <P>
 * This manager does not enforce any particular system naming convention.
 * <P>
 * LayoutBlocks are usually addressed by userName.  The systemName is hidden
 *    from the user for the most part.
 *
 * @author      Dave Duchamp Copyright (C) 2007
 * @version	$Revision: 1.3 $
 */
public class LayoutBlockManager extends AbstractManager {

    public LayoutBlockManager() {
        super();
    }

    public char systemLetter() { return 'I'; }
    public char typeLetter() { return 'B'; }
	private int blkNum = 1;
    
    /**
     * Method to create a new LayoutBlock if the LayoutBlock does not exist
     *   Returns null if a LayoutBlock with the same systemName or userName
     *       already exists, or if there is trouble creating a new LayoutBlock.
	 *   Note that since the userName is used to address LayoutBlocks, the 
	 *       user name must be present.  If the user name is not present,
	 *       the new LayoutBlock is not created, and null is returned.
     */
    public LayoutBlock createNewLayoutBlock(String systemName, String userName) {
        // Check that LayoutBlock does not already exist
        LayoutBlock block = null;
        if (userName == null || userName.equals("")) {
			log.error("Attempt to create a LayoutBlock with no user name");
			return null;
		}
		block = getByUserName(userName);
		if (block!=null) return null;
		// here if not found under user name
		String sName = "";
		if (systemName == null) {
			// create a new unique system name
			boolean found = true;
			while (found) {
				sName = "ILB"+blkNum;
				blkNum++;
				block = getBySystemName(sName);
				if (block==null) found = false;
			}
		}
		else {
			// try the supplied system name
			block = getBySystemName((systemName.toUpperCase()));
			if (block!=null) return null;
			sName = systemName.toUpperCase();
		}
        // LayoutBlock does not exist, create a new LayoutBlock
        block = new LayoutBlock(sName,userName);
        if (block!=null) {
            // save in the maps
            register(block);
        }
        return block;
    }

    /**
     * Remove an existing LayoutBlock. 
	 */
    public void deleteLayoutBlock(LayoutBlock block) {
        deregister(block);
    }
    
    /** 
     * Method to get an existing LayoutBlock.  First looks up assuming that
     *      name is a User Name.  If this fails looks up assuming
     *      that name is a System Name.  If both fail, returns null.
     */
    public LayoutBlock getLayoutBlock(String name) {
        LayoutBlock block = getByUserName(name);
        if (block!=null) return block;
        return getBySystemName(name);
    }

    public LayoutBlock getBySystemName(String name) {
		String key = name.toUpperCase();
        return (LayoutBlock)_tsys.get(key);
    }

    public LayoutBlock getByUserName(String key) {
        return (LayoutBlock)_tuser.get(key);
    }
    
    static LayoutBlockManager _instance = null;
    static public LayoutBlockManager instance() {
        if (_instance == null) {
            _instance = new LayoutBlockManager();
        }
        return (_instance);
    }
	
	/**
	 * Method to find a LayoutBlock with a specified Sensor assigned as its 
	 *    occupancy sensor.  Returns the block or null if no existing LayoutBlock
	 *    has the Sensor assigned.
	 */
	public LayoutBlock getBlockWithSensorAssigned(Sensor s) {
		java.util.Iterator iter = getSystemNameList().iterator();
        while (iter.hasNext()) {
            String sName = (String)iter.next();
            if (sName==null) { 
                log.error("System name null during scan of LayoutBlocks");
            }
            else {
				LayoutBlock block = getBySystemName(sName);
				if (block.getOccupancySensor() == s) return block;
            }
        }
		return null;
	}
	
	/**
	 * Initializes/checks the Paths of all Blocks associated with LayoutBlocks.
	 * <P>
	 * This routine should be called when loading panels, after all Layout Editor panels have been loaded.
	 */
	public void initializeLayoutBlockPaths() {
		// cycle through all LayoutBlocks, updating Paths of associated jmri.Blocks
		java.util.Iterator iter = getSystemNameList().iterator();
		while (iter.hasNext()) {
			String sName = (String)iter.next();
			if (sName==null) log.error("System name null during initialization of LayoutBlocks");
			log.debug("LayoutBlock initialization - system name = "+sName);
			LayoutBlock b = getBySystemName(sName); 
			b.updatePaths();
		}
	}
	
	private boolean warnConnectivity = true;
	/**
	 * Controls switching off incompatible block connectivity messages
	 * <P>
	 * Warnings are always on when program starts up. Once stopped by the user, these messages may not
	 *	be switched on again until program restarts.
	 */
	public boolean warn() {return warnConnectivity;}
	public void turnOffWarning() {warnConnectivity = false;}
	

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutBlockManager.class.getName());
}

/* @(#)LayoutBlockManager.java */
