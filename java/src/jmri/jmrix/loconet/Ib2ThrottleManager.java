package jmri.jmrix.loconet;

import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.DccThrottle;
import jmri.ThrottleManager;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.ThrottleListener;
import jmri.jmrix.AbstractThrottleManager;
import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Specialization of a ThrottleManager for the Intellibox.
 *
 * @author		Bob Jacobsen  Copyright (C) 2014
 * @version 		$Revision$
 */
public class Ib2ThrottleManager extends jmri.jmrix.loconet.LnThrottleManager {

    /**
     * Constructor. 
     */
    public Ib2ThrottleManager(LocoNetSystemConnectionMemo memo) {
    	super(memo);
    	log.debug("Ib2ThrottleManager created");
    }

    DccThrottle createThrottle(LocoNetSystemConnectionMemo memo, LocoNetSlot s) {
        return new Ib2Throttle(memo, s);
    }

    static Logger log = LoggerFactory.getLogger(Ib2ThrottleManager.class.getName());
}
