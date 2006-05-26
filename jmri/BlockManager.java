// RouteManager.java

package jmri;

import com.sun.java.util.collections.List;

/**
 * Basic Implementation of a BlockManager.
 * <P>
 * Note that this does not enforce any particular system naming convention.
 * <P>
 * Note this is a concrete class, unlike the interface/implementation pairs
 * of most Managers, because there are currently only one implementation for Blocks.
 *
 * @author      Bob Jacobsen Copyright (C) 2006
 * @version	$Revision: 1.1 $
 */
public class BlockManager extends AbstractManager
    implements java.beans.PropertyChangeListener {

    public BlockManager() {
        super();
    }

    public char systemLetter() { return 'I'; }
    public char typeLetter() { return 'B'; }
    
    /**
     * Method to create a new Block if it does not exist
     *   Returns null if a Block with the same systemName or userName
     *       already exists, or if there is trouble creating a new Block.
     */
    public Block createNewBlock(String systemName, String userName) {
        // Check that Block does not already exist
        Block r;
        if (userName!= null && !userName.equals("")) {
            r = getByUserName(userName);
            if (r!=null) return null;
        }
        r = getBySystemName(systemName);
        if (r!=null) return null;
        // Block does not exist, create a new Block
		String sName = systemName.toUpperCase();
        r = new Block(sName,userName);
        if (r!=null) {
            // save in the maps
            register(r);
        }
        return r;
    }

    /** 
     * Method to get an existing Block.  First looks up assuming that
     *      name is a User Name.  If this fails looks up assuming
     *      that name is a System Name.  If both fail, returns null.
     */
    public Block getBlock(String name) {
        Block r = getByUserName(name);
        if (r!=null) return r;
        return getBySystemName(name);
    }

    public Block getBySystemName(String name) {
		String key = name.toUpperCase();
        return (Block)_tsys.get(key);
    }

    public Block getByUserName(String key) {
        return (Block)_tuser.get(key);
    }
    
    static BlockManager _instance = null;
    static public BlockManager instance() {
        if (_instance == null) {
            _instance = new BlockManager();
        }
        return (_instance);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BlockManager.class.getName());
}

/* @(#)BlockManager.java */
