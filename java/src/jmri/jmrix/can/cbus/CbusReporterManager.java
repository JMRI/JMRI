package jmri.jmrix.can.cbus;

import jmri.Reporter;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.AbstractReporterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement ReporterManager for CAN CBUS systems.
 * <p>
 * System names are "MRnnnnn", where M is the user-configurable system getSystemPrefix(),
 * nnnnn is the reporter number without padding.
 * <p>
 * CBUS Reporters are NOT automatically created.
 *
 * @author Mark Riddoch Copyright (C) 2015
 * @author Steve Young Copyright (C) 2019
 */
public class CbusReporterManager extends AbstractReporterManager {

    public CbusReporterManager(CanSystemConnectionMemo memo) {
        super(memo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CanSystemConnectionMemo getMemo() {
        return (CanSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reporter createNewReporter(String systemName, String userName) {
        log.debug("ReporterManager create new CbusReporter: {}", systemName);
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        Reporter t = new CbusReporter(addr, getMemo().getTrafficController(), getSystemPrefix());
        t.setUserName(userName);
        t.addPropertyChangeListener(this);
        return t;
    }

    /**
     * {@inheritDoc}
     * Checks for reporter number between 0 and 65535
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        // name must be in the MSnnnnn format (M is user configurable); no + or ; or - for Reporter address
        log.debug("Checking system name: {}", systemName);
        if ( systemName == null ) {
            log.debug("Null system name");
            return NameValidity.INVALID;
        }
        try {
            // try to parse the string; success returns true
            int testnum = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1, systemName.length()));
            if ( testnum < 0 ) {
                log.debug("Number field cannot be negative in system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ( testnum > 65535  ) {
                log.debug("Number field cannot be greater than 65535 in system name: {}", systemName);
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
        log.debug("Valid system name: {}", systemName);
        return NameValidity.VALID;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddReporterEntryToolTip");
    }

    private static final Logger log = LoggerFactory.getLogger(CbusReporterManager.class);

}
