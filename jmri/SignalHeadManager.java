// SignalHeadManager.java

package jmri;

import com.sun.java.util.collections.List;

/**
 * Interface for obtaining signal heads.
 * <P>
 * This doesn't have a "new" method, as SignalHeads
 * are separately implemented, instead of being system-specific.
 *
 * @author      Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.7 $
 */
public interface SignalHeadManager extends Manager {

    // to free resources when no longer used
    public void dispose();

    /**
     * Locate via user name, then system name if needed.
     * Does not create a new one if nothing found
     *
     * @param name
     * @return null if no match found
     */
    public SignalHead getSignalHead(String name);

    public SignalHead getByUserName(String s);
    public SignalHead getBySystemName(String s);
    /**
     * Get a list of all SignalHead system names.
     */
    public List getSystemNameList();

}


/* @(#)SignalHeadManager.java */
