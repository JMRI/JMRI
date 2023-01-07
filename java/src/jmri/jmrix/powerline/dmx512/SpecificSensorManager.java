package jmri.jmrix.powerline.dmx512;

import java.util.List;
import jmri.Sensor;
import jmri.jmrix.powerline.SerialReply;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.jmrix.powerline.X10Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the system-specific Sensor implementation.
 * <p>
 * System names are "PSaaa", where aaa is the unit number
 * <p>
 * Sensors are numbered from 1.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2006, 2007, 2008
 * @author Ken Cameron, (C) 2009, sensors from poll replies Converted to
 * multiple connection
 * @author Ken Cameron Copyright (C) 2023
 */
public class SpecificSensorManager extends jmri.jmrix.powerline.SerialSensorManager {

    public SpecificSensorManager(SerialTrafficController tc) {
        super(tc);
        this.tc = tc;
    }

    SerialTrafficController tc = null;

    /**
     * Process a reply to a poll of Sensors of one node
     */
    @Override
    public synchronized void reply(SerialReply r) {
        // process for updates
        processForPollReq(r);
    }

    /**
     * These values need to persist between calls as the address is a different
     * reply from the command packet and timing might have them in separate
     * reads
     */

    private void processForPollReq(SerialReply l) {
    }

    private final static Logger log = LoggerFactory.getLogger(SpecificSensorManager.class);

}
