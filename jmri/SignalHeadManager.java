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
 * @version	$Revision: 1.5 $
 */
public interface SignalHeadManager extends Manager {

    // to free resources when no longer used
    public void dispose();

    public SignalHead getByUserName(String s);
    public SignalHead getBySystemName(String s);
    /**
     * Get a list of all Turnouts' system names.
     */
    public List getSystemNameList();

}


/* @(#)SignalHeadManager.java */
