package jmri.jmrix.pi;

import jmri.Turnout;

/**
 * Implement Pi turnout manager.
 * <p>
 * System names are "PTnnn", where P is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author   Paul Bender Copyright (C) 2015
 */
public class RaspberryPiTurnoutManager extends jmri.managers.AbstractTurnoutManager {

    // ctor has to register for RaspberryPi events
    public RaspberryPiTurnoutManager(RaspberryPiSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RaspberryPiSystemConnectionMemo getMemo() {
        return (RaspberryPiSystemConnectionMemo) memo;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t = new RaspberryPiTurnout(systemName, userName);
        return t;
    }

}
