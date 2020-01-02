package jmri.jmrix.nce;

import java.util.Locale;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for NCE systems.
 * <p>
 * System names are "NTnnn", where N is the (multichar) system connection prefix,
 * T is the Turnout type identifier, nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class NceTurnoutManager extends jmri.managers.AbstractTurnoutManager implements NceListener {

    public NceTurnoutManager(NceSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NceSystemConnectionMemo getMemo() {
        return (NceSystemConnectionMemo) memo;
    }

    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1));
        Turnout t = new NceTurnout(getMemo().getNceTrafficController(), getSystemPrefix(), addr);
        t.setUserName(userName);

        return t;
    }

    /**
     * Get the bit address from the system name.
     *
     * @param systemName system name for turnout
     * @return index value for turnout
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(getSystemPrefix())) || (!systemName.startsWith(getSystemPrefix() + "T"))) {
            // here if an illegal nce light system name
            log.error("illegal character in header field of nce turnout system name: " + systemName);
            return (0);
        }
        // name must be in the NLnnnnn format (N is user configurable)
        int num = 0;
        try {
            num = Integer.parseInt(systemName.substring(
                        getSystemPrefix().length() + 1, systemName.length())
                    );
        } catch (NumberFormatException e) {
            log.debug("illegal character in number field of system name: " + systemName);
            return (0);
        }
        if (num <= 0) {
            log.error("invalid nce turnout system name: " + systemName);
            return (0);
        } else if (num > 4096) {
            log.warn("bit number out of range in nce turnout system name: " + systemName);
            return (0);
        }
        return (num);
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    @Override
    public void reply(NceReply r) {

    }

    @Override
    public void message(NceMessage m) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        return super.validateNmraAccessorySystemNameFormat(name, locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(NceTurnoutManager.class);

}
