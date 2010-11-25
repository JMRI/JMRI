package jmri.util;

import jmri.jmrix.DCCManufacturerList;

/**
 * Common utility method for returning the System Connection Name from
 * the System Name Prefix
 *
 * @author Kevin Dickerson  Copyright 2010
 * @version $Revision: 1.1 $
 */
public class ConnectionNameFromSystemName{
    
    static public String getConnectionName(String prefix){
        java.util.List<Object> list 
            = jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
        if (list != null) {
            for (Object memo : list) {
                if (((jmri.jmrix.SystemConnectionMemo)memo).getSystemPrefix().equals(prefix))
                    return ((jmri.jmrix.SystemConnectionMemo)memo).getUserName();
            }
        }
        //Fall through if the system isn't using the new SystemConnectionMemo registration
        return DCCManufacturerList.getDCCSystemFromType(prefix.charAt(0));
    
    }

}