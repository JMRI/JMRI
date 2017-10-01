package jmri.jmrix.can.cbus;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the CBUS-specific Sensor implementation.
 *
 * System names are "MSnnn", where nnn is the sensor number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class CbusSensorManager extends jmri.managers.AbstractSensorManager implements CanListener {

    @Override
    public String getSystemPrefix() {
        return memo.getSystemPrefix();
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        memo.getTrafficController().removeCanListener(this);
        super.dispose();
    }

    //Implemented ready for new system connection memo
    public CbusSensorManager(CanSystemConnectionMemo memo) {
        this.memo = memo;
        memo.getTrafficController().addCanListener(this);
    }

    CanSystemConnectionMemo memo;

    // CBUS-specific methods

    @Override
    public Sensor createNewSensor(String systemName, String userName) {
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
            log.debug("Unable to convert {} into Cbus format +nn", addr);
        }

        // OK, make
        Sensor s = new CbusSensor(getSystemPrefix(), addr, memo.getTrafficController());
        s.setUserName(userName);
        return s;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws jmri.JmriException {
        // first, check validity
        try {
            validateSystemNameFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        // prefix + as service to user
        int unsigned = 0;
        try {
            unsigned = Integer.valueOf(curAddress).intValue(); // on unsigned integer, will add "+" next
        } catch (NumberFormatException ex) {
            // already warned
        }
        if (unsigned > 0) {
            curAddress = "+" + curAddress;
        }
        return getSystemPrefix() + typeLetter() + curAddress;
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {
        // always return this (the current) name without change
        try {
            validateSystemNameFormat(curAddress);
        } catch (IllegalArgumentException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false);
            return null;
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
            throw new IllegalArgumentException("Did not find usable hardware address: " + address + " for a valid Cbus sensor address");
        }
        switch (v.length) {
            case 1:
                int unsigned = 0;
                try {
                    unsigned = Integer.valueOf(address).intValue(); // accept unsigned integer, will add "+" upon creation
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

    /**
     * Provide a manager-specific tooltip for the Add new item beantable pane.
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddInputEntryToolTip");
        return entryToolTip;
    }

    // listen for sensors, creating them as needed
    @Override
    public void reply(CanReply l) {
        // doesn't do anything, because for now 
        // we want you to create manually
    }

    @Override
    public void message(CanMessage l) {
        // doesn't do anything, because 
        // messages come from us
    }

    /**
     * No mechanism currently exists to request status updates from all layout
     * sensors.
     */
    @Override
    public void updateAll() {
    }

    private final static Logger log = LoggerFactory.getLogger(CbusSensorManager.class);

}
