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
        Light l = new CbusLight(getSystemPrefix(), addr, memo.getTrafficController());
        l.setUserName(userName);
        return l;
    }

    public boolean allowMultipleAdditions() {
        return false;
    }

    public String createSystemName(String curAddress, String prefix) throws JmriException {
        try {
            validateSystemNameFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
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

    void validateSystemNameFormat(String address) throws IllegalArgumentException {
        CbusAddress a = new CbusAddress(address);
        CbusAddress[] v = a.split();
        if (v == null) {
            throw new IllegalArgumentException("Did not find usable system name: " + address + " to a valid Cbus light address");
        }
        switch (v.length) {
            case 1:
                if (address.startsWith("+") || address.startsWith("-")) {
                    break;
                }
                throw new IllegalArgumentException("can't make 2nd event from systemname " + address);
            case 2:
                break;
            default:
                throw new IllegalArgumentException("Wrong number of events in address: " + address);
        }
    }

    @Override
    public boolean validSystemNameFormat(String systemName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e){
            log.warn("Error: "+e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean validSystemNameConfig(String systemName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        try {
            validateSystemNameFormat(addr);
        } catch (IllegalArgumentException e){
            log.warn("Error: "+e.getMessage());
            return false;
        }
        return true;
    }

    private static final Logger log = LoggerFactory.getLogger(CbusLightManager.class.getName());

}
