package jmri.jmrix.lenz.hornbyelite;


/**
 * Defines the routines that differentiate a Hornby Elite Command Station from a
 * Lenz command station.
 *
 * @author	Paul Bender Copyright (C) 2008
 */
public class HornbyEliteCommandStation extends jmri.jmrix.lenz.LenzCommandStation implements jmri.CommandStation {

    /**
     * The Hornby Elite does NOT use a service mode
     */
    public boolean getHasServiceMode() {
        return false;
    }

    /**
     * The Hornby Elite does support Ops Mode programming
     */
    public boolean isOpsModePossible() {
        return true;
    }

}
