// CrrInit.java

package apps.cornwall;

import jmri.*;
import jmri.jmrix.loconet.LnTurnoutManager;

import com.sun.java.util.collections.List;

/**
 * Start Cornwall RR initialization and automation.
 * <P>
 * Based on Crr0024.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.3 $
 */
public class CrrInit extends jmri.jmrit.automat.AbstractAutomaton {

    /**
     * Obtain the input and output objects
     * <P>
     * This also sets the outpts to an initial state
     * to make sure everything is consistent at the start.
     */
    protected void init() {
        log.debug("CrrInit.init");
   }

    /**
     * Watch sensors, and when it changes adjust outputs to match.
     * @return Always returns true to continue operation
     */
    protected boolean handle() {
        log.debug("CrrInit.handle");

        // sequence initialization of all the DCC turnouts
        TurnoutManager tm = LnTurnoutManager.instance();
        List tos = tm.getSystemNameList();
        for (int i = 0; i<tos.size(); i++) {
            String name = ((String)tos.get(i));
            tm.getBySystemName(name).setCommandedState(Turnout.CLOSED);
            wait(250);
        }

        // start an Automaton for each section that exists
        for (int i = 0; i<30; i++) {
            startSection("apps.cornwall.CrrSection"+i+"A");
            startSection("apps.cornwall.CrrSection"+i+"B");
            startSection("apps.cornwall.CrrSection"+i+"C");
        }

        return false;   // terminate after 1st pass
    }

    void startSection(String name) {
        try {
            CrrSection s = (CrrSection)Class.forName(name).newInstance();
            // was able to create the object. start operation
            if (log.isDebugEnabled()) log.debug("Starting "+name);
            s.start();
        } catch (ClassNotFoundException ce) {
            // if class doesn't exist, no problem
        } catch (IllegalAccessException xe) {
            // this is an unexpected error
            log.warn("unexpected for class "+name+": "+xe);
        } catch (InstantiationException ie) {
            // this is an unexpected error
            log.warn("unexpected for class "+name+": "+ie);
        }
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CrrInit.class.getName());

}

/* @(#)CrrInit.java */
