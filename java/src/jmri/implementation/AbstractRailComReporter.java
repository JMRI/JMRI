package jmri.implementation;

import jmri.LocoAddress;
import jmri.RailCom;

/**
 * Extend AbstractReporter for RailCom reporters
 * <p>
 * This class is based on {@link jmri.jmrix.rfid.RfidReporter}
 *
 * @author Matthew Harris Copyright (c) 2011
 * @author Paul Bender Copyright (c) 2016,2019
 * @since 4.5.4
 */
public abstract class AbstractRailComReporter extends AbstractIdTagReporter {

    public AbstractRailComReporter(String systemName) {
        super(systemName);
    }

    public AbstractRailComReporter(String systemName, String userName) {
        super(systemName, userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean hasExitReports();

    // Methods to support PhysicalLocationReporter interface
    
    /**
     * Get the locomotive address we're reporting about from the current report.
     *
     * Note: We ignore the string passed in, because RailCom Reporters don't send
     * String type reports.
     * {@inheritDoc}
     */
    @Override
    public LocoAddress getLocoAddress(String rep) {
        Object cr = this.getCurrentReport();
        if (cr instanceof RailCom) {
            return ((RailCom) cr).getLocoAddress();
        }
        // not a RailCom report, for example an RFID tag on a CBUS Reporter, or no report
        return super.getLocoAddress(rep);
    }

}
