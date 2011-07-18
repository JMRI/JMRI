package jmri.util;

import jmri.jmrix.DCCManufacturerList;

/**
 * Common utility method for returning the System Connection Name from
 * the System Name Prefix
 *
 * @author Kevin Dickerson  Copyright 2010
 * @version $Revision$
 */
public class ConnectionNameFromSystemName{
    
    /**
     * Locates the connected systems name from a given prefix.
     * @param prefix
     * @return The Connection System Name
     */
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
    /*
     *  Returns the System prefix of a connection given the system name.
     */
    /**
     * Locates the connected systems prefix from a given System name.
     * @param name
     * @return The system prefix
     */
    static public String getPrefixFromName(String name){
        if (name==null)
            return null;
        java.util.List<Object> list 
            = jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
        if (list != null) {
            for (Object memo : list) {
                if (((jmri.jmrix.SystemConnectionMemo)memo).getUserName().equals(name)){
                    return ((jmri.jmrix.SystemConnectionMemo)memo).getSystemPrefix();
                }
            }
        }
        String prefix = Character.toString(DCCManufacturerList.getTypeFromDCCSystem(name));
        //Fall through if the system isn't using the new SystemConnectionMemo registration
        return prefix;
    
    }

}