package jmri.jmrix.anyma_dmx;

import jmri.Turnout;

/**
 * Implement turnout manager.
 * <P>
 * System names are "PTnnn", where nnn is the turnout number without padding.
 *
 * @author   Paul Bender Copyright (C) 2015
 * @author George Warner Copyright (C) 2017
 * @since       4.9.6
 */
public class AnymaDMX_TurnoutManager extends jmri.managers.AbstractTurnoutManager {

    private String prefix = null;

    // ctor has to register for AnymaDMX_ events
    public AnymaDMX_TurnoutManager(String prefix) {
        super();
        this.prefix=prefix.toUpperCase();
    }

    /**
     * Provides access to the system prefix string.
     * This was previously called the "System letter"
     */
    @Override
    public String getSystemPrefix(){ return prefix; }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t = new AnymaDMX_Turnout(systemName,userName);
        return t;
    }

}
