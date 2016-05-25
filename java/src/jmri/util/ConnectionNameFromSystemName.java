package jmri.util;

import jmri.jmrix.DCCManufacturerList;
import jmri.jmrix.SystemConnectionMemo;

/**
 * Common utility method for returning the System Connection Name from the
 * System Name Prefix
 *
 * @author Kevin Dickerson Copyright 2010
 */
public class ConnectionNameFromSystemName {

    /**
     * Locates the connected systems name from a given prefix.
     *
     * @param prefix
     * @return The Connection System Name
     */
    static public String getConnectionName(String prefix) {
        java.util.List<SystemConnectionMemo> list
                = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        if (list != null) {
            for (SystemConnectionMemo memo : list) {
                if (memo.getSystemPrefix().equals(prefix)) {
                    return memo.getUserName();
                }
            }
        }
        //Fall through if the system isn't using the new SystemConnectionMemo registration
        return DCCManufacturerList.getDCCSystemFromType(prefix.charAt(0));

    }
    /**
     * Locates the connected systems prefix from a given System name.
     *
     * @param name
     * @return The system prefix
     */
    static public String getPrefixFromName(String name) {
        if (name == null) {
            return null;
        }
        java.util.List<SystemConnectionMemo> list
                = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        if (list != null) {
            for (SystemConnectionMemo memo : list) {
                if (memo.getUserName().equals(name)) {
                    return memo.getSystemPrefix();
                }
            }
        }
        String prefix = Character.toString(DCCManufacturerList.getTypeFromDCCSystem(name));
        //Fall through if the system isn't using the new SystemConnectionMemo registration
        return prefix;

    }

}
