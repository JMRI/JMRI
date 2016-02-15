// RaspberryPiTurnoutManager.java

package jmri.jmrix.pi;

import jmri.Turnout;

/**
 * Implement turnout manager.
 * <P>
 * System names are "PTnnn", where nnn is the turnout number without padding.
 *
 * @author			Paul Bender Copyright (C) 2015 
 * @version			$Revision$
 */
public class RaspberryPiTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    private String prefix = null;

    // ctor has to register for RaspberryPi events
    public RaspberryPiTurnoutManager(String prefix) {
        super();
        this.prefix=prefix;
    }

    /**
     * Provides access to the system prefix string.
     * This was previously called the "System letter"
     */
    @Override
    public String getSystemPrefix(){ return prefix; }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t = new RaspberryPiTurnout(systemName,userName);
        return t;
    }

}

/* @(#)RaspberryPiTurnoutManager.java */
