// DefaultLogixManager.java

package jmri;

/**
 * Basic Implementation of a LogixManager.
 * <P>
 * Note that Logix system names must begin with IX, and be followed by a 
 * string, usually, but not always, a number. All alphabetic characters
 * in a Logix system name must be upper case. This is enforced when a Logix
 * is created.
 * <P>
 * The system names of Conditionals belonging to a Logix begin with the 
 * Logix's system name, then there is a capital C and a number.  
 *
 * @author      Dave Duchamp Copyright (C) 2007
 * @version	$Revision: 1.4 $
 */
public class DefaultLogixManager extends AbstractManager
    implements LogixManager, java.beans.PropertyChangeListener {

    public DefaultLogixManager() {
        super();
    }

    public char systemLetter() { return 'I'; }
    public char typeLetter() { return 'X'; }
    
    /**
     * Method to create a new Logix if the Logix does not exist
     *   Returns null if a Logix with the same systemName or userName
     *       already exists, or if there is trouble creating a new Logix.
     */
    public Logix createNewLogix(String systemName, String userName) {
        // Check that Logix does not already exist
        Logix x;
        if (userName!= null && !userName.equals("")) {
            x = getByUserName(userName);
            if (x!=null) return null;
        }
		String sName = systemName.toUpperCase();
        x = getBySystemName(systemName);
		if (x==null) getBySystemName(sName);
        if (x!=null) return null;
        // Logix does not exist, create a new Logix
        x = new DefaultLogix(sName,userName);
        if (x!=null) {
            // save in the maps
            register(x);
        }
        return x;
    }

    /**
     * Remove an existing Logix and delete all its conditionals. 
	 * Logix must have been deactivated
     * before invoking this.
     */
    public void deleteLogix(Logix x) {
		// delete conditionals if there are any
		int numConditionals = x.getNumConditionals();
		if (numConditionals>0) {
			Conditional c = null;
			for (int i = 0;i<numConditionals;i++) {
				c = InstanceManager.conditionalManagerInstance().getBySystemName(
						x.getConditionalByNumberOrder(i));
				InstanceManager.conditionalManagerInstance().deleteConditional(c);
			}
		}
		// delete the Logix				
        deregister(x);
		x.dispose();
    }
	
	/**
	 * Activate all Logixs that are not currently active
	 * This method is called after a configuration file is loaded.
	 */
	public void activateAllLogixs() {
		// iterate thru all Logixs that exist
		java.util.Iterator iter =
                                    getSystemNameList().iterator();
		while (iter.hasNext()) {
			// get the next Logix
			String sysName = (String)iter.next();
			if (sysName==null) {
				log.error("System name null when activating Logixs");
				break;
			}
			Logix x = getBySystemName(sysName);
			if (x==null) {
				log.error("Error getting Logix *"+sysName+"* when activating Logixs");
				break;
			}
			if (loadDisabled) {
				// user has requested that Logixs be loaded disabled
				x.setEnabled(false);
			}
			x.activateLogix();
		}
		// reset the load switch
		loadDisabled = false;
	}

    /** 
     * Method to get an existing Logix.  First looks up assuming that
     *      name is a User Name.  If this fails looks up assuming
     *      that name is a System Name.  If both fail, returns null.
     */
    public Logix getLogix(String name) {
        Logix x = getByUserName(name);
        if (x!=null) return x;
        return getBySystemName(name);
    }

    public Logix getBySystemName(String name) {
		String key = name.toUpperCase();
        return (Logix)_tsys.get(key);
    }

    public Logix getByUserName(String key) {
        return (Logix)_tuser.get(key);
    }
	
	/** 
	 * Support for loading Logixs in a disabled state to debug loops
	 */
	boolean loadDisabled = false; 
	public void setLoadDisabled(boolean s) {loadDisabled = s;}
    
    static DefaultLogixManager _instance = null;
    static public DefaultLogixManager instance() {
        if (_instance == null) {
            _instance = new DefaultLogixManager();
        }
        return (_instance);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultLogixManager.class.getName());
}

/* @(#)DefaultLogixManager.java */
