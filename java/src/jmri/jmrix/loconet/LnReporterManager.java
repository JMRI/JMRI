package jmri.jmrix.loconet;

import jmri.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the LocoNet-specific Reporter implementation. System names are
 * "LRnnn", where nnn is the Reporter number without padding.
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class LnReporterManager extends jmri.managers.AbstractReporterManager implements LocoNetListener {

    // ctor has to register for LocoNet events
    public LnReporterManager(LnTrafficController tc, String prefix) {
        this.prefix = prefix;
        this.tc = tc;
        if (tc != null) {
            tc.addLocoNetListener(~0, this);
        } else {
            log.error("No layout connection, Reporter manager can't function");
        }
    }

    LnTrafficController tc;
    String prefix;

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    public void dispose() {
        if (tc != null) {
            tc.removeLocoNetListener(~0, this);
        }
        super.dispose();
    }

    @Override
    public Reporter createNewReporter(String systemName, String userName) {
        Reporter t;
        int addr = Integer.parseInt(systemName.substring(prefix.length() + 1));
        t = new LnReporter(addr, tc, prefix);
        t.setUserName(userName);
        t.addPropertyChangeListener(this);

        return t;
    }

    /**
     * Get the bit address from the system name.
     *
     * @param systemName the system name
     * @return the bit address
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(getSystemPrefix())) || (!systemName.startsWith(getSystemPrefix() + "R"))) {
            // here if an illegal loconet reporter system name
            log.error("invalid character in header field of loconet reporter system name: {}", systemName);
            return (0);
        }
        // name must be in the LRnnnnn format (L is user configurable)
        int num;
        try {
            num = Integer.parseInt(systemName.substring(
                    getSystemPrefix().length() + 1, systemName.length()));
        } catch (NumberFormatException e) {
            log.warn("invalid character in number field of system name: {}", systemName);
            return (0);
        }
        if (num <= 0) {
            log.warn("invalid loconet reporter system name: {}", systemName);
            return (0);
        } else if (num > 4096) {
            log.warn("bit number out of range in loconet reporter system name: {}", systemName);
            return (0);
        }
        return (num);
    }

    /**
     * Public method to validate system name format.
     *
     * @param systemName the name to validate
     * @return VALID if system name has a valid format; otherwise return INVALID
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    // listen for transponder messages, creating Reporters as needed
    @Override
    public void message(LocoNetMessage l) {
        // check message type
        if (l.getOpCode() != 0xD0) {
            return;
        }
        if ((l.getElement(1) & 0xC0) != 0) {
            return;
        }

        // message type OK, check address
        int addr = (l.getElement(1) & 0x1F) * 128 + l.getElement(2) + 1;

        LnReporter r = (LnReporter) provideReporter("LR" + addr); // NOI18N
        r.message(l); // make sure it got the message
    }

    private final static Logger log = LoggerFactory.getLogger(LnReporterManager.class);

}
