package jmri.jmrix.tmcc;

import java.util.Locale;
import jmri.JmriException;
import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for TMCC serial systems.
 * <p>
 * System names are "TTnnn", where T is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2006
 */
public class SerialTurnoutManager extends AbstractTurnoutManager implements SerialListener {

    public SerialTurnoutManager(TmccSystemConnectionMemo memo) {
        super(memo);
        memo.getTrafficController().addSerialListener(this);
        log.debug("TMCC TurnoutManager prefix={}", getSystemPrefix());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TmccSystemConnectionMemo getMemo() {
        return (TmccSystemConnectionMemo) memo;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        // validate the system name
        String sName = validateSystemNameFormat(systemName);
        // does this turnout already exist?
        Turnout t = getBySystemName(sName);
        if (t != null) {
            log.debug("Turnout already exists");
            return null;
        }
        // create the turnout
        log.debug("new SerialTurnout with addr = {}", systemName.substring(getSystemPrefix().length() + 1));
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        t = new SerialTurnout(getSystemPrefix(), addr, getMemo());
        t.setUserName(userName);
        return t;
    }

    /**
     * Listeners for messages from the command station.
     */
    @Override
    public void message(SerialMessage m) {
        log.debug("message received unexpectedly: {}", m.toString());
    }

    // Listen for status changes from TMCC system
    @Override
    public void reply(SerialReply r) {
        // There isn't anything meaningful coming back at this time.
        log.debug("reply received unexpectedly: {}", r.toString());
    }

    // Turnout address format is more than a simple number.
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        try {
            return makeSystemName(curAddress);
        } catch (IllegalArgumentException ex) {
            throw new JmriException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        return validateIntegerSystemNameFormat(name, 1, 99, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        NameValidity validity = super.validSystemNameFormat(systemName);
        if (validity == NameValidity.VALID) {
            int num;
            try {
                num = Integer.parseInt(systemName.substring(getSystemNamePrefix().length()));
                if (num < 0 || num > 99) {
                    validity = NameValidity.INVALID;
                }
            } catch (NumberFormatException ex) {
                validity = NameValidity.INVALID;
            }
        }
        return validity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManager.class);

}
