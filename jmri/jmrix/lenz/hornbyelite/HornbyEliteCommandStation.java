/*
 * HornbyEliteCommandStation.java
 */

package jmri.jmrix.lenz.hornbyelite;


/**
 * Defines the routines that differentiate a Hornby Elite Command Station
 * from a Lenz command station.
 *
 * @author			Paul Bender Copyright (C) 2008
 * @version			$Revision: 1.1 $
 */
public class HornbyEliteCommandStation extends jmri.jmrix.lenz.LenzCommandStation implements jmri.jmrix.DccCommandStation,jmri.CommandStation {

    /**
     * The Hornby Elite does NOT use a service mode
     */
    public boolean getHasServiceMode() {return false;}

    /**
     * The Hornby Elite does NOT support Ops Mode programming
     */
    public boolean isOpsModePossible() {return false;}

    /*
     * We need to register for logging
     */
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(HornbyEliteCommandStation.class.getName());
    
}


/* @(#)HornbyEliteCommandStation.java */
