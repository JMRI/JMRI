package jmri.jmrix.can.cbus;

import jmri.JmriException;
import jmri.Light;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CAN CBUS implementation of a LightManager.
 * <p>
 * Lights must be manually created.
 *
 * @author Matthew Harris Copyright (C) 2015
 * @since 3.11.7
 */
public class CbusLightManager extends AbstractLightManager {

    public CbusLightManager(CanSystemConnectionMemo memo) {
        this.memo = memo;
        prefix = memo.getSystemPrefix();
    }

    CanSystemConnectionMemo memo;

    String prefix = "M";

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    /**
     * Internal method to invoke the factory, after all the logic for returning
     * an existing method has been invoked.
     *
     * @return never null
     */
    @Override
    protected Light createNewLight(String systemName, String userName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        // first, check validity
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            throw e;
        }
        try {
            if (Integer.valueOf(addr).intValue() > 0 && !addr.startsWith("+")) {
                // accept unsigned positive integer, prefix "+"
                addr = "+" + addr;
            }
        } catch (NumberFormatException ex) {
            log.debug("Unable to convert " + addr + " into Cbus format +nn");
        }
        Light l = new CbusLight(getSystemPrefix(), addr, memo.getTrafficController());
        l.setUserName(userName);
        return l;
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

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
            unsigned = Integer.valueOf(curAddress).intValue(); // accept unsigned integer, will add "+" next
        } catch (NumberFormatException ex) {
            // already warned
        }
        if (unsigned > 0) {
            curAddress = "+" + curAddress;
        }
        return getSystemPrefix() + typeLetter() + curAddress;
    }

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
            log.debug("Warning: " + e.getMessage());
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
            throw new IllegalArgumentException("Did not find usable hardware address: " + address + " for a valid Cbus light address");
        }
        switch (v.length) {
            case 1:
                int unsigned = 0;
                try {
                    unsigned = Integer.valueOf(address).intValue(); // on unsigned integer, will add "+" upon creation
                } catch (NumberFormatException ex) {
                    log.debug("Unable to convert " + address + " into Cbus format +nn");
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
    public boolean validSystemNameConfig(String systemName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e){
            log.debug("Warning: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddOutputEntryToolTip");
        return entryToolTip;
    }

    private static final Logger log = LoggerFactory.getLogger(CbusLightManager.class);

}
