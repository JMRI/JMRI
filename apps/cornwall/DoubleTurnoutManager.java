// DoubleTurnoutManager.java

package apps.cornwall;

import java.util.Hashtable;
import java.util.Enumeration;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.Collections;
import jmri.jmrix.loconet.*;
import jmri.jmrix.cmri.serial.*;
import jmri.*;

/**
 * Implementation of a TurnoutManager that can share between a
 * cmri.serial.SerialTurnoutManager and loconet.LnTurnoutManager
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version	$Revision: 1.2 $
 */
public class DoubleTurnoutManager extends AbstractTurnoutManager {

    public DoubleTurnoutManager() {
        if (loconetManager== null)
            log.error("No LocoNet turnout manager!");
        else
            InstanceManager.configureManagerInstance().register(loconetManager);

        if (serialManager== null)
            log.error("No CMRI serial turnout manager!");
        else
            InstanceManager.configureManagerInstance().register(serialManager);
    }

    public Turnout newTurnout(String systemName, String userName) {
        // if system name is null, supply one from the userName
        if (systemName == null) {
            try {
                // try to make a number into LT31 or similar
                int addr = Integer.valueOf(userName).intValue();
                systemName = ""+systemLetter()+"T"+addr;
            } catch ( java.lang.NumberFormatException ex ) {
                // Not a number, assume it's already LT31 or similar
                systemName = userName;
            }
        }


        // return existing if there is one
        Turnout t;
        if ( (userName != null) && ((t = getByUserName(userName)) != null)) return t;
        if ( (t = getBySystemName(systemName)) != null) {
            if (userName != null) log.warn("Found turnout via system name ("+systemName
                                    +") with non-null user name ("+userName+")");
            return t;
        }

        if (loconetManager != null && (loconetManager.systemLetter() == systemName.charAt(0)) )
            t = loconetManager.newTurnout(systemName, userName);

        else if (serialManager != null && (serialManager.systemLetter() == systemName.charAt(0)) )
            t = serialManager.newTurnout(systemName, userName);

        // by default
        else t = loconetManager.newTurnout(systemName, userName);

        _tsys.put(systemName, t);
        if (userName!=null) _tuser.put(userName, t);
        t.addPropertyChangeListener(this);

        return t;
    }

    // to free resources when no longer used
    public void dispose() {
        // drop sub-managers
        loconetManager.dispose();
        loconetManager = null;

        serialManager.dispose();
        serialManager = null;

        // and the superclass
        super.dispose();
    }

    /**
     * @return The system-specific prefix letter for the master implementation,
     * which in this case is the Loconet version
     */
    public char systemLetter() { return loconetManager.systemLetter(); }

    LnTurnoutManager loconetManager = LnTurnoutManager.instance();
    SerialTurnoutManager serialManager = SerialTurnoutManager.instance();

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DoubleTurnoutManager.class.getName());
}

/* @(#)DoubleTurnoutManager.java */
