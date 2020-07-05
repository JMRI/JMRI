package jmri.implementation;

import jmri.LocoAddress;
import jmri.RailCom;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Extend AbstractReporter for RailCom reporters
 * <p>
 * This file is based on @{link jmri.jmrix.rfid.RfidReporter}
 *
 * @author Matthew Harris Copyright (c) 2011
 * @author Paul Bender Copyright (c) 2016,2019
 * @since 4.5.4
 */
@API(status = EXPERIMENTAL)
public class AbstractRailComReporter extends AbstractIdTagReporter {

    public AbstractRailComReporter(String systemName) {
        super(systemName);
    }

    public AbstractRailComReporter(String systemName, String userName) {
        super(systemName, userName);
    }

    // Methods to support PhysicalLocationReporter interface
    /**
     * getLocoAddress()
     *
     * get the locomotive address we're reporting about from the current report.
     *
     * Note: We ignore the string passed in, because RailCom Reporters don't send
     * String type reports.
     */
    @Override
    public LocoAddress getLocoAddress(String rep) {
        // For now, we assume the current report.
        // IdTags passed by RailCom reporters are actually jmri.RailCom objects
        // so we use properties of the RailCom object here.
        RailCom cr = (RailCom) this.getCurrentReport();
        return cr.getLocoAddress();
    }

}
