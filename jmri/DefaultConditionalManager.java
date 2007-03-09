// DefaultConditionalManager.java

package jmri;

import com.sun.java.util.collections.List;

/**
 * Basic Implementation of a ConditionalManager.
 * <P>
 * Note that Conditionals always have an associated parent Logix.
 * <P>
 * Logix system names must begin with IX, and be followed by a 
 * string, usually, but not always, a number. The system names of Conditionals 
 * always begin with the parent Logix's system name, then there is a capital C 
 * and a number.
 *<P>  
 * Conditional system names are set automatically when the Conditional is created.
 * All alphabetic characters in a Conditional system name must be upper case. This 
 * is enforced when a new Conditional is created via LogixTableAction.java.
 *
 * @author      Dave Duchamp Copyright (C) 2007
 * @version	$Revision: 1.1 $
 */
public class DefaultConditionalManager extends AbstractManager
    implements ConditionalManager, java.beans.PropertyChangeListener {

    public DefaultConditionalManager() {
        super();
    }

    public char systemLetter() { return 'I'; }
    public char typeLetter() { return 'X'; }
    
    /**
     * Method to create a new Conditional if the Conditional does not exist
     *   Returns null if a Conditional with the same systemName or userName
     *       already exists, or if there is trouble creating a new Conditional.
     */
    public Conditional createNewConditional(String systemName, String userName) {
	        // Check that Conditional does not already exist
        Conditional c;
        if (userName!= null && !userName.equals("")) {
            c = getByUserName(userName);
            if (c!=null) return null;
        }
		String sName = systemName.toUpperCase();
        c = getBySystemName(systemName);
		if (c==null) getBySystemName(sName);
        if (c!=null) return null;
        // Conditional does not exist, create a new Conditional
        c = new DefaultConditional(sName,userName);
        if (c!=null) {
            // save in the maps
            register(c);
        }
        return c;
    }

    /**
     * Remove an existing Conditional. Parent Logix must have been deactivated
     * before invoking this.
     */
    public void deleteConditional(Conditional c) {
        deregister(c);
    }

    /** 
     * Method to get an existing Conditional.  First looks up assuming that
     *      name is a User Name.  If this fails looks up assuming
     *      that name is a System Name.  If both fail, returns null.
     * @param name
     * @return null if no match found
     */
    public Conditional getConditional(String name) {
        Conditional c = getByUserName(name);
        if (c!=null) return c;
        return getBySystemName(name);
    }

    public Conditional getByUserName(String key) {
        return (Conditional)_tuser.get(key);
    }

    public Conditional getBySystemName(String name) {
		String key = name.toUpperCase();
        return (Conditional)_tsys.get(key);
    }

	/**
	 * Get a list of all Conditional system names with the specified Logix parent
	 */
	public List getSystemNameListForLogix(Logix x) {
		log.error("getSystemNameListForLogix - Not implemented yet.");
		return null;
	}
    
    static DefaultConditionalManager _instance = null;
    static public DefaultConditionalManager instance() {
        if (_instance == null) {
            _instance = new DefaultConditionalManager();
        }
        return (_instance);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultConditionalManager.class.getName());
}



/* @(#)DefaultConditionalManager.java */
