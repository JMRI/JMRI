package jmri.jmrix.rps;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RPS implementation of a ReporterManager.
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2019
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
    @Nonnull
    public RpsSystemConnectionMemo getMemo() {
        return (RpsSystemConnectionMemo) memo;
    }

    /**
     * Create a new reporter if all checks are passed.
     * System name is normalized to ensure uniqueness.
     */
    @Override
    protected Reporter createNewReporter(@Nonnull String systemName, String userName) {
        log.debug("creating {}", userName);
        RpsReporter r = new RpsReporter(systemName, userName, getSystemPrefix());
        Distributor.instance().addMeasurementListener(r);
        return r;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        if (!prefix.equals(getSystemPrefix())) {
            log.warn("prefix does not match memo.prefix");
            throw new JmriException("Unable to convert " + curAddress + ", Prefix does not match");
        }
        String sys = getSystemPrefix() + typeLetter() + curAddress;
        // first, check validity
        try {
            validSystemNameFormat(sys);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.getMessage());
        }
        return sys;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        return getMemo().validateSystemNameFormat(name, this, locale);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return getMemo().validSystemNameFormat(systemName, typeLetter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddReporterEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(RpsReporterManager.class);

}
