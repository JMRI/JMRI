package jmri.jmrix.can.cbus;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement SensorManager for CAN CBUS systems.
 * <p>
 * System names are "MS+n;-m", where M is the user configurable system prefix,
 * n and m are the events (signed for on/off, separated by ;).
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class CbusSensorManager extends jmri.managers.AbstractSensorManager {

    /**
     * Ctor using a given system connection memo
     */
    public CbusSensorManager(CanSystemConnectionMemo memo) {
        this.memo = memo;
        prefix = memo.getSystemPrefix();
    }

    private CanSystemConnectionMemo memo;
    private String prefix = "M";

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

    // CBUS-specific methods

    /** 
     * {@inheritDoc} 
     */
    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        String addr = systemName.substring(prefix.length() + 1);
        // first, check validity
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            throw e;
        }
        // validate (will add "+" to unsigned int)
        String newAddress = CbusAddress.validateSysName(addr);
        // OK, make
        Sensor s = new CbusSensor(prefix, newAddress, memo.getTrafficController());
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
            validateSystemNameFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        // prefix unsigned int with "+" as service to user
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
            validateSystemNameFormat(testAddr);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        testAddr = CbusAddress.validateSysName(testAddr); // normalize Merg address
        Sensor s = getBySystemName(prefix + typeLetter() + testAddr);
        if (s != null) {
            // build local addresses
            for (int x = 1; x < 10; x++) {
                testAddr = CbusAddress.getIncrement(testAddr); // getIncrement will perform a max check on the numbers
                s = getBySystemName(prefix + typeLetter() + testAddr);
                if (s == null) {
                    // If the hardware address + x does not already exist,
                    // then this can be considered the next valid address.
                    return testAddr;
                }
            }
            // feedback when next address is also in use
            log.warn("10 hardware addresses starting at {} already in use. No new {} Sensors added", curAddress, memo.getUserName());
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
    public NameValidity validSystemNameFormat(String systemName) {
        String addr;
        try {
            addr = systemName.substring(prefix.length() + 1); // get only the address part
        } catch (StringIndexOutOfBoundsException e){
            return NameValidity.INVALID;
        }
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e){
            return NameValidity.INVALID;
        }
        return NameValidity.VALID;
    }

    /**
     * Work out the details for Cbus hardware address validation.
     * Logging of handled cases no higher than WARN.
     *
     * @param address the hardware address to check
     * @throws IllegalArgumentException when delimiter is not found
     */
    void validateSystemNameFormat(String address) throws IllegalArgumentException {
        String newAddress = CbusAddress.validateSysName(address);
        log.debug("validated system name {}", newAddress);
    }

    /**
     * {@inheritDoc}
     *
     * Forces upper case and trims leading and trailing whitespace, adding +/- if not present.
     * Does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException.
     */
    @CheckReturnValue
    @Override
    public @Nonnull
    String normalizeSystemName(@Nonnull String inputName) {
        String address = inputName.toUpperCase().trim();
        // check Cbus hardware address parts
        if ((!address.startsWith(prefix + typeLetter()) || (address.length() < prefix.length() + 2))) {
            return address;
        }
        try {
            address = CbusAddress.validateSysName(address.substring(prefix.length() + 1));
        } catch (IllegalArgumentException e) {
            return address;
        } catch (StringIndexOutOfBoundsException e) {
            return address;
        }
        return prefix + typeLetter() + address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    /**
     * {@inheritDoc}
     * Send a query message to each sensor using the active address
     * eg. for a CBUS address "-7;+5", the query will go to event 7.
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
