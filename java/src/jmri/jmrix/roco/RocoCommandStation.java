package jmri.jmrix.roco;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the standard/common routines used in multiple classes related to 
 * a Roco Command Station, on an XPressNet network.
 *
 * @author	Bob Jacobsen Copyright (C) 2001 
 * @author      Paul Bender Copyright (C) 2016
 */
public class RocoCommandStation extends jmri.jmrix.lenz.LenzCommandStation implements jmri.CommandStation {

    /*
     * We need to register for logging
     */
    private final static Logger log = LoggerFactory.getLogger(RocoCommandStation.class.getName());

}
