package jmri.jmrix.lenz.li100;

import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.lenz.XNetPacketizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an extention of the XNetPacketizer to handle the device specific
 * requirements of the LI100.
 * <p>
 * In particular, LI100XNetPacketizer overrides the automatic exit from service
 * mode in the AbstractMRTrafficController.
 *
 * @author Paul Bender Copyright (C) 2009
 */
public class LI100XNetPacketizer extends XNetPacketizer {

    public LI100XNetPacketizer(jmri.jmrix.lenz.LenzCommandStation pCommandStation) {
        super(pCommandStation);
        log.debug("Loading LI100 Extention to XNetPacketizer");
    }

    /**
     * @return null for LI100
     */
    @Override
    protected AbstractMRMessage enterNormalMode() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(LI100XNetPacketizer.class);

}
