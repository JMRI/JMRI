package jmri.jmrix.pi;

import javax.annotation.Nonnull;
import jmri.Sensor;
import jmri.jmrix.pi.extendgpio.ExtensionService;

import jmri.JmriException;

/**
 * Manage the RaspberryPi specific Sensor implementation.
 *
 * System names are "PSnnn", where P is the user configurable system prefix,
 * nnn is the sensor number without padding.
 *
 * @author   Paul Bender Copyright (C) 2015
 */
public class RaspberryPiSensorManager extends jmri.managers.AbstractSensorManager {

    // ctor has to register for RaspberryPi events
    public RaspberryPiSensorManager(RaspberryPiSystemConnectionMemo memo) {
        super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public RaspberryPiSystemConnectionMemo getMemo() {
        return (RaspberryPiSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        return new RaspberryPiSensor(systemName, userName);
    }

    /**
     * Do the sensor objects provided by this manager support configuring
     * an internal pullup or pull down resistor?
     * <p>
     * For Raspberry Pi systems, it is possible to set the pullup or 
     * pulldown resistor, so return true.
     *
     * @return true if pull up/pull down configuration is supported.
     */
    @Override
    public boolean isPullResistanceConfigurable(){
       return true;
    }
    
   /**
     * Require address portion of SystemName to either start with ":" or be numberic.
     * {@inheritDoc} 
     */
    @Nonnull
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        if (curAddress.substring(0,1).equals (":")) {
            return prefix + typeLetter() + curAddress;
        } else {
            return prefix + typeLetter() + checkNumeric(curAddress);
        }
    }
    
    /**
     * Validates to either ":xxx..." or Integer Format 0-999 with valid prefix.
     * eg. PT0 to PT999, PT:MCP23017:1:32:0
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        int prefixLen = getSystemNamePrefix().length();
        if (name.length() <= prefixLen) {
            throw new jmri.NamedBean.BadSystemNameException();
        }
        if (name.substring (prefixLen, prefixLen+1).equals (":")) {
            return ExtensionService.validateSystemNameFormat (name);
        } else {
            return this.validateIntegerSystemNameFormat(name, 0, 999, locale);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }
}
