// SerialTurnoutManager.java

package jmri.jmrix.cmri.serial;

import jmri.JmriException;
import jmri.Turnout;

/**
 * Implement turnout manager for CMRI serial systems
 * <P>
 * System names are "CTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.1 $
 */
public class SerialTurnoutManager extends jmri.AbstractTurnoutManager {

    // ABC implementations

    // to free resources when no longer used
    public void dispose() throws JmriException {
        super.dispose();
    }

    // CMRI-serial-specific methods

    public void putBySystemName(SerialTurnout t) {
        String system = "CT"+t.getNumber();
        _tsys.put(system, t);
    }

    public Turnout newTurnout(String systemName, String userName) {
        // if system name is null, supply one from the number in userName
        if (systemName == null) systemName = "CT"+userName;

        // return existing if there is one
        Turnout t;
        if ( (userName != null) && ((t = getByUserName(userName)) != null)) return t;
        if ( (t = getBySystemName(systemName)) != null) return t;

        // get number from name
        if (!systemName.startsWith("CT")) {
            log.error("Invalid system name for C/MRI serial turnout: "+systemName);
            return null;
        }
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new SerialTurnout(addr);
        t.setUserName(userName);

        _tsys.put(systemName, t);
        if (userName!=null) _tuser.put(userName, t);
        t.addPropertyChangeListener(this);

        return t;
    }

    public SerialTurnoutManager() {
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialTurnoutManager.class.getName());

}

/* @(#)SerialTurnoutManager.java */
