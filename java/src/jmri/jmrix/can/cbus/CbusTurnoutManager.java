package jmri.jmrix.can.cbus;

import jmri.JmriException;
import jmri.Turnout;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.AbstractTurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CAN CBUS implementation of a TurnoutManager.
 * <p>
 * Turnouts must be manually created.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @since 2.3.1
 */
public class CbusTurnoutManager extends AbstractTurnoutManager {

    public CbusTurnoutManager(CanSystemConnectionMemo memo) {
        this.memo = memo;
        prefix = memo.getSystemPrefix();
    }

    CanSystemConnectionMemo memo;

    String prefix = "M";

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    @Override
    protected Turnout createNewTurnout(String systemName, String userName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        // first, check validity
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            throw e;
        }
        try {
            if (Integer.valueOf(addr) > 0 && !addr.startsWith("+")) {
                // accept unsigned positive integer, prefix "+"
                addr = "+" + addr;
            }
        } catch (NumberFormatException ex) {
            log.debug("Unable to convert {} into Cbus format +nn", addr);
        }
        Turnout t = new CbusTurnout(getSystemPrefix(), addr, memo.getTrafficController());
        t.setUserName(userName);
        return t;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        // first, check validity
        try {
            validateSystemNameFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        // prefix + as service to user
        int unsigned = 0;
        try {
            unsigned = Integer.valueOf(curAddress); // on unsigned integer, will add "+" next
        } catch (NumberFormatException ex) {
            // already warned
        }
        if (unsigned > 0 && !curAddress.startsWith("+")) {
            curAddress = "+" + curAddress;
        }
        return getSystemPrefix() + typeLetter() + curAddress;
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) throws JmriException {
        // always return this (the current) name without change
        try {
            validateSystemNameFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        return curAddress;
    }

    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1); // get only the address part
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e){
            log.debug("Warning: {}", e.getMessage());
            return NameValidity.INVALID;
        }
        return NameValidity.VALID;
    }

    /**
     * Work out the details for Cbus hardware address validation
     * Logging of handled cases no higher than WARN.
     *
     * @param address the hardware address to check
     * @throws IllegalArgumentException when delimiter is not found
     */
    void validateSystemNameFormat(String address) throws IllegalArgumentException {
        CbusAddress a = new CbusAddress(address);
        CbusAddress[] v = a.split();
        if (v == null) {
            throw new IllegalArgumentException("Did not find usable hardware address: " + address + " for a valid Cbus turnout address");
        }
        switch (v.length) {
            case 1:
                int unsigned = 0;
                try {
                    unsigned = Integer.valueOf(address); // accept unsigned integer, will add "+" upon creation
                } catch (NumberFormatException ex) {
                    log.debug("Unable to convert {} into Cbus format +nn", address);
                }
                if (address.startsWith("+") || address.startsWith("-") || unsigned > 0) {
                    break;
                }
                throw new IllegalArgumentException("can't make 2nd event from address " + address);
            case 2:
                break;
            default:
                throw new IllegalArgumentException("Wrong number of events in address: " + address);
        }
    }

    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddOutputEntryToolTip");
        return entryToolTip;
    }

    private final static Logger log = LoggerFactory.getLogger(CbusTurnoutManager.class);

}
