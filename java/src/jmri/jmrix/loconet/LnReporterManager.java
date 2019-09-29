package jmri.jmrix.loconet;

import java.util.Locale;
import jmri.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the LocoNet-specific Reporter implementation.
 * <p>
 * System names are "LRnnn", where L is the user configurable system prefix,
 * nnn is the Reporter number without padding.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class LnReporterManager extends jmri.managers.AbstractReporterManager implements LocoNetListener {

    protected final LnTrafficController tc;
    
    // ctor has to register for LocoNet events
    public LnReporterManager(LocoNetSystemConnectionMemo memo) {
        super(memo);
        tc = memo.getLnTrafficController();
        if (tc != null) {
            tc.addLocoNetListener(~0, this);
        } else {
            log.error("No layout connection, Reporter manager can't function");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocoNetSystemConnectionMemo getMemo() {
        return (LocoNetSystemConnectionMemo) memo;
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
        int addr = Integer.parseInt(systemName.substring(getSystemNamePrefix().length()));
        t = new LnReporter(addr, tc, getSystemPrefix());
        t.setUserName(userName);
        t.addPropertyChangeListener(this);

        return t;
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
    public String validateSystemNameFormat(String systemName, Locale locale) {
        return validateIntegerSystemNameFormat(systemName, 1, 4096, locale);
    }

    /**
     * Get the bit address from the system name.
     * @param systemName a valid LocoNet-based Reporter System Name
     * @return the turnout number extracted from the system name
     */
    public int getBitFromSystemName(String systemName) {
        try {
            validateSystemNameFormat(systemName, Locale.getDefault());
        } catch (IllegalArgumentException ex) {
            return 0;
        }
        return Integer.parseInt(systemName.substring(getSystemNamePrefix().length()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    // listen for transponder messages, creating Reporters as needed
    @Override
    public void message(LocoNetMessage l) {
        // check message type
        int addr;
        switch (l.getOpCode()) {
            case LnConstants.OPC_MULTI_SENSE:
                if ((l.getElement(1) & 0xC0) == 0) {
                    addr = (l.getElement(1) & 0x1F) * 128 + l.getElement(2) + 1;
                    break;
                }
                return;
            case LnConstants.OPC_PEER_XFER:
                if (l.getElement(1) == 0x09 && l.getElement(2) == 0x00) {
                    addr = (l.getElement(5) & 0x1F) * 128 + l.getElement(6) + 1;
                    break;
                }
                return;
            case LnConstants.OPC_LISSY_UPDATE:
                if (l.getElement(1) == 0x08) {
                    addr =  (l.getElement(4) & 0x7F);
                    break;
                }
                return;
            default:
                return;
        }
        log.debug("Reporter[{}]",addr);
        LnReporter r = (LnReporter) provideReporter(getSystemNamePrefix() + addr); // NOI18N
        r.messageFromManager(l); // make sure it got the message
    }

    private final static Logger log = LoggerFactory.getLogger(LnReporterManager.class);

}
