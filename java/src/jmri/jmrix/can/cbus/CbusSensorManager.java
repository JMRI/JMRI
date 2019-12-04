package jmri.jmrix.can.cbus;

import java.util.Locale;
import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement SensorManager for CAN CBUS systems.
 * <p>
 * System names are "MS+n;-m", where M is the user configurable system prefix, n
 * and m are the events (signed for on/off, separated by ;).
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class CbusSensorManager extends jmri.managers.AbstractSensorManager {

    /**
     * Ctor using a given system connection memo
     */
    public CbusSensorManager(CanSystemConnectionMemo memo) {
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
    public void dispose() {
        super.dispose();
    }

    // CBUS-specific methods
    /**
     * {@inheritDoc}
     */
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        // first, check validity
        try {
            validateAddressFormat(addr);
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            throw e;
        }
        // validate (will add "+" to unsigned int)
        String newAddress = CbusAddress.validateSysName(addr);
        // OK, make
        Sensor s = new CbusSensor(getSystemPrefix(), newAddress, getMemo().getTrafficController());
        s.setUserName(userName);
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        // first, check validity
        try {
            validateAddressFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        // getSystemPrefix() unsigned int with "+" as service to user
        String newAddress = CbusAddress.validateSysName(curAddress);
        return prefix + typeLetter() + newAddress;
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
    public String getNextValidAddress(String curAddress, String prefix) throws JmriException {
        String testAddr = curAddress;
        // make sure starting name is valid
        try {
            validateAddressFormat(testAddr);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        testAddr = CbusAddress.validateSysName(testAddr); // normalize Merg address
        Sensor s = getBySystemName(prefix + typeLetter() + testAddr);
        if (s != null) {
            // build local addresses
            for (int x = 1; x < 10; x++) {
                testAddr = CbusAddress.getIncrement(testAddr); // getIncrement will perform a max check on the numbers
                s = getBySystemName(getSystemPrefix() + typeLetter() + testAddr);
                if (s == null) {
                    // If the hardware address + x does not already exist,
                    // then this can be considered the next valid address.
                    return testAddr;
                }
            }
            // feedback when next address is also in use
            log.warn("10 hardware addresses starting at {} already in use. No new {} Sensors added", curAddress, getMemo().getUserName());
            return null;
        } else {
            // If the initially requested hardware address does not already exist,
            // then this can be considered the next valid address.
            return testAddr;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        validateSystemNamePrefix(name, locale);
        try {
            validateAddressFormat(name.substring(getSystemNamePrefix().length()));
        } catch (IllegalArgumentException ex) {
            throw new jmri.NamedBean.BadSystemNameException(locale, "InvalidSystemNameCBUS", ex.getMessage());
        }
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        String addr;
        try {
            addr = systemName.substring(getSystemPrefix().length() + 1); // get only the address part
        } catch (StringIndexOutOfBoundsException e) {
            return NameValidity.INVALID;
        }
        try {
            validateAddressFormat(addr);
        } catch (IllegalArgumentException e) {
            return NameValidity.INVALID;
        }
        return NameValidity.VALID;
    }

    /**
     * Work out the details for Cbus hardware address validation. Logging of
     * handled cases no higher than WARN.
     *
     * @param address the hardware address to check
     * @throws IllegalArgumentException when delimiter is not found
     */
    void validateAddressFormat(String address) throws IllegalArgumentException {
        String newAddress = CbusAddress.validateSysName(address);
        log.debug("validated system name {}", newAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    /**
     * {@inheritDoc} Send a query message to each sensor using the active
     * address eg. for a CBUS address "-7;+5", the query will go to event 7.
     */
    @Override
    public void updateAll() {
        log.info("Requesting status for all sensors");
        getNamedBeanSet().forEach((nb) -> {
            if (nb instanceof CbusSensor) {
                nb.requestUpdateFromLayout();
            }
        });
    }

    private final static Logger log = LoggerFactory.getLogger(CbusSensorManager.class);

}
