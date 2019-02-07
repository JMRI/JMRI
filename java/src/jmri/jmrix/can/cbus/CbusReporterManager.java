package jmri.jmrix.can.cbus;

import jmri.Reporter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficController;
import jmri.managers.AbstractReporterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the CBUS-specific Reporter implementation.
 * <p>
 * System names are "MRnnn", where M is the user-configurable system prefix,
 * nnn is the reporter number without padding.
 * Reporters are NOT automatically created.
 *
 * @author Mark Riddoch Copyright (C) 2015
 * @author Steve Young Copyright (C) 2019
 */
public class CbusReporterManager extends AbstractReporterManager {

    @SuppressWarnings("LeakingThisInConstructor")
    public CbusReporterManager(CanSystemConnectionMemo memo) {
        this.tc = memo.getTrafficController();
        this.prefix = memo.getSystemPrefix();
    }

    TrafficController tc;
    String prefix;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reporter createNewReporter(String systemName, String userName) {
        Reporter t;
        int addr = Integer.parseInt(systemName.substring(prefix.length() + 1));
        t = new CbusReporter(addr, tc, prefix);
        t.setUserName(userName);
        return t;
    }

    /**
     * {@inheritDoc}
     * name must be in the MRnnnnn format (M is user configurable);  no split or negative for Reporter address
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        
        if ( systemName == null ) {
            return NameValidity.INVALID;
        }
        try {
            // try to parse the string; success returns true
            int testnum = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1, systemName.length()));
            if ( testnum < 1 ) {
                return NameValidity.INVALID;
            }
            if ( testnum > 65535  ) {
                return NameValidity.INVALID;
            }
        }
        catch (NumberFormatException e) {
            log.debug("Illegal character in number field of system name: {}", systemName);
            return NameValidity.INVALID;
        }
        catch (StringIndexOutOfBoundsException e) {
            log.debug("Wrong length ( missing MR? ) for system name: {}", systemName);
            return NameValidity.INVALID;
        }
        
        return NameValidity.VALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddReporterEntryToolTip");
        return entryToolTip;
    }

    /** 
     * {@inheritDoc}
     * Able to use the abstract for the new value as CBUS device numbers are pure numeric format, no on or off.
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    private static final Logger log = LoggerFactory.getLogger(CbusReporterManager.class);

}
