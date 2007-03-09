// ConditionalManager.java

package jmri;

import com.sun.java.util.collections.List;

/**
 * Interface for obtaining Conditionals
 * <P>
 * This doesn't have a "new" method, since Conditionals are 
 * separately implemented, instead of being system-specific.
 *
 * @author      Dave Duchamp Copyright (C) 2007
 * @version	$Revision: 1.1 $
 */
public interface ConditionalManager extends Manager {

    // to free resources when no longer used
    public void dispose();
    
    /**
     * Method to create a new Conditional if the Conditional does not exist
     *   Returns null if a Conditional with the same systemName or userName
     *       already exists, or if there is trouble creating a new Conditional.
     */
    public Conditional createNewConditional(String systemName, String userName);

    /**
     * Method to get an existing Conditional.  First looks up assuming that
     *      name is a User Name.  If this fails looks up assuming
     *      that name is a System Name.  If both fail, returns null.
     *
     * @param name
     * @return null if no match found
     */
    public Conditional getConditional(String name);

    public Conditional getByUserName(String s);
    public Conditional getBySystemName(String s);
    
	/**
	 * Get a list of all Conditional system names with the specified Logix parent
	 */
	public List getSystemNameListForLogix(Logix x);
	
    /** 
     * Delete Conditional by removing it from 
     *     the manager.
     * The parent Logix must first be deactivated so it
     *     stops processing.
     */
    void deleteConditional(Conditional c); 
}


/* @(#)ConditionalManager.java */
