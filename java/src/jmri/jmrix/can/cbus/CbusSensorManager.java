package jmri.jmrix.can.cbus;

import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the CBUS-specific Sensor implementation.
 * <p>
 * System names are "MSnnn", where M is the user-configurable system prefix,
 * nnn is the sensor number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class CbusSensorManager extends jmri.managers.AbstractSensorManager {

    /** 
     * {@inheritDoc} 
     */
    @Override
    public String getSystemPrefix() {
        return memo.getSystemPrefix();
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    //Implemented ready for new system connection memo
    public CbusSensorManager(CanSystemConnectionMemo memo) {
        this.memo = memo;
    }

    CanSystemConnectionMemo memo;

    // CBUS-specific methods

    /** 
     * {@inheritDoc} 
     */
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
        // validate and add + to int
        String newAddress = CbusAddress.validateSysName(addr);
        // OK, make
        Sensor s = new CbusSensor(getSystemPrefix(), newAddress, memo.getTrafficController());
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
        // prefix + as service to user
        String newAddress = CbusAddress.validateSysName(curAddress);
        return getSystemPrefix() + typeLetter() + newAddress;
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
    public String getNextValidAddress(String curAddress, String prefix) {
        String testAddr = curAddress;
        // make sure starting name is valid
        try {
            validateSystemNameFormat(testAddr);
        } catch (IllegalArgumentException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false);
            return null;
        }
        
       // log.warn("prefix {} typeLetter() {} testAddr {}",prefix ,typeLetter() , testAddr); // M, S,+123
        
        //If the hardware address passed does not already exist then this can
        //be considered the next valid address.
        Sensor s = getBySystemName(prefix + typeLetter() + testAddr);
        if (s == null) {
            return testAddr;
        }

        // getIncrement will have performed a max check on the numbers
        String newaddr = CbusAddress.getIncrement(testAddr);
        if (newaddr==null) {
            return null;
        }
        //If the new hardware address does not already exist then this can
        //be considered the next valid address.
        Sensor snew = getBySystemName(prefix + typeLetter() + newaddr);
        if (snew == null) {
            return newaddr;
        }
        return null;
    }

    /** 
     * {@inheritDoc} 
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        String addr;
        try {
            addr = systemName.substring(getSystemPrefix().length() + 1); // get only the address part
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
     * Work out the details for Cbus hardware address validation
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
     */
    @Override
    public String getEntryToolTip() {
        String entryToolTip = Bundle.getMessage("AddInputEntryToolTip");
        return entryToolTip;
    }

    /**
     * {@inheritDoc}
     * Send a query message to each sensor using the active address
     * eg. for a CBUS address "-7;+5", the query will got to event 7.
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
