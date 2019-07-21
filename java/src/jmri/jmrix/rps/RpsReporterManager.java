package jmri.jmrix.rps;

import jmri.JmriException;
import jmri.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPS implementation of a ReporterManager.
 *
 * @author	Bob Jacobsen Copyright (C) 2008, 2019
 * @since 2.3.1
 */
public class RpsReporterManager extends jmri.managers.AbstractReporterManager {

    public RpsReporterManager(RpsSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpsSystemConnectionMemo getMemo() {
        return (RpsSystemConnectionMemo) memo;
    }

    /**
     * Create a new reporter if all checks are passed.
     * System name is normalized to ensure uniqueness.
     */
    @Override
    protected Reporter createNewReporter(String systemName, String userName) {
        log.debug(userName);
        RpsReporter r = new RpsReporter(systemName, userName, getSystemPrefix());
        Distributor.instance().addMeasurementListener(r);
        return r;
    }

    public String createSystemName(String curAddress, String prefix) throws JmriException {
        if (!prefix.equals(getSystemPrefix())) {
            log.warn("prefix does not match memo.prefix");
            return null;
        }
        String sys = getSystemPrefix() + typeLetter() + curAddress;
        // first, check validity
        try {
            validSystemNameFormat(sys);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        return sys;
    }

    /**
     * Public method to validate system name format returns 'true' if system
     * name has a valid format, else returns 'false'.
     *
     * @param systemName the address to check
     * @throws IllegalArgumentException when delimiter is not found
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (RpsAddress.validSystemNameFormat(systemName, 'R', getSystemPrefix()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddReporterEntryToolTip");
    }

    /**
     * Static function returning the RpsReporterManager instance to use.
     *
     * @return The registered RpsReporterManager instance for general use.
     * @deprecated since 4.15.6
     */
    @Deprecated
    static public RpsReporterManager instance() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(RpsReporterManager.class);

}
