// SprogTurnoutManager.java

package jmri.jmrix.sprog;

import jmri.JmriException;
import jmri.Turnout;

/**
 * Implement turnout manager for Sprog systems
 * <P>
 * System names are "STnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision: 1.4 $
 */
public class SprogTurnoutManager extends jmri.AbstractTurnoutManager {

    public SprogTurnoutManager() {
        prefix = "ST";
        _instance = this;
    }

    // to free resources when no longer used
    public void dispose() {
        super.dispose();
    }

    // Sprog-specific methods

    public void putBySystemName(SprogTurnout t) {
        String system = prefix+t.getNumber();
        _tsys.put(system, t);
    }

    public Turnout newTurnout(String systemName, String userName) {
        // if system name is null, supply one from the number in userName
        if (systemName == null) systemName = prefix+userName;

        // return existing if there is one
        Turnout t;
        if ( (userName != null) && ((t = getByUserName(userName)) != null)) return t;
        if ( (t = getBySystemName(systemName)) != null) return t;

        // get number from name
        if (!systemName.startsWith(prefix)) {
            log.error("Invalid system name for Sprog turnout: "+systemName);
            return null;
        }
        int addr = Integer.valueOf(systemName.substring(2)).intValue();
        t = new SprogTurnout(addr);
        t.setUserName(userName);

        _tsys.put(systemName, t);
        if (userName!=null) _tuser.put(userName, t);
        t.addPropertyChangeListener(this);

        return t;
    }

    static public SprogTurnoutManager instance() {
        if (_instance == null) _instance = new SprogTurnoutManager();
        return _instance;
    }
    static SprogTurnoutManager _instance = null;

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogTurnoutManager.class.getName());

}

/* @(#)SprogTurnoutManager.java */
